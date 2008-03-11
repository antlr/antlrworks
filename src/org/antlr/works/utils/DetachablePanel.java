package org.antlr.works.utils;

import org.antlr.works.prefs.AWPrefs;
import org.antlr.xjlib.appkit.frame.XJDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

public class DetachablePanel extends JPanel {

    protected DetachablePanelDelegate delegate;
    protected JPanel mainPanel;
    protected TitlePanel titlePanel;
    protected String title;

    protected boolean detached = false;
    protected JButton detach;
    protected XJDialog window;
    protected int tag;

    private PropertyChangeListener pcl;
    private ActionListener al;

    public DetachablePanel(String title, DetachablePanelDelegate delegate) {
        super(new BorderLayout());

        this.delegate = delegate;
        this.title = title;

        createTitleBar(title);

        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener(pcl = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    String prop = e.getPropertyName();
                    if(prop.equals("focusOwner") && e.getNewValue() != null) {
                        Component c = (Component)e.getNewValue();
                        if(isParentOf(c))
                            focusGained();
                        else
                            focusLost();
                    }
                }
            }
        );
    }

    public void close() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(pcl);
        detach.removeActionListener(al);
        delegate = null;
        window = null;
    }

    public void createTitleBar(String title) {
        Box box = Box.createHorizontalBox();
        JLabel l = new JLabel(title);
        l.setFont(l.getFont().deriveFont(11.0f));
        l.setForeground(Color.white);
        box.add(Box.createHorizontalStrut(2));
        box.add(l);
        box.add(Box.createHorizontalGlue());
        box.add(detach = createDetachButton());

        titlePanel = new TitlePanel();
        titlePanel.setMinimumSize(new Dimension(0, 15));
        titlePanel.setMaximumSize(new Dimension(0, 15));
        titlePanel.setPreferredSize(new Dimension(0, 15));
        titlePanel.add(box);

        super.add(titlePanel, BorderLayout.NORTH);
    }

    public JButton createDetachButton() {
        JButton detach = new JButton(IconManager.shared().getIconDetach());
        detach.setBorder(null);
        detach.setBorderPainted(false);
        detach.setOpaque(false);
        detach.setFocusable(false);
        detach.setVerticalAlignment(JButton.CENTER);
        detach.setMargin(new Insets(0, 0, 0, 0));

        detach.addActionListener(al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(detached)
                    attach();
                else
                    detach();
            }
        });
        return detach;
    }

    public boolean isParentOf(Component c) {
        Component p = c.getParent();
        return p != null && (p == this || isParentOf(p));
    }

    public void focusGained() {
        titlePanel.setFocused(true);
    }

    public void focusLost() {
        titlePanel.setFocused(false);
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public void setVisible(boolean flag) {
        super.setVisible(flag);
        if(detached)
            window.setVisible(flag);
    }

    public boolean isDetached() {
        return detached;
    }

    private Point previousPosition;
    private Dimension previousSize;

    public void detach() {
        detached = true;

        Point p = getLocationOnScreen();

        delegate.panelDoDetach(this);

        detach.setIcon(IconManager.shared().getIconAttach());

        if(AWPrefs.getDetachableChildren())
            window = new DetachableWindow(delegate.panelParentContainer());
        else
            window = new DetachableWindow(null);
        window.setTitle(title);
        if(previousPosition == null) {
            window.setPosition(p.x, p.y);
        } else {
            window.setPosition(previousPosition);
        }

        if(previousSize == null) {
            window.setSize(getWidth(), getHeight());
        } else {
            window.setSize(previousSize);
        }

        window.getContentPane().add(this);

        window.setVisible(true);
    }

    public void attach() {
        detached = false;
        previousPosition = window.getPosition();
        previousSize = window.getSize();
        window.getContentPane().remove(0);
        window.close();
        detach.setIcon(IconManager.shared().getIconDetach());
        delegate.panelDoAttach(this);
    }

    public static class TitlePanel extends JPanel {

        public boolean focused = false;

        public TitlePanel() {
            super(new BorderLayout());
        }

        public void setFocused(boolean flag) {
            this.focused = flag;
            repaint();
        }

        public void paintComponent(Graphics g) {
            Color startColor;
            Color endColor;
            if(focused) {
                startColor = new Color(0.1f, 0.6f, 0.9f);
                endColor = new Color(0.8f, 0.9f, 1.0f);
            } else {
                startColor = new Color(0.7f, 0.7f, 0.7f);
                endColor = new Color(0.9f, 0.9f, 0.9f);
            }

            int x = getVisibleRect().width;
            int y = getVisibleRect().height;

            GradientPaint gradient = new GradientPaint(0, 0, startColor, x, y, endColor);

            Graphics2D g2d = (Graphics2D)g;
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, x, y);
        }
    }

    public class DetachableWindow extends XJDialog {

        public DetachableWindow(Container container) {
            super(container, false);
        }

        public void dialogWillCloseCancel() {
            if(detached) {
                DetachablePanel.this.setVisible(false);
                delegate.panelDoClose(DetachablePanel.this);
            }
        }
    }
}
