package org.antlr.works.stringtemplate;

import org.antlr.works.ate.*;
import org.antlr.works.ate.syntax.misc.*;
import org.antlr.works.editor.*;
import org.antlr.works.editor.completion.*;
import org.antlr.works.editor.navigation.*;
import org.antlr.works.find.*;
import org.antlr.works.grammar.element.Jumpable;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.works.menu.*;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.stringtemplate.element.*;
import org.antlr.works.stringtemplate.menu.ContextualStringTemplateMenuFactory;
import org.antlr.works.stringtemplate.syntax.*;
import org.antlr.xjlib.appkit.frame.XJWindow;
import org.antlr.xjlib.appkit.menu.*;
import org.antlr.xjlib.appkit.undo.*;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.foundation.XJUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.util.*;
import java.util.List;

/*

[The "BSD licence"]
Copyright (c) 2005-08 Jean Bovet
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
public class STWindow extends XJWindow
        implements AutoCompletionMenuDelegate, XJUndoDelegate,
        GoToRuleDelegate, FindAndReplaceDelegate, FindMenuDelegate,
        GoToMenuDelegate {

    private STWindowMenu stringTemplateMenu;

    private STWindowToolbar toolbar;

    private JPanel mainPanel;

    private final java.util.List<GrammarWindowTab> tabs = new ArrayList<GrammarWindowTab>();

    private final Set<String> loadedStringTemplateFileNames = new HashSet<String>();

    public AutoCompletionMenu autoCompletionMenu;

    public ATEPanel textEditor;

    /* Components */
    public STRulePanel stRulesPanel;

    /* Tools */
    public FindAndReplace findAndReplace;
    public GoToHistory goToHistory;
    public GoToRule goToRule;

    public STWindow() {

    }

    @Override
    public void awake() {
        stringTemplateMenu = new STWindowMenu(this);
        toolbar = new STWindowToolbar(this);

//        toolbarPanel = new JPanel(new BorderLayout());
//        toolbarPanel.setBorder(null);

        createTextEditor();

        stRulesPanel = new STRulePanel(this);

        create();
        assemble();
        super.awake();
    }

    public void setContentPanel(JPanel panel) {
        getContentPane().add(panel);
        pack();
    }

    public void assemble() {
        JSplitPane verticalSplit = new JSplitPane();
        verticalSplit.setBorder(null);
        verticalSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        verticalSplit.setRightComponent(textEditor);
        verticalSplit.setLeftComponent(stRulesPanel);
        verticalSplit.setContinuousLayout(false);
        verticalSplit.setOneTouchExpandable(true);
        verticalSplit.setResizeWeight(0.25);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(null);
		mainPanel.add(toolbar.getToolbar(), BorderLayout.NORTH);
        //mainPanel.add(toolbarPanel, BorderLayout.NORTH);
        mainPanel.add(verticalSplit, BorderLayout.CENTER);

        setContentPanel(mainPanel);
    }

    public void refreshMainMenuBar() {
        getMainMenuBar().refreshState();
    }

    public boolean loadStringTemplate(String name) {
        String fileName = name+".st";

        String folder = getDocument().getDocumentFolder();
        if(folder == null) {
            return false;
        }

        String file = XJUtils.concatPath(folder, fileName);
        if(!new File(file).exists()) {
            return false;
        }

        if(loadedStringTemplateFileNames.contains(fileName)) return true;
        loadedStringTemplateFileNames.add(fileName);

//        STDocumentFactory factory = new STDocumentFactory(STWindow.class);
//        STDocument doc = factory.createDocument(this);
//        DocumentContainer window = doc.getEditor();
//
//        doc.awake();
//        try {
//            doc.load(file);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        window.addDocument(doc);
//        addStringTemplate(window);
//
        return true;
    }

    public STWindowToolbar getToolbar() {
        return toolbar;
    }

    public ActionRefactor getActionRefactor() {
        return stringTemplateMenu.getActionRefactor();
    }

    public GoToMenu getActionGoTo() {
        return stringTemplateMenu.getActionGoTo();
    }

    public ContextualStringTemplateMenuFactory createContextualStringTemplateMenuFactory() {
        return new ContextualStringTemplateMenuFactory(stringTemplateMenu);
    }

    public JPopupMenu getContextualMenu(int textIndex) {
        return stringTemplateMenu.getContextualMenu(textIndex);
    }

    public STWindowMenu getComponentContainerStringTemplateMenu() {
        return stringTemplateMenu;
    }

    public boolean componentDocumentWillSave() {
        AWPrefs.setLastSavedDocument(getFilePath());
        if(!isFileWritable()) {
            XJAlert.display(getJavaContainer(), "Cannot Save", "This file cannot be saved. Check the file permission on the disk and try again.");
            return false;
        }
        return true;
    }

    @Override
    public boolean close(boolean force) {
		if(!super.close(force)) return false;
        goToRule.close();

        autoCompletionMenu.close();

        textEditor.close();

        unregisterUndo(this);

        stringTemplateMenu.close();
        toolbar.close();
		return true;
    }

//    public void createFile(String name) {
//        String path = getEditor().getDocument().getDocumentFolder();
//        String file = XJUtils.concatPath(path, name+".stg");
//        String content = "group "+name+";\n";
//        try {
//            XJUtils.writeStringToFile(content, file);
//        } catch (IOException e) {
//            XJAlert.display(window.getJavaContainer(), "Create File Error",
//                    "Cannot create file '"+name+"' because:\n"+e.toString());
//            return;
//        }
//        selectEditor(name);
//    }


    public void customizeFileMenu(XJMenu menu) {
        stringTemplateMenu.customizeFileMenu(menu);
    }

    public void customizeMenuBar(XJMainMenuBar menubar) {
        stringTemplateMenu.customizeMenuBar(menubar);
    }

    public void menuItemState(XJMenuItem item) {
        stringTemplateMenu.menuItemState(item);
    }

    public void handleMenuSelected(XJMenu menu) {
    }

    public void windowActivated() {
        for(GrammarWindowTab et : tabs) {
            et.editorActivated();
        }
    }


    public int getSimilarTab(GrammarWindowTab tab) {
        for (int i = 0; i < tabs.size(); i++) {
            GrammarWindowTab t = tabs.get(i);
            if(t.getTabName().equals(tab.getTabName()))
                return i;
        }
        return -1;
    }

    public void setComponent(JPanel panel, GrammarWindowTab tab) {
        setComponent(panel, tab.getTabComponent());
    }

    public void setComponent(JPanel panel, Component c) {
        panel.removeAll();
        panel.add(c);
        panel.revalidate();
        panel.repaint();
    }

    public void create() {
        initAutoCompletion();
        initTools();

        register();
    }

    protected void createTextEditor() {
        textEditor = new ATEPanel(this);
        textEditor.setParserEngine(new ATEStringTemplateSyntaxEngine());
        textEditor.setSyntaxColoring(true);
        textEditor.setAnalysisColumnVisible(false);
        applyPrefs();

        textEditor.setDelegate(new TextPanelDelegate());
    }

    protected void initAutoCompletion() {
        if(autoCompletionMenu != null) {
            autoCompletionMenu.close();
        }
        autoCompletionMenu = new AutoCompletionMenu(this, getTextPane(), this);
    }

    protected void initTools() {
        goToHistory = new GoToHistory();
        findAndReplace = new FindAndReplace(this);
        goToRule = new GoToRule(this, this, getTextPane());
    }

    protected void register() {
        registerUndo(this, getTextPane());
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

    public String getText() {
        return textEditor.getText();
    }

    public void setText(String text) {
        textEditor.setText(text);
    }

    public ATEPanel getTextEditor() {
        return textEditor;
    }

    public ATETextPane getTextPane() {
        return textEditor.getTextPane();
    }

    public void setCaretPosition(int pos) {
        textEditor.setCaretPosition(pos);
    }

    public int getCaretPosition() {
        return textEditor.getCaretPosition();
    }

    public GrammarEngine getGrammarEngine() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        getActionGoTo().goToDeclaration(ref);
    }

    public void goToDeclaration(final Jumpable ref) {
        if (ref == null) return;
        goToHistoryRememberCurrentPosition();
        int index = getFirstDeclarationPosition(ref.getName());
        if (index != -1) setCaretPosition(index);
    }

    public Set<Integer> getBreakpoints() {
        // todo not used
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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

    public EditorRules getEditorRules() {
        return null;
    }

    public List<ElementTemplateRule> getRules() {
        return ((ATEStringTemplateSyntaxParser)getTextEditor().getParserEngine().getParser()).templateRules;
    }

    public void addUsagesTab(Usages usage) {
        // really used?
    }

    public void selectTextRange(int startIndex, int endIndex) {
        textEditor.selectTextRange(startIndex, endIndex);
    }

    public List<ATEToken> getTokens() {
        return textEditor.getTokens();
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

    public boolean isRulesSorted() {
        return stRulesPanel.isRulesSorted();
    }

    public void toggleRulesSorting() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_TOGGLE_RULE_SORT);
        stRulesPanel.toggleSorting();
        getToolbar().updateStates();
        stRulesPanel.refreshRules();
    }

    public JPopupMenu rulesGetContextualMenu() {
        ContextualStringTemplateMenuFactory factory = createContextualStringTemplateMenuFactory();
        XJMenuItemCheck item = (XJMenuItemCheck) factory.addItem(STWindowMenu.MI_SORT_RULES);
        item.setSelected(isRulesSorted());

        return factory.menu;
    }

    public void componentDocumentContentChanged() {
        // Called when the document associated file has changed on the disk
        if(!isFileExists()) {
            XJAlert.display(getJavaContainer(), "Warning", "The document cannot be found on the disk anymore.");
            return;
        }

        if(AWPrefs.isAlertFileChangesDetected()) {
            XJAlert alert = XJAlert.createInstance();
            alert.setDisplayDoNotShowAgainButton(true);
            int result = alert.showCustom(getJavaContainer(), "File Changes",
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
            XJAlert.display(getJavaContainer(), "Error Reloading Document", "An error occurred when reloading the document:\n"+e.toString());
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
        XJUndo undo = getUndo(getTextPane());
        if(undo != null)
            undo.beginUndoGroup(name);
    }

    public void endTextPaneUndoGroup() {
        XJUndo undo = getUndo(getTextPane());
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

    public void print() {
        try {
            textEditor.print();
        } catch (PrinterException e) {
            XJAlert.display(getJavaContainer(), "Print Error", "An error occurred while printing:\n"+e.toString());
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
        public void ateEngineAfterParsing() {
            stRulesPanel.refreshRules();
        }

        @Override
        public void ateCaretUpdate(int index) {
            // Update the auto-completion list
            autoCompletionMenu.updateAutoCompleteList();
        }

        @Override
        public void ateInvokePopUp(Component component, int x, int y) {
            JPopupMenu m = getContextualMenu(textEditor.getTextIndexAtPosition(x, y));
            if(m != null)
                m.show(component,  x, y);
        }
    }

}
