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
import edu.usfca.xj.foundation.notification.XJNotificationCenter;
import edu.usfca.xj.foundation.notification.XJNotificationObserver;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.dialog.DialogStatistics;

import java.awt.*;
import java.awt.event.KeyEvent;

public class EditorMenu implements XJMenuItemDelegate, XJNotificationObserver {

    // Edit
    public static final int MI_EDIT_UNDO = 0;
    public static final int MI_EDIT_REDO = 1;
    public static final int MI_EDIT_CUT = 2;
    public static final int MI_EDIT_COPY = 3;
    public static final int MI_EDIT_PASTE = 4;
    public static final int MI_EDIT_SELECT_ALL = 5;
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
    public static final int MI_GOTO_BACKWARD = 44;
    public static final int MI_GOTO_FORWARD = 45;
    public static final int MI_PREV_BREAKPOINT = 46;
    public static final int MI_NEXT_BREAKPOINT = 47;

    // Grammar
    public static final int MI_SHOW_DECISION_DFA = 50;
    public static final int MI_INSERT_TEMPLATE = 51;
    public static final int MI_GROUP = 52;
    public static final int MI_UNGROUP = 53;
    public static final int MI_CHECK_GRAMMAR = 54;

    // Refactor
    public static final int MI_RENAME = 60;
    public static final int MI_REPLACE_LITERAL_WITH_TOKEN_LABEL = 61;
    public static final int MI_REMOVE_LEFT_RECURSION = 62;
    public static final int MI_REMOVE_ALL_LEFT_RECURSION = 63;
    public static final int MI_EXTRACT_RULE = 64;
    public static final int MI_INLINE_RULE = 65;

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
    //public static final int MI_SAVE_ANTLR_NFA_DOT = 112;
    //public static final int MI_SAVE_RAW_NFA_DOT = 113;
    //public static final int MI_SAVE_OPTIMIZED_NFA_DOT = 114;
    public static final int MI_EXPORT_EVENT = 115;

    public static final int MI_PRIVATE_STATS = 200;
    public static final int MI_PRIVATE_UNREGISTER = 201;

    protected EditorWindow editor = null;

    protected XJMenuItem menuItemUndo = null;
    protected XJMenuItem menuItemRedo = null;


    public EditorMenu(EditorWindow editor) {
        this.editor = editor;
        XJNotificationCenter.defaultCenter().addObserver(this, Debugger.NOTIF_DEBUG_STARTED);
        XJNotificationCenter.defaultCenter().addObserver(this, Debugger.NOTIF_DEBUG_STOPPED);
    }

    public void close() {
        XJNotificationCenter.defaultCenter().removeObserver(this);
    }

    public void notificationFire(Object source, String name) {
        editor.getMainMenuBar().refresh();
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
        //exportMenu.addSeparator();
        //exportMenu.addItem(new XJMenuItem("ANTLR NFA as DOT...", MI_SAVE_ANTLR_NFA_DOT, this));
        //exportMenu.addItem(new XJMenuItem("Raw NFA as DOT...", MI_SAVE_RAW_NFA_DOT, this));
        //exportMenu.addItem(new XJMenuItem("Optimized NFA as DOT...", MI_SAVE_OPTIMIZED_NFA_DOT, this));

        menu.insertItemAfter(exportMenu, XJMainMenuBar.MI_SAVEAS);

        menu.insertSeparatorAfter(XJMainMenuBar.MI_SAVEAS);
    }

/*
    public void customizeWindowMenu(XJMenu menu) {
        menu.insertItemBefore(new XJMenuItem("Show Console", '=', KeyEvent.VK_EQUALS, MI_SHOW_CONSOLE, this), XJMainMenuBar.MI_WINDOW);
        menu.insertSeparatorBefore(XJMainMenuBar.MI_WINDOW);
    }
*/

    public void customizeHelpMenu(XJMenu menu) {
        menu.insertItemAfter(new XJMenuItem("Check for Updates", MI_CHECK_UPDATES, this), XJMainMenuBar.MI_HELP);
        menu.insertItemAfter(new XJMenuItem("Send Feedback", MI_SEND_FEEDBACK, this), XJMainMenuBar.MI_HELP);
        menu.insertItemAfter(new XJMenuItem("Submit Statistics...", MI_SUBMIT_STATS, this), XJMainMenuBar.MI_HELP);
        menu.insertSeparatorAfter(XJMainMenuBar.MI_HELP);
    }

    public void customizeMenuBar(XJMainMenuBar menubar) {
        createEditMenu(menubar);
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
        if(EditorPreferences.getPrivateMenu()) {
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
        menu.addItem(new XJMenuItem("Run Interpreter", 'i', KeyEvent.VK_F8, MI_RUN_INTERPRETER, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Debug...", 'd', KeyEvent.VK_F9, MI_DEBUG, this));
        menu.addItem(new XJMenuItem("Build and Debug...", 'b', KeyEvent.VK_F10, MI_BUILD_AND_DEBUG, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Debug Remote...", 'g', KeyEvent.VK_F11, MI_DEBUG_REMOTE, this));

        menubar.addCustomMenu(menu);
    }

    private void createGenerateMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("Generate");
        menu.addItem(new XJMenuItem("Generate Code...", MI_GENERATE_CODE, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Show Lexer Code", MI_SHOW_GENERATED_LEXER_CODE, this));
        menu.addItem(new XJMenuItem("Show Parser Code", MI_SHOW_GENERATED_PARSER_CODE, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Show Rule Code", MI_SHOW_RULE_GENCODE, this));

        menubar.addCustomMenu(menu);
    }

    private void createGoToMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("Go To");

        menu.addItem(new XJMenuItem("Rule...", 'u', KeyEvent.VK_B, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_GOTO_RULE, this));
        menu.addItem(new XJMenuItem("Declaration", 'b', KeyEvent.VK_B, MI_GOTO_DECLARATION, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Line...", 'g', KeyEvent.VK_G, MI_GOTO_LINE, this));
        menu.addItem(new XJMenuItem("Character...", MI_GOTO_CHARACTER, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Backward", 'b', KeyEvent.VK_LEFT, XJMenuItem.getKeyModifier() | Event.ALT_MASK, MI_GOTO_BACKWARD, this));
        menu.addItem(new XJMenuItem("Forward", 'f', KeyEvent.VK_RIGHT, XJMenuItem.getKeyModifier() | Event.ALT_MASK, MI_GOTO_FORWARD, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Previous Breakpoint", MI_PREV_BREAKPOINT, this));
        menu.addItem(new XJMenuItem("Next Breakpoint", MI_NEXT_BREAKPOINT, this));

        menubar.addCustomMenu(menu);
    }

    private void createRefactorMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("Refactor");
        menu.addItem(new XJMenuItem("Rename...", 'f', KeyEvent.VK_F6, Event.SHIFT_MASK, MI_RENAME, this));
        menu.addItem(new XJMenuItem("Replace Literal With Token Label...", MI_REPLACE_LITERAL_WITH_TOKEN_LABEL, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Remove Left Recursion", MI_REMOVE_LEFT_RECURSION, this));
        menu.addItem(new XJMenuItem("Remove All Left Recursion", MI_REMOVE_ALL_LEFT_RECURSION, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Extract Rule...", MI_EXTRACT_RULE, this));
        menu.addItem(new XJMenuItem("Inline Rule", MI_INLINE_RULE, this));

        menubar.addCustomMenu(menu);
    }

    private void createGrammarMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("Grammar");
        menu.addItem(new XJMenuItem("Show Current Decision DFA", MI_SHOW_DECISION_DFA, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Insert Rule From Template", 't', KeyEvent.VK_T, Event.CTRL_MASK, MI_INSERT_TEMPLATE, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Group...", MI_GROUP, this));
        menu.addItem(new XJMenuItem("Ungroup", MI_UNGROUP, this));

        XJMenu folding = new XJMenu();
        folding.setTitle("Folding");
        folding.addItem(new XJMenuItem("Toggle Rule", 't', KeyEvent.VK_PERIOD, MI_EXPAND_COLLAPSE_RULE, this));
        folding.addItem(new XJMenuItem("Expand All Rules", 'e', KeyEvent.VK_PLUS, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_EXPAND_ALL_RULES, this));
        folding.addItem(new XJMenuItem("Collapse All Rules", 'c', KeyEvent.VK_MINUS, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_COLLAPSE_ALL_RULES, this));
        folding.addSeparator();
        folding.addItem(new XJMenuItem("Toggle Action", 'a', KeyEvent.VK_MINUS, MI_EXPAND_COLLAPSE_ACTION, this));
        folding.addItem(new XJMenuItem("Expand All Actions", 'x', KeyEvent.VK_PLUS, XJMenuItem.getKeyModifier() | Event.ALT_MASK, MI_EXPAND_ALL_ACTIONS, this));
        folding.addItem(new XJMenuItem("Collapse All Actions", 'o', KeyEvent.VK_MINUS, XJMenuItem.getKeyModifier() | Event.ALT_MASK, MI_COLLAPSE_ALL_ACTIONS, this));
        
        menu.addSeparator();
        menu.addItem(folding);
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Check Grammar", 'r', KeyEvent.VK_R, MI_CHECK_GRAMMAR, this));

        menubar.addCustomMenu(menu);
    }

    private void createFindMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("Find");
        menu.addItem(new XJMenuItem("Find...", 'f', KeyEvent.VK_F, MI_FIND, this));
        menu.addItem(new XJMenuItem("Find Next", 'n', KeyEvent.VK_F3, 0, MI_FIND_NEXT, this));
        menu.addItem(new XJMenuItem("Find Previous", 'p', KeyEvent.VK_F3, Event.SHIFT_MASK, MI_FIND_PREV, this));
        menu.addItem(new XJMenuItem("Find Text at Caret", 't', KeyEvent.VK_F3, MI_FIND_TOKEN, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Find Usages", 'f', KeyEvent.VK_F7, Event.ALT_MASK, MI_FIND_USAGE, this));

        menubar.addCustomMenu(menu);
    }

    private void createEditMenu(XJMainMenuBar menubar) {
        XJMenu menu = new XJMenu();
        menu.setTitle("Edit");
        menu.addItem(menuItemUndo = new XJMenuItem("Undo", 'z', KeyEvent.VK_Z, MI_EDIT_UNDO, this));
        menu.addItem(menuItemRedo = new XJMenuItem("Redo", 'z', KeyEvent.VK_Z, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_EDIT_REDO, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Cut", 'x', KeyEvent.VK_X, MI_EDIT_CUT, this));
        menu.addItem(new XJMenuItem("Copy", 'c', KeyEvent.VK_C, MI_EDIT_COPY, this));
        menu.addItem(new XJMenuItem("Paste", 'v', KeyEvent.VK_V, MI_EDIT_PASTE, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Select All", 'a', KeyEvent.VK_A, MI_EDIT_SELECT_ALL, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItemCheck("Auto-Indentation", MI_TOGGLE_AUTOINDENT, this, true));
        //menu.addItem(new XJMenuItemCheck("Syntax Coloring", MI_TOGGLE_SYNTAX_COLORING, this, true));
        //menu.addItem(new XJMenuItemCheck("Syntax Diagram", MI_TOGGLE_SYNTAX_DIAGRAM, this, true));
        menu.addItem(new XJMenuItemCheck("Optimize Syntax Diagram", MI_TOGGLE_NFA_OPTIMIZATION, this, true));

        menubar.addCustomMenu(menu);
    }

    public void menuItemState(XJMenuItem item) {
        switch(item.getTag()) {
            case MI_EDIT_UNDO:
            case MI_EDIT_REDO:
            case MI_EDIT_CUT:
            case MI_EDIT_PASTE:
            case MI_RENAME:
            case MI_REPLACE_LITERAL_WITH_TOKEN_LABEL:
            case MI_REMOVE_LEFT_RECURSION:
            case MI_REMOVE_ALL_LEFT_RECURSION:
            case MI_EXTRACT_RULE:
            case MI_INLINE_RULE:
            case MI_INSERT_TEMPLATE:
            case MI_GROUP:
            case MI_UNGROUP:
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

            case MI_GOTO_BACKWARD:
                item.setEnabled(editor.editorGoToHistory.canGoBackward());
                break;
            case MI_GOTO_FORWARD:
                item.setEnabled(editor.editorGoToHistory.canGoForward());
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
                    item.setEnabled(EditorPreferences.getP4Enabled());
                break;
        }
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        editor.handleMenuEvent(menu, item);
        handleMenuEdit(item.getTag());
        handleMenuView(item.getTag());
        handleMenuFind(item.getTag());
        handleMenuGrammar(item.getTag());
        handleMenuRefactor(item.getTag());
        handleMenuGoTo(item.getTag());
        handleMenuGenerate(item.getTag());
        handleMenuRun(item.getTag());
        handleMenuSCM(item.getTag());
        handleMenuPrivate(item.getTag());
        handleMenuHelp(item.getTag());
        handleMenuExport(item.getTag());
    }

    public void handleMenuEdit(int itemTag) {
        switch(itemTag) {
            case MI_EDIT_UNDO:
                editor.actionsEdit.performUndo();
                break;

            case MI_EDIT_REDO:
                editor.actionsEdit.performRedo();
                break;

            case MI_EDIT_CUT:
                editor.actionsEdit.performCutToClipboard();
                break;

            case MI_EDIT_COPY:
                editor.actionsEdit.performCopyToClipboard();
                break;

            case MI_EDIT_PASTE:
                editor.actionsEdit.performPasteFromClipboard();
                break;

            case MI_EDIT_SELECT_ALL:
                editor.actionsEdit.performSelectAll();
                break;

            case MI_TOGGLE_AUTOINDENT:
                editor.toggleAutoIndent();
                break;

            case MI_TOGGLE_SYNTAX_COLORING:
                editor.toggleSyntaxColoring();
                break;

            case MI_TOGGLE_SYNTAX_DIAGRAM:
                editor.toggleSyntaxDiagram();
                break;

            case MI_TOGGLE_NFA_OPTIMIZATION:
                editor.toggleNFAOptimization();
                break;
        }
    }

    public void handleMenuView(int itemTag) {
        switch(itemTag) {
            case MI_EXPAND_COLLAPSE_RULE:
                editor.actionsView.expandCollapseRule();
                break;

            case MI_EXPAND_ALL_RULES:
                editor.actionsView.expandAllRules();
                break;

            case MI_COLLAPSE_ALL_RULES:
                editor.actionsView.collapseAllRules();
                break;

            case MI_EXPAND_COLLAPSE_ACTION:
                editor.actionsView.expandCollapseAction();
                break;

            case MI_EXPAND_ALL_ACTIONS:
                editor.actionsView.expandAllActions();
                break;

            case MI_COLLAPSE_ALL_ACTIONS:
                editor.actionsView.collapseAllActions();
                break;
        }
    }

    public void handleMenuFind(int itemTag) {
        switch(itemTag) {
            case MI_FIND:
                editor.actionsFind.find();
                break;

            case MI_FIND_NEXT:
                editor.actionsFind.findNext();
                break;

            case MI_FIND_PREV:
                editor.actionsFind.findPrev();
                break;

            case MI_FIND_TOKEN:
                editor.actionsFind.findSelection();
                break;

            case MI_FIND_USAGE:
                editor.actionsFind.findUsage();
                break;
        }
    }

    public void handleMenuGrammar(int itemTag) {
        switch(itemTag) {
            case MI_SHOW_DECISION_DFA:
                editor.actionsGrammar.showDecisionDFA();
                break;

            case MI_INSERT_TEMPLATE:
                editor.actionsGrammar.insertRuleFromTemplate();
                break;

            case MI_GROUP:
                editor.actionsGrammar.group();
                break;

            case MI_UNGROUP:
                editor.actionsGrammar.ungroup();
                break;

            case MI_CHECK_GRAMMAR:
                editor.actionsGrammar.checkGrammar();
                break;
        }
    }

    public void handleMenuRefactor(int itemTag) {
        switch(itemTag) {
            case MI_RENAME:
                editor.actionsRefactor.rename();
                break;

            case MI_REPLACE_LITERAL_WITH_TOKEN_LABEL:
                editor.actionsRefactor.replaceLiteralWithTokenLabel();
                break;

            case MI_REMOVE_LEFT_RECURSION:
                editor.actionsRefactor.removeLeftRecursion();
                break;

            case MI_REMOVE_ALL_LEFT_RECURSION:
                editor.actionsRefactor.removeAllLeftRecursion();
                break;

            case MI_EXTRACT_RULE:
                editor.actionsRefactor.extractRule();
                break;

            case MI_INLINE_RULE:
                editor.actionsRefactor.inlineRule();
                break;
        }
    }

    public void handleMenuGoTo(int itemTag) {
        switch(itemTag) {
            case MI_GOTO_RULE:
                editor.actionsGoTo.goToRule();
                break;

            case MI_GOTO_DECLARATION:
                editor.actionsGoTo.goToDeclaration();
                break;

            case MI_GOTO_LINE:
                editor.actionsGoTo.goToLine();
                break;

            case MI_GOTO_CHARACTER:
                editor.actionsGoTo.goToCharacter();
                break;

            case MI_GOTO_BACKWARD:
                editor.actionsGoTo.goToBackward();
                break;

            case MI_GOTO_FORWARD:
                editor.actionsGoTo.goToForward();
                break;

            case MI_PREV_BREAKPOINT:
                editor.actionsGoTo.goToBreakpoint(-1);
                break;

            case MI_NEXT_BREAKPOINT:
                editor.actionsGoTo.goToBreakpoint(1);
                break;
        }
    }

    public void handleMenuGenerate(int itemTag) {
        switch(itemTag) {
            case MI_GENERATE_CODE:
                editor.actionsGenerate.generateCode();
                break;

            case MI_SHOW_GENERATED_LEXER_CODE:
                editor.actionsGenerate.showGeneratedCode(true);
                break;

            case MI_SHOW_GENERATED_PARSER_CODE:
                editor.actionsGenerate.showGeneratedCode(false);
                break;

            case MI_SHOW_RULE_GENCODE:
                editor.actionsGenerate.showRuleGeneratedCode();
                break;
        }
    }

    public void handleMenuRun(int itemTag) {
        switch(itemTag) {
            case MI_RUN_INTERPRETER:
                editor.actionsRun.runInterpreter();
                break;
            case MI_DEBUG:
                editor.actionsRun.debug();
                break;

            case MI_BUILD_AND_DEBUG:
                editor.actionsRun.buildAndDebug();
                break;

            case MI_DEBUG_REMOTE:
                editor.actionsRun.debugRemote();
                break;
        }
    }

    public void handleMenuSCM(int itemTag) {
        switch(itemTag) {
            case MI_P4_EDIT:
                editor.actionsSCM.editFile();
                break;
            case MI_P4_ADD:
                editor.actionsSCM.addFile();
                break;
            case MI_P4_DELETE:
                editor.actionsSCM.deleteFile();
                break;
            case MI_P4_REVERT:
                editor.actionsSCM.revertFile();
                break;
            case MI_P4_SUBMIT:
                editor.actionsSCM.submitFile();
                break;
            case MI_P4_SYNC:
                editor.actionsSCM.sync();
                break;
        }
    }

    public void handleMenuHelp(int itemTag) {
        switch(itemTag) {
            case MI_SUBMIT_STATS:
                editor.actionsHelp.submitStats();
                break;
            case MI_SEND_FEEDBACK:
                editor.actionsHelp.sendFeedback();
                break;
            case MI_CHECK_UPDATES:
                editor.actionsHelp.checkUpdates();
                break;
        }
    }

    public void handleMenuPrivate(int itemTag) {
        switch(itemTag) {
            case MI_PRIVATE_STATS:
                new DialogStatistics(editor.getWindowContainer()).runModal();
                break;
            case MI_PRIVATE_UNREGISTER:
                EditorPreferences.removeUserRegistration();
                break;
        }
    }

    public void handleMenuExport(int itemTag) {
        switch(itemTag) {
            case MI_EXPORT_AS_IMAGE:
                editor.actionsExport.exportAsImage();
                break;

            case MI_EXPORT_AS_EPS:
                editor.actionsExport.exportAsEPS();
                break;

/*            case MI_SAVE_ANTLR_NFA_DOT:
                editor.visual.saveANTLRNFA2DOT(editor.getCurrentRule());
                break;

            case MI_SAVE_RAW_NFA_DOT:
                editor.visual.saveRawNFA2DOT(editor.getCurrentRule());
                break;

            case MI_SAVE_OPTIMIZED_NFA_DOT:
                editor.visual.saveOptimizedNFA2DOT(editor.getCurrentRule());
                break;*/

            case MI_EXPORT_EVENT:
                editor.actionsExport.exportEventsAsTextFile();
                break;
        }
    }
}
