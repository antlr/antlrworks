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
import edu.usfca.xj.foundation.XJSystem;
import org.antlr.works.ate.syntax.generic.ATESyntaxEngine;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.utils.HelpManager;
import org.antlr.works.utils.Utils;
import org.antlr.works.IDE;

import javax.swing.*;
import javax.swing.border.LineBorder;
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
        setSize(getDefaultSize());

        prepareGeneralTab();
        prepareEditorTab();
        prepareSyntaxTab();
        prepareCompilerTab();
        prepareDebuggerTab();
        prepareSCMTab();
        prepareUpdateTab();

        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                apply();
            }
        });
    }

    public Dimension getDefaultSize() {
        return new Dimension(700, 360);
    }

    public void prepareGeneralTab() {
        browseDotToolPathButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(XJFileChooser.shared().displayChooseDirectory(getJavaContainer())) {
                    dotToolPathField.setText(XJFileChooser.shared().getSelectedFilePath());
                    AWPrefs.setDOTToolPath(dotToolPathField.getText());
                }
            }
        });

        lafCombo.removeAllItems();
        UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
        for (int i=0; i<info.length; i++) {
            lafCombo.addItem(info[i].getName());
        }
        lafCombo.setEnabled(!IDE.isPlugin());

        getPreferences().bindToPreferences(startupActionCombo, AWPrefs.PREF_STARTUP_ACTION, AWPrefs.STARTUP_OPEN_LAST_DOC);
        getPreferences().bindToPreferences(lafCombo, AWPrefs.PREF_LOOK_AND_FEEL, XJLookAndFeel.getDefaultLookAndFeelName());
        getPreferences().bindToPreferences(dotToolPathField, AWPrefs.PREF_DOT_TOOL_PATH, AWPrefs.DEFAULT_DOT_TOOL_PATH);

        // General - debug only
        getPreferences().bindToPreferences(debugVerboseButton, AWPrefs.PREF_DEBUG_VERBOSE, false);
        getPreferences().bindToPreferences(debugDontOptimizeNFA, AWPrefs.PREF_DEBUG_DONT_OPTIMIZE_NFA, false);
    }

    public void prepareEditorTab() {
        foldingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionsFoldingAnchorsButton.setEnabled(foldingButton.isSelected());
            }
        });

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String fontNames[] = ge.getAvailableFontFamilyNames();

        editorFontCombo.removeAllItems();
        for (int i=0; i<fontNames.length; i++) {
            editorFontCombo.addItem(fontNames[i]);
        }

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
    }

    public void bindSyntax(String identifier, JPanel colorPanel, JCheckBox bold, JCheckBox italic) {
        getPreferences().bindToPreferences(colorPanel, AWPrefs.getSyntaxColorKey(identifier), AWPrefs.getSyntaxDefaultColor(identifier));
        getPreferences().bindToPreferences(bold, AWPrefs.getSyntaxBoldKey(identifier), AWPrefs.getSyntaxDefaultBold(identifier));
        getPreferences().bindToPreferences(italic, AWPrefs.getSyntaxItalicKey(identifier), AWPrefs.getSyntaxDefaultItalic(identifier));
    }

    public void defaultSyntax(String identifier, JPanel colorPanel, JCheckBox bold, JCheckBox italic) {
        getPreferences().defaultPreference(colorPanel, AWPrefs.getSyntaxColorKey(identifier), AWPrefs.getSyntaxDefaultColor(identifier));
        getPreferences().defaultPreference(bold, AWPrefs.getSyntaxBoldKey(identifier), AWPrefs.getSyntaxDefaultBold(identifier));
        getPreferences().defaultPreference(italic, AWPrefs.getSyntaxItalicKey(identifier), AWPrefs.getSyntaxDefaultItalic(identifier));
    }

    public void prepareSyntaxTab() {
        syntaxDefaultButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                defaultSyntax(AWPrefs.PREF_SYNTAX_PARSER, parserColorPanel, parserBoldButton, parserItalicButton);
                defaultSyntax(AWPrefs.PREF_SYNTAX_LEXER, lexerColorPanel, lexerBoldButton, lexerItalicButton);
                defaultSyntax(AWPrefs.PREF_SYNTAX_LABEL, labelColorPanel, labelsBoldButton, labelsItalicButton);
                defaultSyntax(AWPrefs.PREF_SYNTAX_REFS, refsActionColorPanel, refsActionBoldButton, refsActionItalicButton);
                defaultSyntax(AWPrefs.PREF_SYNTAX_COMMENT, commentsColorPanel, commentsBoldButton, commentsItalicButton);
                defaultSyntax(AWPrefs.PREF_SYNTAX_STRING, stringsColorPanel, stringsBoldButton, stringsItalicButton);
                defaultSyntax(AWPrefs.PREF_SYNTAX_KEYWORD, keywordsColorPanel, keywordsBoldButton, keywordsItalicButton);
            }
        });

        bindSyntax(AWPrefs.PREF_SYNTAX_PARSER, parserColorPanel, parserBoldButton, parserItalicButton);
        bindSyntax(AWPrefs.PREF_SYNTAX_LEXER, lexerColorPanel, lexerBoldButton, lexerItalicButton);
        bindSyntax(AWPrefs.PREF_SYNTAX_LABEL, labelColorPanel, labelsBoldButton, labelsItalicButton);
        bindSyntax(AWPrefs.PREF_SYNTAX_REFS, refsActionColorPanel, refsActionBoldButton, refsActionItalicButton);
        bindSyntax(AWPrefs.PREF_SYNTAX_COMMENT, commentsColorPanel, commentsBoldButton, commentsItalicButton);
        bindSyntax(AWPrefs.PREF_SYNTAX_STRING, stringsColorPanel, stringsBoldButton, stringsItalicButton);
        bindSyntax(AWPrefs.PREF_SYNTAX_KEYWORD, keywordsColorPanel, keywordsBoldButton, keywordsItalicButton);
    }

    public void prepareCompilerTab() {
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

        compilerRadioButtonGroup = new ButtonGroup();
        compilerRadioButtonGroup.add(jikesRadio);
        compilerRadioButtonGroup.add(integratedRadio);
        compilerRadioButtonGroup.add(javacRadio);

        integratedRadio.setActionCommand("integrated");
        javacRadio.setActionCommand("javac");
        jikesRadio.setActionCommand("jikes");

        getPreferences().bindToPreferences(javacCustomPathButton, AWPrefs.PREF_JAVAC_CUSTOM_PATH, AWPrefs.DEFAULT_JAVAC_CUSTOM_PATH);
        getPreferences().bindToPreferences(javacPathField, AWPrefs.PREF_JAVAC_PATH, AWPrefs.DEFAULT_JAVAC_PATH);
        getPreferences().bindToPreferences(jikesPathField, AWPrefs.PREF_JIKES_PATH, AWPrefs.DEFAULT_JIKES_PATH);
        getPreferences().bindToPreferences(compilerRadioButtonGroup, AWPrefs.PREF_COMPILER, AWPrefs.DEFAULT_COMPILER);
    }

    public void prepareDebuggerTab() {
        getPreferences().bindToPreferences(debugDefaultLocalPortField, AWPrefs.PREF_DEBUG_LOCALPORT, AWPrefs.DEFAULT_DEBUG_LOCALPORT);
        getPreferences().bindToPreferences(debugLaunchTimeoutField, AWPrefs.PREF_DEBUG_LAUNCHTIMEOUT, AWPrefs.DEFAULT_DEBUG_LAUNCHTIMEOUT);

        getPreferences().bindToPreferences(debugNonConsumedColorPanel, AWPrefs.PREF_NONCONSUMED_TOKEN_COLOR, AWPrefs.DEFAULT_NONCONSUMED_TOKEN_COLOR);
        getPreferences().bindToPreferences(debugConsumedColorPanel, AWPrefs.PREF_CONSUMED_TOKEN_COLOR, AWPrefs.DEFAULT_CONSUMED_TOKEN_COLOR);
        getPreferences().bindToPreferences(debugHiddenColorPanel, AWPrefs.PREF_HIDDEN_TOKEN_COLOR, AWPrefs.DEFAULT_HIDDEN_TOKEN_COLOR);
        getPreferences().bindToPreferences(debugDeadColorPanel, AWPrefs.PREF_DEAD_TOKEN_COLOR, AWPrefs.DEFAULT_DEAD_TOKEN_COLOR);
        getPreferences().bindToPreferences(debugLTColorPanel, AWPrefs.PREF_LOOKAHEAD_TOKEN_COLOR, AWPrefs.DEFAULT_LOOKAHEAD_TOKEN_COLOR);
    }

    public void prepareSCMTab() {
        getPreferences().bindToPreferences(enablePerforceCheckBox, AWPrefs.PREF_SCM_P4_ENABLED, false);
        getPreferences().bindToPreferences(p4PortField, AWPrefs.PREF_SCM_P4_PORT, "");
        getPreferences().bindToPreferences(p4UserField, AWPrefs.PREF_SCM_P4_USER, "");
        getPreferences().bindToPreferences(p4PasswordField, AWPrefs.PREF_SCM_P4_PASSWORD, "");
        getPreferences().bindToPreferences(p4ClientField, AWPrefs.PREF_SCM_P4_CLIENT, "");
        getPreferences().bindToPreferences(p4ExecPathField, AWPrefs.PREF_SCM_P4_EXEC, "");
    }

    public void prepareUpdateTab() {
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

        getPreferences().bindToPreferences(updateTypeCombo, AWPrefs.PREF_UPDATE_TYPE, AWPrefs.DEFAULT_UPDATE_TYPE);
        getPreferences().bindToPreferences(downloadPathField, AWPrefs.PREF_DOWNLOAD_PATH, AWPrefs.DEFAULT_DOWNLOAD_PATH);
    }

    public void becomingVisibleForTheFirstTime() {
        lafIndex = lafCombo.getSelectedIndex();
        javacPathField.setEnabled(javacCustomPathButton.isSelected());
        browseJavacPath.setEnabled(javacCustomPathButton.isSelected());
        actionsFoldingAnchorsButton.setEnabled(foldingButton.isSelected());
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_PREFERENCES);
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
        ATESyntaxEngine.setDelay(AWPrefs.getParserDelay());
        XJApplication.setAutoSave(AWPrefs.getAutoSaveEnabled(), AWPrefs.getAutoSaveDelay());
    }

    public JComponent getComponent() {
        return tabbedPane1;
    }

    public void apply() {
        dialogPane.requestFocus();
        getPreferences().applyPreferences();
        if(lafIndex != lafCombo.getSelectedIndex()) {
            lafIndex = lafCombo.getSelectedIndex();
            if(!IDE.isPlugin())
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

    public boolean shouldDisplayMainMenuBar() {
        return super.shouldDisplayMainMenuBar() && !IDE.isPlugin();
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
        label25 = new JLabel();
        debugVerboseButton = new JCheckBox();
        debugDontOptimizeNFA = new JCheckBox();
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
        tabSyntax = new JPanel();
        label26 = new JLabel();
        parserColorPanel = new JPanel();
        parserBoldButton = new JCheckBox();
        parserItalicButton = new JCheckBox();
        label27 = new JLabel();
        lexerColorPanel = new JPanel();
        lexerBoldButton = new JCheckBox();
        lexerItalicButton = new JCheckBox();
        label28 = new JLabel();
        labelColorPanel = new JPanel();
        labelsBoldButton = new JCheckBox();
        labelsItalicButton = new JCheckBox();
        label29 = new JLabel();
        refsActionColorPanel = new JPanel();
        refsActionBoldButton = new JCheckBox();
        refsActionItalicButton = new JCheckBox();
        label30 = new JLabel();
        commentsColorPanel = new JPanel();
        commentsBoldButton = new JCheckBox();
        commentsItalicButton = new JCheckBox();
        label31 = new JLabel();
        stringsColorPanel = new JPanel();
        stringsBoldButton = new JCheckBox();
        stringsItalicButton = new JCheckBox();
        label32 = new JLabel();
        keywordsColorPanel = new JPanel();
        keywordsBoldButton = new JCheckBox();
        syntaxDefaultButton = new JButton();
        keywordsItalicButton = new JCheckBox();
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
        label33 = new JLabel();
        debugDefaultLocalPortField = new JTextField();
        label34 = new JLabel();
        debugLaunchTimeoutField = new JTextField();
        label35 = new JLabel();
        label12 = new JLabel();
        debugNonConsumedColorPanel = new JPanel();
        label13 = new JLabel();
        debugConsumedColorPanel = new JPanel();
        label14 = new JLabel();
        debugHiddenColorPanel = new JPanel();
        label15 = new JLabel();
        debugDeadColorPanel = new JPanel();
        label16 = new JLabel();
        debugLTColorPanel = new JPanel();
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

                        //---- label25 ----
                        label25.setText("Debug:");
                        tabGeneral.add(label25, cc.xy(3, 9));

                        //---- debugVerboseButton ----
                        debugVerboseButton.setText("Verbose");
                        tabGeneral.add(debugVerboseButton, cc.xywh(5, 9, 3, 1));

                        //---- debugDontOptimizeNFA ----
                        debugDontOptimizeNFA.setText("Don't optimize NFA");
                        tabGeneral.add(debugDontOptimizeNFA, cc.xywh(5, 11, 3, 1));
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

                    //======== tabSyntax ========
                    {
                        tabSyntax.setLayout(new FormLayout(
                            new ColumnSpec[] {
                                new ColumnSpec(Sizes.dluX(10)),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                new ColumnSpec(ColumnSpec.RIGHT, Sizes.DEFAULT, FormSpec.NO_GROW),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                FormFactory.DEFAULT_COLSPEC,
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                FormFactory.DEFAULT_COLSPEC,
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                FormFactory.DEFAULT_COLSPEC,
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                new ColumnSpec(Sizes.dluX(20)),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                FormFactory.DEFAULT_COLSPEC,
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
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
                                FormFactory.DEFAULT_ROWSPEC
                            }));

                        //---- label26 ----
                        label26.setText("Parser References:");
                        tabSyntax.add(label26, cc.xywh(3, 3, 1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));

                        //======== parserColorPanel ========
                        {
                            parserColorPanel.setBackground(new Color(255, 255, 51));
                            parserColorPanel.setBorder(LineBorder.createBlackLineBorder());
                            parserColorPanel.setForeground(Color.black);
                            parserColorPanel.setPreferredSize(new Dimension(70, 20));
                            parserColorPanel.setLayout(new FlowLayout());
                        }
                        tabSyntax.add(parserColorPanel, cc.xy(5, 3));

                        //---- parserBoldButton ----
                        parserBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
                        parserBoldButton.setText("Bold");
                        tabSyntax.add(parserBoldButton, cc.xy(7, 3));

                        //---- parserItalicButton ----
                        parserItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                        parserItalicButton.setText("Italic");
                        tabSyntax.add(parserItalicButton, cc.xy(9, 3));

                        //---- label27 ----
                        label27.setText("Lexer References:");
                        tabSyntax.add(label27, cc.xy(3, 5));

                        //======== lexerColorPanel ========
                        {
                            lexerColorPanel.setBackground(new Color(255, 255, 51));
                            lexerColorPanel.setBorder(LineBorder.createBlackLineBorder());
                            lexerColorPanel.setForeground(Color.black);
                            lexerColorPanel.setPreferredSize(new Dimension(70, 20));
                            lexerColorPanel.setLayout(new FlowLayout());
                        }
                        tabSyntax.add(lexerColorPanel, cc.xy(5, 5));

                        //---- lexerBoldButton ----
                        lexerBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
                        lexerBoldButton.setText("Bold");
                        tabSyntax.add(lexerBoldButton, cc.xy(7, 5));

                        //---- lexerItalicButton ----
                        lexerItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                        lexerItalicButton.setText("Italic");
                        tabSyntax.add(lexerItalicButton, cc.xy(9, 5));

                        //---- label28 ----
                        label28.setText("Labels:");
                        tabSyntax.add(label28, cc.xy(3, 7));

                        //======== labelColorPanel ========
                        {
                            labelColorPanel.setBackground(new Color(255, 255, 51));
                            labelColorPanel.setBorder(LineBorder.createBlackLineBorder());
                            labelColorPanel.setForeground(Color.black);
                            labelColorPanel.setPreferredSize(new Dimension(70, 20));
                            labelColorPanel.setLayout(new FlowLayout());
                        }
                        tabSyntax.add(labelColorPanel, cc.xy(5, 7));

                        //---- labelsBoldButton ----
                        labelsBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
                        labelsBoldButton.setText("Bold");
                        tabSyntax.add(labelsBoldButton, cc.xy(7, 7));

                        //---- labelsItalicButton ----
                        labelsItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                        labelsItalicButton.setText("Italic");
                        tabSyntax.add(labelsItalicButton, cc.xy(9, 7));

                        //---- label29 ----
                        label29.setText("References in action:");
                        tabSyntax.add(label29, cc.xy(3, 9));

                        //======== refsActionColorPanel ========
                        {
                            refsActionColorPanel.setBackground(new Color(255, 255, 51));
                            refsActionColorPanel.setBorder(LineBorder.createBlackLineBorder());
                            refsActionColorPanel.setForeground(Color.black);
                            refsActionColorPanel.setPreferredSize(new Dimension(70, 20));
                            refsActionColorPanel.setLayout(new FlowLayout());
                        }
                        tabSyntax.add(refsActionColorPanel, cc.xy(5, 9));

                        //---- refsActionBoldButton ----
                        refsActionBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
                        refsActionBoldButton.setText("Bold");
                        tabSyntax.add(refsActionBoldButton, cc.xy(7, 9));

                        //---- refsActionItalicButton ----
                        refsActionItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                        refsActionItalicButton.setText("Italic");
                        tabSyntax.add(refsActionItalicButton, cc.xy(9, 9));

                        //---- label30 ----
                        label30.setText("Comments:");
                        tabSyntax.add(label30, cc.xy(3, 11));

                        //======== commentsColorPanel ========
                        {
                            commentsColorPanel.setBackground(new Color(255, 255, 51));
                            commentsColorPanel.setBorder(LineBorder.createBlackLineBorder());
                            commentsColorPanel.setForeground(Color.black);
                            commentsColorPanel.setPreferredSize(new Dimension(70, 20));
                            commentsColorPanel.setLayout(new FlowLayout());
                        }
                        tabSyntax.add(commentsColorPanel, cc.xy(5, 11));

                        //---- commentsBoldButton ----
                        commentsBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
                        commentsBoldButton.setText("Bold");
                        tabSyntax.add(commentsBoldButton, cc.xy(7, 11));

                        //---- commentsItalicButton ----
                        commentsItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                        commentsItalicButton.setText("Italic");
                        tabSyntax.add(commentsItalicButton, cc.xy(9, 11));

                        //---- label31 ----
                        label31.setText("Strings:");
                        tabSyntax.add(label31, cc.xy(3, 13));

                        //======== stringsColorPanel ========
                        {
                            stringsColorPanel.setBackground(new Color(255, 255, 51));
                            stringsColorPanel.setBorder(LineBorder.createBlackLineBorder());
                            stringsColorPanel.setForeground(Color.black);
                            stringsColorPanel.setPreferredSize(new Dimension(70, 20));
                            stringsColorPanel.setLayout(new FlowLayout());
                        }
                        tabSyntax.add(stringsColorPanel, cc.xy(5, 13));

                        //---- stringsBoldButton ----
                        stringsBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
                        stringsBoldButton.setText("Bold");
                        tabSyntax.add(stringsBoldButton, cc.xy(7, 13));

                        //---- stringsItalicButton ----
                        stringsItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                        stringsItalicButton.setText("Italic");
                        tabSyntax.add(stringsItalicButton, cc.xy(9, 13));

                        //---- label32 ----
                        label32.setText("Keywords:");
                        tabSyntax.add(label32, cc.xy(3, 15));

                        //======== keywordsColorPanel ========
                        {
                            keywordsColorPanel.setBackground(new Color(255, 255, 51));
                            keywordsColorPanel.setBorder(LineBorder.createBlackLineBorder());
                            keywordsColorPanel.setForeground(Color.black);
                            keywordsColorPanel.setPreferredSize(new Dimension(70, 20));
                            keywordsColorPanel.setLayout(new FlowLayout());
                        }
                        tabSyntax.add(keywordsColorPanel, cc.xy(5, 15));

                        //---- keywordsBoldButton ----
                        keywordsBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
                        keywordsBoldButton.setText("Bold");
                        tabSyntax.add(keywordsBoldButton, cc.xy(7, 15));

                        //---- syntaxDefaultButton ----
                        syntaxDefaultButton.setText("Default");
                        tabSyntax.add(syntaxDefaultButton, cc.xy(13, 15));

                        //---- keywordsItalicButton ----
                        keywordsItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                        keywordsItalicButton.setText("Italic");
                        tabSyntax.add(keywordsItalicButton, cc.xy(9, 15));
                    }
                    tabbedPane1.addTab("Syntax", tabSyntax);

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
                                new ColumnSpec(ColumnSpec.RIGHT, Sizes.DEFAULT, FormSpec.NO_GROW),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                FormFactory.DEFAULT_COLSPEC,
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                new ColumnSpec(Sizes.dluX(10)),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                new ColumnSpec(ColumnSpec.RIGHT, Sizes.DEFAULT, FormSpec.NO_GROW),
                                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                new ColumnSpec("max(default;20dlu)"),
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

                        //---- label33 ----
                        label33.setText("Default local port:");
                        tabDebugger.add(label33, cc.xy(3, 3));

                        //---- debugDefaultLocalPortField ----
                        debugDefaultLocalPortField.setText("0xC001");
                        tabDebugger.add(debugDefaultLocalPortField, cc.xy(5, 3));

                        //---- label34 ----
                        label34.setText("Remote parser launch time-out:");
                        tabDebugger.add(label34, cc.xy(9, 3));

                        //---- debugLaunchTimeoutField ----
                        debugLaunchTimeoutField.setText("5");
                        tabDebugger.add(debugLaunchTimeoutField, cc.xy(11, 3));

                        //---- label35 ----
                        label35.setText("seconds");
                        tabDebugger.add(label35, cc.xy(13, 3));

                        //---- label12 ----
                        label12.setHorizontalAlignment(SwingConstants.RIGHT);
                        label12.setText("Non-consumed token:");
                        tabDebugger.add(label12, cc.xy(3, 7));

                        //======== debugNonConsumedColorPanel ========
                        {
                            debugNonConsumedColorPanel.setBackground(new Color(255, 255, 51));
                            debugNonConsumedColorPanel.setBorder(LineBorder.createBlackLineBorder());
                            debugNonConsumedColorPanel.setForeground(Color.black);
                            debugNonConsumedColorPanel.setPreferredSize(new Dimension(70, 20));
                            debugNonConsumedColorPanel.setLayout(new FlowLayout());
                        }
                        tabDebugger.add(debugNonConsumedColorPanel, cc.xy(5, 7));

                        //---- label13 ----
                        label13.setHorizontalAlignment(SwingConstants.RIGHT);
                        label13.setText("Consumed token:");
                        tabDebugger.add(label13, cc.xy(3, 9));

                        //======== debugConsumedColorPanel ========
                        {
                            debugConsumedColorPanel.setBackground(new Color(255, 255, 51));
                            debugConsumedColorPanel.setBorder(LineBorder.createBlackLineBorder());
                            debugConsumedColorPanel.setForeground(Color.black);
                            debugConsumedColorPanel.setPreferredSize(new Dimension(70, 20));
                            debugConsumedColorPanel.setLayout(new FlowLayout());
                        }
                        tabDebugger.add(debugConsumedColorPanel, cc.xy(5, 9));

                        //---- label14 ----
                        label14.setHorizontalAlignment(SwingConstants.RIGHT);
                        label14.setText("Hidden token:");
                        tabDebugger.add(label14, cc.xy(3, 11));

                        //======== debugHiddenColorPanel ========
                        {
                            debugHiddenColorPanel.setBackground(new Color(255, 255, 51));
                            debugHiddenColorPanel.setBorder(LineBorder.createBlackLineBorder());
                            debugHiddenColorPanel.setForeground(Color.black);
                            debugHiddenColorPanel.setPreferredSize(new Dimension(70, 20));
                            debugHiddenColorPanel.setLayout(new FlowLayout());
                        }
                        tabDebugger.add(debugHiddenColorPanel, cc.xy(5, 11));

                        //---- label15 ----
                        label15.setHorizontalAlignment(SwingConstants.RIGHT);
                        label15.setText("Dead token:");
                        tabDebugger.add(label15, cc.xy(3, 13));

                        //======== debugDeadColorPanel ========
                        {
                            debugDeadColorPanel.setBackground(new Color(255, 255, 51));
                            debugDeadColorPanel.setBorder(LineBorder.createBlackLineBorder());
                            debugDeadColorPanel.setForeground(Color.black);
                            debugDeadColorPanel.setPreferredSize(new Dimension(70, 20));
                            debugDeadColorPanel.setLayout(new FlowLayout());
                        }
                        tabDebugger.add(debugDeadColorPanel, cc.xy(5, 13));

                        //---- label16 ----
                        label16.setHorizontalAlignment(SwingConstants.RIGHT);
                        label16.setText("Lookahead token:");
                        tabDebugger.add(label16, cc.xy(3, 15));

                        //======== debugLTColorPanel ========
                        {
                            debugLTColorPanel.setBackground(new Color(255, 255, 51));
                            debugLTColorPanel.setBorder(LineBorder.createBlackLineBorder());
                            debugLTColorPanel.setForeground(Color.black);
                            debugLTColorPanel.setPreferredSize(new Dimension(70, 20));
                            debugLTColorPanel.setLayout(new FlowLayout());
                        }
                        tabDebugger.add(debugLTColorPanel, cc.xy(5, 15));
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
    private JLabel label25;
    private JCheckBox debugVerboseButton;
    private JCheckBox debugDontOptimizeNFA;
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
    private JPanel tabSyntax;
    private JLabel label26;
    private JPanel parserColorPanel;
    private JCheckBox parserBoldButton;
    private JCheckBox parserItalicButton;
    private JLabel label27;
    private JPanel lexerColorPanel;
    private JCheckBox lexerBoldButton;
    private JCheckBox lexerItalicButton;
    private JLabel label28;
    private JPanel labelColorPanel;
    private JCheckBox labelsBoldButton;
    private JCheckBox labelsItalicButton;
    private JLabel label29;
    private JPanel refsActionColorPanel;
    private JCheckBox refsActionBoldButton;
    private JCheckBox refsActionItalicButton;
    private JLabel label30;
    private JPanel commentsColorPanel;
    private JCheckBox commentsBoldButton;
    private JCheckBox commentsItalicButton;
    private JLabel label31;
    private JPanel stringsColorPanel;
    private JCheckBox stringsBoldButton;
    private JCheckBox stringsItalicButton;
    private JLabel label32;
    private JPanel keywordsColorPanel;
    private JCheckBox keywordsBoldButton;
    private JButton syntaxDefaultButton;
    private JCheckBox keywordsItalicButton;
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
    private JLabel label33;
    private JTextField debugDefaultLocalPortField;
    private JLabel label34;
    private JTextField debugLaunchTimeoutField;
    private JLabel label35;
    private JLabel label12;
    private JPanel debugNonConsumedColorPanel;
    private JLabel label13;
    private JPanel debugConsumedColorPanel;
    private JLabel label14;
    private JPanel debugHiddenColorPanel;
    private JLabel label15;
    private JPanel debugDeadColorPanel;
    private JLabel label16;
    private JPanel debugLTColorPanel;
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
