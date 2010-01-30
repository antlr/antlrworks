package org.antlr.works.stringtemplate.syntax;

import org.antlr.works.ate.syntax.generic.ATESyntaxEngine;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.generic.ATESyntaxParser;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stringtemplate.element.ElementTemplateArgumentBlock;
import org.antlr.works.stringtemplate.element.ElementTemplateCommentScope;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.util.HashSet;
import java.util.Set;

/*

[The "BSD licence"]
Copyright (c) 2009
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

public class ATEStringTemplateSyntaxEngine extends ATESyntaxEngine {

    protected SimpleAttributeSet blockAttr;
    protected SimpleAttributeSet labelAttr;
    private SimpleAttributeSet templateAttr;

    private static final Set<String> s;

    static {
        s = new HashSet<String>();
        s.add("default");
        s.add("first");
        s.add("group");
        s.add("if");
        s.add("implements");
        s.add("interface");
        s.add("last");
        s.add("length");
        s.add("optional");
        s.add("rest");
        s.add("strip");
        s.add("super");
        s.add("trunc");
        s.add("else");
        s.add("endif");
        s.add("elseif");
        s.add("separator");
        s.add("it");
    }

    public ATEStringTemplateSyntaxEngine() {
        super();
        blockAttr = new SimpleAttributeSet();
        labelAttr = new SimpleAttributeSet();
        templateAttr = new SimpleAttributeSet();
    }

    @Override
    public ATESyntaxLexer createLexer() {
        return new ATEStringTemplateSyntaxLexer();
    }

    @Override
    public ATESyntaxParser createParser() {
        return new ATEStringTemplateSyntaxParser();
    }

    public Set<String> getKeywords() {
        return s;
    }

//    private void applyBlockAttribute(SimpleAttributeSet attr) {
//        StyleConstants.setForeground(attr, Color.BLUE);
//    }
//
//    public void applyKeywordAttribute(SimpleAttributeSet keywordAttr) {
//        StyleConstants.setForeground(keywordAttr, Color.RED);
//        StyleConstants.setBold(keywordAttr, true);
//    }
//
//    public void applyCommentAttribute(SimpleAttributeSet commentAttr) {
//        StyleConstants.setForeground(commentAttr, new Color(0, 0.5f, 0));
//        StyleConstants.setItalic(commentAttr, true);
//    }
//
    private void applyAttribute(SimpleAttributeSet attr, String identifier) {
        StyleConstants.setForeground(attr, AWPrefs.getSyntaxColor(identifier));
        StyleConstants.setBold(attr, AWPrefs.getSyntaxBold(identifier));
        StyleConstants.setItalic(attr, AWPrefs.getSyntaxItalic(identifier));
    }

    @Override
    public void refreshColoring() {
        super.refreshColoring();

        applyAttribute(labelAttr, AWPrefs.PREF_SYNTAX_LABEL);
        applyAttribute(blockAttr, AWPrefs.PREF_SYNTAX_BLOCK);
        applyAttribute(keywordAttr, AWPrefs.PREF_SYNTAX_KEYWORD);
        applyAttribute(commentAttr, AWPrefs.PREF_SYNTAX_COMMENT);
        applyAttribute(templateAttr, AWPrefs.PREF_SYNTAX_PARSER);
    }

    public AttributeSet getAttributeForToken(ATEToken token) {
        AttributeSet attr = super.getAttributeForToken(token);
        if (token.scope instanceof ElementTemplateArgumentBlock) {
            if (attr == null)
                attr = labelAttr;
        }
        switch(token.type) {
            case ATEStringTemplateSyntaxLexer.TOKEN_DEFINED_TO_BE:
            case ATEStringTemplateSyntaxLexer.TOKEN_EQUAL:
            case ATEStringTemplateSyntaxLexer.TOKEN_OPEN_DOUBLE_ANGLE:
            case ATEStringTemplateSyntaxLexer.TOKEN_CLOSE_DOUBLE_ANGLE:
            case ATEStringTemplateSyntaxLexer.TOKEN_OPEN_SINGLE_ANGLE:
            case ATEStringTemplateSyntaxLexer.TOKEN_CLOSE_SINGLE_ANGLE:
            case ATEStringTemplateSyntaxLexer.TOKEN_DOUBLE_QUOTE:
                attr = keywordAttr;
                break;
            case ATEStringTemplateSyntaxLexer.TOKEN_ANGLE_COMMENT:
            case ATEStringTemplateSyntaxLexer.TOKEN_DOLLAR_COMMENT:
                attr = commentAttr;
                break;
            case ATEStringTemplateSyntaxLexer.TOKEN_DECL:
            case ATEStringTemplateSyntaxLexer.TOKEN_REFERENCE:
            case ATEStringTemplateSyntaxLexer.TOKEN_MAP_DECL:
                attr = templateAttr;
                break;
            case ATEStringTemplateSyntaxLexer.TOKEN_ARG_REFERENCE:
                attr = labelAttr;
                break;
            case ATEStringTemplateSyntaxLexer.TOKEN_LITERAL:
                attr = null;
                break;
        }
        if (token.scope instanceof ElementTemplateCommentScope) {
            attr = commentAttr;
        }
        return attr;
    }
}
