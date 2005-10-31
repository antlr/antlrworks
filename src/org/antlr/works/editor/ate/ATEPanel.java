package org.antlr.works.editor.ate;

import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.swing.TextUtils;
import org.antlr.works.editor.analysis.AnalysisColumn;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.text.Element;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.BadLocationException;
import java.awt.*;
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

public class ATEPanel extends JPanel {

    // @todo eliminiate all import to other package (like preferences)

    // @todo refactorize
    public EditorWindow editor;

    // @todo see who is using these fields !!!
    public ATETextPane textPane;
    public ATEGutter gutter;
    public AnalysisColumn analysisColumn;
    public TextPaneListener textPaneListener;

    protected ATEFoldingManager foldingManager;
    protected ATEUnderlyingManager underlyingManager;

    protected boolean isTyping = false;

    public static final String unixEndOfLine = "\n";
    public static int ANALYSIS_COLUMN_WIDTH = 18;

    public ATEPanel(EditorWindow editor) {
        super(new BorderLayout());
        this.editor = editor;
        createTextPane();
    }

    public void setFoldingManager(ATEFoldingManager manager) {
        this.foldingManager = manager;
    }

    public void setUnderlyingManager(ATEUnderlyingManager manager) {
        this.underlyingManager = manager;
    }

    public void setIsTyping(boolean flag) {
        isTyping = flag;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setCaretPosition(int position) {
        textPane.setCaretPosition(position);
    }

    public int getCaretPosition() {
        return textPane.getCaretPosition();
    }

    public void setHighlightCursorLine(boolean flag) {
        textPane.setHighlightCursorLine(flag);
    }

    public void setUnderlying(boolean flag) {
        underlyingManager.setUnderlying(flag);
    }

    public boolean isUnderlying() {
        return underlyingManager.underlying;
    }

    public void toggleAnalysis() {
        analysisColumn.setVisible(!analysisColumn.isVisible());
        if(analysisColumn.isVisible())
            analysisColumn.setPreferredSize(new Dimension(ANALYSIS_COLUMN_WIDTH, 0));
        else
            analysisColumn.setPreferredSize(new Dimension(0, 0));
    }

    public void applyFont() {
        textPane.setFont(new Font(EditorPreferences.getEditorFont(), Font.PLAIN, EditorPreferences.getEditorFontSize()));
        TextUtils.createTabs(textPane);
    }

    public void refresh() {
        underlyingManager.reset();
        repaint();
    }

    public int getSelectionStart() {
        return textPane.getSelectionStart();
    }

    public int getSelectionEnd() {
        return textPane.getSelectionEnd();
    }

    public void insertText(int index, String text) {
        try {
            textPane.getDocument().insertString(index, text, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void replaceSelectedText(String replace) {
        replaceText(getSelectionStart(), getSelectionEnd(), replace);
    }

    public void replaceText(int start, int end, String text) {
        try {
            textPane.getDocument().remove(start, end-start);
            textPane.getDocument().insertString(start, text, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void selectTextRange(int start, int end) {
        textPane.setCaretPosition(start);
        textPane.moveCaretPosition(end);
        textPane.getCaret().setSelectionVisible(true);

        Rectangle r;
        try {
            r = textPane.modelToView(start);
            textPane.scrollRectToVisible(r);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void textPaneDidPaint(Graphics g) {
        underlyingManager.paint(g);
    }

    protected void createTextPane() {
        textPane = new ATETextPane(this);
        textPane.setBackground(Color.white);
        textPane.setBorder(null);

        applyFont();
        textPane.setWordWrap(false);

        textPane.getDocument().addDocumentListener(textPaneListener = new TextPaneListener());
        // Set by default the end of line property in order to always use the Unix style
        textPane.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, unixEndOfLine);

        textPane.addCaretListener(new TextPaneCaretListener());
        textPane.addMouseListener(new TextPaneMouseAdapter());
        textPane.addMouseMotionListener(new TextPaneMouseMotionAdapter());

        // Gutter
        gutter = new ATEGutter(this);
        gutter.setFoldingEnabled(EditorPreferences.getFoldingEnabled());

        // Scroll pane
        JScrollPane textScrollPane = new JScrollPane(textPane);
        textScrollPane.setWheelScrollingEnabled(true);
        textScrollPane.setRowHeaderView(gutter);

        // Analysis column
        analysisColumn = new AnalysisColumn(editor);
        analysisColumn.setMinimumSize(new Dimension(ANALYSIS_COLUMN_WIDTH, 0));
        analysisColumn.setMaximumSize(new Dimension(ANALYSIS_COLUMN_WIDTH, Integer.MAX_VALUE));
        analysisColumn.setPreferredSize(new Dimension(ANALYSIS_COLUMN_WIDTH, analysisColumn.getPreferredSize().height));

        Box box = Box.createHorizontalBox();
        box.add(textScrollPane);
        box.add(analysisColumn);

        add(box, BorderLayout.CENTER);
    }

    protected class TextPaneCaretListener implements CaretListener {

        public void caretUpdate(CaretEvent e) {
            editor.caretUpdate(e.getDot());

            // Each time the cursor moves, update the visible part of the text pane
            // to redraw the highlighting
            if(textPane.highlightCursorLine)
                textPane.repaint();
        }
    }

    // @todo change public to protected and refactor if needed
    public class TextPaneListener implements DocumentListener {

        protected int enable = 0;

        public synchronized void enable() {
            enable--;
        }

        public synchronized void disable() {
            enable++;
        }

        public synchronized boolean isEnable() {
            return enable == 0;
        }

        public void changeUpdate(int offset, int length, boolean insert) {
            editor.changeUpdate(offset, length, insert);
        }

        public void insertUpdate(DocumentEvent e) {
            setIsTyping(true);

            if(isEnable()) {
                changeUpdate(e.getOffset(), e.getLength(), true);
            }
        }

        public void removeUpdate(DocumentEvent e) {
            setIsTyping(true);

            if(isEnable()) {
                changeUpdate(e.getOffset(), -e.getLength(), false);
            }
        }

        public void changedUpdate(DocumentEvent e) {
        }
    }

    protected class TextPaneMouseAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            // Update the cursor highligthing
            if(textPane.highlightCursorLine)
                textPane.repaint();

            // Expand any collapsed rule if the caret
            // has been placed in the placeholder zone
            Element elem = textPane.getStyledDocument().getCharacterElement(getCaretPosition());
            ATEFoldingEntityProxy proxy = textPane.getTopLevelEntityProxy(elem);
            if(proxy != null && !proxy.getEntity().foldingEntityIsExpanded()) {
                textPane.toggleFolding(proxy);
                gutter.markDirty();
            }

            editor.displayIdeas(e.getPoint());
        }

        public void mouseExited(MouseEvent e) {
            if(textPane.hasFocus()) {
                // Do not hide the ideas because
                // otherwise we don't be able to access the idea
                editor.tipsHide();
            }
        }
    }

    protected class TextPaneMouseMotionAdapter extends MouseMotionAdapter {
        public void mouseMoved(MouseEvent e) {
            if(textPane.hasFocus()) {
                Point relativePoint = e.getPoint();
                Point absolutePoint = SwingUtilities.convertPoint(textPane, relativePoint, editor.getJavaContainer());
                editor.displayTips(relativePoint, absolutePoint);
            }
        }
    }

}
