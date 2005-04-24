package org.antlr.works.editor.visual;

import org.antlr.analysis.NFAState;
import org.antlr.works.editor.EditorThread;
import org.antlr.works.parser.Parser;
import org.antlr.works.visualization.fa.FAFactory;
import org.antlr.works.visualization.fa.FAState;
import org.antlr.works.visualization.graphics.GFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*

[The "BSD licence"]
Copyright (c) 2004-05 Jean Bovet
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

public class VisualDrawing extends EditorThread {

    protected Visual visual;

    protected GFactory factory = new GFactory();

    protected FAState state;

    protected String text;
    protected Parser.Rule rule;

    protected boolean latchSilent = false;

    protected String threadText;
    protected Parser.Rule threadRule;

    protected Parser.Rule threadLastProcessedRule;

    protected Map cacheOptimizedNFA = new HashMap();
    protected Map cacheGraphs = new HashMap();

    public VisualDrawing(Visual visual) {
        this.visual = visual;

        start();
    }

    public synchronized void setText(String text) {
        this.text = text;
        awakeThread(500);
    }

    public synchronized void setRule(Parser.Rule rule, boolean immediate) {
        this.rule = rule;
        awakeThread(immediate?0:500);
    }

    public synchronized void clearCacheGraphs() {
        cacheGraphs.clear();
    }

    public synchronized Map getCacheGraphs() {
        return cacheGraphs;
    }

    public synchronized void refresh() {
        visual.panel.setRule(threadLastProcessedRule);
        visual.panel.setGraphs((List)cacheGraphs.get(threadLastProcessedRule));
        visual.panel.update();
    }

    public synchronized boolean threadShouldProcess() {
        return text != null || rule != null;
    }

    public synchronized void threadPrepareProcess() {
        this.threadText = text;
        this.threadRule = rule;

        text = null;
        rule = null;
    }

    private void threadProcessText() {
        if(threadText == null)
            return;

        try {
            visual.engine.setGrammarText(threadText);
        } catch (Exception e) {
        } finally {
            // Flush all caches in cache because the grammar has changed
            cacheOptimizedNFA.clear();
            clearCacheGraphs();
        }
    }

    private void threadProcessRule() {
        if(threadRule == null)
            return;

        if(visual.engine.g == null) {
            System.err.println("Cannot display rule \""+threadRule+"\" because Grammar is null");
            return;
        }

        NFAState startState = visual.engine.g.getRuleStartState(threadRule.name);
        if(startState == null) {
            System.err.println("Cannot find start state for rule \""+threadRule+"\"");
            return;
        }

        // Try to get the optimized NFA from cache first. If the grammar didn't change (i.e. user
        // only moving cursor in the text zone), the speed-up can be important.
        state = (FAState)cacheOptimizedNFA.get(threadRule);
        if(state == null) {
            state = new FAFactory(visual.engine.g).buildOptimizedNFA(startState);
            if(state != null)
                cacheOptimizedNFA.put(threadRule, state);
        }

        // Try also to optimize the graph if the context or skin didn't change.
        createGraphsForRule(threadRule);

        threadLastProcessedRule = threadRule;

        refresh();
    }

    protected synchronized void createGraphsForRule(Parser.Rule rule) {
        List graphs = (List)cacheGraphs.get(rule);
        if(graphs == null) {
            graphs = factory.buildGraphsForRule(visual.engine, rule.name, rule.errors);
            if(graphs != null)
                cacheGraphs.put(rule, graphs);
        }
    }

    public void threadRun() {
        if(threadShouldProcess()) {
            threadPrepareProcess();
            threadProcessText();
            threadProcessRule();
        }
    }

}
