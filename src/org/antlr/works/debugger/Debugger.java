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

import org.antlr.runtime.ClassicToken;
import org.antlr.runtime.Token;
import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.debugger.events.DBEvent;
import org.antlr.works.debugger.input.DBInputTextTokenInfo;
import org.antlr.works.debugger.local.DBLocal;
import org.antlr.works.debugger.panels.*;
import org.antlr.works.debugger.remote.DBRemoteConnectDialog;
import org.antlr.works.debugger.tivo.DBPlayer;
import org.antlr.works.debugger.tivo.DBPlayerContextInfo;
import org.antlr.works.debugger.tivo.DBRecorder;
import org.antlr.works.debugger.tree.DBASTModel;
import org.antlr.works.debugger.tree.DBASTPanel;
import org.antlr.works.debugger.tree.DBParseTreeModel;
import org.antlr.works.debugger.tree.DBParseTreePanel;
import org.antlr.works.editor.EditorConsole;
import org.antlr.works.editor.EditorMenu;
import org.antlr.works.editor.EditorProvider;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.grammar.EngineGrammar;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.swing.CustomSplitPanel;
import org.antlr.works.swing.CustomToggleButton;
import org.antlr.works.swing.DetachablePanel;
import org.antlr.works.swing.DetachablePanelDelegate;
import org.antlr.works.syntax.element.ElementBlock;
import org.antlr.works.syntax.element.ElementGrammarName;
import org.antlr.works.syntax.element.ElementRule;
import org.antlr.works.utils.Console;
import org.antlr.works.utils.Utils;
import org.antlr.xjlib.appkit.frame.XJDialog;
import org.antlr.xjlib.appkit.gview.GView;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.foundation.notification.XJNotificationCenter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Debugger extends EditorTab implements DetachablePanelDelegate {

    public static final String DEFAULT_LOCAL_ADDRESS = "localhost";

    public static final String NOTIF_DEBUG_STARTED = "NOTIF_DEBUG_STARTED";
    public static final String NOTIF_DEBUG_STOPPED = "NOTIF_DEBUG_STOPPED";

    public static final int OPTION_NONE = 0;
    public static final int OPTION_AGAIN = 1;
    public static final int OPTION_BUILD = 2;

    public static final float PERCENT_WIDTH_LEFT = 0.2f;
    public static final float PERCENT_WIDTH_MIDDLE = 0.5f;

    protected JPanel panel;

    protected DBInputPanel inputPanel;
    protected DBOutputPanel outputPanel;

    protected DBParseTreePanel parseTreePanel;
    protected DBParseTreeModel parseTreeModel;

    protected DBASTPanel astPanel;
    protected DBASTModel astModel;

    protected DBStackPanel stackPanel;
    protected DBEventsPanel eventsPanel;

    protected DBControlPanel controlPanel;

    protected CustomSplitPanel splitPanel;
    protected Map<Component,CustomToggleButton> components2toggle;

    protected CEditorGrammar editor;

    protected Set<Integer> breakpoints;

    protected DBLocal local;
    protected DBRecorder recorder;
    protected DBPlayer player;

    protected boolean running;
    protected long dateOfModificationOnDisk = 0;

    protected int debuggerCursorIndex = -1;

    private boolean closing = false;

    public Debugger(CEditorGrammar editor) {
        this.editor = editor;
    }

    public void awake() {
        panel = new JPanel(new BorderLayout());
        splitPanel = new CustomSplitPanel();
        components2toggle = new HashMap<Component, CustomToggleButton>();

        controlPanel = new DBControlPanel(this);

        inputPanel = new DBInputPanel(this);
        inputPanel.setTag(CustomSplitPanel.LEFT_INDEX);
        outputPanel = new DBOutputPanel(this);
        outputPanel.setTag(CustomSplitPanel.LEFT_INDEX);

        parseTreePanel = new DBParseTreePanel(this);
        parseTreePanel.setTag(CustomSplitPanel.MIDDLE_INDEX);
        parseTreeModel = parseTreePanel.getModel();

        astPanel = new DBASTPanel(this);
        astPanel.setTag(CustomSplitPanel.MIDDLE_INDEX);
        astModel = astPanel.getModel();

        stackPanel = new DBStackPanel(this);
        stackPanel.setTag(CustomSplitPanel.RIGHT_INDEX);
        eventsPanel = new DBEventsPanel(this);
        eventsPanel.setTag(CustomSplitPanel.RIGHT_INDEX);

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(splitPanel, BorderLayout.CENTER);
        panel.add(createToggleButtons(), BorderLayout.SOUTH);

        local = new DBLocal(this);
        recorder = new DBRecorder(this);
        player = new DBPlayer(this);

        updateStatusInfo();
    }

    public void close() {
        closing = true;

        debuggerStop(true);

        splitPanel.close();

        controlPanel.close();
        inputPanel.close();
        outputPanel.close();

        parseTreePanel.close();
        astPanel.close();
        stackPanel.close();
        eventsPanel.close();

        local.close();
        recorder.close();
        player.close();

        parseTreeModel.close();
        astModel.close();

        for(CustomToggleButton b : components2toggle.values()) {
            for(ActionListener al : b.getActionListeners()) {
                b.removeActionListener(al);
            }
        }
        editor = null;
    }

    public void componentShouldLayout(Dimension size) {
        assemblePanelsIntoSplitPane(size.width);
        astPanel.componentShouldLayout(new Dimension((int) (size.width*PERCENT_WIDTH_MIDDLE), size.height));
    }

    public static final int TOGGLE_INPUT = 0;
    public static final int TOGGLE_OUTPUT = 1;
    public static final int TOGGLE_PTREE = 2;
    public static final int TOGGLE_AST = 3;
    public static final int TOGGLE_STACK = 4;
    public static final int TOGGLE_EVENTS = 5;

    public Box createToggleButtons() {
        Box b = Box.createHorizontalBox();
        b.add(createToggleButton("Input", TOGGLE_INPUT, inputPanel));
        b.add(createToggleButton("Output", TOGGLE_OUTPUT, outputPanel));
        b.add(Box.createHorizontalStrut(15));
        b.add(createToggleButton("Parse Tree", TOGGLE_PTREE, parseTreePanel));
        b.add(createToggleButton("AST", TOGGLE_AST, astPanel));
        b.add(Box.createHorizontalStrut(15));
        b.add(createToggleButton("Stack", TOGGLE_STACK, stackPanel));
        b.add(createToggleButton("Events", TOGGLE_EVENTS, eventsPanel));
        b.add(Box.createHorizontalGlue());
        return b;
    }

    public JToggleButton createToggleButton(String title, int tag, Component c) {
        CustomToggleButton b = new CustomToggleButton(title);
        b.setTag(tag);
        b.setFocusable(false);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performToggleButtonAction((CustomToggleButton)e.getSource());
            }

        });
        components2toggle.put(c, b);
        return b;
    }

    public void assemblePanelsIntoSplitPane(int width) {
        setComponentVisible(inputPanel, true);
        setComponentVisible(outputPanel, false);

        setComponentVisible(parseTreePanel, true);
        setComponentVisible(astPanel, false);

        setComponentVisible(stackPanel, true);
        setComponentVisible(eventsPanel, false);

        splitPanel.setComponentWidth(inputPanel, width*PERCENT_WIDTH_LEFT);
        splitPanel.setComponentWidth(outputPanel, width*PERCENT_WIDTH_LEFT);

        splitPanel.setComponentWidth(parseTreePanel, width*PERCENT_WIDTH_MIDDLE);
        splitPanel.setComponentWidth(astPanel, width*PERCENT_WIDTH_MIDDLE);

        splitPanel.setComponents(inputPanel, parseTreePanel, stackPanel);
    }

    public void setComponentVisible(Component c, boolean flag) {
        c.setVisible(flag);
        JToggleButton b = components2toggle.get(c);
        b.setSelected(flag);
    }

    public void performToggleButtonAction(CustomToggleButton button) {
        switch(button.getTag()) {
            case TOGGLE_INPUT:
                toggleComponents(inputPanel, outputPanel, CustomSplitPanel.LEFT_INDEX);
                break;
            case TOGGLE_OUTPUT:
                toggleComponents(outputPanel, inputPanel, CustomSplitPanel.LEFT_INDEX);
                break;

            case TOGGLE_PTREE:
                toggleComponents(parseTreePanel, astPanel, CustomSplitPanel.MIDDLE_INDEX);
                break;
            case TOGGLE_AST:
                toggleComponents(astPanel, parseTreePanel, CustomSplitPanel.MIDDLE_INDEX);
                break;

            case TOGGLE_STACK:
                toggleComponents(stackPanel, eventsPanel, CustomSplitPanel.RIGHT_INDEX);
                break;
            case TOGGLE_EVENTS:
                toggleComponents(eventsPanel, stackPanel, CustomSplitPanel.RIGHT_INDEX);
                break;
        }
    }

    public void toggleComponents(DetachablePanel c, DetachablePanel other, int index) {
        c.setVisible(!c.isVisible());
        if(c.isVisible()) {
            if(!other.isDetached())
                setComponentVisible(other, false);
            if(!c.isDetached())
                splitPanel.setComponent(c, index);
        } else {
            if(other.isVisible() && !other.isDetached())
                splitPanel.setComponent(other, index);
            else
                splitPanel.setComponent(null, index);
        }
    }

    public void toggleInputTokensBox() {
        inputPanel.toggleInputTokensBox();
    }

    public boolean isInputTokenVisible() {
        return inputPanel.isInputTokensBoxVisible();
    }

    public void selectConsoleTab() {
        editor.selectConsoleTab();
    }

    public DBOutputPanel getOutputPanel() {
        return outputPanel;
    }

    public DBRecorder getRecorder() {
        return recorder;
    }

    public DBPlayer getPlayer() {
        return player;
    }

    public Container getWindowComponent() {
        return editor.getWindowContainer();
    }

    public EditorConsole getConsole() {
        return editor.getConsole();
    }

    public List<ElementBlock> getBlocks() {
        return editor.getBlocks();
    }

    public EditorProvider getProvider() {
        return editor;
    }

    public Container getContainer() {
        return panel;
    }

    public void updateStatusInfo() {
        controlPanel.updateStatusInfo();
    }

    public void breaksOnEvent() {
        inputPanel.updateOnBreakEvent();
        parseTreePanel.updateOnBreakEvent();
        astPanel.updateOnBreakEvent();
        stackPanel.updateOnBreakEvent();
        eventsPanel.updateOnBreakEvent();
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
            return breakpoints.contains(Integer.valueOf(line));
    }

    public boolean isBreakpointAtToken(Token token) {
        return inputPanel.isBreakpointAtToken(token);
    }

    public void selectToken(Token token, int line, int pos) {
        if(token != null) {
            /** If token is not null, ask the input text object the
             * line and character number.
             */

            DBInputTextTokenInfo info = inputPanel.getTokenInfoForToken(token);
            if(info == null)
                setGrammarPosition(line, pos);
            else
                setGrammarPosition(info.line, info.charInLine);
        } else {
            /** If token is null, the line and pos will be provided as parameters */
            setGrammarPosition(line, pos);
        }

        inputPanel.selectToken(token);
        parseTreePanel.selectToken(token);
        astPanel.selectToken(token);
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

    public void markLocationInGrammar(int index) {
        try {
            editor.setCaretPosition(index);
            debuggerCursorIndex = index;
        } catch(Exception e) {
            getConsole().print(e);
        }
    }

    public void resetMarkLocationInGrammar() {
        debuggerCursorIndex = -1;
    }

    public int getDebuggerCursorIndex() {
        return debuggerCursorIndex;
    }

    public List<ElementRule> getRules() {
        return editor.getRules();
    }

    public List<ElementRule> getSortedRules() {
        return editor.getSortedRules();
    }

    public String getEventsAsString() {
        return eventsPanel.getEventsAsString();
    }

    public int getNumberOfEvents() {
        return eventsPanel.getNumberOfEvents();
    }

    public void launchLocalDebugger(int options) {
        // If the grammar is dirty, build it anyway
        if(getGrammar().getType() == ElementGrammarName.TREEPARSER) {
            XJAlert.display(editor.getWindowContainer(), "Unsupported Grammar Type",
                    "ANTLRWorks supports tree grammar debugging only if you \"debug remote\".");
            return;
        }

        if(needsToGenerateGrammar()) {
            if(AWPrefs.getDebuggerAskGen()) {
                int result = XJAlert.displayCustomAlert(editor.getWindowContainer(), "Generate and compile",
                        "The grammar has been modified and needs to be generated and compiled again. You can choose " +
                                "to cancel the operation, to continue without generating and compiling the grammar or " +
                                "to generate and compile the grammar.",
                        new String[] { "Cancel", "Continue", "Generate and compile" }, 2, 0);
                switch(result) {
                    case 0: return;
                    case 2: options = options | OPTION_BUILD; break;

                }
            } else {
                options = options | OPTION_BUILD;
            }
        }

        if((options & OPTION_BUILD) > 0 && !editor.ensureDocumentSaved()) {
            return;
        }

        if((options & OPTION_BUILD) > 0 || !local.isRequiredFilesExisting()) {
            local.prepareAndLaunch(options);

            grammarGenerated();
        } else {
            local.prepareAndLaunch(options);
        }
    }

    public void debuggerLocalDidRun(boolean build) {
        if(build)
            StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_LOCAL_DEBUGGER_BUILD);
        else
            StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_LOCAL_DEBUGGER);
        debuggerLaunch(DEFAULT_LOCAL_ADDRESS, AWPrefs.getDebugDefaultLocalPort(), false);
    }

    public void launchRemoteDebugger() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_REMOTE_DEBUGGER);
        DBRemoteConnectDialog dialog = new DBRemoteConnectDialog(getWindowComponent());
        if(dialog.runModal() == XJDialog.BUTTON_OK) {
            debuggerLaunch(dialog.getAddress(), dialog.getPort(), true);
        }
    }

    public void debuggerLaunch(String address, int port, boolean remote) {
        if(remote && !debuggerLaunchGrammar()) {
            XJAlert.display(editor.getWindowContainer(), "Error",
                    "Cannot launch the debugger.\nException while parsing grammar.");
            return;
        }

        queryGrammarBreakpoints();

        inputPanel.prepareForGrammar(getGrammar());
        player.setInputBuffer(inputPanel.getInputBuffer());

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
        editor.getTextPane().getCaret().setVisible(false);

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
        if(recorder.getStatus() == DBRecorder.STATUS_STOPPING) {
            if(force || XJAlert.displayAlertYESNO(editor.getWindowContainer(), "Stopping", "The debugger is currently stopping. Do you want to force stop it ?") == XJAlert.YES) {
                local.forceStop();
                recorder.stop();
            }
        } else
            recorder.requestStop();
    }

    public boolean isRunning() {
        return running;
    }

    public void resetGUI() {
        stackPanel.clear();
        eventsPanel.clear();
        parseTreePanel.clear();
        astPanel.clear();
    }

    public int computeAbsoluteGrammarIndex(int lineIndex, int pos) {
        List<ATELine> lines = editor.getLines();
        if(lineIndex-1<0 || lineIndex-1 >= lines.size())
            return -1;

        ATELine line = lines.get(lineIndex-1);
        return line.position+pos-1;
    }

    public void addEvent(DBEvent event, DBPlayerContextInfo info) {
        eventsPanel.addEvent(event, info);
    }

    public void playEvents(List events, int lastEventPosition, boolean reset) {
        player.playEvents(events, lastEventPosition, reset);
        breaksOnEvent();
    }

    public void playerSetLocation(int line, int pos) {
        parseTreeModel.setLocation(line, pos);
    }

    public void playerPushRule(String ruleName) {
        stackPanel.pushRule(ruleName);
        parseTreeModel.pushRule(ruleName);
        astModel.pushRule(ruleName);
    }

    public void playerPopRule(String ruleName) {
        stackPanel.popRule();
        parseTreeModel.popRule();
        astModel.popRule();
    }

    public void playerConsumeToken(Token token) {
        parseTreeModel.addToken(token);
    }

    public void playerRecognitionException(Exception e) {
        parseTreeModel.addException(e);
    }

    public void playerBeginBacktrack(int level) {
        parseTreeModel.beginBacktrack(level);
    }

    public void playerEndBacktrack(int level, boolean success) {
        parseTreeModel.endBacktrack(level, success);
    }

    public void playerNilNode(int id) {
        astModel.nilNode(id);
    }

	public void playerErrorNode(int id, String text) {
		astModel.errorNode(id, text);
	}

    public void playerCreateNode(int id, Token token) {
        astModel.createNode(id, token);
    }

    public void playerCreateNode(int id, String text, int type) {
        astModel.createNode(id, new ClassicToken(type, text));
    }

    public void playerBecomeRoot(int newRootID, int oldRootID) {
        astModel.becomeRoot(newRootID, oldRootID);
    }

    public void playerAddChild(int rootID, int childID) {
        astModel.addChild(rootID, childID);
    }

    public void playerSetTokenBoundaries(int id, int startIndex, int stopIndex) {
        /** Currently ignored */
    }

    public void recorderStatusDidChange() {
        if(closing) return;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateStatusInfo();
            }
        });
    }

    public void recorderDidStop() {
        if(closing) return;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                resetMarkLocationInGrammar();
                editor.getTextPane().setEditable(true);
                editor.getTextPane().requestFocusInWindow();

                // Tells the caret to be visible a little bit later
                // to let Swing focus the component
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        editor.getTextPane().getCaret().setVisible(true);
                    }
                });

                inputPanel.stop();
                running = false;
                editor.refreshMainMenuBar();
                XJNotificationCenter.defaultCenter().postNotification(this, NOTIF_DEBUG_STOPPED);
            }
        });
    }

    public boolean canExportToBitmap() {
        return getExportableGView() != null;
    }

    public boolean canExportToEPS() {
        return getExportableGView() != null;
    }

    public GView getExportableGView() {
        Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        if(Utils.isComponentChildOf(c, parseTreePanel))
            return parseTreePanel.getGraphView();
        else if(Utils.isComponentChildOf(c, astPanel))
            return astPanel.getGraphView();
        else
            return null;
    }

    public String getTabName() {
        return "Debugger";
    }

    public Component getTabComponent() {
        return getContainer();
    }

    public JPopupMenu treeGetContextualMenu() {
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

/*        Integer i = (Integer)data.get(KEY_SPLITPANE_A);
        if(i != null)
            ioSplitPane.setDividerLocation(i.intValue());

        i = (Integer)data.get(KEY_SPLITPANE_B);
        if(i != null)
            ioTreeSplitPane.setDividerLocation(i.intValue());

        i = (Integer)data.get(KEY_SPLITPANE_C);
        if(i != null)
            treeInfoPanelSplitPane.setDividerLocation(i.intValue());*/
    }

    public Map getPersistentData() {
        /*Map data = new HashMap();
        data.put(KEY_SPLITPANE_A, new Integer(ioSplitPane.getDividerLocation()));
        data.put(KEY_SPLITPANE_B, new Integer(ioTreeSplitPane.getDividerLocation()));
        data.put(KEY_SPLITPANE_C, new Integer(treeInfoPanelSplitPane.getDividerLocation()));
        return data;*/
        return new HashMap();
    }

    public void panelDoDetach(DetachablePanel panel) {
        splitPanel.setComponent(null, panel.getTag());
    }

    public void panelDoAttach(DetachablePanel panel) {
        Component c = splitPanel.getComponentAtIndex(panel.getTag());
        if(c != null) {
            c.setVisible(false);
            splitPanel.setComponent(null, panel.getTag());

            CustomToggleButton button = components2toggle.get(c);
            button.setSelected(false);
        }
        splitPanel.setComponent(panel, panel.getTag());
    }

    public void panelDoClose(DetachablePanel panel) {
        CustomToggleButton button = components2toggle.get(panel);
        button.setSelected(false);
    }

    public Container panelParentContainer() {
        return editor.getJavaContainer();
    }

    public boolean canDebugAgain() {
        return local.canDebugAgain();
    }

    public void warning(Object o, String message) {
        getConsole().println("["+o.getClass().getName()+" - event "+getNumberOfEvents()+"] Warning: "+message, Console.LEVEL_WARNING);
    }

    public String getOutputPath() {
        return editor.getOutputPath();
    }
}
