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

package org.antlr.xjlib.appkit.gview.utils;

import org.antlr.xjlib.appkit.gview.base.Vector2D;

import java.awt.*;

public class GMagnetic {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private int kind = HORIZONTAL;
    private double factor = 0;
    private boolean visible = false;

    public static GMagnetic createHorizontal(double factor) {
        GMagnetic m = new GMagnetic();
        m.kind = HORIZONTAL;
        m.factor = factor;
        return m;
    }

    public static GMagnetic createVertical(double factor) {
        GMagnetic m = new GMagnetic();
        m.kind = VERTICAL;
        m.factor = factor;
        return m;
    }

    public void setVisible(boolean flag) {
        this.visible = flag;
    }

    public boolean isVisible() {
        return visible;
    }

    public void showAndAjust(Vector2D position, Dimension realSize) {
        setVisible(false);

        if(kind == HORIZONTAL) {
            if(Math.abs(position.y-realSize.height*factor)<5) {
                position.y = realSize.height*factor;
                setVisible(true);
            }
        } else {
            if(Math.abs(position.x-realSize.width*factor)<5) {
                position.x = realSize.width*factor;
                setVisible(true);
            }
        }
    }

    public void draw(Graphics2D g2d, Dimension realSize) {
        int middleV = (int)(realSize.height*factor);
        int middleH = (int)(realSize.width*factor);

        if(kind == HORIZONTAL) {
            g2d.drawLine(0, middleV, realSize.width, middleV);
        } else {
            g2d.drawLine(middleH, 0, middleH, realSize.height);
        }

    }
}
