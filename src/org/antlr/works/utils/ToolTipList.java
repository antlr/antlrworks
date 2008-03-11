package org.antlr.works.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
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

public class ToolTipList extends JPanel {

    protected static final int VISIBLE_TIPS = 10;
    protected static final Color BACKGROUND_COLOR = new Color(1.0f, 1.0f, 0.6f, 0.8f);

    protected DefaultListModel tipsModel;
    protected JList tipsList;
    protected JScrollPane tipsScrollPane;
    protected ToolTipListDelegate delegate;

    public ToolTipList(ToolTipListDelegate delegate) {
        super(new BorderLayout());

        this.delegate = delegate;

        tipsModel = new DefaultListModel();

        tipsList = new JList(tipsModel);
        tipsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tipsList.setBackground(BACKGROUND_COLOR);
        tipsList.setSelectionForeground(Color.black);
        tipsList.setSelectionBackground(BACKGROUND_COLOR);
        tipsList.setPrototypeCellValue("This is a rule name g");
        tipsList.addKeyListener(new MyListKeyAdapter());
        tipsList.setFocusable(false);

        tipsScrollPane = new JScrollPane(tipsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tipsScrollPane.setFocusable(false);
        tipsScrollPane.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        addMouseListener(new MyMouseAdapter());

        add(tipsScrollPane, BorderLayout.CENTER);
    }

    protected void notifyHide() {
        if(delegate != null)
            delegate.toolTipListHide();
    }

    public void clear() {
        tipsModel.clear();
    }

    public void setText(String text) {
        String[] lines = text.split("\n");
        clear();
        for(int index=0; index<lines.length; index++)
            addLine(lines[index]);
    }
    public void addLine(String text) {
        tipsModel.addElement(text);
    }

    public void selectFirstLine() {
        tipsList.setSelectedIndex(0);
    }

    public void resize() {
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
            Dimension d = new Dimension(width+10, height);
            setSize(d);
            tipsList.setSize(d);
            tipsScrollPane.setSize(d);
        }
    }

    protected class MyMouseAdapter extends MouseAdapter {
        public void mouseEntered(MouseEvent e) {
            notifyHide();
        }

        public void mousePressed(MouseEvent e) {
            notifyHide();
        }
    }

    protected class MyListKeyAdapter extends KeyAdapter {

        public void selectNextListElement(int direction) {
            int index = tipsList.getSelectedIndex();
            index += direction;
            index = Math.min(Math.max(0, index), tipsModel.size()-1);

            tipsList.setSelectedIndex(index);
            tipsList.scrollRectToVisible(tipsList.getCellBounds(index, index));
        }

        public void keyPressed(KeyEvent e) {
            if(e.isConsumed())
                return;

            switch(e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                case KeyEvent.VK_ENTER:
                    notifyHide();
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
