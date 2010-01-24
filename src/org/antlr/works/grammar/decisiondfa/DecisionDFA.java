package org.antlr.works.grammar.decisiondfa;

import org.antlr.Tool;
import org.antlr.analysis.DFA;
import org.antlr.codegen.CodeGenerator;
import org.antlr.tool.DOTGenerator;
import org.antlr.tool.Grammar;
import org.antlr.works.components.GrammarWindow;
import org.antlr.works.grammar.GrammarDOTTab;

import java.util.Collections;
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

    public DecisionDFA(GrammarWindow window) {
        super(window);
    }

    @Override
    protected boolean willLaunch() {
        return checkForCurrentRule();
    }

    @Override
    public void willRun() {
        line = window.getTextEditor().getCurrentLinePosition();
        column = window.getTextEditor().getCurrentColumnPosition();
    }

    @Override
    public String getDOTString() throws Exception {
        DecisionDFAEngine engine = window.decisionDFAEngine;
        Grammar g;

        int adjustedColumn = getDecisionColumn(g = engine.getDiscoveredParserGrammar());
        if(adjustedColumn == -1)
            adjustedColumn = getDecisionColumn(g = engine.getDiscoveredLexerGrammar());

        if(adjustedColumn == -1)
            throw new Exception("No decision in the current line");

        CodeGenerator generator = new CodeGenerator(new Tool(), g,
                (String) g.getOption("language"));

        DFA dfa = g.getLookaheadDFAFromPositionInFile(line, adjustedColumn);
        decisionNumber = dfa.getDecisionNumber();
        DOTGenerator dg = new DOTGenerator(g);
        g.setCodeGenerator(generator);
        dg.setArrowheadType("none");
        dg.setRankdir("LR");    // Left-to-right
        return dg.getDOT( dfa.startState );
    }

    public int getDecisionColumn(Grammar g) {
        if(g == null) return -1;
        
        List columns = g.getLookaheadDFAColumnsForLineInFile(line);
        // sort the columns as they appears to be not always in ascending order
        Collections.sort(columns);
        int adjustedColumn = -1;
        for(int index = columns.size()-1; index >=0; index--) {
            Integer match = (Integer)columns.get(index);
            if(match <= column) {
                adjustedColumn = match;
                break;
            } else if(index == 0)
                adjustedColumn = match;
        }
        return adjustedColumn;
    }

    public String getTabName() {
        return "Decision "+decisionNumber+" of \""+rule.name+"\"";
    }

}
