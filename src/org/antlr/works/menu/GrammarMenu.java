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
import org.antlr.works.components.GrammarWindow;
import org.antlr.works.grammar.CheckGrammar;
import org.antlr.works.grammar.CheckGrammarDelegate;
import org.antlr.works.grammar.RulesDependency;
import org.antlr.works.grammar.TokensDFA;
import org.antlr.works.grammar.antlr.GrammarResult;
import org.antlr.works.grammar.decisiondfa.DecisionDFA;
import org.antlr.works.grammar.element.ElementGroup;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.grammar.syntax.GrammarSyntaxParser;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.utils.XJDialogProgressDelegate;

import javax.swing.*;
import java.util.List;

public class GrammarMenu implements CheckGrammarDelegate, XJDialogProgressDelegate {

    private final GrammarWindow window;    
    private CheckGrammar checkGrammar;
    private boolean checkingGrammar;

    public GrammarMenu(GrammarWindow window) {
        this.window = window;
    }

    public void showTokensSD() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_TOKENS_SD);
        window.syntaxDiagramTab.setRule(new ElementRule(Grammar.ARTIFICIAL_TOKENS_RULENAME), true);
    }

    public void showTokensDFA() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_TOKENS_DFA);
        TokensDFA decision = new TokensDFA(window);
        decision.launch();
    }

    public void showDecisionDFA() {
        DecisionDFA decision = new DecisionDFA(window);
        decision.launch();
    }

    public void highlightDecisionDFA() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_DECISION_DFA);
        try {
            if(window.decisionDFAEngine.getDecisionDFACount() == 0) {
                window.decisionDFAEngine.discoverAllDecisions();
            } else {
                window.decisionDFAEngine.reset();
            }
            window.decisionDFAEngine.refresh();
            window.decisionDFAEngine.refreshMenu();
        } catch (Exception e) {
            e.printStackTrace();
            XJAlert.display(window.getJavaContainer(), "Error", "Cannot show the DFA:\n"+e.toString());
        }
    }

    public void showDependency() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_RULE_DEPENDENCY);
        RulesDependency dependency = new RulesDependency(window);
        dependency.launch();
    }

    public void group() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_RULE_GROUP);

        String s = (String)JOptionPane.showInputDialog(window.getJavaContainer(), "Group Name:", "Group",
                JOptionPane.QUESTION_MESSAGE, null, null, "Group");
        if(s != null && s.length() > 0) {
            List<ElementRule> rules = window.editorRules.getSelectedRules();
            if(!rules.isEmpty()) {
                window.beginGroupChange("Group");

                ElementRule firstRule = rules.get(0);
                ElementRule lastRule = rules.get(rules.size()-1);

                int end = lastRule.getEndIndex();
                window.getTextEditor().insertText(end+1, "\n"+ GrammarSyntaxParser.END_GROUP+"\n");

                int start = firstRule.getStartIndex();
                window.getTextEditor().insertText(start-1, "\n"+ GrammarSyntaxParser.BEGIN_GROUP+s+"\n");

                window.endGroupChange();
            }
        }
    }

    public void ungroup() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_RULE_UNGROUP);

        ElementGroup openGroup = window.editorRules.getSelectedGroup();
        if(openGroup == null) {
            // No open group selected in the tree. Try to find the closest open group
            // by moving backward
            openGroup = window.editorRules.findOpenGroupClosestToLocation(window.getTextPane().getSelectionStart());
            if(openGroup == null) {
                // Still no open group ? Give up
                XJAlert.display(window.getJavaContainer(), "Ungroup", "Cannot ungroup because no enclosing group has been found.");
                return;
            }
        }

        ElementGroup closingGroup = window.editorRules.findClosingGroupForGroup(openGroup);

        window.beginGroupChange("Ungroup");

        if(closingGroup != null) {
            // End of file is considered as a closing group but no group really exists
            // for that purpose
            ATEToken t = closingGroup.token;
            window.replaceText(t.getStartIndex()-1, t.getEndIndex(), "");
        }

        ATEToken t = openGroup.token;
        window.replaceText(t.getStartIndex()-1, t.getEndIndex(), "");

        window.endGroupChange();
    }

    public void ignore() {
        window.editorRules.ignoreSelectedRules(true);
    }

    public void consider() {
        window.editorRules.ignoreSelectedRules(false);
    }

    public void checkGrammar() {
        window.showProgress("Checking Grammar...", this);

        if(AWPrefs.isClearConsoleBeforeCheckGrammar()) {
            window.consoleTab.clear();
        }

        window.saveAll();

        window.consoleTab.makeCurrent();
        window.consoleTab.println("Checking Grammar "+window.getGrammarFileName()+"...");

        checkGrammar = new CheckGrammar(window, this);
        checkGrammar.check();

        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_CHECK_GRAMMAR);
    }

    public void checkGrammarDidBegin(CheckGrammar source) {
        checkingGrammar = true;
    }

    public void checkGrammarDidEnd(CheckGrammar source, GrammarResult result) {
        checkGrammar.close();
        checkGrammar = null;

        checkingGrammar = false;
        window.hideProgress();
        if(result.isSuccess()) {
            if(AWPrefs.isAlertCheckGrammarSuccess()) {
                XJAlert alert = XJAlert.createInstance();
                alert.setDisplayDoNotShowAgainButton(true);
                alert.showSimple(window.getJavaContainer(), "Success", "Check Grammar succeeded.");
                AWPrefs.setAlertCheckGrammarSuccess(!alert.isDoNotShowAgain());
            }
        } else {
            if(result.getErrorCount() > 0) {
                XJAlert.display(window.getJavaContainer(), "Error", "Check Grammar reported some errors:\n"+result.getFirstErrorMessage()+"\nConsult the console for more information.");
            } else if(result.getWarningCount() > 0) {
                XJAlert.display(window.getJavaContainer(), "Warning", "Check Grammar reported some warnings:\n"+result.getFirstWarningMessage()+"\nConsult the console for more information.");
            } else {
                XJAlert.display(window.getJavaContainer(), "Error", "Check Grammar reported some errors.\nConsult the console for more information.");
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
