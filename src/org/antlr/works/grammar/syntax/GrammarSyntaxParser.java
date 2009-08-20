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

import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.generic.ATESyntaxParser;
import org.antlr.works.ate.syntax.misc.ATEScope;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.element.*;

import java.util.*;

/**
 * This class is the main ANTLRWorks parser for the ANTLR 3 grammar. Its purpose is to quickly parse the relevant
 * information needed for the syntax (references, actions, blocks, etc) without spending too much time in parsing
 * all the details of the grammar.
 */
public class GrammarSyntaxParser extends ATESyntaxParser {

    private static final ElementRewriteBlock REWRITE_BLOCK = new ElementRewriteBlock();
    private static final ElementArgumentBlock ARGUMENT_BLOCK = new ElementArgumentBlock();
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

    public final List<ElementRule> rules = new ArrayList<ElementRule>();
    public final List<ElementGroup> groups = new ArrayList<ElementGroup>();
    public final List<ElementBlock> blocks = new ArrayList<ElementBlock>();         // tokens {}, options {}
    public final List<ElementAction> actions = new ArrayList<ElementAction>();        // { action } in rules
    public final List<ElementReference> references = new ArrayList<ElementReference>();
    public final List<ElementImport> imports = new ArrayList<ElementImport>();
    public final List<ATEToken> decls = new ArrayList<ATEToken>();

    private final LabelTable labels = new LabelTable();
    private final List<ATEToken> unresolvedReferences = new ArrayList<ATEToken>();
    private final Set<String> declaredReferenceNames = new HashSet<String>();
    private final Map<ATEToken,ElementRule> refsToRules = new HashMap<ATEToken,ElementRule>();

    private ElementGrammarName name;
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

    public ElementGrammarName getName() {
        return name;
    }

    @Override
    public void close() {
        super.close();
        clear();
    }

    @Override
    public void parseTokens() {
        clear();

        if(!nextToken()) return;

        while(true) {

            if(matchName()) continue;
            if(matchScope()) continue; // scope before block
            if(matchBlock()) continue;
            if(matchImport()) continue;
            if(matchRule()) continue;

            if(matchRuleGroup()) continue; // before single comment

            if(matchSingleComment(0)) continue;
            if(matchComplexComment(0)) continue;

            // Nothing matches, go to next token
            if(!nextToken()) break;
        }

        resolveReferences();
    }

    private void clear() {
        rules.clear();
        groups.clear();
        blocks.clear();
        actions.clear();
        references.clear();
        imports.clear();
        decls.clear();
        currentRule = null;
        declaredReferenceNames.clear();
        unresolvedReferences.clear();
        refsToRules.clear();
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
     * (lexer|parser|tree|) grammar JavaLexer;
     *
     * @return true if the name of the grammar is matched
     */
    private boolean matchName() {
        if(!isID(0)) return false;

        mark();
        if(tryMatchName()) {
            return true;
        } else {
            rewind();
            return false;
        }
    }

    private boolean tryMatchName() {
        ATEToken start = T(0);

        // Check if the grammar has a type (e.g. lexer, parser, tree, etc)
        if(ElementGrammarName.isKnownType(start.getAttribute())) {
            if(!nextToken()) return false;
        }

        if(!matchID(0, "grammar")) return false;

        // After the type comes the name of the grammar
        ATEToken name = T(0);
        if(!nextToken()) return false;

        // The next token must be a semi colon
        if(!matchSEMI(0)) return false;

        this.name = new ElementGrammarName(name, start, T(-1), start);
        return true;
    }

    /**
     * Matches a scope declaration:
     *
     * scope [name] BLOCK
     *
     * where
     *  BLOCK = { ... }
     *
     * @return true if a scope is matched
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
            ElementBlock block = new ElementBlock(start.getAttribute().toLowerCase(), start);
            ATEToken beginBlock = T(0);
            if(matchBalancedToken(ATESyntaxLexer.TOKEN_LCURLY, ATESyntaxLexer.TOKEN_RCURLY, null, true)) {
                beginBlock.type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
                T(-1).type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
                start.type = GrammarSyntaxLexer.TOKEN_BLOCK_LABEL;
                blocks.add(block);
                return true;
            }
        }

        rewind();
        return false;
    }

    /**
     * Matches a scope reference in a rule definition:
     *
     * scope name (',' name)* ';'
     *
     * @return true if a scope is matched
     */
    private boolean matchScopeUse() {
        if(!isID(0, "scope")) return false;

        mark();

        // Must begin with the keyword 'scope'
        ATEToken start = T(0);
        if(!matchID(0, "scope")) return false;

        // Match the first name
        if (matchID(0)) {

            // Loop over additional scopes
            while(matchChar(0, ",")) {
                // match an ID
                if(!matchID(0)) {
                    rewind();
                    return false;
                }
            }

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
     * @return true if a block is matched
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
        if(matchBalancedToken(ATESyntaxLexer.TOKEN_LCURLY, ATESyntaxLexer.TOKEN_RCURLY, block, true)) {
            beginBlock.type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
            T(-1).type = GrammarSyntaxLexer.TOKEN_BLOCK_LIMIT;
            start.type = GrammarSyntaxLexer.TOKEN_BLOCK_LABEL;
            blocks.add(block);

            block.end = T(-1);
            block.internalTokens = new ArrayList<ATEToken>(getTokens().subList(startIndex, getPosition()));
            block.parse();
            if(block.isTokenBlock) {
                List<ATEToken> tokens = block.getDeclaredTokens();
                for (ATEToken lexerToken : tokens) {
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
     * Matches an import statement:
     *
     * import JavaDecl, JavaAnnotations, JavaExpr, JavaStat, JavaLexerRules;
     *
     *
     * @return true if an import is matched
     */
    private boolean matchImport() {
        mark();

        // Must begin with the keyword 'import'
        if(!matchID(0, "import")) return false;

        while(!matchSEMI(0)) {
            if(T(0) != null) {
                imports.add(new ElementImport(name, T(0)));                
            }
            if(!matchID(0)) {
                rewind();
                return false;
            }
            if(!matchChar(0, ",")) {
                rewind();
                return false;
            }
        }

        return true;
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
     * @return true if a rule is matched
     */
    private boolean matchRule() {
        mark();
        try {
            if(tryMatchRule()) {
                return true;
            } else {
                rewind();
                return false;
            }
        } finally {
            currentRule = null;
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

        // Match any comments
        while(true) {
            if(matchSingleComment(0)) continue;
            if(matchComplexComment(0)) continue;
            break;
        }

        // Match any returns
        if(matchID(0, "returns")) {
            matchArguments();
        }

        // Match any optional "!"
        matchChar(0, "!");

        // Match any comments, scopes and blocks
        while(true) {
            if(matchScopeUse()) continue;
            if(matchBlock()) continue;
            if(matchSingleComment(0)) continue;
            if(matchComplexComment(0)) continue;

            if(isCOLON(0)) {
                // When a colon is matched, we are at the beginning of the content of the rule
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
        while(true) {
            // Match the end of the rule
            if(matchEndOfRule(tokenName, oldRefsSize, oldBlocksSize, oldActionsSize)) return true;

            // Match any block
            if(matchBlock(OPTIONS_BLOCK_NAME)) continue;

            // Match any ST rewrite template
            if(matchRewriteTemplate()) continue;

            // Match any assignment
            if(matchAssignment(labels)) continue;

            // Match any internal reference
            if(matchInternalRefInRule()) continue;

            // Match any action
            if(matchAction()) continue;

            // Match node token
            if(matchSingleQuoteString(0) && matchOptionalNodeToken()) continue;

            // Nothing matched, go to the next token
            if(!nextToken()) return false;
        }
    }

    private boolean matchEndOfRule(ElementToken tokenName, int oldRefsSize, int oldBlocksSize, int oldActionsSize) {
        if(!matchSEMI(0)) return false;

        // Match any comments between the end of the rule (;) and the catch
        while(matchComplexComment(0) || matchSingleComment(0)) {
            // match all of them...
        }

        // End of the rule.
        matchRuleExceptionGroup();

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

    private boolean matchInternalRefInRule() {
        // Probably a reference inside the rule.
        ATEToken refToken = T(0);

        if(!matchID(0)) return false;

        // Try to match the node token
        if(!matchOptionalNodeToken()) return false;

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
        ElementAction action = new ElementAction(this, currentRule, t0);
        if(matchBalancedToken(ATESyntaxLexer.TOKEN_LCURLY, ATESyntaxLexer.TOKEN_RCURLY, action, true)) {
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

    private boolean matchAssignment(LabelTable labels) {
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

    /** Matches a rewrite template according to the following grammar:

     Build a tree for a template rewrite:
     ^(TEMPLATE (ID|ACTION) ^(ARGLIST ^(ARG ID ACTION) ...) )
     where ARGLIST is always there even if no args exist.
     ID can be "template" keyword.  If first child is ACTION then it's
     an indirect template ref

     -> foo(a={...}, b={...})
     -> ({string-e})(a={...}, b={...})  // e evaluates to template name
     -> {%{$ID.text}} // create literal template from string (done in ActionTranslator)
     -> {st-expr} // st-expr evaluates to ST

     rewrite_template
     {Token st=null;}
     :   // -> template(a={...},...) "..."
     {LT(1).getText().equals("template")}? // inline
     rewrite_template_head {st=LT(1);}
     ( DOUBLE_QUOTE_STRING_LITERAL! | DOUBLE_ANGLE_STRING_LITERAL! )
     {#rewrite_template.addChild(#[st]);}

     |	// -> foo(a={...}, ...)
     rewrite_template_head

     |	// -> ({expr})(a={...}, ...)
     rewrite_indirect_template_head

     |	// -> {...}
     ACTION
     ;

     // -> foo(a={...}, ...)
     rewrite_template_head
     :	id lp:LPAREN^ {#lp.setType(TEMPLATE); #lp.setText("TEMPLATE");}
     rewrite_template_args
     RPAREN!
     ;

     // -> ({expr})(a={...}, ...)
     rewrite_indirect_template_head
     :	lp:LPAREN^ {#lp.setType(TEMPLATE); #lp.setText("TEMPLATE");}
     ACTION
     RPAREN!
     LPAREN! rewrite_template_args RPAREN!
     ;

     rewrite_template_args
     :	rewrite_template_arg (COMMA! rewrite_template_arg)*
     {#rewrite_template_args = #(#[ARGLIST,"ARGLIST"], rewrite_template_args);}
     |	{#rewrite_template_args = #[ARGLIST,"ARGLIST"];}
     ;

     rewrite_template_arg
     :   id a:ASSIGN^ {#a.setType(ARG); #a.setText("ARG");} ACTION
     ;
     * @return true if a rewrite syntax is matched
     */

    private boolean matchRewriteTemplate() {
        if(!isTokenType(0, GrammarSyntaxLexer.TOKEN_REWRITE)) return false;

        if(!nextToken()) return false;

        // Match any comments between the -> and the template
        while(matchComplexComment(0) || matchSingleComment(0)) {
            // match all of them...
        }

        // Check first for any semantic predicate:
        // e.g: -> {...}?
        if(matchAction()) {
            // If it is not a semantic predicate, it's an action
            // like -> {...}
            if(!matchChar(0, "?")) return true;

            // Otherwise, it's a semantic predicate and we continue to match
            // the rewrite as usual
        }

        // Check for -> template(...) ("..." | <<...>>)
        if(isID(0, "template")) {
            // inline template
            if(!matchRewriteTemplateHead()) return false;

            if(matchDoubleQuotedString()) return true;
            if(matchDoubleAngleString()) return true;
        } else if(matchRewriteTemplateHead()) {
            // matched -> foo(...)
        } else if(matchRewriteIndirectTemplateHead()) {
            // matched -> ({expr})(...)
        } else if(matchAction()) {
            // matched -> {...}
        } else {
            return true;
        }

        return true;
    }

    private boolean matchRewriteIndirectTemplateHead() {
        if(!isLPAREN(0)) return false;

        mark();
        if(tryMatchRewriteIndirectTemplateHead()) {
            return true;
        } else {
            rewind();
            return false;
        }
    }

    private boolean tryMatchRewriteIndirectTemplateHead() {
        if(!matchLPAREN(0)) return false;
        if(!matchAction()) return false;
        if(!matchRPAREN(0)) return false;

        if(!matchLPAREN(0)) return false;
        if(!matchRewriteTemplateArgs()) return false;
        return matchRPAREN(0);
    }

    private boolean matchRewriteTemplateHead() {
        if(!isID(0)) return false;

        mark();
        if(tryMatchRewriteTemplateHead()) {
            return true;
        } else {
            rewind();
            return false;
        }
    }

    private boolean tryMatchRewriteTemplateHead() {
        if(!matchID(0)) return false;
        if(!matchLPAREN(0)) return false;
        if(!matchRewriteTemplateArgs()) return false;
        return matchRPAREN(0);

    }

    private boolean matchRewriteTemplateArgs() {
        if(matchRewriteTemplateArg()) {
            while(matchChar(0, ",")) {
                matchSingleComment(0);
                matchComplexComment(0);
                if(!matchRewriteTemplateArg()) return false;
            }
        }
        return true;
    }

    private boolean matchRewriteTemplateArg() {
        if(!isID(0) && !isChar(1, "=")) return false;

        mark();
        if(tryMatchRewriteTemplateArg()) {
            return true;
        } else {
            rewind();
            return false;
        }
    }

    private boolean tryMatchRewriteTemplateArg() {
        if(!matchID(0)) return false;
        if(!matchChar(0, "=")) return false;

        return matchBalancedToken(ATESyntaxLexer.TOKEN_LCURLY, ATESyntaxLexer.TOKEN_RCURLY, REWRITE_FUNCTION, true);

    }

    private boolean matchDoubleQuotedString() {
        if(isTokenType(0, ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING)) {
            T(0).scope = REWRITE_BLOCK;
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    private boolean matchDoubleAngleString() {
        return matchBalancedToken(GrammarSyntaxLexer.TOKEN_OPEN_DOUBLE_ANGLE, GrammarSyntaxLexer.TOKEN_CLOSE_DOUBLE_ANGLE, REWRITE_BLOCK, false);
    }

    private boolean matchArguments() {
        return matchBalancedToken(ATESyntaxLexer.TOKEN_LBRACK, ATESyntaxLexer.TOKEN_RBRACK, ARGUMENT_BLOCK, true);
    }

    /**
     *
     *
     *
     exceptionGroup
     :	( exceptionHandler )+ ( finallyClause )?
     |	finallyClause
     ;

     exceptionHandler
     :    "catch"^ ARG_ACTION ACTION
     ;

     finallyClause
     :    "finally"^ ACTION
     ;

     */
    private void matchRuleExceptionGroup() {
        while(matchID(0, "catch")) {
            matchArguments();
            matchAction();
        }
        if(matchID(0, "finally")) {
            matchAction();
        }
    }

    /**
     * Matches the group token used to group rules in the rule lists
     *
     * @return true if a rule group is matched
     */
    private boolean matchRuleGroup() {
        if(!isSingleComment(0)) return false;

        ATEToken token = T(0);
        String comment = token.getAttribute();

        if(comment.startsWith(BEGIN_GROUP)) {
            groups.add(new ElementGroup(comment.substring(BEGIN_GROUP.length(), comment.length()-1), rules.size()-1, token));
            nextToken();
            return true;
        } else if(comment.startsWith(END_GROUP)) {
            groups.add(new ElementGroup(rules.size()-1, token));
            nextToken();
            return true;
        }
        return false;
    }

    /**
     * Adds a new reference
     *
     * @param ref The token representing the reference
     * @param addOnlyIfKnownLabel
     * @return True if the reference is a label reference
     */
    private boolean addReference(ATEToken ref, boolean addOnlyIfKnownLabel) {
        refsToRules.put(ref, currentRule);
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
    private boolean matchBalancedToken(int open, int close, ATEScope scope, boolean matchInternalRef) {
        if(T(0) == null || T(0).type != open) return false;

        mark();
        int balance = 0;
        while(true) {
            T(0).scope = scope;
            if(T(0).type == open)
                balance++;
            else if(T(0).type == close) {
                balance--;
                if(balance == 0) {
                    nextToken();
                    return true;
                }
            }
            if(!nextToken()) break;

            matchInternalRefInBalancedToken(matchInternalRef);
        }
        rewind();
        return false;
    }

    private void matchInternalRefInBalancedToken(boolean matchInternalRef) {
        if(matchInternalRef && isChar(0, "$") && isID(1)) {
            T(0).type = GrammarSyntaxLexer.TOKEN_INTERNAL_REF;

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
            }
        }
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

    /**
     * Matches a node token. For example
     * FOO<a=b; c=d> or FOO<a> or FOO<a.b.c>
     */
    private boolean matchOptionalNodeToken() {
        mark();
        if(matchChar(0, "<")) {
            while(matchID(0)) {
                if(isChar(0, ">")) break;

                if(matchChar(0, ".")) continue;

                if(!matchChar(0, "=")) {
                    rewind();
                    return false;
                }
                if(!matchSEMI(0)) {
                    rewind();
                    return false;
                }
            }
            if(!matchChar(0, ">")) {
                rewind();
                return false;
            }
            return true;
        } else {
            return true;
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
        return isTokenType(index, ATESyntaxLexer.TOKEN_LCURLY);
    }

    private static class LabelTable {

        Set<String> labels = new HashSet<String>();

        public void clear() {
            labels.clear();
        }

        public void add(String label) {
            labels.add(label);
        }

        public boolean lookup(String label) {
            return labels.contains(label);
        }
    }

}
