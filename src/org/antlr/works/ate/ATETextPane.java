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

import org.antlr.works.ate.folding.ATEFoldingEntity;
import org.antlr.works.ate.folding.ATEFoldingEntityProxy;
import org.antlr.works.ate.swing.ATECustomEditorKit;
import org.antlr.works.ate.swing.ATELabelView;
import org.antlr.works.ate.swing.ATEParagraphView;
import org.antlr.works.ate.swing.ATEStyledDocument;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ATETextPane extends JTextPane
{
    public static final String ATTRIBUTE_CHARACTER_FOLDING_PROXY = "char_folding_proxy";
    public static final String ATTRIBUTE_PARAGRAPH_FOLDING_PROXY = "para_folding_proxy";

    protected ATEPanel textEditor;
    protected boolean wrap = false;
    protected boolean highlightCursorLine = false;

    public ATETextPane(ATEPanel textEditor) {
        super(new ATEStyledDocument());
        setCaret(new ATECaret());
        setEditorKit(new ATECustomEditorKit(this));

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

    /** Override setText() in order to reset the colorization
     *
     */

    public void setText(String text) {
        super.setText(text);
        textEditor.resetColoring();
    }

    /** The method isViewVisible used the same code but inlined
     * for speed improvement (modify it if this one gets modified)
     */

    public List getAttributesFromElement(Element e, String key) {
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
        List attributes = getAttributesFromElement(e, ATTRIBUTE_CHARACTER_FOLDING_PROXY);
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
        List attributes = getAttributesFromElement(v.getElement(), getKeyForView(v));
        if(!attributes.isEmpty()) {
            ATEFoldingEntityProxy proxy = (ATEFoldingEntityProxy)attributes.get(attributes.size()-1);
            return proxy.getEntity();
        } else
            return null;
    }

    public boolean isTopMostInvisible(View v) {
        List attributes = getAttributesFromElement(v.getElement(), getKeyForView(v));
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

    /** This methods is called *very* frequently so the code is duplicated
     * from getAttributesFromElement to speed up
     */

    public boolean isViewVisible(View v) {
        // @todo OPTIMIZATION

        Element e = v.getElement();
        String key = getKeyForView(v);

        for(int level=0; level<=1; level++) {
            Object o = e.getAttributes().getAttribute(key+level);
            if(o instanceof ATEFoldingEntityProxy) {
                ATEFoldingEntityProxy proxy =  (ATEFoldingEntityProxy)o;
                if(proxy.getEntity() == null)
                    continue;

                if(!proxy.getEntity().foldingEntityIsExpanded())
                    return false;
            }
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

    /** This method returns true if the view visibility has changed. This is currently
     * working only for ATELabelView. ATEParagraphView doesn't slow too much the typing
     * so it was not worth optimizing its visibility state. Also, ATEParagraphView is much
     * harder to deal with because it doesn't call this method in increasing order, like
     * ATELabelView, but in any order. We would have to maintain an interval-set to keep
     * track of which interval remain to be asked - use Terence ANTLR interval set in the future?
     */

    public int visibilityChangedStartIndex = -1;
    public int visibilityChangedEndIndex = -1;

    public boolean isViewVisibilityDirty(View view) {
        /** WARNING: for performance reason, ATELabelView is computing the following test
         * inline. Any modification here should be reflected in ATELabelView.isVisible().
         */
        if(visibilityChangedEndIndex-visibilityChangedStartIndex <= 0)
            return false;

        int viewStartOffset = view.getStartOffset();
        int viewEndOffset = view.getEndOffset();

        if(viewStartOffset >= visibilityChangedEndIndex)
            return false;
        if(viewEndOffset <= visibilityChangedStartIndex)
            return false;

        // Reduce the interval of dirty visibility index. We assume
        // that the view are calling this method in order (which is the case now).
        visibilityChangedStartIndex = viewEndOffset;

        return true;
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

        visibilityChangedStartIndex = Math.min(start, startPara);
        visibilityChangedEndIndex = end;

        ((ATEStyledDocument)getDocument()).setParagraphAttributes(startPara, end-startPara, paraAttr, false);
        ((ATEStyledDocument)getDocument()).setCharacterAttributes(start, end-start, charAttr, false);

        // Make sure to move the caret out of the collapsed zone
        if(!proxy.getEntity().foldingEntityIsExpanded()
                && getCaretPosition() >= start && getCaretPosition() <= end)
            setCaretPosition(start-1);

        textEditor.foldingManager.textPaneDidFold();
    }

    protected class ATECaret extends DefaultCaret {

        public boolean selectingWord = false;
        public int selectingWordStart;
        public int selectingWordEnd;

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

        public void mouseClicked(MouseEvent e) {
            // Do not call super if more than one click
            // because it causes the word selection to deselect
            if(e.getClickCount() < 2)
                super.mouseClicked(e);
        }

        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);

            selectingWord = false;

            if (SwingUtilities.isLeftMouseButton(e)) {
                if(e.getClickCount() == 2) {
                    selectWord();
                    selectingWord = true;
                    selectingWordStart = getSelectionStart();
                    selectingWordEnd = getSelectionEnd();
                    e.consume();
                }
            }
        }

        public void mouseDragged(MouseEvent e) {
            if(selectingWord) {
                extendSelectionWord(e);
            } else {
                super.mouseDragged(e);
            }
        }

        public void extendSelectionWord(MouseEvent e) {
            int mouseCharIndex = viewToModel(e.getPoint());

            if(mouseCharIndex > selectingWordEnd) {
                int npos = findNextWordBoundary(mouseCharIndex);
                if(npos > selectingWordEnd)
                    select(selectingWordStart, npos);
            } else if(mouseCharIndex < selectingWordStart) {
                int npos = findPrevWordBoundary(mouseCharIndex);
                if(npos < selectingWordStart)
                    select(Math.max(0, npos), selectingWordEnd);
            } else
                select(selectingWordStart, selectingWordEnd);
        }

        public void selectWord() {
            int p = getCaretPosition();

            setCaretPosition(findPrevWordBoundary(p));
            moveCaretPosition(findNextWordBoundary(p));
        }

        public int findPrevWordBoundary(int pos) {
            int index = pos-1;
            String s = getText();
            while(index >= 0 && isWordChar(s.charAt(index))) {
                index--;
            }
            return index +1;
        }

        public int findNextWordBoundary(int pos) {
            int index = pos;
            String s = getText();
            while(index < s.length() && isWordChar(s.charAt(index))) {
                index++;
            }
            return index;
        }

        public boolean isWordChar(char c) {
            return Character.isLetterOrDigit(c) || c == '_';
        }
    }
}
