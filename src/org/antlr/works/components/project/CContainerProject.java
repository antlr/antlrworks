package org.antlr.works.components.project;

import edu.usfca.xj.appkit.frame.XJFrame;
import edu.usfca.xj.appkit.frame.XJWindow;
import edu.usfca.xj.appkit.menu.XJMainMenuBar;
import edu.usfca.xj.appkit.swing.XJTree;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.foundation.XJUtils;
import org.antlr.works.components.ComponentContainer;
import org.antlr.works.components.ComponentEditor;
import org.antlr.works.components.project.file.CContainerProjectFile;
import org.antlr.works.components.project.file.CContainerProjectGrammar;
import org.antlr.works.components.project.file.CContainerProjectText;
import org.antlr.works.project.ProjectToolbar;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
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

public class CContainerProject extends XJWindow implements ComponentContainer {

    protected XJMainMenuBar projectDefaultMainMenuBar;
    protected ProjectToolbar toolbar;
    protected JSplitPane filesEditorSplitPane;

    protected JPanel projectPanel;
    protected XJTree filesTree;
    protected DefaultMutableTreeNode filesTreeRootNode;
    protected DefaultTreeModel filesTreeModel;

    protected ComponentContainer currentFileContainer;

    public CContainerProject() {
        projectPanel = new JPanel(new BorderLayout());

        toolbar = new ProjectToolbar(this);
        projectPanel.add(toolbar.getToolbar(), BorderLayout.NORTH);

        filesEditorSplitPane = new JSplitPane();
        filesEditorSplitPane.setBorder(null);
        filesEditorSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        filesEditorSplitPane.setLeftComponent(createFilesTree());
        filesEditorSplitPane.setRightComponent(currentEditorPanel());
        filesEditorSplitPane.setContinuousLayout(true);
        filesEditorSplitPane.setOneTouchExpandable(true);
        filesEditorSplitPane.setDividerLocation(150);

        projectPanel.add(filesEditorSplitPane, BorderLayout.CENTER);

        getContentPane().add(projectPanel);

        getJFrame().pack();
    }

    public void awake() {
        super.awake();
        projectDefaultMainMenuBar = mainMenuBar;

       // addFilePath("/Users/bovet/calc.g");
       // addFilePath("/Users/bovet/SMTPLexer.java");
    }

    public void setDefaultMainMenuBar() {
        setMainMenuBar(projectDefaultMainMenuBar);
    }

    public void setTitle(String title) {
        super.setTitle(title+" - [Project]");
    }

    public JComponent createFilesTree() {

        filesTree = new XJTree() {
            public String getToolTipText(MouseEvent e) {
                TreePath path = getPathForLocation(e.getX(), e.getY());
                if(path == null)
                    return "";

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                FileEditorItem item = (FileEditorItem) node.getUserObject();
                if(item == null)
                    return "";

                return item.filePath;
            }
        };

        filesTree.setBorder(null);
        // Apparently, if I don't set the tooltip here, nothing is displayed (weird)
        filesTree.setToolTipText("");
        filesTree.setDragEnabled(true);
        filesTree.setRootVisible(false);
        filesTree.setShowsRootHandles(true);
//        filesTree.setEnableDragAndDrop();

        filesTreeRootNode = new DefaultMutableTreeNode();
        filesTreeModel = new DefaultTreeModel(filesTreeRootNode);

        filesTree.setModel(filesTreeModel);
        filesTree.addTreeSelectionListener(new RuleTreeSelectionListener());

        JScrollPane scrollPane = new JScrollPane(filesTree);
        scrollPane.setWheelScrollingEnabled(true);
        return scrollPane;
    }

    public void addFilePath(String filePath) {
        filesTreeRootNode.add(new DefaultMutableTreeNode(new FileEditorItem(filePath)));
    }

    public void addFilePaths(List filePaths) {
        for (Iterator iterator = filePaths.iterator(); iterator.hasNext();) {
            String filePath = (String) iterator.next();
            addFilePath(filePath);
        }
        filesTreeModel.reload();
        getDocument().changeDone();
    }

    public void removeSelectedFile() {
        TreePath selPath[] = filesTree.getSelectionPaths();
        if(selPath == null || selPath.length < 1)
            return;

        boolean removed = false;

        for (int i = 0; i < selPath.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)selPath[i].getLastPathComponent();
            FileEditorItem item = (FileEditorItem)node.getUserObject();
            if(item.isDirty()) {
                int result = (XJAlert.displayAlertYESNOCANCEL(getJavaContainer(), "Save Content", "Do you want to save the content of "+item.getFileName()+" before removing it from the project ?"));
                switch(result) {
                    case XJAlert.CANCEL:
                        break;
                    case XJAlert.YES:
                        item.save();
                    case XJAlert.NO:
                        item.close();
                        filesTreeRootNode.remove(node);
                        removed = true;
                        break;
                }
            }
        }

        if(removed) {
            currentFileContainer = null;

            filesTreeModel.reload();
            getDocument().changeDone();
        }
    }

    public JPanel createInfoPanel(String info) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(info);
        l.setHorizontalAlignment(JLabel.CENTER);
        l.setFont(new Font("dialog", Font.PLAIN, 36));
        l.setForeground(Color.gray);
        p.add(l, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.lightGray));
        return p;
    }

    public JPanel loadingEditorPanel() {
        return createInfoPanel("Loading...");
    }

    public JPanel noEditorPanel() {
        return createInfoPanel("No Selected File");
    }

    public JPanel currentEditorPanel() {
        if(currentFileContainer == null)
            return noEditorPanel();
        else
            return currentFileContainer.getEditor().getPanel();
    }

    public void setEditorZonePanel(JPanel panel) {
        int loc = filesEditorSplitPane.getDividerLocation();
        filesEditorSplitPane.setRightComponent(panel);
        filesEditorSplitPane.setDividerLocation(loc);
        filesEditorSplitPane.repaint();
    }

    public void setDefaultSize() {
        Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        r.height *= 0.75;
        r.width *= 0.8;
        getRootPane().setPreferredSize(r.getSize());
    }

    public void loadText(String text) {
        // Not used for project
    }

    public String getText() {
        // Not used for project
        return null;
    }

    public boolean willSaveDocument() {
        // Not used for project
        return true;
    }

    public void close() {
        runClosureOnFileEditorItems(new FileEditorItemClosure() {
            public void process(FileEditorItem item) {
                item.close();
            }
        });

        super.close();
    }

    public void build() {
        
    }

    public ComponentEditor getEditor() {
        // There is no specific editor for the project
        return null;
    }

    public XJFrame getXJFrame() {
        return this;
    }

    public void windowActivated() {
        super.windowActivated();

        runClosureOnFileEditorItems(new FileEditorItemClosure() {
            public void process(FileEditorItem item) {
                item.handleExternalModification();
                item.windowActivated();
            }
        });
    }

    public void windowDocumentPathDidChange() {
        // Called when the document associated file has changed on the disk
        // Not used because we don't allow external modification of the project file
    }

    /** Project handling methods
     *
     */

    public void setCurrentFileEditor(FileEditorItem item) {
        if(item == null) {
            currentFileContainer = null;
            setMainMenuBar(projectDefaultMainMenuBar);
            setEditorZonePanel(currentEditorPanel());
        } else {
            currentFileContainer = item.getComponentContainer();
            if(currentFileContainer == null) {
                setEditorZonePanel(loadingEditorPanel());

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        currentFileContainer = getSelectedFileEditorItem().createEditor();
                    }
                });
            } else {
                fileEditorItemDidLoad(item);
            }
        }
    }

    public void fileEditorItemDidLoad(FileEditorItem item) {
        setMainMenuBar(item.container.getMainMenuBar());
        setEditorZonePanel(currentEditorPanel());
        currentFileContainer.getEditor().componentIsSelected();
    }

    public FileEditorItem getSelectedFileEditorItem() {
        TreePath selPath[] = filesTree.getSelectionPaths();
        if(selPath == null || selPath.length < 1)
            return null;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)selPath[0].getLastPathComponent();
        return (FileEditorItem)node.getUserObject();
    }

    public void changeCurrentEditor() {
        setCurrentFileEditor(getSelectedFileEditorItem());
    }

    /** Management of project's files
     *
     */

    public void fileDidBecomeDirty(CContainerProjectFile file) {
        getDocument().changeDone();
    }

    /** Persistence management
     *
     */

    public void documentWillSave() {
        runClosureOnFileEditorItems(new FileEditorItemClosure() {
            public void process(FileEditorItem item) {
                ComponentContainer container = item.getComponentContainer();
                if(container != null)
                    container.getDocument().performSave(false);
            }
        });
    }

    public void setPersistentData(Map data) {
        List files = (List)data.get("files");
        if(files != null) {
            addFilePaths(files);
        }
    }

    public Map persistentData() {
        Map data = new HashMap();

        List files = new ArrayList();

        for(int index=0; index<filesTreeRootNode.getChildCount(); index++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) filesTreeRootNode.getChildAt(index);
            FileEditorItem item = (FileEditorItem) node.getUserObject();
            files.add(item.filePath);
        }

        data.put("files", files);

        return data;
    }

    /** Utility methods
     *
     */

    public interface FileEditorItemClosure {
        public void process(FileEditorItem item);
    }

    public void runClosureOnFileEditorItems(FileEditorItemClosure closure) {
        for(int index=0; index<filesTreeRootNode.getChildCount(); index++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) filesTreeRootNode.getChildAt(index);
            FileEditorItem item = (FileEditorItem) node.getUserObject();
            closure.process(item);
        }
    }

    protected class RuleTreeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            changeCurrentEditor();
        }
    }

    protected class FileEditorItem {

        public String filePath;
        public ComponentContainer container;

        public FileEditorItem(String filePath) {
            this.filePath = filePath;
            this.container = null; // lazy initialization
        }

        public boolean isDirty() {
            if(container != null)
                return container.getDocument().isDirty();
            else
                return false;
        }

        public void save() {
            if(container != null)
                container.getDocument().performSave(false);
        }

        public void close() {
            if(container != null)
                container.close();
        }

        public ComponentContainer createEditor() {
            if(filePath.endsWith(".g")) {
                container = new CContainerProjectGrammar(CContainerProject.this);
            } else {
                container = new CContainerProjectText(CContainerProject.this);
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    container.getDocument().performLoad(filePath);
                    fileEditorItemDidLoad(FileEditorItem.this);
                }
            });

            return container;
        }

        public String getFileName() {
            return XJUtils.getLastPathComponent(filePath);
        }

        public ComponentContainer getComponentContainer() {
            return container;
        }

        public void windowActivated() {
            if(container != null)
                container.getEditor().componentActivated();
        }

        public void handleExternalModification() {
            if(container == null)
                return;

            if(container.getDocument().isModifiedOnDisk()) {
                container.getEditor().componentDocumentContentChanged();
            }
        }

        /** Called by the XJTree to display the cell content. Use only the last path component
         * (that is the name of file) only.
         */

        public String toString() {
            return getFileName();
        }

    }
}
