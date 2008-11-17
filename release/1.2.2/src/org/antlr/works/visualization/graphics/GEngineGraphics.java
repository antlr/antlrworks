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

import org.antlr.xjlib.appkit.gview.base.Vector2D;
import org.antlr.xjlib.appkit.gview.shape.SLinkArc;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.QuadCurve2D;

public class GEngineGraphics extends GEngine {

    SLinkArc link_arc = new SLinkArc();

    public GEngineGraphics() {
    }

    public Graphics2D getG2D() {
        return context.getGraphics2D();
    }

    public void setColor(Color color) {
        getG2D().setColor(color);
    }

    public Color getColor() {
        return getG2D().getColor();
    }

    public void setLineWidth(float width) {
        getG2D().setStroke(new BasicStroke(width));
    }

    public float getStringPixelWidth(Font font, String s) {
        getG2D().setFont(font);
        TextLayout layout = new TextLayout(s, getG2D().getFont(), getG2D().getFontRenderContext());
        return (float)layout.getBounds().getWidth();
    }

    public void drawLine(float x0, float y0, float x1, float y1) {
        getG2D().drawLine((int)x0, (int)y0, (int)x1, (int)y1);
    }

    public void drawArc(float x, float y, float w, float h, int a0, int a1) {
        getG2D().drawArc((int)x, (int)y, (int)w, (int)h, a0, a1);
    }

    public void drawCircle(float x, float y, float r) {
        getG2D().drawArc((int)(x-r), (int)(y-r), (int)(2*r), (int)(2*r), 0, 360);
    }

    public void drawRect(float x, float y, float dx, float dy) {
        getG2D().drawRect((int)x, (int)y, (int)dx, (int)dy);
    }

    public void drawRoundRect(float x, float y, float dx, float dy, float arc_dx, float arc_dy) {
        getG2D().drawRoundRect((int)x, (int)y, (int)dx, (int)dy, (int)arc_dx, (int)arc_dy);
    }

    public void drawOval(float x, float y, float dx, float dy) {
        getG2D().drawOval((int)x, (int)y, (int)dx, (int)dy);
    }

    public void fillRect(float x, float y, float dx, float dy) {
        getG2D().fillRect((int)x, (int)y, (int)dx, (int)dy);
    }

    public void fillOval(float x, float y, float dx, float dy) {
        getG2D().fillOval((int)x, (int)y, (int)dx, (int)dy);
    }

    public void fillCircle(float x, float y, float r) {
        getG2D().fillArc((int)(x-r), (int)(y-r), (int)(2*r), (int)(2*r), 0, 360);
    }

    public void drawRightArrow(float ox, float oy, float w, float h) {
        int[] x = new int[] { (int)(ox-w), (int)ox, (int)(ox-w) };
        int[] y = new int[] { (int)(oy-h), (int)oy, (int)(oy+h) };

        getG2D().drawPolygon(x, y, 3);
        getG2D().fillPolygon(x, y, 3);
    }

    public void drawUpArrow(float ox, float oy, float w, float h) {
        int[] x = new int[] { (int)(ox-h), (int)ox, (int)(ox+h) };
        int[] y = new int[] { (int)(oy+w), (int)oy, (int)(oy+w) };

        getG2D().drawPolygon(x, y, 3);
        getG2D().fillPolygon(x, y, 3);
    }

    public void drawDownArrow(float ox, float oy, float w, float h) {
        int[] x = new int[] { (int)(ox-h), (int)ox, (int)(ox+h) };
        int[] y = new int[] { (int)(oy-w), (int)oy, (int)(oy-w) };

        getG2D().drawPolygon(x, y, 3);
        getG2D().fillPolygon(x, y, 3);
    }

    public void drawString(Font font, String s, float x, float y, int align) {
        getG2D().setFont(font);
        TextLayout layout = new TextLayout(s, font, getG2D().getFontRenderContext());
        float tx = Float.MIN_VALUE;
        float ty = Float.MIN_VALUE;
        switch(align) {
            case GContext.ALIGN_CENTER:
                tx = (float)(x-layout.getBounds().getWidth()*0.5);
                ty = (float)(y+layout.getBounds().getHeight()*0.5);
                break;
            case GContext.ALIGN_CENTER_UP:
                tx = (float)(x-layout.getBounds().getWidth()*0.5);
                ty = y;
                break;
            case GContext.ALIGN_RIGHT:
                tx = (float)(x-layout.getBounds().getWidth());
                ty = (float)(y+layout.getBounds().getHeight()*0.5);
                break;
            case GContext.ALIGN_LEFT:
                tx = x;
                ty = (float)(y+layout.getBounds().getHeight()*0.5);
                break;
        }
        layout.draw(getG2D(), tx, ty-1);
    }

    public void drawSpline(float x0, float y0, float x1, float y1, float startOffset, float endOffset, float flateness, boolean arrow) {
        link_arc.setStart(x0, y0);
        link_arc.setEnd(x1, y1);
        link_arc.setStartTangentOffset(startOffset);
        link_arc.setEndTangentOffset(endOffset);
        link_arc.setFlateness(flateness);
        link_arc.setArrowVisible(arrow);
        link_arc.update();
        link_arc.setColor(getG2D().getColor());
        link_arc.draw(getG2D());
    }

    public void drawArcConnector(float x0, float y0, float x1, float y1,
                                 float start_offset, float end_offset,
                                 float ctrl_offset, float arc_offset,
                                 boolean arrow)
    {
        if(x0>x1) {
            ctrl_offset *= -1;
            arc_offset *= -1;
        }

        float cx = x1-ctrl_offset;
        float ax0 = cx-arc_offset;

        Vector2D a = new Vector2D(cx, y0);
        Vector2D b = new Vector2D(x1, y1);
        Vector2D z = a.add(b.sub(a).setLength(Math.abs(arc_offset)));

        drawLine(x0+start_offset, y0, ax0, y0);
        drawSpline((float)z.x, (float)z.y, x1, y1, 0, end_offset, 0, arrow);

        QuadCurve2D.Float quad = new QuadCurve2D.Float();
        quad.setCurve(ax0, y0, cx, y0, (float)z.x, (float)z.y);
        getG2D().draw(quad);
    }


}
