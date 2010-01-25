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

import org.antlr.works.ate.swing.ATEKeyBindings;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.GrammarWindow;
import org.antlr.works.grammar.element.ElementAction;
import org.antlr.works.grammar.element.ElementGroup;
import org.antlr.works.grammar.element.ElementReference;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.works.grammar.syntax.GrammarSyntaxLexer;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.utils.IconManager;
import org.antlr.xjlib.appkit.swing.XJTree;
import org.antlr.xjlib.appkit.swing.XJTreeDelegate;
import org.antlr.xjlib.foundation.XJSystem;

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
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class EditorRules implements XJTreeDelegate {

    protected GrammarWindow window;

    protected boolean programmaticallySelectingRule = false;
    protected boolean selectNextRule = false;

    protected XJTree rulesTree;
    protected DefaultMutableTreeNode rulesTreeRootNode;
    protected DefaultTreeModel rulesTreeModel;
    protected List<String> rulesTreeExpandedNodes;
    protected RuleTreeUserObject selectedObject;

    protected boolean sort;

    private TreeSelectionListener tsl;
    private MouseListener ml;
    private JScrollPane rulesScrollPane;

    public EditorRules(GrammarWindow window) {
        this.window = window;

        rulesTree = new RuleTree();
        rulesTree.setBorder(null);
        // Apparently, if I don't set the tooltip here, nothing is displayed (weird)
        rulesTree.setToolTipText("");
        rulesTree.setDragEnabled(true);
        rulesScrollPane = new JScrollPane(rulesTree);
        rulesScrollPane.setWheelScrollingEnabled(true);

        rulesTree.setDelegate(this);
        rulesTree.setEnableDragAndDrop();

        rulesTreeRootNode = new DefaultMutableTreeNode(new RuleTreeUserObject(window));
        rulesTreeModel = new DefaultTreeModel(rulesTreeRootNode);
        rulesTreeExpandedNodes = new ArrayList<String>();

        rulesTree.setModel(rulesTreeModel);
        rulesTree.addMouseListener(ml = new RuleTreeMouseListener());
        rulesTree.addTreeSelectionListener(tsl = new RuleTreeSelectionListener());

        rulesTree.setRootVisible(true);
        rulesTree.setCellRenderer(new RulesTableRenderer());
        rulesTree.setRowHeight(17);
        rulesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        setSorted(AWPrefs.getPreferences().getBoolean(AWPrefs.PREF_TOOLBAR_SORT, false));        
    }

    public void close() {
        window = null;
        rulesTree.close();
        rulesTree.removeMouseListener(ml);
        rulesTree.removeTreeSelectionListener(tsl);
        rulesTree.setCellRenderer(null);
        rulesTree.setDelegate(null);
        rulesTree = null;
    }

    public JScrollPane getComponent() {
        return rulesScrollPane;
    }

    public void setKeyBindings(ATEKeyBindings keyBindings) {
        keyBindings.addKeyBinding("RULE_MOVE_UP", KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.CTRL_MASK),
                new RuleMoveUpAction());
        keyBindings.addKeyBinding("RULE_MOVE_DOWN", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.CTRL_MASK),
                new RuleMoveDownAction());
    }

    public GrammarEngine getGrammarEngine() {
        return window.getGrammarEngine();
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
        for (ElementRule r : getSelectedRules()) {
            r.ignored = flag;
        }
        rulesTree.repaint();
        window.rulesDidChange();
    }

    /**
     * This method iterates over all rules and all blocks inside each rule to
     * find a sequence of token equals to "$channel=HIDDEN" or "skip()".
     */
    public void findTokensToIgnore(boolean reset) {
        List<ElementRule> rules = getRules();
        if(rules == null || rules.isEmpty())
            return;

        findTokensToIgnore(rules, reset);

        rulesTree.repaint();
    }

    public static void findTokensToIgnore(List<ElementRule> rules, boolean reset) {
        for (ElementRule rule : rules) {
            if(reset)
                rule.ignored = false;

            List<ElementAction> actions = rule.getActions();
            if (actions == null || actions.isEmpty())
                continue;

            for (ElementAction action : actions) {
                List<ATEToken> tokens = action.getTokens();
                for (int t = 0; t < tokens.size(); t++) {
                    ATEToken token = tokens.get(t);
                    /* the 'channel' token can be either an ID or a reference if a rule in the grammar has the name
                   'channel' */
                    if ((token.type == ATESyntaxLexer.TOKEN_ID || token.type == GrammarSyntaxLexer.TOKEN_REFERENCE)
                            && token.getAttribute().equals("channel") && t + 3 < tokens.size())
                    {
                        ATEToken t1 = tokens.get(t + 1);
                        ATEToken t2 = tokens.get(t + 2);
                        if (t1.type != ATESyntaxLexer.TOKEN_CHAR || !t1.getAttribute().equals("="))
                            continue;

                        if (t2.type != ATESyntaxLexer.TOKEN_ID || !t2.getAttribute().equals("HIDDEN"))
                            continue;

                        rule.ignored = true;
                        break;
                    }
                    if (token.type == GrammarSyntaxLexer.TOKEN_ID && token.getAttribute().equals("skip")) {
                        // Take skip() into account only if it is the only token in the block
                        if (tokens.size() == 5 && t == 1) {
                            rule.ignored = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    public boolean getFirstSelectedRuleIgnoredFlag() {
        List<ElementRule> selectedRules = getSelectedRules();
        return !(selectedRules == null || selectedRules.isEmpty()) && (selectedRules.get(0)).ignored;
    }

    public class RuleMoveUpAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            ElementRule sourceRule = getEnclosingRuleAtPosition(window.getCaretPosition());
            int previousRuleIndex = getGrammarEngine().getRules().indexOf(sourceRule)-1;
            if(previousRuleIndex>=0) {
                ElementRule targetRule = getGrammarEngine().getRuleAtIndex(previousRuleIndex);
                moveRule(sourceRule, targetRule, true);
            }
        }
    }

    public class RuleMoveDownAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            ElementRule targetRule = getEnclosingRuleAtPosition(window.getCaretPosition());
            int nextRuleIndex = getGrammarEngine().getRules().indexOf(targetRule)+1;
            ElementRule sourceRule = getGrammarEngine().getRuleAtIndex(nextRuleIndex);
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
        selectRuleInTreeAtPosition(window.getCaretPosition());
    }

    public ElementGroup getSelectedGroup() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)rulesTree.getSelectionPath().getLastPathComponent();
        RuleTreeUserObject n = (RuleTreeUserObject)node.getUserObject();
        if(n.group != null)
            return n.group;
        else
            return null;
    }

    public ElementGroup findOpenGroupClosestToLocation(int location) {
        // Look backward into the list of groups
        List<ElementGroup> groups = getGrammarEngine().getGroups();
        if(groups == null || groups.isEmpty())
            return null;

        ElementGroup previous = null;
        for (ElementGroup group : groups) {
            if (!group.openGroup)
                continue;

            ATEToken t = group.token;
            if (t.getStartIndex() > location)
                break;

            previous = group;
        }
        return previous;
    }

    public ElementGroup findClosingGroupForGroup(ElementGroup group) {
        List<ElementGroup> groups = getGrammarEngine().getGroups();
        if(groups == null || groups.isEmpty())
            return null;

        int index = groups.indexOf(group)+1;
        if(index == -1)
            return null;

        int open = 0;
        while(index < groups.size()) {
            ElementGroup g = groups.get(index);
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

    public List<ElementRule> getRules() {
        return getGrammarEngine().getRules();
    }

    public List<ElementRule> getSortedRules() {
        return getSortedRules(getRules());
    }

    public List<ElementRule> getSortedRules(List<ElementRule> rules) {
        if(rules == null)
            return null;

        List<ElementRule> sortedRules = new ArrayList<ElementRule>(rules);
        Collections.sort(sortedRules);
        if(!sortedRules.isEmpty()) {
            ElementRule firstRule = sortedRules.get(0);
            if(firstRule.lexer) {
                for(int index=0; index<sortedRules.size(); index++) {
                    ElementRule rule = sortedRules.get(0);
                    if(!rule.lexer)
                        break;

                    sortedRules.add(rule);
                    sortedRules.remove(0);
                }
            }
        }
        return sortedRules;
    }

    public List<ElementRule> getSelectedRules() {
        List<ElementRule> rules = new ArrayList<ElementRule>(); // GrammarSyntaxRule objects
        for (Object o1 : rulesTree.getSelectedNodes()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) o1;
            RuleTreeUserObject o = (RuleTreeUserObject) node.getUserObject();
            if (o.rule != null)
                rules.add(o.rule);
        }
        return rules;
    }

    public ElementRule getLastRule() {
        List<ElementRule> rules = getGrammarEngine().getRules();
        if(rules != null && !rules.isEmpty())
            return rules.get(rules.size()-1);
        else
            return null;
    }

    public ElementRule getLastParserRule() {
        List<ElementRule> rules = getGrammarEngine().getRules();
        for(int index = rules.size()-1; index>0; index--) {
            ElementRule rule = rules.get(index);
            if(!rule.lexer)
                return rule;
        }
        return null;
    }

    public ElementRule getLastLexerRule() {
        List<ElementRule> rules = getGrammarEngine().getRules();
        for(int index = rules.size()-1; index>0; index--) {
            ElementRule rule = rules.get(index);
            if(rule.lexer)
                return rule;
        }
        return null;
    }

    public List<String> getRulesStartingWith(String match) {
        List<String> matches = new ArrayList<String>();
        if(getGrammarEngine().getRules() == null)
            return matches;

        List<ElementRule> rules = getGrammarEngine().getRules();
        for (ElementRule r : rules) {
            String rname = r.name.toLowerCase();
            if (rname.startsWith(match) && !matches.contains(r.name))
                matches.add(r.name);
        }
        return matches;
    }

    public List<ElementReference> getReferencesInRule(ElementRule rule) {
        if(getGrammarEngine().getRules() == null)
            return null;

        List<ElementReference> refs = new ArrayList<ElementReference>();
        for (ElementReference r : getGrammarEngine().getReferences()) {
            if (r.rule == rule)
                refs.add(r);
        }
        return refs;
    }

    public ElementRule getEnclosingRuleAtPosition(int pos) {
        if(getGrammarEngine().getRules() == null)
            return null;

        for (ElementRule r : getGrammarEngine().getRules()) {
            if (r.containsIndex(pos))
                return r;
        }
        return null;
    }

    public ElementRule selectRuleInTreeAtPosition(int pos) {
        if(programmaticallySelectingRule || getGrammarEngine().getRules() == null)
            return null;

        programmaticallySelectingRule = true;
        ElementRule rule = getEnclosingRuleAtPosition(pos);
        selectRuleInTree(rule);
        programmaticallySelectingRule = false;
        return rule;
    }

    public ElementRule selectRuleNameInTree(String name) {
        if(programmaticallySelectingRule || getGrammarEngine().getRules() == null)
            return null;

        ElementRule rule = null;
        programmaticallySelectingRule = true;
        for (ElementRule r : getGrammarEngine().getRules()) {
            if (r.name.equals(name)) {
                selectRuleInTree(r);
                rule = r;
                break;
            }
        }
        programmaticallySelectingRule = false;
        return rule;
    }

    public ElementRule getRuleAtIndex(int index) {
        if(getGrammarEngine().getRules() == null)
            return null;

        for (ElementRule r : getGrammarEngine().getRules()) {
            if (index >= r.getStartIndex() && index <= r.getEndIndex())
                return r;
        }
        return null;
    }

    public boolean isRuleAtIndex(int index) {
        return getRuleAtIndex(index) != null;
    }

    public void selectNextRule() {
        ElementRule rule = getEnclosingRuleAtPosition(window.getCaretPosition());
        int index = getGrammarEngine().getRules().indexOf(rule)+1;
        rule = getGrammarEngine().getRuleAtIndex(index);
        if(rule != null) {
            window.setCaretPosition(rule.getStartIndex());
            window.rulesCaretPositionDidChange();
        }
    }

    public void selectRuleInTree(ElementRule rule) {
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

    public void goToRule(ElementRule rule) {
        window.goToHistoryRememberCurrentPosition();
        window.setCaretPosition(rule.start.getStartIndex());
    }

    public void rebuildTree() {
        saveExpandedNodes();
        rememberSelectedTreeItem();

        rulesTreeRootNode.removeAllChildren();

        List<ElementRule> rules = getGrammarEngine().getRules();
        List<ElementGroup> groups = getGrammarEngine().getGroups();
        if(rules == null || groups == null)
            return;

        if(groups.isEmpty()) {
            buildTree(rulesTreeRootNode, rules, 0, rules.size()-1);
        } else {
            Stack<DefaultMutableTreeNode> parentStack = new Stack<DefaultMutableTreeNode>();
            parentStack.add(rulesTreeRootNode);

            int ruleIndex = 0;
            for (ElementGroup group : groups) {
                DefaultMutableTreeNode parentNode = parentStack.peek();
                if (group.ruleIndex >= 0) {
                    buildTree(parentNode, rules, ruleIndex, group.ruleIndex);
                    ruleIndex = group.ruleIndex + 1;
                }

                if (group.openGroup) {
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(new RuleTreeUserObject(group));
                    parentNode.add(node);
                    parentStack.push(node);
                } else {
                    if (parentStack.size() > 1)
                        parentStack.pop();
                }
            }

            if(ruleIndex < rules.size()) {
                DefaultMutableTreeNode parentNode = parentStack.peek();
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
        for (String groupName : rulesTreeExpandedNodes) {
            DefaultMutableTreeNode node = findNodeWithGroupName(groupName);
            if (node != null)
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

    protected void buildTree(DefaultMutableTreeNode parentNode, List<ElementRule> rules, int from, int to) {
        // Sort the list of subrules
        List<ElementRule> subrules = rules.subList(from, to+1);
        if(sort && !subrules.isEmpty()) {
            subrules = getSortedRules(subrules);
            Collections.sort(subrules);
            ElementRule firstRule = subrules.get(0);
            if(firstRule.lexer) {
                for(int index=0; index<subrules.size(); index++) {
                    ElementRule rule = subrules.get(0);
                    if(!rule.lexer)
                        break;

                    subrules.add(rule);
                    subrules.remove(0);
                }
            }
        }

        for (ElementRule rule : subrules) {
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
            selectRuleInTreeAtPosition(window.getCaretPosition());
    }

    public boolean moveRule(ElementRule sourceRule, ElementRule targetRule, boolean dropAbove) {
        if(sourceRule == null || targetRule == null)
            return false;

        String sourceRuleText = window.getText().substring(sourceRule.getStartIndex(), sourceRule.getEndIndex()+1);

        try {
            Document doc = window.getTextPane().getDocument();

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
                window.setCaretPosition(targetInsertionIndex);
            } else {
                doc.insertString(targetInsertionIndex, "\n"+sourceRuleText, null);
                doc.remove(removeStartIndex, removeLength);
                window.setCaretPosition(targetInsertionIndex-removeLength);
            }
            return true;
        } catch (BadLocationException e) {
            window.consoleTab.println(e);
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

        List<ElementRule> selRules = new ArrayList<ElementRule>();
        for (TreePath aSelPath : selPath) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) aSelPath.getLastPathComponent();
            RuleTreeUserObject n = (RuleTreeUserObject) node.getUserObject();
            if (n.rule != null)
                selRules.add(n.rule);
        }

        if(!selRules.isEmpty()) {
            goToRule(selRules.get(0));
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

        ElementRule sourceRule = ((EditorRules.RuleTreeUserObject) sourceObject).rule;
        ElementRule targetRule = ((EditorRules.RuleTreeUserObject) targetObject).rule;

        return moveRule(sourceRule, targetRule, dropLocation == XJTree.DROP_ABOVE);
    }

    public static final Color HIGHLIGHTED_COLOR = new Color(0, 0.5f, 1, 0.4f);

    public static class RulesTableRenderer extends DefaultTreeCellRenderer {

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

            // FIX AW-5
            if(XJSystem.isWindows()) {
                setBackgroundSelectionColor(HIGHLIGHTED_COLOR);
            }

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

                List<Object> selectedObjects = new ArrayList<Object>(); // RuleTreeUserObject objects
                for (Object o : rulesTree.getSelectedNodes()) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                    selectedObjects.add(node.getUserObject());
                }
                JPopupMenu menu = window.rulesGetContextualMenu(selectedObjects);
                if(menu != null)
                    menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

    }

    public static class RuleTreeUserObject implements Transferable {

        public GrammarWindow editor;
        public ElementRule rule;
        public ElementGroup group;

        public RuleTreeUserObject(ElementRule rule) {
            this.rule = rule;
        }

        public RuleTreeUserObject(ElementGroup group) {
            this.group = group;
        }

        public RuleTreeUserObject(GrammarWindow editor) {
            this.editor = editor;
        }

        public String toString() {
            if(group != null)
                return group.name;
            else if(rule != null)
                return rule.name;
            else if(editor != null && editor.getDocument() != null) {
                return editor.getDocument().getDocumentName();
            } else {
                return "";
            }
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

    public static class RuleTree extends XJTree {

        @Override
        public String getToolTipText(MouseEvent e) {
            TreePath path = getPathForLocation(e.getX(), e.getY());
            if(path == null)
                return "";

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            RuleTreeUserObject n = (RuleTreeUserObject) node.getUserObject();
            if(n == null)
                return "";

            ElementRule r = n.rule;
            if(r == null || !r.hasErrors())
                return "";
            else
                return r.getErrorMessageHTML();
        }
    }
}
