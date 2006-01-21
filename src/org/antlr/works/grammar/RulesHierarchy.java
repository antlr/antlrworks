package org.antlr.works.grammar;

import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.syntax.GrammarSyntaxReference;
import org.antlr.works.syntax.GrammarSyntaxRule;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
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

public class RulesHierarchy extends GrammarDOTTab {

    protected int line;
    protected int column;

    protected List visitedRules = new ArrayList();
    protected List visitedRefs = new ArrayList();
    protected StringBuffer hierarchy;

    public RulesHierarchy(CEditorGrammar editor, GrammarDOTTabDelegate delegate) {
        super(editor, delegate);
    }

    public void willRun() {
    }

    protected void generateDOTFile() throws Exception {
        GrammarSyntaxRule rule = editor.getCurrentRule();
        if(rule == null)
            throw new Exception("No rule is selected");

        List refs = editor.rules.getReferencesInRule(rule);
        if(refs == null || refs.isEmpty())
            throw new Exception("The selected rule doesn't contain any references");

        visitedRules.clear();
        visitedRefs.clear();

        hierarchy = new StringBuffer();
        hierarchy.append("digraph {\n");
        buildGraph(rule);
        hierarchy.append("}");

        BufferedWriter bw = new BufferedWriter(new FileWriter(tempInputFile));
        bw.write(hierarchy.toString());
        bw.close();
    }

    protected void buildGraph(GrammarSyntaxRule rule) {
        if(rule == null)
            return;

        visitedRules.add(rule.name);

        List refs = editor.rules.getReferencesInRule(rule);
        if(refs == null || refs.isEmpty())
            return;

        for (Iterator iterator = refs.iterator(); iterator.hasNext();) {
            GrammarSyntaxReference reference = (GrammarSyntaxReference) iterator.next();
            String refRuleName = reference.token.getAttribute();
            String visitedRef = rule.name+" -> "+refRuleName;

            if(visitedRefs.contains(visitedRef))
                continue;

            visitedRefs.add(visitedRef);

            hierarchy.append(visitedRef);
            hierarchy.append(";\n");

            if(!visitedRules.contains(refRuleName))
                buildGraph(editor.rules.getRuleWithName(refRuleName));
        }
    }

    public String getTabName() {
        return "Hierarchy of \""+rule.name+"\"";
    }

}
