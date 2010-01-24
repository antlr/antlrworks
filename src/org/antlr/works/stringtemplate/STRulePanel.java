package org.antlr.works.stringtemplate;

import org.antlr.works.stringtemplate.element.ElementTemplateRule;
import org.antlr.works.stringtemplate.syntax.ATEStringTemplateSyntaxParser;
import org.antlr.xjlib.appkit.swing.XJTableView;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*

[The "BSD licence"]
Copyright (c) 2009 Jean Bovet
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

public class STRulePanel extends JPanel {

    protected STWindow window;

    private STRuleModel model;
    private final XJTableView tableView = new XJTableView();

    public STRulePanel(STWindow window) {
        this.window = window;
        model = new STRuleModel();

        tableView.setAlternateBackground(true);

        refreshRules();

        setLayout(new BorderLayout());
        tableView.autoresizeColumns();
        tableView.getTable().addMouseListener(new RuleTableMouseListener());
        add(tableView, BorderLayout.CENTER);
    }

    public boolean isRulesSorted() {
        return model.isRulesSorted();
    }

    public void toggleSorting() {
        model.toggleSort();
    }

    public void refreshRules() {
        model.clear();
        List<ElementTemplateRule> rules = ((ATEStringTemplateSyntaxParser) window.getTextEditor().getParserEngine().getParser()).templateRules;
        for (ElementTemplateRule rule : rules) {
            model.addRule(rule);
        }
        model.fireSort();
        tableView.getTable().setModel(model);
        tableView.autoresizeColumns();
    }

    public class RuleTableMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            int selectedRow = tableView.getTable().getSelectedRow();
            String selectedRuleName = null;

            if (selectedRow > -1) {
                selectedRuleName = tableView.getTable().getModel().getValueAt(selectedRow, 0).toString();
                window.goToRule(selectedRuleName);
            }

            tableView.requestFocusInWindow();

            checkForPopupTrigger(e);
        }

        public void mouseReleased(MouseEvent e) {
            checkForPopupTrigger(e);
        }

        public void checkForPopupTrigger(MouseEvent e) {
            if(e.isPopupTrigger()) {
                JPopupMenu menu = window.rulesGetContextualMenu();
                if(menu != null)
                    menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private static class STRuleModel extends AbstractTableModel {

        private final List<String> rules = new ArrayList<String>();
        private final List<String> sortedRules = new ArrayList<String>();
        private boolean sorted = false;

        public STRuleModel() {
        }

        public void addRule(ElementTemplateRule rule) {
            rules.add(rule.name);
            sortedRules.add(rule.name);
            final int index = rules.size()-1;
            fireTableRowsInserted(index, index);
        }

        public boolean isRulesSorted() {
            return sorted;
        }

        public boolean toggleSort() {
            sorted = !sorted;
            return sorted;
        }

        public void fireSort() {
            Collections.sort(sortedRules);
        }

        public void clear() {
            rules.clear();
            sortedRules.clear();
        }

        public int getRowCount() {
            return rules.size();
        }

        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return "Rules";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (sorted) {
                return sortedRules.get(rowIndex);
            } else {
                return rules.get(rowIndex);
            }
        }
    }
}
