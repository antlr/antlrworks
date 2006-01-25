package org.antlr.works.components.text;

import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.ATEPanelDelegate;
import org.antlr.works.ate.ATETextPane;
import org.antlr.works.ate.syntax.language.ATELanguageSyntaxEngine;
import org.antlr.works.components.ComponentContainer;
import org.antlr.works.components.ComponentEditor;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.TextUtils;

import javax.swing.*;
import java.awt.*;
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

public class CEditorText extends ComponentEditor implements ATEPanelDelegate {

    protected ATEPanel textEditor;
    protected ATELanguageSyntaxEngine syntaxEngine;

    protected JLabel cursorLabel;

    public CEditorText(ComponentContainer container) {
        super(container);
    }

    public void create() {
        syntaxEngine = createLanguageEngine();

        textEditor = new ATEPanel(getXJFrame());
        textEditor.setParserEngine(syntaxEngine);
        textEditor.setSyntaxColoring(true);
        textEditor.setAnalysisColumnVisible(false);
        textEditor.setDelegate(this);
        applyPrefs();

        cursorLabel = new JLabel();

        statusBar.add(Box.createHorizontalStrut(5));
        statusBar.add(cursorLabel);
        statusBar.add(Box.createHorizontalStrut(5));
        statusBar.add(createSeparator());

        mainPanel.add(textEditor, BorderLayout.CENTER);
    }

    public ATELanguageSyntaxEngine createLanguageEngine() {
        return new ATELanguageSyntaxEngine();
    }

    public void applyPrefs() {
        textEditor.setFoldingEnabled(AWPrefs.getFoldingEnabled());
        textEditor.setHighlightCursorLine(AWPrefs.getHighlightCursorEnabled());
        textEditor.refresh();
        applyFont();
    }

    public void applyFont() {
        getTextPane().setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        TextUtils.createTabs(getTextPane());
    }

    public void loadText(String text) {
        textEditor.loadText(text);
    }

    public String getText() {
        return getTextPane().getText();
    }

    public void close() {
        super.close();
    }

    public void notificationPrefsChanged() {
        applyPrefs();
    }

    public void componentDocumentContentChanged() {
        int oldCursorPosition = getTextPane().getCaretPosition();
        getDocument().reload();
        getTextPane().setCaretPosition(oldCursorPosition);
    }

    public void componentIsSelected() {
        getTextPane().requestFocus();
    }
    
    public ATETextPane getTextPane() {
        return textEditor.getTextPane();
    }

    public void ateCaretUpdate(int index) {
        cursorLabel.setText(textEditor.getCurrentLinePosition()+":"+textEditor.getCurrentColumnPosition());
    }

    public void ateChangeUpdate(int offset, int length, boolean insert) {
        container.setDirty();
    }

    public void ateAutoIndent(int offset, int length) {        
    }

    public void ateMousePressed(Point point) {
    }

    public void ateMouseExited() {
    }

    public void ateMouseMoved(Point relativePoint) {
    }

    public void ateInvokePopUp(Component component, int x, int y) {
    }

    public void ateParserWillParse() {
    }

    public void ateParserDidParse() {
    }
}
