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

import org.antlr.xjlib.foundation.XJXMLSerializable;

import java.awt.*;
import java.awt.geom.Point2D;

public class Vector2D implements XJXMLSerializable {

    public double x = 0;
    public double y = 0;

    public static Vector2D vector(Point p) {
        return new Vector2D(p.x, p.y);
    }

    public static Vector2D vector(Point2D p) {
        return new Vector2D(p.getX(), p.getY());
    }

    public Vector2D() {
    }

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public Vector2D setLength(double l) {
        double ol = length();
        if(ol == 0) {
            x = 0;
            y = 0;
        } else {
            x = x/ol*l;
            y = y/ol*l;
        }
        return this;
    }

    public double length() {
        return Math.sqrt(x*x+y*y);
    }

    public Vector2D stretch(double f) {
        x *= f;
        y *= f;
        return this;
    }

    public Vector2D shift(double dx, double dy) {
        x += dx;
        y += dy;
        return this;
    }

    public Vector2D vectorLength(double l) {
        Vector2D v = this.copy();
        v.setLength(l);
        return v;
    }

    public Vector2D rotate(double degree) {
        if(Math.abs(degree) == 90) {
            double temp = x;
            x = y;
            y = temp;
            // Fixed rotation bug on 11/23/05
            if(degree < 0) y = -y; else x = -x;
        } else {
            double angle = Math.toRadians(degree);

            double rx = Math.cos(angle)*x-Math.sin(angle)*y;
            double ry = Math.sin(angle)*x+Math.cos(angle)*y;

            x = rx;
            y = ry;
        }
        return this;
    }

    public Vector2D normalize() {
        double r = length();
        if(r == 0)
            return new Vector2D();
        else
            return new Vector2D(x/r, y/r);
    }

    public Vector2D append(Vector2D v2) {
        setX(x+v2.x);
        setY(y+v2.y);
        return this;
    }

    public Vector2D add(Vector2D v2) {
        return new Vector2D(x+v2.x, y+v2.y);
    }

    public Vector2D sub(Vector2D v2) {
        return new Vector2D(x-v2.x, y-v2.y);
    }

    public double dot(Vector2D v2) {
        return x*v2.x+y*v2.y;
    }

    public double cross(Vector2D v2) {
        return x*v2.y-y*v2.x;
    }

    public int crossSign(Vector2D v2) {
        double cross = cross(v2);
        if(cross == 0)
            return 0;
        else
            return cross<0?-1:1;
    }

    public Point toPoint() {
        return new Point((int)x, (int)y);
    }

    public boolean equals(Object other) {
        if(other instanceof Vector2D) {
            Vector2D otherVector = (Vector2D) other;
            return otherVector.x == x && otherVector.y == y;
        } else {
            return false;
        }
    }

    public String toString() {
        return "<Vector2D: "+x+", "+y+" >";
    }

    // *** Cloneable

    public Vector2D copy() {
        return new Vector2D(x, y);
    }

}
