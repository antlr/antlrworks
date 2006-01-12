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

package org.antlr.works.ate.syntax;

import java.util.ArrayList;
import java.util.List;

public class ATEGenericLexer {

    public static final int TOKEN_SINGLE_QUOTE_STRING = 1;
    public static final int TOKEN_DOUBLE_QUOTE_STRING = 2;
    public static final int TOKEN_SINGLE_COMMENT = 3;
    public static final int TOKEN_COMPLEX_COMMENT = 4;
    public static final int TOKEN_BLOCK = 5;
    public static final int TOKEN_ID = 6;
    public static final int TOKEN_COLON = 7;
    public static final int TOKEN_SEMI = 8;
    public static final int TOKEN_LPAREN = 9;
    public static final int TOKEN_RPAREN = 10;
    public static final int TOKEN_CHAR = 11;
    public static final int TOKEN_REFERENCE = 12;
    public static final int TOKEN_RULE = 13;
    public static final int TOKEN_LABEL = 14;
    public static final int TOKEN_OTHER = 99;

    protected String text;
    protected int position;

    protected int lineNumber;
    protected int lineIndex;    // position of the line in characters
    protected List lines;

    public ATEGenericLexer(String text) {
        this.text = text;
        lines = new ArrayList();
    }

    public List getLines() {
        return lines;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public List parseTokens() {
        List tokens = new ArrayList();
        position = -1;
        lineNumber = 0;
        lines.add(new ATELine(0));

        while(nextCharacter()) {
            ATEToken token = null;

            if(C(0) == '\'')
                token = matchSingleQuoteString();
            else if(C(0) == '\"')
                token = matchDoubleQuoteString();
            else if(C(0) == '/' && C(1) == '/')
                token = matchSingleComment();
            else if(C(0) == '/' && C(1) == '*')
                token = matchComplexComment();
            else if(C(0) == '{')
                token = matchBlock('{', '}');
            else if(C(0) == '[')
                token = matchBlock('[', ']');
            else if(C(0) == ':')
                token = createNewToken(TOKEN_COLON);
            else if(C(0) == ';')
                token = createNewToken(TOKEN_SEMI);
            else if(isLetter() || C(0) == '@')
                token = matchID();
            else if(C(0) == '(')
                token = createNewToken(TOKEN_LPAREN);
            else if(C(0) == ')')
                token = createNewToken(TOKEN_RPAREN);
            else if(!isWhitespace())
                token = createNewToken(TOKEN_CHAR);

            if(token != null) {
                token.index = tokens.size();
                tokens.add(token);
            }
        }
        return tokens;
    }

    public ATEToken matchID() {
        int sp = position;
        while(isID(C(1)) && nextCharacter()) {
        }
        return createNewToken(TOKEN_ID, sp);
    }

    public ATEToken matchSingleQuoteString() {
        int sp = position;
        while(nextCharacter()) {
            if(C(0) == '\'' || matchNewLine())
                return createNewToken(TOKEN_SINGLE_QUOTE_STRING, sp);
        }
        return null;
    }

    public ATEToken matchDoubleQuoteString() {
        int sp = position;
        while(nextCharacter()) {
            if(C(0) == '\"' || matchNewLine())
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
            if(C(0) == '*' && C(1) == '/') {
                // Don't forget to eat the next character ;-)
                nextCharacter();
                return createNewToken(TOKEN_COMPLEX_COMMENT, sp, position+2);
            }
        }
        // Complex comment terminates also at the end of the text
        return createNewToken(TOKEN_COMPLEX_COMMENT, sp, position);
    }

    public ATEToken matchBlock(char start, char end) {
        // Skip all strings, comments and embedded blocks
        int sp = position;
        int embedded = 0;
        int startLineNumber = lineNumber;
        int startLineIndex = lineIndex;
        while(nextCharacter()) {
            if(C(0) == '\'') {
                matchSingleQuoteString();
            } else if(C(0) == '\"') {
                matchDoubleQuoteString();
            } else if(C(0) == '/' && C(1) == '/') {
                matchSingleComment();
            } else if(C(0) == '/' && C(1) == '*') {
                matchComplexComment();
            } else if(C(0) == start) {
                embedded++;
            } else if(C(0) == end) {
                if(embedded == 0)
                    return createNewToken(TOKEN_BLOCK, sp, position+1, startLineNumber, lineNumber, startLineIndex, lineIndex);
                else
                    embedded--;
            }
        }
        return null;
    }

    public boolean nextCharacter() {
        position++;
        if(position<text.length()) {
            // Skip control character
            if(C(0) == '\\')
                position += 2;

            if(matchNewLine()) {
                lineNumber++;
                lineIndex = position+1;
                lines.add(new ATELine(lineIndex));
            }
            return position<text.length();
        } else
            return false;
    }

    public char C(int index) {
        if(position+index<text.length())
            return text.charAt(position+index);
        else
            return 0;
    }

    public boolean matchNewLine() {
        if(C(0) == '\n') return true;    // Unix style
        else if(C(0) == '\r' && C(1) == '\n') return true;    // Windows style
        else if(C(0) == '\r' && C(1) != '\n') return true;    // Mac style

        return false;
    }

    public boolean isWhitespace() {
        return Character.isWhitespace(C(0));
    }

    public boolean isLetter() {
        return Character.isLetter(C(0));
    }

    public boolean isLetterOrDigit() {
        return isLetterOrDigit(C(0));
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
