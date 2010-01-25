package org.antlr.works.editor;

import org.antlr.works.components.GrammarWindow;
import org.antlr.works.editor.idea.*;
import org.antlr.works.grammar.element.ElementRule;

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

public class EditorIdeas implements IdeaManagerDelegate, IdeaProvider {

    public IdeaManager ideaManager;
    public GrammarWindow window;

    public EditorIdeas(GrammarWindow window) {
        this.window = window;
    }

    public void awake() {
        ideaManager = new IdeaManager();
        ideaManager.setOverlay(new IdeaOverlay(window, window.getTextPane()));
        ideaManager.addProvider(this);
        ideaManager.setDelegate(this);
    }

    public void close() {
        window = null;
        ideaManager.close();
    }

    public void hide() {
        ideaManager.hide();
    }

    public void toggleEnabled() {
        ideaManager.setEnabled(!ideaManager.enabled());
    }

    public boolean isEnabled() {
        return ideaManager.enabled();
    }

    public List<IdeaAction> ideaProviderGetActions(int position) {
        List<IdeaAction> actions = new ArrayList<IdeaAction>();
        List<EditorInspectorItem> items = window.editorInspector.getAllItemsAtIndex(position);
        for (EditorInspectorItem item : items) {
            List<IdeaAction> itemActions = item.getIdeaActions();
            if (itemActions != null)
                actions.addAll(itemActions);
        }
        return actions;
    }


    public boolean ideaManagerWillDisplayIdea() {
        return !window.autoCompletionMenu.isVisible() && window.isFileWritable();
    }

    public void display(Point p) {
        display(window.getTextPane().viewToModel(p));
    }

    public void display(int position) {
        ElementRule enclosingRule = window.editorRules.getEnclosingRuleAtPosition(position);
        if(enclosingRule == null || enclosingRule.isExpanded())
            ideaManager.displayAnyIdeasAvailable(position);
    }

}
