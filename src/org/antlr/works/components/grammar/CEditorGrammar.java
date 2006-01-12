package org.antlr.works.components.grammar;

import edu.usfca.xj.appkit.menu.XJMainMenuBar;
import edu.usfca.xj.appkit.menu.XJMenu;
import edu.usfca.xj.appkit.menu.XJMenuItem;
import edu.usfca.xj.appkit.swing.XJTree;
import edu.usfca.xj.appkit.undo.XJUndo;
import edu.usfca.xj.appkit.undo.XJUndoDelegate;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.foundation.XJSystem;
import edu.usfca.xj.foundation.XJUtils;
import edu.usfca.xj.foundation.notification.XJNotificationCenter;
import edu.usfca.xj.foundation.notification.XJNotificationObserver;
import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.ATEPanelDelegate;
import org.antlr.works.ate.ATETextPane;
import org.antlr.works.ate.syntax.ATEGenericLexer;
import org.antlr.works.ate.syntax.ATELine;
import org.antlr.works.ate.syntax.ATEParserEngine;
import org.antlr.works.ate.syntax.ATEToken;
import org.antlr.works.completion.AutoCompletionMenu;
import org.antlr.works.completion.AutoCompletionMenuDelegate;
import org.antlr.works.completion.RuleTemplates;
import org.antlr.works.components.ComponentContainer;
import org.antlr.works.components.ComponentEditor;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.editor.*;
import org.antlr.works.find.FindAndReplace;
import org.antlr.works.grammar.EditorGrammar;
import org.antlr.works.interpreter.EditorInterpreter;
import org.antlr.works.menu.*;
import org.antlr.works.navigation.GoToHistory;
import org.antlr.works.navigation.GoToRule;
import org.antlr.works.parser.ParserAction;
import org.antlr.works.parser.ParserReference;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.prefs.AWPrefsDialog;
import org.antlr.works.rules.Rules;
import org.antlr.works.rules.RulesDelegate;
import org.antlr.works.stats.Statistics;
import org.antlr.works.syntax.AutoIndentation;
import org.antlr.works.syntax.ImmediateColoring;
import org.antlr.works.syntax.Syntax;
import org.antlr.works.utils.TextUtils;
import org.antlr.works.visualization.Visual;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

public class CEditorGrammar extends ComponentEditor implements AutoCompletionMenuDelegate,
        RulesDelegate, EditorProvider, ATEPanelDelegate,
        XJUndoDelegate, XJNotificationObserver
{

        /* Completion */

    public AutoCompletionMenu autoCompletionMenu;
    public RuleTemplates ruleTemplates;

    /* Tools */

    public FindAndReplace findAndReplace;

    public GoToRule goToRule;
    public GoToHistory goToHistory;
    public ImmediateColoring immediateSyntaxColoring;
    public AutoIndentation autoIndent;

    /* Managers */

    public EditorBreakpointManager breakpointManager;
    public EditorFoldingManager foldingManager;
    public EditorUnderlyingManager underlyingManager;
    public EditorAnalysisManager analysisManager;

    /* Components */

    public ATEParserEngine parserEngine;
    public Rules rules;
    public Visual visual;
    public EditorInterpreter interpreter;
    public Debugger debugger;

    /* Editor */

    public EditorConsole console;
    public EditorToolbar toolbar;
    public EditorCache editorCache;
    public EditorMenu editorMenu;
    public EditorIdeas editorIdeas;
    public EditorTips editorTips;
    public EditorInspector editorInspector;
    public EditorPersistence persistence;
    public EditorKeyBindings keyBindings;

    /* Menu */

    public MenuFolding menuFolding;
    public MenuFind menuFind;
    public MenuGrammar menuGrammar;
    public MenuRefactor menuRefactor;
    public MenuGoTo menuGoTo;
    public MenuGenerate menuGenerate;
    public MenuRun menuRun;
    public MenuSCM menuSCM;
    public MenuExport menuExport;

    public ATEPanel textEditor;

    /* Swing */

    protected JScrollPane rulesScrollPane;
    protected XJTree rulesTree;

    protected JTabbedPane tabbedPane;

    protected Box infoBox;
    protected JLabel infoLabel;
    protected JLabel cursorLabel;
    protected JLabel scmLabel;

    protected JSplitPane rulesTextSplitPane;
    protected JSplitPane upDownSplitPane;

    /* Other */

    protected boolean windowFirstDisplay = true;
    protected String lastSelectedRule;

    protected List tabs = new ArrayList();

    /* Grammar */

    protected EditorGrammar grammar;
    protected Syntax syntax;

    public CEditorGrammar(ComponentContainer container) {
        super(container);
    }

    public void create() {
        createInterface();

        initCore();
        initMenus();

        initManagers();
        initComponents();

        initAutoCompletion();
        initTools();

        awakeInstances();
        awakeInterface();

        register();
    }

    protected void initComponents() {
        parserEngine = new ATEParserEngine(this, textEditor);
        parserEngine.awake();

        rules = new Rules(this, parserEngine, rulesTree);
        rules.setDelegate(this);

        syntax = new Syntax(this, parserEngine);

        visual = new Visual(this);
        visual.setParser(parserEngine);

        interpreter = new EditorInterpreter(this);
        debugger = new Debugger(this);
    }

    protected void initTools() {
        goToRule = new GoToRule(this, getJFrame(), getTextPane());
        goToHistory = new GoToHistory();
        immediateSyntaxColoring = new ImmediateColoring(getTextPane());
        autoIndent = new AutoIndentation(getTextPane());
        findAndReplace = new FindAndReplace(this);
    }

    protected void initAutoCompletion() {
        autoCompletionMenu = new AutoCompletionMenu(this, getTextPane(), getJFrame());
        ruleTemplates = new RuleTemplates(this, getTextPane(), getJFrame());
    }

    protected void initCore() {
        console = new EditorConsole(this);
        console.makeCurrent();

        editorCache = new EditorCache();
        editorMenu = new EditorMenu(this);
        editorIdeas = new EditorIdeas(this);
        editorTips = new EditorTips(this);
        editorInspector = new EditorInspector(this);

        keyBindings = new EditorKeyBindings(getTextPane());

        persistence = new EditorPersistence(this);

        grammar = new EditorGrammar(this);
    }

    protected void initMenus() {
        menuFolding = new MenuFolding(this);
        menuFind = new MenuFind(this);
        menuGrammar = new MenuGrammar(this);
        menuRefactor = new MenuRefactor(this);
        menuGoTo = new MenuGoTo(this);
        menuGenerate = new MenuGenerate(this);
        menuRun = new MenuRun(this);
        menuSCM = new MenuSCM(this);
        menuExport = new MenuExport(this);
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

        menuSCM.awake();

        interpreter.awake();
        debugger.awake();

        rules.setKeyBindings(keyBindings);
    }

    protected void createInterface() {
        textEditor = new ATEPanel(getJFrame(), this);
        textEditor.setSyntaxColoring(true);
        textEditor.setDelegate(this);
        textEditor.setFoldingEnabled(AWPrefs.getFoldingEnabled());
        textEditor.setHighlightCursorLine(AWPrefs.getHighlightCursorEnabled());
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

        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
        tabbedPane.addMouseListener(new TabbedPaneMouseListener());
        tabbedPane.addChangeListener(new TabbedPaneChangeListener());

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
        upDownSplitPane.add(tabbedPane, JSplitPane.BOTTOM);
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

        mainPanel.add(toolbar.getToolbar(), BorderLayout.NORTH);
        mainPanel.add(upDownSplitPane, BorderLayout.CENTER);
        mainPanel.add(infoBox, BorderLayout.SOUTH);

        if(!XJSystem.isMacOS()) {
            rulesTextSplitPane.setDividerSize(10);
            upDownSplitPane.setDividerSize(10);
        }

        upDownSplitPane.setDividerLocation(300);
        rulesTextSplitPane.setDividerLocation(150);
    }

    protected void awakeInterface() {
        addTab(visual);
        addTab(interpreter);
        addTab(debugger);
        addTab(console);

        selectVisualizationTab();
    }

    protected void register() {
        getXJFrame().registerUndo(this, getTextPane());

        XJNotificationCenter.defaultCenter().addObserver(this, AWPrefsDialog.NOTIF_PREFS_APPLIED);
        XJNotificationCenter.defaultCenter().addObserver(this, Debugger.NOTIF_DEBUG_STARTED);
        XJNotificationCenter.defaultCenter().addObserver(this, Debugger.NOTIF_DEBUG_STOPPED);
    }

    public void applyFont() {
        getTextPane().setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        TextUtils.createTabs(getTextPane());
    }

    protected static JComponent createSeparator() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        Dimension d = s.getMaximumSize();
        d.width = 2;
        s.setMaximumSize(d);
        return s;
    }

    public void close() {
        XJNotificationCenter.defaultCenter().removeObserver(this);

        toolbar.close();
        editorIdeas.close();
        editorMenu.close();
        debugger.close();
        visual.close();
    }

    public void selectVisualizationTab() {
        selectTab(visual.getTabComponent());
    }

    public void selectInterpreterTab() {
        selectTab(interpreter.getTabComponent());
        makeBottomComponentVisible();
    }

    public void selectDebuggerTab() {
        selectTab(debugger.getTabComponent());
        makeBottomComponentVisible();
    }

    public void addTab(EditorTab tab) {
        tabs.add(tab);
        tabbedPane.add(tab.getTabName(), tab.getTabComponent());
        selectTab(tab.getTabComponent());
    }

    public EditorTab getSelectedTab() {
        return (EditorTab)tabs.get(tabbedPane.getSelectedIndex());
    }

    public void selectTab(Component c) {
        tabbedPane.setSelectedComponent(c);
        refreshMainMenuBar();
    }

    public void makeBottomComponentVisible() {
        if(upDownSplitPane.getBottomComponent().getHeight() == 0) {
            upDownSplitPane.setDividerLocation(upDownSplitPane.getLastDividerLocation());
        }
    }

    public EditorGrammar getGrammar() {
        return grammar;
    }

    public EditorConsole getConsole() {
        return console;
    }

    public ATETextPane getTextPane() {
        return textEditor.getTextPane();
    }

    public ATEPanel getTextEditor() {
        return textEditor;
    }

    public Syntax getSyntax() {
        return syntax;
    }

    public void toggleAutoIndent() {
        setAutoIndent(!autoIndent());
    }

    public void toggleSyntaxColoring() {
        textEditor.toggleSyntaxColoring();
    }

    public void toggleRulesSorting() {
        rules.toggleSorting();
        interpreter.setRules(getRules());
    }

    public void toggleSyntaxDiagram() {
        visual.setEnable(!visual.isEnable());
        if(visual.isEnable()) {
            visual.setText(getText(), getFileName());
        }
        updateVisualization(false);
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
            ATEToken token = (ATEToken) tokens.get(t);
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
        textEditor.resetColoring();
        parserEngine.parse();
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

    public void enableTextPaneUndo() {
        XJUndo undo = getXJFrame().getUndo(getTextPane());
        if(undo != null)
            undo.enableUndo();
    }

    public void disableTextPaneUndo() {
        XJUndo undo = getXJFrame().getUndo(getTextPane());
        if(undo != null)
            undo.disableUndo();
    }

    public void undoManagerWillUndo(boolean redo) {
        disableTextPane(false);
    }

    public void undoManagerDidUndo(boolean redo) {
        enableTextPane(false);
        changeUpdate();
    }

    public void loadText(String text) {
        disableTextPane(true);
        try {
            getTextPane().setText(text);
            getTextPane().setCaretPosition(0);
            getTextPane().moveCaretPosition(0);
            getTextPane().getCaret().setSelectionVisible(true);
            grammarChanged();
            parserEngine.parse();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            enableTextPane(true);
        }
    }

    public void setText(String text) {
        getTextPane().setText(text);
        grammarChanged();
        parserEngine.parse();
        changeDone();
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
        ATEToken t = getTokenAtPosition(getTextPane().getSelectionStart());
        if(t == null)
            return -1;
        else
            return t.getStartIndex();
    }

    public int getSelectionRightIndexOnTokenBoundary() {
        ATEToken t = getTokenAtPosition(getTextPane().getSelectionEnd());
        if(t == null)
            return -1;
        else
            return t.getEndIndex();
    }

    public synchronized String getFileFolder() {
        return XJUtils.getPathByDeletingLastComponent(getFilePath());
    }

    public synchronized String getFilePath() {
        return getDocument().getDocumentPath();
    }

    public synchronized String getFileName() {
        return getDocument().getDocumentName();
    }

    public Container getWindowContainer() {
        return getJFrame();
    }

    public List getRules() {
        if(rules.isSorted()) {
            return rules.getSortedRules();
        } else
            return rules.getRules();
    }

    public List getActions() {
        return parserEngine.getActions();
    }

    public List getReferences() {
        return parserEngine.getReferences();
    }

    public List getTokens() {
        return parserEngine.getTokens();
    }

    public List getLines() {
        return parserEngine.getLines();
    }

    public int getCurrentLinePosition() {
        return getLinePositionAtIndex(getCaretPosition());
    }

    public int getLinePositionAtIndex(int index) {
        return getLineIndexAtTextPosition(index) + 1;
    }

    public int getCurrentColumnPosition() {
        return getColumnPositionAtIndex(getCaretPosition());
    }

    public int getColumnPositionAtIndex(int index) {
        int lineIndex = getLineIndexAtTextPosition(index);
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
            ATELine line = (ATELine)lines.get(i);
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

        ATELine startLine = (ATELine)lines.get(lineIndex);
        int start = startLine.position;
        if(lineIndex+1 >= lines.size()) {
            return new Point(start, getTextPane().getDocument().getLength()-1);
        } else {
            ATELine endLine = (ATELine)lines.get(lineIndex+1);
            int end = endLine.position;
            return new Point(start, end-1);
        }
    }

    public void goToHistoryRememberCurrentPosition() {
        goToHistory.addPosition(getCaretPosition());
        refreshMainMenuBar();
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

    public ATEToken getCurrentToken() {
        return getTokenAtPosition(getCaretPosition());
    }

    public ATEToken getTokenAtPosition(int pos) {
        List tokens = getTokens();
        if(tokens == null)
            return null;

        for(int index=0; index<tokens.size(); index++) {
            ATEToken token = (ATEToken)tokens.get(index);
            if(token.containsIndex(pos))
                return token;
        }
        return null;
    }

    public ParserRule getCurrentRule() {
        return rules.getEnclosingRuleAtPosition(getCaretPosition());
    }

    public ParserAction getCurrentAction() {
        List actions = parserEngine.getActions();
        int position = getCaretPosition();
        for(int index=0; index<actions.size(); index++) {
            ParserAction action = (ParserAction)actions.get(index);
            if(action.containsIndex(position))
                return action;
        }
        return null;
    }

    public void setCaretPosition(int position) {
        setCaretPosition(position, AWPrefs.getSmoothScrolling());
    }

    public void setCaretPosition(int position, boolean animate) {
        ParserRule rule = rules.getEnclosingRuleAtPosition(position);
        if(rule != null && !rule.isExpanded()) {
            foldingManager.toggleFolding(rule);
        }
        textEditor.setCaretPosition(position, true, animate);
    }

    public int getCaretPosition() {
        return textEditor.getCaretPosition();
    }

    public void customizeFileMenu(XJMenu menu) {
        editorMenu.customizeFileMenu(menu);
    }

    public void customizeMenuBar(XJMainMenuBar menubar) {
        editorMenu.customizeMenuBar(menubar);
    }

    public void menuItemState(XJMenuItem item) {
        editorMenu.menuItemState(item);
    }

    /** Update methods
    */

    public void updateVisualization(boolean immediate) {
        if(visual.isEnable()) {
            ParserRule r = rules.getEnclosingRuleAtPosition(getCaretPosition());
            if(r == null) {
                visual.setPlaceholder("Select a rule to display its syntax diagram");
            } else {
                visual.setRule(r, immediate);
            }
        } else {
            visual.setPlaceholder("Syntax Diagram Disabled");
        }
    }

    public void updateInformation() {
        String t;
        int size = 0;
        if(parserEngine.getRules() != null)
            size = parserEngine.getRules().size();
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

        int warnings = syntax.getNumberOfRulesWithErrors();
        if(warnings > 0)
            t += " ("+warnings+" warnings)";

        infoLabel.setText(t);
    }

    public void updateCursorInfo() {
        cursorLabel.setText(getCurrentLinePosition()+":"+getCurrentColumnPosition());
    }

    public void updateSCMStatus(String status) {
        scmLabel.setVisible(AWPrefs.getP4Enabled());
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

    public void ateParserWillParse() {
        persistence.store();
    }

    public void ateParserDidParse() {
        persistence.restore();

        textEditor.setIsTyping(false);
        textEditor.refresh();
        updateInformation();
        updateCursorInfo();

        visual.setText(getText(), getFileName());
        updateVisualization(false);

        interpreter.setRules(getRules());

        rules.parserDidParse();
        syntax.parserDidParse();

        // Make sure to invoke the ideas after Rules
        // has completely updated its list (which should
        // be done inside rules.parserDidParse()
        editorIdeas.display(getCaretPosition());

        if(windowFirstDisplay) {
            windowFirstDisplay = false;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // Don't select the first rule for now ;-)
                    //rules.selectFirstRule();
                    updateVisualization(true);
                    executeFirstOpeningOperations();
                }
            });
        }
    }

    public void changeDone() {
        grammarChanged();
        editorCache.invalidate();
        getDocument().changeDone();
    }

    public void grammarChanged() {
        grammar.makeDirty();
    }

    public void notificationFire(Object source, String name) {
        if(name.equals(AWPrefsDialog.NOTIF_PREFS_APPLIED)) {
            textEditor.setFoldingEnabled(AWPrefs.getFoldingEnabled());
            textEditor.setHighlightCursorLine(AWPrefs.getHighlightCursorEnabled());
            textEditor.refresh();
            applyFont();
            updateSCMStatus(null);
        } else if(name.equals(Debugger.NOTIF_DEBUG_STARTED)) {
            editorIdeas.hide();
        } else if(name.equals(Debugger.NOTIF_DEBUG_STOPPED)) {
        }
    }

    public void componentDidAwake() {
        updateInformation();
        updateCursorInfo();
        menuSCM.setSilent(true);
        menuSCM.queryFileStatus();

        // Request focus in the text pane
        getTextPane().requestFocus();
    }

    public void componentActivated() {
        syntax.resetTokenVocab();
        syntax.rebuildAll();
        textEditor.refresh();
    }

    public void componentIsSelected() {
        getTextPane().requestFocus();
    }
    
    public void componentDocumentContentChanged() {
        // Called when the document associated file has changed on the disk
        int oldCursorPosition = getCaretPosition();
        getDocument().reload();
        setCaretPosition(oldCursorPosition);
    }

    /** AutoCompletionMenuDelegate method: return the list of corresponding words
     *  given a partial word
     */

    public List autoCompletionMenuGetMatchingWordsForPartialWord(String partialWord) {
        if(parserEngine == null || parserEngine.getRules() == null)
            return null;

        partialWord = partialWord.toLowerCase();
        List matchingRules = new ArrayList();

        if(rules.isRuleAtIndex(getCaretPosition())) {
            // Inside a rule - show all rules in alphabetical order

            List sortedRules = Collections.list(Collections.enumeration(parserEngine.getRules()));
            Collections.sort(sortedRules);

            for(Iterator iterator = sortedRules.iterator(); iterator.hasNext(); ) {
                ParserRule rule = (ParserRule)iterator.next();
                if(rule.name.toLowerCase().startsWith(partialWord) && !matchingRules.contains(rule.name))
                    matchingRules.add(rule.name);
            }
        } else {
            // Not inside rule - show only undefined rules

            List sortedUndefinedReferences = Collections.list(Collections.enumeration(syntax.getUndefinedReferences()));
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
            immediateSyntaxColoring.colorize(offset, length);
            autoIndent.indent(offset, length);
        }

        changeDone();

        adjustTokens(offset, length);
        textEditor.changeOccurred();

        parserEngine.parse();
        visual.cancelDrawingProcess();
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

        ParserRule rule = rules.selectRuleInTreeAtPosition(index);
        if(rule == null || rule.name == null) {
            updateVisualization(false);
            lastSelectedRule = null;
            return;
        }

        if(lastSelectedRule == null || !lastSelectedRule.equals(rule.name)) {
            lastSelectedRule = rule.name;
            updateVisualization(false);
        }
    }

    public void executeFirstOpeningOperations() {
        // Called after parser has completed
        checkGrammarVersion();
    }

    public void checkGrammarVersion() {
        // Check to see if "class" and "extends" are in the grammar text which
        // means that the grammar is probably an ANTLR version 2 grammar.

        boolean version2 = false;
        List tokens = parserEngine.getTokens();
        for(int index=0; index<tokens.size(); index++) {
            ATEToken t = (ATEToken)tokens.get(index);
            if(t.type == ATEGenericLexer.TOKEN_ID && t.getAttribute().equals("class")) {
                if(index+2<tokens.size()) {
                    ATEToken t2 = (ATEToken)tokens.get(index+2);
                    if(t2.type == ATEGenericLexer.TOKEN_ID && t2.getAttribute().equals("extends")) {
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

    public boolean wasSaving = false;

    public boolean componentDocumentWillSave() {
        if(menuSCM.isFileWritable())
            return true;

        if(XJAlert.displayAlertYESNO(getWindowContainer(), "Cannot Save", "This file is currently closed in the SCM depot.\nDo you want to open it for edit before saving its content ?") == XJAlert.YES) {
            // Open the file using the SCM
            menuSCM.editFile();
            // Will save the file again once the SCM commands
            // is completed (see scmCommandsDidComplete)
            wasSaving = true;
        }
        return false;
    }

    public void scmCommandsDidComplete() {
        if(wasSaving) {
            wasSaving = false;
            getDocument().performSave(false);
        }
    }

    protected class TabbedPaneMouseListener extends MouseAdapter {

        protected static final int CLOSING_INDEX_LIMIT = 4;

        public void displayPopUp(MouseEvent event) {
            if(tabbedPane.getSelectedIndex() < CLOSING_INDEX_LIMIT)
                return;

            if(!event.isPopupTrigger())
                return;

            JPopupMenu popup = new JPopupMenu();
            JMenuItem item = new JMenuItem("Close");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if(tabbedPane.getSelectedIndex() < CLOSING_INDEX_LIMIT)
                        return;

                    tabs.remove(tabbedPane.getSelectedIndex());
                    tabbedPane.removeTabAt(tabbedPane.getSelectedIndex());
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

    protected class TabbedPaneChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            refreshMainMenuBar();
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
