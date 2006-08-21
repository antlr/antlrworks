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

package org.antlr.works.ate.syntax.generic;

import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.ate.syntax.misc.ATEToken;

import java.util.ArrayList;
import java.util.List;

public class ATESyntaxLexer {

    public static final int TOKEN_SINGLE_QUOTE_STRING = 1;
    public static final int TOKEN_DOUBLE_QUOTE_STRING = 2;
    public static final int TOKEN_SINGLE_COMMENT = 3;
    public static final int TOKEN_COMPLEX_COMMENT = 4;
    public static final int TOKEN_ID = 5;
    public static final int TOKEN_CHAR = 6;
    public static final int TOKEN_OTHER = 7;

    protected List tokens;
    protected String text;
    protected int position;

    protected int lineNumber;
    protected int lineIndex;    // position of the line in characters
    protected List lines;

    /** c0 and c1 are character cache for quick access to the current
     * character (c0) and the next character (c1)
     */
    protected char c0;
    protected char c1;

    public ATESyntaxLexer() {
        lines = new ArrayList();
        tokens = new ArrayList();
    }

    public List getTokens() {
        return tokens;
    }

    public List getLines() {
        return lines;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void tokenize(String text) {
        this.text = text;

        position = -1;
        lineNumber = 0;
        lines.clear();
        lines.add(new ATELine(0));

        tokens.clear();
        tokenize();
    }

    protected void tokenize() {
        while(nextCharacter()) {
            ATEToken token = null;

            if(c0 == '\'')
                token = matchSingleQuoteString();
            else if(c0 == '\"')
                token = matchDoubleQuoteString();
            else if(c0 == '/' && c1 == '/')
                token = matchSingleComment();
            else if(c0 == '/' && c1 == '*')
                token = matchComplexComment();
            else if(isLetter())
                token = matchID();
            else if(!isWhitespace())
                token = createNewToken(TOKEN_CHAR);

            addToken(token);
        }
    }

    public void addToken(ATEToken token) {
        if(token != null) {
            token.index = tokens.size();
            tokens.add(token);
        }
    }

    public ATEToken matchID() {
        int sp = position;
        while(isID(c1) && nextCharacter()) {
        }
        return createNewToken(TOKEN_ID, sp);
    }

    public ATEToken matchSingleQuoteString() {
        int sp = position;
        while(nextCharacter()) {
            if(c0 == '\'' || matchNewLine())
                return createNewToken(TOKEN_SINGLE_QUOTE_STRING, sp);
        }
        return null;
    }

    public ATEToken matchDoubleQuoteString() {
        int sp = position;
        while(nextCharacter()) {
            if(c0 == '\"' || matchNewLine())
                return createNewToken(TOKEN_DOUBLE_QUOTE_STRING, sp);
        }
        return null;
    }

    public ATEToken matchSingleComment() {
        int sp = position;
        while(nextCharacter()) {
            if(matchNewLine())
                return createNewToken(TOKEN_SINGLE_COMMENT, sp);
        }
        return createNewToken(TOKEN_SINGLE_COMMENT, sp, position);
    }

    public ATEToken matchComplexComment() {
        int sp = position;
        while(nextCharacter()) {
            if(c0 == '*' && c1 == '/') {
                // Don't forget to eat the next character ;-)
                nextCharacter();
                return createNewToken(TOKEN_COMPLEX_COMMENT, sp, position+2);
            }
        }
        // Complex comment terminates also at the end of the text
        return createNewToken(TOKEN_COMPLEX_COMMENT, sp, position);
    }

    public boolean nextCharacter() {
        boolean valid = false;
        final int length = text.length();

        c0 = c1 = 0;
        position++;
        if(position < length) {
            // Skip control character
            if(text.charAt(position) == '\\')
                position += 2;

            valid = position < length;
            if(valid) {
                c0 = text.charAt(position);
                if(position + 1 < length)
                    c1 = text.charAt(position+1);
            }

            if(matchNewLine()) {
                lineNumber++;
                lineIndex = position+1;
                lines.add(new ATELine(lineIndex));
            }
        }
        return valid;
    }

    public boolean matchNewLine() {
        if(c0 == '\n') return true;    // Unix style
        else if(c0 == '\r' && c1 == '\n') return true;    // Windows style
        else if(c0 == '\r' && c1 != '\n') return true;    // Mac style
        else return false;
    }

    public boolean isWhitespace() {
        return Character.isWhitespace(c0);
    }

    public boolean isLetter() {
        return Character.isLetter(c0);
    }

    public boolean isLetterOrDigit() {
        return isLetterOrDigit(c0);
    }

    public boolean isLetterOrDigit(char c) {
        return Character.isLetterOrDigit(c);
    }

    public boolean isID(char c) {
        if(Character.isLetterOrDigit(c))
            return true;

        return c == '_' || c == '$';
    }

    public ATEToken createNewToken(int type) {
        return createNewToken(type, position);
    }

    public ATEToken createNewToken(int type, int start) {
        return createNewToken(type, start, position+1);
    }

    public ATEToken createNewToken(int type, int start, int end) {
        return createNewToken(type, start, end, lineNumber, lineNumber, lineIndex, lineIndex);
    }

    public ATEToken createNewToken(int type, int start, int end,
                                int startLineNumber, int endLineNumber,
                                int startLineIndex, int endLineIndex) {
        return new ATEToken(type, start, end, startLineNumber, endLineNumber, startLineIndex, endLineIndex, text);
    }

}
