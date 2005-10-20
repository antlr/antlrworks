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

package org.antlr.works.editor.textpane;

import org.antlr.works.parser.ParserRule;
import org.antlr.works.editor.EditorWindow;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.View;
import java.awt.*;

public class EditorTextPane extends JTextPane
{
    protected static final String ATTRIBUTE_FOLDING_RULE = "folding_rule";

    protected EditorWindow editor;
    protected boolean wrap = false;
    protected boolean highlightCursorLine = true;
    protected EditorTextPaneDelegate delegate = null;

    public EditorTextPane() {
        super(new EditorStyledDocument());
        setEditorKit(new EditorCustomEditorKit());
    }

    public EditorTextPane(EditorWindow editor) {
        super(new EditorStyledDocument());
        setEditorKit(new EditorCustomEditorKit());
        this.editor = editor;
    }

    public void setWordWrap(boolean flag) {
        this.wrap = flag;
    }

    public boolean getWordWrap() {
        return wrap;
    }

    public void setHighlightCursorLine(boolean highlightCursorLine) {
        this.highlightCursorLine = highlightCursorLine;
    }

    public boolean highlightCursorLine() {
        return highlightCursorLine;
    }

    public ParserRule getRule(View v) {
        Object value = v.getAttributes().getAttribute(EditorTextPane.ATTRIBUTE_FOLDING_RULE);
        if(value != null && value instanceof ParserRule)
            return (ParserRule)value;
        else
            return null;
    }

    public boolean isViewVisible(View v) {
        ParserRule rule = getRule(v);
        if(rule == null)
            return true;
        else
            return !rule.isCollapsed();
    }

    public boolean getScrollableTracksViewportWidth() {
        if (!wrap)
        {
            Component parent = getParent();
            return parent == null || getUI().getPreferredSize(this).width < parent.getSize().width;
        } else
            return super.getScrollableTracksViewportWidth();
    }

    public void setBounds(int x, int y, int width, int height) {
        if (!wrap) {
            Dimension size = this.getPreferredSize();
            super.setBounds(x, y,
                    Math.max(size.width, width), Math.max(size.height, height));
        } else {
            super.setBounds(x, y, width, height);
        }
    }

    public void setDelegate(EditorTextPaneDelegate delegate) {
        this.delegate = delegate;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(delegate != null)
            delegate.editorTextPaneDidPaint(g);
    }

    public void toggleFolding(ParserRule rule) {
        int start = rule.getStartIndex();
        int end = rule.getEndIndex();
        rule.setCollapsed(!rule.isCollapsed());

        SimpleAttributeSet attr = new SimpleAttributeSet();
        attr.addAttribute(ATTRIBUTE_FOLDING_RULE, rule);
        if(rule.isCollapsed())
            editor.beginTextPaneUndoGroup("Collapse Rule");
        else
            editor.beginTextPaneUndoGroup("Expand Rule");
        ((EditorStyledDocument)getDocument()).setCharacterAttributes(start, end-start, attr, false);
        ((EditorStyledDocument)getDocument()).setParagraphAttributes(start, end-start, attr, false);
        editor.endTextPaneUndoGroup();

        if(delegate != null)
            delegate.editorTextPaneDidFold();
    }

}
