package org.antlr.works.editor;

import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.editor.idea.IdeaAction;
import org.antlr.works.grammar.RefactorEngine;
import org.antlr.works.grammar.decisiondfa.DecisionDFAEngine;
import org.antlr.works.grammar.element.*;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.xjlib.foundation.XJUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

public class EditorInspector {

    private GrammarEngine engine;
    private DecisionDFAEngine decisionDFAEngine;
    private InspectorDelegate delegate;

    public EditorInspector(GrammarEngine engine, DecisionDFAEngine decisionDFAEngine, InspectorDelegate delegate) {
        this.engine = engine;
        this.decisionDFAEngine = decisionDFAEngine;
        this.delegate = delegate;
    }

    public void close() {
        engine = null;
        decisionDFAEngine = null;
        delegate = null;
    }

    public List<EditorInspectorItem> getErrors() {
        List<EditorInspectorItem> errors = new ArrayList<EditorInspectorItem>();
        discoverInvalidGrammarName(errors);
        discoverInvalidCharLiteralTokens(errors);
        discoverUndefinedReferences(errors);
        discoverUndefinedImports(errors);
        discoverDuplicateRules(errors);
        return errors;
    }

    public List<EditorInspectorItem> getWarnings() {
        List<EditorInspectorItem> warnings = new ArrayList<EditorInspectorItem>();
        discoverLeftRecursionRules(warnings);
        discoverLeftRecursiveRulesSet(warnings);
        return warnings;
    }

    public List<EditorInspectorItem> getDecisionDFAs() {
        List<EditorInspectorItem> items = new ArrayList<EditorInspectorItem>();
        discoverDecisionDFAs(items);
        return items;
    }

    protected List<EditorInspectorItem> getAllItemsAtIndex(int index) {
        List<EditorInspectorItem> items = new ArrayList<EditorInspectorItem>();
        items.addAll(getItemsAtIndex(getErrors(), index));
        items.addAll(getItemsAtIndex(getWarnings(), index));
        items.addAll(getItemsAtIndex(getDecisionDFAs(), index));
        return items;
    }

    protected List<EditorInspectorItem> getItemsAtIndex(List<EditorInspectorItem> items, int index) {
        List<EditorInspectorItem> filteredItems = new ArrayList<EditorInspectorItem>();
        for (EditorInspectorItem item : items) {
            if (index >= item.startIndex && index <= item.endIndex)
                filteredItems.add(item);
        }
        return filteredItems;
    }

    protected void discoverInvalidGrammarName(List<EditorInspectorItem> items) {
        ElementGrammarName n = getGrammarName();
        String grammarFileName = getGrammarNameFromFile();
        if(n != null && grammarFileName != null && !grammarFileName.equals(n.getName())) {
            ATEToken t = n.name;
            EditorInspectorItem item = new ItemInvalidGrammarName();
            item.setAttributes(t, t.getStartIndex(), t.getEndIndex(),
                    t.startLineNumber, Color.red,
                    "Invalid grammar name '"+t.getAttribute()+"'");
            items.add(item);
        }
    }

    private ElementGrammarName getGrammarName() {
        return engine.getElementName();
    }

    private String getGrammarNameFromFile() {
        String filename = delegate.getFileName();
        if(filename == null) {
            return null;
        }
        return XJUtils.getPathByDeletingPathExtension(filename);
    }

    protected void discoverInvalidCharLiteralTokens(List<EditorInspectorItem> items) {
        List<ATEToken> tokens = engine.getTokens();
        if(tokens == null)
            return;

        for (ATEToken t : tokens) {
            if (t.type == ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING) {
                if (RefactorEngine.ignoreScopeForDoubleQuoteLiteral(t.scope)) continue;

                EditorInspectorItem item = new ItemInvalidCharLiteral();
                item.setAttributes(t, t.getStartIndex(), t.getEndIndex(),
                        t.startLineNumber, Color.red,
                        "Invalid character literal '" + t.getAttribute() + "' - must use single quote");
                items.add(item);
            }
        }
    }

    protected void discoverUndefinedReferences(List<EditorInspectorItem> items) {
        List<ElementReference> undefinedRefs = engine.getUndefinedReferences();
        if(undefinedRefs == null)
            return;

        for (ElementReference ref : undefinedRefs) {
            EditorInspectorItem item = new ItemUndefinedReference();
            item.setAttributes(ref.token, ref.token.getStartIndex(), ref.token.getEndIndex(),
                    ref.token.startLineNumber, Color.red,
                    "Undefined reference \"" + ref.token.getAttribute() + "\"");
            items.add(item);
        }
    }

    protected void discoverUndefinedImports(List<EditorInspectorItem> items) {
        List<ElementImport> imports = engine.getUndefinedImports();
        if(imports == null)
            return;

        for (ElementImport ref : imports) {
            EditorInspectorItem item = new ItemUndefinedImport();
            item.setAttributes(ref.token, ref.token.getStartIndex(), ref.token.getEndIndex(),
                    ref.token.startLineNumber, Color.red,
                    "Undefined import \"" + ref.token.getAttribute() + "\"");
            items.add(item);
        }
    }

    protected void discoverDuplicateRules(List<EditorInspectorItem> items) {
        List<ElementRule> rules = engine.getDuplicateRules();
        if(rules == null)
            return;

        for (ElementRule rule : rules) {
            EditorInspectorItem item = new ItemDuplicateRule();
            item.setAttributes(rule.start, rule.start.getStartIndex(), rule.start.getEndIndex(),
                    rule.start.startLineNumber, Color.red,
                    "Duplicate rule \"" + rule.name + "\"");
            items.add(item);
        }
    }

    protected void discoverLeftRecursionRules(List<EditorInspectorItem> items) {
        List<ElementRule> rules = engine.getRules();
        if(rules == null)
            return;

        for (ElementRule rule : rules) {
            if (!rule.hasLeftRecursion())
                continue;

            EditorInspectorItem item = new ItemLeftRecursion();
            item.setAttributes(rule.start, rule.start.getStartIndex(), rule.start.getEndIndex(),
                    rule.start.startLineNumber, Color.blue,
                    "Rule \"" + rule.name + "\" is left-recursive");
            items.add(item);
        }
    }

    protected void discoverLeftRecursiveRulesSet(List<EditorInspectorItem> items) {
        List<ElementRule> rules = engine.getRules();
        if(rules == null)
            return;

        for (ElementRule rule : rules) {
            Set rulesSet = rule.getLeftRecursiveRulesSet();
            if (rulesSet == null || rulesSet.size() < 2)
                continue;

            EditorInspectorItem item = new EditorInspectorItem();
            item.setAttributes(rule.start, rule.start.getStartIndex(), rule.start.getEndIndex(),
                    rule.start.startLineNumber, Color.blue,
                    "Rule \"" + rule.name + "\" is mutually left-recursive with other rules (see Console)");
            items.add(item);
        }
    }

    protected void discoverDecisionDFAs(List<EditorInspectorItem> items) {
        items.addAll(decisionDFAEngine.getDecisionDFAItems());
    }

    public class ItemUndefinedReference extends EditorInspectorItem {

        public List<IdeaAction> getIdeaActions() {
            List<IdeaAction> actions = new ArrayList<IdeaAction>();
            actions.add(new IdeaAction("Create rule '"+token.getAttribute()+"'", this, IDEA_CREATE_RULE, token));
            return actions;
        }

        public void ideaActionFire(IdeaAction action, int actionID) {
            switch(actionID) {
                case IDEA_CREATE_RULE:
                    delegate.createRuleAtIndex(((ElementToken)action.token).lexer, action.token.getAttribute(), null);
                    break;
            }
        }

    }

    public class ItemUndefinedImport extends EditorInspectorItem {

        public List<IdeaAction> getIdeaActions() {
            List<IdeaAction> actions = new ArrayList<IdeaAction>();
            actions.add(new IdeaAction("Create file '"+token.getAttribute()+"'", this, IDEA_CREATE_FILE, token));
            return actions;
        }

        public void ideaActionFire(IdeaAction action, int actionID) {
            switch(actionID) {
                case IDEA_CREATE_FILE:
                    delegate.createFile(action.token.getAttribute());
                    break;
            }
        }

    }

    public class ItemDuplicateRule extends EditorInspectorItem {

        public List<IdeaAction> getIdeaActions() {
            List<IdeaAction> actions = new ArrayList<IdeaAction>();
            actions.add(new IdeaAction("Delete rule '"+token.getAttribute()+"'", this, IDEA_DELETE_RULE, token));
            return actions;
        }

        public void ideaActionFire(IdeaAction action, int actionID) {
            switch(actionID) {
                case IDEA_DELETE_RULE:
                    delegate.deleteRuleAtCurrentPosition();
                    break;
            }
        }
    }

    public class ItemLeftRecursion extends EditorInspectorItem {

        public List<IdeaAction> getIdeaActions() {
            List<IdeaAction> actions = new ArrayList<IdeaAction>();
            actions.add(new IdeaAction("Remove left recursion of rule '"+token.getAttribute()+"'", this, IDEA_REMOVE_LEFT_RECURSION, token));
            return actions;
        }

        public void ideaActionFire(IdeaAction action, int actionID) {
            switch(actionID) {
                case IDEA_REMOVE_LEFT_RECURSION:
                    delegate.removeLeftRecursion();
                    break;
            }
        }
    }

    public class ItemInvalidCharLiteral extends EditorInspectorItem {

        public List<IdeaAction> getIdeaActions() {
            List<IdeaAction> actions = new ArrayList<IdeaAction>();
            actions.add(new IdeaAction("Convert literals to single quote", this, IDEA_CONVERT_TO_SINGLE_QUOTE, token));
            return actions;
        }

        public void ideaActionFire(IdeaAction action, int actionID) {
            switch(actionID) {
                case IDEA_CONVERT_TO_SINGLE_QUOTE:
                    delegate.convertLiteralsToSingleQuote();
                    break;
            }
        }
    }

    public class ItemInvalidGrammarName extends EditorInspectorItem {

        public List<IdeaAction> getIdeaActions() {
            List<IdeaAction> actions = new ArrayList<IdeaAction>();
            actions.add(new IdeaAction("Change grammar name to '"+getGrammarNameFromFile()+"'", this, IDEA_FIX_GRAMMAR_NAME, token));
            return actions;
        }

        public void ideaActionFire(IdeaAction action, int actionID) {
            switch(actionID) {
                case IDEA_FIX_GRAMMAR_NAME:
                    ElementGrammarName n = getGrammarName();
                    ATEToken name = n.name;
                    delegate.replaceText(name.start, name.end, getGrammarNameFromFile());
                    break;
            }
        }
    }

}
