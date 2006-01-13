package org.antlr.works.syntax;

import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.misc.ATEToken;

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

public class GrammarSyntaxLexer extends ATESyntaxLexer {

    public static final int TOKEN_BLOCK = 100;
    public static final int TOKEN_COLON = 101;
    public static final int TOKEN_SEMI = 102;
    public static final int TOKEN_LPAREN = 103;
    public static final int TOKEN_RPAREN = 104;
    public static final int TOKEN_REFERENCE = 105;
    public static final int TOKEN_RULE = 106;
    public static final int TOKEN_LABEL = 107;

    protected void tokenize() {
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

            addToken(token);
        }
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
                    return createNewToken(GrammarSyntaxLexer.TOKEN_BLOCK, sp, position+1, startLineNumber, lineNumber, startLineIndex, lineIndex);
                else
                    embedded--;
            }
        }
        return null;
    }


}
