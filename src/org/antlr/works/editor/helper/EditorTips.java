package org.antlr.works.editor.helper;

import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.tips.TipsManager;
import org.antlr.works.editor.tips.TipsOverlay;
import org.antlr.works.editor.tips.TipsProvider;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.parser.Token;

import javax.swing.*;
import javax.swing.text.BadLocationException;
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

public class EditorTips implements TipsProvider {

    public TipsManager tipsManager;
    public EditorWindow editor;

    public EditorTips(EditorWindow editor) {
        this.editor = editor;
    }

    public void awake() {
        tipsManager = new TipsManager();
        tipsManager.setOverlay(new TipsOverlay(editor, editor.getJFrame(), editor.getTextPane()));
        tipsManager.addProvider(this);
    }

    public void toggleEnabled() {
        tipsManager.setEnabled(!tipsManager.enabled());
    }

    public void hide() {
        tipsManager.hide();
    }

    public void display(Point relativePoint, Point absolutePoint) {
        if(editor.getTokens() == null)
            return;

        int position = editor.getTextPane().viewToModel(relativePoint);

        Token token = editor.getTokenAtPosition(position);
        ParserRule enclosingRule = editor.rules.getEnclosingRuleAtPosition(position);
        ParserRule rule = editor.rules.getRuleStartingWithToken(token);

        Point p = null;
        try {
            if(token != null) {
                // Make sure the mouse is over the token because
                // Swing will return a valid position even if the mouse
                // is on the remaining blank part of the line
                Rectangle r1 = editor.getTextPane().modelToView(token.getStartIndex());
                Rectangle r2 = editor.getTextPane().modelToView(token.getEndIndex());
                if(r1.union(r2).contains(relativePoint)) {
                    p = SwingUtilities.convertPoint(editor.getTextPane(), new Point(relativePoint.x+2, r2.y-5), editor.getJFrame());
                }
            }
        } catch (BadLocationException e) {
            // Ignore
        }
        tipsManager.displayAnyTipsAvailable(token, rule, enclosingRule, p);
    }

    public List tipsProviderGetTips(Token token, ParserRule rule, ParserRule enclosingRule) {
        List tips = new ArrayList();

        if(editor.rules.isUndefinedReference(token)) {
            tips.add("Undefined reference '"+token.getAttribute()+"'");
        }

        if(editor.rules.isDuplicateRule(token.getAttribute())) {
            tips.add("Duplicate rule '"+token.getAttribute()+"'");
        }

        if(rule != null && rule.hasLeftRecursion()) {
            tips.add("Rule has left recursion");
        }

        return tips;
    }

}
