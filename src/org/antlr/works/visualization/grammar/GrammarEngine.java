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


package org.antlr.works.visualization.grammar;

import org.antlr.analysis.DFA;
import org.antlr.analysis.DecisionProbe;
import org.antlr.analysis.NFAState;
import org.antlr.tool.ErrorManager;
import org.antlr.tool.Grammar;
import org.antlr.tool.GrammarNonDeterminismMessage;
import org.antlr.works.interfaces.CancelObject;
import org.antlr.works.interfaces.Console;
import org.antlr.works.parser.Token;
import org.antlr.works.util.ErrorListener;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GrammarEngine {

    public List errors;

    protected Grammar grammar;
    protected Grammar lexerGrammar;
    protected Console console;

    public GrammarEngine(Console console) {
        this.console = console;
        errors = new ArrayList();
    }

    public void setGrammarText(String text, String filename) throws Exception {
        ErrorManager.setErrorListener(ErrorListener.shared());
        grammar = new Grammar(filename, text);
        grammar.createNFAs();
        lexerGrammar = createLexerGrammar(console, grammar);
    }

    public NFAState getRuleStartState(String name) {
        if(Token.isLexerName(name))
            return lexerGrammar != null ? lexerGrammar.getRuleStartState(name):null;
        else
            return grammar.getRuleStartState(name);
    }

    public Grammar getGrammarForRule(String name) {
        if(Token.isLexerName(name))
            return lexerGrammar;
        else
            return grammar;
    }

    public boolean hasGrammar() {
        return grammar != null;
    }

    public void analyze(CancelObject cancelObject) throws Exception {
        boolean oldVerbose = DecisionProbe.verbose;
        DecisionProbe.verbose = true;

        ErrorManager.setErrorListener(ErrorListener.shared());

        try {
            ErrorListener.shared().clear();

            createLookaheadDFAs(cancelObject);
            if(cancelObject != null && cancelObject.cancel())
                return;

            buildNonDeterministicErrors();
        } catch(Exception e) {
            throw e;
        } finally {
            DecisionProbe.verbose = oldVerbose;
        }
    }

    public static Grammar createLexerGrammar(Console console, Grammar grammar) {
        String lexerGrammarStr = grammar.getLexerGrammar();
        StringReader sr = new StringReader(lexerGrammarStr);
        Grammar lexerGrammar = new Grammar();
        lexerGrammar.setFileName("<internally-generated-lexer>");
        lexerGrammar.importTokenVocabulary(grammar);
        try {
            lexerGrammar.setGrammarContent(sr);
            lexerGrammar.createNFAs();
        } catch (Exception e) {
            console.print(e);
        }
        sr.close();
        return lexerGrammar;
    }

    protected void createLookaheadDFAs(CancelObject cancelObject) {
        for (int decision=1; decision<=grammar.getNumberOfDecisions(); decision++) {
            NFAState decisionStartState = grammar.getDecisionNFAStartState(decision);
            if ( decisionStartState.getNumberOfTransitions()>1 ) {
                DFA lookaheadDFA = new DFA(decision, decisionStartState);
                grammar.setLookaheadDFA(decision, lookaheadDFA);
            }
            if(cancelObject != null && cancelObject.cancel())
                break;
        }
    }

    protected void buildNonDeterministicErrors() {
        errors.clear();
        for (Iterator iterator = ErrorListener.shared().warnings.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            if ( o instanceof GrammarNonDeterminismMessage )
                errors.add(buildNonDeterministicError((GrammarNonDeterminismMessage)o));
        }
    }

    protected GrammarEngineError buildNonDeterministicError(GrammarNonDeterminismMessage nondetMsg) {
        GrammarEngineError error = new GrammarEngineError();

        List nonDetAlts = nondetMsg.probe.getNonDeterministicAltsForState(nondetMsg.problemState);
        error.setLine(nondetMsg.probe.dfa.getDecisionASTNode().getLine()-1);

        Set disabledAlts = nondetMsg.probe.getDisabledAlternatives(nondetMsg.problemState);
        List labels = nondetMsg.probe.getSampleNonDeterministicInputSequence(nondetMsg.problemState);
        String input = nondetMsg.probe.getInputSequenceDisplay(labels);
        error.setMessage("Decision can match input such as \""+input+"\" using multiple alternatives");

        int firstAlt = 0;
        for (Iterator iter = nonDetAlts.iterator(); iter.hasNext();) {
            Integer displayAltI = (Integer) iter.next();
            NFAState nfaStart = nondetMsg.probe.dfa.getNFADecisionStartState();

            int tracePathAlt =
                nfaStart.translateDisplayAltToWalkAlt(displayAltI.intValue());
            if ( firstAlt == 0 ) {
                firstAlt = tracePathAlt;
            }
            List path =
                nondetMsg.probe.getNFAPathStatesForAlt(firstAlt,
                                                       tracePathAlt,
                                                       labels);
            error.addPath(path, disabledAlts.contains(displayAltI));

            // Find all rules enclosing each state (because a path can extend over multiple rules)
            for (Iterator iterator = path.iterator(); iterator.hasNext();) {
                NFAState state = (NFAState)iterator.next();
                error.addRule(state.getEnclosingRule());
            }
        }

        return error;
    }

}
