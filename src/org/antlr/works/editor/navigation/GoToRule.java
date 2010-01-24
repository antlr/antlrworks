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


package org.antlr.works.editor.navigation;

import org.antlr.works.utils.OverlayObject;
import org.antlr.xjlib.appkit.frame.XJWindow;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class GoToRule extends OverlayObject {

    public JTextField ruleNameField;
    public JList matchingRuleList;
    public DefaultListModel matchingRuleListModel;
    public JScrollPane matchingRuleScrollPane;
    public GoToRuleDelegate delegate;

    public static final int VISIBLE_MATCHING_RULES = 15;

    public GoToRule(GoToRuleDelegate delegate, XJWindow window, JComponent parentComponent) {
        super(window, parentComponent);
        this.delegate = delegate;
    }

    public void close() {
        super.close();
    }

    public JComponent overlayCreateInterface() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Go To Rule:"), BorderLayout.NORTH);

        ruleNameField = new JTextField();
        ruleNameField.addKeyListener(new TextFieldKeyAdapter());
        ruleNameField.getDocument().addDocumentListener(new TextFieldDocumentListener());

        matchingRuleListModel = new DefaultListModel();

        matchingRuleList = new JList(matchingRuleListModel);
        matchingRuleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        matchingRuleList.setBackground(new Color(235, 244, 254));
        matchingRuleList.setPrototypeCellValue("This is a rule name g");
        matchingRuleList.addKeyListener(new ListKeyAdapter());

        // FIX AW-85
        matchingRuleList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2) {
                    goToRule();
                    hide();                    
                }
            }
        });

        matchingRuleScrollPane = new JScrollPane(matchingRuleList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        matchingRuleScrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        matchingRuleScrollPane.setVisible(false);
        parentFrame.getLayeredPane().add(matchingRuleScrollPane, JLayeredPane.MODAL_LAYER);

        panel.add(ruleNameField, BorderLayout.SOUTH);

        return panel;
    }

    public boolean overlayWillDisplay() {
        ruleNameField.setText("");
        // Invoke focus later because otherwise it fails on Windows
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ruleNameField.requestFocusInWindow();
            }
        });
        updateAutoCompletionList();
        return true;
    }

    public void hide() {
        super.hide();
        matchingRuleScrollPane.setVisible(false);
    }

    public void resize() {
        super.resize();
        resizeMatchingRules();
    }

    public void resizeMatchingRules() {
        Rectangle r = content.getBounds();
        int height = matchingRuleList.getFixedCellHeight();
        int size = matchingRuleListModel.size();
        if(size > 0) {
            height = height*Math.min(VISIBLE_MATCHING_RULES, size)+5;
            matchingRuleScrollPane.setBounds(r.x,  r.y+r.height, r.width, height);
        }
    }

    public void updateAutoCompletionList() {
        matchingRuleListModel.removeAllElements();

        List<String> rules = delegate.getRulesStartingWith(ruleNameField.getText().toLowerCase());
        if(rules.isEmpty()) {
            matchingRuleScrollPane.setVisible(false);
            ruleNameField.setForeground(Color.red);
            return;
        } else {
            ruleNameField.setForeground(Color.black);
        }

        for (String rule : rules) {
            matchingRuleListModel.addElement(rule);
        }
        matchingRuleList.setSelectedIndex(0);

        resizeMatchingRules();
        matchingRuleScrollPane.setVisible(true);
    }

    public void selectNextListElement(int direction) {
        int index = matchingRuleList.getSelectedIndex();
        index += direction;
        index = Math.min(Math.max(0, index), matchingRuleListModel.size()-1);

        matchingRuleList.setSelectedIndex(index);
        matchingRuleList.scrollRectToVisible(matchingRuleList.getCellBounds(index, index));
    }

    public void goToRule() {
        if(matchingRuleListModel.isEmpty())
            return;

        int index = matchingRuleList.getSelectedIndex();
        if(index >= 0) {
            delegate.goToRule((String)matchingRuleListModel.get(index));
        }
    }

    public class ListKeyAdapter extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
            if(e.isConsumed())
                return;

            if(!content.isVisible())
                return;

            switch(e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                    hide();
                    e.consume();
                    break;

                case KeyEvent.VK_ENTER:
                    goToRule();
                    hide();
                    e.consume();
                    break;

                case KeyEvent.VK_UP:
                    selectNextListElement(-1);
                    e.consume();
                    break;

                case KeyEvent.VK_DOWN:
                    selectNextListElement(1);
                    e.consume();
                    break;
            }
        }
    }

    public class TextFieldKeyAdapter extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
            if(e.isConsumed())
                return;

            if(!content.isVisible())
                return;

            switch(e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                    hide();
                    e.consume();
                    break;

                case KeyEvent.VK_ENTER:
                    goToRule();
                    hide();
                    e.consume();
                    break;

                case KeyEvent.VK_UP:
                    selectNextListElement(-1);
                    e.consume();
                    break;

                case KeyEvent.VK_DOWN:
                    selectNextListElement(1);
                    e.consume();
                    break;
            }
        }
    }

    public class TextFieldDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent event) {
            updateAutoCompletionList();
        }

        public void removeUpdate(DocumentEvent event) {
            updateAutoCompletionList();
        }

        public void changedUpdate(DocumentEvent event) {
            updateAutoCompletionList();
        }
    }

}
