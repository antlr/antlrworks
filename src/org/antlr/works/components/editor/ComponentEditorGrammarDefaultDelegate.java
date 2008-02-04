package org.antlr.works.components.editor;

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

public class ComponentEditorGrammarDefaultDelegate implements ComponentEditorGrammarDelegate {

    public JSplitPane splitPane;

    public ComponentEditorGrammarDefaultDelegate(JSplitPane splitPane) {
        this.splitPane = splitPane;
    }

    public void setBottomComponentVisible(boolean visible) {
        if(visible) {
            splitPane.setDividerLocation(splitPane.getLastDividerLocation());
            // It may happen that the last divider location is already collapsed!
            // In this case, we use the relative divider location.
            if(!isBottomComponentVisible())
                splitPane.setDividerLocation(0.6f);
        } else {
            splitPane.setDividerLocation(1.0f);
        }
    }

    public boolean isBottomComponentVisible() {
        return splitPane.getBottomComponent().getHeight() != 0;
    }
}
