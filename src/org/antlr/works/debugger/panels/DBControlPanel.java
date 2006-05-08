package org.antlr.works.debugger.panels;

import org.antlr.works.debugger.Debugger;
import org.antlr.works.debugger.events.DBEvent;
import org.antlr.works.debugger.tivo.DBRecorder;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.Statistics;
import org.antlr.works.utils.IconManager;
import org.antlr.works.utils.NumberSet;

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

    protected JCheckBox breakAll;
    protected JCheckBox breakLocation;
    protected JCheckBox breakConsume;
    protected JCheckBox breakLT;
    protected JCheckBox breakException;

    protected JLabel infoLabel;

    protected Debugger debugger;

    public DBControlPanel(Debugger debugger) {
        super(new BorderLayout());

        this.debugger = debugger;

        Box box = Box.createHorizontalBox();
        box.add(createRevealTokensButton());
        box.add(Box.createHorizontalStrut(20));
        box.add(stopButton = createStopButton());
        box.add(Box.createHorizontalStrut(20));
        box.add(goToStartButton = createGoToStartButton());
        box.add(backButton = createStepBackButton());
        box.add(fastForwardButton = createFastForwardButton());
        box.add(forwardButton = createStepForwardButton());
        box.add(goToEndButton = createGoToEndButton());
        box.add(Box.createHorizontalStrut(20));
        box.add(createBreakEventsBox());
        box.add(Box.createHorizontalGlue());
        box.add(createInfoLabelPanel());

        add(box, BorderLayout.CENTER);
    }

    public JButton createStopButton() {
        JButton button = new JButton(IconManager.shared().getIconStop());
        button.setToolTipText("Stop");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                debugger.debuggerStop(false);
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
                debugger.getRecorder().stepBackward(getBreakEvent());
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
                debugger.getRecorder().stepForward(getBreakEvent());
                updateInterfaceLater();
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_STEP_FORWARD);
            }
        });
        return button;
    }

    public JButton createGoToStartButton() {
        JButton button = new JButton(IconManager.shared().getIconGoToStart());
        button.setToolTipText("Go To Start");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                debugger.restorePreviousGrammarAttributeSet();
                debugger.getRecorder().goToStart();
                updateInterfaceLater();
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_GOTO_START);
            }
        });
        return button;
    }

    public JButton createGoToEndButton() {
        JButton button = new JButton(IconManager.shared().getIconGoToEnd());
        button.setToolTipText("Go To End");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                debugger.getRecorder().goToEnd();
                updateInterfaceLater();
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_GOTO_END);
            }
        });
        return button;
    }

    public JButton createFastForwardButton() {
        JButton button = new JButton(IconManager.shared().getIconFastForward());
        button.setToolTipText("Fast forward");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                debugger.getRecorder().fastForward();
                updateInterfaceLater();
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_FAST_FORWARD);
            }
        });
        return button;
    }

    static int[] COMBO_BREAK_EVENTS = new int[] { DBEvent.LOCATION,
            DBEvent.CONSUME_TOKEN,
            DBEvent.LT,
            DBEvent.RECOGNITION_EXCEPTION,
            DBEvent.ALL
    };

    public JComponent createBreakEventsBox() {
        Box box = Box.createHorizontalBox();

        box.add(new JLabel("Break on:"));
        box.add(breakAll = createBreakButton("All"));
        box.add(breakLocation = createBreakButton("Location"));
        box.add(breakConsume = createBreakButton("Consume"));
        box.add(breakLT = createBreakButton("LT"));
        box.add(breakException = createBreakButton("Exception"));

        AWPrefs.getPreferences().bindToPreferences(breakAll, AWPrefs.PREF_DEBUG_BREAK_ALL, false);
        AWPrefs.getPreferences().bindToPreferences(breakLocation, AWPrefs.PREF_DEBUG_BREAK_LOCACTION, false);
        AWPrefs.getPreferences().bindToPreferences(breakConsume, AWPrefs.PREF_DEBUG_BREAK_CONSUME, true);
        AWPrefs.getPreferences().bindToPreferences(breakLT, AWPrefs.PREF_DEBUG_BREAK_LT, false);
        AWPrefs.getPreferences().bindToPreferences(breakException, AWPrefs.PREF_DEBUG_BREAK_EXCEPTION, false);

        return box;
    }

    public JCheckBox createBreakButton(String title) {
        JCheckBox button = new JCheckBox(title);
        button.setFocusable(false);
        return button;
    }

    public JButton createRevealTokensButton() {
        JButton tokenButton = new JButton(IconManager.shared().getIconTokens());
        tokenButton.setToolTipText("Reveal tokens in input text");
        tokenButton.setFocusable(false);
        tokenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                debugger.getPlayer().toggleInputTextTokensBox();
                Statistics.shared().recordEvent(Statistics.EVENT_DEBUGGER_TOGGLE_INPUT_TOKENS);
            }
        });
        return tokenButton;
    }

    public JComponent createInfoLabelPanel() {
        infoLabel = new JLabel();
        return infoLabel;
    }

    public Set getBreakEvent() {
        NumberSet set = new NumberSet();

        if(breakAll.isSelected())
            set.add(DBEvent.ALL);

        if(breakLocation.isSelected())
            set.add(DBEvent.LOCATION);

        if(breakConsume.isSelected())
            set.add(DBEvent.CONSUME_TOKEN);

        if(breakLT.isSelected())
            set.add(DBEvent.LT);

        if(breakException.isSelected())
            set.add(DBEvent.RECOGNITION_EXCEPTION);

        return set;
    }

    public void updateStatusInfo() {
        String info = "-";
        switch(debugger.getRecorder().getStatus()) {
            case DBRecorder.STATUS_STOPPED: info = "Stopped"; break;
            case DBRecorder.STATUS_STOPPING: info = "Stopping"; break;
            case DBRecorder.STATUS_LAUNCHING: info = "Launching"; break;
            case DBRecorder.STATUS_RUNNING: info = "Running"; break;
            case DBRecorder.STATUS_BREAK: info = "Break on "+DBEvent.getEventName(debugger.getRecorder().getStoppedOnEvent()); break;
        }
        infoLabel.setText("Status: "+info);
        updateInterface();
    }

    public void updateInterface() {
        stopButton.setEnabled(debugger.getRecorder().getStatus() != DBRecorder.STATUS_STOPPED);

        boolean enabled = debugger.getRecorder().isAlive();
        boolean atBeginning = debugger.getRecorder().isAtBeginning();
        boolean atEnd = debugger.getRecorder().isAtEnd();

        backButton.setEnabled(enabled && !atBeginning);
        forwardButton.setEnabled(enabled && !atEnd);
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
