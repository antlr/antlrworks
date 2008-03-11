package org.antlr.works.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
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

public class CustomToggleButton extends JToggleButton {

    public int tag;
    public final int round = 4;
    public final int height = 22;
    public final float fontSize = 12f;

    public CustomToggleButton(String title) {
        super(title);
        setBorderPainted(false);
        setMaximumSize(new Dimension(0, height));
        setFont(getFont().deriveFont(fontSize));
    }
    
    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        paintButton((Graphics2D)g, 0, 0, getWidth()-2, getHeight()-1, isSelected());
    }

    public void paintButton(Graphics2D g2d, int x, int y, int width, int height, boolean selected) {
        Color topColor;
        Color middleUpColor;
        Color middleDownColor;
        Color bottomColor;
        final Color snowColor = new Color(0.95f, 0.95f, 0.95f);

        if(selected) {
            topColor = new Color(0.7f, 0.9f, 1.0f);
            middleUpColor = new Color(0.5f, 0.7f, 1.0f);
            middleDownColor = new Color(0.1f, 0.6f, 0.9f);
            bottomColor = new Color(0.8f, 0.9f, 1.0f);
        } else {
            topColor = new Color(0.99f, 0.99f, 0.99f);
            middleUpColor = new Color(0.9f, 0.9f, 0.9f);
            middleDownColor = new Color(0.85f, 0.85f, 0.85f);
            bottomColor = new Color(0.99f, 0.99f, 0.99f);
        }

        GradientPaint gradient = new GradientPaint(x, y, topColor,
                x, y+height/2, middleUpColor);
        g2d.setPaint(gradient);
        g2d.fillRect(x, y, width, height/2);

        g2d.setColor(snowColor);
        g2d.drawLine(x, y+1, x+width, y+1);

        gradient = new GradientPaint(x, y+height/2, middleDownColor,
                x, y+height, bottomColor);
        g2d.setPaint(gradient);
        g2d.fillRect(x, y+height/2, width, height/2);

        if(selected)
            g2d.setColor(Color.darkGray);
        else
            g2d.setColor(Color.gray);
        g2d.drawRoundRect(x, y, width, height, round, round);

        // Label
        TextLayout layout = new TextLayout(getText(), g2d.getFont(), g2d.getFontRenderContext());
        Rectangle2D r = layout.getBounds();

        float tx = (float) (width*0.5f-r.getWidth()*0.5f);
        float ty = (float) (height*0.5f+r.getHeight()*0.5f);
        g2d.setColor(Color.black);
        layout.draw(g2d, tx, ty);
    }

}
