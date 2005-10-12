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
import org.antlr.works.editor.find.FindAndReplace;
import org.antlr.works.editor.helper.*;
import org.antlr.works.editor.rules.Rules;
import org.antlr.works.editor.rules.RulesDelegate;
import org.antlr.works.editor.swing.AutoCompletionMenu;
import org.antlr.works.editor.swing.AutoCompletionMenuDelegate;
import org.antlr.works.editor.swing.Gutter;
import org.antlr.works.editor.swing.TemplateRules;
import org.antlr.works.editor.tool.TActions;
import org.antlr.works.editor.tool.TColorize;
import org.antlr.works.editor.tool.TGoToRule;
import org.antlr.works.editor.tool.TGrammar;
import org.antlr.works.editor.undo.Undo;
import org.antlr.works.editor.visual.Visual;
import org.antlr.works.editor.idea.IdeaOverlay;
import org.antlr.works.editor.idea.IdeaActionDelegate;
import org.antlr.works.editor.idea.IdeaAction;
import org.antlr.works.interpreter.Interpreter;
import org.antlr.works.parser.*;
import org.antlr.works.stats.Statistics;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class EditorWindow extends XJWindow implements ThreadedParserObserver,
     AutoCompletionMenuDelegate, RulesDelegate, EditorProvider, IdeaActionDelegate
{
    public ThreadedParser parser = null;
    public KeyBindings keyBindings = null;
    public AutoCompletionMenu autoCompletionMenu = null;
    public TGoToRule goToRule = null;
    public FindAndReplace findAndReplace = null;
    public TActions actions = null;
    public TColorize colorize = null;
    public TGrammar grammar = null;
    public TemplateRules templateRules = null;
    public IdeaOverlay ideaOverlay = null;

    public Rules rules = null;
    public Visual visual = null;
    public Interpreter interpreter = null;
    public Debugger debugger = null;
    public Console console = null;
    public GoToHistory goToHistory = null;

    private Map undos = new HashMap();

    private boolean windowFirstDisplay = true;

    public EditorGUI editorGUI = null;
    protected EditorCache editorCache = null;
    protected EditorMenu editorMenu = null;

    protected MenuEdit menuEditActions = null;
    protected MenuGrammar menuGrammarActions = null;
    protected MenuGoTo menuGoToActions = null;
    protected MenuGenerate menuGenerateActions = null;
    protected MenuRun menuRunActions = null;
    protected MenuSCM menuSCMActions = null;
    protected MenuExport menuExportActions = null;
    protected MenuHelp menuHelpActions = null;

    public EditorWindow() {

        console = new Console(this);
        console.makeCurrent();

        goToHistory = new GoToHistory();

        editorGUI = new EditorGUI(this);
        editorCache = new EditorCache();
        editorMenu = new EditorMenu(this);

        menuEditActions = new MenuEdit(this);
        menuGrammarActions = new MenuGrammar(this);
        menuGoToActions = new MenuGoTo(this);
        menuGenerateActions = new MenuGenerate(this);
        menuRunActions = new MenuRun(this);
        menuSCMActions = new MenuSCM(this);
        menuExportActions = new MenuExport(this);
        menuHelpActions = new MenuHelp(this);

        editorGUI.createInterface();

        visual = new Visual(this);
        interpreter = new Interpreter(this);
        debugger = new Debugger(this);

        parser = new ThreadedParser(this);
        parser.addObserver(this);

        keyBindings = new KeyBindings(getTextPane());

        autoCompletionMenu = new AutoCompletionMenu(this, getTextPane(), jFrame);
        templateRules = new TemplateRules(this, getTextPane(), jFrame);
        goToRule = new TGoToRule(this, jFrame, getTextPane());
        ideaOverlay = new IdeaOverlay(this, jFrame, getTextPane());
        findAndReplace = new FindAndReplace(this);

        rules = new Rules(parser, getTextPane(), editorGUI.rulesTree);
        actions = new TActions(parser, getTextPane());
        grammar = new TGrammar(this);

        rules.setDelegate(this);
        rules.setActions(actions);
        rules.setKeyBindings(keyBindings);

        actions.setRules(rules);
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
        menuSCMActions.queryFileStatus();
    }

    public void close() {
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

    public JTextPane getTextPane() {
        return editorGUI.textPane;
    }

    public Gutter getGutter() {
        return editorGUI.gutter;
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
            visual.setText(getPlainText(), getFileName());
            updateVisualization(false);
        }
        Statistics.shared().recordEvent(Statistics.EVENT_TOGGLE_SYNTAX_DIAGRAM);
    }

    public void toggleNFAOptimization() {
        visual.toggleNFAOptimization();
        updateVisualization(false);
        Statistics.shared().recordEvent(Statistics.EVENT_TOGGLE_NFA_OPTIMIZATION);
    }

    public void changeUpdate() {
        changeUpdate(-1, -1);
    }

    public void changeUpdate(int offset, int length) {
        changeDone();
        rules.parseRules();
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
        rules.parseRules();
        changeDone();
    }

    public void enableTextPane(boolean undo) {
        editorGUI.textPaneListener.enable();
        if(undo)
            enableTextPaneUndo();
    }

    public void disableTextPane(boolean undo) {
        editorGUI.textPaneListener.disable();
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

    public synchronized String getPlainText() {
        if(editorCache.getString(EditorCache.CACHE_PLAIN_TEXT) == null)
            editorCache.setObject(EditorCache.CACHE_PLAIN_TEXT, actions.getPlainText());
        return editorCache.getString(EditorCache.CACHE_PLAIN_TEXT);
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
        int end = 0;
        if(lineIndex+1 >= lines.size()) {
            return new Point(start, getTextPane().getDocument().getLength()-1);
        } else {
            Line endLine = (Line)lines.get(lineIndex+1);
            end = endLine.position;
            return new Point(start, end-1);
        }
    }


    public Token getTokenAtPosition(int pos) {
        Iterator iterator = getTokens().iterator();
        while(iterator.hasNext()) {
            Token token = (Token)iterator.next();
            if(pos >= token.getStart() && pos <= token.getEnd())
                return token;
        }
        return null;
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
        // @todo add listeners (see if it is fast enough)
        interpreter.grammarChanged();
        debugger.grammarChanged();
        menuGenerateActions.generateCode.grammarChanged();
    }

    public void selectTextRange(int start, int end) {
        editorGUI.selectTextRange(start, end);
    }

    public Parser.Rule getCurrentRule() {
        return rules.getRuleAtPosition(getCaretPosition());
    }

    public void setCaretPosition(int position) {
        editorGUI.textPane.setCaretPosition(position);
    }

    public int getCaretPosition() {
        return editorGUI.getCaretPosition();
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
        Parser.Rule r = rules.getRuleAtPosition(getCaretPosition());
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

    public void parserDidComplete() {
        editorGUI.updateInformation();
        editorGUI.updateCursorInfo();

        visual.setText(getPlainText(), getFileName());
        updateVisualization(false);

        colorize.colorize();
        interpreter.setRules(parser.getRules());
        getGutter().setRules(parser.getRules(), parser.getLines());

        if(windowFirstDisplay) {
            windowFirstDisplay = false;
            rules.selectFirstRule();
        }

        // Invoke the idea dectection later because rules didn't updated
        // yet its rule list (parserDidComplete first run here and then
        // on Rules - the order can change in the future).
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                editorGUI.detectIdeaIfAvailable(getCaretPosition());
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

    public List getMatchingWordsForPartialWord(String partialWord) {
        if(parser == null || parser.getRules() == null)
            return null;

        partialWord = partialWord.toLowerCase();
        List matchingRules = new ArrayList();
        Iterator iterator = parser.getRules().iterator();
        while(iterator.hasNext()) {
            Parser.Rule rule = (Parser.Rule)iterator.next();
            if(rule.name.toLowerCase().startsWith(partialWord))
                matchingRules.add(rule.name);
        }
        return matchingRules;
    }

    /* Idea action delegate */

    public static final int IDEA_DELETE_RULE = 0;
    public static final int IDEA_CREATE_RULE = 1;

    public void ideaActionFire(IdeaAction action, int actionID) {
        switch(actionID) {
            case IDEA_DELETE_RULE:
                Parser.Rule r = rules.getRuleAtPosition(getCaretPosition());
                if(r != null)
                    editorGUI.replaceText(r.getStartIndex(), r.getEndIndex(), "");
                break;
            case IDEA_CREATE_RULE:
                break;
        }
    }
}
