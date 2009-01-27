package org.antlr.xjlib.appkit.swing;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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

/* Portion of this code has been inspired by the following Java Tips:

    http://www.javaworld.com/javaworld/javatips/jw-javatip97.html  (Rob Kenworthy)
    http://www.javaworld.com/javaworld/javatips/jw-javatip114.html (Andrew Armstrong)

*/

public class XJTree extends JTree implements DragGestureListener, DropTargetListener, DragSourceListener, Autoscroll {

    protected XJTreeDelegate delegate;
    protected DragSource dragSource;
    protected DropTarget dt;
    protected TreePath oldSelectedPath;

    protected Timer autoExpandTimer;
    protected TreePath lastPath;
    protected Point lastPoint = new Point();
    protected Rectangle cueLine = new Rectangle();

    protected BufferedImage dragImage;
    protected Point dragImageOffset = new Point();

    protected final int AUTOSCROLL_MARGIN = 12;

    protected int dropLocation;
    public static final int DROP_ABOVE = 0;
    public static final int DROP_ONTO = 1;
    public static final int DROP_BELOW = 2;

    public XJTree() {
        autoExpandTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(isRootVisible() && getRowForPath(lastPath) == 0) {
                    // Do not expand the root
                    return;
                }

                if(isExpanded(lastPath))
                    collapsePath(lastPath);
                else
                    expandPath(lastPath);
            }
        });
        autoExpandTimer.setRepeats(false);
    }

    public void close() {
        autoExpandTimer.stop();
        autoExpandTimer = null;
        dragSource = null;
        dt = null;
    }

    public void setDelegate(XJTreeDelegate delegate) {
        this.delegate = delegate;
    }

    public void setEnableDragAndDrop() {
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this, getDelegateConstants(), this);
        dt = new DropTarget(this, this);
    }

    public int getDelegateConstants() {
        return delegate.xjTreeDragAndDropConstants(this);
    }

    public DefaultMutableTreeNode getOldSelectedNode() {
        return (DefaultMutableTreeNode)oldSelectedPath.getLastPathComponent();
    }

    public DefaultMutableTreeNode getSelectedNode() {
        if(getSelectionPath() == null)
            return null;
        else
            return (DefaultMutableTreeNode)getSelectionPath().getLastPathComponent();
    }

    public List<Object> getSelectedNodes() {
        List<Object> nodes = new ArrayList<Object>();
        if(getSelectionPaths() != null) {
            for (int i = 0; i < getSelectionPaths().length; i++) {
                TreePath treePath = getSelectionPaths()[i];
                nodes.add(treePath.getLastPathComponent());
            }
        }
        return nodes;
    }

    /** This method deselects the current selection of the item
     * under the mouse location if it is not part of the current selection
     */

    public void modifySelectionIfNecessary(MouseEvent me) {
        boolean partOfSelection = false;
        int row = getRowForLocation(me.getX(), me.getY());
        if(getSelectionRows() != null) {
            for (int i = 0; i < getSelectionRows().length; i++) {
                int selRow = getSelectionRows()[i];
                if(selRow == row)
                    partOfSelection = true;
            }
        }

        if(!partOfSelection)
            setSelectionRow(row);
    }

    protected void cleanUpAfterDrag() {
        paintImmediately(cueLine);
    }

    // AutoScroll interface

    public Insets getAutoscrollInsets() {
        Rectangle outer = getBounds();
        Rectangle inner = getParent().getBounds();

        return new Insets(inner.y-outer.y+AUTOSCROLL_MARGIN, inner.x-outer.x+AUTOSCROLL_MARGIN,
                outer.height-inner.height+AUTOSCROLL_MARGIN,
                outer.width-inner.width+AUTOSCROLL_MARGIN);
    }

    public void autoscroll(Point point) {
        int row = getRowForLocation(point.x, point.y);
        if(row < 0)
            return;

        Rectangle outer = getBounds();
        if(point.y + outer.y <= AUTOSCROLL_MARGIN)
            row = row <= 0 ? 0 : row - 1;
        else
            row = row < getRowCount() - 1 ? row + 1 : row;

        scrollRowToVisible(row);
    }

    // DragGestureListener interface method

    public void dragGestureRecognized(DragGestureEvent event) {
        try {
            createDragImage(event);
            dragSource.startDrag(event, delegate.xjTreeDragSourceDefaultCursor(this), dragImage, new Point(5,5), (Transferable)getSelectedNode().getUserObject(), this);
        } catch(Exception e) {
            // Ignore currently
        }
    }

    public void createDragImage(DragGestureEvent event) {
        Point dragOrigin = event.getDragOrigin();
        TreePath path = getPathForLocation(dragOrigin.x, dragOrigin.y);
        Rectangle r = getPathBounds(path);
        dragImageOffset.setLocation(dragOrigin.x - r.x, dragOrigin.y - r.y);

        JLabel label = (JLabel)getCellRenderer().getTreeCellRendererComponent(this, path.getLastPathComponent(),
                false, isExpanded(path), getModel().isLeaf(path.getLastPathComponent()), 0, false);
        label.setSize(r.width, r.height);

        dragImage = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2d = dragImage.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));
        label.paint(g2d);
        g2d.dispose();
    }

    // DropTargetListener interface method

    public void dragEnter(DropTargetDragEvent event) {
        oldSelectedPath = getSelectionPath();
        if(event.getDropAction() != getDelegateConstants()) {
            cleanUpAfterDrag();
            event.rejectDrag();
        } else
            event.acceptDrag(getDelegateConstants());
    }

    public void dragOver(DropTargetDragEvent event) {
        int x = event.getLocation().x;
        int y = event.getLocation().y;
        TreePath p = getClosestPathForLocation(x, y);
        if(p != lastPath) {
            lastPath = p;
            autoExpandTimer.restart();
        }

        if(p == null || event.getDropAction() != getDelegateConstants()) {
            cleanUpAfterDrag();
            event.rejectDrag();
        } else {
            event.acceptDrag(getDelegateConstants());

            Graphics2D g2d = (Graphics2D)getGraphics();
            Rectangle rpath = getPathBounds(lastPath);
            Rectangle oldCueLine = (Rectangle) cueLine.clone();
            if(y < rpath.y + rpath.height/2) {
                if(getRowForPath(lastPath) == 0) {
                    cueLine.setRect(0, rpath.y, getWidth(), 2);
                } else {
                    cueLine.setRect(0, rpath.y - 1, getWidth(), 2);
                }
                dropLocation = DROP_ABOVE;
            } else {
                cueLine.setRect(0, rpath.y + rpath.height - 1, getWidth(), 2);
                dropLocation = DROP_BELOW;
            }

            if(!oldCueLine.equals(cueLine)) {
                paintImmediately(oldCueLine);
            }

            g2d.setColor(Color.black);
            g2d.fill(cueLine);
        }
    }

    public void dropActionChanged(DropTargetDragEvent event) {
        if(event.getDropAction() != getDelegateConstants()) {
            cleanUpAfterDrag();
            event.rejectDrag();
        } else
            event.acceptDrag(getDelegateConstants());
    }

    public void dragExit(DropTargetEvent dte) {
        cleanUpAfterDrag();
    }

    public void drop(DropTargetDropEvent event) {
        autoExpandTimer.stop();

        int x = (int)event.getLocation().getX();
        int y = (int)event.getLocation().getY();
        int row = getRowForLocation(x, y);
        if(row == -1) {
            cleanUpAfterDrag();
            event.rejectDrop();
            return;
        }

        Object targetObject = ((DefaultMutableTreeNode)(getPathForRow(row).getLastPathComponent())).getUserObject();

        if(delegate.xjTreeDrop(this, getOldSelectedNode().getUserObject(), targetObject, dropLocation)) {
            scrollPathToVisible(getPathForLocation(x, y));
            event.acceptDrop(getDelegateConstants());
            event.dropComplete(true);
        } else
            event.rejectDrop();
        cleanUpAfterDrag();
    }

    // DragSourceListener interface method

    public void dragEnter(DragSourceDragEvent dsde) {
    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
    }

}
