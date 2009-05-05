package org.antlr.works.stringtemplate;

import org.antlr.works.components.container.ComponentContainer;
import org.antlr.works.components.container.ComponentContainerGrammar;
import org.antlr.works.components.container.ComponentContainerInternal;
import org.antlr.works.components.document.ComponentDocumentGrammar;
import org.antlr.works.components.ComponentWindow;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.appkit.frame.XJWindow;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuItem;

import javax.swing.*;
import java.awt.*;

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
public class STWindow extends XJWindow implements ComponentWindow {

    private final ComponentContainer componentContainer;

    public STWindow() {
        this.componentContainer = new ComponentContainerStringTemplate(this);
    }

    @Override
    public void awake() {
        super.awake();
        componentContainer.awake();
        componentContainer.assemble(false);
    }

    public ComponentContainer getComponentContainer() {
        return componentContainer;
    }

    public void setContentPanel(JPanel panel) {
        getContentPane().add(panel);
        pack();
    }

    @Override
    public void dirtyChanged() {
        componentContainer.dirtyChanged();
    }

    @Override
    public void windowActivated() {
        super.windowActivated();
        componentContainer.windowActivated();
    }

    @Override
    public void windowDocumentPathDidChange(XJDocument doc) {
        // Called when the document associated file has changed on the disk
        STDocument st = (STDocument) doc;
        st.getEditor().componentDocumentContentChanged();
    }

    @Override
    public void becomingVisibleForTheFirstTime() {
        componentContainer.becomingVisibleForTheFirstTime();
    }

    @Override
    public void customizeFileMenu(XJMenu menu) {
        componentContainer.customizeFileMenu(menu);
    }

    @Override
    public void customizeMenuBar(XJMainMenuBar menubar) {
        componentContainer.customizeMenuBar(menubar);
    }

    @Override
    public void menuItemState(XJMenuItem item) {
        componentContainer.menuItemState(item);
    }

    @Override
    public void handleMenuSelected(XJMenu menu) {
        componentContainer.handleMenuSelected(menu);
    }

    @Override
    public boolean close(boolean force) {
        return super.close(force) && componentContainer.close();
    }


}
