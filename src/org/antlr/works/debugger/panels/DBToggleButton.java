package org.antlr.works.debugger.panels;

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

public class DBToggleButton extends JToggleButton {

    public int tag;

    public DBToggleButton(String title) {
        super(title);
        setBorderPainted(false);
        setMaximumSize(new Dimension(0, 25));
        setFont(getFont().deriveFont(12f));
    }
    
    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public final int round = 4;

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;

        paintBackground(g2d);

        TextLayout layout = new TextLayout(getText(), g.getFont(), g2d.getFontRenderContext());
        Rectangle2D r = layout.getBounds();

        float tx = (float) (getWidth()*0.5f-r.getWidth()*0.5f);
        float ty = (float) (getHeight()*0.5f+r.getHeight()*0.5f);
        g2d.setColor(Color.black);
        layout.draw(g2d, tx, ty);
    }

    public void paintBackground(Graphics2D g2d) {
        int x = getWidth()-3;
        int y = getHeight()-3;

        if(isSelected())
            g2d.setColor(Color.lightGray);
        else
            g2d.setColor(new Color(0.98f, 0.98f, 0.98f));
        g2d.fillRoundRect(1, 1, x, y, round, round);

        if(isSelected())
            g2d.setColor(Color.darkGray);
        else
            g2d.setColor(Color.gray);
        g2d.drawRoundRect(1, 1, x, y, round, round);
    }
}
