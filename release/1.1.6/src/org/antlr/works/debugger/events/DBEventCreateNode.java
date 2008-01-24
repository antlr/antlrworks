package org.antlr.works.debugger.events;
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

public class DBEventCreateNode extends DBEvent {

    public int id;

    /** A node with an index to a token in the input stream */
    public int tokenIndex = -1;

    /** A node can also be created with a text and token type.
     * This is most of the time for imaginary tokens that don't
     * have a "real" token in the input stream: so tokenIndex is not provided
     */
    public String text = null;

    public int type;
    
    public DBEventCreateNode(int id, int tokenIndex) {
        super(CREATE_NODE);
        this.id = id;
        this.tokenIndex = tokenIndex;
    }

    public DBEventCreateNode(int id, String text, int type) {
        super(CREATE_NODE);
        this.id = id;
        this.text = text;
        this.type = type;
    }

    public String toString() {
        if(tokenIndex != -1)
            return "Create node "+id+" ("+tokenIndex+")";
        else
            return "Create node "+id+" ("+text+"/"+type+")";
    }
}
