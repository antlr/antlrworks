package org.antlr.works.plugin.container;

import org.antlr.xjlib.appkit.frame.XJFrame;
import org.antlr.xjlib.appkit.frame.XJFrameInterface;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuItem;
import org.antlr.xjlib.appkit.undo.XJUndo;
import org.antlr.xjlib.appkit.undo.XJUndoDelegate;
import org.antlr.xjlib.appkit.undo.XJUndoEngine;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
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

public class PCXJFrameInterface implements XJFrameInterface {

    private PluginWindow window;
    private XJUndoEngine undoEngine;

    public PCXJFrameInterface(PluginWindow window) {
        this.window = window;
        undoEngine = new XJUndoEngine();
    }

    public void registerUndo(XJUndoDelegate delegate, JTextPane textPane) {
        undoEngine.registerUndo(new XJUndo(undoEngine, delegate), textPane);
    }

    public void unregisterUndo(XJUndoDelegate delegate) {
        undoEngine.unregisterUndo(delegate);
    }

    public XJUndo getUndo(JTextPane textPane) {
        return undoEngine.getUndo(textPane);
    }

    public XJUndo getCurrentUndo() {
        return undoEngine.getCurrentUndo();
    }

    public XJMainMenuBar getMainMenuBar() {
        return window.getMainMenuBar();
    }

    public Container getJavaContainer() {
        return window.getContentPane();
    }

    public JLayeredPane getLayeredPane() {
        return window.getLayeredPane();
    }

    public JRootPane getRootPane() {
        return window.getRootPane();
    }

    private void performUndo() {
        XJUndo undo = getCurrentUndo();
        if(undo != null) {
            undo.performUndo();
        }
    }

    private void performRedo() {
        XJUndo undo = getCurrentUndo();
        if(undo != null) {
            undo.performRedo();
        }
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        switch(item.getTag()) {
            case XJMainMenuBar.MI_UNDO:
                performUndo();
                break;
            case XJMainMenuBar.MI_REDO:
                performRedo();
                break;
            case XJMainMenuBar.MI_CUT:
                XJFrame.performActionOnFocusedJComponent(DefaultEditorKit.cutAction);
                break;
            case XJMainMenuBar.MI_COPY:
                XJFrame.performActionOnFocusedJComponent(DefaultEditorKit.copyAction);
                break;
            case XJMainMenuBar.MI_PASTE:
                XJFrame.performActionOnFocusedJComponent(DefaultEditorKit.pasteAction);
                break;
            case XJMainMenuBar.MI_SELECT_ALL:
                XJFrame.performActionOnFocusedJComponent(DefaultEditorKit.selectAllAction);
                break;
        }

    }


}
