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

package org.antlr.xjlib.appkit.gview.event;

import org.antlr.xjlib.appkit.gview.GView;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class GEventDragSelection extends GAbstractEvent {

    private boolean selecting = false;
    private Point p1 = null;
    private Point p2 = null;

    public GEventDragSelection(GView view) {
        super(view);
    }

    public void mousePressed(MouseEvent e, Point mousePosition) {
        if(hasExclusiveValue(GEventManager.EXCLUSIVE_DRAG_VALUE))
            return;

        if(e.getClickCount() != 1)
            return;
        
        int mask = InputEvent.BUTTON1_DOWN_MASK;
        if((e.getModifiersEx() & mask) == mask && delegate.eventQueryElementAtPoint(mousePosition) == null) {
            addExclusiveValue(GEventManager.EXCLUSIVE_DRAG_VALUE);
            selecting = true;
            delegate.eventSouldSelectAllElements(false);
            p1 = mousePosition;
        }
    }

    public void mouseReleased(MouseEvent e, Point mousePosition) {
        removeExclusiveValue(GEventManager.EXCLUSIVE_DRAG_VALUE);
        delegate.eventShouldRepaint();
        selecting = false;
        p1 = null;
        p2 = null;
    }

    public void mouseDragged(MouseEvent e, Point mousePosition) {
        if(selecting == false)
            return;

        p2 = mousePosition;
        delegate.eventSelectElementsInRect(p1.x, p1.y, p2.x-p1.x, p2.y-p1.y);
        delegate.eventShouldRepaint();
    }

    public void draw(Graphics g) {
        if(selecting && p1 != null && p2 != null) {
            Graphics2D g2d = (Graphics2D)g;

            int x = Math.min(p1.x, p2.x);
            int y = Math.min(p1.y, p2.y);
            int dx = Math.abs(p2.x-p1.x);
            int dy = Math.abs(p2.y-p1.y);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f ));
            g.setColor(Color.gray);
            g.fillRect(x, y, dx, dy);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f ));
            g.setColor(Color.black);
            g.drawRect(x, y, dx, dy);
        }
    }
}
