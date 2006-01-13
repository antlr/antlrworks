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
import java.util.Iterator;
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
        return createInfoPanel("No Selected File");
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

    public void setMainMenuBar(ProjectFileItem item) {
        if(item != null)
            project.setMainMenuBar(item.getComponentContainer().getMainMenuBar());
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

    public void fileEditorItemDidLoad(ProjectFileItem item) {
        addFileItemToTab(item);
        setEditorZoneToTab();
        item.getComponentContainer().getEditor().componentIsSelected();
    }

    public void addFileItemToTab(ProjectFileItem item) {
        int index;
        if(isTabbedPaneContainFileItem(item)) {
            // The item is already in a tab. Just select the tab.
            index = getIndexOfFileItemInTab(item);
        } else {
            tabbedPane.addTab(item.getFileName(), item.getEditorPanel());
            index = tabbedPane.getComponentCount()-1;
        }
        tabbedPane.setSelectedIndex(index);
    }

    public void removeFileItemFromTab(ProjectFileItem item) {
        int index = getIndexOfFileItemInTab(item);
        tabbedPane.removeTabAt(index);
        if(tabbedPane.getComponentCount() == 0) {
            setEditorZoneToEmpty();
        } else {
            if(index > 0)
                index--;

            if(tabbedPane.getSelectedIndex() == index)
                setMainMenuBar(getSelectedFileItem());
            else
                tabbedPane.setSelectedIndex(index);
        }
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

    public ProjectFileItem getSelectedFileItem() {
        Component component = tabbedPane.getSelectedComponent();
        if(component == null)
            return null;

        for (Iterator iterator = project.getFileEditorItems().iterator(); iterator.hasNext();) {
            ProjectFileItem item = (ProjectFileItem) iterator.next();
            if(item.getEditorPanel() == component)
                return item;
        }
        return null;
    }

    protected class TabbedPaneChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            setMainMenuBar(getSelectedFileItem());
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
                    removeFileItemFromTab(getSelectedFileItem());
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
            fileEditorItemDidLoad(item);
        }

    }

}
