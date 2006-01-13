package org.antlr.works.syntax;

import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.generic.ATESyntaxParser;
import org.antlr.works.ate.syntax.misc.ATEToken;

import java.util.ArrayList;
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

public class GrammarSyntaxBlock {

    public String name;
    public ATEToken start;
    public ATEToken end;

    public boolean isTokenBlock = false;
    public boolean isOptionsBlock = false;

    public List internalTokens;
    public String tokenVocab;

    public GrammarSyntaxBlock(String name, ATEToken start, ATEToken end) {
        this.name = name;
        this.start = start;
        this.end = end;
    }

    public List getInternalTokens() {
        if(internalTokens == null) {
            String content = end.getAttribute();
            content = content.substring(1, content.length());

            GrammarSyntaxLexer lexer = new GrammarSyntaxLexer();
            lexer.tokenize(content);

            ParseInternalTokens p = new ParseInternalTokens();
            p.parse(lexer.getTokens());

            internalTokens = p.internalTokens;
        }
        return internalTokens;
    }

    public void parseOptionsBlock() {
        List tokens = getInternalTokens();
        for(int index=0; index<tokens.size(); index++) {
            ATEToken t = (ATEToken)tokens.get(index);
            if(t.getAttribute().equals("tokenVocab") && index+1<tokens.size()) {
                t = (ATEToken) tokens.get(index+1);
                tokenVocab = t.getAttribute();
            }
        }
    }

    public String getTokenVocab() {
        return tokenVocab;
    }

    public class ParseInternalTokens extends ATESyntaxParser {

        public List internalTokens;

        public void parseTokens() {
            internalTokens = new ArrayList();
            
            while(nextToken()) {
                if(T(0).type == ATESyntaxLexer.TOKEN_ID) {
                    if(isChar(1, "=") || isTokenType(1, GrammarSyntaxLexer.TOKEN_SEMI))
                        internalTokens.add(T(0));
                }
            }
        }

    }
}
