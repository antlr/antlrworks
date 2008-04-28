package org.antlr.works.grammar.element;

import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.syntax.GrammarSyntaxParser;

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

/** A block is, for example:
 *
 * tokens { FOO="a"; OTHER; }
 *
 * or
 *
 * options
 * {
 *	 tokenVocab=DataViewExpressions;
 *	 output=AST;
 *	 ASTLabelType=CommonTree;
 * }
 *
 *
 */
public class ElementBlock extends ElementScopable {

    public String name;
    public ATEToken start;
    public ATEToken end;

    public boolean isTokenBlock = false;
    public boolean isOptionsBlock = false;

    public List<ATEToken> internalTokens;
    private List<ATEToken> declaredTokens;

    private String tokenVocab;

    public ElementBlock(String name, ATEToken start) {
        this.name = name;
        this.start = start;
    }

    /**
     * Parse the content of the block to extract information that will be reused later
     */
    public void parse() {
        if(name.equals(GrammarSyntaxParser.OPTIONS_BLOCK_NAME)) {
            // Look only for the tokenVocab key/value pair.
            parseForTokenVocab();
            isOptionsBlock = true;
        } else if(name.equals(GrammarSyntaxParser.TOKENS_BLOCK_NAME)) {
            // Look for tokens
            parseForTokens();
            isTokenBlock = true;
        }
    }

    /**
     * Parses for the tokenVocab key/value pair of a block of type 'options'
     */
    private void parseForTokenVocab() {
        for(int index=0; index<internalTokens.size(); index++) {
            ATEToken t = internalTokens.get(index);
            if(t.getAttribute().equals("tokenVocab") && index+2<internalTokens.size()) {
                t = internalTokens.get(index+2);
                tokenVocab = t.getAttribute();
            }
        }
    }

    /**
     * Parses the internal tokens of a block of type 'tokens'.
     *
     * The tokens are considered as the following:
     * TOKEN_A="value";
     * TOKEN_B;
     * ...
     */
    private void parseForTokens() {
        declaredTokens = new ArrayList<ATEToken>();
        for(int index=0; index<internalTokens.size(); index++) {
            ATEToken t = internalTokens.get(index);
            if(t.getAttribute().equals("=")) {
                declaredTokens.add(internalTokens.get(index-1));
                // skip the value and the semi
                index += 2;
            } else if(t.getAttribute().equals(";")) {
                declaredTokens.add(internalTokens.get(index-1));
                // skip the semi
                index++;

            }
        }
    }

    public String getTokenVocab() {
        return tokenVocab;
    }

    public List<ATEToken> getDeclaredTokens() {
        return declaredTokens;
    }

    public List<String> getDeclaredTokensAsString() {
        List<String> names = new ArrayList<String>();
        for(int t=0; t<declaredTokens.size(); t++) {
            names.add(declaredTokens.get(t).getAttribute());
        }
        return names;
    }

}
