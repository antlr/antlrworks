package org.antlr.works.components.project.file;

import edu.usfca.xj.appkit.document.XJDataPlainText;
import edu.usfca.xj.appkit.document.XJDocument;
import edu.usfca.xj.appkit.frame.XJFrame;
import edu.usfca.xj.appkit.menu.*;
import org.antlr.works.components.ComponentContainer;
import org.antlr.works.components.ComponentDocument;
import org.antlr.works.components.ComponentEditor;
import org.antlr.works.components.project.CContainerProject;
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

public abstract class CContainerProjectFile implements ComponentContainer, XJMenuBarCustomizer, XJMenuBarDelegate {

    public CContainerProject project;

    public ComponentEditor editor;
    public ComponentDocument document;

    public XJMainMenuBar mainMenuBar;

    public CContainerProjectFile(CContainerProject project) {
        this.project = project;

        document = createDocument();
        document.setJavaContainer(project.getJavaContainer());
        document.setDocumentData(new XJDataPlainText());
        document.setComponentContainer(this);

        editor = createEditor();
        editor.create();
        editor.componentDidAwake();

        awake();
    }

    public void awake() {

    }

    public abstract ComponentDocument createDocument();
    public abstract ComponentEditor createEditor();

    public void createMainMenuBar() {
        if(mainMenuBar == null) {
            mainMenuBar = XJMainMenuBar.createInstance();
            mainMenuBar.setCustomizer(this);
            mainMenuBar.setDelegate(this);
            mainMenuBar.createMenuBar();
        }
    }

    public XJMainMenuBar getMainMenuBar() {
        if(mainMenuBar == null)
            createMainMenuBar();
        return mainMenuBar;
    }

    public ComponentEditor getEditor() {
        return editor;
    }

    public XJDocument getDocument() {
        return document;
    }

    public XJFrame getXJFrame() {
        return project.getXJFrame();
    }

    public void loadText(String text) {
        editor.loadText(text);
    }

    public String getText() {
        return editor.getText();
    }

    public void close() {
        editor.close();
        document.performClose(true);

        if(getMainMenuBar() == getMainMenuBar()) {
            // If the main menu bar is the one of the editor,
            // the replace it by the default project menu bar.
            project.setDefaultMainMenuBar();
        }
        XJMainMenuBar.removeInstance(getMainMenuBar());
    }

    public boolean willSaveDocument() {
        return editor.componentDocumentWillSave();
    }

    public void setDirty() {
        project.fileDidBecomeDirty(this);
    }

    /** Menu delegate and customizer
     * Note:
     * Because each project's file owns its main menu bar,
     * we have to redirect every menu event to the project itself
     * so it has a chance to handle them.
     */

    public void customizeFileMenu(XJMenu menu) {
        project.customizeFileMenu(menu);
        editor.customizeFileMenu(menu);
    }

    public void customizeWindowMenu(XJMenu menu) {
        project.customizeWindowMenu(menu);
        editor.customizeWindowMenu(menu);
    }

    public void customizeHelpMenu(XJMenu menu) {
        project.customizeHelpMenu(menu);
        editor.customizeHelpMenu(menu);
    }

    public void customizeMenuBar(XJMainMenuBar menubar) {
        project.customizeMenuBar(menubar);
        editor.customizeMenuBar(menubar);
    }

    public void menuItemState(XJMenuItem item) {
        project.menuItemState(item);
        editor.menuItemState(item);
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        project.handleMenuEvent(menu, item);
    }

}
