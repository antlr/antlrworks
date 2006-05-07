package org.antlr.works.debugger.tree;

import org.antlr.runtime.Token;

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

public class DBASTModel {

    /** Stack of rule. Each rule contains a stack of roots */
    public Stack rules = new Stack();

    /** Map of nodes */
    public Map nodesMap = new HashMap();

    public List listeners = new ArrayList();

    public void addListener(DBASTModelListener listener) {
        listeners.add(listener);
    }

    public void fireDataChanged() {
        for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            DBASTModelListener listener = (DBASTModelListener) iterator.next();
            listener.modelChanged(this);
        }
    }

    public void clear() {
        rules.clear();
        nodesMap.clear();
        fireDataChanged();
    }

    /* Methods used to query the model */

    public int getRuleCount() {
        return rules.size();
    }

    public Rule getRuleAtIndex(int index) {
        if(index < 0 || index >= rules.size())
            return null;
        else
            return (Rule) rules.get(index);
    }

    public int getRootCount() {
        return getRoots().size();
    }

    /* Methods used by the debugger */

    public void pushRule(String name) {
        rules.push(new Rule(name, new Stack()));
    }

    public void popRule() {
        rules.pop();
    }

    public void pushRoot(ASTNode node) {
        getRoots().push(node);
    }

    /** Replace a root node by another one */
    public void replaceRoot(ASTNode oldRoot, ASTNode newRoot) {
        Stack roots = getRoots();
        int index = roots.indexOf(oldRoot);
        roots.remove(index);
        roots.add(index, newRoot);
    }

    /** Remove a root node */
    public void removeRoot(ASTNode node) {
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
        ASTNode newRoot = getTreeNode(newRootID);
        ASTNode oldRoot = getTreeNode(oldRootID);
        oldRoot.becomeParent(newRoot);
        replaceRoot(oldRoot, newRoot);
    }

    public void addChild(int rootID, int childID) {
        ASTNode root = getTreeNode(rootID);
        ASTNode child = getTreeNode(childID);
        if(root == null) {
            System.err.println("Root node "+rootID+" not found!");
            return;
        }
        if(child == null) {
            System.err.println("Child node "+childID+" not found!");
            return;
        }

        removeRoot(child);
        root.addChild(child);
    }

    /* Utility methods */

    protected ASTNode createNilTreeNode(int id) {
        ASTNode node = createTreeNode(id);
        node.nil = true;
        return node;
    }

    protected ASTNode createTreeNode(int id, Token token) {
        ASTNode node = createTreeNode(id);
        node.token = token;
        return node;
    }

    protected ASTNode createTreeNode(int id) {
        ASTNode node = new ASTNode(id);
        nodesMap.put(new Integer(id), node);
        return node;
    }

    protected ASTNode getTreeNode(int id) {
        return (ASTNode) nodesMap.get(new Integer(id));
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

        public ASTNode getRootAtIndex(int index) {
            return (ASTNode)roots.get(index);
        }

        public Stack getRoots() {
            return roots;
        }
    }

    public class ASTNode extends DBTreeNode {

        public int id;
        public boolean nil = false;

        public ASTNode parent = null;

        public ASTNode(int id) {
            this.id = id;
            /** Children is defined in DefaultMutableTreeNode */
            children = new Vector();
        }

        /** Add a child */
        public void addChild(ASTNode node) {
            if(node.nil) {
                /** If the child node is a nil node, add its children only */
                for (int i = 0; i < node.children.size(); i++) {
                    ASTNode child = (ASTNode) node.children.get(i);
                    child.parent = this;
                    children.add(child);
                }
            } else {
                node.parent = this;
                children.add(node);
            }
        }

        /** Remove a child */
        public void removeChild(ASTNode node) {
            children.remove(node);
        }

        /** Replace the current parent node with another one */
        public void becomeParent(ASTNode node) {
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
        public void replaceChild(ASTNode oldNode, ASTNode newNode) {
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

        public String toString() {
            if(nil)
                return "nil";
            else if(token == null)
                return String.valueOf(id);
            else
                return token.getText();
        }
    }
}
