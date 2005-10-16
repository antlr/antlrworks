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

    public static final int MI_FIND_USAGE = 20;
    public static final int MI_RENAME = 21;
    public static final int MI_REPLACE_LITERAL_WITH_TOKEN_LABEL = 22;
    public static final int MI_REMOVE_LEFT_RECURSION = 23;
    public static final int MI_INSERT_TEMPLATE = 24;
    public static final int MI_GROUP = 25;
    public static final int MI_UNGROUP = 26;
    public static final int MI_HIDE_ACTION = 27;
    public static final int MI_SHOW_ALL_ACTION = 28;
    public static final int MI_HIDE_ALL_ACTION = 29;
    public static final int MI_CHECK_GRAMMAR = 30;

    public static final int MI_FIND = 31;
    public static final int MI_FIND_NEXT = 32;
    public static final int MI_FIND_PREV = 33;
    public static final int MI_SHOW_DECISION_DFA = 34;

    public static final int MI_GOTO_RULE = 40;
    public static final int MI_GOTO_DECLARATION = 41;
    public static final int MI_GOTO_LINE = 42;
    public static final int MI_GOTO_CHARACTER = 43;
    public static final int MI_GOTO_BACKWARD = 44;
    public static final int MI_GOTO_FORWARD = 45;
    public static final int MI_PREV_BREAKPOINT = 46;
    public static final int MI_NEXT_BREAKPOINT = 47;

    public static final int MI_GENERATE_CODE = 50;
    public static final int MI_SHOW_GENERATED_PARSER_CODE = 51;
    public static final int MI_SHOW_GENERATED_LEXER_CODE = 52;
    public static final int MI_SHOW_RULE_GENCODE = 53;

    public static final int MI_SAVE_AS_IMAGE = 54;
    public static final int MI_SAVE_ANTLR_NFA_DOT = 55;
    public static final int MI_SAVE_RAW_NFA_DOT = 56;
    public static final int MI_SAVE_OPTIMIZED_NFA_DOT = 57;
    public static final int MI_EXPORT_EVENT = 58;

    public static final int MI_RUN_INTERPRETER = 60;
    public static final int MI_DEBUG = 61;
    public static final int MI_BUILD_AND_DEBUG = 62;
    public static final int MI_DEBUG_REMOTE = 63;

    public static final int MI_P4_EDIT = 70;
    public static final int MI_P4_ADD = 71;
    public static final int MI_P4_DELETE = 72;
    public static final int MI_P4_REVERT = 73;
    public static final int MI_P4_SUBMIT = 74;
    public static final int MI_P4_SYNC = 75;

    public static final int MI_SUBMIT_STATS = 80;
    public static final int MI_SEND_FEEDBACK = 81;
    public static final int MI_CHECK_UPDATES = 82;

    public static final int MI_PRIVATE_STATS = 100;
    public static final int MI_PRIVATE_UNREGISTER = 101;

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
        exportMenu.setTitle("Export Rule");
        exportMenu.addItem(new XJMenuItem("As Image...", MI_SAVE_AS_IMAGE, this));
        exportMenu.addSeparator();
        exportMenu.addItem(new XJMenuItem("ANTLR NFA as DOT...", MI_SAVE_ANTLR_NFA_DOT, this));
        exportMenu.addItem(new XJMenuItem("Raw NFA as DOT...", MI_SAVE_RAW_NFA_DOT, this));
        exportMenu.addItem(new XJMenuItem("Optimized NFA as DOT...", MI_SAVE_OPTIMIZED_NFA_DOT, this));

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
        createMenuEdit(menubar);
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

        menu.addItem(new XJMenuItem("Rule...", 'n', KeyEvent.VK_N, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_GOTO_RULE, this));
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
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Hide Action", '-', KeyEvent.VK_MINUS, MI_HIDE_ACTION, this));
        menu.addItem(new XJMenuItem("Show All Actions", '+', KeyEvent.VK_PLUS, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_SHOW_ALL_ACTION, this));
        menu.addItem(new XJMenuItem("Hide All Actions", '-', KeyEvent.VK_MINUS, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_HIDE_ALL_ACTION, this));
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
        menu.addItem(new XJMenuItem("Find Previous", 'p', KeyEvent.VK_F3, Event.ALT_MASK, MI_FIND_PREV, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem("Find Usages", 'f', KeyEvent.VK_F7, Event.ALT_MASK, MI_FIND_USAGE, this));

        menubar.addCustomMenu(menu);
    }

    private void createMenuEdit(XJMainMenuBar menubar) {
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
        menu.addItem(new XJMenuItemCheck("Syntax Coloring", MI_TOGGLE_SYNTAX_COLORING, this, true));
        menu.addItem(new XJMenuItemCheck("Syntax Diagram", MI_TOGGLE_SYNTAX_DIAGRAM, this, true));
        menu.addItem(new XJMenuItemCheck("Optimize Syntax Diagram", MI_TOGGLE_NFA_OPTIMIZATION, this, true));

        menubar.addCustomMenu(menu);
    }

    public void menuItemState(XJMenuItem item) {
        switch(item.getTag()) {
            case MI_EDIT_PASTE:
            case MI_RENAME:
            case MI_REPLACE_LITERAL_WITH_TOKEN_LABEL:
            case MI_REMOVE_LEFT_RECURSION:
            case MI_INSERT_TEMPLATE:
            case MI_GROUP:
            case MI_UNGROUP:
            case MI_HIDE_ACTION:
            case MI_SHOW_ALL_ACTION:
            case MI_HIDE_ALL_ACTION:
            case MI_CHECK_GRAMMAR:
            case MI_FIND:
            case MI_RUN_INTERPRETER:
            case MI_DEBUG:
            case MI_BUILD_AND_DEBUG:
            case MI_DEBUG_REMOTE:
                item.setEnabled(!isDebuggerRunning());
                break;

            case MI_GOTO_BACKWARD:
                item.setEnabled(editor.goToHistory.canGoBackward());
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
                    item.setEnabled(EditorPreferences.getP4Enabled());
                break;
        }
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        editor.handleMenuEvent(menu, item);
        handleMenuEdit(item.getTag());
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
                editor.menuEditActions.performUndo();
                break;

            case MI_EDIT_REDO:
                editor.menuEditActions.performRedo();
                break;

            case MI_EDIT_CUT:
                editor.menuEditActions.performCutToClipboard();
                break;

            case MI_EDIT_COPY:
                editor.menuEditActions.performCopyToClipboard();
                break;

            case MI_EDIT_PASTE:
                editor.menuEditActions.performPasteFromClipboard();
                break;

            case MI_EDIT_SELECT_ALL:
                editor.menuEditActions.performSelectAll();
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

    public void handleMenuFind(int itemTag) {
        switch(itemTag) {
            case MI_FIND:
                editor.menuFindActions.find();
                break;

            case MI_FIND_NEXT:
                editor.menuFindActions.findNext();
                break;

            case MI_FIND_PREV:
                editor.menuFindActions.findPrev();
                break;

            case MI_FIND_USAGE:
                editor.menuFindActions.findUsage();
                break;
        }
    }

    public void handleMenuGrammar(int itemTag) {
        switch(itemTag) {
            case MI_SHOW_DECISION_DFA:
                editor.menuGrammarActions.showDecisionDFA();
                break;

            case MI_INSERT_TEMPLATE:
                editor.menuGrammarActions.insertRuleFromTemplate();
                break;

            case MI_GROUP:
                editor.menuGrammarActions.group();
                break;

            case MI_UNGROUP:
                editor.menuGrammarActions.ungroup();
                break;

            case MI_HIDE_ACTION:
                editor.menuGrammarActions.hideAction();
                break;

            case MI_SHOW_ALL_ACTION:
                editor.menuGrammarActions.showAllActions();
                break;

            case MI_HIDE_ALL_ACTION:
                editor.menuGrammarActions.hideAllActions();
                break;

            case MI_CHECK_GRAMMAR:
                editor.menuGrammarActions.checkGrammar();
                break;
        }
    }

    public void handleMenuRefactor(int itemTag) {
        switch(itemTag) {
            case MI_RENAME:
                editor.menuRefactorActions.rename();
                break;

            case MI_REPLACE_LITERAL_WITH_TOKEN_LABEL:
                editor.menuRefactorActions.replaceLiteralWithTokenLabel();
                break;

            case MI_REMOVE_LEFT_RECURSION:
                editor.menuRefactorActions.removeLeftRecursion();
                break;
        }
    }

    public void handleMenuGoTo(int itemTag) {
        switch(itemTag) {
            case MI_GOTO_RULE:
                editor.menuGoToActions.goToRule();
                break;

            case MI_GOTO_DECLARATION:
                editor.menuGoToActions.goToDeclaration();
                break;

            case MI_GOTO_LINE:
                editor.menuGoToActions.goToLine();
                break;

            case MI_GOTO_CHARACTER:
                editor.menuGoToActions.goToCharacter();
                break;

            case MI_GOTO_BACKWARD:
                editor.menuGoToActions.goToBackward();
                break;

            case MI_GOTO_FORWARD:
                editor.menuGoToActions.goToForward();
                break;

            case MI_PREV_BREAKPOINT:
                editor.menuGoToActions.goToBreakpoint(-1);
                break;

            case MI_NEXT_BREAKPOINT:
                editor.menuGoToActions.goToBreakpoint(1);
                break;
        }
    }

    public void handleMenuGenerate(int itemTag) {
        switch(itemTag) {
            case MI_GENERATE_CODE:
                editor.menuGenerateActions.generateCode();
                break;

            case MI_SHOW_GENERATED_LEXER_CODE:
                editor.menuGenerateActions.showGeneratedCode(true);
                break;

            case MI_SHOW_GENERATED_PARSER_CODE:
                editor.menuGenerateActions.showGeneratedCode(false);
                break;

            case MI_SHOW_RULE_GENCODE:
                editor.menuGenerateActions.showRuleGeneratedCode();
                break;
        }
    }

    public void handleMenuRun(int itemTag) {
        switch(itemTag) {
            case MI_RUN_INTERPRETER:
                editor.menuRunActions.runInterpreter();
                break;
            case MI_DEBUG:
                editor.menuRunActions.debug();
                break;

            case MI_BUILD_AND_DEBUG:
                editor.menuRunActions.buildAndDebug();
                break;

            case MI_DEBUG_REMOTE:
                editor.menuRunActions.debugRemote();
                break;
        }
    }

    public void handleMenuSCM(int itemTag) {
        switch(itemTag) {
            case MI_P4_EDIT:
                editor.menuSCMActions.editFile();
                break;
            case MI_P4_ADD:
                editor.menuSCMActions.addFile();
                break;
            case MI_P4_DELETE:
                editor.menuSCMActions.deleteFile();
                break;
            case MI_P4_REVERT:
                editor.menuSCMActions.revertFile();
                break;
            case MI_P4_SUBMIT:
                editor.menuSCMActions.submitFile();
                break;
            case MI_P4_SYNC:
                editor.menuSCMActions.sync();
                break;
        }
    }

    public void handleMenuHelp(int itemTag) {
        switch(itemTag) {
            case MI_SUBMIT_STATS:
                editor.menuHelpActions.submitStats();
                break;
            case MI_SEND_FEEDBACK:
                editor.menuHelpActions.sendFeedback();
                break;
            case MI_CHECK_UPDATES:
                editor.menuHelpActions.checkUpdates();
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
            case MI_SAVE_AS_IMAGE:
                editor.visual.saveAsImage();
                break;

            case MI_SAVE_ANTLR_NFA_DOT:
                editor.visual.saveANTLRNFA2DOT(editor.getCurrentRule());
                break;

            case MI_SAVE_RAW_NFA_DOT:
                editor.visual.saveRawNFA2DOT(editor.getCurrentRule());
                break;

            case MI_SAVE_OPTIMIZED_NFA_DOT:
                editor.visual.saveOptimizedNFA2DOT(editor.getCurrentRule());
                break;

            case MI_EXPORT_EVENT:
                editor.menuExportActions.exportEventsAsTextFile();
                break;
        }
    }
}
