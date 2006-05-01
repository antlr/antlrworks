package org.antlr.works.debugger.ast;

import org.antlr.runtime.Token;

import javax.swing.tree.TreeNode;
import java.util.*;
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

public class DebuggerASTModel {

    /** Stack of rule. Each rule contains a stack of roots */
    public Stack rules = new Stack();

    /** Map of nodes */
    public Map nodesMap = new HashMap();


    public List listeners = new ArrayList();

    public void addListener(DebuggerASTModelListener listener) {
        listeners.add(listener);
    }

    public void fireDataChanged() {
        for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            DebuggerASTModelListener listener = (DebuggerASTModelListener) iterator.next();
            listener.modelChanged(this);
        }
    }

    /* Methods used to query the model */

    public int getRuleCount() {
        return rules.size();
    }

    public Rule getRuleAtIndex(int index) {
        return (Rule) rules.get(index);
    }

    public int getRootCount() {
        Stack roots = getRoots();
        if(roots == null)
            return 0;
        else
            return roots.size();
    }

    public ASTTreeNode getRootAtIndex(int index) {
        return (ASTTreeNode) getRoots().get(index);
    }

    /* Methods used by the debugger */

    public void pushRule(String name) {
        rules.push(new Rule(name, new Stack()));
    }

    public void popRule() {
        rules.pop();
    }

    public void pushRoot(ASTTreeNode node) {
        getRoots().push(node);
    }

    public void popRoot() {
        getRoots().pop();
    }

    /** Replace a root node by another one */
    public void replaceRoot(ASTTreeNode oldRoot, ASTTreeNode newRoot) {
        Stack roots = getRoots();
        int index = roots.indexOf(oldRoot);
        roots.remove(index);
        roots.add(index, newRoot);
    }

    /** Remove a root node */
    public void removeRoot(ASTTreeNode node) {
        getRoots().remove(node);
    }

    /* Methods used by the protocol */

    public void nilNode(int id) {
        pushRoot(createNilTreeNode(id));
    }

    public void createNode(int id, Token token) {
        createTreeNode(id, token);
    }

    public void becomeRoot(int newRootID, int oldRootID) {
        ASTTreeNode newRoot = getTreeNode(newRootID);
        ASTTreeNode oldRoot = getTreeNode(oldRootID);
        oldRoot.becomeParent(newRoot);
        replaceRoot(oldRoot, newRoot);
    }

    public void addChild(int rootID, int childID) {
        ASTTreeNode root = getTreeNode(rootID);
        ASTTreeNode child = getTreeNode(childID);
        removeRoot(child);
        root.addChild(child);
    }

    /* Utility methods */

    protected ASTTreeNode createNilTreeNode(int id) {
        ASTTreeNode node = createTreeNode(id);
        node.nil = true;
        return node;
    }

    protected ASTTreeNode createTreeNode(int id, Token token) {
        ASTTreeNode node = createTreeNode(id);
        node.token = token;
        return node;
    }

    protected ASTTreeNode createTreeNode(int id) {
        ASTTreeNode node = new ASTTreeNode(id);

        nodesMap.put(new Integer(id), node);
        return node;
    }

    protected ASTTreeNode getTreeNode(int id) {
        return (ASTTreeNode) nodesMap.get(new Integer(id));
    }

    protected Stack getRoots() {
        if(rules.isEmpty())
            return null;
        else
            return ((Rule)rules.peek()).roots;
    }

    public class Rule {

        public String name;
        public Stack roots;

        public Rule(String name, Stack roots) {
            this.name = name;
            this.roots = roots;
        }

        public ASTTreeNode getRootAtIndex(int index) {
            return (ASTTreeNode)roots.get(index);
        }
    }

    public class ASTTreeNode implements TreeNode {

        public int id;
        public Token token = null;
        public boolean nil = false;

        public ASTTreeNode parent = null;
        public List children = new ArrayList();

        public ASTTreeNode(int id) {
            this.id = id;
        }

        /** Add a child */
        public void addChild(ASTTreeNode node) {
            if(node.nil) {
                /** If the child node is a nil node, add its children only */
                for (int i = 0; i < node.children.size(); i++) {
                    ASTTreeNode child = (ASTTreeNode) node.children.get(i);
                    child.parent = this;
                    children.add(child);
                }
            } else {
                node.parent = this;
                children.add(node);
            }
        }

        /** Remove a child */
        public void removeChild(ASTTreeNode node) {
            children.remove(node);
        }

        /** Replace the current parent node with another one */
        public void becomeParent(ASTTreeNode node) {
            node.detach();
            if(parent != null)
                parent.replaceChild(this, node);

            /** Do not add a nil node to the new parent: the nil
             * node is removed from the tree
             */
            if(!nil)
                node.addChild(this);
        }

        /** Replace a child with another one */
        public void replaceChild(ASTTreeNode oldNode, ASTTreeNode newNode) {
            int index = children.indexOf(oldNode);
            children.remove(index);
            if(newNode.nil) {
                /** If the new node is a nil node, add its children only */
                children.addAll(index, newNode.children);
            } else
                children.add(index, newNode);
        }

        /** Detach this node from its parent */
        public void detach() {
            if(parent != null) {
                parent.removeChild(this);
                parent = null;
            }
        }

        /* TreeNode interface */

        public TreeNode getChildAt(int childIndex) {
            return (TreeNode)children.get(childIndex);
        }

        public int getChildCount() {
            return children.size();
        }

        public TreeNode getParent() {
            return parent;
        }

        public int getIndex(TreeNode node) {
            return children.indexOf(node);
        }

        public boolean getAllowsChildren() {
            return true;
        }

        public boolean isLeaf() {
            return children.isEmpty();
        }

        public Enumeration children() {
            return Collections.enumeration(children);
        }

        public String toString() {
            if(token == null)
                return String.valueOf(id);
            else
                return token.getText();
        }
    }
}
