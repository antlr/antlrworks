package org.antlr.works.components;

import org.antlr.Tool;
import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.ATEPanelDelegate;
import org.antlr.works.ate.ATETextPane;
import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.ate.syntax.misc.ATEThread;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.dialog.AWPrefsDialog;
import org.antlr.works.editor.*;
import org.antlr.works.editor.completion.AutoCompletionMenu;
import org.antlr.works.editor.completion.AutoCompletionMenuDelegate;
import org.antlr.works.editor.navigation.GoToHistory;
import org.antlr.works.editor.navigation.GoToRule;
import org.antlr.works.editor.navigation.GoToRuleDelegate;
import org.antlr.works.find.FindAndReplace;
import org.antlr.works.find.FindAndReplaceDelegate;
import org.antlr.works.find.Usages;
import org.antlr.works.grammar.GrammarAutoIndent;
import org.antlr.works.grammar.decisiondfa.DecisionDFAEngine;
import org.antlr.works.grammar.element.ElementImport;
import org.antlr.works.grammar.element.ElementReference;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.grammar.element.Jumpable;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.works.grammar.engine.GrammarEngineDelegate;
import org.antlr.works.grammar.engine.GrammarEngineImpl;
import org.antlr.works.interpreter.InterpreterTab;
import org.antlr.works.menu.*;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.utils.Console;
import org.antlr.works.utils.Utils;
import org.antlr.works.visualization.SyntaxDiagramTab;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.appkit.frame.XJWindow;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuItem;
import org.antlr.xjlib.appkit.menu.XJMenuItemCheck;
import org.antlr.xjlib.appkit.text.XJURLLabel;
import org.antlr.xjlib.appkit.undo.XJUndo;
import org.antlr.xjlib.appkit.undo.XJUndoDelegate;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.utils.XJDialogProgress;
import org.antlr.xjlib.appkit.utils.XJDialogProgressDelegate;
import org.antlr.xjlib.foundation.XJSystem;
import org.antlr.xjlib.foundation.XJUtils;
import org.antlr.xjlib.foundation.notification.XJNotificationCenter;
import org.antlr.xjlib.foundation.notification.XJNotificationObserver;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
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
public class GrammarWindow
        extends XJWindow
        implements AutoCompletionMenuDelegate, ATEPanelDelegate,
        XJUndoDelegate, InspectorDelegate, GrammarEngineDelegate,
        FindAndReplaceDelegate, GoToRuleDelegate, GoToMenuDelegate,
        FindMenuDelegate,
        XJNotificationObserver {

    /* Tabs */

    private static final int CLOSING_INDEX_LIMIT = 4;
    private final Map<Integer, GrammarWindowTab> indexToEditorTab = new HashMap<Integer, GrammarWindowTab>();
    private final List<GrammarWindowTab> tabs = new ArrayList<GrammarWindowTab>();

    public final SyntaxDiagramTab syntaxDiagramTab;
    public final InterpreterTab interpreterTab;
    private final DebuggerTab debuggerTab;
    public final ConsoleTab consoleTab;

    /* Components of the window */

    private final GrammarWindowMenu menu;
    private final GrammarWindowToolbar toolbar;

    private final GrammarEngine grammarEngine;
    public final EditorRules editorRules;

    private final FindAndReplace findAndReplace;
    public final DecisionDFAEngine decisionDFAEngine;

    private final GoToRule goToRule;
    public final GoToHistory goToHistory;

    private ConsoleStatus consoleStatus;
    private GrammarMemoryStatus memoryStatus;
    public AutoCompletionMenu autoCompletionMenu;

    /* TextEditor Managers */

    private EditorGutterColumnManager gutterColumnManager;
    private EditorFoldingManager foldingManager;
    private EditorOverlayManager underlyingManager;
    private EditorAnalysisManager analysisManager;

    /* Editors */

    public final EditorIdeas editorIdeas;
    public final EditorTips editorTips;
    public final EditorInspector editorInspector;
    private final EditorPersistence editorPersistence;
    private GrammarEditorKit editorKit;
    public ATEPanel textEditor;

    /* Swing */

    private MouseListener ml;
    private ChangeListener cl;

    private JSplitPane horizontalSplit;
    private JTabbedPane bottomTab;
    private Box statusBar;

    private JLabel infoLabel;
    private JLabel cursorLabel;
    private JLabel writableLabel;

    private XJDialogProgress progress;

    /* Other */

    private int debuggerLocation = -1;
    private Jumpable highlightedReference;

    private boolean windowFirstDisplay = true;
    private String lastSelectedRule;

    private AfterParseOperations afterParserOp;

    public GrammarWindow() {
        createTextEditor();
        createStatusBar();
        resetAutoCompletion();
        
        syntaxDiagramTab = new SyntaxDiagramTab(this);
        interpreterTab = new InterpreterTab(this);
        debuggerTab = new DebuggerTab(new GrammarDebuggerDelegate(this));
        consoleTab = new ConsoleTab(this);
        consoleTab.makeCurrent();

        menu = new GrammarWindowMenu(this);
        toolbar = new GrammarWindowToolbar(this);        
        afterParserOp = new AfterParseOperations();
        grammarEngine = new GrammarEngineImpl(this);
        decisionDFAEngine = new DecisionDFAEngine(this);
        goToRule = new GoToRule(this, this, getTextPane());
        goToHistory = new GoToHistory();
        findAndReplace = new FindAndReplace(this);
                                                 
        editorIdeas = new EditorIdeas(this);
        editorTips = new EditorTips(this);
        editorInspector = new EditorInspector(grammarEngine, decisionDFAEngine, this);
        editorPersistence = new EditorPersistence(this);
        editorRules = new EditorRules(this);

        textEditor.setParserEngine(grammarEngine.getSyntaxEngine());
        editorRules.setKeyBindings(textEditor.getKeyBindings());
    }

    @Override
    public void awake() {
        super.awake();
        
        debuggerTab.awake();
        interpreterTab.awake();

        menu.awake();
        toolbar.awake();
        editorIdeas.awake();
        editorTips.awake();

        assemble();
        applyPrefs();        
    }

    public void assemble() {
        bottomTab = new JTabbedPane();
        bottomTab.setTabPlacement(JTabbedPane.BOTTOM);

        bottomTab.addTab("Syntax Diagram", syntaxDiagramTab.getTabComponent());
        bottomTab.addTab("Interpreter", interpreterTab.getTabComponent());
        bottomTab.addTab("Console", consoleTab.getTabComponent());
        bottomTab.addTab("Debugger", debuggerTab.getTabComponent());

        bottomTab.addMouseListener(ml = new BottomTabbedPaneMouseListener());
        bottomTab.addChangeListener(cl = new BottomTabbedPaneChangeListener());

        final JSplitPane verticalSplit = new JSplitPane();
        verticalSplit.setBorder(null);
        verticalSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        verticalSplit.setLeftComponent(editorRules.getComponent());
        verticalSplit.setRightComponent(textEditor);
        verticalSplit.setContinuousLayout(true);
        verticalSplit.setOneTouchExpandable(true);
        verticalSplit.setResizeWeight(0.2);

        horizontalSplit = new JSplitPane();
        horizontalSplit.setBorder(null);
        horizontalSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
        horizontalSplit.setTopComponent(verticalSplit);
        horizontalSplit.setBottomComponent(bottomTab);
        horizontalSplit.setContinuousLayout(true);
        horizontalSplit.setOneTouchExpandable(true);
        horizontalSplit.setResizeWeight(0.7);

        final JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.addComponentListener(new MainPanelComponentListener());
        mainPanel.setBorder(null);
        mainPanel.add(toolbar.getToolbar(), BorderLayout.NORTH);
        mainPanel.add(horizontalSplit, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        XJNotificationCenter.defaultCenter().addObserver(this, AWPrefsDialog.NOTIF_PREFS_APPLIED);
        XJNotificationCenter.defaultCenter().addObserver(this, DebuggerTab.NOTIF_DEBUG_STARTED);
        XJNotificationCenter.defaultCenter().addObserver(this, DebuggerTab.NOTIF_DEBUG_STOPPED);

        registerUndo(this, getTextPane());

        getContentPane().add(mainPanel);
        pack();
    }

    private void createTextEditor() {
        textEditor = new ATEPanel(this);
        textEditor.setEditorKit(editorKit = new GrammarEditorKit(this));
        textEditor.setSyntaxColoring(true);
        textEditor.setDelegate(this);

        gutterColumnManager = new EditorGutterColumnManager(this);
        textEditor.setGutterColumnManager(gutterColumnManager);

        foldingManager = new EditorFoldingManager(this);
        textEditor.setFoldingManager(foldingManager);

        underlyingManager = new EditorOverlayManager(this);
        textEditor.setUnderlyingManager(underlyingManager);

        analysisManager = new EditorAnalysisManager(this);
        textEditor.setAnalysisManager(analysisManager);
    }

    private void createStatusBar() {
        infoLabel = new JLabel();
        cursorLabel = new JLabel();
        writableLabel = new JLabel();
        consoleStatus = new ConsoleStatus();
        memoryStatus = new GrammarMemoryStatus();

        statusBar = new GrammarStatusBar();
        statusBar.setPreferredSize(new Dimension(0, 30));

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
        statusBar.add(memoryStatus);
    }

    private void resetAutoCompletion() {
        if(autoCompletionMenu != null) {
            autoCompletionMenu.close();
        }
        autoCompletionMenu = new AutoCompletionMenu(this, getTextPane(), this);
    }

    private static JComponent createSeparator() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        Dimension d = s.getMaximumSize();
        d.width = 2;
        s.setMaximumSize(d);
        return s;
    }

    @Override
    public boolean close(boolean force) {
        if(!super.close(force)) return false;

        XJNotificationCenter.defaultCenter().removeObserver(this);

        goToRule.close();

        autoCompletionMenu.close();

        decisionDFAEngine.close();
        interpreterTab.close();

        consoleTab.close();
        editorIdeas.close();
        editorTips.close();
        editorInspector.close();

        editorPersistence.close();
        grammarEngine.close();

        editorRules.close();
        syntaxDiagramTab.close();

        afterParserOp.stop();
        afterParserOp = null;

        gutterColumnManager.close();
        foldingManager.close();
        underlyingManager.close();
        analysisManager.close();

        textEditor.close();
        editorKit.close();

        consoleStatus = null;
        memoryStatus.close();
        memoryStatus = null;

        menu.close();

        debuggerTab.close();
        toolbar.close();

        bottomTab.removeMouseListener(ml);
        bottomTab.removeChangeListener(cl);

        ml = null;
        cl = null;

        return true;
    }

    @Override
    public String autosaveName() {
        if(AWPrefs.getRestoreWindows())
            return getDocument().getDocumentName();
        else
            return null;
    }

    @Override
    public void setDefaultSize() {
        if(XJApplication.shared().useDesktopMode()) {
            super.setDefaultSize();
            return;
        }

        Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        r.width *= 0.8;
        r.height *= 0.8;
        setPreferredSize(r.getSize());
    }

    @Override
    public void windowActivated() {
        for(GrammarWindowTab et : tabs) {
            et.editorActivated();
        }

        componentActivated();
        // before activating the window itself
        super.windowActivated();
    }

    @Override
    public void windowDocumentPathDidChange(XJDocument doc) {
        // Called when the document associated file has changed on the disk
        componentDocumentContentChanged();
    }

    @Override
    public void becomingVisibleForTheFirstTime() {
        componentDidAwake();
        debuggerTab.componentShouldLayout(getSize());
    }

    @Override
    public void customizeFileMenu(XJMenu menu) {
        this.menu.customizeFileMenu(menu);
    }

    @Override
    public void customizeMenuBar(XJMainMenuBar menubar) {
        menu.customizeMenuBar(menubar);
    }

    @Override
    public void menuItemState(XJMenuItem item) {
        menu.menuItemState(item);
    }

    @Override
    public void handleMenuSelected(XJMenu menu) {
        this.menu.handleMenuSelected(menu);
    }

    public SyntaxDiagramTab getSyntaxDiagramTab() {
        return syntaxDiagramTab;
    }

    public InterpreterTab getInterpreterTab() {
        return interpreterTab;
    }

    public ConsoleTab getConsoleTab() {
        return consoleTab;
    }

    public GrammarWindowTab getSelectedTab() {
        int index = bottomTab.getSelectedIndex();
        switch(index) {
            case 0:
                return getSyntaxDiagramTab();
            case 1:
                return getInterpreterTab();
            case 2:
                return getConsoleTab();
            case 3:
                return debuggerTab;
            default:
                return indexToEditorTab.get(index);
        }
    }

    public void addTab(GrammarWindowTab tab) {
        /** Replace any existing tab with this one if the title matches. Don't
         * replace the first three tabs because they are always visible.
         */
        int index = getSimilarTab(tab);
        if(index == -1) {
            tabs.add(tab);
            bottomTab.add(tab.getTabName(), tab.getTabComponent());
            index = bottomTab.getTabCount()-1;
        } else {
            tabs.remove(index);
            tabs.add(index, tab);
            bottomTab.removeTabAt(index+CLOSING_INDEX_LIMIT);
            bottomTab.insertTab(tab.getTabName(), null, tab.getTabComponent(), null, index+CLOSING_INDEX_LIMIT);
        }

        indexToEditorTab.put(index, tab);
        selectTab(tab.getTabComponent());
    }

    private void selectTab(Component c) {
        if(bottomTab.getSelectedComponent() != c) {
            bottomTab.setSelectedComponent(c);
            refreshMainMenuBar();
        }
        makeBottomTabVisible();
    }

    private int getSimilarTab(GrammarWindowTab tab) {
        for (int i = 0; i < tabs.size(); i++) {
            GrammarWindowTab t = tabs.get(i);
            if(t.getTabName().equals(tab.getTabName()))
                return i;
        }
        return -1;
    }

    private void closeTab(int index) {
        if(index < CLOSING_INDEX_LIMIT)
            return;

        tabs.remove(index-CLOSING_INDEX_LIMIT);
        bottomTab.removeTabAt(index);
    }

    private void makeBottomTabVisible() {
        if(horizontalSplit.getBottomComponent().getHeight() == 0) {
            horizontalSplit.setDividerLocation(horizontalSplit.getLastDividerLocation());
        }
    }

    public boolean documentWillSave() {
        AWPrefs.setLastSavedDocument(getFilePath());
        if(!isFileWritable()) {
            XJAlert.display(getJavaContainer(), "Cannot Save", "This file cannot be saved. Check the file permission on the disk and try again.");
            return false;
        }
        return true;
    }

    public DebuggerTab getDebuggerTab() {
        return debuggerTab;
    }

    public DebugMenu getDebugMenu() {
        return menu.getDebugMenu();
    }

    public ActionRefactor getActionRefactor() {
        // todo rename this
        return menu.getActionRefactor();
    }

    public GoToMenu getGoToMenu() {
        return menu.getGoToMenu();
    }

    public void applyPrefs() {
        afterParserOp.setDefaultThreshold(AWPrefs.getParserDelay());
        textEditor.setFoldingEnabled(AWPrefs.getFoldingEnabled());
        textEditor.setLineNumberEnabled(AWPrefs.getLineNumberEnabled());
        textEditor.setHighlightCursorLine(AWPrefs.getHighlightCursorEnabled());
        textEditor.refresh();
        // Need to re-create the auto-completion pop-up because the vstyle is in prefs
        // and requires new key bindings
        resetAutoCompletion();
        applyFont();
    }

    public void applyFont() {
        getTextPane().setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        getTextPane().setTabSize(AWPrefs.getEditorTabSize());
    }

    public ATETextPane getTextPane() {
        return textEditor.getTextPane();
    }

    public ATEPanel getTextEditor() {
        return textEditor;
    }

    public void gotoToRule(String grammar, final String name) {
        if(!grammar.equals(grammarEngine.getGrammarName())) {
            // todo support
//            // rule is in another window
//            final GrammarEditor window = (GrammarEditor)getContainer().selectEditor(grammar);
//            // set the caret position
//            SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    window.gotoToRule(name);
//                }
//            });
        } else {
            // rule is in current window
            gotoToRule(name);
        }
    }

    private void gotoToRule(String name) {
        int index = grammarEngine.getFirstDeclarationPosition(name);
        if(index != -1) {
            setCaretPosition(index);
        }
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
        editorRules.toggleSorting();
        interpreterTab.setRules(getNaturalRules());
    }

    public boolean isRulesSorted() {
        return editorRules.isSorted();
    }

    public void toggleSyntaxDiagram() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_TOGGLE_SYNTAX_DIAGRAM);
        syntaxDiagramTab.setEnable(!syntaxDiagramTab.isEnabled());
        if(syntaxDiagramTab.isEnabled()) {
            syntaxDiagramTab.setText(getText(), getFileName());
        }
        updateVisualization(false);
    }

    public boolean isSyntaxDiagramDisplayed() {
        return syntaxDiagramTab.isEnabled();
    }

    public void toggleIdeas() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_TOGGLE_IDEAS);
        editorIdeas.toggleEnabled();
    }

    public boolean isIdeasEnabled() {
        return editorIdeas.isEnabled();
    }

    public void changeUpdate() {
        ateChangeUpdate(-1, -1, false);
    }

    /** Being group that can be undone as one unit */
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

    public void enableTextPaneUndo() {
        XJUndo undo = getUndo(getTextPane());
        if(undo != null)
            undo.enableUndo();
    }

    public void disableTextPaneUndo() {
        XJUndo undo = getUndo(getTextPane());
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

    public synchronized void setText(String text) {
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
        getConsoleTab().println(error, Console.LEVEL_ERROR);
    }

    public void reportError(Exception e) {
        getConsoleTab().println(e);
    }

    public Tool getANTLRTool() {
        String[] params = AWPrefs.getANTLR3Options();
        if(getOutputPath() != null) {
            File output = new File(getOutputPath());
            if(!output.exists()) {
                output.mkdirs();
            }
            if ( getFileFolder() !=null ) {
                params = Utils.concat(params,
                                      new String[] {
                                          "-o", output.getAbsolutePath(),
                                          "-lib", getFileFolder() });
            }
            else {
                params = Utils.concat(params,
                                      new String[] {
                                          "-o", output.getAbsolutePath()});
            }
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
        syntaxDiagramTab.clearCacheGraphs();
        editorRules.refreshRules();

        // Try to update the graph first and if they cannot be updated (i.e. the cache is empty), draw them again.
        if(!syntaxDiagramTab.update()) {
            updateVisualization(true);
        }
        updateInformation();
    }

    public void createFile(String name) {
        // not used anymore
    }

    public void createRuleAtIndex(boolean lexer, String name, String content) {
        getActionRefactor().createRuleAtIndex(lexer, name, content);
    }

    public void deleteRuleAtCurrentPosition() {
        getActionRefactor().deleteRuleAtIndex(getCaretPosition());
    }

    public void removeLeftRecursion() {
        getActionRefactor().removeLeftRecursion();
    }

    public void convertLiteralsToSingleQuote() {
        getActionRefactor().convertLiteralsToSingleQuote();
    }

    public void replaceText(int leftIndex, int rightIndex, String text) {
        textEditor.replaceText(leftIndex, rightIndex, text);
    }

    public void selectTextRange(int start, int end) {
        textEditor.selectTextRange(start, end);
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

    public synchronized boolean isFileExists() {
        String path = getFilePath();
        if(path == null) {
            return false;
        } else {
            File f = new File(path);
            return f.exists();
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
        File outputPath = new File(AWPrefs.getOutputPath());
        if(outputPath.isAbsolute()) {
            return outputPath.getAbsolutePath();
        }
        if(getFilePath() == null) {
            return null;
        }
        return XJUtils.concatPath(XJUtils.getPathByDeletingLastComponent(getFilePath()), outputPath.getPath());
    }

    public GrammarEngine getGrammarEngine() {
        return grammarEngine;
    }

    public List<ElementRule> getRules() {
        return editorRules.getRules();
    }

    public List<ElementRule> getSortedRules() {
        return editorRules.getSortedRules();
    }

    public List<ElementRule> getNaturalRules() {
        return editorRules.isSorted()? editorRules.getSortedRules(): editorRules.getRules();
    }

    public EditorRules getEditorRules() {
        return editorRules;
    }

    public void addUsagesTab(Usages usage) {
        addTab(usage);
    }

    public List<ATEToken> getTokens() {
        return textEditor.getTokens();
    }

    public List<ATELine> getLines() {
        return textEditor.getLines();
    }

    public void goToHistoryRememberCurrentPosition() {
        goToHistory.addPosition(getCaretPosition());
        getMainMenuBar().refresh();
    }

    public ElementReference getCurrentReference() {
        return getReferenceAtPosition(getCaretPosition());
    }

    public ElementReference getReferenceAtPosition(int pos) {
        for (ElementReference ref : grammarEngine.getReferences()) {
            if (ref.containsIndex(pos))
                return ref;
        }
        return null;
    }

    public ElementImport getImportAtPosition(int pos) {
        for (ElementImport element : grammarEngine.getImports()) {
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
        return editorRules.getEnclosingRuleAtPosition(getCaretPosition());
    }

    public void setCaretPosition(int position) {
        setCaretPosition(position, AWPrefs.getSmoothScrolling());
    }

    private void setCaretPosition(int position, boolean animate) {
        ElementRule rule = editorRules.getEnclosingRuleAtPosition(position);
        if(rule != null && !rule.isExpanded()) {
            foldingManager.toggleFolding(rule);
        }
        textEditor.setCaretPosition(position, true, animate);
    }

    public int getCaretPosition() {
        return textEditor.getCaretPosition();
    }

    public void updateVisualization(boolean immediate) {
        if(syntaxDiagramTab.isEnabled()) {
            ElementRule r = editorRules.getEnclosingRuleAtPosition(getCaretPosition());
            if(r == null) {
                syntaxDiagramTab.setPlaceholder("Select a rule to display its syntax diagram");
            } else {
                if(r.hasErrors() && r.needsToBuildErrors()) {
                    grammarEngine.computeRuleErrors(r);
                    try {
                        syntaxDiagramTab.createGraphsForRule(r);
                    } catch (Exception e) {
                        // ignore
                    }
                }

                syntaxDiagramTab.setRule(r, immediate);
            }
        } else {
            syntaxDiagramTab.setPlaceholder("Syntax Diagram Disabled");
        }
    }

    public void updateInformation() {
        String t;
        int size = grammarEngine.getNumberOfRules();
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

        int warnings = grammarEngine.getNumberOfErrors();
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

    public FindAndReplace getFindAndReplace() {
        return findAndReplace;
    }

    public GoToRule getGoToRule() {
        return goToRule;
    }

    public boolean goToRule(String ruleName) {
        ElementRule rule = editorRules.selectRuleNameInTree(ruleName);
        if(rule != null) {
            goToHistoryRememberCurrentPosition();
            editorRules.goToRule(rule);
            return true;
        }
        return false;
    }

    public void goToDeclaration() {
        Jumpable ref = getCurrentReference();
        if(ref == null) {
            ref = getImportAtPosition(getCaretPosition());
        }
        getGoToMenu().goToDeclaration(ref);
    }

    public void goToDeclaration(final Jumpable ref) {
        goToHistoryRememberCurrentPosition();
        if(ref instanceof ElementImport) {
        //    getContainer().selectEditor(ref.getName());
        } else if(ref != null) {
            GrammarEngine engine = getGrammarEngine();
            int index = engine.getFirstDeclarationPosition(ref.getName());
            if(index == -1) {
                // This grammar does not contain the declaration. Search in the other children
                // starting from the root grammarEngine
                engine = engine.getRootEngine();
                List<String> grammars = engine.getGrammarsOverriddenByRule(ref.getName());
                if(!grammars.isEmpty()) {
                    //getContainer().selectEditor(grammars.get(0));

                    // set the caret position
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            GrammarEngine engine = getGrammarEngine();
                            int index = engine.getFirstDeclarationPosition(ref.getName());
                            if(index != -1) {
                                setCaretPosition(index);
                            }
                        }
                    });
                }
            } else {
                setCaretPosition(index);
            }
        }
    }

    public List<String> getRulesStartingWith(String match) {
        return editorRules.getRulesStartingWith(match);
    }

    /** Rules delegate methods
     *
     */

    public void rulesCaretPositionDidChange() {
        updateVisualization(false);
    }

    public void rulesDidChange() {
        interpreterTab.updateIgnoreTokens(getRules());
    }

    public JPopupMenu rulesGetContextualMenu(List selectedObjects) {
        if(selectedObjects.isEmpty())
            return null;

        ContextualMenuFactory factory = createContextualMenuFactory();
        factory.addItem(GrammarWindowMenu.MI_GROUP_RULE);
        factory.addItem(GrammarWindowMenu.MI_UNGROUP_RULE);
        factory.addSeparator();
        XJMenuItemCheck item = (XJMenuItemCheck) factory.addItem(GrammarWindowMenu.MI_IGNORE_RULE);
        item.setSelected(editorRules.getFirstSelectedRuleIgnoredFlag());

        return factory.menu;
    }

    /** Parser delegate methods
     */
    public void ateEngineBeforeParsing() {
        editorPersistence.store();
    }

    public void ateEngineAfterParsing() {
        updateInformation();
        updateCursorInfo();

        if(windowFirstDisplay) {
            windowFirstDisplay = false;
            afterParseOperations();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateVisualization(true);
                    findTokensToIgnore(true);
                }
            });
        } else {
            afterParserOp.awakeThread();
        }
    }

    private void afterParseOperations() {
        editorPersistence.restore();

        grammarEngine.parserCompleted();
        grammarEngine.updateAll();

        interpreterTab.setRules(getNaturalRules());
        editorRules.parserDidParse();
        decisionDFAEngine.reset();
        decisionDFAEngine.refreshMenu();

        // Make sure to invoke the ideas after Rules
        // has completely updated its list (which should
        // be done inside rules.parserDidParse())
        editorIdeas.display(getCaretPosition());

        syntaxDiagramTab.setText(getText(), getFileName());
        updateVisualization(false);

        // Damage the window and repaint it
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
        grammarEngine.markDirty();
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

    public void notificationFire(Object source, String name) {
        if(name.equals(AWPrefsDialog.NOTIF_PREFS_APPLIED)) {
            notificationPrefsChanged();
        } else if(name.equals(DebuggerTab.NOTIF_DEBUG_STARTED)) {
            notificationDebuggerStarted();
        } else if(name.equals(DebuggerTab.NOTIF_DEBUG_STOPPED)) {
            notificationDebuggerStopped();
        }
    }

    private void notificationPrefsChanged() {
        applyPrefs();
    }

    private void notificationDebuggerStarted() {
        getMainMenuBar().refresh();
        editorIdeas.hide();
    }

    private void notificationDebuggerStopped() {
        // nothing to do
    }

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

    public void componentActivated() {
        consoleTab.makeCurrent();
        grammarEngine.reset();
        grammarEngine.updateAll();
        textEditor.getTextPane().setWritable(isFileWritable());
        textEditor.refresh();
        updateInformation();
    }

    public void componentDidHide() {
        // Hide the ideas if the component is hidden. For example, in the project
        // window, if a component is hidden, the ideas have to be also hidden otherwise
        // they are floating above the new visible component which is weird.
        editorIdeas.hide();
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

        int oldCursorPosition = getCaretPosition();
        try {
            getDocument().reload();
        } catch (Exception e) {
            e.printStackTrace();
            XJAlert.display(getJavaContainer(), "Error Reloading Document", "An error occurred when reloading the document:\n"+e.toString());
        }
        grammarChanged();
        setCaretPosition(Math.min(oldCursorPosition, getText().length()));
    }

    /** AutoCompletionMenuDelegate method: return the list of corresponding words
     *  given a partial word
     */

    public List<String> autoCompletionMenuGetMatchingWordsForPartialWord(String partialWord) {
        if(grammarEngine.getNumberOfRules() == 0) {
            return null;
        }

        partialWord = partialWord.toLowerCase();
        List<String> matchingRules = new ArrayList<String>();

        if(editorRules.isRuleAtIndex(getCaretPosition())) {
            // Inside a rule - show all rules in alphabetical order

            List<ElementRule> sortedRules = Collections.list(Collections.enumeration(grammarEngine.getRules()));
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

            List<ElementReference> sortedUndefinedReferences = Collections.list(Collections.enumeration(grammarEngine.getUndefinedReferences()));
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
        syntaxDiagramTab.cancelDrawingProcess();
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
            getGoToMenu().goToDeclaration(highlightedReference);
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
        JPopupMenu m = getContextualMenu(textEditor.getTextIndexAtPosition(x, y));
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
        ElementRule rule = editorRules.selectRuleInTreeAtPosition(index);
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

    public void findTokensToIgnore(boolean reset) {
        editorRules.findTokensToIgnore(reset);
        interpreterTab.setRules(getNaturalRules());
    }

    public void print() {
        try {
            textEditor.print();
        } catch (PrinterException e) {
            XJAlert.display(getJavaContainer(), "Print Error", "An error occurred while printing:\n"+e.toString());
        }
    }

    public void showProgress(String title, XJDialogProgressDelegate delegate) {
        if(progress == null)
            progress = new XJDialogProgress(getJavaContainer());
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
        return new ContextualMenuFactory(menu);
    }

    public JPopupMenu getContextualMenu(int textIndex) {
        return menu.getContextualMenu(textIndex);
    }
        
    public Set<Integer> getBreakpoints() {
        return gutterColumnManager.getBreakpoints();
    }

    public void refreshMainMenuBar() {
        getMainMenuBar().refreshState();
    }

    public void selectInterpreterTab() {
        selectTab(interpreterTab.getContainer());
    }

    public void selectConsoleTab() {
        selectTab(consoleTab.getContainer());
    }

    public void selectDebuggerTab() {
        selectTab(debuggerTab.getContainer());
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

        public final Box box;
        public final XJURLLabel label;
        public boolean visible;
        public int currentDisplayedLevel;

        public ConsoleStatus() {
            box = Box.createHorizontalBox();

            label = new XJURLLabel(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectTab(consoleTab.getTabComponent());
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

    public class BottomTabbedPaneMouseListener extends MouseAdapter {

        public void displayPopUp(MouseEvent event) {
            if(bottomTab.getSelectedIndex() < CLOSING_INDEX_LIMIT)
                return;

            if(!event.isPopupTrigger())
                return;

            JPopupMenu popup = new JPopupMenu();
            JMenuItem item = new JMenuItem("Close");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    closeTab(bottomTab.getSelectedIndex());
                }
            });
            popup.add(item);
            popup.show(event.getComponent(), event.getX(), event.getY());
        }

        @Override
        public void mousePressed(MouseEvent event) {
            displayPopUp(event);
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            displayPopUp(event);
        }
    }

    private class BottomTabbedPaneChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            refreshMainMenuBar();
        }
    }

    private class MainPanelComponentListener extends ComponentAdapter {

        @Override
        public void componentHidden(ComponentEvent e) {
            componentDidHide();
        }
    }


}
