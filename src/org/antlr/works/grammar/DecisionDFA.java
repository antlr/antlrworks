package org.antlr.works.grammar;

import org.antlr.analysis.DFA;
import org.antlr.tool.DOTGenerator;
import org.antlr.tool.Grammar;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.syntax.GrammarSyntaxLexer;
import org.antlr.works.syntax.GrammarSyntaxRule;

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

public class DecisionDFA extends GrammarDOTTab {

    protected int line;
    protected int column;

    protected int decisionNumber;

    public DecisionDFA(CEditorGrammar editor, GrammarDOTTabDelegate delegate) {
        super(editor, delegate);
    }

    protected boolean willLaunch() {
        return checkForCurrentRule();
    }

    public void willRun() {
        ATEToken t = findClosestDecisionToken();
        if(t == null) {
            line = editor.getTextEditor().getCurrentLinePosition();
            column = editor.getTextEditor().getCurrentColumnPosition();
        } else {
            line = editor.getTextEditor().getLinePositionAtIndex(t.getStartIndex());
            column = editor.getTextEditor().getColumnPositionAtIndex(t.getStartIndex());
            editor.setCaretPosition(t.getStartIndex());
        }
    }

    public ATEToken findClosestDecisionToken() {
        ATEToken ct = editor.getCurrentToken();
        List tokens = editor.getTokens();
        int nestedParen = 0;
        for(int index=tokens.indexOf(ct); index >= 0; index--) {
            ATEToken t = (ATEToken)tokens.get(index);
            if(t.type == GrammarSyntaxLexer.TOKEN_COLON)
                return t;
            else if(t.type == GrammarSyntaxLexer.TOKEN_RPAREN)
                nestedParen++;
            else if(t.type == GrammarSyntaxLexer.TOKEN_LPAREN) {
                if(nestedParen == 0)
                    return t;
                else
                    nestedParen--;
            }
        }
        return null;
    }

    public String getDOTString() throws Exception {
        Grammar g;

        GrammarSyntaxRule rule = editor.getCurrentRule();
        if(rule.lexer)
            g = editor.getEngineGrammar().getLexerGrammar();
        else
            g = editor.getEngineGrammar().getParserGrammar();

        editor.getEngineGrammar().analyze();

        List columns = g.getLookaheadDFAColumnsForLineInFile(line);
        int adjustedColumn = -1;
        for(int index = columns.size()-1; index >=0; index--) {
            Integer match = (Integer)columns.get(index);
            if(match.intValue() <= column) {
                adjustedColumn = match.intValue();
                break;
            } else if(index == 0)
                adjustedColumn = match.intValue();
        }

        if(adjustedColumn == -1)
            throw new Exception("No decision in the current line");

        DFA dfa = g.getLookaheadDFAFromPositionInFile(line, adjustedColumn);
        decisionNumber = dfa.getDecisionNumber();
        DOTGenerator dg = new DOTGenerator(g);
        dg.setArrowheadType("none");
        dg.setRankdir("LR");    // Left-to-right
        return dg.getDOT( dfa.startState );
    }

    public String getTabName() {
        return "Decision "+decisionNumber+" of \""+rule.name+"\"";
    }

}
