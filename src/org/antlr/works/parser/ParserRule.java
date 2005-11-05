package org.antlr.works.parser;

import org.antlr.works.editor.ate.ATEBreakpointEntity;
import org.antlr.works.editor.ate.ATEFoldingEntity;
import org.antlr.works.editor.helper.PersistentObject;
import org.antlr.works.visualization.grammar.GrammarEngineError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

public class ParserRule implements Comparable, PersistentObject, ATEFoldingEntity, ATEBreakpointEntity {

    public String name;
    public Token start;
    public Token colon;
    public Token end;

    public boolean expanded = true;
    public boolean breakpoint;

    public boolean isAllUpperCase = false;
    public boolean hasLeftRecursion = false;

    public List errors;
    protected Parser parser;

    protected int refsStartIndex = -1;
    protected int refsEndIndex = -1;

    public ParserRule(Parser parser, String name, Token start, Token colon, Token end) {
        this.parser = parser;
        this.name = name;
        this.start = start;
        this.colon = colon;
        this.end = end;
        this.isAllUpperCase = name.equals(name.toUpperCase());
    }

    public void completed() {
        // Called when the rule has been completely parsed
        this.hasLeftRecursion = detectLeftRecursion();
    }

    public void setReferencesIndexes(int startIndex, int endIndex) {
        this.refsStartIndex = Math.max(0, startIndex);
        this.refsEndIndex = endIndex;
    }

    public List getReferences() {
        if(refsStartIndex != -1 && refsEndIndex != -1)
            return parser.references.subList(refsStartIndex, refsEndIndex+1);
        else
            return null;
    }

    public int getStartIndex() {
        return start.getStartIndex();
    }

    public int getEndIndex() {
        return end.getEndIndex();
    }

    public int getLength() {
        return getEndIndex()-getStartIndex();
    }

    public int getInternalTokensStartIndex() {
        for(Iterator iter = getTokens().iterator(); iter.hasNext(); ) {
            Token token = (Token)iter.next();
            if(token.getAttribute().equals(":")) {
                token = (Token)iter.next();
                return token.getStartIndex();
            }
        }
        return -1;
    }

    public int getInternalTokensEndIndex() {
        Token token = (Token)parser.tokens.get(end.index-1);
        return token.getEndIndex();
    }

    public List getBlocks() {
        List blocks = new ArrayList();
        Token lastToken = null;
        for(int index=start.index; index<end.index; index++) {
            Token token = (Token)parser.tokens.get(index);
            if(token.type == Lexer.TOKEN_BLOCK) {
                if(lastToken != null && lastToken.type == Lexer.TOKEN_ID && lastToken.getAttribute().equals("options"))
                    continue;

                blocks.add(token);
            }
            lastToken = token;
        }
        return blocks;
    }

    public List getTokens() {
        List t = new ArrayList();
        for(int index=start.index; index<end.index; index++) {
            t.add(parser.tokens.get(index));
        }
        return t;
    }

    public List getAlternatives() {
        List alts = new ArrayList();
        List alt = null;
        boolean findColon = true;
        int level = 0;
        for(Iterator iter = getTokens().iterator(); iter.hasNext(); ) {
            Token token = (Token)iter.next();
            if(findColon) {
                if(token.getAttribute().equals(":")) {
                    findColon = false;
                    alt = new ArrayList();
                }
            } else {
                if(token.getAttribute().equals("("))
                    level++;
                else if(token.getAttribute().equals(")"))
                    level--;
                else if(token.type != Lexer.TOKEN_BLOCK && level == 0) {
                    if(token.getAttribute().equals("|")) {
                        alts.add(alt);
                        alt = new ArrayList();
                        continue;
                    }
                }
                alt.add(token);
            }
        }
        if(alt != null && !alt.isEmpty())
            alts.add(alt);
        return alts;
    }

    public void setErrors(List errors) {
        this.errors = errors;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean isLexerRule() {
        return isAllUpperCase;
    }

    public boolean hasLeftRecursion() {
        return hasLeftRecursion;
    }

    public boolean detectLeftRecursion() {
        for(Iterator iter = getAlternatives().iterator(); iter.hasNext(); ) {
            List alts = (List)iter.next();
            if(alts.isEmpty())
                continue;

            Token firstTokenInAlt = (Token)alts.get(0);
            if(firstTokenInAlt.getAttribute().equals(name))
                return true;
        }
        return false;
    }

    public String getTextRuleAfterRemovingLeftRecursion() {
        StringBuffer head = new StringBuffer();
        StringBuffer star = new StringBuffer();

        for(Iterator iter = getAlternatives().iterator(); iter.hasNext(); ) {
            List alts = (List)iter.next();
            Token firstTokenInAlt = (Token)alts.get(0);
            if(firstTokenInAlt.getAttribute().equals(name)) {
                if(alts.size() > 1) {
                    if(star.length() > 0)
                        star.append(" | ");
                    int start = ((Token)alts.get(1)).getStartIndex();
                    int end = ((Token)alts.get(alts.size()-1)).getEndIndex();
                    star.append(firstTokenInAlt.text.substring(start, end));
                }
            } else {
                if(head.length() > 0)
                    head.append(" | ");
                int start = firstTokenInAlt.getStartIndex();
                int end = ((Token)alts.get(alts.size()-1)).getEndIndex();
                head.append(firstTokenInAlt.text.substring(start, end));
            }
        }

        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append(head);
        sb.append(") ");
        sb.append("(");
        sb.append(star);
        sb.append(")*");

        return sb.toString();
    }

    public boolean hasErrors() {
        if(errors == null)
            return false;
        else
            return !errors.isEmpty();
    }

    public String getErrorMessageString(int index) {
        GrammarEngineError error = (GrammarEngineError) errors.get(index);
        return error.message;
    }

    public String getErrorMessageHTML() {
        StringBuffer message = new StringBuffer();
        message.append("<html>");
        for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
            GrammarEngineError error = (GrammarEngineError) iterator.next();
            message.append(error.message);
            if(iterator.hasNext())
                message.append("<br>");
        }
        message.append("</html>");
        return message.toString();
    }

    public String toString() {
        return name;
    }

    public boolean containsIndex(int index) {
        return index >= getStartIndex() && index <= getEndIndex();
    }
    
    public int compareTo(Object o) {
        ParserRule otherRule = (ParserRule) o;
        return this.name.compareTo(otherRule.name);
    }

    public int getUniqueIdentifier() {
        return name.hashCode();
    }

    public boolean canBeCollapsed() {
        return colon.startLineNumber <= end.startLineNumber - 1;
    }

    public void foldingEntitySetExpanded(boolean expanded) {
        setExpanded(expanded);
    }

    public boolean foldingEntityIsExpanded() {
        return isExpanded();
    }

    public boolean foldingEntityCanBeCollapsed() {
        return canBeCollapsed();
    }

    public int foldingEntityGetStartParagraphIndex() {
        return getStartIndex();
    }

    public int foldingEntityGetStartIndex() {
        return colon.getStartIndex();
    }

    public int foldingEntityGetEndIndex() {
        return getEndIndex();
    }

    public int foldingEntityGetStartLine() {
        return colon.startLineNumber;
    }

    public int foldingEntityGetEndLine() {
        return end.endLineNumber;
    }

    public String foldingEntityPlaceholderString() {
        return ": ... ;";
    }

    public String foldingEntityID() {
        return String.valueOf(getUniqueIdentifier());
    }

    public int foldingEntityLevel() {
        return 0;
    }

    public int breakpointEntityUniqueID() {
        return getUniqueIdentifier();
    }

    public int breakpointEntityIndex() {
        return getStartIndex();
    }

    public int breakpointEntityLine() {
        return start.startLineNumber;
    }

    public void breakpointEntitySetBreakpoint(boolean flag) {
        this.breakpoint = flag;
    }

    public boolean breakpointEntityIsBreakpoint() {
        return breakpoint;
    }

    public Object getPersistentID() {
        return new Integer(getUniqueIdentifier());
    }

    public void persistentAssign(PersistentObject otherObject) {
        ParserRule oldRule = (ParserRule)otherObject;
        this.expanded = oldRule.expanded;
        this.breakpoint = oldRule.breakpoint;
    }

}
