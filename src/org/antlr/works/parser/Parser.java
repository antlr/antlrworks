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

public class Parser {

    protected List tokens;
    protected int position;

    protected Token currentToken;

    public static final String BEGIN_GROUP = "// $<";
    public static final String END_GROUP = "// $>";

    public static final List blockIdentifiers;
    public static final List ruleModifiers;
    public static final List keywords;

    protected Lexer lexer;

    public List rules = null;
    public List groups = null;
    public List blocks = null;
    public List actions = null;
    public ParserName name = null;

    static {
        blockIdentifiers = new ArrayList();
        blockIdentifiers.add("options");
        blockIdentifiers.add("tokens");
        blockIdentifiers.add("header");

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

        lexer = new Lexer(text);
        tokens = lexer.parseTokens();
        position = -1;
        while(nextToken()) {
            ParserName n = matchName();
            if(n != null) {
                name = n;
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
                ParserRule rule = matchRule(actions);
                if(rule != null)
                    rules.add(rule);
            } else if(T(0).type == Lexer.TOKEN_SINGLE_COMMENT) {
                ParserGroup group = matchRuleGroup(rules);
                if(group != null)
                    groups.add(group);
            }
        }
    }

    public List getLines() {
        if(lexer == null)
            return null;
        else
            return lexer.lines;
    }

    public int getMaxLines() {
        if(lexer == null)
            return 0;
        else
            return lexer.line;
    }

    public ParserName matchName() {
        Token start = T(0);

        if(start.type != Lexer.TOKEN_ID)
            return null;

        if(start.getAttribute().equals("grammar")) {
            Token type = T(-1);
            if(type != null && type.type == Lexer.TOKEN_ID) {
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
            return new ParserBlock(start.getAttribute(), start, T(0));
        } else
            return null;
    }

    public ParserRule matchRule(List actions) {
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
        while(nextToken()) {
            // Match also all actions inside the rule
            if(T(0).type == Lexer.TOKEN_BLOCK) {
                Token t = T(-1);
                if(t == null || !blockIdentifiers.contains(t.getAttribute().toLowerCase())) {
                    // An action is a block not preceeded by a block identifier
                    actions.add(new ParserAction(name, T(0)));
                }
            }

            if(T(0).type == Lexer.TOKEN_SEMI)
                return new ParserRule(this, name, start, colonToken, T(0));
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

    public boolean nextToken() {
        position++;
        return position<tokens.size();
    }

    public Token T(int index) {
        if(position+index >= 0 && position+index < tokens.size())
            return (Token)tokens.get(position+index);
        else
            return null;
    }

}
