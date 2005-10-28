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

import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.textpane.folding.Entity;
import org.antlr.works.editor.textpane.folding.EntityProxy;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.View;
import java.awt.*;

public class EditorTextPane extends JTextPane
{
    protected static final String ATTRIBUTE_CHARACTER_FOLDING_PROXY = "char_folding_proxy";
    protected static final String ATTRIBUTE_PARAGRAPH_FOLDING_PROXY = "para_folding_proxy";

    protected EditorWindow editor;
    protected boolean wrap = false;
    protected boolean highlightCursorLine = false;
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

    public Entity getEntity(View v) {
        Object value = null;
        if(v instanceof EditorLabelView)
            value = v.getAttributes().getAttribute(EditorTextPane.ATTRIBUTE_CHARACTER_FOLDING_PROXY);
        else if(v instanceof EditorParagraphView)
            value = v.getAttributes().getAttribute(EditorTextPane.ATTRIBUTE_PARAGRAPH_FOLDING_PROXY);
        if(value != null && value instanceof EntityProxy) {
            EntityProxy proxy =  (EntityProxy)value;
            return proxy.getEntity();
        } else
            return null;
    }

    public boolean isViewVisible(View v) {
        Entity entity = getEntity(v);
        if(entity == null)
            return true;
        else
            return entity.foldingEntityIsExpanded();
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

    public void toggleFolding(EntityProxy proxy) {
        Entity fo = proxy.getEntity();
        int start = fo.foldingEntityGetStartIndex();
        int startPara = fo.foldingEntityGetStartParagraphIndex();
        int end = fo.foldingEntityGetEndIndex();
        fo.foldingEntitySetExpanded(!fo.foldingEntityIsExpanded());

        SimpleAttributeSet paraAttr = new SimpleAttributeSet();
        paraAttr.addAttribute(ATTRIBUTE_PARAGRAPH_FOLDING_PROXY, proxy);

        SimpleAttributeSet charAttr = new SimpleAttributeSet();
        charAttr.addAttribute(ATTRIBUTE_CHARACTER_FOLDING_PROXY, proxy);

        editor.disableTextPaneUndo();
        ((EditorStyledDocument)getDocument()).setParagraphAttributes(startPara, end-startPara, paraAttr, false);
        ((EditorStyledDocument)getDocument()).setCharacterAttributes(start, end-start, charAttr, false);
        editor.enableTextPaneUndo();

        // Make sure to move the caret out of the collapsed zone
        if(!proxy.getEntity().foldingEntityIsExpanded()
                && getCaretPosition() >= start && getCaretPosition() <= end)
            setCaretPosition(start-1);

        if(delegate != null)
            delegate.editorTextPaneDidFold();
    }

}
