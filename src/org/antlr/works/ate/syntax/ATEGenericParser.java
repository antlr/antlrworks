package org.antlr.works.ate.syntax;

import java.util.List;
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

public abstract class ATEGenericParser {

    protected List tokens;
    protected int position;
    protected int mark;

    protected ATEGenericLexer lexer;

    public void init(String text) {
        lexer = new ATEGenericLexer(text);
        tokens = lexer.parseTokens();
        position = -1;
        mark = -1;
    }

    public List getTokens() {
        return tokens;
    }

    public List getLines() {
        if(lexer == null)
            return null;
        else
            return lexer.getLines();
    }

    public int getMaxLines() {
        if(lexer == null)
            return 0;
        else
            return lexer.getLineNumber();
    }

    public void mark() {
        mark = position;
    }

    public void rewind() {
        position = mark;
    }

    public boolean nextToken() {
        position++;
        return position<tokens.size();
    }

    public void skip(int count) {
        for(int i=0; i<count; i++) {
            nextToken();
        }
    }
    
    public ATEToken T(int index) {
        if(position+index >= 0 && position+index < tokens.size())
            return (ATEToken)tokens.get(position+index);
        else
            return null;
    }

    public boolean isChar(int index, String c) {
        return isTokenType(index, ATEGenericLexer.TOKEN_CHAR) && T(index).getAttribute().equals(c);
    }

    public boolean isLPAREN(int index) {
        return isTokenType(index, ATEGenericLexer.TOKEN_LPAREN);
    }

    public boolean isSEMI(int index) {
        return isTokenType(index, ATEGenericLexer.TOKEN_SEMI);
    }

    public boolean isCOLON(int index) {
        return isTokenType(index, ATEGenericLexer.TOKEN_COLON);
    }

    public boolean isSingleComment(int index) {
        return isTokenType(index, ATEGenericLexer.TOKEN_SINGLE_COMMENT);
    }

    public boolean isComplexComment(int index) {
        return isTokenType(index, ATEGenericLexer.TOKEN_SINGLE_COMMENT);
    }

    public boolean isID(int index) {
        return isTokenType(index, ATEGenericLexer.TOKEN_ID);
    }

    public boolean isID(int index, String attribute) {
        return isTokenType(index, ATEGenericLexer.TOKEN_ID) && T(index).getAttribute().equals(attribute);
    }

    public boolean isBLOCK(int index) {
        return isTokenType(index, ATEGenericLexer.TOKEN_BLOCK);
    }

    public boolean isTokenType(int index, int type) {
        return T(index) != null && T(index).type == type;
    }

}
