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

import org.antlr.works.ate.syntax.misc.ATEThread;
import org.antlr.works.grammar.AWGrammarError;
import org.antlr.works.syntax.GrammarSyntaxRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VisualAnalysis extends ATEThread {

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
            visual.getGrammar().analyze();
            if(!cancel())
                threadMarkRulesWithWarningsOrErrors();
        } catch(Exception e) {
            visual.editor.console.print(e);
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

        for (Iterator iterator = visual.getParserEngine().getRules().iterator(); iterator.hasNext();) {
            GrammarSyntaxRule rule = (GrammarSyntaxRule)iterator.next();
            updateRuleWithErrors(rule, threadFetchErrorsForRule(rule));
        }
        visual.delegate.visualizationDidMarkRules(visual);
    }

    private void updateRuleWithErrors(GrammarSyntaxRule rule, List errors) {
        rule.setErrors(errors);
        visual.drawing.createGraphsForRule(rule);
    }

    private List threadFetchErrorsForRule(GrammarSyntaxRule rule) {
        List errors = new ArrayList();
        for (Iterator iterator = visual.getGrammar().getErrors().iterator(); iterator.hasNext();) {
            AWGrammarError error = (AWGrammarError) iterator.next();
            if(error.line>=rule.start.startLineNumber && error.line<=rule.end.startLineNumber)
                errors.add(error);
        }
        return errors;
    }

    public void threadReportException(Exception e) {
        visual.getConsole().print(e);
    }

    public void threadRun() {
        if(shouldAnalyzeAndReset())
            threadAnalyze();
    }

}
