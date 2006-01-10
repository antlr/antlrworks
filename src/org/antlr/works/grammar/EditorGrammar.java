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


package org.antlr.works.grammar;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.antlr.Tool;
import org.antlr.analysis.DecisionProbe;
import org.antlr.analysis.NFAState;
import org.antlr.tool.ErrorManager;
import org.antlr.tool.Grammar;
import org.antlr.tool.GrammarNonDeterminismMessage;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.parser.ParserName;
import org.antlr.works.parser.Token;
import org.antlr.works.utils.ErrorListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EditorGrammar {

    protected Grammar parserGrammar;
    protected Grammar lexerGrammar;
    protected List errors;

    protected boolean grammarDirty;
    protected boolean grammarAnalyzeDirty;

    protected CEditorGrammar editor;

    public EditorGrammar(CEditorGrammar editor) {
        this.editor = editor;
        errors = new ArrayList();
        makeDirty();
    }

    public void makeDirty() {
        grammarDirty = true;
        grammarAnalyzeDirty = true;
    }

    public boolean isDirty() {
        return grammarDirty || grammarAnalyzeDirty;
    }

    public Grammar getParserGrammar() {
        createGrammars();
        return parserGrammar;
    }

    public Grammar getLexerGrammar() {
        createGrammars();
        return lexerGrammar;
    }

    public NFAState getRuleStartState(String name) {
        Grammar g;

        if(Token.isLexerName(name))
            g = getLexerGrammar();
        else
            g = getParserGrammar();

        return g == null ? null:g.getRuleStartState(name);
    }

    public Grammar getGrammarForRule(String name) {
        if(Token.isLexerName(name))
            return getLexerGrammar();
        else
            return getParserGrammar();
    }

    public List getErrors() {
        return errors;
    }

    public boolean hasGrammar() {
        switch(getType()) {
            case ParserName.COMBINED:
                return parserGrammar != null;
            case ParserName.TREEPARSER:
            case ParserName.PARSER:
                return parserGrammar != null;
            case ParserName.LEXER:
                return lexerGrammar != null;
        }
        return false;
    }

    public Grammar getANTLRGrammar() {
        switch(getType()) {
            case ParserName.COMBINED:
                return parserGrammar;
            case ParserName.TREEPARSER:
            case ParserName.PARSER:
                return parserGrammar;
            case ParserName.LEXER:
                return lexerGrammar;
        }
        return null;
    }

    public Tool getANTLRTool() {
        if(editor.getFileFolder() != null)
            return new Tool(new String[] { "-lib", editor.getFileFolder() } );
        else
            return new Tool();
    }

    public String getName() {
        ParserName name = editor.parser.getName();
        if(name == null)
            return null;
        else
            return name.name;
    }

    public int getType() {
        ParserName name = editor.parser.getName();
        if(name == null)
            return ParserName.COMBINED;
        else
            return name.getType();
    }

    public void createGrammars() {
        if(!grammarDirty)
            return;

        ErrorManager.setErrorListener(ErrorListener.shared());

        try {
            switch(getType()) {
                case ParserName.COMBINED:
                    createCombinedGrammar();
                    break;
                case ParserName.TREEPARSER:
                case ParserName.PARSER:
                    createParserGrammar();
                    break;
                case ParserName.LEXER:
                    createLexerGrammar();
                    break;
            }
        } catch(Exception e) {
            editor.console.print(e);
        }
        grammarDirty = false;
    }

    protected String getFileName() {
        String fileName = editor.getFileName();
        return fileName==null?"<notsaved>":fileName;
    }

    protected Grammar createNewGrammar(String filename, String content) throws TokenStreamException, RecognitionException {
        Grammar g = new Grammar();
        g.setTool(getANTLRTool());
        g.setFileName(filename);
        g.setGrammarContent(content);
        return g;
    }

    protected void createCombinedGrammar() throws TokenStreamException, RecognitionException {
        parserGrammar = createNewGrammar(getFileName(), editor.getText());
        parserGrammar.createNFAs();
        lexerGrammar = createLexerGrammarFromCombinedGrammar(parserGrammar);
    }

    protected Grammar createLexerGrammarFromCombinedGrammar(Grammar grammar) {
        String lexerGrammarStr = grammar.getLexerGrammar();
        if(lexerGrammarStr == null)
            return null;

        Grammar lexerGrammar = new Grammar();
        lexerGrammar.setTool(getANTLRTool());
        lexerGrammar.setFileName("<internally-generated-lexer>");
        lexerGrammar.importTokenVocabulary(grammar);
        try {
            lexerGrammar.setGrammarContent(lexerGrammarStr);
            //lexerGrammar.addArtificialMatchTokensRule();
            lexerGrammar.createNFAs();
        } catch (Exception e) {
            editor.console.print(e);
        }
        return lexerGrammar;
    }

    protected void createParserGrammar() throws TokenStreamException, RecognitionException {
        parserGrammar = createNewGrammar(getFileName(), editor.getText());
        parserGrammar.createNFAs();
    }

    protected void createLexerGrammar() throws TokenStreamException, RecognitionException {
        lexerGrammar = createNewGrammar(getFileName(), editor.getText());
        lexerGrammar.createNFAs();
        lexerGrammar.addArtificialMatchTokensRule();
    }

    public void analyze() {
        createGrammars();

        if(!grammarAnalyzeDirty)
            return;

        boolean oldVerbose = DecisionProbe.verbose;
        DecisionProbe.verbose = true;

        ErrorManager.setErrorListener(ErrorListener.shared());

        try {
            ErrorListener.shared().clear();
            parserGrammar.createLookaheadDFAs();
            buildNonDeterministicErrors();
        } catch(Exception e) {
            editor.console.print(e);
        }
        DecisionProbe.verbose = oldVerbose;
        grammarAnalyzeDirty = false;
    }

    protected void buildNonDeterministicErrors() {
        errors.clear();
        for (Iterator iterator = ErrorListener.shared().warnings.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            if ( o instanceof GrammarNonDeterminismMessage )
                errors.add(buildNonDeterministicError((GrammarNonDeterminismMessage)o));
        }
    }

    protected EditorGrammarError buildNonDeterministicError(GrammarNonDeterminismMessage nondetMsg) {
        EditorGrammarError error = new EditorGrammarError();

        List nonDetAlts = nondetMsg.probe.getNonDeterministicAltsForState(nondetMsg.problemState);
        error.setLine(nondetMsg.probe.dfa.getDecisionASTNode().getLine()-1);

        Set disabledAlts = nondetMsg.probe.getDisabledAlternatives(nondetMsg.problemState);
        List labels = nondetMsg.probe.getSampleNonDeterministicInputSequence(nondetMsg.problemState);
        String input = nondetMsg.probe.getInputSequenceDisplay(labels);
        error.setMessage("Decision can match input such as \""+input+"\" using multiple alternatives");

        int firstAlt = 0;

        //System.err.println("***"+error.message);

        for (Iterator iter = nonDetAlts.iterator(); iter.hasNext();) {
            Integer displayAltI = (Integer) iter.next();
            NFAState nfaStart = nondetMsg.probe.dfa.getNFADecisionStartState();

            int tracePathAlt =
                nfaStart.translateDisplayAltToWalkAlt(nondetMsg.probe.dfa, displayAltI.intValue());
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
                //System.err.println(state+"/"+state.getEnclosingRule());
                error.addRule(state.getEnclosingRule());
            }
        }

        return error;
    }

}
