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

import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.components.ComponentToolbar;
import org.antlr.works.components.ComponentWindow;
import org.antlr.works.components.ComponentWindowImpl;
import org.antlr.works.components.document.ComponentDocument;
import org.antlr.works.components.document.ComponentDocumentFactory;
import org.antlr.works.components.editor.ComponentEditor;
import org.antlr.works.components.editor.ComponentEditorGrammar;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.debugger.api.DebuggerDelegate;
import org.antlr.works.editor.EditorConsole;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.generate.CodeGenerate;
import org.antlr.works.grammar.element.ElementBlock;
import org.antlr.works.grammar.element.ElementImport;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.works.menu.ActionDebugger;
import org.antlr.works.menu.ActionGoTo;
import org.antlr.works.menu.ActionRefactor;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.utils.Console;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.appkit.frame.XJFrameInterface;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuItem;
import org.antlr.xjlib.appkit.swing.XJTabbedPane;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.foundation.XJUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ComponentContainerGrammar implements ComponentContainer {

    private final Set<ComponentContainer> containers = new LinkedHashSet<ComponentContainer>();
    private final Map<Component, ComponentContainer> componentToContainer = new HashMap<Component, ComponentContainer>();
    private final Map<Integer, EditorTab> indexToEditorTab = new HashMap<Integer, EditorTab>();

    private ComponentEditor editor;
    private ComponentContainer selectedContainer;
    private ComponentContainerGrammarMenu componentContainerGrammarMenu;

    private ComponentToolbar toolbar;
    private Debugger debugger;

    private XJTabbedPane editorsTab;
    private JTabbedPane bottomTab;

    private JPanel toolbarPanel;
    private JPanel rulesPanel;
    private JPanel bottomPanel;

    private JPanel sdPanel;
    private JPanel interpreterPanel;
    private JPanel consolePanel;
    private JPanel debuggerPanel;

    private JPanel mainPanel;

    private EditorsTabChangeListener etc;
    private MouseListener ml;
    private ChangeListener cl;
    
    private final List<EditorTab> tabs = new ArrayList<EditorTab>();

    private final Set<String> loadedGrammarFileNames = new HashSet<String>();
    private final Map<String, GrammarEngine> engines = new HashMap<String, GrammarEngine>();

    private final ComponentWindow window;

    public ComponentContainerGrammar(ComponentWindow window) {
        this.window = window;

        selectedContainer = this;
        containers.add(this);

        debugger = new Debugger(new ContainerDebuggerDelegate());
        componentContainerGrammarMenu = new ComponentContainerGrammarMenu(this);
        toolbar = new ComponentToolbar(this);
    }

    public void awake() {
        componentContainerGrammarMenu.awake();

        debugger.awake();
        toolbar.awake();

        editorsTab = new XJTabbedPane();

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

        debuggerPanel = new JPanel(new BorderLayout());
        debuggerPanel.setBorder(null);
        bottomTab.addTab("Debugger", debuggerPanel);

        editorsTab.addChangeListener(etc = new EditorsTabChangeListener());
        bottomTab.addMouseListener(ml = new TabbedPaneMouseListener());
        bottomTab.addChangeListener(cl = new TabbedPaneChangeListener());
    }

    public void assemble(boolean separateRules) {
        JSplitPane horizontalSplit;
        if(separateRules) {
            horizontalSplit = new JSplitPane();
            horizontalSplit.setBorder(null);
            horizontalSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
            horizontalSplit.setTopComponent(editorsTab);
            horizontalSplit.setBottomComponent(bottomPanel);
            horizontalSplit.setContinuousLayout(true);
            horizontalSplit.setOneTouchExpandable(true);
            horizontalSplit.setResizeWeight(0.6);
        } else {
            JSplitPane verticalSplit = new JSplitPane();
            verticalSplit.setBorder(null);
            verticalSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            verticalSplit.setLeftComponent(rulesPanel);
            verticalSplit.setRightComponent(editorsTab);
            verticalSplit.setContinuousLayout(true);
            verticalSplit.setOneTouchExpandable(true);
            verticalSplit.setResizeWeight(0.2);

            horizontalSplit = new JSplitPane();
            horizontalSplit.setBorder(null);
            horizontalSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
            horizontalSplit.setTopComponent(verticalSplit);
            horizontalSplit.setBottomComponent(bottomPanel);
            horizontalSplit.setContinuousLayout(true);
            horizontalSplit.setOneTouchExpandable(true);
            horizontalSplit.setResizeWeight(0.7);
        }
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(null);
        mainPanel.add(toolbarPanel, BorderLayout.NORTH);
        mainPanel.add(horizontalSplit, BorderLayout.CENTER);

        window.setContentPanel(mainPanel);
    }

    public void refreshMainMenuBar() {
        if(getXJFrame().getMainMenuBar() != null) {
            getXJFrame().getMainMenuBar().refreshState();
        }
    }

    public void addGrammar(ComponentContainer container) {
        Component c = container.getEditor().getPanel();
        componentToContainer.put(c, container);
        containers.add(container);
    }

    public boolean loadGrammar(String name) {
        String fileName = name+".g";

        String file = XJUtils.concatPath(getDocument().getDocumentFolder(), fileName);
        if(!new File(file).exists()) {
            return false;
        }

        if(loadedGrammarFileNames.contains(fileName)) return true;
        loadedGrammarFileNames.add(fileName);
        
        ComponentDocumentFactory factory = new ComponentDocumentFactory(ComponentWindowImpl.class);
        ComponentDocumentInternal doc = factory.createInternalDocument(this);
        ComponentContainerInternal container = (ComponentContainerInternal) doc.getContainer();

        doc.awake();
        try {
            doc.load(file);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        window.addDocument(doc);
        addGrammar(container);

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

    public ComponentEditorGrammar selectGrammar(String name) {
        ComponentContainer c = getContainerForName(name);
        if(c != null) {
            return selectGrammar(c);
        }
        return null;
    }

    private ComponentEditorGrammar selectGrammar(ComponentContainer c) {
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

    public ComponentToolbar getToolbar() {
        return toolbar;
    }

    public ComponentEditorGrammar getSelectedEditor() {
        return (ComponentEditorGrammar) getSelectedContainer().getEditor();
    }

    public ComponentContainer getSelectedContainer() {
        return selectedContainer;
    }

    public Debugger getDebugger() {
        return debugger;
    }

    public ActionDebugger getActionDebugger() {
        return componentContainerGrammarMenu.getActionDebugger();
    }

    public ActionRefactor getActionRefactor() {
        return componentContainerGrammarMenu.getActionRefactor();
    }

    public ActionGoTo getActionGoTo() {
        return componentContainerGrammarMenu.getActionGoTo();
    }

    public void saveAll() {
        for(ComponentContainer container : containers) {
            container.getDocument().save(false);
        }
    }

    public ComponentContainerGrammarMenu getComponentContainerGrammarMenu() {
        return componentContainerGrammarMenu;
    }

    public boolean close() {
        for(ComponentContainer container : containers) {
            if(container == this) continue;
            container.close();
        }

        componentContainerGrammarMenu.close();

        editor.close();
        debugger.close();
        toolbar.close();

        editorsTab.removeChangeListener(etc);
        bottomTab.removeMouseListener(ml);
        bottomTab.removeChangeListener(cl);

        ml = null;
        cl = null;

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
        addGrammar(this);
        selectGrammar(this);

        getSelectedEditor().becomingVisibleForTheFirstTime();

        debugger.componentShouldLayout(getSize());

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
        String file = XJUtils.concatPath(path, name+".g");
        String content = "grammar "+name+";\n";
        try {
            XJUtils.writeStringToFile(content, file);
        } catch (IOException e) {
            XJAlert.display(window.getJavaContainer(), "Create File Error",
                    "Cannot create file '"+name+"' because:\n"+e.toString());
            return;
        }
        reloadEditor(getSelectedEditor());
        selectGrammar(name);
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
        componentContainerGrammarMenu.customizeFileMenu(menu);
    }

    public void customizeMenuBar(XJMainMenuBar menubar) {
        componentContainerGrammarMenu.customizeMenuBar(menubar);
    }

    public void menuItemState(XJMenuItem item) {
        componentContainerGrammarMenu.menuItemState(item);
    }

    public void handleMenuSelected(XJMenu menu) {
        componentContainerGrammarMenu.handleMenuSelected(menu);
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

    public ContextualMenuFactory createContextualMenuFactory() {
        return new ContextualMenuFactory(componentContainerGrammarMenu);
    }

    public JPopupMenu getContextualMenu(int textIndex) {
        return componentContainerGrammarMenu.getContextualMenu(textIndex);
    }

    private static final int CLOSING_INDEX_LIMIT = 4;

    public void addTab(EditorTab tab) {
        /** Replace any existing tab with this one if the title matches. Don't
         * replace the first three tabs because they are always visible.
         */
        int index = getSimilarTab(tab);
        if(index == -1) {
            tabs.add(tab);
            bottomTab.add(tab.getTabName(), tab.getTabComponent());
            index = bottomTab.getTabCount()-1;
        } else {
            tabs.remove(index);
            tabs.add(index, tab);
            bottomTab.removeTabAt(index+CLOSING_INDEX_LIMIT);
            bottomTab.insertTab(tab.getTabName(), null, tab.getTabComponent(), null, index+CLOSING_INDEX_LIMIT);
        }

        indexToEditorTab.put(index, tab);
        selectTab(tab.getTabComponent());
    }

    private void closeTab(int index) {
        if(index < CLOSING_INDEX_LIMIT)
            return;

        tabs.remove(index-CLOSING_INDEX_LIMIT);
        bottomTab.removeTabAt(index);
    }

    public void selectConsoleTab(ComponentEditor editor) {
        switchToEditor((ComponentEditorGrammar) editor);
        selectTab(consolePanel);
    }

    public void selectInterpreterTab(ComponentEditor editor) {
        switchToEditor((ComponentEditorGrammar) editor);
        selectTab(interpreterPanel);
    }

    public void selectSyntaxDiagramTab(ComponentEditor editor) {
        switchToEditor((ComponentEditorGrammar) editor);
        selectTab(sdPanel);
    }

    public void documentLoaded(ComponentDocument document) {

    }

    public void editorParsed(ComponentEditor editor) {
        reloadEditor(editor);
    }

    private void reloadEditor(ComponentEditor editor) {
        ComponentEditorGrammar eg = (ComponentEditorGrammar) editor;
        GrammarEngine engine = eg.getGrammarEngine();

        engines.put(editor.getDocument().getDocumentNameWithoutExtension(), engine);

        // indicate to the engine that the parser completed
        engine.parserCompleted();

        // make sure all the imported grammars are loaded
        for(ElementImport element : engine.getImports()) {
            loadGrammar(element.getName());
        }

        // update the hierarchy starting with the root grammar
        updateHierarchy();

        // update the engine
        engine.updateAll();

        // damage the text editor to update all underlyings
        eg.getTextEditor().damage();
    }

    private void updateHierarchy() {
        // always start with the root grammar
        GrammarEngine engine = engines.get(getDocument().getDocumentNameWithoutExtension());
        Set<GrammarEngine> alreadyVisitedEngines = new HashSet<GrammarEngine>();
        engine.updateHierarchy(engines, alreadyVisitedEngines);
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
        int index = bottomTab.getSelectedIndex();
        switch(index) {
            case 0:
                return getSelectedEditor().getComponentSD();
            case 1:
                return getSelectedEditor().getComponentInterpreter();
            case 2:
                return getSelectedEditor().getComponentConsole();
            case 3:
                return debugger;
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

    private void switchToEditor(final ComponentEditorGrammar editor) {
        setComponent(toolbarPanel, toolbar.getToolbar());
        setComponent(rulesPanel, editor.getComponentRules());

        setComponent(sdPanel, editor.getComponentSD());
        setComponent(interpreterPanel, editor.getComponentInterpreter());
        setComponent(consolePanel, editor.getComponentConsole());
        setComponent(debuggerPanel, debugger);

        bottomPanel.removeAll();
        bottomPanel.add(bottomTab, BorderLayout.CENTER);
        bottomPanel.add(editor.getStatusComponent(), BorderLayout.SOUTH);
        bottomPanel.revalidate();
        bottomPanel.repaint();

        toolbar.updateStates();

        editor.refreshMainMenuBar();

        window.setTitle(selectedContainer.getDocument().getDocumentPath());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                editor.getTextEditor().getTextPane().requestFocus();
            }
        });
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

    public class ContainerDebuggerDelegate implements DebuggerDelegate {

        public GrammarEngine getGrammarEngine() {
            return getSelectedEditor().getGrammarEngine();
        }

        public String getGrammarName() {
            return XJUtils.getPathByDeletingPathExtension(getSelectedEditor().getGrammarFileName());
        }

        public void debuggerStarted() {
            selectTab(debuggerPanel);

            ((EditorConsole)getConsole()).makeCurrent();

            for(ComponentContainer c : containers) {
                c.getEditor().setEditable(false);
            }
            refreshMainMenuBar();
        }

        public void debuggerStopped() {
            getSelectedEditor().setDebuggerLocation(-1);
            for(ComponentContainer c : containers) {
                c.getEditor().setEditable(true);
            }
            refreshMainMenuBar();
        }

        public void debuggerSetLocation(String grammar, int line, int column) {
            selectGrammar(grammar);
            int grammarIndex = computeAbsoluteGrammarIndex(line, column);
            if(grammarIndex >= 0) {
                getSelectedEditor().setDebuggerLocation(grammarIndex);
            }
        }

        public void debuggerSelectText(String grammar, int line, int column) {
            selectGrammar(grammar);
            int grammarIndex = computeAbsoluteGrammarIndex(line, column);
            if(grammarIndex >= 0) {
                getSelectedEditor().selectTextRange(grammarIndex, grammarIndex+1);
            }
        }

        public XJDocument getDocument() {
            return getSelectedEditor().getDocument();
        }

        public List<ElementRule> getRules() {
            return getSelectedEditor().getRules();
        }

        public List<ElementRule> getSortedRules() {
            return getSelectedEditor().getSortedRules();
        }

        public boolean ensureDocumentSaved() {
            return getSelectedEditor().ensureDocumentSaved();
        }

        public CodeGenerate getCodeGenerate() {
            return new CodeGenerate(getSelectedEditor(), null);
        }

        public String getTokenVocab() {
            return getSelectedEditor().getGrammarEngine().getTokenVocab();
        }

        public Container getContainer() {
            return getSelectedEditor().getJavaContainer();
        }

        public Console getConsole() {
            return getSelectedEditor().getConsole();
        }

        public List<ElementBlock> getBlocks() {
            return getSelectedEditor().getGrammarEngine().getBlocks();
        }

        public Map<Integer, Set<String>> getBreakpoints() {
            Map<Integer,Set<String>> breakpoints = new HashMap<Integer, Set<String>>();
            for(ComponentContainer c : containers) {
                ComponentEditorGrammar g = (ComponentEditorGrammar) c.getEditor();
                for(Integer line : g.getBreakpoints()) {
                    Set<String> names = breakpoints.get(line);
                    if(names == null) {
                        names = new HashSet<String>();
                        breakpoints.put(line, names);
                    }
                    names.add(XJUtils.getPathByDeletingPathExtension(g.getGrammarFileName()));
                }
            }
            return breakpoints;
        }

        public ContextualMenuFactory createContextualMenuFactory() {
            return ComponentContainerGrammar.this.createContextualMenuFactory();
        }

        public void selectConsoleTab() {
            getSelectedEditor().selectConsoleTab();
        }

        private int computeAbsoluteGrammarIndex(int lineIndex, int column) {
            List<ATELine> lines = getSelectedEditor().getLines();
            if(lineIndex-1<0 || lineIndex-1 >= lines.size())
                return -1;

            ATELine line = lines.get(lineIndex-1);
            return line.position+column-1;
        }

    }

    public class TabbedPaneMouseListener extends MouseAdapter {

        public void displayPopUp(MouseEvent event) {
            if(bottomTab.getSelectedIndex() < CLOSING_INDEX_LIMIT)
                return;

            if(!event.isPopupTrigger())
                return;

            JPopupMenu popup = new JPopupMenu();
            JMenuItem item = new JMenuItem("Close");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    closeTab(bottomTab.getSelectedIndex());
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
            Component c = editorsTab.getSelectedComponent();
            selectedContainer = componentToContainer.get(c);
            ComponentEditorGrammar editor = (ComponentEditorGrammar) selectedContainer.getEditor();

            switchToEditor(editor);
        }

    }
}
