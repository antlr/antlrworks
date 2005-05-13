package org.antlr.works.editor.undo;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.util.Stack;

/*

[The "BSD licence"]
Copyright (c) 2004-05 Jean Bovet
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

public class Undo {

    protected UndoDelegate delegate;

    public UndoManager undoManager = new UndoManager();
    protected UndoAction undoAction = new UndoAction(undoManager);
    protected RedoAction redoAction = new RedoAction(undoManager);

    protected Stack groupEditEvent = new Stack();

    protected int enable = 0;

    public Undo(UndoDelegate delegate) {
        this.delegate = delegate;

        undoAction.setRedoAction(redoAction);
        redoAction.setUndoAction(undoAction);
    }

    public void bindTo(JTextPane textPane) {
        textPane.getDocument().addUndoableEditListener(new TextPaneUndoableEditListener());
    }

    public void performUndo() {
        undoAction.actionPerformed(null);
        fireUndoStateDidChange();
    }

    public void performRedo() {
        redoAction.actionPerformed(null);
        fireUndoStateDidChange();
    }

    public void beginUndoGroup(String name) {
        groupEditEvent.push(new CustomCompoundEdit(name));
    }

    public void endUndoGroup() {
        CustomCompoundEdit edit = (CustomCompoundEdit)groupEditEvent.pop();
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

    protected void addEditEvent(UndoableEdit edit) {
        undoManager.addEdit(edit);
        undoAction.updateUndoState();
        redoAction.updateRedoState();
        fireUndoStateDidChange();
    }

    protected void fireUndoStateDidChange() {
        delegate.undoStateDidChange(this);
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

    protected class CustomCompoundEdit extends CompoundEdit {

        protected String name;

        public CustomCompoundEdit(String name) {
            this.name = name;
        }

        public String getPresentationName() {
            return name;
        }

    }
}
