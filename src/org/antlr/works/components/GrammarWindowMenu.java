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

package org.antlr.works.components;

import org.antlr.works.IDE;
import org.antlr.works.editor.GrammarWindowTab;
import org.antlr.works.grammar.element.ElementGrammarName;
import org.antlr.works.menu.*;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.menu.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

public class GrammarWindowMenu implements XJMenuItemDelegate {

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

    // Run
    public static final int MI_RUN_INTERPRETER = 80;
    public static final int MI_RUN = 81;
    public static final int MI_DEBUG = 82;
    public static final int MI_DEBUG_AGAIN = 83;
    public static final int MI_DEBUG_REMOTE = 84;
    public static final int MI_DEBUG_SHOW_INPUT_TOKENS = 86;
    public static final int MI_EDIT_TEST_RIG = 87;

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

    private FindMenu actionFind;
    private org.antlr.works.menu.GrammarMenu actionGrammar;
    private GoToMenu goToMenu;
    private GenerateMenu actionGenerate;
    private DebugMenu debugMenu;
    private ExportMenu actionExport;
    private ActionRefactor actionRefactor;

    private GrammarWindow window;
    private XJMenuItem ignoreRuleMenuItem;

    /** The resource bundle used to get localized strings */
    private static ResourceBundle resourceBundle = IDE.getMenusResourceBundle();

    public GrammarWindowMenu(GrammarWindow window) {
        this.window = window;

        actionFind = new FindMenu(window);
        actionGrammar = new org.antlr.works.menu.GrammarMenu(window);
        goToMenu = new GoToMenu(window);
        actionGenerate = new GenerateMenu(window);
        debugMenu = new DebugMenu(window);
        actionExport = new ExportMenu(window);
        actionRefactor = new GrammarRefactorMenu(window);
    }

    public void close() {
        actionGenerate.close();
    }

    public void awake() {
        actionGenerate.awake();
    }

    public boolean isDebuggerRunning() {
        return debugMenu.isRunning();
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

    private void createRunMenu(XJMainMenuBar menubar) {
        XJMenu menu = new XJMenu();
        menu.setTitle(resourceBundle.getString("menu.title.run"));

        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.run"), KeyEvent.VK_R, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_RUN, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.debug"), KeyEvent.VK_D, MI_DEBUG, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.debugAgain"), KeyEvent.VK_D, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_DEBUG_AGAIN, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.debugRemote"), MI_DEBUG_REMOTE, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showInputTokens"), MI_DEBUG_SHOW_INPUT_TOKENS, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(getEditTestRigTitle(), MI_EDIT_TEST_RIG, this));

        menubar.addCustomMenu(menu);
    }

    private String getEditTestRigTitle() {
        String grammarName = "";
        String language = null;
        String menuItemName;

        if (window.getDebuggerTab() != null) {
            if (window.getDebuggerTab().getDelegate().getDocument() != null)
                grammarName = window.getDebuggerTab().getDelegate().getDocument().getDocumentName();
            if (window.getDebuggerTab().getDelegate().getGrammarEngine() != null)
                language = window.getDebuggerTab().getDelegate().getGrammarEngine().getGrammarLanguage();
        }

        if (grammarName != null && !"".equals(grammarName))
            menuItemName = "Edit " + grammarName + " Test Rig";
        else
            menuItemName = "Edit Test Rig";

        if (language != null) {
            menuItemName = menuItemName.concat(" for " + language);
        }
        return menuItemName;
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
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle(resourceBundle.getString("menu.title.grammar"));
        menu.addItem(new XJMenuItemCheck(resourceBundle.getString("menu.item.highlightDecisionDFA"), MI_HIGHLIGHT_DECISION_DFA, this, false));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showRuleDependencyGraph"), MI_SHOW_DEPENDENCY, this));
        menu.addSeparator();
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showTokensSyntaxDiagram"), MI_SHOW_TOKENS_SD, this));
        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.showTokensDFA"), MI_SHOW_TOKENS_DFA, this));
        menu.addSeparator();

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

        menu.addItem(rules);
        //menu.addItem(folding);
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

    public JPopupMenu getContextualMenu(int textIndex) {
        boolean overReference = window.getCurrentReference() != null;
        boolean overToken = window.getCurrentToken() != null;
        boolean overRule = window.getCurrentRule() != null;
        boolean overSelection = window.getTextPane().getSelectionStart() != window.getTextPane().getSelectionEnd();
        boolean overDecisionDFA = window.decisionDFAEngine.isDecisionPointAroundLocation(window.getTextEditor().getLineIndexAtTextPosition(textIndex),
                window.getTextEditor().getColumnPositionAtIndex(textIndex));

        ContextualMenuFactory factory = new ContextualMenuFactory(this);
        factory.addItem(MI_GOTO_RULE);
        if(overReference)
            factory.addItem(MI_GOTO_DECLARATION);

        factory.addSeparator();
        if(overToken)
            factory.addItem(MI_RENAME);
        if(actionRefactor.canReplaceLiteralWithTokenLabel())
            factory.addItem(MI_REPLACE_LITERAL_WITH_TOKEN_LABEL);
        if(actionRefactor.canExtractRule())
            factory.addItem(MI_EXTRACT_RULE);
        if(actionRefactor.canInlineRule())
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
        GrammarWindowTab tab = window.getSelectedTab();

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
                item.setEnabled(window.isFileWritable());
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
                item.setEnabled(!isDebuggerRunning() && debugMenu.canDebugAgain());
                break;

            case MI_EDIT_TEST_RIG:
                item.setTitle(getEditTestRigTitle());
                boolean enabled = (XJApplication.shared().getActiveWindow() != null) &&
                        (XJApplication.shared().getActiveWindow().getDocument() != null) &&
                        XJApplication.shared().getActiveWindow().getDocument().getDocumentName() != null;
                item.setEnabled(!isDebuggerRunning() && enabled);
                break;

            case MI_RUN:
            case MI_DEBUG:
            case MI_DEBUG_REMOTE:
                item.setEnabled(!isDebuggerRunning());
                break;

            case MI_GOTO_BACK:
                item.setEnabled(window.goToHistory.canGoBack());
                break;
            case MI_GOTO_FORWARD:
                item.setEnabled(window.goToHistory.canGoForward());
                break;

            case MI_EXPORT_AS_IMAGE:
                item.setEnabled(tab != null && tab.canExportToBitmap());
                break;

            case MI_EXPORT_AS_EPS:
                item.setEnabled(tab != null && tab.canExportToEPS());
                break;

            case MI_EXPORT_AS_DOT:
                item.setEnabled(tab != null && tab.canExportToDOT());
                break;

            case MI_DEBUG_SHOW_INPUT_TOKENS:
                item.setTitle(debugMenu.isInputTokenVisible()?
                        resourceBundle.getString("menu.item.hideInputTokens") : resourceBundle.getString("menu.item.showInputTokens"));
                break;

            case MI_HIGHLIGHT_DECISION_DFA:
                if(window.decisionDFAEngine.getDecisionDFACount() == 0) {
                    item.setSelected(false);
                } else {
                    item.setSelected(true);
                }
                break;
        }
    }

    public void handleMenuSelected(XJMenu menu) {
        boolean ignored = window.editorRules.getFirstSelectedRuleIgnoredFlag();
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
        handleMenuPrivate(item.getTag());
        handleMenuExport(item.getTag());
    }

    public void handleMenuFile(int itemTag) {
        switch(itemTag) {
            case MI_PRINT:
                window.print();
                break;
        }
    }

    public void handleMenuFind(int itemTag) {
        switch(itemTag) {
            case MI_FIND:
                actionFind.find();
                break;

            case MI_FIND_NEXT:
                actionFind.findNext();
                break;

            case MI_FIND_PREV:
                actionFind.findPrev();
                break;

            case MI_FIND_TOKEN:
                actionFind.findSelection();
                break;

            case MI_FIND_USAGE:
                actionFind.findUsage();
                break;
        }
    }

    public void handleMenuGrammar(XJMenuItem item) {
        switch(item.getTag()) {
            case MI_SHOW_TOKENS_SD:
                actionGrammar.showTokensSD();
                break;

            case MI_SHOW_TOKENS_DFA:
                actionGrammar.showTokensDFA();
                break;

            case MI_SHOW_DECISION_DFA:
                actionGrammar.showDecisionDFA();
                break;

            case MI_HIGHLIGHT_DECISION_DFA:
                actionGrammar.highlightDecisionDFA();
                break;

            case MI_SHOW_DEPENDENCY:
                actionGrammar.showDependency();
                break;

            case MI_GROUP_RULE:
                actionGrammar.group();
                break;

            case MI_UNGROUP_RULE:
                actionGrammar.ungroup();
                break;

            case MI_IGNORE_RULE:
                if(item.isSelected())
                    actionGrammar.ignore();
                else
                    actionGrammar.consider();
                break;

            case MI_CHECK_GRAMMAR:
                actionGrammar.checkGrammar();
                break;
        }
    }

    public void handleMenuRefactor(int itemTag) {
        switch(itemTag) {
            case MI_RENAME:
                actionRefactor.rename();
                break;

            case MI_REPLACE_LITERAL_WITH_TOKEN_LABEL:
                actionRefactor.replaceLiteralWithTokenLabel();
                break;

            case MI_LITERAL_TO_SINGLEQUOTE:
                actionRefactor.convertLiteralsToSingleQuote();
                break;

            case MI_LITERAL_TO_DOUBLEQUOTE:
                actionRefactor.convertLiteralsToDoubleQuote();
                break;

            case MI_LITERAL_TO_CSTYLEQUOTE:
                actionRefactor.convertLiteralsToCStyleQuote();
                break;

            case MI_REMOVE_LEFT_RECURSION:
                actionRefactor.removeLeftRecursion();
                break;

            case MI_REMOVE_ALL_LEFT_RECURSION:
                actionRefactor.removeAllLeftRecursion();
                break;

            case MI_EXTRACT_RULE:
                actionRefactor.extractRule();
                break;

            case MI_INLINE_RULE:
                actionRefactor.inlineRule();
                break;
        }
    }

    public void handleMenuGoTo(int itemTag) {
        switch(itemTag) {
            case MI_GOTO_RULE:
                goToMenu.goToRule();
                break;

            case MI_GOTO_DECLARATION:
                goToMenu.goToDeclaration();
                break;

            case MI_GOTO_LINE:
                goToMenu.goToLine();
                break;

            case MI_GOTO_CHARACTER:
                goToMenu.goToCharacter();
                break;

            case MI_GOTO_BACK:
                goToMenu.goToBackward();
                break;

            case MI_GOTO_FORWARD:
                goToMenu.goToForward();
                break;

            case MI_PREV_BREAKPOINT:
                goToMenu.goToBreakpoint(-1);
                break;

            case MI_NEXT_BREAKPOINT:
                goToMenu.goToBreakpoint(1);
                break;
        }
    }

    public void handleMenuGenerate(int itemTag) {
        switch(itemTag) {
            case MI_GENERATE_CODE:
                actionGenerate.generateCode();
                break;

            case MI_SHOW_GENERATED_LEXER_CODE:
                actionGenerate.showGeneratedCode(ElementGrammarName.LEXER);
                break;

            case MI_SHOW_GENERATED_PARSER_CODE:
                actionGenerate.showGeneratedCode(ElementGrammarName.PARSER);
                break;

            case MI_SHOW_RULE_GENCODE:
                actionGenerate.showRuleGeneratedCode();
                break;
        }
    }

    public void handleMenuRun(int itemTag) {
        switch(itemTag) {
            case MI_RUN_INTERPRETER:
                debugMenu.runInterpreter();
                break;

            case MI_RUN:
                debugMenu.run();
                break;

            case MI_DEBUG:
                debugMenu.debug();
                break;

            case MI_DEBUG_AGAIN:
                debugMenu.debugAgain();
                break;

            case MI_DEBUG_REMOTE:
                debugMenu.debugRemote();
                break;

            case MI_DEBUG_SHOW_INPUT_TOKENS:
                debugMenu.toggleInputTokens();
                window.refreshMainMenuBar();
                break;

            case MI_EDIT_TEST_RIG:
                debugMenu.showEditTestRig();
                break;
        }
    }

    public void handleMenuPrivate(int itemTag) {
        switch(itemTag) {
            case MI_PRIVATE_UNREGISTER:
                AWPrefs.removeUserRegistration();
                break;
            case MI_SERIALIZE_SD:
                window.syntaxDiagramTab.serializeSyntaxDiagram();
                break;
        }
    }

    public void handleMenuExport(int itemTag) {
        switch(itemTag) {
            case MI_EXPORT_AS_IMAGE:
                actionExport.exportAsImage();
                break;

            case MI_EXPORT_AS_EPS:
                actionExport.exportAsEPS();
                break;

            case MI_EXPORT_AS_DOT:
                actionExport.exportAsDOT();
                break;

            case MI_EXPORT_ALL_AS_IMAGE:
                actionExport.exportAllRulesAsImage();
                break;

            case MI_EXPORT_ALL_AS_EPS:
                actionExport.exportAllRulesAsEPS();
                break;

            case MI_EXPORT_EVENT:
                actionExport.exportEventsAsTextFile();
                break;
        }
    }

    public ActionRefactor getActionRefactor() {
        return actionRefactor;
    }

    public DebugMenu getDebugMenu() {
        return debugMenu;
    }

    public GoToMenu getGoToMenu() {
        return goToMenu;
    }
}
