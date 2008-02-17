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

package org.antlr.works.components.container;

import org.antlr.works.components.document.ComponentDocument;
import org.antlr.works.components.document.ComponentDocumentFactory;
import org.antlr.works.components.document.ComponentDocumentGrammar;
import org.antlr.works.components.editor.ComponentEditor;
import org.antlr.works.components.editor.ComponentEditorGrammar;
import org.antlr.works.editor.EditorMenu;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.menu.*;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.appkit.frame.XJFrameInterface;
import org.antlr.xjlib.appkit.frame.XJWindow;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuItem;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentContainerGrammar extends XJWindow implements ComponentContainer {

    private List<ComponentContainer> containers = new ArrayList<ComponentContainer>();
    private Map<Component, ComponentContainer> componentToContainer = new HashMap<Component, ComponentContainer>();
    private Map<Integer, EditorTab> indexToEditorTab = new HashMap<Integer, EditorTab>();

    private ComponentEditor editor;
    private ComponentContainer selectedContainer;

    private EditorMenu editorMenu;

    private MenuFind menuFind;
    private MenuGrammar menuGrammar;
    private MenuRefactor menuRefactor;
    private MenuGoTo menuGoTo;
    private MenuGenerate menuGenerate;
    private MenuDebugger menuDebugger;
    private MenuSCM menuSCM;
    private MenuExport menuExport;

    private JTabbedPane editorsTab;
    private JTabbedPane bottomTab;

    private JPanel toolbarPanel;
    private JPanel rulesPanel;
    private JPanel bottomPanel;

    private JPanel sdPanel;
    private JPanel interpreterPanel;
    private JPanel consolePanel;

    private JSplitPane verticalSplit;
    private JSplitPane horizontalSplit;

    private List<EditorTab> tabs = new ArrayList<EditorTab>();

    private MouseListener ml;
    private ChangeListener cl;

    public ComponentContainerGrammar() {
        selectedContainer = this;
        initMenus();
    }

    @Override
    public void awake() {
        super.awake();

        // todo use selected editor
        getMenuSCM().awake();

        editorsTab = new JTabbedPane();

        bottomTab = new JTabbedPane();
        bottomTab.setTabPlacement(JTabbedPane.BOTTOM);

        toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBorder(null);

        rulesPanel = new JPanel(new BorderLayout());
        rulesPanel.setBorder(null);

        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(null);

        sdPanel = new JPanel(new BorderLayout());
        sdPanel.setBorder(null);
        bottomTab.addTab("Syntax Diagram", sdPanel);

        interpreterPanel = new JPanel(new BorderLayout());
        interpreterPanel.setBorder(null);
        bottomTab.addTab("Interpreter", interpreterPanel);

        consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBorder(null);
        bottomTab.addTab("Console", consolePanel);

        // todo remove this listener when closing
        editorsTab.addChangeListener(new EditorsTabChangeListener());
        bottomTab.addMouseListener(ml = new TabbedPaneMouseListener());
        bottomTab.addChangeListener(cl = new TabbedPaneChangeListener());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(null);
        mainPanel.add(editorsTab);

        verticalSplit = new JSplitPane();
        verticalSplit.setBorder(null);
        verticalSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        verticalSplit.setLeftComponent(rulesPanel);
        verticalSplit.setRightComponent(mainPanel);
        verticalSplit.setContinuousLayout(true);
        verticalSplit.setOneTouchExpandable(true);

        horizontalSplit = new JSplitPane();
        horizontalSplit.setBorder(null);
        horizontalSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
        horizontalSplit.setTopComponent(verticalSplit);
        horizontalSplit.setBottomComponent(bottomPanel);
        horizontalSplit.setContinuousLayout(true);
        horizontalSplit.setOneTouchExpandable(true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(null);
        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(horizontalSplit, BorderLayout.CENTER);

        getContentPane().add(panel);
        pack();
    }

    private void initMenus() {
        editorMenu = new EditorMenu(this);

        menuFind = new MenuFind(this);
        menuGrammar = new MenuGrammar(this);
        menuRefactor = new MenuRefactor(this);
        menuGoTo = new MenuGoTo(this);
        menuGenerate = new MenuGenerate(this);
        menuDebugger = new MenuDebugger(this);
        menuSCM = new MenuSCM(this);
        menuExport = new MenuExport(this);
    }

    private void closeMenus() {
        editorMenu.close();

        menuFind.close();
        menuGrammar.close();
        menuRefactor.close();
        menuGoTo.close();
        menuGenerate.close();
        menuDebugger.close();
        menuSCM.close();
        menuExport.close();
    }

    @Override
    public String autosaveName() {
        if(AWPrefs.getRestoreWindows())
            return getDocument().getDocumentPath();
        else
            return null;
    }

    @Override
    public void setDefaultSize() {
        if(XJApplication.shared().useDesktopMode()) {
            super.setDefaultSize();
            return;
        }

        Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        r.width *= 0.8;
        r.height *= 0.8;
        getRootPane().setPreferredSize(r.getSize());
    }

    public void addGrammar(ComponentContainer container) {
        Component c = container.getEditor().getPanel();
        componentToContainer.put(c, container);
        editorsTab.addTab(container.getDocument().getDocumentName(), c);
    }

    public void open(String file) {
        ComponentDocumentFactory factory = new ComponentDocumentFactory();
        ComponentDocumentInternal doc = factory.createInternalDocument(this);
        ComponentContainerInternal container = (ComponentContainerInternal) doc.getContainer();

        addDocument(doc);
        doc.awake();
        try {
            doc.load(file);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        addGrammar(container);
        containers.add(container);
    }

    public ComponentEditorGrammar getSelectedEditor() {
        return (ComponentEditorGrammar) getSelectedContainer().getEditor();
    }

    public ComponentContainer getSelectedContainer() {
        return selectedContainer;
    }

    @Override
    public boolean close() {
        if(!super.close()) {
            return false;
        }

        for(ComponentContainer container : containers) {
            if(container != this) {
                container.close();
            }
        }

        closeMenus();

        editor.close();

        bottomTab.removeMouseListener(ml);
        bottomTab.removeChangeListener(cl);

        ml = null;
        cl = null;

        return true;
    }

    public void setDocument(ComponentDocument document) {
        super.setDocument(document);
    }

    public ComponentDocument getDocument() {
        return (ComponentDocument) super.getDocument();
    }

    @Override
    public void becomingVisibleForTheFirstTime() {
        addGrammar(this);

        // todo remove that before production
        open("/Users/bovet/Grammars/split/ExprLex.g");

        // todo lazily using current editor?
        getMenuSCM().setSilent(true);
        getMenuSCM().queryFileStatus();

        if(verticalSplit != null)
            verticalSplit.setDividerLocation((int)(getSize().width*0.2));
        if(horizontalSplit != null)
            horizontalSplit.setDividerLocation((int)(getSize().height*0.5));

        // todo not really nice here
        getSelectedEditor().componentDidAwake();
        getSelectedEditor().componentShouldLayout(getSize());

        for(ComponentContainer container : containers) {
            if(container == this) continue;
            container.becomingVisibleForTheFirstTime();
        }
    }

    public void setEditor(ComponentEditor editor) {
        this.editor = editor;
    }

    public ComponentEditor getEditor() {
        return editor;
    }

    public XJFrameInterface getXJFrame() {
        return this;
    }

    @Override
    public void customizeFileMenu(XJMenu menu) {
        editorMenu.customizeFileMenu(menu);
    }

    @Override
    public void customizeMenuBar(XJMainMenuBar menubar) {
        editorMenu.customizeMenuBar(menubar);
    }

    @Override
    public void menuItemState(XJMenuItem item) {
        editorMenu.menuItemState(item);
    }

    @Override
    public void handleMenuSelected(XJMenu menu) {
        editorMenu.handleMenuSelected(menu);
    }

    @Override
    public void windowActivated() {
        super.windowActivated();

        for(ComponentContainer container : containers) {
            container.getEditor().componentActivated();
        }
    }

    @Override
    public void windowDocumentPathDidChange(XJDocument doc) {
        // Called when the document associated file has changed on the disk
        ComponentDocumentGrammar g = (ComponentDocumentGrammar) doc;
        g.getEditor().componentDocumentContentChanged();
    }

    public ContextualMenuFactory createContextualMenuFactory() {
        return new ContextualMenuFactory(editorMenu);
    }

    public void addTab(EditorTab tab) {
        /** Replace any existing tab with this one if the title matches. Don't
         * replace the first three tabs because they are always visible.
         */
        int index = getSimilarTab(tab);
        if(index > 3) {
            tabs.remove(index);
            tabs.add(index, tab);
            bottomTab.removeTabAt(index);
            bottomTab.insertTab(tab.getTabName(), null, tab.getTabComponent(), null, index);
        } else {
            tabs.add(tab);
            bottomTab.add(tab.getTabName(), tab.getTabComponent());
        }

        if(index == -1) {
            index = bottomTab.getTabCount()-1;
        }
        indexToEditorTab.put(index, tab);

        selectTab(tab.getTabComponent());
    }

    public int getSimilarTab(EditorTab tab) {
        for (int i = 0; i < tabs.size(); i++) {
            EditorTab t = tabs.get(i);
            if(t.getTabName().equals(tab.getTabName()))
                return i;
        }
        return -1;
    }

    public EditorTab getSelectedTab() {
        // todo this is invoked way too many times at startup
        int index = bottomTab.getSelectedIndex();
        switch(index) {
            case 0:
                return getSelectedEditor().getComponentSD();
            case 1:
                return getSelectedEditor().getComponentInterpreter();
            case 2:
                return getSelectedEditor().getComponentConsole();
            default:
                return indexToEditorTab.get(index);
        }
    }

    public void selectTab(Component c) {
        if(bottomTab.getSelectedComponent() != c) {
            bottomTab.setSelectedComponent(c);
            getSelectedEditor().refreshMainMenuBar();
        }
    }

    public MenuFind getMenuFind() {
        return menuFind;
    }

    public MenuGrammar getMenuGrammar() {
        return menuGrammar;
    }

    public MenuRefactor getMenuRefactor() {
        return menuRefactor;
    }

    public MenuGoTo getMenuGoTo() {
        return menuGoTo;
    }

    public MenuGenerate getMenuGenerate() {
        return menuGenerate;
    }

    public MenuDebugger getMenuDebugger() {
        return menuDebugger;
    }

    public MenuSCM getMenuSCM() {
        return menuSCM;
    }

    public MenuExport getMenuExport() {
        return menuExport;
    }

    public class TabbedPaneMouseListener extends MouseAdapter {

        protected static final int CLOSING_INDEX_LIMIT = 4;

        public void displayPopUp(MouseEvent event) {
            if(bottomTab.getSelectedIndex() < CLOSING_INDEX_LIMIT)
                return;

            if(!event.isPopupTrigger())
                return;

            JPopupMenu popup = new JPopupMenu();
            JMenuItem item = new JMenuItem("Close");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if(bottomTab.getSelectedIndex() < CLOSING_INDEX_LIMIT)
                        return;

                    tabs.remove(bottomTab.getSelectedIndex());
                    bottomTab.removeTabAt(bottomTab.getSelectedIndex());
                }
            });
            popup.add(item);
            popup.show(event.getComponent(), event.getX(), event.getY());
        }

        @Override
        public void mousePressed(MouseEvent event) {
            displayPopUp(event);
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            displayPopUp(event);
        }
    }

    public class TabbedPaneChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            getSelectedEditor().refreshMainMenuBar();
        }
    }

    private class EditorsTabChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent event) {
            Component c = ((JTabbedPane) event.getSource()).getSelectedComponent();
            selectedContainer = componentToContainer.get(c);
            ComponentEditorGrammar editor = (ComponentEditorGrammar) selectedContainer.getEditor();

            setComponent(toolbarPanel, editor.getToolbarComponent());
            setComponent(rulesPanel, editor.getComponentRules());

            setComponent(sdPanel, editor.getComponentSD());
            setComponent(interpreterPanel, editor.getComponentInterpreter());
            setComponent(consolePanel, editor.getComponentConsole());

            bottomPanel.removeAll();
            bottomPanel.add(bottomTab, BorderLayout.CENTER);
            bottomPanel.add(editor.getStatusComponent(), BorderLayout.SOUTH);
            bottomPanel.revalidate();
            bottomPanel.repaint();

            editor.refreshMainMenuBar();

            setTitle(selectedContainer.getDocument().getDocumentPath());
        }

        public void setComponent(JPanel panel, EditorTab tab) {
            setComponent(panel, tab.getTabComponent());
        }

        public void setComponent(JPanel panel, Component c) {
            panel.removeAll();
            panel.add(c);
            panel.revalidate();
            panel.repaint();
        }
    }
}
