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
import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.foundation.XJSystem;
import org.antlr.works.stats.StatisticsManager;
import org.antlr.works.stats.StatisticsReporter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DialogReports extends XJDialog {

    protected StatisticsManager guiManager;
    protected StatisticsManager grammarManager;
    protected StatisticsManager runtimeManager;

    public DialogReports(Container parent) {
        super(parent, true);

        initComponents();
        setSize(550, 500);

        setDefaultButton(submitButton);
        setOKButton(submitButton);
        setCancelButton(cancelButton);

        humanFormatCheck.setSelected(true);
        statsTextArea.setTabSize(2);

        typeCombo.addActionListener(new MyActionListener());
        humanFormatCheck.addActionListener(new MyActionListener());
        allRadioButton.addActionListener(new MyActionListener());
        oneByOneRadio.addActionListener(new MyActionListener());
        currentSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                updateInfo(true);
            }
        });
    }

    public void dialogWillDisplay() {
        guiManager = new StatisticsManager(StatisticsManager.TYPE_GUI);
        grammarManager = new StatisticsManager(StatisticsManager.TYPE_GRAMMAR);
        runtimeManager = new StatisticsManager(StatisticsManager.TYPE_RUNTIME);

        updateInfo(false);
    }

    public void dialogWillCloseOK() {
        StatisticsReporter reporter = new StatisticsReporter(parent);
        if(!reporter.submitGUI(guiManager)) {
            XJAlert.display(getJavaComponent(), "Submission failed", "An error has occurred when sending the ANTLRWorks statistics:\n"+reporter.getError());
            return;
        }
        if(!reporter.submitGrammar(grammarManager)) {
            XJAlert.display(getJavaComponent(), "Submission failed", "An error has occurred when sending the ANTLR grammar statistics:\n"+reporter.getError());
            return;
        }
        if(!reporter.submitRuntime(runtimeManager)) {
            XJAlert.display(getJavaComponent(), "Submission failed", "An error has occurred when sending the ANTLR runtime statistics:\n"+reporter.getError());
            return;
        }

        guiManager.reset();
        grammarManager.reset();
        runtimeManager.reset();

        XJAlert.display(getJavaComponent(), "Thank you", "The statistics have been successfully transmitted. Thank you.");
    }

    protected void updateInfo(boolean textOnly) {
        StatisticsManager sm = null;
        boolean rangeEnabled = true;

        switch(typeCombo.getSelectedIndex()) {
            case 0:
                rangeEnabled = false;
                sm = guiManager;
                break;
            case 1:
                rangeEnabled = true;
                sm = grammarManager;
                break;
            case 2:
                rangeEnabled = true;
                sm = runtimeManager;
                break;
        }

        if(!textOnly)
            setRangeEnabled(rangeEnabled, sm);

        setText(sm);

        currentSpinner.setEnabled(oneByOneRadio.isSelected());
        infoLabel.setEnabled(oneByOneRadio.isSelected());
    }

    protected void setRangeEnabled(boolean flag, StatisticsManager sm) {
        allRadioButton.setEnabled(flag);
        oneByOneRadio.setEnabled(flag);
        currentSpinner.setEnabled(flag);
        infoLabel.setEnabled(flag);

        currentSpinner.setValue(new Integer(1));
        SpinnerNumberModel spm = (SpinnerNumberModel)currentSpinner.getModel();
        spm.setMaximum(new Integer(sm.getStatsCount()));

        if(flag)
            infoLabel.setText("of "+sm.getStatsCount());
        else
            infoLabel.setText("");
    }

    protected void setText(StatisticsManager sm) {
        if(allRadioButton.isSelected()) {
            boolean humanReadable = isHumanReadable();
            boolean multiple = sm.getStatsCount() > 1;

            StringBuffer s = new StringBuffer();
            if(sm.getStatsCount() == 0) {
                s.append("* no statistics available *");
            } else {
                for(int i=0; i<sm.getStatsCount(); i++) {
                    if(humanReadable && multiple)
                        s.append("\n**** ").append(i + 1).append(" ****\n\n");
                    s.append(isHumanReadable() ? sm.getReadableString(i) : sm.getRawString(i));
                    if(!humanReadable && multiple)
                        s.append('\n');
                }
            }
            statsTextArea.setText(s.toString());
        } else {
            int index = ((Integer)currentSpinner.getValue()).intValue()-1;
            String text = isHumanReadable() ? sm.getReadableString(index) : sm.getRawString(index);
            if(text == null)
                text = "* no statistics available *";

            statsTextArea.setText(text);
        }
        statsTextArea.setCaretPosition(0);
    }

    protected boolean isHumanReadable() {
        return humanFormatCheck.isSelected();
    }

    protected class MyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            updateInfo(false);
        }
    }

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPane = new JPanel();
		label1 = new JLabel();
		typeCombo = new JComboBox();
		humanFormatCheck = new JCheckBox();
		label2 = new JLabel();
		allRadioButton = new JRadioButton();
		currentSpinner = new JSpinner();
		oneByOneRadio = new JRadioButton();
		infoLabel = new JLabel();
		scrollPane1 = new JScrollPane();
		statsTextArea = new JTextArea();
		buttonBar = new JPanel();
		submitButton = new JButton();
		cancelButton = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setTitle("Review reports before submission");
		Container contentPane2 = getContentPane();
		contentPane2.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setPreferredSize(new Dimension(500, 500));
			dialogPane.setLayout(new BorderLayout());
			
			//======== contentPane ========
			{
				contentPane.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						new ColumnSpec("max(min;30dlu)"),
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
						new RowSpec(RowSpec.CENTER, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
					}));
				
				//---- label1 ----
				label1.setHorizontalAlignment(SwingConstants.RIGHT);
				label1.setText("View report for:");
				contentPane.add(label1, cc.xy(1, 1));
				
				//---- typeCombo ----
				typeCombo.setModel(new DefaultComboBoxModel(new String[] {
					"ANTLRWorks",
					"ANTLR - grammar",
					"ANTLR - runtime"
				}));
				contentPane.add(typeCombo, cc.xywh(3, 1, 5, 1));
				
				//---- humanFormatCheck ----
				humanFormatCheck.setText("Human readable format");
				contentPane.add(humanFormatCheck, cc.xywh(3, 3, 5, 1));
				
				//---- label2 ----
				label2.setHorizontalAlignment(SwingConstants.RIGHT);
				label2.setText("Range:");
				contentPane.add(label2, cc.xy(1, 5));
				
				//---- allRadioButton ----
				allRadioButton.setSelected(true);
				allRadioButton.setText("All");
				contentPane.add(allRadioButton, cc.xy(3, 5));
				
				//---- currentSpinner ----
				currentSpinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
				contentPane.add(currentSpinner, cc.xy(7, 5));
				
				//---- oneByOneRadio ----
				oneByOneRadio.setText("One by one");
				contentPane.add(oneByOneRadio, cc.xy(5, 5));
				
				//---- infoLabel ----
				infoLabel.setText("of 999");
				contentPane.add(infoLabel, cc.xy(9, 5));
				
				//======== scrollPane1 ========
				{
					
					//---- statsTextArea ----
					statsTextArea.setEditable(false);
					scrollPane1.setViewportView(statsTextArea);
				}
				contentPane.add(scrollPane1, cc.xywh(1, 7, 9, 3));
			}
			dialogPane.add(contentPane, BorderLayout.CENTER);
			
			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				buttonBar.setLayout(new FormLayout(
					new ColumnSpec[] {
						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.BUTTON_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.BUTTON_COLSPEC
					},
					RowSpec.decodeSpecs("pref")));
				
				//---- submitButton ----
				submitButton.setText("Submit");

				//---- cancelButton ----
				cancelButton.setText("Cancel");

                if(XJSystem.isMacOS()) {
                    buttonBar.add(cancelButton, cc.xy(3, 1));
                    buttonBar.add(submitButton, cc.xy(5, 1));
                } else {
                    buttonBar.add(submitButton, cc.xy(3, 1));
                    buttonBar.add(cancelButton, cc.xy(5, 1));
                }

			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane2.add(dialogPane, BorderLayout.CENTER);

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(allRadioButton);
		buttonGroup1.add(oneByOneRadio);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPane;
	private JLabel label1;
	private JComboBox typeCombo;
	private JCheckBox humanFormatCheck;
	private JLabel label2;
	private JRadioButton allRadioButton;
	private JSpinner currentSpinner;
	private JRadioButton oneByOneRadio;
	private JLabel infoLabel;
	private JScrollPane scrollPane1;
	private JTextArea statsTextArea;
	private JPanel buttonBar;
	private JButton submitButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
