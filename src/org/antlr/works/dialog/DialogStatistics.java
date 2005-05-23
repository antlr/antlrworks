package org.antlr.works.dialog;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import edu.usfca.xj.appkit.frame.XJDialog;
import org.antlr.works.util.Statistics;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

public class DialogStatistics extends XJDialog {

    protected AbstractTableModel model;

    public DialogStatistics() {
        initComponents();
        setSize(500, 400);
        
        setDefaultButton(okButton);
        setOKButton(okButton);

        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Statistics.shared().reset();
                updateInfo();
            }
        });
    }

    public void updateInfo() {
        label1.setText("Collecting statistics since "+Statistics.shared().getFromDate()+":");
        model.fireTableDataChanged();
    }

    public void dialogWillDisplay() {
        table.setModel(model = new AbstractTableModel() {
            public int getColumnCount() {
                return 2;
            }

            public int getRowCount() {
                return Statistics.shared().getCount();
            }

            public boolean isCellEditable(int row, int col) {
                return false;
            }

            public String getColumnName(int column) {
                switch(column) {
                    case 0:
                        return "Event";
                    case 1:
                        return "Count";
                }
                return "";
            }

            public Object getValueAt(int row, int col) {
                switch(col) {
                    case 0:
                        return Statistics.shared().getEventName(row);
                    case 1:
                        return Statistics.shared().getEventCount(row);
                }
                return null;
            }

            public void setValueAt(Object value, int row, int col) {
            }
        });

        table.getColumnModel().getColumn(1).setMaxWidth(40);
        updateInfo();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPane = new JPanel();
        label1 = new JLabel();
        scrollPane1 = new JScrollPane();
        table = new JTable();
        buttonBar = new JPanel();
        resetButton = new JButton();
        okButton = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setTitle("Statistics");
        Container contentPane2 = getContentPane();
        contentPane2.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(Borders.DIALOG_BORDER);
            dialogPane.setPreferredSize(new Dimension(400, 400));
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
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC
                    }));

                //---- label1 ----
                label1.setText("Current statistics per event:");
                contentPane.add(label1, cc.xy(1, 1));

                //======== scrollPane1 ========
                {

                    //---- table ----
                    table.setCellSelectionEnabled(false);
                    table.setModel(new DefaultTableModel(
                        new Object[][] {
                            {null, null},
                            {null, ""},
                        },
                        new String[] {
                            "Event", "Count"
                        }
                    ) {
                        boolean[] columnEditable = new boolean[] {
                            false, false
                        };
                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                            return columnEditable[columnIndex];
                        }
                    });
                    table.setShowVerticalLines(false);
                    scrollPane1.setViewportView(table);
                }
                contentPane.add(scrollPane1, cc.xywh(1, 3, 3, 4));
            }
            dialogPane.add(contentPane, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
                buttonBar.setLayout(new FormLayout(
                    new ColumnSpec[] {
                        FormFactory.GLUE_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC,
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        FormFactory.BUTTON_COLSPEC
                    },
                    RowSpec.decodeSpecs("pref")));

                //---- resetButton ----
                resetButton.setText("Reset");
                buttonBar.add(resetButton, cc.xy(2, 1));

                //---- okButton ----
                okButton.setText("OK");
                buttonBar.add(okButton, cc.xy(4, 1));
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
    private JScrollPane scrollPane1;
    private JTable table;
    private JPanel buttonBar;
    private JButton resetButton;
    private JButton okButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}
