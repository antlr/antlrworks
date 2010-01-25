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
import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.TextUtils;
import org.antlr.works.utils.Utils;
import org.antlr.xjlib.appkit.frame.XJDialog;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.utils.XJFileChooser;
import org.antlr.xjlib.foundation.XJSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

public class DebuggerInputDialog extends XJDialog {

    private DebuggerTab debuggerTab;

    public DebuggerInputDialog(DebuggerTab debuggerTab, Container parent) {
        super(parent, true);

        this.debuggerTab = debuggerTab;

        initComponents();
        setSize(600, 400);

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

        TextUtils.createTabs(inputTextArea);
        TextUtils.setDefaultTextPaneProperties(inputTextArea);
        
        inputTextArea.setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        inputTextArea.setFocusable(true);
        inputTextArea.requestFocusInWindow();
        inputTextArea.setText(AWPrefs.getDebuggerInputText());

        rulesCombo.removeAllItems();
        for (ElementRule rule : debuggerTab.getSortedRules()) {
            rulesCombo.addItem(rule.name);
        }
        rulesCombo.setSelectedItem(debuggerTab.getStartRule());

        Utils.fillComboWithEOL(eolCombo);
        eolCombo.setSelectedIndex(AWPrefs.getDebuggerEOL());

        if(AWPrefs.getDebuggerInputMode() == 0) {
            textInputRadio.setSelected(true);
        } else {
            fileInputRadio.setSelected(true);
        }

        inputFileField.setText(AWPrefs.getDebuggerInputFile());

        browseInputFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if(XJFileChooser.shared().displayOpenDialog(getContentPane(), false)) {
                    inputFileField.setText(XJFileChooser.shared().getSelectedFilePath());
                    AWPrefs.setDebuggerInputFile(inputFileField.getText());
                    fileInputRadio.setSelected(true);
                }
            }
        });
    }

    public void dialogWillCloseOK() {
        String text = getRawInputText();
        if(text.length() > Preferences.MAX_VALUE_LENGTH) {
            XJAlert.display(getJavaComponent(), "Error", "The input text is too large to be stored in the preferences." +
                    " The text is "+text.length()+" bytes but the preferences can only hold "+Preferences.MAX_VALUE_LENGTH+" bytes. It will be truncated.");
            text = text.substring(0, Preferences.MAX_VALUE_LENGTH-1);
        }
        debuggerTab.setStartRule(getRule());
        AWPrefs.setDebuggerInputText(text);
        AWPrefs.setDebuggerEOL(eolCombo.getSelectedIndex());
        AWPrefs.setDebuggerInputMode(textInputRadio.isSelected()?0:1);
        AWPrefs.setDebuggerInputFile(inputFileField.getText());
    }

    public int getInputMode() {
        return textInputRadio.isSelected()?0:1;
    }
    
    public void setInputText(String text) {
        if(text != null) {
            inputTextArea.setText(text);
        }
    }

    public String getRawInputText() {
        return inputTextArea.getText();
    }

    public String getInputText() {
        return Utils.convertRawTextWithEOL(getRawInputText(), eolCombo);
    }

    public String getInputFile() {
        return inputFileField.getText();
    }

    public String getRule() {
        return (String)rulesCombo.getSelectedItem();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - ANTLR (www.antlr.org)
        dialogPane = new JPanel();
        contentPane = new JPanel();
        textInputRadio = new JRadioButton();
        scrollPane1 = new JScrollPane();
        inputTextArea = new JTextPane();
        fileInputRadio = new JRadioButton();
        inputFileField = new JTextField();
        browseInputFileButton = new JButton();
        label2 = new JLabel();
        rulesCombo = new JComboBox();
        label3 = new JLabel();
        eolCombo = new JComboBox();
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
        				new ColumnSpec(ColumnSpec.RIGHT, Sizes.DEFAULT, FormSpec.NO_GROW),
        				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        				FormFactory.DEFAULT_COLSPEC,
        				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        				new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
        				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        				FormFactory.DEFAULT_COLSPEC
        			},
        			new RowSpec[] {
        				FormFactory.DEFAULT_ROWSPEC,
        				FormFactory.LINE_GAP_ROWSPEC,
        				FormFactory.DEFAULT_ROWSPEC,
        				FormFactory.LINE_GAP_ROWSPEC,
        				new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
        				FormFactory.LINE_GAP_ROWSPEC,
        				FormFactory.DEFAULT_ROWSPEC,
        				FormFactory.LINE_GAP_ROWSPEC,
        				FormFactory.DEFAULT_ROWSPEC,
        				FormFactory.LINE_GAP_ROWSPEC,
        				FormFactory.DEFAULT_ROWSPEC
        			}));

        		//---- textInputRadio ----
        		textInputRadio.setText("Text:");
        		textInputRadio.setSelected(true);
        		contentPane.add(textInputRadio, cc.xy(1, 1));

        		//======== scrollPane1 ========
        		{
        			scrollPane1.setPreferredSize(new Dimension(300, 200));
        			scrollPane1.setViewportView(inputTextArea);
        		}
        		contentPane.add(scrollPane1, cc.xywh(3, 1, 5, 5));

        		//---- fileInputRadio ----
        		fileInputRadio.setText("File:");
        		contentPane.add(fileInputRadio, cc.xy(1, 7));
        		contentPane.add(inputFileField, cc.xywh(3, 7, 3, 1));

        		//---- browseInputFileButton ----
        		browseInputFileButton.setText("Browse...");
        		contentPane.add(browseInputFileButton, cc.xy(7, 7));

        		//---- label2 ----
        		label2.setText("Start Rule:");
        		contentPane.add(label2, cc.xywh(1, 9, 1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        		contentPane.add(rulesCombo, cc.xywh(3, 9, 5, 1));

        		//---- label3 ----
        		label3.setText("Line Endings:");
        		contentPane.add(label3, cc.xywh(1, 11, 1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));

        		//---- eolCombo ----
        		eolCombo.setModel(new DefaultComboBoxModel(new String[] {
        			"Unix (LF)",
        			"Mac (CR)",
        			"Windows (CRLF)"
        		}));
        		contentPane.add(eolCombo, cc.xy(3, 11));
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
        		buttonBar.add(okButton, cc.xy(2, 1));

        		//---- cancelButton ----
        		cancelButton.setText("Cancel");
        		buttonBar.add(cancelButton, cc.xy(4, 1));
        	}
        	dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane2.add(dialogPane, BorderLayout.CENTER);
        setSize(625, 395);

        //---- buttonGroup1 ----
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(textInputRadio);
        buttonGroup1.add(fileInputRadio);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Open Source Project license - ANTLR (www.antlr.org)
    private JPanel dialogPane;
    private JPanel contentPane;
    private JRadioButton textInputRadio;
    private JScrollPane scrollPane1;
    private JTextPane inputTextArea;
    private JRadioButton fileInputRadio;
    private JTextField inputFileField;
    private JButton browseInputFileButton;
    private JLabel label2;
    private JComboBox rulesCombo;
    private JLabel label3;
    private JComboBox eolCombo;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}
