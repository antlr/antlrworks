package org.antlr.works.dialog;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import edu.usfca.xj.appkit.app.XJApplication;
import edu.usfca.xj.appkit.frame.XJDialog;

import javax.swing.*;
import java.awt.*;
/*
 * Created by JFormDesigner on Sat Apr 02 12:18:59 PST 2005
 */

public class DialogConnectDebugRemote extends XJDialog {

	public DialogConnectDebugRemote() {
		initComponents();

        setDefaultButton(connectButton);
        setOKButton(connectButton);
        setCancelButton(cancelButton);
	}

    public String getAddress() {
        return addressField.getText();
    }

    public int getPort() {
        return Integer.parseInt(portField.getText());
    }

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
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
		setResizable(false);
		setTitle("Connect to Remote Parser");
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
				label1.setHorizontalAlignment(SwingConstants.RIGHT);
				label1.setText("Address:");
				contentPane.add(label1, cc.xy(1, 1));
				
				//---- addressField ----
				addressField.setText("localhost");
				contentPane.add(addressField, cc.xy(3, 1));
				
				//---- label2 ----
				label2.setHorizontalAlignment(SwingConstants.RIGHT);
				label2.setText("Port:");
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

				//---- cancelButton ----
				cancelButton.setText("Cancel");

                if(XJApplication.isMacOS()) {
                    buttonBar.add(cancelButton, cc.xy(2, 1));
                    buttonBar.add(connectButton, cc.xy(4, 1));
                } else {
                    buttonBar.add(connectButton, cc.xy(2, 1));
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
	private JTextField addressField;
	private JLabel label2;
	private JTextField portField;
	private JPanel buttonBar;
	private JButton connectButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
