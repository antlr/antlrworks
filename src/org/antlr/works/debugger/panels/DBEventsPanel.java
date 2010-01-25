package org.antlr.works.debugger.panels;

import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.debugger.events.DBEvent;
import org.antlr.works.debugger.tivo.DBPlayerContextInfo;
import org.antlr.works.utils.DetachablePanel;
import org.antlr.xjlib.appkit.swing.XJTable;
import org.antlr.xjlib.appkit.swing.XJTableView;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
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

public class DBEventsPanel extends DetachablePanel {

    public static final int INFO_COLUMN_COUNT = 0;
    public static final int INFO_COLUMN_EVENT = 1;
    public static final int INFO_COLUMN_SUBRULE = 2;
    public static final int INFO_COLUMN_DECISION = 3;
    public static final int INFO_COLUMN_MARK = 4;
    public static final int INFO_COLUMN_BACKTRACK = 5;

    protected XJTableView infoTableView;

    protected EventTableDataModel eventTableDataModel;

    public DBEventsPanel(DebuggerTab debuggerTab) {
        super("Events", debuggerTab);

        eventTableDataModel = new EventTableDataModel();

        infoTableView = new XJTableView();
        infoTableView.setFocusable(true);
        setInfoTableModel(infoTableView.getTable(), eventTableDataModel);

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

    public int getNumberOfEvents() {
        return eventTableDataModel.getRowCount();
    }
    
    public void clear() {
        eventTableDataModel.clear();
    }

    public void updateOnBreakEvent() {
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

    public String getEventsAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(eventTableDataModel.getHeadersAsString());
        sb.append("\n");

        List<EventTableDataModel.EventInfo> events = eventTableDataModel.events;
        for(int i=0; i<events.size(); i++) {
            sb.append(i);
            sb.append(":\t");
            sb.append(events.get(i).toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public class EventTableDataModel extends AbstractTableModel {

        protected List<EventInfo> events = new ArrayList<EventInfo>();

        public void add(DBEvent event, DBPlayerContextInfo info) {
            events.add(new EventInfo(event, info));
        }

        public void clear() {
            events.clear();
            fireTableDataChanged();
            infoTableView.autoresizeColumns();
        }

        public void update() {
            fireTableDataChanged();
            infoTableView.autoresizeColumns();
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
                case INFO_COLUMN_SUBRULE: return "Subrule";
                case INFO_COLUMN_DECISION: return "Decision";
                case INFO_COLUMN_MARK: return "Mark";
                case INFO_COLUMN_BACKTRACK: return "Backtrack";
            }
            return super.getColumnName(column);
        }

        public Class getColumnClass(int columnIndex) {
            switch(columnIndex) {
                case INFO_COLUMN_COUNT: return String.class;
                case INFO_COLUMN_EVENT: return String.class;
                case INFO_COLUMN_SUBRULE: return String.class;
                case INFO_COLUMN_DECISION: return String.class;
                case INFO_COLUMN_MARK: return String.class;
                case INFO_COLUMN_BACKTRACK: return String.class;
            }
            return super.getColumnClass(columnIndex);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            EventInfo info = events.get(rowIndex);
            switch(columnIndex) {
                case INFO_COLUMN_COUNT: return String.valueOf(rowIndex);
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
                return subrule==-1?null:String.valueOf(subrule);
            }

            public Object getDecision() {
                return decision==-1?null:String.valueOf(decision);
            }

            public Object getMark() {
                return mark==-1?null:String.valueOf(mark);
            }

            public Object getBacktrack() {
                return backtrack==-1?null:String.valueOf(backtrack);
            }

            public String getTextForExport(int value) {
                if(value == -1)
                    return "-";
                else
                    return String.valueOf(value);
            }

            public String toString() {
                StringBuilder sb = new StringBuilder();
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

}
