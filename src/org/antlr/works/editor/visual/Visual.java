package org.antlr.works.editor.visual;

import edu.usfca.xj.appkit.frame.XJFrame;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.utils.XJFileChooser;
import org.antlr.tool.DOTGenerator;
import org.antlr.works.parser.Parser;
import org.antlr.works.parser.ThreadedParser;
import org.antlr.works.util.DotGenerator;
import org.antlr.works.util.Statistics;
import org.antlr.works.visualization.fa.FAFactory;
import org.antlr.works.visualization.fa.FAState;
import org.antlr.works.visualization.grammar.GrammarEngine;
import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.GEngineGraphics;
import org.antlr.works.visualization.graphics.GRenderer;
import org.antlr.works.visualization.graphics.panel.GPanel;
import org.antlr.works.visualization.skin.Skin;
import org.antlr.works.visualization.skin.syntaxdiagram.SDSkin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*

[The "BSD licence"]
Copyright (c) 2004 Jean Bovet
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

public class Visual {

    protected XJFrame parent;

    protected VisualDrawing drawing;
    protected VisualAnalysis analysis;

    protected VisualDelegate delegate;

    protected ThreadedParser parser;

    protected GRenderer renderer;
    protected GContext context;
    protected Skin skin;

    protected GrammarEngine engine;

    protected GPanel panel;

    protected boolean enable = true;

    public Visual(XJFrame parent) {
        this.parent = parent;

        renderer = new GRenderer();

        skin = new SDSkin();

        context = new GContext();
        context.setEngine(new GEngineGraphics());
        context.setSkin(skin);

        engine = new GrammarEngine();

        panel = new GPanel(context);
        panel.setParent(parent);

        drawing = new VisualDrawing(this);
        analysis = new VisualAnalysis(this);
    }

    public void close() {
        panel.close();
        drawing.stop();
        analysis.stop();
        while(drawing.isRunning() || analysis.isRunning()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // We don't care if sleep has been interrupted
            }
        }
    }

    public void setEnable(boolean flag) {
        this.enable = flag;
    }

    public boolean isEnable() {
        return enable;
    }

    public void toggleNFAOptimization() {
        drawing.toggleNFAOptimization();
    }

    public void setDelegate(VisualDelegate delegate) {
        this.delegate = delegate;
    }

    public void setParser(ThreadedParser parser) {
        this.parser = parser;
    }

    public void setText(String text) {
        if(isEnable())
            drawing.setText(text);
    }

    public void setRule(Parser.Rule rule, boolean immediate) {
        if(isEnable())
            drawing.setRule(rule, immediate);
    }

    public void checkGrammar() {
        analysis.startAnalysis();
    }

    public void cancelDrawingProcess() {
        drawing.skip();
    }

    public Container getContainer() {
        return panel.getContainer();
    }

    public String chooseDOTFile() {
        if(XJFileChooser.shared().displaySaveDialog(parent.getJavaContainer(), "DOT", "DOT file", false) == false)
            return null;

        return XJFileChooser.shared().getSelectedFilePath();
    }

    public void saveANTLRNFA2DOT(Parser.Rule rule) {
        String dotFile = chooseDOTFile();
        if(dotFile == null)
            return;

        DOTGenerator dotgen = new DOTGenerator(engine.g);
        String dot = dotgen.getDOT(engine.g.getRuleStartState(rule.name));
        try {
            FileWriter writer = new FileWriter(dotFile);
            writer.write(dot);
            writer.close();
        } catch (IOException e) {
            XJAlert.display(parent.getJavaContainer(), "Error", "Cannot save DOT file: "+dotFile+"\nError: "+e);
        }

        Statistics.shared().recordEvent(Statistics.EVENT_EXPORT_ANTLRNFA_DOT);
    }

    public void saveOptimizedNFA2DOT(Parser.Rule rule) {
        String dotFile = chooseDOTFile();
        if(dotFile == null)
            return;

        FAState state = new FAFactory(engine.g).buildNFA(engine.g.getRuleStartState(rule.name), true);
        if(state == null)
            return;

        DotGenerator jdot = new DotGenerator(state);
        try {
            jdot.writeToFile(dotFile);
        } catch (Exception e) {
            XJAlert.display(parent.getJavaContainer(), "Error", "Cannot save DOT file: "+dotFile+"\nError: "+e);
        }

        Statistics.shared().recordEvent(Statistics.EVENT_EXPORT_OPTIMIZEDNFA_DOT);
    }

    public void saveRawNFA2DOT(Parser.Rule rule) {
        String dotFile = chooseDOTFile();
        if(dotFile == null)
            return;

        FAState state = new FAFactory(engine.g).buildNFA(engine.g.getRuleStartState(rule.name), false);
        if(state == null)
            return;

        DotGenerator jdot = new DotGenerator(state);
        try {
            jdot.writeToFile(dotFile);
        } catch (Exception e) {
            XJAlert.display(parent.getJavaContainer(), "Error", "Cannot save DOT file: "+dotFile+"\nError: "+e);
        }

        Statistics.shared().recordEvent(Statistics.EVENT_EXPORT_RAWNFA_DOT);
    }

    public boolean canSaveImage() {
        return panel.getImageOfView() != null;
    }

    public void saveAsImage() {
        if(!canSaveImage()) {
            XJAlert.display(parent.getJavaContainer(), "Error", "Cannot save rule as image because there is no rule selected.");
            return;
        }

        java.util.List extensions = new ArrayList();
        for (int i = 0; i < ImageIO.getWriterFormatNames().length; i++) {
            String ext = ImageIO.getWriterFormatNames()[i].toLowerCase();
            if(!extensions.contains(ext))
                extensions.add(ext);
        }

        if(XJFileChooser.shared().displaySaveDialog(parent.getJavaContainer(), extensions, extensions, false)) {
            String file = XJFileChooser.shared().getSelectedFilePath();
            try {
                ImageIO.write(panel.getImageOfView(), file.substring(file.lastIndexOf(".")+1), new File(file));
            } catch (IOException e) {
                XJAlert.display(parent.getJavaContainer(), "Error", "Image \""+file+"\" cannot be saved because:\n"+e);
            }
            Statistics.shared().recordEvent(Statistics.EVENT_EXPORT_RULE_IMAGE);
        }
    }

}
