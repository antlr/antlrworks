package org.antlr.works.syntax;

import edu.usfca.xj.foundation.XJUtils;
import org.antlr.works.ate.syntax.ATEParserEngine;
import org.antlr.works.ate.syntax.ATEToken;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.parser.Parser;
import org.antlr.works.parser.ParserReference;
import org.antlr.works.parser.ParserRule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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

public class Syntax {

    protected CEditorGrammar editor;
    protected ATEParserEngine parserEngine;

    protected List duplicateRules;
    protected List undefinedReferences;
    protected List hasLeftRecursionRules;

    protected List tokenVocabNames;
    protected String tokenVocabName;

    public Syntax(CEditorGrammar editor, ATEParserEngine parserEngine) {
        this.editor = editor;
        this.parserEngine = parserEngine;

        duplicateRules = new ArrayList();
        undefinedReferences = new ArrayList();
        hasLeftRecursionRules = new ArrayList();
        tokenVocabNames = new ArrayList();
    }

    public int getNumberOfRulesWithErrors() {
        int count = 0;
        if(parserEngine.getRules() != null) {
            for (Iterator iterator = parserEngine.getRules().iterator(); iterator.hasNext();) {
                ParserRule rule = (ParserRule) iterator.next();
                if(rule.hasErrors())
                    count++;
            }
        }
        return count;
    }

    public boolean isDuplicateRule(String rule) {
        for(Iterator iterator = duplicateRules.iterator(); iterator.hasNext(); ) {
            ParserRule r = (ParserRule)iterator.next();
            if(r.name.equals(rule))
                return true;
        }
        return false;
    }

    public boolean isDuplicateRule(ParserRule rule) {
        return duplicateRules.contains(rule);
    }

    public List getHasLeftRecursionRules() {
        return hasLeftRecursionRules;
    }

    public List getDuplicateRules() {
        return duplicateRules;
    }

    public boolean isUndefinedReference(ATEToken t) {
        for(int index=0; index<undefinedReferences.size(); index++) {
            ParserReference ref = (ParserReference)undefinedReferences.get(index);
            if(ref.token.equals(t))
                return true;
        }
        return false;
    }

    public List getUndefinedReferences() {
        return undefinedReferences;
    }

    public List getReferences() {
        return parserEngine.getReferences();
    }

    public void resetTokenVocab() {
        tokenVocabName = null;
        tokenVocabNames.clear();
    }

    public List getTokenVocabNames() {
        String tokenVocab = parserEngine.getTokenVocab();
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
            if(filePath == null)
                // May be null if the file hasn't been saved
                return tokenVocabNames;

            filePath = XJUtils.concatPath(filePath, tokenVocabName +".tokens");
            if(new File(filePath).exists()) {
                // Read the tokens from the file if it exists
                List tokens = Parser.parsePropertiesString(XJUtils.getStringFromFile(filePath));
                // Add each token name to the list of tokenVocabNames
                for(int index=0; index<tokens.size(); index++) {
                    ATEToken t = (ATEToken)tokens.get(index);
                    tokenVocabNames.add(t.getAttribute());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokenVocabNames;
    }

    public void rebuildHasLeftRecursionRulesList() {
        if(parserEngine.getRules() == null)
            return;

        hasLeftRecursionRules.clear();
        for(Iterator iter = parserEngine.getRules().iterator(); iter.hasNext();) {
            ParserRule r = (ParserRule)iter.next();
            if(r.hasLeftRecursion())
                hasLeftRecursionRules.add(r);
        }
    }

    public void rebuildDuplicateRulesList() {
        List rules = parserEngine.getRules();
        if(rules == null)
            return;

        List sortedRules = Collections.list(Collections.enumeration(rules));
        Collections.sort(sortedRules);
        Iterator iter = sortedRules.iterator();
        ParserRule currentRule = null;
        duplicateRules.clear();
        while(iter.hasNext()) {
            ParserRule nextRule = (ParserRule) iter.next();
            if(currentRule != null && currentRule.name.equals(nextRule.name) && !duplicateRules.contains(currentRule)) {
                duplicateRules.add(currentRule);
                duplicateRules.add(nextRule);
            }
            currentRule = nextRule;
        }
    }

    public void rebuildUndefinedReferencesList() {
        List existingReferences = parserEngine.getRuleNames();
        existingReferences.addAll(parserEngine.getDeclaredTokenNames());
        existingReferences.addAll(parserEngine.getPredefinedReferences());
        existingReferences.addAll(getTokenVocabNames());

        undefinedReferences.clear();
        List references = parserEngine.getReferences();
        if(references == null)
            return;

        for(int index=0; index<references.size(); index++) {
            ParserReference ref = (ParserReference)references.get(index);
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
