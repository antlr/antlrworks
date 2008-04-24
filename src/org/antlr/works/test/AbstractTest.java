package org.antlr.works.test;

import junit.framework.TestCase;
import org.antlr.Tool;
import org.antlr.works.ate.syntax.generic.ATESyntaxEngineDelegate;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.grammar.syntax.GrammarSyntaxEngine;
import org.antlr.works.grammar.syntax.GrammarSyntaxParser;
import org.antlr.works.utils.Console;
import org.antlr.xjlib.foundation.XJUtils;

import java.io.IOException;
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

public class AbstractTest extends TestCase implements ATESyntaxEngineDelegate {

    protected GrammarSyntaxEngine engine = new GrammarSyntaxEngine();
    protected String text;

    public void parseFile(String fileName) throws IOException {
        this.text = getTextFromFile(fileName);

        engine.setDelegate(this);
        engine.process();
    }

    public ATESyntaxLexer getLexer() {
        return engine.getLexer();
    }

    public GrammarSyntaxParser getParser() {
        return (GrammarSyntaxParser) engine.getParser();
    }

    public void gotoToRule(String grammar, String name) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getResourceFile(String fileName) {
        return getClass().getResource(fileName).getFile();
    }

    public String getTextFromFile(String fileName) throws IOException {
        return XJUtils.getStringFromFile(getResourceFile(fileName));
    }

    public String getFileName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getText() {
        return text;
    }

    public Tool getANTLRTool() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Console getConsole() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void rulesChanged() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void antlrEngineGrammarDidAnalyze() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getTokenVocabFile(String tokenVocabName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public GrammarSyntaxEngine getSyntaxEngine() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void ateEngineWillParse() {

    }

    public void ateEngineDidParse() {
    }
}
