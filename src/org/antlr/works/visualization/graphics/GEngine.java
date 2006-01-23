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

import java.awt.*;

public abstract class GEngine {

    protected GContext context;

    public void setContext(GContext context) {
        this.context = context;
    }

    public abstract void setColor(Color color);
    public abstract Color getColor();

    public abstract void setLineWidth(float width);

    public abstract float getStringPixelWidth(Font font, String s);

    public abstract void drawLine(float x0, float y0, float x1, float y1);
    public abstract void drawArc(float x, float y, float w, float h, int a0, int a1);
    public abstract void drawCircle(float x, float y, float r);
    public abstract void drawRect(float x, float y, float dx, float dy);
    public abstract void drawRoundRect(float x, float y, float dx, float dy, float arc_dx, float arc_dy);
    public abstract void drawOval(float x, float y, float dx, float dy);

    public abstract void fillRect(float x, float y, float dx, float dy);
    public abstract void fillOval(float x, float y, float dx, float dy);
    public abstract void fillCircle(float x, float y, float r);

    public abstract void drawRightArrow(float ox, float oy, float w, float h);
    public abstract void drawUpArrow(float ox, float oy, float w, float h);
    public abstract void drawDownArrow(float ox, float oy, float w, float h);
    public abstract void drawString(Font font, String s, float x, float y, int align);
    public abstract void drawSpline(float x0, float y0, float x1, float y1, float startOffset, float endOffset, float flateness, boolean arrow);
    public abstract void drawArcConnector(float x0, float y0, float x1, float y1,
                                 float start_offset, float end_offset, float ctrl_offset, float arc_offset,
                                 boolean arrow);
}
