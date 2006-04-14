package org.antlr.works.parsetree;

import edu.usfca.xj.appkit.gview.GView;
import edu.usfca.xj.appkit.gview.base.Rect;
import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GElementRect;
import edu.usfca.xj.appkit.gview.object.GLink;
import edu.usfca.xj.appkit.gview.shape.SLinkElbow;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.ParseTree;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
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

public class ParseTreeGraphView extends GView {

    public static final boolean DRAGGABLE = false;
    
    public static final int HORIZONTAL_GAP = 20;
    public static final int VERTICAL_GAP = 20;

    public static final int MARGIN = 10;

    public static final Color HIGHLIGHTED_COLOR = new Color(0, 0.5f, 1, 0.4f);
    public static final Font DEFAULT_FONT = new Font("Monospaced", Font.PLAIN, 11);

    protected TreeNode root;
    protected GElementNode highlightedNode;
    protected Map treeNodeToGElementMap = new HashMap();
    protected Map gelementToTreeNodeMap = new HashMap();
    protected Graphics2D g2d;
    protected ParseTreePanel panel;

    public ParseTreeGraphView(ParseTreePanel panel) {
        this.panel = panel;
        setPreferredSize(new Dimension(0, 0));
    }

    public void addDefaultEventManager() {
        // No event manager by default for parse tree view
    }

    public void setRoot(TreeNode root) {
        this.root = root;
        setRootElement(null);
    }

    public void refresh() {
        setRootElement(null);
        rebuild();
        repaint();
    }

    public void rebuild() {
        if(getRootElement() == null && root != null && g2d != null) {
            treeNodeToGElementMap.clear();
            gelementToTreeNodeMap.clear();

            GElement element = buildGraph(root, g2d);

            treeNodeToGElementMap.put(root, element);
            gelementToTreeNodeMap.put(element, root);

            element.move(MARGIN, MARGIN);
            setSizeMargin(MARGIN);
            setRootElement(element);
        }
    }

    public void paintComponent(Graphics g) {
        g2d = (Graphics2D)g;
        g2d.setFont(DEFAULT_FONT);
        rebuild();
        super.paintComponent(g);
    }

    public GElement buildGraph(TreeNode node, Graphics2D g) {
        Color nodeColor = Color.black;
        String nodeLabel = node.toString();

        if(node instanceof ParseTree) {
            Object payload = ((ParseTree)node).payload;
            if(payload instanceof CommonToken) {
                CommonToken t = (CommonToken)payload;
                nodeLabel = t.getText();
            } else {
                nodeLabel = payload.toString();
            }
        } else if(node instanceof ParseTreeNode) {
            if(!((ParseTreeNode)node).isEnabled())
                nodeColor = Color.gray;
        }

        FontMetrics fm = g.getFontMetrics();
        double width = (nodeLabel==null?0:fm.stringWidth(nodeLabel))+16;
        double height = fm.getHeight()+8;

        GElementNode nodeElement = new GElementNode();
        nodeElement.setDraggable(DRAGGABLE);
        nodeElement.setSize(width, height);
        
        // Must call setPositionOfUpperLeftCorner after
        // setting the size!!!!
        nodeElement.setPositionOfUpperLeftCorner(0, 0);
        nodeElement.setLabel(nodeLabel);

        nodeElement.setColor(nodeColor);
        nodeElement.setLabelColor(nodeColor);

        treeNodeToGElementMap.put(node, nodeElement);
        gelementToTreeNodeMap.put(nodeElement, node);

        double x = 0;
        for(int index=0; index<node.getChildCount(); index++) {
            TreeNode child = node.getChildAt(index);
            GElement childElement = buildGraph(child, g);
            Rect r = childElement.bounds();
            childElement.move(x, height+VERTICAL_GAP);
            x += r.r.width;
            if(index < node.getChildCount()-1)
                x += HORIZONTAL_GAP;

            GLink link = new GLink(nodeElement, GLink.ANCHOR_BOTTOM,
                                    childElement, GLink.ANCHOR_TOP,
                                    GLink.SHAPE_ELBOW, "", 0);
            link.setDraggable(DRAGGABLE);

            SLinkElbow l = (SLinkElbow)link.getLink();
            l.setOutOffsetLength(10);
            l.getArrow().setLength(6);

            nodeElement.addElement(link);
            nodeElement.addElement(childElement);
        }

        if(x > 0) {
            if(x >= width)
                nodeElement.setPositionOfUpperLeftCorner(x*0.5-width*0.5, 0);
            else {
                nodeElement.move((width-x)*0.5, 0);
                nodeElement.setPositionOfUpperLeftCorner(0, 0);
            }
        }

        return nodeElement;
    }

    public TreeNode getTreeNode(GElementNode elem) {
        return (TreeNode) gelementToTreeNodeMap.get(elem);
    }

    public void highlightNode(TreeNode node) {
        if(highlightedNode != null) {
            highlightedNode.setHighlighted(false);
            highlightedNode = null;
        }

        GElementNode element = (GElementNode)treeNodeToGElementMap.get(node);
        if(element == null)
            return;

        element.setHighlighted(true);
        highlightedNode = element;

        scrollNodeToVisible(node);

        repaint();
    }

    public void scrollNodeToVisible(TreeNode node) {
        GElementNode element = (GElementNode)treeNodeToGElementMap.get(node);
        if(element == null)
            return;

        scrollElementToVisible(element);
    }

    public JPopupMenu getContextualMenu(GElement element) {
        return panel.getContextualMenu();
    }

    public class GElementNode extends GElementRect {

        public boolean highlighted = false;

        public void setHighlighted(boolean flag) {
            this.highlighted = flag;
        }

        public void draw(Graphics2D g) {
            if(highlighted) {
                Rectangle r = getFrame().rectangle();
                g.setColor(HIGHLIGHTED_COLOR);
                g.fillRect(r.x, r.y, r.width, r.height);
            }

            super.draw(g);
        }

    }
}
