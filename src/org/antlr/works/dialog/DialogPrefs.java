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
import org.antlr.works.parser.ThreadedParser;
import org.antlr.works.stats.Statistics;
import org.antlr.works.util.HelpManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DialogPrefs extends XJPanel {

    public static final String NOTIF_PREFS_APPLIED = "NOTIF_PREFS_APPLIED";

    protected ButtonGroup compilerRadioButtonGroup;
    protected int lafIndex = 0;

    public DialogPrefs() {
        super();

        initComponents();
        setSize(700, 360);

        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                apply();
            }
        });

        browseDotToolPathButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(XJFileChooser.shared().displayChooseDirectory(getJavaContainer())) {
                    dotToolPathField.setText(XJFileChooser.shared().getSelectedFilePath());
                    EditorPreferences.setDOTToolPath(dotToolPathField.getText());
                }
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

        fillColorComboBox(nonConsumedTokenColor);
        fillColorComboBox(consumedTokenColor);
        fillColorComboBox(hiddenTokenColor);
        fillColorComboBox(deadTokenColor);
        fillColorComboBox(lookaheadTokenColor);

        // General
        getPreferences().bindToPreferences(startupActionCombo, EditorPreferences.PREF_STARTUP_ACTION, EditorPreferences.STARTUP_OPEN_LAST_DOC);
        getPreferences().bindToPreferences(lafCombo, EditorPreferences.PREF_LOOK_AND_FEEL, XJLookAndFeel.getDefaultLookAndFeelName());

        // Editor
        getPreferences().bindToPreferences(autoSaveButton, EditorPreferences.PREF_AUTOSAVE_ENABLED, false);
        getPreferences().bindToPreferences(autoSaveDelayField, EditorPreferences.PREF_AUTOSAVE_DELAY, 5);
        getPreferences().bindToPreferences(highlightCursorLineButton, EditorPreferences.PREF_HIGHLIGHTCURSORLINE, true);
        getPreferences().bindToPreferences(tabWidthField, EditorPreferences.PREF_TAB_WIDTH, EditorPreferences.DEFAULT_TAB_WIDTH);
        getPreferences().bindToPreferences(editorFontCombo, EditorPreferences.PREF_EDITOR_FONT, EditorPreferences.DEFAULT_EDITOR_FONT);
        getPreferences().bindToPreferences(editorFontSizeSpinner, EditorPreferences.PREF_EDITOR_FONT_SIZE, EditorPreferences.DEFAULT_EDITOR_FONT_SIZE);
        getPreferences().bindToPreferences(parserDelayField, EditorPreferences.PREF_PARSER_DELAY, EditorPreferences.DEFAULT_PARSER_DELAY);

        // Visualization
        getPreferences().bindToPreferences(dotToolPathField, EditorPreferences.PREF_DOT_TOOL_PATH, EditorPreferences.DEFAULT_DOT_TOOL_PATH);
        getPreferences().bindToPreferences(dotImageFormatField, EditorPreferences.PREF_DOT_IMAGE_FORMAT, EditorPreferences.DEFAULT_DOT_IMAGE_FORMAT);

        // SCM - Perforce
        getPreferences().bindToPreferences(enablePerforceCheckBox, EditorPreferences.PREF_SCM_P4_ENABLED, false);
        getPreferences().bindToPreferences(p4PortField, EditorPreferences.PREF_SCM_P4_PORT, "");
        getPreferences().bindToPreferences(p4UserField, EditorPreferences.PREF_SCM_P4_USER, "");
        getPreferences().bindToPreferences(p4PasswordField, EditorPreferences.PREF_SCM_P4_PASSWORD, "");
        getPreferences().bindToPreferences(p4ClientField, EditorPreferences.PREF_SCM_P4_CLIENT, "");
        getPreferences().bindToPreferences(p4ExecPathField, EditorPreferences.PREF_SCM_P4_EXEC, "");

        // Compiler
        getPreferences().bindToPreferences(jikesPathField, EditorPreferences.PREF_JIKES_PATH, EditorPreferences.DEFAULT_JIKES_PATH);
        getPreferences().bindToPreferences(compilerRadioButtonGroup, EditorPreferences.PREF_COMPILER, EditorPreferences.DEFAULT_COMPILER);

        // Statistics
        getPreferences().bindToPreferences(reportTypeCombo, EditorPreferences.PREF_STATS_REMINDER_METHOD, EditorPreferences.DEFAULT_STATS_REMINDER_METHOD);

        // Updates
        getPreferences().bindToPreferences(updateTypeCombo, EditorPreferences.PREF_UPDATE_TYPE, EditorPreferences.DEFAULT_UPDATE_TYPE);
        getPreferences().bindToPreferences(downloadPathField, EditorPreferences.PREF_DOWNLOAD_PATH, EditorPreferences.DEFAULT_DOWNLOAD_PATH);

        // Colors
        getPreferences().bindToPreferences(nonConsumedTokenColor, EditorPreferences.PREF_NONCONSUMED_TOKEN_COLOR, EditorPreferences.DEFAULT_NONCONSUMED_TOKEN_COLOR);
        getPreferences().bindToPreferences(consumedTokenColor, EditorPreferences.PREF_CONSUMED_TOKEN_COLOR, EditorPreferences.DEFAULT_CONSUMED_TOKEN_COLOR);
        getPreferences().bindToPreferences(hiddenTokenColor, EditorPreferences.PREF_HIDDEN_TOKEN_COLOR, EditorPreferences.DEFAULT_HIDDEN_TOKEN_COLOR);
        getPreferences().bindToPreferences(deadTokenColor, EditorPreferences.PREF_DEAD_TOKEN_COLOR, EditorPreferences.DEFAULT_DEAD_TOKEN_COLOR);
        getPreferences().bindToPreferences(lookaheadTokenColor, EditorPreferences.PREF_LOOKAHEAD_TOKEN_COLOR, EditorPreferences.DEFAULT_LOOKAHEAD_TOKEN_COLOR);
    }

    public void dialogWillDisplay() {
        lafIndex = lafCombo.getSelectedIndex();
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
        ThreadedParser.setDelay(EditorPreferences.getParserDelay());
        XJApplication.setAutoSave(EditorPreferences.getAutoSaveEnabled(), EditorPreferences.getAutoSaveDelay());
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
        XJLookAndFeel.applyLookAndFeel(EditorPreferences.getLookAndFeel());
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
        tabEditor = new JPanel();
        label3 = new JLabel();
        editorFontCombo = new JComboBox();
        editorFontSizeSpinner = new JSpinner();
        autoSaveButton = new JCheckBox();
        autoSaveDelayField = new JTextField();
        label11 = new JLabel();
        highlightCursorLineButton = new JCheckBox();
        label1 = new JLabel();
        label22 = new JLabel();
        parserDelayField = new JTextField();
        label23 = new JLabel();
        tabWidthField = new JTextField();
        tabVisual = new JPanel();
        panel9 = new JPanel();
        label24 = new JLabel();
        dotToolPathField = new JTextField();
        browseDotToolPathButton = new JButton();
        label25 = new JLabel();
        dotImageFormatField = new JTextField();
        tabCompiler = new JPanel();
        jikesRadio = new JRadioButton();
        integratedRadio = new JRadioButton();
        javacRadio = new JRadioButton();
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

                        //---- label1 ----
                        label1.setHorizontalAlignment(SwingConstants.RIGHT);
                        label1.setText("Tab width:");
                        tabEditor.add(label1, cc.xy(3, 9));

                        //---- label22 ----
                        label22.setText("Parser delay:");
                        tabEditor.add(label22, cc.xy(3, 11));

                        //---- parserDelayField ----
                        parserDelayField.setText("250");
                        tabEditor.add(parserDelayField, cc.xy(5, 11));

                        //---- label23 ----
                        label23.setText("ms");
                        tabEditor.add(label23, cc.xy(7, 11));

                        //---- tabWidthField ----
                        tabWidthField.setText("8");
                        tabEditor.add(tabWidthField, cc.xy(5, 9));
                    }
                    tabbedPane1.addTab("Editor", tabEditor);

                    //======== tabVisual ========
                    {
                        tabVisual.setLayout(new FormLayout(
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
                                FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.LINE_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC
                            }));

                        //======== panel9 ========
                        {
                            panel9.setBorder(new TitledBorder("DOT"));
                            panel9.setLayout(new FormLayout(
                                new ColumnSpec[] {
                                    FormFactory.DEFAULT_COLSPEC,
                                    FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                    new ColumnSpec(ColumnSpec.RIGHT, Sizes.DEFAULT, FormSpec.NO_GROW),
                                    FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                    new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                                    FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                    FormFactory.DEFAULT_COLSPEC,
                                    FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                    FormFactory.DEFAULT_COLSPEC
                                },
                                new RowSpec[] {
                                    FormFactory.DEFAULT_ROWSPEC,
                                    FormFactory.LINE_GAP_ROWSPEC,
                                    FormFactory.DEFAULT_ROWSPEC,
                                    FormFactory.LINE_GAP_ROWSPEC,
                                    FormFactory.DEFAULT_ROWSPEC
                                }));

                            //---- label24 ----
                            label24.setText("Tool path:");
                            panel9.add(label24, cc.xy(3, 1));
                            panel9.add(dotToolPathField, cc.xy(5, 1));

                            //---- browseDotToolPathButton ----
                            browseDotToolPathButton.setText("Browse...");
                            panel9.add(browseDotToolPathButton, cc.xy(7, 1));

                            //---- label25 ----
                            label25.setText("Image format:");
                            panel9.add(label25, cc.xy(3, 3));

                            //---- dotImageFormatField ----
                            dotImageFormatField.setText("png");
                            panel9.add(dotImageFormatField, cc.xy(5, 3));
                        }
                        tabVisual.add(panel9, cc.xywh(3, 5, 5, 1));
                    }
                    tabbedPane1.addTab("Visualization", tabVisual);

                    //======== tabCompiler ========
                    {
                        tabCompiler.setLayout(new FormLayout(
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
                        tabCompiler.add(jikesRadio, cc.xy(3, 5));

                        //---- integratedRadio ----
                        integratedRadio.setActionCommand("integrated");
                        integratedRadio.setText("com.sun.tools.javac");
                        tabCompiler.add(integratedRadio, cc.xywh(3, 9, 2, 1));

                        //---- javacRadio ----
                        javacRadio.setSelected(true);
                        javacRadio.setText("javac");
                        tabCompiler.add(javacRadio, cc.xy(3, 3));

                        //---- label4 ----
                        label4.setText("Path:");
                        tabCompiler.add(label4, cc.xywh(3, 7, 1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
                        tabCompiler.add(jikesPathField, cc.xy(4, 7));

                        //---- browseJikesPath ----
                        browseJikesPath.setText("Browse...");
                        tabCompiler.add(browseJikesPath, cc.xy(5, 7));
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
    private JPanel tabEditor;
    private JLabel label3;
    private JComboBox editorFontCombo;
    private JSpinner editorFontSizeSpinner;
    private JCheckBox autoSaveButton;
    private JTextField autoSaveDelayField;
    private JLabel label11;
    private JCheckBox highlightCursorLineButton;
    private JLabel label1;
    private JLabel label22;
    private JTextField parserDelayField;
    private JLabel label23;
    private JTextField tabWidthField;
    private JPanel tabVisual;
    private JPanel panel9;
    private JLabel label24;
    private JTextField dotToolPathField;
    private JButton browseDotToolPathButton;
    private JLabel label25;
    private JTextField dotImageFormatField;
    private JPanel tabCompiler;
    private JRadioButton jikesRadio;
    private JRadioButton integratedRadio;
    private JRadioButton javacRadio;
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
