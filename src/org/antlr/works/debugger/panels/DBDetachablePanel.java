package org.antlr.works.debugger.panels;

import javax.swing.*;
import java.awt.*;
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

public class DBDetachablePanel extends JPanel {

    protected JPanel mainPanel;
    protected TitlePanel titlePanel;

    public DBDetachablePanel(String title) {
        super(new BorderLayout());
        createTitleBar(title);
        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener(
            new PropertyChangeListener() {
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

    public void createTitleBar(String title) {
        Box box = Box.createHorizontalBox();
        JLabel l = new JLabel(title);
        l.setFont(l.getFont().deriveFont(11.0f));
        l.setForeground(Color.white);
        box.add(Box.createHorizontalStrut(2));
        box.add(l);
        titlePanel = new TitlePanel();
        titlePanel.add(box);
        super.add(titlePanel, BorderLayout.NORTH);
    }

    public boolean isParentOf(Component c) {
        Component p = c.getParent();
        if(p != null) {
            if(p == this)
                return true;
            else
                return isParentOf(p);
        }
        return false;
    }

    public void focusGained() {
        titlePanel.setFocused(true);
    }

    public void focusLost() {
        titlePanel.setFocused(false);
    }

    public class TitlePanel extends JPanel {

        public boolean focused = false;

        public TitlePanel() {
            super(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Color.lightGray));
        }

        public void setFocused(boolean flag) {
            this.focused = flag;
            repaint();
        }

        public void paintComponent(Graphics g) {
            Color startColor;
            Color endColor;
            if(focused) {
                startColor = new Color(0.0f, 0.6f, 1.0f);
                endColor = new Color(0.5f, 0.9f, 1.0f);
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
}
