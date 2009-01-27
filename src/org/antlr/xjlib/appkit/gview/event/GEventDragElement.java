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
import org.antlr.xjlib.appkit.gview.object.GElement;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class GEventDragElement extends GAbstractEvent {

    public GElement dragElement = null;

    protected boolean dragging = false;
    protected Point p1 = null;
    protected Point p2 = null;

    protected Vector2D dragElementOffset = null;

    public GEventDragElement(GView view) {
        super(view);
    }

    public void mousePressed(MouseEvent e, Point mousePosition) {
        if(hasExclusiveValue(GEventManager.EXCLUSIVE_DRAG_VALUE))
            return;

        if(hasExclusiveValue(GEventManager.EXCLUSIVE_CREATE_LINK_VALUE))
            return;

        if(e.getClickCount() != 1)
            return;

        if((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK)
            return;

        dragElement = delegate.eventQueryElementAtPoint(mousePosition);
        if(dragElement != null && delegate.eventIsSelectedElement(dragElement)) {
            addExclusiveValue(GEventManager.EXCLUSIVE_DRAG_VALUE);
            dragging = true;
            p1 = mousePosition;
        } else if(dragElement != null && dragElement.isDraggable()) {
            addExclusiveValue(GEventManager.EXCLUSIVE_DRAG_VALUE);
            dragElement.beginDrag();
            dragElementOffset = Vector2D.vector(mousePosition).sub(dragElement.getPosition());
            return;
        }

        dragElement = null;
    }

    public void mouseReleased(MouseEvent e, Point mousePosition) {
        removeExclusiveValue(GEventManager.EXCLUSIVE_DRAG_VALUE);

        view.hideAllMagnetics();

        dragElement = null;
        dragging = false;
    }

    public void mouseDragged(MouseEvent e, Point mousePosition) {
        if(dragElement != null) {
            Vector2D mouse = Vector2D.vector(mousePosition);
            Vector2D position = mouse.sub(dragElementOffset);

            view.showAndAjustPositionToMagnetics(position);
            dragElement.moveToPosition(position);

            delegate.eventChangeDone();
            delegate.eventShouldRepaint();
        } else if(dragging) {
            p2 = mousePosition;
            delegate.eventMoveSelectedElements(p2.x-p1.x, p2.y-p1.y);
            p1 = p2;
            delegate.eventChangeDone();
            delegate.eventShouldRepaint();
        }
    }
}
