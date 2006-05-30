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

package org.antlr.works.editor;

import edu.usfca.xj.appkit.menu.*;
import org.antlr.works.IDE;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.prefs.AWPrefs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

public class EditorMenu implements XJMenuItemDelegate {

    // Edit
    public static final int MI_TOGGLE_SYNTAX_COLORING = 6;
    public static final int MI_TOGGLE_SYNTAX_DIAGRAM = 7;
    public static final int MI_TOGGLE_NFA_OPTIMIZATION = 9;
    public static final int MI_TOGGLE_AUTOINDENT = 10;

    // View
    public static final int MI_EXPAND_COLLAPSE_RULE = 20;
    public static final int MI_EXPAND_ALL_RULES = 21;
    public static final int MI_COLLAPSE_ALL_RULES = 22;
    public static final int MI_EXPAND_COLLAPSE_ACTION = 23;
    public static final int MI_EXPAND_ALL_ACTIONS = 24;
    public static final int MI_COLLAPSE_ALL_ACTIONS = 25;

    // Find
    public static final int MI_FIND = 30;
    public static final int MI_FIND_NEXT = 31;
    public static final int MI_FIND_PREV = 32;
    public static final int MI_FIND_TOKEN = 33;
    public static final int MI_FIND_USAGE = 34;

    // Go To
    public static final int MI_GOTO_RULE = 40;
    public static final int MI_GOTO_DECLARATION = 41;
    public static final int MI_GOTO_LINE = 42;
    public static final int MI_GOTO_CHARACTER = 43;
    public static final int MI_GOTO_BACK = 44;
    public static final int MI_GOTO_FORWARD = 45;
    public static final int MI_PREV_BREAKPOINT = 46;
    public static final int MI_NEXT_BREAKPOINT = 47;

    // Grammar
    public static final int MI_SHOW_TOKENS_SD = 50;
    public static final int MI_SHOW_TOKENS_DFA = 51;
    public static final int MI_SHOW_DECISION_DFA = 52;
    public static final int MI_SHOW_DEPENDENCY = 53;
    public static final int MI_INSERT_TEMPLATE = 54;
    public static final int MI_GROUP_RULE = 55;
    public static final int MI_UNGROUP_RULE = 56;
    public static final int MI_IGNORE_RULE = 57;
    public static final int MI_CHECK_GRAMMAR = 58;

    // Refactor
    public static final int MI_RENAME = 60;
    public static final int MI_REPLACE_LITERAL_WITH_TOKEN_LABEL = 61;
    public static final int MI_LITERAL_TO_SINGLEQUOTE = 62;
    public static final int MI_LITERAL_TO_DOUBLEQUOTE = 63;
    public static final int MI_LITERAL_TO_CSTYLEQUOTE = 64;
    public static final int MI_REMOVE_LEFT_RECURSION = 65;
    public static final int MI_REMOVE_ALL_LEFT_RECURSION = 66;
    public static final int MI_EXTRACT_RULE = 67;
    public static final int MI_INLINE_RULE = 68;

    // Generate
    public static final int MI_GENERATE_CODE = 70;
    public static final int MI_SHOW_GENERATED_PARSER_CODE = 71;
    public static final int MI_SHOW_GENERATED_LEXER_CODE = 72;
    public static final int MI_SHOW_RULE_GENCODE = 73;

    // Debugger
    public static final int MI_RUN_INTERPRETER = 80;
    public static final int MI_DEBUG = 81;
    public static final int MI_BUILD_AND_DEBUG = 82;
    public static final int MI_DEBUG_REMOTE = 83;
    public static final int MI_DEBUG_SHOW_INFO_PANEL = 84;
    public static final int MI_DEBUG_SHOW_OUTPUT_PANEL = 85;
    public static final int MI_DEBUG_SHOW_INPUT_TOKENS = 86;

    // SCM
    public static final int MI_P4_EDIT = 90;
    public static final int MI_P4_ADD = 91;
    public static final int MI_P4_DELETE = 92;
    public static final int MI_P4_REVERT = 93;
    public static final int MI_P4_SUBMIT = 94;
    public static final int MI_P4_SYNC = 95;

    // Help
    public static final int MI_SUBMIT_STATS = 100;
    public static final int MI_SEND_FEEDBACK = 101;
    public static final int MI_CHECK_UPDATES = 102;

    // File Export
    public static final int MI_EXPORT_AS_IMAGE = 110;
    public static final int MI_EXPORT_AS_EPS = 111;
    public static final int MI_EXPORT_AS_DOT = 112;
    public static final int MI_EXPORT_EVENT = 115;

    public static final int MI_PRIVATE_UNREGISTER = 200;

    protected CEditorGrammar editor = null;
    protected XJMenuItem ignoreRuleMenuItem;

    /** The resource bundle used to get localized strings */
    protected static ResourceBundle resourceBundle = IDE.getMenusResourceBundle();

    public EditorMenu(CEditorGrammar editor) {
        this.editor = editor;
    }

    public void close() {
    }

    public boolean isDebuggerRunning() {
        return editor.debugger.isRunning();
    }

    public void customizeFileMenu(XJMenu menu) {
        XJMenu exportMenu = new XJMenu();
        exportMenu.setTitle(resourceBundle.getString("menu.title.exportEvents"));
        exportMenu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.asText"), MI_EXPORT_EVENT, this));

        menu.insertItemAfter(exportMenu, XJMainMenuBar.MI_SAVEAS);

        exportMenu = new XJMenu();
        exportMenu.setTitle(resourceBundle.getString("menu.title.export"));
        exportMenu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.exportAsEPS"), MI_EXPORT_AS_EPS, this));
        exportMenu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.exportAsBitmap"), MI_EXPORT_AS_IMAGE, this));
        exportMenu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.exportAsDot"), MI_EXPORT_AS_DOT, this));

        menu.insertItemAfter(exportMenu, XJMainMenuBar.MI_SAVEAS);

        menu.insertSeparatorAfter(XJMainMenuBar.MI_SAVEAS);
    }

    public void customizeMenuBar(XJMainMenuBar menubar) {
        createFindMenu(menubar);
        createGoToMenu(menubar);
        createGrammarMenu(menubar);
        createRefactorMenu(menubar);
        createGenerateMenu(menubar);
        createRunMenu(menubar);
        createSCMMenu(menubar);
        createPrivateMenu(menubar);
    }

    private void createPrivateMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        if(AWPrefs.getPrivateMenu()) {
            menu = new XJMenu();
            menu.setTitle("*");
            menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.unregisterUser"), MI_PRIVATE_UNREGISTER, this));

            menubar.addCustomMenu(menu);
        }
    }

    private void createSCMMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle(resourceBundle.getString("menu.title.scm"));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.scmOpenForEdit"), MI_P4_EDIT, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.scmMarkForAdd"), MI_P4_ADD, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.scmMarkForDelete"), MI_P4_DELETE, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.scmRevert"), MI_P4_REVERT, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.scmSubmit"), MI_P4_SUBMIT, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.scmSync"), MI_P4_SYNC, this));

        menubar.addCustomMenu(menu);
    }

    private void createRunMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle(resourceBundle.getString("menu.title.debugger"));
        //menu.addItem(new XJMenuItem("Run Interpreter", KeyEvent.VK_F8, MI_RUN_INTERPRETER, this));
        //menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.debug"), KeyEvent.VK_F9, MI_DEBUG, this));
        /** Removed since 05/01/06 because 'Debug' automatically detects any change
         * to the grammar and rebuild it.
         */
        //menu.addItem(new XJMenuItem("Build and Debug...", KeyEvent.VK_F10, MI_BUILD_AND_DEBUG, this));
        //menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.debugRemote"), KeyEvent.VK_F11, MI_DEBUG_REMOTE, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showInformation"), MI_DEBUG_SHOW_INFO_PANEL, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showOutput"), MI_DEBUG_SHOW_OUTPUT_PANEL, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showInputTokens"), MI_DEBUG_SHOW_INPUT_TOKENS, this));

        menubar.addCustomMenu(menu);
    }

    private void createGenerateMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle(resourceBundle.getString("menu.title.generate"));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.generateCode"), MI_GENERATE_CODE, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showParserCode"), MI_SHOW_GENERATED_PARSER_CODE, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showLexerCode"), MI_SHOW_GENERATED_LEXER_CODE, this));
        menu.addSeparator();
        menu.addItem(createMenuItem(MI_SHOW_RULE_GENCODE));

        menubar.addCustomMenu(menu);
    }

    private void createGoToMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle(resourceBundle.getString("menu.title.goto"));

        menu.addItem(createMenuItem(MI_GOTO_RULE));
        menu.addItem(createMenuItem(MI_GOTO_DECLARATION));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.gotoLine"), KeyEvent.VK_G, MI_GOTO_LINE, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.gotoCharacter"), MI_GOTO_CHARACTER, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.gotoBack"), KeyEvent.VK_LEFT, XJMenuItem.getKeyModifier() | Event.ALT_MASK, MI_GOTO_BACK, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.gotoForward"), KeyEvent.VK_RIGHT, XJMenuItem.getKeyModifier() | Event.ALT_MASK, MI_GOTO_FORWARD, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.gotoPreviousBreakpoint"), MI_PREV_BREAKPOINT, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.gotoNextBreakpoint"), MI_NEXT_BREAKPOINT, this));

        menubar.addCustomMenu(menu);
    }

    private void createRefactorMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle(resourceBundle.getString("menu.title.refactor"));
        menu.addItem(createMenuItem(MI_RENAME));
        menu.addItem(createMenuItem(MI_REPLACE_LITERAL_WITH_TOKEN_LABEL));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.removeLeftRecursion"), MI_REMOVE_LEFT_RECURSION, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.removeAllLeftRecursion"), MI_REMOVE_ALL_LEFT_RECURSION, this));
        menu.addSeparator();
        menu.addItem(createMenuItem(MI_EXTRACT_RULE));
        menu.addItem(createMenuItem(MI_INLINE_RULE));
        menu.addSeparator();

        XJMenu literals = new XJMenu();
        literals.setTitle(resourceBundle.getString("menu.title.convertLiterals"));
        literals.addItem(new XJMenuItem(resourceBundle.getString("menu.item.convertToSingleQuote"), MI_LITERAL_TO_SINGLEQUOTE, this));
        literals.addItem(new XJMenuItem(resourceBundle.getString("menu.item.convertToDoubleQuote"), MI_LITERAL_TO_DOUBLEQUOTE, this));
        literals.addItem(new XJMenuItem(resourceBundle.getString("menu.item.convertToCStyleQuote"), MI_LITERAL_TO_CSTYLEQUOTE, this));

        menu.addItem(literals);

        menubar.addCustomMenu(menu);
    }

    private void createGrammarMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle(resourceBundle.getString("menu.title.grammar"));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showTokensSyntaxDiagram"), MI_SHOW_TOKENS_SD, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showTokensDFA"), MI_SHOW_TOKENS_DFA, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showDecisionDFA"), MI_SHOW_DECISION_DFA, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showRuleDependencyGraph"), MI_SHOW_DEPENDENCY, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.insertRuleFromTemplate"), KeyEvent.VK_T, MI_INSERT_TEMPLATE, this));

        XJMenu rules = new XJMenu();
        rules.setTitle(resourceBundle.getString("menu.title.rules"));
        rules.addItem(createMenuItem(MI_GROUP_RULE));
        rules.addItem(createMenuItem(MI_UNGROUP_RULE));
        rules.addSeparator();
        rules.addItem(ignoreRuleMenuItem = createMenuItem(MI_IGNORE_RULE));

        XJMenu folding = new XJMenu();
        folding.setTitle(resourceBundle.getString("menu.title.folding"));
        folding.addItem(new XJMenuItem(resourceBundle.getString("menu.item.toggleRule"), KeyEvent.VK_PERIOD, MI_EXPAND_COLLAPSE_RULE, this));
        folding.addItem(new XJMenuItem(resourceBundle.getString("menu.item.expandAllRules"), KeyEvent.VK_PLUS, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_EXPAND_ALL_RULES, this));
        folding.addItem(new XJMenuItem(resourceBundle.getString("menu.item.collapseAllRules"), KeyEvent.VK_MINUS, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_COLLAPSE_ALL_RULES, this));
        folding.addSeparator();
        folding.addItem(new XJMenuItem(resourceBundle.getString("menu.item.toggleAction"), KeyEvent.VK_MINUS, MI_EXPAND_COLLAPSE_ACTION, this));
        folding.addItem(new XJMenuItem(resourceBundle.getString("menu.item.expandAllActions"), KeyEvent.VK_PLUS, XJMenuItem.getKeyModifier() | Event.ALT_MASK, MI_EXPAND_ALL_ACTIONS, this));
        folding.addItem(new XJMenuItem(resourceBundle.getString("menu.item.collapseAllActions"), KeyEvent.VK_MINUS, XJMenuItem.getKeyModifier() | Event.ALT_MASK, MI_COLLAPSE_ALL_ACTIONS, this));

        menu.addSeparator();
        menu.addItem(rules);
        menu.addItem(folding);
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.checkGrammar"), KeyEvent.VK_R, MI_CHECK_GRAMMAR, this));

        menubar.addCustomMenu(menu);
    }

    private void createFindMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle(resourceBundle.getString("menu.title.find"));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.find"), KeyEvent.VK_F, MI_FIND, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.findNext"), KeyEvent.VK_F3, 0, MI_FIND_NEXT, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.findPrevious"), KeyEvent.VK_F3, Event.SHIFT_MASK, MI_FIND_PREV, this));
        menu.addItem(createMenuItem(MI_FIND_TOKEN));
        menu.addSeparator();
        menu.addItem(createMenuItem(MI_FIND_USAGE));

        menubar.addCustomMenu(menu);
    }

    public XJMenuItem createMenuItem(int tag) {
        return createMenuItem(tag, false);
    }

    public XJMenuItem createMenuItem(int tag, boolean contextual) {
        XJMenuItem item = null;
        switch(tag) {
            case MI_FIND_TOKEN:
                item = new XJMenuItem(resourceBundle.getString("menu.item.findTextAtCaret"), KeyEvent.VK_F3, MI_FIND_TOKEN, this);
                break;
            case MI_FIND_USAGE:
                item = new XJMenuItem(resourceBundle.getString("menu.item.findUsages"), KeyEvent.VK_F7, Event.ALT_MASK, MI_FIND_USAGE, this);
                break;

            case MI_SHOW_DECISION_DFA:
                item = new XJMenuItem(resourceBundle.getString("menu.item.showDecisionDFA"), MI_SHOW_DECISION_DFA, this);
                break;

            case MI_SHOW_DEPENDENCY:
                item = new XJMenuItem(resourceBundle.getString("menu.item.showRuleDependencyGraph"), MI_SHOW_DEPENDENCY, this);
                break;

            case MI_GOTO_RULE:
                item = new XJMenuItem(contextual? resourceBundle.getString("contextual.item.gotoRule") : resourceBundle.getString("menu.item.gotoRule"), KeyEvent.VK_B, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_GOTO_RULE, this);
                break;

            case MI_GOTO_DECLARATION:
                item = new XJMenuItem(contextual? resourceBundle.getString("contextual.item.goToDeclaration") : resourceBundle.getString("menu.item.gotoDeclaration"), KeyEvent.VK_B, MI_GOTO_DECLARATION, this);
                break;

            case MI_RENAME:
                item = new XJMenuItem(resourceBundle.getString("menu.item.rename"), KeyEvent.VK_F6, Event.SHIFT_MASK, MI_RENAME, this);
                break;

            case MI_REPLACE_LITERAL_WITH_TOKEN_LABEL:
                item = new XJMenuItem(resourceBundle.getString("menu.item.replaceLiteralsWithTokenLabel"), MI_REPLACE_LITERAL_WITH_TOKEN_LABEL, this);
                break;

            case MI_EXTRACT_RULE:
                item = new XJMenuItem(resourceBundle.getString("menu.item.extractRule"), MI_EXTRACT_RULE, this);
                break;

            case MI_INLINE_RULE:
                item = new XJMenuItem(resourceBundle.getString("menu.item.inlineRule"), MI_INLINE_RULE, this);
                break;

            case MI_SHOW_RULE_GENCODE:
                item = new XJMenuItem(resourceBundle.getString("menu.item.showRuleCode"), MI_SHOW_RULE_GENCODE, this);
                break;

            case MI_GROUP_RULE:
                item = new XJMenuItem(resourceBundle.getString("menu.item.group"), MI_GROUP_RULE, this);
                break;

            case MI_UNGROUP_RULE:
                item = new XJMenuItem(resourceBundle.getString("menu.item.ungroup"), MI_UNGROUP_RULE, this);
                break;

            case MI_IGNORE_RULE:
                item = new XJMenuItemCheck(resourceBundle.getString("menu.item.ignoreInInterpreter"), MI_IGNORE_RULE, this, true);
                break;

            case MI_EXPORT_AS_IMAGE:
                item = new XJMenuItem(contextual? resourceBundle.getString("contextual.item.exportAsBitmapImage") : resourceBundle.getString("menu.item.exportAsBitmap"), MI_EXPORT_AS_IMAGE, this);
                break;

            case MI_EXPORT_AS_EPS:
                item = new XJMenuItem(contextual? resourceBundle.getString("contextual.item.exportAsEPS") : resourceBundle.getString("menu.item.exportAsEPS"), MI_EXPORT_AS_EPS, this);
                break;

            case MI_EXPORT_AS_DOT:
                item = new XJMenuItem(contextual? resourceBundle.getString("contextual.item.exportAsDot") : resourceBundle.getString("menu.item.exportAsDot"), MI_EXPORT_AS_DOT, this);
                break;
        }
        return item;
    }

    public JPopupMenu getContextualMenu() {
        boolean overReference = editor.getCurrentReference() != null;
        boolean overToken = editor.getCurrentToken() != null;
        boolean overRule = editor.getCurrentRule() != null;
        boolean overSelection = editor.getTextPane().getSelectionStart() != editor.getTextPane().getSelectionEnd();

        ContextualMenuFactory factory = new ContextualMenuFactory(this);
        factory.addItem(MI_GOTO_RULE);
        if(overReference)
            factory.addItem(MI_GOTO_DECLARATION);

        factory.addSeparator();
        if(overToken)
            factory.addItem(MI_RENAME);
        if(editor.menuRefactor.canReplaceLiteralWithTokenLabel())
            factory.addItem(MI_REPLACE_LITERAL_WITH_TOKEN_LABEL);
        if(editor.menuRefactor.canExtractRule())
            factory.addItem(MI_EXTRACT_RULE);
        if(editor.menuRefactor.canInlineRule())
            factory.addItem(MI_INLINE_RULE);

        if(overToken) {
            factory.addSeparator();
            if(overSelection)
                factory.addItem(MI_FIND_TOKEN);
            factory.addItem(MI_FIND_USAGE);
        }

        if(overRule) {
            factory.addSeparator();
            factory.addItem(MI_SHOW_DECISION_DFA);
            factory.addItem(MI_SHOW_DEPENDENCY);
            factory.addItem(MI_SHOW_RULE_GENCODE);
        }

        return factory.menu;
    }

    public void menuItemState(XJMenuItem item) {
        EditorTab tab = editor.getSelectedTab();

        switch(item.getTag()) {
            case XJMainMenuBar.MI_UNDO:
            case XJMainMenuBar.MI_REDO:
                if(isDebuggerRunning())
                    item.setEnabled(false);
                break;

            case XJMainMenuBar.MI_CUT:
            case XJMainMenuBar.MI_PASTE:

            case MI_RENAME:
            case MI_REPLACE_LITERAL_WITH_TOKEN_LABEL:
            case MI_LITERAL_TO_SINGLEQUOTE:
            case MI_LITERAL_TO_DOUBLEQUOTE:
            case MI_LITERAL_TO_CSTYLEQUOTE:
            case MI_REMOVE_LEFT_RECURSION:
            case MI_REMOVE_ALL_LEFT_RECURSION:
            case MI_EXTRACT_RULE:
            case MI_INLINE_RULE:
            case MI_INSERT_TEMPLATE:
            case MI_GROUP_RULE:
            case MI_UNGROUP_RULE:
            case MI_EXPAND_COLLAPSE_RULE:
            case MI_EXPAND_ALL_RULES:
            case MI_COLLAPSE_ALL_RULES:
            case MI_EXPAND_COLLAPSE_ACTION:
            case MI_EXPAND_ALL_ACTIONS:
            case MI_COLLAPSE_ALL_ACTIONS:
            case MI_CHECK_GRAMMAR:
            case MI_FIND:
            case MI_RUN_INTERPRETER:
            case MI_DEBUG:
            case MI_BUILD_AND_DEBUG:
            case MI_DEBUG_REMOTE:
                item.setEnabled(!isDebuggerRunning());
                break;

            case MI_GOTO_BACK:
                item.setEnabled(editor.goToHistory.canGoBack());
                break;
            case MI_GOTO_FORWARD:
                item.setEnabled(editor.goToHistory.canGoForward());
                break;

            case MI_P4_EDIT:
            case MI_P4_ADD:
            case MI_P4_DELETE:
            case MI_P4_REVERT:
            case MI_P4_SUBMIT:
            case MI_P4_SYNC:
                if(isDebuggerRunning())
                    item.setEnabled(false);
                else
                    item.setEnabled(AWPrefs.getP4Enabled());
                break;

            case MI_EXPORT_AS_IMAGE:
                item.setEnabled(tab.canExportToBitmap());
                break;

            case MI_EXPORT_AS_EPS:
                item.setEnabled(tab.canExportToEPS());
                break;

            case MI_EXPORT_AS_DOT:
                item.setEnabled(tab.canExportToDOT());
                break;

            case MI_DEBUG_SHOW_INFO_PANEL:
                item.setTitle(editor.menuDebugger.isInfoPanelVisible()?
                        resourceBundle.getString("menu.item.hideInformation") : resourceBundle.getString("menu.item.showInformation"));
                break;

            case MI_DEBUG_SHOW_OUTPUT_PANEL:
                item.setTitle(editor.menuDebugger.isOutputPanelVisible()?
                        resourceBundle.getString("menu.item.hideOutput") : resourceBundle.getString("menu.item.showOutput"));
                break;

            case MI_DEBUG_SHOW_INPUT_TOKENS:
                item.setTitle(editor.menuDebugger.isInputTokenVisible()?
                        resourceBundle.getString("menu.item.hideInputTokens") : resourceBundle.getString("menu.item.showInputTokens"));
                break;
        }
    }

    public void handleMenuSelected(XJMenu menu) {
        boolean ignored = editor.rules.getFirstSelectedRuleIgnoredFlag();
        ignoreRuleMenuItem.setSelected(ignored);
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        handleMenuView(item.getTag());
        handleMenuFind(item.getTag());
        handleMenuGrammar(item);
        handleMenuRefactor(item.getTag());
        handleMenuGoTo(item.getTag());
        handleMenuGenerate(item.getTag());
        handleMenuRun(item.getTag());
        handleMenuSCM(item.getTag());
        handleMenuPrivate(item.getTag());
        handleMenuExport(item.getTag());
    }

    public void handleMenuView(int itemTag) {
        switch(itemTag) {
            case MI_EXPAND_COLLAPSE_RULE:
                editor.menuFolding.expandCollapseRule();
                break;

            case MI_EXPAND_ALL_RULES:
                editor.menuFolding.expandAllRules();
                break;

            case MI_COLLAPSE_ALL_RULES:
                editor.menuFolding.collapseAllRules();
                break;

            case MI_EXPAND_COLLAPSE_ACTION:
                editor.menuFolding.expandCollapseAction();
                break;

            case MI_EXPAND_ALL_ACTIONS:
                editor.menuFolding.expandAllActions();
                break;

            case MI_COLLAPSE_ALL_ACTIONS:
                editor.menuFolding.collapseAllActions();
                break;
        }
    }

    public void handleMenuFind(int itemTag) {
        switch(itemTag) {
            case MI_FIND:
                editor.menuFind.find();
                break;

            case MI_FIND_NEXT:
                editor.menuFind.findNext();
                break;

            case MI_FIND_PREV:
                editor.menuFind.findPrev();
                break;

            case MI_FIND_TOKEN:
                editor.menuFind.findSelection();
                break;

            case MI_FIND_USAGE:
                editor.menuFind.findUsage();
                break;
        }
    }

    public void handleMenuGrammar(XJMenuItem item) {
        switch(item.getTag()) {
            case MI_SHOW_TOKENS_SD:
                editor.menuGrammar.showTokensSD();
                break;

            case MI_SHOW_TOKENS_DFA:
                editor.menuGrammar.showTokensDFA();
                break;

            case MI_SHOW_DECISION_DFA:
                editor.menuGrammar.showDecisionDFA();
                break;

            case MI_SHOW_DEPENDENCY:
                editor.menuGrammar.showDependency();
                break;

            case MI_INSERT_TEMPLATE:
                editor.menuGrammar.insertRuleFromTemplate();
                break;

            case MI_GROUP_RULE:
                editor.menuGrammar.group();
                break;

            case MI_UNGROUP_RULE:
                editor.menuGrammar.ungroup();
                break;

            case MI_IGNORE_RULE:
                if(item.isSelected())
                    editor.menuGrammar.ignore();
                else
                    editor.menuGrammar.consider();
                break;

            case MI_CHECK_GRAMMAR:
                editor.menuGrammar.checkGrammar();
                break;
        }
    }

    public void handleMenuRefactor(int itemTag) {
        switch(itemTag) {
            case MI_RENAME:
                editor.menuRefactor.rename();
                break;

            case MI_REPLACE_LITERAL_WITH_TOKEN_LABEL:
                editor.menuRefactor.replaceLiteralWithTokenLabel();
                break;

            case MI_LITERAL_TO_SINGLEQUOTE:
                editor.menuRefactor.convertLiteralsToSingleQuote();
                break;

            case MI_LITERAL_TO_DOUBLEQUOTE:
                editor.menuRefactor.convertLiteralsToDoubleQuote();
                break;

            case MI_LITERAL_TO_CSTYLEQUOTE:
                editor.menuRefactor.convertLiteralsToCStyleQuote();
                break;

            case MI_REMOVE_LEFT_RECURSION:
                editor.menuRefactor.removeLeftRecursion();
                break;

            case MI_REMOVE_ALL_LEFT_RECURSION:
                editor.menuRefactor.removeAllLeftRecursion();
                break;

            case MI_EXTRACT_RULE:
                editor.menuRefactor.extractRule();
                break;

            case MI_INLINE_RULE:
                editor.menuRefactor.inlineRule();
                break;
        }
    }

    public void handleMenuGoTo(int itemTag) {
        switch(itemTag) {
            case MI_GOTO_RULE:
                editor.menuGoTo.goToRule();
                break;

            case MI_GOTO_DECLARATION:
                editor.menuGoTo.goToDeclaration();
                break;

            case MI_GOTO_LINE:
                editor.menuGoTo.goToLine();
                break;

            case MI_GOTO_CHARACTER:
                editor.menuGoTo.goToCharacter();
                break;

            case MI_GOTO_BACK:
                editor.menuGoTo.goToBackward();
                break;

            case MI_GOTO_FORWARD:
                editor.menuGoTo.goToForward();
                break;

            case MI_PREV_BREAKPOINT:
                editor.menuGoTo.goToBreakpoint(-1);
                break;

            case MI_NEXT_BREAKPOINT:
                editor.menuGoTo.goToBreakpoint(1);
                break;
        }
    }

    public void handleMenuGenerate(int itemTag) {
        switch(itemTag) {
            case MI_GENERATE_CODE:
                editor.menuGenerate.generateCode();
                break;

            case MI_SHOW_GENERATED_LEXER_CODE:
                editor.menuGenerate.showGeneratedCode(true);
                break;

            case MI_SHOW_GENERATED_PARSER_CODE:
                editor.menuGenerate.showGeneratedCode(false);
                break;

            case MI_SHOW_RULE_GENCODE:
                editor.menuGenerate.showRuleGeneratedCode();
                break;
        }
    }

    public void handleMenuRun(int itemTag) {
        switch(itemTag) {
            case MI_RUN_INTERPRETER:
                editor.menuDebugger.runInterpreter();
                break;
            case MI_DEBUG:
                editor.menuDebugger.debug();
                break;

            case MI_BUILD_AND_DEBUG:
                editor.menuDebugger.buildAndDebug();
                break;

            case MI_DEBUG_REMOTE:
                editor.menuDebugger.debugRemote();
                break;

            case MI_DEBUG_SHOW_INFO_PANEL:
                editor.menuDebugger.toggleInformationPanel();
                editor.refreshMainMenuBar();
                break;

            case MI_DEBUG_SHOW_OUTPUT_PANEL:
                editor.menuDebugger.toggleOutputPanel();
                editor.refreshMainMenuBar();
                break;

            case MI_DEBUG_SHOW_INPUT_TOKENS:
                editor.menuDebugger.toggleInputTokens();
                editor.refreshMainMenuBar();
                break;
        }
    }

    public void handleMenuSCM(int itemTag) {
        switch(itemTag) {
            case MI_P4_EDIT:
                editor.menuSCM.editFile();
                break;
            case MI_P4_ADD:
                editor.menuSCM.addFile();
                break;
            case MI_P4_DELETE:
                editor.menuSCM.deleteFile();
                break;
            case MI_P4_REVERT:
                editor.menuSCM.revertFile();
                break;
            case MI_P4_SUBMIT:
                editor.menuSCM.submitFile();
                break;
            case MI_P4_SYNC:
                editor.menuSCM.sync();
                break;
        }
    }

    public void handleMenuPrivate(int itemTag) {
        switch(itemTag) {
            case MI_PRIVATE_UNREGISTER:
                AWPrefs.removeUserRegistration();
                break;
        }
    }

    public void handleMenuExport(int itemTag) {
        switch(itemTag) {
            case MI_EXPORT_AS_IMAGE:
                editor.menuExport.exportAsImage();
                break;

            case MI_EXPORT_AS_EPS:
                editor.menuExport.exportAsEPS();
                break;

            case MI_EXPORT_AS_DOT:
                editor.menuExport.exportAsDOT();
                break;

            case MI_EXPORT_EVENT:
                editor.menuExport.exportEventsAsTextFile();
                break;
        }
    }

}
