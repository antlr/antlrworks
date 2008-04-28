package org.antlr.works.plugin.container;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import org.antlr.works.components.container.ComponentContainer;
import org.antlr.works.components.document.ComponentDocument;
import org.antlr.works.components.document.ComponentDocumentFactory;
import org.antlr.works.components.document.ComponentDocumentGrammar;
import org.antlr.works.components.editor.ComponentEditor;
import org.antlr.works.components.editor.ComponentEditorGrammar;
import org.antlr.works.components.editor.ComponentEditorGrammarDefaultDelegate;
import org.antlr.works.components.toolbar.ComponentToolbar;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.dialog.DialogAbout;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.menu.ActionDebugger;
import org.antlr.works.menu.ActionRefactor;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.app.XJPreferences;
import org.antlr.xjlib.appkit.frame.XJDialog;
import org.antlr.xjlib.appkit.frame.XJFrameInterface;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenuItemDelegate;

import javax.swing.*;
import java.awt.*;
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

public class PluginContainer implements ComponentContainer {
    
    private JRootPane rootPane;
    private ComponentEditorGrammar editor;

    private XJPreferences prefs;
    private ComponentDocumentGrammar document;
    private ComponentToolbar toolbar;
    private XJMainMenuBar mainMenuBar;

    private PCXJFrameInterface frameInterface;
    private PCMenuHelpDelegate menuHelpDelegate;
    private PCXJApplicationInterface appInterface = new PCXJApplicationInterface(this);
    private PluginContainerDelegate delegate;

    public PluginContainer() {
        XJApplication.setShared(appInterface);
        
        frameInterface = new PCXJFrameInterface(this);
        menuHelpDelegate = new PCMenuHelpDelegate(this);

        rootPane = new JRootPane();

        prefs = new XJPreferences(getClass());

        document = new ComponentDocumentFactory().createInternalDocument(this);

        editor = (ComponentEditorGrammar) document.getEditor();
        editor.awake();

        toolbar = new ComponentToolbar(this);

        mainMenuBar = new XJMainMenuBar();
        mainMenuBar.setDelegate(new PCMenuBarDelegate(this));
        mainMenuBar.setCustomizer(new PCMenuCustomizer(this));
        mainMenuBar.createMenuBar(XJMainMenuBar.IGNORE_FILEMENU 
                | XJMainMenuBar.IGNORE_WINDOWMENU);


        // Must register custom action in order to override the default mechanism in IntelliJ 7
        // todo add back (don't use with plugin tester)
/*        if(PIUtils.isRunningWithIntelliJ7OrAbove()) {
            registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
                    DefaultEditorKit.beginLineAction);
            registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, Event.SHIFT_MASK),
                    DefaultEditorKit.selectionBeginLineAction);

            registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
                    DefaultEditorKit.endLineAction);
            registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_END, Event.SHIFT_MASK),
                    DefaultEditorKit.selectionEndLineAction);
        }   */
    }

    public void registerKeyBinding(KeyStroke ks, final String action) {
        AnAction a = new AnAction() {
            public void actionPerformed(AnActionEvent event) {
                editor.getTextPane().getActionMap().get(action).actionPerformed(null);
            }
        };

        final String uniqueAction = action+this;
        ActionManager.getInstance().registerAction(uniqueAction, a);
        a.registerCustomShortcutSet(new CustomShortcutSet(ks),
                editor.getTextPane());
    }

    public void setDelegate(PluginContainerDelegate delegate) {
        this.delegate = delegate;
    }

    public void setEditorGrammarDelegate(ComponentEditorGrammarDefaultDelegate delegate) {
        editor.setDelegate(delegate);
    }

    public XJMenuItemDelegate getMenuHelpDelegate() {
        return menuHelpDelegate;
    }

    public XJPreferences getPreferences() {
        return prefs;
    }

    public Container getParent() {
        return XJDialog.resolveOwner(getRootPane());
    }

    public JRootPane getRootPane() {
        return rootPane;
    }

    public JComponent getEditorComponent() {
        return editor.getTextEditor();
    }

    public JComponent getRulesComponent() {
        return editor.getRulesComponent();
    }

    public JComponent getTabbedComponent() {
        return null;
        //return editor.getTabbedComponent();
    }

    public JComponent getMenubarComponent() {
        return mainMenuBar.getJMenuBar();
    }

    public JComponent getToolbarComponent() {
        return toolbar.getToolbar();
    }

    public JComponent getStatusComponent() {
        return editor.getStatusComponent();
    }

    public void load(String file) {
        try {
            document.load(file);
        } catch (Exception e) {
            // todo alert?
            e.printStackTrace();
        }
    }

    public Container getContentPane() {
        return rootPane.getContentPane();
    }

    public JLayeredPane getLayeredPane() {
        return rootPane.getLayeredPane();
    }

    public void becomingVisibleForTheFirstTime() {
        editor.componentDidAwake();
        editor.componentShouldLayout(rootPane.getSize());
    }

    public static void showAbout() {
        new DialogAbout().show();
    }

    public void activate() {
        XJApplication.setShared(appInterface);
    }

    public void deactivate() {

    }

    // ******** ComponentContainer **********

    public boolean close() {
        editor.close();
        return true;
    }

    public ComponentEditor getEditor() {
        return editor;
    }

    public void setDirty() {
        if(delegate != null)
            delegate.pluginDocumentDidChange();
    }

    public ComponentDocument getDocument() {
        return document;
    }

    public XJFrameInterface getXJFrame() {
        return frameInterface;
    }

    public XJMainMenuBar getMainMenuBar() {
        return mainMenuBar;
    }

    public void setEditor(ComponentEditor editor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDocument(ComponentDocument document) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Dimension getSize() {
        return rootPane.getSize();
    }

    public ContextualMenuFactory createContextualMenuFactory() {

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public JPopupMenu getContextualMenu(int textIndex) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public EditorTab getSelectedTab() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void selectTab(Component c) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addTab(EditorTab tab) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void documentLoaded(ComponentDocument document) {
        //To change body of implemented methods use File | Settings | File Templates.
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

    public ComponentEditorGrammar selectGrammar(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void editorContentChanged() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    // ----- ComponentToolbarDelegate

    public Debugger getDebugger() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ActionDebugger getActionDebugger() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ActionRefactor getActionRefactor() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ComponentEditorGrammar getSelectedEditor() {
        return editor;
    }

    public void debug() {

        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void debugAgain() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
