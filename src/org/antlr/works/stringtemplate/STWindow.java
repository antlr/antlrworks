package org.antlr.works.stringtemplate;

import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.ATEPanelAdapter;
import org.antlr.works.ate.syntax.java.ATEJavaSyntaxEngine;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.xjlib.appkit.frame.XJWindow;

import javax.swing.*;
import java.awt.*;/*

[The "BSD licence"]
Copyright (c) 2009 Jean Bovet
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

public class STWindow extends XJWindow {

    private final STRulePanel rulePanel = new STRulePanel();
    private final ATEPanel textPanel = new ATEPanel(this);

    @Override
    public void awake() {
        super.awake();

        prepareTextPanel();
        
        final JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(rulePanel, BorderLayout.WEST);
        contentPanel.add(textPanel, BorderLayout.CENTER);
        getContentPane().add(contentPanel);
        pack();
    }

    private void prepareTextPanel() {
        textPanel.setSyntaxColoring(true);
        textPanel.setParserEngine(new ATEJavaSyntaxEngine());
        textPanel.setSyntaxColoring(true);
        textPanel.setAnalysisColumnVisible(false);
        textPanel.setFoldingEnabled(AWPrefs.getFoldingEnabled());
        textPanel.setLineNumberEnabled(AWPrefs.getLineNumberEnabled());
        textPanel.setHighlightCursorLine(AWPrefs.getHighlightCursorEnabled());
        textPanel.getTextPane().setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        textPanel.getTextPane().setTabSize(AWPrefs.getEditorTabSize());

        textPanel.setDelegate(new TextPanelDelegate());
    }

    public String getText() {
        return textPanel.getText();
    }

    public void loadText(String text) {
        textPanel.loadText(text);
    }

    private class TextPanelDelegate extends ATEPanelAdapter {

        @Override
        public void ateChangeUpdate(int offset, int length, boolean insert) {
            // Indicate to the document that a change has been done. This will
            // automatically trigger an alert when the window is closed to ask
            // the user if he wants to save the document
            getDocument().changeDone();
        }

    }
}
