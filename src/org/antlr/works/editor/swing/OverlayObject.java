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


package org.antlr.works.editor.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public abstract class OverlayObject {

    protected JFrame parentFrame;
    protected JComponent parentComponent;
    protected JComponent content;

    public OverlayObject(JFrame parentFrame, JComponent parentComponent) {

        this.parentFrame = parentFrame;
        this.parentComponent = parentComponent;

        parentFrame.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                content.setVisible(false);
            }
        });

        parentComponent.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                resize();
            }

            public void componentResized(ComponentEvent e) {
                resize();
            }
        });

        parentComponent.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (content.isVisible()) {
                    hide();
                }
            }

            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (content.isVisible()) {
                    hide();
                }
            }
        });

        createKeyBindings();
        content = overlayCreateInterface();
        content.setVisible(false);

        parentFrame.getLayeredPane().add(content, JLayeredPane.MODAL_LAYER);
    }

    public void createKeyBindings() {
        if(overlayDisplayKeyStroke() == null)
            return;

        parentComponent.getInputMap().put(overlayDisplayKeyStroke(), overlayDisplayKeyStrokeMappingName());
        parentComponent.getActionMap().put(overlayDisplayKeyStrokeMappingName(), new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                display();
            }
        });
    }

    public void hide() {
        content.setVisible(false);
        parentComponent.requestFocus();
    }

    public void resize() {
        Rectangle r = parentComponent.getVisibleRect();
        Point p = SwingUtilities.convertPoint(parentComponent, new Point(r.x, r.y), parentFrame.getRootPane());
        content.setBounds(p.x+r.width/2-150, p.y+r.height/2-50, 300, 40);
    }

    public void display() {
        overlayWillDisplay();
        resize();
        content.setVisible(true);
    }

    public abstract JComponent overlayCreateInterface();
    public abstract void overlayWillDisplay();
    public KeyStroke overlayDisplayKeyStroke() {
        return null;
    }
    public String overlayDisplayKeyStrokeMappingName() {
        return null;
    }

}
