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

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import org.antlr.Tool;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.works.utils.IconManager;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.frame.XJPanel;
import org.stringtemplate.v4.ST;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DialogAbout extends XJPanel {

    public InfoTableModel tableModel = new InfoTableModel();

    public DialogAbout() {
        initComponents();

        appIconButton.setIcon(IconManager.shared().getIconApplication());
        copyrightLabel.setText("Copyright (c) 2005-2012 Jean Bovet & Terence Parr");

        versionLabel.setText("Version "+XJApplication.getAppVersionLong());

        infoTable.setModel(tableModel);
        infoTable.getParent().setBackground(Color.white);

        tableModel.addInfo("ANTLRWorks", XJApplication.getAppVersionShort());
		tableModel.addInfo("ANTLR", new Tool().VERSION);
		tableModel.addInfo("StringTemplate v3", StringTemplate.VERSION);
		tableModel.addInfo("StringTemplate v4", ST.VERSION);
		tableModel.addInfo("Java", System.getProperty("java.version")+" ("+System.getProperty("java.vendor")+")");
        tableModel.fireTableDataChanged();

        resetAcknowledge();
        addAcknowledge("Contributions by RP Talusan <rp_talusan@yahoo.com>");
        addAcknowledge("ANTLR and StringTemplate are (c) 1989-2010 Terence Parr");
        addAcknowledge("gUnit is (c) 2008-2009 Leon Jen-Yuan Su");
        addAcknowledge("Application & Mac OS X document icons are (c) Simon Bovet");
        addAcknowledge("Portion of the GUI uses JGoodies, (c) 2002-2004 Karsten Lentzsch");
        addAcknowledge("Portion of the GUI was created using JFormDesigner, (c) 2004-2005 Karl Tauber");
        addAcknowledge("B-spline algorithm is (c) Leen Ammeraal <http://home.wxs.nl/~ammeraal/grjava.html>");
        addAcknowledge("BrowserLauncher is (c) 2001 Eric Albert <ejalbert@cs.stanford.edu>");

        setResizable(false);
        setSize(800, 500);
        center();
    }

    public void resetAcknowledge() {
        acknowledgeTextArea.setText("");
        //acknowledgeTextArea.setBackground(jFrame.getBackground());
        acknowledgeTextArea.setBackground(null);
    }

    public void addAcknowledge(String ack) {
        acknowledgeTextArea.setText(acknowledgeTextArea.getText()+"\n"+ack);
    }

    public boolean isAuxiliaryWindow() {
        return true;
    }

    public class InfoTableModel extends DefaultTableModel {

        public List<Object[]> info = new ArrayList<Object[]>();

        public Object getValueAt(int row, int column) {
            return (info.get(row))[column];
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public int getRowCount() {
            if(info == null)
                return 0;
            else
                return info.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int column) {
            if(column == 0)
                return "Name";
            else
                return "Version";
        }

        public void addInfo(String name, String version) {
            info.add(new Object[] { name, version });
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - ANTLR (www.antlr.org)
        appIconButton = new JButton();
        descriptionLabel = new JLabel();
        titleLabel = new JLabel();
        versionLabel = new JLabel();
        copyrightLabel = new JLabel();
        tabbedPane1 = new JTabbedPane();
        panel2 = new JPanel();
        acknowledgeTextArea = new JTextArea();
        panel1 = new JPanel();
        scrollPane1 = new JScrollPane();
        infoTable = new JTable();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setResizable(false);
        setTitle("About");
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
        	new ColumnSpec[] {
        		new ColumnSpec(Sizes.dluX(10)),
        		FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        		FormFactory.DEFAULT_COLSPEC,
        		FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        		new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
        		FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        		new ColumnSpec(Sizes.dluX(10))
        	},
        	new RowSpec[] {
        		new RowSpec(Sizes.dluY(10)),
        		FormFactory.LINE_GAP_ROWSPEC,
        		new RowSpec(RowSpec.BOTTOM, Sizes.DEFAULT, FormSpec.NO_GROW),
        		FormFactory.LINE_GAP_ROWSPEC,
        		new RowSpec("top:max(default;15dlu)"),
        		FormFactory.LINE_GAP_ROWSPEC,
        		new RowSpec("bottom:max(default;10dlu)"),
        		FormFactory.LINE_GAP_ROWSPEC,
        		new RowSpec("top:max(default;10dlu)"),
        		FormFactory.LINE_GAP_ROWSPEC,
        		new RowSpec("fill:max(default;60dlu):grow"),
        		FormFactory.LINE_GAP_ROWSPEC,
        		new RowSpec(Sizes.dluY(10))
        	}));

        //---- appIconButton ----
        appIconButton.setIcon(null);
        appIconButton.setBorderPainted(false);
        appIconButton.setContentAreaFilled(false);
        appIconButton.setDefaultCapable(false);
        appIconButton.setEnabled(true);
        appIconButton.setFocusable(false);
        appIconButton.setFocusPainted(false);
        appIconButton.setPreferredSize(new Dimension(124, 144));
        appIconButton.setMaximumSize(new Dimension(136, 144));
        appIconButton.setMinimumSize(new Dimension(136, 144));
        contentPane.add(appIconButton, cc.xywh(3, 3, 1, 8));

        //---- descriptionLabel ----
        descriptionLabel.setText("A graphical development environment for developing and debugging ANTLR v3 grammars");
        descriptionLabel.setHorizontalAlignment(SwingConstants.LEFT);
        descriptionLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        descriptionLabel.setVerticalTextPosition(SwingConstants.TOP);
        descriptionLabel.setVerticalAlignment(SwingConstants.TOP);
        contentPane.add(descriptionLabel, cc.xywh(5, 7, 1, 2));

        //---- titleLabel ----
        titleLabel.setText("ANTLRWorks");
        titleLabel.setFont(new Font("Lucida Grande", Font.BOLD, 36));
        contentPane.add(titleLabel, cc.xy(5, 3));

        //---- versionLabel ----
        versionLabel.setText("Version 1.0 early access 1");
        contentPane.add(versionLabel, cc.xy(5, 5));

        //---- copyrightLabel ----
        copyrightLabel.setText("Copyright (c) 2005 Jean Bovet & Terence Parr");
        contentPane.add(copyrightLabel, cc.xy(5, 9));

        //======== tabbedPane1 ========
        {

        	//======== panel2 ========
        	{
        		panel2.setLayout(new FormLayout(
        			new ColumnSpec[] {
        				new ColumnSpec(Sizes.dluX(10)),
        				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        				new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
        				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        				new ColumnSpec(Sizes.dluX(10))
        			},
        			new RowSpec[] {
        				new RowSpec(Sizes.dluY(10)),
        				FormFactory.LINE_GAP_ROWSPEC,
        				new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.NO_GROW),
        				FormFactory.LINE_GAP_ROWSPEC,
        				new RowSpec(Sizes.dluY(10))
        			}));

        		//---- acknowledgeTextArea ----
        		acknowledgeTextArea.setText("ANTLR and StringTemplate are (c) 1989-2005 Terence Parr\nXJLibrary is (c) 2004-2005 Jean Bovet\nPortion of the GUI uses JGoodies, (c) 2002-2004 Karsten Lentzsch\nPortion of the GUI was created using JFormDesigner, (c) 2004-2005 Karl Tauber\nBrowserLauncher is (c) 2001 Eric Albert <ejalbert@cs.stanford.edu>\nApplication icon is (c) Matthew McClintock <matthew@mc.clintock.com>\n");
        		acknowledgeTextArea.setEditable(false);
        		acknowledgeTextArea.setBackground(SystemColor.window);
        		panel2.add(acknowledgeTextArea, cc.xy(3, 3));
        	}
        	tabbedPane1.addTab("Acknowledgment", panel2);


        	//======== panel1 ========
        	{
        		panel1.setLayout(new FormLayout(
        			new ColumnSpec[] {
        				new ColumnSpec(Sizes.dluX(10)),
        				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        				new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
        				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        				new ColumnSpec(Sizes.dluX(10))
        			},
        			new RowSpec[] {
        				new RowSpec(Sizes.dluY(10)),
        				FormFactory.LINE_GAP_ROWSPEC,
        				new RowSpec(RowSpec.FILL, Sizes.dluY(10), FormSpec.DEFAULT_GROW),
        				FormFactory.LINE_GAP_ROWSPEC,
        				new RowSpec(Sizes.dluY(10))
        			}));

        		//======== scrollPane1 ========
        		{

        			//---- infoTable ----
        			infoTable.setModel(new DefaultTableModel(
        				new Object[][] {
        					{null, null},
        					{null, null},
        					{null, null},
        					{null, null},
        				},
        				new String[] {
        					"Name", "Version"
        				}
        			) {
        				boolean[] columnEditable = new boolean[] {
        					false, false
        				};
        				@Override
        				public boolean isCellEditable(int rowIndex, int columnIndex) {
        					return columnEditable[columnIndex];
        				}
        			});
        			infoTable.setShowVerticalLines(true);
        			scrollPane1.setViewportView(infoTable);
        		}
        		panel1.add(scrollPane1, cc.xy(3, 3));
        	}
        	tabbedPane1.addTab("Information", panel1);

        }
        contentPane.add(tabbedPane1, cc.xywh(3, 11, 3, 1));
        pack();
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Open Source Project license - ANTLR (www.antlr.org)
    private JButton appIconButton;
    private JLabel descriptionLabel;
    private JLabel titleLabel;
    private JLabel versionLabel;
    private JLabel copyrightLabel;
    private JTabbedPane tabbedPane1;
    private JPanel panel2;
    private JTextArea acknowledgeTextArea;
    private JPanel panel1;
    private JScrollPane scrollPane1;
    private JTable infoTable;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


}
