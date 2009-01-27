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

public class GElementCircle extends GElement implements XJXMLSerializable {

    public static final int DEFAULT_RADIUS = 20;

    protected double radius = DEFAULT_RADIUS;

    public GElementCircle() {
    }

    public void setRadius(double radius) {
        this.radius = radius;
        elementDimensionDidChange();
    }

    public double getRadius() {
        return radius;
    }

    public void updateAnchors() {
        setAnchor(ANCHOR_CENTER, position, Anchor2D.DIRECTION_FREE);
        setAnchor(ANCHOR_TOP, position.add(new Vector2D(0, -radius)), Anchor2D.DIRECTION_TOP);
        setAnchor(ANCHOR_BOTTOM, position.add(new Vector2D(0, radius)), Anchor2D.DIRECTION_BOTTOM);
        setAnchor(ANCHOR_LEFT, position.add(new Vector2D(-radius, 0)), Anchor2D.DIRECTION_LEFT);
        setAnchor(ANCHOR_RIGHT, position.add(new Vector2D(radius, 0)), Anchor2D.DIRECTION_RIGHT);
    }

    public double getDefaultAnchorOffset(String anchorKey) {
        if(anchorKey != null && anchorKey.equals(ANCHOR_CENTER))
            return radius;
        else
            return 0;
    }

    public Rect getFrame() {
        double x = getPositionX()-radius;
        double y = getPositionY()-radius;
        double dx = radius*2;
        double dy = radius*2;
        return new Rect(x, y, dx, dy);
    }

    public boolean isInside(Point p) {
        return Math.abs(p.getX()-getPositionX())<radius && Math.abs(p.getY()-getPositionY())<radius;
    }

    public void draw(Graphics2D g) {
        if(isVisibleInClip(g)) {
            if(labelVisible) {
                g.setColor(labelColor);
                SLabel.drawCenteredString(getLabel(), (int)getPositionX(), (int)getPositionY(), g);
            }

            if(color != null)
                g.setColor(color);
            else
                g.setColor(Color.black);

            g.setStroke(strokeSize);

            drawShape(g);

            g.setStroke(strokeNormal);
        }
    }

    public void drawShape(Graphics2D g) {
        super.drawShape(g);

        int x = (int)(getPositionX()-radius);
        int y = (int)(getPositionY()-radius);

        g.drawOval(x, y, (int)(radius*2), (int)(radius*2));
    }

}
