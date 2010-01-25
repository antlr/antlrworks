package org.antlr.works.stringtemplate;

import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.IconManager;
import org.antlr.works.utils.Toolbar;
import org.antlr.xjlib.appkit.swing.XJRollOverButton;
import org.antlr.xjlib.appkit.swing.XJRollOverButtonToggle;

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

public class STWindowToolbar {

    public Toolbar toolbar;

    public JButton backward;
    public JButton forward;

    public JToggleButton sort;

    public JButton find;

    public STWindow window;

    public STWindowToolbar(STWindow window) {
        this.window = window;

        createInterface();
        addActions();
    }

    public void close() {
        window = null;
        AWPrefs.getPreferences().unbindFromPreferences(sort, AWPrefs.PREF_TOOLBAR_ST_SORT);
    }

    public JComponent getToolbar() {
        return toolbar;
    }

    public void createInterface() {
        toolbar = Toolbar.createHorizontalToolbar();
        toolbar.addElement(sort = createToggleButton(IconManager.shared().getIconSort(), "Toggle Sort rules"));
        toolbar.addElement(find = createButton(IconManager.shared().getIconFind(), "Find text"));
        toolbar.addGroupSeparator();
        toolbar.addElement(backward = createButton(IconManager.shared().getIconBackward(), "Back"));
        toolbar.addElement(forward = createButton(IconManager.shared().getIconForward(), "Forward"));

        AWPrefs.getPreferences().bindToPreferences(sort, AWPrefs.PREF_TOOLBAR_ST_SORT, false);
    }

    public void updateStates() {
        sort.setSelected(window.isRulesSorted());
    }

    public STWindow getSelectedEditor() {
        return window;
    }

    public void addActions() {
        backward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getSelectedEditor().goToBackward();
            }
        });

        forward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getSelectedEditor().goToForward();
            }
        });

        sort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getSelectedEditor().toggleRulesSorting();
            }
        });

        find.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getSelectedEditor().find();
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