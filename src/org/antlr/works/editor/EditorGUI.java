package org.antlr.works.editor;

import edu.usfca.xj.appkit.gview.timer.GTimer;
import edu.usfca.xj.appkit.gview.timer.GTimerDelegate;
import org.antlr.works.editor.swing.Gutter;
import org.antlr.works.editor.swing.TextEditorPane;
import org.antlr.works.editor.undo.Undo;
import org.antlr.works.editor.undo.UndoDelegate;
import org.antlr.works.parser.Parser;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;

/*

[The "BSD licence"]
Copyright (c) 2004-05 Jean Bovet
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

public class EditorGUI implements UndoDelegate {

    public EditorWindow editor;

    public Gutter gutter;
    public JScrollPane textScrollPane;

    public JScrollPane rulesScrollPane;
    public JTable rulesTable;

    public JTabbedPane viewTabbedPane;
    public JPanel mainPanel;

    public Box infoPanel;
    public ActivityPanel activityPanel;
    public JLabel infoLabel;

    public JSplitPane rulesTextSplitPane;
    public JSplitPane upDownSplitPane;

    public TextEditorPane textPane;
    public TextPaneListener textPaneListener;

    public String lastSelectedRule;

    public EditorGUI(EditorWindow editor) {
        this.editor = editor;
    }

    public void createInterface() {
        editor.getRootPane().setPreferredSize(new Dimension(1024, 700));

        createTextPane();

        gutter = new Gutter(textPane);

        textScrollPane = new JScrollPane(textPane);
        textScrollPane.setWheelScrollingEnabled(true);
        textScrollPane.setRowHeaderView(gutter);

        rulesTable = new JTable();
        rulesTable.setBorder(null);
        rulesTable.setPreferredScrollableViewportSize(new Dimension(200, 0));

        rulesScrollPane = new JScrollPane(rulesTable);
        rulesScrollPane.setBorder(null);
        rulesScrollPane.setPreferredSize(new Dimension(200, 0));
        rulesScrollPane.setWheelScrollingEnabled(true);

        // Assemble

        viewTabbedPane = new JTabbedPane();
        viewTabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
        viewTabbedPane.addMouseListener(new TabMouseListener());

        rulesTextSplitPane = new JSplitPane();
        rulesTextSplitPane.setBorder(null);
        rulesTextSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        rulesTextSplitPane.setLeftComponent(rulesScrollPane);
        rulesTextSplitPane.setRightComponent(textScrollPane);
        rulesTextSplitPane.setContinuousLayout(true);
        rulesTextSplitPane.setPreferredSize(new Dimension(0, 400));
        rulesTextSplitPane.setOneTouchExpandable(true);
        rulesTextSplitPane.setDividerLocation(0.3);

        upDownSplitPane = new JSplitPane();
        upDownSplitPane.setBorder(null);
        upDownSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        upDownSplitPane.add(rulesTextSplitPane, JSplitPane.TOP);
        upDownSplitPane.add(viewTabbedPane, JSplitPane.BOTTOM);
        upDownSplitPane.setContinuousLayout(true);
        upDownSplitPane.setOneTouchExpandable(true);

        infoLabel = new JLabel();

        infoPanel = new Box(BoxLayout.X_AXIS);
        infoPanel.setPreferredSize(new Dimension(0, 30));

        activityPanel = new ActivityPanel();

        infoPanel.add(Box.createHorizontalStrut(5));
        infoPanel.add(activityPanel);
        infoPanel.add(Box.createHorizontalStrut(5));
        infoPanel.add(infoLabel);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(upDownSplitPane, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        editor.getContentPane().add(mainPanel);
        editor.pack();
        upDownSplitPane.setDividerLocation(0.5);
    }

    public void createTextPane() {
        textPane = new TextEditorPane();
        textPane.setBackground(Color.white);
        textPane.setBorder(null);

        textPane.setFont(new Font("Courier", Font.PLAIN, 12));
        textPane.setWordWrap(false);

        textPaneListener = new TextPaneListener();
        textPane.getDocument().addDocumentListener(textPaneListener);

        textPane.addCaretListener(new TextPaneCaretListener());

        textPane.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                // @todo check to see if this works on Windows/Linux

                if(event.getKeyCode() == KeyEvent.VK_C && (event.isMetaDown() || event.isControlDown())) {
                    editor.menuEditActions.performCopyToClipboard();
                    event.consume();
                }

                if(event.getKeyCode() == KeyEvent.VK_X && (event.isMetaDown() || event.isControlDown())) {
                    editor.menuEditActions.performCutToClipboard();
                    event.consume();
                }
            }
        });

        textPane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Dimension d = textPane.getSize();
                d.width = 20;
                gutter.setPreferredSize(d);
            }
        });
    }

    public int getCaretPosition() {
        return textPane.getCaretPosition();
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

        Rectangle r = null;
        try {
            r = textPane.modelToView(start);
            textPane.scrollRectToVisible(r);
        } catch (BadLocationException e1) {
        }
    }

    public void updateInformation() {
        String t;
        int size = editor.parser.getRules().size();
        switch(size) {
            case 0:
                t = "No rules";
                break;
            case 1:
                t = "One rule";
                break;
            default:
                t = size+" rules";
                break;
        }

        int warnings = editor.rules.getNumberOfRulesWithErrors();
        if(warnings > 0)
            t += " ("+warnings+" warnings)";

        infoLabel.setText(t);
    }

    public void registerUndo(Undo undo, JComponent component) {
        component.addFocusListener(new EditorFocusListener());
    }

    public void updateUndoRedo(Object source) {
        Undo undo = editor.getUndo(source);
        if(undo != null) {
            updateUndoRedo(undo);
        }
    }

    public void updateUndoRedo(Undo undo) {
        editor.editorMenu.menuItemUndo.setEnabled(undo.canUndo());
        editor.editorMenu.menuItemRedo.setEnabled(undo.canRedo());

        if(undo.canUndo()) {
            editor.editorMenu.menuItemUndo.setTitle("Undo "+undo.undoManager.getPresentationName());
        } else {
            editor.editorMenu.menuItemUndo.setTitle("Undo");
        }
        if(undo.canRedo()) {
            editor.editorMenu.menuItemRedo.setTitle("Redo "+undo.undoManager.getPresentationName());
        } else {
            editor.editorMenu.menuItemRedo.setTitle("Redo");                        
        }
    }

    public void undoStateDidChange(Undo undo) {
        updateUndoRedo(undo);
    }

    protected class TabMouseListener extends MouseAdapter {

        public void mousePressed(MouseEvent event) {
            if(viewTabbedPane.getSelectedIndex()<3)
                return;

            if(event.isPopupTrigger()) {
                JPopupMenu popup = new JPopupMenu();
                JMenuItem item = new JMenuItem("Close");
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if(viewTabbedPane.getSelectedIndex()<3)
                            return;

                        viewTabbedPane.removeTabAt(viewTabbedPane.getSelectedIndex());
                    }
                });
                popup.add(item);
                popup.show(event.getComponent(), event.getX(), event.getY());
            }
        }
    }

    protected class ActivityPanel extends JPanel implements GTimerDelegate {

        private GTimer timer = new GTimer(this, 500);
        private boolean color = false;
        private boolean activity = false;

        public ActivityPanel() {
            setMaximumSize(new Dimension(12, 12));
            setBorder(BorderFactory.createEtchedBorder());
            updateInfo();
        }

        public void start() {
            activity = true;
            timer.start();
            updateInfo();
        }

        public void stop() {
            timer.stop();
            activity = false;
            updateInfo();
            repaint();
        }

        public void updateInfo() {
            if(activity)
                setToolTipText("Analysis in progress");
            else
                setToolTipText("Analysis completed");
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(activity)
                g.setColor(color?Color.white:Color.yellow);
            else
                g.setColor(Color.green);

            Rectangle r = getBounds();
            g.fillRect(0, 0, r.width, r.height);
        }

        public void timerFired(GTimer timer) {
            color = !color;
            repaint();
        }
    }

    protected class TextPaneCaretListener implements CaretListener {

        public boolean isUpdating = false;

        public void caretUpdate(CaretEvent e) {
            if(isUpdating)
                return;

            isUpdating = true;
            //updateCurrentLineHilite();
            isUpdating = false;

            editor.autoCompletionMenu.updateAutoCompleteList();

            Parser.Rule rule = editor.rules.selectRuleAtPosition(e.getDot());
            if(rule == null || rule.name == null)
                return;

            if(lastSelectedRule == null || !lastSelectedRule.equals(rule.name)) {
                lastSelectedRule = rule.name;
                editor.updateVisualization(false);
            }
        }

    }

    protected class TextPaneListener implements DocumentListener {

        private int enable = 0;

        public synchronized void enable() {
            enable--;
        }

        public synchronized void disable() {
            enable++;
        }

        public synchronized boolean isEnable() {
            return enable == 0;                                                                
        }

        public void changeUpdate(int offset, int length) {
            editor.changeUpdate(offset, length);
        }

        public void insertUpdate(DocumentEvent e) {
            if(!isEnable())
                return;

            changeUpdate(e.getOffset(), e.getLength());
        }

        public void removeUpdate(DocumentEvent e) {
            if(!isEnable())
                return;

            changeUpdate(e.getOffset(), -e.getLength());
        }

        public void changedUpdate(DocumentEvent e) {
        }
    }

    protected class EditorFocusListener implements FocusListener {

        public void focusGained(FocusEvent event) {
            updateUndoRedo(event.getSource());
        }

        public void focusLost(FocusEvent event) {
        }
    }
}
