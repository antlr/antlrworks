package org.antlr.works.editor.helper;

import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.idea.IdeaAction;
import org.antlr.works.editor.idea.IdeaActionDelegate;
import org.antlr.works.parser.Lexer;
import org.antlr.works.parser.ParserReference;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.parser.Token;

import java.awt.*;
import java.util.ArrayList;
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

public class EditorInspector {

    public EditorWindow editor;

    public EditorInspector(EditorWindow editor) {
        this.editor = editor;
    }

    public void refresh() {
        // @todo cache errors/warnings
    }

    public List getErrors() {
        List errors = new ArrayList();
        discoverInvalidCharLiteralTokens(errors);
        discoverUndefinedReferences(errors);
        discoverDuplicateRules(errors);
        return errors;
    }

    public List getWarnings() {
        List warnings = new ArrayList();
        discoverLeftRecursionRules(warnings);
        return warnings;
    }

    public List getErrorsAtIndex(int index) {
        return getItemsAtIndex(getErrors(), index);
    }

    public List getWarningsAtIndex(int index) {
        return getItemsAtIndex(getWarnings(), index);
    }

    protected List getAllItemsAtIndex(int index) {
        List items = new ArrayList();
        items.addAll(getErrorsAtIndex(index));
        items.addAll(getWarningsAtIndex(index));
        return items;
    }

    protected List getItemsAtIndex(List items, int index) {
        List filteredItems = new ArrayList();
        for(int i=0; i<items.size(); i++) {
            Item item = (Item)items.get(i);
            if(index >= item.startIndex && index <= item.endIndex)
                filteredItems.add(item);
        }
        return filteredItems;
    }

    protected void discoverInvalidCharLiteralTokens(List items) {
        List tokens = editor.getTokens();
        if(tokens == null)
            return;

        for(int index=0; index<tokens.size(); index++) {
            Token t = (Token)tokens.get(index);
            if(t.type == Lexer.TOKEN_SINGLE_QUOTE_STRING) {
                String a = t.getAttribute();
                String c = a.substring(1, a.length()-1);
                if(c.length() <= 1)
                    continue;

                if(c.length() == 2 && c.charAt(0) == '\\')
                    continue;

                if(c.length() == 6 && c.charAt(0) == '\\' && c.charAt(1) == 'u')
                    continue;

                Item item = new Item();
                item.setAttributes(t, t.getStartIndex(), t.getEndIndex(),
                        t.startLineNumber, Color.red,
                        "Invalid character literal '"+t.getAttribute()+"'");
                items.add(item);
            }
        }
    }

    protected void discoverUndefinedReferences(List items) {
        List undefinedRefs = editor.rules.getUndefinedReferences();
        if(undefinedRefs == null)
            return;

        for(int index=0; index<undefinedRefs.size(); index++) {
            ParserReference ref = (ParserReference)undefinedRefs.get(index);
            Item item = new ItemUndefinedReference();
            item.setAttributes(ref.token, ref.token.getStartIndex(), ref.token.getEndIndex(),
                    ref.token.startLineNumber, Color.red,
                    "Undefined reference \""+ref.token.getAttribute()+"\"");
            items.add(item);
        }
    }

    protected void discoverDuplicateRules(List items) {
        List rules = editor.rules.getDuplicateRules();
        if(rules == null)
            return;

        for(int index=0; index<rules.size(); index++) {
            ParserRule rule = (ParserRule)rules.get(index);
            Item item = new ItemDuplicateRule();
            item.setAttributes(rule.start, rule.start.getStartIndex(), rule.start.getEndIndex(),
                    rule.start.startLineNumber, Color.red,
                    "Duplicate rule \""+rule.name+"\"");
            items.add(item);
        }
    }

    protected void discoverLeftRecursionRules(List items) {
        List rules = editor.rules.getRules();
        if(rules == null)
            return;

        for(int index=0; index<rules.size(); index++) {
            ParserRule rule = (ParserRule)rules.get(index);
            if(!rule.hasLeftRecursion())
                continue;

            Item item = new ItemLeftRecursion();
            item.setAttributes(rule.start, rule.start.getStartIndex(), rule.start.getEndIndex(),
                    rule.start.startLineNumber, Color.blue,
                    "Left recursion for rule \""+rule.name+"\"");
            items.add(item);
        }
    }

    public class Item implements IdeaActionDelegate {

        public static final int IDEA_DELETE_RULE = 0;
        public static final int IDEA_CREATE_RULE = 1;
        public static final int IDEA_REMOVE_LEFT_RECURSION = 2;

        public Token token;
        public int startIndex;
        public int endIndex;
        public int startLineNumber;
        public Color color;
        public String description;

        public void setAttributes(Token token, int startIndex, int endIndex, int startLineNumber, Color color, String description) {
            this.token = token;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.startLineNumber = startLineNumber;
            this.color = color;
            this.description = description;
        }

        public List getIdeaActions() {
            return null;
        }

        public void ideaActionFire(IdeaAction action, int actionID) {
        }

    }

    public class ItemUndefinedReference extends Item {

        public List getIdeaActions() {
            List actions = new ArrayList();
            actions.add(new IdeaAction("Create rule '"+token.getAttribute()+"'", this, IDEA_CREATE_RULE, token));
            return actions;
        }

        public void ideaActionFire(IdeaAction action, int actionID) {
            switch(actionID) {
                case IDEA_CREATE_RULE:
                    editor.actionsRefactor.createRuleAtIndex(action.token.isLexer(), action.token.getAttribute(), null);
                    break;
            }
        }

    }

    public class ItemDuplicateRule extends Item {

        public List getIdeaActions() {
            List actions = new ArrayList();
            actions.add(new IdeaAction("Delete rule '"+token.getAttribute()+"'", this, IDEA_DELETE_RULE, token));
            return actions;
        }

        public void ideaActionFire(IdeaAction action, int actionID) {
            switch(actionID) {
                case IDEA_DELETE_RULE:
                    editor.actionsRefactor.deleteRuleAtIndex(editor.getCaretPosition());
                    break;
            }
        }
    }

    public class ItemLeftRecursion extends Item {

        public List getIdeaActions() {
            List actions = new ArrayList();
            actions.add(new IdeaAction("Remove left recursion of rule '"+token.getAttribute()+"'", this, IDEA_REMOVE_LEFT_RECURSION, token));
            return actions;
        }

        public void ideaActionFire(IdeaAction action, int actionID) {
            switch(actionID) {
                case IDEA_REMOVE_LEFT_RECURSION:
                    editor.actionsRefactor.removeLeftRecursion();
                    break;
            }
        }
    }

}
