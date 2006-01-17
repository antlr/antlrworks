package org.antlr.works.project;

import edu.usfca.xj.appkit.swing.XJTree;
import org.antlr.works.components.project.CContainerProject;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
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

    public void windowActivated() {
        runClosureOnFileEditorItems(new FileEditorItemClosure() {
            public void process(ProjectFileItem item) {
                if(item.handleExternalModification())
                    project.changeDone();
                item.windowActivated();
            }
        });
    }

    public void saveAll() {
        runClosureOnFileEditorItems(new FileEditorItemClosure() {
            public void process(ProjectFileItem item) {
                if(item.save()) {
                    // Reset modification date in the build list
                    project.getBuildList().resetModificationDate(item.getFilePath(), item.getFileType());
                }
            }
        });
    }

    public void setPersistentData(Map data) {
        reload();

        if(data != null) {
            for (Iterator iterator = data.keySet().iterator(); iterator.hasNext();) {
                String fileName = (String)iterator.next();
                ProjectFileItem item = loader.getFileItemForFileName(fileName);
                if(item != null) {
                    // Only apply data to existing files
                    item.setPersistentData((Map) data.get(fileName));
                }
            }
        }

        reopen();
    }

    public Map getPersistentData() {
        Map data = new HashMap();
        for (Iterator iterator = getFileEditorItems().iterator(); iterator.hasNext();) {
            ProjectFileItem item = (ProjectFileItem) iterator.next();
            data.put(item.getFileName(), item.getPersistentData());
        }
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
                if(filesTree.getRowForLocation(e.getX(), e.getY()) != -1) {
                    TreePath p = filesTree.getPathForLocation(e.getX(), e.getY());
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)p.getLastPathComponent();
                    Object userObject = node.getUserObject();
                    if(userObject instanceof ProjectFileItem) {
                        project.openFileItem((ProjectFileItem)userObject);
                    }
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

            File[] files = new File(sourcePath).listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String name = file.getName();

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
}
