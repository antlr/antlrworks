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

package org.antlr.works.ate.syntax.generic;

import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.syntax.misc.ATEToken;

import javax.swing.text.AttributeSet;
import java.util.ArrayList;
import java.util.List;

public abstract class ATESyntaxEngine {

    protected ATEPanel textEditor;

    protected ATESyntaxLexer lexer;
    protected ATESyntaxParser parser;

    protected List tokens;

    protected static int delay = 250;

    public ATESyntaxEngine() {
        lexer = createLexer();
        parser = createParser();
    }

    public void setTextEditor(ATEPanel textEditor) {
        this.textEditor = textEditor;
    }

    public static void setDelay(int delay) {
        ATESyntaxEngine.delay = delay;
    }

    public synchronized List getTokens() {
        return tokens;
    }

    public synchronized List getLines() {
        return lexer.getLines();
    }

    public synchronized int getMaxLines() {
        return lexer.getLineNumber();
    }

    protected synchronized void lexerDidRun(ATESyntaxLexer lexer) {
    }

    protected synchronized void parserDidRun(ATESyntaxParser parser) {
    }

    public abstract ATESyntaxLexer createLexer();
    public abstract ATESyntaxParser createParser();
    public abstract AttributeSet getAttributeForToken(ATEToken token);

    public void refreshColoring() {

    }

    public void processSyntax() {
        // First run the lexer
        lexer.tokenize(textEditor.getTextPane().getText());
        tokens = new ArrayList(lexer.getTokens());
        lexerDidRun(lexer);

        // And then the parser if it exists
        if(parser != null) {
            parser.parse(tokens);
            parserDidRun(parser);
        }
    }

    public void process() {
        textEditor.ateEngineWillParse();
        processSyntax();
        textEditor.ateEngineDidParse();
    }

}
