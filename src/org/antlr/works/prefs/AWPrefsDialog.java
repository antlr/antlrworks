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

package org.antlr.works.prefs;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import edu.usfca.xj.appkit.app.XJApplication;
import edu.usfca.xj.appkit.app.XJPreferences;
import edu.usfca.xj.appkit.frame.XJPanel;
import edu.usfca.xj.appkit.swing.XJLookAndFeel;
import edu.usfca.xj.appkit.utils.XJFileChooser;
import edu.usfca.xj.foundation.notification.XJNotificationCenter;
import org.antlr.works.ate.syntax.ATEParserEngine;
import org.antlr.works.stats.Statistics;
import org.antlr.works.utils.HelpManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AWPrefsDialog extends XJPanel {

    public static final String NOTIF_PREFS_APPLIED = "NOTIF_PREFS_APPLIED";

    protected ButtonGroup compilerRadioButtonGroup;
    protected int lafIndex = 0;

    public AWPrefsDialog() {
        super();

        initComponents();
        setSize(700, 360);

        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                apply();
            }
        });

        foldingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionsFoldingAnchorsButton.setEnabled(foldingButton.isSelected());
            }
        });

        browseDotToolPathButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(XJFileChooser.shared().displayChooseDirectory(getJavaContainer())) {
                    dotToolPathField.setText(XJFileChooser.shared().getSelectedFilePath());
                    AWPrefs.setDOTToolPath(dotToolPathField.getText());
                }
            }
        });

        javacCustomPathButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                javacPathField.setEnabled(javacCustomPathButton.isSelected());
                browseJavacPath.setEnabled(javacCustomPathButton.isSelected());
            }
        });

        browseJavacPath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(XJFileChooser.shared().displayChooseDirectory(getJavaContainer())) {
                    javacPathField.setText(XJFileChooser.shared().getSelectedFilePath());
                    AWPrefs.setJavaCPath(javacPathField.getText());
                }
            }
        });

        browseJikesPath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(XJFileChooser.shared().displayChooseDirectory(getJavaContainer())) {
                    jikesPathField.setText(XJFileChooser.shared().getSelectedFilePath());
                    AWPrefs.setJikesPath(jikesPathField.getText());
                }
            }
        });

        browseUpdateDownloadPathButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(XJFileChooser.shared().displayChooseDirectory(getJavaContainer())) {
                    downloadPathField.setText(XJFileChooser.shared().getSelectedFilePath());
                    AWPrefs.setDownloadPath(downloadPathField.getText());
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

        fillColorComboBox(nonConsumedTokenColor);
        fillColorComboBox(consumedTokenColor);
        fillColorComboBox(hiddenTokenColor);
        fillColorComboBox(deadTokenColor);
        fillColorComboBox(lookaheadTokenColor);

        // General
        getPreferences().bindToPreferences(startupActionCombo, AWPrefs.PREF_STARTUP_ACTION, AWPrefs.STARTUP_OPEN_LAST_DOC);
        getPreferences().bindToPreferences(lafCombo, AWPrefs.PREF_LOOK_AND_FEEL, XJLookAndFeel.getDefaultLookAndFeelName());

        // Editor
        getPreferences().bindToPreferences(autoSaveButton, AWPrefs.PREF_AUTOSAVE_ENABLED, false);
        getPreferences().bindToPreferences(autoSaveDelayField, AWPrefs.PREF_AUTOSAVE_DELAY, 5);
        getPreferences().bindToPreferences(highlightCursorLineButton, AWPrefs.PREF_HIGHLIGHTCURSORLINE, true);
        getPreferences().bindToPreferences(tabWidthField, AWPrefs.PREF_TAB_WIDTH, AWPrefs.DEFAULT_TAB_WIDTH);
        getPreferences().bindToPreferences(editorFontCombo, AWPrefs.PREF_EDITOR_FONT, AWPrefs.DEFAULT_EDITOR_FONT);
        getPreferences().bindToPreferences(editorFontSizeSpinner, AWPrefs.PREF_EDITOR_FONT_SIZE, AWPrefs.DEFAULT_EDITOR_FONT_SIZE);
        getPreferences().bindToPreferences(parserDelayField, AWPrefs.PREF_PARSER_DELAY, AWPrefs.DEFAULT_PARSER_DELAY);
        getPreferences().bindToPreferences(foldingButton, AWPrefs.PREF_EDITOR_FOLDING, AWPrefs.DEFAULT_EDITOR_FOLDING);
        getPreferences().bindToPreferences(actionsFoldingAnchorsButton, AWPrefs.PREF_ACTIONS_ANCHORS_FOLDING, AWPrefs.DEFAULT_ACTIONS_ANCHORS_FOLDING);
        getPreferences().bindToPreferences(smoothScrollingButton, AWPrefs.PREF_SMOOTH_SCROLLING, AWPrefs.DEFAULT_SMOOTH_SCROLLING);

        // Visualization
        getPreferences().bindToPreferences(dotToolPathField, AWPrefs.PREF_DOT_TOOL_PATH, AWPrefs.DEFAULT_DOT_TOOL_PATH);

        // SCM - Perforce
        getPreferences().bindToPreferences(enablePerforceCheckBox, AWPrefs.PREF_SCM_P4_ENABLED, false);
        getPreferences().bindToPreferences(p4PortField, AWPrefs.PREF_SCM_P4_PORT, "");
        getPreferences().bindToPreferences(p4UserField, AWPrefs.PREF_SCM_P4_USER, "");
        getPreferences().bindToPreferences(p4PasswordField, AWPrefs.PREF_SCM_P4_PASSWORD, "");
        getPreferences().bindToPreferences(p4ClientField, AWPrefs.PREF_SCM_P4_CLIENT, "");
        getPreferences().bindToPreferences(p4ExecPathField, AWPrefs.PREF_SCM_P4_EXEC, "");

        // Compiler
        getPreferences().bindToPreferences(javacCustomPathButton, AWPrefs.PREF_JAVAC_CUSTOM_PATH, AWPrefs.DEFAULT_JAVAC_CUSTOM_PATH);
        getPreferences().bindToPreferences(javacPathField, AWPrefs.PREF_JAVAC_PATH, AWPrefs.DEFAULT_JAVAC_PATH);
        getPreferences().bindToPreferences(jikesPathField, AWPrefs.PREF_JIKES_PATH, AWPrefs.DEFAULT_JIKES_PATH);
        getPreferences().bindToPreferences(compilerRadioButtonGroup, AWPrefs.PREF_COMPILER, AWPrefs.DEFAULT_COMPILER);

        // Statistics
        getPreferences().bindToPreferences(reportTypeCombo, AWPrefs.PREF_STATS_REMINDER_METHOD, AWPrefs.DEFAULT_STATS_REMINDER_METHOD);

        // Updates
        getPreferences().bindToPreferences(updateTypeCombo, AWPrefs.PREF_UPDATE_TYPE, AWPrefs.DEFAULT_UPDATE_TYPE);
        getPreferences().bindToPreferences(downloadPathField, AWPrefs.PREF_DOWNLOAD_PATH, AWPrefs.DEFAULT_DOWNLOAD_PATH);

        // Colors
        getPreferences().bindToPreferences(nonConsumedTokenColor, AWPrefs.PREF_NONCONSUMED_TOKEN_COLOR, AWPrefs.DEFAULT_NONCONSUMED_TOKEN_COLOR);
        getPreferences().bindToPreferences(consumedTokenColor, AWPrefs.PREF_CONSUMED_TOKEN_COLOR, AWPrefs.DEFAULT_CONSUMED_TOKEN_COLOR);
        getPreferences().bindToPreferences(hiddenTokenColor, AWPrefs.PREF_HIDDEN_TOKEN_COLOR, AWPrefs.DEFAULT_HIDDEN_TOKEN_COLOR);
        getPreferences().bindToPreferences(deadTokenColor, AWPrefs.PREF_DEAD_TOKEN_COLOR, AWPrefs.DEFAULT_DEAD_TOKEN_COLOR);
        getPreferences().bindToPreferences(lookaheadTokenColor, AWPrefs.PREF_LOOKAHEAD_TOKEN_COLOR, AWPrefs.DEFAULT_LOOKAHEAD_TOKEN_COLOR);
    }

    public void becomingVisibleForTheFirstTime() {
        lafIndex = lafCombo.getSelectedIndex();
        javacPathField.setEnabled(javacCustomPathButton.isSelected());
        browseJavacPath.setEnabled(javacCustomPathButton.isSelected());
        actionsFoldingAnchorsButton.setEnabled(foldingButton.isSelected());
        Statistics.shared().recordEvent(Statistics.EVENT_SHOW_PREFERENCES);
    }

    public void close() {
        apply();
        super.close();
    }

    public boolean isAuxiliaryWindow() {
        return true;
    }

    public static void applyCommonPrefs() {
        // * WARNING *
        // This function is called at startup and when applying preferences
        ATEParserEngine.setDelay(AWPrefs.getParserDelay());
        XJApplication.setAutoSave(AWPrefs.getAutoSaveEnabled(), AWPrefs.getAutoSaveDelay());
    }

    private void apply() {
        dialogPane.requestFocus();
        getPreferences().applyPreferences();
        if(lafIndex != lafCombo.getSelectedIndex()) {
            lafIndex = lafCombo.getSelectedIndex();
            changeLookAndFeel();
        }
        applyCommonPrefs();
        XJNotificationCenter.defaultCenter().postNotification(this, NOTIF_PREFS_APPLIED);
    }

    private void changeLookAndFeel() {
        XJLookAndFeel.applyLookAndFeel(AWPrefs.getLookAndFeel());
    }

    private static XJPreferences getPreferences() {
        return XJApplication.shared().getPreferences();
    }

    private void fillColorComboBox(JComboBox cb) {
        cb.setRenderer(new ColoredRenderer());
        cb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                ColoredObject item = ((ColoredObject)cb.getSelectedItem());
                if(item != null)
                    cb.setForeground(item.getColor());
            }
        });

        cb.removeAllItems();

        cb.addItem(new ColoredObject(Color.BLACK, "Black"));
        cb.addItem(new ColoredObject(Color.BLUE, "Blue"));
        cb.addItem(new ColoredObject(Color.CYAN, "Cyan"));
        cb.addItem(new ColoredObject(Color.DARK_GRAY, "Dark gray"));
        cb.addItem(new ColoredObject(Color.GRAY, "Gray"));
        cb.addItem(new ColoredObject(Color.GREEN, "Green"));
        cb.addItem(new ColoredObject(Color.LIGHT_GRAY, "Light gray"));
        cb.addItem(new ColoredObject(Color.MAGENTA, "Magenta"));
        cb.addItem(new ColoredObject(Color.ORANGE, "Orange"));
        cb.addItem(new ColoredObject(Color.PINK, "Pink"));
        cb.addItem(new ColoredObject(Color.RED, "Red"));
        cb.addItem(new ColoredObject(Color.WHITE, "White"));
        cb.addItem(new ColoredObject(Color.YELLOW, "Yellow"));
    }

    class ColoredObject {
        Color color;
        Object object;
        public ColoredObject(Color color, Object object) {
            this.color=color;
            this.object=object;
        }
        public Object getObject() { return object; }
        public Color getColor() { return color; }
        public String toString() { return object.toString(); }
    }

    class ColoredRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean hasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index,
                    isSelected,hasFocus);
            c.setForeground(((ColoredObject)value).getColor());
            return c;
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPane = new JPanel();
        tabbedPane1 = new JTabbedPane();
        tabGeneral = new JPanel();
        label2 = new JLabel();
        label5 = new JLabel();
        lafCombo = new JComboBox();
        startupActionCombo = new JComboBox();
        label24 = new JLabel();
        dotToolPathField = new JTextField();
        browseDotToolPathButton = new JButton();
        tabEditor = new JPanel();
        label3 = new JLabel();
        editorFontCombo = new JComboBox();
        editorFontSizeSpinner = new JSpinner();
        autoSaveButton = new JCheckBox();
        autoSaveDelayField = new JTextField();
        label11 = new JLabel();
        highlightCursorLineButton = new JCheckBox();
        smoothScrollingButton = new JCheckBox();
        foldingButton = new JCheckBox();
        actionsFoldingAnchorsButton = new JCheckBox();
        label1 = new JLabel();
        tabWidthField = new JTextField();
        label22 = new JLabel();
        parserDelayField = new JTextField();
        label23 = new JLabel();
        tabCompiler = new JPanel();
        jikesRadio = new JRadioButton();
        integratedRadio = new JRadioButton();
        javacRadio = new JRadioButton();
        javacCustomPathButton = new JCheckBox();
        javacPathField = new JTextField();
        browseJavacPath = new JButton();
        label4 = new JLabel();
        jikesPathField = new JTextField();
        browseJikesPath = new JButton();
        tabDebugger = new JPanel();
        label12 = new JLabel();
        nonConsumedTokenColor = new JComboBox();
        label13 = new JLabel();
        consumedTokenColor = new JComboBox();
        label14 = new JLabel();
        hiddenTokenColor = new JComboBox();
        label15 = new JLabel();
        deadTokenColor = new JComboBox();
        label16 = new JLabel();
        lookaheadTokenColor = new JComboBox();
        tabSCM = new JPanel();
        enablePerforceCheckBox = new JCheckBox();
        label18 = new JLabel();
        p4PortField = new JTextField();
        label19 = new JLabel();
        p4UserField = new JTextField();
        label21 = new JLabel();
        p4PasswordField = new JPasswordField();
        label20 = new JLabel();
        p4ClientField = new JTextField();
        label17 = new JLabel();
        p4ExecPathField = new JTextField();
        tabStats = new JPanel();
        reportTypeCombo = new JComboBox();
        label6 = new JLabel();
        label8 = new JLabel();
        label9 = new JLabel();
        tabUpdates = new JPanel();
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
            dialogPane.setMinimumSize(new Dimension(540, 350));
            dialogPane.setPreferredSize(new Dimension(600, 380));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPane ========
            {
                contentPane.setLayout(new FormLayout(
                        "default, default:grow",
                        "fill:default:grow"));

                //======== tabbedPane1 ========
                {

                    //======== tabGeneral ========
                    {
                        tabGeneral.setLayout(new FormLayout(
                                new ColumnSpec[] {
                                        new ColumnSpec(Sizes.dluX(10)),
                                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                        new ColumnSpec(ColumnSpec.RIGHT, Sizes.DEFAULT, FormSpec.NO_GROW),
                                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                        new ColumnSpec("max(min;20dlu)"),
                                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                        new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                        new ColumnSpec("max(min;40dlu)"),
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
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC
                                }));

                        //---- label2 ----
                        label2.setText("At startup:");
                        tabGeneral.add(label2, cc.xy(3, 3));

                        //---- label5 ----
                        label5.setText("Look and feel:");
                        tabGeneral.add(label5, cc.xy(3, 5));
                        tabGeneral.add(lafCombo, cc.xywh(5, 5, 3, 1));

                        //---- startupActionCombo ----
                        startupActionCombo.setModel(new DefaultComboBoxModel(new String[] {
                                "Create a new document",
                                "Open the last used document"
                        }));
                        tabGeneral.add(startupActionCombo, cc.xywh(5, 3, 3, 1));

                        //---- label24 ----
                        label24.setText("DOT path:");
                        tabGeneral.add(label24, cc.xy(3, 7));

                        //---- dotToolPathField ----
                        dotToolPathField.setToolTipText("Absolute path to the DOT command-line tool");
                        tabGeneral.add(dotToolPathField, cc.xywh(5, 7, 3, 1));

                        //---- browseDotToolPathButton ----
                        browseDotToolPathButton.setText("Browse...");
                        tabGeneral.add(browseDotToolPathButton, cc.xy(9, 7));
                    }
                    tabbedPane1.addTab("General", tabGeneral);

                    //======== tabEditor ========
                    {
                        tabEditor.setLayout(new FormLayout(
                                new ColumnSpec[] {
                                        new ColumnSpec(Sizes.dluX(10)),
                                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                        new ColumnSpec(ColumnSpec.RIGHT, Sizes.DEFAULT, FormSpec.NO_GROW),
                                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                        new ColumnSpec(Sizes.dluX(20)),
                                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                        new ColumnSpec("max(default;45dlu)"),
                                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                        new ColumnSpec(Sizes.dluX(20)),
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
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        new RowSpec(Sizes.dluY(10))
                                }));

                        //---- label3 ----
                        label3.setText("Font:");
                        tabEditor.add(label3, cc.xy(3, 3));

                        //---- editorFontCombo ----
                        editorFontCombo.setActionCommand("editorFontCombo");
                        tabEditor.add(editorFontCombo, cc.xywh(5, 3, 5, 1));

                        //---- editorFontSizeSpinner ----
                        editorFontSizeSpinner.setModel(new SpinnerNumberModel(new Integer(12), new Integer(8), null, new Integer(1)));
                        tabEditor.add(editorFontSizeSpinner, cc.xy(11, 3));

                        //---- autoSaveButton ----
                        autoSaveButton.setText("Auto-save every");
                        tabEditor.add(autoSaveButton, cc.xywh(5, 5, 3, 1));
                        tabEditor.add(autoSaveDelayField, cc.xy(9, 5));

                        //---- label11 ----
                        label11.setText("minutes");
                        tabEditor.add(label11, cc.xy(11, 5));

                        //---- highlightCursorLineButton ----
                        highlightCursorLineButton.setText("Highlight cursor line");
                        tabEditor.add(highlightCursorLineButton, cc.xywh(5, 7, 5, 1));

                        //---- smoothScrollingButton ----
                        smoothScrollingButton.setText("Smooth scrolling");
                        tabEditor.add(smoothScrollingButton, cc.xywh(5, 9, 3, 1));

                        //---- foldingButton ----
                        foldingButton.setText("Enable folding");
                        tabEditor.add(foldingButton, cc.xywh(5, 11, 3, 1));

                        //---- actionsFoldingAnchorsButton ----
                        actionsFoldingAnchorsButton.setText("Display actions anchors");
                        tabEditor.add(actionsFoldingAnchorsButton, cc.xy(7, 13));

                        //---- label1 ----
                        label1.setHorizontalAlignment(SwingConstants.RIGHT);
                        label1.setText("Tab width:");
                        tabEditor.add(label1, cc.xy(3, 15));

                        //---- tabWidthField ----
                        tabWidthField.setText("8");
                        tabEditor.add(tabWidthField, cc.xy(5, 15));

                        //---- label22 ----
                        label22.setText("Parser delay:");
                        tabEditor.add(label22, cc.xy(3, 17));

                        //---- parserDelayField ----
                        parserDelayField.setText("250");
                        tabEditor.add(parserDelayField, cc.xy(5, 17));

                        //---- label23 ----
                        label23.setText("ms");
                        tabEditor.add(label23, cc.xy(7, 17));
                    }
                    tabbedPane1.addTab("Editor", tabEditor);

                    //======== tabCompiler ========
                    {
                        tabCompiler.setLayout(new FormLayout(
                                new ColumnSpec[] {
                                        new ColumnSpec(Sizes.dluX(10)),
                                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                        new ColumnSpec(Sizes.dluX(20)),
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
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC
                                }));

                        //---- jikesRadio ----
                        jikesRadio.setText("jikes");
                        tabCompiler.add(jikesRadio, cc.xywh(3, 7, 2, 1));

                        //---- integratedRadio ----
                        integratedRadio.setActionCommand("integrated");
                        integratedRadio.setText("com.sun.tools.javac");
                        tabCompiler.add(integratedRadio, cc.xywh(3, 11, 3, 1));

                        //---- javacRadio ----
                        javacRadio.setSelected(true);
                        javacRadio.setText("javac");
                        tabCompiler.add(javacRadio, cc.xywh(3, 3, 2, 1));

                        //---- javacCustomPathButton ----
                        javacCustomPathButton.setText("Path:");
                        javacCustomPathButton.setToolTipText("Check to specify a custom path if the default system path doesn't include javac");
                        tabCompiler.add(javacCustomPathButton, cc.xy(4, 5));
                        tabCompiler.add(javacPathField, cc.xy(5, 5));

                        //---- browseJavacPath ----
                        browseJavacPath.setText("Browse...");
                        tabCompiler.add(browseJavacPath, cc.xy(6, 5));

                        //---- label4 ----
                        label4.setText("Path:");
                        tabCompiler.add(label4, cc.xywh(4, 9, 1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
                        tabCompiler.add(jikesPathField, cc.xy(5, 9));

                        //---- browseJikesPath ----
                        browseJikesPath.setText("Browse...");
                        tabCompiler.add(browseJikesPath, cc.xy(6, 9));
                    }
                    tabbedPane1.addTab("Compiler", tabCompiler);

                    //======== tabDebugger ========
                    {
                        tabDebugger.setLayout(new FormLayout(
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
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC
                                }));

                        //---- label12 ----
                        label12.setHorizontalAlignment(SwingConstants.RIGHT);
                        label12.setText("Non-consumed token:");
                        tabDebugger.add(label12, cc.xy(3, 3));

                        //---- nonConsumedTokenColor ----
                        nonConsumedTokenColor.setModel(new DefaultComboBoxModel(new String[] {
                                "Black",
                                "Blue",
                                "Cyan"
                        }));
                        tabDebugger.add(nonConsumedTokenColor, cc.xy(5, 3));

                        //---- label13 ----
                        label13.setHorizontalAlignment(SwingConstants.RIGHT);
                        label13.setText("Consumed token:");
                        tabDebugger.add(label13, cc.xy(3, 5));
                        tabDebugger.add(consumedTokenColor, cc.xy(5, 5));

                        //---- label14 ----
                        label14.setHorizontalAlignment(SwingConstants.RIGHT);
                        label14.setText("Hidden token:");
                        tabDebugger.add(label14, cc.xy(3, 7));
                        tabDebugger.add(hiddenTokenColor, cc.xy(5, 7));

                        //---- label15 ----
                        label15.setHorizontalAlignment(SwingConstants.RIGHT);
                        label15.setText("Dead token:");
                        tabDebugger.add(label15, cc.xy(3, 9));
                        tabDebugger.add(deadTokenColor, cc.xy(5, 9));

                        //---- label16 ----
                        label16.setHorizontalAlignment(SwingConstants.RIGHT);
                        label16.setText("Lookahead token:");
                        tabDebugger.add(label16, cc.xy(3, 11));
                        tabDebugger.add(lookaheadTokenColor, cc.xy(5, 11));
                    }
                    tabbedPane1.addTab("Debugger", tabDebugger);

                    //======== tabSCM ========
                    {
                        tabSCM.setLayout(new FormLayout(
                                new ColumnSpec[] {
                                        new ColumnSpec(Sizes.dluX(10)),
                                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                        new ColumnSpec(ColumnSpec.RIGHT, Sizes.DEFAULT, FormSpec.NO_GROW),
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
                                        new RowSpec(Sizes.DLUY5),
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        new RowSpec(Sizes.DLUY5),
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        FormFactory.DEFAULT_ROWSPEC,
                                        FormFactory.LINE_GAP_ROWSPEC,
                                        new RowSpec(Sizes.dluY(10))
                                }));

                        //---- enablePerforceCheckBox ----
                        enablePerforceCheckBox.setText("Enable Perforce");
                        tabSCM.add(enablePerforceCheckBox, cc.xy(5, 3));

                        //---- label18 ----
                        label18.setText("Port:");
                        tabSCM.add(label18, cc.xy(3, 7));
                        tabSCM.add(p4PortField, cc.xy(5, 7));

                        //---- label19 ----
                        label19.setText("User:");
                        tabSCM.add(label19, cc.xy(3, 9));
                        tabSCM.add(p4UserField, cc.xy(5, 9));

                        //---- label21 ----
                        label21.setText("Password:");
                        tabSCM.add(label21, cc.xy(3, 11));
                        tabSCM.add(p4PasswordField, cc.xy(5, 11));

                        //---- label20 ----
                        label20.setText("Client:");
                        tabSCM.add(label20, cc.xy(3, 13));
                        tabSCM.add(p4ClientField, cc.xy(5, 13));

                        //---- label17 ----
                        label17.setText("P4 executable path:");
                        tabSCM.add(label17, cc.xy(3, 17));
                        tabSCM.add(p4ExecPathField, cc.xy(5, 17));
                    }
                    tabbedPane1.addTab("SCM", tabSCM);

                    //======== tabStats ========
                    {
                        tabStats.setLayout(new FormLayout(
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
                        tabStats.add(reportTypeCombo, cc.xywh(3, 5, 3, 1));

                        //---- label6 ----
                        label6.setText("Submit reports:");
                        tabStats.add(label6, cc.xy(3, 3));

                        //---- label8 ----
                        label8.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                        label8.setText("No personal or confidential information is sent.");
                        tabStats.add(label8, cc.xywh(3, 9, 3, 1));

                        //---- label9 ----
                        label9.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                        label9.setText("Each report is displayed in human readable form before being sent.");
                        tabStats.add(label9, cc.xywh(3, 11, 3, 1));
                    }
                    tabbedPane1.addTab("Statistics", tabStats);

                    //======== tabUpdates ========
                    {
                        tabUpdates.setLayout(new FormLayout(
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
                        tabUpdates.add(label7, cc.xy(3, 3));

                        //---- updateTypeCombo ----
                        updateTypeCombo.setModel(new DefaultComboBoxModel(new String[] {
                                "Manually",
                                "At startup",
                                "Daily",
                                "Weekly"
                        }));
                        tabUpdates.add(updateTypeCombo, cc.xywh(3, 5, 4, 1));

                        //---- checkForUpdatesButton ----
                        checkForUpdatesButton.setText("Check Now");
                        tabUpdates.add(checkForUpdatesButton, cc.xy(7, 5));

                        //---- label10 ----
                        label10.setHorizontalAlignment(SwingConstants.LEFT);
                        label10.setText("Download path:");
                        tabUpdates.add(label10, cc.xy(3, 9));
                        tabUpdates.add(downloadPathField, cc.xywh(3, 11, 3, 1));

                        //---- browseUpdateDownloadPathButton ----
                        browseUpdateDownloadPathButton.setActionCommand("Browse");
                        browseUpdateDownloadPathButton.setText("Browse...");
                        tabUpdates.add(browseUpdateDownloadPathButton, cc.xy(7, 11));
                    }
                    tabbedPane1.addTab("Updates", tabUpdates);
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
    private JPanel tabGeneral;
    private JLabel label2;
    private JLabel label5;
    private JComboBox lafCombo;
    private JComboBox startupActionCombo;
    private JLabel label24;
    private JTextField dotToolPathField;
    private JButton browseDotToolPathButton;
    private JPanel tabEditor;
    private JLabel label3;
    private JComboBox editorFontCombo;
    private JSpinner editorFontSizeSpinner;
    private JCheckBox autoSaveButton;
    private JTextField autoSaveDelayField;
    private JLabel label11;
    private JCheckBox highlightCursorLineButton;
    private JCheckBox smoothScrollingButton;
    private JCheckBox foldingButton;
    private JCheckBox actionsFoldingAnchorsButton;
    private JLabel label1;
    private JTextField tabWidthField;
    private JLabel label22;
    private JTextField parserDelayField;
    private JLabel label23;
    private JPanel tabCompiler;
    private JRadioButton jikesRadio;
    private JRadioButton integratedRadio;
    private JRadioButton javacRadio;
    private JCheckBox javacCustomPathButton;
    private JTextField javacPathField;
    private JButton browseJavacPath;
    private JLabel label4;
    private JTextField jikesPathField;
    private JButton browseJikesPath;
    private JPanel tabDebugger;
    private JLabel label12;
    private JComboBox nonConsumedTokenColor;
    private JLabel label13;
    private JComboBox consumedTokenColor;
    private JLabel label14;
    private JComboBox hiddenTokenColor;
    private JLabel label15;
    private JComboBox deadTokenColor;
    private JLabel label16;
    private JComboBox lookaheadTokenColor;
    private JPanel tabSCM;
    private JCheckBox enablePerforceCheckBox;
    private JLabel label18;
    private JTextField p4PortField;
    private JLabel label19;
    private JTextField p4UserField;
    private JLabel label21;
    private JPasswordField p4PasswordField;
    private JLabel label20;
    private JTextField p4ClientField;
    private JLabel label17;
    private JTextField p4ExecPathField;
    private JPanel tabStats;
    private JComboBox reportTypeCombo;
    private JLabel label6;
    private JLabel label8;
    private JLabel label9;
    private JPanel tabUpdates;
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
