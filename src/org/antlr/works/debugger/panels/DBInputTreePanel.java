package org.antlr.works.debugger.panels;

import org.antlr.runtime.Token;
import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.debugger.input.DBInputProcessor;
import org.antlr.works.debugger.input.DBInputProcessorTree;
import org.antlr.works.debugger.input.DBInputTextTokenInfo;
import org.antlr.works.utils.awtree.AWTreePanel;
import org.antlr.works.utils.awtree.AWTreePanelDelegate;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
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

public class DBInputTreePanel implements DBInputConcretePanel, AWTreePanelDelegate {

    public DebuggerTab debuggerTab;

    public AWTreePanel treePanel;
    public DBInputProcessorTree processorTree;

    public DBInputTreePanel(DebuggerTab debuggerTab) {
        this.debuggerTab = debuggerTab;

        treePanel = new AWTreePanel(new DefaultTreeModel(null));
        treePanel.setRootVisible(true);
        treePanel.setDelegate(this);

        processorTree = new DBInputProcessorTree(treePanel, debuggerTab);
    }

    public void close() {
        treePanel.setDelegate(null);
        debuggerTab = null;
        processorTree.close();
    }

    public void stop() {
    }

    public JComponent getComponent() {
        return treePanel;
    }

    public DBInputProcessor getInputProcessor() {
        return processorTree;
    }

    public void toggleInputTextTokensBox() {
        /** Not applicable here. Ignore */
    }

    public boolean isInputTokensBoxVisible() {
        return false;
    }

    public boolean isBreakpointAtToken(Token token) {
        return processorTree.isBreakpointAtToken(token);
    }

    public void selectToken(Token token) {
        /** Not applicable here. Ignore */
    }

    public DBInputTextTokenInfo getTokenInfoForToken(Token token) {
        return processorTree.getTokenInfoForToken(token);
    }

    public void updateOnBreakEvent() {
        processorTree.updateTreePanel();
    }

    public void awTreeDidSelectTreeNode(TreeNode node, boolean shiftKey) {
        DBInputProcessorTree.InputTreeNode n = (DBInputProcessorTree.InputTreeNode) node;
        if(shiftKey)
            n.toggleBreakpoint();

        debuggerTab.selectToken(n.getToken(), n.getLocation());
    }

    public JPopupMenu awTreeGetContextualMenu() {
        return debuggerTab.treeGetContextualMenu();
    }

}
