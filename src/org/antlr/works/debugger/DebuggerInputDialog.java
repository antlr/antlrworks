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

package org.antlr.works.debugger;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.foundation.XJSystem;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.syntax.GrammarSyntaxRule;
import org.antlr.works.utils.TextUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

public class DebuggerInputDialog extends XJDialog {

    public DebuggerInputDialog(Debugger debugger, Container parent) {
        super(parent, true);

        initComponents();
        setSize(600, 400);

        setDefaultButton(okButton);
        setOKButton(okButton);
        setCancelButton(cancelButton);

        TextUtils.createTabs(inputTextArea);
        inputTextArea.setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        inputTextArea.requestFocus();
        inputTextArea.setText(AWPrefs.getDebuggerInputText());

        rulesCombo.removeAllItems();
        for (Iterator iterator = debugger.getRules().iterator(); iterator.hasNext();) {
            GrammarSyntaxRule rule = (GrammarSyntaxRule)iterator.next();
            rulesCombo.addItem(rule.name);
        }
        rulesCombo.setSelectedItem(AWPrefs.getStartSymbol());
    }

    public void dialogWillCloseOK() {
        AWPrefs.setStartSymbol(getRule());
        AWPrefs.setDebuggerInputText(getInputText());
    }

    public void setInputText(String text) {
        if(text != null)
            inputTextArea.setText(text);
    }

    public String getInputText() {
        return inputTextArea.getText();
    }

    public String getRule() {
        return (String)rulesCombo.getSelectedItem();
    }

    // Note: put 500 for the width of the scrollpane
    // Also copy the line for button OS sensitive location

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPane = new JPanel();
        scrollPane1 = new JScrollPane();
        inputTextArea = new JTextPane();
        label2 = new JLabel();
        rulesCombo = new JComboBox();
        label1 = new JLabel();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setTitle("Input Text");
        Container contentPane2 = getContentPane();
        contentPane2.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(Borders.DIALOG_BORDER);
            dialogPane.setLayout(new BorderLayout());

            //======== contentPane ========
            {
                contentPane.setLayout(new FormLayout(
                    new ColumnSpec[] {
                        FormFactory.DEFAULT_COLSPEC,
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC
                    },
                    new RowSpec[] {
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC
                    }));

                //======== scrollPane1 ========
                {
                    scrollPane1.setPreferredSize(new Dimension(300, 200));

                    //---- inputTextArea ----
                    //inputTextArea.setLineWrap(false);
                    scrollPane1.setViewportView(inputTextArea);
                }
                contentPane.add(scrollPane1, cc.xywh(1, 3, 5, 1));

                //---- label2 ----
                label2.setText("Start Rule:");
                contentPane.add(label2, cc.xy(1, 5));
                contentPane.add(rulesCombo, cc.xy(3, 5));

                //---- label1 ----
                label1.setText("Input text:");
                contentPane.add(label1, cc.xywh(1, 1, 3, 1));
            }
            dialogPane.add(contentPane, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
                buttonBar.setLayout(new FormLayout(
                    new ColumnSpec[] {
                        FormFactory.GLUE_COLSPEC,
                        FormFactory.BUTTON_COLSPEC,
                        FormFactory.RELATED_GAP_COLSPEC,
                        FormFactory.BUTTON_COLSPEC
                    },
                    RowSpec.decodeSpecs("pref")));

                //---- okButton ----
                okButton.setText("OK");

                //---- cancelButton ----
                cancelButton.setText("Cancel");

                if(XJSystem.isMacOS()) {
                    buttonBar.add(cancelButton, cc.xy(2, 1));
                    buttonBar.add(okButton, cc.xy(4, 1));
                } else {
                    buttonBar.add(okButton, cc.xy(2, 1));
                    buttonBar.add(cancelButton, cc.xy(4, 1));
                }
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane2.add(dialogPane, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPane;
    private JScrollPane scrollPane1;
    private JTextPane inputTextArea;
    private JLabel label2;
    private JComboBox rulesCombo;
    private JLabel label1;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}
