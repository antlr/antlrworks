package org.antlr.works.debugger.panels;

import org.antlr.works.debugger.Debugger;
import org.antlr.works.swing.DetachablePanel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
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

public class DBStackPanel extends DetachablePanel {

    public static final int INFO_COLUMN_COUNT = 0;
    public static final int INFO_COLUMN_RULE = 1;

    protected JTable infoTable;

    protected DBStackPanel.RuleTableDataModel ruleTableDataModel;

    protected Stack<String> rules = new Stack<String>();

    public DBStackPanel(Debugger debugger) {
        super("Stack", debugger);

        ruleTableDataModel = new DBStackPanel.RuleTableDataModel();

        infoTable = new JTable();
        setInfoTableModel(ruleTableDataModel);

        JScrollPane infoScrollPane = new JScrollPane(infoTable);
        infoScrollPane.setWheelScrollingEnabled(true);
        infoTable.getParent().setBackground(Color.white);

        mainPanel.add(infoScrollPane, BorderLayout.CENTER);
    }

    public void setInfoTableModel(AbstractTableModel model) {
        infoTable.setModel(model);
        infoTable.getColumnModel().getColumn(DBStackPanel.INFO_COLUMN_COUNT).setMaxWidth(35);
        selectLastInfoTableItem();
    }

    public void selectLastInfoTableItem() {
        int count = ruleTableDataModel.rules.size();
        infoTable.scrollRectToVisible(infoTable.getCellRect(count-1, 0, true));
    }

    public void clear() {
        rules.clear();
        ruleTableDataModel.clear();
    }

    public void updateOnBreakEvent() {
        ruleTableDataModel.update();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                selectLastInfoTableItem();
            }
        });
    }

    public void pushRule(String ruleName) {
        rules.push(ruleName);
        ruleTableDataModel.add(ruleName);
    }

    public void popRule() {
        ruleTableDataModel.remove(rules.peek());
        rules.pop();
    }

    public class RuleTableDataModel extends AbstractTableModel {

        protected java.util.List rules = new ArrayList();

        public void add(Object rule) {
            rules.add(rule);
        }

        public void remove(Object rule) {
            rules.remove(rule);
        }

        public void clear() {
            rules.clear();
            fireTableDataChanged();
        }

        public void update() {
            fireTableDataChanged();
        }

        public int getRowCount() {
            return rules.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int column) {
            switch(column) {
                case DBStackPanel.INFO_COLUMN_COUNT: return "#";
                case DBStackPanel.INFO_COLUMN_RULE: return "Rule";
            }
            return super.getColumnName(column);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch(columnIndex) {
                case DBStackPanel.INFO_COLUMN_COUNT: return String.valueOf(rowIndex);
                case DBStackPanel.INFO_COLUMN_RULE: return rules.get(rowIndex);
            }
            return null;
        }
    }

}
