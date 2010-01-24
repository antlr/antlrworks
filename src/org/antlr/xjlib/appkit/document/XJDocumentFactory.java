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

package org.antlr.xjlib.appkit.document;

import org.antlr.xjlib.appkit.frame.XJWindow;

import java.util.Collections;
import java.util.List;

public class XJDocumentFactory {

    private Class documentClass;
    private Class windowClass;
    private Class dataClass;
    private String ext;
    private String description;

    public XJDocumentFactory(Class documentClass, Class windowClass, Class dataClass, String ext, String description) {
        this.documentClass = documentClass;
        this.windowClass = windowClass;
        this.dataClass = dataClass;
        this.ext = ext;
        this.description = description;
    }

    public boolean handlesPath(String path) {
        return path != null && path.endsWith("."+ext);
    }

    public List<String> getExtensions() {
        return Collections.singletonList(ext);
    }

    public String getDescriptionString() {
        return description;
    }

    public XJDocument createDocument() throws IllegalAccessException, InstantiationException {
        XJDocument document = (XJDocument)documentClass.newInstance();
        document.setDocumentData((XJData)dataClass.newInstance());
        document.setDocumentFileType(getExtensions(), getDescriptionString());

        XJWindow window = (XJWindow)windowClass.newInstance();
        window.addDocument(document);
        document.setWindow(window);

        return document;
    }

}
