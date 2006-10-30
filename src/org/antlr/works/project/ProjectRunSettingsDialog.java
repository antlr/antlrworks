package org.antlr.works.project;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.foundation.XJSystem;

import javax.swing.*;
import java.awt.*;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
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

public class ProjectRunSettingsDialog extends XJDialog {

    public ProjectRunSettingsDialog(Container parent) {
        super(parent, true);

        initComponents();
        setSize(550, 160);

        if(XJSystem.isMacOS()) {
            CellConstraints cc = new CellConstraints();

            buttonBar.remove(cancelButton);
            buttonBar.remove(okButton);

            buttonBar.add(cancelButton, cc.xy(2, 1));
            buttonBar.add(okButton, cc.xy(4, 1));
        }

        setDefaultButton(okButton);
        setOKButton(okButton);
        setCancelButton(cancelButton);
    }

    public void dialogWillCloseOK() {
    }

    public void setRunParametersString(String string) {
        runArgumentsField.setText(string);
    }

    public String getRunParametersString() {
        return runArgumentsField.getText();
    }

    public void setShowBeforeRunning(boolean flag) {
        showBeforeRunningButton.setSelected(flag);
    }

    public boolean isShowBeforeRunning() {
        return showBeforeRunningButton.isSelected();
    }

    /** Warning: OK and Cancel button are OS position dependant!
     *
     */

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - ANTLR (www.antlr.org)
        dialogPane = new JPanel();
        contentPane = new JPanel();
        label1 = new JLabel();
        runArgumentsField = new JTextField();
        label2 = new JLabel();
        showBeforeRunningButton = new JCheckBox();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setTitle("Run Settings");
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
        				new ColumnSpec("max(min;200dlu):grow")
        			},
        			new RowSpec[] {
        				FormFactory.DEFAULT_ROWSPEC,
        				FormFactory.LINE_GAP_ROWSPEC,
        				FormFactory.DEFAULT_ROWSPEC,
        				FormFactory.LINE_GAP_ROWSPEC,
        				FormFactory.DEFAULT_ROWSPEC,
        				FormFactory.LINE_GAP_ROWSPEC,
        				FormFactory.DEFAULT_ROWSPEC
        			}));

        		//---- label1 ----
        		label1.setText("Run arguments:");
        		contentPane.add(label1, cc.xy(1, 3));
        		contentPane.add(runArgumentsField, cc.xy(3, 3));

        		//---- label2 ----
        		label2.setText("(i.e. Test input.txt)");
        		label2.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
        		contentPane.add(label2, cc.xy(3, 5));

        		//---- showBeforeRunningButton ----
        		showBeforeRunningButton.setText("Show this dialog before running");
        		contentPane.add(showBeforeRunningButton, cc.xywh(1, 7, 3, 1));
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
        pack();
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Open Source Project license - ANTLR (www.antlr.org)
    private JPanel dialogPane;
    private JPanel contentPane;
    private JLabel label1;
    private JTextField runArgumentsField;
    private JLabel label2;
    private JCheckBox showBeforeRunningButton;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}
