package org.antlr.works.test.ut;

import org.antlr.works.grammar.RefactorEngine;
import org.antlr.works.grammar.RefactorMutator;
import org.antlr.works.test.AbstractTest;
import org.antlr.works.test.TestConstants;
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

public class TestRefactor extends AbstractTest {

    public void testRename_1() throws Exception {
        parseFile(TestConstants.REFACTOR_ORIGINAL_A);

        assertEquals("grammar name", "references", getEngine().getGrammarName());

        String originalText = getTextFromFile(TestConstants.REFACTOR_ORIGINAL_A);

        TestRefactorMutator mutator = new TestRefactorMutator();
        RefactorEngine engine = new RefactorEngine();
        engine.setMutator(mutator);
        engine.setTokens(getEngine().getTokens());

        mutator.setText(originalText);
        engine.renameToken(getEngine().getDecls().get(1), "OTHER_2");

        assertEquals("rename OTHER -> OTHER_2", getTextFromFile(TestConstants.RENAME_OTHER), mutator.getText());

        mutator.setText(originalText);
        engine.renameToken(getEngine().getDecls().get(4), "RAB");

        assertEquals("rename BAR -> RAB", getTextFromFile(TestConstants.RENAME_BAR), mutator.getText());
    }

    public void testRename_2() throws Exception {
        parseFile(TestConstants.REFACTOR_ORIGINAL_B);

        assertEquals("grammar name", "test", getEngine().getGrammarName());

        String originalText = getTextFromFile(TestConstants.REFACTOR_ORIGINAL_B);

        TestRefactorMutator mutator = new TestRefactorMutator();
        RefactorEngine engine = new RefactorEngine();
        engine.setMutator(mutator);
        engine.setTokens(getEngine().getTokens());

        mutator.setText(originalText);
        engine.renameToken(getEngine().getDecls().get(0), "foo");

        assertEquals("rename n_expression -> foo", getTextFromFile(TestConstants.RENAME_B), mutator.getText());
    }

    private class TestRefactorMutator implements RefactorMutator {
        public StringBuilder mutableText;

        public TestRefactorMutator() {
        }

        public void replace(int start, int end, String s) {
            mutableText.replace(start, end, s);
        }

        public void insert(int index, String s) {
            mutableText.insert(index, s);
        }

        public void insertAtLinesBoundary(int index, String s) {
            mutableText.insert(index, s);
        }

        public void delete(int start, int end) {
            mutableText.delete(start, end);
        }

        public void setText(String text) {
            mutableText = new StringBuilder(text);
        }

        public String getText() {
            return mutableText.toString();
        }
    }

}
