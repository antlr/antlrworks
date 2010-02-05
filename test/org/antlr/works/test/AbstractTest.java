package org.antlr.works.test;

import junit.framework.TestCase;
import org.antlr.Tool;
import org.antlr.works.ate.syntax.generic.ATESyntaxEngineDelegate;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.works.grammar.engine.GrammarEngineDelegate;
import org.antlr.works.grammar.engine.GrammarEngineImpl;
import org.antlr.works.grammar.engine.GrammarPropertiesImpl;
import org.antlr.works.grammar.syntax.GrammarSyntaxEngine;
import org.antlr.works.utils.Console;
import org.antlr.works.utils.ConsoleHelper;
import org.antlr.xjlib.foundation.XJUtils;

import java.io.IOException;
import java.util.Set;
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

public abstract class AbstractTest extends TestCase {

    private GrammarEngine engine = new GrammarEngineImpl(new MyGrammarEngineDelegate());
    private String text;
    private String vocabFile;

    @Override
    protected void setUp() throws Exception {
        ConsoleHelper.setCurrent(new MyConsole());
    }

    public void parseFile(String fileName) throws IOException {
        this.text = getTextFromFile(fileName);
        engine.getSyntaxEngine().setDelegate(new MySyntaxEngineDelegate());
        engine.getSyntaxEngine().processSyntax();
        engine.parserCompleted();
        engine.updateAll();
    }

    public String getResourceFile(String fileName) {
        return getClass().getResource(fileName).getFile();
    }

    public String getTextFromFile(String fileName) throws IOException {
        return XJUtils.getStringFromFile(getResourceFile(fileName));
    }

    public String getText() {
        return text;
    }

    public GrammarEngine getEngine() {
        return engine;
    }

    public GrammarSyntaxEngine getSyntaxEngine() {
        return engine.getSyntaxEngine();
    }

    public void readTokenVocabFile(String resourceFile, Set<String> names) throws IOException {
        vocabFile = resourceFile;
        GrammarPropertiesImpl.readTokenVocabFromFile(vocabFile, names);
    }

    private class MyGrammarEngineDelegate implements GrammarEngineDelegate {

        public void engineAnalyzeCompleted() {

        }

        public Tool getANTLRTool() {
            return null;
        }

        public String getGrammarFileName() {
            return null;
        }

        public String getGrammarText() {
            return getText();
        }

        public String getTokenVocabFile(String name) {
            return vocabFile;
        }

        public void gotoToRule(String grammar, String name) {

        }

        public void reportError(Exception e) {
            e.printStackTrace();
        }

        public void reportError(String error) {
            System.err.println(error);
        }
    }

    private class MySyntaxEngineDelegate implements ATESyntaxEngineDelegate {

        public void ateEngineBeforeParsing() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void ateEngineAfterParsing() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getText() {
            return text;
        }
    }

    private class MyConsole implements Console {

        public void setMode(int mode) {
        }

        public void println(String s) {
            System.out.println(s);
        }

        public void println(String s, int level) {
            System.out.println(s);
        }

        public void println(Throwable e) {
            System.out.println(e.toString());
        }

        public void print(String string, int level) {
            System.out.println(string);
        }

        public void print(Throwable e) {
            System.out.println(e.toString());
        }
    }
}
