package org.antlr.works.debugger.tree;

import edu.usfca.xj.appkit.gview.GView;
import edu.usfca.xj.appkit.swing.XJTable;
import edu.usfca.xj.appkit.swing.XJTableDelegate;
import org.antlr.runtime.Token;
import org.antlr.works.awtree.AWTreePanel;
import org.antlr.works.awtree.AWTreePanelDelegate;
import org.antlr.works.debugger.Debugger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
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

public class DBASTPanel extends JPanel implements DBASTModelListener, XJTableDelegate, AWTreePanelDelegate {

    public Debugger debugger;

    public XJTable rulesTable;
    public XJTable rootsTable;
    public AWTreePanel treePanel;

    public DBASTModel model;
    public JSplitPane tablesSplitPane;
    public JSplitPane tableTreeSplitPane;

    public DBASTPanel(Debugger debugger) {
        super(new BorderLayout());

        this.debugger = debugger;

        rulesTable = new XJTable(new RulesTableModel());
        rulesTable.setFocusable(false);
        rulesTable.setDelegate(this);
        rulesTable.setAllowEmptySelection(false);
        rulesTable.setRememberSelection(true);

        rootsTable = new XJTable(new RootsTableModel());
        rootsTable.setFocusable(false);
        rootsTable.setDelegate(this);
        rootsTable.setAllowEmptySelection(false);
        rootsTable.setRememberSelection(true);

        treePanel = new AWTreePanel(new DefaultTreeModel(null));
        treePanel.setRootVisible(true);
        treePanel.setDelegate(this);

        tablesSplitPane = createSplitPane();
        tableTreeSplitPane = createSplitPane();

        JScrollPane rulesScrollPane = new JScrollPane(rulesTable);
        rulesScrollPane.setWheelScrollingEnabled(true);
        tablesSplitPane.setLeftComponent(rulesScrollPane);

        JScrollPane rootsScrollPane = new JScrollPane(rootsTable);
        rootsScrollPane.setWheelScrollingEnabled(true);
        tablesSplitPane.setRightComponent(rootsScrollPane);

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
        tableTreeSplitPane.setDividerLocation(0.3);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tablesSplitPane.setDividerLocation(0.7);
            }
        });        
    }

    public void clear() {
        model.clear();
    }

    public void updateOnBreakEvent() {
        model.fireDataChanged();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                selectLastRule();
                selectLastRootNode();
            }
        });
    }

    public void setModel(DBASTModel model) {
        this.model = model;
        this.model.addListener(this);
    }

    public void modelChanged(DBASTModel model) {
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
            treePanel.setRoot(getRootAtIndex(row));
        treePanel.refresh();
    }

    public void selectToken(Token token) {
        /** Look currently only on the selected rule roots */
        DBASTModel.Rule rule = getSelectedRule();
        if(rule == null)
            return;

        Stack roots = rule.getRoots();
        for (int r = 0; r < roots.size(); r++) {
            DBASTModel.ASTNode node = (DBASTModel.ASTNode) roots.get(r);
            DBTreeNode candidate = node.findNodeWithToken(token);
            if(candidate != null) {
                rootsTable.setSelectedRow(r);
                treePanel.selectNode(candidate);
                break;
            }
        }
    }

    public void selectLastRule() {
        rulesTable.selectLastRow();
    }

    public void selectLastRootNode() {
        rootsTable.selectLastRow();
    }

    public DBASTModel.Rule getSelectedRule() {
        int row = rulesTable.getSelectedRow();
        if(row == -1)
            return null;
        else
            return model.getRuleAtIndex(row);
    }

    public DBASTModel.ASTNode getRootAtIndex(int index) {
        return getSelectedRule().getRootAtIndex(index);
    }

    public GView getGraphView() {
        return treePanel.getGraphView();
    }

    public void tableSelectionChanged(XJTable table, int selectedRow) {
        if(table == rulesTable) {
            rootsModelChanged();
        } else if(table == rootsTable) {
            updateTreePanel();
        }
    }

    public void awTreeDidSelectTreeNode(TreeNode node) {
        DBASTModel.ASTNode n = (DBASTModel.ASTNode)node;
        debugger.selectToken(n.token, n.token.getLine(), n.token.getCharPositionInLine());
    }

    public JPopupMenu awTreeGetContextualMenu() {
        return debugger.treeGetContextualMenu();
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
            DBASTModel.Rule r = getSelectedRule();
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
            return "r"+rowIndex;
            //return getRootAtIndex(rowIndex);
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

}
