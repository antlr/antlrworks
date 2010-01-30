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

import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.ate.syntax.misc.ATEToken;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class ATESyntaxEngine {

    protected ATESyntaxEngineDelegate delegate;

    protected ATESyntaxLexer lexer;
    protected ATESyntaxParser parser;

    protected List<ATEToken> tokens;
    protected SimpleAttributeSet commentAttr;
    protected SimpleAttributeSet stringAttr;
    protected SimpleAttributeSet keywordAttr;

    public ATESyntaxEngine() {
        lexer = createLexer();
        parser = createParser();
        stringAttr = new SimpleAttributeSet();
        keywordAttr = new SimpleAttributeSet();
        commentAttr = new SimpleAttributeSet();
    }

    public void close() {
        parser.close();
        parser = null;
        lexer.close();
        lexer = null;
        delegate = null;
    }

    public void setDelegate(ATESyntaxEngineDelegate delegate) {
        this.delegate = delegate;
    }

    public synchronized List<ATEToken> getTokens() {
        return tokens;
    }

    public synchronized List<ATELine> getLines() {
        return lexer.getLines();
    }

    public synchronized int getMaxLines() {
        return lexer.getLineNumber();
    }

    public ATESyntaxLexer getLexer() {
        return lexer;
    }

    public ATESyntaxParser getParser() {
        return parser;
    }

    public ATESyntaxLexer createLexer() {
        return new ATESyntaxLexer();
    }

    public ATESyntaxParser createParser() {
        // No parser need for generic computer language
        return null;
    }

    public AttributeSet getAttributeForToken(ATEToken token) {
        AttributeSet attr = null;
        switch(token.type) {
            case ATESyntaxLexer.TOKEN_COMPLEX_COMMENT:
            case ATESyntaxLexer.TOKEN_SINGLE_COMMENT:
                attr = commentAttr;
                break;
            case ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING:
            case ATESyntaxLexer.TOKEN_SINGLE_QUOTE_STRING:
                attr = stringAttr;
                break;
            default:
                Set<String> s = getKeywords();
                if(s != null && s.contains(token.getAttribute()))
                    attr = keywordAttr;
                break;
        }
        return attr;
    }

    public void processSyntax() {
        // First run the lexer
        lexer.tokenize(delegate.getText());
        tokens = new ArrayList<ATEToken>(lexer.getTokens());

        // And then the parser if it exists
        if(parser != null) {
            parser.parse(tokens);
        }
    }

    public void process() {
        delegate.ateEngineBeforeParsing();
        processSyntax();
        delegate.ateEngineAfterParsing();
    }

    public void applyCommentAttribute(SimpleAttributeSet commentAttr) {
        StyleConstants.setForeground(commentAttr, Color.lightGray);
        StyleConstants.setItalic(commentAttr, true);
    }

    public void applyStringAttribute(SimpleAttributeSet stringAttr) {
        StyleConstants.setForeground(stringAttr, new Color(0, 0.5f, 0));
        StyleConstants.setBold(stringAttr, true);
    }

    public void applyKeywordAttribute(SimpleAttributeSet keywordAttr) {
        StyleConstants.setForeground(keywordAttr, new Color(0, 0, 0.5f));
        StyleConstants.setBold(keywordAttr, true);
    }

    public void refreshColoring() {
        applyCommentAttribute(commentAttr);
        applyStringAttribute(stringAttr);
        applyKeywordAttribute(keywordAttr);
    }

    /** Returns the set of keyword for the language.
     * Note: this method is called very often
     *
     * @return The set of keywords
     */
    public Set<String> getKeywords() {
        return null;
    }
}
