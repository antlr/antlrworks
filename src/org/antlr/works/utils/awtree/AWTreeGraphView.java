package org.antlr.works.utils.awtree;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.ParseTree;
import org.antlr.works.ate.ATEUtilities;
import org.antlr.xjlib.appkit.gview.GView;
import org.antlr.xjlib.appkit.gview.base.Rect;
import org.antlr.xjlib.appkit.gview.object.GElement;
import org.antlr.xjlib.appkit.gview.object.GElementRect;
import org.antlr.xjlib.appkit.gview.object.GLink;
import org.antlr.xjlib.appkit.gview.shape.SLinkElbow;

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

public class AWTreeGraphView extends GView {

    public static final boolean DRAGGABLE = false;

    public static final int HORIZONTAL_GAP = 20;
    public static final int VERTICAL_GAP = 20;

    public static final int MARGIN = 10;

    public static final Color HIGHLIGHTED_COLOR = new Color(0, 0.5f, 1, 0.4f);
    public static final Font DEFAULT_FONT = new Font("Monospaced", Font.PLAIN, 11);

    protected TreeNode root;
    protected GElementNode highlightedNode;

    protected Map<TreeNode,GElement> treeNodeToGElementMap = new HashMap<TreeNode, GElement>();
    protected Map<GElement,TreeNode> gelementToTreeNodeMap = new HashMap<GElement, TreeNode>();

    protected AWTreePanel panel;
    protected AWTreeModel model;

    protected Graphics2D g2d;
    protected FontMetrics fontMetrics;

    protected boolean dirty = true;

    public AWTreeGraphView(AWTreePanel panel) {
        this.panel = panel;
        setPreferredSize(new Dimension(0, 0));
        setFocusable(true);
    }

    public void addDefaultEventManager() {
        // No event manager by default for tree view
    }

    public void setModel(AWTreeModel model) {
        this.model = model;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public void clear() {
        if(model != null)
            model.clear();
        clearMaps();
    }

    public void refresh() {
        /** Mark the tree as dirty so it gets rebuild */
        dirty = true;

        /** Need to rebuild now because the tree nodes need to be
         * available for other operation (like scrollNodeToVisible())
         */
        rebuild();

        /** Repaint the tree */
        repaint();
    }

    public void rebuild() {
        if(g2d == null || root == null)
            return;

        if(dirty) {
            dirty = false;
            if(model == null)
                rebuildNoModel();
            else
                rebuildWithModel();
        }
    }

    public void clearMaps() {
        treeNodeToGElementMap.clear();
        gelementToTreeNodeMap.clear();
    }

    public void mapNodeAndElement(TreeNode node, GElement element) {
        treeNodeToGElementMap.put(node, element);
        gelementToTreeNodeMap.put(element, node);
    }

    public TreeNode getTreeNodeForElement(GElementNode elem) {
        return gelementToTreeNodeMap.get(elem);
    }

    public GElementNode getGElementForNode(TreeNode node) {
        if(node == null)
            return null;
        else
            return (GElementNode)treeNodeToGElementMap.get(node);
    }

    /** This method rebuild the tree completely. This can be expensive if the tree
     * contains many node. Use a AWTreeModel instead (see rebuildWithModel)
     */

    public void rebuildNoModel() {
        clearMaps();

        GElement element = buildGraph(null);
        element.move(MARGIN, MARGIN);

        setSizeMargin(MARGIN);
        setRootElement(element);
    }

    public GElementNode buildGraph(TreeNode node) {
        if(node == null)
            node = root;

        GElementNode nodeElement = getGElementForNode(node);
        if(nodeElement == null)
            nodeElement = createGElement(node);

        /** Add all the children of the node */
        for(int index=0; index<node.getChildCount(); index++) {
            TreeNode child = node.getChildAt(index);

            /** Then add it to the parent node */
            addChildElement(nodeElement, createGElement(child));

            /** Recursively build the children of the child */
            buildGraph(child);
        }

        /** Adjust the parent element position after adding all its children */
        adjustElementPositionRelativeToItsChildren(node, false);

        return nodeElement;
    }

    /** This method rebuild the tree incrementally using the information provided
     * by the tree model. This method is faster than rebuildNoModel() for large tree.
     *
     * Note: actually it is slower because of the adjustElement...() method which is called too often
     */
    public void rebuildWithModel() {
        for(int n=0; n<model.getNewNodesCount(); n++) {
            TreeNode parent = model.getNewNodeParentAtIndex(n);
            TreeNode child = model.getNewNodeAtIndex(n);

            GElementNode parentElement = getGElementForNode(parent);
            if(parentElement == null) {
                parentElement = createGElement(root);
                parentElement.move(MARGIN, MARGIN);
                setSizeMargin(MARGIN);
                setRootElement(parentElement);
            }

            GElementNode childElement = createGElement(child);
            addChildElement(parentElement, childElement);
            adjustElementPositionRelativeToItsChildren(parent, true);
        }

        autoAdjustSize();

        model.clearNewNodes();
    }

    public void paintComponent(Graphics g) {
        if(g2d != g) {
            g2d = (Graphics2D)g;
            ATEUtilities.prepareForText(g2d);
            g2d.setFont(DEFAULT_FONT);
            fontMetrics = g2d.getFontMetrics();
        }
        rebuild();
        super.paintComponent(g);
    }

    public void addChildElement(GElementNode parent, GElementNode child) {
        /** Get the far right position of the last child */
        double x = parent.getLastChildRightSpan();

        /** Add a gap if there is already a child otherwise use the parent left coordinate */
        if(x > 0)
            x += HORIZONTAL_GAP;
        else
            x = parent.getLeft();

        /** Set the child position */
        child.setPositionOfUpperLeftCorner(x, parent.getBottom()+VERTICAL_GAP);

        /** Create the link from the parent to this child */
        GLink link = new GLink(parent, GLink.ANCHOR_BOTTOM,
                child, GLink.ANCHOR_TOP,
                GLink.SHAPE_ELBOW, "", 0);
        link.setDraggable(DRAGGABLE);

        /** Configure the link geometry */
        SLinkElbow l = (SLinkElbow)link.getLink();
        l.setOutOffsetLength(10);
        l.getArrow().setLength(6);

        /** Add the link and the child */
        parent.addElement(link);
        parent.addElement(child);
    }

    /** This method recursively adjusts a node position relative to its children */

    public void adjustElementPositionRelativeToItsChildren(TreeNode node, boolean recursive) {
        GElementNode element = getGElementForNode(node);
        if(element == null)
            return;

        double elementWidth = element.getWidth();
        double childrenWidth = element.getLastChildRightSpan()-element.getFirstChildLeftSpan();

        /** Nothing to adjust if there is no children */
        if(childrenWidth == 0)
            return;

        /** x and y are the coordinates of the upper-left corner of the frame enclosing
         * the element and all its children.
         */
        double x = Math.min(element.getLeft(), element.getFirstChildLeftSpan());
        double y = element.getTop();
        double spanWidth = Math.max(elementWidth, childrenWidth);

        /** First move the children if needed. To move all the children at once, we move
         * the element itself (which will cause all the children to be moved recursively).
         * The element position is set after this operation.
         */
        double childrenOffset = spanWidth*0.5-childrenWidth*0.5;

        /** Compute the offset to move the children given their current position */
        double offset = x+childrenOffset-element.getFirstChildLeftSpan();
        element.move(offset, 0);

        /** Set the element position */
        element.setPositionOfUpperLeftCorner(x+spanWidth*0.5-elementWidth*0.5, y);

        /** Set the span (children width) of the element */
        element.setSpanWidth(spanWidth);

        /** Recursively adjust the parent node */
        if(recursive)
            adjustElementPositionRelativeToItsChildren(node.getParent(), recursive);
    }

    public String getNodeLabel(TreeNode node) {
        if(node instanceof ParseTree) {
            Object payload = ((ParseTree)node).payload;
            if(payload instanceof CommonToken) {
                CommonToken t = (CommonToken)payload;
                return t.getText();
            } else {
                return payload.toString();
            }
        }
        return node.toString();
    }

    public Color getNodeColor(TreeNode node) {
        if(node instanceof AWTreeNode)
            return ((AWTreeNode)node).getColor();
        else
            return Color.black;
    }

    public GElementNode createGElement(TreeNode node) {
        Color nodeColor = getNodeColor(node);
        String nodeLabel = getNodeLabel(node);

        double width = (nodeLabel==null?0:fontMetrics.stringWidth(nodeLabel))+16;
        double height = fontMetrics.getHeight()+8;

        GElementNode element = new GElementNode();
        element.setDraggable(DRAGGABLE);

        /** Must call setPositionOfUpperLeftCorner after
         * setting the size of the element.
         */
        element.setSize(width, height);
        element.setPositionOfUpperLeftCorner(0, 0);

        element.setLabel(nodeLabel);
        element.setColor(nodeColor);
        element.setLabelColor(nodeColor);

        /** Map the node to the element for quick access */
        mapNodeAndElement(node, element);

        return element;
    }

    public void highlightNode(TreeNode node) {
        if(highlightedNode != null) {
            highlightedNode.setHighlighted(false);
            highlightedNode = null;
        }

        GElementNode element = getGElementForNode(node);
        if(element == null)
            return;

        element.setHighlighted(true);
        highlightedNode = element;

        scrollNodeToVisible(node);

        repaint();
    }

    /** This method is used to repaint a node by applying its color to
     * its corresponding GElement.
     */
    public void repaintNode(TreeNode node) {
        GElementNode element = getGElementForNode(node);
        if(element == null)
            return;

        if(node instanceof AWTreeNode) {
            Color nodeColor = ((AWTreeNode)node).getColor();
            element.setColor(nodeColor);
            element.setLabelColor(nodeColor);
        }
    }

    public void scrollNodeToVisible(TreeNode node) {
        GElementNode element = getGElementForNode(node);
        if(element == null)
            return;

        scrollElementToVisible(element);
    }

    @Override
    public JPopupMenu getContextualMenu(GElement element) {
        return panel.getContextualMenu();
    }

    public static class GElementNode extends GElementRect {

        public boolean highlighted = false;
        public double spanWidth = 0;

        public void setHighlighted(boolean flag) {
            this.highlighted = flag;
        }

        @Override
        public void draw(Graphics2D g) {
            if(highlighted && isVisibleInClip(g)) {
                Rectangle r = getFrame().rectangle();
                g.setColor(HIGHLIGHTED_COLOR);
                g.fillRect(r.x, r.y, r.width, r.height);
            }

            super.draw(g);
        }

        /** Methods to retrieve the span width of the node. The span width
         * is the maximum width of the node and its children.
         */
        public void setSpanWidth(double width) {
            spanWidth = width;
        }

        public double getLeftSpan() {
            if(spanWidth <= getWidth())
                return getLeft();
            else
                return getLeft()-(spanWidth-getWidth())*0.5;
        }

        public double getRightSpan() {
            if(spanWidth <= getWidth())
                return getRight();
            else
                return getRight()+(spanWidth-getWidth())*0.5;
        }

        /** Return the last children right coordinate of this node. Note that links are
         * also in the list of children so we must skip them.
         */
        public double getLastChildRightSpan() {
            if(elements == null)
                return 0;

            for (int i = elements.size()-1; i >= 0; i--) {
                GElement element = (GElement) elements.get(i);
                if(element instanceof GElementNode) {
                    GElementNode n = (GElementNode)element;
                    return n.getRightSpan();
                }
            }

            return 0;
        }

        /** Return the first children left coordinate of this node. Note that links are
         * also in the list of children so we must skip them.
         */
        public double getFirstChildLeftSpan() {
            if(elements == null)
                return 0;

            for (GElement element : elements) {
                if (element instanceof GElementNode) {
                    GElementNode n = (GElementNode) element;
                    return n.getLeftSpan();
                }
            }

            return 0;
        }

        /** Methods used to retrieve the coordinate of the frame. Note that
         * a GElementRect position is always centered so we need to use getFrame()
         * to get the frame rectangle from which we can get the coordinate we want. */

        public double getLeft() {
            return getFrame().r.x;
        }

        public double getTop() {
            return getFrame().r.y;
        }

        public double getRight() {
            Rect r = getFrame();
            return r.r.x+r.r.width;
        }

        public double getBottom() {
            Rect r = getFrame();
            return r.r.y+r.r.height;
        }

        public void setTop(double top) {
            position.y = top+getHeight()*0.5;
        }
    }
}
