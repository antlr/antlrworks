package org.antlr.works.grammar.element;

import org.antlr.works.ate.syntax.misc.ATEToken;

import java.util.ArrayList;
import java.util.List;
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

public class ElementGrammarName {

    public static final List<String> types;

    public static final int COMBINED = 0;
    public static final int PARSER = 1;
    public static final int LEXER = 2;
    public static final int TREEPARSER = 3;

    public ATEToken name;
    public ATEToken start;
    public ATEToken end;
    public ATEToken type;

    static {
        types = new ArrayList<String>();
        types.add("combined");
        types.add("parser");
        types.add("lexer");
        types.add("tree");
    }

    public ElementGrammarName(ATEToken name, ATEToken start, ATEToken end, ATEToken type) {
        this.name = name;
        this.start = type==null?start:type;
        this.end = end;
        this.type = type;
    }

    public void close() {
        name = null;
        start = null;
        end = null;
        type = null;
    }

    public int getType() {
        int t = COMBINED;
        if(type != null) {
            t = types.indexOf(type.getAttribute());
        }

        if(t == -1) {
            return COMBINED;
        } else {
            return t;
        }
    }

    public String getName() {
        return name.getAttribute();
    }

    public static boolean isKnownType(String type) {
        return types.contains(type);
    }

}
