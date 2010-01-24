package org.antlr.works.components.editor;

import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.ATETextPane;
import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.ComponentStatusBar;
import org.antlr.works.components.container.DocumentContainer;
import org.antlr.works.components.document.AWDocument;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.dialog.AWPrefsDialog;
import org.antlr.works.editor.navigation.GoToRule;
import org.antlr.works.find.FindAndReplace;
import org.antlr.works.grammar.element.Jumpable;
import org.antlr.xjlib.appkit.frame.XJFrame;
import org.antlr.xjlib.appkit.undo.XJUndo;
import org.antlr.xjlib.foundation.notification.XJNotificationCenter;
import org.antlr.xjlib.foundation.notification.XJNotificationObserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

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

public abstract class DocumentEditor implements XJNotificationObserver {

    protected DocumentContainer container;
    protected AWDocument document;

    protected JPanel mainPanel;
    protected Box statusBar;

    protected ComponentListener cl;
    protected PropertyChangeListener pcl;

    public DocumentEditor() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.addComponentListener(cl = new MainPanelComponentListener());

        statusBar = new ComponentStatusBar();
        statusBar.setPreferredSize(new Dimension(0, 30));

        XJNotificationCenter.defaultCenter().addObserver(this, AWPrefsDialog.NOTIF_PREFS_APPLIED);
        XJNotificationCenter.defaultCenter().addObserver(this, Debugger.NOTIF_DEBUG_STARTED);
        XJNotificationCenter.defaultCenter().addObserver(this, Debugger.NOTIF_DEBUG_STOPPED);

        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener(
                pcl = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        String prop = e.getPropertyName();
                        if(prop.equals("permanentFocusOwner")) {
                            // Do not refresh all the menu bars...
                            // actually this is wrong because the keyboard focus manager will
                            // invoke this listener even for all the windows, even the inactive ones.
                            // todo - to review this
                            //XJMainMenuBar.refreshAllMenuBars();
                        }
                    }
                }
        );

    }

    public void awake() {
        create();
        assemble();
    }

    public DocumentContainer getContainer() {
        return container;
    }

    public void setContainer(DocumentContainer container) {
        this.container = container;
    }

    public void refreshMainMenuBar() {
        if(getXJFrame().getMainMenuBar() != null)
            getXJFrame().getMainMenuBar().refreshState();
    }

    public JComponent getStatusComponent() {
        return statusBar;
    }

    public void setDocument(AWDocument document) {
        this.document = document;
    }

    public AWDocument getDocument() {
        return document;
    }

    public XJFrame getXJFrame() {
        return container.getXJFrame();
    }

    public Container getJavaContainer() {
        return getXJFrame().getJavaContainer();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public XJUndo getCurrentUndo() {
        return getXJFrame().getCurrentUndo();
    }

    public void notificationFire(Object source, String name) {
        if(name.equals(AWPrefsDialog.NOTIF_PREFS_APPLIED)) {
            notificationPrefsChanged();
        } else if(name.equals(Debugger.NOTIF_DEBUG_STARTED)) {
            notificationDebuggerStarted();
        } else if(name.equals(Debugger.NOTIF_DEBUG_STOPPED)) {
            notificationDebuggerStopped();
        }
    }

    protected static JComponent createSeparator() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        Dimension d = s.getMaximumSize();
        d.width = 2;
        s.setMaximumSize(d);
        return s;
    }

    public void becomingVisibleForTheFirstTime() {
        componentDidAwake();
    }

    /** For subclass only
     *
     */

    public abstract void create();
    public abstract void assemble();

    public abstract void loadText(String text);

    public abstract String getText();
    public abstract ATEPanel getTextEditor();
    public abstract void setCaretPosition(int pos);

    public abstract void goToHistoryRememberCurrentPosition();
    public abstract void goToBackward();
    public abstract void goToForward();
    public abstract List<ATELine> getLines();
    public abstract void find();
    public abstract FindAndReplace getFindAndReplace();
    public abstract GoToRule getGoToRule();
    public abstract ATEToken getCurrentToken();
    public abstract boolean goToRule(String ruleName);
    public abstract void goToDeclaration();
    public abstract void goToDeclaration(final Jumpable ref);
    public abstract List<String> getRulesStartingWith(String match);

    public abstract void beginGroupChange(String name);
    public abstract void endGroupChange();
    public abstract void disableTextPaneUndo();
    public abstract void enableTextPaneUndo();

    public List<ATEToken> getTokens() {
        return getTextEditor().getTokens();
    }

    public ATETextPane getTextPane() {
        return getTextEditor().getTextPane();
    }

    public void setText(String s) {
        getTextEditor().setText(s);
    }

    public int getCaretPosition() {
        return getTextEditor().getCaretPosition();
    }

    public void close() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(pcl);
        pcl = null;
        mainPanel.removeComponentListener(cl);
        cl = null;
        container = null;
        XJNotificationCenter.defaultCenter().removeObserver(this);
    }

    public abstract void componentDocumentContentChanged();

    public boolean componentDocumentWillSave() {
        return true;
    }

    public void componentDidAwake() {
    }

    public void componentActivated() {
    }

    public void componentDidHide() {
    }

    public void componentIsSelected() {
    }

    public void notificationPrefsChanged() {
    }

    public void notificationDebuggerStarted() {
    }

    public void notificationDebuggerStopped() {
    }

    public void setEditable(boolean flag) {

    }

    protected class MainPanelComponentListener extends ComponentAdapter {

        @Override
        public void componentHidden(ComponentEvent e) {
            componentDidHide();
        }
    }

}
