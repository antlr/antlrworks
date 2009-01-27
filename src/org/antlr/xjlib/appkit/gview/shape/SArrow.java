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

package org.antlr.xjlib.appkit.gview.shape;

import org.antlr.xjlib.appkit.gview.base.Vector2D;
import org.antlr.xjlib.foundation.XJXMLSerializable;

import java.awt.*;

public class SArrow implements XJXMLSerializable {

    protected Vector2D anchor = null;
    protected Vector2D direction = null;

    protected double length = 10;
    protected double angle = 45;

    public SArrow() {

    }

    public void setAnchor(double x, double y) {
        anchor = new Vector2D(x, y);
    }

    public void setAnchor(Vector2D anchor) {
        this.anchor = anchor;
    }

    public Vector2D getAnchor() {
        return anchor;
    }

    public void setDirection(Vector2D direction) {
        if(direction != null)
            this.direction = direction.copy();
        else
            this.direction = null;
    }

    public Vector2D getDirection() {
        return direction;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getLength() {
        return length;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngle() {
        return angle;
    }

    public void draw(Graphics g) {
        if(direction == null || anchor == null)
            return;

        Vector2D v = direction.copy();
        v.setLength(length);

        v.rotate(angle*0.5);
        g.drawLine((int)anchor.getX(), (int)anchor.getY(), (int)(anchor.getX()+v.getX()), (int)(anchor.getY()+v.getY()));

        v.rotate(-angle);
        g.drawLine((int)anchor.getX(), (int)anchor.getY(), (int)(anchor.getX()+v.getX()), (int)(anchor.getY()+v.getY()));
    }

}
