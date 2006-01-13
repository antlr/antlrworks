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

package org.antlr.works.generate;

import edu.usfca.xj.appkit.frame.XJFrame;
import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.syntax.java.ATEJavaSyntaxEngine;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.TextUtils;

import java.awt.*;

public class CodeDisplay implements EditorTab {

    protected ATEPanel textEditor;
    protected String title;

    public CodeDisplay(XJFrame parentFrame) {
        textEditor = new ATEPanel(parentFrame);
        textEditor.setParserEngine(new ATEJavaSyntaxEngine());
        textEditor.setSyntaxColoring(true);
        textEditor.setAnalysisColumnVisible(false);
        textEditor.setFoldingEnabled(AWPrefs.getFoldingEnabled());
        textEditor.setHighlightCursorLine(AWPrefs.getHighlightCursorEnabled());
        applyFont();
    }

    public void applyFont() {
        textEditor.getTextPane().setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        TextUtils.createTabs(textEditor.getTextPane());
    }

    public void setText(String text) {
        textEditor.getTextPane().setText(text);
        textEditor.setCaretPosition(0);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Container getContainer() {
        return textEditor;
    }

    public String getTabName() {
        return title;
    }

    public Component getTabComponent() {
        return getContainer();
    }
}
