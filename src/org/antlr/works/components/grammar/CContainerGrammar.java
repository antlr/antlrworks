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

package org.antlr.works.components.grammar;

import edu.usfca.xj.appkit.frame.XJFrame;
import edu.usfca.xj.appkit.frame.XJWindow;
import edu.usfca.xj.appkit.menu.XJMainMenuBar;
import edu.usfca.xj.appkit.menu.XJMenu;
import edu.usfca.xj.appkit.menu.XJMenuItem;
import org.antlr.works.components.ComponentContainer;
import org.antlr.works.components.ComponentEditor;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class CContainerGrammar extends XJWindow implements ComponentContainer {

    protected ComponentEditor editor;

    public CContainerGrammar() {
        editor = new CEditorGrammar(this);
        editor.create();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(editor.getToolbar(), BorderLayout.NORTH);
        panel.add(editor.getPanel(), BorderLayout.CENTER);
        panel.add(editor.getStatusBar(), BorderLayout.SOUTH);

        getContentPane().add(panel);

        getJFrame().pack();
    }

    public void setDefaultSize() {
        Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        r.width *= 0.75;
        r.height *= 0.75;
        getRootPane().setPreferredSize(r.getSize());
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
        super.close();
    }

    public void setPersistentData(Map data) {
    }

    public Map getPersistentData() {
        return null;
    }

    public void becomingVisibleForTheFirstTime() {
        editor.componentDidAwake();
        editor.componentShouldLayout();
    }

    public ComponentEditor getEditor() {
        return editor;
    }

    public XJFrame getXJFrame() {
        return this;
    }

    public void customizeFileMenu(XJMenu menu) {
        editor.customizeFileMenu(menu);
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
        super.menuItemState(item);
        editor.menuItemState(item);
    }

    public void handleMenuSelected(XJMenu menu) {
        super.handleMenuSelected(menu);
        editor.handleMenuSelected(menu);
    }

    public void windowActivated() {
        super.windowActivated();
        editor.componentActivated();
    }

    public void windowDocumentPathDidChange() {
        // Called when the document associated file has changed on the disk
        editor.componentDocumentContentChanged();
    }

}
