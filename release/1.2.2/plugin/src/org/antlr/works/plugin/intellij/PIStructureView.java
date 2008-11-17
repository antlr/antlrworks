package org.antlr.works.plugin.intellij;

import com.intellij.ide.structureView.StructureView;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import org.antlr.works.plugin.container.PluginWindow;

import javax.swing.*;
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

public class PIStructureView implements StructureView {

    public FileEditor fileEditor;
    public PluginWindow window;

    public PIStructureView(FileEditor fileEditor, Project project, PluginWindow window) {
        this.fileEditor = fileEditor;
        this.window = window;
    }

    public FileEditor getFileEditor() {
        return fileEditor;
    }

    public boolean navigateToSelectedElement(boolean requestFocus) {
        return false;
    }

    public JComponent getComponent() {
        return window.getComponentContainer().getRulesComponent();
    }

    public void dispose() {
    }

    public void centerSelectedRow() {
    }

    public void restoreState() {
    }

    public void storeState() {
    }

    // needed for IntelliJ 8.x
    public StructureViewModel getTreeModel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
