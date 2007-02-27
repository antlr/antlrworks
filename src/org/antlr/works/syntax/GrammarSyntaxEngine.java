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

import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.generic.ATESyntaxParser;
import org.antlr.works.ate.syntax.language.ATELanguageSyntaxEngine;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.syntax.element.*;

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
    public static final Color COLOR_BLOCK_LABEL = new Color(1.0f, 0.0f, 0.0f);

    protected List<ElementRule> rules;
    protected List<ElementGroup> groups;
    protected List<ElementBlock> blocks;
    protected List<ElementAction> actions;
    protected List<ElementReference> references;
    protected List<ATEToken> decls;

    protected ElementGrammarName name;

    protected SimpleAttributeSet parserRefAttr;
    protected SimpleAttributeSet lexerRefAttr;
    protected SimpleAttributeSet labelAttr;
    protected SimpleAttributeSet actionRefAttr;
    // todo prefs for that
    protected SimpleAttributeSet blockLabelAttr;

    public GrammarSyntaxEngine() {
        parserRefAttr = new SimpleAttributeSet();
        lexerRefAttr = new SimpleAttributeSet();
        labelAttr = new SimpleAttributeSet();
        actionRefAttr = new SimpleAttributeSet();
        blockLabelAttr = new SimpleAttributeSet();
    }

    public void applyCommentAttribute(SimpleAttributeSet commentAttr) {
        applyAttribute(commentAttr, AWPrefs.PREF_SYNTAX_COMMENT);
    }

    public void applyStringAttribute(SimpleAttributeSet stringAttr) {
        applyAttribute(stringAttr, AWPrefs.PREF_SYNTAX_STRING);
    }

    public void applyKeywordAttribute(SimpleAttributeSet keywordAttr) {
        applyAttribute(keywordAttr, AWPrefs.PREF_SYNTAX_KEYWORD);
    }

    public void applyAttribute(SimpleAttributeSet attr, String identifier) {
        StyleConstants.setForeground(attr, AWPrefs.getSyntaxColor(identifier));
        StyleConstants.setBold(attr, AWPrefs.getSyntaxBold(identifier));
        StyleConstants.setItalic(attr, AWPrefs.getSyntaxItalic(identifier));
    }

    public ATESyntaxLexer createLexer() {
        return new GrammarSyntaxLexer();
    }

    public ATESyntaxParser createParser() {
        return new GrammarSyntaxParser();
    }

    public void refreshColoring() {
        super.refreshColoring();

        applyAttribute(parserRefAttr, AWPrefs.PREF_SYNTAX_PARSER);
        applyAttribute(lexerRefAttr, AWPrefs.PREF_SYNTAX_LEXER);
        applyAttribute(labelAttr, AWPrefs.PREF_SYNTAX_LABEL);
        applyAttribute(actionRefAttr, AWPrefs.PREF_SYNTAX_REFS);

        StyleConstants.setBold(blockLabelAttr, true);
    }

    public AttributeSet getAttributeForToken(ATEToken token) {
        AttributeSet attr = super.getAttributeForToken(token);
        // todo check
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
                attr = blockLabelAttr;
                break;
        }
        return attr;
    }

    public static void setDelay(int delay) {
        GrammarSyntaxEngine.delay = delay;
    }

    public synchronized List<ElementRule> getRules() {
        return rules;
    }

    public synchronized List<ElementGroup> getGroups() {
        return groups;
    }

    public synchronized List<ElementBlock> getBlocks() {
        return blocks;
    }

    public synchronized List<ElementAction> getActions() {
        return actions;
    }

    public synchronized List<ElementReference> getReferences() {
        return references;
    }

    public synchronized List<ATEToken> getDecls() {
        return decls;
    }

    public synchronized ElementGrammarName getName() {
        return name;
    }

    public synchronized List<String> getDeclaredTokenNames() {
        List<String> names = new ArrayList<String>();
        if(blocks != null) {
            for(int index=0; index<blocks.size(); index++) {
                ElementBlock block = blocks.get(index);
                if(block.isTokenBlock) {
                    names.addAll(block.getDeclaredTokensAsString());
                }
            }
        }
        return names;
    }

    public List<String> getPredefinedReferences() {
        return GrammarSyntaxParser.predefinedReferences;
    }

    public synchronized String getTokenVocab() {
        if(blocks == null)
            return null;

        for(int index=0; index<blocks.size(); index++) {
            ElementBlock block = blocks.get(index);
            if(block.isOptionsBlock)
                return block.getTokenVocab();
        }
        return null;
    }

    public synchronized List<String> getRuleNames() {
        List<String> names = new ArrayList<String>();
        if(rules != null) {
            for (int index=0; index<rules.size(); index++) {
                ElementRule rule = rules.get(index);
                names.add(rule.name);
            }
        }
        return names;
    }

    public synchronized ElementRule getRuleAtIndex(int index) {
        if(index < 0 || index >= rules.size())
            return null;
        else
            return rules.get(index);
    }

    /** Cache the attributes of the parser so we can use them later
     * even if the parser is running again.
     */

    protected synchronized void parserDidRun(ATESyntaxParser parser) {
        GrammarSyntaxParser gp = (GrammarSyntaxParser)parser;
        this.rules = new ArrayList<ElementRule>(gp.rules);
        this.groups = new ArrayList<ElementGroup>(gp.groups);
        this.blocks = new ArrayList<ElementBlock>(gp.blocks);
        this.actions = new ArrayList<ElementAction>(gp.actions);
        this.references = new ArrayList<ElementReference>(gp.references);
        this.decls = new ArrayList<ATEToken>(gp.decls);
        this.name = gp.name;
    }

}
