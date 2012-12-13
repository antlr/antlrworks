package org.antlr.works.grammar.decisiondfa;

import org.antlr.analysis.DFA;
import org.antlr.analysis.NFAState;
import org.antlr.tool.Grammar;
import org.antlr.tool.Rule;
import org.antlr.works.ate.ATEOverlayManager;
import org.antlr.works.components.GrammarWindow;
import org.antlr.works.grammar.antlr.ANTLRGrammarEngine;

import java.awt.*;
import java.util.*;
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

public class DecisionDFAEngine {

    private GrammarWindow window;

    private Set<Integer> usesSemPreds = new HashSet<Integer>();
    private Set<Integer> usesSynPreds = new HashSet<Integer>();

    private Map<Integer,List<Integer>> decisionDFA = new HashMap<Integer, List<Integer>>();

    private Grammar discoveredLexerGrammar;
    private Grammar discoveredParserGrammar;

    public DecisionDFAEngine(GrammarWindow window) {
        this.window = window;
    }

    public void close() {
        window = null;
    }

    public void reset() {
        decisionDFA.clear();
    }

    public int getDecisionDFACount() {
        return decisionDFA.size();
    }

    public Grammar getDiscoveredLexerGrammar() {
        return discoveredLexerGrammar;
    }

    public Grammar getDiscoveredParserGrammar() {
        return discoveredParserGrammar;
    }

    public void discoverAllDecisions() throws Exception {
        discover(0, window.getTextEditor().getText().length());
    }

    private void discover(int start, int end) throws Exception {
        Set<Integer> lineIndexes = new HashSet<Integer>();
        for(int index = start; index < end; index++) {
            lineIndexes.add(window.getTextEditor().getLineIndexAtTextPosition(index));
        }

        ANTLRGrammarEngine antlrEngineGrammar = window.getGrammarEngine().getANTLRGrammarEngine();
        antlrEngineGrammar.analyze();
        discoveredLexerGrammar = antlrEngineGrammar.getLexerGrammar();
        discoveredParserGrammar = antlrEngineGrammar.getParserGrammar();

        decisionDFA.clear();
        usesSynPreds.clear();
        usesSemPreds.clear();

        discover(discoveredLexerGrammar, lineIndexes, usesSemPreds, usesSynPreds);
        discover(discoveredParserGrammar, lineIndexes, usesSemPreds, usesSynPreds);
    }

    private void discover(Grammar g, Set<Integer> lineIndexes, Set<Integer> usesSemPreds, Set<Integer> usesSynPreds) {
        if(g == null) return;

        if(g.decisionsWhoseDFAsUsesSemPreds != null) {
            for(DFA dfa : g.decisionsWhoseDFAsUsesSemPreds) {
                usesSemPreds.add(dfa.getDecisionNumber());
            }
        }

        if(g.decisionsWhoseDFAsUsesSynPreds != null) {
            for(DFA dfa : g.decisionsWhoseDFAsUsesSynPreds) {
                usesSynPreds.add(dfa.getDecisionNumber());
            }
        }

        // Get the position information about each DFA decision
        for(Integer lineIndex : lineIndexes) {
            addPositions(lineIndex, g.getLookaheadDFAColumnsForLineInFile(lineIndex));
        }
    }

    public void addPositions(Integer line, List<Integer> columnsForLineInFile) {
        if(columnsForLineInFile.isEmpty()) return;

        decisionDFA.put(line, columnsForLineInFile);
    }

    public boolean isDecisionPointAroundLocation(int line, int column) {
        List<Integer> s = decisionDFA.get(line+1);
        return s != null && (s.contains(column-1) || s.contains(column));
    }

    public List<DecisionDFAItem> getDecisionDFAItems() {
        List<DecisionDFAItem> items = new ArrayList<DecisionDFAItem>();
        for(int lineIndex : decisionDFA.keySet()) {
            for(int columnIndex : decisionDFA.get(lineIndex)) {
                DFA dfa = getDFAAtPosition(lineIndex, columnIndex);
                if(dfa == null) {
                    System.err.println("DFA is null for line "+lineIndex+" and column "+columnIndex);
                    continue;
                }

                Grammar g = discoveredLexerGrammar;
                if(g != null) {
                    Rule r = g.getRule(Grammar.ARTIFICIAL_TOKENS_RULENAME);
                    NFAState s = (NFAState)r.startState.transition(0).target;
                    if(s == null) {
                        System.err.println("NFAState s is null for rule "+r.name);
                        continue;
                    }
                    // Ignore tokens DFA
                    if(dfa.getDecisionNumber() == s.getDecisionNumber()) continue;
                }

                Color c = new Color(0, 128, 64);
                String title = "DFA decision "+dfa.getDecisionNumber();
                String info = "";
                if(usesSemPreds.contains(dfa.getDecisionNumber())) {
                    info += "uses semantic predicate";
                    c = new Color(255, 220, 0);
                } else if(usesSynPreds.contains(dfa.getDecisionNumber())) {
                    info += "uses syntactic predicate";
                    c = new Color(255, 220, 0);
                }
                if(dfa.isCyclic()) {
                    if(info.length() > 0) info += ", ";
                    info += "cyclic";
                }
                if(info.length() > 0) info += ", ";

                if (dfa.getNumberOfStates() != 0) {
                    info += dfa.getNumberOfStates()+" states";
                }
                else {
                    info += "<=" + dfa.getMaxStateNumber() + " states";
                }

                Point p = window.textEditor.getLineTextPositionsAtLineIndex(lineIndex-1);
                if(p != null) {
                    DecisionDFAItem item = new DecisionDFAItem(window);
                    item.setAttributes(null, p.x+columnIndex-1, p.x+columnIndex, lineIndex-1, c, title+" ("+info+")");
                    item.shape = ATEOverlayManager.SHAPE_RECT;
                    items.add(item);                    
                }
            }
        }
        return items;
    }

    public DFA getDFAAtPosition(int line, int column) {
        DFA dfa = null;
        if(discoveredParserGrammar != null) {
            dfa = discoveredParserGrammar.getLookaheadDFAFromPositionInFile(line, column);
        }
        if(dfa == null) {
            if(discoveredLexerGrammar != null) {
                dfa = discoveredLexerGrammar.getLookaheadDFAFromPositionInFile(line, column);
            }
        }

        return dfa;
    }

    public void refreshMenu() {
        window.getMainMenuBar().refresh();
    }

    public void refresh() {
        window.textEditor.damage();
        window.textEditor.repaint();
    }

}
