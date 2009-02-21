package org.antlr.works.test.ut;

import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.editor.EditorInspector;
import org.antlr.works.editor.EditorRules;
import org.antlr.works.grammar.element.ElementBlock;
import org.antlr.works.grammar.element.ElementGrammarName;
import org.antlr.works.grammar.element.ElementReference;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.test.AbstractTest;
import org.antlr.works.test.TestConstants;

import java.util.*;
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

public class TestParser extends AbstractTest {

    public static void main(String[] args) {
        new TestRunner().doRun(new TestSuite(TestParser.class));
    }

    public void testDoubleQuoteStringInArgument() throws Exception {
        parseFile(TestConstants.PREFIX+"arguments.g");
        assertInspector(0);
    }

    public void testEmptyRewriteSyntax() throws Exception {
        parseFile(TestConstants.PREFIX+"empty_rewrite.g");
        assertInspector(0);
    }

    public void testGroups() throws Exception {
        parseFile(TestConstants.PREFIX+"groups.g");
        assertInspector(0);
    }

    public void testIgnoreRules() throws Exception {
        parseFile(TestConstants.PREFIX+"ignore_rules.g");
        EditorRules.findTokensToIgnore(getEngine().getRules(), true);
        int ignored = 0;
        for(ElementRule r : getEngine().getRules()) {
            if(r.ignored) ignored++;
        }
        assertEquals("ignored rules", 3, ignored);
    }

    public void testGrammarType() throws Exception {
        parseFile(TestConstants.PREFIX+"type/combined.g");
        assertEquals("combined grammar", ElementGrammarName.COMBINED, getEngine().getType());

        parseFile(TestConstants.PREFIX+"type/parser.g");
        assertEquals("parser grammar", ElementGrammarName.PARSER, getEngine().getType());

        parseFile(TestConstants.PREFIX+"type/lexer.g");
        assertEquals("lexer grammar", ElementGrammarName.LEXER, getEngine().getType());

        parseFile(TestConstants.PREFIX+"type/tree.g");
        assertEquals("tree grammar", ElementGrammarName.TREEPARSER, getEngine().getType());
    }

    public void testSyntaxBlock() throws Exception {
        parseFile(TestConstants.BLOCKS);
        assertInspector(0);

        assertEquals("grammar name", "demo", getEngine().getGrammarName());

        ElementBlock tokensBlock = getEngine().getBlocks().get(0);
        assertEquals("tokens block", Arrays.asList("FOO", "OTHER", "LAST"), tokensBlock.getDeclaredTokensAsString());

        ElementBlock optionsBlock = getEngine().getBlocks().get(1);
        assertEquals("tokenVocab", "DataViewExpressions", optionsBlock.getTokenVocab());
    }

    public void testReferences() throws Exception {
        parseFile(TestConstants.REFERENCES);
        assertInspector(0);

        assertEquals("grammar name", "references", getEngine().getGrammarName());

        assertEquals("declarations", Arrays.asList("FOO", "OTHER", "LAST", "rule_a", "BAR"), getDeclsAsString(getEngine().getDecls()));
        assertEquals("references", Arrays.asList("FOO", "BAR", "OTHER"), getRefsAsString(getEngine().getReferences()));
    }

    public void testMantra() throws Exception {
        parseFile(TestConstants.MANTRA);
        assertInspector(0);

        assertParserProperties(65, 32, 30, 115, 274); // verified by hand
    }

    public void testCodeGenPhase() throws Exception {
        parseFile(TestConstants.CODE_GEN_PHASE);
        assertInspector(76);

        assertParserProperties(40, 18, 7, 40, 199); // verified by hand

        // now add the remaining token as if they were read from a tokenVocab file
        Set<String> names = new HashSet<String>();
        readTokenVocabFile(getResourceFile(TestConstants.PREFIX+"mantra/Mantra.tokens"), names);
        getSyntaxEngine().resolveReferencesWithExternalNames(names);

        // update the engine
        getEngine().parserCompleted();
        getEngine().updateAll();

        assertParserProperties(40, 18, 7, 40, 199+4); // verified by hand

        assertInspector(0);
    }

    public void testResolvePhase() throws Exception {
        parseFile(TestConstants.RESOLVE_PHASE);
        assertInspector(69);

        Set<String> names = new HashSet<String>();
        readTokenVocabFile(getResourceFile(TestConstants.PREFIX+"mantra/Mantra.tokens"), names);
        getSyntaxEngine().resolveReferencesWithExternalNames(names);

        // update the engine
        getEngine().parserCompleted();
        getEngine().updateAll();

        assertParserProperties(36, 14, 9, 36, 170); // verified by hand
        assertInspector(0);
    }

    public void testSemanticPhase() throws Exception {
        parseFile(TestConstants.SEMANTIC_PHASE);
        assertInspector(69);

        Set<String> names = new HashSet<String>();
        readTokenVocabFile(getResourceFile(TestConstants.PREFIX+"mantra/Mantra.tokens"), names);
        getSyntaxEngine().resolveReferencesWithExternalNames(names);

        // update the engine
        getEngine().parserCompleted();
        getEngine().updateAll();

        assertParserProperties(36, 37, 23, 36, 177); // verified by hand
        assertInspector(0);
    }

    /*********************** HELPER ***************************************/

    private void printParserProperties() {
        System.out.println("Rules="+getEngine().getNumberOfRules());
        System.out.println("Actions="+getEngine().getActions().size());
        System.out.println("Blocks="+getEngine().getBlocks().size());
        System.out.println("Decls="+getEngine().getDecls().size());
        System.out.println("Refs="+getEngine().getReferences().size());
    }

    private void assertParserProperties(int rules, int actions, int blocks, int decls, int references) {
        assertEquals("Number of rules", rules, getEngine().getNumberOfRules());
        assertEquals("Number of actions", actions, getEngine().getActions().size());
        assertEquals("Number of blocks", blocks, getEngine().getBlocks().size());
        assertEquals("Number of declarations", decls, getEngine().getDecls().size());
        assertEquals("Number of references", references, getEngine().getReferences().size());
    }

    private void assertInspector(int errors) {
        EditorInspector inspector = new EditorInspector(getEngine(), null, new MockInspectorDelegate());
        assertEquals("Errors", errors, inspector.getErrors().size());
    }

    private List<String> getDeclsAsString(List<ATEToken> tokens) {
        List<String> names = new ArrayList<String>();
        for(ATEToken token : tokens) {
            names.add(token.getAttribute());
        }
        return names;
    }

    private List<String> getRefsAsString(List<ElementReference> tokens) {
        List<String> names = new ArrayList<String>();
        for(ElementReference token : tokens) {
            names.add(token.token.getAttribute());
        }
        return names;
    }

}
