package org.antlr.works.editor;

import edu.usfca.xj.foundation.XJUtils;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.misc.ATEScope;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.idea.IdeaAction;
import org.antlr.works.idea.IdeaActionDelegate;
import org.antlr.works.syntax.GrammarSyntax;
import org.antlr.works.syntax.element.*;

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

    private GrammarSyntax syntax;
    private InspectorDelegate delegate;

    public EditorInspector(GrammarSyntax syntax, InspectorDelegate delegate) {
        this.syntax = syntax;
        this.delegate = delegate;
    }

    public List<Item> getErrors() {
        List<Item> errors = new ArrayList<Item>();
        discoverInvalidGrammarName(errors);
        discoverInvalidCharLiteralTokens(errors);
        discoverUndefinedReferences(errors);
        discoverDuplicateRules(errors);
        return errors;
    }

    public List<Item> getWarnings() {
        List<Item> warnings = new ArrayList<Item>();
        discoverLeftRecursionRules(warnings);
        discoverLeftRecursiveRulesSet(warnings);
        return warnings;
    }

    public List<Item> getErrorsAtIndex(int index) {
        return getItemsAtIndex(getErrors(), index);
    }

    public List<Item> getWarningsAtIndex(int index) {
        return getItemsAtIndex(getWarnings(), index);
    }

    protected List<Item> getAllItemsAtIndex(int index) {
        List<Item> items = new ArrayList<Item>();
        items.addAll(getErrorsAtIndex(index));
        items.addAll(getWarningsAtIndex(index));
        return items;
    }

    protected List<Item> getItemsAtIndex(List<Item> items, int index) {
        List<Item> filteredItems = new ArrayList<Item>();
        for (Item item : items) {
            if (index >= item.startIndex && index <= item.endIndex)
                filteredItems.add(item);
        }
        return filteredItems;
    }

    protected void discoverInvalidGrammarName(List<Item> items) {
        ElementGrammarName n = getGrammarName();
        String grammarFileName = getGrammarNameFromFile();
        if(n != null && grammarFileName != null && !grammarFileName.equals(n.getName())) {
            ATEToken t = n.name;
            Item item = new ItemInvalidGrammarName();
            item.setAttributes(t, t.getStartIndex(), t.getEndIndex(),
                    t.startLineNumber, Color.red,
                    "Invalid grammar name '"+t.getAttribute()+"'");
            items.add(item);
        }
    }

    private ElementGrammarName getGrammarName() {
        return syntax.getParserEngine().getName();
    }

    private String getGrammarNameFromFile() {
        String filename = delegate.getFileName();
        if(filename == null) {
            return null;
        }
        return XJUtils.getPathByDeletingPathExtension(filename);
    }

    protected void discoverInvalidCharLiteralTokens(List<Item> items) {
        List<ATEToken> tokens = syntax.getParserEngine().getTokens();
        if(tokens == null)
            return;

        for (ATEToken t : tokens) {
            if (t.type == ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING) {
                if (ignoreScopeForDoubleQuoteLiteral(t.scope)) continue;

                Item item = new ItemInvalidCharLiteral();
                item.setAttributes(t, t.getStartIndex(), t.getEndIndex(),
                        t.startLineNumber, Color.red,
                        "Invalid character literal '" + t.getAttribute() + "' - must use single quote");
                items.add(item);
            }
        }
    }

    /**
     * Returns true if the scope should be ignored when checking for double-quote literal
     */
    private boolean ignoreScopeForDoubleQuoteLiteral(ATEScope scope) {
        if(scope == null) return false;

        Class<? extends Object> c = scope.getClass();
        return c.equals(ElementAction.class) || c.equals(ElementBlock.class) || c.equals(ElementRewriteBlock.class)
                || c.equals(ElementRewriteFunction.class) || c.equals(ElementArgumentBlock.class);
    }

    protected void discoverUndefinedReferences(List<Item> items) {
        List<ElementReference> undefinedRefs = syntax.getUndefinedReferences();
        if(undefinedRefs == null)
            return;

        for (ElementReference ref : undefinedRefs) {
            Item item = new ItemUndefinedReference();
            item.setAttributes(ref.token, ref.token.getStartIndex(), ref.token.getEndIndex(),
                    ref.token.startLineNumber, Color.red,
                    "Undefined reference \"" + ref.token.getAttribute() + "\"");
            items.add(item);
        }
    }

    protected void discoverDuplicateRules(List<Item> items) {
        List<ElementRule> rules = syntax.getDuplicateRules();
        if(rules == null)
            return;

        for (ElementRule rule : rules) {
            Item item = new ItemDuplicateRule();
            item.setAttributes(rule.start, rule.start.getStartIndex(), rule.start.getEndIndex(),
                    rule.start.startLineNumber, Color.red,
                    "Duplicate rule \"" + rule.name + "\"");
            items.add(item);
        }
    }

    protected void discoverLeftRecursionRules(List<Item> items) {
        List<ElementRule> rules = syntax.getParserEngine().getRules();
        if(rules == null)
            return;

        for (ElementRule rule : rules) {
            if (!rule.hasLeftRecursion())
                continue;

            Item item = new ItemLeftRecursion();
            item.setAttributes(rule.start, rule.start.getStartIndex(), rule.start.getEndIndex(),
                    rule.start.startLineNumber, Color.blue,
                    "Rule \"" + rule.name + "\" is left-recursive");
            items.add(item);
        }
    }

    protected void discoverLeftRecursiveRulesSet(List<Item> items) {
        List<ElementRule> rules = syntax.getParserEngine().getRules();
        if(rules == null)
            return;

        for (ElementRule rule : rules) {
            Set rulesSet = rule.getLeftRecursiveRulesSet();
            if (rulesSet == null || rulesSet.size() < 2)
                continue;

            Item item = new Item();
            item.setAttributes(rule.start, rule.start.getStartIndex(), rule.start.getEndIndex(),
                    rule.start.startLineNumber, Color.blue,
                    "Rule \"" + rule.name + "\" is mutually left-recursive with other rules (see Console)");
            items.add(item);
        }
    }

    public class Item implements IdeaActionDelegate {

        public static final int IDEA_DELETE_RULE = 0;
        public static final int IDEA_CREATE_RULE = 1;
        public static final int IDEA_REMOVE_LEFT_RECURSION = 2;
        public static final int IDEA_CONVERT_TO_SINGLE_QUOTE = 3;
        public static final int IDEA_FIX_GRAMMAR_NAME = 4;

        public ATEToken token;
        public int startIndex;
        public int endIndex;
        public int startLineNumber;
        public Color color;
        public String description;

        public void setAttributes(ATEToken token, int startIndex, int endIndex, int startLineNumber, Color color, String description) {
            this.token = token;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.startLineNumber = startLineNumber;
            this.color = color;
            this.description = description;
        }

        public List<IdeaAction> getIdeaActions() {
            return null;
        }

        public void ideaActionFire(IdeaAction action, int actionID) {
        }

    }

    public class ItemUndefinedReference extends Item {

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

    public class ItemDuplicateRule extends Item {

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

    public class ItemLeftRecursion extends Item {

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

    public class ItemInvalidCharLiteral extends Item {

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

    public class ItemInvalidGrammarName extends Item {

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
