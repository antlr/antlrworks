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

package org.antlr.works.ate.swing;

import org.antlr.works.ate.ATETextPane;
import org.antlr.xjlib.foundation.XJSystem;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ATEKeyBindings {

    private ATETextPane textComponent;

    public ATEKeyBindings(ATETextPane textComponent) {
        this.textComponent = textComponent;
        this.textComponent.setKeyBindings(this);

        if(XJSystem.isMacOS()) {
            addEmacsKeyBindings();
        }

        addStandardKeyBindings();        
    }

    public void close() {
        textComponent.getActionMap().clear();
        textComponent.getInputMap().clear();
        textComponent = null;
    }

    public void addStandardKeyBindings() {
        InputMap inputMap = textComponent.getInputMap();

        // HOME to move cursor to begin of line
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0);
        inputMap.put(key, DefaultEditorKit.beginLineAction);

        // SHIFT-HOME to move cursor to begin of line and select
        key = KeyStroke.getKeyStroke(KeyEvent.VK_HOME, Event.SHIFT_MASK);
        inputMap.put(key, DefaultEditorKit.selectionBeginLineAction);

        // END to move cursor to end of line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_END, 0);
        inputMap.put(key, DefaultEditorKit.endLineAction);

        // SHIFT-END to move cursor to end of line and select
        key = KeyStroke.getKeyStroke(KeyEvent.VK_END, Event.SHIFT_MASK);
        inputMap.put(key, DefaultEditorKit.selectionEndLineAction);

        // Add shift-delete to act as the standard delete key
        addKeyBinding("SHIFT_DELETE", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Event.SHIFT_MASK), new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                if(!textComponent.isWritable()) return;
                textComponent.getActionMap().get(DefaultEditorKit.deleteNextCharAction).actionPerformed(actionEvent);
            }
        });
    }

    public void addEmacsKeyBindings() {
        InputMap inputMap = textComponent.getInputMap();

        // Ctrl-b to go backward one character
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.backwardAction);

        // Ctrl-f to go forward one character
        key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.forwardAction);

        // Ctrl-p to go up one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.upAction);

        // Ctrl-n to go down one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.downAction);

        // Ctrl-a to move cursor to begin of line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.beginLineAction);

        // Ctrl-e to move cursor to end of line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.endLineAction);

        // Ctrl-d to delete the character under the cursor
        addKeyBinding("CONTROL_D", KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK), new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                if(!textComponent.isWritable()) return;
                textComponent.getActionMap().get(DefaultEditorKit.deleteNextCharAction).actionPerformed(actionEvent);
            }
        });

        // Ctrl-k to delete the characters from the current position to the end of the line
        // Has to create a custom action to handle this one.
        addKeyBinding("CONTROL_K", KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK), new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if(!textComponent.isWritable()) return;

                int start = textComponent.getCaretPosition();
                Document doc = textComponent.getDocument();
                String t;
                try {
                    t = doc.getText(start, doc.getLength()-start);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                    return;
                }

                int end = 0;
                while(end<t.length() && t.charAt(end) != '\n' && t.charAt(end) != '\r') {
                    end++;
                }

                try {
                    end = Math.max(1, end);

                    String content = doc.getText(start, end);
                    doc.remove(start, end);

                    // Copy the deleted portion of text into the system clipboard
                    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                    cb.setContents(new StringSelection(content), null);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });

        // Ctrl-t swap the two characters before and after the current position
        // Has to create a custom action to handle this one.
        addKeyBinding("CONTROL_T", KeyStroke.getKeyStroke(KeyEvent.VK_T, Event.CTRL_MASK), new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if(!textComponent.isWritable()) return;

                int p = textComponent.getCaretPosition();
                Document doc = textComponent.getDocument();

                if(p < 1 || p >= doc.getLength())
                    return;

                try {
                    String before = doc.getText(p-1, 1);
                    doc.remove(p-1, 1);
                    doc.insertString(p, before, null);
                    textComponent.setCaretPosition(p);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public void addKeyBinding(String name, KeyStroke keystroke, AbstractAction action) {
        textComponent.getActionMap().put(name, action);
        textComponent.getInputMap().put(keystroke, name);
    }

}
