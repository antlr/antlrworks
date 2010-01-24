/*

[The "BSD licence"]
Copyright (c) 2009
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

package org.antlr.works.stringtemplate;

import org.antlr.works.IDE;
import org.antlr.works.menu.ActionRefactor;
import org.antlr.works.menu.FindMenu;
import org.antlr.works.menu.GoToMenu;
import org.antlr.works.menu.STRefactorMenu;
import org.antlr.works.stringtemplate.menu.ContextualStringTemplateMenuFactory;
import org.antlr.xjlib.appkit.menu.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

public class STWindowMenu implements XJMenuItemDelegate {

    public static final int MI_PRINT = 5;

    // Find
    public static final int MI_FIND = 30;
    public static final int MI_FIND_NEXT = 31;
    public static final int MI_FIND_PREV = 32;
    public static final int MI_FIND_TOKEN = 33;

    // Go To
    public static final int MI_GOTO_RULE = 40;
    public static final int MI_GOTO_DECLARATION = 41;
    public static final int MI_GOTO_LINE = 42;
    public static final int MI_GOTO_CHARACTER = 43;
    public static final int MI_GOTO_BACK = 44;
    public static final int MI_GOTO_FORWARD = 45;

    // Refactor
    public static final int MI_RENAME = 61;
    public static final int MI_INLINE_RULE = 69;

    // Help
    public static final int MI_SUBMIT_STATS = 100;
    public static final int MI_SEND_FEEDBACK = 101;
    public static final int MI_CHECK_UPDATES = 102;

    // Sort
    public static final int MI_SORT_RULES = 110;

    private FindMenu actionFind;
    private GoToMenu actionGoTo;
    private ActionRefactor actionRefactor;

    private STWindow window;

    /** The resource bundle used to get localized strings */
    private static ResourceBundle resourceBundle = IDE.getMenusResourceBundle();

    public STWindowMenu(STWindow window) {
        this.window = window;

        actionFind = new FindMenu(window);
        actionGoTo = new GoToMenu(window);
        actionRefactor = new STRefactorMenu(window);
    }

    public void close() {
        window = null;
    }

    public void customizeFileMenu(XJMenu menu) {
        menu.insertItemAfter(new XJMenuItem(resourceBundle.getString("menu.item.print"), KeyEvent.VK_P, MI_PRINT, this), XJMainMenuBar.MI_SAVEAS);
        menu.insertSeparatorAfter(XJMainMenuBar.MI_SAVEAS);
    }

    public void customizeMenuBar(XJMainMenuBar menubar) {
        createFindMenu(menubar);
        createGoToMenu(menubar);
        createRefactorMenu(menubar);
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

        menubar.addCustomMenu(menu);
    }

    private void createRefactorMenu(XJMainMenuBar menubar) {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle(resourceBundle.getString("menu.title.refactor"));
        menu.addItem(createMenuItem(MI_RENAME));
//        menu.addItem(createMenuItem(MI_REPLACE_LITERAL_WITH_TOKEN_LABEL));
//        menu.addSeparator();
//        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.removeLeftRecursion"), MI_REMOVE_LEFT_RECURSION, this));
//        menu.addItem(new XJMenuItem(resourceBundle.getString("menu.item.removeAllLeftRecursion"), MI_REMOVE_ALL_LEFT_RECURSION, this));
//        menu.addSeparator();
//        menu.addItem(createMenuItem(MI_EXTRACT_RULE));
//        menu.addItem(createMenuItem(MI_INLINE_RULE));
//        menu.addSeparator();
//
//        XJMenu literals = new XJMenu();
//        literals.setTitle(resourceBundle.getString("menu.title.convertLiterals"));
//        literals.addItem(new XJMenuItem(resourceBundle.getString("menu.item.convertToSingleQuote"), MI_LITERAL_TO_SINGLEQUOTE, this));
//        literals.addItem(new XJMenuItem(resourceBundle.getString("menu.item.convertToDoubleQuote"), MI_LITERAL_TO_DOUBLEQUOTE, this));
//        literals.addItem(new XJMenuItem(resourceBundle.getString("menu.item.convertToCStyleQuote"), MI_LITERAL_TO_CSTYLEQUOTE, this));
//
//        menu.addItem(literals);

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
            case MI_GOTO_RULE:
                item = new XJMenuItem(contextual? resourceBundle.getString("contextual.item.gotoRule") : resourceBundle.getString("menu.item.gotoRule"), KeyEvent.VK_B, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_GOTO_RULE, this);
                break;

            case MI_GOTO_DECLARATION:
                item = new XJMenuItem(contextual? resourceBundle.getString("contextual.item.goToDeclaration") : resourceBundle.getString("menu.item.gotoDeclaration"), KeyEvent.VK_B, MI_GOTO_DECLARATION, this);
                break;

            case MI_RENAME:
                item = new XJMenuItem(resourceBundle.getString("menu.item.rename"), KeyEvent.VK_F6, Event.SHIFT_MASK, MI_RENAME, this);
                break;

            case MI_INLINE_RULE:
                item = new XJMenuItem(resourceBundle.getString("menu.item.inlineRule"), MI_INLINE_RULE, this);
                break;

            case MI_SORT_RULES:
                item = new XJMenuItemCheck(resourceBundle.getString("menu.item.sortRules"), MI_SORT_RULES, this, false);
                break;

        }
        return item;
    }

    public void menuItemState(final XJMenuItem item) {
        switch(item.getTag()) {
            case XJMainMenuBar.MI_CUT:
            case XJMainMenuBar.MI_PASTE:
                item.setEnabled(window.isFileWritable());
                break;
            case MI_GOTO_BACK:
                item.setEnabled(window.goToHistory.canGoBack());
                break;
            case MI_GOTO_FORWARD:
                item.setEnabled(window.goToHistory.canGoForward());
                break;
        }
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        handleMenuFile(item.getTag());
        handleMenuFind(item.getTag());
        handleMenuRefactor(item.getTag());
        handleMenuGoTo(item.getTag());
        handleSortRules(item.getTag());
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
        }
    }

    public void handleMenuRefactor(int itemTag) {
        switch(itemTag) {
            case MI_RENAME:
                actionRefactor.rename();
                break;

            case MI_INLINE_RULE:
                actionRefactor.inlineRule();
                break;
        }
    }

    public void handleMenuGoTo(int itemTag) {
        switch(itemTag) {
            case MI_GOTO_RULE:
                actionGoTo.goToRule();
                break;

            case MI_GOTO_DECLARATION:
                actionGoTo.goToDeclaration();
                break;

            case MI_GOTO_LINE:
                actionGoTo.goToLine();
                break;

            case MI_GOTO_CHARACTER:
                actionGoTo.goToCharacter();
                break;

            case MI_GOTO_BACK:
                actionGoTo.goToBackward();
                break;

            case MI_GOTO_FORWARD:
                actionGoTo.goToForward();
                break;
        }
    }

    public void handleSortRules(int itemTag) {
        switch(itemTag) {
            case MI_SORT_RULES:
                window.toggleRulesSorting();
                break;
        }
    }

    public ActionRefactor getActionRefactor() {
        return actionRefactor;
    }

    public GoToMenu getActionGoTo() {
        return actionGoTo;
    }

    public JPopupMenu getContextualMenu(int textIndex) {
        boolean overReference = window.getCurrentReference() != null;
        boolean overToken = window.getCurrentToken() != null;
        boolean overSelection = window.getTextPane().getSelectionStart() != window.getTextPane().getSelectionEnd();

        ContextualStringTemplateMenuFactory factory = new ContextualStringTemplateMenuFactory(this);
        factory.addItem(MI_GOTO_RULE);
        if(overReference)
            factory.addItem(MI_GOTO_DECLARATION);

        factory.addSeparator();
        if(overToken)
            factory.addItem(MI_RENAME);

        if(overToken) {
            factory.addSeparator();
            if(overSelection)
                factory.addItem(MI_FIND_TOKEN);
            //factory.addItem(MI_FIND_USAGE);
        }

        return factory.menu;
    }
}