package org.antlr.works.grammar.engine;

import org.antlr.tool.Grammar;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.generic.ATESyntaxParser;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.element.*;
import org.antlr.works.grammar.syntax.GrammarSyntaxLexer;
import org.antlr.works.grammar.syntax.GrammarSyntaxParser;
import org.antlr.xjlib.foundation.XJUtils;

import java.io.IOException;
import java.util.*;
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

public class GrammarProperties {

    private GrammarProperties parentProperties;
    private final List<GrammarProperties> importedProperties = new ArrayList<GrammarProperties>();

    private ElementGrammarName name;

    private List<ElementRule> rules;
    private List<ElementGroup> groups;
    private List<ElementBlock> blocks;
    private List<ElementAction> actions;
    private List<ElementReference> references;
    private List<ElementImport> imports;
    private List<ATEToken> decls;

    private final List<ElementRule> duplicateRules = new ArrayList<ElementRule>();
    private final List<ElementReference> undefinedReferences = new ArrayList<ElementReference>();
    // todo used?
    private final List<ElementRule> hasLeftRecursionRules = new ArrayList<ElementRule>();

    private final Set<String> tokenVocabNames = new HashSet<String>();
    private String tokenVocabName;

    public GrammarProperties() {
    }

    public void update(GrammarSyntaxParser parser) {
        this.rules = new ArrayList<ElementRule>(parser.rules);
        this.groups = new ArrayList<ElementGroup>(parser.groups);
        this.blocks = new ArrayList<ElementBlock>(parser.blocks);
        this.actions = new ArrayList<ElementAction>(parser.actions);
        this.references = new ArrayList<ElementReference>(parser.references);
        this.imports = new ArrayList<ElementImport>(parser.imports);
        this.decls = new ArrayList<ATEToken>(parser.decls);
        this.name = parser.name;

        for(ElementRule r : rules) {
            r.setSyntax(this);
        }
    }

    public List<ElementRule> getRules() {
        return rules;
    }

    public ElementRule getRuleWithName(String name) {
        List<ElementRule> rules = getRules();
        for (ElementRule r : rules) {
            if (r.name.equals(name))
                return r;
        }
        return null;
    }

    public List<ElementGroup> getGroups() {
        return groups;
    }

    public List<ElementBlock> getBlocks() {
        return blocks;
    }

    public List<ElementAction> getActions() {
        return actions;
    }

    public List<ElementReference> getReferences() {
        return references;
    }

    public List<ElementImport> getImports() {
        return imports;
    }

    public List<ATEToken> getDecls() {
        return decls;
    }

    public ElementGrammarName getElementName() {
        return name;
    }

    public String getName() {
        return name.getName();
    }

    public int getType() {
        return name.getType();
    }

    public boolean isParserGrammar() {
        return getType() == ElementGrammarName.PARSER;
    }

    public boolean isLexerGrammar() {
        return getType() == ElementGrammarName.LEXER;
    }

    public boolean isCombinedGrammar() {
        return getType() == ElementGrammarName.COMBINED;
    }

    public boolean isTreeParserGrammar() {
        return getType() == ElementGrammarName.TREEPARSER;
    }

    public List<String> getDeclaredTokenNames() {
        List<String> names = new ArrayList<String>();
        if(blocks != null) {
            for (ElementBlock block : blocks) {
                if (block.isTokenBlock) {
                    names.addAll(block.getDeclaredTokensAsString());
                }
            }
        }
        return names;
    }

    public List<String> getPredefinedReferences() {
        return GrammarSyntaxParser.predefinedReferences;
    }

    public synchronized String getTokenVocab() {
        if(blocks == null)
            return null;

        for (ElementBlock block : blocks) {
            if (block.isOptionsBlock)
                return block.getTokenVocab();
        }
        return null;
    }

    public synchronized List<String> getRuleNames() {
        List<String> names = new ArrayList<String>();
        if(rules != null) {
            for (ElementRule rule : rules) {
                names.add(rule.name);
            }
        }
        return names;
    }

    public synchronized ElementRule getRuleAtIndex(int index) {
        if(index < 0 || index >= rules.size())
            return null;
        else
            return rules.get(index);
    }

    public int getNumberOfRulesWithErrors() {
        int count = 0;
        if(getRules() != null) {
            for (ElementRule rule : getRules()) {
                if (rule.hasErrors())
                    count++;
            }
        }
        return count;
    }

    public int getNumberOfErrors() {
        int count = 0;
        if(getRules() != null) {
            for (ElementRule rule : getRules()) {
                if (rule.hasErrors())
                    count+=rule.getErrors().size();
            }
        }
        return count;
    }

    public List<ElementRule> getDuplicateRules() {
        return duplicateRules;
    }

    public List<ElementReference> getUndefinedReferences() {
        return undefinedReferences;
    }

    public void resetTokenVocab() {
        tokenVocabName = null;
        tokenVocabNames.clear();
    }

    public Set<String> getTokenVocabNames() {
        String tokenVocab = getTokenVocab();
        if(tokenVocab == null) {
            tokenVocabName = null;
            tokenVocabNames.clear();
            return tokenVocabNames;
        }

        if(tokenVocabName != null && tokenVocabName.equals(tokenVocab))
            return tokenVocabNames;

        tokenVocabName = tokenVocab;
        tokenVocabNames.clear();

        try {
            String file = engine.getTokenVocabFile(tokenVocabName+".tokens");
            if(file != null) {
                readTokenVocabFromFile(file, tokenVocabNames);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokenVocabNames;
    }

    public static boolean readTokenVocabFromFile(String filePath, Set<String> tokenNames) throws IOException {
        // Read the tokens from the file if it exists
        List<ATEToken> tokens = parsePropertiesString(XJUtils.getStringFromFile(filePath));
        // Add each token name to the list of tokenVocabNames
        for (ATEToken t : tokens) {
            tokenNames.add(t.getAttribute());
        }

        return true;
    }

    private static List<ATEToken> parsePropertiesString(final String content) {

        class ParseProperties extends ATESyntaxParser {

            public List<ATEToken> propertiesTokens;

            public void parseTokens() {
                propertiesTokens = new ArrayList<ATEToken>();
                while(nextToken()) {
                    if(T(0).type == ATESyntaxLexer.TOKEN_ID) {
                        if(isChar(1, "=") || isChar(1, "\n"))
                            propertiesTokens.add(T(0));
                    }
                }
            }

        }

        GrammarSyntaxLexer lexer = new GrammarSyntaxLexer();
        lexer.tokenize(content);

        ParseProperties parser = new ParseProperties();
        parser.parse(lexer.getTokens());
        return parser.propertiesTokens;
    }

    public void rebuildHasLeftRecursionRulesList() {
        if(getRules() == null)
            return;

        hasLeftRecursionRules.clear();
        for (ElementRule r : getRules()) {
            // hasLeftRecursion has a side-effect to analyze the rule
            if (r.hasLeftRecursion()) {
                hasLeftRecursionRules.add(r);
            }
        }
    }

    public void rebuildDuplicateRulesList() {
        List<ElementRule> rules = getRules();
        if(rules == null)
            return;

        List<ElementRule> sortedRules = Collections.list(Collections.enumeration(rules));
        Collections.sort(sortedRules);
        Iterator<ElementRule> iter = sortedRules.iterator();
        ElementRule currentRule = null;
        duplicateRules.clear();
        while(iter.hasNext()) {
            ElementRule nextRule = iter.next();
            if(currentRule != null && currentRule.name.equals(nextRule.name) && !duplicateRules.contains(currentRule)) {
                duplicateRules.add(currentRule);
                duplicateRules.add(nextRule);
            }
            currentRule = nextRule;
        }
    }

    public void rebuildUndefinedReferencesList() {
        List<String> existingReferences = getRuleNames();
        existingReferences.addAll(getDeclaredTokenNames());
        existingReferences.addAll(getPredefinedReferences());

        Set<String> tokenVocabNames = getTokenVocabNames();
        existingReferences.addAll(tokenVocabNames);
        // todo does it really update the list of references that this class uses?
        engine.resolveReferencesWithExternalNames(tokenVocabNames);

        undefinedReferences.clear();
        List<ElementReference> references = getReferences();
        if(references == null)
            return;

        for (ElementReference ref : references) {
            if (existingReferences.contains(ref.token.getAttribute())) continue;
            if (!getGrammarsOverriddenByRule(ref.token.getAttribute()).isEmpty()) continue;
            undefinedReferences.add(ref);
        }
    }

    public void rebuildAll() {
        rebuildDuplicateRulesList();
        rebuildUndefinedReferencesList();
        rebuildHasLeftRecursionRulesList();
    }

    public void parserDidParse() {
        rebuildAll();
    }

    public List<String> getAllGeneratedNames() throws Exception {
        List<String> names = new ArrayList<String>();
        Grammar g = engine.getAntlrGrammar().getDefaultGrammar();
        names.add(g.getRecognizerName());
        for(Grammar gd : g.getDelegates()) {
            names.add(gd.getRecognizerName());
        }

        Grammar lexer = getAntlrGrammar().getLexerGrammar();
        if(lexer != null) {
            names.add(lexer.getRecognizerName());
        }
        return names;
    }

    public void updateHierarchy(Map<String, GrammarProperties> entities) {
        importedProperties.clear();
        for(ElementImport element : imports) {
            GrammarProperties d = entities.get(element.getName()+".g");
            if(d != null) {
                d.setParent(this);
                importedProperties.add(d);
                d.updateHierarchy(entities);
            }
        }
        resetRules();
    }


    public GrammarProperties getParent() {
        return parentProperties;
    }

    public void setParent(GrammarProperties parent) {
        this.parentProperties = parent;
    }

    /**
     * Returns the list of grammars that overrides the rule specified
     * in parameter. Overrides has the same meaning than in Java: the rule
     * of a parent grammar is declared again in one or more child grammar.
     */
    public List<String> getGrammarsOverriddenByRule(String name) {
        List<String> grammars = new ArrayList<String>();
        for(GrammarProperties child : importedProperties) {
            for(ATEToken decl : child.getDecls()) {
                if(decl.getAttribute().equals(name)) {
                    grammars.add(child.getName());
                    break;
                }
            }
            grammars.addAll(child.getGrammarsOverriddenByRule(name));
        }
        return grammars;
    }

    /**
     * Returns the list of grammars that this rule overrides.
     */
    public List<String> getGrammarsOverridingRule(String name) {
        List<String> grammars = new ArrayList<String>();
        if(parentProperties != null) {
            for(ATEToken decl : parentProperties.getDecls()) {
                if(decl.getAttribute().equals(name)) {
                    grammars.add(parentProperties.getName());
                    break;
                }
            }
            grammars.addAll(parentProperties.getGrammarsOverridingRule(name));
        }
        return grammars;
    }

    public int getFirstDeclarationPosition(String name) {
        ATEToken token = getFirstDeclaration(name);
        if(token != null) {
            return token.start;
        } else {
            return -1;
        }
    }

    private ATEToken getFirstDeclaration(String name) {
        for(ATEToken decl : getDecls()) {
            if(decl.getAttribute().equals(name)) {
                return decl;
            }
        }
        return null;
    }

    public void resetRules() {
        for(ElementRule r : rules) {
            r.resetHierarchy();
        }
    }
}
