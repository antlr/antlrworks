package org.antlr.works.editor.textpane;

import org.antlr.works.editor.textpane.folding.Entity;

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
    public final Color highlightColor = new Color(1.0f, 1.0f, 0.5f, 0.3f);
    public final Color foldedColor = new Color(0.8f, 0.8f, 0.8f, 0.4f);

    public EditorParagraphView(Element elem) {
        super(elem);
    }

    public EditorTextPane getEditorPane() {
        return (EditorTextPane)getContainer();
    }

    public Entity getEntity() {
        return getEditorPane().getEntity(this);
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
        if(!isVisible()) {
            // If the paragraph view is not visible, make sure to jump
            // at the start/end of the rule (because it is collapsed).
            if(direction == SwingConstants.SOUTH)
                return getEntity().foldingEntityGetEndIndex()+1;
            if(direction == SwingConstants.NORTH)
                return getEntity().foldingEntityGetStartIndex()-1;
        }

        return super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
    }

    public float getAlignment(int axis) {
        if(isVisible())
            return super.getAlignment(axis);
        else
    	    return 0.0f;
    }

    public float getInvisibleSpan(int axis) {
        if(getStartOffset() == getEntity().foldingEntityGetStartIndex()) {
            // This view is the first paragraph view for the collapsed rule.
            // We adjust its size to display the placeholder.
            if(axis == X_AXIS)
                return getWidth();
            else
            // @todo replace by the real height of the font
                return 20;
        } else {
            // This view is not the first paragraph view for the collapsed rule.
            // We simply set its size to 0 because we really don't want it to be
            // visible nor to take any space.
            return 0;
        }
    }

    public float getPreferredSpan(int axis) {
        if(isVisible())
            return super.getPreferredSpan(axis);
        else
            return getInvisibleSpan(axis);
    }

    public float getMaximumSpan(int axis) {
        if(isVisible())
            return super.getMaximumSpan(axis);
        else
            return getInvisibleSpan(axis);
    }

    public float getMinimumSpan(int axis) {
        if(isVisible())
            return super.getMinimumSpan(axis);
        else
            return getInvisibleSpan(axis);
    }

    public void paint(Graphics g, Shape allocation) {
        if(isVisible()) {
            // Paragraph visible, see if we should paint the
            // underlying cursor line highlighting
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
        } else {
            Rectangle alloc = (allocation instanceof Rectangle) ?
                    (Rectangle)allocation :
                    allocation.getBounds();

            int x = alloc.x + getLeftInset();
            int y = alloc.y + getTopInset();
            if(getStartOffset() == getEntity().foldingEntityGetStartIndex()) {
                // Draw the placeholder only in the first rule paragraph. A rule
                // may have multiple paragraphs view ;-)

                FontMetrics fm = g.getFontMetrics();
                String leftString = getEntity().getFoldedLeftString();
                String placeholder = getEntity().getFoldedPlaceholderString();
                String rightString = getEntity().getFoldedRightString();

                g.setColor(Color.black);
                g.drawString(leftString, x, y+fm.getHeight());

                g.setColor(Color.lightGray);
                g.drawString(placeholder, x+fm.stringWidth(leftString), y+fm.getHeight());

                g.setColor(foldedColor);
                g.fillRect(x+fm.stringWidth(leftString), y, fm.stringWidth(placeholder), alloc.height);

                g.setColor(Color.black);
                g.drawString(rightString, x+fm.stringWidth(leftString+placeholder), y+fm.getHeight());
            }
        }
    }
}
