package org.antlr.works.tests;

import edu.usfca.xj.foundation.XJUtils;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.RefactorEngine;
import org.antlr.works.grammar.RefactorMutator;
import org.antlr.works.syntax.ElementBlock;
import org.antlr.works.syntax.ElementReference;
import org.antlr.works.syntax.GrammarSyntaxLexer;
import org.antlr.works.syntax.GrammarSyntaxParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class TestsParser extends TestCase {

    GrammarSyntaxLexer lexer = new GrammarSyntaxLexer();
    GrammarSyntaxParser parser = new GrammarSyntaxParser();

    public static void main(String[] args) {
        new TestRunner().doRun(new TestSuite(TestsParser.class));
    }

    public void testSyntaxBlock() throws IOException {
        setup("blocks.txt");

        assertEquals("grammar name", "demo", parser.name.getName());

        ElementBlock tokensBlock = parser.blocks.get(0);
        assertEquals("tokens block", Arrays.asList("FOO", "OTHER", "LAST"), tokensBlock.getDeclaredTokensAsString());

        ElementBlock optionsBlock = parser.blocks.get(1);
        assertEquals("tokenVocab", "DataViewExpressions", optionsBlock.getTokenVocab());
    }

    public void testReferences() throws IOException {
        setup("references.txt");

        assertEquals("grammar name", "references", parser.name.getName());

        assertEquals("declarations", Arrays.asList("FOO", "OTHER", "LAST", "rule_a", "BAR"), getDeclsAsString(parser.decls));
        assertEquals("references", Arrays.asList("FOO", "BAR", "OTHER"), getRefsAsString(parser.references));
    }

    public void testRenameReference() throws IOException {
        setup("references.txt");

        assertEquals("grammar name", "references", parser.name.getName());

        String originalText = getTextFromFile("references.txt");

        TestRefactorMutator mutator = new TestRefactorMutator();
        RefactorEngine engine = new RefactorEngine();
        engine.setMutator(mutator);
        engine.setTokens(parser.getTokens());

        mutator.setText(originalText);
        engine.renameToken(parser.decls.get(1), "OTHER_2");

        assertEquals("rename OTHER -> OTHER_2", getTextFromFile("rename_other.txt"), mutator.getText());

        mutator.setText(originalText);
        engine.renameToken(parser.decls.get(4), "RAB");

        assertEquals("rename BAR -> RAB", getTextFromFile("rename_bar.txt"), mutator.getText());
    }

    private void setup(String fileName) throws IOException {
        lexer.tokenize(getTextFromFile(fileName));
        parser.parse(lexer.getTokens());
    }

    private String getTextFromFile(String fileName) throws IOException {
        return XJUtils.getStringFromFile(getClass().getResource(fileName).getFile());
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

    private class TestRefactorMutator implements RefactorMutator {
        public StringBuffer mutableText;

        public TestRefactorMutator() {
        }

        public void replace(int start, int end, String s) {
            mutableText.replace(start, end, s);
        }

        public void insert(int index, String s) {
            mutableText.insert(index, s);
        }

        public void delete(int start, int end) {
            mutableText.delete(start, end);
        }

        public void setText(String text) {
            mutableText = new StringBuffer(text);
        }

        public String getText() {
            return mutableText.toString();
        }
    }
    
}
