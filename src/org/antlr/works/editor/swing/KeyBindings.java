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

package org.antlr.works.editor.swing;

import edu.usfca.xj.foundation.XJSystem;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class KeyBindings {

    private JTextComponent textComponent = null;

    public KeyBindings(JTextComponent textComponent) {
        this.textComponent = textComponent;
        // @todo currently only on Mac OS
        if(XJSystem.isMacOS())
            addEmacsKeyBindings();

        addStandardKeyBindings();
    }

    public void addStandardKeyBindings() {
        InputMap inputMap = textComponent.getInputMap();

        // HOME to move cursor to begin of line
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0);
        inputMap.put(key, DefaultEditorKit.beginLineAction);

        // END to move cursor to end of line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_END, 0);
        inputMap.put(key, DefaultEditorKit.endLineAction);
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

        // Ctrl-d to delete the character under the cursor
        key = KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.deleteNextCharAction);

        // Ctrl-a to move cursor to begin of line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.beginLineAction);

        // Ctrl-e to move cursor to end of line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.endLineAction);

        // Ctrl-k to delete the characters from the current position to the end of the line
        // Has to create a custom action to handle this one.
        addKeyBinding("CONTROL_K", KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK), new AbstractAction() {
            public void actionPerformed(ActionEvent e) {

                int start = textComponent.getCaretPosition();
                String t = null;
                try {
                    t = textComponent.getText(start, textComponent.getDocument().getLength()-start);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    return;
                }

                int end = 0;
                while(end<t.length() && t.charAt(end) != '\n' && t.charAt(end) != '\r') {
                    end++;
                }

                Document doc = textComponent.getDocument();
                try {
                    doc.remove(start, Math.max(1, end));
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
