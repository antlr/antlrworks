package org.antlr.works.grammar.element;

import org.antlr.works.ate.folding.ATEFoldingEntity;
import org.antlr.works.ate.gutter.ATEGutterItem;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.editor.EditorPersistentObject;
import org.antlr.works.grammar.antlr.GrammarError;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.works.grammar.syntax.GrammarSyntaxParser;
import org.antlr.works.utils.IconManager;

import javax.swing.*;
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

public class ElementRule extends ElementScopable implements Comparable, EditorPersistentObject, ATEFoldingEntity, ATEGutterItem {

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

    public boolean hierarchyAnalyzed = false;

    public List<GrammarError> errors;
    public boolean needsToBuildErrors = true;

    protected GrammarSyntaxParser parser;

    protected int refsStartIndex = -1;
    protected int refsEndIndex = -1;

    protected int blocksStartIndex = -1;
    protected int blocksEndIndex = -1;

    protected int actionsStartIndex = -1;
    protected int actionsEndIndex = -1;

    private GrammarEngine engine;

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

    public void resetHierarchy() {
        hierarchyAnalyzed = false;        
    }

    public GrammarEngine getEngine() {
        return engine;
    }

    public void setEngine(GrammarEngine engine) {
        this.engine = engine;
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
        for (ATEToken token : getTokens()) {
            if (findColon) {
                if (token.getAttribute().equals(":")) {
                    findColon = false;
                    alt = new ArrayList<ATEToken>();
                }
            } else {
                if (token.getAttribute().equals("("))
                    level++;
                else if (token.getAttribute().equals(")"))
                    level--;
                else if (level == 0) { // removed token.type != GrammarSyntaxLexer.TOKEN_BLOCK &&
                    if (token.getAttribute().equals("|")) {
                        alts.add(alt);
                        alt = new ArrayList<ATEToken>();
                        continue;
                    }
                }
                alt.add(token);
            }
        }
        if(alt != null && !alt.isEmpty()) {
            alts.add(alt);
        }
        return alts;
    }

    public void setErrors(List<GrammarError> errors) {
        this.errors = errors;
    }

    public List<GrammarError> getErrors() {
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
        StringBuilder head = new StringBuilder();
        StringBuilder star = new StringBuilder();

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

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(head);
        sb.append(")");
        if(star.length() > 0) {
            sb.append(" (");
            sb.append(star);
            sb.append(")*");
        }

        return sb.toString();
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public void setNeedsToBuildErrors(boolean flag) {
        this.needsToBuildErrors = flag;
    }

    public boolean needsToBuildErrors() {
        return needsToBuildErrors;
    }

    public String getErrorMessageString(int index) {
        GrammarError error = errors.get(index);
        return error.messageText;
    }

    public String getErrorMessageHTML() {
        StringBuilder message = new StringBuilder();
        message.append("<html>");
        for (Iterator<GrammarError> iterator = errors.iterator(); iterator.hasNext();) {
            GrammarError error = iterator.next();
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

    public int getItemIndex() {
        return getStartIndex();
    }

    public void setItemIndex(int index) {        
        start.start = index;
    }

    public static final int ITEM_TYPE_OVERRIDE = 1;
    public static final int ITEM_TYPE_OVERRIDDEN = 2;

    public boolean override = false;
    public List<String> overrideGrammars;
    public boolean isOverridden = false;
    public List<String> overriddenGrammars;

    public List<Integer> types = new ArrayList<Integer>();

    private void analyzeHierarchy() {
        // Look at the grammar this rule overrides
        overrideGrammars = engine.getGrammarsOverriddenByRule(name);
        override = !overrideGrammars.isEmpty();

        // Look at the grammar this rule is overridden by
        overriddenGrammars = engine.getGrammarsOverridingRule(name);
        isOverridden = !overriddenGrammars.isEmpty();
    }

    public synchronized List<Integer> getItemTypes() {
        if(!hierarchyAnalyzed) {
            analyzeHierarchy();
            hierarchyAnalyzed = true;

            types.clear();
            if(override) {
                types.add(ITEM_TYPE_OVERRIDE);
            }
            if(isOverridden) {
                types.add(ITEM_TYPE_OVERRIDDEN);                
            }
        }
        return types;
    }

    public int getItemWidth() {
        int width = 0;
        for(int type : getItemTypes()) {
            width += getItemIcon(type).getIconWidth();
        }
        return width;
    }

    public int getItemHeight() {
        int height = 0;
        for(int type : getItemTypes()) {
            height = Math.max(height, getItemIcon(type).getIconHeight());
        }
        return height;
    }

    public ImageIcon getItemIcon(int type) {
        if(type == ITEM_TYPE_OVERRIDE) {
            return IconManager.shared().getIconOverride();
        }
        if(type == ITEM_TYPE_OVERRIDDEN) {
            return IconManager.shared().getIconOverridden();
        }
        return null;
    }

    public String getItemTooltip(int type) {
        if(type == ITEM_TYPE_OVERRIDE) {
            return "Overrides rule in "+overrideGrammars;
        }
        if(type == ITEM_TYPE_OVERRIDDEN) {
            return "Is overridden in "+overriddenGrammars;
        }
        return null;
    }

    public void itemAction(int type) {
        switch (type) {
            case ITEM_TYPE_OVERRIDE:
                engine.gotoToRule(overrideGrammars.get(0), name);
                break;
            case ITEM_TYPE_OVERRIDDEN:
                engine.gotoToRule(overriddenGrammars.get(0), name);
                break;
        }
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
