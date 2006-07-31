package org.antlr.works.project;

import org.antlr.works.components.project.CContainerProject;
import org.antlr.works.components.project.file.CContainerProjectGrammar;
import org.antlr.works.components.project.file.CContainerProjectJava;
import org.antlr.works.components.project.file.CContainerProjectText;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

public class ProjectEditorZone {

    protected CContainerProject project;
    protected JPanel panel;
    protected JTabbedPane tabbedPane;
    protected JPanel emptyPanel;
    protected JPanel loadingPanel;

    public ProjectEditorZone(CContainerProject project) {
        this.project = project;

        emptyPanel = createEmptyEditorPanel();
        loadingPanel = createLoadingEditorPanel();

        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(new TabbedPaneChangeListener());
        tabbedPane.addMouseListener(new TabbedPaneMouseListener());

        panel = new JPanel(new BorderLayout());
        openFileItem(null);
    }

    public JPanel getPanel() {
        return panel;
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

    public JPanel createLoadingEditorPanel() {
        return createInfoPanel("Loading...");
    }

    public JPanel createEmptyEditorPanel() {
        return createInfoPanel("No Editor");
    }

    public void setEditorZoneToEmpty() {
        panel.remove(tabbedPane);
        panel.add(emptyPanel, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();

        project.setDefaultMainMenuBar();
    }

    public void setEditorZoneToTab() {
        panel.remove(emptyPanel);
        panel.add(tabbedPane, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    public void setProjectFileItem(ProjectFileItem item) {
        if(item == null)
            return;
                
        project.setMainMenuBar(item.getComponentContainer().getMainMenuBar());
        project.setToolBar(item.getComponentContainer().getEditor().getToolbarComponent());
        project.setStatusBar(item.getComponentContainer().getEditor().getStatusComponent());

        /** Tell the editor that is has been select. Do that later in order to avoid
         * another component to request the focus after the editor.
         */

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getSelectedFileItem().getComponentContainer().getEditor().componentIsSelected();
            }
        });
    }

    public void openFileItem(ProjectFileItem item) {
        if(item == null) {
            setEditorZoneToEmpty();
        } else {
            if(item.getComponentContainer() == null) {
                new ProjectFileItemFactory(item).create();
            } else {
                fileEditorItemDidLoad(item);
            }
        }
    }

    public void closeActiveEditor() {
        removeFileItemFromTab(getSelectedFileItem());
        project.refreshMainMenuBar();
    }
    
    public void fileEditorItemDidLoad(ProjectFileItem item) {
        addFileItemToTab(item);
        setEditorZoneToTab();
    }

    public void addFileItemToTab(ProjectFileItem item) {
        int index;
        if(isTabbedPaneContainFileItem(item)) {
            // The item is already in a tab. Just select the tab.
            index = getIndexOfFileItemInTab(item);
        } else {
            index = tabbedPane.getSelectedIndex();
            if(index == -1) {
                tabbedPane.addTab(item.getFileName(), item.getEditorPanel());
                index = tabbedPane.getComponentCount()-1;
            } else {
                index++;
                tabbedPane.insertTab(item.getFileName(), null, item.getEditorPanel(), null, index);
            }

            tabbedPane.setToolTipTextAt(index, item.getFilePath());
        }
        tabbedPane.setSelectedIndex(index);
        item.setOpened(true);
        item.setTabIndex(index);
    }

    public void removeFileItemFromTab(ProjectFileItem item) {
        int index = getIndexOfFileItemInTab(item);
        if(index == -1)
            return;
        
        tabbedPane.removeTabAt(index);
        if(tabbedPane.getComponentCount() == 0) {
            setEditorZoneToEmpty();
        } else {
            if(index > 0)
                index--;

            if(tabbedPane.getSelectedIndex() == index)
                setProjectFileItem(getSelectedFileItem());
            else
                tabbedPane.setSelectedIndex(index);
        }
        item.setOpened(false);
    }

    public boolean isTabbedPaneContainFileItem(ProjectFileItem item) {
        return getIndexOfFileItemInTab(item) != -1;
    }

    public int getIndexOfFileItemInTab(ProjectFileItem item) {
        for(int index=0; index<tabbedPane.getComponentCount(); index++) {
            if(tabbedPane.getComponentAt(index) == item.getEditorPanel())
                return index;
        }
        return -1;
    }

    public ProjectFileItem getFileItemForTabComponent(Component component) {
        if(component == null)
            return null;

        for (Iterator iterator = project.getFileEditorItems().iterator(); iterator.hasNext();) {
            ProjectFileItem item = (ProjectFileItem) iterator.next();
            if(item.getEditorPanel() == component)
                return item;
        }

        return null;
    }

    public ProjectFileItem getSelectedFileItem() {
        return getFileItemForTabComponent(tabbedPane.getSelectedComponent());
    }

    public static final String KEY_SELECTED_FILE_NAME = "KEY_SELECTED_FILE_NAME";

    public void setPersistentData(Map data) {
        if(data == null)
            return;

        String fileName = (String) data.get(KEY_SELECTED_FILE_NAME);
        if(fileName != null) {
            for(int index=0; index<tabbedPane.getComponentCount(); index++) {
                ProjectFileItem item = getFileItemForTabComponent(tabbedPane.getComponentAt(index));
                if(item == null)
                    continue;

                if(item.getFileName().equals(fileName)) {
                    tabbedPane.setSelectedIndex(index);
                    break;
                }
            }
        }
    }

    public Map getPersistentData() {
        Map data = new HashMap();
        if(getSelectedFileItem() != null)
            data.put(KEY_SELECTED_FILE_NAME, getSelectedFileItem().getFileName());
        return data;
    }

    public static final int DIRECTION_LEFT = -1;
    public static final int DIRECTION_RIGHT = 1;

    public void moveActiveEditor(int direction) {
        int index = tabbedPane.getSelectedIndex();
        if(index == -1)
            return;

        if(direction == DIRECTION_LEFT && index <= 0)
            return;

        if(direction == DIRECTION_RIGHT && index >= tabbedPane.getTabCount()-1)
            return;

        swapTab(index, index+direction);
    }

    public void swapTab(int a, int b) {
        String title = tabbedPane.getTitleAt(a);
        Component c = tabbedPane.getComponentAt(a);
        tabbedPane.removeTabAt(a);
        tabbedPane.insertTab(title, null, c, null, b);
        tabbedPane.setSelectedIndex(b);

        // Update also each ProjectFileItem's tab index
        getFileItemForTabComponent(tabbedPane.getComponentAt(a)).setTabIndex(a);
        getFileItemForTabComponent(tabbedPane.getComponentAt(b)).setTabIndex(b);
    }

    protected class TabbedPaneChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            setProjectFileItem(getSelectedFileItem());
        }
    }

    protected class TabbedPaneMouseListener extends MouseAdapter {

        public void displayPopUp(MouseEvent event) {
            if(!event.isPopupTrigger())
                return;

            JPopupMenu popup = new JPopupMenu();
            JMenuItem item = new JMenuItem("Close");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    closeActiveEditor();
                }
            });
            popup.add(item);
            popup.show(event.getComponent(), event.getX(), event.getY());
        }

        public void mousePressed(MouseEvent event) {
            displayPopUp(event);
        }

        public void mouseReleased(MouseEvent event) {
            displayPopUp(event);
        }
    }

    protected class ProjectFileItemFactory {

        public ProjectFileItem item;

        public ProjectFileItemFactory(ProjectFileItem item) {
            this.item = item;
        }

        public void create() {
            String type = ProjectFileItem.getFileType(item.getFilePath());
            if(type.equals(ProjectFileItem.FILE_TYPE_GRAMMAR))
                new CContainerProjectGrammar(project, item);
            else if(type.equals(ProjectFileItem.FILE_TYPE_JAVA))
                new CContainerProjectJava(project, item);
            else
                new CContainerProjectText(project, item);

            item.getComponentContainer().getDocument().performLoad(item.getFilePath());

            if(!item.isOpened()) {
                // Update the layout of the item only if it is created and not loaded
                // from disk (isOpened() will be false if it is being just created).
                // When loaded from disk, the project will contain all data needed
                // for the layout: that's why we don't need to layout (again) the item.
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        // @todo needs to get the size of the editor
                        //item.getComponentContainer().getEditor().componentShouldLayout();
                    }
                });
            }

            fileEditorItemDidLoad(item);
        }

    }

}
