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

import edu.usfca.xj.appkit.menu.XJMainMenuBar;
import edu.usfca.xj.appkit.menu.XJMenu;
import edu.usfca.xj.appkit.menu.XJMenuItem;
import edu.usfca.xj.appkit.menu.XJMenuItemDelegate;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.dialog.DialogStatistics;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.prefs.AWPrefs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

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
    public static final int MI_SHOW_DECISION_DFA = 51;
    public static final int MI_SHOW_DEPENDENCY = 52;
    public static final int MI_INSERT_TEMPLATE = 53;
    public static final int MI_GROUP_RULE = 54;
    public static final int MI_UNGROUP_RULE = 55;
    public static final int MI_IGNORE_RULE = 56;
    public static final int MI_CONSIDER_RULE = 57;
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

    // Run
    public static final int MI_RUN_INTERPRETER = 80;
    public static final int MI_DEBUG = 81;
    public static final int MI_BUILD_AND_DEBUG = 82;
    public static final int MI_DEBUG_REMOTE = 83;

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
    public static final int MI_EXPORT_EVENT = 115;

    public static final int MI_PRIVATE_STATS = 200;
    public static final int MI_PRIVATE_UNREGISTER = 201;

    protected CEditorGrammar editor = null;

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
        exportMenu.setTitle("Export Events");
        exportMenu.addItem(new XJMenuItem("As Text...", MI_EXPORT_EVENT, this));

        menu.insertItemAfter(exportMenu, XJMainMenuBar.MI_SAVEAS);

        exportMenu = new XJMenu();
        exportMenu.setTitle("Export");
        exportMenu.addItem(new XJMenuItem("As Bitmap Image...", MI_EXPORT_AS_IMAGE, this));
        exportMenu.addItem(new XJMenuItem("As EPS...", MI_EXPORT_AS_EPS, this));

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
            menu.addItem(new XJMenuItem("Statistics...", MI_PRIVATE_STATS, this));
            menu.addItem(new XJMenuItem("Unregister user", MI_PRIVATE_UNREGISTER, this));

            menubar.addCustomMenu(menu);
        }
    }

    private void createSCMMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("SCM");
        menu.addItem(new XJMenuItem("Open for Edit", MI_P4_EDIT, this));
        menu.addItem(new XJMenuItem("Mark for Add", MI_P4_ADD, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Mark for Delete", MI_P4_DELETE, this));
        menu.addItem(new XJMenuItem("Revert", MI_P4_REVERT, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Submit...", MI_P4_SUBMIT, this));
        menu.addItem(new XJMenuItem("Sync", MI_P4_SYNC, this));

        menubar.addCustomMenu(menu);
    }

    private void createRunMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("Run");
        menu.addItem(new XJMenuItem("Run Interpreter", KeyEvent.VK_F8, MI_RUN_INTERPRETER, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Debug...", KeyEvent.VK_F9, MI_DEBUG, this));
        menu.addItem(new XJMenuItem("Build and Debug...", KeyEvent.VK_F10, MI_BUILD_AND_DEBUG, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Debug Remote...", KeyEvent.VK_F11, MI_DEBUG_REMOTE, this));

        menubar.addCustomMenu(menu);
    }

    private void createGenerateMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("Generate");
        menu.addItem(new XJMenuItem("Generate Code...", MI_GENERATE_CODE, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Show Parser Code", MI_SHOW_GENERATED_PARSER_CODE, this));
        menu.addItem(new XJMenuItem("Show Lexer Code", MI_SHOW_GENERATED_LEXER_CODE, this));
        menu.addSeparator();
        menu.addItem(createMenuItem(MI_SHOW_RULE_GENCODE));

        menubar.addCustomMenu(menu);
    }

    private void createGoToMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("Go To");

        menu.addItem(createMenuItem(MI_GOTO_RULE));
        menu.addItem(createMenuItem(MI_GOTO_DECLARATION));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Line...", KeyEvent.VK_G, MI_GOTO_LINE, this));
        menu.addItem(new XJMenuItem("Character...", MI_GOTO_CHARACTER, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Back", KeyEvent.VK_LEFT, XJMenuItem.getKeyModifier() | Event.ALT_MASK, MI_GOTO_BACK, this));
        menu.addItem(new XJMenuItem("Forward", KeyEvent.VK_RIGHT, XJMenuItem.getKeyModifier() | Event.ALT_MASK, MI_GOTO_FORWARD, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Previous Breakpoint", MI_PREV_BREAKPOINT, this));
        menu.addItem(new XJMenuItem("Next Breakpoint", MI_NEXT_BREAKPOINT, this));

        menubar.addCustomMenu(menu);
    }

    private void createRefactorMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("Refactor");
        menu.addItem(createMenuItem(MI_RENAME));
        menu.addItem(createMenuItem(MI_REPLACE_LITERAL_WITH_TOKEN_LABEL));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Remove Left Recursion", MI_REMOVE_LEFT_RECURSION, this));
        menu.addItem(new XJMenuItem("Remove All Left Recursion", MI_REMOVE_ALL_LEFT_RECURSION, this));
        menu.addSeparator();
        menu.addItem(createMenuItem(MI_EXTRACT_RULE));
        menu.addItem(createMenuItem(MI_INLINE_RULE));
        menu.addSeparator();

        XJMenu literals = new XJMenu();
        literals.setTitle("Convert Literals");
        literals.addItem(new XJMenuItem("To Single Quote Literals", MI_LITERAL_TO_SINGLEQUOTE, this));
        literals.addItem(new XJMenuItem("To Double Quote Literals", MI_LITERAL_TO_DOUBLEQUOTE, this));
        literals.addItem(new XJMenuItem("To C-style Quote Literals", MI_LITERAL_TO_CSTYLEQUOTE, this));

        menu.addItem(literals);

        menubar.addCustomMenu(menu);
    }

    private void createGrammarMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("Grammar");
        menu.addItem(new XJMenuItem("Show Tokens Syntax Diagram", MI_SHOW_TOKENS_SD, this));
        menu.addItem(new XJMenuItem("Show Decision DFA", MI_SHOW_DECISION_DFA, this));
        menu.addItem(new XJMenuItem("Show Rule Dependency Graph", MI_SHOW_DEPENDENCY, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Insert Rule From Template", KeyEvent.VK_T, MI_INSERT_TEMPLATE, this));

        XJMenu rules = new XJMenu();
        rules.setTitle("Rules");
        rules.addItem(createMenuItem(MI_GROUP_RULE));
        rules.addItem(createMenuItem(MI_UNGROUP_RULE));
        rules.addSeparator();
        rules.addItem(createMenuItem(MI_IGNORE_RULE));
        rules.addItem(createMenuItem(MI_CONSIDER_RULE));

        XJMenu folding = new XJMenu();
        folding.setTitle("Folding");
        folding.addItem(new XJMenuItem("Toggle Rule", KeyEvent.VK_PERIOD, MI_EXPAND_COLLAPSE_RULE, this));
        folding.addItem(new XJMenuItem("Expand All Rules", KeyEvent.VK_PLUS, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_EXPAND_ALL_RULES, this));
        folding.addItem(new XJMenuItem("Collapse All Rules", KeyEvent.VK_MINUS, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_COLLAPSE_ALL_RULES, this));
        folding.addSeparator();
        folding.addItem(new XJMenuItem("Toggle Action", KeyEvent.VK_MINUS, MI_EXPAND_COLLAPSE_ACTION, this));
        folding.addItem(new XJMenuItem("Expand All Actions", KeyEvent.VK_PLUS, XJMenuItem.getKeyModifier() | Event.ALT_MASK, MI_EXPAND_ALL_ACTIONS, this));
        folding.addItem(new XJMenuItem("Collapse All Actions", KeyEvent.VK_MINUS, XJMenuItem.getKeyModifier() | Event.ALT_MASK, MI_COLLAPSE_ALL_ACTIONS, this));

        menu.addSeparator();
        menu.addItem(rules);
        menu.addItem(folding);
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Check Grammar", KeyEvent.VK_R, MI_CHECK_GRAMMAR, this));

        menubar.addCustomMenu(menu);
    }

    private void createFindMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("Find");
        menu.addItem(new XJMenuItem("Find...", KeyEvent.VK_F, MI_FIND, this));
        menu.addItem(new XJMenuItem("Find Next", KeyEvent.VK_F3, 0, MI_FIND_NEXT, this));
        menu.addItem(new XJMenuItem("Find Previous", KeyEvent.VK_F3, Event.SHIFT_MASK, MI_FIND_PREV, this));
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
                item = new XJMenuItem("Find Text at Caret", KeyEvent.VK_F3, MI_FIND_TOKEN, this);
                break;
            case MI_FIND_USAGE:
                item = new XJMenuItem("Find Usages", KeyEvent.VK_F7, Event.ALT_MASK, MI_FIND_USAGE, this);
                break;

            case MI_SHOW_DECISION_DFA:
                item = new XJMenuItem("Show Decision DFA", MI_SHOW_DECISION_DFA, this);
                break;

            case MI_SHOW_DEPENDENCY:
                item = new XJMenuItem("Show Rule Dependency Graph", MI_SHOW_DEPENDENCY, this);
                break;

            case MI_GOTO_RULE:
                item = new XJMenuItem(contextual?"Go To Rule...":"Rule...", KeyEvent.VK_B, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_GOTO_RULE, this);
                break;

            case MI_GOTO_DECLARATION:
                item = new XJMenuItem(contextual?"Go To Declaration":"Declaration", KeyEvent.VK_B, MI_GOTO_DECLARATION, this);
                break;

            case MI_RENAME:
                item = new XJMenuItem("Rename...", KeyEvent.VK_F6, Event.SHIFT_MASK, MI_RENAME, this);
                break;

            case MI_REPLACE_LITERAL_WITH_TOKEN_LABEL:
                item = new XJMenuItem("Replace Literals With Token Label...", MI_REPLACE_LITERAL_WITH_TOKEN_LABEL, this);
                break;

            case MI_EXTRACT_RULE:
                item = new XJMenuItem("Extract Rule...", MI_EXTRACT_RULE, this);
                break;

            case MI_INLINE_RULE:
                item = new XJMenuItem("Inline Rule", MI_INLINE_RULE, this);
                break;

            case MI_SHOW_RULE_GENCODE:
                item = new XJMenuItem("Show Rule Code", MI_SHOW_RULE_GENCODE, this);
                break;

            case MI_GROUP_RULE:
                item = new XJMenuItem("Group...", MI_GROUP_RULE, this);
                break;

            case MI_UNGROUP_RULE:
                item = new XJMenuItem("Ungroup", MI_UNGROUP_RULE, this);
                break;

            case MI_IGNORE_RULE:
                item = new XJMenuItem("Ignore in Interpreter", MI_IGNORE_RULE, this);
                break;

            case MI_CONSIDER_RULE:
                item = new XJMenuItem("Consider in Interpreter", MI_CONSIDER_RULE, this);
                break;

            case MI_EXPORT_AS_IMAGE:
                item = new XJMenuItem(contextual?"Export As Bitmap Image...":"As Bitmap Image...", MI_EXPORT_AS_IMAGE, this);
                break;

            case MI_EXPORT_AS_EPS:
                item = new XJMenuItem(contextual?"Export As EPS...":"As EPS...", MI_EXPORT_AS_EPS, this);
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
            case MI_EXPORT_AS_EPS:
                EditorTab tab = editor.getSelectedTab();
                item.setEnabled(tab.hasExportableGView());
                break;
        }
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        handleMenuView(item.getTag());
        handleMenuFind(item.getTag());
        handleMenuGrammar(item.getTag());
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

    public void handleMenuGrammar(int itemTag) {
        switch(itemTag) {
            case MI_SHOW_TOKENS_SD:
                editor.menuGrammar.showTokensSD();
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
                editor.menuGrammar.ignore();
                break;

            case MI_CONSIDER_RULE:
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
                editor.menuRun.runInterpreter();
                break;
            case MI_DEBUG:
                editor.menuRun.debug();
                break;

            case MI_BUILD_AND_DEBUG:
                editor.menuRun.buildAndDebug();
                break;

            case MI_DEBUG_REMOTE:
                editor.menuRun.debugRemote();
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
            case MI_PRIVATE_STATS:
                new DialogStatistics(editor.getWindowContainer()).runModal();
                break;
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

            case MI_EXPORT_EVENT:
                editor.menuExport.exportEventsAsTextFile();
                break;
        }
    }

}
