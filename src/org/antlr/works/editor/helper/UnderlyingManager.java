package org.antlr.works.editor.helper;

import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.ate.ATEUnderlyingManager;
import org.antlr.works.parser.ParserReference;
import org.antlr.works.parser.ParserRule;

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

public class UnderlyingManager extends ATEUnderlyingManager {

    protected EditorWindow editor;

    public UnderlyingManager(EditorWindow editor) {
        super(editor.editorGUI.textEditor);
        this.editor = editor;
    }

    public void render(Graphics g) {
        renderUndefinedReferences(g);
        renderDuplicateRules(g);
        renderHasLeftRecursionRules(g);
    }

    protected void renderUndefinedReferences(Graphics g) {
        List undefinedRefs = editor.rules.getUndefinedReferences();
        if(undefinedRefs == null)
            return;

        for(int index=0; index<undefinedRefs.size(); index++) {
            ParserReference ref = (ParserReference)undefinedRefs.get(index);
            drawUnderlineAtIndexes(g, Color.red, ref.token.getStartIndex(), ref.token.getEndIndex());
        }
    }

    protected void renderDuplicateRules(Graphics g) {
        List refs = editor.rules.getReferences();
        if(refs == null)
            return;

        for(int index=0; index<refs.size(); index++) {
            ParserReference ref = (ParserReference)refs.get(index);
            if(editor.rules.isDuplicateRule(ref.token.getAttribute()))
                drawUnderlineAtIndexes(g, Color.blue, ref.token.getStartIndex(), ref.token.getEndIndex());
        }
    }

    protected void renderHasLeftRecursionRules(Graphics g) {
        List rules = editor.rules.getRules();
        if(rules == null)
            return;

        for(int index=0; index<rules.size(); index++) {
            ParserRule rule = (ParserRule)rules.get(index);
            if(rule.hasLeftRecursion())
                drawUnderlineAtIndexes(g, Color.green, rule.start.getStartIndex(), rule.start.getEndIndex());
        }
    }

}
