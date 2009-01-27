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

import org.antlr.xjlib.appkit.gview.base.Rect;
import org.antlr.xjlib.appkit.gview.base.Vector2D;
import org.antlr.xjlib.foundation.XJXMLSerializable;

import java.awt.*;

public class SLabel implements XJXMLSerializable {

    protected Vector2D position = null;
    protected String title = null;
    protected Color color = Color.black;
    protected boolean visible = true;

    public SLabel() {

    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(double x, double y) {
        setPosition(new Vector2D(x, y));
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setVisible(boolean flag) {
        this.visible = flag;
    }

    public boolean isVisible() {
        return visible;
    }

    public void draw(Graphics g) {
        if(position == null || title == null || !visible)
            return;

        g.setColor(color);
        SLabel.drawCenteredString(title, (int)position.getX(), (int)position.getY(), g);
    }

    public static void drawCenteredString(String s, double x, double y, Graphics g) {
        if(s != null) {
            FontMetrics fm = g.getFontMetrics();
            int xx = (int)(x-fm.stringWidth(s)*0.5);
            int yy = (int)(y+fm.getHeight()*0.5);
            g.drawString(s, xx, yy);
        }
    }

    public static Rect getFrame(String s, double x, double y, Graphics g) {
        if(s != null && g != null) {
            FontMetrics fm = g.getFontMetrics();
            return new Rect(x-fm.stringWidth(s)*0.5, y-fm.getHeight()*0.5,
                            fm.stringWidth(s), fm.getHeight());
        } else
            return new Rect(0, 0, 0, 0);
    }

}
