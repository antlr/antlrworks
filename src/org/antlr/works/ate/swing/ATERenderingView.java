package org.antlr.works.ate.swing;

import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.ATETextPane;
import org.antlr.works.ate.syntax.generic.ATESyntaxEngine;
import org.antlr.works.ate.syntax.misc.ATEToken;

import javax.swing.text.*;
import java.awt.*;
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

/** This class is responsible to draw each line of the text and
 * apply the appropriate attributes for each token.
 *
 * The idea behind this class has been inspired by the open-source project DrJava:
 * http://drjava.sourceforge.net/
 */
public class ATERenderingView extends PlainView {

    public static final Color highlightColor = new Color(1.0f, 1.0f, 0.5f, 0.3f);
    public static Font DEFAULT_FONT;

    private ATEPanel textEditor;
    private ATETextPane textPane;
    private List tokens;

    public ATERenderingView(Element elem, ATEPanel textEditor) {
      super(elem);
        this.textEditor = textEditor;
        this.textPane = textEditor.getTextPane();
    }

    /**
     * Renders the given range in the model as normal unselected
     * text.  Uses the foreground or disabled color to render the text.
     *
     * @param g  the graphics context
     * @param x  the starting X coordinate >= 0
     * @param y  the starting Y coordinate >= 0
     * @param p0 the beginning position in the model >= 0
     * @param p1 the ending position in the model >= 0
     * @return the X location of the end of the range >= 0
     * @throws javax.swing.text.BadLocationException
     *          if the range is invalid
     */
    protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        if(p0 == p1)
            return x;

        if(!textEditor.isSyntaxColoring()) {
            return super.drawUnselectedText(g, x, y, p0, p1);
        }

        // Highlight the background where the cursor is located
        final int cursorPosition = textPane.getCaretPosition()+1;
        if(cursorPosition > p0 && cursorPosition <= p1) {
            g.setColor(highlightColor);
            final int fontHeight = metrics.getHeight();
            g.fillRect(x, y-fontHeight+metrics.getDescent(), textPane.getWidth(), fontHeight);
        }

        // Note: the tokens are not contiguous (e.g. white spaces are ignored)
        final Document doc = getDocument();
        final ATESyntaxEngine engine = textEditor.getParserEngine();
        tokens = engine.getTokens();
        int p = p0;
        int start = findStartingTokenIndex(p0, 0, tokens.size(), 0);
        for (int i = start; i < tokens.size(); i++) {
            ATEToken t = (ATEToken) tokens.get(i);
            AttributeSet attribute = engine.getAttributeForToken(t);
            if(t.start >= p0 && t.start <= p1) {
                // Fill any non-contiguous token with default color
                if(t.start > p) {
                    x = drawText(g, x, y, p, t.start, p1, doc, null);
                }

                x = drawText(g, x, y, t.start, t.end, p1, doc, attribute);
                p = t.end;
            } else if(t.end >= p0 && t.start < p0) {
                x = drawText(g, x, y, p0, t.end, p1, doc, attribute);
                p = t.end;
            } else if(t.start > p1) {
                break;
            }
        }

        // Fill any remaining range with default color
        if(p < p1) {
            x = drawText(g, x, y, p, p1, p1, doc, null);
        }

        return x;
    }

    /** This method finds the first token that is located in the line index p0
     *
     * @param p0
     * @param low
     * @param high
     * @param candidate
     * @return The index of the first token on line p0
     */
    private int findStartingTokenIndex(int p0, int low, int high, int candidate) {
        if(Math.abs(high-low) <= 1)
            return Math.min(candidate, low);

        int middle = low + (high-low) / 2;
        ATEToken t = (ATEToken) tokens.get(middle);
        if(p0 >= t.startLineIndex && p0 <= t.endLineIndex) {
            return findStartingTokenIndex(p0, low, middle, middle);
        } else {
            if(t.startLineIndex < p0)
                return findStartingTokenIndex(p0, middle, high, candidate);
            else
                return findStartingTokenIndex(p0, low, middle, candidate);
        }
    }

    private int drawText(Graphics g, int x, int y, int start, int end, int max, Document doc, AttributeSet attribute) throws BadLocationException
    {
        int length = end - start;
        if(start + length > max)
            length = max - start;

        applyAttribute(g, attribute);
        Segment text = getLineBuffer();
        doc.getText(start, length, text);

        return Utilities.drawTabbedText(text, x, y, g, this, start);
    }

    private void applyAttribute(Graphics g, AttributeSet attribute) {
        if(attribute == null) {
            g.setColor(Color.black);
            g.setFont(DEFAULT_FONT);
            return;
        }

        Font f = DEFAULT_FONT;
        if(StyleConstants.isBold(attribute))
            f = f.deriveFont(Font.BOLD);
        if(StyleConstants.isItalic(attribute))
            f = f.deriveFont(Font.ITALIC);
        g.setFont(f);

        Color c = StyleConstants.getForeground(attribute);
        if(c == null)
            g.setColor(Color.black);
        else
            g.setColor(c);
    }

    /**
     * Renders the given range in the model as selected text.  This
     * is implemented to render the text in the color specified in
     * the hosting component.  It assumes the highlighter will render
     * the selected background.
     *
     * @param g  the graphics context
     * @param x  the starting X coordinate >= 0
     * @param y  the starting Y coordinate >= 0
     * @param p0 the beginning position in the model >= 0
     * @param p1 the ending position in the model >= 0
     * @return the location of the end of the range
     * @throws javax.swing.text.BadLocationException
     *          if the range is invalid
     */
    protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        g.setColor(Color.black);
        return super.drawSelectedText(g, x, y, p0, p1);
    }

}
