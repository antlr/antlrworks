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

package org.antlr.works.editor.completion;

import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.utils.OverlayObject;
import org.antlr.xjlib.appkit.frame.XJFrame;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.List;

public class AutoCompletionMenu extends OverlayObject {

    protected AutoCompletionMenuDelegate delegate;

    protected DefaultListModel listModel;
    protected JList list;

    protected List<String> words;
    /** Used to store most recently used during autocompletion
     *  the newest should be stored at the front of the list.
     */
    protected static List<String> recentlyUsedWords = new LinkedList<String>();
    protected int maxWordLength;

    protected int insertionStartIndex;
    protected int insertionEndIndex;

    protected int displayIndex;

    public static int visibleMatchingRules = 15;

    public AutoCompletionMenu(AutoCompletionMenuDelegate delegate, JTextComponent textComponent, XJFrame frame) {
        super(frame, textComponent);
        this.delegate = delegate;
    }

    @Override
    public void close() {
        super.close();
        delegate = null;
    }

    public boolean isVStyle() {
        return AWPrefs.isVStyleAutoCompletion();
    }

    public JTextComponent getTextComponent() {
        return (JTextComponent)parentComponent;
    }

    public JComponent overlayCreateInterface() {
        visibleMatchingRules = (isVStyle()?7:15); //if it's on all the time, better if there's less displayed on screen
        getTextComponent().addKeyListener(new MyKeyAdapter());

        listModel = new DefaultListModel();

        list = new JList(listModel) {
            public int getVisibleRowCount() {
                return Math.min(listModel.getSize(), visibleMatchingRules);
            }
        };
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(new Color(235, 244, 254));
        list.addKeyListener(new MyKeyAdapter());
        list.setPrototypeCellValue("This is a rule name g");
        list.addMouseListener(new ListMouseAdapter());

        JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        return scrollPane;
    }

    public boolean overlayWillDisplay() {
        if (isVStyle()){
            //this is vsStyle, so whenever space is pressed, this is run
            int index = getTextComponent().getCaretPosition()+1;//the space hasn't happened yet

            String partialWord = "";
            setInsertionStartIndex(index);
            setInsertionEndIndex(index);

            List<String> matchingRules = delegate.autoCompletionMenuGetMatchingWordsForPartialWord(partialWord);
            if(matchingRules.size() == 0) {   //this'll only happen if there's no rules made yet (since partial word is "")
                return false;
            }

            list.setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, 12));
            list.setAutoscrolls(true);
            setDisplayIndex(index);
            setWordLists(matchingRules, matchingRules);
            delegate.autoCompletionMenuWillDisplay();
            selectMostRecentlyUsedWordPosition(partialWord, matchingRules.get(0));
            return true;
        } else {
            int position = getTextComponent().getCaretPosition();

            int index = getPartialWordBeginsAtPosition(position);
            String partialWord = "";
            if(index < position)
                partialWord = getTextComponent().getText().substring(index+1, position);

            setInsertionStartIndex(index+1);
            setInsertionEndIndex(position);

            List<String> matchingRules = delegate.autoCompletionMenuGetMatchingWordsForPartialWord(partialWord);
            if(matchingRules.size() == 0) {
                return false;
            } else if(matchingRules.size() == 1) {
                completePartialWord(matchingRules.get(0));
                return false;
            }

            list.setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, 12));
            list.setAutoscrolls(true);
            setDisplayIndex(index+1);
            setWordLists(matchingRules, matchingRules);
            delegate.autoCompletionMenuWillDisplay();
            return true;
        }
    }

    public KeyStroke overlayDisplayKeyStroke() {
        if (isVStyle())
            return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0); //whenever space is pressed
        else
            return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK);
    }

    public String overlayDisplayKeyStrokeMappingName() {
        return "controlEspace";
    }

    public void setWordLists(List<String> names, List<String> words) {
        listModel.clear();
        for (String name : names) listModel.addElement(name);

        this.words = words;
        maxWordLength = 0;
        for (String word : words) {
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

    public static boolean isCharIdentifier(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static boolean isAlphaNumericOr_(int keyCode) {
        return (keyCode>=65 && keyCode <=90 || keyCode == 45); //is a-zA-Z or '_'(45)
    }

    private static boolean isFunctionKey(int keyCode) {
        //16-18 (ctrl,shift,alt) 20 is capslock
        return (keyCode >= 16 && keyCode <=18 || keyCode==20);
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
        if(list.getSelectedIndex() >= 0){
            String partialWord = words.get(list.getSelectedIndex());
            if (isVStyle())  {
                recentlyUsedWords.remove(partialWord); //put it at the beginning of the list if it exists
                ((LinkedList)recentlyUsedWords).addFirst(partialWord);
            }
            completePartialWord(partialWord);
        }
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
            height = height*Math.min(visibleMatchingRules, size)+5;
            content.setBounds(p.x - 3, p.y + rect.height, maxWordLength*8+50, height);
        }
    }

    public void selectMostRecentlyUsedWordPosition(String partialWord, String firstWordInList){
        String mostRecentWord="";
        for (String recentlyUsedWord : recentlyUsedWords) {
            if (recentlyUsedWord.toLowerCase().startsWith(partialWord) && words.contains(recentlyUsedWord)) {
                mostRecentWord = recentlyUsedWord;
                break;
            }
        }

        if (mostRecentWord.length()>0){
            list.setSelectedValue(mostRecentWord,true);
        }
        else {
            //don't have any recent words that match, so select first word
            list.setSelectedValue(firstWordInList,true);
        }
        //fix the scrolling:
        //put the word in the middle of the box
        int selectedIndex = list.getSelectedIndex();
        int bottomIndex = Math.max(0,selectedIndex-1);
        int topIndex = Math.min(words.size()-1, selectedIndex+2);
        list.scrollRectToVisible(list.getCellBounds(bottomIndex, topIndex));
    }

    public void updateAutoCompleteList() {
        if(!content.isVisible())
            return;

        int position = getTextComponent().getCaretPosition();
        int index = getPartialWordBeginsAtPosition(position);
        String partialWord = "";
        if(index<position)
            partialWord = getTextComponent().getText().substring(index+1, position);

        List<String> matchingRules = delegate.autoCompletionMenuGetMatchingWordsForPartialWord(partialWord);
        if(matchingRules == null || matchingRules.size() == 0) {
            hide();
        } else {
            setInsertionEndIndex(position);
            if (!isVStyle()) setWordLists(matchingRules, matchingRules);
            selectMostRecentlyUsedWordPosition(partialWord,matchingRules.get(0));
            resize();
        }
    }

    public class ListMouseAdapter extends MouseAdapter {
        public void mouseReleased(MouseEvent e) {
            if (e.isConsumed() || !content.isVisible())
                return;
            autoComplete();
            content.setVisible(false);
            parentComponent.requestFocusInWindow(); //return focus to text
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

            int keyCode = e.getKeyCode();
            if(!content.isVisible())
                return;
            switch(keyCode) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_RIGHT:
                    content.setVisible(false);
                    break;

                case KeyEvent.VK_BACK_SPACE:
                    int position = getTextComponent().getCaretPosition();
                    int index = getPartialWordBeginsAtPosition(position);
                    if (position-1 <= index)
                        content.setVisible(false);
                    //make it so they can backspace out of a word and it closes autocomplete
                    //(it closes if the word they were typing completely deletes)
                    //position-2 : right as they delete the last letter(it hasn't been updated yet)
                    //position-1 : they have deleted the last letter and are now deleting the space
                    //I personally like position-1 better...I think

                    break;
                case KeyEvent.VK_T: //if Ctrl+T is pressed
                case KeyEvent.VK_F: //if Ctrl+F is pressed
                    if (!e.isControlDown()) break;
                    content.setVisible(false);  //don't consume
                    break;

                case KeyEvent.VK_ESCAPE:
                    // it'd be cool to do intellij CTRL+mouse = goto...but it won't work in this class :(
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
                    if (isVStyle()) {content.setVisible(false);break;}   //good.mdiehl: this just gets annoying when autocomplete is always up
                    move(list.getVisibleRowCount() - 1);
                    e.consume();
                    break;

                case KeyEvent.VK_PAGE_UP:
                    if (isVStyle()) {content.setVisible(false);break;}   //good.mdiehl: this just gets annoying when autocomplete is always up
                    move(-(list.getVisibleRowCount() - 1));
                    e.consume();
                    break;

                case KeyEvent.VK_HOME:
                    if (isVStyle()) {content.setVisible(false);break;}   //good.mdiehl: this just gets annoying when autocomplete is always up
                    move(-listModel.getSize());
                    e.consume();
                    break;

                case KeyEvent.VK_END:
                    if (isVStyle()) {content.setVisible(false);break;}   //good.mdiehl: this just gets annoying when autocomplete is always up
                    move(listModel.getSize());
                    e.consume();
                    break;

                default: //good.mdiehl: if they type anything not part of an identifier, close the autocomplete (ctrl,alt,shift are ok)
                    if (!(isAlphaNumericOr_(keyCode) || isFunctionKey(keyCode)) ){
                        //System.out.println(keyCode); //good.mdiehl: find out which key was pressed that is killing autocomplete
                        content.setVisible(false);
                    }
            }
        }

    }
}
