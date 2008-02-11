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

import org.antlr.works.IDE;
import org.antlr.works.components.container.ComponentContainerGrammar;
import org.antlr.works.components.editor.ComponentEditorGrammar;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.syntax.element.ElementGrammarName;
import org.antlr.xjlib.appkit.menu.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

public class EditorMenu implements XJMenuItemDelegate {

    public static final int MI_PRINT = 5;

    // Edit
    public static final int MI_TOGGLE_SYNTAX_COLORING = 6;
    public static final int MI_TOGGLE_SYNTAX_DIAGRAM = 7;
    public static final int MI_TOGGLE_NFA_OPTIMIZATION = 9;
    public static final int MI_TOGGLE_AUTOINDENT = 10;

    // View
    public static final int MI_EXPAND_COLLAPSE_RULE = 20;
    public static final int MI_EXPAND_COLLAPSE_ACTION = 21;

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
    public static final int MI_HIGHLIGHT_DECISION_DFA = 50;
    public static final int MI_SHOW_DECISION_DFA = 51;
    public static final int MI_SHOW_DEPENDENCY = 52;
    public static final int MI_SHOW_TOKENS_SD = 53;
    public static final int MI_SHOW_TOKENS_DFA = 54;
    public static final int MI_INSERT_TEMPLATE = 55;
    public static final int MI_GROUP_RULE = 56;
    public static final int MI_UNGROUP_RULE = 57;
    public static final int MI_IGNORE_RULE = 58;
    public static final int MI_CHECK_GRAMMAR = 59;

    // Refactor
    public static final int MI_RENAME = 61;
    public static final int MI_REPLACE_LITERAL_WITH_TOKEN_LABEL = 62;
    public static final int MI_LITERAL_TO_SINGLEQUOTE = 63;
    public static final int MI_LITERAL_TO_DOUBLEQUOTE = 64;
    public static final int MI_LITERAL_TO_CSTYLEQUOTE = 65;
    public static final int MI_REMOVE_LEFT_RECURSION = 66;
    public static final int MI_REMOVE_ALL_LEFT_RECURSION = 67;
    public static final int MI_EXTRACT_RULE = 68;
    public static final int MI_INLINE_RULE = 69;

    // Generate
    public static final int MI_GENERATE_CODE = 70;
    public static final int MI_SHOW_GENERATED_PARSER_CODE = 71;
    public static final int MI_SHOW_GENERATED_LEXER_CODE = 72;
    public static final int MI_SHOW_RULE_GENCODE = 73;

    // Debugger
    public static final int MI_RUN_INTERPRETER = 80;
    public static final int MI_DEBUG = 81;
    public static final int MI_DEBUG_AGAIN = 82;
    public static final int MI_DEBUG_REMOTE = 83;
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
    public static final int MI_EXPORT_ALL_AS_IMAGE = 112;
    public static final int MI_EXPORT_ALL_AS_EPS = 113;
    public static final int MI_EXPORT_AS_DOT = 114;
    public static final int MI_EXPORT_EVENT = 115;

    public static final int MI_PRIVATE_UNREGISTER = 200;
    public static final int MI_SERIALIZE_SD = 201;

    protected ComponentContainerGrammar container;
    protected XJMenuItem ignoreRuleMenuItem;

    public XJMenu menuGrammar;

    /** The resource bundle used to get localized strings */
    protected static ResourceBundle resourceBundle = IDE.getMenusResourceBundle();

    public EditorMenu(ComponentContainerGrammar container) {
        this.container = container;
    }

    public void close() {
        container = null;
    }

    public ComponentEditorGrammar getEditor() {
        return container.getSelectedEditor();
    }

    public boolean isDebuggerRunning() {
        return getEditor().debugger.isRunning();
    }

    public void customizeFileMenu(XJMenu menu) {
        menu.insertItemAfter(new XJMenuItem(resourceBundle.getString("menu.item.print"), KeyEvent.VK_P, MI_PRINT, this), XJMainMenuBar.MI_SAVEAS);
        menu.insertSeparatorAfter(XJMainMenuBar.MI_SAVEAS);

        XJMenu exportMenu = new XJMenu();
        exportMenu.setTitle(resourceBundle.getString("menu.title.exportEvents"));
        exportMenu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.asText"), MI_EXPORT_EVENT, this));

        menu.insertItemAfter(exportMenu, XJMainMenuBar.MI_SAVEAS);

        exportMenu = new XJMenu();
        exportMenu.setTitle(resourceBundle.getString("menu.title.exportAllRules"));
        exportMenu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.exportAsEPS"), MI_EXPORT_ALL_AS_EPS, this));
        exportMenu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.exportAsBitmap"), MI_EXPORT_ALL_AS_IMAGE, this));

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
        if(!IDE.isPlugin())
            createSCMMenu(menubar);
        createPrivateMenu(menubar);
    }

    private void createPrivateMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        if(AWPrefs.getPrivateMenu()) {
            menu = new XJMenu();
            menu.setTitle("*");
            menu.addItem(new XJMenuItem("Unregister user", MI_PRIVATE_UNREGISTER, this));
            menu.addItem(new XJMenuItem("Serialize Syntax Diagrams...", MI_SERIALIZE_SD, this));

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
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.debug"), KeyEvent.VK_D, MI_DEBUG, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.debugAgain"), KeyEvent.VK_D, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_DEBUG_AGAIN, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.debugRemote"), MI_DEBUG_REMOTE, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showInputTokens"), MI_DEBUG_SHOW_INPUT_TOKENS, this));

        menubar.addCustomMenu(menu);
    }

    private void createGenerateMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle(resourceBundle.getString("menu.title.generate"));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.generateCode"), KeyEvent.VK_G, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_GENERATE_CODE, this));
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
        menuGrammar = new XJMenu();
        menuGrammar.setTitle(resourceBundle.getString("menu.title.grammar"));
        menuGrammar.addItem(new XJMenuItemCheck(resourceBundle.getString("menu.item.highlightDecisionDFA"), MI_HIGHLIGHT_DECISION_DFA, this, false));
        menuGrammar.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showRuleDependencyGraph"), MI_SHOW_DEPENDENCY, this));
        menuGrammar.addSeparator();
        menuGrammar.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showTokensSyntaxDiagram"), MI_SHOW_TOKENS_SD, this));
        menuGrammar.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showTokensDFA"), MI_SHOW_TOKENS_DFA, this));
        menuGrammar.addSeparator();
        menuGrammar.addItem(new XJMenuItem(resourceBundle.getString("menu.item.insertRuleFromTemplate"), KeyEvent.VK_T, MI_INSERT_TEMPLATE, this));

        XJMenu rules = new XJMenu();
        rules.setTitle(resourceBundle.getString("menu.title.rules"));
        rules.addItem(createMenuItem(MI_GROUP_RULE));
        rules.addItem(createMenuItem(MI_UNGROUP_RULE));
        rules.addSeparator();
        rules.addItem(ignoreRuleMenuItem = createMenuItem(MI_IGNORE_RULE));

        /* Currently not supported
        XJMenu folding = new XJMenu();
        folding.setTitle(resourceBundle.getString("menu.title.folding"));
        folding.addItem(new XJMenuItem(resourceBundle.getString("menu.item.toggleRule"), KeyEvent.VK_PERIOD, MI_EXPAND_COLLAPSE_RULE, this));
        folding.addItem(new XJMenuItem(resourceBundle.getString("menu.item.toggleAction"), KeyEvent.VK_MINUS, MI_EXPAND_COLLAPSE_ACTION, this));

        menu.addSeparator();*/

        menuGrammar.addItem(rules);
        //menu.addItem(folding);
        menuGrammar.addSeparator();
        menuGrammar.addItem(new XJMenuItem(resourceBundle.getString("menu.item.checkGrammar"), KeyEvent.VK_R, MI_CHECK_GRAMMAR, this));

        menubar.addCustomMenu(menuGrammar);
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

    public JPopupMenu getContextualMenu(int textIndex) {
        boolean overReference = getEditor().getCurrentReference() != null;
        boolean overToken = getEditor().getCurrentToken() != null;
        boolean overRule = getEditor().getCurrentRule() != null;
        boolean overSelection = getEditor().getTextPane().getSelectionStart() != getEditor().getTextPane().getSelectionEnd();
        boolean overDecisionDFA = getEditor().decisionDFAEngine.isDecisionPointAroundLocation(getEditor().getTextEditor().getLineIndexAtTextPosition(textIndex),
                getEditor().getTextEditor().getColumnPositionAtIndex(textIndex));

        ContextualMenuFactory factory = new ContextualMenuFactory(this);
        factory.addItem(MI_GOTO_RULE);
        if(overReference)
            factory.addItem(MI_GOTO_DECLARATION);

        factory.addSeparator();
        if(overToken)
            factory.addItem(MI_RENAME);
        if(container.getMenuRefactor().canReplaceLiteralWithTokenLabel())
            factory.addItem(MI_REPLACE_LITERAL_WITH_TOKEN_LABEL);
        if(container.getMenuRefactor().canExtractRule())
            factory.addItem(MI_EXTRACT_RULE);
        if(container.getMenuRefactor().canInlineRule())
            factory.addItem(MI_INLINE_RULE);

        if(overToken) {
            factory.addSeparator();
            if(overSelection)
                factory.addItem(MI_FIND_TOKEN);
            factory.addItem(MI_FIND_USAGE);
        }

        if(overRule) {
            factory.addSeparator();
            if(overDecisionDFA)
                factory.addItem(MI_SHOW_DECISION_DFA);
            factory.addItem(MI_SHOW_DEPENDENCY);
            factory.addItem(MI_SHOW_RULE_GENCODE);
        }

        return factory.menu;
    }

    public void menuItemState(final XJMenuItem item) {
        EditorTab tab = getEditor().getSelectedTab();

        switch(item.getTag()) {
            case XJMainMenuBar.MI_UNDO:
            case XJMainMenuBar.MI_REDO:
                if(isDebuggerRunning()) {
                    // FIX AW-79
                    // Note: that's weird, but I have to invoke this later otherwise the menu is not disabled
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            item.setEnabled(false);
                        }
                    });
                }
                break;

            case XJMainMenuBar.MI_CUT:
            case XJMainMenuBar.MI_PASTE:
                item.setEnabled(getEditor().isFileWritable());
                break;

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
            case MI_EXPAND_COLLAPSE_ACTION:
            case MI_CHECK_GRAMMAR:
            case MI_FIND:
            case MI_RUN_INTERPRETER:
                item.setEnabled(!isDebuggerRunning());
                break;

            case MI_DEBUG_AGAIN:
                item.setEnabled(!isDebuggerRunning() && getEditor().debugger.canDebugAgain());
                break;

            case MI_DEBUG:
            case MI_DEBUG_REMOTE:
                item.setEnabled(!isDebuggerRunning());
                break;

            case MI_GOTO_BACK:
                item.setEnabled(getEditor().goToHistory.canGoBack());
                break;
            case MI_GOTO_FORWARD:
                item.setEnabled(getEditor().goToHistory.canGoForward());
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

            case MI_DEBUG_SHOW_INPUT_TOKENS:
                item.setTitle(container.getMenuDebugger().isInputTokenVisible()?
                        resourceBundle.getString("menu.item.hideInputTokens") : resourceBundle.getString("menu.item.showInputTokens"));
                break;

            case MI_HIGHLIGHT_DECISION_DFA:
                if(getEditor().decisionDFAEngine.getDecisionDFACount() == 0) {
                    item.setSelected(false);
                } else {
                    item.setSelected(true);
                }
                break;
        }
    }

    public void handleMenuSelected(XJMenu menu) {
        boolean ignored = getEditor().rules.getFirstSelectedRuleIgnoredFlag();
        ignoreRuleMenuItem.setSelected(ignored);
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        handleMenuFile(item.getTag());
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

    public void handleMenuFile(int itemTag) {
        switch(itemTag) {
            case MI_PRINT:
                getEditor().print();
                break;
        }
    }

    public void handleMenuFind(int itemTag) {
        switch(itemTag) {
            case MI_FIND:
                container.getMenuFind().find();
                break;

            case MI_FIND_NEXT:
                container.getMenuFind().findNext();
                break;

            case MI_FIND_PREV:
                container.getMenuFind().findPrev();
                break;

            case MI_FIND_TOKEN:
                container.getMenuFind().findSelection();
                break;

            case MI_FIND_USAGE:
                container.getMenuFind().findUsage();
                break;
        }
    }

    public void handleMenuGrammar(XJMenuItem item) {
        switch(item.getTag()) {
            case MI_SHOW_TOKENS_SD:
                container.getMenuGrammar().showTokensSD();
                break;

            case MI_SHOW_TOKENS_DFA:
                container.getMenuGrammar().showTokensDFA();
                break;

            case MI_SHOW_DECISION_DFA:
                container.getMenuGrammar().showDecisionDFA();
                break;

            case MI_HIGHLIGHT_DECISION_DFA:
                container.getMenuGrammar().highlightDecisionDFA();
                break;

            case MI_SHOW_DEPENDENCY:
                container.getMenuGrammar().showDependency();
                break;

            case MI_INSERT_TEMPLATE:
                container.getMenuGrammar().insertRuleFromTemplate();
                break;

            case MI_GROUP_RULE:
                container.getMenuGrammar().group();
                break;

            case MI_UNGROUP_RULE:
                container.getMenuGrammar().ungroup();
                break;

            case MI_IGNORE_RULE:
                if(item.isSelected())
                    container.getMenuGrammar().ignore();
                else
                    container.getMenuGrammar().consider();
                break;

            case MI_CHECK_GRAMMAR:
                container.getMenuGrammar().checkGrammar();
                break;
        }
    }

    public void handleMenuRefactor(int itemTag) {
        switch(itemTag) {
            case MI_RENAME:
                container.getMenuRefactor().rename();
                break;

            case MI_REPLACE_LITERAL_WITH_TOKEN_LABEL:
                container.getMenuRefactor().replaceLiteralWithTokenLabel();
                break;

            case MI_LITERAL_TO_SINGLEQUOTE:
                container.getMenuRefactor().convertLiteralsToSingleQuote();
                break;

            case MI_LITERAL_TO_DOUBLEQUOTE:
                container.getMenuRefactor().convertLiteralsToDoubleQuote();
                break;

            case MI_LITERAL_TO_CSTYLEQUOTE:
                container.getMenuRefactor().convertLiteralsToCStyleQuote();
                break;

            case MI_REMOVE_LEFT_RECURSION:
                container.getMenuRefactor().removeLeftRecursion();
                break;

            case MI_REMOVE_ALL_LEFT_RECURSION:
                container.getMenuRefactor().removeAllLeftRecursion();
                break;

            case MI_EXTRACT_RULE:
                container.getMenuRefactor().extractRule();
                break;

            case MI_INLINE_RULE:
                container.getMenuRefactor().inlineRule();
                break;
        }
    }

    public void handleMenuGoTo(int itemTag) {
        switch(itemTag) {
            case MI_GOTO_RULE:
                container.getMenuGoTo().goToRule();
                break;

            case MI_GOTO_DECLARATION:
                container.getMenuGoTo().goToDeclaration();
                break;

            case MI_GOTO_LINE:
                container.getMenuGoTo().goToLine();
                break;

            case MI_GOTO_CHARACTER:
                container.getMenuGoTo().goToCharacter();
                break;

            case MI_GOTO_BACK:
                container.getMenuGoTo().goToBackward();
                break;

            case MI_GOTO_FORWARD:
                container.getMenuGoTo().goToForward();
                break;

            case MI_PREV_BREAKPOINT:
                container.getMenuGoTo().goToBreakpoint(-1);
                break;

            case MI_NEXT_BREAKPOINT:
                container.getMenuGoTo().goToBreakpoint(1);
                break;
        }
    }

    public void handleMenuGenerate(int itemTag) {
        switch(itemTag) {
            case MI_GENERATE_CODE:
                container.getMenuGenerate().generateCode();
                break;

            case MI_SHOW_GENERATED_LEXER_CODE:
                container.getMenuGenerate().showGeneratedCode(ElementGrammarName.LEXER);
                break;

            case MI_SHOW_GENERATED_PARSER_CODE:
                container.getMenuGenerate().showGeneratedCode(ElementGrammarName.PARSER);
                break;

            case MI_SHOW_RULE_GENCODE:
                container.getMenuGenerate().showRuleGeneratedCode();
                break;
        }
    }

    public void handleMenuRun(int itemTag) {
        switch(itemTag) {
            case MI_RUN_INTERPRETER:
                container.getMenuDebugger().runInterpreter();
                break;

            case MI_DEBUG:
                container.getMenuDebugger().debug();
                break;

            case MI_DEBUG_AGAIN:
                container.getMenuDebugger().debugAgain();
                break;

            case MI_DEBUG_REMOTE:
                container.getMenuDebugger().debugRemote();
                break;

            case MI_DEBUG_SHOW_INPUT_TOKENS:
                container.getMenuDebugger().toggleInputTokens();
                getEditor().refreshMainMenuBar();
                break;
        }
    }

    public void handleMenuSCM(int itemTag) {
        switch(itemTag) {
            case MI_P4_EDIT:
                container.getMenuSCM().editFile();
                break;
            case MI_P4_ADD:
                container.getMenuSCM().addFile();
                break;
            case MI_P4_DELETE:
                container.getMenuSCM().deleteFile();
                break;
            case MI_P4_REVERT:
                container.getMenuSCM().revertFile();
                break;
            case MI_P4_SUBMIT:
                container.getMenuSCM().submitFile();
                break;
            case MI_P4_SYNC:
                container.getMenuSCM().sync();
                break;
        }
    }

    public void handleMenuPrivate(int itemTag) {
        switch(itemTag) {
            case MI_PRIVATE_UNREGISTER:
                AWPrefs.removeUserRegistration();
                break;
            case MI_SERIALIZE_SD:
                getEditor().visual.serializeSyntaxDiagram();
                break;
        }
    }

    public void handleMenuExport(int itemTag) {
        switch(itemTag) {
            case MI_EXPORT_AS_IMAGE:
                container.getMenuExport().exportAsImage();
                break;

            case MI_EXPORT_AS_EPS:
                container.getMenuExport().exportAsEPS();
                break;

            case MI_EXPORT_AS_DOT:
                container.getMenuExport().exportAsDOT();
                break;

            case MI_EXPORT_ALL_AS_IMAGE:
                container.getMenuExport().exportAllRulesAsImage();
                break;

            case MI_EXPORT_ALL_AS_EPS:
                container.getMenuExport().exportAllRulesAsEPS();
                break;

            case MI_EXPORT_EVENT:
                container.getMenuExport().exportEventsAsTextFile();
                break;
        }
    }

}
