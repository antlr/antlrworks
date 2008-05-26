package org.antlr.works.plugin.container;

import org.antlr.works.components.ComponentWindow;
import org.antlr.works.components.container.ComponentContainer;
import org.antlr.works.components.container.ComponentContainerGrammar;
import org.antlr.works.components.document.ComponentDocument;
import org.antlr.works.dialog.DialogAbout;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.app.XJPreferences;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.appkit.frame.XJDialog;
import org.antlr.xjlib.appkit.frame.XJFrameInterface;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuItem;
import org.antlr.xjlib.appkit.menu.XJMenuItemDelegate;
import org.antlr.xjlib.appkit.undo.XJUndo;
import org.antlr.xjlib.appkit.undo.XJUndoDelegate;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
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

public class PluginWindow implements ComponentWindow {
    
    private final JRootPane rootPane;
    private final ComponentContainerGrammar container;

    private final XJPreferences prefs;
    private final XJMainMenuBar mainMenuBar;
    private final List<XJDocument> documents = new ArrayList<XJDocument>();

    private final PCXJFrameInterface frameInterface;
    private final PCMenuHelpDelegate menuHelpDelegate;
    private final PCXJApplicationInterface appInterface = new PCXJApplicationInterface(this);
    private PluginContainerDelegate delegate;

    public PluginWindow() {
        XJApplication.setShared(appInterface);
        
        frameInterface = new PCXJFrameInterface(this);
        menuHelpDelegate = new PCMenuHelpDelegate(this);

        rootPane = new JRootPane();

        prefs = new XJPreferences(getClass());

        mainMenuBar = new XJMainMenuBar();
        mainMenuBar.setDelegate(new PCMenuBarDelegate(this));
        mainMenuBar.setCustomizer(new PCMenuCustomizer(this));

        container = new ComponentContainerGrammar(this);

    }

    public void awake() {
        container.awake();
        container.assemble(true);

        mainMenuBar.createMenuBar(XJMainMenuBar.IGNORE_FILEMENU
                | XJMainMenuBar.IGNORE_WINDOWMENU);
        
        rootPane.setContentPane(container.getEditorComponent());
        rootPane.setJMenuBar(mainMenuBar.getJMenuBar());
    }

    public void show() {
        // do nothing
    }

    public void bringToFront() {
        // do nothing
    }

    public boolean isMaximized() {
        return true;
    }

    public void offsetPosition(int x, int y) {
        // do nothing
    }

    public boolean isCompletelyOnScreen() {
        return true;
    }

    public boolean performClose(boolean force) {
        // todo?
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDelegate(PluginContainerDelegate delegate) {
        this.delegate = delegate;
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

    public Container getContentPane() {
        return rootPane.getContentPane();
    }

    public JLayeredPane getLayeredPane() {
        return rootPane.getLayeredPane();
    }

    public void becomingVisibleForTheFirstTime() {
        container.becomingVisibleForTheFirstTime();
    }

    public static void showAbout() {
        new DialogAbout().show();
    }

    public void activate() {
        XJApplication.setShared(appInterface);
    }

    public void deactivate() {

    }

    public boolean close() {
        return container.close();
    }

    // ComponentWindow

    public void setDirty() {
        if(delegate != null)
            delegate.pluginDocumentDidChange();
    }

    public void resetDirty() {
        // todo?
    }

    public void setTitle(String title) {
        // todo?
    }

    public ComponentContainer getComponentContainer() {
        return container;
    }

    public void setContentPanel(JPanel panel) {
        getContentPane().add(panel);
    }

    public void addDocument(XJDocument doc) {
        if(!documents.contains(doc)) {
            documents.add(doc);
        }
    }

    public void setDocument(XJDocument document) {
        documents.clear();
        documents.add(document);
    }

    public ComponentDocument getDocument() {
        if(documents.isEmpty()) {
            return null;
        } else {
            return (ComponentDocument) documents.get(0);
        }
    }

    public Dimension getSize() {
        return rootPane.getSize();
    }

    public XJFrameInterface getXJFrame() {
        return frameInterface;
    }

    // XJFrameInterface

    public void registerUndo(XJUndoDelegate delegate, JTextPane textPane) {
        frameInterface.registerUndo(delegate, textPane);
    }

    public void unregisterUndo(XJUndoDelegate delegate) {
        frameInterface.unregisterUndo(delegate);
    }

    public XJUndo getUndo(JTextPane textPane) {
        return frameInterface.getUndo(textPane);
    }

    public XJUndo getCurrentUndo() {
        return frameInterface.getCurrentUndo();
    }

    public XJMainMenuBar getMainMenuBar() {
        return mainMenuBar;
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        frameInterface.handleMenuEvent(menu, item);
    }

    public Container getJavaContainer() {
        return frameInterface.getJavaContainer();
    }

}
