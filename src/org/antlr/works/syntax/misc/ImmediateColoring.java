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


package org.antlr.works.syntax.misc;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class ImmediateColoring implements Runnable {

    protected int offset;
    protected int length;
    protected int comment = 0;
    protected JTextPane textPane;

    public ImmediateColoring(JTextPane textPane) {
        this.textPane = textPane;
    }

    public void colorize(int offset, int length) {
        this.offset = offset;
        this.length = length;
        SwingUtilities.invokeLater(this);
    }

    public void run() {
        try {
            String s = textPane.getDocument().getText(offset, length);
            char c = s.charAt(0);
            if(c == '\n' || c == '\r') {
                MutableAttributeSet attr = textPane.getInputAttributes();
                StyleConstants.setForeground(attr, Color.black);
                StyleConstants.setBold(attr, false);
                StyleConstants.setItalic(attr, false);
                comment = 0;
            } else if(c == '/') {
                comment++;
            } else if(c == '*' && comment == 1) {
                comment++;
            } else {
                comment = 0;
            }

            if(comment == 2) {
                MutableAttributeSet attr = textPane.getInputAttributes();
                StyleConstants.setForeground(attr, Color.lightGray);
                StyleConstants.setBold(attr, false);
                StyleConstants.setItalic(attr, true);
                textPane.getStyledDocument().setCharacterAttributes(offset-1, 2, attr, true);
                comment = 0;
            }

        } catch (BadLocationException e1) {
            // ignore exception
        }
    }
}
