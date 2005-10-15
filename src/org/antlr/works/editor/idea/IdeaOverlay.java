package org.antlr.works.editor.idea;

import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.swing.OverlayObject;
import org.antlr.works.util.IconManager;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextLayout;
import java.util.Iterator;
import java.util.List;
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

public class IdeaOverlay extends OverlayObject {

    protected static final int VISIBLE_IDEAS = 10;

    protected EditorWindow editor;
    protected DefaultListModel ideasModel;
    protected JList ideasList;
    protected JScrollPane ideasScrollPane;
    protected JToggleButton ideaButton;

    protected List ideas;

    public IdeaOverlay(EditorWindow editor, JFrame parentFrame, JComponent parentComponent) {
        super(parentFrame, parentComponent);
        this.editor = editor;
    }

    public void setIdeas(List ideas) {
        this.ideas = ideas;
    }

    public JComponent overlayCreateInterface() {
        JPanel panel = new JPanel(new BorderLayout());

        ideaButton = new JToggleButton();
        ideaButton.setIcon(IconManager.shared().getIconWarning());
        ideaButton.setBackground(Color.white);
        ideaButton.setFocusable(false);
        ideaButton.addActionListener(new IdeaActionListener());
        ideaButton.setToolTipText("Click to display ideas");

        panel.add(ideaButton, BorderLayout.CENTER);

        ideasModel = new DefaultListModel();

        ideasList = new JList(ideasModel);
        ideasList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ideasList.setBackground(new Color(235, 244, 254));
        ideasList.setPrototypeCellValue("This is a rule name g");
        ideasList.addKeyListener(new ListKeyAdapter());
        ideasList.addMouseListener(new ListMouseAdapter());
        ideasList.addMouseMotionListener(new ListMouseMotionAdapter());

        ideasScrollPane = new JScrollPane(ideasList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ideasScrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        ideasScrollPane.setVisible(false);
        parentFrame.getLayeredPane().add(ideasScrollPane, JLayeredPane.MODAL_LAYER);

        return panel;
    }

    public int overlayDefaultWidth() {
        return 20;
    }

    public int overlayDefaultHeight() {
        return 20;
    }

    public int overlayDefaultAlignment() {
        return ALIGN_CUSTOM;
    }

    public Point overlayCustomPosition() {
        Point lp = editor.getLineTextPositionsAtTextPosition(editor.getCaretPosition());

        int y = 0;
        try {
            y = editor.getTextPane().modelToView(lp.y).y;
        } catch (Exception e) {
            // Ignore
        }

        Point p = SwingUtilities.convertPoint(parentComponent, new Point(0, 0), parentFrame.getRootPane());
        return new Point(p.x + 5, p.y + y);
    }

    public void hide() {
        super.hide();
        ideasScrollPane.setVisible(false);
    }

    public void resize() {
        super.resize();
        resizeIdeas();
    }

    public void resizeIdeas() {
        Rectangle r = content.getBounds();
        int height = ideasList.getFixedCellHeight();
        int size = ideasModel.size();
        if(size > 0) {
            int width = 0;
            for(int i=0; i<ideasModel.size(); i++) {
                IdeaAction action = (IdeaAction)ideasModel.getElementAt(i);
                TextLayout layout = new TextLayout(action.name, ideasList.getFont(), ((Graphics2D)ideasList.getGraphics()).getFontRenderContext());
                width = Math.max(width, (int)layout.getBounds().getWidth());
            }
            height = height*Math.min(VISIBLE_IDEAS, size)+5;
            ideasScrollPane.setBounds(r.x,  r.y+r.height, width+10, height);
        }
    }

    public void updateIdeasList() {
        ideasModel.clear();
        for(Iterator iter = ideas.iterator(); iter.hasNext();) {
            ideasModel.addElement(iter.next());
        }
        ideasList.setSelectedIndex(0);
    }

    public void overlayWillDisplay() {
        updateIdeasList();
        ideaButton.setSelected(false);
    }

    public void applyIdea(int index) {
        IdeaAction action = (IdeaAction)ideas.get(index);
        action.run();
    }

    public class IdeaActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            ideasScrollPane.setVisible(ideaButton.isSelected());
        }
    }

    public class ListMouseMotionAdapter extends MouseMotionAdapter {
        public void mouseMoved(MouseEvent e) {
            ideasList.setSelectedIndex(ideasList.locationToIndex(e.getPoint()));
        }
    }

    public class ListMouseAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            applyIdea(ideasList.getSelectedIndex());
            hide();
        }
    }

    public class ListKeyAdapter extends KeyAdapter {

        public void selectNextListElement(int direction) {
            int index = ideasList.getSelectedIndex();
            index += direction;
            index = Math.min(Math.max(0, index), ideasModel.size()-1);

            ideasList.setSelectedIndex(index);
            ideasList.scrollRectToVisible(ideasList.getCellBounds(index, index));
        }

        public void keyPressed(KeyEvent e) {
            if (e.isConsumed())
                return;

            if (!content.isVisible())
                return;

            switch(e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                    hide();
                    e.consume();
                    break;

                case KeyEvent.VK_ENTER:
                    applyIdea(ideasList.getSelectedIndex());
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
