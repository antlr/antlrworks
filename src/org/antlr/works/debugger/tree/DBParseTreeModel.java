package org.antlr.works.debugger.tree;

import org.antlr.runtime.Token;
import org.antlr.tool.Grammar;
import org.antlr.works.awtree.AWTreeModel;
import org.antlr.works.awtree.AWTreeNode;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.prefs.AWPrefsDialog;
import org.antlr.xjlib.foundation.notification.XJNotificationCenter;
import org.antlr.xjlib.foundation.notification.XJNotificationObserver;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
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

public class DBParseTreeModel extends AWTreeModel implements XJNotificationObserver {

    public Stack<ParseTreeNode> rules = new Stack<ParseTreeNode>();
    public Stack<Backtrack> backtrackStack = new Stack<Backtrack>();

    public Color lookaheadTokenColor;
    public TreeNode lastNode;
    public int line, pos;

    public Debugger debugger;

    public List<DBParseTreeModelListener> listeners = new ArrayList<DBParseTreeModelListener>();

    public DBParseTreeModel(Debugger debugger) {
        this.debugger = debugger;
        initRules();
        initColors();
        XJNotificationCenter.defaultCenter().addObserver(this, AWPrefsDialog.NOTIF_PREFS_APPLIED);
    }

    public void close() {
        debugger = null;
        listeners.clear();
        XJNotificationCenter.defaultCenter().removeObserver(this);
    }

    public void addListener(DBParseTreeModelListener listener) {
        listeners.add(listener);
    }

    public void fireDataChanged() {
        for (DBParseTreeModelListener listener : listeners) {
            listener.modelChanged(this);
        }
    }

    public void initRules() {
        rules.clear();
        rules.push(new ParseTreeNode("root"));
    }

    public void initColors() {
        lookaheadTokenColor = AWPrefs.getLookaheadTokenColor();
    }

    public void clear() {
        super.clear();

        initRules();
        backtrackStack.clear();

        setLastNode(null);
        fireDataChanged();

        line = pos = 0;
    }

    public void setLastNode(TreeNode node) {
        this.lastNode = node;
    }

    public TreeNode getLastNode() {
        return lastNode;
    }

    public void setLocation(int line, int pos) {
        this.line = line;
        this.pos = pos;
    }

    public void pushRule(String name) {
        ParseTreeNode parentRuleNode = rules.peek();

        ParseTreeNode ruleNode = new ParseTreeNode(name);
        ruleNode.setPosition(line, pos);
        rules.push(ruleNode);

        addNode(parentRuleNode, ruleNode);
        addNodeToCurrentBacktrack(ruleNode);

        setLastNode(ruleNode);
    }

    public void popRule() {
        rules.pop();
    }

    public TreeNode getRootRule() {
        return rules.firstElement();
    }

    public TreeNode peekRule() {
        if(rules.isEmpty())
            return null;
        else
            return rules.peek();
    }

    public void addToken(Token token) {
        ParseTreeNode ruleNode = rules.peek();
        // todo this is way to long to access the needed grammar
        ParseTreeNode elementNode = new ParseTreeNode(token, debugger.getSyntaxEngine().getAntlrGrammar().getANTLRGrammar());
        elementNode.setPosition(line, pos);
        addNode(ruleNode, elementNode);
        addNodeToCurrentBacktrack(elementNode);
        setLastNode(elementNode);
    }

    public void addException(Exception e) {
        ParseTreeNode ruleNode = rules.peek();
        ParseTreeNode errorNode = new ParseTreeNode(e);
        errorNode.setPosition(line, pos);
        addNode(ruleNode, errorNode);
        addNodeToCurrentBacktrack(errorNode);
        setLastNode(errorNode);
    }

    public void addNodeToCurrentBacktrack(ParseTreeNode node) {
        if(backtrackStack.isEmpty())
            return;

        Backtrack b = backtrackStack.peek();
        b.addNode(node);
    }

    public void beginBacktrack(int level) {
        backtrackStack.push(new Backtrack(level, lookaheadTokenColor));
    }

    public void endBacktrack(int level, boolean success) {
        Backtrack b = backtrackStack.pop();
        b.end(success);
        setLastNode(b.getLastNode());
    }

    public void notificationFire(Object source, String name) {
        if(name.equals(AWPrefsDialog.NOTIF_PREFS_APPLIED)) {
            initColors();
        }
    }

    public static class ParseTreeNode extends DBTreeNode {

        protected String s;
        protected Exception e;

        public ParseTreeNode(String s) {
            this.s = s;
        }

        public ParseTreeNode(Exception e) {
            this.e = e;
        }

        public ParseTreeNode(Token token, Grammar grammar) {
            super(token, grammar);
        }

        public String toString() {
            if(s != null)
                return s;
            else if(e != null)
                return e.toString();
            else
                return super.toString();
        }

    }

    public static class Backtrack {

        public int level;
        public Color lookaheadTokenColor;
        public LinkedList<DBTreeNode> nodes = new LinkedList<DBTreeNode>();

        public Backtrack(int level, Color lookaheadTokenColor) {
            this.level = level;
            this.lookaheadTokenColor = lookaheadTokenColor;
        }

        /** Node added to the backtrack object are displayed in blue
         * by default. The definitive color will be applied by the end()
         * method.
         */

        public void addNode(DBTreeNode node) {
            node.setColor(lookaheadTokenColor);
            nodes.add(node);
        }

        public void end(boolean success) {
            Color color = getColor(success);
            for (DBTreeNode node : nodes) {
                node.setColor(color);
            }
        }

        public AWTreeNode getLastNode() {
            if(nodes.isEmpty())
                return null;
            else
                return nodes.getLast();
        }

        protected Color getColor(boolean success) {
            Color c = success?Color.green.darker():Color.red;
            for(int i=1; i<level; i++) {
                c = c.darker().darker();
            }
            return c;
        }

    }

}
