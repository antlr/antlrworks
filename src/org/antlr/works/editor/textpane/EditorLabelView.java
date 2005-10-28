package org.antlr.works.editor.textpane;

import org.antlr.works.editor.textpane.folding.Entity;

import javax.swing.text.Element;
import javax.swing.text.LabelView;
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

public class EditorLabelView extends LabelView {

    public final Color foldedColor = new Color(0.8f, 0.8f, 0.8f, 0.4f);

    public EditorLabelView(Element elem) {
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

    public float getInvisibleSpan(int axis) {
        if(getStartOffset() == getEntity().foldingEntityGetStartIndex()) {
            // This view is the first paragraph view for the collapsed rule.
            // We adjust its size to display the placeholder.
            if(axis == X_AXIS)
                return 100;
            else
                // @todo replace by the real height of the font
                return super.getPreferredSpan(axis);
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
            super.paint(g, allocation);
        } else {
            if(getStartOffset() == getEntity().foldingEntityGetStartIndex()) {
                // Draw the placeholder only in the first rule paragraph. A rule
                // may have multiple paragraphs view ;-)

                Rectangle alloc = (allocation instanceof Rectangle) ?
                        (Rectangle)allocation :
                        allocation.getBounds();

                FontMetrics fm = g.getFontMetrics();
                String placeholder = getEntity().getFoldedPlaceholderString();

                int x = alloc.x ;
                int y = alloc.y + fm.getHeight() - fm.getDescent();

                g.setColor(foldedColor);
                g.fillRect(x, alloc.y, fm.stringWidth(placeholder), alloc.height);

                g.setColor(Color.lightGray);
                g.drawString(placeholder, x, y);
            }
        }

        /*if(getEditorPane().isViewVisible(this))
            g.setColor(Color.black);
        else
            g.setColor(Color.red);
        g.drawRect(alloc.x+1, alloc.y+1, alloc.width-2, alloc.height-2);*/
    }

}
