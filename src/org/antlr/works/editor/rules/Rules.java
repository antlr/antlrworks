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

package org.antlr.works.editor.rules;

import edu.usfca.xj.appkit.swing.XJTree;
import edu.usfca.xj.appkit.swing.XJTreeDelegate;
import org.antlr.works.editor.helper.KeyBindings;
import org.antlr.works.editor.tool.TActions;
import org.antlr.works.parser.*;
import org.antlr.works.stats.Statistics;
import org.antlr.works.util.IconManager;

import javax.swing.*;
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

public class Rules implements ThreadedParserObserver, XJTreeDelegate {

    private RulesDelegate delegate = null;
    private ThreadedParser parser = null;
    private TActions actions = null;

    private List duplicateRules = null;
    private List undefinedRules = null;

    private boolean selectingRule = false;
    private boolean skipParseRules = false;
    private boolean selectNextRule = false;

    private JTextPane textPane;

    private JTree rulesTree;
    private DefaultMutableTreeNode rulesTreeRootNode;
    private DefaultTreeModel rulesTreeModel;
    private List rulesTreeExpandedNodes;

    public Rules(ThreadedParser parser, JTextPane textPane, XJTree rulesTree) {
        this.parser = parser;
        this.textPane = textPane;
        this.rulesTree = rulesTree;

        duplicateRules = new ArrayList();
        undefinedRules = new ArrayList();

        rulesTree.setDelegate(this);
        rulesTree.setEnableDragAndDrop();

        rulesTreeRootNode = new DefaultMutableTreeNode(new RuleTreeUserObject(-1));
        rulesTreeModel = new DefaultTreeModel(rulesTreeRootNode);
        rulesTreeExpandedNodes = new ArrayList();

        rulesTree.setModel(rulesTreeModel);
        rulesTree.addMouseListener(new RuleTreeMouseListener());

        rulesTree.setRootVisible(false);
        rulesTree.setShowsRootHandles(true);
        rulesTree.setCellRenderer(new CustomTableRenderer());
        rulesTree.setRowHeight(17);
        rulesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        parser.addObserver(this);
    }

    public void setDelegate(RulesDelegate delegate) {
        this.delegate = delegate;
    }

    public void setActions(TActions actions) {
        this.actions = actions;
    }

    public void setKeyBindings(KeyBindings keyBindings) {
        keyBindings.addKeyBinding("RULE_MOVE_UP", KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.CTRL_MASK),
                new RuleMoveUpAction());
        keyBindings.addKeyBinding("RULE_MOVE_DOWN", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.CTRL_MASK),
                new RuleMoveDownAction());
    }

    public int getNumberOfRulesWithErrors() {
        int count = 0;
        if(parser.getRules() != null) {
            for (Iterator iterator = parser.getRules().iterator(); iterator.hasNext();) {
                Parser.Rule rule = (Parser.Rule) iterator.next();
                if(rule.hasErrors())
                    count++;
            }
        }
        return count;
    }

    public Cursor xjTreeDragSourceDefaultCursor(XJTree tree) {
        return DragSource.DefaultMoveDrop;
    }

    public int xjTreeDragAndDropConstants(XJTree tree) {
        return DnDConstants.ACTION_MOVE;
    }

    public boolean xjTreeDrop(XJTree tree, Object sourceObject, Object targetObject, int dropLocation) {
        Statistics.shared().recordEvent(Statistics.EVENT_DROP_RULE);

        Parser.Rule sourceRule = ((Rules.RuleTreeUserObject) sourceObject).rule;
        Parser.Rule targetRule = ((Rules.RuleTreeUserObject) targetObject).rule;

        return moveRule(sourceRule, targetRule, dropLocation == XJTree.DROP_ABOVE);
    }

    public class RuleMoveUpAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            Parser.Rule sourceRule = getEnclosingRuleAtPosition(textPane.getCaretPosition());
            int previousRuleIndex = parser.getRules().indexOf(sourceRule)-1;
            if(previousRuleIndex>=0) {
                Parser.Rule targetRule = parser.getRuleAtIndex(previousRuleIndex);
                moveRule(sourceRule, targetRule, true);
            }
        }
    }

    public class RuleMoveDownAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            Parser.Rule targetRule = getEnclosingRuleAtPosition(textPane.getCaretPosition());
            int nextRuleIndex = parser.getRules().indexOf(targetRule)+1;
            Parser.Rule sourceRule = parser.getRuleAtIndex(nextRuleIndex);
            if(sourceRule != null) {
                moveRule(sourceRule, targetRule, true);
                selectNextRule = true;
            }
        }
    }

    public void setSkipParseRules(boolean flag) {
        this.skipParseRules = flag;
    }

    public void parseRules() {
        if(skipParseRules)
            return;
        parser.parse();
    }

    public void refreshRules() {
        saveExpandedNodes();
        rulesTreeModel.reload();
        restoreExpandedNodes();

        // Select again the row (otherwise, the selection is lost)
        selectRuleAtPosition(textPane.getCaretPosition());
    }

    public Parser.Group getSelectedGroup() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)rulesTree.getSelectionPath().getLastPathComponent();
        RuleTreeUserObject n = (RuleTreeUserObject)node.getUserObject();
        if(n.group != null)
            return n.group;
       else
            return null;
    }

    public Parser.Group findOpenGroupClosestToLocation(int location) {
        // Look backward into the list of groups
        List groups = parser.getGroups();
        if(groups == null || groups.isEmpty())
            return null;

        Parser.Group previous = null;
        for(int index = 0; index < groups.size(); index++) {
            Parser.Group group = (Parser.Group)groups.get(index);
            if(!group.openGroup)
                continue;

            Token t = group.token;
            if(t.getStart() > location)
                break;

            previous = group;
        }
        return previous;
    }

    public Parser.Group findClosingGroupForGroup(Parser.Group group) {
        List groups = parser.getGroups();
        if(groups == null || groups.isEmpty())
            return null;

        int index = groups.indexOf(group)+1;
        if(index == -1)
            return null;

        int open = 0;
        while(index < groups.size()) {
            Parser.Group g = (Parser.Group) groups.get(index);
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

    public Parser.Rule getLastParserRule() {
        List rules = parser.getRules();
        for(int index = rules.size()-1; index>0; index--) {
            Parser.Rule rule = (Parser.Rule)rules.get(index);
            if(!rule.isLexerRule())
                return rule;
        }
        return null;
    }

    public Parser.Rule getLastLexerRule() {
        List rules = parser.getRules();
        for(int index = rules.size()-1; index>0; index--) {
            Parser.Rule rule = (Parser.Rule)rules.get(index);
            if(rule.isLexerRule())
                return rule;
        }
        return null;
    }

    public List getRulesStartingWith(String match) {
        List matches = new ArrayList();
        if(parser.getRules() == null)
            return matches;

        for(Iterator iterator = parser.getRules().iterator(); iterator.hasNext(); ) {
            Parser.Rule r = (Parser.Rule)iterator.next();
            if(r.name.startsWith(match))
                matches.add(r.name);
        }
        return matches;
    }

    public Parser.Rule getEnclosingRuleAtPosition(int pos) {
        if(parser.getRules() == null)
            return null;

        Iterator iterator = parser.getRules().iterator();
        while(iterator.hasNext()) {
            Parser.Rule r = (Parser.Rule)iterator.next();
            if(pos>= r.start.getStart() && pos<=r.end.getEnd())
                return r;
        }
        return null;
    }


    public Parser.Rule selectRuleAtPosition(int pos) {
        if(selectingRule || parser.getRules() == null)
            return null;

        selectingRule = true;
        Parser.Rule rule = getEnclosingRuleAtPosition(pos);
        selectRule(rule);
        selectingRule = false;
        return rule;
    }

    public Parser.Rule selectRuleName(String name) {
        if(selectingRule || parser.getRules() == null)
            return null;

        Parser.Rule rule = null;
        selectingRule = true;
        Iterator iterator = parser.getRules().iterator();
        while(iterator.hasNext()) {
            Parser.Rule r = (Parser.Rule)iterator.next();
            if(r.name.equals(name)) {
                selectRule(r);
                rule = r;
                break;
            }
        }
        selectingRule = false;
        return rule;
    }

    public boolean isRuleName(String name) {
        Iterator iterator = parser.getRules().iterator();
        while(iterator.hasNext()) {
            Parser.Rule r = (Parser.Rule)iterator.next();
            if(r.name.equals(name))
                return true;
        }
        return false;
    }

    public Parser.Rule getRuleAtIndex(int index) {
        if(parser.getRules() == null)
            return null;

        Iterator iterator = parser.getRules().iterator();
        while(iterator.hasNext()) {
            Parser.Rule r = (Parser.Rule)iterator.next();
            if(index >= r.getStartIndex() && index <= r.getEndIndex())
                return r;
        }
        return null;
    }

    public Parser.Rule getRuleStartingWithToken(Token startToken) {
        if(parser.getRules() == null)
            return null;

        Iterator iterator = parser.getRules().iterator();
        while(iterator.hasNext()) {
            Parser.Rule r = (Parser.Rule)iterator.next();
            if(r.start == startToken)
                return r;
        }
        return null;
    }

    public boolean isRuleAtIndex(int index) {
        return getRuleAtIndex(index) != null;
    }

    public boolean isDuplicateRule(String rule) {
        return duplicateRules.contains(rule);
    }

    public boolean isUndefinedRule(String rule) {
        return undefinedRules.contains(rule);
    }

    public List getUndefinedRules() {
        return undefinedRules;
    }

    public void selectFirstRule() {
        if(parser.getRules().size() == 0)
            return;

        selectRule((Parser.Rule)parser.getRules().get(0));
    }

    public void selectNextRule() {
        Parser.Rule rule = getEnclosingRuleAtPosition(textPane.getCaretPosition());
        int index = parser.getRules().indexOf(rule)+1;
        rule = parser.getRuleAtIndex(index);
        if(rule != null) {
            textPane.setCaretPosition(rule.getStartIndex());
            delegate.rulesCaretPositionDidChange();
        }
    }

    public void selectRule(Parser.Rule rule) {
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

    public void selectTextRules(List rules) {
        if(rules == null || rules.isEmpty())
            return;

        selectingRule = true;

        Parser.Rule startRule = (Parser.Rule) rules.get(0);
        Parser.Rule endRule = (Parser.Rule) rules.get(rules.size()-1);

        textPane.requestFocus(true);
        textPane.setCaretPosition(startRule.start.getStart());
        textPane.moveCaretPosition(endRule.end.getEnd());
        textPane.getCaret().setSelectionVisible(true);

        Rectangle r;
        try {
            r = textPane.modelToView(startRule.start.getStart());
            textPane.scrollRectToVisible(r);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
        selectingRule = false;

        delegate.rulesDidSelectRule();
    }

    public void selectTextRule(Parser.Rule rule) {
        if(rule == null)
            return;

        selectingRule = true;

        textPane.setCaretPosition(rule.start.getStart());
        textPane.moveCaretPosition(rule.end.getEnd());
        textPane.getCaret().setSelectionVisible(true);

        Rectangle r;
        try {
            r = textPane.modelToView(rule.start.getStart());
            textPane.scrollRectToVisible(r);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
        selectingRule = false;

        delegate.rulesDidSelectRule();
    }

    public void rebuildDuplicateRulesList() {
        List rules = parser.getRules();
        List sortedRules = Collections.list(Collections.enumeration(rules));
        Collections.sort(sortedRules);
        Iterator iter = sortedRules.iterator();
        Parser.Rule currentRule = null;
        duplicateRules.clear();
        while(iter.hasNext()) {
            Parser.Rule nextRule = (Parser.Rule) iter.next();
            if(currentRule != null && currentRule.name.equals(nextRule.name) && !duplicateRules.contains(currentRule)) {
                duplicateRules.add(currentRule.name);
            }
            currentRule = nextRule;
        }
    }

    public boolean isNameToken(Token t) {
        Parser.Name name = parser.getName();
        return t.getStart() >= name.start.getStart() && t.getEnd() <= name.end.getEnd();
    }

    public boolean isBlockToken(Token t) {
        for(Iterator iter = parser.getBlocks().iterator(); iter.hasNext(); ) {
            Parser.Block b = (Parser.Block)iter.next();
            if(t.getStart() >= b.start.getStart() && t.getEnd() <= b.end.getEnd())
                return true;
        }
        return false;
    }

    public void rebuildUndefinedRulesList() {
        List ruleNames = parser.getRuleNames();
        undefinedRules.clear();
        for(Iterator iter = parser.getTokens().iterator(); iter.hasNext(); ) {
            Token t = (Token)iter.next();
            if(t.isAllUpperCase())
                continue;

            if(t.type != Lexer.TOKEN_ID)
                continue;
            
            if(isNameToken(t))
                continue;

            if(isBlockToken(t))
                continue;

            if(Parser.keywords.contains(t.getAttribute()))
                continue;

            if(!ruleNames.contains(t.getAttribute()))
                undefinedRules.add(t.getAttribute());
        }
    }

    public void rebuildTree() {
        saveExpandedNodes();

        rulesTreeRootNode.removeAllChildren();

        List rules = parser.getRules();
        List groups = parser.getGroups();
        if(groups.isEmpty()) {
            buildTree(rulesTreeRootNode, rules, 0, rules.size()-1);
        } else {
            Stack parentStack = new Stack();
            parentStack.add(rulesTreeRootNode);

            int ruleIndex = 0;
            for(int index=0; index<groups.size(); index++) {
                Parser.Group group = (Parser.Group)groups.get(index);

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
        restoreExpandedNodes();
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

    protected void buildTree(DefaultMutableTreeNode parentNode, List rules, int from, int to) {
        for(int index=from; index<=to; index++) {
            parentNode.add(new DefaultMutableTreeNode(new RuleTreeUserObject(index)));
        }
    }

    public void parserDidComplete() {
        //long t = System.currentTimeMillis();
        rebuildDuplicateRulesList();
        rebuildUndefinedRulesList();
        //long delta = System.currentTimeMillis()-t;
        //System.out.println("Rebuild in "+delta);

        rebuildTree();
        if(selectNextRule) {
            // Can be set by RuleMoveDown() class when a rule is moved down. Selection has to occurs here
            // after rules have been parsed. We use this flag to select the next rule instead of the current one.
            selectNextRule = false;
            selectNextRule();
        } else
            selectRuleAtPosition(textPane.getCaret().getDot());
    }

    public boolean moveRule(Parser.Rule sourceRule, Parser.Rule targetRule, boolean dropAbove) {
        if(sourceRule == null || targetRule == null)
            return false;
        
        String sourceRuleText = actions.getPlainText(sourceRule.getStartIndex(), sourceRule.getEndIndex())+"\n";

        try {
            Document doc = textPane.getDocument();

            int removeStartIndex = sourceRule.getStartIndex();
            int targetInsertionIndex = dropAbove ? targetRule.getStartIndex() : targetRule.getEndIndex()+2;

            // Remove one more character to remove the end of line of the rule
            int removeLength = sourceRule.getLength()+1;
            if(removeStartIndex+removeLength > doc.getLength())
                removeLength--;

            if(sourceRule.getStartIndex()>targetRule.getStartIndex()) {
                doc.remove(removeStartIndex, removeLength);
                doc.insertString(targetInsertionIndex, sourceRuleText, null);
                textPane.setCaretPosition(targetInsertionIndex);
            } else {
                doc.insertString(targetInsertionIndex, sourceRuleText, null);
                doc.remove(removeStartIndex, removeLength);
                textPane.setCaretPosition(targetInsertionIndex-removeLength);
            }
            return true;
        } catch (BadLocationException e) {
            e.printStackTrace();
            return false;
        }
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
                if(n.rule.isLexerRule())
                    setIcon(IconManager.shared().getIconLexer());
                else
                    setIcon(IconManager.shared().getIconParser());                
            }

            if(n.rule != null && n.rule.hasErrors()) {
                setForeground(Color.red);
                setFont(getFont().deriveFont(Font.BOLD));
                setToolTipText(n.rule.getErrorMessageHTML());
            } else {
                setForeground(Color.black);
                setFont(getFont().deriveFont(Font.PLAIN));
            }

            return r;
        }
    }

    public class RuleTreeMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
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
            selectTextRules(selRules);
        }
    }

    public class RuleTreeUserObject implements Transferable {

        public int ruleIndex;
        public Parser.Rule rule;
        public Parser.Group group;

        public RuleTreeUserObject(int index) {
            this.ruleIndex = index;
            this.rule = parser.getRuleAtIndex(ruleIndex);
        }

        public RuleTreeUserObject(Parser.Group group) {
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
