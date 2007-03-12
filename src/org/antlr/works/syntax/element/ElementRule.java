package org.antlr.works.syntax.element;

import org.antlr.works.ate.breakpoint.ATEBreakpointEntity;
import org.antlr.works.ate.folding.ATEFoldingEntity;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.editor.EditorPersistentObject;
import org.antlr.works.grammar.EngineGrammarError;
import org.antlr.works.syntax.GrammarSyntaxParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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

public class ElementRule extends ElementScopable implements Comparable, EditorPersistentObject, ATEFoldingEntity, ATEBreakpointEntity {

    public String name;
    public ATEToken start;
    public ATEToken colon;
    public ATEToken end;

    public boolean ignored = false;
    public boolean expanded = true;
    public boolean breakpoint = false;

    public boolean lexer = false;

    // Flag if a rule has a single left recursion that can be removed by ANTLRWorks
    public boolean hasLeftRecursion = false;
    public boolean leftRecursionAnalyzed = false;

    // Set of rules that are mutually left recursive (cannot be fixed by ANTLRWorks)
    public Set leftRecursiveRulesSet;

    public List<EngineGrammarError> errors;
    public boolean needsToBuildErrors = true;

    protected GrammarSyntaxParser parser;

    protected int refsStartIndex = -1;
    protected int refsEndIndex = -1;

    protected int blocksStartIndex = -1;
    protected int blocksEndIndex = -1;

    protected int actionsStartIndex = -1;
    protected int actionsEndIndex = -1;

    public ElementRule(String name) {
        this.name = name;
        this.lexer = ATEToken.isLexerName(name);
    }

    public ElementRule(GrammarSyntaxParser parser, String name, ATEToken start, ATEToken colon, ATEToken end) {
        this.parser = parser;
        this.name = name;
        this.start = start;
        this.colon = colon;
        this.end = end;
        this.lexer = ATEToken.isLexerName(name);
    }

    public void completed() {
        // Called when the rule has been completely parsed
        // Do not analyze the left recursion now, but on-demand.
        leftRecursionAnalyzed = false;
    }

    public void setReferencesIndexes(int startIndex, int endIndex) {
        this.refsStartIndex = Math.max(0, startIndex);
        this.refsEndIndex = endIndex;
    }

    public List<ElementReference> getReferences() {
        if(refsStartIndex != -1 && refsEndIndex != -1)
            return parser.references.subList(refsStartIndex, refsEndIndex+1);
        else
            return null;
    }

    public void setBlocksIndexes(int startIndex, int endIndex) {
        this.blocksStartIndex = Math.max(0, startIndex);
        this.blocksEndIndex = endIndex;
    }

    public List<ElementBlock> getBlocks() {
        if(blocksStartIndex != -1 && blocksEndIndex != -1)
            return parser.blocks.subList(blocksStartIndex, blocksEndIndex+1);
        else
            return null;
    }

    public void setActionsIndexes(int startIndex, int endIndex) {
        this.actionsStartIndex = Math.max(0, startIndex);
        this.actionsEndIndex = endIndex;
    }

    public List<ElementAction> getActions() {
        if(actionsStartIndex != -1 && actionsEndIndex != -1)
            return parser.actions.subList(actionsStartIndex, actionsEndIndex+1);
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
        for(Iterator<ATEToken> iter = getTokens().iterator(); iter.hasNext(); ) {
            ATEToken token = iter.next();
            if(token.getAttribute().equals(":")) {
                token = iter.next();
                return token.getStartIndex();
            }
        }
        return -1;
    }

    public int getInternalTokensEndIndex() {
        ATEToken token = parser.getTokens().get(end.index-1);
        return token.getEndIndex();
    }

    public List<ATEToken> getTokens() {
        List<ATEToken> t = new ArrayList<ATEToken>();
        for(int index=start.index; index<end.index; index++) {
            t.add(parser.getTokens().get(index));
        }
        return t;
    }

    public List<List<ATEToken>> getAlternatives() {
        List<List<ATEToken>> alts = new ArrayList<List<ATEToken>>();
        List<ATEToken> alt = null;
        boolean findColon = true;
        int level = 0;
        // todo check
/*        for(Iterator iter = getTokens().iterator(); iter.hasNext(); ) {
            ATEToken token = (ATEToken)iter.next();
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
                else if(token.type != GrammarSyntaxLexer.TOKEN_BLOCK && level == 0) {
                    if(token.getAttribute().equals("|")) {
                        alts.add(alt);
                        alt = new ArrayList();
                        continue;
                    }
                }
                alt.add(token);
            }
        }*/
        if(alt != null && !alt.isEmpty())
            alts.add(alt);
        return alts;
    }

    public void setErrors(List<EngineGrammarError> errors) {
        this.errors = errors;
    }

    public List<EngineGrammarError> getErrors() {
        return errors;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setLeftRecursiveRulesSet(Set rulesSet) {
        leftRecursiveRulesSet = rulesSet;
    }

    public Set getLeftRecursiveRulesSet() {
        return leftRecursiveRulesSet;
    }

    public boolean hasLeftRecursion() {
        if(!leftRecursionAnalyzed) {
            leftRecursionAnalyzed = true;
            hasLeftRecursion = detectLeftRecursion();
        }
        return hasLeftRecursion;
    }

    public boolean detectLeftRecursion() {
        for (List<ATEToken> alts : getAlternatives()) {
            if (alts.isEmpty())
                continue;

            ATEToken firstTokenInAlt = alts.get(0);
            if (firstTokenInAlt.getAttribute().equals(name))
                return true;
        }
        return false;
    }

    public String getTextRuleAfterRemovingLeftRecursion() {
        StringBuffer head = new StringBuffer();
        StringBuffer star = new StringBuffer();

        for (List<ATEToken> alts : getAlternatives()) {
            ATEToken firstTokenInAlt = alts.get(0);
            if (firstTokenInAlt.getAttribute().equals(name)) {
                if (alts.size() > 1) {
                    if (star.length() > 0)
                        star.append(" | ");
                    int start = (alts.get(1)).getStartIndex();
                    int end = (alts.get(alts.size() - 1)).getEndIndex();
                    star.append(firstTokenInAlt.getText().substring(start, end));
                }
            } else {
                if (head.length() > 0)
                    head.append(" | ");
                int start = firstTokenInAlt.getStartIndex();
                int end = (alts.get(alts.size() - 1)).getEndIndex();
                head.append(firstTokenInAlt.getText().substring(start, end));
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

    public void setNeedsToBuildErrors(boolean flag) {
        this.needsToBuildErrors = flag;
    }

    public boolean needsToBuildErrors() {
        return needsToBuildErrors;
    }

    public String getErrorMessageString(int index) {
        EngineGrammarError error = errors.get(index);
        return error.messageText;
    }

    public String getErrorMessageHTML() {
        StringBuffer message = new StringBuffer();
        message.append("<html>");
        for (Iterator<EngineGrammarError> iterator = errors.iterator(); iterator.hasNext();) {
            EngineGrammarError error = iterator.next();
            message.append(error.messageText);
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
        ElementRule otherRule = (ElementRule) o;
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
        return getUniqueIdentifier();
    }

    public void persistentAssign(EditorPersistentObject otherObject) {
        ElementRule oldRule = (ElementRule)otherObject;
        this.ignored = oldRule.ignored;
        this.expanded = oldRule.expanded;
        this.breakpoint = oldRule.breakpoint;
        this.leftRecursiveRulesSet = oldRule.leftRecursiveRulesSet;
    }

}
