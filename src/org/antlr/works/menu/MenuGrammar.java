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

import org.antlr.tool.Grammar;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.container.ComponentContainerGrammar;
import org.antlr.works.grammar.*;
import org.antlr.works.grammar.decisiondfa.DecisionDFA;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.syntax.GrammarSyntaxParser;
import org.antlr.works.syntax.element.ElementGroup;
import org.antlr.works.syntax.element.ElementRule;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.utils.XJDialogProgressDelegate;

import javax.swing.*;
import java.util.List;

public class MenuGrammar extends MenuAbstract implements CheckGrammarDelegate, XJDialogProgressDelegate {

    private CheckGrammar checkGrammar;
    private boolean checkingGrammar;

    public MenuGrammar(ComponentContainerGrammar editor) {
        super(editor);
    }

    public void showTokensSD() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_TOKENS_SD);
        getEditor().visual.setRule(new ElementRule(Grammar.ARTIFICIAL_TOKENS_RULENAME), true);
        getEditor().makeBottomComponentVisible();
    }

    public void showTokensDFA() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_TOKENS_DFA);
        TokensDFA decision = new TokensDFA(getEditor());
        decision.launch();
    }

    public void showDecisionDFA() {
        DecisionDFA decision = new DecisionDFA(getEditor());
        decision.launch();
    }

    public void highlightDecisionDFA() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_DECISION_DFA);
        try {
            if(getEditor().decisionDFAEngine.getDecisionDFACount() == 0) {
                getEditor().decisionDFAEngine.discoverAllDecisions();
            } else {
                getEditor().decisionDFAEngine.reset();
            }
            getEditor().decisionDFAEngine.refresh();
            getEditor().decisionDFAEngine.refreshMenu();
        } catch (Exception e) {
            e.printStackTrace();
            XJAlert.display(getEditor().getWindowContainer(), "Error", "Cannot show the DFA:\n"+e.toString());
        }
    }

    public void showDependency() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_RULE_DEPENDENCY);
        RulesDependency dependency = new RulesDependency(getEditor());
        dependency.launch();
    }

    public void insertRuleFromTemplate() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_INSERT_RULE_TEMPLATE);
        getEditor().ruleTemplates.display();
    }

    public void group() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_RULE_GROUP);

        String s = (String)JOptionPane.showInputDialog(getEditor().getWindowContainer(), "Group Name:", "Group",
                JOptionPane.QUESTION_MESSAGE, null, null, "Group");
        if(s != null && s.length() > 0) {
            List<ElementRule> rules = getEditor().rules.getSelectedRules();
            if(!rules.isEmpty()) {
                getEditor().beginGroupChange("Group");

                ElementRule firstRule = rules.get(0);
                ElementRule lastRule = rules.get(rules.size()-1);

                int end = lastRule.getEndIndex();
                getEditor().getTextEditor().insertText(end+1, "\n"+GrammarSyntaxParser.END_GROUP+"\n");

                int start = firstRule.getStartIndex();
                getEditor().getTextEditor().insertText(start-1, "\n"+ GrammarSyntaxParser.BEGIN_GROUP+s+"\n");

                getEditor().endGroupChange();
            }
        }
    }

    public void ungroup() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_RULE_UNGROUP);

        ElementGroup openGroup = getEditor().rules.getSelectedGroup();
        if(openGroup == null) {
            // No open group selected in the tree. Try to find the closest open group
            // by moving backward
            openGroup = getEditor().rules.findOpenGroupClosestToLocation(getEditor().getTextPane().getSelectionStart());
            if(openGroup == null) {
                // Still no open group ? Give up
                XJAlert.display(getEditor().getWindowContainer(), "Ungroup", "Cannot ungroup because no enclosing group has been found.");
                return;
            }
        }

        ElementGroup closingGroup = getEditor().rules.findClosingGroupForGroup(openGroup);

        getEditor().beginGroupChange("Ungroup");

        if(closingGroup != null) {
            // End of file is considered as a closing group but no group really exists
            // for that purpose
            ATEToken t = closingGroup.token;
            getEditor().replaceText(t.getStartIndex()-1, t.getEndIndex(), "");
        }

        ATEToken t = openGroup.token;
        getEditor().replaceText(t.getStartIndex()-1, t.getEndIndex(), "");

        getEditor().endGroupChange();
    }

    public void ignore() {
        getEditor().rules.ignoreSelectedRules(true);
    }

    public void consider() {
        getEditor().rules.ignoreSelectedRules(false);
    }

    public void checkGrammar() {
        getEditor().showProgress("Checking Grammar...", this);

        getEditor().console.makeCurrent();
        getEditor().console.println("Checking Grammar...");

        checkGrammar = new CheckGrammar(getEditor(), this);
        checkGrammar.check();

        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_CHECK_GRAMMAR);
    }

    public void checkGrammarDidBegin() {
        checkingGrammar = true;
    }

    public void checkGrammarDidEnd(EngineGrammarResult result) {
        checkGrammar.close();
        checkGrammar = null;

        checkingGrammar = false;
        getEditor().hideProgress();
        if(result.isSuccess()) {
            XJAlert.display(getEditor().getWindowContainer(), "Success", "Check Grammar succeeded.");
        } else {
            if(result.getErrorCount() > 0) {
                XJAlert.display(getEditor().getWindowContainer(), "Error", "Check Grammar reported some errors:\n"+result.getFirstErrorMessage()+"\nConsult the console for more information.");
            } else if(result.getWarningCount() > 0) {
                XJAlert.display(getEditor().getWindowContainer(), "Warning", "Check Grammar reported some warnings:\n"+result.getFirstWarningMessage()+"\nConsult the console for more information.");
            } else {
                XJAlert.display(getEditor().getWindowContainer(), "Error", "Check Grammar reported some errors.\nConsult the console for more information.");
            }
        }
    }

    public void dialogDidCancel() {
        if(checkingGrammar) {
            checkGrammar.cancel();
            checkGrammar.close();
            checkGrammar = null;
        }
    }
}
