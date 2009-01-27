package org.antlr.xjlib.appkit.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/*

[The "BSD licence"]
Copyright (c) 2008 Jean Bovet
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

public class XJRotableToggleButton extends JComponent {

    public static final int ROTATE_90 = 1;
    public static final int ROTATE_270 = 2;

    private static final int SEPARATOR = 3;
    private static final int MARGIN_WIDTH = 20;
    private static final int BUTTON_HEIGHT = 22;
    private static final int ARC = 4;

    private int computedWidth = 0;
    private int computedHeight = 0;

    private int rotation = 0;
    private ImageIcon icon;
    private String title;

    private boolean selected;
    private List<ActionListener> actionListeners = new ArrayList<ActionListener>();

    private Object payload;

    public XJRotableToggleButton(String title) {
        this(title, null);
    }

    public XJRotableToggleButton(String title, ImageIcon icon) {
        this.title = title;
        this.icon = icon;

        addMouseListener(new MouseListener() {

            private boolean pressed;
            private Boolean previousSelected;

            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
                if(pressed) {
                    selected = !selected;
                    repaint();
                }
            }

            public void mouseExited(MouseEvent e) {
                if(pressed) {
                    selected = !selected;
                    repaint();
                }
            }

            public void mousePressed(MouseEvent e) {
                if(previousSelected == null) {
                    previousSelected = selected;
                }
                selected = !selected;
                pressed = true;
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                pressed = false;
                if(selected != previousSelected) {
                    triggerAction();
                }
                previousSelected = null;
            }
        });

        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
            }

            public void mouseMoved(MouseEvent e) {
            }
        });
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public Dimension getPreferredSize() {
        if(computedHeight == 0 || computedWidth == 0) {
            computeSize((Graphics2D) getGraphics());
        }
        if(rotation == 0) {
            return new Dimension(computedWidth, computedHeight);
        } else {
            // height and width are inverted because the button is rotated
            return new Dimension(computedHeight, computedWidth);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    private void computeSize(Graphics2D g2d) {
        TextLayout tl = new TextLayout(title, g2d.getFont(), g2d.getFontRenderContext());
        Rectangle2D tb = tl.getBounds();
        computedWidth = 0;
        if(icon != null) {
            computedWidth += icon.getIconWidth();
            computedWidth += SEPARATOR;
        }
        computedWidth += tb.getWidth();
        computedHeight = BUTTON_HEIGHT;

        computedWidth += MARGIN_WIDTH;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform tr = g2d.getTransform();
        switch(rotation) {
            case ROTATE_90:
                tr.rotate(Math.PI/2);
                tr.translate(0, -computedHeight);
                g2d.setTransform(tr);
                break;
            case ROTATE_270:
                tr.rotate(-Math.PI/2);
                tr.translate(-computedWidth, 0);
                g2d.setTransform(tr);
                break;
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        drawAquaBackground(g2d);

        TextLayout layout = new TextLayout(title, g2d.getFont(), g2d.getFontRenderContext());
        Rectangle2D r = layout.getBounds();
        int offset = MARGIN_WIDTH;
        if(icon != null) {
            int voffset = (computedHeight - icon.getIconHeight())/2;
            g2d.drawImage(icon.getImage(), offset, voffset, null);
            offset += icon.getIconWidth();
            offset += SEPARATOR;
        } else {
            offset = (int) ((computedWidth - r.getWidth()) / 2);
        }

        int voffset = (int) ((computedHeight - r.getHeight()) / 2);
        g2d.setColor(Color.black);
        layout.draw(g2d, offset, computedHeight-voffset-layout.getDescent()/2);
    }

    public void addActionListener(ActionListener actionListener) {
        actionListeners.add(actionListener);
    }

    public List<ActionListener> getActionListeners() {
        return actionListeners;
    }

    public void removeActionListener(ActionListener actionListener) {
        actionListeners.remove(actionListener);
    }

    public void removeAllActionListeners() {
        actionListeners.clear();
    }

    public void triggerAction() {
        for(ActionListener al : actionListeners) {
            al.actionPerformed(new ActionEvent(this, 0, null));
        }
    }

    private void drawStandardBackground(Graphics2D g2d) {
        if(selected) {
            g2d.setPaint(new GradientPaint(0, -10, Color.gray, 0, computedHeight +10, Color.white, false));
            g2d.fillRoundRect(1, 0, computedWidth-2, computedHeight-1, ARC, ARC);
        } else {
            g2d.setPaint(new GradientPaint(0, -20, Color.lightGray, 0, computedHeight, Color.white, false));
            g2d.fillRoundRect(1, 0, computedWidth-2, computedHeight-1, ARC, ARC);
        }
        g2d.setColor(Color.black);
        g2d.drawRoundRect(1, 0, computedWidth-2, computedHeight-1, ARC, ARC);
    }

    private void drawAquaBackground(Graphics2D g2d) {
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

        int x = 1;
        int y = 1;
        int width = computedWidth-2;
        int height = computedHeight-2;

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
        g2d.drawRoundRect(x, y, width, height, ARC, ARC);
    }

}
