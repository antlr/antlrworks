package org.antlr.works.components.document;

import org.antlr.works.components.container.ComponentContainer;
import org.antlr.works.components.editor.ComponentEditor;
import org.antlr.xjlib.appkit.document.XJDataPlainText;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.foundation.XJUtils;
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

public abstract class ComponentDocument extends XJDocument {

    private ComponentContainer container;
    private ComponentEditor editor;

    public ComponentEditor getEditor() {
        return editor;
    }

    public void setEditor(ComponentEditor editor) {
        this.editor = editor;
    }

    public ComponentContainer getContainer() {
        // The container is usually the window of the document. However, for internal container,
        // the container is InternalContainer and does not extends from XJWindow.
        if(container != null) {
            return container;
        } else {
            return (ComponentContainer) getWindow();
        }
    }

    public void setContainer(ComponentContainer container) {
        this.container = container;
    }

    @Override
    public void awake() {
        super.awake();
        editor.awake();
    }

    @Override
    public void changeDone() {
        super.changeDone();
        getContainer().setDirty();
    }

    @Override
    public void documentWillWriteData() {
        XJDataPlainText data = (XJDataPlainText)getDocumentData();
        data.setText(XJUtils.getLocalizedText(editor.getText()));
    }

    @Override
    public void documentDidReadData() {
        XJDataPlainText data = (XJDataPlainText)getDocumentData();
        editor.loadText(XJUtils.getNormalizedText(data.getText()));
        getContainer().documentLoaded(this);
    }

}
