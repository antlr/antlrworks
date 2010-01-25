package org.antlr.works.debugger.panels;

import org.antlr.runtime.Token;
import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.debugger.input.DBInputProcessor;
import org.antlr.works.debugger.input.DBInputProcessorToken;
import org.antlr.works.debugger.input.DBInputTextTokenInfo;
import org.antlr.works.prefs.AWPrefs;
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

public class DBInputTokenPanel implements DBInputConcretePanel {

    protected TextPane inputTextPane;
    protected JScrollPane textScrollPane;
    protected DBInputProcessorToken processorToken;

    public DBInputTokenPanel(DebuggerTab debuggerTab) {
        inputTextPane = new TextPane();
        inputTextPane.setBackground(Color.white);
        inputTextPane.setBorder(null);
        inputTextPane.setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        inputTextPane.setText("");
        inputTextPane.setEditable(false);

        TextUtils.createTabs(inputTextPane);

        processorToken = new DBInputProcessorToken(debuggerTab, inputTextPane);

        textScrollPane = new JScrollPane(inputTextPane);
        textScrollPane.setWheelScrollingEnabled(true);
    }

    public void close() {
        processorToken.close();
    }

    public void stop() {
        processorToken.stop();
    }

    public JComponent getComponent() {
        return textScrollPane;
    }

    public DBInputProcessor getInputProcessor() {
        return processorToken;
    }

    public void toggleInputTextTokensBox() {
        processorToken.setDrawTokensBox(!processorToken.isTokensBoxVisible());
    }

    public boolean isInputTokensBoxVisible() {
        return processorToken.isTokensBoxVisible();
    }

    public boolean isBreakpointAtToken(Token token) {
        return processorToken.isBreakpointAtToken(token);
    }

    public void selectToken(Token token) {
        processorToken.selectToken(token);
    }

    public DBInputTextTokenInfo getTokenInfoForToken(Token token) {
        return processorToken.getTokenInfoForToken(token);
    }

    public void updateOnBreakEvent() {
        processorToken.updateOnBreakEvent();
    }

}
