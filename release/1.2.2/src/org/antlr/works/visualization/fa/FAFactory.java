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
import org.antlr.analysis.RuleClosureTransition;
import org.antlr.analysis.State;
import org.antlr.analysis.Transition;
import org.antlr.tool.Grammar;

import java.util.*;

/** This class builds an "GUI" NFA from an "ANTLR" NFA by removing redundant epsilon transition(s).
 *
 */

public class FAFactory {

    protected Grammar g;
    protected boolean optimize;
    protected FAAnalysis analysis = new FAAnalysis();
    protected Map<NFAState,FAState> processedStates = new HashMap<NFAState, FAState>();
    protected Map<Integer,FAState> skippedStatesMap = new HashMap<Integer, FAState>();

    protected int newStateNumber = State.INVALID_STATE_NUMBER-1;

    public FAFactory(Grammar g) {
        this.g = g;
    }

    public FAState buildNFA(NFAState state, boolean optimize) {
        this.optimize = optimize;
        return build(state);
    }

    public Map<Integer,FAState> getSkippedStatesMap() {
        return skippedStatesMap;
    }

    public FAState build(NFAState state) {
        processedStates.clear();

        // First compute the incoming transition for each state. This will be used later to
        // know if a state can be simplified or not.
        analysis.analyze(state);
        return buildRecursiveState(state, new HashSet<NFAState>());
    }

    public FAState buildRecursiveState(NFAState state, Set<NFAState> currentPath) {
        if(processedStates.get(state) != null) {
            FAState js = processedStates.get(state);
            if(currentPath.contains(state)) {
                // Set this temporary flag to indicate to the parent method that
                // the transition to be created has to be flagged as "loop".
                js.loop = true;
            }
            return js;
        }

        FAState js = new FAState(state);
        processedStates.put(state, js);
        currentPath.add(state);

        if(state.isAcceptState()) {
            // Stop as soon as we reach an accepted state
            return js;
        }

        for(int t=0; t<state.getNumberOfTransitions(); t++) {
            FAState parentState = js;

            Transition transition = state.transition(t);
            NFAState target = (NFAState)transition.target;
            if(targetStateIsInAnotherRule(transition)) {
                target = targetStateOfTransition(transition);
                parentState = createRuleReferenceState(parentState, transition, null);
            }

            if(transition.isEpsilon()) {
                buildRecursiveSkipState(parentState, target, new HashSet<NFAState>(currentPath), new ArrayList<Integer>());
            } else {
                FAState targetState = buildRecursiveState(target, new HashSet<NFAState>(currentPath));
                if(targetState.loop) {
                    // Handle "loop" transition by creating a "normal" transition and assigning a flag
                    // to this transition so when drawing it, we can draw the arrow at the right place
                    // (in the reverse direction)
                    targetState.addTransition(new FATransition(transition.label.toString(g), parentState), true);
                    targetState.loop = false;
                } else
                    parentState.addTransition(new FATransition(transition.label.toString(g), targetState));
            }
        }
        return js;
    }

    /** This method is used to skip redundant state following epsilon transition.
     *
     */

    public void buildRecursiveSkipState(FAState parentState, NFAState state, Set<NFAState> currentPath, List<Integer> skippedStates) {
        if(canBeSkipped(state)) {
            // If the state can be skipped, apply recursively the same method for each transition(s)
            // providing the parent state.

            // Record each skipped state. They will be added later to the transition that replace them
            Integer skippedState = state.stateNumber;
            skippedStates.add(skippedState);
            skippedStatesMap.put(skippedState, parentState);

            for(int t=0; t<state.getNumberOfTransitions(); t++) {
                Transition transition = state.transition(t);
                if(targetStateIsInAnotherRule(transition)) {
                    NFAState target = targetStateOfTransition(transition);
                    FAState ruleRefState = createRuleReferenceState(parentState, transition, skippedStates);
                    buildRecursiveSkipState(ruleRefState, target, currentPath, new ArrayList<Integer>(skippedStates));
                } else
                    buildRecursiveSkipState(parentState, (NFAState)transition.target, currentPath, new ArrayList<Integer>(skippedStates));
            }
        } else {
            // The state cannot be skipped. Build the remaining of the NFA...
            FAState targetState = buildRecursiveState(state, currentPath);
            // and then create the transition from the parentState to this current state
            // (this is the simplification ;-))
            if(targetState.loop) {
                // See comment above (in the previous method)
                targetState.addTransition(new FATransition(parentState, skippedStates), true);
                targetState.loop = false;
            } else
                parentState.addTransition(new FATransition(targetState, skippedStates));
        }
    }

    public boolean targetStateIsInAnotherRule(Transition transition) {
        return transition instanceof RuleClosureTransition;
    }

    public String nameOfExternalReferencedRule(Transition transition) {
        if(transition instanceof RuleClosureTransition) {
            RuleClosureTransition rct = (RuleClosureTransition)transition;
            return g.getRuleName(rct.rule.index);
        } else
            return null;
    }

    public FAState createRuleReferenceState(FAState parentState, Transition transition, List<Integer> skippedStates) {
        // Create epsilon transition before the external reference transition
        FAState dummyState = new FAState(newStateNumber--);
        dummyState.enclosingRuleName = parentState.enclosingRuleName;
        FATransition epsilon = new FATransition(dummyState, skippedStates);
        parentState.addTransition(epsilon);

        FAState ruleRefState = new FAState(newStateNumber--);
        ruleRefState.enclosingRuleName = parentState.enclosingRuleName;
        FATransition tr = new FATransition(nameOfExternalReferencedRule(transition), ruleRefState);
        tr.setExternalRuleRef(true);
        dummyState.addTransition(tr);

        return ruleRefState;
    }

    public NFAState targetStateOfTransition(Transition transition) {
        NFAState target;
        if(transition instanceof RuleClosureTransition) {
            RuleClosureTransition rct = (RuleClosureTransition)transition;
            target = rct.followState;
        } else {
            target = (NFAState)transition.target;
        }
        return target;
    }

    /** This method returns true if the state can be skipped, using several criteria.
     *
     */

    public boolean canBeSkipped(NFAState state) {
        if(!optimize)
            return false;

        // Cannot skip first state
        if(state.stateNumber == 0)
            return false;

        // Cannot skip accepted state
        if(state.isAcceptState())
            return false;

        // Cannot skip alternative state
        if(state.getDecisionNumber() > 0)
            return false;

        // Cannot skip start of block state
        if(state.endOfBlockStateNumber != State.INVALID_STATE_NUMBER)
            return false;

        // Cannot skip state with more than one incoming transition (often an end-of-alternative state)
        if(analysis.numberOfIncomingTransition(state)>1)
            return false;

        return hasOneOrMoreEpsilonTransitionOnly(state);
    }

    public boolean hasOneEpsilonTransitionOnly(NFAState state) {
        return state.getNumberOfTransitions() == 1 && state.transition(0).isEpsilon();
    }

    public boolean hasOneOrMoreEpsilonTransitionOnly(NFAState state) {
        for(int t=0; t<state.getNumberOfTransitions(); t++) {
            Transition transition = state.transition(t);
            if(!transition.isEpsilon())
                return false;
        }
        return state.getNumberOfTransitions()>0;
    }

    public boolean hasMoreThanOneEpsilonTransitionOnly(NFAState state) {
        for(int t=0; t<state.getNumberOfTransitions(); t++) {
            Transition transition = state.transition(t);
            if(!transition.isEpsilon())
                return false;
        }
        return state.getNumberOfTransitions()>1;
    }

    public boolean isAlternativeTransitionEndingAtSameState(NFAState state) {
        NFAState endState = endStateOfAlternative((NFAState)state.transition(0).target);

        for(int t=1; t<state.getNumberOfTransitions(); t++) {
            Transition transition = state.transition(t);
            NFAState newEndState = endStateOfAlternative((NFAState)transition.target);
            if(!endState.equals(newEndState))
                return false;
        }
        return true;
    }

    public NFAState endStateOfAlternative(NFAState alt) {
        int endOfBlockStateNumber = alt.endOfBlockStateNumber;

        NFAState state = alt;
        while(state.stateNumber != endOfBlockStateNumber) {
            state = (NFAState)state.transition(0).target;
        }
        return state;
    }
}
