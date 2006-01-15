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

import org.antlr.works.ate.ATEPanel;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class ATEAutoIndentation implements Runnable {

    protected int offset;
    protected int length;
    protected boolean enabled = true;

    protected ATEPanel textEditor;

    public ATEAutoIndentation(ATEPanel textEditor) {
        this.textEditor = textEditor;
    }

    public void setEnabled(boolean enable) {
        this.enabled = enable;
    }

    public boolean enabled() {
        return enabled;
    }

    public void indent(int offset, int length) {
        this.offset = offset;
        this.length = length;
        if(enabled())
            SwingUtilities.invokeLater(this);
    }

    public Document getDocument() {
        return textEditor.getTextPane().getDocument();
    }

    public boolean autoIndentAfterReturn() throws BadLocationException {
        String s = getDocument().getText(offset-1, length+1);
        if(s.length() == 0)
            return false;

        if(s.length() == 1 || (s.charAt(0) != '\n' && s.charAt(1) == '\n')) {
            // Find the beginning of the previous line
            String t = getDocument().getText(0, offset);
            for(int i = offset - 2; i >= 0; i--) {
                // Stop when a newline is found or the beginning of the text is reached
                if(t.charAt(i) == '\n' || i == 0) {
                    // Find the first non-white space/tab character;
                    if(i > 0) {
                        // Reached \n, increment i to begin past it
                        i++;
                    }
                    // Go forward to see how many white-space or tab there are
                    // before reaching a character
                    int start = i;
                    while((i < offset - 1) && (t.charAt(i) == ' ' || t.charAt(i) == '\t')) {
                        i++;
                    }

                    // If we reached the end of the line without any other character
                    // we have to increment i in order to take into account the last
                    // white space or tab
                    if(i == offset - 1 && (t.charAt(i) == ' ' || t.charAt(i) == '\t')) {
                        i++;
                    }
                    getDocument().insertString(offset+1, t.substring(start, i), null);
                    return true;
                }
            }
        }
        return false;
    }


    public void run() {
        try {
            if(!autoIndentAfterReturn()) {
                textEditor.ateAutoIndent(offset, length);
            }
        } catch (BadLocationException e) {
            // ignore exception
        }
    }

}
