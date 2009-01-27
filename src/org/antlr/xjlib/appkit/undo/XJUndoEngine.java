package org.antlr.xjlib.appkit.undo;

import org.antlr.xjlib.appkit.menu.XJMainMenuBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
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

public class XJUndoEngine {

    protected XJMainMenuBar mainMenuBar;
    protected Map<JTextPane,XJUndo> undos = new HashMap<JTextPane, XJUndo>();

    public XJUndoEngine() {
    }

    public void setMainMenuBar(XJMainMenuBar mainMenuBar) {
        this.mainMenuBar = mainMenuBar;
    }

    public void registerUndo(XJUndo undo, JTextPane component) {
        undo.bindTo(component);
        component.addFocusListener(new EditorFocusListener());
        undos.put(component, undo);
    }

    public void unregisterUndo(XJUndoDelegate delegate) {
        for(JTextPane tp : new HashSet<JTextPane>(undos.keySet())) {
            XJUndo undo = undos.get(tp);
            if(undo.delegate == delegate) {
                undo.close();
                undos.remove(tp);
                for(FocusListener fl : tp.getFocusListeners()) {
                    tp.removeFocusListener(fl);
                }
            }
        }
    }

    public XJUndo getCurrentUndo() {
        // Use the permanent focus owner because on Windows/Linux, an opened menu become
        // the current focus owner (non-permanent).
        return undos.get(KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner());
    }

    public XJUndo getUndo(Object object) {
        return undos.get(object);
    }

    public void updateUndoRedo(Object source) {
        XJUndo undo = getUndo(source);
        updateUndoRedo(undo);
    }

    public void updateUndoRedo(XJUndo undo) {
        if(mainMenuBar != null)
            mainMenuBar.menuUndoRedoItemState(getCurrentUndo());
    }

    public void undoStateDidChange(XJUndo undo) {
        updateUndoRedo(undo);
    }

    protected class EditorFocusListener implements FocusListener {

        public void focusGained(FocusEvent event) {
            updateUndoRedo(event.getSource());
        }

        public void focusLost(FocusEvent event) {
            // Update the menu only if the event is not temporary. Temporary
            // focus lost can be, for example, when opening a menu on Windows/Linux.
            if(!event.isTemporary())
                updateUndoRedo(null);
        }
    }

}
