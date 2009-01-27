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

import org.antlr.xjlib.appkit.gview.object.GElement;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class GEventManager {

    private List<GAbstractEvent> eventObjects = new ArrayList<GAbstractEvent>();
    private Set exclusiveValueSet = new HashSet();
    private GEventDelegate eventDelegate = null;

    public static final int EVENT_DRAW = 1;
    public static final int EVENT_MOUSE_PRESSED = 2;
    public static final int EVENT_MOUSE_RELEASED = 3;
    public static final int EVENT_MOUSE_DRAGGED = 4;
    public static final int EVENT_MOUSE_MOVED = 5;

    public static final Integer EXCLUSIVE_DRAG_VALUE = new Integer(99);
    public static final Integer EXCLUSIVE_CREATE_LINK_VALUE = new Integer(100);

    public GEventManager(GEventDelegate delegate) {
        this.eventDelegate = delegate;
    }

    public void add(GAbstractEvent event) {
        event.setManager(this);
        event.setDelegate(eventDelegate);
        eventObjects.add(event);
    }

    public boolean canFocusOnElement(GElement element) {
        Iterator<GAbstractEvent> iterator = eventObjects.iterator();
        while(iterator.hasNext()) {
            GAbstractEvent eventObject = iterator.next();
            if(!eventObject.shouldFocusOnElement(element))
                return false;
        }
        return true;
    }

    public void addExclusiveValue(Object value) {
        exclusiveValueSet.add(value);
    }

    public void removeExclusiveValue(Object value) {
        exclusiveValueSet.remove(value);
    }

    public boolean hasExclusiveValue(Object value) {
        return exclusiveValueSet.contains(value);
    }

    public void performEventObjects(int action, Object event, Point mousePosition, Object param) {
        Iterator<GAbstractEvent> iterator = eventObjects.iterator();
        while(iterator.hasNext()) {
            GAbstractEvent eventObject = iterator.next();
            switch(action) {
                case EVENT_DRAW:
                    eventObject.draw((Graphics)param);
                    break;
                case EVENT_MOUSE_PRESSED:
                    eventObject.mousePressed((MouseEvent)event, mousePosition);
                    break;
                case EVENT_MOUSE_RELEASED:
                    eventObject.mouseReleased((MouseEvent)event, mousePosition);
                    break;
                case EVENT_MOUSE_DRAGGED:
                    eventObject.mouseDragged((MouseEvent)event, mousePosition);
                    break;
                case EVENT_MOUSE_MOVED:
                    eventObject.mouseMoved((MouseEvent)event, mousePosition);
                    break;
            }
        }
    }


}
