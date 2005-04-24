package org.antlr.works.util;

import org.antlr.works.visualization.fa.FAState;
import org.antlr.works.visualization.fa.FATransition;
import org.antlr.works.visualization.fa.FAWalker;
import org.antlr.works.visualization.fa.FAWalkerDelegate;

import java.io.FileWriter;

/*

[The "BSD licence"]
Copyright (c) 2004 Jean Bovet
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

public class DotGenerator implements FAWalkerDelegate {

    public static final int MODE_HEADER = 1;
    public static final int MODE_TRANSITION = 2;

    public StringBuffer dot = new StringBuffer();

    public DotGenerator(FAState state) {
        dot.append("digraph NFA {\n");
        dot.append("rankdir=LR\n");
        new FAWalker(this, MODE_HEADER).walk(state);
        new FAWalker(this, MODE_TRANSITION).walk(state);
        dot.append("}");
    }

    public void writeToFile(String name) throws Exception {
        FileWriter fw = new FileWriter(name);
        fw.write(dot.toString());
        fw.close();
    }

    public void walkerState(FAState state, int mode) {
        if(mode == MODE_HEADER) {
            dot.append("node [fontsize=11, shape = circle]; \""+state.stateNumber+"\"\n");
        }
    }

    public void walkerTransition(FATransition transition, int mode) {
        if(mode == MODE_TRANSITION) {
            dot.append("\""+transition.source+"\" -> \""+transition.target+"\"");
            String label = transition.label==null?"e":transition.label;
            dot.append(" [fontsize=11, fontname=\"Courier\", label = \""+label+"\"];\n");
        }
    }
}
