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

package org.antlr.works.editor.visual;

import org.antlr.works.editor.helper.EditorThread;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.visualization.grammar.GrammarEngineError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VisualAnalysis extends EditorThread {

    protected Visual visual;
    private boolean analyze = false;

    public VisualAnalysis(Visual visual) {
        this.visual = visual;
        start(500);
    }

    public synchronized void startAnalysis() {
        this.analyze = true;
    }

    private synchronized boolean shouldAnalyzeAndReset() {
        if(analyze) {
            analyze = false;
            return true;
        } else
            return false;
    }

    private void threadAnalyze() {
        visual.delegate.visualizationProcessDidBegin(visual);
        try {
            visual.engine.analyze(this);
            if(!cancel())
                threadMarkRulesWithWarningsOrErrors();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(!cancel()) {
                visual.panel.createPanel();
                visual.drawing.refresh();                
            }
            visual.delegate.visualizationProcessDidEnd(visual);
        }
    }

    private void threadMarkRulesWithWarningsOrErrors() {
        // Clear graphic cache because we have to redraw each rule again
        visual.drawing.clearCacheGraphs();

        for (Iterator iterator = visual.parser.getRules().iterator(); iterator.hasNext();) {
            ParserRule rule = (ParserRule)iterator.next();
            updateRuleWithErrors(rule, threadFetchErrorsForRule(rule));
        }
        visual.delegate.visualizationDidMarkRules(visual);
    }

    private void updateRuleWithErrors(ParserRule rule, List errors) {
        rule.setErrors(errors);
        visual.drawing.createGraphsForRule(rule);
    }

    private List threadFetchErrorsForRule(ParserRule rule) {
        List errors = new ArrayList();

        for (Iterator iterator = visual.engine.errors.iterator(); iterator.hasNext();) {
            GrammarEngineError error = (GrammarEngineError) iterator.next();
            if(error.line>=rule.start.startLineNumber && error.line<=rule.end.startLineNumber)
                errors.add(error);
        }
        return errors;
    }

    public void threadRun() {
        if(shouldAnalyzeAndReset())
            threadAnalyze();
    }

}
