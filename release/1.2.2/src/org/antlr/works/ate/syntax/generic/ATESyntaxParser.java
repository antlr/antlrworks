package org.antlr.works.ate.syntax.generic;

import org.antlr.works.ate.syntax.misc.ATEToken;

import java.util.List;
import java.util.Stack;
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

public abstract class ATESyntaxParser {

    private List<ATEToken> tokens;
    private Stack<Integer> marks = new Stack<Integer>();
    private int position;

    private ATEToken t0;
    private ATEToken t1;

    public ATESyntaxParser() {
    }

    public void close() {
        if(tokens != null) {
            tokens.clear();            
        }
        t0 = t1 = null;
    }

    public void parse(List<ATEToken> tokens) {
        this.tokens = tokens;
        marks.clear();
        position = -1;
        clearTokenCache();
        parseTokens();
    }

    public abstract void parseTokens();

    public List<ATEToken> getTokens() {
        return tokens;
    }

    public int getPosition() {
        return position;
    }

    public void mark() {
        marks.push(position);
    }

    public void rewind() {
        position = marks.pop();
        clearTokenCache();
    }

    public boolean previousToken() {
        position--;
        clearTokenCache();
        return position >= 0;
    }

    public boolean nextToken() {
        position++;
        clearTokenCache();
        return position<tokens.size();
    }

    public boolean moreTokens() {
        return position<tokens.size();
    }

    public boolean skip(int count) {
        if(count == 1) {
            return nextToken();
        } else {
            for(int i=0; i<count; i++) {
                if(!nextToken()) return false;
            }
        }
        return true;
    }
    
    public ATEToken T(int index) {
        if(index == 0) {
            if(t0 == null)
                t0 = getToken(0);
            return t0;
        } else if(index == 1) {
            if(t1 == null)
                t1 = getToken(1);
            return t1;
        } else
            return getToken(index);
    }

    public ATEToken getToken(int index) {
        if(position+index >= 0 && position+index < tokens.size())
            return tokens.get(position+index);
        else
            return null;
    }

    private void clearTokenCache() {
        t0 = null;
        t1 = null;
    }

    public boolean matchSingleComment(int index) {
        if(isSingleComment(index)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    public boolean matchComplexComment(int index) {
        if(isComplexComment(index)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    public boolean matchSingleQuoteString(int index) {
        if(isTokenType(index, ATESyntaxLexer.TOKEN_SINGLE_QUOTE_STRING)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    public boolean isChar(int index, String c) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_CHAR) && T(index).getAttribute().equals(c);
    }

    public boolean isSingleComment(int index) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_SINGLE_COMMENT);
    }

    public boolean isComplexComment(int index) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_COMPLEX_COMMENT);
    }

    public boolean isID(int index) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_ID);
    }

    public boolean isID(int index, String attribute) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_ID) && T(index).getAttribute().equals(attribute);
    }

    public boolean isTokenType(int index, int type) {
        return T(index) != null && T(index).type == type;
    }

}
