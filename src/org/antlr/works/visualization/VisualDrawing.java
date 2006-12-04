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
import org.antlr.works.ate.syntax.misc.ATEThread;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.syntax.GrammarSyntaxRule;
import org.antlr.works.utils.Console;
import org.antlr.works.visualization.graphics.GFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualDrawing extends ATEThread {

    protected Visual visual;

    protected GFactory factory = new GFactory();

    protected String text;
    protected GrammarSyntaxRule rule;

    protected String threadText;
    protected GrammarSyntaxRule threadRule;
    protected GrammarSyntaxRule threadLastProcessedRule;

    protected Map cacheGraphs = new HashMap();

    public VisualDrawing(Visual visual) {
        this.visual = visual;
        start();
    }

    public void toggleNFAOptimization() {
        factory.toggleNFAOptimization();
        clearCacheGraphs();
    }

    public synchronized void setText(String text) {
        this.text = text;
        awakeThread(500);
    }

    public synchronized void setRule(GrammarSyntaxRule rule, boolean immediate) {
        this.rule = rule;
        awakeThread(immediate?0:500);
    }

    public synchronized void clearCacheGraphs() {
        cacheGraphs.clear();
    }

    /**
     * Tries to refresh the current graph in cache. If the graphs are not in cache, return false.
     */
    public synchronized boolean refresh() {
        List graphs = (List)cacheGraphs.get(threadLastProcessedRule);
        if(graphs == null || graphs.isEmpty()) {
            return false;
        } else {
            visual.panel.setRule(threadLastProcessedRule);
            visual.panel.setGraphs(graphs);
            visual.panel.update();
            return true;
        }
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
            visual.getEngineGrammar().createGrammars();
        } catch (Exception e) {
            // @todo ignore for now but later with prefs?
            //visual.editor.console.print(e);
        } finally {
            // Flush all caches in cache because the grammar has changed
            clearCacheGraphs();
        }
    }

    private void threadProcessRule() throws Exception {
        if(threadRule == null)
            return;

        String error = null;

        if(visual.getEngineGrammar().hasGrammar()) {
            NFAState startState = null;
            try {
                startState = visual.getEngineGrammar().getRuleStartState(threadRule.name);
                /*Grammar g = visual.getEngineGrammar().getLexerGrammar();
                for(int i=104; i<152; i++) {
                   // System.out.println(g.getTokenDisplayName(g.getTokenType("T"+i)));
                } */
            } catch (Exception e) {
                // @todo ignore for now but later with prefs?                
                //visual.editor.console.print(e);
            }
            if(startState == null)
                error = "Cannot display rule \"" + threadRule + "\" because start state not found";
        } else {
            error = "Cannot display rule \""+threadRule+"\" because grammar cannot be generated";
        }

        if(error != null) {
            visual.setPlaceholder(error);
            visual.getConsole().println(error, Console.LEVEL_ERROR);
            return;
        }

        // Try to get the optimized graph from cache first. If the grammar didn't change (i.e. user
        // only moving cursor in the text zone), the speed-up can be important.
        createGraphsForRule(threadRule);

        threadLastProcessedRule = threadRule;

        refresh();
    }

    protected synchronized void createGraphsForRule(GrammarSyntaxRule rule) throws Exception {
        List graphs = (List)cacheGraphs.get(rule);
        if(graphs == null) {
            factory.setOptimize(!AWPrefs.getDebugDontOptimizeNFA());
            factory.setConsole(visual.getConsole());
            graphs = factory.buildGraphsForRule(visual.getEngineGrammar(), rule.name, rule.errors);
            if(graphs != null)
                cacheGraphs.put(rule, graphs);
        }
    }

    public void threadReportException(Exception e) {
        visual.getConsole().print(e);
    }

    public void threadRun() throws Exception {
        visual.getConsole().setMode(Console.MODE_QUIET);

        if(threadShouldProcess()) {
            threadPrepareProcess();
            threadProcessText();
            threadProcessRule();
        }
    }

}
