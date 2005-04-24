package org.antlr.works.visualization.fa;

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

import org.antlr.analysis.NFAState;

import java.util.*;

/** Class defining an "GUI" NFA state. It is basically the same as a FAState.
 *
 */

public class FAState {

    public int stateNumber = -1;
    public boolean acceptedState = false;
    public String enclosingRuleName = null;
    public List transitions = new ArrayList();

    // If the state represents a reference to an external state, this field
    // contains the name of the referenced rule.
    public String externalRuleRefName = null;

    // List of skipped states (they won't be displayed but we need to know their
    // number in order to display corrected the error paths)
    public Set skippedStates = new HashSet();

    // Temporary variable that is used by FAFactory to know when to build "loop" transition
    public boolean loop = false;

    public FAState(int stateNumber) {
        this.stateNumber = stateNumber;
    }

    public FAState(NFAState state) {
        this.stateNumber = state.stateNumber;
        this.acceptedState = state.isAcceptState();
        this.enclosingRuleName = state.getEnclosingRule();
    }

    public FAState(String externalRuleRefName) {
        this.externalRuleRefName = externalRuleRefName;
    }

    public void addTransition(FATransition transition) {
        transition.setSourceState(this);
        transitions.add(transition);
        sortTransitions();
    }

    public void addTransition(FATransition transition, boolean loop) {
        transition.setSourceState(this);
        transition.setLoop(loop);
        transitions.add(transition);
        sortTransitions();
    }

    private void sortTransitions() {
        if(transitions.size()<=1)
            return;

        // We assume here that only one transition is a loop. This transition should always be
        // located at the end of the list.

        for(int t=0; t<transitions.size(); t++) {
            FATransition transition = (FATransition)transitions.get(t);
            if(transition.loop && t<transitions.size()-1) {
                // This loop transition is not at the end of the list. Move it.
                Collections.swap(transitions, t, transitions.size()-1);
                break;
            }
        }
    }

    public FATransition getFirstTransition() {
        if(transitions.size()>0)
            return (FATransition)transitions.get(0);
        else
            return null;
    }

    public FATransition transition(int index) {
        return (FATransition)transitions.get(index);
    }

    public int getNumberOfTransitions(){
        return transitions.size();
    }

    public FAState getNextFirstState() {
        return getFirstTransition().target;
    }

    public FATransition getTransitionToStateNumber(int stateNumber) {
        Iterator iterator = transitions.iterator();
        while(iterator.hasNext()) {
            FATransition transition = (FATransition)iterator.next();
            if(transition.target.containsStateNumber(stateNumber))
                return transition;
        }
        return null;
    }

    /** This method returns true if the state is an alternative. "loop" transitions are skipped.
     *
     */

    public boolean isAlternative() {
        return getNumberOfTransitions() > 1;
    }

    /** This method returns true if the state is single state (that is with only one non-loop transition)
     *
     */

    public boolean isSingle() {
        return getNumberOfTransitions() == 1;
    }

    public boolean hasLoopTransition() {
        Iterator iterator = transitions.iterator();
        while(iterator.hasNext()) {
            FATransition transition = (FATransition)iterator.next();
            if(transition.loop)
                return true;
        }
        return false;
    }

    public void addSkippedState(int stateNumber) {
        skippedStates.add(new Integer(stateNumber));
    }

    public boolean containsStateNumber(int stateNumber) {
        if(this.stateNumber == stateNumber)
            return true;
        else
            return skippedStates.contains(new Integer(stateNumber));
    }

    public int hashCode() {
        return stateNumber;
    }

    public boolean equals(Object o) {
        FAState otherState = (FAState)o;
        return containsStateNumber(otherState.stateNumber);
    }

    public String toString() {
        if(externalRuleRefName == null)
            return String.valueOf(stateNumber);
        else
            return "<"+externalRuleRefName+">";
    }

    /** Method used by the TestSuite
     *
     */

    public void getPaths(Set alreadyVisitedStates, String path, List outPaths) {
        alreadyVisitedStates.add(this);

        if(transitions.size() == 0) {
            outPaths.add(path);
            return;
        }

        Iterator iterator = transitions.iterator();
        while(iterator.hasNext()) {
            FATransition transition = (FATransition)iterator.next();
            FAState state = transition.target;

            String np = (path.length()==0?"":path+",")+this+"-"+(transition.label!=null?transition.label:"e")+"-"+state;

            if(alreadyVisitedStates.contains(state)) {
                outPaths.add(np);
            } else {
                Set s = new HashSet();
                s.addAll(alreadyVisitedStates);

                state.getPaths(s, np, outPaths);
            }
        }
    }

}