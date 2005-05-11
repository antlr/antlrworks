package org.antlr.works.parser;

import java.util.ArrayList;
import java.util.List;

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

public class Lexer {

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

    public String text;
    public int position;
    public int line;
    public int linePosition;    // position of the line in characters
    public List lines;

    public Lexer(String text) {
        this.text = text;
        lines = new ArrayList();
    }

    public List parseTokens() {
        List tokens = new ArrayList();
        position = -1;
        line = 0;
        lines.add(new Integer(0));

        while(nextCharacter()) {
            Token token = null;

            if(C(0) == '\'')
                token = matchSingleQuoteString();
            else if(C(0) == '\"')
                token = matchDoubleQuoteString();
            else if(C(0) == '/' && C(1) == '/')
                token = matchSingleComment();
            else if(C(0) == '/' && C(1) == '*')
                token = matchComplexComment();
            else if(C(0) == '{')
                token = matchBlock();
            else if(C(0) == ':')
                token = new Token(TOKEN_COLON, position, position+1, line, linePosition, text);
            else if(C(0) == ';')
                token = new Token(TOKEN_SEMI, position, position+1, line, linePosition, text);
            else if(isLetter())
                token = matchID();
            else if(C(0) == '(')
                token = new Token(TOKEN_LPAREN, position, position+1, line, linePosition, text);
            else if(C(0) == ')')
                token = new Token(TOKEN_RPAREN, position, position+1, line, linePosition, text);
            else if(C(0) != ' ')
                token = new Token(TOKEN_CHAR, position, position+1, line, linePosition, text);

            if(token != null)
                tokens.add(token);
        }
        return tokens;
    }

    public Token matchID() {
        int sp = position;
        while(isID(C(1)) && nextCharacter()) {
        }
        return new Token(TOKEN_ID, sp, position+1, line, linePosition, text);
    }

    public Token matchSingleQuoteString() {
        int sp = position;
        while(nextCharacter()) {
            if(C(0) == '\'' || C(0) == '\n')
                return new Token(TOKEN_SINGLE_QUOTE_STRING, sp, position+1, line, linePosition, text);
        }
        return null;
    }

    public Token matchDoubleQuoteString() {
        int sp = position;
        while(nextCharacter()) {
            if(C(0) == '\"' || C(0) == '\n')
                return new Token(TOKEN_DOUBLE_QUOTE_STRING, sp, position+1, line, linePosition, text);
        }
        return null;
    }

    public Token matchSingleComment() {
        int sp = position;
        while(nextCharacter()) {
            if(C(0) == '\n')
                return new Token(TOKEN_SINGLE_COMMENT, sp, position+1, line, linePosition, text);
        }
        return new Token(TOKEN_SINGLE_COMMENT, sp, position, line, linePosition, text);
    }

    public Token matchComplexComment() {
        int sp = position;
        while(nextCharacter()) {
            if(C(0) == '*' && C(1) == '/')
                return new Token(TOKEN_COMPLEX_COMMENT, sp, position+2, line, linePosition, text);
        }
        // Complex comment terminates also at the end of the text
        return new Token(TOKEN_COMPLEX_COMMENT, sp, position, line, linePosition, text);
    }

    public Token matchBlock() {
        // Skip all strings, comments and embedded blocks
        int sp = position;
        int embedded = 0;
        while(nextCharacter()) {
            if(C(0) == '\'') {
                matchSingleQuoteString();
            } else if(C(0) == '\"') {
                matchDoubleQuoteString();
            } else if(C(0) == '/' && C(1) == '/') {
                matchSingleComment();
            } else if(C(0) == '/' && C(1) == '*') {
                matchComplexComment();
            } else if(C(0) == '{') {
                embedded++;
            } else if(C(0) == '}') {
                if(embedded == 0)
                    return new Token(TOKEN_BLOCK, sp, position+1, line, linePosition, text);
                else
                    embedded--;
            }
        }
        return null;
    }

    public boolean nextCharacter() {
        position++;
        if(position<text.length()) {
            if(C(0) == '\\')
                position += 2;

            if(C(0) == '\n') {
                line++;
                linePosition = position+1;
                lines.add(new Integer(linePosition));
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

        if(c == '_' || c == '$')
            return true;

        return false;
    }

}
