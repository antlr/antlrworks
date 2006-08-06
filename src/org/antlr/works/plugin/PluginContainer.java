package org.antlr.works.plugin;

import edu.usfca.xj.appkit.app.XJApplication;
import edu.usfca.xj.appkit.app.XJApplicationDelegate;
import edu.usfca.xj.appkit.app.XJApplicationInterface;
import edu.usfca.xj.appkit.app.XJPreferences;
import edu.usfca.xj.appkit.document.XJDataPlainText;
import edu.usfca.xj.appkit.document.XJDocument;
import edu.usfca.xj.appkit.frame.XJFrame;
import edu.usfca.xj.appkit.frame.XJFrameInterface;
import edu.usfca.xj.appkit.frame.XJWindow;
import edu.usfca.xj.appkit.menu.XJMainMenuBar;
import edu.usfca.xj.appkit.menu.XJMenu;
import edu.usfca.xj.appkit.menu.XJMenuBarCustomizer;
import edu.usfca.xj.appkit.menu.XJMenuItem;
import edu.usfca.xj.appkit.undo.XJUndo;
import edu.usfca.xj.appkit.undo.XJUndoDelegate;
import org.antlr.works.components.ComponentContainer;
import org.antlr.works.components.ComponentEditor;
import org.antlr.works.components.grammar.CDocumentGrammar;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.components.grammar.CEditorGrammarDefaultDelegate;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
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

public class PluginContainer extends XJApplicationDelegate
        implements ComponentContainer,
        XJApplicationInterface,
        XJMenuBarCustomizer,
        XJFrameInterface
{
    protected JRootPane rootPane;
    protected CEditorGrammar editor;

    protected XJPreferences prefs;
    protected CDocumentGrammar document;
    protected XJMainMenuBar mainMenuBar;

    public PluginContainer() {
        XJApplication.setShared(this);
        XJApplication.setDelegate(this);

        /** Make sure the frame do not create menu bar when
         * they are created (which is the default behavior)
         */
        XJFrame._defaultShouldDisplayMenuBar = false;

        rootPane = new JRootPane();

        prefs = new XJPreferences(getClass());
        document = new CDocumentGrammar();
        document.setComponentContainer(this);
        document.setDocumentData(new XJDataPlainText());

        editor = new CEditorGrammar(this);
        editor.create();

        mainMenuBar = new XJMainMenuBar();
        mainMenuBar.setCustomizer(this);
        mainMenuBar.createMenuBar(XJMainMenuBar.IGNORE_FILEMENU |
        XJMainMenuBar.IGNORE_WINDOWMENU | XJMainMenuBar.IGNORE_HELPMENU);
    }

    public void setEditorGrammarDelegate(CEditorGrammarDefaultDelegate delegate) {
        editor.setDelegate(delegate);
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
        return editor.getTabbedComponent();
    }

    public JComponent getMenubarComponent() {
        return mainMenuBar.getJMenuBar();
    }

    public JComponent getToolbarComponent() {
        return editor.getToolbarComponent();
    }

    public JComponent getStatusComponent() {
        return editor.getStatusComponent();
    }

    public void load(String file) {
        document.performLoad(file);
    }

    public void loadText(String text) {
        editor.loadText(text);
    }

    public String getText() {
        return editor.getText();
    }

    public boolean willSaveDocument() {
        return editor.componentDocumentWillSave();
    }

    public void close() {
        editor.close();
    }

    public void setPersistentData(Map data) {
    }

    public Map getPersistentData() {
        return null;
    }

    public void becomingVisibleForTheFirstTime() {
        editor.componentDidAwake();
        editor.componentShouldLayout(rootPane.getSize());
    }

    public ComponentEditor getEditor() {
        return editor;
    }

    // **** XJFrameInterface ****

    public void registerUndo(XJUndoDelegate delegate, JTextPane textPane) {
    }

    public XJUndo getUndo(JTextPane textPane) {
        return null;
    }

    public XJUndo getCurrentUndo() {
        return null;
    }

    public XJMainMenuBar getMainMenuBar() {
        return mainMenuBar;
    }

    public Container getJavaContainer() {
        return rootPane.getContentPane();
    }

    public JLayeredPane getLayeredPane() {
        return rootPane.getLayeredPane();
    }

    public Container getContentPane() {
        return rootPane.getContentPane();
    }

    // *******************************

    public void setDirty() {
    }

    public XJDocument getDocument() {
        return document;
    }

    public XJFrameInterface getXJFrame() {
        return this;
    }

    public void customizeFileMenu(XJMenu menu) {
        editor.customizeFileMenu(menu);
    }

    public void customizeEditMenu(XJMenu menu) {
        editor.customizeEditMenu(menu);
    }

    public void customizeWindowMenu(XJMenu menu) {
        editor.customizeWindowMenu(menu);
    }

    public void customizeHelpMenu(XJMenu menu) {
        editor.customizeHelpMenu(menu);
    }

    public void customizeMenuBar(XJMainMenuBar menubar) {
        editor.customizeMenuBar(menubar);
    }

    public void menuItemState(XJMenuItem item) {
        editor.menuItemState(item);
    }

    public void handleMenuSelected(XJMenu menu) {
        editor.handleMenuSelected(menu);
    }

    public void windowActivated() {
        editor.componentActivated();
    }

    public void windowDocumentPathDidChange() {
        editor.componentDocumentContentChanged();
    }

    // *** XJApplicationInterface *********

    public List emptyList = new ArrayList();

    public XJPreferences getPreferences() {
        return prefs;
    }

    public List getDocumentExtensions() {
        return emptyList;
    }

    public XJDocument getActiveDocument() {
        return document;
    }

    public XJDocument newDocument() {
        return null;
    }

    public boolean openDocument() {
        return false;
    }

    public List getWindows() {
        return emptyList;
    }

    public boolean supportsPersistence() {
        return false;
    }

    public boolean hasPreferencesMenuItem() {
        return false;
    }

    public List recentFiles() {
        return emptyList;
    }

    public void clearRecentFiles() {
    }

    public void addRecentFile(String path) {
    }

    public void removeRecentFile(String file) {
    }

    public void performQuit() {
    }

    public void displayPrefs() {
    }

    public void displayAbout() {
    }

    public void displayHelp() {
    }

    public boolean openDocument(String file) {
        return false;
    }

    public void addWindow(XJWindow window) {
    }

    public void removeWindow(XJWindow window) {
    }

    public void addDocument(XJDocument document) {
    }

    public XJDocument getDocumentForPath(String path) {
        return null;
    }

    public void removeDocument(XJDocument document) {
    }

    public List getDocuments() {
        List l = new ArrayList();
        l.add(document);
        return l;
    }

    public boolean openLastUsedDocument() {
        return false;
    }

    public XJWindow getActiveWindow() {
        return null;
    }

}
