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

package org.antlr.works.editor;

import edu.usfca.xj.appkit.swing.XJTree;
import edu.usfca.xj.appkit.swing.XJTreeDelegate;
import org.antlr.works.ate.swing.ATEKeyBindings;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.syntax.GrammarSyntaxEngine;
import org.antlr.works.syntax.GrammarSyntaxGroup;
import org.antlr.works.syntax.GrammarSyntaxReference;
import org.antlr.works.syntax.GrammarSyntaxRule;
import org.antlr.works.utils.IconManager;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class EditorRules implements XJTreeDelegate {

    protected CEditorGrammar editor;

    protected boolean programmaticallySelectingRule = false;
    protected boolean selectNextRule = false;

    protected XJTree rulesTree;
    protected DefaultMutableTreeNode rulesTreeRootNode;
    protected DefaultTreeModel rulesTreeModel;
    protected List rulesTreeExpandedNodes;
    protected RuleTreeUserObject selectedObject;

    protected boolean sort;

    public EditorRules(CEditorGrammar editor, XJTree rulesTree) {
        this.editor = editor;
        this.rulesTree = rulesTree;

        rulesTree.setDelegate(this);
        rulesTree.setEnableDragAndDrop();

        rulesTreeRootNode = new DefaultMutableTreeNode(new RuleTreeUserObject((GrammarSyntaxRule)null));
        rulesTreeModel = new DefaultTreeModel(rulesTreeRootNode);
        rulesTreeExpandedNodes = new ArrayList();

        rulesTree.setModel(rulesTreeModel);
        rulesTree.addMouseListener(new RuleTreeMouseListener());
        rulesTree.addTreeSelectionListener(new RuleTreeSelectionListener());

        rulesTree.setRootVisible(false);
        rulesTree.setShowsRootHandles(true);
        rulesTree.setCellRenderer(new RulesTableRenderer());
        rulesTree.setRowHeight(17);
        rulesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    }

    public void setKeyBindings(ATEKeyBindings keyBindings) {
        keyBindings.addKeyBinding("RULE_MOVE_UP", KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.CTRL_MASK),
                new RuleMoveUpAction());
        keyBindings.addKeyBinding("RULE_MOVE_DOWN", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.CTRL_MASK),
                new RuleMoveDownAction());
    }

    public GrammarSyntaxEngine getParserEngine() {
        return editor.getParserEngine();
    }

    public boolean isSorted() {
        return sort;
    }

    public void setSorted(boolean flag) {
        this.sort = flag;
    }

    public void toggleSorting() {
        sort = !sort;
        rebuildTree();
    }

    public void ignoreSelectedRules(boolean flag) {
        for (Iterator iterator = getSelectedRules().iterator(); iterator.hasNext();) {
            GrammarSyntaxRule r = (GrammarSyntaxRule) iterator.next();
            r.ignored = flag;
        }
        rulesTree.repaint();
        editor.rulesDidChange();
    }

    /** This method iterates over all rules and all blocks inside each rule to
     * find a sequence of token equals to "$channel=HIDDEN" or "skip()".
     */

    public void findTokensToIgnore() {
        List rules = getRules();
        if(rules == null || rules.isEmpty())
            return;

        ATESyntaxLexer lexer = new ATESyntaxLexer();
        for (Iterator ruleIter = rules.iterator(); ruleIter.hasNext();) {
            GrammarSyntaxRule rule = (GrammarSyntaxRule) ruleIter.next();
            List blocks = rule.getBlocks();
            if(blocks == null || blocks.isEmpty())
                continue;

            rule.ignored = false;

            for (Iterator blockIter = blocks.iterator(); blockIter.hasNext();) {
                ATEToken block = (ATEToken) blockIter.next();
                lexer.tokenize(block.getAttribute());

                List tokens = lexer.getTokens();
                for(int t=0; t<tokens.size(); t++) {
                    ATEToken token = (ATEToken)tokens.get(t);
                    if(token.type == ATESyntaxLexer.TOKEN_ID && token.getAttribute().equals("channel") && t+3 < tokens.size()) {
                        ATEToken t1 = (ATEToken)tokens.get(t+1);
                        ATEToken t2 = (ATEToken)tokens.get(t+2);
                        if(t1.type != ATESyntaxLexer.TOKEN_CHAR || !t1.getAttribute().equals("="))
                            continue;

                        if(t2.type != ATESyntaxLexer.TOKEN_ID || !t2.getAttribute().equals("HIDDEN"))
                            continue;

                        rule.ignored = true;
                        break;
                    }
                    if(token.type == ATESyntaxLexer.TOKEN_ID && token.getAttribute().equals("skip")) {
                        // Take skip() into account only if it is the only token in the block
                        if(tokens.size() == 6 && t == 1) {
                            rule.ignored = true;
                            break;                                                    
                        }
                    }
                }
            }
        }

        rulesTree.repaint();
    }

    public boolean getFirstSelectedRuleIgnoredFlag() {
        List selectedRules = getSelectedRules();
        if(selectedRules == null || selectedRules.isEmpty())
            return false;
        else
            return ((GrammarSyntaxRule)selectedRules.get(0)).ignored;
    }

    public class RuleMoveUpAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            GrammarSyntaxRule sourceRule = getEnclosingRuleAtPosition(editor.getCaretPosition());
            int previousRuleIndex = getParserEngine().getRules().indexOf(sourceRule)-1;
            if(previousRuleIndex>=0) {
                GrammarSyntaxRule targetRule = getParserEngine().getRuleAtIndex(previousRuleIndex);
                moveRule(sourceRule, targetRule, true);
            }
        }
    }

    public class RuleMoveDownAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            GrammarSyntaxRule targetRule = getEnclosingRuleAtPosition(editor.getCaretPosition());
            int nextRuleIndex = getParserEngine().getRules().indexOf(targetRule)+1;
            GrammarSyntaxRule sourceRule = getParserEngine().getRuleAtIndex(nextRuleIndex);
            if(sourceRule != null) {
                moveRule(sourceRule, targetRule, true);
                selectNextRule = true;
            }
        }
    }

    public void refreshRules() {
        saveExpandedNodes();
        rulesTreeModel.reload();
        restoreExpandedNodes();

        // Select again the row (otherwise, the selection is lost)
        selectRuleInTreeAtPosition(editor.getCaretPosition());
    }

    public GrammarSyntaxGroup getSelectedGroup() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)rulesTree.getSelectionPath().getLastPathComponent();
        RuleTreeUserObject n = (RuleTreeUserObject)node.getUserObject();
        if(n.group != null)
            return n.group;
       else
            return null;
    }

    public GrammarSyntaxGroup findOpenGroupClosestToLocation(int location) {
        // Look backward into the list of groups
        List groups = getParserEngine().getGroups();
        if(groups == null || groups.isEmpty())
            return null;

        GrammarSyntaxGroup previous = null;
        for(int index = 0; index < groups.size(); index++) {
            GrammarSyntaxGroup group = (GrammarSyntaxGroup)groups.get(index);
            if(!group.openGroup)
                continue;

            ATEToken t = group.token;
            if(t.getStartIndex() > location)
                break;

            previous = group;
        }
        return previous;
    }

    public GrammarSyntaxGroup findClosingGroupForGroup(GrammarSyntaxGroup group) {
        List groups = getParserEngine().getGroups();
        if(groups == null || groups.isEmpty())
            return null;

        int index = groups.indexOf(group)+1;
        if(index == -1)
            return null;

        int open = 0;
        while(index < groups.size()) {
            GrammarSyntaxGroup g = (GrammarSyntaxGroup) groups.get(index);
            if(g.openGroup)
                open++;
            else if(open == 0)
                return g;
            else
                open--;
            index++;
        }
        return null;
    }

    public List getRules() {
        return getParserEngine().getRules();
    }

    public List getSortedRules() {
        return getSortedRules(getRules());
    }
    
    public List getSortedRules(List rules) {
        if(rules == null)
            return null;

        List sortedRules = new ArrayList(rules);
        Collections.sort(sortedRules);
        if(!sortedRules.isEmpty()) {
            GrammarSyntaxRule firstRule = (GrammarSyntaxRule)sortedRules.get(0);
            if(firstRule.lexer) {
                for(int index=0; index<sortedRules.size(); index++) {
                    GrammarSyntaxRule rule = (GrammarSyntaxRule)sortedRules.get(0);
                    if(!rule.lexer)
                        break;

                    sortedRules.add(rule);
                    sortedRules.remove(0);
                }
            }
        }
        return sortedRules;
    }

    public List getSelectedRules() {
        List rules = new ArrayList(); // GrammarSyntaxRule objects
        for (Iterator iterator = rulesTree.getSelectedNodes().iterator(); iterator.hasNext();) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) iterator.next();
            RuleTreeUserObject o = (RuleTreeUserObject) node.getUserObject();
            if(o.rule != null)
                rules.add(o.rule);
        }
        return rules;
    }

    public GrammarSyntaxRule getLastRule() {
        List rules = getParserEngine().getRules();
        if(rules != null && !rules.isEmpty())
            return (GrammarSyntaxRule)rules.get(rules.size()-1);
        else
        return null;
    }

    public GrammarSyntaxRule getLastParserRule() {
        List rules = getParserEngine().getRules();
        for(int index = rules.size()-1; index>0; index--) {
            GrammarSyntaxRule rule = (GrammarSyntaxRule)rules.get(index);
            if(!rule.lexer)
                return rule;
        }
        return null;
    }

    public GrammarSyntaxRule getLastLexerRule() {
        List rules = getParserEngine().getRules();
        for(int index = rules.size()-1; index>0; index--) {
            GrammarSyntaxRule rule = (GrammarSyntaxRule)rules.get(index);
            if(rule.lexer)
                return rule;
        }
        return null;
    }

    public GrammarSyntaxRule getRuleWithName(String name) {
        List rules = getParserEngine().getRules();
        for(int index=0; index<rules.size(); index++) {
            GrammarSyntaxRule r = (GrammarSyntaxRule)rules.get(index);
            if(r.name.equals(name))
                return r;
        }
        return null;
    }

    public List getRulesStartingWith(String match) {
        List matches = new ArrayList();
        if(getParserEngine().getRules() == null)
            return matches;

        List rules = getParserEngine().getRules();
        for(int index=0; index<rules.size(); index++) {
            GrammarSyntaxRule r = (GrammarSyntaxRule)rules.get(index);
            String rname = r.name.toLowerCase();
            if(rname.startsWith(match) && !matches.contains(r.name))
                matches.add(r.name);
        }
        return matches;
    }

    public List getReferencesInRule(GrammarSyntaxRule rule) {
        if(getParserEngine().getRules() == null)
            return null;

        List refs = new ArrayList();
        for(Iterator iterator = getParserEngine().getReferences().iterator(); iterator.hasNext(); ) {
            GrammarSyntaxReference r = (GrammarSyntaxReference)iterator.next();
            if(r.rule == rule)
                refs.add(r);
        }
        return refs;
    }

    public GrammarSyntaxRule getEnclosingRuleAtPosition(int pos) {
        if(getParserEngine().getRules() == null)
            return null;

        Iterator iterator = getParserEngine().getRules().iterator();
        while(iterator.hasNext()) {
            GrammarSyntaxRule r = (GrammarSyntaxRule)iterator.next();
            if(r.containsIndex(pos))
                return r;
        }
        return null;
    }

    public GrammarSyntaxRule selectRuleInTreeAtPosition(int pos) {
        if(programmaticallySelectingRule || getParserEngine().getRules() == null)
            return null;

        programmaticallySelectingRule = true;
        GrammarSyntaxRule rule = getEnclosingRuleAtPosition(pos);
        selectRuleInTree(rule);
        programmaticallySelectingRule = false;
        return rule;
    }

    public GrammarSyntaxRule selectRuleNameInTree(String name) {
        if(programmaticallySelectingRule || getParserEngine().getRules() == null)
            return null;

        GrammarSyntaxRule rule = null;
        programmaticallySelectingRule = true;
        Iterator iterator = getParserEngine().getRules().iterator();
        while(iterator.hasNext()) {
            GrammarSyntaxRule r = (GrammarSyntaxRule)iterator.next();
            if(r.name.equals(name)) {
                selectRuleInTree(r);
                rule = r;
                break;
            }
        }
        programmaticallySelectingRule = false;
        return rule;
    }

    public GrammarSyntaxRule getRuleAtIndex(int index) {
        if(getParserEngine().getRules() == null)
            return null;

        Iterator iterator = getParserEngine().getRules().iterator();
        while(iterator.hasNext()) {
            GrammarSyntaxRule r = (GrammarSyntaxRule)iterator.next();
            if(index >= r.getStartIndex() && index <= r.getEndIndex())
                return r;
        }
        return null;
    }

    public boolean isRuleAtIndex(int index) {
        return getRuleAtIndex(index) != null;
    }

    public void selectNextRule() {
        GrammarSyntaxRule rule = getEnclosingRuleAtPosition(editor.getCaretPosition());
        int index = getParserEngine().getRules().indexOf(rule)+1;
        rule = getParserEngine().getRuleAtIndex(index);
        if(rule != null) {
            editor.setCaretPosition(rule.getStartIndex());
            editor.rulesCaretPositionDidChange();
        }
    }

    public void selectRuleInTree(GrammarSyntaxRule rule) {
        if(rule == null)
            return;

        Enumeration enumeration = rulesTreeRootNode.depthFirstEnumeration();
        while(enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)enumeration.nextElement();
            RuleTreeUserObject n = (RuleTreeUserObject)node.getUserObject();
            if(n != null && n.rule == rule) {
                TreePath path = new TreePath(node.getPath());
                rulesTree.setSelectionPath(path);
                rulesTree.scrollPathToVisible(path);
                break;
            }
        }
    }

    public void goToRule(GrammarSyntaxRule rule) {
        editor.setCaretPosition(rule.start.getStartIndex());
    }

    public void rebuildTree() {
        saveExpandedNodes();
        rememberSelectedTreeItem();

        rulesTreeRootNode.removeAllChildren();

        List rules = getParserEngine().getRules();
        List groups = getParserEngine().getGroups();
        if(rules == null || groups == null)
            return;

        if(groups.isEmpty()) {
            buildTree(rulesTreeRootNode, rules, 0, rules.size()-1);
        } else {
            Stack parentStack = new Stack();
            parentStack.add(rulesTreeRootNode);

            int ruleIndex = 0;
            for(int index=0; index<groups.size(); index++) {
                GrammarSyntaxGroup group = (GrammarSyntaxGroup)groups.get(index);

                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)parentStack.peek();
                if(group.ruleIndex >= 0) {
                    buildTree(parentNode, rules, ruleIndex, group.ruleIndex);
                    ruleIndex = group.ruleIndex+1;
                }

                if(group.openGroup) {
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(new RuleTreeUserObject(group));
                    parentNode.add(node);
                    parentStack.push(node);
                } else {
                    if(parentStack.size()>1)
                        parentStack.pop();
                }
            }

            if(ruleIndex < rules.size()) {
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)parentStack.peek();
                buildTree(parentNode, rules, ruleIndex, rules.size()-1);
            }
        }

        rulesTreeModel.reload();
        
        restoreSelectedTreeItem();
        restoreExpandedNodes();
    }

    public void rememberSelectedTreeItem() {
        DefaultMutableTreeNode node = rulesTree.getSelectedNode();
        selectedObject = null;
        if(node != null)
            selectedObject = (RuleTreeUserObject)node.getUserObject();
    }

    public void restoreSelectedTreeItem() {
        if(selectedObject == null)
            return;

        DefaultMutableTreeNode node = findNodeWithRuleName(selectedObject.rule.name);
        if(node == null)
            return;

        programmaticallySelectingRule = true;
        TreePath path = new TreePath(node.getPath());
        rulesTree.setSelectionPath(path);
        rulesTree.scrollPathToVisible(path);
        programmaticallySelectingRule = false;
    }

    public void saveExpandedNodes() {
        rulesTreeExpandedNodes.clear();
        Enumeration e = rulesTreeRootNode.depthFirstEnumeration();
        while(e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
            if(!node.isLeaf() && !node.isRoot() && rulesTree.isExpanded(new TreePath(node.getPath()))) {
                RuleTreeUserObject n = (RuleTreeUserObject)node.getUserObject();
                rulesTreeExpandedNodes.add(n.group.name);
            }
        }
    }

    public void restoreExpandedNodes() {
        Iterator iterator = rulesTreeExpandedNodes.iterator();
        while(iterator.hasNext()) {
            String groupName = (String)iterator.next();
            DefaultMutableTreeNode node = findNodeWithGroupName(groupName);
            if(node != null)
                rulesTree.expandPath(new TreePath(node.getPath()));
        }
    }

    public DefaultMutableTreeNode findNodeWithGroupName(String groupName) {
        Enumeration e = rulesTreeRootNode.depthFirstEnumeration();
        while(e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
            RuleTreeUserObject n = (RuleTreeUserObject)node.getUserObject();
            if(n.group != null && n.group.name.equalsIgnoreCase(groupName))
                return node;
        }
        return null;
    }

    public DefaultMutableTreeNode findNodeWithRuleName(String ruleName) {
        Enumeration e = rulesTreeRootNode.depthFirstEnumeration();
        while(e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
            RuleTreeUserObject n = (RuleTreeUserObject)node.getUserObject();
            if(n.rule != null && n.rule.name.equalsIgnoreCase(ruleName))
                return node;
        }
        return null;
    }

    protected void buildTree(DefaultMutableTreeNode parentNode, List rules, int from, int to) {
        // Sort the list of subrules
        List subrules = rules.subList(from, to+1);
        if(sort && !subrules.isEmpty()) {
            subrules = getSortedRules(subrules);
            Collections.sort(subrules);
            GrammarSyntaxRule firstRule = (GrammarSyntaxRule)subrules.get(0);
            if(firstRule.lexer) {
                for(int index=0; index<subrules.size(); index++) {
                    GrammarSyntaxRule rule = (GrammarSyntaxRule)subrules.get(0);
                    if(!rule.lexer)
                        break;

                    subrules.add(rule);
                    subrules.remove(0);
                }
            }
        }

        for(int index=0; index<subrules.size(); index++) {
            GrammarSyntaxRule rule = (GrammarSyntaxRule) subrules.get(index);
            parentNode.add(new DefaultMutableTreeNode(new RuleTreeUserObject(rule)));
        }
    }

    public void parserDidParse() {
        rebuildTree();
        if(selectNextRule) {
            // Can be set by RuleMoveDown() class when a rule is moved down. Selection has to occurs here
            // after rules have been parsed. We use this flag to select the next rule instead of the current one.
            selectNextRule = false;
            selectNextRule();
        } else
            selectRuleInTreeAtPosition(editor.getCaretPosition());
    }

    public boolean moveRule(GrammarSyntaxRule sourceRule, GrammarSyntaxRule targetRule, boolean dropAbove) {
        if(sourceRule == null || targetRule == null)
            return false;
        
        String sourceRuleText = editor.getText().substring(sourceRule.getStartIndex(), sourceRule.getEndIndex()+1);

        try {
            Document doc = editor.getTextPane().getDocument();

            int removeStartIndex = sourceRule.getStartIndex();
            int targetInsertionIndex = dropAbove ? targetRule.getStartIndex() : targetRule.getEndIndex();

            // should move at the line after the rule (a comment can be located
            // after the end of the rule but still on the same line)
            
            // Remove one more character to remove the end of line of the rule
            int removeLength = sourceRule.getLength()+1;
            if(removeStartIndex+removeLength > doc.getLength())
                removeLength--;

            if(sourceRule.getStartIndex()>targetRule.getStartIndex()) {
                doc.remove(removeStartIndex, removeLength);
                doc.insertString(targetInsertionIndex, "\n"+sourceRuleText, null);
                editor.setCaretPosition(targetInsertionIndex);
            } else {
                doc.insertString(targetInsertionIndex, "\n"+sourceRuleText, null);
                doc.remove(removeStartIndex, removeLength);
                editor.setCaretPosition(targetInsertionIndex-removeLength);
            }
            return true;
        } catch (BadLocationException e) {
            editor.console.print(e);
            return false;
        }
    }

    public void selectRuleFromUserAction() {
        // Do not select the rule if the selection changes
        // was triggered programmatically instead than by the user
        if(programmaticallySelectingRule)
            return;

        /** Select a rule only if there is one selected row
         */
        if(rulesTree.getSelectionCount() != 1)
            return;

        TreePath selPath[] = rulesTree.getSelectionPaths();
        if(selPath == null)
            return;

        List selRules = new ArrayList();
        for(int path=0; path<selPath.length; path++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)selPath[path].getLastPathComponent();
            RuleTreeUserObject n = (RuleTreeUserObject)node.getUserObject();
            if(n.rule != null)
                selRules.add(n.rule);
        }

        if(!selRules.isEmpty()) {
            goToRule((GrammarSyntaxRule)selRules.get(0));
            // Request focus because it was lost when moving the caret in the document
            rulesTree.requestFocusInWindow();
        }
    }

    public Cursor xjTreeDragSourceDefaultCursor(XJTree tree) {
        return DragSource.DefaultMoveDrop;
    }

    public int xjTreeDragAndDropConstants(XJTree tree) {
        return DnDConstants.ACTION_MOVE;
    }

    public boolean xjTreeDrop(XJTree tree, Object sourceObject, Object targetObject, int dropLocation) {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_DROP_RULE);

        GrammarSyntaxRule sourceRule = ((EditorRules.RuleTreeUserObject) sourceObject).rule;
        GrammarSyntaxRule targetRule = ((EditorRules.RuleTreeUserObject) targetObject).rule;

        return moveRule(sourceRule, targetRule, dropLocation == XJTree.DROP_ABOVE);
    }

    public class RulesTableRenderer extends DefaultTreeCellRenderer {

        public Component getTreeCellRendererComponent(
                            JTree tree,
                            Object value,
                            boolean sel,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus)
        {
            Component r = super.getTreeCellRendererComponent(
                            tree, value, sel,
                            expanded, leaf, row,
                            hasFocus);

            setIcon(null);
            setToolTipText("");

            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            RuleTreeUserObject n = (RuleTreeUserObject)node.getUserObject();
            if(n.rule != null) {
                if(n.rule.lexer)
                    setIcon(IconManager.shared().getIconLexer());
                else
                    setIcon(IconManager.shared().getIconParser());                
            }

            setFont(getFont().deriveFont(Font.PLAIN));
            setForeground(Color.black);

            if(n.rule != null) {
                if(n.rule.hasErrors()) {
                    setForeground(Color.red);
                    setToolTipText(n.rule.getErrorMessageHTML());
                }
                if(n.rule.ignored)
                    setFont(getFont().deriveFont(Font.ITALIC));
            }
            
            return r;
        }
    }

    public class RuleTreeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            selectRuleFromUserAction();
        }
    }

    public class RuleTreeMouseListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            selectRuleFromUserAction();
            checkForPopupTrigger(e);
        }

        public void mouseReleased(MouseEvent e) {
            checkForPopupTrigger(e);
        }

        public void checkForPopupTrigger(MouseEvent e) {
            if(e.isPopupTrigger()) {
                rulesTree.modifySelectionIfNecessary(e);

                List selectedObjects = new ArrayList(); // RuleTreeUserObject objects
                for (Iterator iterator = rulesTree.getSelectedNodes().iterator(); iterator.hasNext();) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) iterator.next();
                    selectedObjects.add(node.getUserObject());
                }
                JPopupMenu menu = editor.rulesGetContextualMenu(selectedObjects);
                if(menu != null)
                    menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

    }

    public class RuleTreeUserObject implements Transferable {

        public GrammarSyntaxRule rule;
        public GrammarSyntaxGroup group;

        public RuleTreeUserObject(GrammarSyntaxRule rule) {
            this.rule = rule;
        }

        public RuleTreeUserObject(GrammarSyntaxGroup group) {
            this.group = group;
        }

        public String toString() {
            if(group != null)
                return group.name;
            else if(rule != null)
                return rule.name;
            else
                return "";
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[0];
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return false;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return null;
        }
    }

}
