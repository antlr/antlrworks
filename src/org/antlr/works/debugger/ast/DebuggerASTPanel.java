package org.antlr.works.debugger.ast;

import edu.usfca.xj.appkit.swing.XJTable;
import edu.usfca.xj.appkit.swing.XJTableDelegate;
import org.antlr.works.parsetree.ParseTreePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
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

public class DebuggerASTPanel extends JPanel implements DebuggerASTModelListener, XJTableDelegate {

    public XJTable rulesTable;
    public XJTable rootsTable;
    public ParseTreePanel treePanel;

    public DebuggerASTModel model;
    public RulesTableModel rulesModel;
    public RootsTableModel rootsModel;
    public JSplitPane tablesSplitPane;
    public JSplitPane tableTreeSplitPane;

    public DebuggerASTPanel() {
        super(new BorderLayout());

        rulesTable = new XJTable(rulesModel = new RulesTableModel());
        rulesTable.setFocusable(false);
        rulesTable.setDelegate(this);
        rulesTable.setAllowEmptySelection(false);
        rulesTable.setRememberSelection(true);

        rootsTable = new XJTable(rootsModel = new RootsTableModel());
        rootsTable.setFocusable(false);
        rootsTable.setDelegate(this);
        rootsTable.setAllowEmptySelection(false);
        rootsTable.setRememberSelection(true);

        treePanel = new ParseTreePanel(new DefaultTreeModel(null));

        tablesSplitPane = createSplitPane();
        tableTreeSplitPane = createSplitPane();

        tablesSplitPane.setLeftComponent(new JScrollPane(rulesTable));
        tablesSplitPane.setRightComponent(new JScrollPane(rootsTable));

        tableTreeSplitPane.setLeftComponent(tablesSplitPane);
        tableTreeSplitPane.setRightComponent(treePanel);

        add(tableTreeSplitPane, BorderLayout.CENTER);
    }

    public JSplitPane createSplitPane() {
        JSplitPane sp = new JSplitPane();
        sp.setBorder(null);
        sp.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        sp.setContinuousLayout(true);
        sp.setOneTouchExpandable(true);
        return sp;
    }

    public void componentShouldLayout() {
        tableTreeSplitPane.setDividerLocation(0.5);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tablesSplitPane.setDividerLocation(0.5);
            }
        });        
    }

    public void setModel(DebuggerASTModel model) {
        this.model = model;
        this.model.addListener(this);
    }

    public void modelChanged(DebuggerASTModel model) {
        rulesModelChanged();
        rootsModelChanged();
    }

    public void rulesModelChanged() {
        rulesTable.reload();
    }

    public void rootsModelChanged() {
        rootsTable.reload();
        updateTreePanel();
    }

    public void updateTreePanel() {
        int row = rootsTable.getSelectedRow();
        if(row == -1)
            treePanel.setRoot(null);
        else
            treePanel.setRoot(getSelectedRootAtIndex(row));
        treePanel.refresh();
    }

    public DebuggerASTModel.Rule getSelectedRule() {
        int row = rulesTable.getSelectedRow();
        if(row == -1)
            return null;
        else
            return model.getRuleAtIndex(row);
    }

    public DebuggerASTModel.ASTTreeNode getSelectedRootAtIndex(int index) {
        return getSelectedRule().getRootAtIndex(index);
    }

    public void tableSelectionChanged(XJTable table, int selectedRow) {
        if(table == rulesTable) {
            rootsModelChanged();
        } else if(table == rootsTable) {
            updateTreePanel();
        }
    }

    public class RulesTableModel extends DefaultTableModel {

        public int getRowCount() {
            if(model == null)
                return 0;
            else
                return model.getRuleCount();
        }

        public int getColumnCount() {
            return 1;
        }

        public String getColumnName(int column) {
            return "Rule";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return model.getRuleAtIndex(rowIndex).name;
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    public class RootsTableModel extends DefaultTableModel {

        public int getRowCount() {
            DebuggerASTModel.Rule r = getSelectedRule();
            if(r == null)
                return 0;
            else
                return r.roots.size();
        }

        public int getColumnCount() {
            return 1;
        }

        public String getColumnName(int column) {
            return "Root";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return getSelectedRootAtIndex(rowIndex);
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

}
