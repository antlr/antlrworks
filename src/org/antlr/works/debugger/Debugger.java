package org.antlr.works.debugger;

import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.appkit.utils.XJAlert;
import org.antlr.runtime.Token;
import org.antlr.tool.Grammar;
import org.antlr.works.dialog.DialogBuildAndDebug;
import org.antlr.works.dialog.DialogConnectDebugRemote;
import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.swing.TreeUtilities;
import org.antlr.works.editor.swing.TextEditorPane;
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


/*

[The "BSD licence"]
Copyright (c) 2004-05 Jean Bovet
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

public class Debugger {

    // @todo check to see if this is parametrizable
    public static final String localAddress = "localhost";
    public static final int localPort = 2005;

    protected JPanel panel;
    protected TextEditorPane textPane;

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

    public Debugger(EditorWindow editor) {
        this.editor = editor;

        panel = new JPanel(new BorderLayout());

        JSplitPane treeStackSplitPane = new JSplitPane();
        treeStackSplitPane.setBorder(null);
        treeStackSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        treeStackSplitPane.setLeftComponent(createTreePanel());
        treeStackSplitPane.setRightComponent(createListInfoPanel());
        treeStackSplitPane.setContinuousLayout(true);
        treeStackSplitPane.setPreferredSize(new Dimension(400, 0));
        treeStackSplitPane.setOneTouchExpandable(true);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setBorder(null);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createInputPanel());
        splitPane.setRightComponent(treeStackSplitPane);
        splitPane.setContinuousLayout(true);
        splitPane.setPreferredSize(new Dimension(400, 0));
        splitPane.setOneTouchExpandable(true);

        panel.add(createControlPanel(), BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        debuggerLocal = new DebuggerLocal(this);
        recorder = new DebuggerRecorder(this);
        player = new DebuggerPlayer(this);

        breakCombo.setSelectedIndex(DebuggerEvent.CONSUME_TOKEN);

        updateStatusInfo();
    }

    public JComponent createInputPanel() {
        textPane = new TextEditorPane();
        textPane.setPreferredSize(new Dimension(200, 0));
        textPane.setBackground(Color.white);
        textPane.setBorder(null);
        textPane.setFont(new Font("Courier", Font.PLAIN, 12));
        textPane.setText("");
        textPane.setEditable(false);

        JScrollPane textScrollPane = new JScrollPane(textPane);
        textScrollPane.setWheelScrollingEnabled(true);

        JButton tokenButton = new JButton(IconManager.getIconTokens());
        tokenButton.setToolTipText("Show/hide tokens boxes");
        tokenButton.setFocusable(false);
        tokenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.inputText.setDrawTokensBox(!player.inputText.isDrawTokensBox());
                textPane.repaint();
            }
        });

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(tokenButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(textScrollPane, BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);

        return panel;
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

        JScrollPane treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setWheelScrollingEnabled(true);
        treeScrollPane.setPreferredSize(new Dimension(600, 0));

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
            }
        });

        displayStackButton = new JRadioButton("Stack");
        displayStackButton.setFocusable(false);
        displayStackButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                infoList.setModel(stackListModel);
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
        listScrollPane.setPreferredSize(new Dimension(50, 0));

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
        box.add(Box.createHorizontalGlue());
        box.add(createInfoPanel());
        return box;
    }

    public JComponent createInfoPanel() {
        infoLabel = new JLabel();
        return infoLabel;
    }

    public JButton createDebuggerStopButton() {
        JButton button = new JButton(IconManager.getIconStop());
        button.setToolTipText("Stop");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                debuggerStop();
            }
        });
        return button;
    }

    public JButton createStepBackwardButton() {
        JButton button = new JButton(IconManager.getIconStepBackward());
        button.setToolTipText("Step Backward");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                recorder.stepBackward();
            }
        });
        return button;
    }

    public JButton createStepForwardButton() {
        JButton button = new JButton(IconManager.getIconStepForward());
        button.setToolTipText("Step Forward");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                recorder.stepForward();
            }
        });
        return button;
    }

    public JButton createGoToStartButton() {
        JButton button = new JButton(IconManager.getIconGoToStart());
        button.setToolTipText("Go to start");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                restorePreviousGrammarAttributeSet();
                recorder.goToStart();
            }
        });
        return button;
    }

    public JButton createGoToEndButton() {
        JButton button = new JButton(IconManager.getIconGoToEnd());
        button.setToolTipText("Go to end");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                recorder.goToEnd();
            }
        });
        return button;
    }

    public JComponent createBreakComboBox() {
        Box box = Box.createHorizontalBox();

        box.add(new JLabel("Step on"));

        breakCombo = new JComboBox();

        for (int i = 0; i < DebuggerEvent.getEvents().length; i++) {
            breakCombo.addItem(DebuggerEvent.getEvents()[i]);
        }

        breakCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JComboBox combo = (JComboBox)event.getSource();
                recorder.setBreaksOnEventType(combo.getSelectedIndex());
            }
        });

        box.add(breakCombo);

        return box;
    }

    public JButton createExpandAllButton() {
        JButton button = new JButton(IconManager.getIconExpandAll());
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
        JButton button = new JButton(IconManager.getIconCollapseAll());
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

    public void setBreakpoints(Set breakpoints) {
        this.breakpoints = breakpoints;
    }

    public boolean isBreakpointAtLine(int line) {
        return breakpoints.contains(new Integer(line));
    }

    public void displayNodeInfo(Object node) {
        DebuggerTreeNode treeNode = (DebuggerTreeNode)node;
        XJAlert.display("Node info", treeNode.getInfoString());
    }

    public List getRules() {
        return editor.parser.getRuleNames();
    }

    public List getEvents() {
        return recorder.getCurrentEvents();
    }

    public void launchLocalDebugger(boolean build) {
        debuggerLocal.setOutputPath(EditorPreferences.getOutputPath());
        debuggerLocal.setANTLR3Path(EditorPreferences.getANTLR3Path());
        debuggerLocal.setStartRule(EditorPreferences.getStartSymbol());
        
        if(build) {
            DialogBuildAndDebug dialog = new DialogBuildAndDebug(this);
            if(dialog.runModal() == XJDialog.BUTTON_OK) {
                debuggerLocal.setOutputPath(dialog.getOutputPath());
                debuggerLocal.setANTLR3Path(dialog.getANTLR3Path());
                debuggerLocal.setStartRule(dialog.getRule());
                debuggerLocal.prepareAndLaunch(true, true);
            }
        } else
            debuggerLocal.prepareAndLaunch(false, true);
    }

    public void debuggerLocalDidRun(boolean success) {
        if(success) {
            debuggerLaunch(localAddress, localPort);
        }
    }

    public void launchRemoteDebugger() {
        DialogConnectDebugRemote dialog = new DialogConnectDebugRemote();
        if(dialog.runModal() == XJDialog.BUTTON_OK) {
            debuggerLaunch(dialog.getAddress(), dialog.getPort());
        }
    }

    public void debuggerLaunch(String address, int port) {
        if(!debuggerLaunchGrammar()) {
            XJAlert.display("Error", "Cannot launch the debugger.\nException while parsing grammar.");
            return;
        }

        if(!recorder.start(address, port)) {
            XJAlert.display("Connection Error", "Cannot launch the debugger.\nTime-out waiting to connect to the remote parser.");
            return;
        }

        editor.getTextPane().setEditable(false);
        editor.getTextPane().requestFocus(false);
        previousGrammarAttributeSet = null;
        player.resetPlayEvents();
    }

    public boolean debuggerLaunchGrammar() {
        try {
            grammar = new Grammar(editor.getPlainText());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void debuggerStop() {
        if(recorder.getStatus() == DebuggerRecorder.STATUS_STOPPING) {
            if(XJAlert.displayAlertYESNO("Stopping", "The debugger is currently stopping. Do you want to force stop it ?") == XJAlert.YES) {
                recorder.forceStop();
            }
        } else
            recorder.stop();
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

    public int computeAbsoluteGrammarIndex(int line, int pos) {
        List lines = editor.getLines();
        if(line-1<0 || line-1 >= lines.size())
            return -1;

        Integer i = (Integer)lines.get(line-1);
        int index = i.intValue();

        String t = editor.getText();

        // Compute also the tab position using a tab length of 8
        // @todo parametrize the tab length
        int tab = 8;
        int offset = 0;
        int c = 0;
        while(offset<pos) {
            if(t.charAt(index+c) == '\t') {
                offset = ((offset/tab)+1)*tab;
            } else {
                offset++;
            }

            c++;
        }
        return index+(c-1);
    }

    public void playEvents(List events) {
        player.playEvents(events);
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
            }
        });
    }


    protected class StackListModel extends DefaultListModel {
        public Object getElementAt(int index) { return "#"+(index+1)+" "+super.getElementAt(index); };
    }

    protected class EventListModel extends DefaultListModel {
        public Object getElementAt(int index) { return "#"+(index+1)+" "+super.getElementAt(index); };
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
                return token.getText()+" <"+grammar.getTokenName(token.getType())+">";
            else if(e != null)
                return e.toString();

            return "?";
        }

        public String getInfoString() {
            if(s != null)
                return s;
            else if(token != null)
                return token.getText()+" <"+grammar.getTokenName(token.getType())+">";
            else if(e != null)
                return e.toString();

            return "?";
        }
    }
}
