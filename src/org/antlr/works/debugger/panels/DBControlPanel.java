package org.antlr.works.debugger.panels;

import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.debugger.events.DBEvent;
import org.antlr.works.debugger.tivo.DBPlayerContextInfo;
import org.antlr.works.debugger.tivo.DBRecorder;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.utils.IconManager;
import org.antlr.works.utils.NumberSet;
import org.antlr.works.utils.Toolbar;
import org.antlr.xjlib.appkit.swing.XJRollOverButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
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

public class DBControlPanel extends JPanel {

    protected JButton stopButton;
    protected JButton goToStartButton;
    protected JButton goToEndButton;
    protected JButton fastForwardButton;
    protected JButton backButton;
    protected JButton forwardButton;
    protected JButton stepOverButton;

    protected JCheckBox breakAllButton;
    protected JCheckBox breakLocationButton;
    protected JCheckBox breakConsumeButton;
    protected JCheckBox breakLTButton;
    protected JCheckBox breakExceptionButton;

    protected JLabel infoLabel;

    protected DebuggerTab debuggerTab;

    public DBControlPanel(DebuggerTab debuggerTab) {
        super(new BorderLayout());

        this.debuggerTab = debuggerTab;

        Toolbar box = Toolbar.createHorizontalToolbar();
        box.addElement(stopButton = createStopButton());
        box.addGroupSeparator();
        box.addElement(goToStartButton = createGoToStartButton());
        box.addElement(backButton = createStepBackButton());
        box.addElement(forwardButton = createStepForwardButton());
        box.addElement(stepOverButton = createStepOverButton());
        box.addElement(fastForwardButton = createFastForwardButton());
        box.addElement(goToEndButton = createGoToEndButton());
        box.addGroupSeparator();
        createBreakEvents(box);
        box.addElement(Box.createHorizontalGlue());
        box.addElement(createInfoLabelPanel());

        add(box, BorderLayout.CENTER);
    }

    public void close() {
        AWPrefs.getPreferences().unbindFromPreferences(breakAllButton, AWPrefs.PREF_DEBUG_BREAK_ALL);
        AWPrefs.getPreferences().unbindFromPreferences(breakLocationButton, AWPrefs.PREF_DEBUG_BREAK_LOCATION);
        AWPrefs.getPreferences().unbindFromPreferences(breakConsumeButton, AWPrefs.PREF_DEBUG_BREAK_CONSUME);
        AWPrefs.getPreferences().unbindFromPreferences(breakLTButton, AWPrefs.PREF_DEBUG_BREAK_LT);
        AWPrefs.getPreferences().unbindFromPreferences(breakExceptionButton, AWPrefs.PREF_DEBUG_BREAK_EXCEPTION);

        debuggerTab = null;
    }

    public JButton createStopButton() {
        JButton button = XJRollOverButton.createMediumButton(IconManager.shared().getIconStop());
        button.setToolTipText("Stop");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                debuggerTab.debuggerStop(false);
                StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_DEBUGGER_STOP);
            }
        });
        return button;
    }

    public JButton createStepBackButton() {
        JButton button = XJRollOverButton.createMediumButton(IconManager.shared().getIconStepBackward());
        button.setToolTipText("Step Back");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                debuggerTab.getRecorder().stepBackward(getBreakEvent());
                updateInterfaceLater();
                StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_DEBUGGER_STEP_BACK);
            }
        });
        return button;
    }

    public JButton createStepForwardButton() {
        JButton button = XJRollOverButton.createMediumButton(IconManager.shared().getIconStepForward());
        button.setToolTipText("Step Forward");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                debuggerTab.getRecorder().stepForward(getBreakEvent());
                updateInterfaceLater();
                StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_DEBUGGER_STEP_FORWARD);
            }
        });
        return button;
    }

    public JButton createStepOverButton() {
        JButton button = XJRollOverButton.createMediumButton(IconManager.shared().getIconStepOver());
        button.setToolTipText("Step Over");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                debuggerTab.getRecorder().stepOver();
                updateInterfaceLater();
                StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_DEBUGGER_STEP_OVER);
            }
        });
        return button;
    }

    public JButton createGoToStartButton() {
        JButton button = XJRollOverButton.createMediumButton(IconManager.shared().getIconGoToStart());
        button.setToolTipText("Go To Start");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                debuggerTab.resetGrammarLocation();
                debuggerTab.getRecorder().goToStart();
                updateInterfaceLater();
                StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_DEBUGGER_GOTO_START);
            }
        });
        return button;
    }

    public JButton createGoToEndButton() {
        JButton button = XJRollOverButton.createMediumButton(IconManager.shared().getIconGoToEnd());
        button.setToolTipText("Go To End");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                debuggerTab.getRecorder().goToEnd();
                updateInterfaceLater();
                StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_DEBUGGER_GOTO_END);
            }
        });
        return button;
    }

    public JButton createFastForwardButton() {
        JButton button = XJRollOverButton.createMediumButton(IconManager.shared().getIconFastForward());
        button.setToolTipText("Fast forward");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                debuggerTab.getRecorder().fastForward();
                updateInterfaceLater();
                StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_DEBUGGER_FAST_FORWARD);
            }
        });
        return button;
    }

    public void createBreakEvents(Toolbar box) {
        box.addElement(new JLabel("Break on:"));
        box.addElement(breakAllButton = createBreakButton("All"));
        box.addElement(breakLocationButton = createBreakButton("Location"));
        box.addElement(breakConsumeButton = createBreakButton("Consume"));
        box.addElement(breakLTButton = createBreakButton("LT"));
        box.addElement(breakExceptionButton = createBreakButton("Exception"));

        AWPrefs.getPreferences().bindToPreferences(breakAllButton, AWPrefs.PREF_DEBUG_BREAK_ALL, false);
        AWPrefs.getPreferences().bindToPreferences(breakLocationButton, AWPrefs.PREF_DEBUG_BREAK_LOCATION, false);
        AWPrefs.getPreferences().bindToPreferences(breakConsumeButton, AWPrefs.PREF_DEBUG_BREAK_CONSUME, true);
        AWPrefs.getPreferences().bindToPreferences(breakLTButton, AWPrefs.PREF_DEBUG_BREAK_LT, false);
        AWPrefs.getPreferences().bindToPreferences(breakExceptionButton, AWPrefs.PREF_DEBUG_BREAK_EXCEPTION, false);
    }

    public JCheckBox createBreakButton(String title) {
        JCheckBox button = new JCheckBox(title);
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /** Select 'All' if no events are selected */
                if(getBreakEvent().isEmpty()) {
                    breakAllButton.setSelected(true);
                    AWPrefs.getPreferences().setBoolean(AWPrefs.PREF_DEBUG_BREAK_ALL, true);
                }
            }
        });
        return button;
    }

    public JComponent createInfoLabelPanel() {
        infoLabel = new JLabel();
        return infoLabel;
    }

    public Set getBreakEvent() {
        NumberSet set = new NumberSet();

        if(breakAllButton.isSelected())
            set.add(DBEvent.ALL);

        if(breakLocationButton.isSelected())
            set.add(DBEvent.LOCATION);

        if(breakConsumeButton.isSelected())
            set.add(DBEvent.CONSUME_TOKEN);

        if(breakLTButton.isSelected())
            set.add(DBEvent.LT);

        if(breakExceptionButton.isSelected())
            set.add(DBEvent.RECOGNITION_EXCEPTION);

        return set;
    }

    public void updateStatusInfo() {
        StringBuilder info = new StringBuilder();

        String status = "-";
        switch(debuggerTab.getRecorder().getStatus()) {
            case DBRecorder.STATUS_STOPPED: status = "Stopped"; break;
            case DBRecorder.STATUS_STOPPING: status = "Stopping"; break;
            case DBRecorder.STATUS_LAUNCHING: status = "Launching"; break;
            case DBRecorder.STATUS_RUNNING: status = "Running"; break;
            case DBRecorder.STATUS_BREAK: status = "Break on "+DBEvent.getEventName(debuggerTab.getRecorder().getStoppedOnEvent()); break;
        }

        info.append(status);

        DBPlayerContextInfo context = debuggerTab.getPlayer().getContextInfo();
        if(context.isBacktracking()) {
            info.append(" (backtrack ");
            info.append(context.getBacktrack());
            info.append(")");
        }

        infoLabel.setText(info.toString());
        updateInterface();
    }

    public void updateInterface() {
        stopButton.setEnabled(debuggerTab.getRecorder().getStatus() != DBRecorder.STATUS_STOPPED);

        boolean enabled = debuggerTab.getRecorder().isAlive();
        boolean atBeginning = debuggerTab.getRecorder().isAtBeginning();
        boolean atEnd = debuggerTab.getRecorder().isAtEnd();

        backButton.setEnabled(enabled && !atBeginning);
        forwardButton.setEnabled(enabled && !atEnd);
        stepOverButton.setEnabled(enabled && !atEnd);
        fastForwardButton.setEnabled(enabled && !atEnd);
        goToStartButton.setEnabled(enabled && !atBeginning);
        goToEndButton.setEnabled(enabled && !atEnd);
    }

    public void updateInterfaceLater() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateInterface();
            }
        });
    }

}
