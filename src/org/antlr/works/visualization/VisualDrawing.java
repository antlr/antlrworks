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

package org.antlr.works.visualization;

import org.antlr.analysis.NFAState;
import org.antlr.works.editor.EditorThread;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.visualization.graphics.GFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualDrawing extends EditorThread {

    protected Visual visual;

    protected GFactory factory = new GFactory();

    protected String text;
    protected String filename;
    protected ParserRule rule;

    protected boolean latchSilent = false;

    protected String threadText;
    protected ParserRule threadRule;

    protected ParserRule threadLastProcessedRule;

    protected Map cacheGraphs = new HashMap();

    public VisualDrawing(Visual visual) {
        super(visual.editor.console);
        this.visual = visual;
        start();
    }

    public void toggleNFAOptimization() {
        factory.toggleNFAOptimization();
        clearCacheGraphs();
    }

    public synchronized void setText(String text, String filename) {
        this.text = text;
        this.filename = filename;
        awakeThread(500);
    }

    public synchronized void setRule(ParserRule rule, boolean immediate) {
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
            //visual.getGrammar().setGrammarText(threadText, filename);
            visual.getGrammar().createGrammars();
        } catch (Exception e) {
            // Ignore
            visual.editor.console.print(e);
        } finally {
            // Flush all caches in cache because the grammar has changed
            clearCacheGraphs();
        }
    }

    private void threadProcessRule() {
        if(threadRule == null)
            return;

        String error = null;

        if(visual.getGrammar().hasGrammar()) {
            NFAState startState = visual.getGrammar().getRuleStartState(threadRule.name);
            if(startState == null)
                error = "Cannot display rule \"" + threadRule + "\" because start state not found";
        } else {
            error = "Cannot display rule \""+threadRule+"\" because grammar cannot be generated";
        }

        if(error != null) {
            visual.setPlaceholder(error);
            console.println(error);
            return;
        }

        // Try to get the optimized graph from cache first. If the grammar didn't change (i.e. user
        // only moving cursor in the text zone), the speed-up can be important.
        createGraphsForRule(threadRule);

        threadLastProcessedRule = threadRule;

        refresh();
    }

    protected synchronized void createGraphsForRule(ParserRule rule) {
        List graphs = (List)cacheGraphs.get(rule);
        if(graphs == null) {
            graphs = factory.buildGraphsForRule(visual.getGrammar(), rule.name, rule.errors);
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
