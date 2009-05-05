package org.antlr.works.stringtemplate.syntax;

import org.antlr.works.ate.syntax.generic.ATESyntaxParser;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.ate.syntax.misc.ATEScope;
import org.antlr.works.stringtemplate.element.*;

import java.util.*;

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

public class ATEStringTemplateSyntaxParser extends ATESyntaxParser {

    private static final ElementTemplateArgumentBlock ARGUMENT_BLOCK = new ElementTemplateArgumentBlock();
    private static final ElementTemplateExpressionBlock EXPR_BLOCK = new ElementTemplateExpressionBlock();
    private static final ElementTemplateCommentScope COMMENT_SCOPE = new ElementTemplateCommentScope();

    public final List<ElementTemplateRule> templateRules = new ArrayList<ElementTemplateRule>();
    public final List<ElementTemplateReference> references = new ArrayList<ElementTemplateReference>();
    public final List<ElementTemplateMapDefinition> mapDefinitions = new ArrayList<ElementTemplateMapDefinition>();
    public final List<ATEToken> decls = new ArrayList<ATEToken>();
    public final List<ATEToken> maps = new ArrayList<ATEToken>();
    public List<ATEToken> currentArgs = new ArrayList<ATEToken>();
    public final List<String> currentArgNames = new ArrayList<String>();

    private final List<ATEToken> unresolvedReferences = new ArrayList<ATEToken>();
    private final Set<String> declaredReferenceNames = new HashSet<String>();
    private final Map<ATEToken,ElementTemplateRule> refsToRules = new HashMap<ATEToken,ElementTemplateRule>();

    private final Set<String> declaredMapNames = new HashSet<String>();

    private ElementTemplateName name;
    private ElementTemplateRule currentTemplateRule;
    private ElementTemplateMapDefinition currentTemplateMap;

    public ATEStringTemplateSyntaxParser() {

    }

    public ElementTemplateName getName() {
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

            if(matchNewline(0)) continue;

            if(matchSingleComment(0)) continue;
            if(matchComplexComment(0)) continue;

            if(matchName()) continue;
            if(matchTemplate()) continue;
            if(matchMapDefinition()) continue;

            // Nothing matches, go to next token
            if(!nextToken()) break;
        }

        resolveReferences();
    }

    private void clear() {
        templateRules.clear();
        decls.clear();
        declaredReferenceNames.clear();
        declaredMapNames.clear();
        unresolvedReferences.clear();
        references.clear();
        refsToRules.clear();
        currentTemplateRule = null;
    }

    /**
     * Resolves the unresolved references by looking at the set of declared references
     */
    private void resolveReferences() {
        for(int i=unresolvedReferences.size()-1; i >= 0; i--) {
            ATEToken ref = unresolvedReferences.get(i);
            if(declaredReferenceNames.contains(ref.getAttribute())) {
                ref.type = ATEStringTemplateSyntaxLexer.TOKEN_REFERENCE;
                references.add(new ElementTemplateReference(refsToRules.get(ref), ref));
                unresolvedReferences.remove(i);
            }
        }
    }

    /**
     * Matches the name of the group:
     *
     * group StringTemplate:STSuperGroup implements ITemplate1, ITemplate2;
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

    private boolean matchSuperGroup() {
        mark();
        if(tryMatchSuperGroup()) {
            return true;
        } else {
            rewind();
            return false;
        }
    }

    private boolean matchInterface() {
        mark();
        if(tryMatchInterface()) {
            return true;
        } else {
            rewind();
            return false;
        }
    }

    private boolean tryMatchName() {
        ATEToken start = T(0);

        if(!matchID(0, "group")) return false;

        // After the 'group' comes the name of the template
        ATEToken name = T(0);
        if(!nextToken()) return false;

        matchSuperGroup(); // match the optional super group
        matchInterface(); // match the optional interface/s

        // The next token must be a semi colon
        if(!matchSEMI(0)) return false;

        this.name = new ElementTemplateName(name, start, T(-1));
        return true;
    }

    private boolean tryMatchSuperGroup() {
        if(!matchCOLON(0)) return false;
        if(!matchID(0)) return false;
        return true;
    }

    private boolean tryMatchInterface() {
        if (!matchID(0, "implements")) return false;

        if (!isCOMMA(0)) return false;

        while (matchCOMMA(0)) {
            if (!matchID(0)) return false;
        }

        return true;
    }

    /**
     * Matches a template:
     *
     * templateNameID ARG ::= STRING | BIGSTRING
     *
     * where
     *  ARG = '(' (argID (',' argID)*)? ')'
     *  BIGSTRING = '<<' ... '>>'
     *
     * @return true if a template is matched
     */
    private boolean matchTemplate() {
        mark();
        try {
            if(tryMatchTemplate()) {
                return true;
            } else {
                rewind();
                return false;
            }
        } finally {
            currentTemplateRule = null;
        }
    }

    private boolean tryMatchTemplate() {
        ATEToken start = T(0);
        if(start == null) return false;

        String name = start.getAttribute();
        if(!matchID(0)) return false;

        currentArgs = new ArrayList<ATEToken>();
        currentArgNames.clear();
        // Match any optional argument
        matchArguments();

        // should be '::=' after arguments
        if(isDEFINED_TO_BE(0)) {
            // When a defineToBe is matched, we are at the beginning of the content of the template rule
            nextToken();
        } else {
            // Invalid template rule matching
            return false;
        }

        final ATEToken definedToBeToken = T(-1);
        currentTemplateRule = new ElementTemplateRule(this, name, start, definedToBeToken, null, currentArgs);

        // loop through all new lines
        while (matchNewline(0));

        if (isOPEN_DOUBLE_ANGLE(0)) {
            if (!tryMatchTemplateBigString(start)) return false;
        } else if (isDOUBLE_QUOTE(0)) {
            if (!tryMatchTemplateString(start)) return false;
        } else {
            if (!tryMatchTemplateAssign(start)) return false;
        }

        return true;
    }

    private boolean tryMatchTemplateBigString(ATEToken rule) {
        if (!matchOPEN_DOUBLE_ANGLE(0)) return false;

        while (true) {
            if (matchCLOSE_DOUBLE_ANGLE(0)) {
                if (rule != null) {
                    currentTemplateRule.end = T(-1);
                    rule.type = ATEStringTemplateSyntaxLexer.TOKEN_DECL;
                    addDeclaration(rule);
                    templateRules.add(currentTemplateRule);
                }
                return true;
            }

            if (matchAngleComment(0)) continue;
            if (matchExpression()) continue;
            if (matchLiteral()) continue;

            // Nothing matched, go to the next token
            if(!nextToken()) return false;
        }
    }

    private boolean tryMatchTemplateString(ATEToken rule) {
        if (!matchDOUBLE_QUOTE(0)) return false;

        while (true) {
            if (matchDOUBLE_QUOTE(0) || matchNewline(0)) {
                if (rule != null) {
                    currentTemplateRule.end = T(-1);
                    rule.type = ATEStringTemplateSyntaxLexer.TOKEN_DECL;
                    addDeclaration(rule);
                    templateRules.add(currentTemplateRule);
                }
                return true;
            }

            if (matchAngleComment(0)) continue;
            if (matchDollarComment(0)) continue;
            if (matchExpression()) continue;
            if (matchLiteral()) continue;

            // Nothing matched, go to the next token
            if(!nextToken()) return false;
        }
    }

    private boolean tryMatchTemplateAssign(ATEToken rule) {
        if (isID(0)) {
            unresolvedReferences.add(T(0));
            addReference(T(0));

            if (rule != null) {
                currentTemplateRule.end = T(0);
                rule.type = ATEStringTemplateSyntaxLexer.TOKEN_DECL;
                addDeclaration(rule);
                templateRules.add(currentTemplateRule);
            }

            nextToken();

            return true;
        }
        return false;
    }

    private boolean matchExpression() {
        if(T(0) == null || T(0).type != ATEStringTemplateSyntaxLexer.TOKEN_OPEN_SINGLE_ANGLE) return false;

        mark();
        int balance = 0;
        while(true) {
            T(0).scope = EXPR_BLOCK;
            if(T(0).type == ATEStringTemplateSyntaxLexer.TOKEN_OPEN_SINGLE_ANGLE)
                balance++;
            else if(T(0).type == ATEStringTemplateSyntaxLexer.TOKEN_CLOSE_SINGLE_ANGLE) {
                balance--;
                if(balance == 0) {
                    nextToken();
                    return true;
                }
            }
            // check if
            if(isID(0)) {
                // check if an arg, else check if it's a template rule or a map definition
                String refName = T(0).getAttribute();
                if (currentArgNames.contains(refName)) {
                    T(0).type = ATEStringTemplateSyntaxLexer.TOKEN_ARG_REFERENCE;
                } else {
                    unresolvedReferences.add(T(0));
                    addReference(T(0));
                }
                nextToken();
                while(isChar(0, ".") && isID(1)) {
                    if(!skip(2)) return false;
                }
            } else {
                if(!nextToken()) break;
            }
        }
        rewind();
        return false;
    }

    private boolean matchLiteral() {
        ATEToken t = T(0);
        if (t == null) return false;
        t.type = ATEStringTemplateSyntaxLexer.TOKEN_LITERAL;
        nextToken();
        return true;
    }

    private boolean matchArguments() {
        if(T(0) == null || T(0).type != ATESyntaxLexer.TOKEN_LPAREN) return false;

        mark();
        while(true) {
            T(0).scope = ARGUMENT_BLOCK;
            if(T(0).type == ATESyntaxLexer.TOKEN_RPAREN) {
                nextToken();
                return true;
            } else if (T(0).type == ATEStringTemplateSyntaxLexer.TOKEN_EQUAL) {
                nextToken();
                if (!matchTemplateString()) return false;
            }
            // check if
            if(isID(0)) {
                T(0).type = ATEStringTemplateSyntaxLexer.TOKEN_ARG_DECL;
                currentArgs.add(T(0));
                currentArgNames.add(T(0).getAttribute());
            }
            if(!nextToken()) break;
        }
        rewind();
        return false;
    }

    private boolean matchTemplateString() {
        mark();
        if(tryMatchTemplateString(null)) {
            return true;
        } else {
            rewind();
            return false;
        }
    }
    /**
     * Matches a template:
     *
     * templateNameID ::= '[' mapPairs ']'
     *
     * where
     *  mapPairs = keyValuePair (COMMA keyValuePair)* (COMMA 'default' COLON keyValue)?
     *  keyValuePair = STRING COLON (BIGSTRING | STRING | ID)
     *  BIGSTRING = '<<' ... '>>'
     *
     * @return true if a template is matched
     */
    private boolean matchMapDefinition() {
        mark();
        try {
                if(tryMapDefinition()) {
                return true;
            } else {
                rewind();
                return false;
            }
        } finally {
            currentTemplateRule = null;
        }
    }

    private boolean tryMapDefinition() {
        ATEToken start = T(0);
        if(start == null) return false;

        String name = start.getAttribute();
        if(!matchID(0)) return false;

        // should be '::=' right after id
        if(isDEFINED_TO_BE(0)) {
            // When a defineToBe is matched, we are at the beginning of the content of the template map
            nextToken();
        } else {
            // Invalid template map matching
            return false;
        }

        final ATEToken definedToBeToken = T(-1);
        currentTemplateMap = new ElementTemplateMapDefinition(name, start, definedToBeToken, null);

        // loop through all new lines
        while (matchNewline(0));

        if (!tryMatchMapDefinitionBody(start)) return false;

        return true;
    }

    private boolean tryMatchMapDefinitionBody(ATEToken rule) {
        if (!isLBRACK(0)) return false;

        while (true) {
            if (isRBRACK(0)) {
                nextToken();
                currentTemplateMap.end = T(-1);
                rule.type = ATEStringTemplateSyntaxLexer.TOKEN_MAP_DECL;
                addMapDefinition(rule);
                mapDefinitions.add(currentTemplateMap);
                return true;
            }

            if (matchSingleComment(0)) continue;
            if (matchComplexComment(0)) continue;
            if (matchValuePair()) continue;

            // Nothing matched, go to the next token
            if(!nextToken()) return false;
        }
    }

    private boolean matchValuePair() {
        matchSingleComment(0);
        matchComplexComment(0);

        if (!tryMatchKey()) return false;
        if (!matchCOLON(0)) return false;

        if (isOPEN_DOUBLE_ANGLE(0)) {
            if (!tryMatchTemplateBigString(null)) return false;
        } else if (isDOUBLE_QUOTE(0)) {
            if (!tryMatchTemplateString(null)) return false;
        } else {
            if (!tryMatchTemplateAssign(null)) return false;
        }

        matchSingleComment(0);
        matchComplexComment(0);

        while (matchCOMMA(0)) {
            if (!matchValuePair()) return false;
        }

        return true;
    }

    private boolean tryMatchKey() {
        if (matchID(0, "default")) return true;

        if (!isDOUBLE_QUOTE(0)) return false;

        ATEToken start = T(0);
        start.type = ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING;
        nextToken();

        while (true) {
            ATEToken t = T(0);
            if (isDOUBLE_QUOTE(0) || isNewline(0)) {
                t.type = ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING;
                nextToken();
                return true;
            }

            t.type = ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING;
            if (!nextToken()) return false;
        }
    }

    private void addReference(ATEToken ref) {
        refsToRules.put(ref, currentTemplateRule);
    }

    private void addDeclaration(ATEToken token) {
        decls.add(token);
        declaredReferenceNames.add(token.getAttribute());
    }

    private void addMapDefinition(ATEToken token) {
        maps.add(token);
        declaredMapNames.add(token.getAttribute());
    }

    /**
     * Matches all tokens until the balanced token's attribute is equal to close.
     * Expects the current token to be the open token (e.g. '{' whatever needs to be balanced)
     *
     * @param open The open attribute
     * @param close The close attribute
     * @param scope The scope to assign to the tokens between the two balanced tokens
     * @return true if the match succeeded
     */
    private boolean matchCommentScope(int open, int close, ATEScope scope) {
        if(T(0) == null || T(0).type != open) return false;

        mark();
        int balance = 0;
        while(true) {
            T(0).scope = scope;
            if(T(0).type == close) {
                nextToken();
                return true;
            }
            if(!nextToken()) break;
        }
        rewind();
        return false;
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

    private boolean matchCOLON(int index) {
        if(isCOLON(index)) {
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

    private boolean matchCOMMA(int index) {
        if(isCOMMA(index)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    private boolean matchDOUBLE_QUOTE(int index) {
        if(isDOUBLE_QUOTE(index)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    private boolean matchOPEN_DOUBLE_ANGLE(int index) {
        if(isOPEN_DOUBLE_ANGLE(index)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    private boolean matchCLOSE_DOUBLE_ANGLE(int index) {
        if(isCLOSE_DOUBLE_ANGLE(index)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    private boolean matchAngleComment(int index) {
        if(isAngleComment(index)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    private boolean matchDollarComment(int index) {
        if(isDollarComment(index)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    private boolean matchNewline(int index) {
        if(isNewline(index)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean matchSingleComment(int index) {
        if (!matchCommentScope(ATEStringTemplateSyntaxLexer.TOKEN_START_SINGLE_COMMENT,
                               ATEStringTemplateSyntaxLexer.TOKEN_NEWLINE,
                               COMMENT_SCOPE)) return false;
        return true;
    }

    @Override
    public boolean matchComplexComment(int index) {
        if (!matchCommentScope(ATEStringTemplateSyntaxLexer.TOKEN_START_COMPLEX_COMMENT,
                               ATEStringTemplateSyntaxLexer.TOKEN_END_COMPLEX_COMMENT,
                               COMMENT_SCOPE)) return false;
        return true;
    }

    private boolean isLPAREN(int index) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_LPAREN);
    }

    private boolean isSEMI(int index) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_SEMI);
    }

    private boolean isCOLON(int index) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_COLON);
    }

    private boolean isCOMMA(int index) {
        return isTokenType(index, ATEStringTemplateSyntaxLexer.TOKEN_COMMA);
    }

    private boolean isDEFINED_TO_BE(int index) {
        return isTokenType(index, ATEStringTemplateSyntaxLexer.TOKEN_DEFINED_TO_BE);
    }

    private boolean isDOUBLE_QUOTE(int index) {
        return isTokenType(index, ATEStringTemplateSyntaxLexer.TOKEN_DOUBLE_QUOTE);
    }

    private boolean isOPEN_DOUBLE_ANGLE(int index) {
        return isTokenType(index, ATEStringTemplateSyntaxLexer.TOKEN_OPEN_DOUBLE_ANGLE);
    }

    private boolean isLBRACK(int index) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_LBRACK);
    }

    private boolean isRBRACK(int index) {
        return isTokenType(index, ATESyntaxLexer.TOKEN_RBRACK);
    }

    private boolean isCLOSE_DOUBLE_ANGLE(int index) {
        return isTokenType(index, ATEStringTemplateSyntaxLexer.TOKEN_CLOSE_DOUBLE_ANGLE);
    }

    private boolean isAngleComment(int index) {
        return isTokenType(index, ATEStringTemplateSyntaxLexer.TOKEN_ANGLE_COMMENT);
    }

    private boolean isDollarComment(int index) {
        return isTokenType(index, ATEStringTemplateSyntaxLexer.TOKEN_DOLLAR_COMMENT);
    }

    private boolean isNewline(int index) {
        return isTokenType(index, ATEStringTemplateSyntaxLexer.TOKEN_NEWLINE);
    }

    private boolean isStartSingleComment(int index) {
        return isTokenType(index, ATEStringTemplateSyntaxLexer.TOKEN_START_SINGLE_COMMENT);
    }

    private boolean isStartComplexComment(int index) {
        return isTokenType(index, ATEStringTemplateSyntaxLexer.TOKEN_START_COMPLEX_COMMENT);
    }

    private boolean isEndComplexComment(int index) {
        return isTokenType(index, ATEStringTemplateSyntaxLexer.TOKEN_END_COMPLEX_COMMENT);
    }
}