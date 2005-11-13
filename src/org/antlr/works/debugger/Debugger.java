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
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.foundation.notification.XJNotificationCenter;
import org.antlr.runtime.Token;
import org.antlr.tool.ErrorManager;
import org.antlr.tool.Grammar;
import org.antlr.works.dialog.DialogConnectDebugRemote;
import org.antlr.works.dialog.DialogGenerate;
import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.swing.TextPane;
import org.antlr.works.editor.swing.TextUtils;
import org.antlr.works.editor.swing.TreeUtilities;
import org.antlr.works.parser.Line;
import org.antlr.works.stats.Statistics;
import org.antlr.works.util.ErrorListener;
import org.antlr.works.util.IconManager;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class Debugger implements DebuggerLocal.StreamWatcherDelegate {

    public static final String DEFAULT_LOCAL_ADDRESS = "localhost";
    public static final int DEFAULT_LOCAL_PORT = 0xC001;

    public static final String NOTIF_DEBUG_STARTED = "NOTIF_DEBUG_STARTED";
    public static final String NOTIF_DEBUG_STOPPED = "NOTIF_DEBUG_STOPPED";

    public static final boolean BUILD_AND_DEBUG = true;
    public static final boolean DEBUG = false;

    protected JPanel panel;
    protected TextPane inputTextPane;
    protected TextPane outputTextPane;

    protected JScrollPane treeScrollPane;
    protected JTree tree;
    protected DefaultTreeModel treeModel;

    protected JList infoList;
    protected JRadioButton displayEventButton;
    protected JRadioButton displayStackButton;

    protected DefaultListModel stackListModel;
    protected DefaultListModel eventListModel;

    protected JLabel infoLabel;
    protected JButton debugButton;
    protected JButton stopButton;
    protected JButton backwardButton;
    protected JButton forwardButton;
    protected JButton goToStartButton;
    protected JButton goToEndButton;
    protected JComboBox breakCombo;

    protected EditorWindow editor;
    protected AttributeSet previousGrammarAttributeSet;
    protected int previousGrammarPosition;

    protected Set breakpoints;

    protected DebuggerLocal debuggerLocal;
    protected DebuggerRecorder recorder;
    protected DebuggerPlayer player;

    protected Stack playCallStack;

    protected Grammar grammar;

    protected boolean running;
    public JSplitPane ioSplitPane;
    protected JSplitPane ioTreeSplitPane;
    public JSplitPane treeStackSplitPane;

    public Debugger(EditorWindow editor) {
        this.editor = editor;
    }

    public void awake() {
        panel = new JPanel(new BorderLayout());

        treeStackSplitPane = new JSplitPane();
        treeStackSplitPane.setBorder(null);
        treeStackSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        treeStackSplitPane.setLeftComponent(createTreePanel());
        treeStackSplitPane.setRightComponent(createListInfoPanel());
        treeStackSplitPane.setContinuousLayout(true);
        treeStackSplitPane.setOneTouchExpandable(true);

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
        ioTreeSplitPane.setRightComponent(treeStackSplitPane);
        ioTreeSplitPane.setContinuousLayout(true);
        ioTreeSplitPane.setOneTouchExpandable(true);

        panel.add(createControlPanel(), BorderLayout.NORTH);
        panel.add(ioTreeSplitPane, BorderLayout.CENTER);

        debuggerLocal = new DebuggerLocal(this);
        recorder = new DebuggerRecorder(this);
        player = new DebuggerPlayer(this);

        updateStatusInfo();

        // Invoke the setDividerLocation() later - otherwise
        // they don't resize correctly. If someone knows the reason,
        // please let me know.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ioSplitPane.setDividerLocation(0.7);
                treeStackSplitPane.setDividerLocation(0.5);
                ioTreeSplitPane.setDividerLocation(0.2);
            }
        });
    }

    public Container getWindowComponent() {
        return editor.getWindowContainer();
    }

    public void close() {
        debuggerStop(true);
    }

    public JComponent createInputPanel() {
        inputTextPane = new TextPane();
        inputTextPane.setBackground(Color.white);
        inputTextPane.setBorder(null);
        inputTextPane.setFont(new Font(EditorPreferences.getEditorFont(), Font.PLAIN, EditorPreferences.getEditorFontSize()));
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
        outputTextPane.setFont(new Font(EditorPreferences.getEditorFont(), Font.PLAIN, EditorPreferences.getEditorFontSize()));
        outputTextPane.setText("");
        outputTextPane.setEditable(false);

        TextUtils.createTabs(outputTextPane);

        JScrollPane textScrollPane = new JScrollPane(outputTextPane);
        textScrollPane.setWheelScrollingEnabled(true);
        textScrollPane.setPreferredSize(new Dimension(200, 50));

        return textScrollPane;
    }

    public JComponent createTreePanel() {
        treeModel = new DefaultTreeModel(null);

        tree = new JTree(treeModel);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
        treeRenderer.setClosedIcon(null);
        treeRenderer.setLeafIcon(null);
        treeRenderer.setOpenIcon(null);

        tree.setCellRenderer(treeRenderer);
        
        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 2) {
                        displayNodeInfo(selPath.getLastPathComponent());
                        e.consume();
                    }
                }
            }
        });

        treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setWheelScrollingEnabled(true);

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(createExpandAllButton());
        box.add(createCollapseAllButton());

        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(treeScrollPane, BorderLayout.CENTER);
        treePanel.add(box, BorderLayout.SOUTH);

        return treePanel;
    }

    public JPanel createListInfoPanel() {
        stackListModel = new StackListModel();
        eventListModel = new EventListModel();

        displayEventButton = new JRadioButton("Events");
        displayEventButton.setFocusable(false);
        displayEventButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                infoList.setModel(eventListModel);
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_SHOW_EVENTS_LIST);
            }
        });

        displayStackButton = new JRadioButton("Stack");
        displayStackButton.setFocusable(false);
        displayStackButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                infoList.setModel(stackListModel);
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_SHOW_RULES_STACK);
            }
        });

        displayEventButton.setSelected(false);
        displayStackButton.setSelected(true);

        ButtonGroup bp = new ButtonGroup();
        bp.add(displayEventButton);
        bp.add(displayStackButton);

        infoList = new JList(stackListModel);
        infoList.setPrototypeCellValue("ThisIsARuleName");

        JScrollPane listScrollPane = new JScrollPane(infoList);
        listScrollPane.setWheelScrollingEnabled(true);
        //listScrollPane.setPreferredSize(new Dimension(50, 0));

        JPanel infoListControlPanel = new JPanel();
        infoListControlPanel.add(displayStackButton);
        infoListControlPanel.add(displayEventButton);

        JPanel infoListPanel = new JPanel(new BorderLayout());
        infoListPanel.add(infoListControlPanel, BorderLayout.SOUTH);
        infoListPanel.add(listScrollPane, BorderLayout.CENTER);

        return infoListPanel;
    }

    public Box createControlPanel() {
        Box box = Box.createHorizontalBox();
        box.add(stopButton = createDebuggerStopButton());
        box.add(Box.createHorizontalStrut(20));
        box.add(goToStartButton = createGoToStartButton());
        box.add(goToEndButton = createGoToEndButton());
        box.add(Box.createHorizontalStrut(20));
        box.add(backwardButton = createStepBackwardButton());
        box.add(forwardButton = createStepForwardButton());
        box.add(Box.createHorizontalStrut(20));
        box.add(createBreakComboBox());
        box.add(Box.createHorizontalStrut(20));
        box.add(createRevealTokensButton());
        box.add(Box.createHorizontalGlue());
        box.add(createInfoPanel());
        return box;
    }

    public JComponent createInfoPanel() {
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

    public JButton createStepBackwardButton() {
        JButton button = new JButton(IconManager.shared().getIconStepBackward());
        button.setToolTipText("Step Backward");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                recorder.stepBackward(getBreakEvent());
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_STEP_BACKWARD);
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

        EditorPreferences.getPreferences().bindToPreferences(breakCombo, EditorPreferences.PREF_DEBUG_BREAK_EVENT, DebuggerEvent.CONSUME_TOKEN);

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

    public JButton createExpandAllButton() {
        JButton button = new JButton(IconManager.shared().getIconExpandAll());
        button.setToolTipText("Expand All");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                TreeUtilities.expandAll(tree);
            }
        });
        return button;
    }

    public JButton createCollapseAllButton() {
        JButton button = new JButton(IconManager.shared().getIconCollapseAll());
        button.setToolTipText("Collapse All");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                TreeUtilities.collapseAll(tree);
            }
        });
        return button;
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
        }
        infoLabel.setText("Status: "+info);
        updateInterface();
    }

    public void updateInterface() {
        stopButton.setEnabled(recorder.getStatus() != DebuggerRecorder.STATUS_STOPPED);

        boolean enabled = recorder.getStatus() == DebuggerRecorder.STATUS_RUNNING;
        backwardButton.setEnabled(enabled);
        forwardButton.setEnabled(enabled);
        goToStartButton.setEnabled(enabled);
        goToEndButton.setEnabled(enabled);
    }

    public void grammarChanged() {
        debuggerLocal.grammarChanged();
    }

    public void setBreakpoints(Set breakpoints) {
        this.breakpoints = breakpoints;
    }

    public boolean isBreakpointAtLine(int line) {
        return breakpoints.contains(new Integer(line));
    }

    public int getBreakEvent() {
        return breakCombo.getSelectedIndex();
    }

    public void displayNodeInfo(Object node) {
        DebuggerTreeNode treeNode = (DebuggerTreeNode)node;
        XJAlert.display(editor.getWindowContainer(), "Node info", treeNode.getInfoString());
    }

    public List getRules() {
        return editor.parser.getRuleNames();
    }

    public List getEvents() {
        return recorder.getCurrentEvents();
    }

    public void launchLocalDebugger(boolean buildAndDebug) {
        if(buildAndDebug || !debuggerLocal.isRequiredFilesExisting()) {
            DialogGenerate dialog = new DialogGenerate(getWindowComponent());
            if(dialog.runModal() == XJDialog.BUTTON_OK) {
                debuggerLocal.setOutputPath(dialog.getOutputPath());
                debuggerLocal.prepareAndLaunch(BUILD_AND_DEBUG);
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
        debuggerLaunch(DEFAULT_LOCAL_ADDRESS, DEFAULT_LOCAL_PORT);
    }

    public void launchRemoteDebugger() {
        DialogConnectDebugRemote dialog = new DialogConnectDebugRemote(getWindowComponent());
        if(dialog.runModal() == XJDialog.BUTTON_OK) {
            Statistics.shared().recordEvent(Statistics.EVENT_REMOTE_DEBUGGER);
            debuggerLaunch(dialog.getAddress(), dialog.getPort());
        }
    }

    public void debuggerLaunch(String address, int port) {
        if(!debuggerLaunchGrammar()) {
            XJAlert.display(editor.getWindowContainer(), "Error",
                    "Cannot launch the debugger.\nException while parsing grammar.");
            return;
        }

        recorder.connect(address, port);
    }

    public void connectionSuccess() {
        XJNotificationCenter.defaultCenter().postNotification(this, NOTIF_DEBUG_STARTED);

        running = true;
        editor.selectDebuggerTab();

        editor.console.openGroup("Debug");
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
            ErrorManager.setErrorListener(ErrorListener.shared());
            grammar = new Grammar(editor.getFileName(), editor.getText());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void debuggerStop(boolean force) {
        if(recorder.getStatus() == DebuggerRecorder.STATUS_STOPPING) {
            if(force || XJAlert.displayAlertYESNO(editor.getWindowContainer(), "Stopping", "The debugger is currently stopping. Do you want to force stop it ?") == XJAlert.YES)
                recorder.forceStop();
        } else
            recorder.stop();

        editor.console.closeGroup();
    }

    public boolean isRunning() {
        return running;
    }

    public void resetGUI() {
        playCallStack = new Stack();
        playCallStack.push(new Debugger.DebuggerTreeNode("root"));

        stackListModel.removeAllElements();
        eventListModel.removeAllElements();

        TreeNode node = (TreeNode)playCallStack.peek();
        treeModel.setRoot(node);
        updateParseTree(node);
    }

    public void updateParseTree(TreeNode node) {
        treeModel.reload();
        TreeUtilities.expandAll(tree);
        tree.scrollPathToVisible(new TreePath(treeModel.getPathToRoot(node)));
    }

    public void storeGrammarAttributeSet(int index) {
        // Note: getCharacterAttributes() only returns the attribute of the character
        // at the caret position.
        previousGrammarPosition = index;
        previousGrammarAttributeSet = editor.getTextPane().getCharacterAttributes().copyAttributes();
    }

    public void restorePreviousGrammarAttributeSet() {
        if(previousGrammarAttributeSet != null) {
            editor.getTextPane().getStyledDocument().setCharacterAttributes(previousGrammarPosition, 1, previousGrammarAttributeSet, true);
            previousGrammarAttributeSet = null;
        }
    }

    public void selectLastInfoListItem() {
        if(displayEventButton.isSelected())
            infoList.ensureIndexIsVisible(eventListModel.getSize()-1);
        else
            infoList.ensureIndexIsVisible(stackListModel.getSize()-1);
    }

    public int computeAbsoluteGrammarIndex(int lineIndex, int pos) {
        List lines = editor.getLines();
        if(lineIndex-1<0 || lineIndex-1 >= lines.size())
            return -1;

        Line line = (Line)lines.get(lineIndex-1);
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

    public void playEvents(List events, boolean reset) {
        player.playEvents(events, reset);
    }

    public void pushRule(String ruleName) {
        Debugger.DebuggerTreeNode parentRuleNode = (Debugger.DebuggerTreeNode)playCallStack.peek();
        Debugger.DebuggerTreeNode ruleNode = new Debugger.DebuggerTreeNode(ruleName);
        parentRuleNode.add(ruleNode);
        updateParseTree(ruleNode);

        playCallStack.push(ruleNode);
        stackListModel.addElement(ruleNode);
        selectLastInfoListItem();
    }

    public void popRule(String ruleName) {
        stackListModel.removeElement(playCallStack.peek());
        selectLastInfoListItem();
        playCallStack.pop();
    }

    public void addToken(Token token) {
        DebuggerTreeNode ruleNode = (DebuggerTreeNode)playCallStack.peek();
        DebuggerTreeNode elementNode = new DebuggerTreeNode(token);
        ruleNode.add(elementNode);
        updateParseTree(elementNode);
    }

    public void addException(Exception e) {
        DebuggerTreeNode ruleNode = (DebuggerTreeNode)playCallStack.peek();
        DebuggerTreeNode errorNode = new DebuggerTreeNode(e);
        ruleNode.add(errorNode);
        updateParseTree(errorNode);
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
                running = false;
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

    protected class StackListModel extends DefaultListModel {
        public Object getElementAt(int index) { return "#"+(index+1)+" "+super.getElementAt(index); }
    }

    protected class EventListModel extends DefaultListModel {
        public Object getElementAt(int index) { return "#"+(index+1)+" "+super.getElementAt(index); }
    }

    protected class DebuggerTreeNode extends DefaultMutableTreeNode {

        protected String s;
        protected Token token;
        protected Exception e;

        public DebuggerTreeNode(String s) {
            this.s = s;
        }

        public DebuggerTreeNode(Token token) {
            this.token = token;
        }

        public DebuggerTreeNode(Exception e) {
            this.e = e;
        }

        public String toString() {
            if(s != null)
                return s;
            else if(token != null)
                return token.getText()+" <"+grammar.getTokenDisplayName(token.getType())+">";
            else if(e != null)
                return e.toString();

            return "?";
        }

        public String getInfoString() {
            if(s != null)
                return s;
            else if(token != null)
                return token.getText()+" <"+grammar.getTokenDisplayName(token.getType())+">";
            else if(e != null)
                return e.toString();

            return "?";
        }
    }
}
