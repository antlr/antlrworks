package org.antlr.works.test.ut;

import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.syntax.GrammarSyntax;
import org.antlr.works.syntax.element.ElementBlock;
import org.antlr.works.syntax.element.ElementReference;
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

    public void testSyntaxBlock() throws Exception {
        parseFile(TestConstants.BLOCKS);

        assertEquals("grammar name", "demo", parser.name.getName());

        ElementBlock tokensBlock = parser.blocks.get(0);
        assertEquals("tokens block", Arrays.asList("FOO", "OTHER", "LAST"), tokensBlock.getDeclaredTokensAsString());

        ElementBlock optionsBlock = parser.blocks.get(1);
        assertEquals("tokenVocab", "DataViewExpressions", optionsBlock.getTokenVocab());
    }

    public void testReferences() throws Exception {
        parseFile(TestConstants.REFERENCES);

        assertEquals("grammar name", "references", parser.name.getName());

        assertEquals("declarations", Arrays.asList("FOO", "OTHER", "LAST", "rule_a", "BAR"), getDeclsAsString(parser.decls));
        assertEquals("references", Arrays.asList("FOO", "BAR", "OTHER"), getRefsAsString(parser.references));
    }

    public void testMantra() throws Exception {
        parseFile(TestConstants.MANTRA);
        // these number have been verified by hand
        assertParserProperties(65, 32, 28, 115, 274);
    }

    public void testCodeGenPhase() throws Exception {
        parseFile(TestConstants.CODE_GEN_PHASE);
        assertParserProperties(40, 18, 7, 40, 199); // verified by hand

        // now add the remaining token as if they were read from a tokenVocab file
        Set<String> names = new HashSet<String>();
        GrammarSyntax.readTokenVocabFromFile(getResourceFile(TestConstants.PREFIX+"mantra/Mantra.tokens"), names);
        parser.resolveReferencesWithExternalNames(names);
        assertParserProperties(40, 18, 7, 40, 199+4); // verified by hand
    }

    public void testResolvePhase() throws Exception {
        parseFile(TestConstants.RESOLVE_PHASE);

        Set<String> names = new HashSet<String>();
        GrammarSyntax.readTokenVocabFromFile(getResourceFile(TestConstants.PREFIX+"mantra/Mantra.tokens"), names);
        parser.resolveReferencesWithExternalNames(names);

        assertParserProperties(36, 14, 7, 36, 170); // verified by hand
    }

    public void testSemanticPhase() throws Exception {
        parseFile(TestConstants.SEMANTIC_PHASE);

        Set<String> names = new HashSet<String>();
        GrammarSyntax.readTokenVocabFromFile(getResourceFile(TestConstants.PREFIX+"mantra/Mantra.tokens"), names);
        parser.resolveReferencesWithExternalNames(names);

        assertParserProperties(36, 37, 23, 36, 177); // verified by hand
    }

    /*********************** HELPER ***************************************/

    private void printParserProperties() {
        System.out.println("Rules="+parser.rules.size());
        System.out.println("Actions="+parser.actions.size());
        System.out.println("Blocks="+parser.blocks.size());
        System.out.println("Decls="+parser.decls.size());
        System.out.println("Refs="+parser.references.size());
    }

    private void assertParserProperties(int rules, int actions, int blocks, int decls, int references) {
        assertEquals("Number of rules", rules, parser.rules.size());
        assertEquals("Number of actions", actions, parser.actions.size());
        assertEquals("Number of blocks", blocks, parser.blocks.size());
        assertEquals("Number of declarations", decls, parser.decls.size());
        assertEquals("Number of references", references, parser.references.size());
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
