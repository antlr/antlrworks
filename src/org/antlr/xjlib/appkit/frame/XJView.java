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

package org.antlr.xjlib.appkit.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class XJView extends JPanel {

    protected static final BasicStroke strokeNormal = new BasicStroke(1);
    protected static final BasicStroke strokeBold = new BasicStroke(2);

    public XJView() {
        setFocusable(true);

        addMouseMotionListener(new DefaultMouseMotionListener());
        addMouseListener(new DefaultMouseListener());
        addKeyListener(new DefaultKeyListener());
        addFocusListener(new DefaultFocusListener());
    }

    public void handleMousePressed(MouseEvent e) {
    }

    public void handleMouseReleased(MouseEvent e) {
    }

    public void handleMouseDragged(MouseEvent e) {

    }

    public void handleMouseMoved(MouseEvent e) {
    }

    public void handleMouseEntered(MouseEvent e) {
    }

    public void handleMouseExited(MouseEvent e) {
    }

    public void handleKeyPressed(KeyEvent e) {
    }

    public void handleKeyTyped(KeyEvent e) {
    }

    public class DefaultMouseMotionListener extends MouseMotionAdapter {

        public void mouseDragged(MouseEvent e) {
            handleMouseDragged(e);
        }

        public void mouseMoved(MouseEvent e) {
            handleMouseMoved(e);
        }
    }

    public class DefaultMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            handleMousePressed(e);
            requestFocus();
        }

        public void mouseReleased(MouseEvent e) {
            handleMouseReleased(e);
        }

        public void mouseEntered(MouseEvent e) {
            handleMouseEntered(e);
        }

        public void mouseExited(MouseEvent e) {
            handleMouseExited(e);
        }

    }

    public class DefaultKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            handleKeyPressed(e);
        }

        public void keyTyped(KeyEvent e) {
            handleKeyTyped(e);
        }
    }

    public class DefaultFocusListener extends FocusAdapter {

        public void focusGained(FocusEvent e) {
            repaint();
        }

        public void focusLost(FocusEvent e) {
            repaint();
        }
    }

}
