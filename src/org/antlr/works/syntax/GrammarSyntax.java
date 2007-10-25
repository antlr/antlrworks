package org.antlr.works.syntax;

import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.generic.ATESyntaxParser;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.syntax.element.ElementReference;
import org.antlr.works.syntax.element.ElementRule;
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

public class GrammarSyntax {

    private List<ElementRule> duplicateRules = new ArrayList<ElementRule>();
    private List<ElementReference> undefinedReferences = new ArrayList<ElementReference>();
    private List<ElementRule> hasLeftRecursionRules = new ArrayList<ElementRule>();

    private Set<String> tokenVocabNames = new HashSet<String>();
    private String tokenVocabName;

    private GrammarSyntaxDelegate delegate;

    public GrammarSyntax(GrammarSyntaxDelegate delegate) {
        this.delegate = delegate;
    }

    public void close() {
        delegate = null;
    }

    public GrammarSyntaxEngine getParserEngine() {
        return delegate.getParserEngine();
    }

    public int getNumberOfRulesWithErrors() {
        int count = 0;
        if(getParserEngine().getRules() != null) {
            for (ElementRule rule : getParserEngine().getRules()) {
                if (rule.hasErrors())
                    count++;
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
        String tokenVocab = getParserEngine().getTokenVocab();
        if(tokenVocab == null) {
            tokenVocabNames.clear();
            return tokenVocabNames;
        }

        if(tokenVocabName != null && tokenVocabName.equals(tokenVocab))
            return tokenVocabNames;

        tokenVocabName = tokenVocab;
        tokenVocabNames.clear();

        try {
            String file = delegate.getTokenVocabFile(tokenVocabName+".tokens");
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
        if(getParserEngine().getRules() == null)
            return;

        hasLeftRecursionRules.clear();
        for (ElementRule r : getParserEngine().getRules()) {
            // hasLeftRecursion has a side-effect to analyze the rule
            if (r.hasLeftRecursion()) {
                hasLeftRecursionRules.add(r);
            }
        }
    }

    public void rebuildDuplicateRulesList() {
        List<ElementRule> rules = getParserEngine().getRules();
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
        List<String> existingReferences = getParserEngine().getRuleNames();
        existingReferences.addAll(getParserEngine().getDeclaredTokenNames());
        existingReferences.addAll(getParserEngine().getPredefinedReferences());

        Set<String> tokenVocabNames = getTokenVocabNames();
        existingReferences.addAll(tokenVocabNames);
        delegate.getParser().resolveReferencesWithExternalNames(tokenVocabNames);

        undefinedReferences.clear();
        List<ElementReference> references = getParserEngine().getReferences();
        if(references == null)
            return;

        for (ElementReference ref : references) {
            if (!existingReferences.contains(ref.token.getAttribute()))
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

}
