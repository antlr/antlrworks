package org.antlr.works.components.document;

import org.antlr.works.components.container.ComponentContainer;
import org.antlr.works.components.container.ComponentContainerInternal;
import org.antlr.works.components.container.ComponentDocumentInternal;
import org.antlr.works.components.editor.ComponentEditor;
import org.antlr.works.components.editor.ComponentEditorGrammar;
import org.antlr.works.utils.Localizable;
import org.antlr.xjlib.appkit.document.XJDataPlainText;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.appkit.document.XJDocumentFactory;/*

[The "BSD licence"]
Copyright (c) 2005-07 Jean Bovet
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

public class ComponentDocumentFactory extends XJDocumentFactory {

    // todo provide factory?
    public ComponentDocumentFactory(Class windowClass) {
        super(ComponentDocumentGrammar.class,
                windowClass,
                XJDataPlainText.class,
                "g",
                Localizable.getLocalizedString(Localizable.DOCUMENT_TYPE));
    }

    @Override
    public XJDocument createDocument() throws IllegalAccessException, InstantiationException {
        ComponentDocument doc = (ComponentDocument) super.createDocument();
        createAndBindEditor(doc);
        return doc;
    }

    public ComponentDocumentInternal createInternalDocument(ComponentContainer mainContainer) {
        ComponentDocumentInternal doc = new ComponentDocumentInternal();
        doc.setDocumentData(new XJDataPlainText());
        doc.setDocumentFileType(getExtensions(), getDescriptionString());

        ComponentContainerInternal container = new ComponentContainerInternal(mainContainer);
        container.setDocument(doc);
        doc.setContainer(container);

        createAndBindEditor(doc);

        return doc;
    }

    private void createAndBindEditor(ComponentDocument document) {
        ComponentEditor editor = new ComponentEditorGrammar();

        editor.setDocument(document);
        document.setEditor(editor);

        ComponentContainer container = document.getContainer();
        editor.setContainer(container);
        container.setEditor(editor);
    }

}
