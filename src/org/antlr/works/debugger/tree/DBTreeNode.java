package org.antlr.works.debugger.tree;

import org.antlr.runtime.Token;
import org.antlr.tool.Grammar;
import org.antlr.works.awtree.AWTreeNode;

import java.awt.*;
import java.util.Enumeration;
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

public class DBTreeNode extends AWTreeNode {

    protected Token token;
    protected Grammar grammar;

    protected int line;
    protected int pos;

    protected Color color = Color.black;

    public DBTreeNode() {

    }
    
    public DBTreeNode(Token token, Grammar grammar) {
        this.token = token;
        this.grammar = grammar;
    }

    public void setPosition(int line, int pos) {
        this.line = line;
        this.pos = pos;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public DBTreeNode findNodeWithToken(Token t) {
        if(token != null && t.getTokenIndex() == token.getTokenIndex())
            return this;

        for(Enumeration childrenEnumerator = children(); childrenEnumerator.hasMoreElements(); ) {
            DBTreeNode node = (DBTreeNode) childrenEnumerator.nextElement();
            DBTreeNode candidate = node.findNodeWithToken(t);
            if(candidate != null)
                return candidate;
        }
        return null;
    }

    public String toString() {
        if(token != null)
            return token.getText()+" <"+grammar.getTokenDisplayName(token.getType())+">";
        else
            return "?";
    }

    public String getInfoString() {
        return toString();
    }

}
