package org.antlr.xjlib.appkit.swing;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

public class XJTabbedPane extends JPanel {

    private final List<Pane> panes = new ArrayList<Pane>();
    private final JTabbedPane tabbedPane = new JTabbedPane();

    public XJTabbedPane() {
        setLayout(new BorderLayout());
        tabbedPane.addMouseListener(new TabbedPaneMouseListener());        
    }

    public void addComponent(String name, Component c) {
        panes.add(new Pane(name, c));
        if(panes.size() == 1) {
            add(c, BorderLayout.CENTER);
            ChangeEvent e = new ChangeEvent(tabbedPane);            
            for(ChangeListener l : tabbedPane.getChangeListeners()) {
                l.stateChanged(e);
            }
        } else if(panes.size() == 2) {
            removeAll();
            add(tabbedPane, BorderLayout.CENTER);
            tabbedPane.add(panes.get(0).name,  panes.get(0).c);
            tabbedPane.add(name, c);
        } else if(panes.size() > 2) {
            tabbedPane.add(name, c);
        }
    }

    public void removeComponent(Component c) {
        int index = indexOfComponent(c);
        if(index > 0) {
            tabbedPane.remove(c);
            panes.remove(index);
            if(panes.size() == 1) {
                removeAll();
                tabbedPane.removeAll();
                Pane p = panes.get(0);
                add(p.c, BorderLayout.CENTER);

                ChangeEvent e = new ChangeEvent(tabbedPane);
                for(ChangeListener l : tabbedPane.getChangeListeners()) {
                    l.stateChanged(e);
                }
            }
        }
    }

    public void selectComponent(Component c) {
        if(panes.size() > 1) {
            tabbedPane.setSelectedComponent(c);
        }
    }

    public boolean hasComponent(Component component) {
        for(Pane p : panes) {
            if(p.c == component) return true;
        }
        return false;
    }

    public int indexOfComponent(Component component) {
        for (int i = 0; i < panes.size(); i++) {
            if (panes.get(i).c == component) return i;
        }
        return -1;
    }

    public Component getSelectedComponent() {
        if(panes.size() > 1) {
            return tabbedPane.getSelectedComponent();
        } else {
            return panes.get(0).c;
        }
    }

    public int getIndexOfSelectedComponent() {
        return indexOfComponent(getSelectedComponent());
    }

    public void setTitleAt(int index, String name) {
        panes.get(index).name = name;
        if(panes.size() > 1) {
            tabbedPane.setTitleAt(index, name);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        tabbedPane.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        tabbedPane.removeChangeListener(listener);
    }

    private class Pane {
        private String name;
        private Component c;

        private Pane(String name, Component c) {
            this.name = name;
            this.c = c;
        }
    }

    public class TabbedPaneMouseListener extends MouseAdapter {

        public void displayPopUp(MouseEvent event) {
            if(!event.isPopupTrigger())
                return;

            // cannot close root grammar
            if(getIndexOfSelectedComponent() == 0)
                return;

            JPopupMenu popup = new JPopupMenu();
            JMenuItem item = new JMenuItem("Close");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    removeComponent(getSelectedComponent());
                }
            });
            popup.add(item);
            popup.show(event.getComponent(), event.getX(), event.getY());
        }

        @Override
        public void mousePressed(MouseEvent event) {
            displayPopUp(event);
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            displayPopUp(event);
        }
    }


}
