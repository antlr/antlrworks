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

package org.antlr.works.completion;

import edu.usfca.xj.appkit.frame.XJFrameInterface;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.utils.OverlayObject;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;

public class AutoCompletionMenu extends OverlayObject {

    protected AutoCompletionMenuDelegate delegate;

    protected DefaultListModel listModel;
    protected JList list;

    protected List words;
    protected int maxWordLength;

    protected int insertionStartIndex;
    protected int insertionEndIndex;

    protected int displayIndex;

    public static final int VISIBLE_MATCHING_RULES = 15;

    public AutoCompletionMenu(AutoCompletionMenuDelegate delegate, JTextComponent textComponent, XJFrameInterface frame) {
        super(frame, textComponent);
        this.delegate = delegate;
    }

    public JTextComponent getTextComponent() {
        return (JTextComponent)parentComponent;
    }

    public JComponent overlayCreateInterface() {
        getTextComponent().addKeyListener(new MyKeyAdapter());

        listModel = new DefaultListModel();

        list = new JList(listModel) {
            public int getVisibleRowCount() {
                return Math.min(listModel.getSize(), VISIBLE_MATCHING_RULES);
            }
        };
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(new Color(235, 244, 254));
        list.addKeyListener(new MyKeyAdapter());
        list.setPrototypeCellValue("This is a rule name g");

        JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        return scrollPane;
    }

    public boolean overlayWillDisplay() {
        int position = getTextComponent().getCaretPosition();

        int index = getPartialWordBeginsAtPosition(position);
        String partialWord = "";
        if(index < position)
            partialWord = getTextComponent().getText().substring(index+1, position);

        setInsertionStartIndex(index+1);
        setInsertionEndIndex(position);

        List matchingRules = delegate.autoCompletionMenuGetMatchingWordsForPartialWord(partialWord);
        if(matchingRules.size() == 0) {
            return false;
        } else if(matchingRules.size() == 1) {
            completePartialWord((String)matchingRules.get(0));
            return false;
        }

        list.setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, 12));
        setDisplayIndex(index+1);
        setWordLists(matchingRules, matchingRules);

        delegate.autoCompletionMenuWillDisplay();
        return true;
    }

    public KeyStroke overlayDisplayKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK);
    }

    public String overlayDisplayKeyStrokeMappingName() {
        return "controlEspace";
    }

    public void setWordLists(List names, List words) {
        listModel.clear();
        Iterator iterator = names.iterator();
        while(iterator.hasNext())
            listModel.addElement(iterator.next());

        this.words = words;
        maxWordLength = 0;
        for (iterator = words.iterator(); iterator.hasNext();) {
            String word = (String) iterator.next();
            maxWordLength = Math.max(maxWordLength, word.length());
        }
    }

    public void setInsertionStartIndex(int startIndex) {
        insertionStartIndex = startIndex;
    }

    public void setInsertionEndIndex(int endIndex) {
        insertionEndIndex = endIndex;
    }

    public void setDisplayIndex(int index) {
        this.displayIndex = index;
    }

    public boolean isCharIdentifier(char c) {
        if(Character.isLetterOrDigit(c))
            return true;

        return c == '_';
    }

    public int getPartialWordBeginsAtPosition(int pos) {
        String t = getTextComponent().getText();
        int index = pos-1;
        while((index>=0) && isCharIdentifier(t.charAt(index))) {
            index--;
        }
        return index;
    }

    public void completePartialWord(String word) {
        try {
            Document doc = getTextComponent().getDocument();
            doc.remove(insertionStartIndex, insertionEndIndex-insertionStartIndex);
            doc.insertString(insertionStartIndex, word, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void autoComplete() {
        if(list.getSelectedIndex() >= 0)
            completePartialWord((String)words.get(list.getSelectedIndex()));
    }

    public void resize() {
        Rectangle rect = null;

        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_AUTO_COMPLETION_MENU);

        try {
            rect = getTextComponent().getUI().modelToView(getTextComponent(), displayIndex);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        if(rect == null)
            return;

        Point p = SwingUtilities.convertPoint(getTextComponent(), new Point(rect.x, rect.y), parentFrame.getRootPane());
        int height = list.getFixedCellHeight();
        int size = listModel.size();
        if(size > 0) {
            height = height*Math.min(VISIBLE_MATCHING_RULES, size)+5;
            content.setBounds(p.x - 3, p.y + rect.height, maxWordLength*8+50, height);
        }
    }

    public void updateAutoCompleteList() {
        if(!content.isVisible())
            return;

        int position = getTextComponent().getCaretPosition();
        int index = getPartialWordBeginsAtPosition(position);
        String partialWord = "";
        if(index<position)
            partialWord = getTextComponent().getText().substring(index+1, position);

        List matchingRules = delegate.autoCompletionMenuGetMatchingWordsForPartialWord(partialWord);
        if(matchingRules == null || matchingRules.size() == 0) {
            hide();
        } else {
            setInsertionEndIndex(position);
            setWordLists(matchingRules, matchingRules);
            resize();
        }
    }

    public class MyKeyAdapter extends KeyAdapter {

        public void move(int delta) {
            if(listModel.getSize() < 1)
                return;

            int current = list.getSelectedIndex();
            int index = Math.max(0, Math.min(listModel.getSize() - 1, current + delta));
            list.setSelectionInterval(index, index);
            list.scrollRectToVisible(list.getCellBounds(index, index));
        }

        public void keyPressed(KeyEvent e) {
            if(e.isConsumed())
                return;

            if(!content.isVisible())
                return;

            switch(e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_RIGHT:
                    content.setVisible(false);
                    break;

                case KeyEvent.VK_ESCAPE:
                    content.setVisible(false);
                    e.consume();
                    break;

                case KeyEvent.VK_ENTER:
                    autoComplete();
                    content.setVisible(false);
                    e.consume();
                    break;

                case KeyEvent.VK_DOWN:
                    move(1);
                    e.consume();
                    break;

                case KeyEvent.VK_UP:
                    move(-1);
                    e.consume();
                    break;

                case KeyEvent.VK_PAGE_DOWN:
                    move(list.getVisibleRowCount() - 1);
                    e.consume();
                    break;

                case KeyEvent.VK_PAGE_UP:
                    move(-(list.getVisibleRowCount() - 1));
                    e.consume();
                    break;

                case KeyEvent.VK_HOME:
                    move(-listModel.getSize());
                    e.consume();
                    break;

                case KeyEvent.VK_END:
                    move(listModel.getSize());
                    e.consume();
                    break;
            }
        }
    }
}
