/*

[The "BSD licence"]
Copyright (c) 2005 Jean Bovet
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

package org.antlr.works.interpreter;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.tree.ParseTree;
import org.antlr.runtime.tree.Tree;
import org.antlr.tool.Grammar;
import org.antlr.works.utils.awtree.AWTreeNode;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.Enumeration;

public class EditorInterpreterTreeModel extends DefaultTreeModel {

    protected Grammar grammar;

    public EditorInterpreterTreeModel() {
        super(null);
    }

    public void setGrammar(Grammar grammar) {
        this.grammar = grammar;
    }

    public void setTree(Tree tree) {
        setRoot(new InterpreterTreeNode(null, tree));
    }

    public class InterpreterTreeNode extends AWTreeNode {

        protected Tree tree;

        public InterpreterTreeNode(TreeNode parent, Tree tree) {
            this.parent = (MutableTreeNode) parent;
            this.tree = tree;
        }

        public TreeNode getChildAt(int childIndex) {
            return new InterpreterTreeNode(this, tree.getChild(childIndex));
        }

        public int getChildCount() {
            return tree.getChildCount();
        }

        public TreeNode getParent() {
            return parent;
        }

        public int getIndex(TreeNode node) {
            for(int i=0; i<tree.getChildCount(); i++) {
                if(tree.getChild(i) == node)
                    return i;
            }
            return -1;
        }

        public boolean getAllowsChildren() {
            return true;
        }

        public boolean isLeaf() {
            return getChildCount() == 0;
        }

        public Enumeration children() {
            return null;
        }

        public Object getPayload() {
            if(tree instanceof ParseTree)
                return ((ParseTree)tree).payload;
            else
                return null;
        }

        public Color getColor() {
            return Color.black;
        }

        public String getInfoString() {
            StringBuilder info = new StringBuilder();
            Object payload = getPayload();
            if(payload instanceof CommonToken) {
                CommonToken t = (CommonToken)payload;
                info.append("Type: ").append(grammar.getTokenDisplayName(t.getType())).append("\n");
                info.append("Text: ").append(t.getText()).append("\n");
                info.append("Line: ").append(t.getLine()).append("\n");
                info.append("Char: ").append(t.getCharPositionInLine()).append("\n");
                info.append("Channel: ").append(t.getChannel()).append("\n");
            } else if(payload instanceof NoViableAltException) {
                NoViableAltException e = (NoViableAltException)payload;
                info.append("Description: ").append(e.grammarDecisionDescription).append("\n");
                info.append("Descision: ").append(e.decisionNumber).append("\n");
                info.append("State: ").append(e.stateNumber).append("\n");
            } else {
                if(isLeaf())
                    info.append(payload.toString());
                else
                    info.append("Rule: ").append(payload.toString());
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
