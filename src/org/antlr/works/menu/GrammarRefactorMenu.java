package org.antlr.works.menu;

import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.GrammarWindow;
import org.antlr.works.grammar.RefactorEngine;
import org.antlr.works.grammar.RefactorMutator;
import org.antlr.works.grammar.element.ElementReference;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.utils.Utils;
import org.antlr.xjlib.appkit.undo.XJUndo;
import org.antlr.xjlib.appkit.utils.XJAlert;

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

public class GrammarRefactorMenu implements ActionRefactor {

    private final GrammarWindow window;
    private RefactorEngine engine;
    private EditorTextMutator mutator;

    public GrammarRefactorMenu(GrammarWindow window) {
        this.window = window;
        engine = new RefactorEngine();
    }

    public void rename() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_RENAME);

        ATEToken token = window.getCurrentToken();
        if(token == null)
            return;

        String s = (String) JOptionPane.showInputDialog(window.getJavaContainer(), "Rename '"+token.getAttribute()+"' and its usages to:", "Rename",
                JOptionPane.QUESTION_MESSAGE, null, null, token.getAttribute());
        if(s != null && !s.equals(token.getAttribute())) {
            beginRefactor("Rename");
            engine.renameToken(token, s);
            endRefactor();
        }
    }

    public boolean canReplaceLiteralWithTokenLabel() {
        ATEToken token = window.getCurrentToken();
        return token != null && (token.type == ATESyntaxLexer.TOKEN_SINGLE_QUOTE_STRING || token.type == ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING);
    }

    public void replaceLiteralWithTokenLabel() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_REPLACE_LITERALS);

        ATEToken token = window.getCurrentToken();
        if(token == null)
            return;

        if(token.type != ATESyntaxLexer.TOKEN_SINGLE_QUOTE_STRING && token.type != ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING) {
            XJAlert.display(window.getJavaContainer(), "Cannot Replace Literal With Token Label", "The current token is not a literal.");
            return;
        }

        window.selectTextRange(token.getStartIndex(), token.getEndIndex());
        String s = (String)JOptionPane.showInputDialog(window.getJavaContainer(), "Replace Literal '"+token.getAttribute()+"' with token label:", "Replace Literal With Token Label",
                JOptionPane.QUESTION_MESSAGE, null, null, "");
        if(s != null && !s.equals(token.getAttribute())) {
            beginRefactor("Replace Literal With Token Label");
            replaceLiteralTokenWithTokenLabel(token, s);
            endRefactor();
        }
    }

    public void replaceLiteralTokenWithTokenLabel(ATEToken t, String name) {
        // First insert the rule at the end of the grammar
        mutator.insert(window.getText().length(), "\n\n"+name+"\n\t:\t"+t.getAttribute()+"\n\t;");

        // Then rename all strings token
        List<ATEToken> tokens = window.getTokens();
        String attr = t.getAttribute();
        for(int index = tokens.size()-1; index>0; index--) {
            ATEToken token = tokens.get(index);
            if(token.type != ATESyntaxLexer.TOKEN_SINGLE_QUOTE_STRING && token.type != ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING)
                continue;

            if(!token.getAttribute().equals(attr))
                continue;

            mutator.replace(token.getStartIndex(), token.getEndIndex(), name);
        }
    }

    public void convertLiteralsToSingleQuote() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_CONVERT_LITERALS_TO_SINGLE);

        beginRefactor("Convert Literals To Single Quote Literals");
        convertLiteralsToSpecifiedQuote(ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING, '\'', '"');
        endRefactor();
    }

    public void convertLiteralsToDoubleQuote() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_CONVERT_LITERALS_TO_DOUBLE);

        beginRefactor("Convert Literals To Double Quote Literals");
        convertLiteralsToSpecifiedQuote(ATESyntaxLexer.TOKEN_SINGLE_QUOTE_STRING, '"', '\'');
        endRefactor();
    }

    public void convertLiteralsToCStyleQuote() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_CONVERT_LITERALS_TO_CSTYLE);

        beginRefactor("Convert Literals To C-style Quote Literals");

        List<ATEToken> tokens = window.getTokens();
        for(int index = tokens.size()-1; index>0; index--) {
            ATEToken token = tokens.get(index);

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

            mutator.replace(token.getStartIndex(), token.getEndIndex(), replaced);
        }

        endRefactor();
    }

    protected void convertLiteralsToSpecifiedQuote(int tokenType, char quote, char unescapeQuote) {
        List<ATEToken> tokens = window.getTokens();
        for(int index = tokens.size()-1; index>0; index--) {
            ATEToken token = tokens.get(index);
            if(token.type != tokenType)
                continue;

            // FIX AW-56
            if(RefactorEngine.ignoreScopeForDoubleQuoteLiteral(token.scope))
                continue;

            String attribute = token.getAttribute();
            String stripped = attribute.substring(1, attribute.length()-1);
            if(stripped.indexOf(quote) != -1 || stripped.indexOf(unescapeQuote) != -1)
                stripped = escapeStringQuote(stripped, quote, unescapeQuote);

            mutator.replace(token.getStartIndex(), token.getEndIndex(), quote+stripped+quote);
        }
    }

    protected String escapeStringQuote(String s, char escapeQuote, char unescapeQuote) {
        // Escape the quote found in s.
        // Example:
        // "hello'world" -> 'hello\'world'
        // "hello\'world" -> 'hello\'world'
        // "hello\"world" -> 'hello"world'

        StringBuilder sb = new StringBuilder();
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
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_REMOVE_LEFT_RECURSION);

        ElementRule rule = window.editorRules.getEnclosingRuleAtPosition(window.getCaretPosition());
        if(rule == null) {
            XJAlert.display(window.getJavaContainer(), "Remove Left Recursion", "There is no rule at cursor position.");
            return;
        }

        if(!rule.hasLeftRecursion()) {
            XJAlert.display(window.getJavaContainer(), "Remove Left Recursion", "The rule doesn't have a left recursion.");
            return;
        }

        beginRefactor("Remove Left Recursion");
        String ruleText = rule.getTextRuleAfterRemovingLeftRecursion();
        mutator.replace(rule.getInternalTokensStartIndex(), rule.getInternalTokensEndIndex(), ruleText);
        endRefactor();
    }

    public void removeAllLeftRecursion() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_REMOVE_ALL_LEFT_RECURSION);

        beginRefactor("Remove All Left Recursion");
        List<ElementRule> rules = window.editorRules.getRules();
        for(int index = rules.size()-1; index >= 0; index--) {
            ElementRule rule = rules.get(index);
            if(rule.hasLeftRecursion()) {
                String ruleText = rule.getTextRuleAfterRemovingLeftRecursion();
                mutator.replace(rule.getInternalTokensStartIndex(), rule.getInternalTokensEndIndex(), ruleText);
            }
        }
        endRefactor();
    }

    public boolean canExtractRule() {
        int leftIndex = window.getSelectionLeftIndexOnTokenBoundary();
        int rightIndex = window.getSelectionRightIndexOnTokenBoundary();
        return leftIndex != -1 && rightIndex != -1;
    }

    public void extractRule() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_EXTRACT_RULE);

        if(!canExtractRule()) {
            XJAlert.display(window.getJavaContainer(), "Extract Rule", "At least one token must be selected.");
            return;
        }

        int leftIndex = window.getSelectionLeftIndexOnTokenBoundary();
        int rightIndex = window.getSelectionRightIndexOnTokenBoundary();

        window.selectTextRange(leftIndex, rightIndex);

        String ruleName = (String)JOptionPane.showInputDialog(window.getJavaContainer(), "Rule name:", "Extract Rule",
                JOptionPane.QUESTION_MESSAGE, null, null, "");
        if(ruleName != null && ruleName.length() > 0) {
            beginRefactor("Extract Rule");
            boolean lexer = ATEToken.isLexerName(ruleName);
            int index = insertionIndexForRule(lexer);
            String ruleContent = window.getText().substring(leftIndex, rightIndex);
            if(index > window.getCaretPosition()) {
                insertRuleAtIndex(createRule(ruleName, ruleContent), index);
                mutator.replace(leftIndex, rightIndex, ruleName);
            } else {
                mutator.replace(leftIndex, rightIndex, ruleName);
                insertRuleAtIndex(createRule(ruleName, ruleContent), index);
            }
            endRefactor();
        }
    }

    public boolean canInlineRule() {
        return window.editorRules.getEnclosingRuleAtPosition(window.getCaretPosition()) != null;
    }

    public void inlineRule() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_INLINE_RULE);

        ElementRule rule = window.editorRules.getEnclosingRuleAtPosition(window.getCaretPosition());
        if(rule == null) {
            XJAlert.display(window.getJavaContainer(), "Inline Rule", "There is no rule at cursor position.");
            return;
        }

        inlineRule(rule);
    }

    protected void inlineRule(ElementRule rule) {
        String oldContent = window.getText();

        beginRefactor("Inline");

        String ruleName = rule.name;
        String ruleContent = Utils.trimString(oldContent.substring(rule.colon.getEndIndex(), rule.end.getStartIndex()));

        List<ElementRule> rules = window.editorRules.getRules();
        if(rule.end.index - rule.colon.index > 2) {
            // More than one token, append ()
            ruleContent = "("+ruleContent+")";
        }

        for(int r=rules.size()-1; r>=0; r--) {
            ElementRule candidate = rules.get(r);
            if(candidate == rule) {
                mutator.delete(rule.getStartIndex(), rule.getEndIndex()+1);
            } else {
                List<ElementReference> references = candidate.getReferences();
                if(references == null)
                    continue;

                for(int index=references.size()-1; index>=0; index--) {
                    ElementReference ref = references.get(index);
                    if(ref.token.getAttribute().equals(ruleName)) {
                        mutator.replace(ref.token.getStartIndex(), ref.token.getEndIndex(), ruleContent);
                    }
                }
            }
        }

        endRefactor();
    }

    public void createRuleAtIndex(boolean lexer, String name, String content) {
        beginRefactor("Create Rule");
        int index = insertionIndexForRule(lexer);
        insertRuleAtIndex(createRule(name, content), index);
        window.setCaretPosition(index);
        endRefactor();
    }

    public void deleteRuleAtIndex(int index) {
        ElementRule r = window.editorRules.getEnclosingRuleAtPosition(index);
        if(r != null)
            window.replaceText(r.getStartIndex(), r.getEndIndex(), "");
    }

    public int insertionIndexForRule(boolean lexer) {
        // Add the rule in the next line by default
        Point p = window.getTextEditor().getLineTextPositionsAtTextPosition(window.getCaretPosition());
        int insertionIndex = p.y;

        ElementRule rule = window.editorRules.getEnclosingRuleAtPosition(window.getCaretPosition());
        if(rule != null) {
            if(rule.lexer) {
                if(lexer) {
                    // Add new rule just after this one
                    insertionIndex = rule.getEndIndex();
                } else {
                    // Add new rule after the last parser rule
                    ElementRule last = window.editorRules.getLastParserRule();
                    if(last != null) insertionIndex = last.getEndIndex();
                }
            } else {
                if(lexer) {
                    // Add new rule after the last lexer rule
                    ElementRule last = window.editorRules.getLastLexerRule();
                    if(last != null) {
                        insertionIndex = last.getEndIndex();
                    } else {
                        // Add new rule after the last rule
                        last = window.editorRules.getLastRule();
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
        StringBuilder sb = new StringBuilder();

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
        return sb.toString();
    }

    protected void insertRuleAtIndex(String rule, int index) {
        mutator.insertAtLinesBoundary(index, rule);
    }

    protected void beginRefactor(String name) {
        window.beginGroupChange(name);
        mutator = new EditorTextMutator();
        engine.setMutator(mutator);
        engine.setTokens(window.getTokens());
    }

    protected void endRefactor() {
        mutator.apply();
        mutator = null;
        window.endGroupChange();
    }

    protected void refactorReplaceEditorText(String text) {
        int oldCaretPosition = window.getCaretPosition();
        window.disableTextPaneUndo();
        window.setText(text);
        window.enableTextPaneUndo();
        window.getTextEditor().setCaretPosition(Math.min(oldCaretPosition, text.length()), false, false);
    }

    public class EditorTextMutator implements RefactorMutator {

        public StringBuilder mutableText;

        public EditorTextMutator() {
            mutableText = new StringBuilder(window.getText());
        }

        public void replace(int start, int end, String s) {
            mutableText.replace(start, end, s);
        }

        public void insert(int index, String s) {
            mutableText.insert(index, s);
        }

        public void insertAtLinesBoundary(int index, String s) {
            if(!(mutableText.charAt(index) == '\n' && mutableText.charAt(index-1) == '\n')) {
                mutableText.insert(index++, '\n');
            }
            mutableText.insert(index, s);
            int end = index+s.length();
            if(!(mutableText.charAt(end) == '\n' && end+1 < mutableText.length() && mutableText.charAt(end+1) == '\n'))
            {
                mutableText.insert(end, '\n');
            }
        }

        public void delete(int start, int end) {
            mutableText.delete(start, end);
        }

        public void apply() {
            String text = mutableText.toString();
            String oldContent = window.getText();

            refactorReplaceEditorText(text);

            XJUndo undo = window.getUndo(window.getTextPane());
            undo.addEditEvent(new UndoableRefactoringEdit(oldContent, text));
        }

    }

    protected class UndoableRefactoringEdit extends AbstractUndoableEdit {

        public String oldContent;
        public String newContent;

        public UndoableRefactoringEdit(String oldContent, String newContent) {
            this.oldContent = oldContent;
            this.newContent = newContent;
        }

        public void redo() {
            super.redo();
            refactorReplaceEditorText(newContent);
        }

        public void undo() {
            super.undo();
            refactorReplaceEditorText(oldContent);
        }
    }

}
