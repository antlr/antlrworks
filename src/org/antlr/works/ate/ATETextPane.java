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

package org.antlr.works.ate;

import org.antlr.works.ate.swing.ATEEditorKit;
import org.antlr.works.ate.swing.ATEKeyBindings;
import org.antlr.works.ate.swing.ATERenderingView;
import org.antlr.xjlib.appkit.undo.XJUndo;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.AbstractUndoableEdit;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class ATETextPane extends JTextPane
{
    public static final String ATTRIBUTE_CHARACTER_FOLDING_PROXY = "char_folding_proxy";
    public static final String ATTRIBUTE_PARAGRAPH_FOLDING_PROXY = "para_folding_proxy";

    protected ATEPanel textEditor;
    protected ATEKeyBindings keyBindings;

    private boolean writable = true;

    protected boolean wrap = false;
    protected boolean highlightCursorLine = false;
    private int destinationCursorPosition = -1;

    public ATETextPane(ATEPanel textEditor, StyledEditorKit editorKit) {
        super(new DefaultStyledDocument());
        setCaret(new ATECaret());
        setEditorKit(editorKit==null?new ATEEditorKit(textEditor):editorKit);
        this.textEditor = textEditor;
    }

    public void close() {
        setCaret(new DefaultCaret());
        textEditor = null;        
    }

    public ATEKeyBindings getKeyBindings() {
        return keyBindings;
    }

    public void setKeyBindings(ATEKeyBindings keyBindings) {
        this.keyBindings = keyBindings;
    }

    public void setWritable(boolean flag) {
        setEditable(flag);
        this.writable = flag;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWordWrap(boolean flag) {
        this.wrap = flag;
    }

    public boolean getWordWrap() {
        return wrap;
    }

    public void setHighlightCursorLine(boolean flag) {
        this.highlightCursorLine = flag;
    }

    public boolean highlightCursorLine() {
        return highlightCursorLine;
    }

    /** Override setFont() to apply the font to the coloring view
     *
     * @param f The font
     */
    public void setFont(Font f) {
        super.setFont(f);
        ATERenderingView.DEFAULT_FONT = f;
    }

    public void setTabSize(int size) {
        getDocument().putProperty(PlainDocument.tabSizeAttribute, size);
    }

    public boolean getScrollableTracksViewportWidth() {
        if(!wrap) {
            Component parent = getParent();
            return parent == null || getUI().getPreferredSize(this).width < parent.getSize().width;
        } else
            return super.getScrollableTracksViewportWidth();
    }

    public void setBounds(int x, int y, int width, int height) {
        if(!wrap) {
            Dimension size = this.getPreferredSize();
            super.setBounds(x, y,
                    Math.max(size.width, width), Math.max(size.height, height));
        } else {
            super.setBounds(x, y, width, height);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintDestinationCursor(g);
        textEditor.textPaneDidPaint(g);
    }

    private void paintDestinationCursor(Graphics g) {
        if(destinationCursorPosition < 0) return;

        try {
            Rectangle r = modelToView(destinationCursorPosition);
            g.setColor(Color.black);
            g.drawRect(r.x,  r.y, r.width, r.height);
        } catch (BadLocationException e) {
            // Ignore
            e.printStackTrace();
        }
    }

    /**
     * Paints the text area for printing. Make sure the caret and the current line background is not painted.
     *
     * @param g The Graphics context
     */
    public void printPaint(Graphics g) {
        boolean flag = highlightCursorLine();
        setHighlightCursorLine(false);
        boolean caretVisible = getCaret().isVisible();
        getCaret().setVisible(false);
        paint(g);
        getCaret().setVisible(caretVisible);
        setHighlightCursorLine(flag);
    }

    @Override
    protected void processKeyEvent(KeyEvent keyEvent) {
        // If the document is not writable, emits a beep
        if(writable) {
            if(keyEvent.getKeyCode() == KeyEvent.VK_TAB && keyEvent.getID() == KeyEvent.KEY_PRESSED) {
                int start = getSelectionStart();
                int stop = getSelectionEnd();
                if(start != stop) {
                    // Ident the lines covered by the selection
                    try {
                        indentText(start, stop, keyEvent.isShiftDown()?-1:1);
                        keyEvent.consume();
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                } else {
                    super.processKeyEvent(keyEvent);
                }
            } else {
                super.processKeyEvent(keyEvent);
            }
        } else {
            super.processKeyEvent(keyEvent);
        }
    }

    protected void indentText(final int start, final int stop, final int direction) throws BadLocationException {
        final String text = getText();

        final int begin = findBeginningLineBoundary(start);
        int end = findEndLineBoundary(stop);

        // skip the last line if the cursor is located at the beginning of it
        if(stop == findBeginningLineBoundary(stop)) {
            end = findEndLineBoundary(stop-1);
        }

        final StringBuilder modifiedPortion = new StringBuilder();

        int selectionStart = start;
        int selectionEnd = stop;

        boolean modified = false;
        boolean beginLine = true;
        boolean firstLine = true;
        for(int i=begin; i<end; i++) {
            char c = text.charAt(i);
            if(beginLine) {
                // remove/add tab only at the beginning of lines
                beginLine = false;

                boolean ignoreChars = c == '\t' || c == ' ';
                // we are not at the beginning of a line if another character is encountered
                if(!ignoreChars) {
                    beginLine = false;
                }

                if(ignoreChars && direction == -1) {
                    if(firstLine && i < start) {
                        // we are before the beginning of the selection
                        selectionStart--;
                    }
                    selectionEnd--;
                    firstLine = false;

                    // remove tab by simply skipping the append step (continue)
                    modified = true;
                    continue;
                } else if(direction > 0) {
                    // add tab
                    if(firstLine && i <= start) {
                        // we are before the beginning of the selection
                        selectionStart++;
                    }
                    selectionEnd++;
                    firstLine = false;

                    modifiedPortion.append('\t');
                    modified = true;
                }
            } else if(c == '\n') {
                beginLine = true;
            }
            modifiedPortion.append(text.charAt(i));
        }

        // nothing has been modified
        if(!modified) return;

        // create the new text
        final StringBuilder modifiedText = new StringBuilder(text);
        modifiedText.replace(begin, end, modifiedPortion.toString());

        // replace the text with the modified idented text
        textEditor.disableUndo();
        XJUndo undo = textEditor.getTextPaneUndo();
        undo.addEditEvent(new UndoableRefactoringEdit(text, modifiedText.toString()));
        setText(modifiedText.toString());
        textEditor.enableUndo();

        // adjust the selection
        getCaret().setDot(selectionStart);
        getCaret().moveDot(selectionEnd);
    }

    public int findBeginningLineBoundary(int pos) {
        int index = pos-1;
        String s = getText();
        while(index >= 0 && s.charAt(index)!='\n') {
            index--;
        }
        return index +1;
    }

    public int findEndLineBoundary(int pos) {
        int index = pos;
        String s = getText();
        while(index < s.length() && s.charAt(index)!='\n') {
            index++;
        }
        return index+1;
    }

    public int findPrevWordBoundary(int pos) {
        int index = pos-1;
        String s = getText();
        while(index >= 0 && isWordChar(s.charAt(index))) {
            index--;
        }
        return index +1;
    }

    public int findNextWordBoundary(int pos) {
        int index = pos;
        String s = getText();
        while(index < s.length() && isWordChar(s.charAt(index))) {
            index++;
        }
        return index;
    }

    public boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    protected class UndoableRefactoringEdit extends AbstractUndoableEdit {

        public String oldContent;
        public String newContent;

        public UndoableRefactoringEdit(String oldContent, String newContent) {
            this.oldContent = oldContent;
            this.newContent = newContent;
        }

        public void redo() {
            super.redo();
            refactorReplaceEditorText(newContent);
        }

        public void undo() {
            super.undo();
            refactorReplaceEditorText(oldContent);
        }

        private void refactorReplaceEditorText(String text) {
            int old = getCaretPosition();
            textEditor.disableUndo();
            setText(text);
            textEditor.enableUndo();
            setCaretPosition(Math.min(old, text.length()));
        }
    }

    protected class ATECaret extends DefaultCaret {

        public boolean selectingWord = false;
        public boolean selectingLine = false;
        public boolean draggingWord = false;
        public boolean draggingLine = false;
        public boolean startedDragging = false; //this is true after 'mousepressed' and while 'mousedragging'
        public int mouseDraggingOffset;
        public int selectionMovingLineBegin;
        public int selectionMovingLineEnd;
        public int selectionAnchorLineBegin;
        public int selectionAnchorLineEnd;
        public int selectionStart;
        public int selectionEnd;
        public int dragDropCursorPosition = 0;

        public ATECaret() {
            setBlinkRate(500);
        }

        public void paint(Graphics g) {
            if(!isVisible())
                return;
            try {
                Rectangle r = ATETextPane.this.modelToView(getDot());
                g.setColor(ATETextPane.this.getCaretColor());
                g.drawLine(r.x, r.y, r.x, r.y + r.height - 1);
                g.drawLine(r.x+1, r.y, r.x+1, r.y + r.height - 1);
            }
            catch (BadLocationException e) {
                // ignore
            }
        }

        protected synchronized void damage(Rectangle r) {
            if(r == null)
                return;

            x = r.x;
            y = r.y;
            width = 2;
            height = r.height;
            repaint();
        }

        public void mouseClicked(MouseEvent e) {
            // Do not call super if more than one click
            // because it causes the word selection to deselect
            if(e.getClickCount()<2 || !SwingUtilities.isLeftMouseButton(e)){
                super.mouseClicked(e);
            }
        }

        public void mousePressed(MouseEvent e) {
            if(!isWritable() || e.getButton() != MouseEvent.BUTTON1) {
                super.mousePressed(e);
                return;
            }

            draggingWord =false;
            draggingLine =false;
            selectingWord=false;
            selectingLine=false;
            startedDragging=false;

            if(e.getClickCount()%2 == 0) {
                selectWord();
                selectingWord = true;
                selectionStart = getSelectionStart();
                selectionEnd = getSelectionEnd();
                setDragEnabled(false);
                e.consume();
                return;
            }
            if(e.getClickCount()%3 == 0) {
                selectLine();
                selectingLine = true;
                selectionStart = getSelectionStart();
                selectionEnd = getSelectionEnd();
                selectionMovingLineBegin=selectionStart;
                selectionAnchorLineBegin=selectionStart;
                selectionMovingLineEnd=selectionEnd;
                selectionAnchorLineEnd=selectionEnd;
                setDragEnabled(false);
                e.consume();
                return;
            }

            selectionStart = getSelectionStart();
            selectionEnd = getSelectionEnd();
            int mouseCharIndex = viewToModel(e.getPoint());
            if(selectionStart!=selectionEnd&&e.getClickCount()==1&&
                    mouseCharIndex>=selectionStart&&mouseCharIndex<=selectionEnd){
                String s = getText();
                if ((selectionStart==0||s.charAt(selectionStart-1)=='\n')&&s.charAt(selectionEnd-1)=='\n'){
                    draggingLine=true;
                } else {
                    draggingWord=true;
                }
                mouseDraggingOffset=mouseCharIndex-selectionStart;
            }
            // Call super only after handling the double-click otherwise the current
            // caret position will be already moved due to the super() selection.
            if (!draggingWord&&!draggingLine)
                super.mousePressed(e);
        }

        public void mouseReleased(MouseEvent e) {
            if(!isWritable() || e.getButton() != MouseEvent.BUTTON1) {
                super.mouseReleased(e);
                return;
            }

            if (draggingWord||draggingLine){
                //the text had been selected, so let the regular click happen
                endDestinationCursor();
                super.mousePressed(e);
            }
            if (startedDragging){
                super.mousePressed(e);
            }
            if (draggingWord){
                moveSelectionWord(e);
            }else if (draggingLine){
                moveSelectionLine(e);
            }
            //all need to release
            super.mouseReleased(e);
        }

        public void mouseDragged(MouseEvent e) {
            if(!isWritable() || e.getButton() != MouseEvent.BUTTON1) {
                super.mouseDragged(e);
                return;
            }

            //if it's being dragged, paint the selection permanent, and show a cursor where dragging to
            if (draggingWord ||draggingLine){
                dragDropCursorPosition = viewToModel(e.getPoint());
                if (!startedDragging){
                    startedDragging=true;
                    beginDestinationCursor();
                }
            }
            if(selectingWord) {
                extendSelectionWord(e);
                ATETextPane.this.repaint();
            } else if (selectingLine){
                extendSelectionLine(e);
            } else if (draggingWord){
                setDestinationCursorPosition(viewToModel(e.getPoint()));
            } else if (draggingLine){
                int mouseCharIndex = viewToModel(e.getPoint());
                int lineBegin = findBeginningLineBoundary(mouseCharIndex);
                setDestinationCursorPosition(lineBegin);
            } else{
                super.mouseDragged(e);
            }
            // Repaint to avoid leaving trace with the underlying line highlighting
            ATETextPane.this.repaint();
        }

        /**
         * Sets the position of an overlay cursor. It is used mostly to display the destination
         * when a selection is dragged to another location in the text.
         *
         * To disable this cursor, set pos to -1.
         *
         * @param pos Position to show. To hide, use -1
         */
        private void setDestinationCursorPosition(int pos) {
            destinationCursorPosition = pos;
            if(destinationCursorPosition != -1) {
                try {
                    // Make sure to scroll the text in order for the destination cursor to be always visible
                    ATETextPane.this.scrollRectToVisible(modelToView(pos));
                } catch (BadLocationException e) {
                    // ignore
                }
            }
            ATETextPane.this.repaint();
        }

        private void beginDestinationCursor() {
            // Disable Swing d&d to avoid conflict between Swing and our own drag stuff
            setDragEnabled(false);
            setVisible(false);
        }

        private void endDestinationCursor() {
            setDragEnabled(true);
            setVisible(true);
            setDestinationCursorPosition(-1);
        }

        private void moveSelectionLine(MouseEvent e)  {
            int mouseCharIndex = viewToModel(e.getPoint());
            StyledDocument doc = getStyledDocument();
            textEditor.getTextPaneUndo().beginUndoGroup("moveSelectionLine");
            try{
                if(mouseCharIndex > selectionEnd) {
                    int moveTo = findBeginningLineBoundary(mouseCharIndex);
                    int offset = moveTo-selectionEnd;
                    String s = doc.getText(selectionEnd, offset);
                    doc.remove(selectionEnd,offset);
                    doc.insertString(selectionStart,s,null);
                    selectionEnd = selectionEnd+offset;
                    selectionStart = selectionStart+offset;
                }
                else if(mouseCharIndex < selectionStart) {
                    int moveFrom = Math.max(0,findBeginningLineBoundary(mouseCharIndex));
                    int offset = selectionStart - moveFrom;
                    String s = doc.getText(moveFrom, offset);
                    doc.insertString(selectionEnd,s,null);
                    doc.remove(moveFrom,offset);
                    selectionEnd = selectionEnd-offset;
                    selectionStart = selectionStart-offset;
                }
            }catch(BadLocationException ex){System.out.println(ex);}
            textEditor.getTextPaneUndo().endUndoGroup();
        }

        private void moveSelectionWord(MouseEvent e) {
            int mouseCharIndex = viewToModel(e.getPoint());
            StyledDocument doc = getStyledDocument();
            textEditor.getTextPaneUndo().beginUndoGroup("moveSelectionWord");
            try{
                if(mouseCharIndex > selectionEnd) {
                    int offset = mouseCharIndex-selectionEnd;
                    String s = doc.getText(selectionEnd, offset);
                    doc.remove(selectionEnd,offset);
                    doc.insertString(selectionStart,s,null);
                    selectionEnd = selectionEnd+offset;
                    selectionStart = selectionStart+offset;
                }
                else if(mouseCharIndex < selectionStart) {
                    mouseCharIndex = Math.max(0,mouseCharIndex);
                    int offset = selectionStart - mouseCharIndex;
                    String s = doc.getText(mouseCharIndex, offset);
                    doc.insertString(selectionEnd,s,null);
                    doc.remove(mouseCharIndex,offset);
                    selectionEnd = selectionEnd-offset;
                    selectionStart = selectionStart-offset;
                }
            }catch(BadLocationException ex){System.out.println(ex);}
            textEditor.getTextPaneUndo().endUndoGroup();
        }

        public void extendSelectionWord(MouseEvent e) {
            int mouseCharIndex = viewToModel(e.getPoint());

            if(mouseCharIndex > selectionEnd) {
                int npos = findNextWordBoundary(mouseCharIndex);
                if(npos > selectionEnd)
                    select(selectionStart, npos);
            } else if(mouseCharIndex < selectionStart) {
                int npos = findPrevWordBoundary(mouseCharIndex);
                if(npos < selectionStart)
                    select(Math.max(0, npos), selectionEnd);
            } else
                select(selectionStart, selectionEnd);
        }

        public void extendSelectionLine(MouseEvent e) {
            int mouseCharIndex = viewToModel(e.getPoint());
            if(mouseCharIndex > selectionMovingLineEnd) {
                int npos = findEndLineBoundary(mouseCharIndex);
                if(npos > selectionMovingLineEnd){
                    selectionMovingLineEnd = npos;
                    selectionMovingLineBegin = findBeginningLineBoundary(mouseCharIndex);
                    selectionStart = Math.min(selectionMovingLineBegin,selectionAnchorLineBegin);
                    selectionEnd = Math.max(selectionMovingLineEnd,selectionAnchorLineEnd);
                    select(selectionStart, selectionEnd);
                }
            } else if(mouseCharIndex < selectionMovingLineBegin) {
                int npos = Math.max(0,findBeginningLineBoundary(mouseCharIndex));
                if(npos < selectionMovingLineBegin){
                    selectionMovingLineBegin = npos;
                    selectionMovingLineEnd = findEndLineBoundary(mouseCharIndex);
                    selectionEnd = Math.max(selectionMovingLineEnd,selectionAnchorLineEnd);
                    selectionStart = Math.min(selectionMovingLineBegin,selectionAnchorLineBegin);
                    select(selectionStart, selectionEnd);
                }
            } else{ //it's somewhere between the selection, so move the line up or down if needed
                select(selectionStart, selectionEnd);
            }
        }

        public void selectWord() {
            int p = getCaretPosition();

            setCaretPosition(findPrevWordBoundary(p));
            moveCaretPosition(findNextWordBoundary(p));
        }

        public void selectLine() {
            int p = getCaretPosition();

            setCaretPosition(findBeginningLineBoundary(p));
            moveCaretPosition(findEndLineBoundary(p));
        }

    }

}
