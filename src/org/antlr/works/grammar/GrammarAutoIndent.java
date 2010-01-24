package org.antlr.works.grammar;

import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.GrammarWindow;
import org.antlr.works.prefs.AWPrefs;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
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

public class GrammarAutoIndent {

    public static void autoIndentOnSpecificKeys(GrammarWindow window, Document doc, int offset, int length) throws BadLocationException {
        String s = doc.getText(offset-1, length+1);
        if(s.length() < 2)
            return;

        char c1 = s.charAt(0);
        char c2 = s.charAt(1);
        if(c1 == '\n' || c1 == '\r') {
            if(c2 == '|') {
                doc.remove(offset, 1);
                doc.insertString(offset, "\t"+c2+"\t", null);
            } else if(c2 == ';') {
                doc.remove(offset, 1);
                doc.insertString(offset, "\t"+c2, null);
            }
        } else if(c2 == ':') {
            // Disable the auto-indent on ':' if we are in a block, action, etc.
            // This is indicated by the fact that the token has a scope.
            ATEToken token = window.getCurrentToken();
            if(token != null && token.scope != null)
                return;

            // Try to reach the beginning of the line by parsing only an ID
            // (which is the rule name)
            boolean beginningOfRule = true;
            int originalOffset = offset;
            while(--offset >= 0) {
                String t = doc.getText(offset, 1);
                char c = t.charAt(0);
                if(c == '\n' || c == '\r') {
                    break;
                }
                if(c != ' ' && c != '_' && !Character.isLetterOrDigit(c)) {
                    beginningOfRule = false;
                    break;
                }
            }
            if(beginningOfRule && AWPrefs.autoIndentColonInRule()) {
                int lengthOfRule = originalOffset-offset;
                int tabSize = AWPrefs.getEditorTabSize();

                if(lengthOfRule > tabSize+1) {
                    doc.remove(originalOffset, 1);
                    doc.insertString(originalOffset, "\n\t:\t", null);
                } else if(lengthOfRule < tabSize+1) {
                    doc.remove(originalOffset, 1);
                    doc.insertString(originalOffset, "\t:\t", null);
                } else {
                    doc.insertString(originalOffset+1, "\t", null);
                }
            }
        }
    }

}
