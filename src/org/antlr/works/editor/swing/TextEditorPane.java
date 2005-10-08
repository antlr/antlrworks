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

package org.antlr.works.editor.swing;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class TextEditorPane extends JTextPane
{
    protected boolean wrap = false;
    protected TextEditorPaneDelegate delegate = null;

    public TextEditorPane() {
        super();
    }
    
    public TextEditorPane(StyledDocument doc) {
        super(doc);
    }

    public void setWordWrap(boolean flag) {
        this.wrap = flag;
    }

    public boolean getWordWrap() {
        return wrap;
    }

    public boolean getScrollableTracksViewportWidth() {
        if (!wrap)
        {
            Component parent = getParent();
            return ((parent == null) ? true :
                    (getUI().getPreferredSize(this).width <
                    parent.getSize().width));
        } else
            return super.getScrollableTracksViewportWidth();
    }

    public void setBounds(int x, int y, int width, int height) {
        if (!wrap) {
            Dimension size = this.getPreferredSize();
            super.setBounds(x, y,
                    Math.max(size.width, width), Math.max(size.height, height));
        } else {
            super.setBounds(x, y, width, height);
        }
    }

    public void setDelegate(TextEditorPaneDelegate delegate) {
        this.delegate = delegate;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(delegate != null)
            delegate.textEditorPaneDidPaint(g);
    }

}
