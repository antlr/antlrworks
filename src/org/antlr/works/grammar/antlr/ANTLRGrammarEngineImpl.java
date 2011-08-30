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


package org.antlr.works.grammar.antlr;

import antlr.TokenStreamException;
import org.antlr.analysis.NFAState;
import org.antlr.runtime.RecognitionException;
import org.antlr.tool.*;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.element.ElementGrammarName;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.works.utils.ErrorListener;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ANTLRGrammarEngineImpl implements ANTLRGrammarEngine {

    private Grammar parserGrammar;
    private Grammar lexerGrammar;
    private List<GrammarError> errors;

    private boolean needsToCreateGrammar;
    private boolean needsToAnalyzeGrammar;

    private final GrammarResult createGrammarResult = new GrammarResult();
    private final GrammarResult analyzeResult = new GrammarResult();

    private GrammarEngine engine;

    public ANTLRGrammarEngineImpl() {
        errors = new ArrayList<GrammarError>();
        markDirty();
    }

    public void setGrammarEngine(GrammarEngine engine) {
        this.engine = engine;
    }

    public GrammarEngine getGrammarEngine() {
        return engine;
    }

    public void close() {
        errors = null;
    }

    public void markDirty() {
        needsToCreateGrammar = true;
        needsToAnalyzeGrammar = true;
    }

    public Grammar getParserGrammar() {
        return parserGrammar;
    }

    public Grammar getLexerGrammar() {
        return lexerGrammar;
    }

    public NFAState getRuleStartState(String name) throws Exception {
        Grammar g;
        createGrammars();
        if(ATEToken.isLexerName(name))
            g = getLexerGrammar();
        else
            g = getParserGrammar();

        return g == null ? null:g.getRuleStartState(name);
    }

    public Grammar getGrammarForRule(String name) throws Exception {
        createGrammars();
        if(ATEToken.isLexerName(name))
            return getLexerGrammar();
        else
            return getParserGrammar();
    }

    public List<GrammarError> getErrors() {
        return errors;
    }

    public boolean hasGrammar() {
        switch(engine.getType()) {
            case ElementGrammarName.COMBINED:
                return parserGrammar != null;
            case ElementGrammarName.TREEPARSER:
            case ElementGrammarName.PARSER:
                return parserGrammar != null;
            case ElementGrammarName.LEXER:
                return lexerGrammar != null;
        }
        return false;
    }

    public Grammar getDefaultGrammar() {
        switch(engine.getType()) {
            case ElementGrammarName.COMBINED:
                return parserGrammar;
            case ElementGrammarName.TREEPARSER:
            case ElementGrammarName.PARSER:
                return parserGrammar;
            case ElementGrammarName.LEXER:
                return lexerGrammar;
        }
        return null;
    }

    public void createGrammars() throws Exception {
        if(!needsToCreateGrammar) {
            if(createGrammarResult.isSuccess()) {
                return;
            } else {
                needsToCreateGrammar = true;
            }
        }

        ErrorListener el = ErrorListener.getThreadInstance();
        ErrorManager.setErrorListener(el);

        parserGrammar = null;
        lexerGrammar = null;

        createGrammarResult.clear();

        try {
            switch(engine.getType()) {
                case ElementGrammarName.COMBINED:
                    createCombinedGrammar();
                    break;
                case ElementGrammarName.TREEPARSER:
                case ElementGrammarName.PARSER:
                    createParserGrammar();
                    break;
                case ElementGrammarName.LEXER:
                    createLexerGrammar();
                    break;
            }

            // if no exception, then assume create grammar was successful
            needsToCreateGrammar = false;
        } finally {
            // store the result of creating the grammars
            createGrammarResult.setErrors(el.errors);
            createGrammarResult.setWarnings(el.warnings);

            el.clear();
            ErrorManager.removeErrorListener();
        }
    }

    private Grammar createNewGrammar() throws TokenStreamException, RecognitionException, IOException {
        Grammar g = new Grammar();
        g.setTool(engine.getANTLRTool());
        g.setFileName(engine.getGrammarFileName());
        g.setGrammarContent(engine.getGrammarText());
        g.composite.createNFAs();

        // don't want errors from a previous grammar to interfere with this new grammar.
        // must reset error state otherwise analysis will not proceed if
        // there were previous errors.
        ErrorManager.resetErrorState();
        return g;
    }

    private void createCombinedGrammar() throws Exception {
        createParserGrammar();
        lexerGrammar = createLexerGrammarFromCombinedGrammar(parserGrammar);
    }

    private Grammar createLexerGrammarFromCombinedGrammar(Grammar grammar) throws Exception {
        String lexerGrammarStr = grammar.getLexerGrammar();
        if(lexerGrammarStr == null)
            return null;

        Grammar lexerGrammar = new Grammar();
        lexerGrammar.implicitLexer = true;
        lexerGrammar.setTool(engine.getANTLRTool());
        lexerGrammar.setFileName("<internally-generated-lexer>");
        lexerGrammar.importTokenVocabulary(grammar);

        lexerGrammar.setGrammarContent(lexerGrammarStr);
        lexerGrammar.composite.createNFAs();

        return lexerGrammar;
    }

    private void createParserGrammar() throws TokenStreamException, RecognitionException, IOException {
        parserGrammar = createNewGrammar();
    }

    private void createLexerGrammar() throws TokenStreamException, RecognitionException, IOException {
        lexerGrammar = createNewGrammar();
    }

    private void printLeftRecursionToConsole(List rules) {
        StringBuilder info = new StringBuilder();
        info.append("Aborting because the following rules are mutually left-recursive:");
        for (Object rule : rules) {
            Set rulesSet = (Set) rule;
            info.append("\n    ");
            info.append(rulesSet);
        }
        engine.reportError(info.toString());
    }

    private void markLeftRecursiveRules(List rules) {
        // 'rules' is a list of set of rules given by ANTLR
        for (Object ruleSet : rules) {
            final Set rulesSet = (Set) ruleSet;
            for (Object rule : rulesSet) {
                final Rule aRule = (Rule) rule;
                final ElementRule r = engine.getRuleWithName(aRule.name);
                if (r == null)
                    continue;
                r.setLeftRecursiveRulesSet(rulesSet);
            }
        }
    }

    public GrammarResult analyze() throws Exception {
        // if there is no need to analyze the grammar, return the previous result
        if(!needsToAnalyzeGrammar) {
            GrammarResult r = analyzeCompleted(null);
            if(r.isSuccess()) {
                return r;
            } else {
                needsToAnalyzeGrammar = true;
            }
        }

        // Set the error listener
        ErrorListener el = ErrorListener.getThreadInstance();
        ErrorManager.setErrorListener(el);

        createGrammars();

        Grammar g = getDefaultGrammar();
        if(g == null) {
            return analyzeCompleted(el);
        }

        List rules = g.checkAllRulesForLeftRecursion();
        if(!rules.isEmpty()) {
            printLeftRecursionToConsole(rules);
            markLeftRecursiveRules(rules);
        }

        if(ErrorManager.doNotAttemptAnalysis()) {
            return analyzeCompleted(el);
        }

        try {
            if ( g.nfa==null ) {
                g.composite.createNFAs();
            }
            g.createLookaheadDFAs();
            if(engine.isCombinedGrammar()) {
                // If the grammar is combined, analyze also the lexer
                if(lexerGrammar != null) {
                    lexerGrammar.composite.createNFAs();
                    lexerGrammar.createLookaheadDFAs();
                }
            }

            buildNonDeterministicErrors(el);
            markRulesWithWarningsOrErrors();
        } catch(Exception e) {
            // ignore
        }

        return analyzeCompleted(el);
    }

    private GrammarResult analyzeCompleted(ErrorListener el) throws InvocationTargetException, InterruptedException {
        if(SwingUtilities.isEventDispatchThread()) {
            engine.antlrGrammarEngineAnalyzeCompleted();
        } else {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    engine.antlrGrammarEngineAnalyzeCompleted();
                }
            });
        }

        if(el != null) {
            // no need to analyze the grammar
            needsToAnalyzeGrammar = false;

            // store the analyze result
            analyzeResult.clear();
            analyzeResult.setErrors(el.errors);
            analyzeResult.setWarnings(el.warnings);

            if(el.hasErrors() || el.hasWarnings()) {
                // in order to get the same error and warnings messages in the console
                // next time the grammar is checked (if it is not touched), we need to
                // turn this flag on again (see AW-182).
                needsToAnalyzeGrammar = true;
            }

            // clear the error listener
            el.clear();
            ErrorManager.removeErrorListener();
        }

        return getCompleteResult();
    }

    private GrammarResult getCompleteResult() {
        GrammarResult result = new GrammarResult();
        result.errors.clear();
        result.errors.addAll(createGrammarResult.errors);
        result.errors.addAll(analyzeResult.errors);

        result.warnings.clear();
        result.warnings.addAll(createGrammarResult.warnings);
        result.warnings.addAll(analyzeResult.warnings);

        return result;
    }

    public void cancel() {
        Grammar g = getDefaultGrammar();
        if(g != null)
            g.externallyAbortNFAToDFAConversion();
    }

    private void buildNonDeterministicErrors(ErrorListener el) {
        errors.clear();
        for (Message warning : el.warnings) {
            buildError(warning);
        }
        for (Message error : el.errors) {
            buildError(error);
        }
    }

    private void buildError(Object o) {
        if(o instanceof GrammarUnreachableAltsMessage)
            errors.add(buildUnreachableAltsError((GrammarUnreachableAltsMessage)o));
        else if(o instanceof GrammarNonDeterminismMessage)
            errors.add(buildNonDeterministicError((GrammarNonDeterminismMessage)o));
        else if(o instanceof NonRegularDecisionMessage)
            errors.add(buildNonRegularDecisionError((NonRegularDecisionMessage)o));
    }

    private GrammarError buildNonDeterministicError(GrammarNonDeterminismMessage message) {
        GrammarError error = new GrammarError();
        error.setLine(message.probe.dfa.getDecisionASTNode().getLine()-1);

        List labels = message.probe.getSampleNonDeterministicInputSequence(message.problemState);
        error.setLabels(labels);

        String input = message.probe.getInputSequenceDisplay(labels);
        error.setMessageText("Decision can match input such as \""+input+"\" using multiple alternatives");
        error.setMessage(message);

        return error;
    }

    private GrammarError buildUnreachableAltsError(GrammarUnreachableAltsMessage message) {
        GrammarError error = new GrammarError();

        error.setLine(message.probe.dfa.getDecisionASTNode().getLine()-1);
        error.setMessageText("The following alternatives are unreachable: "+message.alts);
        error.setMessage(message);

        return error;
    }

    private GrammarError buildNonRegularDecisionError(NonRegularDecisionMessage message) {
        GrammarError error = new GrammarError();

        error.setLine(message.probe.dfa.getDecisionASTNode().getLine()-1);
        error.setMessageText(message.toString());
        error.setMessage(message);

        return error;
    }

    private void markRulesWithWarningsOrErrors() throws Exception {
        for (ElementRule rule : engine.getRules()) {
            updateRuleWithErrors(rule, fetchErrorsForRule(rule));
        }
    }

    private void updateRuleWithErrors(ElementRule rule, List<GrammarError> errors) throws Exception {
        rule.setErrors(errors);
        rule.setNeedsToBuildErrors(true);
    }

    private List<GrammarError> fetchErrorsForRule(ElementRule rule) {
        List<GrammarError> errors = new ArrayList<GrammarError>();
        for (GrammarError error : getErrors()) {
            if (error.line >= rule.start.startLineNumber && error.line <= rule.end.startLineNumber)
                errors.add(error);
        }
        return errors;
    }

    public void computeRuleErrors(ElementRule rule) {
        List<GrammarError> errors = rule.getErrors();
        for (GrammarError error : errors) {
            Object o = error.getMessage();
            if (o instanceof GrammarUnreachableAltsMessage)
                computeRuleError(error, (GrammarUnreachableAltsMessage) o);
            else if (o instanceof GrammarNonDeterminismMessage)
                computeRuleError(error, (GrammarNonDeterminismMessage) o);
            else if (o instanceof NonRegularDecisionMessage)
                computeRuleError(error, (NonRegularDecisionMessage) o);
        }

        rule.setNeedsToBuildErrors(false);
    }

    private void computeRuleError(GrammarError error, GrammarNonDeterminismMessage message) {
        List nonDetAlts = message.probe.getNonDeterministicAltsForState(message.problemState);
        Set disabledAlts = message.probe.getDisabledAlternatives(message.problemState);

        int firstAlt = 0;

        for (Object nonDetAlt : nonDetAlts) {
            Integer displayAltI = (Integer) nonDetAlt;
            NFAState nfaStart = message.probe.dfa.getNFADecisionStartState();

            int tracePathAlt = nfaStart.translateDisplayAltToWalkAlt(displayAltI);
            if (firstAlt == 0)
                firstAlt = tracePathAlt;

            List path =
                    message.probe.getNFAPathStatesForAlt(firstAlt,
                            tracePathAlt,
                            error.getLabels());

            error.addPath(path, disabledAlts.contains(displayAltI));
            error.addStates(path);

            // Find all rules enclosing each state (because a path can extend over multiple rules)
            for (Object aPath : path) {
                NFAState state = (NFAState) aPath;
                error.addRule(state.enclosingRule.name);
            }
        }
    }

    private void computeRuleError(GrammarError error, GrammarUnreachableAltsMessage message) {
        NFAState state = message.probe.dfa.getNFADecisionStartState();
        for (Object alt : message.alts) {
            error.addUnreachableAlt(state, (Integer) alt);
            error.addStates(state);
            error.addRule(state.enclosingRule.name);
        }
    }

    private void computeRuleError(GrammarError error, NonRegularDecisionMessage message) {
        NFAState state = message.probe.dfa.getNFADecisionStartState();
        for (Object alt : message.altsWithRecursion) {
            // Use currently the unreachable alt for display purpose only
            error.addUnreachableAlt(state, (Integer) alt);
            error.addStates(state);
            error.addRule(state.enclosingRule.name);
        }
    }

}
