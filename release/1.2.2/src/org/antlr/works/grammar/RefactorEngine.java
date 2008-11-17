package org.antlr.works.grammar;

import org.antlr.works.ate.syntax.misc.ATEScope;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.element.*;
import org.antlr.works.grammar.syntax.GrammarSyntaxLexer;

import java.util.List;
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

public class RefactorEngine {

    private List<ATEToken> tokens;
    private RefactorMutator mutator;

    public void setTokens(List<ATEToken> tokens) {
        this.tokens = tokens;
    }

    public void setMutator(RefactorMutator mutator) {
        this.mutator = mutator;
    }

    public boolean renameToken(ATEToken t, String name) {
        String attr = t.getAttribute();

        boolean renameRefRule = t.type == GrammarSyntaxLexer.TOKEN_REFERENCE || t.type == GrammarSyntaxLexer.TOKEN_DECL;

        for(int index = tokens.size()-1; index>0; index--) {
            ATEToken token = tokens.get(index);
            if(!token.getAttribute().equals(attr)) continue;

            if(token.type == t.type ||
                    renameRefRule && (token.type == GrammarSyntaxLexer.TOKEN_REFERENCE || token.type == GrammarSyntaxLexer.TOKEN_DECL))
            {
                mutator.replace(token.getStartIndex(), token.getEndIndex(), name);
            }
        }
        return true;
    }

    /**
     * Returns true if the scope should be ignored when checking for double-quote literal
     */
    public static boolean ignoreScopeForDoubleQuoteLiteral(ATEScope scope) {
        if(scope == null) return false;

        Class c = scope.getClass();
        return c.equals(ElementAction.class) || c.equals(ElementBlock.class) || c.equals(ElementRewriteBlock.class)
                || c.equals(ElementRewriteFunction.class) || c.equals(ElementArgumentBlock.class);
    }


}
