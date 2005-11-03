package org.antlr.works.editor.actions;

import edu.usfca.xj.appkit.utils.XJAlert;
import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.undo.Undo;
import org.antlr.works.parser.Lexer;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.parser.Token;
import org.antlr.works.stats.Statistics;
import org.antlr.works.util.Utils;

import javax.swing.*;
import javax.swing.undo.AbstractUndoableEdit;
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

public class ActionsRefactor extends AbstractActions {

    public ActionsRefactor(EditorWindow editor) {
        super(editor);
    }

    public void rename() {
        Token token = editor.getCurrentToken();
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
                editor.editorGUI.textEditor.replaceText(token.getStartIndex(), token.getEndIndex(), name);
            }
        }
    }

    public void replaceLiteralWithTokenLabel() {
        Token token = editor.getCurrentToken();
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
        editor.editorGUI.textEditor.insertText(insertionIndex, "\n\n"+name+"\n\t:\t"+t.getAttribute()+"\n\t;");

        // Then rename all strings token
        List tokens = editor.getTokens();
        String attr = t.getAttribute();
        for(int index = tokens.size()-1; index>0; index--) {
            Token token = (Token) tokens.get(index);
            if(token.type != Lexer.TOKEN_SINGLE_QUOTE_STRING && token.type != Lexer.TOKEN_DOUBLE_QUOTE_STRING)
                continue;

            if(!token.getAttribute().equals(attr))
                continue;

            editor.editorGUI.textEditor.replaceText(token.getStartIndex(), token.getEndIndex(), name);
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
        editor.editorGUI.textEditor.replaceText(rule.getInternalTokensStartIndex(), rule.getInternalTokensEndIndex(), ruleText);
        editor.endGroupChange();
    }

    public void removeAllLeftRecursion() {
        editor.beginGroupChange("Remove All Left Recursion");
        List rules = editor.rules.getRules();
        for(int index = rules.size()-1; index >= 0; index--) {
            ParserRule rule = (ParserRule)rules.get(index);
            if(rule.hasLeftRecursion()) {
                String ruleText = rule.getTextRuleAfterRemovingLeftRecursion();
                editor.editorGUI.textEditor.replaceText(rule.getInternalTokensStartIndex(), rule.getInternalTokensEndIndex(), ruleText);                
            }
        }
        editor.endGroupChange();
    }

    public void extractRule() {
        String ruleName = (String)JOptionPane.showInputDialog(editor.getJavaContainer(), "Rule name:", "Extract Rule",
                            JOptionPane.QUESTION_MESSAGE, null, null, "");
        if(ruleName != null && ruleName.length() > 0) {
            editor.beginGroupChange("Extract Rule");
            boolean lexer = ruleName.equals(ruleName.toUpperCase());
            int index = insertionIndexForRule(lexer);
            String ruleContent = editor.getSelectedText();
            if(index > editor.getCaretPosition()) {
                insertRuleAtIndex(createRule(ruleName, ruleContent), index);
                editor.getTextPane().replaceSelection(ruleName);
            } else {
                editor.editorGUI.textEditor.replaceSelectedText(ruleName);
                insertRuleAtIndex(createRule(ruleName, ruleContent), index);
            }
            editor.endGroupChange();
        }
    }

    public void inlineRule() {
        ParserRule rule = editor.rules.getEnclosingRuleAtPosition(editor.getCaretPosition());
        if(rule == null) {
            XJAlert.display(editor.getWindowContainer(), "Inline Rule", "There is no rule at cursor position.");
            return;
        }

        inlineRule(rule);
    }

    protected void inlineRule(String ruleName) {
        ParserRule rule = editor.rules.getRuleWithName(ruleName);
        if(rule == null) {
            XJAlert.display(editor.getWindowContainer(), "Inline Rule", "Rule \""+ruleName+"\" doesn't exist.");
            return;
        }
        inlineRule(rule);
    }

    protected void inlineRule(ParserRule rule) {
        String oldContent = editor.getText();
        StringBuffer s = new StringBuffer(oldContent);

        String ruleName = rule.name;
        String ruleContent = Utils.trimString(oldContent.substring(rule.colon.getEndIndex(), rule.end.getStartIndex()));
        List rules = editor.rules.getRules();
        for(int r=rules.size()-1; r>=0; r--) {
            ParserRule candidate = (ParserRule)rules.get(r);
            Token tstart = candidate.colon;
            Token tend = candidate.end;
            List tokens = editor.getTokens();
            for(int index=tokens.indexOf(tend)-1; index>tokens.indexOf(tstart); index--) {
                Token t = (Token)tokens.get(index);
                if(t.getAttribute().equals(ruleName))
                    s.replace(t.getStartIndex(), t.getEndIndex(), ruleContent);
            }
        }

        replaceEditorTextAndAdjustCaretPosition(s.toString());

        Undo undo = editor.getUndo(getTextPane());
        undo.addEditEvent(new UndoableInlineRuleEdit(oldContent, rule.name));
    }

    protected void replaceEditorTextAndAdjustCaretPosition(String newText) {
        int oldCaretPosition = editor.getTextPane().getCaretPosition();
        String oldText = editor.getText();
        int caretOffset = newText.length()-oldText.length();
        editor.disableTextPaneUndo();
        editor.getTextPane().setText(newText);
        editor.enableTextPaneUndo();
        editor.getTextPane().setCaretPosition(oldCaretPosition +caretOffset);
    }

    public int insertionIndexForRule(boolean lexer) {
        // Add the rule in the next line by default
        Point p = editor.getLineTextPositionsAtTextPosition(getCaretPosition());
        int insertionIndex = p.y;

        ParserRule rule = editor.rules.getEnclosingRuleAtPosition(getCaretPosition());
        if(rule != null) {
            if(rule.isLexerRule()) {
                if(lexer) {
                    // Add new rule just after this one
                    insertionIndex = rule.getEndIndex();
                } else {
                    // Add new rule after the last parser rule
                    ParserRule last = editor.rules.getLastParserRule();
                    if(last != null) insertionIndex = last.getEndIndex();
                }
            } else {
                if(lexer) {
                    // Add new rule after the last lexer rule
                    ParserRule last = editor.rules.getLastLexerRule();
                    if(last != null) {
                        insertionIndex = last.getEndIndex();
                    } else {
                        // Add new rule after the last rule
                        last = editor.rules.getLastRule();
                        if(last != null) insertionIndex = last.getEndIndex();
                    }
                } else {
                    // Add new rule just after this one
                    insertionIndex = rule.getEndIndex();
                }
            }
        }
        return insertionIndex;
    }

    public String createRule(String name, String content) {
        StringBuffer sb = new StringBuffer();

        sb.append("\n");
        sb.append("\n");
        sb.append(name);

        if(name.length() >= EditorPreferences.getEditorTabSize())
            sb.append("\n");

        sb.append("\t:");
        if(content != null && content.length() > 0) {
            sb.append("\t");
            sb.append(content);
        }
        sb.append("\n\t;");
        sb.append("\n");
        return sb.toString();
    }

    public void insertRuleAtIndex(String rule, int index) {
        editor.editorGUI.textEditor.insertText(index, rule);
    }

    protected class UndoableInlineRuleEdit extends AbstractUndoableEdit {

        public String oldContent;
        public String ruleName;

        public UndoableInlineRuleEdit(String oldContent, String ruleName) {
            this.oldContent = oldContent;
            this.ruleName = ruleName;
        }

        public String getPresentationName() {
            return "Inline Rule \""+ruleName+"\"";
        }

        public void redo() {
            super.redo();
            inlineRule(ruleName);
        }

        public void undo() {
            super.undo();
            replaceEditorTextAndAdjustCaretPosition(oldContent);
        }
    }

}
