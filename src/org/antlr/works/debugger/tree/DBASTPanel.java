package org.antlr.works.debugger.tree;

import org.antlr.runtime.Token;
import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.utils.DetachablePanel;
import org.antlr.works.utils.awtree.AWTreePanel;
import org.antlr.works.utils.awtree.AWTreePanelDelegate;
import org.antlr.xjlib.appkit.gview.GView;
import org.antlr.xjlib.appkit.swing.XJTable;
import org.antlr.xjlib.appkit.swing.XJTableDelegate;
import org.antlr.xjlib.appkit.swing.XJTableView;

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

public class DBASTPanel extends DetachablePanel implements DBASTModelListener, XJTableDelegate, AWTreePanelDelegate {

    public DebuggerTab debuggerTab;

    public XJTableView rulesTableView;
    public XJTableView rootsTableView;
    public AWTreePanel treePanel;

    public DBASTModel model;
    public JSplitPane tablesSplitPane;
    public JSplitPane tableTreeSplitPane;

    public DBASTPanel(DebuggerTab debuggerTab) {
        super("AST", debuggerTab);

        this.debuggerTab = debuggerTab;

        model = new DBASTModel(debuggerTab);
        model.addListener(this);

        rulesTableView = new XJTableView();
        rulesTableView.setFocusable(true);
        rulesTableView.getTable().setModel(new RulesTableModel());
        rulesTableView.getTable().setDelegate(this);
        rulesTableView.getTable().setAllowEmptySelection(false);
        rulesTableView.getTable().setRememberSelection(true);
        rulesTableView.autoresizeColumns();

        rootsTableView = new XJTableView();
        rootsTableView.getTable().setModel(new RootsTableModel());
        rootsTableView.setFocusable(true);
        rootsTableView.getTable().setDelegate(this);
        rootsTableView.getTable().setAllowEmptySelection(false);
        rootsTableView.getTable().setRememberSelection(true);
        rootsTableView.autoresizeColumns();

        treePanel = new AWTreePanel(new DefaultTreeModel(null));
        treePanel.setRootVisible(true);
        treePanel.setDelegate(this);

        tablesSplitPane = new JSplitPane();
        tablesSplitPane.setBorder(null);
        tablesSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        tablesSplitPane.setContinuousLayout(true);
        tablesSplitPane.setOneTouchExpandable(true);
        tablesSplitPane.setResizeWeight(0.5);

        //rulesTableView.setMinimumSize(new Dimension(150, 0));
        //rootsTableView.setMinimumSize(new Dimension(150, 0));

        tablesSplitPane.setLeftComponent(rulesTableView);
        tablesSplitPane.setRightComponent(rootsTableView);

        tableTreeSplitPane = new JSplitPane();
        tableTreeSplitPane.setBorder(null);
        tableTreeSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        tableTreeSplitPane.setContinuousLayout(true);
        tableTreeSplitPane.setOneTouchExpandable(true);
        tableTreeSplitPane.setResizeWeight(0.5);
        
        tableTreeSplitPane.setLeftComponent(tablesSplitPane);
        tableTreeSplitPane.setRightComponent(treePanel);

        mainPanel.add(tableTreeSplitPane, BorderLayout.CENTER);
    }

    @Override
    public void close() {
        super.close();
        treePanel.setDelegate(null);
        debuggerTab = null;
    }

    public void clear() {
        model.clear();
        treePanel.clear();
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

    public DBASTModel getModel() {
        return model;
    }
    
    public void modelChanged(DBASTModel model) {
        rulesModelChanged();
        rootsModelChanged();
    }

    public void rulesModelChanged() {
        rulesTableView.getTable().reload();
    }

    public void rootsModelChanged() {
        rootsTableView.getTable().reload();
        updateTreePanel();
    }

    public void updateTreePanel() {
        int row = rootsTableView.getTable().getSelectedRow();
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

        Stack<DBASTModel.ASTNode> roots = rule.getRoots();
        for (int r = 0; r < roots.size(); r++) {
            DBASTModel.ASTNode node = roots.get(r);
            DBTreeNode candidate = node.findNodeWithToken(token);
            if(candidate != null) {
                rootsTableView.getTable().setSelectedRow(r);
                treePanel.selectNode(candidate);
                break;
            }
        }
    }

    public void selectLastRule() {
        rulesTableView.getTable().selectLastRow();
    }

    public void selectLastRootNode() {
        rootsTableView.getTable().selectLastRow();
    }

    public DBASTModel.Rule getSelectedRule() {
        int row = rulesTableView.getTable().getSelectedRow();
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
        if(table == rulesTableView.getTable()) {
            rootsModelChanged();
        } else if(table == rootsTableView.getTable()) {
            updateTreePanel();
        }
    }

    public void awTreeDidSelectTreeNode(TreeNode node, boolean shiftKey) {
        DBASTModel.ASTNode n = (DBASTModel.ASTNode)node;
        debuggerTab.selectToken(n.token, n.getLocation());
    }

    public JPopupMenu awTreeGetContextualMenu() {
        return debuggerTab.treeGetContextualMenu();
    }

    public class RulesTableModel extends DefaultTableModel {

        public int getRowCount() {
        	if(DBASTPanel.this == null)
        		return 0;
        	
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
        	if(DBASTPanel.this == null)
        		return 0;

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
