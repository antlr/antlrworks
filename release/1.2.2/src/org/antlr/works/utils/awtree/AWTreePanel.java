package org.antlr.works.utils.awtree;

import org.antlr.works.utils.IconManager;
import org.antlr.works.utils.Toolbar;
import org.antlr.works.utils.TreeUtilities;
import org.antlr.xjlib.appkit.gview.object.GElement;
import org.antlr.xjlib.appkit.swing.XJRollOverButton;
import org.antlr.xjlib.appkit.utils.XJAlert;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;

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

public class AWTreePanel extends JPanel {

    protected Component listViewComponent;
    protected Component graphViewComponent;

    protected JTree tree;
    protected DefaultTreeModel jTreeModel;

    protected AWTreeGraphView treeGraphView;
    protected JScrollPane graphScrollPane;

    protected AWTreePanelDelegate delegate;

    public AWTreePanel(DefaultTreeModel treeModel) {
        super(new BorderLayout());

        this.jTreeModel = treeModel;
        tree = new JTree(treeModel);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
        treeRenderer.setClosedIcon(null);
        treeRenderer.setLeafIcon(null);
        treeRenderer.setOpenIcon(null);

        tree.setCellRenderer(treeRenderer);

        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 2) {
                        displayNodeInfo(selPath.getLastPathComponent());
                        e.consume();
                    }
                }
            }

        });

        listViewComponent = createListView();
        graphViewComponent = createGraphView();

        add(graphViewComponent, BorderLayout.CENTER);
    }

    public void setDelegate(AWTreePanelDelegate delegate) {
        this.delegate = delegate;
    }

    public void setAWTreeModel(AWTreeModel model) {
        treeGraphView.setModel(model);
    }

    public Component createListView() {
        JPanel panel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setBackground(Color.white);

        Toolbar box = Toolbar.createHorizontalToolbar();
        box.addElement(createExpandAllButton());
        box.addElement(createCollapseAllButton());
        box.add(Box.createHorizontalGlue());
        box.addElement(createDisplayAsGraphButton());
        //if(XJSystem.isMacOS())
        //    box.add(Box.createHorizontalStrut(15));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);

        return panel;
    }

    public JButton createDisplayAsGraphButton() {
        JButton button = XJRollOverButton.createMediumButton(IconManager.shared().getIconGraph());
        button.setToolTipText("Display as Graph");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                toggleGraph();
            }
        });
        return button;
    }

    public JButton createExpandAllButton() {
        JButton button = XJRollOverButton.createMediumButton(IconManager.shared().getIconExpandAll());
        button.setToolTipText("Expand All");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                TreeUtilities.expandAll(tree);
            }
        });
        return button;
    }

    public JButton createCollapseAllButton() {
        JButton button = XJRollOverButton.createMediumButton(IconManager.shared().getIconCollapseAll());
        button.setToolTipText("Collapse All");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                TreeUtilities.collapseAll(tree);
            }
        });
        return button;
    }

    public Component createGraphView() {
        JPanel panel = new JPanel(new BorderLayout());

        treeGraphView = new AWTreeGraphView(this);
        treeGraphView.setAutoAdjustSize(true);
        treeGraphView.setBackground(Color.white);
        treeGraphView.setDrawBorder(false);

        ParseTreeMouseAdapter adapter = new ParseTreeMouseAdapter();
        treeGraphView.addMouseListener(adapter);
        treeGraphView.addMouseMotionListener(adapter);

        graphScrollPane = new JScrollPane(treeGraphView);
        graphScrollPane.setWheelScrollingEnabled(true);

        Toolbar box = Toolbar.createHorizontalToolbar();
        box.addElement(new JLabel("Zoom"));
        box.addElement(createZoomSlider());
        box.add(Box.createHorizontalGlue());
        box.addElement(createDisplayAsListButton());
        //if(XJSystem.isMacOS())
        //    box.add(Box.createHorizontalStrut(15));

        panel.add(graphScrollPane, BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);

        return panel;
    }

    public JButton createDisplayAsListButton() {
        JButton button = XJRollOverButton.createMediumButton(IconManager.shared().getIconListTree());
        button.setToolTipText("Display as List");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                toggleGraph();
            }
        });
        return button;
    }

    public JSlider createZoomSlider() {
        JSlider slider = new JSlider();
        slider.setFocusable(false);
        slider.setMinimum(1);
        slider.setMaximum(200);
        slider.setValue(100);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                JSlider slider = (JSlider)event.getSource();

                treeGraphView.setZoom((float)slider.getValue()/100);
                treeGraphView.repaint();
                treeGraphView.revalidate();
            }
        });
        return slider;
    }

    public void setRootVisible(boolean flag) {
        tree.setRootVisible(flag);
    }

    public void setRoot(TreeNode node) {
        jTreeModel.setRoot(node);
        treeGraphView.setRoot(node);
    }

    public Object getRoot() {
        return jTreeModel.getRoot();
    }

    public AWTreeGraphView getGraphView() {
        return treeGraphView;
    }

    public void clear() {
        treeGraphView.clear();
    }

    public void refresh() {
        jTreeModel.reload();
        //TreeUtilities.expandAll(tree);
        treeGraphView.refresh();
    }

    public void toggleGraph() {
        if(getComponent(0) == listViewComponent) {
            remove(listViewComponent);
            add(graphViewComponent, BorderLayout.CENTER);
        } else {
            remove(graphViewComponent);
            add(listViewComponent, BorderLayout.CENTER);
        }
        repaint();
        revalidate();
    }

    public void selectNode(TreeNode node) {
        TreePath path = new TreePath(jTreeModel.getPathToRoot(node));
        tree.scrollPathToVisible(path);
        tree.setSelectionPath(path);

        treeGraphView.highlightNode(node);
    }

    public void scrollNodeToVisible(TreeNode node) {
        if(node == null)
            return;

        tree.scrollPathToVisible(new TreePath(jTreeModel.getPathToRoot(node)));
        treeGraphView.scrollNodeToVisible(node);
    }

    public void displayNodeInfo(Object node) {
        AWTreeNode n = (AWTreeNode)node;
        XJAlert.display(this, "Node info", n.getInfoString());
    }

    public JPopupMenu getContextualMenu() {
        if(delegate != null)
            return delegate.awTreeGetContextualMenu();
        else
            return null;
    }

    protected class ParseTreeMouseAdapter implements MouseListener, MouseMotionListener {

        public Point origin;
        public Rectangle r;
        public boolean dragging;

        public void mousePressed(MouseEvent e) {
            GElement elem = treeGraphView.getElementAtMousePosition(e);
            if(elem != null && elem instanceof AWTreeGraphView.GElementNode) {
                TreeNode node = treeGraphView.getTreeNodeForElement((AWTreeGraphView.GElementNode)elem);
                if(node == null)
                    return;

                boolean shiftKey = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK;
                if(delegate != null)
                    delegate.awTreeDidSelectTreeNode(node, shiftKey);

                selectNode(node);
            }

            origin = SwingUtilities.convertPoint(treeGraphView, e.getPoint(), null);
            r = treeGraphView.getVisibleRect();
            dragging = true;
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
            dragging = false;
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            if(!dragging)
                return;

            Point p = SwingUtilities.convertPoint(treeGraphView, e.getPoint(), null);

            Rectangle r1 = new Rectangle(r);
            r1.x -= p.x - origin.x;
            r1.y -= p.y - origin.y;
            treeGraphView.scrollRectToVisible(r1);
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

}
