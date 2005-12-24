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

package org.antlr.works.ate;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ATETextPane extends JTextPane
{
    private static final String ATTRIBUTE_CHARACTER_FOLDING_PROXY = "char_folding_proxy";
    private static final String ATTRIBUTE_PARAGRAPH_FOLDING_PROXY = "para_folding_proxy";

    protected ATEPanel textEditor;
    protected boolean wrap = false;
    protected boolean highlightCursorLine = false;

    public ATETextPane(ATEPanel textEditor) {
        super(new ATEStyledDocument());
        setCaret(new ATECaret());
        setEditorKit(new ATECustomEditorKit());
        this.textEditor = textEditor;
    }

    public void setWordWrap(boolean flag) {
        this.wrap = flag;
    }

    public boolean getWordWrap() {
        return wrap;
    }

    public void setHighlightCursorLine(boolean flag) {
        this.highlightCursorLine = flag;
    }

    public boolean highlightCursorLine() {
        return highlightCursorLine;
    }

    public List getAttributeFromElement(Element e, String key) {
        List attributes = new ArrayList();
        Object o;
        for(int level=0; level<=1; level++) {
            o = e.getAttributes().getAttribute(key+level);
            if(o instanceof ATEFoldingEntityProxy)
                attributes.add(o);
        }
        return attributes;
    }

    public ATEFoldingEntityProxy getTopLevelEntityProxy(Element e) {
        List attributes = getAttributeFromElement(e, ATTRIBUTE_CHARACTER_FOLDING_PROXY);
        if(attributes.isEmpty())
            return null;
        else
            return (ATEFoldingEntityProxy)attributes.get(attributes.size()-1);
    }

    public String getKeyForView(View v) {
        if(v instanceof ATELabelView)
            return ATTRIBUTE_CHARACTER_FOLDING_PROXY;
        else if(v instanceof ATEParagraphView)
            return ATTRIBUTE_PARAGRAPH_FOLDING_PROXY;
        return null;
    }

    public ATEFoldingEntity getEntity(View v) {
        List attributes = getAttributeFromElement(v.getElement(), getKeyForView(v));
        if(!attributes.isEmpty()) {
            ATEFoldingEntityProxy proxy = (ATEFoldingEntityProxy)attributes.get(attributes.size()-1);
            return proxy.getEntity();
        } else
            return null;
    }

    public boolean isTopMostInvisible(View v) {
        List attributes = getAttributeFromElement(v.getElement(), getKeyForView(v));
        boolean hidden = false;
        for(int index=0; index<attributes.size(); index++) {
            ATEFoldingEntityProxy proxy = (ATEFoldingEntityProxy)attributes.get(index);
            if(proxy.getEntity() == null)
                continue;

            if(index == attributes.size()-1)
                return !hidden;

            if(!proxy.getEntity().foldingEntityIsExpanded())
                hidden = true;
        }
        return false;
    }

    public boolean isViewVisible(View v) {
        List attributes = getAttributeFromElement(v.getElement(), getKeyForView(v));
        for(int index=0; index<attributes.size(); index++) {
            ATEFoldingEntityProxy proxy =  (ATEFoldingEntityProxy)attributes.get(index);
            if(proxy.getEntity() == null)
                continue;

            if(!proxy.getEntity().foldingEntityIsExpanded())
                return false;
        }
        return true;
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

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        textEditor.textPaneDidPaint(g);
    }

    public void toggleFolding(ATEFoldingEntityProxy proxy) {
        if(proxy == null)
            return;

        ATEFoldingEntity entity = proxy.getEntity();
        int start = entity.foldingEntityGetStartIndex();
        int startPara = entity.foldingEntityGetStartParagraphIndex();
        int end = entity.foldingEntityGetEndIndex();
        int level = entity.foldingEntityLevel();
        entity.foldingEntitySetExpanded(!entity.foldingEntityIsExpanded());

        SimpleAttributeSet paraAttr = new SimpleAttributeSet();
        paraAttr.addAttribute(ATTRIBUTE_PARAGRAPH_FOLDING_PROXY+level, proxy);

        SimpleAttributeSet charAttr = new SimpleAttributeSet();
        charAttr.addAttribute(ATTRIBUTE_CHARACTER_FOLDING_PROXY+level, proxy);

        textEditor.foldingManager.textPaneWillFold();

        ((ATEStyledDocument)getDocument()).setParagraphAttributes(startPara, end-startPara, paraAttr, false);
        ((ATEStyledDocument)getDocument()).setCharacterAttributes(start, end-start, charAttr, false);

        // Make sure to move the caret out of the collapsed zone
        if(!proxy.getEntity().foldingEntityIsExpanded()
                && getCaretPosition() >= start && getCaretPosition() <= end)
            setCaretPosition(start-1);

        textEditor.foldingManager.textPaneDidFold();
    }

    protected class ATECaret extends DefaultCaret {

        public ATECaret() {
            setBlinkRate(500);
        }
        
        public void paint(Graphics g) {
            if (!isVisible())
                return;

            try {
                Rectangle r = ATETextPane.this.modelToView(getDot());
                g.setColor(ATETextPane.this.getCaretColor());
                g.drawLine(r.x, r.y, r.x, r.y + r.height - 1);
                g.drawLine(r.x+1, r.y, r.x+1, r.y + r.height - 1);
            }
            catch (BadLocationException e) {
                // ignore
            }
        }

        protected synchronized void damage(Rectangle r) {
            if (r == null)
                return;

            x = r.x;
            y = r.y;
            width = 2;
            height = r.height;
            repaint();
        }
    }
}
