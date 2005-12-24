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


package org.antlr.works.syntax;

import org.antlr.works.prefs.AWPrefs;

import javax.swing.*;
import javax.swing.text.BadLocationException;

public class AutoIndentation implements Runnable {

    protected int offset;
    protected int length;
    protected boolean enabled = true;

    private JTextPane textPane;

    public AutoIndentation(JTextPane textPane) {
        this.textPane = textPane;
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

    public boolean autoIndentAfterReturn(String s) throws BadLocationException {
        if(s.length() == 1 || (s.charAt(0) != '\n' && s.charAt(1) == '\n')) {
            // Find the beginning of the previous line
            String t = textPane.getDocument().getText(0, offset);
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
                    textPane.getDocument().insertString(offset+1, t.substring(start, i), null);
                    return true;
                }
            }
        }
        return false;
    }

    public void autoIndentOnSpecificKeys(String s) throws BadLocationException {
        char c1 = s.charAt(0);
        char c2 = s.charAt(1);
        if(c1 == '\n' || c1 == '\r') {
            if(c2 == '|') {
                textPane.getDocument().remove(offset, 1);
                textPane.getDocument().insertString(offset, "\t"+c2+"\t", null);
            } else if(c2 == ';') {
                textPane.getDocument().remove(offset, 1);
                textPane.getDocument().insertString(offset, "\t"+c2, null);
            }
        } else if(c2 == ':') {
            // Try to reach the beginning of the line by parsing only an ID
            // (which is the rule name)
            boolean beginningOfRule = true;
            int originalOffset = offset;
            while(--offset >= 0) {
                String t = textPane.getDocument().getText(offset, 1);
                char c = t.charAt(0);
                if(c == '\n' || c == '\r') {
                    // beginning of line reached
                    break;
                }
                if(c != ' ' && c != '_' && !Character.isLetterOrDigit(c)) {
                    beginningOfRule = false;
                    break;
                }
            }
            if(beginningOfRule) {
                int lengthOfRule = originalOffset-offset;
                int tabSize = AWPrefs.getEditorTabSize();

                if(lengthOfRule > tabSize+1) {
                    textPane.getDocument().remove(originalOffset, 1);
                    textPane.getDocument().insertString(originalOffset, "\n\t:\t", null);
                } else if(lengthOfRule < tabSize+1) {
                    textPane.getDocument().remove(originalOffset, 1);
                    textPane.getDocument().insertString(originalOffset, "\t:\t", null);
                } else {
                    textPane.getDocument().insertString(originalOffset+1, "\t", null);
                }
            }
        }
    }

    public void run() {
        try {
            String s = textPane.getDocument().getText(offset-1, length+1);
            if(autoIndentAfterReturn(s))
                return;

            autoIndentOnSpecificKeys(s);
        } catch (BadLocationException e) {
            // ignore exception
        }
    }

}
