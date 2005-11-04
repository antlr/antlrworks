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

import edu.usfca.xj.appkit.swing.XJTree;
import edu.usfca.xj.foundation.notification.XJNotificationCenter;
import edu.usfca.xj.foundation.notification.XJNotificationObserver;
import edu.usfca.xj.foundation.XJSystem;
import org.antlr.works.dialog.DialogPrefs;
import org.antlr.works.editor.ate.ATEPanel;
import org.antlr.works.editor.rules.Rules;
import org.antlr.works.editor.swing.TextUtils;
import org.antlr.works.editor.tool.TAutoIndent;
import org.antlr.works.editor.tool.TImmediateColorization;
import org.antlr.works.editor.undo.Undo;
import org.antlr.works.editor.undo.UndoDelegate;
import org.antlr.works.parser.ParserRule;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;

public class EditorGUI implements UndoDelegate, XJNotificationObserver {

    public EditorWindow editor;

    public JScrollPane rulesScrollPane;
    public XJTree rulesTree;

    public JTabbedPane viewTabbedPane;
    public JPanel mainPanel;

    public Box infoPanel;
    public JLabel infoLabel;
    public JLabel cursorLabel;
    public JLabel scmLabel;

    public JSplitPane rulesTextSplitPane;
    public JSplitPane upDownSplitPane;

    public EditorToolbar toolbar;
    public ATEPanel textEditor;

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

        textEditor = new ATEPanel(editor.getJFrame());
        textEditor.setDelegate(editor);
        textEditor.setFoldingEnabled(EditorPreferences.getFoldingEnabled());
        textEditor.setHighlightCursorLine(EditorPreferences.getHighlightCursorEnabled());
        applyFont();
        
        immediateColorization = new TImmediateColorization(textEditor.getTextPane());
        autoIndent = new TAutoIndent(textEditor.getTextPane());

        rulesTree = new XJTree() {
            public String getToolTipText(MouseEvent e) {
                TreePath path = getPathForLocation(e.getX(), e.getY());
                if(path == null)
                    return "";

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Rules.RuleTreeUserObject n = (Rules.RuleTreeUserObject) node.getUserObject();
                if(n == null)
                    return "";

                ParserRule r = n.rule;
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
        rulesTextSplitPane.setRightComponent(textEditor);
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
        cursorLabel = new JLabel();
        scmLabel = new JLabel();

        infoPanel = new InfoPanel();
        infoPanel.setPreferredSize(new Dimension(0, 30));

        infoPanel.add(Box.createHorizontalStrut(5));
        infoPanel.add(infoLabel);
        infoPanel.add(Box.createHorizontalStrut(5));
        infoPanel.add(createSeparator());
        infoPanel.add(Box.createHorizontalStrut(5));
        infoPanel.add(cursorLabel);
        infoPanel.add(Box.createHorizontalStrut(5));
        infoPanel.add(createSeparator());
        infoPanel.add(Box.createHorizontalStrut(5));
        infoPanel.add(scmLabel);

        toolbar = new EditorToolbar(editor);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(toolbar.getToolbar(), BorderLayout.NORTH);
        mainPanel.add(upDownSplitPane, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        editor.getContentPane().add(mainPanel);
        editor.pack();

        upDownSplitPane.setDividerLocation(0.5);

        if(!XJSystem.isMacOS()) {
            rulesTextSplitPane.setDividerSize(10);
            upDownSplitPane.setDividerSize(10);
        }

        XJNotificationCenter.defaultCenter().addObserver(this, DialogPrefs.NOTIF_PREFS_APPLIED);
    }

    public static JComponent createSeparator() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        Dimension d = s.getMaximumSize();
        d.width = 2;
        s.setMaximumSize(d);
        return s;
    }

    public void close() {
        XJNotificationCenter.defaultCenter().removeObserver(this);
    }

    public void setAutoIndent(boolean flag) {
        autoIndent.setEnabled(flag);
    }

    public boolean autoIndent() {
        return autoIndent.enabled();
    }

    public int getCurrentLinePosition() {
        return editor.getLineIndexAtTextPosition(editor.getCaretPosition()) + 1;
    }

    public int getCurrentColumnPosition() {
        int lineIndex = editor.getLineIndexAtTextPosition(editor.getCaretPosition());
        Point linePosition = editor.getLineTextPositionsAtLineIndex(lineIndex);
        if(linePosition == null)
            return 1;
        else
            return editor.getCaretPosition() - linePosition.x + 1;
    }

    public void updateInformation() {
        String t;
        int size = 0;
        if(editor.parser.getRules() != null)
            size = editor.parser.getRules().size();
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

    public void updateCursorInfo() {
        cursorLabel.setText(getCurrentLinePosition()+":"+getCurrentColumnPosition());
    }

    public void updateSCMStatus(String status) {
        scmLabel.setVisible(EditorPreferences.getP4Enabled());
        if(status != null)
            scmLabel.setText("SCM Status: "+status);
        else
            scmLabel.setText("");
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
                editor.editorMenu.menuItemUndo.setTitle(undo.undoManager.getUndoPresentationName());
            if(undo.canRedo())
                editor.editorMenu.menuItemRedo.setTitle(undo.undoManager.getRedoPresentationName());
        }
    }

    public void undoStateDidChange(Undo undo) {
        updateUndoRedo(undo);
    }

    public void notificationFire(Object source, String name) {
        if(name.equals(DialogPrefs.NOTIF_PREFS_APPLIED)) {
            textEditor.setFoldingEnabled(EditorPreferences.getFoldingEnabled());
            textEditor.setHighlightCursorLine(EditorPreferences.getHighlightCursorEnabled());
            textEditor.refresh();
            applyFont();
            editor.getMainMenuBar().refreshState();
            updateSCMStatus(null);
        }
    }

    public void makeBottomComponentVisible() {
        if(upDownSplitPane.getBottomComponent().getHeight() == 0) {
            upDownSplitPane.setDividerLocation(upDownSplitPane.getLastDividerLocation());
        }
    }

    public void parserDidComplete() {
        textEditor.setIsTyping(false);
        textEditor.refresh();
        updateInformation();
        updateCursorInfo();
    }

    public void applyFont() {
        textEditor.getTextPane().setFont(new Font(EditorPreferences.getEditorFont(), Font.PLAIN, EditorPreferences.getEditorFontSize()));
        TextUtils.createTabs(textEditor.getTextPane());
    }

    protected class TabMouseListener extends MouseAdapter {

        protected static final int CLOSING_INDEX_LIMIT = 4;

        public void displayPopUp(MouseEvent event) {
            if(viewTabbedPane.getSelectedIndex() < CLOSING_INDEX_LIMIT)
                return;

            if(!event.isPopupTrigger())
                return;

            JPopupMenu popup = new JPopupMenu();
            JMenuItem item = new JMenuItem("Close");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if(viewTabbedPane.getSelectedIndex() < CLOSING_INDEX_LIMIT)
                        return;

                    viewTabbedPane.removeTabAt(viewTabbedPane.getSelectedIndex());
                }
            });
            popup.add(item);
            popup.show(event.getComponent(), event.getX(), event.getY());
        }

        public void mousePressed(MouseEvent event) {
            displayPopUp(event);
        }

        public void mouseReleased(MouseEvent event) {
            displayPopUp(event);
        }
    }

    protected class InfoPanel extends Box {

        public InfoPanel() {
            super(BoxLayout.X_AXIS);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Rectangle r = getBounds();

            g.setColor(Color.darkGray);
            g.drawLine(0, 0, r.width, 0);

            g.setColor(Color.lightGray);
            g.drawLine(0, 1, r.width, 1);
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
