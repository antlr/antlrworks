package org.antlr.works.editor.actions;

import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.tool.TUsage;
import org.antlr.works.parser.Parser;
import org.antlr.works.parser.Token;
import org.antlr.works.stats.Statistics;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/*

[The "BSD licence"]
Copyright (c) 2004-05 Jean Bovet
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

public class MenuGrammar extends AbstractActions {

    public MenuGrammar(EditorWindow editor) {
        super(editor);
    }

    public void findUsage() {
        Token token = getTokenAtPosition(getCaretPosition());
        if(token == null)
            return;

        String tokenAttribute = token.getAttribute();

        TUsage usage = new TUsage(editor);
        editor.getTabbedPane().add("Usages of \""+tokenAttribute+"\"", usage.getContainer());
        editor.getTabbedPane().setSelectedIndex(editor.getTabbedPane().getTabCount()-1);

        Iterator iterator = editor.getTokens().iterator();
        while(iterator.hasNext()) {
            Token candidate = (Token)iterator.next();
            if(candidate.getAttribute().equals(tokenAttribute)) {
                Parser.Rule matchedRule = editor.rules.getRuleAtPosition(candidate.start);
                if(matchedRule != null)
                    usage.addMatch(matchedRule, candidate);
            }
        }

        Statistics.shared().recordEvent(Statistics.EVENT_FIND_USAGES);
    }

    public void goToDeclaration() {
        Token token = getTokenAtPosition(getCaretPosition());
        if(token == null)
            return;

        Parser.Rule rule = editor.rules.selectRuleName(token.getAttribute());
        if(rule == null)
            return;

        editor.rules.selectTextRule(rule);
        Statistics.shared().recordEvent(Statistics.EVENT_GOTO_DECLARATION);
    }

    public void rename() {
        Token token = getTokenAtPosition(getCaretPosition());
        if(token == null)
            return;

        String s = (String)JOptionPane.showInputDialog(editor.getJavaContainer(), "Rename '"+token.getAttribute()+"' and its usages to:", "Rename",
                JOptionPane.QUESTION_MESSAGE, null, null, token.getAttribute());
        if(s != null && !s.equals(token.getAttribute())) {
            editor.disableTextPane(false);
            editor.beginTextPaneUndoGroup("Rename");
            renameToken(token, s);
            editor.endTextPaneUndoGroup();
            editor.enableTextPane(false);
            editor.colorize.reset();
            editor.rules.parseRules();
            editor.changeDone();
            Statistics.shared().recordEvent(Statistics.EVENT_RENAME);
        }
    }

    public void renameToken(Token t, String name) {
        List tokens = editor.getTokens();
        String attr = t.getAttribute();
        for(int index = tokens.size()-1; index>0; index--) {
            Token token = (Token) tokens.get(index);
            if(token.type == t.type && token.getAttribute().equals(attr)) {
                editor.editorGUI.replaceText(token.start, token.end, name);
            }
        }
    }

    public int getLineNumberForPosition(int pos) {
        List lines = editor.getLines();
        if(lines == null)
            return -1;

        for(int i=0; i<lines.size(); i++) {
            Integer line = (Integer)lines.get(i);
            if(line.intValue() > pos) {
                return i-1;
            }
        }
        return lines.size()-1;
    }

    public Point getLinePositions(int line) {
        List lines = editor.getLines();
        if(line == -1 || lines == null)
            return null;

        Integer start = (Integer)lines.get(line);
        Integer end = null;
        if(line+1 >= lines.size()) {
            return new Point(start.intValue(), getTextPane().getDocument().getLength()-1);
        } else {
            end = (Integer)lines.get(line+1);
            return new Point(start.intValue(), end.intValue()-1);
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

    public void checkGrammar() {
        editor.visual.checkGrammar();
        Statistics.shared().recordEvent(Statistics.EVENT_CHECK_GRAMMAR);                    
    }

    public void moveCursorToLine(int line) {
        if(line < 0 || line > editor.getLines().size()-1)
            return;

        Integer index = (Integer)editor.getLines().get(line);
        setCaretPosition(index.intValue());
    }

    public Token getTokenAtPosition(int pos) {
        Iterator iterator = editor.getTokens().iterator();
        while(iterator.hasNext()) {
            Token token = (Token)iterator.next();
            if(pos >= token.start && pos <= token.end)
                return token;
        }
        return null;
    }

}
