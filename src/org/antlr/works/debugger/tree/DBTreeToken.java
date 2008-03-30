package org.antlr.works.debugger.tree;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.debug.RemoteDebugEventSocketListener.ProxyTree;
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

public class DBTreeToken implements Token {
	public ProxyTree tree;
	public int ID;

	public DBTreeToken(ProxyTree tree) {
		this.tree = tree;
		this.ID = tree.ID;
	}

    public String getText() {
        return tree.text;
    }

    public void setText(String text) {
        tree.text = text;
    }

    public int getType() {
        return tree.type;
    }

    public void setType(int ttype) {
        tree.type = ttype;
    }

    public int getLine() {
        return tree.line;
    }

    public void setLine(int line) {
		tree.line = line;
	}

    public int getCharPositionInLine() {
        return tree.charPos;
    }

    public void setCharPositionInLine(int pos) {
		tree.charPos = pos;
	}

    public int getChannel() {
        return 0;
    }

    public void setChannel(int channel) {
    }

    public int getTokenIndex() {
        return tree.tokenIndex;
    }

    public void setTokenIndex(int index) {
		tree.tokenIndex = index;
	}

    public CharStream getInputStream() {
        return null;
    }

    public void setInputStream(CharStream input) {
    }

    public String toString() {
		String tokenIndexS = tree.tokenIndex>=0?(",@"+tree.tokenIndex):"";
		String pos = tree.line>0?(","+tree.line+":"+tree.charPos):"";
		return "["+tree.text+"/, <"+tree.type+">, "+tree.ID+pos+tokenIndexS+"]";
    }
}
