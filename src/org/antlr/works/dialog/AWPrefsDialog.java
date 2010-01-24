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
import org.antlr.works.IDE;
import org.antlr.works.debugger.local.DBLocal;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.utils.HelpManager;
import org.antlr.works.utils.TextUtils;
import org.antlr.works.utils.Utils;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.app.XJPreferences;
import org.antlr.xjlib.appkit.frame.XJPanel;
import org.antlr.xjlib.appkit.swing.XJLookAndFeel;
import org.antlr.xjlib.appkit.utils.XJFileChooser;
import org.antlr.xjlib.foundation.notification.XJNotificationCenter;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class AWPrefsDialog extends XJPanel {

    public static final String NOTIF_PREFS_APPLIED = "NOTIF_PREFS_APPLIED";

    protected ButtonGroup compilerRadioButtonGroup;
    protected ButtonGroup testRigRadioButtonGroup;
    protected int lafIndex = 0;

    public AWPrefsDialog() {
        super();

        initComponents();

        prepareGeneralTab();
        prepareEditorTab();
        prepareSyntaxTab();
        prepareCompilerTab();
        prepareDebuggerTab();
        prepareAdvancedTab();
        prepareUpdateTab();
        prepareTestRigTab();

        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                apply();
            }
        });
    }

    public void prepareGeneralTab() {
        browseOutputPathButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(XJFileChooser.shared().displayChooseDirectory(getJavaContainer())) {
                    outputPathField.setText(XJFileChooser.shared().getSelectedFilePath());
                    AWPrefs.setOutputPath(outputPathField.getText());
                }
            }
        });

        browseDotToolPathButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(XJFileChooser.shared().displayOpenDialog(getJavaContainer(), false)) {
                    dotToolPathField.setText(XJFileChooser.shared().getSelectedFilePath());
                    AWPrefs.setDOTToolPath(dotToolPathField.getText());
                }
            }
        });

        lafCombo.removeAllItems();
        UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo anInfo : info) {
            lafCombo.addItem(anInfo.getName());
        }
        lafCombo.setEnabled(true);

        getPreferences().bindToPreferences(startupActionCombo, AWPrefs.PREF_STARTUP_ACTION, AWPrefs.STARTUP_OPEN_LAST_OPENED_DOC);
        getPreferences().bindToPreferences(restoreWindowsBoundButton, AWPrefs.PREF_RESTORE_WINDOWS, AWPrefs.DEFAULT_RESTORE_WINDOWS);
        getPreferences().bindToPreferences(lafCombo, AWPrefs.PREF_LOOK_AND_FEEL, XJLookAndFeel.getDefaultLookAndFeelName());
        getPreferences().bindToPreferences(desktopModeButton, AWPrefs.PREF_DESKTOP_MODE, AWPrefs.DEFAULT_DESKTOP_MODE);
        getPreferences().bindToPreferences(outputPathField, AWPrefs.PREF_OUTPUT_PATH, AWPrefs.DEFAULT_OUTPUT_PATH);
        getPreferences().bindToPreferences(dotToolPathField, AWPrefs.PREF_DOT_TOOL_PATH, AWPrefs.DEFAULT_DOT_TOOL_PATH);
        getPreferences().bindToPreferences(antlr3OptionsField, AWPrefs.PREF_ANTLR3_OPTIONS, AWPrefs.DEFAULT_ANTLR3_OPTIONS);

        // General - debug only
        //getPreferences().bindToPreferences(debugVerboseButton, AWPrefs.PREF_DEBUG_VERBOSE, false);
        //getPreferences().bindToPreferences(debugDontOptimizeNFA, AWPrefs.PREF_DEBUG_DONT_OPTIMIZE_NFA, false);
    }

    public void prepareEditorTab() {
        /*foldingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionsFoldingAnchorsButton.setEnabled(foldingButton.isSelected());
            }
        }); */

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String fontNames[] = ge.getAvailableFontFamilyNames();

        editorFontCombo.removeAllItems();
        for (String fontName : fontNames) {
            editorFontCombo.addItem(fontName);
        }

        getPreferences().bindToPreferences(autoSaveButton, AWPrefs.PREF_AUTOSAVE_ENABLED, false);
        getPreferences().bindToPreferences(autoSaveDelayField, AWPrefs.PREF_AUTOSAVE_DELAY, 5);
        getPreferences().bindToPreferences(backupFileButton, AWPrefs.PREF_BACKUP_FILE_ENABLED, false);
        getPreferences().bindToPreferences(highlightCursorLineButton, AWPrefs.PREF_HIGHLIGHTCURSORLINE, true);
        getPreferences().bindToPreferences(tabWidthField, AWPrefs.PREF_TAB_WIDTH, AWPrefs.DEFAULT_TAB_WIDTH);
        getPreferences().bindToPreferences(editorFontCombo, AWPrefs.PREF_EDITOR_FONT, AWPrefs.DEFAULT_EDITOR_FONT);
        getPreferences().bindToPreferences(editorFontSizeSpinner, AWPrefs.PREF_EDITOR_FONT_SIZE, AWPrefs.DEFAULT_EDITOR_FONT_SIZE);
        getPreferences().bindToPreferences(parserDelayField, AWPrefs.PREF_PARSER_DELAY, AWPrefs.DEFAULT_PARSER_DELAY);
        getPreferences().bindToPreferences(autoIndentColonInRuleButton, AWPrefs.PREF_AUTO_IDENT_COLON_RULE, AWPrefs.DEFAULT_AUTO_INDENT_COLON_RULE);
        getPreferences().bindToPreferences(showLineNumbers, AWPrefs.PREF_LINE_NUMBER, false);
        getPreferences().bindToPreferences(vstyleAutocompletionButton, AWPrefs.PREF_VSTYLE_AUTOCOMPLETION, false);
//        getPreferences().bindToPreferences(foldingButton, AWPrefs.PREF_EDITOR_FOLDING, AWPrefs.DEFAULT_EDITOR_FOLDING);
//        getPreferences().bindToPreferences(actionsFoldingAnchorsButton, AWPrefs.PREF_ACTIONS_ANCHORS_FOLDING, AWPrefs.DEFAULT_ACTIONS_ANCHORS_FOLDING);
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
                defaultSyntax(AWPrefs.PREF_SYNTAX_BLOCK, blockLabelsColorPanel, blockLabelsBoldButton, blockLabelsItalicButton);
                defaultSyntax(AWPrefs.PREF_SYNTAX_COMMENT, commentsColorPanel, commentsBoldButton, commentsItalicButton);
                defaultSyntax(AWPrefs.PREF_SYNTAX_STRING, stringsColorPanel, stringsBoldButton, stringsItalicButton);
                defaultSyntax(AWPrefs.PREF_SYNTAX_KEYWORD, keywordsColorPanel, keywordsBoldButton, keywordsItalicButton);
            }
        });

        bindSyntax(AWPrefs.PREF_SYNTAX_PARSER, parserColorPanel, parserBoldButton, parserItalicButton);
        bindSyntax(AWPrefs.PREF_SYNTAX_LEXER, lexerColorPanel, lexerBoldButton, lexerItalicButton);
        bindSyntax(AWPrefs.PREF_SYNTAX_LABEL, labelColorPanel, labelsBoldButton, labelsItalicButton);
        bindSyntax(AWPrefs.PREF_SYNTAX_REFS, refsActionColorPanel, refsActionBoldButton, refsActionItalicButton);
        bindSyntax(AWPrefs.PREF_SYNTAX_BLOCK, blockLabelsColorPanel, blockLabelsBoldButton, blockLabelsItalicButton);
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

        classpathCustomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                customClasspathField.setEnabled(classpathCustomButton.isSelected());
                browseCustomClassPathButton.setEnabled(classpathCustomButton.isSelected());
            }
        });

        browseCustomClassPathButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(XJFileChooser.shared().displayChooseDirectory(getJavaContainer())) {
                    customClasspathField.setText(XJFileChooser.shared().getSelectedFilePath());
                    AWPrefs.setCustomClassPath(customClasspathField.getText());
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

        getPreferences().bindToPreferences(classpathSystemButton, AWPrefs.PREF_CLASSPATH_SYSTEM, AWPrefs.DEFAULT_CLASSPATH_SYSTEM);
        getPreferences().bindToPreferences(classpathCustomButton, AWPrefs.PREF_CLASSPATH_CUSTOM, AWPrefs.DEFAULT_CLASSPATH_CUSTOM);
        getPreferences().bindToPreferences(customClasspathField, AWPrefs.PREF_CUSTOM_CLASS_PATH, AWPrefs.DEFAULT_PREF_CUSTOM_CLASS_PATH);
    }

    public void prepareDebuggerTab() {
        getPreferences().bindToPreferences(debugDefaultLocalPortField, AWPrefs.PREF_DEBUG_LOCALPORT, AWPrefs.DEFAULT_DEBUG_LOCALPORT);
        getPreferences().bindToPreferences(debugLaunchTimeoutField, AWPrefs.PREF_DEBUG_LAUNCHTIMEOUT, AWPrefs.DEFAULT_DEBUG_LAUNCHTIMEOUT);

        getPreferences().bindToPreferences(debugNonConsumedColorPanel, AWPrefs.PREF_NONCONSUMED_TOKEN_COLOR, AWPrefs.DEFAULT_NONCONSUMED_TOKEN_COLOR);
        getPreferences().bindToPreferences(debugConsumedColorPanel, AWPrefs.PREF_CONSUMED_TOKEN_COLOR, AWPrefs.DEFAULT_CONSUMED_TOKEN_COLOR);
        getPreferences().bindToPreferences(debugHiddenColorPanel, AWPrefs.PREF_HIDDEN_TOKEN_COLOR, AWPrefs.DEFAULT_HIDDEN_TOKEN_COLOR);
        getPreferences().bindToPreferences(debugDeadColorPanel, AWPrefs.PREF_DEAD_TOKEN_COLOR, AWPrefs.DEFAULT_DEAD_TOKEN_COLOR);
        getPreferences().bindToPreferences(debugLTColorPanel, AWPrefs.PREF_LOOKAHEAD_TOKEN_COLOR, AWPrefs.DEFAULT_LOOKAHEAD_TOKEN_COLOR);

        getPreferences().bindToPreferences(detachablePanelChildrenButton, AWPrefs.PREF_DETACHABLE_CHILDREN, AWPrefs.DEFAULT_DETACHABLE_CHILDREN);
        getPreferences().bindToPreferences(askGenButton, AWPrefs.PREF_DEBUGGER_ASK_GEN, AWPrefs.DEFAULT_DEBUGGER_ASK_GEN);
    }

    public void prepareAdvancedTab() {
        getPreferences().bindToPreferences(checkGrammarSuccessButton, AWPrefs.PREF_ALERT_CHECK_GRAMMAR_SUCCESS, true);
        getPreferences().bindToPreferences(generateCodeSuccessButton, AWPrefs.PREF_ALERT_GENERATE_CODE_SUCCESS, true);
        getPreferences().bindToPreferences(fileModifiedOnDiskButton, AWPrefs.PREF_ALERT_FILE_CHANGES_DETECTED, true);
        getPreferences().bindToPreferences(interpreterLimitationButton, AWPrefs.PREF_ALERT_INTERPRETER_LIMITATION, true);

        getPreferences().bindToPreferences(clearConsoleBeforeCheckButton, AWPrefs.PREF_CLEAR_CONSOLE_BEFORE_CHECK, false);
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

    public void prepareTestRigTab() {
        testRigRadioButtonGroup = new ButtonGroup();
        testRigRadioButtonGroup.add(testRigDefaultRadio);
        testRigRadioButtonGroup.add(testRigTextRadio);

        TextUtils.createTabs(testTextArea);
        TextUtils.setDefaultTextPaneProperties(testTextArea);

        testTextArea.setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        testTextArea.setFocusable(true);
        testTextArea.requestFocusInWindow();

        testRigLanguageComboBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                prepareTestRigTabValues();
            }
        });

        testRigDefaultRadio.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (testRigDefaultRadio.isSelected()) {
                    testTextArea.setEnabled(false);
                } else {
                    testTextArea.setEnabled(true);
                }
            }
        });

        testRigTextRadio.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (testRigDefaultRadio.isSelected()) {
                    testTextArea.setEnabled(false);
                } else {
                    testTextArea.setEnabled(true);
                }
            }
        });

        prepareTestRigTabValues();
    }

    private void prepareTestRigTabValues() {
        String grammarLanguage = testRigLanguageComboBox.getSelectedItem().toString();
        testTextArea.setText(AWPrefs.getTestRigTemplateTextByLanguage(grammarLanguage));
        if ("".equals(testTextArea.getText())) {
            try {
                if ("JAVA".equalsIgnoreCase(grammarLanguage)) {
                    testTextArea.setText(Utils.stringFromFile(IDE.getApplicationPath() + File.separatorChar + 
                            DBLocal.parserGlueCodeTemplatePath + DBLocal.parserGlueCodeTemplateName + ".st"));
                } else if ("PYTHON".equalsIgnoreCase(grammarLanguage)) {
                    testTextArea.setText(Utils.stringFromFile(IDE.getApplicationPath() + File.separatorChar +
                            DBLocal.parserGlueCodeTemplatePath + DBLocal.parserGlueCodeTemplateName + "_python.st"));

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (AWPrefs.TEST_RIG_MODE_DEFAULT.equals(AWPrefs.getTestRigTemplateModeByLanguage(grammarLanguage))) {
            testRigDefaultRadio.setSelected(true);
            testTextArea.setEnabled(false);
        } else {
            testRigTextRadio.setSelected(true);
            testTextArea.setEnabled(true);
        }
    }

    private void applyTestRigPrefs() {
        String grammarLanguage = testRigLanguageComboBox.getSelectedItem().toString();
        AWPrefs.setTestRigTemplateModeByLanguage(grammarLanguage, testRigDefaultRadio.isSelected() ?
                AWPrefs.TEST_RIG_MODE_DEFAULT : AWPrefs.TEST_RIG_MODE_TEXT);
        AWPrefs.setTestRigTemplateTextByLanguage(grammarLanguage, testTextArea.getText());
    }

    public void displayTestRigTab() {
        tabbedPane1.setSelectedComponent(tabTestRig);
    }

    @Override
    public void becomingVisibleForTheFirstTime() {
        lafIndex = lafCombo.getSelectedIndex();
        javacPathField.setEnabled(javacCustomPathButton.isSelected());
        browseJavacPath.setEnabled(javacCustomPathButton.isSelected());
        customClasspathField.setEnabled(classpathCustomButton.isSelected());
        browseCustomClassPathButton.setEnabled(classpathCustomButton.isSelected());
        // not implemented
        //actionsFoldingAnchorsButton.setEnabled(foldingButton.isSelected());
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_PREFERENCES);
    }

    @Override
    public boolean close(boolean force) {
        apply();
        return super.close(force);
    }

    @Override
    public boolean isAuxiliaryWindow() {
        return true;
    }

    public static void applyCommonPrefs() {
        // * WARNING *
        // This function is called at startup and when applying preferences
        XJApplication.setAutoSave(AWPrefs.getAutoSaveEnabled(), AWPrefs.getAutoSaveDelay());
    }

    public JComponent getComponent() {
        return tabbedPane1;
    }

    public void apply() {
        dialogPane.requestFocusInWindow();
        getPreferences().applyPreferences();
        if(lafIndex != lafCombo.getSelectedIndex()) {
            lafIndex = lafCombo.getSelectedIndex();
            changeLookAndFeel();
        }
        applyCommonPrefs();
        applyTestRigPrefs();
        XJNotificationCenter.defaultCenter().postNotification(this, NOTIF_PREFS_APPLIED);
    }

    private void changeLookAndFeel() {
        XJLookAndFeel.applyLookAndFeel(AWPrefs.getLookAndFeel());
    }

    private static XJPreferences getPreferences() {
        return XJApplication.shared().getPreferences();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - ANTLR (www.antlr.org)
        dialogPane = new JPanel();
        contentPane = new JPanel();
        tabbedPane1 = new JTabbedPane();
        tabGeneral = new JPanel();
        label2 = new JLabel();
        label5 = new JLabel();
        lafCombo = new JComboBox();
        startupActionCombo = new JComboBox();
        restoreWindowsBoundButton = new JCheckBox();
        desktopModeButton = new JCheckBox();
        label25 = new JLabel();
        outputPathField = new JTextField();
        browseOutputPathButton = new JButton();
        label24 = new JLabel();
        dotToolPathField = new JTextField();
        browseDotToolPathButton = new JButton();
        label37 = new JLabel();
        antlr3OptionsField = new JTextField();
        tabEditor = new JPanel();
        label3 = new JLabel();
        editorFontCombo = new JComboBox();
        editorFontSizeSpinner = new JSpinner();
        autoSaveButton = new JCheckBox();
        autoSaveDelayField = new JTextField();
        label11 = new JLabel();
        backupFileButton = new JCheckBox();
        highlightCursorLineButton = new JCheckBox();
        smoothScrollingButton = new JCheckBox();
        autoIndentColonInRuleButton = new JCheckBox();
        showLineNumbers = new JCheckBox();
        vstyleAutocompletionButton = new JCheckBox();
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
        label38 = new JLabel();
        blockLabelsColorPanel = new JPanel();
        blockLabelsBoldButton = new JCheckBox();
        blockLabelsItalicButton = new JCheckBox();
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
        keywordsItalicButton = new JCheckBox();
        syntaxDefaultButton = new JButton();
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
        label9 = new JLabel();
        classpathSystemButton = new JCheckBox();
        classpathCustomButton = new JCheckBox();
        customClasspathField = new JTextField();
        browseCustomClassPathButton = new JButton();
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
        label36 = new JLabel();
        detachablePanelChildrenButton = new JCheckBox();
        askGenButton = new JCheckBox();
        panel1 = new JPanel();
        label8 = new JLabel();
        checkGrammarSuccessButton = new JCheckBox();
        generateCodeSuccessButton = new JCheckBox();
        fileModifiedOnDiskButton = new JCheckBox();
        interpreterLimitationButton = new JCheckBox();
        label6 = new JLabel();
        clearConsoleBeforeCheckButton = new JCheckBox();
        tabUpdates = new JPanel();
        label7 = new JLabel();
        updateTypeCombo = new JComboBox();
        checkForUpdatesButton = new JButton();
        label10 = new JLabel();
        downloadPathField = new JTextField();
        browseUpdateDownloadPathButton = new JButton();
        tabTestRig = new JPanel();
        label17 = new JLabel();
        testRigLanguageComboBox = new JComboBox();
        testRigDefaultRadio = new JRadioButton();
        testRigTextRadio = new JRadioButton();
        scrollPane1 = new JScrollPane();
        testTextArea = new JTextPane();
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
        				tabGeneral.add(label5, cc.xy(3, 7));
        				tabGeneral.add(lafCombo, cc.xywh(5, 7, 3, 1));

        				//---- startupActionCombo ----
        				startupActionCombo.setModel(new DefaultComboBoxModel(new String[] {
        					"Create a new document",
        					"Open the last opened document",
        					"Open the last saved document",
        					"Open all opened documents when ANTLRWorks was closed"
        				}));
        				tabGeneral.add(startupActionCombo, cc.xywh(5, 3, 3, 1));

        				//---- restoreWindowsBoundButton ----
        				restoreWindowsBoundButton.setText("Restore project's windows position and size");
        				tabGeneral.add(restoreWindowsBoundButton, cc.xywh(5, 5, 3, 1));

        				//---- desktopModeButton ----
        				desktopModeButton.setText("Desktop Mode");
        				desktopModeButton.setToolTipText("This option will take effect after restarting ANTLRWorks");
        				tabGeneral.add(desktopModeButton, cc.xywh(5, 9, 3, 1));

        				//---- label25 ----
        				label25.setText("Output path:");
        				tabGeneral.add(label25, cc.xy(3, 11));

        				//---- outputPathField ----
        				outputPathField.setToolTipText("Relative path will be generated in the same directory as the grammar itself");
        				tabGeneral.add(outputPathField, cc.xywh(5, 11, 3, 1));

        				//---- browseOutputPathButton ----
        				browseOutputPathButton.setText("Browse...");
        				tabGeneral.add(browseOutputPathButton, cc.xy(9, 11));

        				//---- label24 ----
        				label24.setText("DOT path:");
        				tabGeneral.add(label24, cc.xy(3, 13));

        				//---- dotToolPathField ----
        				dotToolPathField.setToolTipText("Absolute path to the DOT command-line tool");
        				tabGeneral.add(dotToolPathField, cc.xywh(5, 13, 3, 1));

        				//---- browseDotToolPathButton ----
        				browseDotToolPathButton.setText("Browse...");
        				tabGeneral.add(browseDotToolPathButton, cc.xy(9, 13));

        				//---- label37 ----
        				label37.setText("ANTLR options:");
        				tabGeneral.add(label37, cc.xy(3, 15));
        				tabGeneral.add(antlr3OptionsField, cc.xywh(5, 15, 3, 1));
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
        				editorFontSizeSpinner.setModel(new SpinnerNumberModel(12, 8, null, 1));
        				tabEditor.add(editorFontSizeSpinner, cc.xy(11, 3));

        				//---- autoSaveButton ----
        				autoSaveButton.setText("Auto-save every");
        				tabEditor.add(autoSaveButton, cc.xywh(5, 5, 3, 1));
        				tabEditor.add(autoSaveDelayField, cc.xy(9, 5));

        				//---- label11 ----
        				label11.setText("minutes");
        				tabEditor.add(label11, cc.xy(11, 5));

        				//---- backupFileButton ----
        				backupFileButton.setText("Create backup file");
        				tabEditor.add(backupFileButton, cc.xywh(5, 7, 5, 1));

        				//---- highlightCursorLineButton ----
        				highlightCursorLineButton.setText("Highlight cursor line");
        				tabEditor.add(highlightCursorLineButton, cc.xywh(5, 9, 5, 1));

        				//---- smoothScrollingButton ----
        				smoothScrollingButton.setText("Smooth scrolling");
        				tabEditor.add(smoothScrollingButton, cc.xywh(5, 11, 3, 1));

        				//---- autoIndentColonInRuleButton ----
        				autoIndentColonInRuleButton.setText("Auto-indent ':' in rule");
        				tabEditor.add(autoIndentColonInRuleButton, cc.xywh(5, 13, 7, 1));

        				//---- showLineNumbers ----
        				showLineNumbers.setText("Show line numbers");
        				tabEditor.add(showLineNumbers, cc.xywh(5, 15, 5, 1));

        				//---- vstyleAutocompletionButton ----
        				vstyleAutocompletionButton.setText("Visual Studio auto-completion menu");
        				vstyleAutocompletionButton.setToolTipText("Displayed when a space is pressed, remember previous auto-completed word, continues to auto-complete without decreasing the word choice as you type");
        				tabEditor.add(vstyleAutocompletionButton, cc.xywh(5, 17, 9, 1));

        				//---- label1 ----
        				label1.setText("Tab width:");
        				label1.setHorizontalAlignment(SwingConstants.RIGHT);
        				tabEditor.add(label1, cc.xy(3, 19));

        				//---- tabWidthField ----
        				tabWidthField.setText("8");
        				tabEditor.add(tabWidthField, cc.xy(5, 19));

        				//---- label22 ----
        				label22.setText("Update delay:");
        				tabEditor.add(label22, cc.xy(3, 21));

        				//---- parserDelayField ----
        				parserDelayField.setText("250");
        				tabEditor.add(parserDelayField, cc.xy(5, 21));

        				//---- label23 ----
        				label23.setText("ms");
        				tabEditor.add(label23, cc.xy(7, 21));
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
        						FormFactory.DEFAULT_ROWSPEC,
        						FormFactory.LINE_GAP_ROWSPEC,
        						FormFactory.DEFAULT_ROWSPEC
        					}));

        				//---- label26 ----
        				label26.setText("Parser References:");
        				tabSyntax.add(label26, cc.xywh(3, 3, 1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));

        				//======== parserColorPanel ========
        				{
        					parserColorPanel.setForeground(Color.black);
        					parserColorPanel.setPreferredSize(new Dimension(70, 20));
        					parserColorPanel.setBackground(new Color(255, 255, 51));
        					parserColorPanel.setBorder(LineBorder.createBlackLineBorder());
        					parserColorPanel.setLayout(new FlowLayout());
        				}
        				tabSyntax.add(parserColorPanel, cc.xy(5, 3));

        				//---- parserBoldButton ----
        				parserBoldButton.setText("Bold");
        				parserBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        				tabSyntax.add(parserBoldButton, cc.xy(7, 3));

        				//---- parserItalicButton ----
        				parserItalicButton.setText("Italic");
        				parserItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
        				tabSyntax.add(parserItalicButton, cc.xy(9, 3));

        				//---- label27 ----
        				label27.setText("Lexer References:");
        				tabSyntax.add(label27, cc.xy(3, 5));

        				//======== lexerColorPanel ========
        				{
        					lexerColorPanel.setForeground(Color.black);
        					lexerColorPanel.setPreferredSize(new Dimension(70, 20));
        					lexerColorPanel.setBackground(new Color(255, 255, 51));
        					lexerColorPanel.setBorder(LineBorder.createBlackLineBorder());
        					lexerColorPanel.setLayout(new FlowLayout());
        				}
        				tabSyntax.add(lexerColorPanel, cc.xy(5, 5));

        				//---- lexerBoldButton ----
        				lexerBoldButton.setText("Bold");
        				lexerBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        				tabSyntax.add(lexerBoldButton, cc.xy(7, 5));

        				//---- lexerItalicButton ----
        				lexerItalicButton.setText("Italic");
        				lexerItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
        				tabSyntax.add(lexerItalicButton, cc.xy(9, 5));

        				//---- label28 ----
        				label28.setText("Labels:");
        				tabSyntax.add(label28, cc.xy(3, 7));

        				//======== labelColorPanel ========
        				{
        					labelColorPanel.setForeground(Color.black);
        					labelColorPanel.setPreferredSize(new Dimension(70, 20));
        					labelColorPanel.setBackground(new Color(255, 255, 51));
        					labelColorPanel.setBorder(LineBorder.createBlackLineBorder());
        					labelColorPanel.setLayout(new FlowLayout());
        				}
        				tabSyntax.add(labelColorPanel, cc.xy(5, 7));

        				//---- labelsBoldButton ----
        				labelsBoldButton.setText("Bold");
        				labelsBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        				tabSyntax.add(labelsBoldButton, cc.xy(7, 7));

        				//---- labelsItalicButton ----
        				labelsItalicButton.setText("Italic");
        				labelsItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
        				tabSyntax.add(labelsItalicButton, cc.xy(9, 7));

        				//---- label29 ----
        				label29.setText("References in action:");
        				tabSyntax.add(label29, cc.xy(3, 9));

        				//======== refsActionColorPanel ========
        				{
        					refsActionColorPanel.setForeground(Color.black);
        					refsActionColorPanel.setPreferredSize(new Dimension(70, 20));
        					refsActionColorPanel.setBackground(new Color(255, 255, 51));
        					refsActionColorPanel.setBorder(LineBorder.createBlackLineBorder());
        					refsActionColorPanel.setLayout(new FlowLayout());
        				}
        				tabSyntax.add(refsActionColorPanel, cc.xy(5, 9));

        				//---- refsActionBoldButton ----
        				refsActionBoldButton.setText("Bold");
        				refsActionBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        				tabSyntax.add(refsActionBoldButton, cc.xy(7, 9));

        				//---- refsActionItalicButton ----
        				refsActionItalicButton.setText("Italic");
        				refsActionItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
        				tabSyntax.add(refsActionItalicButton, cc.xy(9, 9));

        				//---- label38 ----
        				label38.setText("Blocks:");
        				tabSyntax.add(label38, cc.xy(3, 11));

        				//======== blockLabelsColorPanel ========
        				{
        					blockLabelsColorPanel.setForeground(Color.black);
        					blockLabelsColorPanel.setPreferredSize(new Dimension(70, 20));
        					blockLabelsColorPanel.setBackground(new Color(255, 255, 51));
        					blockLabelsColorPanel.setBorder(LineBorder.createBlackLineBorder());
        					blockLabelsColorPanel.setLayout(new FlowLayout());
        				}
        				tabSyntax.add(blockLabelsColorPanel, cc.xy(5, 11));

        				//---- blockLabelsBoldButton ----
        				blockLabelsBoldButton.setText("Bold");
        				blockLabelsBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        				tabSyntax.add(blockLabelsBoldButton, cc.xy(7, 11));

        				//---- blockLabelsItalicButton ----
        				blockLabelsItalicButton.setText("Italic");
        				blockLabelsItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
        				tabSyntax.add(blockLabelsItalicButton, cc.xy(9, 11));

        				//---- label30 ----
        				label30.setText("Comments:");
        				tabSyntax.add(label30, cc.xy(3, 13));

        				//======== commentsColorPanel ========
        				{
        					commentsColorPanel.setForeground(Color.black);
        					commentsColorPanel.setPreferredSize(new Dimension(70, 20));
        					commentsColorPanel.setBackground(new Color(255, 255, 51));
        					commentsColorPanel.setBorder(LineBorder.createBlackLineBorder());
        					commentsColorPanel.setLayout(new FlowLayout());
        				}
        				tabSyntax.add(commentsColorPanel, cc.xy(5, 13));

        				//---- commentsBoldButton ----
        				commentsBoldButton.setText("Bold");
        				commentsBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        				tabSyntax.add(commentsBoldButton, cc.xy(7, 13));

        				//---- commentsItalicButton ----
        				commentsItalicButton.setText("Italic");
        				commentsItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
        				tabSyntax.add(commentsItalicButton, cc.xy(9, 13));

        				//---- label31 ----
        				label31.setText("Strings:");
        				tabSyntax.add(label31, cc.xy(3, 15));

        				//======== stringsColorPanel ========
        				{
        					stringsColorPanel.setForeground(Color.black);
        					stringsColorPanel.setPreferredSize(new Dimension(70, 20));
        					stringsColorPanel.setBackground(new Color(255, 255, 51));
        					stringsColorPanel.setBorder(LineBorder.createBlackLineBorder());
        					stringsColorPanel.setLayout(new FlowLayout());
        				}
        				tabSyntax.add(stringsColorPanel, cc.xy(5, 15));

        				//---- stringsBoldButton ----
        				stringsBoldButton.setText("Bold");
        				stringsBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        				tabSyntax.add(stringsBoldButton, cc.xy(7, 15));

        				//---- stringsItalicButton ----
        				stringsItalicButton.setText("Italic");
        				stringsItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
        				tabSyntax.add(stringsItalicButton, cc.xy(9, 15));

        				//---- label32 ----
        				label32.setText("Keywords:");
        				tabSyntax.add(label32, cc.xy(3, 17));

        				//======== keywordsColorPanel ========
        				{
        					keywordsColorPanel.setForeground(Color.black);
        					keywordsColorPanel.setPreferredSize(new Dimension(70, 20));
        					keywordsColorPanel.setBackground(new Color(255, 255, 51));
        					keywordsColorPanel.setBorder(LineBorder.createBlackLineBorder());
        					keywordsColorPanel.setLayout(new FlowLayout());
        				}
        				tabSyntax.add(keywordsColorPanel, cc.xy(5, 17));

        				//---- keywordsBoldButton ----
        				keywordsBoldButton.setText("Bold");
        				keywordsBoldButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        				tabSyntax.add(keywordsBoldButton, cc.xy(7, 17));

        				//---- keywordsItalicButton ----
        				keywordsItalicButton.setText("Italic");
        				keywordsItalicButton.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
        				tabSyntax.add(keywordsItalicButton, cc.xy(9, 17));

        				//---- syntaxDefaultButton ----
        				syntaxDefaultButton.setText("Default");
        				tabSyntax.add(syntaxDefaultButton, cc.xy(13, 19));
        			}
        			tabbedPane1.addTab("Syntax", tabSyntax);


        			//======== tabCompiler ========
        			{
        				tabCompiler.setLayout(new FormLayout(
        					new ColumnSpec[] {
        						new ColumnSpec(Sizes.dluX(10)),
        						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        						FormFactory.DEFAULT_COLSPEC,
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
        						FormFactory.DEFAULT_ROWSPEC,
        						FormFactory.LINE_GAP_ROWSPEC,
        						new RowSpec(Sizes.dluY(10)),
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
        				integratedRadio.setText("com.sun.tools.javac");
        				integratedRadio.setActionCommand("integrated");
        				tabCompiler.add(integratedRadio, cc.xywh(3, 11, 3, 1));

        				//---- javacRadio ----
        				javacRadio.setText("javac");
        				javacRadio.setSelected(true);
        				tabCompiler.add(javacRadio, cc.xywh(3, 3, 2, 1));

        				//---- javacCustomPathButton ----
        				javacCustomPathButton.setText("Path:");
        				javacCustomPathButton.setToolTipText("Check to specify a custom path if the default system path doesn't include javac");
        				tabCompiler.add(javacCustomPathButton, cc.xywh(4, 5, 1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
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

        				//---- label9 ----
        				label9.setText("Classpath:");
        				tabCompiler.add(label9, cc.xy(3, 15));

        				//---- classpathSystemButton ----
        				classpathSystemButton.setText("System");
        				tabCompiler.add(classpathSystemButton, cc.xy(4, 15));

        				//---- classpathCustomButton ----
        				classpathCustomButton.setText("Custom:");
        				tabCompiler.add(classpathCustomButton, cc.xy(4, 17));
        				tabCompiler.add(customClasspathField, cc.xy(5, 17));

        				//---- browseCustomClassPathButton ----
        				browseCustomClassPathButton.setText("Browse...");
        				tabCompiler.add(browseCustomClassPathButton, cc.xy(6, 17));
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
        				debugDefaultLocalPortField.setText("491000");
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
        				label12.setText("Non-consumed token:");
        				label12.setHorizontalAlignment(SwingConstants.RIGHT);
        				tabDebugger.add(label12, cc.xy(3, 7));

        				//======== debugNonConsumedColorPanel ========
        				{
        					debugNonConsumedColorPanel.setForeground(Color.black);
        					debugNonConsumedColorPanel.setPreferredSize(new Dimension(70, 20));
        					debugNonConsumedColorPanel.setBackground(new Color(255, 255, 51));
        					debugNonConsumedColorPanel.setBorder(LineBorder.createBlackLineBorder());
        					debugNonConsumedColorPanel.setLayout(new FlowLayout());
        				}
        				tabDebugger.add(debugNonConsumedColorPanel, cc.xy(5, 7));

        				//---- label13 ----
        				label13.setText("Consumed token:");
        				label13.setHorizontalAlignment(SwingConstants.RIGHT);
        				tabDebugger.add(label13, cc.xy(3, 9));

        				//======== debugConsumedColorPanel ========
        				{
        					debugConsumedColorPanel.setForeground(Color.black);
        					debugConsumedColorPanel.setPreferredSize(new Dimension(70, 20));
        					debugConsumedColorPanel.setBackground(new Color(255, 255, 51));
        					debugConsumedColorPanel.setBorder(LineBorder.createBlackLineBorder());
        					debugConsumedColorPanel.setLayout(new FlowLayout());
        				}
        				tabDebugger.add(debugConsumedColorPanel, cc.xy(5, 9));

        				//---- label14 ----
        				label14.setText("Hidden token:");
        				label14.setHorizontalAlignment(SwingConstants.RIGHT);
        				tabDebugger.add(label14, cc.xy(3, 11));

        				//======== debugHiddenColorPanel ========
        				{
        					debugHiddenColorPanel.setForeground(Color.black);
        					debugHiddenColorPanel.setPreferredSize(new Dimension(70, 20));
        					debugHiddenColorPanel.setBackground(new Color(255, 255, 51));
        					debugHiddenColorPanel.setBorder(LineBorder.createBlackLineBorder());
        					debugHiddenColorPanel.setLayout(new FlowLayout());
        				}
        				tabDebugger.add(debugHiddenColorPanel, cc.xy(5, 11));

        				//---- label15 ----
        				label15.setText("Dead token:");
        				label15.setHorizontalAlignment(SwingConstants.RIGHT);
        				tabDebugger.add(label15, cc.xy(3, 13));

        				//======== debugDeadColorPanel ========
        				{
        					debugDeadColorPanel.setForeground(Color.black);
        					debugDeadColorPanel.setPreferredSize(new Dimension(70, 20));
        					debugDeadColorPanel.setBackground(new Color(255, 255, 51));
        					debugDeadColorPanel.setBorder(LineBorder.createBlackLineBorder());
        					debugDeadColorPanel.setLayout(new FlowLayout());
        				}
        				tabDebugger.add(debugDeadColorPanel, cc.xy(5, 13));

        				//---- label16 ----
        				label16.setText("Lookahead token:");
        				label16.setHorizontalAlignment(SwingConstants.RIGHT);
        				tabDebugger.add(label16, cc.xy(3, 15));

        				//======== debugLTColorPanel ========
        				{
        					debugLTColorPanel.setForeground(Color.black);
        					debugLTColorPanel.setPreferredSize(new Dimension(70, 20));
        					debugLTColorPanel.setBackground(new Color(255, 255, 51));
        					debugLTColorPanel.setBorder(LineBorder.createBlackLineBorder());
        					debugLTColorPanel.setLayout(new FlowLayout());
        				}
        				tabDebugger.add(debugLTColorPanel, cc.xy(5, 15));

        				//---- label36 ----
        				label36.setText("Detachable panels:");
        				tabDebugger.add(label36, cc.xy(3, 19));

        				//---- detachablePanelChildrenButton ----
        				detachablePanelChildrenButton.setText("Children of project's window");
        				tabDebugger.add(detachablePanelChildrenButton, cc.xywh(5, 19, 5, 1));

        				//---- askGenButton ----
        				askGenButton.setText("Ask before generating and compiling");
        				tabDebugger.add(askGenButton, cc.xywh(5, 21, 5, 1));
        			}
        			tabbedPane1.addTab("Debugger", tabDebugger);


        			//======== panel1 ========
        			{
        				panel1.setLayout(new FormLayout(
        					new ColumnSpec[] {
        						new ColumnSpec(Sizes.dluX(10)),
        						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        						FormFactory.DEFAULT_COLSPEC,
        						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        						FormFactory.DEFAULT_COLSPEC
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

        				//---- label8 ----
        				label8.setText("Display alerts:");
        				panel1.add(label8, cc.xywh(3, 3, 1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));

        				//---- checkGrammarSuccessButton ----
        				checkGrammarSuccessButton.setText("Successfull check grammar");
        				panel1.add(checkGrammarSuccessButton, cc.xy(5, 3));

        				//---- generateCodeSuccessButton ----
        				generateCodeSuccessButton.setText("Successfull code generation");
        				panel1.add(generateCodeSuccessButton, cc.xy(5, 5));

        				//---- fileModifiedOnDiskButton ----
        				fileModifiedOnDiskButton.setText("File modified on disk");
        				panel1.add(fileModifiedOnDiskButton, cc.xy(5, 7));

        				//---- interpreterLimitationButton ----
        				interpreterLimitationButton.setText("Interpreter limitations");
        				panel1.add(interpreterLimitationButton, cc.xy(5, 9));

        				//---- label6 ----
        				label6.setText("Console:");
        				panel1.add(label6, cc.xywh(3, 11, 1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));

        				//---- clearConsoleBeforeCheckButton ----
        				clearConsoleBeforeCheckButton.setText("Clear before checking grammar");
        				panel1.add(clearConsoleBeforeCheckButton, cc.xy(5, 11));
        			}
        			tabbedPane1.addTab("Advanced", panel1);


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
        				label7.setText("Check for ANTLRWorks updates:");
        				label7.setHorizontalAlignment(SwingConstants.LEFT);
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
        				label10.setText("Download path:");
        				label10.setHorizontalAlignment(SwingConstants.LEFT);
        				tabUpdates.add(label10, cc.xy(3, 9));
        				tabUpdates.add(downloadPathField, cc.xywh(3, 11, 3, 1));

        				//---- browseUpdateDownloadPathButton ----
        				browseUpdateDownloadPathButton.setText("Browse...");
        				browseUpdateDownloadPathButton.setActionCommand("Browse");
        				tabUpdates.add(browseUpdateDownloadPathButton, cc.xy(7, 11));
        			}
        			tabbedPane1.addTab("Updates", tabUpdates);

                    //======== tabTestRig ========
                    {
                        tabTestRig.setLayout(new FormLayout(
                                new ColumnSpec[] {
                                    new ColumnSpec(Sizes.dluX(10)),
                                    FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                    FormFactory.DEFAULT_COLSPEC,
                                    FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                                    FormFactory.DEFAULT_COLSPEC,
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
                                    FormFactory.UNRELATED_GAP_ROWSPEC,
                                    FormFactory.DEFAULT_ROWSPEC,
                                    FormFactory.LINE_GAP_ROWSPEC,
                                    new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                                    FormFactory.LINE_GAP_ROWSPEC,
                                    new RowSpec(Sizes.dluY(10))
                                }));

                        //---- label17 ----
                        label17.setText("Test Rig for:");
                        tabTestRig.add(label17, cc.xywh(3, 3, 3, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));

                        //---- testRigLanguageComboBox ----
                        testRigLanguageComboBox.setModel(new DefaultComboBoxModel(new String[] {
                            "Java",
                            "Python"
                        }));
                        tabTestRig.add(testRigLanguageComboBox, cc.xy(7, 3));

                        //---- testRigDefaultRadio ----
                        testRigDefaultRadio.setText("Use default Test Rig Template");
                        testRigDefaultRadio.setSelected(true);
                        tabTestRig.add(testRigDefaultRadio, cc.xywh(5, 5, 5, 1));

                        //---- testRigTextRadio ----
                        testRigTextRadio.setText("Text:");
                        tabTestRig.add(testRigTextRadio, cc.xywh(5, 7, 1, 1, CellConstraints.DEFAULT, CellConstraints.TOP));

                        //======== scrollPane1 ========
                        {
                            scrollPane1.setViewportView(testTextArea);
                        }
                        tabTestRig.add(scrollPane1, cc.xywh(7, 7, 3, 1));
                    }
                    tabbedPane1.addTab("Test Rig", tabTestRig);

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
        pack();
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Open Source Project license - ANTLR (www.antlr.org)
    private JPanel dialogPane;
    private JPanel contentPane;
    private JTabbedPane tabbedPane1;
    private JPanel tabGeneral;
    private JLabel label2;
    private JLabel label5;
    private JComboBox lafCombo;
    private JComboBox startupActionCombo;
    private JCheckBox restoreWindowsBoundButton;
    private JCheckBox desktopModeButton;
    private JLabel label25;
    private JTextField outputPathField;
    private JButton browseOutputPathButton;
    private JLabel label24;
    private JTextField dotToolPathField;
    private JButton browseDotToolPathButton;
    private JLabel label37;
    private JTextField antlr3OptionsField;
    private JPanel tabEditor;
    private JLabel label3;
    private JComboBox editorFontCombo;
    private JSpinner editorFontSizeSpinner;
    private JCheckBox autoSaveButton;
    private JTextField autoSaveDelayField;
    private JLabel label11;
    private JCheckBox backupFileButton;
    private JCheckBox highlightCursorLineButton;
    private JCheckBox smoothScrollingButton;
    private JCheckBox autoIndentColonInRuleButton;
    private JCheckBox showLineNumbers;
    private JCheckBox vstyleAutocompletionButton;
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
    private JLabel label38;
    private JPanel blockLabelsColorPanel;
    private JCheckBox blockLabelsBoldButton;
    private JCheckBox blockLabelsItalicButton;
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
    private JCheckBox keywordsItalicButton;
    private JButton syntaxDefaultButton;
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
    private JLabel label9;
    private JCheckBox classpathSystemButton;
    private JCheckBox classpathCustomButton;
    private JTextField customClasspathField;
    private JButton browseCustomClassPathButton;
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
    private JLabel label36;
    private JCheckBox detachablePanelChildrenButton;
    private JCheckBox askGenButton;
    private JPanel panel1;
    private JLabel label8;
    private JCheckBox checkGrammarSuccessButton;
    private JCheckBox generateCodeSuccessButton;
    private JCheckBox fileModifiedOnDiskButton;
    private JCheckBox interpreterLimitationButton;
    private JLabel label6;
    private JCheckBox clearConsoleBeforeCheckButton;
    private JPanel tabUpdates;
    private JLabel label7;
    private JComboBox updateTypeCombo;
    private JButton checkForUpdatesButton;
    private JLabel label10;
    private JTextField downloadPathField;
    private JButton browseUpdateDownloadPathButton;
    private JPanel tabTestRig;
    private JLabel label17;
    private JComboBox testRigLanguageComboBox;
    private JRadioButton testRigDefaultRadio;
    private JRadioButton testRigTextRadio;
    private JScrollPane scrollPane1;
    private JTextPane testTextArea;
    private JPanel buttonBar;
    private JButton applyButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}
