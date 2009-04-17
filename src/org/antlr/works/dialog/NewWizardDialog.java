/*

[The "BSD licence"]
Copyright (c) 2009
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
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.antlr.xjlib.appkit.frame.XJDialog;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.foundation.XJSystem;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NewWizardDialog extends XJDialog {

    public static final int GRAMMAR_TYPE_PARSER = 0;
    public static final int GRAMMAR_TYPE_LEXER = 1;
    public static final int GRAMMAR_TYPE_TREE_GRAMMAR = 2;
    public static final int GRAMMAR_TYPE_COMBINED_GRAMMAR = 3;
    public static final String ST_TEMPLATE_PATHNAME = "templates/NewGrammarTemplate";

    public static final String ST_ATTR_GRAMMAR_NAME = "grammar_name";
    public static final String ST_ATTR_GRAMMAR_TYPE = "grammar_type";
    public static final String ST_ATTR_LEX_IDENTIFIER = "lex_id";
    public static final String ST_ATTR_LEX_INTEGER = "lex_int";
    public static final String ST_ATTR_LEX_FLOAT = "lex_float";
    public static final String ST_ATTR_LEX_COMMENT = "lex_comment";
    public static final String ST_ATTR_LEX_COMMENT_SINGLE = "lex_slcomment";
    public static final String ST_ATTR_LEX_COMMENT_MULTI = "lex_mlcomment";
    public static final String ST_ATTR_LEX_WHITESPACE = "lex_ws";
    public static final String ST_ATTR_LEX_TAB = "lex_tab";
    public static final String ST_ATTR_LEX_CARRIAGE_RETURN = "lex_cr";
    public static final String ST_ATTR_LEX_LINEFEED = "lex_lf";
    public static final String ST_ATTR_LEX_STRING_SINGLE = "lex_string_single";
    public static final String ST_ATTR_LEX_STRING_DOUBLE = "lex_string_double";
    public static final String ST_ATTR_LEX_CHAR = "lex_char";
    public static final String ST_ATTR_LEX_HAS_STRING_OR_CHAR = "has_string_char_literal";

    public NewWizardDialog(Container parent) {
        super(parent, true);
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
    }

    @Override
    public boolean dialogCanCloseOK() {
        if ("".equals(grammarNameField.getText().trim())) {
            XJAlert.display(getJavaComponent(), "Error", "Please enter a Grammar Name.");
            return false;
        }
        return true;
    }

    public String getGeneratedText() {
        StringTemplateGroup group = new StringTemplateGroup("NewGrammarGroup", DefaultTemplateLexer.class);
        StringTemplate glueCode = group.getInstanceOf(ST_TEMPLATE_PATHNAME);
        glueCode.setAttribute(ST_ATTR_GRAMMAR_NAME, grammarNameField.getText().trim());
        glueCode.setAttribute(ST_ATTR_GRAMMAR_TYPE, getGrammarType());

        if (grammarTypeComboBox.getSelectedIndex() == GRAMMAR_TYPE_LEXER ||
                grammarTypeComboBox.getSelectedIndex() == GRAMMAR_TYPE_COMBINED_GRAMMAR) {
            glueCode.setAttribute(ST_ATTR_LEX_IDENTIFIER, cbIdentifier.isSelected());
            glueCode.setAttribute(ST_ATTR_LEX_INTEGER, cbInteger.isSelected());
            glueCode.setAttribute(ST_ATTR_LEX_FLOAT, cbFloat.isSelected());
            glueCode.setAttribute(ST_ATTR_LEX_COMMENT, cbComments.isSelected());
            glueCode.setAttribute(ST_ATTR_LEX_COMMENT_SINGLE, cbComments.isSelected() && cbSingleLine.isSelected());
            glueCode.setAttribute(ST_ATTR_LEX_COMMENT_MULTI, cbComments.isSelected() && cbMultiLine.isSelected());
            glueCode.setAttribute(ST_ATTR_LEX_STRING_SINGLE, cbString.isSelected() && singleQuoteRadio.isSelected());
            glueCode.setAttribute(ST_ATTR_LEX_STRING_DOUBLE, cbString.isSelected() && doubleQuoteRadio.isSelected());
            glueCode.setAttribute(ST_ATTR_LEX_CHAR, cbCharacters.isSelected());
            glueCode.setAttribute(ST_ATTR_LEX_WHITESPACE, cbWhiteSpace.isSelected());
            glueCode.setAttribute(ST_ATTR_LEX_TAB, cbWhiteSpace.isSelected() && cbTabChar.isSelected());
            glueCode.setAttribute(ST_ATTR_LEX_CARRIAGE_RETURN, cbWhiteSpace.isSelected() && cbCarriageReturnChar.isSelected());
            glueCode.setAttribute(ST_ATTR_LEX_LINEFEED, cbWhiteSpace.isSelected() && cbNewlineChar.isSelected());
            glueCode.setAttribute(ST_ATTR_LEX_HAS_STRING_OR_CHAR, cbString.isSelected() || cbCharacters.isSelected());
        }

        return glueCode.toString();
    }

    private String getGrammarType() {
        switch (grammarTypeComboBox.getSelectedIndex()) {
            case GRAMMAR_TYPE_PARSER:
                return "parser grammar";
            case GRAMMAR_TYPE_LEXER:
                return "lexer grammar";
            case GRAMMAR_TYPE_TREE_GRAMMAR:
                return "tree grammar";
            case GRAMMAR_TYPE_COMBINED_GRAMMAR:
                return "grammar";
            default:
                return "grammar";
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Ryan Paul Talusan
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label1 = new JLabel();
        grammarNameField = new JTextField();
        label2 = new JLabel();
        grammarTypeComboBox = new JComboBox();
        lexicalItemPanel = new JPanel();
        lexLeftPanel = new JPanel();
        cbIdentifier = new JCheckBox();
        cbInteger = new JCheckBox();
        cbFloat = new JCheckBox();
        cbComments = new JCheckBox();
        commentsPanel = new JPanel();
        cbSingleLine = new JCheckBox();
        cbMultiLine = new JCheckBox();
        lexRightPanel = new JPanel();
        cbString = new JCheckBox();
        stringPanel = new JPanel();
        singleQuoteRadio = new JRadioButton();
        doubleQuoteRadio = new JRadioButton();
        cbCharacters = new JCheckBox();
        cbWhiteSpace = new JCheckBox();
        wsPanel = new JPanel();
        cbTabChar = new JCheckBox();
        cbNewlineChar = new JCheckBox();
        cbCarriageReturnChar = new JCheckBox();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setTitle("New Grammar Wizard");
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(Borders.DIALOG_BORDER);
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new FormLayout(
                        new ColumnSpec[] {
                                FormFactory.RELATED_GAP_COLSPEC,
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
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
                                FormFactory.UNRELATED_GAP_ROWSPEC,
                                new RowSpec(RowSpec.TOP, Sizes.DEFAULT, RowSpec.DEFAULT_GROW)
                        }
                ));

                //---- label1 ----
                label1.setText("Grammar Name:");
                contentPanel.add(label1, cc.xy(3, 1));
                contentPanel.add(grammarNameField, cc.xywh(5, 1, 3, 1));

                //---- label2 ----
                label2.setText("Type:");
                contentPanel.add(label2, cc.xy(3, 3));

                //---- grammarTypeComboBox ----
                grammarTypeComboBox.setModel(new DefaultComboBoxModel(new String[] {
                    "Parser",
                    "Lexer",
                    "Tree Grammar",
                    "Combined Grammar"
                }));
                grammarTypeComboBox.setSelectedIndex(3);
                grammarTypeComboBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        switch (grammarTypeComboBox.getSelectedIndex()) {
                            case GRAMMAR_TYPE_PARSER:
                            case GRAMMAR_TYPE_TREE_GRAMMAR:
                                cbIdentifier.setEnabled(false);
                                cbInteger.setEnabled(false);
                                cbFloat.setEnabled(false);
                                cbComments.setEnabled(false);
                                cbSingleLine.setEnabled(false);
                                cbMultiLine.setEnabled(false);
                                cbString.setEnabled(false);
                                cbCharacters.setEnabled(false);
                                cbWhiteSpace.setEnabled(false);
                                cbTabChar.setEnabled(false);
                                cbNewlineChar.setEnabled(false);
                                cbCarriageReturnChar.setEnabled(false);
                                break;
                            case GRAMMAR_TYPE_LEXER:
                            case GRAMMAR_TYPE_COMBINED_GRAMMAR:
                                cbIdentifier.setEnabled(true);
                                cbInteger.setEnabled(true);
                                cbFloat.setEnabled(true);
                                cbComments.setEnabled(true);
                                if (cbComments.isSelected()) {
                                    cbSingleLine.setEnabled(true);
                                    cbMultiLine.setEnabled(true);
                                }
                                cbString.setEnabled(true);
                                cbCharacters.setEnabled(true);
                                cbWhiteSpace.setEnabled(true);
                                if (cbWhiteSpace.isSelected()) {
                                    cbTabChar.setEnabled(true);
                                    cbNewlineChar.setEnabled(true);
                                    cbCarriageReturnChar.setEnabled(true);
                                }
                                break;
                        }
                    }
                });
                contentPanel.add(grammarTypeComboBox, cc.xy(5, 3));

                //======== lexicalItemPanel ========
                {
                    lexicalItemPanel.setBorder(new TitledBorder(null, "Lexical Items", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));
                    lexicalItemPanel.setLayout(new FormLayout(
                            new ColumnSpec[] {
                                    new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                                    FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                    new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
                            },
                            new RowSpec[] {
                                    new RowSpec(RowSpec.TOP, Sizes.DEFAULT, RowSpec.DEFAULT_GROW)
                            }
                    ));

                    //======== lexLeftPanel ========
                    {
                        lexLeftPanel.setLayout(new FormLayout(
                                new ColumnSpec[] {
                                        new ColumnSpec(ColumnSpec.DEFAULT, Sizes.dluX(10), FormSpec.NO_GROW),
                                        new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                        new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
                                },
                                new RowSpec[] {
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC
                                }
                        ));

                        //---- cbIdentifier ----
                        cbIdentifier.setText("Identifier");
                        lexLeftPanel.add(cbIdentifier, cc.xywh(1, 1, 2, 1));

                        //---- cbInteger ----
                        cbInteger.setText("Integer");
                        lexLeftPanel.add(cbInteger, cc.xywh(1, 3, 2, 1));

                        //---- cbFloat ----
                        cbFloat.setText("Float");
                        lexLeftPanel.add(cbFloat, cc.xywh(1, 5, 2, 1));

                        //---- cbComments ----
                        cbComments.setText("Comments");
                        lexLeftPanel.add(cbComments, cc.xywh(1, 7, 2, 1));
                        cbComments.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent event) {
                                if (cbComments.isSelected()) {
                                    cbSingleLine.setEnabled(true);
                                    cbMultiLine.setEnabled(true);
                                } else {
                                    cbSingleLine.setEnabled(false);
                                    cbMultiLine.setEnabled(false);
                                }
                            }
                        });

                        //======== commentsPanel ========
                        {
                            commentsPanel.setBorder(new TitledBorder(null, null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));
                            commentsPanel.setLayout(new FormLayout(
                                    new ColumnSpec[] {
                                            FormFactory.DEFAULT_COLSPEC
                                    },
                                    new RowSpec[] {
                                            FormFactory.DEFAULT_ROWSPEC,
                                            FormFactory.LINE_GAP_ROWSPEC,
                                            FormFactory.DEFAULT_ROWSPEC
                                    }
                            ));

                            //---- cbSingleLine ----
                            cbSingleLine.setText("Single-line ( //... )");
                            cbSingleLine.setEnabled(false);
                            cbSingleLine.setSelected(true);
                            commentsPanel.add(cbSingleLine, cc.xy(1, 1));
                            cbSingleLine.addActionListener(new ActionListener(){
                                public void actionPerformed(ActionEvent event) {
                                    if (!cbSingleLine.isSelected() && !cbMultiLine.isSelected()) {
                                        cbComments.setSelected(false);
                                        cbSingleLine.setEnabled(false);
                                        cbMultiLine.setEnabled(false);
                                        cbSingleLine.setSelected(true);
                                        cbMultiLine.setSelected(true);
                                    }
                                }
                            });

                            //---- cbMultiLine ----
                            cbMultiLine.setText("Multi-line ( /* .. */ )");
                            cbMultiLine.setEnabled(false);
                            cbMultiLine.setSelected(true);
                            commentsPanel.add(cbMultiLine, cc.xy(1, 3));
                            cbMultiLine.addActionListener(new ActionListener(){
                                public void actionPerformed(ActionEvent event) {
                                    if (!cbSingleLine.isSelected() && !cbMultiLine.isSelected()) {
                                        cbComments.setSelected(false);
                                        cbSingleLine.setEnabled(false);
                                        cbMultiLine.setEnabled(false);
                                        cbSingleLine.setSelected(true);
                                        cbMultiLine.setSelected(true);
                                    }
                                }
                            });
                        }
                        lexLeftPanel.add(commentsPanel, cc.xy(2, 9));
                    }
                    lexicalItemPanel.add(lexLeftPanel, cc.xy(1, 1));

                    //======== lexRightPanel ========
                    {
                        lexRightPanel.setLayout(new FormLayout(
                                new ColumnSpec[] {
                                        new ColumnSpec(ColumnSpec.DEFAULT, Sizes.dluX(10), FormSpec.NO_GROW),
                                        new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                        new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
                                },
                                new RowSpec[] {
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
                                }
                        ));

                        //---- cbString ----
                        cbString.setText("String");
                        lexRightPanel.add(cbString, cc.xywh(1, 1, 2, 1));
                        cbString.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent event) {
                                if (cbString.isSelected()) {
                                    singleQuoteRadio.setEnabled(true);
                                    doubleQuoteRadio.setEnabled(true);
                                } else {
                                    singleQuoteRadio.setEnabled(false);
                                    doubleQuoteRadio.setEnabled(false);
                                }
                            }
                        });

                        //======== stringPanel ========
                        {
                            stringPanel.setBorder(new EtchedBorder());
                            stringPanel.setLayout(new FormLayout(
                                    new ColumnSpec[] {
                                            FormFactory.DEFAULT_COLSPEC
                                    },
                                    new RowSpec[] {
                                            FormFactory.DEFAULT_ROWSPEC,
                                            FormFactory.LINE_GAP_ROWSPEC,
                                            FormFactory.DEFAULT_ROWSPEC
                                    }
                            ));

                            //---- singleQuoteRadio ----
                            singleQuoteRadio.setText("Single quotes ( 'sample string' )");
                            singleQuoteRadio.setEnabled(false);
                            stringPanel.add(singleQuoteRadio, cc.xy(1, 1));

                            //---- doubleQuoteRadio ----
                            doubleQuoteRadio.setText("Double quotes ( \"sample string\" )");
                            doubleQuoteRadio.setEnabled(false);
                            doubleQuoteRadio.setSelected(true);
                            stringPanel.add(doubleQuoteRadio, cc.xy(1, 3));
                        }
                        lexRightPanel.add(stringPanel, cc.xy(2, 3));

                        //---- cbCharacters ----
                        cbCharacters.setText("Character");
                        lexRightPanel.add(cbCharacters, cc.xywh(1, 5, 2, 1));

                        //---- cbWhiteSpace ----
                        cbWhiteSpace.setText("White Space");
                        lexRightPanel.add(cbWhiteSpace, cc.xywh(1, 7, 3, 1));

                        //======== wsPanel ========
                        {
                            wsPanel.setBorder(new EtchedBorder());
                            wsPanel.setLayout(new FormLayout(
                                    new ColumnSpec[] {
                                            FormFactory.DEFAULT_COLSPEC
                                    },
                                    new RowSpec[] {
                                            FormFactory.DEFAULT_ROWSPEC,
                                            FormFactory.LINE_GAP_ROWSPEC,
                                            FormFactory.DEFAULT_ROWSPEC,
                                            FormFactory.LINE_GAP_ROWSPEC,
                                            FormFactory.DEFAULT_ROWSPEC
                                    }
                            ));

                            //---- cbTabChar ----
                            cbTabChar.setText("Tab character ( /t )");
                            cbTabChar.setEnabled(false);
                            cbTabChar.setSelected(true);
                            wsPanel.add(cbTabChar, cc.xy(1, 1));

                            //---- cbNewlineChar ----
                            cbNewlineChar.setText("Newline character ( /n )");
                            cbNewlineChar.setEnabled(false);
                            cbNewlineChar.setSelected(true);
                            wsPanel.add(cbNewlineChar, cc.xy(1, 3));

                            //---- cbCarriageReturnChar ----
                            cbCarriageReturnChar.setText("Carriage-return character ( /r )");
                            cbCarriageReturnChar.setEnabled(false);
                            cbCarriageReturnChar.setSelected(true);
                            wsPanel.add(cbCarriageReturnChar, cc.xy(1, 5));
                        }
                        lexRightPanel.add(wsPanel, cc.xy(2, 9));
                    }
                    lexicalItemPanel.add(lexRightPanel, cc.xy(3, 1));
                }
                contentPanel.add(lexicalItemPanel, cc.xywh(3, 5, 5, 1));
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
        pack();

        //---- buttonGroup1 ----
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(singleQuoteRadio);
        buttonGroup1.add(doubleQuoteRadio);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ryan Paul Talusan
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel label1;
    private JTextField grammarNameField;
    private JLabel label2;
    private JComboBox grammarTypeComboBox;
    private JPanel lexicalItemPanel;
    private JPanel lexLeftPanel;
    private JCheckBox cbIdentifier;
    private JCheckBox cbInteger;
    private JCheckBox cbFloat;
    private JCheckBox cbComments;
    private JPanel commentsPanel;
    private JCheckBox cbSingleLine;
    private JCheckBox cbMultiLine;
    private JPanel lexRightPanel;
    private JCheckBox cbString;
    private JPanel stringPanel;
    private JRadioButton singleQuoteRadio;
    private JRadioButton doubleQuoteRadio;
    private JCheckBox cbCharacters;
    private JCheckBox cbWhiteSpace;
    private JPanel wsPanel;
    private JCheckBox cbTabChar;
    private JCheckBox cbNewlineChar;
    private JCheckBox cbCarriageReturnChar;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
