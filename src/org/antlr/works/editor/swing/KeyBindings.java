package org.antlr.works.editor.swing;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/*

[The "BSD licence"]
Copyright (c) 2004 Jean Bovet
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

public class KeyBindings {

    private JTextComponent textComponent = null;

    public KeyBindings(JTextComponent textComponent) {
        this.textComponent = textComponent;
        addEmacsKeyBindings();
    }

    public void addEmacsKeyBindings() {
        InputMap inputMap = textComponent.getInputMap();

        //Ctrl-b to go backward one character
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.backwardAction);

        //Ctrl-f to go forward one character
        key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.forwardAction);

        //Ctrl-p to go up one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.upAction);

        //Ctrl-n to go down one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.downAction);

        //Ctrl-d to delete the character under the cursor
        key = KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.deleteNextCharAction);

        //Ctrl-a to move cursor to begin of line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.beginLineAction);

        //Ctrl-e to move cursor to begin of line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.endLineAction);

        //Ctrl-k to delete the characters from the current position to the end of the line
        // Has to create a custom action to handle this one.
        addKeyBinding("CONTROL_K", KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK), new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // Find the paragraph where the caret is currently located and remove the remaining
                // characters. Each paragraph delimits a run of characters separated by a newline.
                int position = textComponent.getCaretPosition();
                Document doc = textComponent.getDocument();
                Element section = doc.getDefaultRootElement();
                int paragraphCount = section.getElementCount();
                for(int i=0; i<paragraphCount; i++) {
                    Element elem = section.getElement(i);
                    if(position>=elem.getStartOffset() && position<=elem.getEndOffset()) {
                        try {
                            doc.remove(position, elem.getEndOffset()-position-1);
                        } catch (BadLocationException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    }
                }
            }
        });
    }

    public void addKeyBinding(String name, KeyStroke keystroke, AbstractAction action) {
        textComponent.getActionMap().put(name, action);
        textComponent.getInputMap().put(keystroke, name);
    }
}
