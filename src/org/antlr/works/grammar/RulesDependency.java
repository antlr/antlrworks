package org.antlr.works.grammar;

import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.GrammarWindow;
import org.antlr.works.grammar.element.ElementReference;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.xjlib.appkit.utils.XJAlert;

import java.util.ArrayList;
import java.util.List;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
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

public class RulesDependency extends GrammarDOTTab {

    protected List<String> visitedRules = new ArrayList<String>();
    protected List<String> visitedRefs = new ArrayList<String>();
    protected StringBuilder dependency;

    protected boolean includeLexerRefs;

    public RulesDependency(GrammarWindow window) {
        super(window);
    }

    @Override
    protected boolean willLaunch() {

        if(!checkForCurrentRule())
            return false;

        ElementRule rule = window.getCurrentRule();
        List<ElementReference> refs = window.editorRules.getReferencesInRule(rule);
        if(refs == null || refs.isEmpty()) {
            XJAlert.display(window.getJavaContainer(), "Error", "The selected rule doesn't contain any references");
            return false;
        }

        includeLexerRefs = true;
        if(!rule.lexer && window.getGrammarEngine().isCombinedGrammar()) {
            includeLexerRefs = XJAlert.displayAlertYESNO(window.getJavaContainer(), "Rule Dependency Graph", "Do you want to include lexer references ?") == XJAlert.YES;
        }

        return true;
    }

    @Override
    public String getDOTString() throws Exception {
        ElementRule rule = window.getCurrentRule();

        visitedRules.clear();
        visitedRefs.clear();

        dependency = new StringBuilder();
        dependency.append("digraph {\n");
        buildGraph(rule);
        dependency.append("}");

        return dependency.toString();
    }

    protected void buildGraph(ElementRule rule) {
        if(rule == null)
            return;

        visitedRules.add(rule.name);

        List<ElementReference> refs = window.editorRules.getReferencesInRule(rule);
        if(refs == null || refs.isEmpty())
            return;

        for (ElementReference reference : refs) {
            String refRuleName = reference.token.getAttribute();
            String visitedRef = rule.name + " -> " + refRuleName;

            if (visitedRefs.contains(visitedRef))
                continue;

            if (ATEToken.isLexerName(reference.token.getAttribute()) && !includeLexerRefs)
                continue;

            visitedRefs.add(visitedRef);

            dependency.append(visitedRef);
            dependency.append(";\n");

            if (!visitedRules.contains(refRuleName))
                buildGraph(window.getGrammarEngine().getRuleWithName(refRuleName));
        }
    }

    public String getTabName() {
        return "Dependency of \""+rule.name+"\"";
    }

}
