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
import org.antlr.works.components.GrammarWindowMenu;
import org.antlr.works.debugger.api.DebuggerDelegate;
import org.antlr.works.debugger.events.DBEvent;
import org.antlr.works.debugger.events.DBEventEnterRule;
import org.antlr.works.debugger.events.DBEventExitRule;
import org.antlr.works.debugger.events.DBEventLocation;
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
import org.antlr.works.editor.GrammarWindowTab;
import org.antlr.works.grammar.element.ElementBlock;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.utils.*;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.frame.XJDialog;
import org.antlr.xjlib.appkit.gview.GView;
import org.antlr.xjlib.appkit.swing.XJRotableToggleButton;
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

public class DebuggerTab extends GrammarWindowTab implements DetachablePanelDelegate {

    public static final String DEFAULT_LOCAL_ADDRESS = "localhost";

    public static final String NOTIF_DEBUG_STARTED = "NOTIF_DEBUG_STARTED";
    public static final String NOTIF_DEBUG_STOPPED = "NOTIF_DEBUG_STOPPED";

    public static final int OPTION_NONE = 0;
    public static final int OPTION_AGAIN = 1;
    public static final int OPTION_BUILD = 2;
    public static final int OPTION_RUN = 4;

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
    protected Map<Component, XJRotableToggleButton> components2toggle;

    protected Map<Integer, Set<String>> breakpoints;

    protected DBLocal local;
    protected DBRecorder recorder;
    protected DBPlayer player;

    protected boolean running;
    protected long dateOfModificationOnDisk = 0;

    private boolean closing = false;
    private String startRule;

    private DebuggerDelegate delegate;
    private String rootGrammarName;

    public DebuggerTab(DebuggerDelegate delegate) {
        super(null);
        this.delegate = delegate;
    }

    public void awake() {
        panel = new JPanel(new BorderLayout());
        splitPanel = new CustomSplitPanel();
        components2toggle = new HashMap<Component, XJRotableToggleButton>();

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

        for(XJRotableToggleButton b : components2toggle.values()) {
            b.removeAllActionListeners();
        }

        delegate = null;
    }

    public void componentShouldLayout(Dimension size) {
        assemblePanelsIntoSplitPane(size.width);
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

    public XJRotableToggleButton createToggleButton(String title, final int tag, Component c) {
        XJRotableToggleButton b = new XJRotableToggleButton(title);
        b.setFocusable(false);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performToggleButtonAction(tag);
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
        XJRotableToggleButton b = components2toggle.get(c);
        b.setSelected(flag);
    }

    public void performToggleButtonAction(int tag) {
        switch(tag) {
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

    public DBOutputPanel getOutputPanel() {
        return outputPanel;
    }

    public DBRecorder getRecorder() {
        return recorder;
    }

    public DBPlayer getPlayer() {
        return player;
    }

    public DebuggerDelegate getDelegate() {
        return delegate;
    }

    public List<ElementBlock> getBlocks() {
        return delegate.getBlocks();
    }

    public Container getWindowContainer() {
        return XJApplication.getActiveContainer();
    }

    public Container getContainer() {
        return panel;
    }

    public Console getConsole() {
        return delegate.getConsole();
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

    public boolean needsToGenerateGrammar() {
        return dateOfModificationOnDisk != delegate.getDocument().getDateOfModificationOnDisk()
                || delegate.getDocument().isDirty();
    }

    public void grammarGenerated() {
        delegate.getDocument().autoSave();
        dateOfModificationOnDisk = delegate.getDocument().getDateOfModificationOnDisk();
    }

    public void queryGrammarBreakpoints() {
        this.breakpoints = delegate.getBreakpoints();
    }

    public boolean isBreakpointAtLine(int line, String name) {
        if(breakpoints == null) return false;

        Set<String> names = breakpoints.get(line);
        if(names == null) return false;

        // return true if the current grammar matches
        return names.contains(name);
    }

    public boolean isBreakpointAtToken(Token token) {
        return inputPanel.isBreakpointAtToken(token);
    }

    public void selectToken(Token token, DBEventLocation location) {
        if(token != null) {
            /** If token is not null, ask the input text object the
             * line and character number.
             */

            DBInputTextTokenInfo info = inputPanel.getTokenInfoForToken(token);
            if(info == null)
                selectGrammarText(location);
            else
                selectGrammarText(info.getLocation());
        } else {
            /** If token is null, the line and pos will be provided as parameters */
            selectGrammarText(location);
        }

        inputPanel.selectToken(token);
        parseTreePanel.selectToken(token);
        astPanel.selectToken(token);
    }

    public void selectGrammarText(DBEventLocation location) {
        if(location != null) {
        delegate.debuggerSelectText(location.getGrammarName(), location.line, location.pos);
    }
    }

    public void setGrammarLocation(DBEventLocation location) {
        if(location != null) {
            delegate.debuggerSetLocation(location.getGrammarName(), location.line, location.pos);
        }
    }

    public void resetGrammarLocation() {
        delegate.debuggerSetLocation(rootGrammarName, -1, -1);
    }

    public List<ElementRule> getRules() {
        return delegate.getRules();
    }

    public List<ElementRule> getSortedRules() {
        return delegate.getSortedRules();
    }

    public void setStartRule(String rule) {
        this.startRule = rule;
    }

    public String getStartRule() {
        if(startRule == null && !getRules().isEmpty()) {
            startRule = getRules().get(0).name;
        }
        return startRule;
    }

    public String getEventsAsString() {
        return eventsPanel.getEventsAsString();
    }

    public int getNumberOfEvents() {
        return eventsPanel.getNumberOfEvents();
    }

    public void launchLocalDebugger(int options) {
        if(needsToGenerateGrammar()) {
            if(AWPrefs.getDebuggerAskGen()) {
                int result = XJAlert.createInstance().displayCustomAlert(getWindowContainer(), "Generate and compile",
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

        if((options & OPTION_BUILD) > 0 && !delegate.ensureDocumentSaved()) {
            return;
        }

        if((options & OPTION_BUILD) > 0 || !local.isRequiredFilesExisting()) {
            local.prepareAndLaunch(options);

            grammarGenerated();
        } else {
            local.prepareAndLaunch(options);
        }
    }

    public boolean debuggerLocalDidRun(boolean build) {
        if(build)
            StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_LOCAL_DEBUGGER_BUILD);
        else
            StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_LOCAL_DEBUGGER);
        return debuggerLaunch(DEFAULT_LOCAL_ADDRESS, AWPrefs.getDebugDefaultLocalPort(), false);
    }

    public void launchRemoteDebugger() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_REMOTE_DEBUGGER);
        DBRemoteConnectDialog dialog = new DBRemoteConnectDialog(getWindowContainer());
        if(dialog.runModal() == XJDialog.BUTTON_OK) {
            debuggerLaunch(dialog.getAddress(), dialog.getPort(), true);
        }
    }

    public boolean debuggerLaunch(String address, int port, boolean remote) {
        if(remote && !debuggerLaunchGrammar()) {
            XJAlert.display(getWindowContainer(), "Error",
                    "Cannot launch the debuggerTab.\nException while parsing grammar.");
            return false;
        }

        queryGrammarBreakpoints();
        rootGrammarName = delegate.getGrammarName();
        inputPanel.prepareForGrammar(delegate.getGrammarEngine());
        player.setInputBuffer(inputPanel.getInputBuffer());

        recorder.connect(address, port);
        return true;
    }

    public void showEditTestRig() {
        local.showEditTestRig();
    }

    public void connectionSuccess() {
        // First set the flag to true before doing anything else
        running = true;

        // The  send the notification
        // todo still needed with the delegate?
        XJNotificationCenter.defaultCenter().postNotification(this, NOTIF_DEBUG_STARTED);

        delegate.debuggerStarted();

        player.resetPlayEvents(true);
    }

    public void connectionFailed() {
        XJAlert.display(getWindowContainer(), "Connection Error",
                "Cannot launch the debuggerTab.\nTime-out waiting to connect to the remote parser.");
    }

    public void connectionCancelled() {
    }

    public boolean debuggerLaunchGrammar() {
        try {
            delegate.getGrammarEngine().analyze();
        } catch (Exception e) {
            getConsole().print(e);
            return false;
        }
        return true;
    }

    public void debuggerStop(boolean force) {
        if(recorder.getStatus() == DBRecorder.STATUS_STOPPING) {
            if(force || XJAlert.displayAlertYESNO(getWindowContainer(), "Stopping", "The debuggerTab is currently stopping. Do you want to force stop it ?") == XJAlert.YES) {
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

    public void addEvent(DBEvent event, DBPlayerContextInfo info) {
        eventsPanel.addEvent(event, info);
    }

    public void playEvents(List events, int lastEventPosition, boolean reset) {
        player.playEvents(events, lastEventPosition, reset);
        breaksOnEvent();
    }

    public void playerSetLocation(DBEventLocation location) {
        parseTreeModel.setLocation(location);
    }

    public void playerPushRule(DBEventEnterRule rule) {
        stackPanel.pushRule(rule);
        parseTreeModel.pushRule(rule.name);
        astModel.pushRule(rule.name);
    }

    public void playerPopRule(DBEventExitRule ruleName) {
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
                resetGrammarLocation();

                inputPanel.stop();
                running = false;

                delegate.debuggerStopped();
                XJNotificationCenter.defaultCenter().postNotification(this, NOTIF_DEBUG_STOPPED);
            }
        });
    }

    @Override
    public boolean canExportToBitmap() {
        return getExportableGView() != null;
    }

    @Override
    public boolean canExportToEPS() {
        return getExportableGView() != null;
    }

    @Override
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
        return "DebuggerTab";
    }

    public Component getTabComponent() {
        return getContainer();
    }

    public JPopupMenu treeGetContextualMenu() {
        ContextualMenuFactory factory = delegate.createContextualMenuFactory();
        factory.addItem(GrammarWindowMenu.MI_EXPORT_AS_EPS);
        factory.addItem(GrammarWindowMenu.MI_EXPORT_AS_IMAGE);
        return factory.menu;
    }

    public void panelDoDetach(DetachablePanel panel) {
        splitPanel.setComponent(null, panel.getTag());
    }

    public void panelDoAttach(DetachablePanel panel) {
        Component c = splitPanel.getComponentAtIndex(panel.getTag());
        if(c != null) {
            c.setVisible(false);
            splitPanel.setComponent(null, panel.getTag());

            XJRotableToggleButton button = components2toggle.get(c);
            button.setSelected(false);
        }
        splitPanel.setComponent(panel, panel.getTag());
    }

    public void panelDoClose(DetachablePanel panel) {
        XJRotableToggleButton button = components2toggle.get(panel);
        button.setSelected(false);
    }

    public Container panelParentContainer() {
        return delegate.getContainer();
    }

    public boolean canDebugAgain() {
        return local.canDebugAgain();
    }

    public void warning(Object o, String message) {
        getConsole().println("["+o.getClass().getName()+" - event "+getNumberOfEvents()+"] Warning: "+message, Console.LEVEL_WARNING);
    }

    public void selectConsoleTab() {
        delegate.selectConsoleTab();
    }
}
