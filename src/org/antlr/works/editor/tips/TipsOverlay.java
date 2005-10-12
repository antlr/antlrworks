package org.antlr.works.editor.tips;

import org.antlr.works.editor.swing.OverlayObject;
import org.antlr.works.editor.EditorWindow;

import javax.swing.*;
import java.util.List;
import java.util.Iterator;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.event.*;
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

public class TipsOverlay extends OverlayObject {

    protected static final int VISIBLE_TIPS = 10;
    protected static final Color BACKGROUND_COLOR = new Color(1.0f, 1.0f, 0.4f, 0.5f);

    protected EditorWindow editor;
    protected DefaultListModel tipsModel;
    protected JList tipsList;
    protected JScrollPane tipsScrollPane;
    protected Point location;

    protected List tips;

    public TipsOverlay(EditorWindow editor, JFrame parentFrame, JComponent parentComponent) {
        super(parentFrame, parentComponent);
        this.editor = editor;
    }

    public void setTips(List tips) {
        this.tips = tips;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public JComponent overlayCreateInterface() {
        JPanel panel = new JPanel(new BorderLayout());

        tipsModel = new DefaultListModel();

        tipsList = new JList(tipsModel);
        tipsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tipsList.setBackground(BACKGROUND_COLOR);
        tipsList.setSelectionBackground(BACKGROUND_COLOR);
        tipsList.setPrototypeCellValue("This is a rule name g");
        tipsList.addKeyListener(new MyListKeyAdapter());
        tipsList.addMouseListener(new MyListMouseAdapter());
        tipsList.setFocusable(false);

        tipsScrollPane = new JScrollPane(tipsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tipsScrollPane.setBackground(BACKGROUND_COLOR);
        tipsScrollPane.setFocusable(false);
        tipsScrollPane.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        panel.add(tipsScrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void resize() {
        resizeTips();
    }

    public void resizeTips() {
        int height = tipsList.getFixedCellHeight();
        int size = tipsModel.size();
        if(size > 0) {
            int width = 0;
            for(int i=0; i<tipsModel.size(); i++) {
                String e = (String)tipsModel.getElementAt(i);
                TextLayout layout = new TextLayout(e, tipsList.getFont(), ((Graphics2D)tipsList.getGraphics()).getFontRenderContext());
                width = Math.max(width, (int)layout.getBounds().getWidth());
            }
            height = height*Math.min(VISIBLE_TIPS, size)+5;
            content.setBounds(location.x,  location.y, width+10, height);
        }
    }

    public void updateTipsList() {
        tipsModel.clear();
        for(Iterator iter = tips.iterator(); iter.hasNext();) {
            tipsModel.addElement(iter.next());
        }
        tipsList.setSelectedIndex(0);
    }

    public void overlayWillDisplay() {
        updateTipsList();
    }

    public class MyListMouseAdapter extends MouseAdapter {
        public void mouseEntered(MouseEvent e) {
            hide();
        }

        public void mousePressed(MouseEvent e) {
            hide();
        }
    }

    public class MyListKeyAdapter extends KeyAdapter {

        public void selectNextListElement(int direction) {
            int index = tipsList.getSelectedIndex();
            index += direction;
            index = Math.min(Math.max(0, index), tipsModel.size()-1);

            tipsList.setSelectedIndex(index);
            tipsList.scrollRectToVisible(tipsList.getCellBounds(index, index));
        }

        public void keyPressed(KeyEvent e) {
            if (e.isConsumed())
                return;

            if (!content.isVisible())
                return;

            switch(e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                case KeyEvent.VK_ENTER:
                    hide();
                    e.consume();
                    break;

                case KeyEvent.VK_UP:
                    selectNextListElement(-1);
                    e.consume();
                    break;

                case KeyEvent.VK_DOWN:
                    selectNextListElement(1);
                    e.consume();
                    break;
            }
        }
    }

}

