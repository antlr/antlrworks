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

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import org.antlr.works.IDE;
import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.debugger.local.DBLocal;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.TextUtils;
import org.antlr.works.utils.Utils;
import org.antlr.xjlib.appkit.frame.XJDialog;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.foundation.XJSystem;
import org.antlr.xjlib.foundation.XJUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

public class DialogTestTemplate extends XJDialog {
    public static final String TEXT_FULLY_QUALIFIED_CLASS_NAME = "Enter fully qualified class name here..";

    private DebuggerTab debuggerTab;

    private String testRigFullPath;
    private String qualifiedFileName;
    private String grammarIdentifier;
    private String grammarLanguage = "Java";

    public DialogTestTemplate(DebuggerTab debuggerTab, Container parent) {
        super(parent, true);

        this.debuggerTab = debuggerTab;
        qualifiedFileName = this.debuggerTab.getDelegate().getDocument().getDocumentPath();
        if (qualifiedFileName != null) {
            testRigFullPath = XJUtils.getPathByDeletingPathExtension(qualifiedFileName) + DBLocal.testRigTemplateSuffix + ".st";
            grammarIdentifier = qualifiedFileName.toUpperCase();
        }
        if (this.debuggerTab.getDelegate().getGrammarEngine() != null)
            grammarLanguage = this.debuggerTab.getDelegate().getGrammarEngine().getGrammarLanguage();

        initComponents();

        if(XJSystem.isMacOS()) {
            buttonBar.remove(okButton);
            buttonBar.remove(cancelButton);

            CellConstraints cc = new CellConstraints();
            buttonBar.add(cancelButton, cc.xy(2, 1));
            buttonBar.add(okButton, cc.xy(4, 1));
        }

        setDefaultButton(okButton);
        setOKButton(okButton);
        setCancelButton(cancelButton);

        TextUtils.createTabs(testTextArea);
        TextUtils.setDefaultTextPaneProperties(testTextArea);

        testTextArea.setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        testTextArea.setFocusable(true);
        testTextArea.requestFocusInWindow();
        testTextArea.setText(getTestRigTemplateFromFile(testRigFullPath));

        if ("".equals(testTextArea.getText())) {
            if (AWPrefs.TEST_RIG_MODE_DEFAULT.equals(AWPrefs.getTestRigTemplateModeByLanguage(grammarLanguage.toUpperCase()))) {
                testTextArea.setText(getDefaultTestRigByLanguage(grammarLanguage));
            } else {
                testTextArea.setText(AWPrefs.getTestRigTemplateTextByLanguage(grammarLanguage.toUpperCase()));
            }
        }

        testClassHiddenField.setText(AWPrefs.getTestRigTemplateClass(grammarIdentifier));
        testClassField.setText(testClassHiddenField.getText());
        if ("".equals(testClassField.getText())) {
            testClassField.setForeground(Color.LIGHT_GRAY);
            testClassField.setText(TEXT_FULLY_QUALIFIED_CLASS_NAME);
        }

        textTestRadio.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (textTestRadio.isSelected()) {
                    testTextArea.setEnabled(true);
                    testClassField.setEnabled(false);
                } else {
                    testTextArea.setEnabled(false);
                    testClassField.setEnabled(true);
                }
            }
        });

        classTestRadio.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (classTestRadio.isSelected()) {
                    testTextArea.setEnabled(false);
                    testClassField.setEnabled(true);
                } else {
                    testTextArea.setEnabled(true);
                    testClassField.setEnabled(false);
                }
            }
        });

        if (AWPrefs.TEST_RIG_MODE_TEXT.equals(AWPrefs.getTestRigTemplateMode(grammarIdentifier))) {
            textTestRadio.setSelected(true);
            testTextArea.setEnabled(true);
            testClassField.setEnabled(false);
        } else {
            classTestRadio.setSelected(true);
            testTextArea.setEnabled(false);
            testClassField.setEnabled(true);
        }

    }

    public void dialogWillCloseOK() {
        String text = getTestRigText();
        if(text.length() > Preferences.MAX_VALUE_LENGTH) {
            XJAlert.display(getJavaComponent(), "Error", "The test template text is too large: "+text.length()+" bytes but preferences can only hold "+Preferences.MAX_VALUE_LENGTH+" bytes. It will be truncated.");
            text = text.substring(0, Preferences.MAX_VALUE_LENGTH-1);
        }
        setTestRigTemplateToFile(testRigFullPath, text);
        AWPrefs.setTestRigTemplateMode(grammarIdentifier, getTestRigMode());
        AWPrefs.setTestRigTemplateClass(grammarIdentifier, getTestRigClass());
    }

    public String getTestRigMode() {
        return textTestRadio.isSelected() ? AWPrefs.TEST_RIG_MODE_TEXT : AWPrefs.TEST_RIG_MODE_CLASS;
    }

    public String getTestRigText() {
        return testTextArea.getText();
    }

    public String getTestRigClass() {
        return testClassHiddenField.getText();
    }

    private String getDefaultTestRigByLanguage(String grammarLanguage) {
        try {
            if ("JAVA".equalsIgnoreCase(grammarLanguage)) {
                return Utils.stringFromFile(IDE.getApplicationPath() + File.separatorChar +
                        DBLocal.parserGlueCodeTemplatePath + DBLocal.parserGlueCodeTemplateName + ".st");
            } else if ("PYTHON".equalsIgnoreCase(grammarLanguage)) {
                return Utils.stringFromFile(IDE.getApplicationPath() + File.separatorChar +
                        DBLocal.parserGlueCodeTemplatePath + DBLocal.parserGlueCodeTemplateName + "_python.st");
            }
        } catch (IOException ioe) {
            this.debuggerTab.getConsole().println(ioe);
        }
        return "";
    }

    private String getTestRigTemplateFromFile(String testRigFullPath) {
        if (testRigFullPath == null) return "";
        try {
            return Utils.stringFromFile(testRigFullPath);
        } catch (IOException ioe) {
            return "";
        }
    }

    private void setTestRigTemplateToFile(String testRigFullPath, String text) {
        try {
            XJUtils.writeStringToFile(text, testRigFullPath);
        } catch (IOException ioe) {
            this.debuggerTab.getConsole().println(ioe);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - RP Talusan
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        textTestRadio = new JRadioButton();
        scrollPane1 = new JScrollPane();
        testTextArea = new JTextPane();
        classTestRadio = new JRadioButton();
        testClassField = new JTextField();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        testClassHiddenField = new JTextField();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        String title = "Edit Test Rig";
        if (qualifiedFileName != null && !"".equals(qualifiedFileName))
            title = "Edit " + XJUtils.getLastPathComponent(qualifiedFileName) + " Test Rig";
        if (grammarLanguage != null && !"".equals(grammarLanguage))
            title += " for " + grammarLanguage;
        setTitle(title);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(Borders.DIALOG_BORDER);
            dialogPane.setMinimumSize(new Dimension(340, 250));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new FormLayout(
                    new ColumnSpec[] {
                        new ColumnSpec(ColumnSpec.RIGHT, Sizes.DEFAULT, FormSpec.NO_GROW),
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC,
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
                    },
                    new RowSpec[] {
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC
                    }));

                //---- textTestRadio ----
                textTestRadio.setText("Text:");
                contentPanel.add(textTestRadio, cc.xy(1, 1));

                //======== scrollPane1 ========
                {
                    scrollPane1.setPreferredSize(new Dimension(300, 200));
                    scrollPane1.setViewportView(testTextArea);
                }
                contentPanel.add(scrollPane1, cc.xywh(3, 1, 3, 5));

                //---- classTestRadio ----
                classTestRadio.setText("Class:");
                contentPanel.add(classTestRadio, cc.xy(1, 7));

                //---- testClassField ----
                testClassField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        if (TEXT_FULLY_QUALIFIED_CLASS_NAME.equals(testClassField.getText())) {
                            testClassField.setForeground(Color.BLACK);
                            testClassField.setText("");
                        }
                    }
                    @Override
                    public void focusLost(FocusEvent e) {
                        testClassHiddenField.setText(testClassField.getText());
                        if ("".equals(testClassField.getText())) {
                            testClassField.setForeground(Color.LIGHT_GRAY);
                            testClassField.setText(TEXT_FULLY_QUALIFIED_CLASS_NAME);
                        }
                    }
                });
                contentPanel.add(testClassField, cc.xywh(3, 7, 3, 1));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

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
                buttonBar.add(okButton, cc.xy(2, 1));

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                buttonBar.add(cancelButton, cc.xy(4, 1));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        setSize(625, 395);

        //---- buttonGroup1 ----
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(textTestRadio);
        buttonGroup1.add(classTestRadio);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - RP Talusan
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JRadioButton textTestRadio;
    private JScrollPane scrollPane1;
    private JTextPane testTextArea;
    private JRadioButton classTestRadio;
    private JTextField testClassField;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    private JTextField testClassHiddenField;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
