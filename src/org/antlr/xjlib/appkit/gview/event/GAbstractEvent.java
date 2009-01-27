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
import org.antlr.xjlib.appkit.gview.object.GElement;

import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class GAbstractEvent {

    protected GEventManager manager = null;
    protected GEventDelegate delegate = null;

    protected GView view = null;

    public GAbstractEvent(GView view) {
        this.view = view;
    }

    public void setDelegate(GEventDelegate delegate) {
        this.delegate = delegate;
    }
    
    public void setManager(GEventManager manager) {
        this.manager = manager;
    }

    public void addExclusiveValue(Object value) {
        manager.addExclusiveValue(value);
    }

    public void removeExclusiveValue(Object value) {
        manager.removeExclusiveValue(value);
    }

    public boolean hasExclusiveValue(Object value) {
        return manager.hasExclusiveValue(value);
    }

    public boolean shouldFocusOnElement(GElement element) {
        return true;
    }

    public void mousePressed(MouseEvent e, Point mousePosition) {

    }

    public void mouseReleased(MouseEvent e, Point mousePosition) {

    }

    public void mouseDragged(MouseEvent e, Point mousePosition) {

    }

    public void mouseMoved(MouseEvent e, Point mousePosition) {

    }

    public void draw(Graphics g) {

    }

}
