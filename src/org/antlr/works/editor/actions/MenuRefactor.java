package org.antlr.works.editor.actions;

import edu.usfca.xj.appkit.utils.XJAlert;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.parser.Lexer;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.parser.Token;
import org.antlr.works.stats.Statistics;

import javax.swing.*;
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

public class MenuRefactor extends AbstractActions {

    public MenuRefactor(EditorWindow editor) {
        super(editor);
    }

    public void rename() {
        Token token = editor.getTokenAtPosition(getCaretPosition());
        if(token == null)
            return;

        String s = (String) JOptionPane.showInputDialog(editor.getJavaContainer(), "Rename '"+token.getAttribute()+"' and its usages to:", "Rename",
                JOptionPane.QUESTION_MESSAGE, null, null, token.getAttribute());
        if(s != null && !s.equals(token.getAttribute())) {
            editor.beginGroupChange("Rename");
            renameToken(token, s);
            editor.endGroupChange();
            Statistics.shared().recordEvent(Statistics.EVENT_RENAME);
        }
    }

    public void renameToken(Token t, String name) {
        List tokens = editor.getTokens();
        String attr = t.getAttribute();
        for(int index = tokens.size()-1; index>0; index--) {
            Token token = (Token) tokens.get(index);
            if(token.type == t.type && token.getAttribute().equals(attr)) {
                editor.editorGUI.replaceText(token.getStartIndex(), token.getEndIndex(), name);
            }
        }
    }

    public void replaceLiteralWithTokenLabel() {
        Token token = editor.getTokenAtPosition(getCaretPosition());
        if(token == null)
            return;

        if(token.type != Lexer.TOKEN_SINGLE_QUOTE_STRING && token.type != Lexer.TOKEN_DOUBLE_QUOTE_STRING) {
            XJAlert.display(editor.getJavaContainer(), "Cannot Replace Literal With Token Label", "The current token is not a string.");
            return;
        }

        String s = (String)JOptionPane.showInputDialog(editor.getJavaContainer(), "Replace Literal '"+token.getAttribute()+"' with token label:", "Replace Literal With Token Label",
                JOptionPane.QUESTION_MESSAGE, null, null, "");
        if(s != null && !s.equals(token.getAttribute())) {
            editor.beginGroupChange("Replace Literal With Token Label");
            replaceLiteralTokenWithTokenLabel(token, s);
            editor.endGroupChange();
        }
    }

    public void replaceLiteralTokenWithTokenLabel(Token t, String name) {
        // First insert the rule at the end of the grammar
        int insertionIndex = editor.getText().length();
        editor.editorGUI.insertText(insertionIndex, "\n\n"+name+"\n\t:\t"+t.getAttribute()+"\n\t;");

        // Then rename all strings token
        List tokens = editor.getTokens();
        String attr = t.getAttribute();
        for(int index = tokens.size()-1; index>0; index--) {
            Token token = (Token) tokens.get(index);
            if(token.type != Lexer.TOKEN_SINGLE_QUOTE_STRING && token.type != Lexer.TOKEN_DOUBLE_QUOTE_STRING)
                continue;

            if(!token.getAttribute().equals(attr))
                continue;

            editor.editorGUI.replaceText(token.getStartIndex(), token.getEndIndex(), name);
        }
    }

    public void removeLeftRecursion() {
        ParserRule rule = editor.rules.getEnclosingRuleAtPosition(editor.getCaretPosition());
        if(rule == null) {
            XJAlert.display(editor.getWindowContainer(), "Remove Left Recursion", "There is no rule at cursor position.");
            return;
        }

        if(!rule.hasLeftRecursion()) {
            XJAlert.display(editor.getWindowContainer(), "Remove Left Recursion", "The rule doesn't have a left recursion.");
            return;
        }

        editor.beginGroupChange("Remove Left Recursion");
        String ruleText = rule.getTextRuleAfterRemovingLeftRecursion();
        editor.editorGUI.replaceText(rule.getInternalTokensStartIndex(), rule.getInternalTokensEndIndex(), ruleText);
        editor.endGroupChange();
    }

}
