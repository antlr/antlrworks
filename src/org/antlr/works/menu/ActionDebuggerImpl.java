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

package org.antlr.works.menu;

import org.antlr.works.components.container.DocumentContainer;
import org.antlr.works.components.editor.GrammarEditor;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.stats.StatisticsAW;

public class ActionDebuggerImpl extends ActionAbstract implements ActionDebugger {

    public ActionDebuggerImpl(DocumentContainer editor) {
        super(editor);
    }

    public GrammarEditor getSelectedEditor() {
        return (GrammarEditor)super.getSelectedEditor();
    }

    public void runInterpreter() {
        try {
            StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_INTERPRETER_MENU);
            getSelectedEditor().selectInterpreterTab();
            getSelectedEditor().interpreter.interpret();
        } catch (Exception e) {
            getSelectedEditor().console.println(e);
        }
    }

    public void run() {
        getContainer().getDebugger().launchLocalDebugger(Debugger.OPTION_RUN);
    }

    public void debug() {
        getContainer().getDebugger().launchLocalDebugger(Debugger.OPTION_NONE);
    }

    public void debugAgain() {
        getContainer().getDebugger().launchLocalDebugger(Debugger.OPTION_AGAIN);
    }

    public void debugRemote() {
        getContainer().getDebugger().launchRemoteDebugger();
    }

    public void toggleInputTokens() {
        getContainer().getDebugger().toggleInputTokensBox();
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_DEBUGGER_TOGGLE_INPUT_TOKENS);
    }

    public void showEditTestRig() {
        getContainer().getDebugger().showEditTestRig();
    }

    public boolean isInputTokenVisible() {
        return getContainer().getDebugger().isInputTokenVisible();
    }

    public boolean isRunning() {
        return getContainer().getDebugger().isRunning();
    }

    public boolean canDebugAgain() {
        return getContainer().getDebugger().canDebugAgain();
    }

    public String getEventsAsString() {
        return getContainer().getDebugger().getEventsAsString();
    }
}
