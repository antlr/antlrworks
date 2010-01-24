package org.antlr.works.stringtemplate;

import org.antlr.works.components.container.DocumentContainer;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.appkit.frame.XJWindow;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuItem;

import javax.swing.*;

/*

[The "BSD licence"]
Copyright (c) 2005-08 Jean Bovet
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
public class STWindow extends XJWindow {

    private final DocumentContainer documentContainer;

    public STWindow() {
        this.documentContainer = new StringTemplateContainer(this);
    }

    @Override
    public void awake() {
        super.awake();
        documentContainer.awake();
        documentContainer.assemble(false);
    }

    public DocumentContainer getComponentContainer() {
        return documentContainer;
    }

    public void setContentPanel(JPanel panel) {
        getContentPane().add(panel);
        pack();
    }

    @Override
    public void dirtyChanged() {
        documentContainer.dirtyChanged();
    }

    @Override
    public void windowActivated() {
        super.windowActivated();
        documentContainer.windowActivated();
    }

    @Override
    public void windowDocumentPathDidChange(XJDocument doc) {
        // Called when the document associated file has changed on the disk
        STDocument st = (STDocument) doc;
        st.getEditor().componentDocumentContentChanged();
    }

    @Override
    public void becomingVisibleForTheFirstTime() {
        documentContainer.becomingVisibleForTheFirstTime();
    }

    @Override
    public void customizeFileMenu(XJMenu menu) {
        documentContainer.customizeFileMenu(menu);
    }

    @Override
    public void customizeMenuBar(XJMainMenuBar menubar) {
        documentContainer.customizeMenuBar(menubar);
    }

    @Override
    public void menuItemState(XJMenuItem item) {
        documentContainer.menuItemState(item);
    }

    @Override
    public void handleMenuSelected(XJMenu menu) {
        documentContainer.handleMenuSelected(menu);
    }

    @Override
    public boolean close(boolean force) {
        return super.close(force) && documentContainer.close();
    }


}
