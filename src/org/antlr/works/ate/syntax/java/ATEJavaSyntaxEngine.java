package org.antlr.works.ate.syntax.java;

import org.antlr.works.ate.syntax.generic.ATESyntaxEngine;

import java.util.HashSet;
import java.util.Set;
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

public class ATEJavaSyntaxEngine extends ATESyntaxEngine {

    private static final Set<String> s;

    static {
        s = new HashSet<String>();
        s.add("package");
        s.add("import");
        s.add("synchronized");
        s.add("instanceof");
        s.add("static");
        s.add("final");
        s.add("public");
        s.add("protected");
        s.add("private");
        s.add("class");
        s.add("extends");
        s.add("implements");
        s.add("abstract");
        s.add("interface");
        s.add("super");
        s.add("void");
        s.add("int");
        s.add("boolean");
        s.add("double");
        s.add("float");
        s.add("return");
        s.add("if");
        s.add("else");
        s.add("null");
        s.add("false");
        s.add("true");
        s.add("new");
        s.add("this");
        s.add("try");
        s.add("catch");
        s.add("switch");
        s.add("case");
        s.add("default");
        s.add("while");
        s.add("for");
        s.add("break");
        s.add("finally");
        s.add("do");
    }

    public Set<String> getKeywords() {
        return s;
    }

}
