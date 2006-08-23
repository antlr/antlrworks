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

import org.antlr.works.ate.swing.ATECustomEditorKit;
import org.antlr.works.ate.swing.ATERenderingView;
import org.antlr.works.ate.swing.ATEStyledDocument;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseEvent;

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
        setEditorKit(new ATECustomEditorKit(this, textEditor));
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

    /** Override setFont() to apply the font to the coloring view
     *
     * @param f The font
     */
    public void setFont(Font f) {
        super.setFont(f);
        ATERenderingView.DEFAULT_FONT = f;
    }

    public boolean getScrollableTracksViewportWidth() {
        if (!wrap) {
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
