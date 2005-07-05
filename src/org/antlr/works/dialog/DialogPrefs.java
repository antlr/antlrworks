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
import edu.usfca.xj.appkit.app.XJApplication;
import edu.usfca.xj.appkit.app.XJPreferences;
import edu.usfca.xj.appkit.frame.XJPanel;
import edu.usfca.xj.appkit.swing.XJLookAndFeel;
import edu.usfca.xj.appkit.utils.XJFileChooser;
import edu.usfca.xj.foundation.notification.XJNotificationCenter;
import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.stats.Statistics;
import org.antlr.works.util.HelpManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DialogPrefs extends XJPanel {

    public static final String NOTIF_PREFS_APPLIED = "NOTIF_PREFS_APPLIED";

    protected ButtonGroup compilerRadioButtonGroup;
    protected int lafIndex = 0;

    public DialogPrefs() {

        initComponents();

        setSize(550, 300);

        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dialogPane.requestFocus();
                getPreferences().applyPreferences();
                if(lafIndex != lafCombo.getSelectedIndex()) {
                    lafIndex = lafCombo.getSelectedIndex();                    
                    changeLookAndFeel();
                }
                XJNotificationCenter.defaultCenter().postNotification(this, NOTIF_PREFS_APPLIED);
            }
        });

        browseJikesPath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(XJFileChooser.shared().displayChooseDirectory(getJavaContainer())) {
                    jikesPathField.setText(XJFileChooser.shared().getSelectedFilePath());
                    EditorPreferences.setJikesPath(jikesPathField.getText());
                }
            }
        });

        browseUpdateDownloadPathButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(XJFileChooser.shared().displayChooseDirectory(getJavaContainer())) {
                    downloadPathField.setText(XJFileChooser.shared().getSelectedFilePath());
                    EditorPreferences.setDownloadPath(downloadPathField.getText());
                }
            }
        });

        checkForUpdatesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                HelpManager.checkUpdates(getJavaContainer(), false);
            }
        });

        compilerRadioButtonGroup = new ButtonGroup();
        compilerRadioButtonGroup.add(jikesRadio);
        compilerRadioButtonGroup.add(integratedRadio);
        compilerRadioButtonGroup.add(javacRadio);

        integratedRadio.setActionCommand("integrated");
        javacRadio.setActionCommand("javac");
        jikesRadio.setActionCommand("jikes");

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String fontNames[] = ge.getAvailableFontFamilyNames();

        editorFontCombo.removeAllItems();
        for (int i=0; i<fontNames.length; i++) {
            editorFontCombo.addItem(fontNames[i]);
        }

        lafCombo.removeAllItems();
        UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
        for (int i=0; i<info.length; i++) {
            lafCombo.addItem(info[i].getName());
        }

        // General
        getPreferences().bindToPreferences(startupActionCombo, EditorPreferences.PREF_STARTUP_ACTION, EditorPreferences.STARTUP_OPEN_LAST_DOC);
        getPreferences().bindToPreferences(consoleShowCheckBox, EditorPreferences.PREF_CONSOLE_SHOW, EditorPreferences.DEFAULT_CONSOLE_SHOW);
        getPreferences().bindToPreferences(tabWidthField, EditorPreferences.PREF_TAB_WIDTH, EditorPreferences.DEFAULT_TAB_WIDTH);
        getPreferences().bindToPreferences(editorFontCombo, EditorPreferences.PREF_EDITOR_FONT, EditorPreferences.DEFAULT_EDITOR_FONT);
        getPreferences().bindToPreferences(editorFontSizeSpinner, EditorPreferences.PREF_EDITOR_FONT_SIZE, EditorPreferences.DEFAULT_EDITOR_FONT_SIZE);
        getPreferences().bindToPreferences(lafCombo, EditorPreferences.PREF_LOOK_AND_FEEL, XJLookAndFeel.getDefaultLookAndFeelName());

        // Compiler
        getPreferences().bindToPreferences(jikesPathField, EditorPreferences.PREF_JIKES_PATH, EditorPreferences.DEFAULT_JIKES_PATH);
        getPreferences().bindToPreferences(compilerRadioButtonGroup, EditorPreferences.PREF_COMPILER, EditorPreferences.DEFAULT_COMPILER);

        // Statistics
        getPreferences().bindToPreferences(reportTypeCombo, EditorPreferences.PREF_STATS_REMINDER_METHOD, EditorPreferences.DEFAULT_STATS_REMINDER_METHOD);

        // Updates
        getPreferences().bindToPreferences(updateTypeCombo, EditorPreferences.PREF_UPDATE_TYPE, EditorPreferences.DEFAULT_UPDATE_TYPE);
        getPreferences().bindToPreferences(downloadPathField, EditorPreferences.PREF_DOWNLOAD_PATH, EditorPreferences.DEFAULT_DOWNLOAD_PATH);
    }

    public void dialogWillDisplay() {
        lafIndex = lafCombo.getSelectedIndex();
        Statistics.shared().recordEvent(Statistics.EVENT_SHOW_PREFERENCES);
    }

    public boolean isAuxiliaryWindow() {
        return true;
    }

    private void changeLookAndFeel() {
        XJLookAndFeel.applyLookAndFeel(EditorPreferences.getLookAndFeel());
    }

    private static XJPreferences getPreferences() {
        return XJApplication.shared().getPreferences();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPane = new JPanel();
        tabbedPane1 = new JTabbedPane();
        panel1 = new JPanel();
        label2 = new JLabel();
        startupActionCombo = new JComboBox();
        label11 = new JLabel();
        consoleShowCheckBox = new JCheckBox();
        tabWidthField = new JTextField();
        label3 = new JLabel();
        editorFontCombo = new JComboBox();
        editorFontSizeSpinner = new JSpinner();
        label1 = new JLabel();
        label5 = new JLabel();
        lafCombo = new JComboBox();
        panel2 = new JPanel();
        jikesRadio = new JRadioButton();
        integratedRadio = new JRadioButton();
        javacRadio = new JRadioButton();
        label4 = new JLabel();
        jikesPathField = new JTextField();
        browseJikesPath = new JButton();
        panel3 = new JPanel();
        reportTypeCombo = new JComboBox();
        label6 = new JLabel();
        label8 = new JLabel();
        label9 = new JLabel();
        panel4 = new JPanel();
        label7 = new JLabel();
        updateTypeCombo = new JComboBox();
        checkForUpdatesButton = new JButton();
        label10 = new JLabel();
        downloadPathField = new JTextField();
        browseUpdateDownloadPathButton = new JButton();
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
            dialogPane.setPreferredSize(new Dimension(600, 300));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPane ========
            {
                contentPane.setLayout(new FormLayout(
                    "default, default:grow",
                    "fill:default:grow"));

                //======== tabbedPane1 ========
                {

                    //======== panel1 ========
                    {
                        panel1.setLayout(new FormLayout(
                            new ColumnSpec[] {
                                new ColumnSpec(Sizes.dluX(10)),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                new ColumnSpec(ColumnSpec.RIGHT, Sizes.DEFAULT, FormSpec.NO_GROW),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                new ColumnSpec(Sizes.dluX(20)),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                new ColumnSpec(Sizes.dluX(30)),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                new ColumnSpec(Sizes.dluX(10))
                            },
                            new RowSpec[] {
                                new RowSpec(Sizes.dluY(10)),
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
                            }));

                        //---- label2 ----
                        label2.setText("At startup:");
                        panel1.add(label2, cc.xy(3, 3));

                        //---- startupActionCombo ----
                        startupActionCombo.setModel(new DefaultComboBoxModel(new String[] {
                            "Create a new document",
                            "Open the last used document"
                        }));
                        panel1.add(startupActionCombo, cc.xywh(5, 3, 5, 1));

                        //---- label11 ----
                        label11.setText("Console:");
                        panel1.add(label11, cc.xy(3, 5));

                        //---- consoleShowCheckBox ----
                        consoleShowCheckBox.setText("Display automatically when message is written");
                        panel1.add(consoleShowCheckBox, cc.xywh(5, 5, 5, 1));

                        //---- tabWidthField ----
                        tabWidthField.setText("8");
                        panel1.add(tabWidthField, cc.xy(5, 7));

                        //---- label3 ----
                        label3.setText("Editor font:");
                        panel1.add(label3, cc.xy(3, 9));

                        //---- editorFontCombo ----
                        editorFontCombo.setActionCommand("editorFontCombo");
                        panel1.add(editorFontCombo, cc.xywh(5, 9, 3, 1));

                        //---- editorFontSizeSpinner ----
                        editorFontSizeSpinner.setModel(new SpinnerNumberModel(new Integer(12), new Integer(8), null, new Integer(1)));
                        panel1.add(editorFontSizeSpinner, cc.xy(9, 9));

                        //---- label1 ----
                        label1.setHorizontalAlignment(SwingConstants.RIGHT);
                        label1.setText("Editor tab width:");
                        panel1.add(label1, cc.xy(3, 7));

                        //---- label5 ----
                        label5.setText("Look and feel:");
                        panel1.add(label5, cc.xy(3, 11));
                        panel1.add(lafCombo, cc.xywh(5, 11, 5, 1));
                    }
                    tabbedPane1.addTab("General", panel1);

                    //======== panel2 ========
                    {
                        panel2.setLayout(new FormLayout(
                            new ColumnSpec[] {
                                new ColumnSpec(Sizes.dluX(10)),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                FormFactory.DEFAULT_COLSPEC,
                                new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                                FormFactory.DEFAULT_COLSPEC,
                                new ColumnSpec(Sizes.dluX(10))
                            },
                            new RowSpec[] {
                                new RowSpec(Sizes.dluY(10)),
                                FormFactory.LINE_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.LINE_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.LINE_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.LINE_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC
                            }));

                        //---- jikesRadio ----
                        jikesRadio.setText("jikes");
                        panel2.add(jikesRadio, cc.xy(3, 5));

                        //---- integratedRadio ----
                        integratedRadio.setActionCommand("integrated");
                        integratedRadio.setText("com.sun.tools.javac");
                        panel2.add(integratedRadio, cc.xywh(3, 9, 2, 1));

                        //---- javacRadio ----
                        javacRadio.setSelected(true);
                        javacRadio.setText("javac");
                        panel2.add(javacRadio, cc.xy(3, 3));

                        //---- label4 ----
                        label4.setText("Path:");
                        panel2.add(label4, cc.xywh(3, 7, 1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
                        panel2.add(jikesPathField, cc.xy(4, 7));

                        //---- browseJikesPath ----
                        browseJikesPath.setText("Browse...");
                        panel2.add(browseJikesPath, cc.xy(5, 7));
                    }
                    tabbedPane1.addTab("Compiler", panel2);

                    //======== panel3 ========
                    {
                        panel3.setLayout(new FormLayout(
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
                                FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.LINE_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.LINE_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC,
                                new RowSpec(RowSpec.TOP, Sizes.DLUY6, FormSpec.NO_GROW),
                                FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.LINE_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.LINE_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC
                            }));

                        //---- reportTypeCombo ----
                        reportTypeCombo.setModel(new DefaultComboBoxModel(new String[] {
                            "Manually",
                            "Remind me automatically each week"
                        }));
                        panel3.add(reportTypeCombo, cc.xywh(3, 5, 3, 1));

                        //---- label6 ----
                        label6.setText("Submit reports:");
                        panel3.add(label6, cc.xy(3, 3));

                        //---- label8 ----
                        label8.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                        label8.setText("No personal or confidential information is sent.");
                        panel3.add(label8, cc.xywh(3, 9, 3, 1));

                        //---- label9 ----
                        label9.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                        label9.setText("Each report is displayed in human readable form before being sent.");
                        panel3.add(label9, cc.xywh(3, 11, 3, 1));
                    }
                    tabbedPane1.addTab("Statistics", panel3);

                    //======== panel4 ========
                    {
                        panel4.setLayout(new FormLayout(
                            new ColumnSpec[] {
                                new ColumnSpec(Sizes.dluX(10)),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                FormFactory.DEFAULT_COLSPEC,
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                FormFactory.DEFAULT_COLSPEC,
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                new ColumnSpec(Sizes.dluX(10))
                            },
                            new RowSpec[] {
                                new RowSpec(Sizes.dluY(10)),
                                FormFactory.LINE_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.LINE_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.LINE_GAP_ROWSPEC,
                                new RowSpec(Sizes.dluY(10)),
                                FormFactory.LINE_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.LINE_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC
                            }));

                        //---- label7 ----
                        label7.setHorizontalAlignment(SwingConstants.LEFT);
                        label7.setText("Check for ANTLRWorks updates:");
                        panel4.add(label7, cc.xy(3, 3));

                        //---- updateTypeCombo ----
                        updateTypeCombo.setModel(new DefaultComboBoxModel(new String[] {
                            "Manually",
                            "At startup",
                            "Daily",
                            "Weekly"
                        }));
                        panel4.add(updateTypeCombo, cc.xywh(3, 5, 4, 1));

                        //---- checkForUpdatesButton ----
                        checkForUpdatesButton.setText("Check Now");
                        panel4.add(checkForUpdatesButton, cc.xy(7, 5));

                        //---- label10 ----
                        label10.setHorizontalAlignment(SwingConstants.LEFT);
                        label10.setText("Download path:");
                        panel4.add(label10, cc.xy(3, 9));
                        panel4.add(downloadPathField, cc.xywh(3, 11, 3, 1));

                        //---- browseUpdateDownloadPathButton ----
                        browseUpdateDownloadPathButton.setText("Browse");
                        panel4.add(browseUpdateDownloadPathButton, cc.xy(7, 11));
                    }
                    tabbedPane1.addTab("Updates", panel4);
                }
                contentPane.add(tabbedPane1, cc.xywh(1, 1, 2, 1));
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
    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JLabel label2;
    private JComboBox startupActionCombo;
    private JLabel label11;
    private JCheckBox consoleShowCheckBox;
    private JTextField tabWidthField;
    private JLabel label3;
    private JComboBox editorFontCombo;
    private JSpinner editorFontSizeSpinner;
    private JLabel label1;
    private JLabel label5;
    private JComboBox lafCombo;
    private JPanel panel2;
    private JRadioButton jikesRadio;
    private JRadioButton integratedRadio;
    private JRadioButton javacRadio;
    private JLabel label4;
    private JTextField jikesPathField;
    private JButton browseJikesPath;
    private JPanel panel3;
    private JComboBox reportTypeCombo;
    private JLabel label6;
    private JLabel label8;
    private JLabel label9;
    private JPanel panel4;
    private JLabel label7;
    private JComboBox updateTypeCombo;
    private JButton checkForUpdatesButton;
    private JLabel label10;
    private JTextField downloadPathField;
    private JButton browseUpdateDownloadPathButton;
    private JPanel buttonBar;
    private JButton applyButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


}
