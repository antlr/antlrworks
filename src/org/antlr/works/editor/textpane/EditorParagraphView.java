package org.antlr.works.editor.textpane;

import org.antlr.works.parser.Parser;

import javax.swing.*;
import javax.swing.text.*;
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

public class EditorParagraphView extends ParagraphView {

    public Rectangle tempRect = new Rectangle();
    public Color highlightColor = new Color(1.0f, 1.0f, 0.5f, 0.3f);

    public EditorParagraphView(Element elem) {
        super(elem);
    }

    public EditorTextPane getEditorPane() {
        return (EditorTextPane)getContainer();
    }

    public boolean isVisible() {
        return getEditorPane().isViewVisible(this);
    }

    public int getNextVisualPositionFrom(int pos,
                                         Position.Bias b,
                                         Shape a,
                                         int direction,
                                         Position.Bias[] biasRet)
                          throws BadLocationException
    {
        Object value = getAttributes().getAttribute("custom");
        if(!isVisible()) {
            if(direction == SwingConstants.SOUTH) {
                Parser.Rule r = (Parser.Rule)value;
                return r.getEndIndex()+1;
            }
            if(direction == SwingConstants.NORTH) {
                Parser.Rule r = (Parser.Rule)value;
                return r.getStartIndex()-1;
            }
        }

        return super.getNextEastWestVisualPositionFrom(pos, b, a, direction, biasRet);
    }

    public void paint(Graphics g, Shape allocation) {
        if(getEditorPane().highlightCursorLine()) {
            Rectangle alloc = (allocation instanceof Rectangle) ?
                    (Rectangle)allocation :
                    allocation.getBounds();
            int n = getViewCount();
            int x = alloc.x + getLeftInset();
            int y = alloc.y + getTopInset();
            Rectangle clip = g.getClipBounds();
            int cursorPosition = getEditorPane().getCaretPosition()+1;
            for (int i = 0; i < n; i++) {
                tempRect.x = x + getOffset(X_AXIS, i);
                tempRect.y = y + getOffset(Y_AXIS, i);
                tempRect.width = getSpan(X_AXIS, i);
                tempRect.height = getSpan(Y_AXIS, i);
                if (tempRect.intersects(clip)) {
                    View v = getView(i);

                    if (v.getStartOffset() < cursorPosition &&
                            cursorPosition <= v.getEndOffset())
                    {
                        g.setColor(highlightColor);
                        g.fillRect(tempRect.x, tempRect.y,
                                alloc.width, tempRect.height);
                    }
                    paintChild(g, tempRect, i);
                }
            }
        } else {
            super.paint(g, allocation);
        }
    }
}
