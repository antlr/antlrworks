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

import org.antlr.xjlib.appkit.gview.base.Rect;
import org.antlr.xjlib.appkit.gview.base.Vector2D;
import org.antlr.xjlib.appkit.gview.shape.SArrow;
import org.antlr.xjlib.foundation.XJXMLSerializable;

import java.awt.*;

public class GElementArrow extends GElement implements XJXMLSerializable {

    protected SArrow arrow;
    protected Vector2D target;

    public GElementArrow() {
        setArrow(new SArrow());
    }

    public void setArrow(SArrow arrow) {
        this.arrow = arrow;
    }

    public SArrow getArrow() {
        return arrow;
    }

    public void setArrowLength(double length) {
        arrow.setLength(length);
    }

    public void setSource(double x, double y) {
        super.setPosition(x, y);
    }

    public void setTarget(double x, double y) {
        arrow.setAnchor(x, y);

        target = new Vector2D(x, y);
        Vector2D direction = getPosition().sub(target);
        arrow.setDirection(direction);
    }

    public void setTarget(Vector2D target) {
        this.target = target;
    }

    public Vector2D getTarget() {
        return target;
    }

    @Override
    public void move(double dx, double dy) {
        super.move(dx, dy);
        target.shift(dx, dy);
        arrow.getAnchor().shift(dx, dy);
    }

    @Override
    public Rect getFrame() {
        return new Rect(getPosition(), target, 2, 2);
    }

    @Override
    public boolean isInside(Point p) {
        return getFrame().contains(p);
    }

    @Override
    public void draw(Graphics2D g) {
        if(isVisibleInClip(g)) {
            g.setColor(color);
            drawShape(g);
        }
    }

    @Override
    public void drawShape(Graphics2D g) {
        super.drawShape(g);
        arrow.draw(g);
        g.drawLine((int)getPositionX(), (int)getPositionY(), (int)target.x, (int)target.y);
    }

}
