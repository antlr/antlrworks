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

package org.antlr.works.editor.actions;

import edu.usfca.xj.appkit.utils.XJAlert;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.tool.TUsage;
import org.antlr.works.parser.Lexer;
import org.antlr.works.parser.Parser;
import org.antlr.works.parser.Token;
import org.antlr.works.stats.Statistics;

import javax.swing.*;
import java.util.Iterator;
import java.util.List;

public class MenuGrammar extends AbstractActions {

    public MenuGrammar(EditorWindow editor) {
        super(editor);
    }

    public void find() {
        editor.findAndReplace.find();
    }

    public void findNext() {
        editor.findAndReplace.next();
    }

    public void findPrev() {
        editor.findAndReplace.prev();
    }

    public void findUsage() {
        Token token = editor.getTokenAtPosition(getCaretPosition());
        if(token == null)
            return;

        String tokenAttribute = token.getAttribute();

        TUsage usage = new TUsage(editor);
        editor.getTabbedPane().add("Usages of \""+tokenAttribute+"\"", usage.getContainer());
        editor.getTabbedPane().setSelectedIndex(editor.getTabbedPane().getTabCount()-1);

        Iterator iterator = editor.getTokens().iterator();
        while(iterator.hasNext()) {
            Token candidate = (Token)iterator.next();
            if(candidate.getAttribute().equals(tokenAttribute)) {
                Parser.Rule matchedRule = editor.rules.getEnclosingRuleAtPosition(candidate.getStart());
                if(matchedRule != null)
                    usage.addMatch(matchedRule, candidate);
            }
        }

        Statistics.shared().recordEvent(Statistics.EVENT_FIND_USAGES);
    }

    public void rename() {
        Token token = editor.getTokenAtPosition(getCaretPosition());
        if(token == null)
            return;

        String s = (String)JOptionPane.showInputDialog(editor.getJavaContainer(), "Rename '"+token.getAttribute()+"' and its usages to:", "Rename",
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
                editor.editorGUI.replaceText(token.getStart(), token.getEnd(), name);
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
        editor.editorGUI.replaceText(insertionIndex, insertionIndex, "\n\n"+name+"\n\t:\t"+t.getAttribute()+"\n\t;");

        // Then rename all strings token
        List tokens = editor.getTokens();
        String attr = t.getAttribute();
        for(int index = tokens.size()-1; index>0; index--) {
            Token token = (Token) tokens.get(index);
            if(token.type != Lexer.TOKEN_SINGLE_QUOTE_STRING && token.type != Lexer.TOKEN_DOUBLE_QUOTE_STRING)
                continue;

            if(!token.getAttribute().equals(attr))
                continue;

            editor.editorGUI.replaceText(token.getStart(), token.getEnd(), name);
        }
    }

    public void removeLeftRecursion() {
        Parser.Rule rule = editor.rules.getEnclosingRuleAtPosition(editor.getCaretPosition());
        if(rule == null) {
            XJAlert.display(editor.getWindowContainer(), "Remove left recursion", "There is no rule at cursor position.");
            return;
        }

        if(!rule.hasLeftRecursion()) {
            XJAlert.display(editor.getWindowContainer(), "Remove left recursion", "The rule doesn't have a left recursion.");
            return;
        }

        editor.beginGroupChange("Remove Left Recursion");
        String ruleText = rule.getTextRuleAfterRemovingLeftRecursion();
        editor.editorGUI.replaceText(rule.getInternalTokensStartIndex(), rule.getInternalTokensEndIndex(), ruleText);
        editor.endGroupChange();
    }

    public void insertRuleFromTemplate() {
        editor.templateRules.display();
    }

    public void group() {
        String s = (String)JOptionPane.showInputDialog(editor.getJavaContainer(), "Group Name:", "Group",
                JOptionPane.QUESTION_MESSAGE, null, null, "Group");
        if(s != null && s.length() > 0) {
            editor.beginGroupChange("Group");

            int end = editor.getTextPane().getSelectionEnd();
            editor.editorGUI.replaceText(end+1, end+1, "\n"+Parser.END_GROUP+"\n");

            int start = editor.getTextPane().getSelectionStart();
            editor.editorGUI.replaceText(start-1, start-1, "\n"+Parser.BEGIN_GROUP+s+"\n");

            editor.endGroupChange();
        }
    }

    public void ungroup() {
        Parser.Group openGroup = editor.rules.getSelectedGroup();
        if(openGroup == null) {
            // No open group selected in the tree. Try to find the closest open group
            // by moving backward
            openGroup = editor.rules.findOpenGroupClosestToLocation(editor.getTextPane().getSelectionStart());
            if(openGroup == null) {
                // Still no open group ? Give up
                XJAlert.display(editor.getWindowContainer(), "Ungroup", "Cannot ungroup because no enclosing group has been found.");
                return;
            }
        }

        Parser.Group closingGroup = editor.rules.findClosingGroupForGroup(openGroup);

        editor.beginGroupChange("Ungroup");

        if(closingGroup != null) {
            // End of file is considered as a closing group but no group really exists
            // for that purpose
            Token t = closingGroup.token;
            editor.editorGUI.replaceText(t.getStart()-1, t.getEnd(), "");
        }

        Token t = openGroup.token;
        editor.editorGUI.replaceText(t.getStart()-1, t.getEnd(), "");

        editor.endGroupChange();
    }

    public void hideAction() {
        editor.actions.hideAction();
        Statistics.shared().recordEvent(Statistics.EVENT_HIDE_SINGLE_ACTION);
    }

    public void showAllActions() {
        editor.actions.showAllActions();
        Statistics.shared().recordEvent(Statistics.EVENT_SHOW_ALL_ACTIONS);
    }

    public void hideAllActions() {
        editor.actions.hideAllActions();
        Statistics.shared().recordEvent(Statistics.EVENT_HIDE_ALL_ACTIONS);
    }

    public void checkGrammar() {
        editor.grammar.checkGrammar();
        Statistics.shared().recordEvent(Statistics.EVENT_CHECK_GRAMMAR);
    }

}
