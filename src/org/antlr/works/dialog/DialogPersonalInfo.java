package org.antlr.works.dialog;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import edu.usfca.xj.appkit.frame.XJDialog;

import javax.swing.*;
import java.awt.*;

import org.antlr.works.util.IconManager;

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

public class DialogPersonalInfo extends XJDialog {

    public DialogPersonalInfo() {
        initComponents();

        setSize(new Dimension(750, 400));

        setDefaultButton(okButton);
        setOKButton(okButton);
    }

    // Note: replace absolute path with IconManager.getIconApp();
    
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPane = new JPanel();
        button1 = new JButton();
        textArea1 = new JTextArea();
        label1 = new JLabel();
        typeCombo = new JComboBox();
        languageExperienceSpinner = new JSpinner();
        label2 = new JLabel();
        programmingExperienceSpinner = new JSpinner();
        label3 = new JLabel();
        label4 = new JLabel();
        countryField = new JTextField();
        label5 = new JLabel();
        dogsNameField = new JTextField();
        buttonBar = new JPanel();
        okButton = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setTitle("ANTLRWorks");
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
                        FormFactory.DEFAULT_COLSPEC,
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        new ColumnSpec("max(min;100dlu):grow"),
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        new ColumnSpec(Sizes.dluX(20)),
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        new ColumnSpec("max(default;30dlu)"),
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC
                    },
                    new RowSpec[] {
                        new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC
                    }));

                //---- button1 ----
                button1.setBorder(null);
                button1.setBorderPainted(false);
                button1.setContentAreaFilled(false);
                button1.setDefaultCapable(false);
                button1.setEnabled(true);
                button1.setFocusPainted(false);
                button1.setFocusable(false);
                button1.setIcon(IconManager.getIconApplication());
                button1.setOpaque(false);
                button1.setRequestFocusEnabled(false);
                button1.setVerifyInputWhenFocusTarget(false);
                contentPane.add(button1, cc.xy(1, 1));

                //---- textArea1 ----
                textArea1.setBackground(UIManager.getColor("window"));
                textArea1.setEditable(false);
                textArea1.setLineWrap(true);
                textArea1.setOpaque(false);
                textArea1.setText("Welcome to ANTLRWorks early release 1!\n\nPlease take a few seconds to fill out some information about yourself to help us improve our products.\n\nThank you!");
                textArea1.setWrapStyleWord(true);
                contentPane.add(textArea1, cc.xywh(3, 1, 7, 1));

                //---- label1 ----
                label1.setText("Your are a");
                contentPane.add(label1, cc.xy(3, 5));

                //---- typeCombo ----
                typeCombo.setModel(new DefaultComboBoxModel(new String[] {
                    "professional programmer",
                    "professor",
                    "researcher",
                    "graduate student",
                    "undergraduate student"
                }));
                contentPane.add(typeCombo, cc.xywh(5, 5, 5, 1));
                contentPane.add(languageExperienceSpinner, cc.xy(7, 7));

                //---- label2 ----
                label2.setHorizontalAlignment(SwingConstants.RIGHT);
                label2.setText("Years of experience with language translation/implementation:");
                contentPane.add(label2, cc.xywh(3, 7, 3, 1));
                contentPane.add(programmingExperienceSpinner, cc.xy(7, 9));

                //---- label3 ----
                label3.setHorizontalAlignment(SwingConstants.RIGHT);
                label3.setText("Years of programming experience:");
                contentPane.add(label3, cc.xywh(3, 9, 3, 1));

                //---- label4 ----
                label4.setHorizontalAlignment(SwingConstants.RIGHT);
                label4.setText("Country were you actually reside:");
                contentPane.add(label4, cc.xywh(3, 11, 3, 1));
                contentPane.add(countryField, cc.xywh(7, 11, 3, 1));

                //---- label5 ----
                label5.setHorizontalAlignment(SwingConstants.RIGHT);
                label5.setText("Dog's name:");
                contentPane.add(label5, cc.xywh(3, 13, 3, 1));
                contentPane.add(dogsNameField, cc.xywh(7, 13, 3, 1));
            }
            dialogPane.add(contentPane, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
                buttonBar.setLayout(new FormLayout(
                    new ColumnSpec[] {
                        FormFactory.GLUE_COLSPEC,
                        FormFactory.BUTTON_COLSPEC
                    },
                    RowSpec.decodeSpecs("pref")));

                //---- okButton ----
                okButton.setText("OK");
                buttonBar.add(okButton, cc.xy(2, 1));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane2.add(dialogPane, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPane;
    private JButton button1;
    private JTextArea textArea1;
    private JLabel label1;
    private JComboBox typeCombo;
    private JSpinner languageExperienceSpinner;
    private JLabel label2;
    private JSpinner programmingExperienceSpinner;
    private JLabel label3;
    private JLabel label4;
    private JTextField countryField;
    private JLabel label5;
    private JTextField dogsNameField;
    private JPanel buttonBar;
    private JButton okButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}
