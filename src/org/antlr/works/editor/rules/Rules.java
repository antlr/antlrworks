package org.antlr.works.editor.rules;

import edu.usfca.xj.appkit.swing.XJTable;
import org.antlr.works.editor.swing.KeyBindings;
import org.antlr.works.editor.tool.TActions;
import org.antlr.works.parser.Parser;
import org.antlr.works.parser.ThreadedParser;
import org.antlr.works.parser.ThreadedParserObserver;
import org.antlr.works.util.IconManager;
import org.antlr.works.util.Statistics;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;

/*

[The "BSD licence"]
Copyright (c) 2004 Jean Bovet
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

public class Rules implements ThreadedParserObserver {

    private RulesDelegate delegate = null;
    private ThreadedParser parser = null;
    private TActions actions = null;

    private boolean selectingRule = false;
    private boolean skipParseRules = false;
    private boolean selectNextRule = false;

    private JTextPane textPane;
    private XJTable rulesTable;

    private DragSource ds;
    private StringSelection transferable;

    private AbstractTableModel rulesTableModel;

    private static final int COLUMN_RULE_NAME = 0;
    private static final int COLUMN_RULE_STATUS = 1;

    public Rules(ThreadedParser parser, JTextPane textPane, XJTable rulesTable) {
        this.parser = parser;
        this.textPane = textPane;
        this.rulesTable = rulesTable;

        parser.addObserver(this);

        rulesTable.setDefaultRenderer(Object.class, new CustomTableRenderer());

        rulesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        rulesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting())
                    return;

                if(selectingRule)
                    return;

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if(!lsm.isSelectionEmpty()) {
                    int row = lsm.getMinSelectionIndex();
                    Parser.Rule rc = Rules.this.parser.getRuleAtIndex(row);
                    selectTextRule(rc);
                }
            }
        });

        rulesTable.setModel(rulesTableModel = new AbstractTableModel() {
            public int getColumnCount() {
                return 2;
            }

            public int getRowCount() {
                if(Rules.this.parser.getRules() != null)
                    return Rules.this.parser.getRules().size();
                else
                    return 0;
            }

            public boolean isCellEditable(int row, int col) {
                return false;
            }

            public String getColumnName(int column) {
                switch(column) {
                    case COLUMN_RULE_NAME:
                        return "Rules";
                    case COLUMN_RULE_STATUS:
                        return "!";
                }
                return "";
            }

            public Object getValueAt(int row, int col) {
                Parser.Rule rc = Rules.this.parser.getRuleAtIndex(row);
                switch(col) {
                    case COLUMN_RULE_NAME:
                        return rc.name;
                    case COLUMN_RULE_STATUS:
                        return null;
                }
                return null;
            }

            public void setValueAt(Object value, int row, int col) {
            }
        });

        rulesTable.getColumnModel().getColumn(COLUMN_RULE_STATUS).setMaxWidth(20);

        ds = new DragSource();
        DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer(rulesTable, DnDConstants.ACTION_MOVE, new TableDragGestureListener());
        DropTarget dt = new DropTarget(rulesTable, new TableDropListener());
    }

    public void setDelegate(RulesDelegate delegate) {
        this.delegate = delegate;
    }

    public void setActions(TActions actions) {
        this.actions = actions;
    }

    public void setKeyBindings(KeyBindings keyBindings) {
        keyBindings.addKeyBinding("RULE_MOVE_UP", KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.CTRL_MASK),
                new RuleMoveUpAction());
        keyBindings.addKeyBinding("RULE_MOVE_DOWN", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.CTRL_MASK),
                new RuleMoveDownAction());
    }

    public int getNumberOfRulesWithErrors() {
        int count = 0;
        for (Iterator iterator = parser.getRules().iterator(); iterator.hasNext();) {
            Parser.Rule rule = (Parser.Rule) iterator.next();
            if(rule.hasErrors())
                count++;
        }
        return count;
    }

    public class RuleMoveUpAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            Parser.Rule sourceRule = getRuleAtPosition(textPane.getCaretPosition());
            int previousRuleIndex = parser.getRules().indexOf(sourceRule)-1;
            if(previousRuleIndex>=0) {
                Parser.Rule targetRule = parser.getRuleAtIndex(previousRuleIndex);
                moveRule(sourceRule, targetRule);
            }
        }
    }

    public class RuleMoveDownAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            Parser.Rule targetRule = getRuleAtPosition(textPane.getCaretPosition());
            int nextRuleIndex = parser.getRules().indexOf(targetRule)+1;
            if(nextRuleIndex<parser.getRules().size()) {
                Parser.Rule sourceRule = parser.getRuleAtIndex(nextRuleIndex);
                moveRule(sourceRule, targetRule);
                selectNextRule = true;
            }
        }
    }

    public void setSkipParseRules(boolean flag) {
        this.skipParseRules = flag;
    }

    public void parseRules() {
        if(skipParseRules)
            return;
        parser.parse();
    }

    public void refreshRules() {
        rulesTableModel.fireTableDataChanged();
        // Select again the row (otherwise, the selection is lost)
        selectRuleAtPosition(textPane.getCaretPosition());
    }

    public Parser.Rule getRuleAtPosition(int pos) {
        if(parser.getRules() == null)
            return null;
        
        Iterator iterator = parser.getRules().iterator();
        while(iterator.hasNext()) {
            Parser.Rule r = (Parser.Rule)iterator.next();
            if(pos>=r.start.start && pos<=r.end.end)
                return r;
        }
        return null;
    }


    public Parser.Rule selectRuleAtPosition(int pos) {
        if(selectingRule || parser.getRules() == null)
            return null;

        selectingRule = true;
        Parser.Rule rule = getRuleAtPosition(pos);
        selectRule(rule);
        selectingRule = false;
        return rule;
    }

    public Parser.Rule selectRuleName(String name) {
        if(selectingRule || parser.getRules() == null)
            return null;

        Parser.Rule rule = null;
        selectingRule = true;
        Iterator iterator = parser.getRules().iterator();
        while(iterator.hasNext()) {
            Parser.Rule r = (Parser.Rule)iterator.next();
            if(r.name.equals(name)) {
                selectRule(r);
                rule = r;
                break;
            }
        }
        selectingRule = false;
        return rule;
    }

    public boolean isRuleName(String name) {
        Iterator iterator = parser.getRules().iterator();
        while(iterator.hasNext()) {
            Parser.Rule r = (Parser.Rule)iterator.next();
            if(r.name.equals(name))
                return true;
        }
        return false;
    }

    public void selectFirstRule() {
        if(parser.getRules().size() == 0)
            return;

        selectRule((Parser.Rule)parser.getRules().get(0));
    }

    public void selectNextRule() {
        Parser.Rule rule = getRuleAtPosition(textPane.getCaretPosition());
        int index = parser.getRules().indexOf(rule)+1;
        rule = parser.getRuleAtIndex(index);
        textPane.setCaretPosition(rule.getStartIndex());
        delegate.rulesCaretPositionDidChange();
    }

    public void selectRule(Parser.Rule rule) {
        if(rule == null)
            return;

        int index = parser.getRules().indexOf(rule);
        rulesTable.getSelectionModel().setSelectionInterval(index, index);
        rulesTable.scrollRectToVisible(rulesTable.getCellRect(index, 0, true));
    }

    public void selectTextRule(Parser.Rule rule) {
        textPane.setCaretPosition(rule.start.start);
        textPane.moveCaretPosition(rule.end.end);
        textPane.getCaret().setSelectionVisible(true);

        Rectangle r = null;
        try {
            r = textPane.modelToView(rule.start.start);
            textPane.scrollRectToVisible(r);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
        delegate.rulesDidSelectRule();
    }

    public void parserDidComplete() {
        rulesTableModel.fireTableDataChanged();
        if(selectNextRule) {
            // Can be set by RuleMoveDown() class when a rule is moved down. Selection has to occurs here
            // after rules have been parsed. We use this flag to select the next rule instead of the current one.
            selectNextRule = false;
            selectNextRule();
        } else
            selectRuleAtPosition(textPane.getCaret().getDot());
    }

    public boolean moveRule(Parser.Rule sourceRule, Parser.Rule targetRule) {
        String sourceRuleText = actions.getPlainText(sourceRule.getStartIndex(), sourceRule.getEndIndex())+"\n";

        try {
            Document doc = textPane.getDocument();

            int removeStartIndex = sourceRule.getStartIndex();

            // Remove one more character to remove the end of line of the rule
            int removeLength = sourceRule.getLength()+1;
            if(removeStartIndex+removeLength > doc.getLength())
                removeLength--;

            if(sourceRule.getStartIndex()>targetRule.getStartIndex()) {
                doc.remove(removeStartIndex, removeLength);
                doc.insertString(targetRule.getStartIndex(), sourceRuleText, null);
                textPane.setCaretPosition(targetRule.getStartIndex());
            } else {
                doc.insertString(targetRule.getStartIndex(), sourceRuleText, null);
                doc.remove(removeStartIndex, removeLength);
                textPane.setCaretPosition(targetRule.getStartIndex()-removeLength);
            }
            return true;
        } catch (BadLocationException e) {
            e.printStackTrace();
            return false;
        }
    }

    public class CustomTableRenderer extends DefaultTableCellRenderer {
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col)
        {
            setIcon(null);
            setToolTipText(null);

            if(table.convertColumnIndexToView(col) == COLUMN_RULE_STATUS) {
                Parser.Rule rule = parser.getRuleAtIndex(row);
                if(rule.hasErrors()) {
                    setIcon(IconManager.getIconWarning());
                    setToolTipText(rule.getErrorMessageHTML());
                }
            }

            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        }
    }

    public class TableDragGestureListener implements DragGestureListener {

        public void dragGestureRecognized(DragGestureEvent event) {
            transferable = new StringSelection(String.valueOf(rulesTable.getSelectedRow()));
            ds.startDrag(event, DragSource.DefaultMoveDrop, transferable, new TableDragSourceListener());
        }
    }

    public class TableDragSourceListener implements DragSourceListener {

        public void dragEnter(DragSourceDragEvent event) {
        }

        public void dragOver(DragSourceDragEvent event) {
        }

        public void dropActionChanged(DragSourceDragEvent event) {
        }

        public void dragExit(DragSourceEvent event) {
        }

        public void dragDropEnd(DragSourceDropEvent event) {
        }
    }

    public class TableDropListener implements DropTargetListener {

        int oldSelectedRow = -1;

        public void dragEnter(DropTargetDragEvent event) {
            oldSelectedRow = rulesTable.getSelectedRow();
            if(event.getDropAction() != DnDConstants.ACTION_MOVE)
                event.rejectDrag();
            else
                event.acceptDrag(DnDConstants.ACTION_MOVE);
        }

        public void dragOver(DropTargetDragEvent event) {
            int row = rulesTable.rowAtPoint(event.getLocation());
            if(row == -1 || event.getDropAction() != DnDConstants.ACTION_MOVE) {
                rulesTable.getSelectionModel().addSelectionInterval(oldSelectedRow, oldSelectedRow);
                event.rejectDrag();
            } else {
                rulesTable.getSelectionModel().addSelectionInterval(row, row);
                event.acceptDrag(DnDConstants.ACTION_MOVE);
            }
        }

        public void dropActionChanged(DropTargetDragEvent event) {
            if(event.getDropAction() != DnDConstants.ACTION_MOVE) {
                rulesTable.getSelectionModel().addSelectionInterval(oldSelectedRow, oldSelectedRow);
                event.rejectDrag();
            } else
                event.acceptDrag(DnDConstants.ACTION_MOVE);
        }

        public void dragExit(DropTargetEvent event) {
            rulesTable.getSelectionModel().addSelectionInterval(oldSelectedRow, oldSelectedRow);
        }

        public void drop(DropTargetDropEvent event) {
            int row = rulesTable.rowAtPoint(event.getLocation());
            if(row == -1) {
                rulesTable.getSelectionModel().addSelectionInterval(oldSelectedRow, oldSelectedRow);
                return;
            }

            Statistics.shared().recordEvent(Statistics.EVENT_DROP_RULE);

            event.acceptDrop(DnDConstants.ACTION_MOVE);

            Parser.Rule sourceRule = parser.getRuleAtIndex(oldSelectedRow);
            Parser.Rule targetRule = parser.getRuleAtIndex(row);

            if(moveRule(sourceRule, targetRule))
                event.dropComplete(true);
            else
                event.rejectDrop();
        }
    }

}
