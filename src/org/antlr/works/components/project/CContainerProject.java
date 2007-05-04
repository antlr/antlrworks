package org.antlr.works.components.project;

import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.appkit.frame.XJFrameInterface;
import edu.usfca.xj.appkit.frame.XJWindow;
import edu.usfca.xj.appkit.menu.XJMainMenuBar;
import edu.usfca.xj.appkit.menu.XJMenu;
import edu.usfca.xj.appkit.menu.XJMenuItem;
import edu.usfca.xj.appkit.menu.XJMenuItemDelegate;
import edu.usfca.xj.foundation.XJUtils;
import org.antlr.works.components.ComponentContainer;
import org.antlr.works.components.ComponentEditor;
import org.antlr.works.components.ComponentStatusBar;
import org.antlr.works.components.project.file.CContainerProjectFile;
import org.antlr.works.project.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
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

public class CContainerProject extends XJWindow implements ComponentContainer, XJMenuItemDelegate {

    protected XJMainMenuBar projectDefaultMainMenuBar;
    protected JSplitPane splitPaneA;
    protected JSplitPane splitPaneB;

    protected JPanel projectPanel;
    protected Box projectStatusBar;

    protected ProjectToolbar projectToolbar;
    protected ProjectExplorer explorer;
    protected ProjectEditorZone editorZone;

    protected ProjectBuilder builder;
    protected ProjectConsole console;
    protected ProjectBuildList buildList;

    public CContainerProject() {
        builder = new ProjectBuilder(this);
        console = new ProjectConsole();
        buildList = new ProjectBuildList();

        projectPanel = new JPanel(new BorderLayout());

        projectToolbar = new ProjectToolbar(this);
        projectPanel.add(projectToolbar.getToolbar(), BorderLayout.NORTH);

        projectStatusBar = new ComponentStatusBar();
        projectStatusBar.setPreferredSize(new Dimension(0, 30));

        projectPanel.add(projectStatusBar, BorderLayout.SOUTH);

        explorer = new ProjectExplorer(this);
        editorZone = new ProjectEditorZone(this);

        splitPaneA = new JSplitPane();
        splitPaneA.setBorder(null);
        splitPaneA.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPaneA.setLeftComponent(explorer.getPanel());
        splitPaneA.setRightComponent(editorZone.getPanel());
        splitPaneA.setContinuousLayout(true);
        splitPaneA.setOneTouchExpandable(true);
        splitPaneA.setDividerLocation(150);

        splitPaneB = new JSplitPane();
        splitPaneB.setBorder(null);
        splitPaneB.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneB.setLeftComponent(splitPaneA);
        splitPaneB.setRightComponent(console.getContainer());
        splitPaneB.setContinuousLayout(true);
        splitPaneB.setOneTouchExpandable(true);
        splitPaneB.setDividerLocation(getRootPane().getPreferredSize().height);

        projectPanel.add(splitPaneB, BorderLayout.CENTER);

        getContentPane().add(projectPanel);

        pack();
    }

    public void awake() {
        super.awake();
        getData().setProject(this);
        projectDefaultMainMenuBar = mainMenuBar;
    }

    public void setDefaultSize() {
        Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        r.height *= 0.75;
        r.width *= 0.8;
        getRootPane().setPreferredSize(r.getSize());
    }

    public void setToolBar(JComponent toolbar) {
        projectToolbar.setCustomToolbar(toolbar);
    }

    public void setStatusBar(JComponent statusbar) {
        if(projectStatusBar.getComponentCount() > 0)
            projectStatusBar.remove(0);

        if(statusbar != null)
            projectStatusBar.add(statusbar, BorderLayout.CENTER);

        projectStatusBar.revalidate();
        projectStatusBar.repaint();
    }

    public void setDefaultMainMenuBar() {
        if(getMainMenuBar() != projectDefaultMainMenuBar)
            setMainMenuBar(projectDefaultMainMenuBar);
    }

    public void refreshMainMenuBar() {
        if(getXJFrame().getMainMenuBar() != null)
            getXJFrame().getMainMenuBar().refreshState();
    }

    public void setTitle(String title) {
        super.setTitle(title+" - [Project]");
    }

    public ProjectData getData() {
        return ((CDocumentProject)getDocument()).data;
    }

    public ProjectBuildList getBuildList() {
        return buildList;
    }

    public String getSourcePath() {
        return getData().getSourcePath();
    }

    public void makeBottomComponentVisible() {
        if(splitPaneB.getBottomComponent().getHeight() == 0) {
            splitPaneB.setDividerLocation(0.6);
        }
    }

    public XJFrameInterface getXJFrame() {
        return this;
    }

    public void becomingVisibleForTheFirstTime() {
        // Show the Project Settings if the project is just created
        if(getDocument().getDocumentPath() == null) {
            getDocument().changeDone();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    projectSettings();
                }
            });
        }
    }

    public void windowActivated() {
        super.windowActivated();

        if(getBuildList().handleExternalModification())
            changeDone();

        explorer.windowActivated();
    }

    public void windowDeactivated() {
        explorer.saveAll();
    }

    public void windowDocumentPathDidChange() {
        // Called when the document associated file has changed on the disk
        // Not used because we don't allow external modification of the project file
    }

    public void changeDone() {
        getDocument().changeDone();
    }

    public void openFileItem(ProjectFileItem item) {
        if(item != null) {
            editorZone.openFileItem(item);
            changeDone();
        }
    }

    public void closeFileItem(ProjectFileItem fileItem) {
        editorZone.removeFileItemFromTab(fileItem);
        fileItem.close();
    }
    
    public List getFileEditorItems() {
        return explorer.getFileEditorItems();
    }

    public String[] getRunParameters() {
        String s = getData().getRunParametersString();
        if(s == null)
            return null;
        else
            return s.split(" ");
    }

    public void projectSettings() {
        ProjectSettingsDialog dialog = new ProjectSettingsDialog(getJavaContainer());
        dialog.setSourcePath(getData().getSourcePath());
        if(dialog.runModal() == XJDialog.BUTTON_OK) {
            getData().setSourcePath(dialog.getSourcePath());
            explorer.reload();
            changeDone();
        }
    }

    public void runSettings(boolean runAfterSettings) {
        ProjectRunSettingsDialog dialog = new ProjectRunSettingsDialog(getJavaContainer());
        dialog.setRunParametersString(getData().getRunParametersString());
        dialog.setShowBeforeRunning(getData().getShowBeforeRunning());
        if(dialog.runModal() == XJDialog.BUTTON_OK) {
            changeDone();
            getData().setRunParametersString(dialog.getRunParametersString());
            getData().setShowBeforeRunning(dialog.isShowBeforeRunning());
            if(runAfterSettings) {
                buildAndRun();
            }
        }
    }

    public void run() {
        String[] params = getRunParameters();
        if(params == null || params.length == 0 || getData().getShowBeforeRunning())
            runSettings(true);
        else
            buildAndRun();
    }

    public void clean() {
        builder.clean();
    }

    public void buildFile() {
        ProjectFileItem item = editorZone.getSelectedFileItem();
        if(item != null) {
            explorer.saveAll();
            clearConsole();

            // Build the file
            builder.buildFile(item);
        }
    }

    public void buildAll() {
        explorer.saveAll();
        clearConsole();
        builder.buildAll();
    }

    public void buildAndRun() {
        explorer.saveAll();
        clearConsole();
        builder.run();
    }

    public void buildReportError(String error) {
        printToConsole(error);
    }

    public void clearConsole() {
        console.clear();
    }

    public void printToConsole(String s) {
        console.print(s);
        makeBottomComponentVisible();
    }

    public void printToConsole(Exception e) {
        printToConsole(XJUtils.stackTrace(e));
    }

    public void close() {
        // Automatically save the project
        getDocument().performAutoSave();

        explorer.close();
        super.close();
    }

    public static final String KEY_WINDOW_LOC = "KEY_WINDOW_LOC";
    public static final String KEY_WINDOW_SIZE = "KEY_WINDOW_SIZE";
    public static final String KEY_SPLITPANE_A = "KEY_SPLITPANE_A";
    public static final String KEY_SPLITPANE_B = "KEY_SPLITPANE_B";

    public void setPersistentData(Map data) {
        if(data == null)
            return;

        Point loc = (Point) data.get(KEY_WINDOW_LOC);
        if(loc != null)
            setLocation(loc);

        Dimension size = (Dimension) data.get(KEY_WINDOW_SIZE);
        if(size != null)
            setSize(size);

        Integer i = (Integer) data.get(KEY_SPLITPANE_A);
        if(i != null)
            splitPaneA.setDividerLocation(i.intValue());

        i = (Integer) data.get(KEY_SPLITPANE_B);
        if(i != null)
            splitPaneB.setDividerLocation(i.intValue());
    }

    public Map getPersistentData() {
        Map<String, Serializable> data = new HashMap<String, Serializable>();
        data.put(KEY_WINDOW_LOC, getLocation());
        data.put(KEY_WINDOW_SIZE, getSize());
        data.put(KEY_SPLITPANE_A, splitPaneA.getDividerLocation());
        data.put(KEY_SPLITPANE_B, splitPaneB.getDividerLocation());
        return data;
    }

    /** This method is called *very* frequently so it has to be really efficient
     *
     */

    public void fileDidBecomeDirty(CContainerProjectFile file, ProjectFileItem item) {
        getBuildList().setFileDirty(item, true);
        changeDone();
    }

    public void documentDidLoad() {
        buildList.setPersistentData((Map<String,Map>) getData().getBuildListData());
        explorer.setPersistentData((Map) getData().getExplorerData());
        editorZone.setPersistentData((Map) getData().getEditorZoneData());
        setPersistentData((Map) getData().getContainerData());
    }

    public void documentWillSave() {
        getData().setBuildListData(buildList.getPersistentData());
        getData().setExplorerData(explorer.getPersistentData());
        getData().setEditorZoneData(editorZone.getPersistentData());
        getData().setContainerData(getPersistentData());
        explorer.saveAll();
    }

    public static final int MI_CLEAN = 500;
    public static final int MI_BUILD_FILE = 501;
    public static final int MI_BUILD_ALL = 502;
    public static final int MI_RUN = 503;
    public static final int MI_RUN_SETTINGS = 504;
    public static final int MI_SETTINGS = 505;
    public static final int MI_CLOSE_EDITOR = 506;
    public static final int MI_MOVE_EDITOR_LEFT = 507;
    public static final int MI_MOVE_EDITOR_RIGHT = 508;

    public void createProjectMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("Project");
        menu.addItem(new XJMenuItem("Run", MI_RUN, this));
        menu.addItem(new XJMenuItem("Edit Run Settings...", MI_RUN_SETTINGS, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Build All", MI_BUILD_ALL, this));
        menu.addItem(new XJMenuItem("Build File", MI_BUILD_FILE, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Clean", MI_CLEAN, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Move Active Editor Left", KeyEvent.VK_LEFT, XJMenuItem.getKeyModifier() | Event.CTRL_MASK, MI_MOVE_EDITOR_LEFT, this));
        menu.addItem(new XJMenuItem("Move Active Editor Right", KeyEvent.VK_RIGHT, XJMenuItem.getKeyModifier() | Event.CTRL_MASK, MI_MOVE_EDITOR_RIGHT, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Settings...", KeyEvent.VK_SEMICOLON, MI_SETTINGS, this));

        menubar.addCustomMenu(menu);
    }

    public void customizeFileMenu(XJMenu menu) {
        menu.insertItemAfter(new XJMenuItem("Close Active Editor", KeyEvent.VK_F4, MI_CLOSE_EDITOR, this), XJMainMenuBar.MI_CLOSE);
    }

    public void customizeMenuBar(XJMainMenuBar menubar) {
        createProjectMenu(menubar);
    }

    public void menuItemState(XJMenuItem item) {
        super.menuItemState(item);
        switch(item.getTag()) {
            case MI_CLOSE_EDITOR:
            case MI_MOVE_EDITOR_LEFT:
            case MI_MOVE_EDITOR_RIGHT:
                item.setEnabled(editorZone.getSelectedFileItem() != null);
                break;
        }
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        super.handleMenuEvent(menu, item);

        switch(item.getTag()) {
            case MI_CLEAN:
                clean();
                break;

            case MI_BUILD_FILE:
                buildFile();
                break;

            case MI_BUILD_ALL:
                buildAll();
                break;

            case MI_RUN:
                run();
                break;

            case MI_RUN_SETTINGS:
                runSettings(false);
                break;

            case MI_SETTINGS:
                projectSettings();
                break;

            case MI_CLOSE_EDITOR:
                editorZone.closeActiveEditor();
                break;

            case MI_MOVE_EDITOR_LEFT:
                editorZone.moveActiveEditor(ProjectEditorZone.DIRECTION_LEFT);
                break;

            case MI_MOVE_EDITOR_RIGHT:
                editorZone.moveActiveEditor(ProjectEditorZone.DIRECTION_RIGHT);
                break;
        }
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

    public ComponentEditor getEditor() {
        // There is no specific editor for the project
        return null;
    }

}
