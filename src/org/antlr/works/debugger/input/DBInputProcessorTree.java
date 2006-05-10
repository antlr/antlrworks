package org.antlr.works.debugger.input;

import org.antlr.runtime.Token;
import org.antlr.tool.Grammar;
import org.antlr.works.awtree.AWTreePanel;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.debugger.tree.DBTreeNode;
import org.antlr.works.debugger.tree.DBTreeToken;
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

public class DBInputProcessorTree implements DBInputProcessor {

    public AWTreePanel treePanel;
    public Debugger debugger;

    public InputTreeNode rootNode;
    public InputTreeNode currentNode;

    public int line, pos;

    public DBInputProcessorTree(AWTreePanel treePanel, Debugger debugger) {
        this.treePanel = treePanel;
        this.debugger = debugger;
    }

    public void updateTreePanel() {
        treePanel.setRoot(rootNode);
        treePanel.refresh();
    }

    public void reset() {
        rootNode = createNode(null);
        currentNode = rootNode;
    }

    public void removeAllLT() {
    }

    public void rewind(int i) {
    }

    public void rewindAll() {
        reset();
    }

    public void LT(Token token) {
    }

    public void consumeToken(Token token, int type) {
        switch(token.getType()) {
            case Token.DOWN:
                currentNode = (InputTreeNode)currentNode.getLastChild();
                break;

            case Token.UP:
                currentNode = (InputTreeNode)currentNode.getParent();
                break;

            default:
                currentNode.add(createNode(token));
                break;
        }
    }

    public InputTreeNode createNode(Token token) {
        InputTreeNode node = new InputTreeNode((DBTreeToken)token, debugger.getGrammar().getANTLRGrammar());
        node.setPosition(line, pos);
        return node;
    }

    public void setLocation(int line, int pos) {
        this.line = line;
        this.pos = pos;
    }

    public int getCurrentTokenIndex() {
        return 0;
    }

    public DBInputTextTokenInfo getTokenInfoAtTokenIndex(int index) {
        return null;
    }

    public DBInputTextTokenInfo getTokenInfoForToken(Token token) {
        //return rootNode.findNodeWithToken(token);
        return null;
    }

    public class InputTreeNode extends DBTreeNode {

        public InputTreeNode(DBTreeToken token, Grammar grammar) {
            super(token, grammar);
        }

        public String toString() {
            if(token != null)
                return token.getText()+" <"+grammar.getTokenDisplayName(token.getType())+">";
            else
                return "nil";
        }

    }
}
