package org.antlr.works.parsetree;

import edu.usfca.xj.appkit.gview.GView;
import edu.usfca.xj.appkit.utils.XJAlert;
import org.antlr.works.editor.swing.TreeUtilities;
import org.antlr.works.util.IconManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

public class ParseTreePanel extends JPanel {

    protected Component listViewComponent;
    protected Component graphViewComponent;

    protected JTree tree;
    protected DefaultTreeModel treeModel;

    protected ParseTreeGraphView parseTreeGraphView;
    protected JScrollPane graphScrollPane;

    public ParseTreePanel(DefaultTreeModel treeModel) {
        super(new BorderLayout());

        this.treeModel = treeModel;
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

    public Component createListView() {
        JPanel panel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setWheelScrollingEnabled(true);

        Box box = Box.createHorizontalBox();
        box.add(createExpandAllButton());
        box.add(createCollapseAllButton());
        box.add(Box.createHorizontalGlue());
        box.add(createDisplayAsGraphButton());

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);

        return panel;
    }

    public JButton createDisplayAsGraphButton() {
        JButton button = new JButton(IconManager.shared().getIconGraph());
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
        JButton button = new JButton(IconManager.shared().getIconExpandAll());
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
        JButton button = new JButton(IconManager.shared().getIconCollapseAll());
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

        parseTreeGraphView = new ParseTreeGraphView();
        parseTreeGraphView.setAutoAdjustSize(true);
        parseTreeGraphView.setBackground(Color.white);
        parseTreeGraphView.setDrawBorder(false);

        graphScrollPane = new JScrollPane(parseTreeGraphView);
        graphScrollPane.setWheelScrollingEnabled(true);

        Box box = Box.createHorizontalBox();
        box.add(new JLabel("Zoom"));
        box.add(createZoomSlider());
        box.add(Box.createHorizontalGlue());
        box.add(createDisplayAsListButton());

        panel.add(graphScrollPane, BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);

        return panel;
    }

    public JButton createDisplayAsListButton() {
        JButton button = new JButton(IconManager.shared().getIconListTree());
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

                parseTreeGraphView.setZoom((float)slider.getValue()/100);
                parseTreeGraphView.repaint();
                parseTreeGraphView.revalidate();
            }
        });
        return slider;
    }

    public void setRoot(TreeNode node) {
        treeModel.setRoot(node);
        parseTreeGraphView.setRoot(node);
        refresh();
    }

    public Object getRoot() {
        return treeModel.getRoot();
    }

    public GView getGraphView() {
        return parseTreeGraphView;
    }

    public void refresh() {
        treeModel.reload();
        TreeUtilities.expandAll(tree);
        parseTreeGraphView.refresh();
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
        TreePath path = new TreePath(treeModel.getPathToRoot(node));
        tree.scrollPathToVisible(path);
        tree.setSelectionPath(path);

        parseTreeGraphView.highlightNode(node);
    }

    public void scrollNodeToVisible(TreeNode node) {
        tree.scrollPathToVisible(new TreePath(treeModel.getPathToRoot(node)));
        parseTreeGraphView.scrollNodeToVisible(node);
    }

    public void displayNodeInfo(Object node) {
        ParseTreeNode n = (ParseTreeNode)node;
        XJAlert.display(this, "Node info", n.getInfoString());
    }

}
