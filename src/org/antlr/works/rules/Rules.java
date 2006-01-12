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

package org.antlr.works.rules;

import edu.usfca.xj.appkit.swing.XJTree;
import edu.usfca.xj.appkit.swing.XJTreeDelegate;
import org.antlr.works.ate.ATEFoldingEntity;
import org.antlr.works.ate.syntax.ATEParserEngine;
import org.antlr.works.ate.syntax.ATEToken;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.editor.EditorKeyBindings;
import org.antlr.works.parser.ParserGroup;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.stats.Statistics;
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

public class Rules implements XJTreeDelegate {

    protected CEditorGrammar editor;
    protected ATEParserEngine parserEngine;
    protected RulesDelegate delegate;

    protected boolean programmaticallySelectingRule = false;
    protected boolean selectNextRule = false;

    protected XJTree rulesTree;
    protected DefaultMutableTreeNode rulesTreeRootNode;
    protected DefaultTreeModel rulesTreeModel;
    protected List rulesTreeExpandedNodes;
    protected RuleTreeUserObject selectedObject;

    protected boolean sort;

    public Rules(CEditorGrammar editor, ATEParserEngine parserEngine, XJTree rulesTree) {
        this.editor = editor;
        this.parserEngine = parserEngine;
        this.rulesTree = rulesTree;

        rulesTree.setDelegate(this);
        rulesTree.setEnableDragAndDrop();

        rulesTreeRootNode = new DefaultMutableTreeNode(new RuleTreeUserObject((ParserRule)null));
        rulesTreeModel = new DefaultTreeModel(rulesTreeRootNode);
        rulesTreeExpandedNodes = new ArrayList();

        rulesTree.setModel(rulesTreeModel);
        rulesTree.addMouseListener(new RuleTreeMouseListener());
        rulesTree.addTreeSelectionListener(new RuleTreeSelectionListener());

        rulesTree.setRootVisible(false);
        rulesTree.setShowsRootHandles(true);
        rulesTree.setCellRenderer(new CustomTableRenderer());
        rulesTree.setRowHeight(17);
        rulesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    }

    public void setDelegate(RulesDelegate delegate) {
        this.delegate = delegate;
    }

    public void setKeyBindings(EditorKeyBindings keyBindings) {
        keyBindings.addKeyBinding("RULE_MOVE_UP", KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.CTRL_MASK),
                new RuleMoveUpAction());
        keyBindings.addKeyBinding("RULE_MOVE_DOWN", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.CTRL_MASK),
                new RuleMoveDownAction());
    }

    public boolean isSorted() {
        return sort;
    }

    public void toggleSorting() {
        sort = !sort;
        rebuildTree();
    }

    public class RuleMoveUpAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            ParserRule sourceRule = getEnclosingRuleAtPosition(editor.getCaretPosition());
            int previousRuleIndex = parserEngine.getRules().indexOf(sourceRule)-1;
            if(previousRuleIndex>=0) {
                ParserRule targetRule = parserEngine.getRuleAtIndex(previousRuleIndex);
                moveRule(sourceRule, targetRule, true);
            }
        }
    }

    public class RuleMoveDownAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            ParserRule targetRule = getEnclosingRuleAtPosition(editor.getCaretPosition());
            int nextRuleIndex = parserEngine.getRules().indexOf(targetRule)+1;
            ParserRule sourceRule = parserEngine.getRuleAtIndex(nextRuleIndex);
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

    public ParserGroup getSelectedGroup() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)rulesTree.getSelectionPath().getLastPathComponent();
        RuleTreeUserObject n = (RuleTreeUserObject)node.getUserObject();
        if(n.group != null)
            return n.group;
       else
            return null;
    }

    public ParserGroup findOpenGroupClosestToLocation(int location) {
        // Look backward into the list of groups
        List groups = parserEngine.getGroups();
        if(groups == null || groups.isEmpty())
            return null;

        ParserGroup previous = null;
        for(int index = 0; index < groups.size(); index++) {
            ParserGroup group = (ParserGroup)groups.get(index);
            if(!group.openGroup)
                continue;

            ATEToken t = group.token;
            if(t.getStartIndex() > location)
                break;

            previous = group;
        }
        return previous;
    }

    public ParserGroup findClosingGroupForGroup(ParserGroup group) {
        List groups = parserEngine.getGroups();
        if(groups == null || groups.isEmpty())
            return null;

        int index = groups.indexOf(group)+1;
        if(index == -1)
            return null;

        int open = 0;
        while(index < groups.size()) {
            ParserGroup g = (ParserGroup) groups.get(index);
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
        return parserEngine.getRules();
    }

    public List getSortedRules() {
        return getSortedRules(getRules());
    }
    
    public List getSortedRules(List rules) {
        List sortedRules = new ArrayList(rules);
        Collections.sort(sortedRules);
        ParserRule firstRule = (ParserRule)sortedRules.get(0);
        if(firstRule.lexer) {
            for(int index=0; index<sortedRules.size(); index++) {
                ParserRule rule = (ParserRule)sortedRules.get(0);
                if(!rule.lexer)
                    break;

                sortedRules.add(rule);
                sortedRules.remove(0);
            }
        }
        return sortedRules;
    }

    public ParserRule getLastRule() {
        List rules = parserEngine.getRules();
        if(rules != null && !rules.isEmpty())
            return (ParserRule)rules.get(rules.size()-1);
        else
        return null;
    }

    public ParserRule getLastParserRule() {
        List rules = parserEngine.getRules();
        for(int index = rules.size()-1; index>0; index--) {
            ParserRule rule = (ParserRule)rules.get(index);
            if(!rule.lexer)
                return rule;
        }
        return null;
    }

    public ParserRule getLastLexerRule() {
        List rules = parserEngine.getRules();
        for(int index = rules.size()-1; index>0; index--) {
            ParserRule rule = (ParserRule)rules.get(index);
            if(rule.lexer)
                return rule;
        }
        return null;
    }

    public ParserRule getRuleWithName(String name) {
        List rules = parserEngine.getRules();
        for(int index=0; index<rules.size(); index++) {
            ParserRule r = (ParserRule)rules.get(index);
            if(r.name.equals(name))
                return r;
        }
        return null;
    }

    public List getRulesStartingWith(String match) {
        List matches = new ArrayList();
        if(parserEngine.getRules() == null)
            return matches;

        List rules = parserEngine.getRules();
        for(int index=0; index<rules.size(); index++) {
            ParserRule r = (ParserRule)rules.get(index);
            String rname = r.name.toLowerCase();
            if(rname.startsWith(match) && !matches.contains(r.name))
                matches.add(r.name);
        }
        return matches;
    }

    public ParserRule getEnclosingRuleAtPosition(int pos) {
        if(parserEngine.getRules() == null)
            return null;

        Iterator iterator = parserEngine.getRules().iterator();
        while(iterator.hasNext()) {
            ParserRule r = (ParserRule)iterator.next();
            if(r.containsIndex(pos))
                return r;
        }
        return null;
    }

    public ParserRule selectRuleInTreeAtPosition(int pos) {
        if(programmaticallySelectingRule || parserEngine.getRules() == null)
            return null;

        programmaticallySelectingRule = true;
        ParserRule rule = getEnclosingRuleAtPosition(pos);
        selectRuleInTree(rule);
        programmaticallySelectingRule = false;
        return rule;
    }

    public ParserRule selectRuleNameInTree(String name) {
        if(programmaticallySelectingRule || parserEngine.getRules() == null)
            return null;

        ParserRule rule = null;
        programmaticallySelectingRule = true;
        Iterator iterator = parserEngine.getRules().iterator();
        while(iterator.hasNext()) {
            ParserRule r = (ParserRule)iterator.next();
            if(r.name.equals(name)) {
                selectRuleInTree(r);
                rule = r;
                break;
            }
        }
        programmaticallySelectingRule = false;
        return rule;
    }

    public boolean isRuleName(String name) {
        Iterator iterator = parserEngine.getRules().iterator();
        while(iterator.hasNext()) {
            ParserRule r = (ParserRule)iterator.next();
            if(r.name.equals(name))
                return true;
        }
        return false;
    }

    public ParserRule getRuleAtIndex(int index) {
        if(parserEngine.getRules() == null)
            return null;

        Iterator iterator = parserEngine.getRules().iterator();
        while(iterator.hasNext()) {
            ParserRule r = (ParserRule)iterator.next();
            if(index >= r.getStartIndex() && index <= r.getEndIndex())
                return r;
        }
        return null;
    }

    public ParserRule getRuleStartingWithToken(ATEToken startToken) {
        if(parserEngine.getRules() == null)
            return null;

        for(Iterator iterator = parserEngine.getRules().iterator(); iterator.hasNext(); ) {
            ParserRule r = (ParserRule)iterator.next();
            if(r.start == startToken)
                return r;
        }
        return null;
    }

    public boolean isRuleAtIndex(int index) {
        return getRuleAtIndex(index) != null;
    }

    public void selectFirstRule() {
        if(parserEngine.getRules() == null || parserEngine.getRules().size() == 0)
            return;

        programmaticallySelectingRule = true;
        ParserRule r = (ParserRule)parserEngine.getRules().get(0);
        selectRuleInTree(r);
        goToRule(r);
        programmaticallySelectingRule = false;
    }

    public void selectNextRule() {
        ParserRule rule = getEnclosingRuleAtPosition(editor.getCaretPosition());
        int index = parserEngine.getRules().indexOf(rule)+1;
        rule = parserEngine.getRuleAtIndex(index);
        if(rule != null) {
            editor.setCaretPosition(rule.getStartIndex());
            delegate.rulesCaretPositionDidChange();
        }
    }

    public void selectRuleInTree(ParserRule rule) {
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

    public void goToRule(ParserRule rule) {
        editor.setCaretPosition(rule.start.getStartIndex());
    }

    public void rebuildTree() {
        saveExpandedNodes();
        rememberSelectedTreeItem();

        rulesTreeRootNode.removeAllChildren();

        List rules = parserEngine.getRules();
        List groups = parserEngine.getGroups();
        if(groups.isEmpty()) {
            buildTree(rulesTreeRootNode, rules, 0, rules.size()-1);
        } else {
            Stack parentStack = new Stack();
            parentStack.add(rulesTreeRootNode);

            int ruleIndex = 0;
            for(int index=0; index<groups.size(); index++) {
                ParserGroup group = (ParserGroup)groups.get(index);

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
            ParserRule firstRule = (ParserRule)subrules.get(0);
            if(firstRule.lexer) {
                for(int index=0; index<subrules.size(); index++) {
                    ParserRule rule = (ParserRule)subrules.get(0);
                    if(!rule.lexer)
                        break;

                    subrules.add(rule);
                    subrules.remove(0);
                }
            }
        }

        for(int index=0; index<subrules.size(); index++) {
            ParserRule rule = (ParserRule) subrules.get(index);
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

    public boolean moveRule(ParserRule sourceRule, ParserRule targetRule, boolean dropAbove) {
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
            goToRule((ParserRule)selRules.get(0));
            // Request focus because it was lost when moving the caret in the document
            rulesTree.requestFocus();
        }
    }

    public Cursor xjTreeDragSourceDefaultCursor(XJTree tree) {
        return DragSource.DefaultMoveDrop;
    }

    public int xjTreeDragAndDropConstants(XJTree tree) {
        return DnDConstants.ACTION_MOVE;
    }

    public boolean xjTreeDrop(XJTree tree, Object sourceObject, Object targetObject, int dropLocation) {
        Statistics.shared().recordEvent(Statistics.EVENT_DROP_RULE);

        ParserRule sourceRule = ((Rules.RuleTreeUserObject) sourceObject).rule;
        ParserRule targetRule = ((Rules.RuleTreeUserObject) targetObject).rule;

        return moveRule(sourceRule, targetRule, dropLocation == XJTree.DROP_ABOVE);
    }

    public ATEFoldingEntity getEntityForKey(Object key) {
        if(parserEngine.getRules() == null)
            return null;

        for(Iterator iterator = parserEngine.getRules().iterator(); iterator.hasNext(); ) {
            ParserRule rule = (ParserRule)iterator.next();
            if(rule.name.equals(key))
                return rule;
        }
        return null;
    }

    public class CustomTableRenderer extends DefaultTreeCellRenderer {

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

            // @todo setFont is really slow (profiler)
            if(n.rule != null && n.rule.hasErrors()) {
                setForeground(Color.red);
                //setFont(getFont().deriveFont(Font.BOLD));
                setToolTipText(n.rule.getErrorMessageHTML());
            } else {
                setForeground(Color.black);
                //setFont(getFont().deriveFont(Font.PLAIN));
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
        }
    }

    public class RuleTreeUserObject implements Transferable {

        public ParserRule rule;
        public ParserGroup group;

        public RuleTreeUserObject(ParserRule rule) {
            this.rule = rule;
        }

        public RuleTreeUserObject(ParserGroup group) {
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
