package org.antlr.works.visualization.graphics;

import java.awt.*;
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

public class GEnginePS extends GEngineGraphics {

    protected StringBuffer ps;

    public GEnginePS() {
        ps = new StringBuffer();
    }

    public String getPSText() {
        return ps.toString();
    }

    public void setColor(Color color) {
    }

    public void setLineWidth(float width) {
        ps.append((int)width);
        ps.append(" setlinewidth");
        ps.append("\n");
    }

    public void drawLine(float x0, float y0, float x1, float y1) {
        ps.append((int)x0);
        ps.append(" ");
        ps.append((int)y0);
        ps.append(" moveto ");
        ps.append((int)x1);
        ps.append(" ");
        ps.append((int)y1);
        ps.append(" lineto");
        ps.append("\n");
    }

    public void drawArc(float x, float y, float w, float h, int a0, int a1) {
    }

    public void drawCircle(float x, float y, float r) {
    }

    public void drawRect(float x, float y, float dx, float dy) {
        ps.append((int)x);
        ps.append(" ");
        ps.append((int)y);
        ps.append(" ");
        ps.append((int)dx);
        ps.append(" ");
        ps.append((int)dy);
        ps.append(" rectstroke");
        ps.append("\n");
    }

    public void drawOval(float x, float y, float dx, float dy) {
    }

    public void fillRect(float x, float y, float dx, float dy) {
        ps.append((int)x);
        ps.append(" ");
        ps.append((int)y);
        ps.append(" ");
        ps.append((int)dx);
        ps.append(" ");
        ps.append((int)dy);
        ps.append(" rectfill");
        ps.append("\n");
    }

    public void fillOval(float x, float y, float dx, float dy) {
    }

    public void fillCircle(float x, float y, float r) {
    }

    public void drawRightArrow(float ox, float oy, float w, float h) {
    }

    public void drawUpArrow(float ox, float oy, float w, float h) {
    }

    public void drawDownArrow(float ox, float oy, float w, float h) {
    }

    public void drawString(Font font, String s, float x, float y, int align) {
    }

    public void drawSpline(float x0, float y0, float x1, float y1, float startOffset, float endOffset, float flateness, boolean arrow) {
    }

    public void drawArcConnector(float x0, float y0, float x1, float y1, float start_offset, float end_offset, float ctrl_offset, float arc_offset, boolean arrow) {
    }

}
