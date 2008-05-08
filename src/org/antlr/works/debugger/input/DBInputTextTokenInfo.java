package org.antlr.works.debugger.input;

import org.antlr.runtime.Token;
import org.antlr.works.debugger.events.DBEventLocation;
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

public class DBInputTextTokenInfo {

    public Token token;

    public int start;
    public int end;

    public DBEventLocation location;

    public DBInputTextTokenInfo(Token token, DBEventLocation location) {
        this.token = token;
        this.location = location;
    }

    public void setStart(int start) {
        this.start = start;
        this.end = start+getText().length();
    }

    public DBEventLocation getLocation() {
        return location;
    }
    
    /**
     * Returns the raw text.
     * 
     * @return The raw text
     */
    public String getRawText() {
        return token.getText();
    }

    /**
     * Returns the normalized text representation of this token. Because Swing represents
     * internally newline by \n no matter which OS is used, we normalize the text of the token
     * to be also \n all the time.
     *
     * @return The normalized text used for display purpose
     */
    public String getText() {
        String t = getRawText();
        if(t.equals("\r\n") || t.equals("\r")) {
            return "\n";
        } else {
            return t;
        }
    }

}
