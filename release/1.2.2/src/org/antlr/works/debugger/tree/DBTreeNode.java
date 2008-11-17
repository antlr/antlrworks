package org.antlr.works.debugger.tree;

import org.antlr.runtime.Token;
import org.antlr.works.debugger.events.DBEventLocation;
import org.antlr.works.utils.awtree.AWTreeNode;

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

    protected DBEventLocation location;

    protected Color color = Color.black;

    public DBTreeNode() {

    }
    
    public DBTreeNode(Token token, DBEventLocation location) {
        this.token = token;
        this.location = location;
    }

    public DBEventLocation getLocation() {
        return location;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public Token getToken() {
        return token;
    }

    public DBTreeNode findNodeWithToken(Token t) {
        return findNodeWithToken(t, null);
    }

    /** Find the last node corresponding to the given token. Because multiple node
     * can contains the same token (backtracking nodes for example), we always want
     * to select the most recent one in the tree (the selection will come from the
     * input panel)
     */

    public DBTreeNode findNodeWithToken(Token t, DBTreeNode lastNodeSoFar) {
        if(t == null)
            return lastNodeSoFar;

        if(token != null) {
            /** Little hack here. If the token is of type DBTreeToken (tree grammar), then
             * the token index cannot be used (we don't have this information). We will use
             * the ID unique to each tree node instead.
             */
            if(token instanceof DBTreeToken && t instanceof DBTreeToken) {
                DBTreeToken t1 = (DBTreeToken)token;
                DBTreeToken t2 = (DBTreeToken)t;
                if(t1.ID == t2.ID)
                    lastNodeSoFar = this;
            } else if(t.getTokenIndex() == token.getTokenIndex() &&
                    t.getType() == token.getType())
            {
                // FIX AW-61 - compare also the token type to avoid selecting the wrong one (e.g. imaginary)
                lastNodeSoFar = this;
            }
        }

        for(Enumeration childrenEnumerator = children(); childrenEnumerator.hasMoreElements(); ) {
            DBTreeNode node = (DBTreeNode) childrenEnumerator.nextElement();
            DBTreeNode candidate = node.findNodeWithToken(t, lastNodeSoFar);
            if(candidate != null)
                lastNodeSoFar = candidate;
        }

        return lastNodeSoFar;
    }

    public String toString() {
        if(token != null) {
            return getTokenDisplayString(token);
        } else {
            return "?";            
        }
    }

    protected String getTokenDisplayString(Token token) {
        if(token.getType() == -1) {
            return "EOF";
        } else {
            String t = token.getText();
            if(t.equals("\n")) {
                return "\\n";
            }
            if(t.equals("\r")) {
                return "\\r";
            }
            if(t.equals("\t")) {
                return "\\t";
            }
            return t;
        }
    }

    public String getInfoString() {
        return toString();
    }

}
