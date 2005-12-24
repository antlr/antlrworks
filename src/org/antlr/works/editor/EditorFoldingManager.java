package org.antlr.works.editor;

import org.antlr.works.ate.ATEFoldingEntity;
import org.antlr.works.ate.ATEFoldingEntityProxy;
import org.antlr.works.ate.ATEFoldingManager;
import org.antlr.works.parser.ParserAction;
import org.antlr.works.parser.ParserRule;
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

    protected EditorWindow editor;

    public EditorFoldingManager(EditorWindow editor) {
        super(editor.textEditor);
        this.editor = editor;
    }

    public void textPaneWillFold() {
        super.textPaneWillFold();
        editor.disableTextPaneUndo();
    }

    public void textPaneDidFold() {
        super.textPaneDidFold();
        editor.enableTextPaneUndo();
        editor.editorIdeas.hide();
        editor.editorTips.hide();
    }

    public ATEFoldingEntityProxy createEntityProxy(ATEFoldingEntity entity) {
        int tag;
        if(entity instanceof ParserRule)
            tag = TAG_RULES;
        else if(entity instanceof ParserAction)
            tag = TAG_ACTIONS;
        else
            return null;

        return new ATEFoldingEntityProxy(this, entity.foldingEntityID(), tag);
    }

    public void provideFoldingEntities() {
        List rules = editor.parser.getRules();
        if(rules != null) {
            for(int index=0; index<rules.size(); index++) {
                ParserRule rule = (ParserRule)rules.get(index);
                addEntity(rule);
            }
        }

        // Add only actions that are in expanded rules
        if(AWPrefs.getFoldingEnabled() && AWPrefs.getDisplayActionsAnchorsFolding()) {
            List actions = editor.parser.getActions();
            if(actions != null) {
                for(int index=0; index<actions.size(); index++) {
                    ParserAction action = (ParserAction)actions.get(index);
                    if(action.rule.isExpanded())
                        addEntity(action);
                }
            }
        }
    }

    public ATEFoldingEntity getEntityForIdentifier(List entities, String identifier) {
        if(entities == null || entities.isEmpty())
            return null;
        // @todo optimize using a map ?
        for(int index=0; index<entities.size(); index++) {
            ATEFoldingEntity entity = (ATEFoldingEntity)entities.get(index);
            if(entity.foldingEntityID().equals(identifier))
                return entity;
        }
        return null;
    }

    public ATEFoldingEntity getEntityForKey(Object key, int tag) {
        if(tag == TAG_ACTIONS)
            return getEntityForIdentifier(editor.parser.getActions(), (String)key);
        else if(tag == TAG_RULES)
            return getEntityForIdentifier(editor.parser.getRules(), (String)key);
        else
            return null;
    }

}
