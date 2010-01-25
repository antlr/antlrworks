package org.antlr.works.debugger.panels;

import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.debugger.events.DBEventEnterRule;
import org.antlr.works.utils.DetachablePanel;
import org.antlr.xjlib.appkit.swing.XJTable;
import org.antlr.xjlib.appkit.swing.XJTableView;

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

    private XJTableView infoTableView;
    private DBStackPanel.RuleTableDataModel ruleTableDataModel;
    private Stack<DBEventEnterRule> rules = new Stack<DBEventEnterRule>();

    public DBStackPanel(DebuggerTab debuggerTab) {
        super("Stack", debuggerTab);

        ruleTableDataModel = new DBStackPanel.RuleTableDataModel();

        infoTableView = new XJTableView();
        setInfoTableModel(infoTableView.getTable(), ruleTableDataModel);

        mainPanel.add(infoTableView, BorderLayout.CENTER);

        infoTableView.autoresizeColumns();
    }

    public void setInfoTableModel(XJTable table, AbstractTableModel model) {
        table.setModel(model);
        selectLastInfoTableItem();
    }

    public void selectLastInfoTableItem() {
        infoTableView.scrollToLastRow();
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

    public void pushRule(DBEventEnterRule rule) {
        rules.push(rule);
        ruleTableDataModel.add(rule.name);
    }

    public void popRule() {
        ruleTableDataModel.remove(rules.peek().name);
        rules.pop();
    }

    public DBEventEnterRule peekRule() {
        if(rules.isEmpty()) {
            return null;
        } else {
            return rules.peek();            
        }
    }

    public class RuleTableDataModel extends AbstractTableModel {

        protected java.util.List<String> rules = new ArrayList<String>();

        public void add(String rule) {
            rules.add(rule);
        }

        public void remove(String rule) {
            rules.remove(rule);
        }

        public void clear() {
            rules.clear();
            fireTableDataChanged();
            infoTableView.autoresizeColumns();
        }

        public void update() {
            fireTableDataChanged();
            infoTableView.autoresizeColumns();
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
