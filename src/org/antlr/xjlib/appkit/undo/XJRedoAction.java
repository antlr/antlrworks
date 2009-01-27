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
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;

public class XJRedoAction extends AbstractAction {

    protected UndoManager undoManager;
    protected XJUndoAction undoAction;

    public XJRedoAction(UndoManager manager) {
        super("Redo");
        setEnabled(false);
        this.undoManager = manager;
    }

    public void setUndoAction(XJUndoAction action) {
        this.undoAction = action;
    }

    public void actionPerformed(ActionEvent e) {
        if(!undoManager.canRedo())
            return;

        try {
            undoManager.redo();
        } catch (CannotRedoException ex) {
            ex.printStackTrace();
        }
        updateRedoState();
        undoAction.updateUndoState();
    }

    protected void updateRedoState() {
        if (undoManager.canRedo()) {
            setEnabled(true);
            putValue(Action.NAME, undoManager.getRedoPresentationName());
        } else {
            setEnabled(false);
            putValue(Action.NAME, "Redo");
        }
    }
}
