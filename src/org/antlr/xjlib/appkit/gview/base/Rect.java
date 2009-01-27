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

package org.antlr.xjlib.appkit.gview.base;

import java.awt.*;

public class Rect {

    public Rectangle r;

    public Rect(Rectangle r) {
        this.r = r;
    }

    public Rect(Vector2D c1, Vector2D c2) {
        create(c1, c2, 0, 0);
    }

    public Rect(Vector2D c1, Vector2D c2, double min_dx, double min_dy) {
        create(c1, c2, min_dx, min_dy);
    }

    public void create(Vector2D c1, Vector2D c2, double min_dx, double min_dy) {
        double x = Math.min(c1.getX(), c2.getX());
        double y = Math.min(c1.getY(), c2.getY());
        double dx = Math.max(min_dx, Math.abs(c1.getX()-c2.getX()));
        double dy = Math.max(min_dy, Math.abs(c1.getY()-c2.getY()));
        r = new Rectangle((int)x, (int)y, (int)dx, (int)dy);
    }

    public Rect(double x, double y, double dx, double dy) {
        if(dx<0) {
            x += dx;
            dx = -dx;
        }
        if(dy<0) {
            y += dy;
            dy = -dy;
        }

        r = new Rectangle((int)x, (int)y, (int)dx, (int)dy);
    }

    public Rect union(Rect r) {
        return new Rect((Rectangle)rectangle().createUnion(r.rectangle()));
    }

    public Rectangle rectangle() {
        return r;
    }

    public static boolean intersect(Rect r1, Rect r2) {
        return r1.rectangle().intersects(r2.rectangle());
    }

    public boolean contains(double x, double y) {
        return r.contains(x, y);
    }

    public boolean contains(Point p) {
        return r.contains(p);
    }

    public void inset(double f) {
        r.x += f;
        r.y += f;
        r.width -= 2*f;
        r.height -= 2*f;
    }

    public String toString() {
        if(r == null)
            return super.toString();
        else
            return r.toString();
    }
}
