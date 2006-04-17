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

package org.antlr.works.debugger;

import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.appkit.gview.GView;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.foundation.notification.XJNotificationCenter;
import org.antlr.runtime.Token;
import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.editor.EditorMenu;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.generate.DialogGenerate;
import org.antlr.works.grammar.EngineGrammar;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.parsetree.ParseTreeNode;
import org.antlr.works.parsetree.ParseTreePanel;
import org.antlr.works.parsetree.ParseTreePanelDelegate;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.Statistics;
import org.antlr.works.utils.IconManager;
import org.antlr.works.utils.StreamWatcherDelegate;
import org.antlr.works.utils.TextPane;
import org.antlr.works.utils.TextUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class Debugger extends EditorTab implements StreamWatcherDelegate, ParseTreePanelDelegate {

    public static final String DEFAULT_LOCAL_ADDRESS = "localhost";

    public static final String NOTIF_DEBUG_STARTED = "NOTIF_DEBUG_STARTED";
    public static final String NOTIF_DEBUG_STOPPED = "NOTIF_DEBUG_STOPPED";

    public static final boolean BUILD_AND_DEBUG = true;
    public static final boolean DEBUG = false;

    public static final int INFO_COLUMN_COUNT = 0;  // 1st column of rule and even table
    public static final int INFO_COLUMN_RULE = 1;   // 2nd column of rule table
    public static final int INFO_COLUMN_EVENT = 1;  // 2nd column of event table
    public static final int INFO_COLUMN_SUBRULE = 2;
    public static final int INFO_COLUMN_DECISION = 3;
    public static final int INFO_COLUMN_MARK = 4;
    public static final int INFO_COLUMN_BACKTRACK = 5;

    protected JPanel panel;
    protected TextPane inputTextPane;
    protected TextPane outputTextPane;

    protected ParseTreePanel parseTreePanel;

    protected JRadioButton displayEventButton;
    protected JRadioButton displayRuleButton;

    protected JTable infoTable;

    protected RuleTableDataModel ruleTableDataModel;
    protected EventTableDataModel eventTableDataModel;

    protected JLabel infoLabel;
    protected JButton debugButton;
    protected JButton stopButton;
    protected JButton backButton;
    protected JButton forwardButton;
    protected JButton goToStartButton;
    protected JButton goToEndButton;
    protected JComboBox breakCombo;

    protected CEditorGrammar editor;
    protected AttributeSet previousGrammarAttributeSet;
    protected int previousGrammarPosition;

    protected Set breakpoints;

    protected DebuggerInputText inputText;
    protected DebuggerLocal debuggerLocal;
    protected DebuggerRecorder recorder;
    protected DebuggerPlayer player;

    protected Stack playCallStack;
    protected Stack backtrackStack;

    protected boolean running;
    protected JSplitPane ioSplitPane;
    protected JSplitPane ioTreeSplitPane;
    protected JSplitPane parseTreeInfoPanelSplitPane;

    protected long dateOfModificationOnDisk = 0;

    public Debugger(CEditorGrammar editor) {
        this.editor = editor;
    }

    public void awake() {
        panel = new JPanel(new BorderLayout());

        parseTreeInfoPanelSplitPane = new JSplitPane();
        parseTreeInfoPanelSplitPane.setBorder(null);
        parseTreeInfoPanelSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        parseTreeInfoPanelSplitPane.setLeftComponent(createTreePanel());
        parseTreeInfoPanelSplitPane.setRightComponent(createInfoPanel());
        parseTreeInfoPanelSplitPane.setContinuousLayout(true);
        parseTreeInfoPanelSplitPane.setOneTouchExpandable(true);

        ioSplitPane = new JSplitPane();
        ioSplitPane.setBorder(null);
        ioSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        ioSplitPane.setLeftComponent(createInputPanel());
        ioSplitPane.setRightComponent(createOutputPanel());
        ioSplitPane.setContinuousLayout(true);
        ioSplitPane.setOneTouchExpandable(true);

        ioTreeSplitPane = new JSplitPane();
        ioTreeSplitPane.setBorder(null);
        ioTreeSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        ioTreeSplitPane.setLeftComponent(ioSplitPane);
        ioTreeSplitPane.setRightComponent(parseTreeInfoPanelSplitPane);
        ioTreeSplitPane.setContinuousLayout(true);
        ioTreeSplitPane.setOneTouchExpandable(true);

        panel.add(createControlPanel(), BorderLayout.NORTH);
        panel.add(ioTreeSplitPane, BorderLayout.CENTER);

        inputText = new DebuggerInputText(this, inputTextPane);
        debuggerLocal = new DebuggerLocal(this);
        recorder = new DebuggerRecorder(this);
        player = new DebuggerPlayer(this, inputText);

        updateStatusInfo();
    }

    public void componentShouldLayout() {
        parseTreeInfoPanelSplitPane.setDividerLocation(0.5);
        ioTreeSplitPane.setDividerLocation(0.2);
        ioSplitPane.setDividerLocation(0.2);
    }

    public Container getWindowComponent() {
        return editor.getWindowContainer();
    }

    public void close() {
        debuggerStop(true);
        player.close();
    }

    public JComponent createInputPanel() {
        inputTextPane = new TextPane();
        inputTextPane.setBackground(Color.white);
        inputTextPane.setBorder(null);
        inputTextPane.setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        inputTextPane.setText("");
        inputTextPane.setEditable(false);

        TextUtils.createTabs(inputTextPane);

        JScrollPane textScrollPane = new JScrollPane(inputTextPane);
        textScrollPane.setWheelScrollingEnabled(true);

        return textScrollPane;
    }

    public JComponent createOutputPanel() {
        outputTextPane = new TextPane();
        outputTextPane.setBackground(Color.white);
        outputTextPane.setBorder(null);
        outputTextPane.setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        outputTextPane.setText("");
        outputTextPane.setEditable(false);

        TextUtils.createTabs(outputTextPane);

        JScrollPane textScrollPane = new JScrollPane(outputTextPane);
        textScrollPane.setWheelScrollingEnabled(true);

        return textScrollPane;
    }

    public JComponent createTreePanel() {
        parseTreePanel = new ParseTreePanel(new DefaultTreeModel(null));
        parseTreePanel.setDelegate(this);
        return parseTreePanel;
    }

    public JPanel createInfoPanel() {
        ruleTableDataModel = new RuleTableDataModel();
        eventTableDataModel = new EventTableDataModel();

        displayEventButton = new JRadioButton("Events");
        displayEventButton.setFocusable(false);
        displayEventButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setInfoTableModel(eventTableDataModel);
            }
        });

        displayRuleButton = new JRadioButton("Rules");
        displayRuleButton.setFocusable(false);
        displayRuleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setInfoTableModel(ruleTableDataModel);
            }
        });

        displayEventButton.setSelected(false);
        displayRuleButton.setSelected(true);

        ButtonGroup bp = new ButtonGroup();
        bp.add(displayEventButton);
        bp.add(displayRuleButton);

        infoTable = new JTable();
        infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        infoTable.setDefaultRenderer(Integer.class, new InfoTableCellRenderer());
        setInfoTableModel(ruleTableDataModel);

        JScrollPane infoScrollPane = new JScrollPane(infoTable);
        infoScrollPane.setWheelScrollingEnabled(true);

        JPanel infoControlPanel = new JPanel();
        infoControlPanel.add(displayRuleButton);
        infoControlPanel.add(displayEventButton);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(infoControlPanel, BorderLayout.SOUTH);
        infoPanel.add(infoScrollPane, BorderLayout.CENTER);

        return infoPanel;
    }

    public void setInfoTableModel(AbstractTableModel model) {
        infoTable.setModel(model);
        infoTable.getColumnModel().getColumn(INFO_COLUMN_COUNT).setMaxWidth(35);
        if(model == eventTableDataModel) {
            infoTable.getColumnModel().getColumn(INFO_COLUMN_EVENT).setMinWidth(100);
            infoTable.getColumnModel().getColumn(INFO_COLUMN_SUBRULE).setMaxWidth(30);
            infoTable.getColumnModel().getColumn(INFO_COLUMN_DECISION).setMaxWidth(30);
            infoTable.getColumnModel().getColumn(INFO_COLUMN_MARK).setMaxWidth(30);
            infoTable.getColumnModel().getColumn(INFO_COLUMN_BACKTRACK).setMaxWidth(30);
        }

        selectLastInfoTableItem();
    }

    public Box createControlPanel() {
        Box box = Box.createHorizontalBox();
        box.add(createRevealTokensButton());
        box.add(Box.createHorizontalStrut(20));
        box.add(stopButton = createDebuggerStopButton());
        box.add(Box.createHorizontalStrut(20));
        box.add(goToStartButton = createGoToStartButton());
        box.add(goToEndButton = createGoToEndButton());
        box.add(Box.createHorizontalStrut(20));
        box.add(backButton = createStepBackButton());
        box.add(forwardButton = createStepForwardButton());
        box.add(Box.createHorizontalStrut(20));
        box.add(createBreakComboBox());
        box.add(Box.createHorizontalGlue());
        box.add(createInfoLabelPanel());
        return box;
    }

    public JComponent createInfoLabelPanel() {
        infoLabel = new JLabel();
        return infoLabel;
    }

    public JButton createDebuggerStopButton() {
        JButton button = new JButton(IconManager.shared().getIconStop());
        button.setToolTipText("Stop");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                debuggerStop(false);
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_STOP);
            }
        });
        return button;
    }

    public JButton createStepBackButton() {
        JButton button = new JButton(IconManager.shared().getIconStepBackward());
        button.setToolTipText("Step Back");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                recorder.stepBackward(getBreakEvent());
                updateInterfaceLater();
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_STEP_BACK);
            }
        });
        return button;
    }

    public JButton createStepForwardButton() {
        JButton button = new JButton(IconManager.shared().getIconStepForward());
        button.setToolTipText("Step Forward");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                recorder.stepForward(getBreakEvent());
                updateInterfaceLater();
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_STEP_FORWARD);
            }
        });
        return button;
    }

    public JButton createGoToStartButton() {
        JButton button = new JButton(IconManager.shared().getIconGoToStart());
        button.setToolTipText("Go to start");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                restorePreviousGrammarAttributeSet();
                recorder.goToStart();
                updateInterfaceLater();
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_GOTO_START);
            }
        });
        return button;
    }

    public JButton createGoToEndButton() {
        JButton button = new JButton(IconManager.shared().getIconGoToEnd());
        button.setToolTipText("Go to end");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                recorder.goToEnd();
                updateInterfaceLater();
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_GOTO_END);
            }
        });
        return button;
    }

    public JComponent createBreakComboBox() {
        Box box = Box.createHorizontalBox();

        box.add(new JLabel("Step on"));

        breakCombo = new JComboBox();

        for (int i = 0; i < DebuggerEvent.ALL+1; i++) {
            breakCombo.addItem(DebuggerEvent.getEventName(i));
        }

        AWPrefs.getPreferences().bindToPreferences(breakCombo, AWPrefs.PREF_DEBUG_BREAK_EVENT, DebuggerEvent.CONSUME_TOKEN);

        box.add(breakCombo);

        return box;
    }

    public JButton createRevealTokensButton() {
        JButton tokenButton = new JButton(IconManager.shared().getIconTokens());
        tokenButton.setToolTipText("Reveal tokens in input text");
        tokenButton.setFocusable(false);
        tokenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.inputText.setDrawTokensBox(!player.inputText.isDrawTokensBox());
                inputTextPane.repaint();
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_TOGGLE_INPUT_TOKENS);
            }
        });
        return tokenButton;
    }

    public Container getContainer() {
        return panel;
    }

    public void updateStatusInfo() {
        String info = "-";
        switch(recorder.getStatus()) {
            case DebuggerRecorder.STATUS_STOPPED: info = "Stopped"; break;
            case DebuggerRecorder.STATUS_STOPPING: info = "Stopping"; break;
            case DebuggerRecorder.STATUS_LAUNCHING: info = "Launching"; break;
            case DebuggerRecorder.STATUS_RUNNING: info = "Running"; break;
            case DebuggerRecorder.STATUS_BREAK: info = "Break on "+DebuggerEvent.getEventName(recorder.getStoppedOnEvent()); break;
        }
        infoLabel.setText("Status: "+info);
        updateInterface();
    }

    public void updateInterfaceLater() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateInterface();
            }
        });
    }

    public void updateInterface() {
        stopButton.setEnabled(recorder.getStatus() != DebuggerRecorder.STATUS_STOPPED);

        boolean enabled = recorder.isRunning();
        boolean atBeginning = recorder.isAtBeginning();
        boolean atEnd = recorder.isAtEnd();

        backButton.setEnabled(enabled && !atBeginning);
        forwardButton.setEnabled(enabled && !atEnd);
        goToStartButton.setEnabled(enabled && !atBeginning);
        goToEndButton.setEnabled(enabled && !atEnd);
    }

    public EngineGrammar getGrammar() {
        return editor.getEngineGrammar();
    }

    public boolean needsToGenerateGrammar() {
        return dateOfModificationOnDisk != editor.getDocument().getDateOfModificationOnDisk()
                || editor.getDocument().isDirty();
    }

    public void grammarGenerated() {
        editor.getDocument().performAutoSave();
        dateOfModificationOnDisk = editor.getDocument().getDateOfModificationOnDisk();
    }

    public void queryGrammarBreakpoints() {
        this.breakpoints = editor.breakpointManager.getBreakpoints();
    }

    public boolean isBreakpointAtLine(int line) {
        if(breakpoints == null)
            return false;
        else
            return breakpoints.contains(new Integer(line));
    }

    public boolean isBreakpointAtToken(Token token) {
        return inputText.isBreakpointAtToken(token);
    }

    public int getBreakEvent() {
        return breakCombo.getSelectedIndex();
    }

    public void parseTreeDidSelectTreeNode(TreeNode node) {
        if(node instanceof DebuggerParseTreeNode) {
            DebuggerParseTreeNode n = (DebuggerParseTreeNode) node;
            if(n.token == null) {
                // token is non-null only for consumed event or other event
                // that is recorded by the DebuggerInputText class.
                // If token is null, the node n itself will contain
                // the position in the grammar.
                setGrammarPosition(n.line, n.pos);
            }
            inputText.selectToken(n.token);
        }
    }

    public void selectTreeParserNode(Token token) {
        DebuggerParseTreeNode root = (DebuggerParseTreeNode) parseTreePanel.getRoot();
        DebuggerParseTreeNode node = root.findNodeWithToken(token);
        if(node != null)
            parseTreePanel.selectNode(node);
    }

    public int grammarIndex;

    public void setGrammarPosition(int line, int pos) {
        grammarIndex = computeAbsoluteGrammarIndex(line, pos);
        if(grammarIndex >= 0) {
            if(editor.getTextPane().hasFocus()) {
                // If the text pane will lose its focus,
                // delay the text selection otherwise
                // the selection will be hidden
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        editor.selectTextRange(grammarIndex, grammarIndex+1);
                    }
                });
            } else
                editor.selectTextRange(grammarIndex, grammarIndex+1);
        }
    }

    public void displayNodeInfo(Object node) {
        DebuggerParseTreeNode treeNode = (DebuggerParseTreeNode)node;
        XJAlert.display(editor.getWindowContainer(), "Node info", treeNode.getInfoString());
    }

    public List getRules() {
        return editor.getRules();
    }

    public String getEventsAsString() {
        StringBuffer sb = new StringBuffer();
        sb.append(eventTableDataModel.getHeadersAsString());
        sb.append("\n");

        List events = eventTableDataModel.events;
        for(int i=0; i<events.size(); i++) {
            sb.append(i);
            sb.append(":\t");
            sb.append(events.get(i).toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public void launchLocalDebugger(boolean buildAndDebug) {
        // If the grammar is dirty, build it anyway

        if(needsToGenerateGrammar())
            buildAndDebug = true;

        if(buildAndDebug || !debuggerLocal.isRequiredFilesExisting()) {
            DialogGenerate dialog = new DialogGenerate(getWindowComponent());
            dialog.setDebugOnly();
            if(dialog.runModal() == XJDialog.BUTTON_OK) {
                debuggerLocal.setOutputPath(dialog.getOutputPath());
                debuggerLocal.prepareAndLaunch(BUILD_AND_DEBUG);

                grammarGenerated();
            }
        } else {
            debuggerLocal.prepareAndLaunch(DEBUG);
        }
    }

    public void debuggerLocalDidRun(boolean builtAndDebug) {
        if(builtAndDebug)
            Statistics.shared().recordEvent(Statistics.EVENT_LOCAL_DEBUGGER_BUILD);
        else
            Statistics.shared().recordEvent(Statistics.EVENT_LOCAL_DEBUGGER);
        debuggerLaunch(DEFAULT_LOCAL_ADDRESS, AWPrefs.getDebugDefaultLocalPort());
    }

    public void launchRemoteDebugger() {
        Statistics.shared().recordEvent(Statistics.EVENT_REMOTE_DEBUGGER);
        DebuggerRemoteConnectDialog dialog = new DebuggerRemoteConnectDialog(getWindowComponent());
        if(dialog.runModal() == XJDialog.BUTTON_OK) {
            debuggerLaunch(dialog.getAddress(), dialog.getPort());
        }
    }

    public void debuggerLaunch(String address, int port) {
        if(!debuggerLaunchGrammar()) {
            XJAlert.display(editor.getWindowContainer(), "Error",
                    "Cannot launch the debugger.\nException while parsing grammar.");
            return;
        }

        queryGrammarBreakpoints();
        recorder.connect(address, port);
    }

    public void connectionSuccess() {
        // First set the flag to true before doing anything else
        // (don't send the notification before for example)
        running = true;

        XJNotificationCenter.defaultCenter().postNotification(this, NOTIF_DEBUG_STARTED);
        editor.selectDebuggerTab();

        editor.console.makeCurrent();

        editor.getTextPane().setEditable(false);
        editor.getTextPane().requestFocus(false);
        previousGrammarAttributeSet = null;
        player.resetPlayEvents(true);
    }

    public void connectionFailed() {
        XJAlert.display(editor.getWindowContainer(), "Connection Error",
                "Cannot launch the debugger.\nTime-out waiting to connect to the remote parser.");
    }

    public void connectionCancelled() {
    }

    public boolean debuggerLaunchGrammar() {
        try {
            getGrammar().analyze();
        } catch (Exception e) {
            editor.getConsole().print(e);
            return false;
        }
        return true;
    }

    public void debuggerStop(boolean force) {
        if(recorder.getStatus() == DebuggerRecorder.STATUS_STOPPING) {
            if(force || XJAlert.displayAlertYESNO(editor.getWindowContainer(), "Stopping", "The debugger is currently stopping. Do you want to force stop it ?") == XJAlert.YES) {
                debuggerLocal.remoteParserProcess.destroy();
                recorder.forceStop();
            }
        } else
            recorder.stop();
    }

    public boolean isRunning() {
        return running;
    }

    public void resetGUI() {
        playCallStack = new Stack();
        playCallStack.push(new Debugger.DebuggerParseTreeNode("root"));

        backtrackStack = new Stack();

        ruleTableDataModel.clear();
        eventTableDataModel.clear();

        TreeNode node = (TreeNode)playCallStack.peek();

        parseTreePanel.setRoot(node);
        updateParseTree(node);
    }

    public void updateParseTree(TreeNode node) {
        if(node == null)
            return;

        parseTreePanel.refresh();
        parseTreePanel.scrollNodeToVisible(node);
    }

    public void storeGrammarAttributeSet(int index) {
        previousGrammarPosition = index;
        previousGrammarAttributeSet = editor.getTextPane().getStyledDocument().getCharacterElement(index+1).getAttributes();
    }

    public void restorePreviousGrammarAttributeSet() {
        if(previousGrammarAttributeSet != null) {
            editor.getTextPane().getStyledDocument().setCharacterAttributes(previousGrammarPosition, 1, previousGrammarAttributeSet, true);
            previousGrammarAttributeSet = null;
        }
    }

    public void selectLastInfoTableItem() {
        int count;
        if(displayEventButton.isSelected())
            count = eventTableDataModel.events.size();
        else
            count = ruleTableDataModel.rules.size();
        infoTable.scrollRectToVisible(infoTable.getCellRect(count-1, 0, true));
    }

    public int computeAbsoluteGrammarIndex(int lineIndex, int pos) {
        List lines = editor.getLines();
        if(lineIndex-1<0 || lineIndex-1 >= lines.size())
            return -1;

        ATELine line = (ATELine)lines.get(lineIndex-1);
        String t = editor.getText();

        // ANTLR gives a position using a tab size of 8. I have to
        // convert this to the current editor tab size
        // @todo if ANTLR changes the tab size, adjust here
        int antlr_tab = 8;
        int antlr_pos = 0;
        int c = 0;
        while(antlr_pos<pos) {
            if(t.charAt(line.position+c) == '\t') {
                antlr_pos = ((antlr_pos/antlr_tab)+1)*antlr_tab;
            } else {
                antlr_pos++;
            }

            c++;
        }
        return line.position+(c-1);
    }

    public void addEvent(DebuggerEvent event, DebuggerPlayer.ContextInfo info) {
        eventTableDataModel.add(event, info);
    }

    public void playEvents(List events, boolean reset) {
        player.playEvents(events, reset);
    }

    public void pushRule(String ruleName, int line, int pos) {
        DebuggerParseTreeNode parentRuleNode = (DebuggerParseTreeNode)playCallStack.peek();
        DebuggerParseTreeNode ruleNode = new DebuggerParseTreeNode(ruleName);
        ruleNode.setPosition(line, pos);

        addNodeToCurrentBacktrack(ruleNode);

        parentRuleNode.add(ruleNode);
        updateParseTree(ruleNode);

        playCallStack.push(ruleNode);
        ruleTableDataModel.add(ruleNode);
        selectLastInfoTableItem();
    }

    public void popRule(String ruleName) {
        ruleTableDataModel.remove(playCallStack.peek());
        selectLastInfoTableItem();
        playCallStack.pop();
    }

    public void addToken(Token token) {
        DebuggerParseTreeNode ruleNode = (DebuggerParseTreeNode)playCallStack.peek();
        DebuggerParseTreeNode elementNode = new DebuggerParseTreeNode(token);

        addNodeToCurrentBacktrack(elementNode);

        ruleNode.add(elementNode);
        updateParseTree(elementNode);
    }

    public void addException(Exception e) {
        DebuggerParseTreeNode ruleNode = (DebuggerParseTreeNode)playCallStack.peek();
        DebuggerParseTreeNode errorNode = new DebuggerParseTreeNode(e);

        addNodeToCurrentBacktrack(errorNode);

        ruleNode.add(errorNode);
        updateParseTree(errorNode);
    }

    public void beginBacktrack(int level) {
        backtrackStack.push(new Backtrack(level));
    }

    public void endBacktrack(int level, boolean success) {
        Backtrack b = (Backtrack) backtrackStack.pop();
        b.end(success);

        updateParseTree(b.getLastNode());
    }

    public void addNodeToCurrentBacktrack(DebuggerParseTreeNode node) {
        if(backtrackStack.isEmpty())
            return;

        Backtrack b = (Backtrack) backtrackStack.peek();
        b.addNode(node);
    }

    public void recorderStatusDidChange() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateStatusInfo();
            }
        });
    }

    public void recorderDidStop() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                restorePreviousGrammarAttributeSet();
                editor.getTextPane().setEditable(true);
                inputText.stop();
                running = false;
                editor.refreshMainMenuBar();
                XJNotificationCenter.defaultCenter().postNotification(this, NOTIF_DEBUG_STOPPED);
            }
        });
    }

    public void streamWatcherDidStarted() {
        outputTextPane.setText("");
    }

    public void streamWatcherDidReceiveString(String string) {
        outputTextPane.setText(outputTextPane.getText()+string);
    }

    public void streamWatcherException(Exception e) {
        editor.getConsole().print(e);
    }

    public boolean canExportToBitmap() {
        return true;
    }

    public boolean canExportToEPS() {
        return true;
    }

    public GView getExportableGView() {
        return parseTreePanel.getGraphView();
    }

    public String getTabName() {
        return "Debugger";
    }

    public Component getTabComponent() {
        return getContainer();
    }

    public JPopupMenu getContextualMenu() {
        ContextualMenuFactory factory = new ContextualMenuFactory(editor.editorMenu);
        factory.addItem(EditorMenu.MI_EXPORT_AS_EPS);
        factory.addItem(EditorMenu.MI_EXPORT_AS_IMAGE);
        return factory.menu;
    }

    public static final String KEY_SPLITPANE_A = "KEY_SPLITPANE_A";
    public static final String KEY_SPLITPANE_B = "KEY_SPLITPANE_B";
    public static final String KEY_SPLITPANE_C = "KEY_SPLITPANE_C";

    public void setPersistentData(Map data) {
        if(data == null)
            return;

        Integer i = (Integer)data.get(KEY_SPLITPANE_A);
        if(i != null)
            ioSplitPane.setDividerLocation(i.intValue());

        i = (Integer)data.get(KEY_SPLITPANE_B);
        if(i != null)
            ioTreeSplitPane.setDividerLocation(i.intValue());

        i = (Integer)data.get(KEY_SPLITPANE_C);
        if(i != null)
            parseTreeInfoPanelSplitPane.setDividerLocation(i.intValue());
    }

    public Map getPersistentData() {
        Map data = new HashMap();
        data.put(KEY_SPLITPANE_A, new Integer(ioSplitPane.getDividerLocation()));
        data.put(KEY_SPLITPANE_B, new Integer(ioTreeSplitPane.getDividerLocation()));
        data.put(KEY_SPLITPANE_C, new Integer(parseTreeInfoPanelSplitPane.getDividerLocation()));
        return data;
    }

    protected class RuleTableDataModel extends AbstractTableModel {

        protected List rules = new ArrayList();

        public void add(Object rule) {
            rules.add(rule);
            fireTableRowsInserted(rules.size()-1, rules.size()-1);
        }

        public void remove(Object rule) {
            rules.remove(rule);
            fireTableDataChanged();
        }

        public void clear() {
            rules.clear();
            fireTableDataChanged();
        }

        public int getRowCount() {
            return rules.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int column) {
            switch(column) {
                case INFO_COLUMN_COUNT: return "#";
                case INFO_COLUMN_RULE: return "Rule";
            }
            return super.getColumnName(column);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch(columnIndex) {
                case INFO_COLUMN_COUNT: return new Integer(rowIndex);
                case INFO_COLUMN_RULE: return rules.get(rowIndex);
            }
            return null;
        }
    }

    protected class EventTableDataModel extends AbstractTableModel {

        protected List events = new ArrayList();

        public void add(DebuggerEvent event, DebuggerPlayer.ContextInfo info) {
            events.add(new EventInfo(event, info));
            fireTableRowsInserted(events.size()-1, events.size()-1);
        }

        public void clear() {
            events.clear();
            fireTableDataChanged();
        }

        public int getRowCount() {
            return events.size();
        }

        public int getColumnCount() {
            return 6;
        }

        public String getColumnName(int column) {
            switch(column) {
                case INFO_COLUMN_COUNT: return "#";
                case INFO_COLUMN_EVENT: return "Event";
                case INFO_COLUMN_SUBRULE: return "SR";
                case INFO_COLUMN_DECISION: return "DEC";
                case INFO_COLUMN_MARK: return "MK";
                case INFO_COLUMN_BACKTRACK: return "BK";
            }
            return super.getColumnName(column);
        }

        public Class getColumnClass(int columnIndex) {
            switch(columnIndex) {
                case INFO_COLUMN_COUNT: return Integer.class;
                case INFO_COLUMN_EVENT: return String.class;
                case INFO_COLUMN_SUBRULE: return Integer.class;
                case INFO_COLUMN_DECISION: return Integer.class;
                case INFO_COLUMN_MARK: return Integer.class;
                case INFO_COLUMN_BACKTRACK: return Integer.class;
            }
            return super.getColumnClass(columnIndex);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            EventInfo info = (EventInfo) events.get(rowIndex);
            switch(columnIndex) {
                case INFO_COLUMN_COUNT: return new Integer(rowIndex);
                case INFO_COLUMN_EVENT: return info.event;
                case INFO_COLUMN_SUBRULE: return info.getSubrule();
                case INFO_COLUMN_DECISION: return info.getDecision();
                case INFO_COLUMN_MARK: return info.getMark();
                case INFO_COLUMN_BACKTRACK: return info.getBacktrack();
            }
            return null;
        }

        public String getHeadersAsString() {
            return "#\tEvent\tSubrule\tDecision\tMark\tBacktrack";
        }

        public class EventInfo {

            public DebuggerEvent event;
            public int subrule;
            public int decision;
            public int mark;
            public int backtrack;

            public EventInfo(DebuggerEvent event, DebuggerPlayer.ContextInfo info) {
                this.event = event;
                this.subrule = info.getSubrule();
                this.decision = info.getDecision();
                this.mark = info.getMark();
                this.backtrack = info.getBacktrack();
            }

            public Object getSubrule() {
                return subrule==-1?null:new Integer(subrule);
            }

            public Object getDecision() {
                return decision==-1?null:new Integer(decision);
            }

            public Object getMark() {
                return mark==-1?null:new Integer(mark);
            }

            public Object getBacktrack() {
                return backtrack==-1?null:new Integer(backtrack);
            }

            public String getTextForExport(int value) {
                if(value == -1)
                    return "-";
                else
                    return String.valueOf(value);
            }

            public String toString() {
                StringBuffer sb = new StringBuffer();
                sb.append(event.toString());
                sb.append("\t");
                sb.append(getTextForExport(subrule));
                sb.append("\t");
                sb.append(getTextForExport(decision));
                sb.append("\t");
                sb.append(getTextForExport(mark));
                sb.append("\t");
                sb.append(getTextForExport(backtrack));
                return sb.toString();
            }
        }
    }

    protected class InfoTableCellRenderer extends DefaultTableCellRenderer {

        public InfoTableCellRenderer() {
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if(column == INFO_COLUMN_COUNT) {
                setHorizontalAlignment(JLabel.RIGHT);
                setHorizontalTextPosition(SwingConstants.RIGHT);
            } else {
                setHorizontalAlignment(JLabel.CENTER);
                setHorizontalTextPosition(SwingConstants.CENTER);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    protected class Backtrack {

        public int level;
        public LinkedList nodes = new LinkedList();

        public Backtrack(int level) {
            this.level = level;
        }

        /** Node added to the backtrack object are displayed in blue
         * by default. The definitive color will be applied by the end()
         * method.
         */

        public void addNode(DebuggerParseTreeNode node) {
            node.setColor(AWPrefs.getLookaheadTokenColor());
            nodes.add(node);
        }

        public void end(boolean success) {
            Color color = getColor(success);
            for (int i = 0; i < nodes.size(); i++) {
                DebuggerParseTreeNode node = (DebuggerParseTreeNode) nodes.get(i);
                node.setColor(color);
            }
        }

        public ParseTreeNode getLastNode() {
            if(nodes.isEmpty())
                return null;
            else
                return (ParseTreeNode) nodes.getLast();
        }

        protected Color getColor(boolean success) {
            Color c = success?Color.green:Color.red;
            for(int i=1; i<level; i++) {
                c = c.darker().darker();
            }
            return c;
        }

    }

    public class DebuggerParseTreeNode extends ParseTreeNode {

        protected String s;
        protected Token token;
        protected Exception e;

        protected int line;
        protected int pos;

        protected Color color = Color.black;

        public DebuggerParseTreeNode(String s) {
            this.s = s;
        }

        public DebuggerParseTreeNode(Token token) {
            this.token = token;
        }

        public DebuggerParseTreeNode(Exception e) {
            this.e = e;
        }

        public DebuggerParseTreeNode findNodeWithToken(Token t) {
            if(token != null && t.getTokenIndex() == token.getTokenIndex())
                return this;

            for(Enumeration childrenEnumerator = children(); childrenEnumerator.hasMoreElements(); ) {
                DebuggerParseTreeNode node = (DebuggerParseTreeNode) childrenEnumerator.nextElement();
                DebuggerParseTreeNode candidate = node.findNodeWithToken(t);
                if(candidate != null)
                    return candidate;
            }
            return null;
        }

        public void setPosition(int line, int pos) {
            this.line = line;
            this.pos = pos;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        public String toString() {
            if(s != null)
                return s;
            else if(token != null)
                return token.getText()+" <"+getGrammar().getANTLRGrammar().getTokenDisplayName(token.getType())+">";
            else if(e != null)
                return e.toString();

            return "?";
        }

        public String getInfoString() {
            if(s != null)
                return s;
            else if(token != null)
                return token.getText()+" <"+getGrammar().getANTLRGrammar().getTokenDisplayName(token.getType())+">";
            else if(e != null)
                return e.toString();

            return "?";
        }

    }
}
