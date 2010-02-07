package org.antlr.works.ate;

import org.antlr.works.ate.analysis.ATEAnalysisColumn;
import org.antlr.works.ate.analysis.ATEAnalysisManager;
import org.antlr.works.ate.folding.ATEFoldingManager;
import org.antlr.works.ate.gutter.ATEGutterColumnManager;
import org.antlr.works.ate.swing.ATEAutoIndentation;
import org.antlr.works.ate.swing.ATEKeyBindings;
import org.antlr.works.ate.syntax.generic.ATESyntaxEngine;
import org.antlr.works.ate.syntax.generic.ATESyntaxEngineDelegate;
import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.xjlib.appkit.frame.XJFrame;
import org.antlr.xjlib.appkit.undo.XJUndo;
import org.antlr.xjlib.appkit.utils.XJSmoothScrolling;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
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

public class ATEPanel extends JPanel implements XJSmoothScrolling.ScrollingDelegate, ATESyntaxEngineDelegate {

    protected XJFrame parentFrame;
    protected XJSmoothScrolling smoothScrolling;

    protected ATEPanelDelegate delegate;
    protected ATETextPane textPane;
    protected ATEKeyBindings keyBindings;
    protected ATEGutter gutter;
    protected ATEAnalysisColumn analysisColumn;

    protected ATEGutterColumnManager gutterColumnsManager;

    protected ATEFoldingManager foldingManager;
    protected ATEOverlayManager underlyingManager;
    protected ATEAnalysisManager analysisManager;

    protected ATESyntaxEngine engine;
    protected ATEAutoIndentation autoIndent;

    protected TextPaneListener textPaneListener;

    protected boolean syntaxColoring = false;
    protected int caretPosition;

    protected CaretListener cl;
    protected MouseListener ml;
    protected MouseMotionListener mml;

    protected static final String unixEndOfLine = "\n";
    protected static int ANALYSIS_COLUMN_WIDTH = 18;

    public ATEPanel(XJFrame parentFrame) {
        this(parentFrame, null);
    }

    public ATEPanel(XJFrame parentFrame, StyledEditorKit editorKit) {
        super(new BorderLayout());
        setParentFrame(parentFrame);
        autoIndent = new ATEAutoIndentation(this);
        createTextPane(editorKit);
    }

    public XJFrame getParentFrame() {
        return parentFrame;
    }

    public void setParentFrame(XJFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public void setParserEngine(ATESyntaxEngine engine) {
        this.engine = engine;
        this.engine.setDelegate(this);
        this.engine.refreshColoring();
    }

    public ATESyntaxEngine getParserEngine() {
        return engine;
    }

    public void setDelegate(ATEPanelDelegate delegate) {
        this.delegate = delegate;
    }

    public void setGutterColumnManager(ATEGutterColumnManager columnManager) {
        this.gutterColumnsManager = columnManager;
    }

    public void setFoldingManager(ATEFoldingManager manager) {
        this.foldingManager = manager;
    }

    public void setUnderlyingManager(ATEOverlayManager manager) {
        this.underlyingManager = manager;
    }

    public void setAnalysisManager(ATEAnalysisManager manager) {
        this.analysisManager = manager;
    }

    public ATEAnalysisManager getAnalysisManager() {
        return analysisManager;
    }

    public void setEditable(boolean flag) {
        textPane.setEditable(flag);
        textPane.setWritable(flag);
    }

    public void setAutoIndent(boolean flag) {
        autoIndent.setEnabled(flag);
    }

    public boolean autoIndent() {
        return autoIndent.enabled();
    }

    public void setCaretPosition(int position) {
        setCaretPosition(position, true, false);
    }

    public void setCaretPosition(int position, boolean adjustScroll, boolean animate) {
        int adjustedPosition = Math.min(position, getText().length());
        if(adjustScroll)
            scrollCenterToPosition(adjustedPosition, animate);
        if(!animate)
            textPane.setCaretPosition(adjustedPosition);
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
        return underlyingManager.isUnderlying();
    }

    public void setFoldingEnabled(boolean flag) {
        gutter.setFoldingEnabled(flag);
    }

    public void setLineNumberEnabled(boolean flag) {
        gutter.setLineNumberEnabled(flag);
    }

    public void setEnableRecordChange(boolean flag) {
        if(flag)
            textPaneListener.enable();
        else
            textPaneListener.disable();
    }

    public void scrollCenterToPosition(int position, boolean animate) {
        try {
            Rectangle r = textPane.modelToView(position);
            if(r != null) {
                Rectangle vis = getVisibleRect();
                r.y -= (vis.height / 2);
                r.height = vis.height;
                if(animate) {
                    // Will move the caret after the scrolling
                    // has completed (see smoothScrollingDidComplete()) 
                    caretPosition = position;
                    smoothScrolling.scrollTo(r);
                } else
                    textPane.scrollRectToVisible(r);
            }
        } catch (BadLocationException ble) {
            // ignore
        }
    }

    public void smoothScrollingDidComplete() {
        textPane.setCaretPosition(caretPosition);
    }

    public void setAnalysisColumnVisible(boolean visible) {
        analysisColumn.setVisible(visible);
        if(visible)
            analysisColumn.setPreferredSize(new Dimension(ANALYSIS_COLUMN_WIDTH, 0));
        else
            analysisColumn.setPreferredSize(new Dimension(0, 0));
    }

    public boolean isAnalysisColumnVisible() {
        return analysisColumn.isVisible();
    }

    public void toggleAnalysis() {
        setAnalysisColumnVisible(!isAnalysisColumnVisible());
    }

    public void setSyntaxColoring(boolean flag) {
        this.syntaxColoring = flag;
        textPane.repaint();
    }

    public boolean isSyntaxColoring() {
        return syntaxColoring;
    }

    public ATEKeyBindings getKeyBindings() {
        return keyBindings;
    }

    public void toggleSyntaxColoring() {
        setSyntaxColoring(!isSyntaxColoring());
    }

    public void setEditorKit(StyledEditorKit editorKit) {
        textPane.setEditorKit(editorKit);
        textPane.getDocument().addDocumentListener(textPaneListener = new TextPaneListener());
        // Set by default the end of line property in order to always use the Unix style
        textPane.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, unixEndOfLine);
    }

    public void damage() {
        if(underlyingManager != null) {
            underlyingManager.reset();
        }

        if(gutter != null) {
            gutter.updateSize();
            gutter.revalidate();
            gutter.markDirty();
        }
    }

    public void refresh() {
        damage();

        if(engine != null)
            engine.refreshColoring();

        repaint();
    }

    public void changeOccurred() {
        // Method called only when a change occurred in the document
        // which needs an immediate effect (in this case, the gutter
        // has to be repainted)
        gutter.markDirty();
        parse();
    }

    public int getSelectionStart() {
        return textPane.getSelectionStart();
    }

    public int getSelectionEnd() {
        return textPane.getSelectionEnd();
    }

    public String getSelectedText() {
        return textPane.getSelectedText();
    }

    public List<ATEToken> getTokens() {
        return engine==null?null:engine.getTokens();
    }

    public List<ATELine> getLines() {
        return engine==null?null:engine.getLines();
    }

    public int getCurrentLinePosition() {
        return getLinePositionAtIndex(getCaretPosition());
    }

    public int getLinePositionAtIndex(int index) {
        return getLineIndexAtTextPosition(index) + 1;
    }

    public int getCurrentColumnPosition() {
        return getColumnPositionAtIndex(getCaretPosition());
    }

    public int getColumnPositionAtIndex(int index) {
        int lineIndex = getLineIndexAtTextPosition(index);
        Point linePosition = getLineTextPositionsAtLineIndex(lineIndex);
        if(linePosition == null)
            return 1;
        else
            return getCaretPosition() - linePosition.x + 1;
    }

    public int getLineIndexAtTextPosition(int pos) {
        List<ATELine> lines = getLines();
        if(lines == null)
            return -1;

        for(int i=0; i<lines.size(); i++) {
            ATELine line = lines.get(i);
            if(line.position > pos) {
                return i-1;
            }
        }
        return lines.size()-1;
    }

    public Point getLineTextPositionsAtTextPosition(int pos) {
        return getLineTextPositionsAtLineIndex(getLineIndexAtTextPosition(pos));
    }

    public Point getLineTextPositionsAtLineIndex(int lineIndex) {
        List<ATELine> lines = getLines();
        if(lineIndex == -1 || lines == null)
            return null;

        ATELine startLine = lines.get(lineIndex);
        int start = startLine.position;
        if(lineIndex+1 >= lines.size()) {
            return new Point(start, getTextPane().getDocument().getLength()-1);
        } else {
            ATELine endLine = lines.get(lineIndex+1);
            int end = endLine.position;
            return new Point(start, end-1);
        }
    }

    /** This method is used when loading the text (mostly for the first time):
     * it loads the text and parse it in the current thread in order to speed-up
     * the display time.
     */

    public void loadText(String text) {
        setEnableRecordChange(false);
        try {
            ateEngineBeforeParsing();

            textPane.setText(normalizeText(text));
            if(engine != null)
                engine.processSyntax();

            textPane.setCaretPosition(0);
            textPane.moveCaretPosition(0);
            textPane.getCaret().setSelectionVisible(true);

            ateEngineAfterParsing();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            setEnableRecordChange(true);
        }
    }

    public void setText(String text) {
        Document doc = textPane.getDocument();
        getTextPaneUndo().beginUndoGroup("setText");
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, text, null);
        } catch (BadLocationException e) {
            // ignore
        }
        getTextPaneUndo().endUndoGroup();
    }

    public void insertText(int index, String text) {
        try {
            textPane.getDocument().insertString(index, normalizeText(text), null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void replaceSelectedText(String replace) {
        replaceText(getSelectionStart(), getSelectionEnd(), replace);
    }

    public void replaceText(int start, int end, String text) {
        getTextPaneUndo().beginUndoGroup("replaceText");
        try {
            textPane.getDocument().remove(start, end-start);
            textPane.getDocument().insertString(start, normalizeText(text), null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        getTextPaneUndo().endUndoGroup();
    }

    public static String normalizeText(String text) {
        return text.replaceAll(System.getProperty("line.separator"), "\n");
    }

    public void selectTextRange(int start, int end) {
        textPane.setCaretPosition(start);
        textPane.moveCaretPosition(end);
        textPane.getCaret().setSelectionVisible(true);

        scrollCenterToPosition(start, false);
    }

    public void deselectTextRange() {
        textPane.setCaretPosition(textPane.getCaretPosition());
    }

    public void print() throws PrinterException {
        new ATEPrintUtility().print();
    }

    public void textPaneDidPaint(Graphics g) {
        if(underlyingManager != null)
            underlyingManager.paint(g);
    }

    public void textPaneInvokePopUp(Component component, int x, int y) {
        if(delegate != null)
            delegate.ateInvokePopUp(component, x, y);
    }

    protected void createTextPane(StyledEditorKit editorKit) {
        textPane = new ATETextPane(this, editorKit);
        textPane.setFocusable(true);
        textPane.setBackground(Color.white);
        textPane.setBorder(null);

        textPane.setWordWrap(false);

        textPane.getDocument().addDocumentListener(textPaneListener = new TextPaneListener());
        // Set by default the end of line property in order to always use the Unix style
        textPane.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, unixEndOfLine);

        textPane.addCaretListener(cl = new TextPaneCaretListener());
        textPane.addMouseListener(ml = new TextPaneMouseAdapter());
        textPane.addMouseMotionListener(mml = new TextPaneMouseMotionAdapter());

        smoothScrolling = new XJSmoothScrolling(textPane, this);

        // Gutter
        gutter = new ATEGutter(this);

        // Key bindings
        keyBindings = new ATEKeyBindings(getTextPane());

        // Scroll pane
        JScrollPane textScrollPane = new JScrollPane(textPane);
        textScrollPane.setWheelScrollingEnabled(true);
        textScrollPane.setRowHeaderView(gutter);

        // Analysis column
        analysisColumn = new ATEAnalysisColumn(this);
        analysisColumn.setMinimumSize(new Dimension(ANALYSIS_COLUMN_WIDTH, 0));
        analysisColumn.setMaximumSize(new Dimension(ANALYSIS_COLUMN_WIDTH, Integer.MAX_VALUE));
        analysisColumn.setPreferredSize(new Dimension(ANALYSIS_COLUMN_WIDTH, analysisColumn.getPreferredSize().height));

        Box box = Box.createHorizontalBox();
        box.add(textScrollPane);
        box.add(analysisColumn);

        add(box, BorderLayout.CENTER);
    }

    public ATETextPane getTextPane() {
        return textPane;
    }

    public ATEGutter getGutter() {
        return gutter;
    }

    public void parse() {
        if(engine != null)
            engine.process();
    }

    public String getText() {
        return getTextPane().getText();
    }

    public void ateEngineBeforeParsing() {
        if(delegate != null)
            delegate.ateEngineBeforeParsing();
    }

    public void ateEngineAfterParsing() {
        if(delegate != null)
            delegate.ateEngineAfterParsing();
    }

    public void ateAutoIndent(int offset, int length) {
        if(delegate != null)
            delegate.ateAutoIndent(offset, length);
    }

    public void ateColoringWillColorize() {
        setEnableRecordChange(false);
        disableUndo();
    }

    public void ateColoringDidColorize() {
        setEnableRecordChange(true);
        enableUndo();
    }

    public XJUndo getTextPaneUndo() {
        return parentFrame.getUndo(getTextPane());
    }

    public void disableUndo() {
        XJUndo undo = getTextPaneUndo();
        if(undo != null)
            undo.disableUndo();
    }

    public void enableUndo() {
        XJUndo undo = getTextPaneUndo();
        if(undo != null)
            undo.enableUndo();
    }

    public int getTextIndexAtPosition(int x, int y) {
        return getTextPane().viewToModel(new Point(x, y));
    }

    public void close() {
        textPane.removeCaretListener(cl);
        textPane.removeMouseListener(ml);
        textPane.removeMouseMotionListener(mml);
        
        keyBindings.close();
        textPane.close();
        analysisColumn.close();
        setParentFrame(null);
        setDelegate(null);
    }

    protected class TextPaneCaretListener implements CaretListener {

        public void caretUpdate(CaretEvent e) {
            if(delegate != null)
                delegate.ateCaretUpdate(e.getDot());

            // Each time the cursor moves, update the visible part of the text pane
            // to redraw the highlighting
            if(textPane.highlightCursorLine)
                textPane.repaint();
        }
    }

    protected class TextPaneListener implements DocumentListener {

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
            if(isEnable()) {
                if(delegate != null)
                    delegate.ateChangeUpdate(offset, length, insert);

                if(insert) {
                    autoIndent.indent(offset, length);
                }

                if(gutter != null) {
                    gutter.changeUpdate(offset, length, insert);                    
                }

                changeOccurred();
            }
        }

        /** Key press comes here */
        public void insertUpdate(DocumentEvent e) {
            changeUpdate(e.getOffset(), e.getLength(), true);
        }

        public void removeUpdate(DocumentEvent e) {
            changeUpdate(e.getOffset(), -e.getLength(), false);
        }

        public void changedUpdate(DocumentEvent e) {
        }

    }

    protected class TextPaneMouseAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            // Update the cursor highligthing
            if(textPane.highlightCursorLine)
                textPane.repaint();

            checkForPopupTrigger(e);

            if(delegate != null)
                delegate.ateMousePressed(e.getPoint());
        }

        public void mouseReleased(MouseEvent e) {
            checkForPopupTrigger(e);
        }

        public void checkForPopupTrigger(MouseEvent e) {
            if(e.isPopupTrigger()) {
                int index = textPane.viewToModel(e.getPoint());
                if(textPane.getSelectionStart() != textPane.getSelectionEnd()) {
                    if(index < textPane.getSelectionStart() || index > textPane.getSelectionEnd())
                        setCaretPosition(index, false, false);
                } else if(index != getCaretPosition())
                    setCaretPosition(index, false, false);

                textPaneInvokePopUp(e.getComponent(), e.getX(), e.getY());
            }
        }

        public void mouseExited(MouseEvent e) {
            if(delegate != null)
                delegate.ateMouseExited();
        }
    }

    protected class TextPaneMouseMotionAdapter extends MouseMotionAdapter {
        public void mouseMoved(MouseEvent e) {
            if(delegate != null)
                delegate.ateMouseMoved(e);
        }
    }

    public class ATEPrintUtility implements Printable {

        public ATEPrintUtility() {
        }

        public void print() throws PrinterException {
            PrinterJob printJob = PrinterJob.getPrinterJob();
            printJob.setPrintable(this);
            if (printJob.printDialog()) {
                printJob.print();
            }
        }

        public int print(Graphics g, PageFormat pf, int pageIndex) {
            double pageWidth = pf.getImageableWidth();
            double pageHeight = pf.getImageableHeight();

            double preferredLineWidth = textPane.getUI().getRootView(textPane).getView(0).getPreferredSpan(View.X_AXIS);
            double scale = pageWidth / preferredLineWidth;
            double lineHeight = textPane.getFontMetrics(textPane.getFont()).getHeight() * scale;

            double numberOfLinesPerPage = pageHeight / lineHeight;
            double numberOfLines = textPane.getDocument().getDefaultRootElement().getElementCount();

            int totalNumPages = (int)Math.ceil(numberOfLines / numberOfLinesPerPage);

            if (pageIndex >= totalNumPages) {
                return NO_SUCH_PAGE;
            } else {
                Graphics2D g2 = (Graphics2D) g;
                disableDoubleBuffering(textPane);

                // Offset to the beginning of the printable region
                g2.translate(pf.getImageableX(), pf.getImageableY());

                // Offset to the beginning of the page to render
                double offsety = pageIndex * (int)numberOfLinesPerPage * lineHeight;
                g2.translate(0f, -offsety);

                // Set the clip to draw only a integer number of lines (and not a fraction of it)
                g2.setClip(null);
                g2.clipRect(0, (int)(offsety), (int)Math.floor(pageWidth), (int)Math.floor((int)numberOfLinesPerPage*lineHeight));

                // Apply the scaling to the graphics
                g2.scale(scale, scale);

                textPane.printPaint(g2);

                // debug frame
                //g2.setColor(Color.red);
                //g2.drawRect(0, (int)(offsety/scale), (int)Math.floor(pageWidth/scale), (int)Math.floor((int)numberOfLinesPerPage*lineHeight/scale));

                enableDoubleBuffering(textPane);
                return Printable.PAGE_EXISTS;
            }
        }

        public void disableDoubleBuffering(Component c) {
            RepaintManager currentManager = RepaintManager.currentManager(c);
            currentManager.setDoubleBufferingEnabled(false);
        }

        public void enableDoubleBuffering(Component c) {
            RepaintManager currentManager = RepaintManager.currentManager(c);
            currentManager.setDoubleBufferingEnabled(true);
        }
    }
}
