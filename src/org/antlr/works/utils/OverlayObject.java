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


package org.antlr.works.utils;

import org.antlr.xjlib.appkit.frame.XJFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public abstract class OverlayObject {

    public static final int DEFAULT_WIDTH = 300;
    public static final int DEFAULT_HEIGHT = 40;

    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_CUSTOM = 2;

    protected XJFrame parentFrame;
    protected JComponent parentComponent;
    protected JComponent content;
    private ComponentAdapter pfcl;
    private ComponentAdapter pccl;
    private MouseAdapter pcml;

    public OverlayObject(XJFrame parentFrame, JComponent parentComponent) {
        this.parentFrame = parentFrame;
        this.parentComponent = parentComponent;

        createKeyBindings();
        content = overlayCreateInterface();
        content.setVisible(false);

        parentFrame.getLayeredPane().add(content, JLayeredPane.MODAL_LAYER);

        createListeners();
    }

    public void close() {
        if(parentFrame.getJavaContainer() != null) {
            parentFrame.getJavaContainer().removeComponentListener(pfcl);
        }
        parentComponent.removeComponentListener(pccl);
        parentComponent.removeMouseListener(pcml);

        parentFrame = null;
        parentComponent = null;
    }

    private void createListeners() {
        parentFrame.getJavaContainer().addComponentListener(pfcl = new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                if(content.isVisible())
                    content.setVisible(false);
            }
        });

        parentComponent.addComponentListener(pccl = new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                if(!isOverlayVisibleInParentComponent()) {
                    hide();
                }

                if(content.isVisible()) {
                    resize();
                }
            }

            public void componentResized(ComponentEvent e) {
                if(!isOverlayVisibleInParentComponent()) {
                    hide();
                }

                if(content.isVisible()) {
                    resize();
                }
            }
        });

        parentComponent.addMouseListener(pcml = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(content.isVisible()) {
                    hide();
                }
            }

            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if(content.isVisible()) {
                    hide();
                }
            }
        });
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

    public boolean isOverlayVisibleInParentComponent() {
        Rectangle vr = SwingUtilities.convertRectangle(parentComponent, parentComponent.getVisibleRect(), parentFrame.getJavaContainer());
        Rectangle cr = SwingUtilities.convertRectangle(parentFrame.getLayeredPane(), content.getBounds(), parentFrame.getJavaContainer());

        return vr.intersects(cr);
    }

    public void hide() {
        if(content.isVisible()) {
            content.setVisible(false);
            parentComponent.requestFocusInWindow();
        }
    }

    public void resize() {
        Rectangle r = parentComponent.getVisibleRect();
        Point p = SwingUtilities.convertPoint(parentComponent, new Point(r.x, r.y), parentFrame.getRootPane());
        int x = 0;
        int y = 0;
        switch(overlayDefaultAlignment()) {
            case ALIGN_CENTER:
                x = p.x+r.width/2-overlayDefaultWidth()/2;
                y = p.y+r.height/2-overlayDefaultHeight()/2;
                break;
            case ALIGN_LEFT:
                x = p.x+5;
                y = p.y+r.height/2-overlayDefaultHeight()/2;
                break;
            case ALIGN_CUSTOM:
                Point cp = overlayCustomPosition();
                if(cp != null) {
                    x = cp.x;
                    y = cp.y;
                }
                break;
        }
        content.setBounds(x, y, overlayDefaultWidth(), overlayDefaultHeight());
    }

    public void display() {
        if(overlayWillDisplay()) {
            resize();
            content.setVisible(true);
        } else {
            content.setVisible(false);
        }
    }

    public boolean isVisible() {
        return content.isVisible();
    }

    public abstract JComponent overlayCreateInterface();
    public abstract boolean overlayWillDisplay();

    public int overlayDefaultWidth() {
        return DEFAULT_WIDTH;
    }

    public int overlayDefaultHeight() {
        return DEFAULT_HEIGHT;
    }

    public int overlayDefaultAlignment() {
        return ALIGN_CENTER;
    }

    public Point overlayCustomPosition() {
        return null;
    }

    public KeyStroke overlayDisplayKeyStroke() {
        return null;
    }

    public String overlayDisplayKeyStrokeMappingName() {
        return null;
    }

}
