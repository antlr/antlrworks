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

import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.stats.Statistics;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.List;

public class AutoCompletionMenu extends OverlayObject {

    private AutoCompletionMenuDelegate delegate;

    private DefaultListModel listModel;
    private JList list;

    private List words;
    private int maxWordLength;

    private int insertionStartIndex;
    private int insertionEndIndex;

    public AutoCompletionMenu(AutoCompletionMenuDelegate delegate, JTextComponent textComponent, JFrame frame) {
        super(frame, textComponent);
        this.delegate = delegate;
    }

    public JTextComponent getTextComponent() {
        return (JTextComponent)parentComponent;
    }

    public JComponent createInterface() {
        getTextComponent().addKeyListener(new MyKeyAdapter());

        listModel = new DefaultListModel();

        list = new JList(listModel) {
            public int getVisibleRowCount() {
                return Math.min(listModel.getSize(), 10);
            }
        };
        list.setFont(new Font(EditorPreferences.getEditorFont(), Font.PLAIN, 12));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(new Color(235, 244, 254));
        list.addKeyListener(new MyKeyAdapter());

        JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        return scrollPane;
    }

    public void displayOverlay() {
        int position = getTextComponent().getCaretPosition();

        int index = getPartialWordBeginsAtPosition(position);
        String partialWord = "";
        if(index<position)
            partialWord = getTextComponent().getText().substring(index+1, position);

        setInsertionStartIndex(index+1);
        setInsertionEndIndex(position);

        List matchingRules = delegate.getMatchingWordsForPartialWord(partialWord);
        if(matchingRules.size() == 0) {
            content.setVisible(false);
            return;
        } else if(matchingRules.size() == 1) {
            content.setVisible(false);
            completePartialWord((String)matchingRules.get(0));
            return;
        }

        showAutoCompleteMenu(index+1, matchingRules, matchingRules);
    }

    public KeyStroke displayOverlayKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK);
    }

    public String displayOverlayKeyStrokeMappingName() {
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

    public boolean isCharIdentifier(char c) {
        if(Character.isLetterOrDigit(c))
            return true;

        if(c == '_')
            return true;

        return false;
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
        completePartialWord((String)words.get(list.getSelectedIndex()));
    }

    public void showAutoCompleteMenu(int index, List names, List words) {
        Rectangle rect = null;

        Statistics.shared().recordEvent(Statistics.EVENT_SHOW_AUTO_COMPLETION_MENU);

        try {
            rect = getTextComponent().getUI().modelToView(getTextComponent(), index);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        if (rect == null)
            return;

        setWordLists(names, words);

        Point p = SwingUtilities.convertPoint(getTextComponent(), getTextComponent().getLocation(), parentFrame.getRootPane());
        content.setBounds(p.x + rect.x-3, p.y + rect.y + rect.height, maxWordLength*8+50, 100);
        content.setVisible(true);
    }

    public void updateAutoCompleteList() {
        if(!content.isVisible())
            return;

        int position = getTextComponent().getCaretPosition();
        int index = getPartialWordBeginsAtPosition(position);
        String partialWord = "";
        if(index<position)
            partialWord = getTextComponent().getText().substring(index+1, position);

        List matchingRules = delegate.getMatchingWordsForPartialWord(partialWord);
        if(matchingRules == null || matchingRules.size() == 0)
            content.setVisible(false);
        else {
            setInsertionEndIndex(position);
            setWordLists(matchingRules, matchingRules);
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
            if (e.isConsumed())
                return;

            if (!content.isVisible())
                return;

            switch(e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_RIGHT:
                    content.setVisible(false);
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
