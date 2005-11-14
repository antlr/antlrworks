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

package org.antlr.works.editor.actions;

import org.antlr.works.editor.EditorWindow;
import org.antlr.works.parser.Line;
import org.antlr.works.parser.ParserReference;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.stats.Statistics;

import javax.swing.*;
import java.util.Set;

public class ActionsGoTo extends AbstractActions {

    public ActionsGoTo(EditorWindow editor) {
        super(editor);
    }

    public void goToRule() {
        editor.goToRule.display();
    }

    public void goToDeclaration() {
        ParserReference ref = editor.getCurrentReference();
        if(ref == null)
            return;

        ParserRule rule = editor.rules.selectRuleNameInTree(ref.token.getAttribute());
        if(rule == null)
            return;

        editor.goToHistoryRememberCurrentPosition();
        editor.rules.goToRule(rule);

        Statistics.shared().recordEvent(Statistics.EVENT_GOTO_DECLARATION);
    }

    public void goToBreakpoint(int direction) {
        Set breakpoints = editor.breakpointManager.getBreakpoints();
        int line = editor.getLineIndexAtTextPosition(getCaretPosition());
        if(line == -1)
            return;

        while(true) {
            line += direction;
            if(line < 0 || line > editor.parser.getMaxLines()-1)
                break;

            if(breakpoints.contains(new Integer(line))) {
                moveCursorToLine(line);
                break;
            }
        }
    }

    public void goToLine() {
        String s = (String)JOptionPane.showInputDialog(editor.getJavaContainer(), "Line number:", "Go To Line",
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if(s != null) {
            moveCursorToLine(Integer.parseInt(s)-1);
            Statistics.shared().recordEvent(Statistics.EVENT_GOTO_LINE);
        }
    }

    public void goToCharacter() {
        String s = (String)JOptionPane.showInputDialog(editor.getJavaContainer(), "Character number:", "Go To Character",
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if(s != null) {
            int character = Integer.parseInt(s)-1;
            if(character < 0 || character > getTextPane().getDocument().getLength()-1)
                return;

            editor.goToHistoryRememberCurrentPosition();

            setCaretPosition(character);
            Statistics.shared().recordEvent(Statistics.EVENT_GOTO_CHAR);
        }
    }

    public void goToBackward() {
        if(editor.editorGoToHistory.canGoBackward()) {
            setCaretPosition(editor.editorGoToHistory.getBackwardPosition(getCaretPosition()));
            editor.getMainMenuBar().refreshState();
        }
    }

    public void goToForward() {
        if(editor.editorGoToHistory.canGoForward()) {
            setCaretPosition(editor.editorGoToHistory.getForwardPosition());
            editor.getMainMenuBar().refreshState();
        }
    }

    public void moveCursorToLine(int lineIndex) {
        if(lineIndex < 0 || lineIndex > editor.getLines().size()-1)
            return;

        Line line = (Line)editor.getLines().get(lineIndex);
        editor.goToHistoryRememberCurrentPosition();
        setCaretPosition(line.position);
    }

}
