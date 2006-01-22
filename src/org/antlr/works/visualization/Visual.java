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

import edu.usfca.xj.appkit.gview.GView;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.utils.XJFileChooser;
import org.antlr.tool.DOTGenerator;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.grammar.EngineGrammar;
import org.antlr.works.stats.Statistics;
import org.antlr.works.syntax.GrammarSyntaxEngine;
import org.antlr.works.syntax.GrammarSyntaxRule;
import org.antlr.works.utils.Console;
import org.antlr.works.utils.DotGenerator;
import org.antlr.works.visualization.fa.FAFactory;
import org.antlr.works.visualization.fa.FAState;
import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.GEngineGraphics;
import org.antlr.works.visualization.graphics.graph.GGraphAbstract;
import org.antlr.works.visualization.graphics.panel.GPanel;
import org.antlr.works.visualization.skin.Skin;
import org.antlr.works.visualization.skin.syntaxdiagram.SDSkin;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileWriter;

public class Visual implements EditorTab {

    protected CEditorGrammar editor;

    protected VisualDrawing drawing;
    protected VisualAnalysis analysis;

    protected VisualDelegate delegate;

    protected GContext context;
    protected Skin skin;

    protected GPanel panel;

    protected boolean enable = true;

    public Visual(CEditorGrammar editor) {
        this.editor = editor;

        skin = new SDSkin();

        context = new GContext();
        context.setEngine(new GEngineGraphics());
        context.setSkin(skin);

        panel = new GPanel(context);

        drawing = new VisualDrawing(this);
        analysis = new VisualAnalysis(this);
    }

    public GrammarSyntaxEngine getParserEngine() {
        return editor.getParserEngine();
    }

    public Console getConsole() {
        return editor.getConsole();
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

    public void setText(String text, String filename) {
        if(isEnable())
            drawing.setText(text, filename);
    }

    public void setRule(GrammarSyntaxRule rule, boolean immediate) {
        if(isEnable())
            drawing.setRule(rule, immediate);
    }

    public void setPlaceholder(String placeholder) {
        panel.setPlaceholder(placeholder);
        panel.setGraphs(null);
    }

    public void checkGrammar() {
        analysis.startAnalysis();
    }

    public void cancelDrawingProcess() {
        drawing.skip();
    }

    public EngineGrammar getGrammar() {
        return editor.getGrammar();
    }

    public Container getContainer() {
        return panel.getContainer();
    }

    public GGraphAbstract getCurrentGraph() {
        return panel.getCurrentGraph();
    }

    public String chooseDOTFile() {
        if(!XJFileChooser.shared().displaySaveDialog(editor.getJavaContainer(), "DOT", "DOT file", false))
            return null;

        return XJFileChooser.shared().getSelectedFilePath();
    }

    public void saveANTLRNFA2DOT(GrammarSyntaxRule rule) {
        String dotFile = chooseDOTFile();
        if(dotFile == null)
            return;

        try {
            DOTGenerator dotgen = new DOTGenerator(getGrammar().getGrammarForRule(rule.name));
            String dot = dotgen.getDOT(getGrammar().getRuleStartState(rule.name));
            FileWriter writer = new FileWriter(dotFile);
            writer.write(dot);
            writer.close();
        } catch (Exception e) {
            XJAlert.display(editor.getJavaContainer(), "Error", "Cannot save DOT file: "+dotFile+"\nError: "+e);
        }

        Statistics.shared().recordEvent(Statistics.EVENT_EXPORT_ANTLRNFA_DOT);
    }

    public void saveOptimizedNFA2DOT(GrammarSyntaxRule rule) {
        String dotFile = chooseDOTFile();
        if(dotFile == null)
            return;

        try {
            FAState state = new FAFactory(getGrammar().getGrammarForRule(rule.name)).buildNFA(getGrammar().getRuleStartState(rule.name), true);
            if(state == null)
                return;

            DotGenerator jdot = new DotGenerator(state);
            jdot.writeToFile(dotFile);
        } catch (Exception e) {
            XJAlert.display(editor.getJavaContainer(), "Error", "Cannot save DOT file: "+dotFile+"\nError: "+e);
        }

        Statistics.shared().recordEvent(Statistics.EVENT_EXPORT_OPTIMIZEDNFA_DOT);
    }

    public void saveRawNFA2DOT(GrammarSyntaxRule rule) {
        String dotFile = chooseDOTFile();
        if(dotFile == null)
            return;

        try {
            FAState state = new FAFactory(getGrammar().getGrammarForRule(rule.name)).buildNFA(getGrammar().getRuleStartState(rule.name), false);
            if(state == null)
                return;

            DotGenerator jdot = new DotGenerator(state);
            jdot.writeToFile(dotFile);
        } catch (Exception e) {
            XJAlert.display(editor.getJavaContainer(), "Error", "Cannot save DOT file: "+dotFile+"\nError: "+e);
        }

        Statistics.shared().recordEvent(Statistics.EVENT_EXPORT_RAWNFA_DOT);
    }

    public boolean canSaveImage() {
        return panel.getImageOfView() != null;
    }

    public BufferedImage getImage() {
        return panel.getImageOfView();
    }

    public boolean hasExportableGView() {
        return true;
    }

    public GView getExportableGView() {
        return null;
    }

    public String getTabName() {
        return "Syntax Diagram";
    }

    public Component getTabComponent() {
        return getContainer();
    }
}
