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
import org.antlr.works.ate.syntax.misc.ATEScope;
import org.antlr.works.ate.syntax.misc.ATEToken;

import java.util.*;

public class GrammarSyntaxParser extends ATESyntaxParser {

    private static final ElementRewriteBlock REWRITE_BLOCK = new ElementRewriteBlock();
    private static final ElementRewriteFunction REWRITE_FUNCTION = new ElementRewriteFunction();

    public static final String BEGIN_GROUP = "// $<";
    public static final String END_GROUP = "// $>";

    public static final String TOKENS_BLOCK_NAME = "tokens";
    public static final String OPTIONS_BLOCK_NAME = "options";
    public static final String PARSER_HEADER_BLOCK_NAME = "@header";
    public static final String LEXER_HEADER_BLOCK_NAME = "@lexer::header";
    public static final String PARSER_MEMBERS_BLOCK_NAME = "@members";
    public static final String LEXER_MEMBERS_BLOCK_NAME = "@lexer::members";

    public static final List<String> blockIdentifiers;
    public static final List<String> ruleModifiers;
    public static final List<String> keywords;
    public static final List<String> predefinedReferences;

    public List<ElementRule> rules = new ArrayList<ElementRule>();
    public List<ElementGroup> groups = new ArrayList<ElementGroup>();
    public List<ElementBlock> blocks = new ArrayList<ElementBlock>();         // tokens {}, options {}
    public List<ElementAction> actions = new ArrayList<ElementAction>();        // { action } in rules
    public List<ElementReference> references = new ArrayList<ElementReference>();
    public List<ATEToken> decls = new ArrayList<ATEToken>();

    public ElementGrammarName name;

    static {
        blockIdentifiers = new ArrayList<String>();
        blockIdentifiers.add(OPTIONS_BLOCK_NAME);
        blockIdentifiers.add(TOKENS_BLOCK_NAME);
        blockIdentifiers.add(PARSER_HEADER_BLOCK_NAME);
        blockIdentifiers.add(LEXER_HEADER_BLOCK_NAME);
        blockIdentifiers.add(PARSER_MEMBERS_BLOCK_NAME);
        blockIdentifiers.add(LEXER_MEMBERS_BLOCK_NAME);

        ruleModifiers = new ArrayList<String>();
        ruleModifiers.add("protected");
        ruleModifiers.add("public");
        ruleModifiers.add("private");
        ruleModifiers.add("fragment");

        keywords = new ArrayList<String>();
        keywords.addAll(blockIdentifiers);
        keywords.addAll(ruleModifiers);
        keywords.add("returns");
        keywords.add("init");

        predefinedReferences = new ArrayList<String>();
        predefinedReferences.add("EOF");
    }

    public GrammarSyntaxParser() {
    }

    @Override
    public void parseTokens() {
        rules.clear();
        groups.clear();
        blocks.clear();
        actions.clear();
        references.clear();
        decls.clear();

        while(nextToken()) {

            if(isComplexComment(0)) continue;

            if(tryMatchName()) continue;
            if(tryMatchScope()) continue; // scope before block
            if(tryMatchBlock()) continue;
            if(tryMatchRule()) continue;

            if(isSingleComment(0)) {
                ElementGroup group = matchRuleGroup(rules);
                if(group != null)
                    groups.add(group);
            }
        }
    }

    private boolean tryMatchName() {
        mark();
        ElementGrammarName n = matchName();
        if(n != null) {
            name = n;
            return true;
        } else {
            rewind();
            return false;
        }
    }

    private boolean tryMatchBlock() {
        mark();
        ElementBlock block = matchBlock();
        if(block != null) {
            blocks.add(block);
            return true;
        } else {
            rewind();
            return false;
        }
    }

    private boolean tryMatchScope() {
        mark();
        if(matchScope()) {
            return true;
        } else {
            rewind();
            return false;
        }
    }

    private boolean tryMatchRule() {
        mark();
        ElementRule rule = matchRule(actions, references);
        if(rule != null) {
            rules.add(rule);
            return true;
        } else {
            rewind();
            return false;
        }
    }

    /**
     * Matches the name of the grammar:
     *
     * grammar lexer JavaLexer;
     *
     */
    private ElementGrammarName matchName() {
        if(isID(0, "grammar")) {
            ATEToken start = T(0);
            nextToken(); // skip 'grammar'

            // Check if the grammar has a type (e.g. lexer, parser, tree, etc)
            ATEToken type = null;
            if(ElementGrammarName.isKnownType(T(0).getAttribute())) {
                type = T(0);
                nextToken(); // skip the type
            }

            // After the type comes the name of the grammar
            ATEToken name = T(0);

            // Loop until we find the semi colon
            while(nextToken()) {
                if(isSEMI(0)) {
                    // semi colon found, the grammar name is matched!
                    return new ElementGrammarName(name, start, T(0), type);
                }
            }
        }

        return null;
    }

    /**
     * Matches a scope:
     *
     * scope [name] ( BLOCK | ';' )
     *
     * where
     *  BLOCK = { ... }
     *
     */
    private boolean matchScope() {
        // Must begin with the keyword 'scope'
        ATEToken start = T(0);
        if(isID(0, "scope")) {
            if(!nextToken()) return false;
        } else {
            return false;
        }

        // Match the optional name
        if(isID(0)) {
            if(!nextToken()) return false;
        }

        // Match either the block or the semi
        if(isOpenBLOCK(0)) {
            ATEToken beginBlock = T(0);
            if(matchBalancedToken("{", "}")) {
                beginBlock.type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
                T(0).type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
                start.type = GrammarSyntaxLexer.TOKEN_BLOCK_LABEL;
            } else {
                return false;
            }
        } else {
            return isSEMI(0);
        }

        return true;
    }

    /**
     * Matches a block:
     *
     * LABEL BLOCK
     *
     * where
     *  LABEL = @id || @bar::foo | label
     *  BLOCK = { ... }
     *
     */
    private ElementBlock matchBlock() {
        ATEToken start = T(0);
        int startIndex = getPosition();
        if(isID(0)) {
            if(!nextToken()) return null;
        } else {
            return null;
        }

        ElementBlock block = new ElementBlock(start.getAttribute().toLowerCase(), start);
        ATEToken beginBlock = T(0);
        if(matchBalancedToken("{", "}", block)) {
            beginBlock.type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
            T(0).type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
            start.type = GrammarSyntaxLexer.TOKEN_BLOCK_LABEL;
        } else {
            return null;
        }

        block.end = T(0);
        block.internalTokens = new ArrayList<ATEToken>(getTokens().subList(startIndex, getPosition()));
        block.parse();
        if(block.isTokenBlock) {
            List<ATEToken> tokens = block.getDeclaredTokens();
            for(int i=0; i<tokens.size(); i++) {
                ATEToken lexerToken = tokens.get(i);
                lexerToken.type = GrammarSyntaxLexer.TOKEN_DECL;
                decls.add(lexerToken);
            }
        }
        return block;
    }

    /**
     * Matches a rule:
     *
     * MODIFIER? ruleNameID ARG? '!'? COMMENT*
     *
     * where
     *  MODIFIER = protected | public | private | fragment
     *  COMMENT = // or /*
     *  ARG = '[' Type arg... ']'
     *
     */
    private ElementRule matchRule(List<ElementAction> actions, List<ElementReference> references) {
        ATEToken start = T(0);

        // Match any modifiers
        if(ruleModifiers.contains(T(0).getAttribute())) {
            // skip the modifier
            if(!nextToken()) return null;
        }

        // Match the name (it has to be an ID)
        if(!isID(0)) return null;

        ElementToken tokenName = (ElementToken) T(0);
        String name = tokenName.getAttribute();
        if(!nextToken()) return null;

        // Match any argument
        if(matchArguments()) {
            if(!nextToken()) return null;
        }

        // Match any returns
        if(T(0).getAttribute().equals("returns")) {
            if(!nextToken()) return null;
            if(matchArguments()) {
                if(!nextToken()) return null;
            }
        }

        // Match any optional "!"
        if(T(0).getAttribute().equals("!")) {
            // skip it
            if(!nextToken()) return null;
        }

        // Match any comments
        while(isSingleComment(0) || isComplexComment(0)) {
            if(!nextToken()) return null;
        }

        // Matches any number of scopes and blocks
        while(true) {
            if(matchScope()) {
                if(!nextToken()) return null;
                continue;
            }
            if(matchBlock() != null) {
                if(!nextToken()) return null;
                continue;
            }
            if(isSingleComment(0) || isComplexComment(0)) {
                if(!nextToken()) return null;
                continue;
            }

            if(isCOLON(0)) {
                // When a colon is matched, we are at the beginning of the content of the rule
                break;
            } else {
                // Invalid rule matching
                return null;
            }
        }

        // Parse the content of the rule (after the ':')
        final ATEToken colonToken = T(0);
        final ElementRule rule = new ElementRule(this, name, start, colonToken, null);
        final int refOldSize = references.size();
        final LabelScope labels = new LabelScope();
        labels.begin();
        while(nextToken()) {
            if(isSEMI(0)) {
                // End of the rule.
                matchRuleExceptionGroup();

                // Record the token that defines the end of the rule
                rule.end = T(0);

                // Change the token type of the name
                tokenName.type = GrammarSyntaxLexer.TOKEN_DECL;
                decls.add(tokenName);

                // Each rule contains the index of its references. It is used when refactoring.
                if(references.size() > refOldSize) {
                    // If the size of the references array has changed, then we have some references
                    // inside this rule. Sets the indexes into the rule.
                    rule.setReferencesIndexes(refOldSize, references.size()-1);
                }

                // Indicate to the rule that is has been parsed completely.
                rule.completed();

                // Return the rule
                return rule;
            } else if(isID(0)) {
                // Probably a reference inside the rule.

                // Check for ST function
                if(isLPAREN(1)) {
                    if(!nextToken()) return null;
                    if(matchBalancedToken("(", ")", REWRITE_FUNCTION)) continue;
                    return null;
                }

                // Match any option block
                // todo only allowed here?
                if(T(0).getAttribute().equals(OPTIONS_BLOCK_NAME)) {
                    if(matchBlock() != null) continue;
                    return null;
                }

                // Check for label:
                //   label=reference
                //   label+=reference
                //   label='string'
                if(isChar(1, "=")) {
                    T(0).type = GrammarSyntaxLexer.TOKEN_LABEL;
                    labels.add(T(0).getAttribute());
                    if(!skip(2)) return null;
                } else if(isChar(1, "+") && isChar(2, "=")) {
                    T(0).type = GrammarSyntaxLexer.TOKEN_LABEL;
                    labels.add(T(0).getAttribute());
                    if(!skip(3)) return null;
                }

                // Skip if the operand is not an ID. Can be a string for example, as in:
                // label='operand'
                if(!isID(0)) continue;

                // Ignore reserved keywords
                ATEToken refToken = T(0);

                // Match any field access, for example:
                // foo.bar.boo
                while(isChar(1, ".") && isID(2)) {
                    if(!skip(2)) return null;
                }

                // Match any option arguments
                if(isChar(1, "[")) {
                    if(!nextToken()) return null;
                    if(matchArguments()) {
                        // do not advance one token because we are at the end of parsing the reference
                        //if(!nextToken()) return null;
                    }
                }

                // Now we have the reference token. Set the token flags
                if(labels.lookup(refToken.getAttribute())) {
                    // Reference is to a label, not a lexer/parser rule
                    refToken.type = GrammarSyntaxLexer.TOKEN_LABEL;
                } else {
                    refToken.type = GrammarSyntaxLexer.TOKEN_REFERENCE;
                    // Create and add the new reference
                    references.add(new ElementReference(rule, refToken));
                }
            } else if(isOpenBLOCK(0)) {
                // Match an action

                ATEToken t0 = T(0);
                ElementAction action = new ElementAction(rule, t0);
                if(matchBalancedToken("{", "}", action)) {
                    t0.type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
                    T(0).type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
                    action.end = T(0);
                    action.actionNum = actions.size();
                    action.setScope(rule);
                    actions.add(action);
                }
            } else if(isTokenType(0, GrammarSyntaxLexer.TOKEN_REWRITE)) {
                // Match a rewrite syntax beginning with ->
            //    if(!nextToken()) return null;

            //    matchRewriteSyntax();
            } else if(isLPAREN(0)) {
                labels.begin();
            } else if(isRPAREN(0)) {
                labels.end();
            }
        }

        return null;
    }

    private boolean matchArguments() {
        return isChar(0, "[") && matchBalancedToken("[", "]");
    }

    // todo check and terminate
    private void matchRuleExceptionGroup() {
        if(!matchOptional("exception"))
            return;

        // Optional ARG_ACTION
        if(isOpenBLOCK(1))
            nextToken();

        while(matchOptional("catch")) {
            nextToken();    // ARG_ACTION: []
            nextToken();    // ACTION: { }
        }
    }

    /**
     * Matches the rewrite syntax:
     * -> { condition }? foo(...)
     * -> { condition }? { ... }
     * -> bar(...)
     * -> ELIST
     * -> $label
     */
    /*private boolean matchRewriteSyntax() {
        if(isOpenBLOCK(0)) {
            // Try to match the condition
            if(matchBalancedToken("{", "}", REWRITE_BLOCK)) {
                if(!nextToken()) return false;

                // Match the '?'
                if(matchChar("?")) {
                    // a function follows
                    if(matchSTFunction()) return true;
                    //if(matchTreeSyntax()) return true;
                    return matchBalancedToken("{", "}", REWRITE_BLOCK);
                } else {
                    // nothing follows
                    // todo ugly -> think about general solution with nextToken
                    previousToken();
                    return true;
                }
            } else {
                return false;
            }
        } else {
//            if(matchSTFunction()) return true;
 //           if(matchTreeSyntax()) return true;

            System.out.println(T(0));
            if(isTokenType(0, ATESyntaxLexer.TOKEN_SINGLE_QUOTE_STRING)) {
                return nextToken();
            } else if(isChar(0, "$") && isID(1)) {
                return skip(2);
            } else if(isID(0)) {
                return nextToken();
            }
        }
        return false;
    }

    private boolean matchSTFunction() {
        if(!matchID(0)) return false;

        if(!isLPAREN(0)) return false;

        return matchBalancedToken("(", ")", REWRITE_FUNCTION);
    }

    private boolean matchTreeSyntax() {
        if(!isChar(0, "^") && !isLPAREN(1)) return false;

        skip(1);

        return matchBalancedToken("(", ")");
    } */

    /**
     * Matches the group token used to group rules in the rule lists
     *
     */
    private ElementGroup matchRuleGroup(List<ElementRule> rules) {
        ATEToken token = T(0);
        String comment = token.getAttribute();

        if(comment.startsWith(BEGIN_GROUP)) {
            return new ElementGroup(comment.substring(BEGIN_GROUP.length(), comment.length()-1), rules.size()-1, token);
        } else if(comment.startsWith(END_GROUP)) {
            return new ElementGroup(rules.size()-1, token);
        } else
            return null;
    }

    /**
     * Matches all tokens until the balanced token's attribute is equal to close.
     *
     * @param open The open attribute
     * @param close The close attribute
     * @return true if the match succeeded
     */
    private boolean matchBalancedToken(String open, String close) {
        return matchBalancedToken(open, close, null);
    }

    private boolean matchBalancedToken(String open, String close, ATEScope scope) {
        T(0).scope = scope;
        int balance = 0;
        while(nextToken()) {
            String attr = T(0).getAttribute();
            T(0).scope = scope;
            if(attr.equals(open))
                balance++;
            else if(attr.equals(close)) {
                if(balance == 0) {
                    return true;
                }
                balance--;
            }
        }
        return false;
    }

    private boolean matchOptional(String t) {
        if(isID(1, t)) {
            nextToken();
            return true;
        } else
            return false;
    }

    // @todo refactor using this method?
    private boolean matchChar(String c) {
        if(isChar(0, c)) {
            return nextToken();
        } else {
            return false;
        }

    }

    // @todo refactor using this method?
    private boolean matchID(int index) {
        if(isID(index)) {
            return nextToken();
        } else {
            return false;
        }
    }

    private boolean isLPAREN(int index) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_LPAREN);
    }

    private boolean isRPAREN(int index) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_RPAREN);
    }

    private boolean isSEMI(int index) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_SEMI);
    }

    private boolean isCOLON(int index) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_COLON);
    }

    private boolean isOpenBLOCK(int index) {
        return isChar(index, "{");
    }

    /**
     * Class used to keep track of the scope of the labels inside a rule.
     *
     */
    // todo still need that?
    private class LabelScope {

        Stack<Set<String>> labels = new Stack<Set<String>>();

        public void begin() {
            labels.push(new HashSet<String>());
        }

        public void end() {
            // todo ask Terence is label are scoped
            //labels.pop();
        }

        public void add(String label) {
            if(labels.isEmpty()) {
                System.err.println("[LabelScope] Stack is empty");
                return;
            }
            labels.peek().add(label);
        }

        public boolean lookup(String label) {
            for(int i=0; i<labels.size(); i++) {
                if(labels.get(i).contains(label)) return true;
            }
            return false;
        }
    }

}
