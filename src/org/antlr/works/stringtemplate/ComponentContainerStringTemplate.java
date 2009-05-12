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

package org.antlr.works.stringtemplate;

import org.antlr.works.components.ComponentWindow;
import org.antlr.works.components.container.ComponentContainer;
import org.antlr.works.components.container.ComponentContainerInternal;
import org.antlr.works.components.document.ComponentDocument;
import org.antlr.works.components.editor.ComponentEditor;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.menu.ActionGoTo;
import org.antlr.works.menu.ActionRefactor;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.menu.ActionDebugger;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.stringtemplate.menu.ContextualStringTemplateMenuFactory;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.appkit.frame.XJFrameInterface;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuItem;
import org.antlr.xjlib.appkit.swing.XJTabbedPane;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.foundation.XJUtils;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ComponentContainerStringTemplate implements ComponentContainer {

    private final Set<ComponentContainer> containers = new LinkedHashSet<ComponentContainer>();
    private final Map<Component, ComponentContainer> componentToContainer = new HashMap<Component, ComponentContainer>();

    private ComponentEditor editor;
    private ComponentContainer selectedContainer;
    private ComponentContainerStringTemplateMenu componentContainerStringTemplateMenu;

    private ComponentToolbarStringTemplate toolbar;

    private XJTabbedPane editorsTab;

    private JPanel toolbarPanel;
    private JPanel rulesPanel;

    private JPanel mainPanel;

    private EditorsTabChangeListener etc;

    private final List<EditorTab> tabs = new ArrayList<EditorTab>();

    private final Set<String> loadedStringTemplateFileNames = new HashSet<String>();

    private final ComponentWindow window;

    public ComponentContainerStringTemplate(ComponentWindow window) {
        this.window = window;

        selectedContainer = this;
        containers.add(this);

        componentContainerStringTemplateMenu = new ComponentContainerStringTemplateMenu(this);
        toolbar = new ComponentToolbarStringTemplate(this);
    }

    public void awake() {
        editorsTab = new XJTabbedPane();

        toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBorder(null);

        rulesPanel = new JPanel(new BorderLayout());
        rulesPanel.setBorder(null);

        editorsTab.addChangeListener(etc = new EditorsTabChangeListener());
    }

    public void assemble(boolean separateRules) {
        JSplitPane verticalSplit = new JSplitPane();
        verticalSplit.setBorder(null);
        verticalSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        verticalSplit.setRightComponent(editorsTab);
        verticalSplit.setLeftComponent(rulesPanel);
        verticalSplit.setContinuousLayout(false);
        verticalSplit.setOneTouchExpandable(true);
        verticalSplit.setResizeWeight(0.25);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(null);
        mainPanel.add(toolbarPanel, BorderLayout.NORTH);
        mainPanel.add(verticalSplit, BorderLayout.CENTER);

        window.setContentPanel(mainPanel);
    }

    public void refreshMainMenuBar() {
        if(getXJFrame().getMainMenuBar() != null) {
            getXJFrame().getMainMenuBar().refreshState();
        }
    }

    public void addStringTemplate(ComponentContainer container) {
        Component c = container.getEditor().getPanel();
        componentToContainer.put(c, container);
        containers.add(container);
    }

    public boolean loadStringTemplate(String name) {
        String fileName = name+".st";

        String folder = getDocument().getDocumentFolder();
        if(folder == null) {
            return false;
        }

        String file = XJUtils.concatPath(folder, fileName);
        if(!new File(file).exists()) {
            return false;
        }

        if(loadedStringTemplateFileNames.contains(fileName)) return true;
        loadedStringTemplateFileNames.add(fileName);

        STDocumentFactory factory = new STDocumentFactory(STWindow.class);
        STDocumentInternal doc = factory.createInternalDocument(this);
        ComponentContainerInternal container = (ComponentContainerInternal) doc.getContainer();

        doc.awake();
        try {
            doc.load(file);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        window.addDocument(doc);
        addStringTemplate(container);

        return true;
    }

    public ComponentContainer getContainerForName(String name) {
        for(ComponentContainer c : containers) {
            if(XJUtils.getPathByDeletingPathExtension(c.getDocument().getDocumentName()).equals(name)) {
                return c;
            }
        }
        return null;
    }

    public ComponentEditor selectEditor(String name) {
        ComponentContainer c = getContainerForName(name);
        if(c != null) {
            return selectEditor(c);
        }
        return null;
    }

    public void selectEditor(XJDocument doc) {
        for(ComponentContainer c : containers) {
            if(c.getDocument() == doc) {
                selectEditor(c);
                break;
            }
        }
    }

    private ComponentEditor selectEditor(ComponentContainer c) {
        Component panel = c.getEditor().getPanel();
        if(!editorsTab.hasComponent(panel)) {
            editorsTab.addComponent(c.getDocument().getDocumentName(), panel);
        }
        editorsTab.selectComponent(panel);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getSelectedEditor().getTextEditor().getTextPane().requestFocus();
            }
        });
        return getSelectedEditor();
    }

    public void dirtyChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateEditorDirtyFlag();
            }
        });
    }

    public void editorContentChanged() {
        updateEditorDirtyFlag();
    }

    public Debugger getDebugger() {
        return null;
    }

    public ActionDebugger getActionDebugger() {
        return null;
    }

    private void updateEditorDirtyFlag() {
        for (ComponentContainer c : containers) {
            Component panel = c.getEditor().getPanel();
            int index = editorsTab.indexOfComponent(panel);
            if (index == -1) continue;

            String title = c.getDocument().getDocumentName();
            if(title == null) {
                title = "";
            }
            if (c.getDocument().isDirty()) {
                editorsTab.setTitleAt(index, "* " + title);
            } else {
                editorsTab.setTitleAt(index, title);
            }
        }
    }

    public ComponentToolbarStringTemplate getToolbar() {
        return toolbar;
    }

    public ComponentEditor getSelectedEditor() {
        return getSelectedContainer().getEditor();
    }

    public ComponentContainer getSelectedContainer() {
        return selectedContainer;
    }

    public ActionRefactor getActionRefactor() {
        return componentContainerStringTemplateMenu.getActionRefactor();
    }

    public ActionGoTo getActionGoTo() {
        return componentContainerStringTemplateMenu.getActionGoTo();
    }

    public void saveAll() {
        for(ComponentContainer container : containers) {
            container.getDocument().save(false);
        }
    }

    public ContextualMenuFactory createContextualMenuFactory() {
        return null;
    }

    public ContextualStringTemplateMenuFactory createContextualStringTemplateMenuFactory() {
        return new ContextualStringTemplateMenuFactory(componentContainerStringTemplateMenu);
    }

    public JPopupMenu getContextualMenu(int textIndex) {
        return componentContainerStringTemplateMenu.getContextualMenu(textIndex);
    }

    public EditorTab getSelectedTab() {
        return null;
    }

    public void selectTab(Component c) {
    }

    public ComponentContainerStringTemplateMenu getComponentContainerStringTemplateMenu() {
        return componentContainerStringTemplateMenu;
    }

    public boolean close() {
        for(ComponentContainer container : containers) {
            if(container == this) continue;
            container.close();
        }

        componentContainerStringTemplateMenu.close();

        editor.close();
        toolbar.close();

       return true;
    }

    public void setDocument(ComponentDocument document) {
        window.setDocument(document);
    }

    public ComponentDocument getDocument() {
        return (ComponentDocument) window.getDocument();
    }

    public Dimension getSize() {
        return window.getSize();
    }

    public void becomingVisibleForTheFirstTime() {
        addStringTemplate(this);
        selectEditor(this);

        getSelectedEditor().becomingVisibleForTheFirstTime();

        for(ComponentContainer container : containers) {
            if(container == this) continue;
            container.becomingVisibleForTheFirstTime();
        }
    }

    public void setDirty() {
        window.setDirty();
    }

    public void createFile(String name) {
        String path = getEditor().getDocument().getDocumentFolder();
        String file = XJUtils.concatPath(path, name+".stg");
        String content = "group "+name+";\n";
        try {
            XJUtils.writeStringToFile(content, file);
        } catch (IOException e) {
            XJAlert.display(window.getJavaContainer(), "Create File Error",
                    "Cannot create file '"+name+"' because:\n"+e.toString());
            return;
        }
        selectEditor(name);
    }

    public void setEditor(ComponentEditor editor) {
        this.editor = editor;
    }

    public ComponentEditor getEditor() {
        return editor;
    }

    public XJFrameInterface getXJFrame() {
        return window;
    }

    public XJMainMenuBar getMainMenuBar() {
        return window.getMainMenuBar();
    }

    public void customizeFileMenu(XJMenu menu) {
        componentContainerStringTemplateMenu.customizeFileMenu(menu);
    }

    public void customizeMenuBar(XJMainMenuBar menubar) {
        componentContainerStringTemplateMenu.customizeMenuBar(menubar);
    }

    public void menuItemState(XJMenuItem item) {
        componentContainerStringTemplateMenu.menuItemState(item);
    }

    public void handleMenuSelected(XJMenu menu) {
    }

    public JComponent getRulesComponent() {
        return rulesPanel;
    }

    public JComponent getEditorComponent() {
        return mainPanel;
    }

    public void windowActivated() {
        for(ComponentContainer container : containers) {
            container.getEditor().componentActivated();
        }
        for(EditorTab et : tabs) {
            et.editorActivated();
        }
    }

    public void documentLoaded(ComponentDocument document) {
    }

    public void editorParsed(ComponentEditor editor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void selectConsoleTab(ComponentEditor editor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void selectInterpreterTab(ComponentEditor editor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void selectSyntaxDiagramTab(ComponentEditor editor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getSimilarTab(EditorTab tab) {
        for (int i = 0; i < tabs.size(); i++) {
            EditorTab t = tabs.get(i);
            if(t.getTabName().equals(tab.getTabName()))
                return i;
        }
        return -1;
    }

    public void addTab(EditorTab tab) {

    }

    public void setComponent(JPanel panel, EditorTab tab) {
        setComponent(panel, tab.getTabComponent());
    }

    private void switchToEditor(final ComponentEditorStringTemplate editor) {
        setComponent(toolbarPanel, toolbar.getToolbar());
        setComponent(rulesPanel, editor.getComponentRules());

        toolbar.updateStates();

        editor.refreshMainMenuBar();

        window.setTitle(selectedContainer.getDocument().getDocumentPath());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                editor.getTextEditor().getTextPane().requestFocus();
            }
        });
    }

    public void setComponent(JPanel panel, Component c) {
        panel.removeAll();
        panel.add(c);
        panel.revalidate();
        panel.repaint();
    }

    private class EditorsTabChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent event) {
            Component c = editorsTab.getSelectedComponent();
            selectedContainer = componentToContainer.get(c);
            ComponentEditorStringTemplate editor = (ComponentEditorStringTemplate) selectedContainer.getEditor();

            switchToEditor(editor);
        }

    }
}
