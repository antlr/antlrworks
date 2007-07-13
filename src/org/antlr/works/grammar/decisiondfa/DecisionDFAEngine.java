package org.antlr.works.grammar.decisiondfa;

import org.antlr.analysis.DFA;
import org.antlr.tool.Grammar;
import org.antlr.works.ate.ATEUnderlyingManager;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.grammar.EngineGrammar;
import org.antlr.works.syntax.element.ElementRule;

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

    private CEditorGrammar editor;
    private EngineGrammar engineGrammar;

    private Set<Integer> usesSemPreds = new HashSet<Integer>();
    private Set<Integer> usesSynPreds = new HashSet<Integer>();

    private Map<Integer,List<Integer>> decisionDFA = new HashMap<Integer, List<Integer>>();

    public DecisionDFAEngine(CEditorGrammar editor) {
        this.editor = editor;
    }

    public void reset() {
        decisionDFA.clear();
    }

    public void discoverAllDecisions(boolean lexer) throws Exception {
        discover(0, editor.getTextEditor().getText().length(), lexer);
    }

    public void discoverDecisionsAtCurrentRule() throws Exception {
        ElementRule r = editor.rules.getEnclosingRuleAtPosition(editor.getCaretPosition());
        if(r == null) {
            throw new RuntimeException("No rule at cursor position.");
        }
        discover(r.getStartIndex(), r.getEndIndex(), r.lexer);
    }

    private void discover(int start, int end, boolean lexer) throws Exception {
        Set<Integer> lineIndexes = new HashSet<Integer>();
        for(int index = start; index < end; index++) {
            lineIndexes.add(editor.getTextEditor().getLineIndexAtTextPosition(index));
        }

        engineGrammar = editor.getEngineGrammar();
        engineGrammar.analyze();

        Grammar g = lexer?engineGrammar.getLexerGrammar():engineGrammar.getParserGrammar();

        // Get the info about syn/sem predicates used by some DFA decision
        Set<DFA> semPredDFAs = new HashSet<DFA>();
        if(g != null) {
            semPredDFAs.addAll(g.decisionsWhoseDFAsUsesSemPreds);
        }

        Set<DFA> synPredDFAs = new HashSet<DFA>();
        if(g != null) {
            synPredDFAs.addAll(g.decisionsWhoseDFAsUsesSynPreds);
        }

        usesSemPreds.clear();
        for(DFA dfa : semPredDFAs) {
            usesSemPreds.add(dfa.getDecisionNumber());
        }

        usesSynPreds.clear();
        for(DFA dfa : synPredDFAs) {
            usesSynPreds.add(dfa.getDecisionNumber());
        }

        // Get the position information about each DFA decision
        decisionDFA.clear();
        for(Integer lineIndex : lineIndexes) {
            if(g != null) {
                addPositions(lineIndex, g.getLookaheadDFAColumnsForLineInFile(lineIndex));
            }
        }

        editor.textEditor.damage();
        editor.textEditor.repaint();
    }

    public void addPositions(Integer line, List<Integer> columnsForLineInFile) {
        if(columnsForLineInFile.isEmpty()) return;

        decisionDFA.put(line, columnsForLineInFile);
    }

    public List<DecisionDFAItem> getDecisionDFAItems() {
        List<DecisionDFAItem> items = new ArrayList<DecisionDFAItem>();
        for(int lineIndex : decisionDFA.keySet()) {
            for(int columnIndex : decisionDFA.get(lineIndex)) {
                DFA dfa = getDFAAtPosition(lineIndex, columnIndex);
                Color c = new Color(255, 128, 0);
                String title = "DFA decision "+dfa.getDecisionNumber();
                if(usesSemPreds.contains(dfa.getDecisionNumber())) {
                    title += " (uses semantic predicates)";
                    c = new Color(255, 111, 207);
                }
                if(usesSynPreds.contains(dfa.getDecisionNumber())) {
                    title += " (uses syntactic predicates)";
                    c = new Color(255, 204, 102);
                }
                Point p = editor.textEditor.getLineTextPositionsAtLineIndex(lineIndex-1);
                DecisionDFAItem item = new DecisionDFAItem(editor);
                item.setAttributes(null, p.x+columnIndex-1, p.x+columnIndex, lineIndex-1, c, title);
                item.shape = ATEUnderlyingManager.SHAPE_RECT;
                items.add(item);
            }
        }
        return items;
    }

    public DFA getDFAAtPosition(int line, int column) {
        DFA dfa = null;
        Grammar g = engineGrammar.getParserGrammar();
        if(g != null) {
            dfa = g.getLookaheadDFAFromPositionInFile(line, column);
        }
        if(dfa == null) {
            g = engineGrammar.getLexerGrammar();
            if(g != null) {
                dfa = g.getLookaheadDFAFromPositionInFile(line, column);
            }
        }

        return dfa;
    }
}
