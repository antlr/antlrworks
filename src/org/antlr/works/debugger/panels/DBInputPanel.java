package org.antlr.works.debugger.panels;

import org.antlr.runtime.Token;
import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.debugger.input.DBInputProcessor;
import org.antlr.works.debugger.input.DBInputTextTokenInfo;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.works.utils.DetachablePanel;

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

public class DBInputPanel extends DetachablePanel {

    protected DebuggerTab debuggerTab;

    protected DBInputTokenPanel inputTokenPanel;
    protected DBInputTreePanel inputTreePanel;

    protected DBInputConcretePanel currentPanel;

    public DBInputPanel(DebuggerTab debuggerTab) {
        super("Input", debuggerTab);

        this.debuggerTab = debuggerTab;

        inputTokenPanel = new DBInputTokenPanel(debuggerTab);
        inputTreePanel = new DBInputTreePanel(debuggerTab);

        /** Set the input token panel by default */
        setCurrentPanel(inputTokenPanel);
    }

    public void setCurrentPanel(DBInputConcretePanel panel) {
        this.currentPanel = panel;
        if(mainPanel.getComponentCount() > 0)
            mainPanel.remove(0);

        mainPanel.add(currentPanel.getComponent(), BorderLayout.CENTER);
    }

    public void prepareForGrammar(GrammarEngine engineGrammar) {
        if(engineGrammar.isTreeParserGrammar())
            setCurrentPanel(inputTreePanel);
        else
            setCurrentPanel(inputTokenPanel);
    }

    public void close() {
        super.close();

        inputTokenPanel.close();
        inputTreePanel.close();

        debuggerTab = null;
        currentPanel.close();
    }

    public DBInputProcessor getInputBuffer() {
        return currentPanel.getInputProcessor();
    }

    public void toggleInputTokensBox() {
        currentPanel.toggleInputTextTokensBox();
    }

    public boolean isInputTokensBoxVisible() {
        return currentPanel.isInputTokensBoxVisible();
    }

    public void updateOnBreakEvent() {
        currentPanel.updateOnBreakEvent();
    }

    public boolean isBreakpointAtToken(Token token) {
        return currentPanel.isBreakpointAtToken(token);
    }

    public void stop() {
        currentPanel.stop();
    }

    public void selectToken(Token token) {
        currentPanel.selectToken(token);
    }

    public DBInputTextTokenInfo getTokenInfoForToken(Token token) {
        return currentPanel.getTokenInfoForToken(token);
    }

}
