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
import org.antlr.works.grammar.DecisionDFA;
import org.antlr.works.stats.Statistics;
import org.antlr.works.syntax.GrammarSyntaxGroup;
import org.antlr.works.syntax.GrammarSyntaxParser;
import org.antlr.works.syntax.GrammarSyntaxRule;
import org.antlr.works.visualization.Visual;
import org.antlr.works.visualization.VisualDelegate;

import javax.swing.*;

public class MenuGrammar extends MenuAbstract implements DecisionDFA.TDecisionDFADelegate, VisualDelegate {

    protected XJDialogProgress progress;

    public MenuGrammar(CEditorGrammar editor) {
        super(editor);
        progress = new XJDialogProgress(editor.getWindowContainer());
    }

    public void showTokensSD() {
        editor.visual.setRule(new GrammarSyntaxRule("Tokens"), true);
        editor.makeBottomComponentVisible();
    }

    public void showDecisionDFA() {
        showProgress("Generating...");
        DecisionDFA decision = new DecisionDFA(editor, this);
        if(!decision.launch())
            hideProgress();
    }

    public void decisionDFADidCompleted(DecisionDFA decision, String error) {
        progress.close();
        if(error == null) {
            GrammarSyntaxRule rule = editor.getCurrentRule();
            decision.setRuleName(rule.name);
            editor.addTab(decision);
            editor.makeBottomComponentVisible();
        } else {
            XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the DFA because: "+error);
        }
    }

    public void insertRuleFromTemplate() {
        editor.ruleTemplates.display();
    }

    public void group() {
        String s = (String)JOptionPane.showInputDialog(editor.getWindowContainer(), "Group Name:", "Group",
                JOptionPane.QUESTION_MESSAGE, null, null, "Group");
        if(s != null && s.length() > 0) {
            editor.beginGroupChange("Group");

            int end = editor.getTextPane().getSelectionEnd();
            editor.textEditor.insertText(end+1, "\n"+GrammarSyntaxParser.END_GROUP+"\n");

            int start = editor.getTextPane().getSelectionStart();
            editor.textEditor.insertText(start-1, "\n"+ GrammarSyntaxParser.BEGIN_GROUP+s+"\n");

            editor.endGroupChange();
        }
    }

    public void ungroup() {
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

    public void checkGrammar() {
        showProgress("Checking Grammar...");

        editor.console.makeCurrent();
        editor.visual.setDelegate(this);
        editor.visual.checkGrammar();

        Statistics.shared().recordEvent(Statistics.EVENT_CHECK_GRAMMAR);
    }

    public void visualizationProcessDidBegin(Visual visual) {
    }

    public void visualizationProcessDidEnd(Visual visual) {
        editor.updateInformation();
        hideProgress();
    }

    public void visualizationDidMarkRules(Visual visual) {
        editor.rules.refreshRules();
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
