/*

[The "BSD licence"]
Copyright (c) 2005 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package org.antlr.works.menu;

import org.antlr.works.components.GrammarWindow;
import org.antlr.works.editor.GrammarWindowTab;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.visualization.SDGenerator;
import org.antlr.works.visualization.SyntaxDiagramTab;
import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.GEngine;
import org.antlr.works.visualization.graphics.GEnginePS;
import org.antlr.works.visualization.graphics.graph.GGraphAbstract;
import org.antlr.xjlib.appkit.gview.GView;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.utils.XJFileChooser;
import org.antlr.xjlib.foundation.XJUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExportMenu {

    private final GrammarWindow window;

    public ExportMenu(GrammarWindow window) {
        this.window = window;
    }

    public void exportEventsAsTextFile() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_EXPORT_EVENTS_AS_TEXT);

        if(!XJFileChooser.shared().displaySaveDialog(window.getJavaContainer(), "txt", "Text file", false))
            return;

        String file = XJFileChooser.shared().getSelectedFilePath();
        if(file == null)
            return;

        try {
            FileWriter writer = new FileWriter(file);
            writer.write(window.getDebugMenu().getEventsAsString());
            writer.close();
        } catch (IOException e) {
            XJAlert.display(window.getJavaContainer(), "Error", "Cannot save text file: "+file+"\nError: "+e);
        }
    }

    public void exportAsImage() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_EXPORT_AS_BITMAP);

        GrammarWindowTab tab = window.getSelectedTab();
        if(!tab.canExportToBitmap())
            return;

        if(tab instanceof SyntaxDiagramTab)
            exportRuleAsImage();
        else
            exportGViewAsImage(tab.getExportableGView());
    }

    public void exportAllRulesAsImage() {
        exportAllRules(true);
    }

    public void exportAllRulesAsEPS() {
        exportAllRules(false);
    }

    public void exportAllRules(boolean asImage) {
        List<String> extensions = null;
        if(asImage) {
            extensions = lookupAvailableImageFormat();            
        }
        if(!XJFileChooser.shared().displayChooseDirectory(window.getJavaContainer(), extensions, extensions, !asImage)) {
            return;
        }

        String directory = XJFileChooser.shared().getSelectedFilePath();
        String extension = XJFileChooser.shared().getSelectedFileExtension();

        SDGenerator sd = new SDGenerator(window.getGrammarEngine());
        for(ElementRule rule : window.getRules()) {
            try {
                if(asImage) {
                    sd.renderRuleToBitmapFile(rule.name, extension, XJUtils.concatPath(directory, rule.name+"."+extension));
                } else {
                    sd.renderRuleToEPSFile(rule.name, XJUtils.concatPath(directory, rule.name+".eps"));
                }
            } catch (Exception e) {
                XJAlert.display(window.getJavaContainer(), "Error", "Images cannot be saved because:\n"+e);
            }
        }
    }

    public void exportRuleAsImage() {
        if(!window.syntaxDiagramTab.canSaveImage()) {
            XJAlert.display(window.getJavaContainer(), "Export Rule to Bitmap Image", "There is no rule at cursor position.");
            return;
        }

        saveImageToDisk(window.syntaxDiagramTab.getImage());
    }

    public void exportGViewAsImage(GView view) {
        saveImageToDisk(view.getImage());
    }

    public void saveImageToDisk(BufferedImage image) {
        List<String> extensions = lookupAvailableImageFormat();
        if(XJFileChooser.shared().displaySaveDialog(window.getJavaContainer(), extensions, extensions, false)) {
            String file = XJFileChooser.shared().getSelectedFilePath();
            try {
                ImageIO.write(image, file.substring(file.lastIndexOf(".")+1), new File(file));
            } catch (IOException e) {
                XJAlert.display(window.getJavaContainer(), "Error", "Image \""+file+"\" cannot be saved because:\n"+e);
            }
        }
    }

    private static List<String> lookupAvailableImageFormat() {
        List<String> extensions = new ArrayList<String>();
        for (int i = 0; i < ImageIO.getWriterFormatNames().length; i++) {
            String ext = ImageIO.getWriterFormatNames()[i].toLowerCase();
            if(!extensions.contains(ext))
                extensions.add(ext);
        }
        return extensions;
    }

    public void exportAsEPS() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_EXPORT_AS_EPS);

        GrammarWindowTab tab = window.getSelectedTab();
        if(!tab.canExportToEPS())
            return;

        if(tab instanceof SyntaxDiagramTab)
            exportRuleAsEPS();
        else
            exportGViewAsEPS(tab.getExportableGView());
    }

    protected void exportRuleAsEPS() {
        if(window.editorRules.getEnclosingRuleAtPosition(window.getCaretPosition()) == null) {
            XJAlert.display(window.getJavaContainer(), "Export Rule to EPS", "There is no rule at cursor position.");
            return;
        }

        GGraphAbstract graph = window.syntaxDiagramTab.getCurrentGraph();

        if(graph == null) {
            XJAlert.display(window.getJavaContainer(), "Export Rule to EPS", "There is no graphical visualization.");
            return;
        }

        if(!XJFileChooser.shared().displaySaveDialog(window.getJavaContainer(), "eps", "EPS file", false))
            return;

        String file = XJFileChooser.shared().getSelectedFilePath();
        if(file == null)
            return;

        try {
            GEnginePS engine = new GEnginePS();

            GContext context = graph.getContext();
            GEngine oldEngine = context.engine;
            context.setEngine(engine);
            graph.draw();
            context.setEngine(oldEngine);

            XJUtils.writeStringToFile(engine.getPSText(), file);
        } catch (Exception e) {
            window.consoleTab.println(e);
            XJAlert.display(window.getJavaContainer(), "Error", "Cannot export to EPS file: "+file+"\nError: "+e);
        }
    }

    protected void exportGViewAsEPS(GView view) {
        if(!XJFileChooser.shared().displaySaveDialog(window.getJavaContainer(), "eps", "EPS file", false))
            return;

        String file = XJFileChooser.shared().getSelectedFilePath();
        if(file == null)
            return;

        try {
            XJUtils.writeStringToFile(view.getEPS(), file);
        } catch (Exception e) {
            window.consoleTab.println(e);
            XJAlert.display(window.getJavaContainer(), "Error", "Cannot export to EPS file: "+file+"\nError: "+e);
        }
    }

    public void exportAsDOT() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_EXPORT_AS_DOT);

        GrammarWindowTab tab = window.getSelectedTab();
        if(!tab.canExportToDOT())
            return;

        if(!XJFileChooser.shared().displaySaveDialog(window.getJavaContainer(), "dot", "DOT file", false))
            return;

        String file = XJFileChooser.shared().getSelectedFilePath();
        if(file == null)
            return;

        try {
            XJUtils.writeStringToFile(tab.getDOTString(), file);
        } catch (Exception e) {
            window.consoleTab.println(e);
            XJAlert.display(window.getJavaContainer(), "Error", "Cannot export to DOT file: "+file+"\nError: "+e);
        }
    }

}
