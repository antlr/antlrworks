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
import org.antlr.works.visualization.serializable.SEncoder;
import org.antlr.works.visualization.serializable.SSerializable;

public class GDimension implements SSerializable {

    public String width = "";
    public String up = "";
    public String down = "";

    private float cacheWidth = Float.MIN_VALUE;
    private float cacheUp = Float.MIN_VALUE;
    private float cacheDown = Float.MIN_VALUE;

    public GDimension() {
    }

    public GDimension(GDimension dimension) {
        this.width = dimension.width;
        this.up = dimension.up;
        this.down = dimension.down;
    }

    public GDimension(String width, String up, String down) {
        this.width = width;
        this.up = up;
        this.down = down;
    }

    public void addWidth(String width) {
        this.width = GLiteral.add(this.width, width);
    }

    public void addUp(String up) {
        this.up = GLiteral.add(this.up, up);
    }

    public void addDown(String down) {
        this.down = GLiteral.add(this.down, down);
    }

    public void maxWidth(String width) {
        this.width = GLiteral.max(this.width, width);
    }

    public void maxUp(String height) {
        this.up = GLiteral.max(this.up, height);
    }

    public void maxDown(String height) {
        this.down = GLiteral.max(this.down, height);
    }

    public float getPixelWidth(GContext context) {
        if(cacheWidth == Float.MIN_VALUE)
            return context.getPixelValue(width);
        else
            return cacheWidth;
    }

    public float getPixelUp(GContext context) {
        if(cacheUp == Float.MIN_VALUE)
            return context.getPixelValue(up);
        else
            return cacheUp;
    }

    public float getPixelDown(GContext context) {
        if(cacheDown == Float.MIN_VALUE)
            return context.getPixelValue(down);
        else
            return cacheDown;
    }

    public float getPixelHeight(GContext context) {
        return getPixelUp(context)+getPixelDown(context);
    }

    public void cache(GContext context) {
        cacheWidth = context.getPixelValue(width);
        cacheUp = context.getPixelValue(up);
        cacheDown = context.getPixelValue(down);
    }

    public String toString() {
        return "{ "+width+", "+up+"|"+down+" }";
    }

    public void encode(SEncoder encoder) {
        encoder.write(width);
        encoder.write(up);
        encoder.write(down);
    }

}
