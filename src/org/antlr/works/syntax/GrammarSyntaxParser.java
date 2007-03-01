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
import org.antlr.works.syntax.element.*;

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

    private LabelScope labels = new LabelScope();
    private List<ATEToken> unresolvedReferences = new ArrayList<ATEToken>();
    private Set<String> declaredReferenceNames = new HashSet<String>();
    private Map<ATEToken,ElementRule> refsToRules = new HashMap<ATEToken,ElementRule>();

    private ElementRule currentRule;

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
        currentRule = null;
        declaredReferenceNames.clear();
        unresolvedReferences.clear();
        refsToRules.clear();

        if(!nextToken()) return;

        while(true) {

            if(matchName()) continue;
            if(matchScope()) continue; // scope before block
            if(matchBlock()) continue;
            if(matchRule()) continue;

            if(matchRuleGroup()) continue; // before single comment

            if(matchSingleComment(0)) continue;
            if(matchComplexComment(0)) continue;

            // Nothing matches, go to next token
            if(!nextToken()) break;
        }

        resolveReferences();
    }

    /**
     * Resolves the unresolved references by using externally provided names. For example,
     * reading a file of token using the option tokenVocab will invoke this method to solve
     * any remaining references that are still unresolved.
     *
     * @param externalNames A list of string representing the external declared reference names
     */
    public void resolveReferencesWithExternalNames(Set<String> externalNames) {
        for(int i=unresolvedReferences.size()-1; i >= 0; i--) {
            ATEToken ref = unresolvedReferences.get(i);
            if(externalNames.contains(ref.getAttribute())) {
                ref.type = GrammarSyntaxLexer.TOKEN_REFERENCE;
                references.add(new ElementReference(refsToRules.get(ref), ref));
                unresolvedReferences.remove(i);
            }
        }
    }

    /**
     * Resolves the unresolved references by looking at the set of declared references
     */
    private void resolveReferences() {
        for(int i=unresolvedReferences.size()-1; i >= 0; i--) {
            ATEToken ref = unresolvedReferences.get(i);
            if(declaredReferenceNames.contains(ref.getAttribute())) {
                ref.type = GrammarSyntaxLexer.TOKEN_REFERENCE;
                references.add(new ElementReference(refsToRules.get(ref), ref));
                unresolvedReferences.remove(i);
            }
        }
    }

    /**
     * Matches the name of the grammar:
     *
     * grammar lexer JavaLexer;
     *
     */
    private boolean matchName() {
        if(!isID(0, "grammar")) return false;

        mark();

        ATEToken start = T(0);
        if(matchID(0, "grammar")) {

            // Check if the grammar has a type (e.g. lexer, parser, tree, etc)
            ATEToken type = T(0);
            if(type == null) return false;
            if(ElementGrammarName.isKnownType(T(0).getAttribute())) {
                if(!nextToken()) return false;
            } else {
                type = null;
            }

            // After the type comes the name of the grammar
            ATEToken name = T(0);

            // Loop until we find the semi colon
            while(nextToken()) {
                if(isSEMI(0)) {
                    // semi colon found, the grammar name is matched.
                    ATEToken end = T(0);
                    nextToken();
                    this.name = new ElementGrammarName(name, start, end, type);
                    return true;
                }
            }
        }

        rewind();
        return false;
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
        if(!isID(0, "scope")) return false;

        mark();

        // Must begin with the keyword 'scope'
        ATEToken start = T(0);
        if(!matchID(0, "scope")) return false;

        // Match the optional name
        matchID(0);

        // Match either the block or the semi
        if(isOpenBLOCK(0)) {
            ATEToken beginBlock = T(0);
            if(matchBalancedToken("{", "}", null, true)) {
                beginBlock.type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
                T(-1).type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
                start.type = GrammarSyntaxLexer.TOKEN_BLOCK_LABEL;
                return true;
            }
        } else {
            if(matchSEMI(0)) return true;
        }

        rewind();
        return false;
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
    private boolean matchBlock() {
        return matchBlock(null);
    }

    private boolean matchBlock(String label) {
        if(label == null && !isID(0)) return false;
        if(label != null && !isID(0, label)) return false;

        mark();

        ATEToken start = T(0);
        int startIndex = getPosition();
        if(label == null) {
            if(!matchID(0)) return false;
        } else {
            if(!matchID(0, label)) return false;
        }

        ElementBlock block = new ElementBlock(start.getAttribute().toLowerCase(), start);
        ATEToken beginBlock = T(0);
        if(matchBalancedToken("{", "}", block, true)) {
            beginBlock.type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
            T(-1).type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
            start.type = GrammarSyntaxLexer.TOKEN_BLOCK_LABEL;
            blocks.add(block);

            block.end = T(-1);
            block.internalTokens = new ArrayList<ATEToken>(getTokens().subList(startIndex, getPosition()));
            block.parse();
            if(block.isTokenBlock) {
                List<ATEToken> tokens = block.getDeclaredTokens();
                for(int i=0; i<tokens.size(); i++) {
                    ATEToken lexerToken = tokens.get(i);
                    lexerToken.type = GrammarSyntaxLexer.TOKEN_DECL;
                    addDeclaration(lexerToken);
                }
            }
            return true;
        }

        rewind();
        return false;
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
    private boolean matchRule() {
        mark();
        if(tryMatchRule()) {
            currentRule = null;
            return true;
        } else {
            rewind();
            currentRule = null;
            return false;
        }
    }

    private boolean tryMatchRule() {
        ATEToken start = T(0);
        if(start == null) return false;

        // Match any modifiers
        if(ruleModifiers.contains(start.getAttribute())) {
            // skip the modifier
            if(!nextToken()) return false;
        }

        // Match the name (it has to be an ID)
        ElementToken tokenName = (ElementToken) T(0);
        String name = tokenName.getAttribute();
        if(!matchID(0)) return false;

        // Match any optional argument
        matchArguments();

        // Match any returns
        if(matchID(0, "returns")) {
            matchArguments();
        }

        // Match any optional "!"
        matchChar(0, "!");

        // Match any comments, scopes and blocks
        while(true) {
            if(matchScope()) continue;
            if(matchBlock()) continue;
            if(matchSingleComment(0)) continue;
            if(matchComplexComment(0)) continue;

            if(isCOLON(0)) {
                // When a colon is matched, we are at the beginning of the content of the rule
                // refactor inside matchColon
                nextToken();
                break;
            } else {
                // Invalid rule matching
                return false;
            }
        }

        // Parse the content of the rule (after the ':')
        final ATEToken colonToken = T(-1);
        final int oldRefsSize = references.size();
        final int oldBlocksSize = blocks.size();
        final int oldActionsSize = actions.size();
        currentRule = new ElementRule(this, name, start, colonToken, null);
        labels.clear();
        labels.begin();
        while(true) {
            // Match the end of the rule
            if(matchEndOfRule(tokenName, oldRefsSize, oldBlocksSize, oldActionsSize)) return true;

            // Match any block
            if(matchBlock(OPTIONS_BLOCK_NAME)) continue;

            // Match any ST function call
            if(matchFunction(0)) continue;

            // Match any assignment
            if(matchAssignment(labels)) continue;

            // Match any internal reference
            if(matchInternalRef()) continue;

            // Match any action
            if(matchAction()) continue;

            if(matchLPAREN(0)) {
                labels.begin();
                continue;
            }

            if(matchRPAREN(0)) {
                labels.end();
                continue;
            }

            if(!nextToken()) return false;
        }
    }

    private boolean matchEndOfRule(ElementToken tokenName, int oldRefsSize, int oldBlocksSize, int oldActionsSize) {
        if(!matchSEMI(0)) return false;

        // End of the rule.
        // todo
        //matchRuleExceptionGroup();

        // Record the token that defines the end of the rule
        currentRule.end = T(-1);

        // Change the token type of the name
        tokenName.type = GrammarSyntaxLexer.TOKEN_DECL;
        addDeclaration(tokenName);

        if(references.size() > oldRefsSize) {
            currentRule.setReferencesIndexes(oldRefsSize, references.size()-1);
        }

        if(blocks.size() > oldBlocksSize) {
            currentRule.setBlocksIndexes(oldBlocksSize, blocks.size()-1);
        }

        if(actions.size() > oldActionsSize) {
            currentRule.setActionsIndexes(oldActionsSize, actions.size()-1);
        }

        // Indicate to the rule that is has been parsed completely.
        currentRule.completed();

        // Return the rule
        rules.add(currentRule);
        return true;
    }

    private boolean matchInternalRef() {
        if(!matchID(0)) return false;

        // Probably a reference inside the rule.
        ATEToken refToken = T(-1);
        // Match any field access, for example:
        // foo.bar.boo
        while(isChar(0, ".") && isID(1)) {
            if(!skip(2)) return false;
        }

        // Match any optional arguments
        matchArguments();

        // Now we have the reference token. Set the token flags
        addReference(refToken, false);
        return true;
    }

    private boolean matchAction() {
        if(!isOpenBLOCK(0)) return false;

        // Match an action
        ATEToken t0 = T(0);
        ElementAction action = new ElementAction(currentRule, t0);
        if(matchBalancedToken("{", "}", action, true)) {
            t0.type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
            T(-1).type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;

            action.end = T(-1);
            action.actionNum = actions.size();
            action.setScope(currentRule);
            actions.add(action);
            return true;
        } else {
            return false;
        }
    }

    private boolean matchAssignment(LabelScope labels) {
        mark();

        ATEToken label = T(0);
        if(matchID(0)) {
            if(matchChar(0, "=")) {
                label.type = GrammarSyntaxLexer.TOKEN_LABEL;
                labels.add(label.getAttribute());
                return true;
            } else if(isChar(0, "+") && isChar(1, "=")) {
                label.type = GrammarSyntaxLexer.TOKEN_LABEL;
                labels.add(label.getAttribute());
                skip(2);
                return true;
            }
        }

        rewind();
        return false;
    }

    private boolean matchFunction(int index) {
        mark();
        if(isTokenType(index, GrammarSyntaxLexer.TOKEN_FUNC)) {
            nextToken();
            if(matchBalancedToken("(", ")", REWRITE_FUNCTION, true)) {
                return true;
            }
        }
        rewind();
        return false;
    }

    private boolean matchArguments() {
        return matchBalancedToken("[", "]", null, true);
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
     * Matches the group token used to group rules in the rule lists
     *
     */
    private boolean matchRuleGroup() {
        if(!isSingleComment(0)) return false;

        ATEToken token = T(0);
        String comment = token.getAttribute();

        if(comment.startsWith(BEGIN_GROUP)) {
            groups.add(new ElementGroup(comment.substring(BEGIN_GROUP.length(), comment.length()-1), rules.size()-1, token));
            return true;
        } else if(comment.startsWith(END_GROUP)) {
            groups.add(new ElementGroup(rules.size()-1, token));
            return true;
        }
        return false;
    }

    /**
     * Adds a new reference
     *
     * @param ref The token representing the reference
     * @return True if the reference is a label reference
     */
    private boolean addReference(ATEToken ref, boolean addOnlyIfKnownLabel) {
        if(labels.lookup(ref.getAttribute())) {
            // Reference is to a label, not a lexer/parser rule
            ref.type = GrammarSyntaxLexer.TOKEN_LABEL;
            return true;
        } else {
            if(!addOnlyIfKnownLabel) {
                ref.type = GrammarSyntaxLexer.TOKEN_REFERENCE;
                references.add(new ElementReference(refsToRules.get(ref), ref));
            }
            return false;
        }
    }

    private void addDeclaration(ATEToken token) {
        decls.add(token);
        declaredReferenceNames.add(token.getAttribute());
    }

    /**
     * Matches all tokens until the balanced token's attribute is equal to close.
     * Expects the current token to be the open token (e.g. '{' whatever needs to be balanced)
     *
     * @param open The open attribute
     * @param close The close attribute
     * @param scope The scope to assign to the tokens between the two balanced tokens
     * @param matchInternalRef True if internal references need to be matched (i.e. $foo, $bar, etc)
     * @return true if the match succeeded
     */
    private boolean matchBalancedToken(String open, String close, ATEScope scope, boolean matchInternalRef) {
        if(T(0) == null || !T(0).getAttribute().equals(open)) return false;

        mark();
        int balance = 0;
        while(true) {
            String attr = T(0).getAttribute();
            T(0).scope = scope;
            if(attr.equals(open))
                balance++;
            else if(attr.equals(close)) {
                balance--;
                if(balance == 0) {
                    nextToken();
                    return true;
                }
            }
            if(!nextToken()) break;

            if(matchInternalRef && isChar(0, "$") && isID(1)) {
                // Look for internal references, that is any ID preceeded by a $
                ATEToken ref = T(1);
                if(!addReference(ref, true)) {
                    // The reference is not a label but a global reference.
                    // The only issue with these global references is that some are not lexer or parser rules
                    // but declared variables or ANTLR internal stuff. To skip these references, we
                    // add all the internal references to a list of unknown reference and we check
                    // after parsing if they are listed as a lexer or parser declaration. Otherwise, we
                    // skip these references.

                    unresolvedReferences.add(ref);
                    refsToRules.put(ref, currentRule);
                }
            }
        }
        rewind();
        return false;
    }

    private boolean matchOptional(String t) {
        if(isID(1, t)) {
            nextToken();
            return true;
        } else
            return false;
    }

    private boolean matchChar(int index, String c) {
        if(isChar(index, c)) {
            nextToken();
            return true;
        } else {
            return false;
        }

    }

    private boolean matchID(int index) {
        if(isID(index)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    private boolean matchID(int index, String text) {
        if(isID(index, text)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    private boolean matchSEMI(int index) {
        if(isSEMI(index)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    private boolean matchLPAREN(int index) {
        if(isLPAREN(index)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    private boolean matchRPAREN(int index) {
        if(isRPAREN(index)) {
            nextToken();
            return true;
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
    private class LabelScope {

        Stack<Set<String>> labels = new Stack<Set<String>>();

        public void clear() {
            labels.clear();
        }

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
