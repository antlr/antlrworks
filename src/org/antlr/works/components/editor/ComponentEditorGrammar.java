package org.antlr.works.components.editor;

import org.antlr.Tool;
import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.ATEPanelDelegate;
import org.antlr.works.ate.ATETextPane;
import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.ate.syntax.misc.ATEThread;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.container.ComponentContainerGrammarMenu;
import org.antlr.works.editor.*;
import org.antlr.works.editor.completion.AutoCompletionMenu;
import org.antlr.works.editor.completion.AutoCompletionMenuDelegate;
import org.antlr.works.editor.completion.RuleTemplates;
import org.antlr.works.editor.navigation.GoToHistory;
import org.antlr.works.editor.navigation.GoToRule;
import org.antlr.works.find.FindAndReplace;
import org.antlr.works.grammar.GrammarAutoIndent;
import org.antlr.works.grammar.decisiondfa.DecisionDFAEngine;
import org.antlr.works.grammar.element.*;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.works.grammar.engine.GrammarEngineDelegate;
import org.antlr.works.grammar.engine.GrammarEngineImpl;
import org.antlr.works.interpreter.EditorInterpreter;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.utils.Console;
import org.antlr.works.utils.Utils;
import org.antlr.works.visualization.Visual;
import org.antlr.xjlib.appkit.menu.XJMenuItemCheck;
import org.antlr.xjlib.appkit.swing.XJTree;
import org.antlr.xjlib.appkit.text.XJURLLabel;
import org.antlr.xjlib.appkit.undo.XJUndo;
import org.antlr.xjlib.appkit.undo.XJUndoDelegate;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.utils.XJDialogProgress;
import org.antlr.xjlib.appkit.utils.XJDialogProgressDelegate;
import org.antlr.xjlib.foundation.XJSystem;
import org.antlr.xjlib.foundation.XJUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
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

public class ComponentEditorGrammar extends ComponentEditor implements AutoCompletionMenuDelegate,
        ATEPanelDelegate, XJUndoDelegate, InspectorDelegate, GrammarEngineDelegate {

    /* Completion */

    public AutoCompletionMenu autoCompletionMenu;
    public RuleTemplates ruleTemplates;

    /* Tools */

    public FindAndReplace findAndReplace;
    public DecisionDFAEngine decisionDFAEngine;

    public GoToRule goToRule;
    public GoToHistory goToHistory;

    /* Managers */

    public EditorGutterColumnManager gutterColumnManager;
    public EditorFoldingManager foldingManager;
    public EditorUnderlyingManager underlyingManager;
    public EditorAnalysisManager analysisManager;

    /* Components */

    public GrammarEngine engine;
    public EditorRules rules;
    public Visual visual;
    public EditorInterpreter interpreter;

    /* Editor */

    public EditorConsole console;
    public EditorIdeas editorIdeas;
    public EditorTips editorTips;
    public EditorInspector editorInspector;
    public EditorPersistence persistence;
    public EditorATEEditorKit editorKit;

    public ATEPanel textEditor;

    /* Swing */

    private JScrollPane rulesScrollPane;
    private XJTree rulesTree;

    private JLabel infoLabel;
    private JLabel cursorLabel;
    private JLabel writableLabel;
    private ConsoleStatus consoleStatus;

    /* Other */

    private int debuggerLocation = -1;
    private Jumpable highlightedReference;
                          
    private boolean windowFirstDisplay = true;
    private String lastSelectedRule;
    private ComponentEditorGrammarDelegate delegate;

    private AfterParseOperations afterParserOp;

    /* Progress */
    private XJDialogProgress progress;

    public ComponentEditorGrammar() {

    }

    public void setDelegate(ComponentEditorGrammarDelegate delegate) {
        this.delegate = delegate;
    }

    public void create() {
        initCore();

        createInterface();

        initEditor();
        initManagers();
        initComponents();
        initAutoCompletion();
        initTools();

        awakeInstances();

        register();
    }

    public void assemble() {
        // todo still used?
        //delegate = new ComponentEditorGrammarDefaultDelegate(upDownSplitPane);

        mainPanel.add(textEditor, BorderLayout.CENTER);
    }

    public Component getComponentRules() {
        return rulesScrollPane;
    }

    public Component getComponentEditor() {
        return textEditor;
    }

    public EditorTab getComponentSD() {
        return visual;
    }

    public EditorTab getComponentInterpreter() {
        return interpreter;
    }

    public EditorTab getComponentConsole() {
        return console;
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
        if(autoCompletionMenu != null) {
            autoCompletionMenu.close();
        }
        autoCompletionMenu = new AutoCompletionMenu(this, getTextPane(), getXJFrame());

        if(ruleTemplates != null) {
            ruleTemplates.close();
        }
        ruleTemplates = new RuleTemplates(this, getTextPane(), getXJFrame());
    }

    protected void initCore() {
        afterParserOp = new AfterParseOperations();

        engine = new GrammarEngineImpl(this);

        decisionDFAEngine = new DecisionDFAEngine(this);
        interpreter = new EditorInterpreter(this);
    }

    protected void initEditor() {
        console = new EditorConsole(this);
        console.makeCurrent();

        editorIdeas = new EditorIdeas(this);
        editorTips = new EditorTips(this);
        editorInspector = new EditorInspector(engine, decisionDFAEngine, this);

        persistence = new EditorPersistence(this);
    }

    protected void initManagers() {
        gutterColumnManager = new EditorGutterColumnManager(this);
        textEditor.setGutterColumnManager(gutterColumnManager);

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

        interpreter.awake();

        rules.setKeyBindings(textEditor.getKeyBindings());

        textEditor.setParserEngine(engine.getSyntaxEngine());
    }

    protected void createTextEditor() {
        textEditor = new ATEPanel(getXJFrame());
        textEditor.setEditorKit(editorKit = new EditorATEEditorKit(this));
        textEditor.setSyntaxColoring(true);
        textEditor.setDelegate(this);
        applyPrefs();
    }

    protected void createRulesPane() {
        rulesTree = new RuleTree();

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

    protected void createStatusBar() {
        infoLabel = new JLabel();
        cursorLabel = new JLabel();
        writableLabel = new JLabel();
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
        statusBar.add(Box.createHorizontalGlue());
    }

    protected void createInterface() {
        createTextEditor();
        createRulesPane();
        createStatusBar();
    }

    protected void register() {
        getXJFrame().registerUndo(this, getTextPane());
    }

    public void applyPrefs() {
        afterParserOp.setDefaultThreshold(AWPrefs.getParserDelay());
        textEditor.setFoldingEnabled(AWPrefs.getFoldingEnabled());
        textEditor.setLineNumberEnabled(AWPrefs.getLineNumberEnabled());
        textEditor.setHighlightCursorLine(AWPrefs.getHighlightCursorEnabled());
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
        ruleTemplates.close();

        decisionDFAEngine.close();
        interpreter.close();

        console.close();
        editorIdeas.close();
        editorTips.close();
        editorInspector.close();

        persistence.close();
        engine.close();

        rules.close();
        visual.close();

        afterParserOp.stop();
        afterParserOp = null;

        gutterColumnManager.close();
        foldingManager.close();
        underlyingManager.close();
        analysisManager.close();

        textEditor.close();

        getXJFrame().unregisterUndo(this);

        editorKit.close();

        consoleStatus = null;
        rulesTree.close();
        rulesTree = null;

        super.close();
    }

    public void addTab(EditorTab tab) {
        container.addTab(tab);
    }

    public void selectVisualizationTab() {
        container.selectSyntaxDiagramTab(this);
        makeBottomComponentVisible();
    }

    public void selectInterpreterTab() {
        container.selectInterpreterTab(this);
        makeBottomComponentVisible();
    }

    public void selectConsoleTab() {
        container.selectConsoleTab(this);
        makeBottomComponentVisible();
    }

    public EditorTab getSelectedTab() {
        return container.getSelectedTab();
    }

    public void selectTab(Component c) {
        container.selectTab(c);
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
        return delegate != null && delegate.isBottomComponentVisible();
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

    public void gotoToRule(String grammar, final String name) {
        if(!grammar.equals(engine.getGrammarName())) {
            // rule is in another editor
            final ComponentEditorGrammar editor = getContainer().selectGrammar(grammar);
            // set the caret position
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    editor.gotoToRule(name);
                }
            });
        } else {
            // rule is in current editor
            gotoToRule(name);
        }
    }

    public void gotoToRule(String name) {
        int index = engine.getFirstDeclarationPosition(name);
        if(index != -1) {
            setCaretPosition(index);
        }
    }

    public void toggleAutoIndent() {
        textEditor.setAutoIndent(!textEditor.autoIndent());
    }

    public void toggleSyntaxColoring() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_TOGGLE_SYNTAX_COLORING);
        textEditor.toggleSyntaxColoring();
    }

    public boolean isSyntaxColored() {
        return textEditor.isSyntaxColoring();
    }

    public void toggleRulesSorting() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_TOGGLE_RULE_SORT);
        rules.toggleSorting();
        interpreter.setRules(getRules());
    }

    public boolean isRulesSorted() {
        return rules.isSorted();
    }

    public void toggleSyntaxDiagram() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_TOGGLE_SYNTAX_DIAGRAM);
        visual.setEnable(!visual.isEnabled());
        if(visual.isEnabled()) {
            visual.setText(getText(), getFileName());
        }
        updateVisualization(false);
    }

    public boolean isSyntaxDiagramDisplayed() {
        return visual.isEnabled();
    }

    public void toggleNFAOptimization() {
        visual.toggleNFAOptimization();
        updateVisualization(false);
    }

    public void toggleIdeas() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_TOGGLE_IDEAS);
        editorIdeas.toggleEnabled();
    }

    public boolean isIdeasEnabled() {
        return editorIdeas.isEnabled();
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
        textEditor.setText(text);
        grammarChanged();
        textEditor.parse();
        changeDone();
    }

    public synchronized String getText() {
        return getTextPane().getText();
    }

    public String getGrammarFileName() {
        String fileName = getFileName();
        return fileName==null?"<notsaved>":fileName;
    }

    public String getGrammarText() {
        return getText();
    }

    public void reportError(String error) {
        getConsole().println(error, Console.LEVEL_ERROR);
    }

    public void reportError(Exception e) {
        getConsole().println(e);
    }

    public Tool getANTLRTool() {
        String[] params = AWPrefs.getANTLR3Options();
        if(getFileFolder() != null) {
            params = Utils.concat(params, new String[] { "-lib", getFileFolder() });
        }
        if(params.length > 0) {
            return new Tool(params);
        } else {
            return new Tool();
        }
    }

    /**
     * This method gets called when the grammar has been analyzed by ANTLR. It has
     * to update the syntax diagram and the rule information to reflect any error detected.
     */
    public void engineAnalyzeCompleted() {
        // Clear graphic cache because we have to redraw each rule again
        visual.clearCacheGraphs();
        rules.refreshRules();

        // Try to update the graph first and if they cannot be updated (i.e. the cache is empty), draw them again.
        if(!visual.update()) {
            updateVisualization(true);
        }
        updateInformation();
    }

    public void createRuleAtIndex(boolean lexer, String name, String content) {
        container.getActionRefactor().createRuleAtIndex(lexer, name, content);
    }

    public void deleteRuleAtCurrentPosition() {
        container.getActionRefactor().deleteRuleAtIndex(getCaretPosition());
    }

    public void removeLeftRecursion() {
        container.getActionRefactor().removeLeftRecursion();
    }

    public void convertLiteralsToSingleQuote() {
        container.getActionRefactor().convertLiteralsToSingleQuote();
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

    public void setDebuggerLocation(int index) {
        this.debuggerLocation = index;
        if(index != -1) {
            textEditor.getTextPane().setCaretPosition(index);            
        }
    }

    public int getDebuggerLocation() {
        return debuggerLocation;
    }

    public int getSelectionLeftIndexOnTokenBoundary() {
        ATEToken t = getTokenAtPosition(getTextPane().getSelectionStart(), true);
        if(t == null)
            return -1;
        else
            return t.getStartIndex();
    }

    public int getSelectionRightIndexOnTokenBoundary() {
        ATEToken t = getTokenAtPosition(getTextPane().getSelectionEnd(), false);
        if(t == null)
            return -1;
        else
            return t.getEndIndex();
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

    public synchronized String getFileFolder() {
        return XJUtils.getPathByDeletingLastComponent(getFilePath());
    }

    public String getTokenVocabFile(String tokenVocabName) {
        String filePath = getFileFolder();
        if(filePath == null) {
            return null;
        }

        String path = XJUtils.concatPath(filePath, tokenVocabName);
        if(new File(path).exists()) {
            return path;
        }

        // No token vocab file in the default directory. Try in the output path.
        path = XJUtils.concatPath(getOutputPath(), tokenVocabName);
        if(new File(path).exists()) {
            return path;
        }

        return null;
    }

    public synchronized String getFilePath() {
        return getDocument().getDocumentPath();
    }

    public synchronized String getFileName() {
        return getDocument().getDocumentName();
    }

    public String getOutputPath() {
        String path = AWPrefs.getOutputPath();
        if(path.startsWith("/") || path.startsWith("\\")) {
            // absolute path
            return path;
        }
        return XJUtils.concatPath(XJUtils.getPathByDeletingLastComponent(getFilePath()), path);
    }

    public Container getWindowContainer() {
        return getXJFrame().getJavaContainer();
    }

    public GrammarEngine getGrammarEngine() {
        return engine;
    }

    public List<ElementRule> getRules() {
        return rules.getRules();
    }

    public List<ElementRule> getSortedRules() {
        return rules.getSortedRules();
    }

    public List<ATEToken> getTokens() {
        return textEditor.getTokens();
    }

    public List<ATELine> getLines() {
        return textEditor.getLines();
    }

    public void goToHistoryRememberCurrentPosition() {
        goToHistory.addPosition(getCaretPosition());
        refreshMainMenuBar();
    }

    public ElementReference getCurrentReference() {
        return getReferenceAtPosition(getCaretPosition());
    }

    public ElementReference getReferenceAtPosition(int pos) {
        for (ElementReference ref : engine.getReferences()) {
            if (ref.containsIndex(pos))
                return ref;
        }
        return null;
    }

    public ElementImport getImportAtPosition(int pos) {
        for (ElementImport element : engine.getImports()) {
            if (element.containsIndex(pos))
                return element;
        }
        return null;
    }

    public ATEToken getCurrentToken() {
        return getTokenAtPosition(getCaretPosition(), false);
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

    public ElementRule getCurrentRule() {
        return rules.getEnclosingRuleAtPosition(getCaretPosition());
    }

    public ElementAction getCurrentAction() {
        List<ElementAction> actions = engine.getActions();
        int position = getCaretPosition();
        for (ElementAction action : actions) {
            if (action.containsIndex(position))
                return action;
        }
        return null;
    }

    public void setCaretPosition(int position) {
        setCaretPosition(position, AWPrefs.getSmoothScrolling());
    }

    public void setCaretPosition(int position, boolean animate) {
        ElementRule rule = rules.getEnclosingRuleAtPosition(position);
        if(rule != null && !rule.isExpanded()) {
            foldingManager.toggleFolding(rule);
        }
        textEditor.setCaretPosition(position, true, animate);
    }

    public int getCaretPosition() {
        return textEditor.getCaretPosition();
    }

    public void updateVisualization(boolean immediate) {
        if(visual.isEnabled()) {
            ElementRule r = rules.getEnclosingRuleAtPosition(getCaretPosition());
            if(r == null) {
                visual.setPlaceholder("Select a rule to display its syntax diagram");
            } else {
                if(r.hasErrors() && r.needsToBuildErrors()) {
                    engine.computeRuleErrors(r);
                    try {
                        visual.createGraphsForRule(r);
                    } catch (Exception e) {
                        // ignore
                    }
                }

                visual.setRule(r, immediate);
            }
        } else {
            visual.setPlaceholder("Syntax Diagram Disabled");
        }
    }

    public void updateInformation() {
        String t;
        int size = engine.getNumberOfRules();
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

        int warnings = engine.getNumberOfErrors();
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

        ContextualMenuFactory factory = container.createContextualMenuFactory();
        factory.addItem(ComponentContainerGrammarMenu.MI_GROUP_RULE);
        factory.addItem(ComponentContainerGrammarMenu.MI_UNGROUP_RULE);
        factory.addSeparator();
        XJMenuItemCheck item = (XJMenuItemCheck) factory.addItem(ComponentContainerGrammarMenu.MI_IGNORE_RULE);
        item.setSelected(rules.getFirstSelectedRuleIgnoredFlag());

        return factory.menu;
    }

    /** Parser delegate methods
     */
    public void ateEngineWillParse() {
        persistence.store();
    }

    public void ateEngineDidParse() {
        updateInformation();
        updateCursorInfo();

        if(windowFirstDisplay) {
            windowFirstDisplay = false;
            afterParseOperations();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateVisualization(true);
                    executeFirstOpeningOperations();
                    findTokensToIgnore(true);
                }
            });
        } else {
            afterParserOp.awakeThread();
        }
    }

    private void afterParseOperations() {
        engine.parserCompleted();
        container.editorParsed(this);

        persistence.restore();

        interpreter.setRules(getRules());
        rules.parserDidParse();
        decisionDFAEngine.reset();
        decisionDFAEngine.refreshMenu();

        // Make sure to invoke the ideas after Rules
        // has completely updated its list (which should
        // be done inside rules.parserDidParse())
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
        return getDocument().getDocumentPath() != null || getDocument().save(false);
    }

    private void grammarChanged() {
        engine.markDirty();
        container.editorContentChanged();
    }

    public void consolePrint(String s, int level) {
        consoleStatus.showLevel(level);
    }

    public void clearConsoleStatus() {
        consoleStatus.clearMessage();
    }

    public void setHighlightedReference(Jumpable highlightedReference) {
        if(highlightedReference != this.highlightedReference) {
            textEditor.repaint();
        }
        this.highlightedReference = highlightedReference;
    }

    public Jumpable getHighlightedReference() {
        return highlightedReference;
    }


    @Override
    public void notificationPrefsChanged() {
        applyPrefs();
    }

    @Override
    public void notificationDebuggerStarted() {
        refreshMainMenuBar();
        editorIdeas.hide();
    }

    @Override
    public void setEditable(boolean flag) {
        textEditor.setEditable(flag);
        if(flag) {
            getTextPane().requestFocusInWindow();

            // Tells the caret to be visible a little bit later
            // to let Swing focus the component
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    getTextPane().getCaret().setVisible(true);
                }
            });
        } else {
            getTextPane().getCaret().setVisible(flag);
        }
    }

    @Override
    public void componentShouldLayout(Dimension size) {
        interpreter.componentShouldLayout();
    }

    @Override
    public void componentDidAwake() {
        updateInformation();
        updateCursorInfo();

        // Request focus in the text pane. A little bit later because
        // in desktop mode, the focus is not taken into account if
        // requested immediately.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getTextPane().requestFocusInWindow();
            }
        });
    }

    @Override
    public void componentActivated() {
        console.makeCurrent();
        engine.reset();
        textEditor.getTextPane().setWritable(isFileWritable());
        textEditor.refresh();
        updateInformation();
    }

    @Override
    public void componentDidHide() {
        // Hide the ideas if the component is hidden. For example, in the project
        // window, if a component is hidden, the ideas have to be also hidden otherwise
        // they are floating above the new visible component which is weird.
        editorIdeas.hide();
    }

    @Override
    public void componentIsSelected() {
        getTextPane().requestFocusInWindow();
    }

    public void componentDocumentContentChanged() {
        // Called when the document associated file has changed on the disk
        int oldCursorPosition = getCaretPosition();
        try {
            getDocument().reload();
        } catch (Exception e) {
            e.printStackTrace();
            XJAlert.display(getWindowContainer(), "Error Reloading Document", "An error occurred when reloading the document:\n"+e.toString());
        }
        grammarChanged();
        setCaretPosition(Math.min(oldCursorPosition, getText().length()));
    }

    /** AutoCompletionMenuDelegate method: return the list of corresponding words
     *  given a partial word
     */

    public List<String> autoCompletionMenuGetMatchingWordsForPartialWord(String partialWord) {
        if(engine.getNumberOfRules() == 0) {
            return null;
        }

        partialWord = partialWord.toLowerCase();
        List<String> matchingRules = new ArrayList<String>();

        if(rules.isRuleAtIndex(getCaretPosition())) {
            // Inside a rule - show all rules in alphabetical order

            List<ElementRule> sortedRules = Collections.list(Collections.enumeration(engine.getRules()));
            Collections.sort(sortedRules,new Comparator<ElementRule>() {
                public int compare(ElementRule o1, ElementRule o2) {
                    return o1.name.compareToIgnoreCase(o2.name);
                }
            });

            for (ElementRule rule : sortedRules) {
                if (rule.name.toLowerCase().startsWith(partialWord) && !matchingRules.contains(rule.name))
                    matchingRules.add(rule.name);
            }
        } else {
            // Not inside rule - show only undefined rules

            List<ElementReference> sortedUndefinedReferences = Collections.list(Collections.enumeration(engine.getUndefinedReferences()));
            Collections.sort(sortedUndefinedReferences,new Comparator<ElementReference>() {
                public int compare(ElementReference o1, ElementReference o2) {
                    return o1.rule.name.compareToIgnoreCase(o2.rule.name);
                }
            });

            for (ElementReference ref : sortedUndefinedReferences) {
                String attr = ref.token.getAttribute();
                if (attr.toLowerCase().startsWith(partialWord)
                        && !attr.equals(partialWord)
                        && !matchingRules.contains(attr)) {
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
        if(textEditor.getTextPane().isWritable()) {
            editorIdeas.display(point);
        }
        if(highlightedReference != null) {
            container.getActionGoTo().goToDeclaration(highlightedReference);
            highlightedReference = null;
        }
    }

    public void ateMouseExited() {
        if(getTextPane().hasFocus()) {
            // Do not hide the ideas because
            // otherwise we don't be able to access the idea
            editorTips.hide();
        }
    }

    public void ateMouseMoved(MouseEvent event) {
        Point pt = event.getPoint();
        if(getTextPane().hasFocus()) {
            Point absolutePoint = SwingUtilities.convertPoint(getTextPane(), pt, getJavaContainer());
            editorTips.display(pt, absolutePoint);

        }

        setHighlightedReference(null);
        if(event.isMetaDown() && XJSystem.isMacOS() || event.isControlDown()) {
            int index = textEditor.getTextIndexAtPosition(pt.x, pt.y);
            Jumpable ref = getReferenceAtPosition(index);
            if(ref == null) {
                ref = getImportAtPosition(index);
            }
            setHighlightedReference(ref);
        }
    }

    public void ateInvokePopUp(Component component, int x, int y) {
        JPopupMenu m = container.getContextualMenu(textEditor.getTextIndexAtPosition(x, y));
        if(m != null)
            m.show(component,  x, y);
    }

    public void ateCaretUpdate(int index) {
        updateCursorInfo();
        if(getTextPane().hasFocus()) {
            editorIdeas.hide();
            if(textEditor.getTextPane().isWritable()) {
                editorIdeas.display(getCaretPosition());
            }
        }

        // Update the auto-completion list
        autoCompletionMenu.updateAutoCompleteList();

        // Only display ideas using the mouse because otherwise when a rule
        // is deleted (for example), the idea might be displayed before
        // the parser was able to complete
        // display(e.getDot());
        ElementRule rule = rules.selectRuleInTreeAtPosition(index);
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
        if(engine.isVersion2()) {
            XJAlert.display(getWindowContainer(), "Incompatible Grammar Version", "This grammar does not appear to be an ANTLR 3.x grammar." +
                    "\nANTLRWorks includes ANTLR 3.x and therefore only ANTLR 3.x grammars are recognized.");
        }
    }

    public void findTokensToIgnore(boolean reset) {
        rules.findTokensToIgnore(reset);
        interpreter.setRules(getRules());
    }

    @Override
    public boolean componentDocumentWillSave() {
        AWPrefs.setLastSavedDocument(getFilePath());
        if(!isFileWritable()) {
            XJAlert.display(getWindowContainer(), "Cannot Save", "This file cannot be saved. Check the file permission on the disk and try again.");
            return false;
        }
        return true;
    }

    public void print() {
        try {
            textEditor.print();
        } catch (PrinterException e) {
            XJAlert.display(getWindowContainer(), "Print Error", "An error occurred while printing:\n"+e.toString());
        }
    }

    public void showProgress(String title, XJDialogProgressDelegate delegate) {
        if(progress == null)
            progress = new XJDialogProgress(getWindowContainer());
        progress.setInfo(title);
        progress.setCancellable(true);
        progress.setDelegate(delegate);
        progress.setIndeterminate(true);
        progress.display();
    }

    public void hideProgress() {
        progress.close();
    }

    public void goToBackward() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_GOTO_BACK);

        if(goToHistory.canGoBack()) {
            setCaretPosition(goToHistory.getBackPosition(getCaretPosition()));
            refreshMainMenuBar();
        }
    }

    public void goToForward() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_GOTO_FORWARD);

        if(goToHistory.canGoForward()) {
            setCaretPosition(goToHistory.getForwardPosition());
            refreshMainMenuBar();
        }
    }

    public void find() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_FIND_DIALOG);
        findAndReplace.find();
    }

    public ContextualMenuFactory createContextualMenuFactory() {
        return container.createContextualMenuFactory();
    }

    public Set<Integer> getBreakpoints() {
        return gutterColumnManager.getBreakpoints();
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

    protected static class RuleTree extends XJTree {

        @Override
        public String getToolTipText(MouseEvent e) {
            TreePath path = getPathForLocation(e.getX(), e.getY());
            if(path == null)
                return "";

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            EditorRules.RuleTreeUserObject n = (EditorRules.RuleTreeUserObject) node.getUserObject();
            if(n == null)
                return "";

            ElementRule r = n.rule;
            if(r == null || !r.hasErrors())
                return "";
            else
                return r.getErrorMessageHTML();
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


}
