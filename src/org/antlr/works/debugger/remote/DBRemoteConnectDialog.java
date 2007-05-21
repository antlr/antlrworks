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

package org.antlr.works.debugger.remote;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import org.antlr.xjlib.appkit.frame.XJDialog;
import org.antlr.xjlib.foundation.XJSystem;
import org.antlr.works.prefs.AWPrefs;

import javax.swing.*;
import java.awt.*;

public class DBRemoteConnectDialog extends XJDialog {

    public DBRemoteConnectDialog(Container parent) {
        super(parent, true);

        initComponents();
        setSize(337, 141);

        if(XJSystem.isMacOS()) {
            CellConstraints cc = new CellConstraints();

            buttonBar.remove(cancelButton);
            buttonBar.remove(connectButton);
            
            buttonBar.add(cancelButton, cc.xy(2, 1));
            buttonBar.add(connectButton, cc.xy(4, 1));
        }

        setDefaultButton(connectButton);
        setOKButton(connectButton);
        setCancelButton(cancelButton);
    }

    public void dialogWillDisplay() {
        portField.setText(String.valueOf(AWPrefs.getDebugDefaultLocalPort()));
    }

    public String getAddress() {
        return addressField.getText();
    }

    public int getPort() {
        return Integer.parseInt(portField.getText());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - ANTLR (www.antlr.org)
        dialogPane = new JPanel();
        contentPane = new JPanel();
        label1 = new JLabel();
        addressField = new JTextField();
        label2 = new JLabel();
        portField = new JTextField();
        buttonBar = new JPanel();
        connectButton = new JButton();
        cancelButton = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setTitle("Connect to Remote Parser");
        setResizable(false);
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
        				new ColumnSpec(Sizes.dluX(120))
        			},
        			new RowSpec[] {
        				FormFactory.DEFAULT_ROWSPEC,
        				FormFactory.LINE_GAP_ROWSPEC,
        				FormFactory.DEFAULT_ROWSPEC,
        				FormFactory.LINE_GAP_ROWSPEC,
        				FormFactory.DEFAULT_ROWSPEC
        			}));

        		//---- label1 ----
        		label1.setText("Address:");
        		label1.setHorizontalAlignment(SwingConstants.RIGHT);
        		contentPane.add(label1, cc.xy(1, 1));

        		//---- addressField ----
        		addressField.setText("localhost");
        		contentPane.add(addressField, cc.xy(3, 1));

        		//---- label2 ----
        		label2.setText("Port:");
        		label2.setHorizontalAlignment(SwingConstants.RIGHT);
        		contentPane.add(label2, cc.xy(1, 3));

        		//---- portField ----
        		portField.setText("2005");
        		contentPane.add(portField, cc.xy(3, 3));
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

        		//---- connectButton ----
        		connectButton.setText("Connect");
        		buttonBar.add(connectButton, cc.xy(2, 1));

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
    private JTextField addressField;
    private JLabel label2;
    private JTextField portField;
    private JPanel buttonBar;
    private JButton connectButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
