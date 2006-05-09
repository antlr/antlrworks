package org.antlr.works.debugger.panels;

import org.antlr.works.debugger.events.DBEvent;
import org.antlr.works.debugger.tivo.DBPlayerContextInfo;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
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

public class DBInfoPanel extends JPanel {

    public static final int INFO_COLUMN_COUNT = 0;  // 1st column of rule and even table
    public static final int INFO_COLUMN_RULE = 1;   // 2nd column of rule table
    public static final int INFO_COLUMN_EVENT = 1;  // 2nd column of event table
    public static final int INFO_COLUMN_SUBRULE = 2;
    public static final int INFO_COLUMN_DECISION = 3;
    public static final int INFO_COLUMN_MARK = 4;
    public static final int INFO_COLUMN_BACKTRACK = 5;

    protected JRadioButton displayEventButton;
    protected JRadioButton displayRuleButton;

    protected JTable infoTable;

    protected RuleTableDataModel ruleTableDataModel;
    protected EventTableDataModel eventTableDataModel;

    protected Stack rules = new Stack();

    public DBInfoPanel() {
        super(new BorderLayout());

        ruleTableDataModel = new RuleTableDataModel();
        eventTableDataModel = new EventTableDataModel();

        displayEventButton = new JRadioButton("Events");
        displayEventButton.setFocusable(false);
        displayEventButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setInfoTableModel(eventTableDataModel);
            }
        });

        displayRuleButton = new JRadioButton("Rules");
        displayRuleButton.setFocusable(false);
        displayRuleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setInfoTableModel(ruleTableDataModel);
            }
        });

        displayEventButton.setSelected(false);
        displayRuleButton.setSelected(true);

        ButtonGroup bp = new ButtonGroup();
        bp.add(displayEventButton);
        bp.add(displayRuleButton);

        infoTable = new JTable();
        //infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        infoTable.setDefaultRenderer(Integer.class, new InfoTableCellRenderer());
        setInfoTableModel(ruleTableDataModel);

        JScrollPane infoScrollPane = new JScrollPane(infoTable);
        infoScrollPane.setWheelScrollingEnabled(true);

        JPanel infoControlPanel = new JPanel();
        infoControlPanel.add(displayRuleButton);
        infoControlPanel.add(displayEventButton);

        add(infoControlPanel, BorderLayout.SOUTH);
        add(infoScrollPane, BorderLayout.CENTER);
    }

    public void setInfoTableModel(AbstractTableModel model) {
        infoTable.setModel(model);

        if(infoTable.getModel() == eventTableDataModel) {
            infoTable.getColumnModel().getColumn(INFO_COLUMN_COUNT).setPreferredWidth(35);
            infoTable.getColumnModel().getColumn(INFO_COLUMN_EVENT).setMinWidth(100);
            infoTable.getColumnModel().getColumn(INFO_COLUMN_SUBRULE).setMaxWidth(30);
            infoTable.getColumnModel().getColumn(INFO_COLUMN_DECISION).setMaxWidth(30);
            infoTable.getColumnModel().getColumn(INFO_COLUMN_MARK).setMaxWidth(30);
            infoTable.getColumnModel().getColumn(INFO_COLUMN_BACKTRACK).setMaxWidth(30);
        } else {
            infoTable.getColumnModel().getColumn(INFO_COLUMN_COUNT).setMaxWidth(35);
        }

        selectLastInfoTableItem();
    }

    public void selectLastInfoTableItem() {
        int count;
        if(displayEventButton.isSelected())
            count = eventTableDataModel.events.size();
        else
            count = ruleTableDataModel.rules.size();
        infoTable.scrollRectToVisible(infoTable.getCellRect(count-1, 0, true));
    }

    public void clear() {
        rules.clear();
        ruleTableDataModel.clear();
        eventTableDataModel.clear();
    }

    public void updateOnBreakEvent() {
        ruleTableDataModel.update();
        eventTableDataModel.update();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                selectLastInfoTableItem();
            }
        });
    }

    public void addEvent(DBEvent event, DBPlayerContextInfo info) {
        eventTableDataModel.add(event, info);
    }

    public void pushRule(String ruleName) {
        rules.push(ruleName);
        ruleTableDataModel.add(ruleName);
    }

    public void popRule() {
        ruleTableDataModel.remove(rules.peek());
        rules.pop();
    }

    public String getEventsAsString() {
        StringBuffer sb = new StringBuffer();
        sb.append(eventTableDataModel.getHeadersAsString());
        sb.append("\n");

        List events = eventTableDataModel.events;
        for(int i=0; i<events.size(); i++) {
            sb.append(i);
            sb.append(":\t");
            sb.append(events.get(i).toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public class RuleTableDataModel extends AbstractTableModel {

        protected List rules = new ArrayList();

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
                case INFO_COLUMN_COUNT: return "#";
                case INFO_COLUMN_RULE: return "Rule";
            }
            return super.getColumnName(column);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch(columnIndex) {
                case INFO_COLUMN_COUNT: return new Integer(rowIndex);
                case INFO_COLUMN_RULE: return rules.get(rowIndex);
            }
            return null;
        }
    }

    public class EventTableDataModel extends AbstractTableModel {

        protected List events = new ArrayList();

        public void add(DBEvent event, DBPlayerContextInfo info) {
            events.add(new EventInfo(event, info));
        }

        public void clear() {
            events.clear();
            fireTableDataChanged();
        }

        public void update() {
            fireTableDataChanged();
        }

        public int getRowCount() {
            return events.size();
        }

        public int getColumnCount() {
            return 6;
        }

        public String getColumnName(int column) {
            switch(column) {
                case INFO_COLUMN_COUNT: return "#";
                case INFO_COLUMN_EVENT: return "Event";
                case INFO_COLUMN_SUBRULE: return "SR";
                case INFO_COLUMN_DECISION: return "DEC";
                case INFO_COLUMN_MARK: return "MK";
                case INFO_COLUMN_BACKTRACK: return "BK";
            }
            return super.getColumnName(column);
        }

        public Class getColumnClass(int columnIndex) {
            switch(columnIndex) {
                case INFO_COLUMN_COUNT: return Integer.class;
                case INFO_COLUMN_EVENT: return String.class;
                case INFO_COLUMN_SUBRULE: return Integer.class;
                case INFO_COLUMN_DECISION: return Integer.class;
                case INFO_COLUMN_MARK: return Integer.class;
                case INFO_COLUMN_BACKTRACK: return Integer.class;
            }
            return super.getColumnClass(columnIndex);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            EventInfo info = (EventInfo) events.get(rowIndex);
            switch(columnIndex) {
                case INFO_COLUMN_COUNT: return new Integer(rowIndex);
                case INFO_COLUMN_EVENT: return info.event;
                case INFO_COLUMN_SUBRULE: return info.getSubrule();
                case INFO_COLUMN_DECISION: return info.getDecision();
                case INFO_COLUMN_MARK: return info.getMark();
                case INFO_COLUMN_BACKTRACK: return info.getBacktrack();
            }
            return null;
        }

        public String getHeadersAsString() {
            return "#\tEvent\tSubrule\tDecision\tMark\tBacktrack";
        }

        public class EventInfo {

            public DBEvent event;
            public int subrule;
            public int decision;
            public int mark;
            public int backtrack;

            public EventInfo(DBEvent event, DBPlayerContextInfo info) {
                this.event = event;
                this.subrule = info.getSubrule();
                this.decision = info.getDecision();
                this.mark = info.getMark();
                this.backtrack = info.getBacktrack();
            }

            public Object getSubrule() {
                return subrule==-1?null:new Integer(subrule);
            }

            public Object getDecision() {
                return decision==-1?null:new Integer(decision);
            }

            public Object getMark() {
                return mark==-1?null:new Integer(mark);
            }

            public Object getBacktrack() {
                return backtrack==-1?null:new Integer(backtrack);
            }

            public String getTextForExport(int value) {
                if(value == -1)
                    return "-";
                else
                    return String.valueOf(value);
            }

            public String toString() {
                StringBuffer sb = new StringBuffer();
                sb.append(event.toString());
                sb.append("\t");
                sb.append(getTextForExport(subrule));
                sb.append("\t");
                sb.append(getTextForExport(decision));
                sb.append("\t");
                sb.append(getTextForExport(mark));
                sb.append("\t");
                sb.append(getTextForExport(backtrack));
                return sb.toString();
            }
        }
    }

    public class InfoTableCellRenderer extends DefaultTableCellRenderer {

        public InfoTableCellRenderer() {
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if(column == INFO_COLUMN_COUNT) {
                setHorizontalAlignment(JLabel.LEFT);
                setHorizontalTextPosition(SwingConstants.LEFT);
            } else {
                setHorizontalAlignment(JLabel.CENTER);
                setHorizontalTextPosition(SwingConstants.CENTER);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

}
