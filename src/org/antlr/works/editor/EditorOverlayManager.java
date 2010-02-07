package org.antlr.works.editor;

import org.antlr.works.ate.ATEOverlayManager;
import org.antlr.works.components.GrammarWindow;

import java.awt.*;
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

public class EditorOverlayManager extends ATEOverlayManager {

    protected GrammarWindow window;

    public EditorOverlayManager(GrammarWindow window) {
        super(window.textEditor);
        this.window = window;
    }

    @Override
    public void close() {
        super.close();
        window = null;
    }

    public void render(Graphics g) {
        renderItems(g, window.editorInspector.getErrors());
        renderItems(g, window.editorInspector.getWarnings());
        renderItems(g, window.editorInspector.getDecisionDFAs());
    }

    private void renderItems(Graphics g, List<EditorInspectorItem> items) {
        if(items == null)
            return;

        for(EditorInspectorItem item : items) {
            drawUnderlineAtIndexes(g, item.color, item.startIndex, item.endIndex, item.shape);
        }
    }

}
