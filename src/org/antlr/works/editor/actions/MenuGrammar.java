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
import edu.usfca.xj.appkit.utils.XJDialogProgress;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.tool.TDecisionDFA;
import org.antlr.works.parser.Parser;
import org.antlr.works.parser.Token;
import org.antlr.works.stats.Statistics;

import javax.swing.*;

public class MenuGrammar extends AbstractActions implements TDecisionDFA.TDecisionDFADelegate {

    protected XJDialogProgress progress;

    public MenuGrammar(EditorWindow editor) {
        super(editor);
        progress = new XJDialogProgress(editor.getWindowContainer());
    }

    public void showDecisionDFA() {
        showProgress("Generating...");
        TDecisionDFA decision = new TDecisionDFA(editor, this);
        if(!decision.launch())
            hideProgress();
    }

    public void decisionDFADidCompleted(TDecisionDFA decision, String error) {
        progress.close();
        if(error == null) {
            Parser.Rule rule = editor.getCurrentRule();
            editor.getTabbedPane().add("Decision "+decision.decisionNumber+" of \""+rule.name+"\"", decision.getContainer());
            editor.getTabbedPane().setSelectedIndex(editor.getTabbedPane().getTabCount()-1);
        } else {
            XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the DFA because: "+error);
        }
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
            editor.editorGUI.replaceText(t.getStartIndex()-1, t.getEndIndex(), "");
        }

        Token t = openGroup.token;
        editor.editorGUI.replaceText(t.getStartIndex()-1, t.getEndIndex(), "");

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
