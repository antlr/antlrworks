package org.antlr.works.editor;

import org.antlr.works.ate.ATEOverlayManager;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.editor.idea.IdeaAction;
import org.antlr.works.editor.idea.IdeaActionDelegate;

import java.awt.*;
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

public class EditorInspectorItem implements IdeaActionDelegate {

    public static final int IDEA_DELETE_RULE = 0;
    public static final int IDEA_CREATE_RULE = 1;
    public static final int IDEA_REMOVE_LEFT_RECURSION = 2;
    public static final int IDEA_CONVERT_TO_SINGLE_QUOTE = 3;
    public static final int IDEA_FIX_GRAMMAR_NAME = 4;
    public static final int IDEA_DECISION_DFA = 5;
    public static final int IDEA_CREATE_FILE = 6;

    public ATEToken token;
    public int startIndex;
    public int endIndex;
    public int startLineNumber;
    public Color color;
    public String description;
    public int shape = ATEOverlayManager.SHAPE_SAW_TOOTH;

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

    @Override
    public String toString() {
        return description;
    }
}
