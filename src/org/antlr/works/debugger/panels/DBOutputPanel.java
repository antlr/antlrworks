package org.antlr.works.debugger.panels;

import org.antlr.works.debugger.Debugger;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.StreamWatcherDelegate;
import org.antlr.works.utils.TextPane;
import org.antlr.works.utils.TextUtils;

import javax.swing.*;
import java.awt.*;
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

public class DBOutputPanel extends JPanel implements StreamWatcherDelegate {

    protected TextPane outputTextPane;
    protected Debugger debugger;

    public DBOutputPanel(Debugger debugger) {
        super(new BorderLayout());

        this.debugger = debugger;

        outputTextPane = new TextPane();
        outputTextPane.setBackground(Color.white);
        outputTextPane.setBorder(null);
        outputTextPane.setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        outputTextPane.setText("");
        outputTextPane.setEditable(false);

        TextUtils.createTabs(outputTextPane);

        JScrollPane textScrollPane = new JScrollPane(outputTextPane);
        textScrollPane.setWheelScrollingEnabled(true);

        add(textScrollPane, BorderLayout.CENTER);
    }

    public void streamWatcherDidStarted() {
        outputTextPane.setText("");
    }

    public void streamWatcherDidReceiveString(String string) {
        outputTextPane.setText(outputTextPane.getText()+string);
    }

    public void streamWatcherException(Exception e) {
        debugger.getConsole().print(e);
    }

}
