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

package org.antlr.works.menu;

import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.utils.XJDialogProgress;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.grammar.*;
import org.antlr.works.stats.Statistics;
import org.antlr.works.syntax.GrammarSyntaxGroup;
import org.antlr.works.syntax.GrammarSyntaxParser;
import org.antlr.works.syntax.GrammarSyntaxRule;

import javax.swing.*;
import java.util.List;

public class MenuGrammar extends MenuAbstract implements GrammarDOTTab.GrammarDOTTabDelegate, CheckGrammarDelegate {

    protected XJDialogProgress progress;
    protected CheckGrammar checkGrammar;

    public MenuGrammar(CEditorGrammar editor) {
        super(editor);
        progress = new XJDialogProgress(editor.getWindowContainer());
        checkGrammar = new CheckGrammar(editor, this);
    }

    public void showTokensSD() {
        Statistics.shared().recordEvent(Statistics.EVENT_SHOW_TOKEN_SD);
        editor.visual.setRule(new GrammarSyntaxRule("Tokens"), true);
        editor.makeBottomComponentVisible();
    }

    public void showDecisionDFA() {
        Statistics.shared().recordEvent(Statistics.EVENT_SHOW_DECISION_DFA);
        DecisionDFA decision = new DecisionDFA(editor, this);
        if(decision.launch())
            showProgress("Generating...");
    }

    public void showDependency() {
        Statistics.shared().recordEvent(Statistics.EVENT_SHOW_RULE_DEPENDENCY);
        RulesDependency dependency = new RulesDependency(editor, this);
        if(dependency.launch())
            showProgress("Generating...");
    }

    public void grammarDOTTabDidComplete(GrammarDOTTab tab, String error) {
        progress.close();
        if(error == null) {
            editor.addTab(tab);
            editor.makeBottomComponentVisible();
        } else {
            if(tab instanceof DecisionDFA)
                XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the DFA:\n"+error);
            if(tab instanceof RulesDependency)
                XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the rule dependency graph:\n"+error);
        }
    }

    public void insertRuleFromTemplate() {
        Statistics.shared().recordEvent(Statistics.EVENT_INSERT_RULE_TEMPLATE);
        editor.ruleTemplates.display();
    }

    public void group() {
        Statistics.shared().recordEvent(Statistics.EVENT_RULE_GROUP);

        String s = (String)JOptionPane.showInputDialog(editor.getWindowContainer(), "Group Name:", "Group",
                JOptionPane.QUESTION_MESSAGE, null, null, "Group");
        if(s != null && s.length() > 0) {
            List rules = editor.rules.getSelectedRules();
            if(!rules.isEmpty()) {
                editor.beginGroupChange("Group");

                GrammarSyntaxRule firstRule = (GrammarSyntaxRule) rules.get(0);
                GrammarSyntaxRule lastRule = (GrammarSyntaxRule) rules.get(rules.size()-1);

                int end = lastRule.getEndIndex();
                editor.textEditor.insertText(end+1, "\n"+GrammarSyntaxParser.END_GROUP+"\n");

                int start = firstRule.getStartIndex();
                editor.textEditor.insertText(start-1, "\n"+ GrammarSyntaxParser.BEGIN_GROUP+s+"\n");

                editor.endGroupChange();
            }
        }
    }

    public void ungroup() {
        Statistics.shared().recordEvent(Statistics.EVENT_RULE_UNGROUP);        

        GrammarSyntaxGroup openGroup = editor.rules.getSelectedGroup();
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

        GrammarSyntaxGroup closingGroup = editor.rules.findClosingGroupForGroup(openGroup);

        editor.beginGroupChange("Ungroup");

        if(closingGroup != null) {
            // End of file is considered as a closing group but no group really exists
            // for that purpose
            ATEToken t = closingGroup.token;
            editor.replaceText(t.getStartIndex()-1, t.getEndIndex(), "");
        }

        ATEToken t = openGroup.token;
        editor.replaceText(t.getStartIndex()-1, t.getEndIndex(), "");

        editor.endGroupChange();
    }

    public void ignore() {
        editor.rules.ignoreSelectedRules(true);
    }

    public void consider() {
        editor.rules.ignoreSelectedRules(false);
    }

    public void checkGrammar() {
        showProgress("Checking Grammar...");

        editor.console.makeCurrent();
        editor.console.println("Checking Grammar...");
        checkGrammar.check();

        Statistics.shared().recordEvent(Statistics.EVENT_CHECK_GRAMMAR);
    }

    public void checkGrammarDidBegin() {
    }

    public void checkGrammarDidEnd() {
        hideProgress();
    }

    protected void showProgress(String title) {
        progress.setInfo(title);
        progress.setCancellable(false);
        progress.setIndeterminate(true);
        progress.display();
    }

    protected void hideProgress() {
        progress.close();
    }

}
