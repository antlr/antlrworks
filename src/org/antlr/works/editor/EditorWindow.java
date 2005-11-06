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
import edu.usfca.xj.appkit.swing.XJTree;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.foundation.XJSystem;
import edu.usfca.xj.foundation.notification.XJNotificationCenter;
import edu.usfca.xj.foundation.notification.XJNotificationObserver;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.dialog.DialogPrefs;
import org.antlr.works.editor.actions.*;
import org.antlr.works.editor.ate.ATEPanel;
import org.antlr.works.editor.ate.ATEPanelDelegate;
import org.antlr.works.editor.ate.ATETextPane;
import org.antlr.works.editor.autocompletion.AutoCompletionMenu;
import org.antlr.works.editor.autocompletion.AutoCompletionMenuDelegate;
import org.antlr.works.editor.autocompletion.TemplateRules;
import org.antlr.works.editor.find.FindAndReplace;
import org.antlr.works.editor.helper.*;
import org.antlr.works.editor.rules.Rules;
import org.antlr.works.editor.rules.RulesDelegate;
import org.antlr.works.editor.swing.TextUtils;
import org.antlr.works.editor.tool.*;
import org.antlr.works.editor.undo.Undo;
import org.antlr.works.editor.undo.UndoDelegate;
import org.antlr.works.editor.visual.Visual;
import org.antlr.works.interpreter.Interpreter;
import org.antlr.works.parser.*;
import org.antlr.works.stats.Statistics;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class EditorWindow
        extends XJWindow
        implements ThreadedParserObserver, AutoCompletionMenuDelegate,
        RulesDelegate, EditorProvider, UndoDelegate, ATEPanelDelegate,
        XJNotificationObserver
{
    /* Auto-completion */

    public AutoCompletionMenu autoCompletionMenu;
    public TemplateRules templateRules;

    /* Tools */

    public FindAndReplace findAndReplace;

    public TGoToRule goToRule;
    public TColorize colorize;
    public TGrammar grammar;
    public TImmediateColorization immediateColorization;
    public TAutoIndent autoIndent;

    /* Managers */

    public EditorBreakpointManager breakpointManager;
    public EditorFoldingManager foldingManager;
    public EditorUnderlyingManager underlyingManager;
    public EditorAnalysisManager analysisManager;

    /* Components */

    public ThreadedParser parser;
    public Rules rules;
    public Visual visual;
    public Interpreter interpreter;
    public Debugger debugger;

    /* Helpers */

    public EditorConsole console;
    public EditorGoToHistory editorGoToHistory;
    public EditorToolbar toolbar;
    public EditorCache editorCache;
    public EditorMenu editorMenu;
    public EditorIdeas editorIdeas;
    public EditorTips editorTips;
    public EditorPersistence persistence;
    public EditorKeyBindings keyBindings;

    /* Actions */

    public ActionsEdit actionsEdit;
    public ActionsView actionsView;
    public ActionsFind actionsFind;
    public ActionsGrammar actionsGrammar;
    public ActionsRefactor actionsRefactor;
    public ActionsGoTo actionsGoTo;
    public ActionsGenerate actionsGenerate;
    public ActionsRun actionsRun;
    public ActionsSCM actionsSCM;
    public ActionsExport actionsExport;
    public ActionsHelp actionsHelp;

    public ATEPanel textEditor;

    /* Swing */

    protected JScrollPane rulesScrollPane;
    protected XJTree rulesTree;

    protected JTabbedPane viewTabbedPane;
    protected JPanel mainPanel;

    protected Box infoBox;
    protected JLabel infoLabel;
    protected JLabel cursorLabel;
    protected JLabel scmLabel;

    protected JSplitPane rulesTextSplitPane;
    protected JSplitPane upDownSplitPane;

    /* Other */

    protected Map undos = new HashMap();

    protected boolean windowFirstDisplay = true;
    protected String lastSelectedRule;

    public EditorWindow() {
        createInterface();

        initHelpers();
        initActions();

        initManagers();
        initComponents();

        initAutoCompletion();
        initTools();

        awakeInstances();
        awakeInterface();

        register();
    }

    protected void initComponents() {
        parser = new ThreadedParser(this);
        parser.awake();

        rules = new Rules(this, parser, rulesTree);
        rules.setDelegate(this);

        visual = new Visual(this);
        visual.setParser(parser);

        interpreter = new Interpreter(this);
        debugger = new Debugger(this);
    }

    protected void initTools() {
        colorize = new TColorize(this);
        goToRule = new TGoToRule(this, getJFrame(), getTextPane());
        immediateColorization = new TImmediateColorization(textEditor.getTextPane());
        autoIndent = new TAutoIndent(textEditor.getTextPane());
        grammar = new TGrammar(this);

        findAndReplace = new FindAndReplace(this);
    }

    protected void initAutoCompletion() {
        autoCompletionMenu = new AutoCompletionMenu(this, getTextPane(), getJFrame());
        templateRules = new TemplateRules(this, getTextPane(), getJFrame());
    }

    protected void initHelpers() {
        console = new EditorConsole(this);
        console.makeCurrent();

        editorCache = new EditorCache();
        editorMenu = new EditorMenu(this);
        editorIdeas = new EditorIdeas(this);
        editorTips = new EditorTips(this);
        editorGoToHistory = new EditorGoToHistory();

        keyBindings = new EditorKeyBindings(getTextPane());

        persistence = new EditorPersistence(this);
    }

    protected void initActions() {
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
    }

    protected void initManagers() {
        breakpointManager = new EditorBreakpointManager(this);
        textEditor.setBreakpointManager(breakpointManager);

        foldingManager = new EditorFoldingManager(this);
        textEditor.setFoldingManager(foldingManager);

        underlyingManager = new EditorUnderlyingManager(this);
        textEditor.setUnderlyingManager(underlyingManager);

        analysisManager = new EditorAnalysisManager(this);
        textEditor.setAnalysisManager(analysisManager);
    }

    protected void awakeInstances() {
        editorIdeas.awake();
        editorTips.awake();

        actionsSCM.awake();

        interpreter.awake();
        debugger.awake();

        rules.setKeyBindings(keyBindings);
    }

    protected void createInterface() {
        Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        r.width *= 0.75;
        r.height *= 0.75;
        getRootPane().setPreferredSize(r.getSize());

        textEditor = new ATEPanel(getJFrame());
        textEditor.setDelegate(this);
        textEditor.setFoldingEnabled(EditorPreferences.getFoldingEnabled());
        textEditor.setHighlightCursorLine(EditorPreferences.getHighlightCursorEnabled());
        applyFont();

        rulesTree = new XJTree() {
            public String getToolTipText(MouseEvent e) {
                TreePath path = getPathForLocation(e.getX(), e.getY());
                if(path == null)
                    return "";

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Rules.RuleTreeUserObject n = (Rules.RuleTreeUserObject) node.getUserObject();
                if(n == null)
                    return "";

                ParserRule r = n.rule;
                if(r == null || !r.hasErrors())
                    return "";
                else
                    return r.getErrorMessageHTML();
            }
        };
        rulesTree.setBorder(null);
        // Apparently, if I don't set the tooltip here, nothing is displayed (weird)
        rulesTree.setToolTipText("");
        rulesTree.setDragEnabled(true);

        rulesScrollPane = new JScrollPane(rulesTree);
        rulesScrollPane.setBorder(null);
        rulesScrollPane.setWheelScrollingEnabled(true);

        // Assemble

        viewTabbedPane = new JTabbedPane();
        viewTabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
        viewTabbedPane.addMouseListener(new TabbedPaneMouseListener());

        rulesTextSplitPane = new JSplitPane();
        rulesTextSplitPane.setBorder(null);
        rulesTextSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        rulesTextSplitPane.setLeftComponent(rulesScrollPane);
        rulesTextSplitPane.setRightComponent(textEditor);
        rulesTextSplitPane.setContinuousLayout(true);
        rulesTextSplitPane.setOneTouchExpandable(true);

        upDownSplitPane = new JSplitPane();
        upDownSplitPane.setBorder(null);
        upDownSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        upDownSplitPane.add(rulesTextSplitPane, JSplitPane.TOP);
        upDownSplitPane.add(viewTabbedPane, JSplitPane.BOTTOM);
        upDownSplitPane.setContinuousLayout(true);
        upDownSplitPane.setOneTouchExpandable(true);

        infoLabel = new JLabel();
        cursorLabel = new JLabel();
        scmLabel = new JLabel();

        infoBox = new InfoPanel();
        infoBox.setPreferredSize(new Dimension(0, 30));

        infoBox.add(Box.createHorizontalStrut(5));
        infoBox.add(infoLabel);
        infoBox.add(Box.createHorizontalStrut(5));
        infoBox.add(createSeparator());
        infoBox.add(Box.createHorizontalStrut(5));
        infoBox.add(cursorLabel);
        infoBox.add(Box.createHorizontalStrut(5));
        infoBox.add(createSeparator());
        infoBox.add(Box.createHorizontalStrut(5));
        infoBox.add(scmLabel);

        toolbar = new EditorToolbar(this);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(toolbar.getToolbar(), BorderLayout.NORTH);
        mainPanel.add(upDownSplitPane, BorderLayout.CENTER);
        mainPanel.add(infoBox, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
        pack();

        if(!XJSystem.isMacOS()) {
            rulesTextSplitPane.setDividerSize(10);
            upDownSplitPane.setDividerSize(10);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                upDownSplitPane.setDividerLocation(0.5);
                rulesTextSplitPane.setDividerLocation(0.3);
            }
        });
    }

    protected void awakeInterface() {
        getTabbedPane().addTab("Syntax Diagram", visual.getContainer());
        getTabbedPane().addTab("Interpreter", interpreter.getContainer());
        getTabbedPane().addTab("Debugger", debugger.getContainer());
        getTabbedPane().addTab("Console", console.getContainer());

        selectVisualizationTab();
    }

    protected void register() {
        // First rules, then EditorWindow
        parser.addObserver(rules);
        parser.addObserver(this);

        registerUndo(new Undo(this), getTextPane());

        XJNotificationCenter.defaultCenter().addObserver(this, DialogPrefs.NOTIF_PREFS_APPLIED);
        XJNotificationCenter.defaultCenter().addObserver(this, Debugger.NOTIF_DEBUG_STARTED);
        XJNotificationCenter.defaultCenter().addObserver(this, Debugger.NOTIF_DEBUG_STOPPED);
    }

    public void applyFont() {
        textEditor.getTextPane().setFont(new Font(EditorPreferences.getEditorFont(), Font.PLAIN, EditorPreferences.getEditorFontSize()));
        TextUtils.createTabs(textEditor.getTextPane());
    }

    protected static JComponent createSeparator() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        Dimension d = s.getMaximumSize();
        d.width = 2;
        s.setMaximumSize(d);
        return s;
    }

    public void close() {
        editorIdeas.close();
        editorMenu.close();
        debugger.close();
        visual.close();
        super.close();
    }

    public void becomingVisibleForTheFirstTime() {
        textPaneRequestFocusLater();
        updateInformation();
        updateCursorInfo();
        actionsSCM.setSilent(true);
        actionsSCM.queryFileStatus();
    }

    public void selectVisualizationTab() {
        getTabbedPane().setSelectedIndex(0);
    }

    public void selectInterpreterTab() {
        getTabbedPane().setSelectedIndex(1);
        makeBottomComponentVisible();
    }

    public void selectDebuggerTab() {
        getTabbedPane().setSelectedIndex(2);
        makeBottomComponentVisible();
    }

    protected void makeBottomComponentVisible() {
        if(upDownSplitPane.getBottomComponent().getHeight() == 0) {
            upDownSplitPane.setDividerLocation(upDownSplitPane.getLastDividerLocation());
        }
    }

    public void registerUndo(Undo undo, JTextPane component) {
        undo.bindTo(component);
        component.addFocusListener(new EditorFocusListener());
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

    public void updateUndoRedo(Object source) {
        Undo undo = getUndo(source);
        updateUndoRedo(undo);
    }

    public void updateUndoRedo(Undo undo) {
        if(editorMenu == null || editorMenu.menuItemUndo == null
                || editorMenu.menuItemRedo == null)
            return;

        editorMenu.menuItemUndo.setTitle("Undo");
        editorMenu.menuItemRedo.setTitle("Redo");

        if(undo == null) {
            editorMenu.menuItemUndo.setEnabled(false);
            editorMenu.menuItemRedo.setEnabled(false);
        } else {
            editorMenu.menuItemUndo.setEnabled(undo.canUndo());
            editorMenu.menuItemRedo.setEnabled(undo.canRedo());

            if(undo.canUndo())
                editorMenu.menuItemUndo.setTitle(undo.undoManager.getUndoPresentationName());
            if(undo.canRedo())
                editorMenu.menuItemRedo.setTitle(undo.undoManager.getRedoPresentationName());
        }
    }

    public void undoStateDidChange(Undo undo) {
        updateUndoRedo(undo);
    }

    public ATETextPane getTextPane() {
        return textEditor.getTextPane();
    }

    public ATEPanel getTextEditor() {
        return textEditor;
    }

    public JTabbedPane getTabbedPane() {
        return viewTabbedPane;
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
        setAutoIndent(!autoIndent());
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
        editorIdeas.toggleEnabled();
    }

    public void toggleTips() {
        editorTips.toggleEnabled();
    }

    public void toggleUnderlying() {
        textEditor.setUnderlying(!textEditor.isUnderlying());
        textEditor.refresh();
    }

    public void toggleAnalysis() {
        textEditor.toggleAnalysis();
    }

    protected void adjustTokens(int location, int length) {
        // We have to shift every offset past the location in order
        // for collapsed view to be correctly rendered (the rule has to be
        // immediately at the right position and cannot wait for the
        // parser to finish)

        if(location == -1)
            return;
        
        List tokens = getTokens();
        if(tokens == null)
            return;

        for(int t=0; t<tokens.size(); t++) {
            Token token = (Token) tokens.get(t);
            if(token.getStartIndex() > location) {
                token.offsetPositionBy(length);
            }
        }
    }

    public void changeUpdate() {
        ateChangeUpdate(-1, -1, false);
    }

    public void setAutoIndent(boolean flag) {
        autoIndent.setEnabled(flag);
    }

    public boolean autoIndent() {
        return autoIndent.enabled();
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

    public void setText(String text) {
        getTextPane().setText(text);
        colorize.reset();
        parser.parse();
    }

    public synchronized String getText() {
        if(editorCache.getString(EditorCache.CACHE_TEXT) == null)
            editorCache.setObject(EditorCache.CACHE_TEXT, getTextPane().getText());
        return editorCache.getString(EditorCache.CACHE_TEXT);
    }

    public void replaceText(int leftIndex, int rightIndex, String text) {
        textEditor.replaceText(leftIndex, rightIndex, text);
    }

    public void selectTextRange(int start, int end) {
        textEditor.selectTextRange(start, end);
    }

    public int getSelectionLeftIndexOnTokenBoundary() {
        Token t = getTokenAtPosition(getTextPane().getSelectionStart());
        if(t == null)
            return -1;
        else
            return t.getStartIndex();
    }

    public int getSelectionRightIndexOnTokenBoundary() {
        Token t = getTokenAtPosition(getTextPane().getSelectionEnd());
        if(t == null)
            return -1;
        else
            return t.getEndIndex();
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

    public List getReferences() {
        return parser.getReferences();
    }

    public List getTokens() {
        return parser.getTokens();
    }

    public List getLines() {
        return parser.getLines();
    }

    public int getCurrentLinePosition() {
        return getLineIndexAtTextPosition(getCaretPosition()) + 1;
    }

    public int getCurrentColumnPosition() {
        int lineIndex = getLineIndexAtTextPosition(getCaretPosition());
        Point linePosition = getLineTextPositionsAtLineIndex(lineIndex);
        if(linePosition == null)
            return 1;
        else
            return getCaretPosition() - linePosition.x + 1;
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
        editorGoToHistory.addPosition(getCaretPosition());
        getMainMenuBar().refreshState();
    }

    public ParserReference getCurrentReference() {
        return getReferenceAtPosition(getCaretPosition());
    }

    public ParserReference getReferenceAtPosition(int pos) {
        List refs = getReferences();
        for(int index=0; index<refs.size(); index++) {
            ParserReference ref = (ParserReference)refs.get(index);
            if(ref.containsIndex(pos))
                return ref;
        }
        return null;
    }

    public Token getCurrentToken() {
        return getTokenAtPosition(getCaretPosition());
    }

    public Token getTokenAtPosition(int pos) {
        List tokens = getTokens();
        for(int index=0; index<tokens.size(); index++) {
            Token token = (Token)tokens.get(index);
            if(token.containsIndex(pos))
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
            if(action.containsIndex(position))
                return action;
        }
        return null;
    }

    public void setCaretPosition(int position) {
        ParserRule rule = rules.getEnclosingRuleAtPosition(position);
        if(rule != null && !rule.isExpanded()) {
            foldingManager.toggleFolding(rule);
        }
        textEditor.setCaretPosition(position);
    }

    public int getCaretPosition() {
        return textEditor.getCaretPosition();
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

    public void updateInformation() {
        String t;
        int size = 0;
        if(parser.getRules() != null)
            size = parser.getRules().size();
        switch(size) {
            case 0:
                t = "No rules";
                break;
            case 1:
                t = "One rule";
                break;
            default:
                t = size+" rules";
                break;
        }

        int warnings = rules.getNumberOfRulesWithErrors();
        if(warnings > 0)
            t += " ("+warnings+" warnings)";

        infoLabel.setText(t);
    }

    public void updateCursorInfo() {
        cursorLabel.setText(getCurrentLinePosition()+":"+getCurrentColumnPosition());
    }

    public void updateSCMStatus(String status) {
        scmLabel.setVisible(EditorPreferences.getP4Enabled());
        if(status != null)
            scmLabel.setText("SCM Status: "+status);
        else
            scmLabel.setText("");
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
        analysisManager.refresh();

        textEditor.setIsTyping(false);
        textEditor.refresh();
        updateInformation();
        updateCursorInfo();

        visual.setText(getText(), getFileName());
        updateVisualization(false);

        colorize.colorize();
        interpreter.setRules(parser.getRules());

        if(windowFirstDisplay) {
            windowFirstDisplay = false;
            rules.selectFirstRule();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    checkGrammar();
                }
            });
        }

        // Invoke the idea dectection later because rules didn't updated
        // yet its rule list (parserDidParse first run here and then
        // on Rules - the order can change in the future).
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                editorIdeas.display(getCaretPosition());
            }
        });
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

    public void notificationFire(Object source, String name) {
        if(name.equals(DialogPrefs.NOTIF_PREFS_APPLIED)) {
            textEditor.setFoldingEnabled(EditorPreferences.getFoldingEnabled());
            textEditor.setHighlightCursorLine(EditorPreferences.getHighlightCursorEnabled());
            textEditor.refresh();
            applyFont();
            getMainMenuBar().refreshState();
            updateSCMStatus(null);
        } else if(name.equals(Debugger.NOTIF_DEBUG_STARTED)) {
            editorIdeas.hide();
        } else if(name.equals(Debugger.NOTIF_DEBUG_STOPPED)) {
        }
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

            List sortedUndefinedReferences = Collections.list(Collections.enumeration(rules.getUndefinedReferences()));
            Collections.sort(sortedUndefinedReferences);

            for(Iterator iterator = sortedUndefinedReferences.iterator(); iterator.hasNext(); ) {
                ParserReference ref = (ParserReference)iterator.next();
                String attr = ref.token.getAttribute();
                if(attr.toLowerCase().startsWith(partialWord)
                        && !attr.equals(partialWord)
                        && !matchingRules.contains(attr))
                {
                    matchingRules.add(attr);
                }
            }
        }

        return matchingRules;
    }

    public void autoCompletionMenuWillDisplay() {
        // Hide any ideas when displaying auto-completion menu
        editorIdeas.hide();
    }

    public void ateChangeUpdate(int offset, int length, boolean insert) {
        if(insert) {
            immediateColorization.colorize(offset, length);
            autoIndent.indent(offset, length);
        }

        changeDone();

        adjustTokens(offset, length);
        textEditor.changeOccurred();

        parser.parse();
        visual.cancelDrawingProcess();

        colorize.setColorizeLocation(offset, length);
    }

    public void ateMousePressed(Point point) {
        if(!debugger.isRunning())
            editorIdeas.display(point);
    }

    public void ateMouseExited() {
        if(getTextPane().hasFocus()) {
            // Do not hide the ideas because
            // otherwise we don't be able to access the idea
            editorTips.hide();
        }
    }

    public void ateMouseMoved(Point relativePoint) {
        if(getTextPane().hasFocus()) {
            Point absolutePoint = SwingUtilities.convertPoint(getTextPane(), relativePoint, getJavaContainer());
            editorTips.display(relativePoint, absolutePoint);
        }
    }

    public void ateCaretUpdate(int index) {
        updateCursorInfo();
        if(getTextPane().hasFocus()) {
            editorIdeas.hide();
            if(!textEditor.isTyping() && !debugger.isRunning())
                editorIdeas.display(getCaretPosition());
        }

        // Update the auto-completion list
        autoCompletionMenu.updateAutoCompleteList();

        // Only display ideas using the mouse because otherwise when a rule
        // is deleted (for example), the idea might be displayed before
        // the parser was able to complete
        // display(e.getDot());

        ParserRule rule = rules.selectRuleAtPosition(index);
        if(rule == null || rule.name == null) {
            visual.setPlaceholder("Select a rule to display its syntax diagram");
            lastSelectedRule = null;
            return;
        }

        if(lastSelectedRule == null || !lastSelectedRule.equals(rule.name)) {
            lastSelectedRule = rule.name;
            updateVisualization(false);
        }
    }

    public void checkGrammar() {
        // Check to see if "class" and "extends" are in the grammar text which
        // means that the grammar is probably an ANTLR version 2 grammar.

        boolean version2 = false;
        List tokens = parser.getTokens();
        for(int index=0; index<tokens.size(); index++) {
            Token t = (Token)tokens.get(index);
            if(t.type == Lexer.TOKEN_ID && t.getAttribute().equals("class")) {
                if(index+2<tokens.size()) {
                    Token t2 = (Token)tokens.get(index+2);
                    if(t2.type == Lexer.TOKEN_ID && t2.getAttribute().equals("extends")) {
                        version2 = true;
                        break;
                    }
                }
            }
        }

        if(version2) {
            XJAlert.display(getWindowContainer(), "Incompatible Grammar Version", "This grammar does not appear to be an ANTLR 3.x grammar." +
                    "\nANTLRWorks includes ANTLR 3.x and therefore only ANTLR 3.x grammars are recognized.");
        }
    }

    protected class TabbedPaneMouseListener extends MouseAdapter {

        protected static final int CLOSING_INDEX_LIMIT = 4;

        public void displayPopUp(MouseEvent event) {
            if(viewTabbedPane.getSelectedIndex() < CLOSING_INDEX_LIMIT)
                return;

            if(!event.isPopupTrigger())
                return;

            JPopupMenu popup = new JPopupMenu();
            JMenuItem item = new JMenuItem("Close");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if(viewTabbedPane.getSelectedIndex() < CLOSING_INDEX_LIMIT)
                        return;

                    viewTabbedPane.removeTabAt(viewTabbedPane.getSelectedIndex());
                }
            });
            popup.add(item);
            popup.show(event.getComponent(), event.getX(), event.getY());
        }

        public void mousePressed(MouseEvent event) {
            displayPopUp(event);
        }

        public void mouseReleased(MouseEvent event) {
            displayPopUp(event);
        }
    }

    protected class EditorFocusListener implements FocusListener {

        public void focusGained(FocusEvent event) {
            updateUndoRedo(event.getSource());
        }

        public void focusLost(FocusEvent event) {
            // Update the menu only if the event is not temporary. Temporary
            // focus lost can be, for example, when opening a menu on Windows/Linux.
            if(!event.isTemporary())
                updateUndoRedo(null);
        }
    }

    protected class InfoPanel extends Box {

        public InfoPanel() {
            super(BoxLayout.X_AXIS);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Rectangle r = getBounds();

            g.setColor(Color.darkGray);
            g.drawLine(0, 0, r.width, 0);

            g.setColor(Color.lightGray);
            g.drawLine(0, 1, r.width, 1);
        }
    }

}
