package org.antlr.works.editor;

import org.antlr.xjlib.foundation.notification.XJNotificationCenter;
import org.antlr.xjlib.foundation.notification.XJNotificationObserver;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.swing.Toolbar;
import org.antlr.works.utils.IconManager;

import javax.swing.*;
import java.awt.*;
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

public class EditorToolbar implements XJNotificationObserver {

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

    public CEditorGrammar editor;

    public EditorToolbar(CEditorGrammar editor) {
        this.editor = editor;

        createInterface();
        addActions();
        
        debugAgain.setEnabled(false);

        XJNotificationCenter.defaultCenter().addObserver(this, Debugger.NOTIF_DEBUG_STARTED);
        XJNotificationCenter.defaultCenter().addObserver(this, Debugger.NOTIF_DEBUG_STOPPED);
    }

    public void close() {
        XJNotificationCenter.defaultCenter().removeObserver(this);
    }

    public JComponent getToolbar() {
        return toolbar;
    }

    public void notificationFire(Object source, String name) {
        if(name.equals(Debugger.NOTIF_DEBUG_STARTED)) {
            find.setEnabled(false);
            debug.setEnabled(false);
            debugAgain.setEnabled(false);
        } else if(name.equals(Debugger.NOTIF_DEBUG_STOPPED)) {
            find.setEnabled(true);
            debug.setEnabled(true);
            debugAgain.setEnabled(editor.debugger.canDebugAgain());
        }
    }

    public void createInterface() {
        toolbar = Toolbar.createHorizontalToolbar();
        toolbar.addElement(sd = (JToggleButton)createNewButton(IconManager.shared().getIconSyntaxDiagram(), "Toggle Syntax diagram", true));
        toolbar.addElement(coloring = (JToggleButton)createNewButton(IconManager.shared().getIconColoring(), "Toggle Syntax coloring", true));
        toolbar.addElement(ideas = (JToggleButton)createNewButton(IconManager.shared().getIconIdea(), "Toggle Syntax ideas", true));
        toolbar.addGroupSeparator();
        toolbar.addElement(sort = (JToggleButton)createNewButton(IconManager.shared().getIconSort(), "Toggle Sort rules", true));
        toolbar.addElement(find = (JButton)createNewButton(IconManager.shared().getIconFind(), "Find text", false));
        toolbar.addGroupSeparator();
        toolbar.addElement(backward = (JButton)createNewButton(IconManager.shared().getIconBackward(), "Back", false));
        toolbar.addElement(forward = (JButton)createNewButton(IconManager.shared().getIconForward(), "Forward", false));
        toolbar.addGroupSeparator();
        toolbar.addElement(debug = (JButton)createNewButton(IconManager.shared().getIconDebug(), "Debug", false));
        toolbar.addElement(debugAgain = (JButton)createNewButton(IconManager.shared().getIconDebugAgain(), "Debug Again", false));

        AWPrefs.getPreferences().bindToPreferences(sort, AWPrefs.PREF_TOOLBAR_SORT, false);
    }

    public void awake() {
        editor.rules.setSorted(AWPrefs.getPreferences().getBoolean(AWPrefs.PREF_TOOLBAR_SORT, false));
        sd.setSelected(true);
        coloring.setSelected(true);
        ideas.setSelected(true);
    }

    public void addActions() {
        backward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editor.menuGoTo.goToBackward();
            }
        });

        forward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editor.menuGoTo.goToForward();
            }
        });

        sort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editor.toggleRulesSorting();
            }
        });

        sd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editor.toggleSyntaxDiagram();
            }
        });

        coloring.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editor.toggleSyntaxColoring();
            }
        });

        ideas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editor.toggleIdeas();
            }
        });

        find.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editor.menuFind.find();
            }
        });

        debug.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editor.menuDebugger.debug();
            }
        });

        debugAgain.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editor.menuDebugger.debugAgain();
            }
        });

    }

    public AbstractButton createNewButton(ImageIcon icon, String tooltip, boolean toggle) {
        AbstractButton button;
        if(toggle)
            button = new JToggleButton(icon);
        else
            button = new JButton(icon);
        button.setToolTipText(tooltip);
        Dimension d = new Dimension(32, 32);
        button.setMinimumSize(d);
        button.setMaximumSize(d);
        button.setPreferredSize(d);
        button.setFocusable(false);
        return button;
    }
}
