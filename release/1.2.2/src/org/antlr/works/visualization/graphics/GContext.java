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

package org.antlr.works.visualization.graphics;

import org.antlr.works.visualization.graphics.primitive.GLiteral;
import org.antlr.works.visualization.graphics.shape.GLink;
import org.antlr.works.visualization.graphics.shape.GNode;
import org.antlr.works.visualization.skin.Skin;

import java.awt.*;
import java.util.Stack;

public class GContext {

    public static final String EPSILON_WIDTH = "w";
    public static final String EPSILON_UP = "u";
    public static final String EPSILON_DOWN = "d";

    public static final String BOX_WIDTH = "W";
    public static final String BOX_UP = "U";
    public static final String BOX_DOWN = "D";

    public static final String NODE_WIDTH = "m";
    public static final String NODE_UP = "y";
    public static final String NODE_DOWN = "z";

    public static final String CHAR_WIDTH = "c";
    public static final String LINE_SPACE = "L";

    public static final int ALIGN_CENTER = 0;
    public static final int ALIGN_CENTER_UP = 1;
    public static final int ALIGN_RIGHT = 2;
    public static final int ALIGN_LEFT = 3;

    public Container container;
    public GContextProvider provider;

    public GEngine engine;
    public Skin skin;

    protected float value_factor = 3.2f;

    public int offsetX = 0;
    public int offsetY = 0;
    public boolean drawnode = false;
    public boolean drawdimension = false;

    private boolean showRuleName = true;

    public final Stack<Color> colorStack = new Stack<Color>();

    public Color nodeColor = Color.black;
    public Color linkColor = Color.black;

    public Font boxFont = null;
    public Font titleFont = null;

    public Graphics2D g2d = null;

    public GContext() {

    }

    public void setProvider(GContextProvider provider) {
        this.provider = provider;
    }

    public void clearCache() {
        boxFont = null;
        titleFont = null;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public void setEngine(GEngine engine) {
        this.engine = engine;
        this.engine.setContext(this);
    }

    public void setGraphics2D(Graphics2D g2d) {
        this.g2d = g2d;
    }

    public Graphics2D getGraphics2D() {
        return g2d;
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
    }

    public float getStartOffset() {
        return skin.getStartOffset(this);
    }

    public float getEndOffset() {
        return skin.getEndOffset(this);
    }

    public void setFactor(float factor) {
        this.value_factor = factor;
        clearCache();
    }

    public float getFactor() {
        return value_factor;
    }

    public boolean isShowRuleName() {
        return showRuleName;
    }

    public void setShowRuleName(boolean showRuleName) {
        this.showRuleName = showRuleName;
    }

    public float getPixelEpsilonUp() {
        return getPixelValue(EPSILON_UP);
    }

    public float getPixelEpsilonDown() {
        return getPixelValue(EPSILON_DOWN);
    }

    public float getPixelBoxWidth() {
        return getPixelValue(BOX_WIDTH);
    }

    public float getPixelBoxUp() {
        return getPixelValue(BOX_UP);
    }

    public float getPixelBoxDown() {
        return getPixelValue(BOX_DOWN);
    }

    public float getPixelBoxEdge() {
        return getPixelBoxWidth()/6;
    }

    public float getPixelLineSpace() {
        return getPixelValue(LINE_SPACE);
    }

    public float getPixelNodeWidth() {
        return getPixelValue(NODE_WIDTH);
    }

    public float getPixelArrowWidth() {
        return getPixelBoxWidth()/8;
    }

    public float getPixelArrowHeight() {
        return (getPixelBoxUp()+getPixelBoxDown())/10;
    }

    public Font getBoxFont() {
        if(boxFont == null)
            boxFont = new Font("Monospaced", Font.BOLD, (int)(4*value_factor));
        return boxFont;
    }

    public Font getRuleFont() {
        if(titleFont == null)
            titleFont = new Font("Monospaced", Font.BOLD, (int)(4*value_factor));
        return titleFont;
    }

    public static String getStringWidth(String label) {
        StringBuilder w = new StringBuilder();
        for(int i=0; i<label.length(); i++) {
            w.append(CHAR_WIDTH);
        }
        return GLiteral.max(w.toString(), GContext.BOX_WIDTH);
    }

    public static String getBoxWidth(String label) {
        StringBuilder w = new StringBuilder();
        for(int i=0; i<label.length()+2; i++) {
            w.append(CHAR_WIDTH);
        }
        return GLiteral.max(w.toString(), GContext.BOX_WIDTH);
    }

    public float getPixelValue(String s) {
        if(s == null || s.length() == 0)
            return 0;

        return GLiteral.evaluate(s, skin.getValuesMap())*value_factor;
    }

    public void setColor(Color color) {
        engine.setColor(color);
    }

    public void pushColor(Color color) {
        colorStack.push(engine.getColor());
        setColor(color);
    }

    public void popColor() {
        setColor(colorStack.pop());
    }

    public Color getColorForLabel(String label) {
        if(provider == null)
            return Color.black;
        else
            return provider.contextGetColorForLabel(label);
    }

    public void setLineWidth(float width) {
        engine.setLineWidth(width);
    }

    public void repaint() {
        container.repaint();
    }

    public void drawLine(float x0, float y0, float x1, float y1) {
        engine.drawLine(x0+offsetX, y0+offsetY, x1+offsetX, y1+offsetY);
    }

    public void drawArc(float x, float y, float w, float h, int a0, int a1) {
        engine.drawArc(x+offsetX, y+offsetY, w, h, a0, a1);
    }

    public void drawCircle(float x, float y, float r, boolean erase) {
        if(erase) {
            pushColor(Color.white);
            fillCircle(x, y, r);
            popColor();
        }
        engine.drawCircle(x+offsetX, y+offsetY, r);
    }

    public void drawRect(float x, float y, float dx, float dy, boolean erase) {
        if(erase) {
            pushColor(Color.white);
            fillRect(x, y, dx, dy);
            popColor();
        }
        engine.drawRect(x+offsetX, y+offsetY, dx, dy);
    }

    public void drawRoundRect(float x, float y, float dx, float dy, float arc_dx, float arc_dy, boolean erase) {
        if(erase) {
            pushColor(Color.white);
            fillRect(x, y, dx, dy);
            popColor();
        }
        engine.drawRoundRect(x+offsetX, y+offsetY, dx, dy, arc_dx, arc_dy);
    }

    public void drawOval(float x, float y, float dx, float dy, boolean erase) {
        if(erase) {
            pushColor(Color.white);
            fillOval(x, y, dx, dy);
            popColor();
        }
        engine.drawOval(x+offsetX, y+offsetY, dx, dy);
    }

    public void fillRect(float x, float y, float dx, float dy) {
        engine.fillRect(x+offsetX, y+offsetY, dx, dy);
    }

    public void fillOval(float x, float y, float dx, float dy) {
        engine.fillOval(x+offsetX, y+offsetY, dx, dy);
    }

    public void fillCircle(float x, float y, float r) {
        engine.fillCircle(x+offsetX, y+offsetY, r);
    }

    public void drawRightArrow(float ox, float oy, float w, float h) {
        engine.drawRightArrow(ox+offsetX, oy+offsetY, w, h);
    }

    public void drawUpArrow(float ox, float oy, float w, float h) {
        engine.drawUpArrow(ox+offsetX, oy+offsetY, w, h);
    }

    public void drawDownArrow(float ox, float oy, float w, float h) {
        engine.drawDownArrow(ox+offsetX, oy+offsetY, w, h);
    }

    public void drawString(Font font, String s, float x, float y, int align) {
        engine.drawString(font, s, x+offsetX, y+offsetY, align);
    }

    public void drawSpline(float x0, float y0, float x1, float y1, float startOffset, float endOffset, float flateness, boolean arrow) {
        engine.drawSpline(x0+offsetX, y0+offsetY, x1+offsetX, y1+offsetY, startOffset, endOffset, flateness, arrow);
    }

    public void drawArcConnector(float x0, float y0, float x1, float y1,
                                 float start_offset, float end_offset, float ctrl_offset, float arc_offset,
                                 boolean arrow)
    {
        engine.drawArcConnector(x0+offsetX, y0+offsetY, x1+offsetX, y1+offsetY, start_offset, end_offset, ctrl_offset, arc_offset, arrow);
    }

    public void drawNode(GNode node) {
        skin.drawNode(node);
    }

    public void drawLink(GLink link) {
        skin.drawLink(link);
    }

    public boolean isObjectVisible(GObject object) {
        if(object instanceof GNode)
            return skin.isNodeVisible();

        if(object instanceof GLink)
            return skin.isLinkVisible();

        return false;
    }

    public boolean objectContainsPoint(GObject object, Point p) {
        if(!isObjectVisible(object))
            return false;

        return skin.objectContainsPoint(object, new Point(p.x-offsetX, p.y-offsetY));
    }

}
