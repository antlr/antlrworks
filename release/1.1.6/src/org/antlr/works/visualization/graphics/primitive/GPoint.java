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

package org.antlr.works.visualization.graphics.primitive;

import org.antlr.works.visualization.graphics.GContext;

public class GPoint {

    public String x = "";
    public String y = "";

    private float cacheX = Float.MIN_VALUE;
    private float cacheY = Float.MIN_VALUE;

    public GPoint() {

    }

    public GPoint(GPoint point) {
        this.x = point.x;
        this.y = point.y;
    }

    public void addX(String x) {
        this.x = GLiteral.add(this.x, x);
    }

    public void addY(String y) {
        this.y = GLiteral.add(this.y, y);
    }

    public void subY(String y) {
        this.y = GLiteral.substract(this.y, y);
    }

    public float getX(GContext context) {
        if(cacheX == Float.MIN_VALUE)
            return context.getPixelValue(x);
        else
            return cacheX;
    }

    public float getY(GContext context) {
        if(cacheY == Float.MIN_VALUE)
            return context.getPixelValue(y);
        else
            return cacheY;
    }

    public void cache(GContext context, float ox, float oy) {
        cacheX = context.getPixelValue(x)+ox;
        cacheY = context.getPixelValue(y)+oy;
    }
}
