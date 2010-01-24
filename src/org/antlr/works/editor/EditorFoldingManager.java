package org.antlr.works.editor;

import org.antlr.works.ate.folding.ATEFoldingEntity;
import org.antlr.works.ate.folding.ATEFoldingEntityProxy;
import org.antlr.works.ate.folding.ATEFoldingManager;
import org.antlr.works.components.GrammarWindow;
import org.antlr.works.grammar.element.ElementAction;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.prefs.AWPrefs;

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

public class EditorFoldingManager extends ATEFoldingManager {

    protected static final int TAG_RULES = 0;
    protected static final int TAG_ACTIONS = 1;

    protected GrammarWindow window;

    public EditorFoldingManager(GrammarWindow window) {
        super(window.textEditor);
        this.window = window;
    }

    @Override
    public void close() {
        super.close();
        window = null;
    }

    public void textPaneWillFold() {
        super.textPaneWillFold();
        window.disableTextPaneUndo();
    }

    public void textPaneDidFold() {
        super.textPaneDidFold();
        window.enableTextPaneUndo();
        window.editorIdeas.hide();
        window.editorTips.hide();
    }

    public ATEFoldingEntityProxy createEntityProxy(ATEFoldingEntity entity) {
        int tag;
        if(entity instanceof ElementRule)
            tag = TAG_RULES;
        else if(entity instanceof ElementAction)
            tag = TAG_ACTIONS;
        else
            return null;

        return new ATEFoldingEntityProxy(this, entity.foldingEntityID(), tag);
    }

    public void provideFoldingEntities() {
        List<ElementRule> rules = window.getGrammarEngine().getRules();
        if(rules != null) {
            for (ElementRule rule : rules) {
                addEntity(rule);
            }
        }

        // Add only actions that are in expanded rules
        if(AWPrefs.getFoldingEnabled() && AWPrefs.getDisplayActionsAnchorsFolding()) {
            List<ElementAction> actions = window.getGrammarEngine().getActions();
            if(actions != null) {
                for (ElementAction action : actions) {
                    if (action.rule.isExpanded()) {
                        // since 1.2, don't display action folding icon to avoid visual clutter
                        // maybe re-introduce it if the folding is supported one day in the window
                        //addEntity(action);
                    }
                }
            }
        }
    }

    public ATEFoldingEntity getEntityForIdentifier(List entities, String identifier) {
        if(entities == null || entities.isEmpty())
            return null;
        // optimize using a map ?
        for (Object entity1 : entities) {
            ATEFoldingEntity entity = (ATEFoldingEntity) entity1;
            if (entity.foldingEntityID().equals(identifier))
                return entity;
        }
        return null;
    }

    public ATEFoldingEntity getEntityForKey(Object key, int tag) {
        if(tag == TAG_ACTIONS)
            return getEntityForIdentifier(window.getGrammarEngine().getActions(), (String)key);
        else if(tag == TAG_RULES)
            return getEntityForIdentifier(window.getGrammarEngine().getRules(), (String)key);
        else
            return null;
    }

}
