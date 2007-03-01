package org.antlr.works.syntax;

import edu.usfca.xj.foundation.XJUtils;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.generic.ATESyntaxParser;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.syntax.element.ElementReference;
import org.antlr.works.syntax.element.ElementRule;

import java.io.File;
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

    protected CEditorGrammar editor;

    protected List<ElementRule> duplicateRules;
    protected List<ElementReference> undefinedReferences;
    protected List<ElementRule> hasLeftRecursionRules;

    protected Set<String> tokenVocabNames;
    protected String tokenVocabName;

    public GrammarSyntax(CEditorGrammar editor) {
        this.editor = editor;

        duplicateRules = new ArrayList<ElementRule>();
        undefinedReferences = new ArrayList<ElementReference>();
        hasLeftRecursionRules = new ArrayList<ElementRule>();
        tokenVocabNames = new HashSet<String>();
    }

    public GrammarSyntaxEngine getParserEngine() {
        return editor.getParserEngine();
    }

    public int getNumberOfRulesWithErrors() {
        int count = 0;
        if(getParserEngine().getRules() != null) {
            for (Iterator<ElementRule> iterator = getParserEngine().getRules().iterator(); iterator.hasNext();) {
                ElementRule rule = iterator.next();
                if(rule.hasErrors())
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
            String filePath = editor.getFileFolder();
            if(filePath == null || !readTokenVocabFromFile(XJUtils.concatPath(filePath, tokenVocabName +".tokens"))) {
                // No token vocab file in the default directory. Try in the output path.
                readTokenVocabFromFile(XJUtils.concatPath(AWPrefs.getOutputPath(), tokenVocabName +".tokens"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokenVocabNames;
    }

    private boolean readTokenVocabFromFile(String filePath) throws IOException {
        if(!new File(filePath).exists())
            return false;

        // Read the tokens from the file if it exists
        List<ATEToken> tokens = parsePropertiesString(XJUtils.getStringFromFile(filePath));
        // Add each token name to the list of tokenVocabNames
        for(int index=0; index<tokens.size(); index++) {
            ATEToken t = tokens.get(index);
            tokenVocabNames.add(t.getAttribute());
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
        for(Iterator<ElementRule> iter = getParserEngine().getRules().iterator(); iter.hasNext();) {
            ElementRule r = iter.next();
            if(r.hasLeftRecursion())
                hasLeftRecursionRules.add(r);
        }
    }

    public void rebuildDuplicateRulesList() {
        List<ElementRule> rules = getParserEngine().getRules();
        if(rules == null)
            return;

        List<ElementRule> sortedRules = Collections.list(Collections.enumeration(rules));
        Collections.sort(sortedRules);
        Iterator iter = sortedRules.iterator();
        ElementRule currentRule = null;
        duplicateRules.clear();
        while(iter.hasNext()) {
            ElementRule nextRule = (ElementRule) iter.next();
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
        editor.getParser().resolveReferencesWithExternalNames(tokenVocabNames);

        undefinedReferences.clear();
        List<ElementReference> references = getParserEngine().getReferences();
        if(references == null)
            return;

        for(int index=0; index<references.size(); index++) {
            ElementReference ref = references.get(index);
            if(!existingReferences.contains(ref.token.getAttribute()))
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
