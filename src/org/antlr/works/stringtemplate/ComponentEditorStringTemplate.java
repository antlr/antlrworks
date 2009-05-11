package org.antlr.works.stringtemplate;

import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.ATEPanelAdapter;
import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.editor.ComponentEditor;
import org.antlr.works.editor.completion.AutoCompletionMenu;
import org.antlr.works.editor.completion.AutoCompletionMenuDelegate;
import org.antlr.works.editor.navigation.GoToHistory;
import org.antlr.works.editor.navigation.GoToRule;
import org.antlr.works.find.FindAndReplace;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stringtemplate.syntax.ATEStringTemplateSyntaxEngine;
import org.antlr.works.stringtemplate.syntax.ATEStringTemplateSyntaxParser;
import org.antlr.works.stringtemplate.element.ElementTemplateRule;
import org.antlr.works.stringtemplate.element.ElementTemplateReference;
import org.antlr.works.stringtemplate.menu.ContextualStringTemplateMenuFactory;
import org.antlr.works.grammar.element.Jumpable;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.xjlib.appkit.undo.XJUndoDelegate;
import org.antlr.xjlib.appkit.undo.XJUndo;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.menu.XJMenuItemCheck;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.util.*;
import java.util.List;

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

public class ComponentEditorStringTemplate extends ComponentEditor implements AutoCompletionMenuDelegate,
    XJUndoDelegate {

    public AutoCompletionMenu autoCompletionMenu;

    public ATEPanel textEditor;

    /* Components */
    public STRulePanel rulesPanel;

    /* Tools */
    public FindAndReplace findAndReplace;
    public GoToHistory goToHistory;
    public GoToRule goToRule;

    public void create() {
        createInterface();

        initAutoCompletion();
        initComponents();
        initTools();

        register();
    }

    protected void createInterface() {
        createTextEditor();
    }

    protected void createTextEditor() {
        textEditor = new ATEPanel(getXJFrame());
        textEditor.setParserEngine(new ATEStringTemplateSyntaxEngine());
        textEditor.setSyntaxColoring(true);
        textEditor.setAnalysisColumnVisible(false);
        applyPrefs();

        textEditor.setDelegate(new TextPanelDelegate());
    }

    public STRulePanel getComponentRules() {
        return rulesPanel;
    }

    public ComponentContainerStringTemplate getContainer() {
        return (ComponentContainerStringTemplate)container;
    }

    protected void initComponents() {
        rulesPanel = new STRulePanel(this);
    }

    protected void initAutoCompletion() {
        if(autoCompletionMenu != null) {
            autoCompletionMenu.close();
        }
        autoCompletionMenu = new AutoCompletionMenu(this, getTextPane(), getXJFrame());
    }

    protected void initTools() {
        goToHistory = new GoToHistory();
        findAndReplace = new FindAndReplace(this);
        goToRule = new GoToRule(this, getXJFrame(), getTextPane());
    }

    protected void register() {
        getXJFrame().registerUndo(this, getTextPane());
    }

    public void assemble() {
        mainPanel.add(textEditor);
    }

    public void applyPrefs() {
        textEditor.setFoldingEnabled(AWPrefs.getFoldingEnabled());
        textEditor.setLineNumberEnabled(AWPrefs.getLineNumberEnabled());
        textEditor.setHighlightCursorLine(AWPrefs.getHighlightCursorEnabled());
        textEditor.getTextPane().setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        textEditor.getTextPane().setTabSize(AWPrefs.getEditorTabSize());
        textEditor.refresh();
        // Need to re-create the auto-completion pop-up because the vstyle is in prefs
        // and requires new key bindings
        initAutoCompletion();
        applyFont();
    }

    public void applyFont() {
        getTextPane().setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        getTextPane().setTabSize(AWPrefs.getEditorTabSize());
    }

    public void close() {
        goToRule.close();
        findAndReplace.close();

        autoCompletionMenu.close();

        rulesPanel = null;

        textEditor.close();

        getXJFrame().unregisterUndo(this);

        super.close();
    }

    public String getText() {
        return textEditor.getText();
    }

    public ATEPanel getTextEditor() {
        return textEditor;
    }

    public void setCaretPosition(int pos) {
        textEditor.setCaretPosition(pos);
    }

    public void goToHistoryRememberCurrentPosition() {
        goToHistory.addPosition(getCaretPosition());
        refreshMainMenuBar();
    }

    public ElementTemplateReference getCurrentReference() {
        return getReferenceAtPosition(getCaretPosition());
    }

    public ElementTemplateReference getReferenceAtPosition(int pos) {
        for (ElementTemplateReference ref : getReferences()) {
            if (ref.containsIndex(pos))
                return ref;
        }
        return null;
    }

    public List<ElementTemplateReference> getReferences() {
        return ((ATEStringTemplateSyntaxParser)getTextEditor().getParserEngine().getParser()).references;
    }

    public List<ATEToken> getDeclarations() {
        return ((ATEStringTemplateSyntaxParser)getTextEditor().getParserEngine().getParser()).decls;
    }

    public void goToBackward() {
        if(goToHistory.canGoBack()) {
            setCaretPosition(goToHistory.getBackPosition(getCaretPosition()));
            refreshMainMenuBar();
        }
    }

    public void goToForward() {
        if(goToHistory.canGoForward()) {
            setCaretPosition(goToHistory.getForwardPosition());
            refreshMainMenuBar();
        }
    }

    public List<ATELine> getLines() {
        return textEditor.getLines();
    }

    public void find() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_FIND_DIALOG);
        findAndReplace.find();
    }

    public FindAndReplace getFindAndReplace() {
        return findAndReplace;
    }

    public GoToRule getGoToRule() {
        return goToRule;
    }

    public boolean goToRule(String ruleName) {
        for (ElementTemplateRule r : getRules()) {
            if (r.name.equals(ruleName)) {
                goToHistoryRememberCurrentPosition();
                setCaretPosition(r.start.getStartIndex());
                return true;
            }
        }
        return false;
    }

    public List<String> getRulesStartingWith(String match) {
        List<String> matches = new ArrayList<String>();
        if (getRules() == null) return matches;

        for (ElementTemplateRule r : getRules()) {
            String rname = r.name.toLowerCase();
            if (rname.startsWith(match) && !matches.contains(r.name))
                matches.add(r.name);
        }
        return matches;
    }

    public void goToDeclaration() {
        Jumpable ref = getCurrentReference();
        container.getActionGoTo().goToDeclaration(ref);
    }

    public void goToDeclaration(final Jumpable ref) {
        if (ref == null) return;
        goToHistoryRememberCurrentPosition();
        int index = getFirstDeclarationPosition(ref.getName());
        if (index != -1) setCaretPosition(index);
    }

    public int getFirstDeclarationPosition(String name) {
        ATEToken token = getFirstDeclaration(name);
        if(token != null) {
            return token.start;
        } else {
            return -1;
        }
    }

    private ATEToken getFirstDeclaration(String name) {
        for(ATEToken decl : getDeclarations()) {
            if(decl.getAttribute().equals(name)) {
                return decl;
            }
        }
        return null;
    }

    public ATEToken getCurrentToken() {
        return getTokenAtPosition(getCaretPosition(), false);
    }

    public List<ElementTemplateRule> getRules() {
        return ((ATEStringTemplateSyntaxParser)getTextEditor().getParserEngine().getParser()).templateRules;
    }

    public ATEToken getTokenAtPosition(int pos, boolean fromRight) {
        List<ATEToken> tokens = getTokens();
        if(tokens == null)
            return null;

        if(fromRight) {
            for (int i = tokens.size()-1; i >= 0; i--) {
                ATEToken token = tokens.get(i);
                if (token.containsIndex(pos))
                    return token;
            }
        } else {
            for (ATEToken token : tokens) {
                if (token.containsIndex(pos))
                    return token;
            }
        }
        return null;
    }

    public void sortRules() {
        rulesPanel.sortRules();
        getComponentRules().refreshRules();
    }

    public JPopupMenu rulesGetContextualMenu() {
        ContextualStringTemplateMenuFactory factory = getContainer().createContextualStringTemplateMenuFactory();
        XJMenuItemCheck item = (XJMenuItemCheck) factory.addItem(ComponentContainerStringTemplateMenu.MI_SORT_RULES);
        item.setSelected(rulesPanel.isRulesSorted());

        return factory.menu;
    }

    public void componentDocumentContentChanged() {
        // Called when the document associated file has changed on the disk
        if(!isFileExists()) {
            XJAlert.display(getWindowContainer(), "Warning", "The document cannot be found on the disk anymore.");
            return;
        }

        if(AWPrefs.isAlertFileChangesDetected()) {
            XJAlert alert = XJAlert.createInstance();
            alert.setDisplayDoNotShowAgainButton(true);
            int result = alert.showCustom(getWindowContainer(), "File Changes",
                    "The file \""+getFileName()+"\" changed on the disk. Do you want to reload it?",
                    "Cancel", "Reload", 1, 0);
            AWPrefs.setAlertFileChangesDetected(!alert.isDoNotShowAgain());
            if(result == 0) {
                return;
            }
        }

        int oldCursorPosition = textEditor.getCaretPosition();
        try {
            getDocument().reload();
        } catch (Exception e) {
            e.printStackTrace();
            XJAlert.display(getWindowContainer(), "Error Reloading Document", "An error occurred when reloading the document:\n"+e.toString());
        }
        textEditor.setCaretPosition(Math.min(oldCursorPosition, getText().length()), true, AWPrefs.getSmoothScrolling());
    }

    public void loadText(String text) {
        textEditor.loadText(text);
    }

    public void beginGroupChange(String name) {
        disableTextPane(false);
        beginTextPaneUndoGroup(name);
    }

    public void endGroupChange() {
        endTextPaneUndoGroup();
        enableTextPane(false);
        textEditor.parse();
        changeDone();
    }

    public void enableTextPane(boolean undo) {
        textEditor.setEnableRecordChange(true);
        if(undo)
            enableTextPaneUndo();
    }

    public void disableTextPane(boolean undo) {
        textEditor.setEnableRecordChange(false);
        if(undo)
            disableTextPaneUndo();
    }

    public void beginTextPaneUndoGroup(String name) {
        XJUndo undo = getXJFrame().getUndo(getTextPane());
        if(undo != null)
            undo.beginUndoGroup(name);
    }

    public void endTextPaneUndoGroup() {
        XJUndo undo = getXJFrame().getUndo(getTextPane());
        if(undo != null)
            undo.endUndoGroup();
    }

    public void disableTextPaneUndo() {
        textEditor.disableUndo();
    }

    public void enableTextPaneUndo() {
        textEditor.enableUndo();
    }

    public void undoManagerWillUndo(boolean redo) {
    }

    public void changeDone() {
        getDocument().changeDone();
    }

    public void undoManagerDidUndo(boolean redo) {
        changeDone();
    }

    @Override
    public void notificationPrefsChanged() {
        applyPrefs();
    }

    public List<String> autoCompletionMenuGetMatchingWordsForPartialWord(String partialWord) {
        partialWord = partialWord.toLowerCase();
        List<String> matchingArgs = new ArrayList<String>();
        List<String> matchingRules = new ArrayList<String>();
        List<ElementTemplateRule> rules = ((ATEStringTemplateSyntaxParser)textEditor.getParserEngine().getParser()).templateRules;
        int index = getCaretPosition();

        if (rules == null || rules.size() == 0) return null;

        for (ElementTemplateRule rule : rules) {
            if (rule.name.toLowerCase().startsWith(partialWord) && !matchingRules.contains(rule.name))
                matchingRules.add(rule.name);

            if (rule.containsIndex(index)) {
                for (ATEToken arg : rule.args) {
                    if (arg.getAttribute().toLowerCase().startsWith(partialWord) && !matchingArgs.contains(arg.getAttribute()))
                        matchingArgs.add(arg.getAttribute());
                }
            }
        }
        Collections.sort(matchingRules);
        Collections.sort(matchingArgs);

        // start with template args, then rule names
        List<String> ret = new ArrayList<String>();
        ret.addAll(matchingArgs);
        ret.addAll(matchingRules);
        return ret;
    }

    public void autoCompletionMenuWillDisplay() {
    }

    public synchronized boolean isFileExists() {
        String path = getFilePath();
        if(path == null) {
            return false;
        } else {
            File f = new File(path);
            return f.exists();
        }
    }

    public synchronized boolean isFileWritable() {
        String path = getFilePath();
        if(path == null) {
            return true;
        } else {
            File f = new File(path);
            return !f.exists() || f.canWrite();
        }
    }

    public synchronized String getFilePath() {
        return getDocument().getDocumentPath();
    }

    public synchronized String getFileName() {
        return getDocument().getDocumentName();
    }

    public Container getWindowContainer() {
        return getXJFrame().getJavaContainer();
    }

    public void print() {
        try {
            textEditor.print();
        } catch (PrinterException e) {
            XJAlert.display(getWindowContainer(), "Print Error", "An error occurred while printing:\n"+e.toString());
        }
    }

    private class TextPanelDelegate extends ATEPanelAdapter {

        @Override
        public void ateChangeUpdate(int offset, int length, boolean insert) {
            // Indicate to the document that a change has been done. This will
            // automatically trigger an alert when the window is closed to ask
            // the user if he wants to save the document
            getDocument().changeDone();
        }

        @Override
        public void ateEngineDidParse() {
            getComponentRules().refreshRules();
        }

        @Override
        public void ateCaretUpdate(int index) {
            // Update the auto-completion list
            autoCompletionMenu.updateAutoCompleteList();
        }

        @Override
        public void ateInvokePopUp(Component component, int x, int y) {
            JPopupMenu m = container.getContextualMenu(textEditor.getTextIndexAtPosition(x, y));
            if(m != null)
                m.show(component,  x, y);
        }
    }
}
