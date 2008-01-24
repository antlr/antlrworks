package org.antlr.works.editor;

import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.swing.ATERenderingView;
import org.antlr.works.debugger.Debugger;

import javax.swing.text.*;
import java.awt.*;
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

public class EditorATERenderingView extends ATERenderingView {

    protected Debugger debugger;

    public EditorATERenderingView(Element elem, ATEPanel textEditor, Debugger debugger) {
        super(elem, textEditor);
        this.debugger = debugger;
        displayOp = new EditorDisplayOperation();
    }

    @Override
    public void close() {
        super.close();
        debugger = null;
        displayOp = null;
    }

    public class EditorDisplayOperation extends DisplayOperation {

        public int renderTextPortion(Graphics g, int x, int y, int start, int end, int max, Document doc, AttributeSet attribute)
                throws BadLocationException
        {
            if(debugger == null) {
                return super.renderTextPortion(g, x, y, start, end, max, doc, attribute);
            }

            final int debuggerCursorIndex = debugger.getDebuggerCursorIndex();
            if(debuggerCursorIndex == -1) {
                return super.renderTextPortion(g, x, y, start, end, max, doc, attribute);
            }

            int length = end - start;
            if(start + length > max)
                length = max - start;

            if(debuggerCursorIndex >= start && debuggerCursorIndex < start+length) {
                final Segment text = getLineBuffer();
                doc.getText(debuggerCursorIndex, 1, text);
                final char c = text.first();

                if(debuggerCursorIndex == start) {
                    drawDebuggerCursor(g, x, y, c);
                    return super.renderTextPortion(g, x, y, start, end, max, doc, attribute);
                } else {
                    x = super.renderTextPortion(g, x, y, start, debuggerCursorIndex-1, max, doc, attribute);
                    drawDebuggerCursor(g, x, y, c);
                    return super.renderTextPortion(g, x, y, debuggerCursorIndex-1, end, max, doc, attribute);
                }
            } else {
                return super.renderTextPortion(g, x, y, start, end, max, doc, attribute);
            }
        }

        private void drawDebuggerCursor(Graphics g, int x, int y, char c) {
            saveColor(g);
            g.setColor(Color.red);
            final int fontHeight = metrics.getHeight();
            g.fillRect(x, y-fontHeight+metrics.getDescent(), metrics.charWidth(c), fontHeight);
            restore(g);
        }

    }
}
