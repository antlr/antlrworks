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


package org.antlr.works.visualization.fa;

import org.antlr.works.visualization.serializable.SEncoder;
import org.antlr.works.visualization.serializable.SSerializable;

import java.util.List;

public class FATransition implements SSerializable {

    public FAState source;
    public String label;
    public FAState target;

    /** True if this is a "loop" transition (reverse direction) */
    public boolean loop = false;

    /** True if this transition represent an external reference rule */
    public boolean externalRuleRef = false;

    /** Set of all skipped states along this transition */
    public List<Integer> skippedStates;

    public FATransition(String label, FAState targetState) {
        this.label = label;
        this.target = targetState;
    }

    public FATransition(FAState targetState) {
        this(targetState, null);
    }

    public FATransition(FAState targetState, List<Integer> skippedStates) {
        this.label = null;  // epsilon transition
        this.target = targetState;
        this.skippedStates = skippedStates;
    }

    public void setSourceState(FAState source) {
        this.source = source;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setExternalRuleRef(boolean flag) {
        this.externalRuleRef = flag;
    }

    public boolean isEpsilon() {
        return label == null;
    }

    public boolean containsStateNumber(int n) {
        for (Integer state : skippedStates) {
            if (state == n)
                return true;
        }
        return false;
    }

    public void encode(SEncoder encoder) {
        encoder.write(source);
        encoder.write(label);
        encoder.write(target);
        encoder.write(loop);
        encoder.write(externalRuleRef);
        if(skippedStates != null) {
            for(Integer s : skippedStates) {
                encoder.write(s);
            }
        }
    }

}
