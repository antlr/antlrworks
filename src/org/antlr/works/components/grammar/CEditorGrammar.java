package org.antlr.works.components.grammar;

import edu.usfca.xj.appkit.app.XJApplication;
import edu.usfca.xj.appkit.menu.XJMainMenuBar;
import edu.usfca.xj.appkit.menu.XJMenu;
import edu.usfca.xj.appkit.menu.XJMenuItem;
import edu.usfca.xj.appkit.menu.XJMenuItemCheck;
import edu.usfca.xj.appkit.swing.XJTree;
import edu.usfca.xj.appkit.text.XJURLLabel;
import edu.usfca.xj.appkit.undo.XJUndo;
import edu.usfca.xj.appkit.undo.XJUndoDelegate;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.foundation.XJUtils;
import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.ATEPanelDelegate;
import org.antlr.works.ate.ATETextPane;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.misc.ATEThread;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.completion.AutoCompletionMenu;
import org.antlr.works.completion.AutoCompletionMenuDelegate;
import org.antlr.works.completion.RuleTemplates;
import org.antlr.works.components.ComponentContainer;
import org.antlr.works.components.ComponentEditor;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.editor.*;
import org.antlr.works.find.FindAndReplace;
import org.antlr.works.grammar.EngineGrammar;
import org.antlr.works.interpreter.EditorInterpreter;
import org.antlr.works.menu.*;
import org.antlr.works.navigation.GoToHistory;
import org.antlr.works.navigation.GoToRule;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.syntax.*;
import org.antlr.works.utils.Console;
import org.antlr.works.visualization.Visual;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
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
        EditorProvider, ATEPanelDelegate,
        XJUndoDelegate
{

    /* Completion */

    public AutoCompletionMenu autoCompletionMenu;
    public RuleTemplates ruleTemplates;

    /* Tools */

    public FindAndReplace findAndReplace;

    public GoToRule goToRule;
    public GoToHistory goToHistory;

    /* Managers */

    public EditorBreakpointManager breakpointManager;
    public EditorFoldingManager foldingManager;
    public EditorUnderlyingManager underlyingManager;
    public EditorAnalysisManager analysisManager;

    /* Components */

    public GrammarSyntaxEngine parserEngine;
    public EditorRules rules;
    public Visual visual;
    public EditorInterpreter interpreter;
    public Debugger debugger;

    /* Editor */

    public EditorConsole console;
    public EditorToolbar toolbar;
    public EditorMenu editorMenu;
    public EditorIdeas editorIdeas;
    public EditorTips editorTips;
    public EditorInspector editorInspector;
    public EditorPersistence persistence;

    /* Menu */

    public MenuFolding menuFolding;
    public MenuFind menuFind;
    public MenuGrammar menuGrammar;
    public MenuRefactor menuRefactor;
    public MenuGoTo menuGoTo;
    public MenuGenerate menuGenerate;
    public MenuDebugger menuDebugger;
    public MenuSCM menuSCM;
    public MenuExport menuExport;

    public ATEPanel textEditor;

    /* Swing */

    protected JScrollPane rulesScrollPane;
    protected XJTree rulesTree;

    protected JTabbedPane tabbedPane;

    protected JLabel infoLabel;
    protected JLabel cursorLabel;
    protected JLabel writableLabel;
    protected JLabel scmLabel;
    protected ConsoleStatus consoleStatus;

    protected JSplitPane rulesTextSplitPane;
    protected JSplitPane upDownSplitPane;

    /* Other */

    protected boolean windowFirstDisplay = true;
    protected String lastSelectedRule;
    protected CEditorGrammarDelegate delegate;

    protected List tabs = new ArrayList();
    protected AfterParseOperations afterParserOp;

    /* Grammar */

    protected EngineGrammar engineGrammar;
    protected GrammarSyntax grammarSyntax;

    public CEditorGrammar(ComponentContainer container) {
        super(container);
        afterParserOp = new AfterParseOperations();
    }

    public void setDelegate(CEditorGrammarDelegate delegate) {
        this.delegate = delegate;
    }

    public void create() {
        initCore();

        createInterface();

        initEditor();
        initMenus();

        initManagers();
        initComponents();

        initAutoCompletion();
        initTools();

        awakeInstances();
        awakeInterface();

        register();
    }

    public void assemble() {
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

        delegate = new CEditorGrammarDefaultDelegate(upDownSplitPane);

        mainPanel.add(upDownSplitPane, BorderLayout.CENTER);
    }

    protected void initComponents() {
        rules = new EditorRules(this, rulesTree);
        visual = new Visual(this);
    }

    protected void initTools() {
        goToRule = new GoToRule(this, getXJFrame(), getTextPane());
        goToHistory = new GoToHistory();
        findAndReplace = new FindAndReplace(this);
    }

    protected void initAutoCompletion() {
        autoCompletionMenu = new AutoCompletionMenu(this, getTextPane(), getXJFrame());
        ruleTemplates = new RuleTemplates(this, getTextPane(), getXJFrame());
    }

    protected void initCore() {
        parserEngine = new GrammarSyntaxEngine();
        grammarSyntax = new GrammarSyntax(this);
        interpreter = new EditorInterpreter(this);
        debugger = new Debugger(this);                   
    }

    protected void initEditor() {
        console = new EditorConsole(this);
        console.makeCurrent();

        editorMenu = new EditorMenu(this);
        editorIdeas = new EditorIdeas(this);
        editorTips = new EditorTips(this);
        editorInspector = new EditorInspector(this);

        persistence = new EditorPersistence(this);

        engineGrammar = new EngineGrammar(this);
    }

    protected void initMenus() {
        menuFolding = new MenuFolding(this);
        menuFind = new MenuFind(this);
        menuGrammar = new MenuGrammar(this);
        menuRefactor = new MenuRefactor(this);
        menuGoTo = new MenuGoTo(this);
        menuGenerate = new MenuGenerate(this);
        menuDebugger = new MenuDebugger(this);
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

        toolbar.awake();

        rules.setKeyBindings(textEditor.getKeyBindings());

        textEditor.setParserEngine(parserEngine);
    }

    protected void createTextEditor() {
        textEditor = new ATEPanel(getXJFrame());
        textEditor.setEditorKit(new EditorATEEditorKit(this));
        textEditor.setSyntaxColoring(true);
        textEditor.setDelegate(this);
        applyPrefs();
    }

    protected void createRulesPane() {
        rulesTree = new XJTree() {
            public String getToolTipText(MouseEvent e) {
                TreePath path = getPathForLocation(e.getX(), e.getY());
                if(path == null)
                    return "";

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                EditorRules.RuleTreeUserObject n = (EditorRules.RuleTreeUserObject) node.getUserObject();
                if(n == null)
                    return "";

                GrammarSyntaxRule r = n.rule;
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
        rulesScrollPane.setWheelScrollingEnabled(true);
    }

    public JComponent getRulesComponent() {
        return rulesScrollPane;
    }

    protected void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
        tabbedPane.addMouseListener(new TabbedPaneMouseListener());
        tabbedPane.addChangeListener(new TabbedPaneChangeListener());
    }

    public JComponent getTabbedComponent() {
        return tabbedPane;
    }

    protected void createToolbar() {
        toolbar = new EditorToolbar(this);
    }

    protected void createStatusBar() {
        infoLabel = new JLabel();
        cursorLabel = new JLabel();
        writableLabel = new JLabel();
        scmLabel = new JLabel();
        consoleStatus = new ConsoleStatus();

        statusBar.add(Box.createHorizontalStrut(5));
        statusBar.add(infoLabel);
        statusBar.add(Box.createHorizontalStrut(5));
        statusBar.add(createSeparator());
        statusBar.add(Box.createHorizontalStrut(5));
        statusBar.add(cursorLabel);
        statusBar.add(Box.createHorizontalStrut(5));
        statusBar.add(createSeparator());
        statusBar.add(Box.createHorizontalStrut(5));
        statusBar.add(writableLabel);
        statusBar.add(Box.createHorizontalStrut(5));
        statusBar.add(createSeparator());
        statusBar.add(Box.createHorizontalStrut(5));
        statusBar.add(consoleStatus.getPanel());
        statusBar.add(Box.createHorizontalStrut(5));
        statusBar.add(createSeparator());
        statusBar.add(Box.createHorizontalStrut(5));
        statusBar.add(scmLabel);
    }

    protected void createInterface() {
        createTextEditor();
        createRulesPane();
        createTabbedPane();
        createToolbar();
        createStatusBar();
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
    }

    public void applyPrefs() {
        afterParserOp.setDefaultThreshold(AWPrefs.getParserDelay());
        textEditor.setFoldingEnabled(AWPrefs.getFoldingEnabled());
        textEditor.setHighlightCursorLine(AWPrefs.getHighlightCursorEnabled());
        textEditor.refresh();
        applyFont();
    }

    public void applyFont() {
        getTextPane().setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        getTextPane().setTabSize(AWPrefs.getEditorTabSize());
    }

    public void close() {
        afterParserOp.stop();
        toolbar.close();
        editorIdeas.close();
        editorMenu.close();
        debugger.close();
        visual.close();
        super.close();
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

    public void selectConsoleTab() {
        selectTab(console.getTabComponent());
        makeBottomComponentVisible();
    }

    public void addTab(EditorTab tab) {
        /** Replace any existing tab with this one if the title matches. Don't
         * replace the first three tabs because they are always visible.
         */
        int index = getSimilarTab(tab);
        if(index > 3) {
            tabs.remove(index);
            tabs.add(index, tab);
            tabbedPane.removeTabAt(index);
            tabbedPane.insertTab(tab.getTabName(), null, tab.getTabComponent(), null, index);
        } else {
            tabs.add(tab);
            tabbedPane.add(tab.getTabName(), tab.getTabComponent());
        }

        selectTab(tab.getTabComponent());
    }

    public int getSimilarTab(EditorTab tab) {
        for (int i = 0; i < tabs.size(); i++) {
            EditorTab t = (EditorTab) tabs.get(i);
            if(t.getTabName().equals(tab.getTabName()))
                return i;
        }
        return -1;
    }

    public EditorTab getSelectedTab() {
        return (EditorTab)tabs.get(tabbedPane.getSelectedIndex());
    }

    public void selectTab(Component c) {
        if(tabbedPane.getSelectedComponent() != c) {
            tabbedPane.setSelectedComponent(c);
            refreshMainMenuBar();
        }
    }

    public void makeBottomComponentVisible() {
        if(!isBottomComponentVisible()) {
            setBottomComponentVisible(true);
        }
    }

    public void setBottomComponentVisible(boolean visible) {
        if(delegate != null)
            delegate.setBottomComponentVisible(visible);
    }

    public boolean isBottomComponentVisible() {
        if(delegate != null)
            return delegate.isBottomComponentVisible();
        else
            return false;
    }

    public JComponent getToolbarComponent() {
        return toolbar.getToolbar();
    }

    public EngineGrammar getEngineGrammar() {
        return engineGrammar;
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

    public Debugger getDebugger() {
        return debugger;
    }

    public GrammarSyntax getSyntax() {
        return grammarSyntax;
    }

    public void toggleAutoIndent() {
        textEditor.setAutoIndent(!textEditor.autoIndent());
    }

    public void toggleSyntaxColoring() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_TOGGLE_SYNTAX_COLORING);
        textEditor.toggleSyntaxColoring();
    }

    public void toggleRulesSorting() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_TOGGLE_RULE_SORT);
        rules.toggleSorting();
        interpreter.setRules(getRules());
    }

    public void toggleSyntaxDiagram() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_TOGGLE_SYNTAX_DIAGRAM);
        visual.setEnable(!visual.isEnable());
        if(visual.isEnable()) {
            visual.setText(getText(), getFileName());
        }
        updateVisualization(false);
    }

    public void toggleNFAOptimization() {
        visual.toggleNFAOptimization();
        updateVisualization(false);
    }

    public void toggleIdeas() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_TOGGLE_IDEAS);
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

    public void changeUpdate() {
        ateChangeUpdate(-1, -1, false);
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
    }

    public void undoManagerDidUndo(boolean redo) {
        changeUpdate();
    }

    public void loadText(String text) {
        disableTextPaneUndo();
        try {
            textEditor.loadText(text);
            grammarChanged();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            enableTextPaneUndo();
        }
    }

    public void setText(String text) {
        getTextPane().setText(text);
        grammarChanged();
        textEditor.parse();
        changeDone();
    }

    public synchronized String getText() {
        return getTextPane().getText();
    }

    public void replaceText(int leftIndex, int rightIndex, String text) {
        textEditor.replaceText(leftIndex, rightIndex, text);
    }

    public void selectTextRange(int start, int end) {
        textEditor.selectTextRange(start, end);
    }

    public void deselectTextRange() {
        textEditor.deselectTextRange();
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

    public synchronized boolean isFileWritable() {
        return new File(getFilePath()).canWrite();
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
        return getXJFrame().getJavaContainer();
    }

    public GrammarSyntaxEngine getParserEngine() {
        return parserEngine;
    }

    public List getRules() {
        if(rules.isSorted()) {
            return rules.getSortedRules();
        } else
            return rules.getRules();
    }

    public List getSortedRules() {
        return rules.getSortedRules();
    }

    public List getBlocks() {
        return parserEngine.getBlocks();
    }

    public List getActions() {
        return parserEngine.getActions();
    }

    public List getReferences() {
        return parserEngine.getReferences();
    }

    public List getTokens() {
        return textEditor.getTokens();
    }

    public List getLines() {
        return textEditor.getLines();
    }

    public void goToHistoryRememberCurrentPosition() {
        goToHistory.addPosition(getCaretPosition());
        refreshMainMenuBar();
    }

    public GrammarSyntaxReference getCurrentReference() {
        return getReferenceAtPosition(getCaretPosition());
    }

    public GrammarSyntaxReference getReferenceAtPosition(int pos) {
        List refs = getReferences();
        for(int index=0; index<refs.size(); index++) {
            GrammarSyntaxReference ref = (GrammarSyntaxReference)refs.get(index);
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

    public GrammarSyntaxRule getCurrentRule() {
        return rules.getEnclosingRuleAtPosition(getCaretPosition());
    }

    public GrammarSyntaxAction getCurrentAction() {
        List actions = parserEngine.getActions();
        int position = getCaretPosition();
        for(int index=0; index<actions.size(); index++) {
            GrammarSyntaxAction action = (GrammarSyntaxAction)actions.get(index);
            if(action.containsIndex(position))
                return action;
        }
        return null;
    }

    public void setCaretPosition(int position) {
        setCaretPosition(position, AWPrefs.getSmoothScrolling());
    }

    public void setCaretPosition(int position, boolean animate) {
        GrammarSyntaxRule rule = rules.getEnclosingRuleAtPosition(position);
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

    public void handleMenuSelected(XJMenu menu) {
        editorMenu.handleMenuSelected(menu);
    }

    /** Update methods
     */

    public void updateVisualization(boolean immediate) {
        if(visual.isEnable()) {
            GrammarSyntaxRule r = rules.getEnclosingRuleAtPosition(getCaretPosition());
            if(r == null) {
                visual.setPlaceholder("Select a rule to display its syntax diagram");
            } else {
                if(r.hasErrors() && r.needsToBuildErrors()) {
                    engineGrammar.computeRuleErrors(r);
                }

                visual.setRule(r, immediate);
            }
        } else {
            visual.setPlaceholder("Syntax Diagram Disabled");
        }
    }

    /** This method gets called by EngineGrammar once the grammar has been analyzed. It has
     * to update the syntax diagram and the rule information to reflect any error detected.
     */

    public void engineGrammarDidAnalyze() {
        visual.update();
        updateInformation();
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

        int warnings = grammarSyntax.getNumberOfRulesWithErrors();
        if(warnings > 0)
            t += " ("+warnings+" warning"+(warnings>0?"s":"")+")";

        infoLabel.setText(t);

        if(isFileWritable()) {
            writableLabel.setText("Writable");
        } else {
            writableLabel.setText("Read-only");
        }
    }

    public void updateCursorInfo() {
        cursorLabel.setText(textEditor.getCurrentLinePosition()+":"+textEditor.getCurrentColumnPosition());
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

    public void rulesDidChange() {
        interpreter.updateIgnoreTokens(getRules());
    }

    public JPopupMenu rulesGetContextualMenu(List selectedObjects) {
        if(selectedObjects.isEmpty())
            return null;

        ContextualMenuFactory factory = new ContextualMenuFactory(editorMenu);
        factory.addItem(EditorMenu.MI_GROUP_RULE);
        factory.addItem(EditorMenu.MI_UNGROUP_RULE);
        factory.addSeparator();
        XJMenuItemCheck item = (XJMenuItemCheck) factory.addItem(EditorMenu.MI_IGNORE_RULE);
        item.setSelected(rules.getFirstSelectedRuleIgnoredFlag());

        return factory.menu;
    }

    /** Parser delegate methods
     */
    public void ateParserWillParse() {                  
        persistence.store();
    }

    public void ateParserDidParse() {
        updateInformation();
        updateCursorInfo();

        if(windowFirstDisplay) {
            windowFirstDisplay = false;
            afterParseOperations();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateVisualization(true);
                    executeFirstOpeningOperations();
                    findTokensToIgnore();
                }
            });
        } else {
            afterParserOp.awakeThread();
        }
    }

    public void afterParseOperations() {
        persistence.restore();

        interpreter.setRules(getRules());
        rules.parserDidParse();
        grammarSyntax.parserDidParse();

        // Make sure to invoke the ideas after Rules
        // has completely updated its list (which should
        // be done inside rules.parserDidParse()
        editorIdeas.display(getCaretPosition());

        visual.setText(getText(), getFileName());
        updateVisualization(false);

        // Damage the editor and repaint it
        textEditor.damage();
        textEditor.repaint();
    }

    public void changeDone() {
        grammarChanged();
        getDocument().changeDone();
    }

    public boolean ensureDocumentSaved() {
        if(getDocument().getDocumentPath() == null)
            return getDocument().performSave(false) == XJApplication.YES;
        else
            return true;
    }

    public void grammarChanged() {
        engineGrammar.makeDirty();
    }

    public void consolePrint(String s, int level) {
        consoleStatus.showLevel(level);
    }

    public void clearConsoleStatus() {
        consoleStatus.clearMessage();
    }

    public void notificationPrefsChanged() {
        applyPrefs();
        updateSCMStatus(null);
    }

    public void notificationDebuggerStarted() {
        refreshMainMenuBar();
        editorIdeas.hide();
    }

    public void componentShouldLayout(Dimension size) {
        if(rulesTextSplitPane != null)
            rulesTextSplitPane.setDividerLocation((int)(size.width*0.2));
        if(upDownSplitPane != null)
            upDownSplitPane.setDividerLocation((int)(size.height*0.5));

        interpreter.componentShouldLayout();
        debugger.componentShouldLayout(size);
    }

    public void componentDidAwake() {
        updateInformation();
        updateCursorInfo();
        menuSCM.setSilent(true);
        menuSCM.queryFileStatus();

        // Request focus in the text pane
        getTextPane().requestFocusInWindow();
    }

    public void componentActivated() {
        grammarSyntax.resetTokenVocab();
        grammarSyntax.rebuildAll();
        textEditor.getTextPane().setEditable(isFileWritable());
        textEditor.refresh();
        updateInformation();
    }

    public void componentDidHide() {
        // Hide the ideas if the component is hidden. For example, in the project
        // window, if a component is hidden, the ideas have to be also hidden otherwise
        // they are floating above the new visible component which is weird.
        editorIdeas.hide();
    }

    public void componentIsSelected() {
        getTextPane().requestFocusInWindow();
    }

    public void componentDocumentContentChanged() {
        // Called when the document associated file has changed on the disk
        int oldCursorPosition = getCaretPosition();
        getDocument().reload();
        grammarChanged();
        setCaretPosition(Math.min(oldCursorPosition, getText().length()));
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
                GrammarSyntaxRule rule = (GrammarSyntaxRule)iterator.next();
                if(rule.name.toLowerCase().startsWith(partialWord) && !matchingRules.contains(rule.name))
                    matchingRules.add(rule.name);
            }
        } else {
            // Not inside rule - show only undefined rules

            List sortedUndefinedReferences = Collections.list(Collections.enumeration(grammarSyntax.getUndefinedReferences()));
            Collections.sort(sortedUndefinedReferences);

            for(Iterator iterator = sortedUndefinedReferences.iterator(); iterator.hasNext(); ) {
                GrammarSyntaxReference ref = (GrammarSyntaxReference)iterator.next();
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
        changeDone();
        visual.cancelDrawingProcess();
    }

    public void ateAutoIndent(int offset, int length) {
        try {
            GrammarAutoIndent.autoIndentOnSpecificKeys(this, getTextPane().getDocument(), offset, length);
        } catch (BadLocationException e) {
            // ignore
        }
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

    public void ateInvokePopUp(Component component, int x, int y) {
        JPopupMenu m = editorMenu.getContextualMenu();
        if(m != null)
            m.show(component,  x, y);
    }

    public void ateCaretUpdate(int index) {
        updateCursorInfo();
        if(getTextPane().hasFocus()) {
            editorIdeas.hide();
            if(!debugger.isRunning())
                editorIdeas.display(getCaretPosition());
        }

        // Update the auto-completion list
        autoCompletionMenu.updateAutoCompleteList();

        // Only display ideas using the mouse because otherwise when a rule
        // is deleted (for example), the idea might be displayed before
        // the parser was able to complete
        // display(e.getDot());
        GrammarSyntaxRule rule = rules.selectRuleInTreeAtPosition(index);
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
            if(t.type == ATESyntaxLexer.TOKEN_ID && t.getAttribute().equals("class")) {
                if(index+2<tokens.size()) {
                    ATEToken t2 = (ATEToken)tokens.get(index+2);
                    if(t2.type == ATESyntaxLexer.TOKEN_ID && t2.getAttribute().equals("extends")) {
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

    public void findTokensToIgnore() {
        rules.findTokensToIgnore();
        interpreter.setRules(getRules());
    }

    public boolean wasSaving = false;

    public boolean componentDocumentWillSave() {
        AWPrefs.setLastSavedDocument(getFilePath());

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

    public static final String KEY_SPLITPANE_A = "KEY_SPLITPANE_A";
    public static final String KEY_SPLITPANE_B = "KEY_SPLITPANE_B";
    public static final String KEY_INTERPRETER = "KEY_INTERPRETER";
    public static final String KEY_DEBUGGER = "KEY_DEBUGGER";

    public void setPersistentData(Map data) {
        if(data == null)
            return;

        Integer i = (Integer)data.get(KEY_SPLITPANE_A);
        if(i != null && rulesTextSplitPane != null)
            rulesTextSplitPane.setDividerLocation(i.intValue());

        i = (Integer)data.get(KEY_SPLITPANE_B);
        if(i != null && upDownSplitPane != null)
            upDownSplitPane.setDividerLocation(i.intValue());

        interpreter.setPersistentData((Map) data.get(KEY_INTERPRETER));
        debugger.setPersistentData((Map) data.get(KEY_DEBUGGER));
    }

    public Map getPersistentData() {
        Map data = new HashMap();
        if(rulesTextSplitPane != null)
            data.put(KEY_SPLITPANE_A, new Integer(rulesTextSplitPane.getDividerLocation()));
        if(upDownSplitPane != null)
            data.put(KEY_SPLITPANE_B, new Integer(upDownSplitPane.getDividerLocation()));
        data.put(KEY_INTERPRETER, interpreter.getPersistentData());
        data.put(KEY_DEBUGGER, debugger.getPersistentData());
        return data;
    }

    /** This class is used to perform after parsing operations in another
     * thread than the main event thread.
     */
    protected class AfterParseOperations extends ATEThread {

        public AfterParseOperations() {
            start();
        }

        protected void threadRun() throws Exception {
            afterParseOperations();
        }
    }

    protected class ConsoleStatus {

        public Box box;
        public XJURLLabel label;
        public boolean visible;
        public int currentDisplayedLevel;

        public ConsoleStatus() {
            box = Box.createHorizontalBox();

            label = new XJURLLabel(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectConsoleTab();
                    clearMessage();
                }
            });

            clearMessage();
        }

        public void showMessage(String message, Color color) {
            label.setText(message);
            label.setUnvisitedURLColor(color);
            label.setVisitedURLColor(color);
            label.repaint();
        }

        public void showLevel(int level) {
            if(level == Console.LEVEL_NORMAL)
                return;

            if(!visible) {
                visible = true;
                box.removeAll();
                box.add(label);
                box.revalidate();
            }

            if(level > currentDisplayedLevel) {
                currentDisplayedLevel = level;
                if(level == Console.LEVEL_ERROR)
                    showMessage("Errors reported in console", Color.red);
                else
                    showMessage("Warnings reported in console", Color.blue);
            }
        }

        public void clearMessage() {
            label.setText("");
            box.removeAll();
            box.add(Box.createHorizontalStrut(20));
            visible = false;
            currentDisplayedLevel = Console.LEVEL_NORMAL;
        }

        public JComponent getPanel() {
            return box;
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

}
