package org.antlr.works.components;

import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.IconManager;
import org.antlr.works.utils.Toolbar;
import org.antlr.xjlib.appkit.swing.XJRollOverButton;
import org.antlr.xjlib.appkit.swing.XJRollOverButtonToggle;
import org.antlr.xjlib.foundation.notification.XJNotificationCenter;
import org.antlr.xjlib.foundation.notification.XJNotificationObserver;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class GrammarWindowToolbar implements XJNotificationObserver {

    public Toolbar toolbar;

    public JButton debug;
    public JButton debugAgain;

    public JButton backward;
    public JButton forward;

    public JToggleButton sort;
    public JToggleButton sd;
    public JToggleButton coloring;
    public JToggleButton ideas;

    public JButton find;

    public GrammarWindow window;

    public GrammarWindowToolbar(GrammarWindow window) {
        this.window = window;

        createInterface();
        addActions();
        
        debugAgain.setEnabled(false);

        XJNotificationCenter.defaultCenter().addObserver(this, DebuggerTab.NOTIF_DEBUG_STARTED);
        XJNotificationCenter.defaultCenter().addObserver(this, DebuggerTab.NOTIF_DEBUG_STOPPED);
    }

    public void close() {
        window = null;
        AWPrefs.getPreferences().unbindFromPreferences(sort, AWPrefs.PREF_TOOLBAR_SORT);        
        XJNotificationCenter.defaultCenter().removeObserver(this);
    }

    public JComponent getToolbar() {
        return toolbar;
    }

    public void notificationFire(Object source, String name) {
        if(name.equals(DebuggerTab.NOTIF_DEBUG_STARTED)) {
            find.setEnabled(false);
            debug.setEnabled(false);
            debugAgain.setEnabled(false);
        } else if(name.equals(DebuggerTab.NOTIF_DEBUG_STOPPED)) {
            find.setEnabled(true);
            debug.setEnabled(true);
            debugAgain.setEnabled(window.getDebuggerTab().canDebugAgain());
        }
    }

    public void createInterface() {
        toolbar = Toolbar.createHorizontalToolbar();
        toolbar.addElement(sd = createToggleButton(IconManager.shared().getIconSyntaxDiagram(), "Toggle Syntax diagram"));
        toolbar.addElement(coloring = createToggleButton(IconManager.shared().getIconColoring(), "Toggle Syntax coloring"));
        toolbar.addElement(ideas = createToggleButton(IconManager.shared().getIconIdea(), "Toggle Syntax ideas"));
        toolbar.addGroupSeparator();
        toolbar.addElement(sort = createToggleButton(IconManager.shared().getIconSort(), "Toggle Sort rules"));
        toolbar.addElement(find = createButton(IconManager.shared().getIconFind(), "Find text"));
        toolbar.addGroupSeparator();
        toolbar.addElement(backward = createButton(IconManager.shared().getIconBackward(), "Back"));
        toolbar.addElement(forward = createButton(IconManager.shared().getIconForward(), "Forward"));
        toolbar.addGroupSeparator();
        toolbar.addElement(debug = createButton(IconManager.shared().getIconDebug(), "Debug"));
        toolbar.addElement(debugAgain = createButton(IconManager.shared().getIconDebugAgain(), "Debug Again"));

        AWPrefs.getPreferences().bindToPreferences(sort, AWPrefs.PREF_TOOLBAR_SORT, false);
    }

    public void updateStates() {
        sort.setSelected(window.isRulesSorted());
        sd.setSelected(window.isSyntaxDiagramDisplayed());
        coloring.setSelected(window.isSyntaxColored());
        ideas.setSelected(window.isIdeasEnabled());
    }

    public void awake() {
        sd.setSelected(true);
        coloring.setSelected(true);
        ideas.setSelected(true);
    }

    public void addActions() {
        backward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.goToBackward();
            }
        });

        forward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.goToForward();
            }
        });

        sort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.toggleRulesSorting();
            }
        });

        sd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.toggleSyntaxDiagram();
            }
        });

        coloring.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.toggleSyntaxColoring();
            }
        });

        ideas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.toggleIdeas();
            }
        });

        find.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.find();
            }
        });

        debug.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.getDebugMenu().debug();
            }
        });

        debugAgain.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.getDebugMenu().debugAgain();
            }
        });

    }

    public JButton createButton(ImageIcon icon, String tooltip) {
        JButton b = XJRollOverButton.createMediumButton(icon);
        b.setToolTipText(tooltip);
        return b;
    }

    public JToggleButton createToggleButton(ImageIcon icon, String tooltip) {
        JToggleButton b = XJRollOverButtonToggle.createMediumButton(icon);
        b.setToolTipText(tooltip);
        return b;
    }


}
