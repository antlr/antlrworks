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

import edu.usfca.xj.appkit.frame.XJWindow;
import edu.usfca.xj.appkit.menu.XJMainMenuBar;
import edu.usfca.xj.appkit.menu.XJMenu;
import edu.usfca.xj.appkit.menu.XJMenuItem;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.editor.actions.*;
import org.antlr.works.editor.ate.ATEGutter;
import org.antlr.works.editor.ate.ATETextPane;
import org.antlr.works.editor.autocompletion.AutoCompletionMenu;
import org.antlr.works.editor.autocompletion.AutoCompletionMenuDelegate;
import org.antlr.works.editor.autocompletion.TemplateRules;
import org.antlr.works.editor.find.FindAndReplace;
import org.antlr.works.editor.helper.*;
import org.antlr.works.editor.idea.*;
import org.antlr.works.editor.rules.Rules;
import org.antlr.works.editor.rules.RulesDelegate;
import org.antlr.works.editor.tips.TipsManager;
import org.antlr.works.editor.tips.TipsOverlay;
import org.antlr.works.editor.tips.TipsProvider;
import org.antlr.works.editor.tool.TColorize;
import org.antlr.works.editor.tool.TGoToRule;
import org.antlr.works.editor.tool.TGrammar;
import org.antlr.works.editor.undo.Undo;
import org.antlr.works.editor.visual.Visual;
import org.antlr.works.interpreter.Interpreter;
import org.antlr.works.parser.*;
import org.antlr.works.stats.Statistics;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.*;
import java.util.List;

public class EditorWindow extends XJWindow implements ThreadedParserObserver,
     AutoCompletionMenuDelegate, RulesDelegate, EditorProvider, IdeaActionDelegate,
     IdeaManagerDelegate, IdeaProvider, TipsProvider
{
    public ThreadedParser parser;
    public KeyBindings keyBindings;
    public AutoCompletionMenu autoCompletionMenu;
    public TGoToRule goToRule;
    public FindAndReplace findAndReplace;
    public TColorize colorize;
    public TGrammar grammar;
    public TemplateRules templateRules;
    public IdeaManager ideaManager;
    public TipsManager tipsManager;

    public BreakpointManager breakpointManager;
    public FoldingManager foldingManager;
    public UnderlyingManager underlyingManager;

    public Rules rules;
    public Visual visual;
    public Interpreter interpreter;
    public Debugger debugger;
    public Console console;
    public GoToHistory goToHistory;

    private Map undos = new HashMap();

    public String lastSelectedRule;

    private boolean windowFirstDisplay = true;

    public EditorGUI editorGUI;
    protected EditorCache editorCache;
    protected EditorMenu editorMenu;

    protected ActionsEdit actionsEdit;
    protected ActionsView actionsView;
    protected ActionsFind actionsFind;
    protected ActionsGrammar actionsGrammar;
    protected ActionsRefactor actionsRefactor;
    protected ActionsGoTo actionsGoTo;
    protected ActionsGenerate actionsGenerate;
    protected ActionsRun actionsRun;
    protected ActionsSCM actionsSCM;
    protected ActionsExport actionsExport;
    protected ActionsHelp actionsHelp;

    protected Persistence persistence;

    public EditorWindow() {

        console = new Console(this);
        console.makeCurrent();

        goToHistory = new GoToHistory();

        editorGUI = new EditorGUI(this);
        editorCache = new EditorCache();
        editorMenu = new EditorMenu(this);

        actionsEdit = new ActionsEdit(this);
        actionsView = new ActionsView(this);
        actionsFind = new ActionsFind(this);
        actionsGrammar = new ActionsGrammar(this);
        actionsRefactor = new ActionsRefactor(this);
        actionsGoTo = new ActionsGoTo(this);
        actionsGenerate = new ActionsGenerate(this);
        actionsRun = new ActionsRun(this);
        actionsSCM = new ActionsSCM(this);
        actionsExport = new ActionsExport(this);
        actionsHelp = new ActionsHelp(this);

        persistence = new Persistence(this);

        parser = new ThreadedParser(this);
        parser.addObserver(this);

        editorGUI.createInterface();
        breakpointManager = new BreakpointManager(this);
        editorGUI.textEditor.setBreakpointManager(breakpointManager);
        foldingManager = new FoldingManager(this);
        editorGUI.textEditor.setFoldingManager(foldingManager);

        underlyingManager = new UnderlyingManager(this);
        editorGUI.textEditor.setUnderlyingManager(underlyingManager);

        visual = new Visual(this);
        interpreter = new Interpreter(this);
        debugger = new Debugger(this);

        keyBindings = new KeyBindings(getTextPane());

        autoCompletionMenu = new AutoCompletionMenu(this, getTextPane(), getJFrame());
        templateRules = new TemplateRules(this, getTextPane(), getJFrame());
        goToRule = new TGoToRule(this, getJFrame(), getTextPane());
        findAndReplace = new FindAndReplace(this);

        ideaManager = new IdeaManager();
        ideaManager.setOverlay(new IdeaOverlay(this, getJFrame(), getTextPane()));
        ideaManager.addProvider(this);
        ideaManager.setDelegate(this);

        tipsManager = new TipsManager();
        tipsManager.setOverlay(new TipsOverlay(this, getJFrame(), getTextPane()));
        tipsManager.addProvider(this);

        rules = new Rules(parser, getTextPane(), editorGUI.rulesTree);
        grammar = new TGrammar(this);

        rules.setDelegate(this);
        rules.setKeyBindings(keyBindings);

        visual.setParser(parser);

        colorize = new TColorize(this);

        getTabbedPane().addTab("Syntax Diagram", visual.getContainer());
        getTabbedPane().addTab("Interpreter", interpreter.getContainer());
        getTabbedPane().addTab("Debugger", debugger.getContainer());
        getTabbedPane().addTab("Console", console.getContainer());

        selectVisualizationTab();

        registerUndo(new Undo(editorGUI), getTextPane());
    }

    public void becomingVisibleForTheFirstTime() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // @todo why ?
                editorGUI.rulesTextSplitPane.setDividerLocation(0.3);
            }
        });
        textPaneRequestFocusLater();
        editorGUI.updateInformation();
        editorGUI.updateCursorInfo();
        actionsSCM.setSilent(true);
        actionsSCM.queryFileStatus();
    }

    public void close() {
        ideaManager.close();
        editorGUI.close();
        editorMenu.close();
        debugger.close();
        visual.close();
        super.close();
    }

    public void selectVisualizationTab() {
        getTabbedPane().setSelectedIndex(0);
    }

    public void selectInterpreterTab() {
        getTabbedPane().setSelectedIndex(1);
        editorGUI.makeBottomComponentVisible();
    }

    public void selectDebuggerTab() {
        getTabbedPane().setSelectedIndex(2);
        editorGUI.makeBottomComponentVisible();
    }

    public void registerUndo(Undo undo, JTextPane component) {
        undo.bindTo(component);
        editorGUI.registerUndo(undo, component);
        undos.put(component, undo);
    }

    public Undo getCurrentUndo() {
        // Use the permanent focus owner because on Windows/Linux, an opened menu become
        // the current focus owner (non-permanent).
        return (Undo)undos.get(KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner());
    }

    public Undo getUndo(Object object) {
        return (Undo)undos.get(object);
    }

    public ATETextPane getTextPane() {
        return editorGUI.textEditor.textPane;
    }

    public ATEGutter getGutter() {
        return editorGUI.textEditor.gutter;
    }

    public JTabbedPane getTabbedPane() {
        return editorGUI.viewTabbedPane;
    }

    public void textPaneRequestFocusLater() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jFrame.setVisible(true);
                getTextPane().requestFocus();
            }
        });
    }

    public void toggleAutoIndent() {
        editorGUI.setAutoIndent(!editorGUI.autoIndent());
    }

    public void toggleSyntaxColoring() {
        colorize.setEnable(!colorize.isEnable());
        if(colorize.isEnable()) {
            colorize.reset();
            colorize.colorize();
        } else
            colorize.removeColorization();

        Statistics.shared().recordEvent(Statistics.EVENT_TOGGLE_SYNTAX_COLORING);
    }

    public void toggleSyntaxDiagram() {
        visual.setEnable(!visual.isEnable());
        if(visual.isEnable()) {
            visual.setText(getText(), getFileName());
            updateVisualization(false);
        }
        Statistics.shared().recordEvent(Statistics.EVENT_TOGGLE_SYNTAX_DIAGRAM);
    }

    public void toggleNFAOptimization() {
        visual.toggleNFAOptimization();
        updateVisualization(false);
        Statistics.shared().recordEvent(Statistics.EVENT_TOGGLE_NFA_OPTIMIZATION);
    }

    public void toggleIdeas() {
        ideaManager.setEnabled(!ideaManager.enabled());
    }

    public void toggleUnderlying() {
        editorGUI.textEditor.setUnderlying(!editorGUI.textEditor.isUnderlying());
        editorGUI.textEditor.refresh();
    }

    public void toggleTips() {
        tipsManager.setEnabled(!tipsManager.enabled());
    }

    public void toggleAnalysis() {
        editorGUI.textEditor.toggleAnalysis();
    }

    protected void adjustTokens(int location, int length) {
        // @todo see with colorize if the colorize thread
        // can use this calculation rather than doing it again
        // We have to do shift of every offset past the location in order
        // for collapsed view to be correctly rendered (the rule has to be
        // immediately at the right position and cannot wait for the
        // parser to finish)

        List tokens = getTokens();

        // This may interfer with TColoriz thread which will also
        // offset all tokens
        for(int t=0; t<tokens.size(); t++) {
            Token token = (Token) tokens.get(t);
            if(token.getStartIndex() > location) {
                token.offsetPositionBy(length);
            }
        }
    }

    public void changeUpdate() {
        changeUpdate(-1, -1, false);
    }

    public void changeUpdate(int offset, int length, boolean insert) {
        if(insert) {
            editorGUI.immediateColorization.colorize(offset, length);
            editorGUI.autoIndent.indent(offset, length);
        }

        changeDone();

        adjustTokens(offset, length);
        getGutter().markDirty();

        parser.parse();
        visual.cancelDrawingProcess();

        colorize.setColorizeLocation(offset, length);
    }

    public void beginGroupChange(String name) {
        disableTextPane(false);
        beginTextPaneUndoGroup(name);
    }

    public void endGroupChange() {
        endTextPaneUndoGroup();
        enableTextPane(false);
        colorize.reset();
        parser.parse();
        changeDone();
    }

    public void enableTextPane(boolean undo) {
        editorGUI.textEditor.textPaneListener.enable();
        if(undo)
            enableTextPaneUndo();
    }

    public void disableTextPane(boolean undo) {
        editorGUI.textEditor.textPaneListener.disable();
        if(undo)
            disableTextPaneUndo();
    }

    public void beginTextPaneUndoGroup(String name) {
        Undo undo = getUndo(getTextPane());
        if(undo != null)
            undo.beginUndoGroup(name);
    }

    public void endTextPaneUndoGroup() {
        Undo undo = getUndo(getTextPane());
        if(undo != null)
            undo.endUndoGroup();
    }

    public void enableTextPaneUndo() {
        Undo undo = getUndo(getTextPane());
        if(undo != null)
            undo.enableUndo();
    }

    public void disableTextPaneUndo() {
        Undo undo = getUndo(getTextPane());
        if(undo != null)
            undo.disableUndo();
    }

    public void setLoadedText(String text) {
        disableTextPane(true);
        try {
            getTextPane().setText(text);
            getTextPane().setCaretPosition(0);
            getTextPane().moveCaretPosition(0);
            getTextPane().getCaret().setSelectionVisible(true);
            grammarChanged();
            parser.parse();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            enableTextPane(true);
        }
    }

    public synchronized String getText() {
        if(editorCache.getString(EditorCache.CACHE_TEXT) == null)
            editorCache.setObject(EditorCache.CACHE_TEXT, getTextPane().getText());
        return editorCache.getString(EditorCache.CACHE_TEXT);
    }

    public synchronized String getFilePath() {
        return getDocument().getDocumentPath();
    }

    public synchronized String getFileName() {
        return getDocument().getDocumentName();
    }

    public Container getWindowContainer() {
        return jFrame;
    }

    public List getActions() {
        return parser.getActions();
    }

    public List getTokens() {
        return parser.getTokens();
    }

    public List getLines() {
        return parser.getLines();
    }

    public int getLineIndexAtTextPosition(int pos) {
        List lines = getLines();
        if(lines == null)
            return -1;

        for(int i=0; i<lines.size(); i++) {
            Line line = (Line)lines.get(i);
            if(line.position > pos) {
                return i-1;
            }
        }
        return lines.size()-1;
    }

    public Point getLineTextPositionsAtTextPosition(int pos) {
        return getLineTextPositionsAtLineIndex(getLineIndexAtTextPosition(pos));
    }

    public Point getLineTextPositionsAtLineIndex(int lineIndex) {
        List lines = getLines();
        if(lineIndex == -1 || lines == null)
            return null;

        Line startLine = (Line)lines.get(lineIndex);
        int start = startLine.position;
        if(lineIndex+1 >= lines.size()) {
            return new Point(start, getTextPane().getDocument().getLength()-1);
        } else {
            Line endLine = (Line)lines.get(lineIndex+1);
            int end = endLine.position;
            return new Point(start, end-1);
        }
    }

    public void goToHistoryRememberCurrentPosition() {
        goToHistory.addPosition(getCaretPosition());
        getMainMenuBar().refreshState();
    }

    public void changeDone() {
        grammarChanged();
        editorCache.invalidate();
        getDocument().changeDone();
    }

    public void grammarChanged() {
        interpreter.grammarChanged();
        debugger.grammarChanged();
        actionsGenerate.generateCode.grammarChanged();
    }

    public void selectTextRange(int start, int end) {
        editorGUI.textEditor.selectTextRange(start, end);
    }

    public Token getCurrentToken() {
        return getTokenAtPosition(getCaretPosition());
    }

    public Token getTokenAtPosition(int pos) {
        Iterator iterator = getTokens().iterator();
        while(iterator.hasNext()) {
            Token token = (Token)iterator.next();
            if(pos >= token.getStartIndex() && pos <= token.getEndIndex())
                return token;
        }
        return null;
    }

    public ParserRule getCurrentRule() {
        return rules.getEnclosingRuleAtPosition(getCaretPosition());
    }

    public ParserAction getCurrentAction() {
        List actions = parser.getActions();
        int position = getCaretPosition();
        for(int index=0; index<actions.size(); index++) {
            ParserAction action = (ParserAction)actions.get(index);
            if(position >= action.token.getStartIndex() && position <= action.token.getEndIndex())
                return action;
        }
        return null;
    }

    public void setCaretPosition(int position) {
        ParserRule rule = rules.getEnclosingRuleAtPosition(position);
        if(rule != null && !rule.isExpanded()) {
            foldingManager.toggleFolding(rule);
        }
        editorGUI.textEditor.setCaretPosition(position);
    }

    public int getCaretPosition() {
        return editorGUI.textEditor.getCaretPosition();
    }

    public void customizeFileMenu(XJMenu menu) {
        editorMenu.customizeFileMenu(menu);
    }

    public void customizeWindowMenu(XJMenu menu) {
        //editorMenu.customizeWindowMenu(menu);
    }

    public void customizeHelpMenu(XJMenu menu) {
        editorMenu.customizeHelpMenu(menu);
    }

    public void customizeMenuBar(XJMainMenuBar menubar) {
        editorMenu.customizeMenuBar(menubar);
    }

    public void menuItemState(XJMenuItem item) {
        super.menuItemState(item);
        editorMenu.menuItemState(item);
    }

    /** Update methods
    */

    public void updateVisualization(boolean immediate) {
        ParserRule r = rules.getEnclosingRuleAtPosition(getCaretPosition());
        if(r != null) {
            visual.setRule(r, immediate);
        }
    }

    /** Rules delegate methods
     *
     */

    public void rulesCaretPositionDidChange() {
        updateVisualization(false);
    }

    public void rulesDidSelectRule() {
        updateVisualization(true);
    }

    /** Parser delegate methods
     */

    public void parserWillParse() {
        persistence.store();
    }

    public void parserDidParse() {
        persistence.restore();

        editorGUI.parserDidComplete();

        visual.setText(getText(), getFileName());
        updateVisualization(false);

        colorize.colorize();
        interpreter.setRules(parser.getRules());
        getGutter().markDirty();

        if(windowFirstDisplay) {
            windowFirstDisplay = false;
            rules.selectFirstRule();
        }

        // Invoke the idea dectection later because rules didn't updated
        // yet its rule list (parserDidParse first run here and then
        // on Rules - the order can change in the future).
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                displayIdeas(getCaretPosition());
            }
        });
    }

    public void windowDocumentPathDidChange() {
        // Called when the document associated file has changed on the disk
        int oldCursorPosition = getCaretPosition();
        getDocument().reload();
        setCaretPosition(oldCursorPosition);
    }

    /** AutoCompletionMenuDelegate method: return the list of corresponding words
     *  given a partial word
     */

    public List autoCompletionMenuGetMatchingWordsForPartialWord(String partialWord) {
        if(parser == null || parser.getRules() == null)
            return null;

        partialWord = partialWord.toLowerCase();
        List matchingRules = new ArrayList();

        if(rules.isRuleAtIndex(getCaretPosition())) {
            // Inside a rule - show all rules in alphabetical order

            List sortedRules = Collections.list(Collections.enumeration(parser.getRules()));
            Collections.sort(sortedRules);

            for(Iterator iterator = sortedRules.iterator(); iterator.hasNext(); ) {
                ParserRule rule = (ParserRule)iterator.next();
                if(rule.name.toLowerCase().startsWith(partialWord) && !matchingRules.contains(rule.name))
                    matchingRules.add(rule.name);
            }
        } else {
            // Not inside rule - show only undefined rules

            List sortedUndefinedRules = Collections.list(Collections.enumeration(rules.getUndefinedTokens()));
            Collections.sort(sortedUndefinedRules);

            for(Iterator iterator = sortedUndefinedRules.iterator(); iterator.hasNext(); ) {
                Token t = (Token)iterator.next();
                if(t.getAttribute().toLowerCase().startsWith(partialWord)
                        && !t.getAttribute().equals(partialWord)
                        && !matchingRules.contains(t.getAttribute()))
                    matchingRules.add(t.getAttribute());
            }
        }

        return matchingRules;
    }

    public void autoCompletionMenuWillDisplay() {
        // Hide any ideas when displaying auto-completion menu
        ideaManager.hide();
    }

    /* Tips provider */

    public void tipsHide() {
        tipsManager.hide();
    }
        
    public void displayTips(Point relativePoint, Point absolutePoint) {
        if(getTokens() == null)
            return;

        int position = getTextPane().viewToModel(relativePoint);

        Token token = getTokenAtPosition(position);
        ParserRule enclosingRule = rules.getEnclosingRuleAtPosition(position);
        ParserRule rule = rules.getRuleStartingWithToken(token);

        Point p = null;
        try {
            if(token != null) {
                // Make sure the mouse is over the token because
                // Swing will return a valid position even if the mouse
                // is on the remaining blank part of the line
                Rectangle r1 = getTextPane().modelToView(token.getStartIndex());
                Rectangle r2 = getTextPane().modelToView(token.getEndIndex());
                if(r1.union(r2).contains(relativePoint)) {
                    p = SwingUtilities.convertPoint(getTextPane(), new Point(relativePoint.x+2, r2.y-5), jFrame);
                }
            }
        } catch (BadLocationException e) {
            // Ignore
        }
        tipsManager.displayAnyTipsAvailable(token, rule, enclosingRule, p);
    }

    public List tipsProviderGetTips(Token token, ParserRule rule, ParserRule enclosingRule) {
        List tips = new ArrayList();

        if(rules.isUndefinedToken(token)) {
            tips.add("Undefined symbol '"+token.getAttribute()+"'");
        }

        if(rules.isDuplicateRule(token.getAttribute())) {
            tips.add("Duplicate rule '"+token.getAttribute()+"'");
        }

        if(rule != null && rule.hasLeftRecursion()) {
            tips.add("Rule has left recursion");
        }

        return tips;
    }

    /* Idea action delegate */

    public static final int IDEA_DELETE_RULE = 0;
    public static final int IDEA_CREATE_RULE = 1;
    public static final int IDEA_REMOVE_LEFT_RECURSION = 2;

    public List ideaProviderGetActions(Token token, ParserRule rule, ParserRule enclosingRule) {
        List actions = new ArrayList();

        if(rules.isUndefinedToken(token)) {
            actions.add(new IdeaAction("Create rule '"+token.getAttribute()+"'", this, IDEA_CREATE_RULE, token));
        }

        if(rules.isDuplicateRule(token.getAttribute())) {
            actions.add(new IdeaAction("Delete rule '"+token.getAttribute()+"'", this, IDEA_DELETE_RULE, token));
        }

        if(rule != null && rule.hasLeftRecursion()) {
            actions.add(new IdeaAction("Remove Left Recursion of rule '"+token.getAttribute()+"'", this, IDEA_REMOVE_LEFT_RECURSION, token));
        }

        return actions;
    }

    public void ideaActionFire(IdeaAction action, int actionID) {
        switch(actionID) {
            case IDEA_DELETE_RULE:
                ParserRule r = rules.getEnclosingRuleAtPosition(getCaretPosition());
                if(r != null)
                    editorGUI.textEditor.replaceText(r.getStartIndex(), r.getEndIndex(), "");
                break;
            case IDEA_CREATE_RULE:
                ideaCreateRule(action);
                break;
            case IDEA_REMOVE_LEFT_RECURSION:
                actionsRefactor.removeLeftRecursion();
                break;
        }
    }

    public boolean ideaManagerWillDisplayIdea() {
        return !autoCompletionMenu.isVisible();
    }

    public void ideasHide() {
        ideaManager.hide();
    }

    public void displayIdeas(Point p) {
        displayIdeas(getTextPane().viewToModel(p));
    }

    public void displayIdeas(int position) {
        if(getTokens() == null)
            return;

        Token token = getTokenAtPosition(position);
        ParserRule rule = rules.getRuleStartingWithToken(token);
        ParserRule enclosingRule = rules.getEnclosingRuleAtPosition(position);
        if(enclosingRule == null || enclosingRule.isExpanded())
            ideaManager.displayAnyIdeasAvailable(token, rule, enclosingRule);
    }

    public void ideaCreateRule(IdeaAction action) {
        boolean lexerToken = action.token.isAllUpperCase();

        // Add the rule in the next line by default
        Point p = getLineTextPositionsAtTextPosition(getCaretPosition());
        int insertionIndex = p.y + 2;

        ParserRule rule = rules.getEnclosingRuleAtPosition(getCaretPosition());
        if(rule != null) {
            // @todo +2 means two lines - find the real line because +2 only walk two characters
            if(rule.isLexerRule()) {
                if(lexerToken) {
                    // Add new rule just after this one
                    insertionIndex = rule.getEndIndex()+2;
                } else {
                    // Add new rule after the last parser rule
                    ParserRule last = rules.getLastParserRule();
                    if(last != null) {
                        insertionIndex = last.getEndIndex()+2;
                    }
                }
            } else {
                if(lexerToken) {
                    // Add new rule after the last lexer rule
                    ParserRule last = rules.getLastLexerRule();
                    if(last != null) {
                        insertionIndex = last.getEndIndex()+2;
                    } else {
                        // Add new rule after the last rule
                        last = rules.getLastRule();
                        if(last != null) {
                            insertionIndex = last.getEndIndex()+2;
                        }
                    }
                } else {
                    // Add new rule just after this one
                    insertionIndex = rule.getEndIndex()+2;
                }
            }
        }

        int tabSize = EditorPreferences.getEditorTabSize();
        String ruleName = action.token.getAttribute();
        insertionIndex = Math.min(getText().length(), insertionIndex);
        if(ruleName.length() > tabSize + 1)
            editorGUI.textEditor.insertText(insertionIndex, action.token.getAttribute()+"\n\t:\n\t;\n\n");
        else
            editorGUI.textEditor.insertText(insertionIndex, action.token.getAttribute()+"\t:\n\t;\n\n");

        setCaretPosition(insertionIndex);
    }

    public void caretUpdate(int index) {
        editorGUI.updateCursorInfo();
        if(editorGUI.textEditor.textPane.hasFocus()) {
            ideasHide();
            if(!editorGUI.textEditor.isTyping())
                displayIdeas(getCaretPosition());
        }

        // Update the auto-completion list
        autoCompletionMenu.updateAutoCompleteList();

        // Only display ideas using the mouse because otherwise when a rule
        // is deleted (for example), the idea might be displayed before
        // the parser was able to complete
        //displayIdeas(e.getDot());

        ParserRule rule = rules.selectRuleAtPosition(index);
        if(rule == null || rule.name == null)
            return;

        if(lastSelectedRule == null || !lastSelectedRule.equals(rule.name)) {
            lastSelectedRule = rule.name;
            updateVisualization(false);
        } else {
            // @todo display message "no rule selected"
        }
    }
}
