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

package org.antlr.works.grammar.syntax;

import org.antlr.works.ate.syntax.generic.ATESyntaxEngine;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.generic.ATESyntaxParser;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.element.ElementToken;
import org.antlr.works.prefs.AWPrefs;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.Set;

public class GrammarSyntaxEngine extends ATESyntaxEngine {

    public static final Color COLOR_PARSER = new Color(0.42f, 0, 0.42f);
    public static final Color COLOR_LEXER = new Color(0, 0, 0.5f);

    private SimpleAttributeSet parserRefAttr;
    private SimpleAttributeSet lexerRefAttr;
    private SimpleAttributeSet labelAttr;
    private SimpleAttributeSet actionRefAttr;
    private SimpleAttributeSet blockAttr;

    public GrammarSyntaxEngine() {
        parserRefAttr = new SimpleAttributeSet();
        lexerRefAttr = new SimpleAttributeSet();
        labelAttr = new SimpleAttributeSet();
        actionRefAttr = new SimpleAttributeSet();
        blockAttr = new SimpleAttributeSet();
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void applyCommentAttribute(SimpleAttributeSet commentAttr) {
        applyAttribute(commentAttr, AWPrefs.PREF_SYNTAX_COMMENT);
    }

    @Override
    public void applyStringAttribute(SimpleAttributeSet stringAttr) {
        applyAttribute(stringAttr, AWPrefs.PREF_SYNTAX_STRING);
    }

    @Override
    public void applyKeywordAttribute(SimpleAttributeSet keywordAttr) {
        applyAttribute(keywordAttr, AWPrefs.PREF_SYNTAX_KEYWORD);
    }

    private void applyAttribute(SimpleAttributeSet attr, String identifier) {
        StyleConstants.setForeground(attr, AWPrefs.getSyntaxColor(identifier));
        StyleConstants.setBold(attr, AWPrefs.getSyntaxBold(identifier));
        StyleConstants.setItalic(attr, AWPrefs.getSyntaxItalic(identifier));
    }

    @Override
    public ATESyntaxLexer createLexer() {
        return new GrammarSyntaxLexer();
    }

    @Override
    public ATESyntaxParser createParser() {
        return new GrammarSyntaxParser();
    }

    @Override
    public void refreshColoring() {
        super.refreshColoring();

        applyAttribute(parserRefAttr, AWPrefs.PREF_SYNTAX_PARSER);
        applyAttribute(lexerRefAttr, AWPrefs.PREF_SYNTAX_LEXER);
        applyAttribute(labelAttr, AWPrefs.PREF_SYNTAX_LABEL);
        applyAttribute(actionRefAttr, AWPrefs.PREF_SYNTAX_REFS);
        applyAttribute(blockAttr, AWPrefs.PREF_SYNTAX_BLOCK);

        StyleConstants.setBold(blockAttr, true);
    }

    @Override
    public AttributeSet getAttributeForToken(ATEToken token) {
        AttributeSet attr = super.getAttributeForToken(token);
        switch(token.type) {
            case GrammarSyntaxLexer.TOKEN_DECL:
            case GrammarSyntaxLexer.TOKEN_REFERENCE:
                if(((ElementToken)token).lexer)
                    attr = lexerRefAttr;
                else
                    attr = parserRefAttr;
                break;

            case GrammarSyntaxLexer.TOKEN_LABEL:
                attr = labelAttr;
                break;

            case GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT:
            case GrammarSyntaxLexer.TOKEN_BLOCK_LABEL:
                attr = blockAttr;
                break;

            case GrammarSyntaxLexer.TOKEN_INTERNAL_REF:
                attr = actionRefAttr;
                break;
        }
        return attr;
    }

    public void resolveReferencesWithExternalNames(Set<String> names) {
        ((GrammarSyntaxParser)getParser()).resolveReferencesWithExternalNames(names);
    }
}
