package org.antlr.works.editor.tool;

import org.antlr.works.editor.EditorWindow;
import org.antlr.works.parser.Parser;
import org.antlr.works.parser.Token;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/*

[The "BSD licence"]
Copyright (c) 2004-05 Jean Bovet
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

public class TUsage {

    protected JPanel panel;
    protected JScrollPane treeScrollPane;
    protected JTree tree;

    protected DefaultTreeModel model;
    protected DefaultMutableTreeNode root;
    protected DefaultMutableTreeNode node;
    protected String lastRule;

    protected EditorWindow editor;

    public TUsage(EditorWindow editor) {
        this.editor = editor;

        panel = new JPanel(new BorderLayout());

        tree = new JTree();
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 1) {
                    }
                    else if(e.getClickCount() == 2) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode)selPath.getLastPathComponent();
                        if(node.getUserObject() instanceof UsageMatch)
                            selectMatch((UsageMatch)node.getUserObject());
                    }
                }
            }
        });

        treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setWheelScrollingEnabled(true);

        panel.add(treeScrollPane, BorderLayout.CENTER);

        root = new DefaultMutableTreeNode();
        model = new DefaultTreeModel(root);
        lastRule = null;

        tree.setModel(model);
    }

    public Container getContainer() {
        return panel;
    }

    public void addMatch(Parser.Rule rule, Token token) {
        if(lastRule == null || lastRule != null && !lastRule.equals(rule.name)) {
            node = new DefaultMutableTreeNode();
            node.setUserObject(rule.name);
            root.add(node);

            lastRule = rule.name;
        }

        DefaultMutableTreeNode matchNode = new DefaultMutableTreeNode();
        matchNode.setUserObject(new UsageMatch(rule, token));
        node.add(matchNode);

        model.reload();
    }

    public void selectMatch(UsageMatch match) {
        editor.selectTextRange(match.token.start, match.token.end);
    }

    public class UsageMatch {
        public Parser.Rule rule;
        public Token token;
        public String contextualText;

        public UsageMatch(Parser.Rule rule, Token token) {
            this.rule = rule;
            this.token = token;
            createContextString();
        }

        public void createContextString() {
            int s = token.start;
            while(s > 0 && token.text.charAt(s) != '\n') {
                s--;
            }

            int e = token.end;
            while(s < token.text.length() && token.text.charAt(e) != '\n') {
                e++;
            }
            contextualText = token.text.substring(s, e);
        }

        public String toString() {
            return token.getAttribute()+" @ ("+token.line+", "+(token.start-token.linePos)+") "+contextualText;
        }
    }
}
