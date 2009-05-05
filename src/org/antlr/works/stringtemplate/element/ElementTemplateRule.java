package org.antlr.works.stringtemplate.element;

import org.antlr.works.ate.folding.ATEFoldingEntity;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.stringtemplate.syntax.ATEStringTemplateSyntaxLexer;
import org.antlr.works.stringtemplate.syntax.ATEStringTemplateSyntaxParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

public class ElementTemplateRule extends ElementTemplateScopable implements Comparable, ATEFoldingEntity {

    public String name;
    public ATEToken start;
    public ATEToken definedToBe;
    public ATEToken end;

    public List<ATEToken> args;

    public boolean expanded = true;

    protected ATEStringTemplateSyntaxParser parser;

    public ElementTemplateRule(String name) {
        this.name = name;
    }

    public ElementTemplateRule(ATEStringTemplateSyntaxParser parser, String name, ATEToken start, ATEToken definedToBe, ATEToken end, List<ATEToken> args) {
        this.parser = parser;
        this.name = name;
        this.start = start;
        this.definedToBe = definedToBe;
        this.end = end;
        this.args = args;
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
            if(token.type == ATEStringTemplateSyntaxLexer.TOKEN_DEFINED_TO_BE) {
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

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public String toString() {
        return name;
    }

    public boolean containsIndex(int index) {
        return index >= getStartIndex() && index <= getEndIndex();
    }

    public int compareTo(Object o) {
        ElementTemplateRule otherRule = (ElementTemplateRule) o;
        return this.name.compareTo(otherRule.name);
    }

    public int getUniqueIdentifier() {
        return name.hashCode();
    }

    public boolean canBeCollapsed() {
        return definedToBe.startLineNumber <= end.startLineNumber - 1;
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
        return definedToBe.getStartIndex();
    }

    public int foldingEntityGetEndIndex() {
        return getEndIndex();
    }

    public int foldingEntityGetStartLine() {
        return definedToBe.startLineNumber;
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

    public int getItemIndex() {
        return getStartIndex();
    }

    public void setItemIndex(int index) {
        start.start = index;
    }
}