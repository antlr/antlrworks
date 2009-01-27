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

package org.antlr.xjlib.appkit.undo;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.util.Stack;

public class XJUndo {

    protected XJUndoEngine engine;
    protected XJUndoDelegate delegate;

    protected UndoManager undoManager = new UndoManager();
    protected XJUndoAction undoAction;
    protected XJRedoAction redoAction;

    protected Stack<CustomCompoundEdit> groupEditEvent = new Stack<CustomCompoundEdit>();

    protected int enable = 0;

    public XJUndo(XJUndoEngine engine, XJUndoDelegate delegate) {
        this.engine = engine;
        this.delegate = delegate;

        undoAction = new XJUndoAction(undoManager);
        redoAction = new XJRedoAction(undoManager);

        undoAction.setRedoAction(redoAction);
        redoAction.setUndoAction(undoAction);
    }

    public void close() {
        engine = null;
        delegate = null;
    }

    public void bindTo(JTextPane textPane) {
        textPane.getDocument().addUndoableEditListener(new TextPaneUndoableEditListener());
    }

    public void performUndo() {
        if(delegate != null)
            delegate.undoManagerWillUndo(false);

        try {
            undoAction.actionPerformed(null);
            fireUndoStateDidChange();
        } finally {
            if(delegate != null)
                delegate.undoManagerDidUndo(false);
        }
    }

    public void performRedo() {
        if(delegate != null)
            delegate.undoManagerWillUndo(true);

        try {
            redoAction.actionPerformed(null);
            fireUndoStateDidChange();
        } finally {
            if(delegate != null)
                delegate.undoManagerDidUndo(true);
        }
    }

    public void beginUndoGroup(String name) {
        groupEditEvent.push(new CustomCompoundEdit(name));
    }

    public void endUndoGroup() {
        CustomCompoundEdit edit = groupEditEvent.pop();
        edit.end();
        addEditEvent(edit);
    }

    public CompoundEdit getUndoGroup() {
        if(groupEditEvent.isEmpty())
            return null;
        else
            return (CompoundEdit)groupEditEvent.peek();
    }

    public void enableUndo() {
        enable--;
    }

    public void disableUndo() {
        enable++;
    }

    public boolean isEnabled() {
        return enable == 0;
    }

    public boolean canUndo() {
        return undoManager.canUndo();
    }

    public boolean canRedo() {
        return undoManager.canRedo();
    }

    public void addEditEvent(UndoableEdit edit) {
        undoManager.addEdit(edit);
        undoAction.updateUndoState();
        redoAction.updateRedoState();
        fireUndoStateDidChange();
    }

    protected void fireUndoStateDidChange() {
        engine.undoStateDidChange(this);
    }

    protected class TextPaneUndoableEditListener implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent e) {
            if(!isEnabled())
                return;

            CompoundEdit groupEdit = getUndoGroup();
            if(groupEdit == null)
                addEditEvent(e.getEdit());
            else
                groupEdit.addEdit(e.getEdit());
        }
    }

    protected static class CustomCompoundEdit extends CompoundEdit {

        protected String name;

        public CustomCompoundEdit(String name) {
            this.name = name;
        }

        public String getUndoPresentationName() {
            return "Undo "+name;
        }

        public String getRedoPresentationName() {
            return "Redo "+name;
        }

    }
}
