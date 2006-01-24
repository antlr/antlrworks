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

package org.antlr.works.syntax;

import org.antlr.works.ate.swing.ATEStyledDocument;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.generic.ATESyntaxParser;
import org.antlr.works.ate.syntax.language.ATELanguageSyntaxEngine;
import org.antlr.works.ate.syntax.misc.ATEToken;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GrammarSyntaxEngine extends ATELanguageSyntaxEngine {

    public static final Color COLOR_PARSER = new Color(0.42f, 0, 0.42f);
    public static final Color COLOR_LEXER = new Color(0, 0, 0.5f);
    public static final Color COLOR_ACTION_REF = new Color(1.0f, 0.0f, 0.0f);

    protected List rules;
    protected List groups;
    protected List blocks;
    protected List actions;
    protected List references;

    protected GrammarSyntaxName name;

    protected SimpleAttributeSet parserRefAttr;
    protected SimpleAttributeSet lexerRefAttr;
    protected SimpleAttributeSet labelAttr;
    protected SimpleAttributeSet actionRefAttr;

    public GrammarSyntaxEngine() {
        parserRefAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(parserRefAttr, COLOR_PARSER);
        StyleConstants.setBold(parserRefAttr, true);

        lexerRefAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(lexerRefAttr, COLOR_LEXER);
        StyleConstants.setBold(lexerRefAttr, true);

        labelAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(labelAttr, Color.black);
        StyleConstants.setItalic(labelAttr, true);

        actionRefAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(actionRefAttr, COLOR_ACTION_REF);
        StyleConstants.setItalic(actionRefAttr, true);
    }

    public ATESyntaxLexer createLexer() {
        return new GrammarSyntaxLexer();
    }

    public ATESyntaxParser createParser() {
        return new GrammarSyntaxParser();
    }

    public AttributeSet getAttributeForToken(ATEToken token) {
        AttributeSet attr = super.getAttributeForToken(token);
        switch(token.type) {
            case GrammarSyntaxLexer.TOKEN_REFERENCE:
            case GrammarSyntaxLexer.TOKEN_RULE:
                if(((GrammarSyntaxToken)token).lexer)
                    attr = lexerRefAttr;
                else
                    attr = parserRefAttr;
                break;

            case GrammarSyntaxLexer.TOKEN_LABEL:
                attr = labelAttr;
                break;
        }
        return attr;
    }

    public void colorizeToken(ATEToken token, ATEStyledDocument doc) {
        super.colorizeToken(token, doc);

        /** Colorize now all internal tokens. We have to do
         * the colorization here by hand because they are internal tokens
         * not included in the global stream of tokens.
         */

        if(token.type != GrammarSyntaxLexer.TOKEN_BLOCK)
            return;

        GrammarSyntaxToken t = (GrammarSyntaxToken)token;
        List internalTokens = t.internalTokens;
        if(internalTokens == null || internalTokens.isEmpty())
            return;

        for(int i=0; i<internalTokens.size(); i++) {
            ATEToken it = (ATEToken)internalTokens.get(i);

            AttributeSet attr;
            if(it.getAttribute().startsWith("$"))
                attr = actionRefAttr;
            else
                attr = getAttributeForToken(it);

            if(attr != null)
                doc.setCharacterAttributes(it.getStartIndex(),
                                            it.getEndIndex()-it.getStartIndex(),
                                            attr, false);
        }

    }

    public static void setDelay(int delay) {
        GrammarSyntaxEngine.delay = delay;
    }

    public synchronized List getRules() {
        return rules;
    }

    public synchronized List getGroups() {
        return groups;
    }

    public synchronized List getBlocks() {
        return blocks;
    }

    public synchronized List getActions() {
        return actions;
    }

    public synchronized List getReferences() {
        return references;
    }

    public synchronized GrammarSyntaxName getName() {
        return name;
    }

    public synchronized List getDeclaredTokenNames() {
        List names = new ArrayList();
        if(blocks != null) {
            for(int index=0; index<blocks.size(); index++) {
                GrammarSyntaxBlock block = (GrammarSyntaxBlock)blocks.get(index);
                if(block.isTokenBlock) {
                    List internalTokens = block.getInternalTokens();
                    for(int t=0; t<internalTokens.size(); t++) {
                        ATEToken token = (ATEToken)internalTokens.get(t);
                        names.add(token.getAttribute());
                    }
                }
            }
        }
        return names;
    }

    public List getPredefinedReferences() {
        return GrammarSyntaxParser.predefinedReferences;
    }

    public synchronized String getTokenVocab() {
        if(blocks == null)
            return null;

        for(int index=0; index<blocks.size(); index++) {
            GrammarSyntaxBlock block = (GrammarSyntaxBlock)blocks.get(index);
            if(block.isOptionsBlock)
                return block.getTokenVocab();
        }
        return null;
    }

    public synchronized List getRuleNames() {
        List names = new ArrayList();
        if(rules != null) {
            for (int index=0; index<rules.size(); index++) {
                GrammarSyntaxRule rule = (GrammarSyntaxRule) rules.get(index);
                names.add(rule.name);
            }
        }
        return names;
    }

    public synchronized GrammarSyntaxRule getRuleAtIndex(int index) {
        if(index < 0 || index >= rules.size())
            return null;
        else
            return (GrammarSyntaxRule)rules.get(index);
    }

    /** Cache the attributes of the parser so we can use them later
     * even if the parser is running again.
     */

    protected synchronized void parserDidRun(ATESyntaxParser parser) {
        GrammarSyntaxParser gp = (GrammarSyntaxParser)parser;
        this.rules = new ArrayList(gp.rules);
        this.groups = new ArrayList(gp.groups);
        this.blocks = new ArrayList(gp.blocks);
        this.actions = new ArrayList(gp.actions);
        this.references = new ArrayList(gp.references);
        this.name = gp.name;
    }

}
