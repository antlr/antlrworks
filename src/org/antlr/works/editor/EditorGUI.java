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

package org.antlr.works.editor;

import edu.usfca.xj.appkit.gview.timer.GTimer;
import edu.usfca.xj.appkit.gview.timer.GTimerDelegate;
import edu.usfca.xj.foundation.notification.XJNotificationCenter;
import edu.usfca.xj.foundation.notification.XJNotificationObserver;
import org.antlr.works.dialog.DialogPrefs;
import org.antlr.works.editor.rules.Rules;
import org.antlr.works.editor.swing.EditorStyledDocument;
import org.antlr.works.editor.swing.Gutter;
import org.antlr.works.editor.swing.TextEditorPane;
import org.antlr.works.editor.swing.TextEditorPaneDelegate;
import org.antlr.works.editor.tool.TAutoIndent;
import org.antlr.works.editor.tool.TImmediateColorization;
import org.antlr.works.editor.undo.Undo;
import org.antlr.works.editor.undo.UndoDelegate;
import org.antlr.works.parser.Lexer;
import org.antlr.works.parser.Parser;
import org.antlr.works.parser.Token;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;

public class EditorGUI implements UndoDelegate, XJNotificationObserver, TextEditorPaneDelegate {

    public EditorWindow editor;

    public Gutter gutter;
    public JScrollPane textScrollPane;

    public JScrollPane rulesScrollPane;
    public JTree rulesTree;

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

    public static final String unixEndOfLine = "\n";

    protected TImmediateColorization immediateColorization;
    protected TAutoIndent autoIndent;

    public EditorGUI(EditorWindow editor) {
        this.editor = editor;
    }

    public void createInterface() {
        Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        r.width *= 0.75;
        r.height *= 0.75;
        editor.getRootPane().setPreferredSize(r.getSize());

        createTextPane();

        immediateColorization = new TImmediateColorization(textPane);
        autoIndent = new TAutoIndent(textPane);

        // Set by default the end of line property in order to always use the Unix style
        textPane.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, unixEndOfLine);
        textPane.setDelegate(this);

        gutter = new Gutter(textPane);

        textScrollPane = new JScrollPane(textPane);
        textScrollPane.setWheelScrollingEnabled(true);
        textScrollPane.setRowHeaderView(gutter);

        rulesTree = new JTree() {
            public String getToolTipText(MouseEvent e) {
                TreePath path = getPathForLocation(e.getX(), e.getY());
                if(path == null)
                    return "";

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Rules.RuleTreeNode n = (Rules.RuleTreeNode) node.getUserObject();
                if(n == null)
                    return "";

                Parser.Rule r = n.rule;
                if(r == null || !r.hasErrors())
                    return "";
                else
                    return r.getErrorMessageHTML();
            }
        };
        rulesTree.setBorder(null);
        // Apparently, if I don't set the tooltip here, nothing is displayed (weird)
        rulesTree.setToolTipText("");
        rulesTree.setDragEnabled(true);

        rulesScrollPane = new JScrollPane(rulesTree);
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

        XJNotificationCenter.defaultCenter().addObserver(this, DialogPrefs.NOTIF_PREFS_APPLIED);
    }

    public void close() {
        XJNotificationCenter.defaultCenter().removeObserver(this);
    }

    public void createTextPane() {
        textPane = new TextEditorPane(new EditorStyledDocument());
        textPane.setBackground(Color.white);
        textPane.setBorder(null);

        applyFont();
        textPane.setWordWrap(false);

        textPaneListener = new TextPaneListener();
        textPane.getDocument().addDocumentListener(textPaneListener);

        textPane.addCaretListener(new TextPaneCaretListener());

        textPane.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
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

    public void setAutoIndent(boolean flag) {
        autoIndent.setEnabled(flag);
    }

    public boolean autoIndent() {
        return autoIndent.enabled();
    }

    public void applyFont() {
        textPane.setFont(new Font(EditorPreferences.getEditorFont(), Font.PLAIN, EditorPreferences.getEditorFontSize()));
        applyTab();
    }

    public void applyTab() {
        int tabSize = EditorPreferences.getEditorTabSize();
        int charWidth = EditorPreferences.getEditorFontSize();
        try {
            charWidth = Toolkit.getDefaultToolkit().getFontMetrics(textPane.getFont()).stringWidth("m");
        } catch(Exception e) {
            // ignore exception
        }

        // @todo I have to create a fixed size array - any other way to do that ?
        TabStop[] tstops = new TabStop[100];
        for(int i = 0; i<100; i++) {
            tstops[i] = new TabStop(i*tabSize*charWidth);
        }
        TabSet tabs = new TabSet(tstops);

        Style style = textPane.getLogicalStyle();
        StyleConstants.setTabSet(style, tabs);
        textPane.setLogicalStyle(style);
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
        } catch (BadLocationException e) {
            e.printStackTrace();
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
        updateUndoRedo(undo);
    }

    public void updateUndoRedo(Undo undo) {
        if(editor.editorMenu == null || editor.editorMenu.menuItemUndo == null
            || editor.editorMenu.menuItemRedo == null)
            return;

        editor.editorMenu.menuItemUndo.setTitle("Undo");
        editor.editorMenu.menuItemRedo.setTitle("Redo");

        if(undo == null) {
            editor.editorMenu.menuItemUndo.setEnabled(false);
            editor.editorMenu.menuItemRedo.setEnabled(false);
        } else {
            editor.editorMenu.menuItemUndo.setEnabled(undo.canUndo());
            editor.editorMenu.menuItemRedo.setEnabled(undo.canRedo());

            if(undo.canUndo())
                editor.editorMenu.menuItemUndo.setTitle("Undo "+undo.undoManager.getPresentationName());
            if(undo.canRedo())
                editor.editorMenu.menuItemRedo.setTitle("Redo "+undo.undoManager.getPresentationName());
        }
    }

    public void undoStateDidChange(Undo undo) {
        updateUndoRedo(undo);
    }

    public void notificationFire(Object source, String name) {
        if(name.equals(DialogPrefs.NOTIF_PREFS_APPLIED)) {
            applyFont();
            textScrollPane.repaint();
        }
    }

    public void makeBottomComponentVisible() {
        if(upDownSplitPane.getBottomComponent().getHeight() == 0) {
            upDownSplitPane.setDividerLocation(upDownSplitPane.getLastDividerLocation());
        }
    }

    public void paintTextEditorPane(Graphics g) {
        g.setColor(Color.red);

        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        if(editor.getTokens() == null)
            return;

        for (Iterator iterator = editor.getTokens().iterator(); iterator.hasNext();) {
            Token token = (Token) iterator.next();

            if(token.type != Lexer.TOKEN_ID || token.isAllUpperCase())
                continue;

            if(!editor.rules.isRuleAtIndex(token.getStart()) || editor.rules.isRuleName(token.getAttribute()))
                continue;

            try {
                Rectangle r1 = textPane.modelToView(token.getStart());
                Rectangle r2 = textPane.modelToView(token.getEnd());

//                if(!textPane.getVisibleRect().intersects(r1))
//                    continue;

                int width = r2.x-r1.x;
                int triangle_size = 5;
                for(int triangle=0; triangle<width/triangle_size; triangle++) {
                    int x = r1.x+triangle*triangle_size;
                    int y = r1.y+r1.height-1;
                    g.drawLine(x, y, x+triangle_size/2, y-triangle_size/2);
                    g.drawLine(x+triangle_size/2, y-triangle_size/2, x+triangle_size, y);
                }
            } catch (BadLocationException e) {
            }
        }
    }

    protected class TabMouseListener extends MouseAdapter {

        public void mousePressed(MouseEvent event) {
            if(viewTabbedPane.getSelectedIndex()<4)
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

        public void caretUpdate(CaretEvent e) {
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

        public void changeUpdate(int offset, int length) {
            editor.changeUpdate(offset, length);
        }

        public void insertUpdate(DocumentEvent e) {
            if(!isEnable())
                return;

            immediateColorization.colorize(e.getOffset(), e.getLength());
            autoIndent.indent(e.getOffset(), e.getLength());

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
            // Update the menu only if the event is not temporary. Temporary
            // focus lost can be, for example, when opening a menu on Windows/Linux.
            if(!event.isTemporary())
                updateUndoRedo(null);
        }
    }
}
