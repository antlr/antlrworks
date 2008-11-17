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

import org.antlr.analysis.NFAState;
import org.antlr.analysis.Transition;

import java.util.*;

/** This class computes the number of incoming transition for each state.
 *
 *
 */

public class FAAnalysis {

    private Set processedStates = new HashSet();
    private Map stateIncomingTransitionCount = new HashMap();

    public FAAnalysis() {
    }

    public void reset() {
        processedStates.clear();
        stateIncomingTransitionCount.clear();
    }

    /** Compute for an ANTLR NFA
     *
     */

    public void analyze(FAState state) {
        reset();
        recursiveAnalysis(state);
    }

    /** Compute for an GUI Editor NFA
     *
     */

    public void analyze(NFAState state) {
        reset();
        recursiveAnalysis(state);
    }

    public void recursiveAnalysis(NFAState state) {
        if(processedStates.contains(state))
            return;
        processedStates.add(state);

        for(int t=0; t<state.getNumberOfTransitions(); t++) {
            Transition transition = state.transition(t);
            addIncomingTransitionToState((NFAState)transition.target);
            recursiveAnalysis((NFAState)transition.target);
        }
    }

    public void recursiveAnalysis(FAState state) {
        if(processedStates.contains(state))
            return;
        processedStates.add(state);

        for(int t=0; t<state.getNumberOfTransitions(); t++) {
            FATransition transition = state.transition(t);
            addIncomingTransitionToState(transition.target);
            recursiveAnalysis(transition.target);
        }
    }

    public void addIncomingTransitionToState(Object state) {
        Integer i = (Integer)stateIncomingTransitionCount.get(state);
        int count = 0;
        if(i != null)
            count = i.intValue();
        stateIncomingTransitionCount.put(state, new Integer(count+1));
    }

    public int numberOfIncomingTransition(Object state) {
        Integer i = (Integer)stateIncomingTransitionCount.get(state);
        if(i == null)
            return 0;
        else
            return i.intValue();
    }
    
    public String toString() {
        StringBuilder s = new StringBuilder();
        Iterator iterator = stateIncomingTransitionCount.keySet().iterator();
        while(iterator.hasNext()) {
            NFAState key = (NFAState)iterator.next();
            Integer count = (Integer)stateIncomingTransitionCount.get(key);
            s.append(key.stateNumber+" = "+count.intValue()+"\n");
        }
        return s.toString();
    }
}
