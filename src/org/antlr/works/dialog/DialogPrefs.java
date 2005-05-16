package org.antlr.works.dialog;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import org.antlr.works.editor.EditorPreferences;
import edu.usfca.xj.appkit.utils.XJFileChooser;
import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.appkit.frame.XJPanel;
import edu.usfca.xj.appkit.app.XJPreferences;
import edu.usfca.xj.appkit.app.XJApplication;

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

public class DialogPrefs extends XJPanel {

    public DialogPrefs() {

        initComponents();

        setSize(400, 200);

        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dialogPane.requestFocus();
                getPreferences().applyPreferences();
            }
        });

        getPreferences().bindToPreferences(startupActionCombo, EditorPreferences.PREF_STARTUP_ACTION, EditorPreferences.STARTUP_OPEN_LAST_DOC);
        getPreferences().bindToPreferences(tabWidthField, EditorPreferences.PREF_TAB_WIDTH, EditorPreferences.DEFAULT_TAB_WIDTH);
    }

    private static XJPreferences getPreferences() {
        return XJApplication.shared().getPreferences();
    }

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPane = new JPanel();
		label2 = new JLabel();
		startupActionCombo = new JComboBox();
		label1 = new JLabel();
		tabWidthField = new JTextField();
		buttonBar = new JPanel();
		applyButton = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
        setTitle("Preferences");        
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
						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
					},
					new RowSpec[] {
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC
					}));
				
				//---- label2 ----
				label2.setText("At startup:");
				contentPane.add(label2, cc.xy(1, 1));
				
				//---- startupActionCombo ----
				startupActionCombo.setModel(new DefaultComboBoxModel(new String[] {
					"Create a new document",
					"Open the last used document"
				}));
				contentPane.add(startupActionCombo, cc.xy(3, 1));
				
				//---- label1 ----
				label1.setHorizontalAlignment(SwingConstants.RIGHT);
				label1.setText("Tab width:");
				contentPane.add(label1, cc.xy(1, 3));
				contentPane.add(tabWidthField, cc.xy(3, 3));
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
				
				//---- applyButton ----
				applyButton.setText("Apply");
				buttonBar.add(applyButton, cc.xy(2, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane2.add(dialogPane, BorderLayout.CENTER);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPane;
	private JLabel label2;
	private JComboBox startupActionCombo;
	private JLabel label1;
	private JTextField tabWidthField;
	private JPanel buttonBar;
	private JButton applyButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
