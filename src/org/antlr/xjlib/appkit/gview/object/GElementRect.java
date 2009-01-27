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

package org.antlr.xjlib.appkit.gview.object;

import org.antlr.xjlib.appkit.gview.base.Anchor2D;
import org.antlr.xjlib.appkit.gview.base.Rect;
import org.antlr.xjlib.appkit.gview.base.Vector2D;
import org.antlr.xjlib.appkit.gview.shape.SLabel;
import org.antlr.xjlib.foundation.XJXMLSerializable;

import java.awt.*;

public class GElementRect extends GElement implements XJXMLSerializable {

    public static final int DEFAULT_WIDTH = 40;
    public static final int DEFAULT_HEIGHT = 40;

    protected double width = DEFAULT_WIDTH;
    protected double height = DEFAULT_HEIGHT;

    public GElementRect() {
    }

    public void setPositionOfUpperLeftCorner(double x, double y) {
        setPosition(x+width*0.5, y+height*0.5);
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
        elementDimensionDidChange();
    }

    public void setWidth(double width) {
        this.width = width;
        elementDimensionDidChange();
    }

    public double getWidth() {
        return width;
    }

    public void setHeight(double height) {
        this.height = height;
        elementDimensionDidChange();
    }

    public double getHeight() {
        return height;
    }

    @Override
    public void updateAnchors() {
        setAnchor(ANCHOR_CENTER, position.copy(), Anchor2D.DIRECTION_FREE);
        setAnchor(ANCHOR_TOP, new Vector2D(0, -height*0.5).append(position), Anchor2D.DIRECTION_TOP);
        setAnchor(ANCHOR_BOTTOM, new Vector2D(0, height*0.5).append(position), Anchor2D.DIRECTION_BOTTOM);
        setAnchor(ANCHOR_LEFT, new Vector2D(-width*0.5, 0).append(position), Anchor2D.DIRECTION_LEFT);
        setAnchor(ANCHOR_RIGHT, new Vector2D(width*0.5, 0).append(position), Anchor2D.DIRECTION_RIGHT);
    }

    @Override
    public Rect getFrame() {
        double x = getPositionX()-getWidth()*0.5;
        double y = getPositionY()-getHeight()*0.5;
        double dx = getWidth();
        double dy = getHeight();
        return new Rect(x, y, dx, dy);
    }

    @Override
    public boolean isInside(Point p) {
        return getFrame().contains(p);
    }

    @Override
    public void draw(Graphics2D g) {
        if(isVisibleInClip(g)) {
            if(labelVisible) {
                g.setColor(labelColor);
                if(label != null && label.length()>0)
                    drawLabel(g);
            }

            g.setColor(color);
            g.setStroke(strokeSize);
            drawShape(g);
            g.setStroke(strokeNormal);
        }
    }

    @Override
    public void drawShape(Graphics2D g) {
        super.drawShape(g);

        Rectangle r = getFrame().rectangle();
        g.drawRect(r.x, r.y, r.width, r.height);
    }

    public void drawLabel(Graphics2D g) {
        SLabel.drawCenteredString(label, (int)getPositionX(), (int)getPositionY(), g);
    }

}
