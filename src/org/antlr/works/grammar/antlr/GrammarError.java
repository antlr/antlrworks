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

package org.antlr.works.grammar.antlr;

import org.antlr.analysis.NFAState;
import org.antlr.tool.Message;

import java.util.ArrayList;
import java.util.List;

public class GrammarError {

    /** Array of array of NFAState states */
    public List<List> paths = new ArrayList<List>();

    /** Array describing which path is disabled */
    public List<Boolean> pathsDisabled = new ArrayList<Boolean>();

    /** Array of array of NFAState states */
    public List<Object[]> unreachableAlts = new ArrayList<Object[]>();

    /** Array of rules concerned by the error */
    public List<String> rules = new ArrayList<String>();

    /** Array of states concerned by the error */
    public List states = new ArrayList();

    public List labels;

    public int line;
    public String messageText;
    public Message message;

    public GrammarError() {
    }

    public void addPath(List path, boolean disabled) {
        this.paths.add(path);
        this.pathsDisabled.add(disabled);
    }

    public void addUnreachableAlt(NFAState state, Integer alt) {
        this.unreachableAlts.add(new Object[] { state, alt});
    }

    public void addRule(String rule) {
        if(!rules.contains(rule))
            rules.add(rule);
    }

    public void addStates(NFAState state) {
        this.states.add(state);
    }

    public void addStates(List states) {
        this.states.addAll(states);
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
    
    public void setLabels(List labels) {
        this.labels = labels;
    }

    public List getLabels() {
        return labels;
    }
}
