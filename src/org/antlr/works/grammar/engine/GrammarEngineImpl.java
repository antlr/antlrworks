package org.antlr.works.grammar.engine;

import org.antlr.Tool;
import org.antlr.tool.Grammar;
import org.antlr.works.ate.syntax.generic.ATESyntaxEngine;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.antlr.ANTLRGrammarEngine;
import org.antlr.works.grammar.antlr.ANTLRGrammarEngineImpl;
import org.antlr.works.grammar.antlr.ANTLRGrammarResult;
import org.antlr.works.grammar.element.*;
import org.antlr.works.grammar.syntax.GrammarSyntaxEngine;

import java.util.List;

/*

[The "BSD licence"]
Copyright (c) 2005-07 Jean Bovet
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

public class GrammarEngineImpl implements GrammarEngine {

    private GrammarEngineDelegate delegate;

    private final GrammarProperties properties = new GrammarProperties();
    private final ANTLRGrammarEngine antlrEngine = new ANTLRGrammarEngineImpl();
    private final GrammarSyntaxEngine syntaxEngine = new GrammarSyntaxEngine();

    public GrammarEngineImpl(GrammarEngineDelegate delegate) {
        this.delegate = delegate;

        properties.setGrammarEngine(this);
        properties.setSyntaxEngine(syntaxEngine);
        properties.setAntlrEngine(antlrEngine);
        antlrEngine.setGrammarEngine(this);
    }

    public void close() {
        antlrEngine.close();
        syntaxEngine.close();
    }

    public GrammarProperties getGrammarProperties() {
        return properties;
    }

    public ANTLRGrammarEngine getANTLRGrammarEngine() {
        return antlrEngine;
    }

    public ATESyntaxEngine getSyntaxEngine() {
        return syntaxEngine;
    }

    public int getType() {
        return properties.getType();
    }

    public ElementGrammarName getElementName() {
        return properties.getElementName();
    }

    public String getGrammarName() {
        return properties.getName();
    }

    public String getGrammarLanguage() {
        try {
            antlrEngine.createGrammars();
            Grammar g = antlrEngine.getParserGrammar();
            if(g == null) {
                g = antlrEngine.getLexerGrammar();
            }
            if(g != null) {
                return (String)g.getOption("language");
            }
        } catch (Exception e) {
            delegate.reportError(e);
        }
        return null;
    }

    public List<ElementRule> getRules() {
        return properties.getRules();
    }

    public ElementRule getRuleWithName(String name) {
        return properties.getRuleWithName(name);
    }

    public List<ElementRule> getDuplicateRules() {
        return properties.getDuplicateRules();
    }

    public ElementRule getRuleAtIndex(int index) {
        return properties.getRuleAtIndex(index);
    }

    public List<ElementReference> getReferences() {
        return properties.getReferences();
    }

    public List<ElementReference> getUndefinedReferences() {
        return properties.getUndefinedReferences();
    }

    public List<ElementImport> getImports() {
        return properties.getImports();
    }

    public List<ElementAction> getActions() {
        return properties.getActions();
    }

    public List<ElementGroup> getGroups() {
        return properties.getGroups();
    }

    public List<ElementBlock> getBlocks() {
        return properties.getBlocks();
    }

    public int getNumberOfLines() {
        return syntaxEngine.getMaxLines();
    }

    public int getNumberOfRules() {
        return properties.getRules().size();
    }

    public int getNumberOfErrors() {
        return properties.getNumberOfErrors();
    }

    public String getTokenVocab() {
        return properties.getTokenVocab();
    }

    public List<String> getAllGeneratedNames() throws Exception {
        return properties.getAllGeneratedNames();
    }

    public int getFirstDeclarationPosition(String name) {
        return properties.getFirstDeclarationPosition(name);
    }

    public List<String> getGrammarsOverriddenByRule(String name) {
        return properties.getGrammarsOverriddenByRule(name);
    }

    public List<String> getGrammarsOverridingRule(String name) {
        return properties.getGrammarsOverridingRule(name);
    }

    public List<ATEToken> getTokens() {
        return syntaxEngine.getTokens();
    }

    public ANTLRGrammarResult analyze() throws Exception {
        return antlrEngine.analyze();
    }

    public void cancelAnalyze() {
        antlrEngine.cancel();
    }

    public void computeRuleErrors(ElementRule rule) {
        antlrEngine.computeRuleErrors(rule);
    }

    public void parseDidParse() {
        properties.parserDidParse();
    }

    public void markDirty() {
        antlrEngine.markDirty();
    }

    public void reset() {
        properties.resetTokenVocab();
        properties.rebuildAll();
    }

    public boolean isVersion2() {
        // Check to see if "class" and "extends" are in the grammar text which
        // means that the grammar is probably an ANTLR version 2 grammar.
        List<ATEToken> tokens = getTokens();
        for(int index=0; index<tokens.size(); index++) {
            ATEToken t = tokens.get(index);
            if(t.type == ATESyntaxLexer.TOKEN_ID && t.getAttribute().equals("class")) {
                if(index+2<tokens.size()) {
                    ATEToken t2 = tokens.get(index+2);
                    if(t2.type == ATESyntaxLexer.TOKEN_ID && t2.getAttribute().equals("extends")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isCombinedGrammar() {
        return properties.isCombinedGrammar();
    }

    public boolean isTreeParserGrammar() {
        return properties.isTreeParserGrammar();
    }

    public void antlrGrammarEngineAnalyzeCompleted() {
        delegate.engineAnalyzeCompleted();
    }

    public String getGrammarFileName() {
        return delegate.getGrammarFileName();
    }

    public String getGrammarText() {
        return delegate.getGrammarText();
    }

    public String getTokenVocabFile(String name) {
        return delegate.getTokenVocabFile(name);
    }

    public Tool getANTLRTool() {
        return delegate.getANTLRTool();
    }

    public void reportError(String error) {
        delegate.reportError(error);
    }

    public void gotoToRule(String grammar, String name) {
        delegate.gotoToRule(grammar, name);
    }
}
