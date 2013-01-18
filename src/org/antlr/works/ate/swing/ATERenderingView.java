package org.antlr.works.ate.swing;

import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.ATETextPane;
import org.antlr.works.ate.ATEUtilities;
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

    private ATERenderingViewDelegate delegate;
    private ATEPanel textEditor;
    private ATETextPane textPane;
    private List<ATEToken> tokens;

    private DisplayOperation displayOp = new DisplayOperation();
    private final ModelToViewOperation modelToViewOp = new ModelToViewOperation();
    private final ViewToModel viewToModelOp = new ViewToModel();

    private Graphics currentGraphics;
    private Color savedColor;

    public ATERenderingView(Element elem, ATEPanel textEditor) {
        super(elem);
        this.textEditor = textEditor;
        this.textPane = textEditor.getTextPane();
    }

    public void setDelegate(ATERenderingViewDelegate delegate) {
        this.delegate = delegate;
        if(delegate != null) {
            displayOp = new DisplayDelegateOperation();
        }
    }

    public void close() {
        textEditor = null;
        textPane = null;
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
            final Element line = getElement().getElement(lineIndex);
            final int p0 = line.getStartOffset();
            final int p1 = line.getEndOffset();

            final int cursorPosition = textPane.getCaretPosition()+1;
            if(cursorPosition > p0 && cursorPosition <= p1) {
                save(g);
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
     */
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        if(textEditor == null || !textEditor.isSyntaxColoring()) {
            return super.modelToView(pos, a, b);
        }

        // Fetch the line index closest to the given position
        final Element element = getElement();
        // Fetch the element for the current view
        final int lineIndex = element.getElementIndex(pos);

        // Fetch the element located at the beginning of the line
        final Element line = element.getElement(lineIndex);
        // Fetch the index of this element
        final int p0 = line.getStartOffset();

        // Fetch the rectangle of the line
        final Rectangle posRect = lineToRect(a, lineIndex);

        // Compute the location of the specified position using our renderText() method:
        // it will take care of all characters attributes.
        posRect.x += renderText(modelToViewOp, currentGraphics, 0, 0, p0, pos);

        // Default width
        posRect.width = 1;
        // The height of a line is considered as fix
        posRect.height = metrics.getHeight();

        // Return the rectangle representing the specified position
        return posRect;
    }

    /**
     * Provides a mapping from the view coordinate space to the logical
     * coordinate space of the model.
     *
     */
    public int viewToModel(float fx, float fy, Shape a, Position.Bias[] biasReturn) {
        if(textEditor == null || !textEditor.isSyntaxColoring()) {
            return super.viewToModel(fx, fy, a, biasReturn);
        }

        // Currently we don't compute the exact bias. We take the default one (forward).
        biasReturn[0] = Position.Bias.Forward;

        // Fetch the allocated region bounds in which to render
        final Rectangle bounds = a.getBounds();
        if(fy < bounds.y) {
            // The y coordinate is below the allocated region in which to render.
            // Return the start offset of the view.
            return getStartOffset();
        } else if(fy > bounds.y + bounds.height) {
            // The y coordinate is above the allocated region in which to render.
            // Return the end offset of the view.
            return getEndOffset() - 1;
        }

        // Now compute the location of line the position corresponds to. It is possible
        // that the position, while in the allocated region, can be located after the
        // last line of the document.
        Element element = getElement();

        // Again consider each line as having a fixed height
        final int lineIndex = Math.abs(((int)fy - bounds.y) / metrics.getHeight() );
        if(lineIndex >= element.getElementCount()) {
            // Past the last line of the document, return the last index of the view
            return getEndOffset() - 1;
        }

        // OK. Now let's see if the x coordinate is past the left or right edge of the line
        final Element line = element.getElement(lineIndex);
        if(fx < bounds.x) {
            // The x coordinate is past the left edge of the line. Return the start offset of the line.
            return line.getStartOffset();
        } else if(fx > bounds.x + bounds.width) {
            // The x coordinate is past the right edge of the line. Return the end offset of the line.
            return line.getEndOffset() - 1;
        }

        // Fine. Now let's compute the exact location by using our custom rendering method
        // that will take care of each token attribute.
        final int p0 = line.getStartOffset();
        final int p1 = line.getEndOffset() - 1;
        try {
            viewToModelOp.setParameters((int)fx, p0);
            renderText(viewToModelOp, currentGraphics, bounds.x, (int)fy, p0, p1);
            return viewToModelOp.modelPos;
        } catch (BadLocationException e) {
            // What should we do? Currently nothing because it should not happen.
            return -1;
        }
    }

    @Override
    public void paint(Graphics g, Shape a) {
        ATEUtilities.prepareForText(g);
        super.paint(g, a);
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

    /**
     * This method renders the text using the token information to set up the display attribute
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
        final int start = findStartingTokenIndex(p0, 0, tokens.size(), 0);
        for (int i = start; i < tokens.size(); i++) {
            ATEToken t = tokens.get(i);
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

        final int middle = low + (high-low) / 2;
        final ATEToken t = tokens.get(middle);
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

        final Color c = StyleConstants.getForeground(attribute);
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

    protected void save(Graphics g) {
        savedColor = g.getColor();
    }

    protected void restore(Graphics g) {
        g.setColor(savedColor);
    }

    public interface TextOperation {
        /**
         * Renders a portion of text at the specified location (x,y) and characters
         * range (start, end) using the specified attribute (attribute).
         *
         * @param g The graphic context
         * @param x The x-coordinate where to start drawing the text
         * @param y The y-coordinate where to start drawing the text
         * @param start The starting index of the character
         * @param end The ending index of the character
         * @param max The maximum length of the text
         * @param doc The document to display
         * @param attribute The attributes to use
         * @return The next x-coordinate
         * @throws BadLocationException If the location is incorrect, this exception is thrown
         */
        int renderTextPortion(Graphics g, int x, int y, int start, int end, int max, Document doc, AttributeSet attribute) throws BadLocationException;
    }

    /**
     * Default class that knows how to render a portion of text
     */
    public class DisplayOperation implements TextOperation {

        public int renderTextPortion(Graphics g, int x, int y, int start, int end, int max, Document doc, AttributeSet attribute)
                throws BadLocationException
        {
            if(g == null)
                return 0;

            int length = end - start;
            if(start + length > max)
                length = max - start;

            save(g);
            applyAttribute(g, attribute);
            Segment text = getLineBuffer();
            doc.getText(start, length, text);

            x = Utilities.drawTabbedText(text, x, y, g, ATERenderingView.this, start);
            restore(g);
            return x;
        }
    }

    /**
     * Class that knows how to render a portion of text given
     * an array of tokens fetched from a delegate object. The tokens
     * are used to render the text with different attribute, given
     * the token.
     */
    public class DisplayDelegateOperation extends DisplayOperation {

        public int renderTextPortion(Graphics g, int x, int y, int start, int end, int max, Document doc, AttributeSet attribute) throws BadLocationException {
            ATERenderingToken[] tokens = delegate.getTokens();
            if(tokens == null || tokens.length == 0) {
                return super.renderTextPortion(g, x, y, start, end, max, doc, attribute);
            }

            // adjust length
            int length = end - start;
            if(start + length > max)
                length = max - start;

            final Segment text = getLineBuffer();
            int cursor = start;
            for(ATERenderingToken t : tokens) {
                if(t.index >= start && t.index < start+length) {
                    // draw up to token
                    if(t.index > cursor) {
                        x = super.renderTextPortion(g, x, y, cursor, t.index, max, doc, attribute);
                        cursor = t.index;
                    }
                    doc.getText(t.index, 1, text);
                    final char c = text.first();
                    save(g);
                    t.drawToken(ATERenderingView.this, t, g, metrics, 
                            x, y, c, doc, attribute, text);
                    restore(g);
                }
            }
            // draw remaining
            if(end > cursor) {
                x = super.renderTextPortion(g, x, y, cursor, end, max, doc, attribute);
            }
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

            save(g);
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

            save(g);
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
