package org.antlr.works.grammar;

import org.antlr.analysis.DFA;
import org.antlr.analysis.NFAState;
import org.antlr.tool.DOTGenerator;
import org.antlr.tool.Grammar;
import org.antlr.tool.Rule;
import org.antlr.works.components.GrammarWindow;
import org.antlr.works.grammar.antlr.ANTLRGrammarEngine;
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

public class TokensDFA extends GrammarDOTTab {

    public TokensDFA(GrammarWindow window) {
        super(window);
    }

    @Override
    public String getDOTString() throws Exception {
        ANTLRGrammarEngine eg = window.getGrammarEngine().getANTLRGrammarEngine();
        eg.analyze();

        Grammar g = eg.getLexerGrammar();
        if(g == null) {
            throw new Exception("Cannot show tokens DFA because there is no lexer grammar");
        }
        Rule r = g.getRule(Grammar.ARTIFICIAL_TOKENS_RULENAME);
        NFAState s = (NFAState)r.startState.transition(0).target;
        DFA dfa = g.getLookaheadDFA(s.getDecisionNumber());

        DOTGenerator dg = new DOTGenerator(g);
        dg.setArrowheadType("none");
        dg.setRankdir("LR");    // Left-to-right
        return dg.getDOT( dfa.startState );
    }

    public String getTabName() {
        return Grammar.ARTIFICIAL_TOKENS_RULENAME+" DFA";
    }

}
