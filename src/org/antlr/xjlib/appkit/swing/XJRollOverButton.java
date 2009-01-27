package org.antlr.xjlib.appkit.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/*

[The "BSD licence"]
Copyright (c) 2005-2008 Jean Bovet
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

public class XJRollOverButton extends JButton {

    private boolean inside = false;

    public static XJRollOverButton createTextButton(String text) {
        XJRollOverButton b = new XJRollOverButton(text);
        return b;
    }

    public static XJRollOverButton createSmallButton(ImageIcon icon) {
        XJRollOverButton b = new XJRollOverButton(icon);
        adjustSize(b, 16, 16);
        return b;
    }

    public static XJRollOverButton createMediumButton(ImageIcon icon) {
        XJRollOverButton b = new XJRollOverButton(icon);
        adjustSize(b, 24, 24);
        return b;
    }

    private static void adjustSize(AbstractButton button, int width, int height) {
        Dimension d = new Dimension(width, height);
        button.setMinimumSize(d);
        button.setMaximumSize(d);
        button.setPreferredSize(d);
    }

    protected XJRollOverButton(ImageIcon icon) {
        super(icon);
        init();
    }

    protected XJRollOverButton(String text) {
        super(text);
        init();
    }

    private void init() {
        setBorderPainted(false);
        setFocusable(false);
        setIconTextGap(0);
        setMargin(new Insets(0, 0, 0, 0));
        setContentAreaFilled(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                inside = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                inside = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if(isEnabled()) {
            if(inside) {
                g.setColor(Color.lightGray);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.darkGray);
                g.drawRect(0, 0, getWidth()-1, getHeight()-1);
            }
        }
        super.paintComponent(g);
    }
    
}
