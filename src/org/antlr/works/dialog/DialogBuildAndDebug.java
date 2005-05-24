package org.antlr.works.dialog;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import edu.usfca.xj.appkit.app.XJApplication;
import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.appkit.utils.XJFileChooser;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.editor.EditorPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

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

public class DialogBuildAndDebug extends XJDialog {

    protected Debugger debugger;

    public DialogBuildAndDebug(Debugger debugger, Container parent) {
        super(parent, true);

        this.debugger = debugger;

        initComponents();
        setSize(615, 127+20);

        rulesCombo.removeAllItems();
        for (Iterator iterator = debugger.getRules().iterator(); iterator.hasNext();) {
            rulesCombo.addItem(iterator.next());
        }

        outputPathField.setText(EditorPreferences.getOutputPath());
        rulesCombo.setSelectedItem(EditorPreferences.getStartSymbol());

        setDefaultButton(okButton);
        setOKButton(okButton);
        setCancelButton(cancelButton);

        browseOutputPathButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(XJFileChooser.shared().displayChooseDirectory(DialogBuildAndDebug.this.getJavaComponent())) {
                    outputPathField.setText(XJFileChooser.shared().getSelectedFilePath());
                }
            }
        });
    }

    public void dialogWillCloseOK() {
        EditorPreferences.setOutputPath(getOutputPath());
        EditorPreferences.setStartSymbol(getRule());
    }

    public String getOutputPath() {
        return outputPathField.getText();
    }

    public String getRule() {
        return (String)rulesCombo.getSelectedItem();
    }

    // Note: - add the setCancelButton/setOKButton method calls!

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPane = new JPanel();
        label1 = new JLabel();
        outputPathField = new JTextField();
        browseOutputPathButton = new JButton();
        label2 = new JLabel();
        rulesCombo = new JComboBox();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setTitle("Build & Debug");
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
                        new ColumnSpec("max(min;200dlu):grow"),
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC
                    },
                    new RowSpec[] {
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC
                    }));

                //---- label1 ----
                label1.setHorizontalAlignment(SwingConstants.RIGHT);
                label1.setText("Output path:");
                contentPane.add(label1, cc.xy(1, 1));
                contentPane.add(outputPathField, cc.xy(3, 1));

                //---- browseOutputPathButton ----
                browseOutputPathButton.setText("Browse...");
                contentPane.add(browseOutputPathButton, cc.xy(5, 1));

                //---- label2 ----
                label2.setHorizontalAlignment(SwingConstants.RIGHT);
                label2.setText("Start symbol:");
                contentPane.add(label2, cc.xy(1, 3));
                contentPane.add(rulesCombo, cc.xy(3, 3));
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

                if(XJApplication.isMacOS()) {
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
    private JLabel label1;
    private JTextField outputPathField;
    private JButton browseOutputPathButton;
    private JLabel label2;
    private JComboBox rulesCombo;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables



}
