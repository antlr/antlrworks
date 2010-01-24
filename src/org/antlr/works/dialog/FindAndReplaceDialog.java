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


package org.antlr.works.dialog;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import org.antlr.works.find.FindAndReplace;
import org.antlr.xjlib.appkit.frame.XJPanel;
import org.antlr.xjlib.appkit.utils.XJAlert;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class FindAndReplaceDialog extends XJPanel {

    private FindAndReplace delegate;

    public FindAndReplaceDialog(FindAndReplace delegate) {
        this.delegate = delegate;

        initComponents();

        Dimension ps = getContentPane().getPreferredSize();
        // This is really hugly... but I don't have time to investigate
        // why in desktop mode this internal frame cannot take its correct
        // preferred size.
        ps.width += 40;
        ps.height += 40;
        setPreferredSize(ps);
        pack();

        setTitle("Find");
        awake();
        center();

        getRootPane().setDefaultButton(nextButton);
        addEscapeHandling();

        createActions();

        // Default values
        ignoreCaseButton.setSelected(true);
        delegate.setIgnoreCase(true);
    }

    public void setFindText(String text) {
        if(text == null || text.length() == 0) return;
        findField.setText(text);
    }

    public void addEscapeHandling() {
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);

        ActionListener cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        };

        getRootPane().registerKeyboardAction(cancelAction, "CancelAction", ks,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void createActions() {
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                delegate.setFindString(findField.getText());
                alertEndOfDocument(this, delegate.next());
            }
        });

        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                delegate.setFindString(findField.getText());
                alertBeginningOfDocument(this, delegate.prev());
            }
        });

        replaceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                delegate.setReplaceString(replaceField.getText());
                delegate.replace();
            }
        });

        replaceAndFindButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                delegate.setFindString(findField.getText());
                delegate.setReplaceString(replaceField.getText());
                delegate.replace();
                alertEndOfDocument(this, delegate.next());
            }
        });

        replaceAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                delegate.setFindString(findField.getText());
                delegate.setReplaceString(replaceField.getText());
                delegate.replaceAll();
            }
        });

        ignoreCaseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                delegate.setIgnoreCase(ignoreCaseButton.isSelected());
            }
        });

        regexButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                delegate.setRegex(regexButton.isSelected());
            }
        });

        optionsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                delegate.setOptions(optionsCombo.getSelectedIndex());
            }
        });
    }

    private void alertEndOfDocument(ActionListener actionListener, boolean result) {
        if(result) return;

        if(XJAlert.displayAlert(getJavaContainer(), "End of Document", "The end of the document has been reached.",
                "Continue", "OK", 0, 1) == 0)
        {
            delegate.setPositionToTop();
            actionListener.actionPerformed(null);
        }
    }

    private void alertBeginningOfDocument(ActionListener actionListener, boolean result) {
        if(result) return;

        if(XJAlert.displayAlert(getJavaContainer(), "Beginning of Document", "The beginning of the document has been reached.",
                "Continue", "OK", 0, 1) == 0)
        {
            delegate.setPositionToBottom();
            actionListener.actionPerformed(null);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - ANTLR (www.antlr.org)
        label1 = new JLabel();
        findField = new JTextField();
        label2 = new JLabel();
        replaceField = new JTextField();
        ignoreCaseButton = new JCheckBox();
        regexButton = new JCheckBox();
        optionsCombo = new JComboBox();
        replaceAllButton = new JButton();
        replaceButton = new JButton();
        replaceAndFindButton = new JButton();
        previousButton = new JButton();
        nextButton = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
                new ColumnSpec[] {
                        new ColumnSpec(Sizes.DLUX5),
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        new ColumnSpec(ColumnSpec.RIGHT, Sizes.DEFAULT, FormSpec.NO_GROW),
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC,
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC,
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC,
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC,
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        new ColumnSpec(Sizes.DLUX5)
                },
                new RowSpec[] {
                        new RowSpec(Sizes.DLUY5),
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        new RowSpec(RowSpec.CENTER, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        new RowSpec(Sizes.DLUY5)
                }));

        //---- label1 ----
        label1.setText("Find:");
        contentPane.add(label1, cc.xy(3, 3));
        contentPane.add(findField, cc.xywh(5, 3, 9, 1));

        //---- label2 ----
        label2.setText("Replace by:");
        contentPane.add(label2, cc.xy(3, 5));
        contentPane.add(replaceField, cc.xywh(5, 5, 9, 1));

        //---- ignoreCaseButton ----
        ignoreCaseButton.setText("Ignore case");
        contentPane.add(ignoreCaseButton, cc.xy(5, 7));

        //---- regexButton ----
        regexButton.setText("Regular expression");
        contentPane.add(regexButton, cc.xy(7, 7));

        //---- optionsCombo ----
        optionsCombo.setModel(new DefaultComboBoxModel(new String[] {
                "Contains",
                "Starts with",
                "Whole words",
                "Ends with"
        }));
        contentPane.add(optionsCombo, cc.xywh(11, 7, 3, 1));

        //---- replaceAllButton ----
        replaceAllButton.setText("Replace All");
        contentPane.add(replaceAllButton, cc.xy(3, 11));

        //---- replaceButton ----
        replaceButton.setText("Replace");
        contentPane.add(replaceButton, cc.xy(5, 11));

        //---- replaceAndFindButton ----
        replaceAndFindButton.setText("Replace & Find");
        contentPane.add(replaceAndFindButton, cc.xy(7, 11));

        //---- previousButton ----
        previousButton.setText("Previous");
        contentPane.add(previousButton, cc.xy(11, 11));

        //---- nextButton ----
        nextButton.setText("Next");
        contentPane.add(nextButton, cc.xy(13, 11));
        pack();
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Open Source Project license - ANTLR (www.antlr.org)
    private JLabel label1;
    private JTextField findField;
    private JLabel label2;
    private JTextField replaceField;
    private JCheckBox ignoreCaseButton;
    private JCheckBox regexButton;
    private JComboBox optionsCombo;
    private JButton replaceAllButton;
    private JButton replaceButton;
    private JButton replaceAndFindButton;
    private JButton previousButton;
    private JButton nextButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}
