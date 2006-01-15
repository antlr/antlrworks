package org.antlr.works.menu;

import edu.usfca.xj.appkit.undo.XJUndo;
import edu.usfca.xj.appkit.utils.XJAlert;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.Statistics;
import org.antlr.works.syntax.GrammarSyntaxLexer;
import org.antlr.works.syntax.GrammarSyntaxReference;
import org.antlr.works.syntax.GrammarSyntaxRule;
import org.antlr.works.utils.Utils;

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

public class MenuRefactor extends MenuAbstract {

    public MenuRefactor(CEditorGrammar editor) {
        super(editor);
    }

    public void rename() {
        ATEToken token = editor.getCurrentToken();
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

    protected void renameToken(ATEToken t, String name) {
        List tokens = editor.getTokens();
        String attr = t.getAttribute();

        boolean renameRefRule = t.type == GrammarSyntaxLexer.TOKEN_REFERENCE || t.type == GrammarSyntaxLexer.TOKEN_RULE;

        for(int index = tokens.size()-1; index>0; index--) {
            ATEToken token = (ATEToken) tokens.get(index);

            if(token.getAttribute().equals(attr)) {
                if(token.type == t.type || renameRefRule &&
                        (token.type == GrammarSyntaxLexer.TOKEN_REFERENCE || token.type == GrammarSyntaxLexer.TOKEN_RULE))
                {
                    editor.replaceText(token.getStartIndex(), token.getEndIndex(), name);
                }
            }
        }
    }

    public void replaceLiteralWithTokenLabel() {
        ATEToken token = editor.getCurrentToken();
        if(token == null)
            return;

        if(token.type != ATESyntaxLexer.TOKEN_SINGLE_QUOTE_STRING && token.type != ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING) {
            XJAlert.display(editor.getJavaContainer(), "Cannot Replace Literal With Token Label", "The current token is not a literal.");
            return;
        }

        editor.selectTextRange(token.getStartIndex(), token.getEndIndex());
        String s = (String)JOptionPane.showInputDialog(editor.getJavaContainer(), "Replace Literal '"+token.getAttribute()+"' with token label:", "Replace Literal With Token Label",
                JOptionPane.QUESTION_MESSAGE, null, null, "");
        if(s != null && !s.equals(token.getAttribute())) {
            editor.beginGroupChange("Replace Literal With Token Label");
            replaceLiteralTokenWithTokenLabel(token, s);
            editor.endGroupChange();
        }
    }

    public void replaceLiteralTokenWithTokenLabel(ATEToken t, String name) {
        // First insert the rule at the end of the grammar
        int insertionIndex = editor.getText().length();
        editor.textEditor.insertText(insertionIndex, "\n\n"+name+"\n\t:\t"+t.getAttribute()+"\n\t;");

        // Then rename all strings token
        List tokens = editor.getTokens();
        String attr = t.getAttribute();
        for(int index = tokens.size()-1; index>0; index--) {
            ATEToken token = (ATEToken) tokens.get(index);
            if(token.type != ATESyntaxLexer.TOKEN_SINGLE_QUOTE_STRING && token.type != ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING)
                continue;

            if(!token.getAttribute().equals(attr))
                continue;

            editor.replaceText(token.getStartIndex(), token.getEndIndex(), name);
        }
    }

    public void convertLiteralsToSingleQuote() {
        editor.beginGroupChange("Convert Literals To Single Quote Literals");
        convertLiteralsToSpecifiedQuote(ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING, '\'', '"');
        editor.endGroupChange();
    }

    public void convertLiteralsToDoubleQuote() {
        editor.beginGroupChange("Convert Literals To Double Quote Literals");
        convertLiteralsToSpecifiedQuote(ATESyntaxLexer.TOKEN_SINGLE_QUOTE_STRING, '"', '\'');
        editor.endGroupChange();
    }

    public void convertLiteralsToCStyleQuote() {
        editor.beginGroupChange("Convert Literals To C-style Quote Literals");

        List tokens = editor.getTokens();
        for(int index = tokens.size()-1; index>0; index--) {
            ATEToken token = (ATEToken) tokens.get(index);

            String attribute;
            String stripped;
            String replaced = null;

            if(token.type == ATESyntaxLexer.TOKEN_SINGLE_QUOTE_STRING || token.type == ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING) {
                attribute = token.getAttribute();
                stripped = attribute.substring(1, attribute.length()-1);
            } else
                continue;

            if(token.type == ATESyntaxLexer.TOKEN_SINGLE_QUOTE_STRING) {
                // Only one character allowed
                if(stripped.length() == 1)
                    continue;
                else if(stripped.length() == 2 && stripped.charAt(0) == '\\')
                    continue;

                if(stripped.indexOf('"') != -1 || stripped.indexOf('\'') != -1)
                    stripped = escapeStringQuote(stripped, '"', '\'');

                replaced = '"'+stripped+'"';
            } else if(token.type == ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING) {
                // String with one character should be converted to single-quote

                if(stripped.length() > 1 && stripped.charAt(0) != '\\')
                    continue;

                if(stripped.indexOf('\'') != -1 || stripped.indexOf('"') != -1)
                    stripped = escapeStringQuote(stripped, '\'', '"');

                replaced = '\''+stripped+'\'';
            }

            editor.replaceText(token.getStartIndex(), token.getEndIndex(), replaced);
        }

        editor.endGroupChange();
    }

    protected void convertLiteralsToSpecifiedQuote(int tokenType, char quote, char unescapeQuote) {
        List tokens = editor.getTokens();
        for(int index = tokens.size()-1; index>0; index--) {
            ATEToken token = (ATEToken) tokens.get(index);
            if(token.type != tokenType)
                continue;

            String attribute = token.getAttribute();
            String stripped = attribute.substring(1, attribute.length()-1);
            if(stripped.indexOf(quote) != -1 || stripped.indexOf(unescapeQuote) != -1)
                stripped = escapeStringQuote(stripped, quote, unescapeQuote);

            editor.replaceText(token.getStartIndex(), token.getEndIndex(), quote+stripped+quote);
        }
    }

    protected String escapeStringQuote(String s, char escapeQuote, char unescapeQuote) {
        // Escape the quote found in s.
        // Example:
        // "hello'world" -> 'hello\'world'
        // "hello\'world" -> 'hello\'world'
        // "hello\"world" -> 'hello"world'

        StringBuffer sb = new StringBuffer();
        for(int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if(c == '\\') {
                i++;
                char c1 = s.charAt(i);
                if(c1 == unescapeQuote)
                    sb.append(c1);
                else {
                    sb.append('\\');
                    sb.append(c1);
                }
            } else if(c == escapeQuote) {
                sb.append('\\');
                sb.append(escapeQuote);
            } else
                sb.append(c);
        }
        return sb.toString();
    }

    public void removeLeftRecursion() {
        GrammarSyntaxRule rule = editor.rules.getEnclosingRuleAtPosition(editor.getCaretPosition());
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
        editor.replaceText(rule.getInternalTokensStartIndex(), rule.getInternalTokensEndIndex(), ruleText);
        editor.endGroupChange();
    }

    public void removeAllLeftRecursion() {
        editor.beginGroupChange("Remove All Left Recursion");
        List rules = editor.rules.getRules();
        for(int index = rules.size()-1; index >= 0; index--) {
            GrammarSyntaxRule rule = (GrammarSyntaxRule)rules.get(index);
            if(rule.hasLeftRecursion()) {
                String ruleText = rule.getTextRuleAfterRemovingLeftRecursion();
                editor.replaceText(rule.getInternalTokensStartIndex(), rule.getInternalTokensEndIndex(), ruleText);
            }
        }
        editor.endGroupChange();
    }

    public void extractRule() {
        int leftIndex = editor.getSelectionLeftIndexOnTokenBoundary();
        int rightIndex = editor.getSelectionRightIndexOnTokenBoundary();
        if(leftIndex == -1 || rightIndex == -1) {
            XJAlert.display(editor.getWindowContainer(), "Extract Rule", "At least one token must be selected.");
            return;
        }

        editor.selectTextRange(leftIndex, rightIndex);

        String ruleName = (String)JOptionPane.showInputDialog(editor.getJavaContainer(), "Rule name:", "Extract Rule",
                            JOptionPane.QUESTION_MESSAGE, null, null, "");
        if(ruleName != null && ruleName.length() > 0) {
            editor.beginGroupChange("Extract Rule");
            boolean lexer = ATEToken.isLexerName(ruleName);
            int index = insertionIndexForRule(lexer);
            String ruleContent = editor.getText().substring(leftIndex, rightIndex);
            if(index > editor.getCaretPosition()) {
                insertRuleAtIndex(createRule(ruleName, ruleContent), index);
                editor.replaceText(leftIndex, rightIndex, ruleName);
            } else {
                editor.replaceText(leftIndex, rightIndex, ruleName);
                insertRuleAtIndex(createRule(ruleName, ruleContent), index);
            }
            editor.endGroupChange();
        }
    }

    public void inlineRule() {
        GrammarSyntaxRule rule = editor.rules.getEnclosingRuleAtPosition(editor.getCaretPosition());
        if(rule == null) {
            XJAlert.display(editor.getWindowContainer(), "Inline Rule", "There is no rule at cursor position.");
            return;
        }

        inlineRule(rule);
    }

    protected void inlineRule(String ruleName) {
        GrammarSyntaxRule rule = editor.rules.getRuleWithName(ruleName);
        if(rule == null) {
            XJAlert.display(editor.getWindowContainer(), "Inline Rule", "Rule \""+ruleName+"\" doesn't exist.");
            return;
        }
        inlineRule(rule);
    }

    protected void inlineRule(GrammarSyntaxRule rule) {
        String oldContent = editor.getText();
        StringBuffer s = new StringBuffer(oldContent);

        String ruleName = rule.name;
        String ruleContent = Utils.trimString(oldContent.substring(rule.colon.getEndIndex(), rule.end.getStartIndex()));

        List rules = editor.rules.getRules();
        if(rule.end.index - rule.colon.index > 2) {
            // More than one token, append ()
            ruleContent = "("+ruleContent+")";
        }

        for(int r=rules.size()-1; r>=0; r--) {
            GrammarSyntaxRule candidate = (GrammarSyntaxRule)rules.get(r);
            if(candidate == rule) {
                s.delete(rule.getStartIndex(), rule.getEndIndex()+1);
            } else {
                List references = candidate.getReferences();
                if(references == null)
                    continue;

                for(int index=references.size()-1; index>=0; index--) {
                    GrammarSyntaxReference ref = (GrammarSyntaxReference)references.get(index);
                    if(ref.token.getAttribute().equals(ruleName)) {
                        s.replace(ref.token.getStartIndex(), ref.token.getEndIndex(), ruleContent);
                    }
                }
            }
        }

        replaceEditorTextAndAdjustCaretPosition(s.toString());

        XJUndo undo = editor.getXJFrame().getUndo(getTextPane());
        undo.addEditEvent(new UndoableInlineRuleEdit(oldContent, rule.name));
    }

    public void createRuleAtIndex(boolean lexer, String name, String content) {
        editor.beginGroupChange("Create Rule");
        int index = insertionIndexForRule(lexer);
        insertRuleAtIndex(createRule(name, content), index);
        setCaretPosition(index);
        editor.endGroupChange();
    }

    protected void replaceEditorTextAndAdjustCaretPosition(String newText) {
        int oldCaretPosition = editor.getTextPane().getCaretPosition();
        String oldText = editor.getText();
        int caretOffset = newText.length()-oldText.length();
        editor.disableTextPaneUndo();
        editor.setText(newText);
        editor.enableTextPaneUndo();
        editor.getTextEditor().setCaretPosition(oldCaretPosition +caretOffset, false, false);
    }

    public int insertionIndexForRule(boolean lexer) {
        // Add the rule in the next line by default
        Point p = editor.getLineTextPositionsAtTextPosition(getCaretPosition());
        int insertionIndex = p.y;

        GrammarSyntaxRule rule = editor.rules.getEnclosingRuleAtPosition(getCaretPosition());
        if(rule != null) {
            if(rule.lexer) {
                if(lexer) {
                    // Add new rule just after this one
                    insertionIndex = rule.getEndIndex();
                } else {
                    // Add new rule after the last parser rule
                    GrammarSyntaxRule last = editor.rules.getLastParserRule();
                    if(last != null) insertionIndex = last.getEndIndex();
                }
            } else {
                if(lexer) {
                    // Add new rule after the last lexer rule
                    GrammarSyntaxRule last = editor.rules.getLastLexerRule();
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

        if(name.length() >= AWPrefs.getEditorTabSize())
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
        editor.textEditor.insertText(index, rule);
    }

    public void deleteRuleAtIndex(int index) {
        GrammarSyntaxRule r = editor.rules.getEnclosingRuleAtPosition(index);
        if(r != null)
            editor.replaceText(r.getStartIndex(), r.getEndIndex(), "");
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
