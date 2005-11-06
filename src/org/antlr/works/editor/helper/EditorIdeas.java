package org.antlr.works.editor.helper;

import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.idea.*;
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

public class EditorIdeas implements IdeaActionDelegate, IdeaManagerDelegate, IdeaProvider {

    public static final int IDEA_DELETE_RULE = 0;
    public static final int IDEA_CREATE_RULE = 1;
    public static final int IDEA_REMOVE_LEFT_RECURSION = 2;

    public IdeaManager ideaManager;
    public EditorWindow editor;

    public EditorIdeas(EditorWindow editor) {
        this.editor = editor;
    }

    public void awake() {
        ideaManager = new IdeaManager();
        ideaManager.setOverlay(new IdeaOverlay(editor, editor.getJFrame(), editor.getTextPane()));
        ideaManager.addProvider(this);
        ideaManager.setDelegate(this);
    }

    public void close() {
        ideaManager.close();
    }

    public void hide() {
        ideaManager.hide();
    }

    public void toggleEnabled() {
        ideaManager.setEnabled(!ideaManager.enabled());
    }

    public List ideaProviderGetActions(Token token, ParserRule rule, ParserRule enclosingRule) {
        List actions = new ArrayList();

        if(editor.rules.isUndefinedReference(token)) {
            actions.add(new IdeaAction("Create rule '"+token.getAttribute()+"'", this, IDEA_CREATE_RULE, token));
        }

        if(editor.rules.isDuplicateRule(token.getAttribute())) {
            actions.add(new IdeaAction("Delete rule '"+token.getAttribute()+"'", this, IDEA_DELETE_RULE, token));
        }

        if(rule != null && rule.hasLeftRecursion()) {
            actions.add(new IdeaAction("Remove left recursion of rule '"+token.getAttribute()+"'", this, IDEA_REMOVE_LEFT_RECURSION, token));
        }

        return actions;
    }

    public void ideaActionFire(IdeaAction action, int actionID) {
        switch(actionID) {
            case IDEA_DELETE_RULE:
                editor.actionsRefactor.deleteRuleAtIndex(editor.getCaretPosition());
                break;
            case IDEA_CREATE_RULE:
                editor.actionsRefactor.createRuleAtIndex(action.token.isLexer(), action.token.getAttribute(), null);
                break;
            case IDEA_REMOVE_LEFT_RECURSION:
                editor.actionsRefactor.removeLeftRecursion();
                break;
        }
    }

    public boolean ideaManagerWillDisplayIdea() {
        return !editor.autoCompletionMenu.isVisible();
    }

    public void display(Point p) {
        display(editor.getTextPane().viewToModel(p));
    }

    public void display(int position) {
        if(editor.getTokens() == null)
            return;

        Token token = editor.getTokenAtPosition(position);
        ParserRule rule = editor.rules.getRuleStartingWithToken(token);
        ParserRule enclosingRule = editor.rules.getEnclosingRuleAtPosition(position);
        if(enclosingRule == null || enclosingRule.isExpanded())
            ideaManager.displayAnyIdeasAvailable(token, rule, enclosingRule);
    }

}
