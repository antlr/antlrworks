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

package org.antlr.works.parser;

import java.util.ArrayList;
import java.util.List;

public class Parser extends AbstractParser {

    public static final String BEGIN_GROUP = "// $<";
    public static final String END_GROUP = "// $>";

    public static final String TOKENS_BLOCK_NAME = "tokens";
    public static final String OPTIONS_BLOCK_NAME = "options";

    public static final List blockIdentifiers;
    public static final List ruleModifiers;
    public static final List keywords;

    public List rules;
    public List groups;
    public List blocks;         // tokens {}, options {}
    public List actions;        // { action } in rules
    public List plainActions;   // @scope::actionname {action}
    public List references;
    public ParserName name;

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
    }

    public Parser() {
    }

    public void parse(String text) {
        rules = new ArrayList();
        groups = new ArrayList();
        blocks = new ArrayList();
        actions = new ArrayList();
        plainActions = new ArrayList();
        references = new ArrayList();

        init(text);
        while(nextToken()) {
            ParserName n = matchName();
            if(n != null) {
                name = n;
                continue;
            }

            ParserPlainAction pa = matchPlainAction();
            if(pa != null) {
                plainActions.add(pa);
                continue;
            }

            ParserBlock block = matchBlock();
            if(block != null) {
                blocks.add(block);
                continue;
            }

            if(T(0) == null)
                continue;
            
            if(T(0).type == Lexer.TOKEN_ID) {
                ParserRule rule = matchRule(actions, references);
                if(rule != null)
                    rules.add(rule);
            } else if(T(0).type == Lexer.TOKEN_SINGLE_COMMENT) {
                ParserGroup group = matchRuleGroup(rules);
                if(group != null)
                    groups.add(group);
            }
        }
    }

    public ParserName matchName() {
        Token start = T(0);

        if(start.type != Lexer.TOKEN_ID)
            return null;

        if(start.getAttribute().equals("grammar")) {
            Token type = T(-1);
            if(type != null && type.type == Lexer.TOKEN_ID) {
                type.type = Lexer.TOKEN_COMPLEX_COMMENT;
                String typeString = type.getAttribute();
                if(!typeString.equals("lexer")
                        && !typeString.equals("parser")
                        && !typeString.equals("combined")
                        && !typeString.equals("treeparser"))
                {
                    type = null;
                }
            } else
                type = null;

            while(nextToken()) {
                if(T(0).type == Lexer.TOKEN_SEMI)
                    return new ParserName(start.getAttribute(), start, T(0), type);
            }
            return null;
        } else
            return null;
    }

    public ParserPlainAction matchPlainAction() {
        Token start = T(0);
        if(start == null)
            return null;

        if(start.type != Lexer.TOKEN_ID)
            return null;

        if(!start.getAttribute().startsWith("@"))
            return null;

        while(nextToken()) {
            if(T(0).type == Lexer.TOKEN_BLOCK)
                return new ParserPlainAction(start.getAttribute(), start, T(0));
        }
        return null;
    }

    public ParserBlock matchBlock() {
        Token start = T(0);
        if(start == null)
            return null;

        if(start.type != Lexer.TOKEN_ID)
            return null;

        if(blockIdentifiers.contains(start.getAttribute().toLowerCase())) {
            if(T(1) == null)
                return null;

            if(T(1).type != Lexer.TOKEN_BLOCK)
                return null;

            nextToken();
            ParserBlock block = new ParserBlock(start.getAttribute(), start, T(0));
            if(start.getAttribute().toLowerCase().equals(TOKENS_BLOCK_NAME))
                block.isTokenBlock = true;
            return block;
        } else
            return null;
    }

    public ParserRule matchRule(List actions, List references) {
        // ("protected" | "public" | "private" | "fragment")? rulename_id '!'?
        // ARG_ACTION? ("returns" ARG_ACTION)? throwsSpec? optionsSpec? ruleScopeSpec?
        // ("init" ACTION)? ":" ... ";"

        Token start = T(0);

        // Match any modifiers
        if(ruleModifiers.contains(T(0).getAttribute())) {
            if(!nextToken())
                return null;
        }

        // Match the name
        Token tokenName = T(0);
        String name = T(0).getAttribute();
        if(!nextToken())
            return null;

        // Match any optional "!"
        if(T(0).getAttribute().equals("!")) {
            if(!nextToken())
                return null;
        }

        // Match any comments
        while((T(0).type == Lexer.TOKEN_SINGLE_COMMENT || T(0).type == Lexer.TOKEN_COMPLEX_COMMENT)) {
            if(!nextToken())
                return null;
        }

        // Loop until a COLON is found (defining the beginning of the body of the rule)
        if(T(0).type != Lexer.TOKEN_COLON) {
            boolean colonFound = false;
            while(nextToken()) {
                if(T(0).type == Lexer.TOKEN_SEMI) {
                    break;
                }
                if(T(0).type == Lexer.TOKEN_COLON) {
                    colonFound = true;
                    break;
                }
            }
            if(!colonFound)
                return null;
        }

        Token colonToken = T(0);
        ParserRule rule = new ParserRule(this, name, start, colonToken, null);
        int refOldSize = references.size();
        while(nextToken()) {
            switch(T(0).type) {
                case Lexer.TOKEN_SEMI:
                    rule.end = T(0);
                    tokenName.isRule = true;                    
                    if(references.size() > refOldSize)
                        rule.setReferencesIndexes(refOldSize, references.size()-1);
                    rule.completed();
                    return rule;

                case Lexer.TOKEN_ID: {
                    // Match also all references inside the rule
                    Token t = T(1);
                    if(t != null && t.type == Lexer.TOKEN_CHAR && t.getAttribute().equals("=")) {
                        // Skip label
                        T(0).isLabel = true;
                        continue;
                    }

                    // Skip also reserved keywords
                    if(keywords.contains(T(0).getAttribute()))
                        continue;

                    // Set the token flags
                    T(0).isReference = true;

                    // Add the new reference
                    references.add(new ParserReference(rule, T(0)));
                    break;
                }

                case Lexer.TOKEN_BLOCK: {
                    // Match also all actions inside the rule
                    Token t = T(-1);
                    if(t == null || !blockIdentifiers.contains(t.getAttribute().toLowerCase())) {
                        // An action is a block not preceeded by a block identifier
                        actions.add(new ParserAction(rule, T(0), actions.size()));
                    }
                    break;
                }
            }
        }
        return null;
    }

    public ParserGroup matchRuleGroup(List rules) {
        Token token = T(0);
        String comment = token.getAttribute();

        if(comment.startsWith(BEGIN_GROUP)) {
            return new ParserGroup(comment.substring(BEGIN_GROUP.length(), comment.length()-1), rules.size()-1, token);
        } else if(comment.startsWith(END_GROUP)) {
                return new ParserGroup(rules.size()-1, token);
        } else
            return null;
    }

}
