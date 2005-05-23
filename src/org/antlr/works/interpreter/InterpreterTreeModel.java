package org.antlr.works.interpreter;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.tree.Tree;
import org.antlr.tool.Grammar;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/*

[The "BSD licence"]
Copyright (c) 2004-05 Jean Bovet
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

public class InterpreterTreeModel extends DefaultTreeModel {

    public Tree tree = null;
    protected Grammar grammar;

    public InterpreterTreeModel() {
        super(null);
    }

    public void setTree(Tree tree) {
        this.tree = tree;
    }

    public void setGrammar(Grammar grammar) {
        this.grammar = grammar;
    }

    public Object getRoot() {
        if(tree == null)
            return null;
        else
            return new NodeWrapper(tree);
    }

    public Object getChild(Object o, int index) {
        NodeWrapper t = (NodeWrapper)o;
        return new NodeWrapper(t.getChild(index));
    }

    public int getChildCount(Object o) {
        NodeWrapper t = (NodeWrapper)o;
        return t.getChildCount();
    }

    public boolean isLeaf(Object o) {
        NodeWrapper t = (NodeWrapper)o;
        return t.isLeaf();
    }

    public void valueForPathChanged(TreePath treePath, Object o) {
    }

    public int getIndexOfChild(Object o, Object child) {
        NodeWrapper parent = (NodeWrapper)o;
        return parent.getIndexOfChild(child);
    }

    public void addTreeModelListener(TreeModelListener treeModelListener) {
    }

    public void removeTreeModelListener(TreeModelListener treeModelListener) {
    }

    public class NodeWrapper {
        protected Tree tree = null;

        public NodeWrapper(Tree tree) {
            this.tree = tree;
        }

        public int getChildCount() {
            return tree.getChildCount();
        }

        public boolean isLeaf() {
            return getChildCount() == 0;
        }

        public Tree getChild(int index) {
            return tree.getChild(index);
        }

        public int getIndexOfChild(Object child) {
            for(int i=0; i<tree.getChildCount(); i++) {
                if(tree.getChild(i) == child)
                    return i;
            }
            return -1;
        }

        public Object getPayload() {
            return tree.getPayload();
        }

        public String getInfoString() {
            StringBuffer info = new StringBuffer();
            Object payload = getPayload();
            if(payload instanceof CommonToken) {
                CommonToken t = (CommonToken)payload;
                info.append("Type: "+grammar.getTokenName(t.getType())+"\n");
                info.append("Text: "+t.getText()+"\n");
                info.append("Line: "+t.getLine()+"\n");
                info.append("Char: "+t.getCharPositionInLine()+"\n");
                info.append("Channel: "+t.getChannel()+"\n");
            } else if(payload instanceof NoViableAltException) {
                NoViableAltException e = (NoViableAltException)payload;
                info.append("Description: "+e.grammarDecisionDescription+"\n");
                info.append("Descision: "+e.decisionNumber+"\n");
                info.append("State: "+e.stateNumber+"\n");
            } else {
                if(isLeaf())
                    info.append(payload.toString());
                else
                    info.append("Rule: "+payload.toString());
            }
            return info.toString();
        }

        public String toString() {
            Object payload = getPayload();
            if(payload instanceof CommonToken) {
                CommonToken t = (CommonToken)payload;
                return t.getText();
            } else if(payload instanceof NoViableAltException)
                return "NoViableAltException";
            else if(payload == null)
                return "<null>";
            else
                return payload.toString();
        }
    }
}
