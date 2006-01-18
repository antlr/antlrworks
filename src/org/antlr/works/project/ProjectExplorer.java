package org.antlr.works.project;

import edu.usfca.xj.appkit.swing.XJTree;
import edu.usfca.xj.foundation.XJUtils;
import org.antlr.works.components.project.CContainerProject;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;
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

public class ProjectExplorer {

    protected CContainerProject project;
    protected JComponent container;

    protected ExplorerLoader loader;

    protected XJTree filesTree;
    protected DefaultMutableTreeNode filesTreeRootNode;
    protected DefaultTreeModel filesTreeModel;

    public ProjectExplorer(CContainerProject project) {
        this.project = project;
        loader = new ExplorerLoader();

        create();
    }

    public void create() {

        filesTree = new XJTree();

        filesTree.setBorder(null);
        // Apparently, if I don't set the tooltip here, nothing is displayed (weird)
        filesTree.setToolTipText("");
        filesTree.setDragEnabled(true);
        filesTree.setRootVisible(false);
        filesTree.setShowsRootHandles(true);
//        filesTree.setEnableDragAndDrop();
        filesTree.setCellRenderer(new CustomTableRenderer());

        filesTreeRootNode = new DefaultMutableTreeNode();
        filesTreeModel = new DefaultTreeModel(filesTreeRootNode);

        filesTree.setModel(filesTreeModel);
        filesTree.addTreeSelectionListener(new FilesTreeSelectionListener());
        filesTree.addMouseListener(new FilesTreeMouseListener());

        JScrollPane scrollPane = new JScrollPane(filesTree);
        scrollPane.setWheelScrollingEnabled(true);

        container = scrollPane;
    }

    public Component getPanel() {
        return container;
    }

    public List getFileEditorItems() {
        return loader.getAllFiles();
    }

    public void close() {
        runClosureOnFileEditorItems(new FileEditorItemClosure() {
            public void process(ProjectFileItem item) {
                item.close();
            }
        });
    }

    public void reopen() {
        runClosureOnFileEditorItems(new FileEditorItemClosure() {
            public void process(ProjectFileItem item) {
                if(item.isOpened())
                    project.openFileItem(item);
            }
        });
    }

    public void reload() {
        loader.reload();
        filesTreeRootNode.removeAllChildren();

        for (Iterator groupIterator = loader.getGroups().iterator(); groupIterator.hasNext();) {
            String groupName = (String) groupIterator.next();

            DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(groupName);
            filesTreeRootNode.add(groupNode);
            for (Iterator fileItemIterator = loader.getFiles(groupName).iterator(); fileItemIterator.hasNext();) {
                ProjectFileItem item = (ProjectFileItem) fileItemIterator.next();
                groupNode.add(new DefaultMutableTreeNode(item));
            }
            filesTree.expandPath(new TreePath(groupNode.getPath()));
        }

        filesTreeModel.reload();
    }

    public void saveAll() {
        runClosureOnFileEditorItems(new FileEditorItemClosure() {
            public void process(ProjectFileItem item) {
                if(item.save()) {
                    // Reset modification date in the build list
                    project.getBuildList().resetModificationDate(item);
                }
            }
        });
    }

    public void setIgnoreFromBuild(ProjectFileItem item, boolean ignore) {
        project.getBuildList().setIgnoreBuild(item, !ignore);
        filesTree.repaint();
    }

    public void windowActivated() {
        runClosureOnFileEditorItems(new FileEditorItemClosure() {
            public void process(ProjectFileItem item) {
                if(item.handleExternalModification())
                    project.changeDone();
                item.windowActivated();
            }
        });
    }

    public static final String KEY_FILE_ITEMS = "KEY_FILE_ITEMS";
    public static final String KEY_EXPANDED_ITEMS = "KEY_EXPANDED_ITEMS";

    public void setPersistentData(Map data) {
        reload();

        if(data != null) {
            List expanded = (List) data.get(KEY_EXPANDED_ITEMS);
            if(expanded != null) {
                for (Iterator iterator = expanded.iterator(); iterator.hasNext();) {
                    String name = (String) iterator.next();
                    for (int i = 0; i < filesTreeRootNode.getChildCount(); i++) {
                        DefaultMutableTreeNode child = (DefaultMutableTreeNode)filesTreeRootNode.getChildAt(i);
                        if(child.getUserObject().equals(name)) {
                            filesTree.expandPath(new TreePath(child.getPath()));
                        }
                    }
                }
            }

            Map fileItems = (Map) data.get(KEY_FILE_ITEMS);
            if(fileItems != null) {
                for (Iterator iterator = fileItems.keySet().iterator(); iterator.hasNext();) {
                    String fileName = (String)iterator.next();
                    ProjectFileItem item = loader.getFileItemForFileName(fileName);
                    if(item != null) {
                        // Only apply data to existing files
                        item.setPersistentData((Map) fileItems.get(fileName));
                    }
                }
            }
        }

        reopen();
    }

    public Map getPersistentData() {
        Map data = new HashMap();

        List expanded = new ArrayList();
        for (int i = 0; i < filesTreeRootNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)filesTreeRootNode.getChildAt(i);
            if(filesTree.isExpanded(new TreePath(child.getPath()))) {
                expanded.add(child.getUserObject());
            }
        }
        data.put(KEY_EXPANDED_ITEMS, expanded);

        Map fileItems = new HashMap();
        for (Iterator iterator = getFileEditorItems().iterator(); iterator.hasNext();) {
            ProjectFileItem item = (ProjectFileItem) iterator.next();
            fileItems.put(item.getFileName(), item.getPersistentData());
        }
        data.put(KEY_FILE_ITEMS, fileItems);

        return data;
    }

    public interface FileEditorItemClosure {
        public void process(ProjectFileItem item);
    }

    public void runClosureOnFileEditorItems(FileEditorItemClosure closure) {
        for (Iterator iterator = loader.getAllFiles().iterator(); iterator.hasNext();) {
            ProjectFileItem item = (ProjectFileItem) iterator.next();
            closure.process(item);
        }
    }

    protected class FilesTreeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
        }
    }

    protected class FilesTreeMouseListener extends MouseAdapter {

        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount() == 2) {
                project.openFileItem(getFileItemAtLocation(e.getX(), e.getY()));
            }
        }

        public void displayPopUp(MouseEvent me) {
            if(!me.isPopupTrigger())
                return;

            ProjectFileItem fileItem = getFileItemAtLocation(me.getX(), me.getY());
            if(fileItem == null)
                return;

            modifySelectionIfNecessary(me);

            String title;
            boolean ignore = project.getBuildList().isIgnoreBuild(fileItem);
            if(ignore)
                title = "Add to build list";
            else
                title = "Remove from build list";

            JMenuItem item = new JMenuItem(title);
            item.addActionListener(new FileContextualMenu(ignore));

            JPopupMenu popup = new JPopupMenu();
            popup.add(item);
            popup.show(me.getComponent(), me.getX(), me.getY());
        }

        public void mousePressed(MouseEvent event) {
            displayPopUp(event);
        }

        public void mouseReleased(MouseEvent event) {
            displayPopUp(event);
        }

        public ProjectFileItem getFileItemAtLocation(int x, int y) {
            if(filesTree.getRowForLocation(x, y) == -1)
                return null;

            TreePath p = filesTree.getPathForLocation(x, y);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)p.getLastPathComponent();
            Object userObject = node.getUserObject();
            if(userObject instanceof ProjectFileItem)
                return (ProjectFileItem)userObject;
            else
                return null;
        }

        /** This method deselects the current selection of the item
         * under the mouse location is not part of the current selection
         */

        public void modifySelectionIfNecessary(MouseEvent me) {
            boolean partOfSelection = false;
            int row = filesTree.getRowForLocation(me.getX(), me.getY());
            if(filesTree.getSelectionRows() != null) {
                for (int i = 0; i < filesTree.getSelectionRows().length; i++) {
                    int selRow = filesTree.getSelectionRows()[i];
                    if(selRow == row)
                        partOfSelection = true;
                }
            }

            if(!partOfSelection)
                filesTree.setSelectionRow(row);
        }

    }

    protected class FileContextualMenu implements ActionListener {

        public boolean ignore;

        public FileContextualMenu(boolean ignore) {
            this.ignore = ignore;
        }

        public void actionPerformed(ActionEvent event) {
            for (int i = 0; i < filesTree.getSelectionPaths().length; i++) {
                TreePath treePath = filesTree.getSelectionPaths()[i];
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                if(node.getUserObject() instanceof ProjectFileItem) {
                    setIgnoreFromBuild((ProjectFileItem)node.getUserObject(), ignore);
                }
            }
        }
    }

    protected class ExplorerLoader {

        protected Map groups = new HashMap();
        protected String sourcePath;

        public boolean reload() {
            boolean reset = false;

            if(sourcePath == null || !sourcePath.equals(project.getSourcePath())) {
                groups.clear();
                sourcePath = project.getSourcePath();
                reset = true;
            }

            if(sourcePath == null)
                return reset;

            for (Iterator iterator = XJUtils.sortedFilesInPath(sourcePath).iterator(); iterator.hasNext();) {
                File f = (File) iterator.next();
                String name = f.getName();

                String type = ProjectFileItem.getFileType(name);
                if(type.equals(ProjectFileItem.FILE_TYPE_UNKNOWN))
                    continue;

                addFileOfType(name, ProjectFileItem.getFileTypeName(type));
            }

            return reset;
        }

        public void addFileOfType(String name, String type) {
            List files = getFiles(type);
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                ProjectFileItem fileItem = (ProjectFileItem) iterator.next();
                if(fileItem.getFileName().equals(name))
                    return;
            }
            files.add(new ProjectFileItem(project, name));
        }

        public List getGroups() {
            return new ArrayList(groups.keySet());
        }

        public List getAllFiles() {
            List allFiles = new ArrayList();
            for (Iterator iterator = groups.values().iterator(); iterator.hasNext();) {
                List files = (List) iterator.next();
                allFiles.addAll(files);
            }
            return allFiles;
        }

        public ProjectFileItem getFileItemForFileName(String filename) {
            for (Iterator groupIterator = groups.values().iterator(); groupIterator.hasNext();) {
                List files = (List) groupIterator.next();
                for (Iterator fileIterator = files.iterator(); fileIterator.hasNext();) {
                    ProjectFileItem item = (ProjectFileItem) fileIterator.next();
                    if(item.getFileName().equals(filename))
                        return item;
                }
            }
            return null;
        }

        public List getFiles(String name) {
            List files = (List) groups.get(name);
            if(files == null) {
                files = new ArrayList();
                groups.put(name, files);
            }
            return files;
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

            setToolTipText("");

            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            if(node.getUserObject() instanceof ProjectFileItem) {
                ProjectFileItem item = (ProjectFileItem)node.getUserObject();
                ProjectBuildList.BuildFile f = project.getBuildList().getBuildFile(item);
                if(f != null && f.isIgnore())
                    setForeground(Color.gray);
                else
                    setForeground(Color.black);
            } else {
                // Do not display the default folder for group because
                // they are not folders ;-)
                setIcon(null);
            }

            return r;
        }
    }

}
