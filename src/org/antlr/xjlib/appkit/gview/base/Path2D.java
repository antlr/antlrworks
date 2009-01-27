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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Path2D {

    private List<Vector2D> points = new ArrayList<Vector2D>();

    public Path2D() {

    }

    public void clear() {
        points.clear();
    }

    public void add(Vector2D v) {
        points.add(v);
    }

    public void add(double x, double y) {
        add(new Vector2D(x, y));
    }

    public void draw(Graphics g) {
        Vector2D lastV = null;

        Iterator<Vector2D> iterator = points.iterator();
        while(iterator.hasNext()) {
            Vector2D v = iterator.next();
            if(lastV != null)
                g.drawLine((int)lastV.getX(), (int)lastV.getY(), (int)v.getX(), (int)v.getY());

            lastV = v;
        }
    }

    public boolean contains(double x, double y) {
        Vector2D oldv = null;
        Iterator<Vector2D> iterator = points.iterator();
        while(iterator.hasNext()) {
            Vector2D v = iterator.next();
            if(oldv != null) {
                Rect r = new Rect(oldv, v);
                r.inset(-4);
                if(r.contains(x, y))
                    return true;
            }
            oldv = v;
        }
        return false;
    }

    public Vector2D getEndDirection() {
        if(points.size()<2)
            return null;

        Vector2D v1 = points.get(points.size()-2);
        Vector2D v2 = points.get(points.size()-1);
        return v1.sub(v2);
    }
}
