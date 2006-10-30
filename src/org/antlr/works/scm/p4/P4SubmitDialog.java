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


package org.antlr.works.scm.p4;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.foundation.XJSystem;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class P4SubmitDialog extends XJDialog {

    public P4SubmitDialog(Container parent) {
        super(parent, true);

        initComponents();
        setSize(550, 500);

        if(XJSystem.isMacOS()) {
            CellConstraints cc = new CellConstraints();

            buttonBar.remove(cancelButton);
            buttonBar.remove(submitButton);

            buttonBar.add(cancelButton, cc.xy(2, 1));
            buttonBar.add(submitButton, cc.xy(4, 1));
        }

        setDefaultButton(submitButton);
        setOKButton(submitButton);
        setCancelButton(cancelButton);
    }

    public String getDescription() {
        return description.getText();
    }

    public boolean getRemainOpen() {
        return keepOpenButton.isSelected();
    }

    public void dialogWillDisplay() {
        description.setText("");
    }

    public boolean dialogCanCloseOK() {
        String text = description.getText();
        if(text.length() == 0) {
            XJAlert.display(getJavaComponent(), "Cannot Submit", "You must provide a description in order to submit.");
            return false;
        } else
            return true;
    }

    public void dialogWillCloseOK() {
        String text = description.getText();
        if(text.length() == 0) {
            XJAlert.display(getJavaComponent(), "Cannot Submit", "You must provide a description in order to submit.");
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - ANTLR (www.antlr.org)
        dialogPane = new JPanel();
        contentPane = new JPanel();
        label1 = new JLabel();
        description = new JTextArea();
        keepOpenButton = new JCheckBox();
        buttonBar = new JPanel();
        submitButton = new JButton();
        cancelButton = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setTitle("Submit File");
        Container contentPane2 = getContentPane();
        contentPane2.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
        	dialogPane.setBorder(Borders.DIALOG_BORDER);
        	dialogPane.setPreferredSize(new Dimension(500, 300));
        	dialogPane.setMinimumSize(new Dimension(300, 300));
        	dialogPane.setLayout(new BorderLayout());

        	//======== contentPane ========
        	{
        		contentPane.setLayout(new FormLayout(
        			new ColumnSpec[] {
        				new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
        				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        				FormFactory.DEFAULT_COLSPEC
        			},
        			new RowSpec[] {
        				FormFactory.DEFAULT_ROWSPEC,
        				FormFactory.LINE_GAP_ROWSPEC,
        				new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
        				FormFactory.LINE_GAP_ROWSPEC,
        				FormFactory.DEFAULT_ROWSPEC
        			}));

        		//---- label1 ----
        		label1.setText("Enter a description:");
        		contentPane.add(label1, cc.xy(1, 1));

        		//---- description ----
        		description.setBorder(LineBorder.createBlackLineBorder());
        		contentPane.add(description, cc.xywh(1, 3, 3, 1));

        		//---- keepOpenButton ----
        		keepOpenButton.setText("Keep file open after submit");
        		contentPane.add(keepOpenButton, cc.xy(1, 5));
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

        		//---- submitButton ----
        		submitButton.setText("Submit");
        		buttonBar.add(submitButton, cc.xy(2, 1));

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
    private JTextArea description;
    private JCheckBox keepOpenButton;
    private JPanel buttonBar;
    private JButton submitButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}
