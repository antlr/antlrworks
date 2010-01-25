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

package org.antlr.works.visualization;

import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.GrammarWindow;
import org.antlr.works.editor.GrammarWindowTab;
import org.antlr.works.grammar.antlr.ANTLRGrammarEngine;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.grammar.syntax.GrammarSyntaxEngine;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.Console;
import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.GContextProvider;
import org.antlr.works.visualization.graphics.GEngineGraphics;
import org.antlr.works.visualization.graphics.graph.GGraphAbstract;
import org.antlr.works.visualization.graphics.panel.GPanel;
import org.antlr.works.visualization.skin.Skin;
import org.antlr.works.visualization.skin.syntaxdiagram.SDSkin;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.utils.XJFileChooser;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SyntaxDiagramTab extends GrammarWindowTab implements GContextProvider {

    protected VisualDrawing drawing;

    protected GContext context;
    protected Skin skin;

    protected GPanel panel;

    protected boolean enable = true;

    public SyntaxDiagramTab(GrammarWindow editor) {
        super(editor);

        skin = new SDSkin();

        context = new GContext();
        context.setEngine(new GEngineGraphics());
        context.setSkin(skin);
        context.setProvider(this);

        panel = new GPanel(editor, context);

        drawing = new VisualDrawing(this);
    }

    public Console getConsole() {
        return window.getConsoleTab();
    }

    public void close() {
        panel.close();
        drawing.stop();
        final long t = System.currentTimeMillis();
        while(drawing.isRunning() && (System.currentTimeMillis() - t) < 5000) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // We don't care if sleep has been interrupted
            }
        }
        context.setProvider(null);
    }

    public void setEnable(boolean flag) {
        this.enable = flag;
    }

    public boolean isEnabled() {
        return enable;
    }

    public void toggleNFAOptimization() {
        drawing.toggleNFAOptimization();
    }

    public Color contextGetColorForLabel(String label) {
        if(label.charAt(0) == '\'' || label.charAt(0) == '"')
            return AWPrefs.getSyntaxColor(AWPrefs.PREF_SYNTAX_STRING);
        else {
            if(ATEToken.isLexerName(label))
                return GrammarSyntaxEngine.COLOR_LEXER;
            else
                return GrammarSyntaxEngine.COLOR_PARSER;
        }
    }

    public void setText(String text, String filename) {
        if(isEnabled())
            drawing.setText(text);
    }

    public void setRule(ElementRule rule, boolean immediate) {
        if(isEnabled())
            drawing.setRule(rule, immediate);
    }

    public void setPlaceholder(String placeholder) {
        panel.setPlaceholder(placeholder);
        panel.setGraphs(null);
    }

    public void cancelDrawingProcess() {
        drawing.skip();
    }

    public void clearCacheGraphs() {
        drawing.clearCacheGraphs();
    }

    public void createGraphsForRule(ElementRule rule) throws Exception {
        drawing.createGraphsForRule(rule);
    }

    public boolean update() {
        panel.createPanel();
        return drawing.refresh();
    }
    
    public ANTLRGrammarEngine getEngineGrammar() {
        return window.getGrammarEngine().getRootEngine().getANTLRGrammarEngine();
    }

    public Container getContainer() {
        return panel.getContainer();
    }

    public GGraphAbstract getCurrentGraph() {
        return panel.getCurrentGraph();
    }

    public boolean canSaveImage() {
        return getImage() != null;
    }

    public BufferedImage getImage() {
        return panel.getImageOfView();
    }

    public boolean canExportToBitmap() {
        return true;
    }

    public boolean canExportToEPS() {
        return true;
    }

    public String getTabName() {
        return "Syntax Diagram";
    }

    public Component getTabComponent() {
        return getContainer();
    }

    public void serializeSyntaxDiagram() {
        XJFileChooser fc = XJFileChooser.shared();
        if(fc.displaySaveDialog(window.getJavaContainer(), "txt", "XML representation", false)) {
            String[] args = new String[] { "-f", window.getFilePath(),
                    "-serialize", fc.getSelectedFilePath(), "-verbose"};
            try {
                org.antlr.works.Console.main(args);
            } catch (Exception e) {
                e.printStackTrace();
                XJAlert.display(window.getJavaContainer(), "Serialize Syntax Diagram", e.toString());
            }
        }
    }
}
