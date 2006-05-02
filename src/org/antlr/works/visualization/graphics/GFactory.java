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

package org.antlr.works.visualization.graphics;

import org.antlr.analysis.NFAState;
import org.antlr.tool.Grammar;
import org.antlr.works.grammar.EngineGrammar;
import org.antlr.works.grammar.EngineGrammarError;
import org.antlr.works.visualization.fa.FAFactory;
import org.antlr.works.visualization.fa.FAState;
import org.antlr.works.visualization.graphics.graph.GGraph;
import org.antlr.works.visualization.graphics.graph.GGraphGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GFactory {

    protected GRenderer renderer = new GRenderer();
    protected boolean optimize = true;

    public GFactory() {
    }

    public void setOptimize(boolean flag) {
        this.optimize = flag;
    }

    public void toggleNFAOptimization() {
        optimize = !optimize;
    }

    public List buildGraphsForRule(EngineGrammar grammar, String rule, List errors) throws Exception {
        if(errors == null || errors.size() == 0)
            return buildGraphsForRule(grammar, rule);
        else
            return buildGraphsForErrors(grammar, rule, errors);
    }

    public List buildGraphsForRule(EngineGrammar grammar, String rule) throws Exception {
        NFAState startState = grammar.getRuleStartState(rule);
        if(startState == null)
            return null;

        FAState state = new FAFactory(grammar.getGrammarForRule(rule)).buildNFA(startState, optimize);
        GGraph graph = renderer.render(state);
        graph.setName(rule);
        return Collections.singletonList(graph);
    }

    public List buildGraphsForErrors(EngineGrammar grammar, String rule, List errors) throws Exception {
        List graphs = new ArrayList();

        Iterator iterator = errors.iterator();
        while(iterator.hasNext()) {
            graphs.add(buildGraphGroup(grammar.getGrammarForRule(rule), (EngineGrammarError)iterator.next()));
        }

        return graphs;
    }

    private GGraphGroup buildGraphGroup(Grammar grammar, EngineGrammarError error) {
        // Create one GGraph for each error rules

        List graphs = new ArrayList();
        FAFactory factory = new FAFactory(grammar);
        for (int i = 0; i < error.rules.size(); i++) {
            String rule = (String)error.rules.get(i);
            NFAState startState = grammar.getRuleStartState(rule);
            FAState state = factory.buildNFA(startState, optimize);

            GGraph graph = renderer.render(state);
            graph.setName(rule);
            graphs.add(graph);
        }

        // Add only graphs that are referenced by at least one error path.
        // For example, the statement rule of the java.g grammar produces
        // states that do not exist in the graph (they are after the accepted state
        // and are ignored by the FAFactory)

        GGraphGroup gg = new GGraphGroup();
        for (Iterator graphIterator = graphs.iterator(); graphIterator.hasNext();) {
            GGraph graph = (GGraph) graphIterator.next();
            if(graph.containsAtLeastOneState(error.states))
                gg.add(graph);
        }

        // Attach all error paths to the GGraphGroup
        for(int i=0; i<error.paths.size(); i++) {
            List states = (List) error.paths.get(i);
            Boolean disabled = (Boolean) error.pathsDisabled.get(i);
            gg.addPath(states, disabled.booleanValue(), factory.getSkippedStatesMap());
        }

        // Attach all unreacheable alts to the GGraphGroup
        for(int i=0; i<error.unreachableAlts.size(); i++) {
            Object[] unreachableAlt = (Object[]) error.unreachableAlts.get(i);
            gg.addUnreachableAlt((NFAState)unreachableAlt[0], (Integer)unreachableAlt[1]);
        }

        if(error.paths.size()>0)
            gg.pathGroup.setPathVisible(0, true);

        return gg;
    }

}
