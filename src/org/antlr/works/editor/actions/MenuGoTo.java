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
import org.antlr.works.parser.Parser;
import org.antlr.works.parser.Token;
import org.antlr.works.stats.Statistics;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

public class MenuGoTo extends AbstractActions {

    public MenuGoTo(EditorWindow editor) {
        super(editor);
    }

    public void goToDeclaration() {
        Token token = editor.getTokenAtPosition(getCaretPosition());
        if(token == null)
            return;

        Parser.Rule rule = editor.rules.selectRuleName(token.getAttribute());
        if(rule == null)
            return;

        editor.rules.selectTextRule(rule);
        Statistics.shared().recordEvent(Statistics.EVENT_GOTO_DECLARATION);
    }

    public int getLineNumberForPosition(int pos) {
        List lines = editor.getLines();
        if(lines == null)
            return -1;

        for(int i=0; i<lines.size(); i++) {
            Line line = (Line)lines.get(i);
            if(line.position > pos) {
                return i-1;
            }
        }
        return lines.size()-1;
    }

    public Point getLinePositions(int lineIndex) {
        List lines = editor.getLines();
        if(lineIndex == -1 || lines == null)
            return null;

        Line startLine = (Line)lines.get(lineIndex);
        int start = startLine.position;
        int end = 0;
        if(lineIndex+1 >= lines.size()) {
            return new Point(start, getTextPane().getDocument().getLength()-1);
        } else {
            Line endLine = (Line)lines.get(lineIndex+1);
            end = endLine.position;
            return new Point(start, end-1);
        }
    }

    public void goToBreakpoint(int direction) {
        Set breakpoints = editor.getGutter().getBreakpoints();
        int line = getLineNumberForPosition(getCaretPosition());
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

            setCaretPosition(character);
            Statistics.shared().recordEvent(Statistics.EVENT_GOTO_CHAR);
        }
    }

    public void moveCursorToLine(int lineIndex) {
        if(lineIndex < 0 || lineIndex > editor.getLines().size()-1)
            return;

        Line line = (Line)editor.getLines().get(lineIndex);
        setCaretPosition(line.position);
    }

}
