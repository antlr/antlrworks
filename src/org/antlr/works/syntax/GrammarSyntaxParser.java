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
import org.antlr.works.ate.syntax.misc.ATEToken;

import java.util.ArrayList;
import java.util.List;

public class GrammarSyntaxParser extends ATESyntaxParser {

    public static final String BEGIN_GROUP = "// $<";
    public static final String END_GROUP = "// $>";

    public static final String TOKENS_BLOCK_NAME = "tokens";
    public static final String OPTIONS_BLOCK_NAME = "options";

    public static final List blockIdentifiers;
    public static final List ruleModifiers;
    public static final List keywords;
    public static final List predefinedReferences;

    public List rules;
    public List groups;
    public List blocks;         // tokens {}, options {}
    public List actions;        // { action } in rules
    public List references;
    public GrammarSyntaxName name;

    static {
        blockIdentifiers = new ArrayList();
        blockIdentifiers.add(OPTIONS_BLOCK_NAME);
        blockIdentifiers.add(TOKENS_BLOCK_NAME);

        ruleModifiers = new ArrayList();
        ruleModifiers.add("protected");
        ruleModifiers.add("public");
        ruleModifiers.add("private");
        ruleModifiers.add("fragment");

        keywords = new ArrayList();
        keywords.addAll(blockIdentifiers);
        keywords.addAll(ruleModifiers);
        keywords.add("returns");
        keywords.add("init");

        predefinedReferences = new ArrayList();
        predefinedReferences.add("EOF");
    }

    public GrammarSyntaxParser() {
    }

    public void parseTokens() {
        rules = new ArrayList();
        groups = new ArrayList();
        blocks = new ArrayList();
        actions = new ArrayList();
        references = new ArrayList();

        while(nextToken()) {

            if(tryMatchName())
                continue;

            if(tryMatchPlainAction())
                continue;

            if(tryMatchBlock())
                continue;

            if(tryMatchScope())
                continue;

            if(isID(0)) {
                GrammarSyntaxRule rule = matchRule(actions, references);
                if(rule != null)
                    rules.add(rule);
            } else if(isSingleComment(0)) {
                GrammarSyntaxGroup group = matchRuleGroup(rules);
                if(group != null)
                    groups.add(group);
            }
        }
    }

    public boolean tryMatchName() {
        mark();

        GrammarSyntaxName n = matchName();
        if(n != null) {
            name = n;
            return true;
        } else {
            rewind();
            return false;
        }
    }

    public boolean tryMatchPlainAction() {
        mark();
        if(matchPlainAction() != null)
            return true;
        else {
            rewind();
            return false;
        }
    }

    public boolean tryMatchBlock() {
        mark();
        GrammarSyntaxBlock block = matchBlock();
        if(block != null) {
            blocks.add(block);
            return true;
        } else {
            rewind();
            return false;
        }
    }

    public boolean tryMatchScope() {
        mark();
        if(matchScope())
            return true;
        else {
            rewind();
            return false;
        }
    }

    public GrammarSyntaxName matchName() {
        ATEToken start = T(0);

        if(!isID(0))
            return null;

        ATEToken type = null;
        if(GrammarSyntaxName.isKnownType(T(0).getAttribute())) {
            // Skip the grammar specifier
            type = T(0);
            nextToken();
        }

        if(isID(0, "grammar")) {
            if(type != null && type.type == ATESyntaxLexer.TOKEN_ID)
                type.type = ATESyntaxLexer.TOKEN_OTHER;

            ATEToken name = T(1);

            while(nextToken()) {
                if(isSEMI(0))
                    return new GrammarSyntaxName(name, start, T(0), type);
            }
            return null;
        } else
            return null;
    }

    public boolean matchScope() {
        if(!isID(0, "scope"))
            return false;

        while(nextToken()) {
            if(isBLOCK(0) || isSEMI(0))
                return true;
        }

        return false;
    }

    public GrammarSyntaxPlainAction matchPlainAction() {
        ATEToken start = T(0);

        if(!isID(0))
            return null;

        if(!start.getAttribute().startsWith("@"))
            return null;

        while(nextToken()) {
            if(isBLOCK(0))
                return new GrammarSyntaxPlainAction(start.getAttribute(), start, T(0));
        }
        return null;
    }

    public GrammarSyntaxBlock matchBlock() {
        ATEToken start = T(0);

        if(!isID(0))
            return null;

        String blockName = start.getAttribute().toLowerCase();
        if(blockIdentifiers.contains(blockName)) {
            if(!isBLOCK(1))
                return null;

            nextToken();
            GrammarSyntaxBlock block = new GrammarSyntaxBlock(start.getAttribute(), start, T(0));
            if(blockName.equals(TOKENS_BLOCK_NAME)) {
                block.isTokenBlock = true;
            } else if(blockName.equals(OPTIONS_BLOCK_NAME)) {
                block.isOptionsBlock = true;
                block.parseOptionsBlock();
            }

            return block;
        } else
            return null;
    }

    public GrammarSyntaxRule matchRule(List actions, List references) {
        // ("protected" | "public" | "private" | "fragment")? rulename_id '!'?
        // ARG_ACTION? ("returns" ARG_ACTION)? throwsSpec? optionsSpec? ruleScopeSpec?
        // ("@init" ACTION)? ":" ... ";"
        // exceptionGroup etc... (see antlr.g)

        ATEToken start = T(0);

        // Match any modifiers
        if(ruleModifiers.contains(T(0).getAttribute())) {
            if(!nextToken())
                return null;
        }

        // Match the name
        ATEToken tokenName = T(0);
        String name = T(0).getAttribute();
        if(!nextToken())
            return null;

        // Match any optional "!"
        if(T(0).getAttribute().equals("!")) {
            if(!nextToken())
                return null;
        }

        // Match any comments
        while(isSingleComment(0) || isComplexComment(0)) {
            if(!nextToken())
                return null;
        }

        // Match any scope statement
        matchScope();

        // Loop until a COLON is found (defining the beginning of the body of the rule)
        if(!isCOLON(0)) {
            boolean colonFound = false;
            while(nextToken()) {
                if(matchScope())
                    continue;

                if(matchPlainAction() != null)
                    continue;

                if(isSEMI(0))
                    break;

                if(isCOLON(0)) {
                    colonFound = true;
                    break;
                }
            }
            if(!colonFound)
                return null;
        }

        ATEToken colonToken = T(0);
        GrammarSyntaxRule rule = new GrammarSyntaxRule(this, name, start, colonToken, null);
        int refOldSize = references.size();
        while(nextToken()) {
            switch(T(0).type) {
                case GrammarSyntaxLexer.TOKEN_SEMI:
                    matchRuleExceptionGroup();
                    rule.end = T(0);
                    tokenName.type = GrammarSyntaxLexer.TOKEN_RULE;
                    if(references.size() > refOldSize)
                        rule.setReferencesIndexes(refOldSize, references.size()-1);
                    rule.completed();
                    return rule;

                case ATESyntaxLexer.TOKEN_ID: {
                    // Match also all references inside the rule

                    // First, skip any label
                    boolean skipLabel = false;

                    if(isChar(1, "="))
                        skipLabel = true;

                    if(isChar(1, "+") && isChar(2, "="))
                        skipLabel = true;

                    if(skipLabel) {
                        T(0).type = GrammarSyntaxLexer.TOKEN_LABEL;
                        continue;
                    }

                    // Skip also reserved keywords
                    if(keywords.contains(T(0).getAttribute()))
                        continue;

                    // Set the token flags
                    T(0).type = GrammarSyntaxLexer.TOKEN_REFERENCE;

                    // Add the new reference
                    references.add(new GrammarSyntaxReference(rule, T(0)));
                    break;
                }

                case GrammarSyntaxLexer.TOKEN_BLOCK: {
                    // Match also all actions inside the rule
                    ATEToken t = T(-1);
                    if(t == null || !blockIdentifiers.contains(t.getAttribute().toLowerCase())) {
                        // An action is a block not preceeded by a block identifier
                        actions.add(new GrammarSyntaxAction(rule, T(0), actions.size()));
                    }
                    break;
                }

                case ATESyntaxLexer.TOKEN_CHAR: {
                    if(isChar(0, "-") && isChar(1, ">")) {
                        skip(2);
                        matchRewriteSyntax();
                    } else if(isChar(0, "$")) {
                        skip(1);
                    }
                }
            }
        }
        return null;
    }

    public void matchRuleExceptionGroup() {
        if(!matchOptional("exception"))
            return;

        // Optional ARG_ACTION
        if(isBLOCK(1))
            nextToken();

        while(matchOptional("catch")) {
            nextToken();    // ARG_ACTION: []
            nextToken();    // ACTION: { }
        }
    }

    public void matchRewriteSyntax() {
        if(isBLOCK(0) && isChar(1, "?") && isID(2) && isLPAREN(3)) {
            // -> { condition }? foo()
            skip(3);
            matchBalancedToken("(", ")");
            return;
        }

        if(isID(0) && isLPAREN(1)) {
            // -> foo(...)
            skip(1);
            matchBalancedToken("(", ")");
            return;
        }

        if(isBLOCK(0)) {
            // -> { new StringTemplate() }
            skip(0);
            return;
        }

        if(isID(0)) {
            // -> ASSIGN
            // Rewind one token because the next ID should not be skipped
            // otherwise it is not colored
            previousToken();
            return;
        }

        if(isChar(0, "$") && isID(1)) {
            // -> $e
            skip(1);
            return;
        }

        // Fall back if there is nothing after the rewrite ->
        previousToken();
    }

    public void matchBalancedToken(String open, String close) {
        // Try to match all tokens until the balanced token's attribute
        // is equal to close

        int balance = 0;
        while(nextToken()) {
            String attr = T(0).getAttribute();
            if(attr.equals(open))
                balance++;
            else if(attr.equals(close)) {
                if(balance == 0)
                    break;
                balance--;
            }
        }
    }

    public GrammarSyntaxGroup matchRuleGroup(List rules) {
        ATEToken token = T(0);
        String comment = token.getAttribute();

        if(comment.startsWith(BEGIN_GROUP)) {
            return new GrammarSyntaxGroup(comment.substring(BEGIN_GROUP.length(), comment.length()-1), rules.size()-1, token);
        } else if(comment.startsWith(END_GROUP)) {
                return new GrammarSyntaxGroup(rules.size()-1, token);
        } else
            return null;
    }

    public boolean matchOptional(String t) {
        if(isID(1, t)) {
            nextToken();
            return true;
        } else
            return false;
    }

    public boolean isLPAREN(int index) {
        return isTokenType(index, GrammarSyntaxLexer.TOKEN_LPAREN);
    }

    public boolean isSEMI(int index) {
        return isTokenType(index, GrammarSyntaxLexer.TOKEN_SEMI);
    }

    public boolean isCOLON(int index) {
        return isTokenType(index, GrammarSyntaxLexer.TOKEN_COLON);
    }

    public boolean isBLOCK(int index) {
        return isTokenType(index, GrammarSyntaxLexer.TOKEN_BLOCK);
    }

    public static List parsePropertiesString(final String content) {

        class ParseProperties extends ATESyntaxParser {

            public List propertiesTokens;

            public void parseTokens() {
                propertiesTokens = new ArrayList();
                while(nextToken()) {
                    if(T(0).type == ATESyntaxLexer.TOKEN_ID) {
                        if(isChar(1, "=") || isChar(1, "\n"))
                            propertiesTokens.add(T(0));
                    }
                }
            }

        }

        GrammarSyntaxLexer lexer = new GrammarSyntaxLexer();
        lexer.tokenize(content);

        ParseProperties parser = new ParseProperties();
        parser.parse(lexer.getTokens());
        return parser.propertiesTokens;
    }

}
