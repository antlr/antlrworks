package org.antlr.works.editor;

import org.antlr.works.ate.swing.ATERenderingToken;
import org.antlr.works.ate.swing.ATERenderingView;
import org.antlr.works.ate.swing.ATERenderingViewDelegate;
import org.antlr.works.components.editor.ComponentEditorGrammar;
import org.antlr.works.grammar.element.Jumpable;

import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;
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

public class EditorATEEditorKit extends StyledEditorKit
        implements ViewFactory, ATERenderingViewDelegate {

    private ComponentEditorGrammar editor;
    private Set<ATERenderingView> views = new HashSet<ATERenderingView>();

    public EditorATEEditorKit(ComponentEditorGrammar editor) {
        this.editor = editor;
    }

    public void close() {
        for(ATERenderingView v : views) {
            v.close();
        }
        views.clear();
        editor = null;
    }

    @Override
    public ViewFactory getViewFactory() {
        return this;
    }

    @Override
    public Document createDefaultDocument() {
        return new DefaultStyledDocument();
    }

    public View create(Element elem) {
        ATERenderingView v = new ATERenderingView(elem, editor.getTextEditor());
        v.setDelegate(this);
        views.add(v);
        return v;
    }

    private ATERenderingToken debuggerCursorToken = new DebuggerCursorToken();
    private HighlightedReferenceStartToken highlightedReferenceStartToken = new HighlightedReferenceStartToken();
    private HighlightedReferenceToken highlightedReferenceToken = new HighlightedReferenceToken();

    public ATERenderingToken[] getTokens() {
        // todo cache that
        List<ATERenderingToken> indexes = new ArrayList<ATERenderingToken>();
        Jumpable ref = editor.getHighlightedReference();
        if(ref != null) {
            highlightedReferenceStartToken.setIndex(ref.getStartIndex());
            highlightedReferenceToken.setIndex(ref.getEndIndex());
            highlightedReferenceToken.setStartToken(highlightedReferenceStartToken);
            indexes.add(highlightedReferenceStartToken);
            indexes.add(highlightedReferenceToken);
        }
        if(editor.getDebuggerLocation() != -1) {
            debuggerCursorToken.setIndex(editor.getDebuggerLocation());
            indexes.add(debuggerCursorToken);
        }
        if(!indexes.isEmpty()) {
            Collections.sort(indexes);
        }

        return indexes.toArray(new ATERenderingToken[indexes.size()]);
    }

    private static class DebuggerCursorToken extends ATERenderingToken {

        public void drawToken(ATERenderingView view, ATERenderingToken t,
                              Graphics g, FontMetrics metrics,
                              int x, int y, char c, Document doc,
                              AttributeSet attribute, Segment text)
        {
            g.setColor(Color.red);
            g.fillRect(x, y- metrics.getHeight()+metrics.getDescent(),
                    metrics.charWidth(c), metrics.getHeight());
        }
    }

    private static class HighlightedReferenceStartToken extends ATERenderingToken {

        private int startX;
        private int startIndex;

        public void drawToken(ATERenderingView view, ATERenderingToken t,
                              Graphics g, FontMetrics metrics,
                              int x, int y, char c, Document doc,
                              AttributeSet attribute, Segment text)
        {
            startX = x;
            startIndex = t.getIndex();
        }

        public int getStartX() {
            return startX;
        }

        public int getStartIndex() {
            return startIndex;
        }
    }

    private static class HighlightedReferenceToken extends ATERenderingToken {

        private HighlightedReferenceStartToken startToken;

        public void setStartToken(HighlightedReferenceStartToken startToken) {
            this.startToken = startToken;
        }

        public void drawToken(ATERenderingView view, ATERenderingToken t,
                              Graphics g, FontMetrics metrics,
                              int x, int y, char c, Document doc,
                              AttributeSet attribute, Segment text)
                throws BadLocationException
        {
            g.setColor(Color.blue);
            //g.drawLine(beginX, y+2, x, y+2);
            g.drawLine(startToken.getStartX(), y+1, x, y+1);

            // draw the text
            doc.getText(startToken.getStartIndex(), t.getIndex()-startToken.getStartIndex(), text);
            Utilities.drawTabbedText(text, startToken.getStartX(), y, g, view, startToken.getStartIndex());
        }
    }

}
