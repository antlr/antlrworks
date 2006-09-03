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

    public static final Color BACKGROUND_HIGHLIGHT_COLOR = new Color(1.0f, 1.0f, 0.5f, 0.3f);
    public static Font DEFAULT_FONT;

    protected ATEPanel textEditor;
    protected ATETextPane textPane;
    protected List tokens;

    protected DisplayOperation displayOp = new DisplayOperation();
    protected ModelToViewOperation modelToViewOp = new ModelToViewOperation();
    protected ViewToModel viewToModelOp = new ViewToModel();

    protected Graphics currentGraphics;
    protected Color savedColor;

    public ATERenderingView(Element elem, ATEPanel textEditor) {
        super(elem);
        this.textEditor = textEditor;
        this.textPane = textEditor.getTextPane();
    }

    /**
     * Renders a line of text, suppressing whitespace at the end
     * and expanding any tabs.  This is implemented to make calls
     * to the methods <code>drawUnselectedText</code> and
     * <code>drawSelectedText</code> so that the way selected and
     * unselected text are rendered can be customized.
     *
     * @param lineIndex the line to draw >= 0
     * @param g the <code>Graphics</code> context
     * @param x the starting X position >= 0
     * @param y the starting Y position >= 0
     * @see #drawUnselectedText
     * @see #drawSelectedText
     */
    protected void drawLine(int lineIndex, Graphics g, int x, int y) {
        // Highlight the background where the cursor is located
        if(textPane.highlightCursorLine()) {
            Element line = getElement().getElement(lineIndex);
            int p0 = line.getStartOffset();
            int p1 = line.getEndOffset();

            final int cursorPosition = textPane.getCaretPosition()+1;
            if(cursorPosition > p0 && cursorPosition <= p1) {
                saveColor(g);
                g.setColor(BACKGROUND_HIGHLIGHT_COLOR);
                final int fontHeight = metrics.getHeight();
                g.fillRect(0, y-fontHeight+metrics.getDescent(), textPane.getWidth(), fontHeight);
                restore(g);
            }
        }

        super.drawLine(lineIndex, g, x, y);
    }

    /**
     * Provides a mapping from the document model coordinate space
     * to the coordinate space of the view mapped to it.
     *
     * @param pos the position to convert >= 0
     * @param a the allocated region to render into
     * @return the bounding box of the given position
     * @exception BadLocationException  if the given position does not
     *   represent a valid location in the associated document
     * @see View#modelToView
     */
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        if(!textEditor.isSyntaxColoring()) {
            return super.modelToView(pos, a, b);
        }

        // line coordinates
        Element map = getElement();
        int lineIndex = map.getElementIndex(pos);
        Rectangle lineArea = lineToRect(a, lineIndex);

        // determine span from the start of the line
        Element line = map.getElement(lineIndex);
        int p0 = line.getStartOffset();

        // fill in the results and return
        lineArea.x += renderText(modelToViewOp, currentGraphics, 0, 0, p0, pos);
        lineArea.width = 1;
        lineArea.height = metrics.getHeight();
        return lineArea;
    }

    /**
     * Provides a mapping from the view coordinate space to the logical
     * coordinate space of the model.
     *
     * @param fx the X coordinate >= 0
     * @param fy the Y coordinate >= 0
     * @param a the allocated region to render into
     * @return the location within the model that best represents the
     *  given point in the view >= 0
     * @see View#viewToModel
     */
    public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
        if(!textEditor.isSyntaxColoring()) {
            return super.viewToModel(fx, fy, a, bias);
        }

        // PENDING(prinz) properly calculate bias
        bias[0] = Position.Bias.Forward;

        Rectangle alloc = a.getBounds();
        Document doc = getDocument();
        int x = (int) fx;
        int y = (int) fy;
        if (y < alloc.y) {
            // above the area covered by this icon, so the the position
            // is assumed to be the start of the coverage for this view.
            return getStartOffset();
        } else if (y > alloc.y + alloc.height) {
            // below the area covered by this icon, so the the position
            // is assumed to be the end of the coverage for this view.
            return getEndOffset() - 1;
        } else {
            // positioned within the coverage of this view vertically,
            // so we figure out which line the point corresponds to.
            // if the line is greater than the number of lines contained, then
            // simply use the last line as it represents the last possible place
            // we can position to.
            Element map = doc.getDefaultRootElement();
            int lineIndex = Math.abs((y - alloc.y) / metrics.getHeight() );
            if (lineIndex >= map.getElementCount()) {
                return getEndOffset() - 1;
            }
            Element line = map.getElement(lineIndex);
            final int firstLineOffset = 0; // jbovet: see comment in PlainView
            if (lineIndex == 0) {
                alloc.x += firstLineOffset;
                alloc.width -= firstLineOffset;
            }
            if (x < alloc.x) {
                // point is to the left of the line
                return line.getStartOffset();
            } else if (x > alloc.x + alloc.width) {
                // point is to the right of the line
                return line.getEndOffset() - 1;
            } else {
                // Determine the offset into the text
                try {
                    int p0 = line.getStartOffset();
                    int p1 = line.getEndOffset() - 1;

                    viewToModelOp.setParameters(x, p0);
                    renderText(viewToModelOp, currentGraphics, alloc.x, y, p0, p1);
                    return viewToModelOp.modelPos;
                } catch (BadLocationException e) {
                    // should not happen
                    return -1;
                }
            }
        }
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
        this.currentGraphics = g;
        return renderText(displayOp, g, x, y, p0, p1);
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
        this.currentGraphics = g;
        return renderText(displayOp, g, x, y, p0, p1);
    }

    /** This method renders the text using the token information to set up the display attribute
     * of each token.
     *
     */
    protected int renderText(TextOperation action, Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        if(p0 == p1)
            return x;

        if(!textEditor.isSyntaxColoring()) {
            return super.drawUnselectedText(g, x, y, p0, p1);
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
                    x = action.renderTextPortion(g, x, y, p, t.start, p1, doc, null);
                }

                x = action.renderTextPortion(g, x, y, t.start, t.end, p1, doc, attribute);
                p = t.end;
            } else if(t.end >= p0 && t.start < p0) {
                x = action.renderTextPortion(g, x, y, p0, t.end, p1, doc, attribute);
                p = t.end;
            } else if(t.start > p1) {
                break;
            }
        }

        // Fill any remaining range with default color
        if(p < p1) {
            x = action.renderTextPortion(g, x, y, p, p1, p1, doc, null);
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

    /** This method applies an AttributeSet to a Graphics context
     *
     * @param g The graphic context
     * @param attribute The attribute to apply
     */
    protected void applyAttribute(Graphics g, AttributeSet attribute) {
        if(attribute == null) {
            g.setColor(Color.black);
            g.setFont(DEFAULT_FONT);
            return;
        }

        g.setFont(getFontForAttribute(attribute));

        Color c = StyleConstants.getForeground(attribute);
        if(c == null)
            g.setColor(Color.black);
        else
            g.setColor(c);
    }

    /** Return the font given the specified attributes
     *
     * @param attribute The font attributes
     * @return The font given the specified attributes
     */
    protected Font getFontForAttribute(AttributeSet attribute) {
        Font f = DEFAULT_FONT;
        if(StyleConstants.isBold(attribute))
            f = f.deriveFont(Font.BOLD);
        if(StyleConstants.isItalic(attribute))
            f = f.deriveFont(Font.ITALIC);
        return f;
    }

    /** Save the current graphics color
     *
     * @param g The graphics
     */
    protected void saveColor(Graphics g) {
        savedColor = g.getColor();
    }

    /** Restore the previously saved graphics color
     *
     * @param g The graphics
     */
    protected void restore(Graphics g) {
        g.setColor(savedColor);
    }

    public interface TextOperation {
        int renderTextPortion(Graphics g, int x, int y, int start, int end, int max, Document doc, AttributeSet attribute) throws BadLocationException;
    }

    public class DisplayOperation implements TextOperation {

        public int renderTextPortion(Graphics g, int x, int y, int start, int end, int max, Document doc, AttributeSet attribute)
                throws BadLocationException
        {
            if(g == null)
                return 0;

            int length = end - start;
            if(start + length > max)
                length = max - start;

            saveColor(g);
            applyAttribute(g, attribute);
            Segment text = getLineBuffer();
            doc.getText(start, length, text);

            x = Utilities.drawTabbedText(text, x, y, g, ATERenderingView.this, start);
            restore(g);
            return x;
        }
    }

    public class ModelToViewOperation implements TextOperation {

        public int renderTextPortion(Graphics g, int x, int y, int start, int end, int max, Document doc, AttributeSet attribute)
                throws BadLocationException
        {
            if(g == null)
                return 0;

            int length = end - start;
            if(start + length > max)
                length = max - start;

            saveColor(g);
            applyAttribute(g, attribute);
            Segment text = getLineBuffer();
            doc.getText(start, length, text);

            x += Utilities.getTabbedTextWidth(text, g.getFontMetrics(), x, ATERenderingView.this, start);
            restore(g);
            return x;
        }
    }

    public class ViewToModel implements TextOperation {

        private int modelPos;
        private int viewX;

        public void setParameters(int viewX, int modelPos) {
            this.viewX = viewX;
            this.modelPos = modelPos;
        }

        public int renderTextPortion(Graphics g, int x, int y, int start, int end, int max, Document doc, AttributeSet attribute)
                throws BadLocationException
        {
            if(g == null)
                return 0;

            int length = end - start;
            if(start + length > max)
                length = max - start;

            saveColor(g);
            applyAttribute(g, attribute);
            Segment text = getLineBuffer();
            doc.getText(start, length, text);

            modelPos += Utilities.getTabbedTextOffset(text, g.getFontMetrics(), x, viewX, ATERenderingView.this, start);
            x +=Utilities.getTabbedTextWidth(text, g.getFontMetrics(), x, ATERenderingView.this, start);

            restore(g);
            return x;
        }
    }

}
