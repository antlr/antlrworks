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
import org.antlr.xjlib.appkit.gview.base.Vector2D;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class GEventDragRootElement extends GAbstractEvent {

    private boolean dragging = false;

    public GEventDragRootElement(GView view) {
        super(view);
    }

    public void mousePressed(MouseEvent e, Point mousePosition) {
        if(hasExclusiveValue(GEventManager.EXCLUSIVE_DRAG_VALUE))
            return;

        if(e.getClickCount() != 1)
            return;

        int mask = InputEvent.SHIFT_DOWN_MASK + InputEvent.BUTTON1_DOWN_MASK;
        if((e.getModifiersEx() & mask) == mask && delegate.eventQueryElementAtPoint(mousePosition) == null) {
            addExclusiveValue(GEventManager.EXCLUSIVE_DRAG_VALUE);
            delegate.eventQueryRootElement().beginDrag();
            dragging = true;
        }
    }

    public void mouseReleased(MouseEvent e, Point mousePosition) {
        removeExclusiveValue(GEventManager.EXCLUSIVE_DRAG_VALUE);
        delegate.eventShouldRepaint();
        dragging = false;
    }

    public void mouseDragged(MouseEvent e, Point mousePosition) {
        if(dragging) {
            delegate.eventQueryRootElement().drag(Vector2D.vector(mousePosition));
            delegate.eventChangeDone();
            delegate.eventShouldRepaint();
        }
    }
}
