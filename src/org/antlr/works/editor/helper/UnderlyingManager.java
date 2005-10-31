package org.antlr.works.editor.helper;

import org.antlr.works.editor.ate.ATEUnderlyingManager;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.parser.Token;
import org.antlr.works.parser.Lexer;
import org.antlr.works.parser.ParserRule;

import java.util.List;
import java.awt.*;
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
        List tokens = editor.getTokens();
        if(tokens == null)
            return;

        for(int index=0; index<tokens.size(); index++) {
            Token token = (Token)tokens.get(index);

            if(token.type != Lexer.TOKEN_ID)
                continue;

            if(editor.rules.isUndefinedToken(token)) {
                drawUnderlineAtIndexes(g, Color.red, token.getStartIndex(), token.getEndIndex());
            }

            if(editor.rules.isDuplicateRule(token.getAttribute())) {
                drawUnderlineAtIndexes(g, Color.blue, token.getStartIndex(), token.getEndIndex());
            }

           ParserRule rule = editor.rules.getRuleStartingWithToken(token);
           if(rule != null && rule.hasLeftRecursion()) {
               drawUnderlineAtIndexes(g, Color.green, token.getStartIndex(), token.getEndIndex());
           }
        }
    }
}
