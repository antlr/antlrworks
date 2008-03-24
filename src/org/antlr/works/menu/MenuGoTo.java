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

import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.container.ComponentContainerGrammar;
import org.antlr.works.grammar.element.ElementImport;
import org.antlr.works.grammar.element.ElementReference;
import org.antlr.works.grammar.syntax.GrammarSyntax;
import org.antlr.works.stats.StatisticsAW;

import javax.swing.*;
import java.util.List;
import java.util.Set;

public class MenuGoTo extends MenuAbstract {

    public MenuGoTo(ComponentContainerGrammar editor) {
        super(editor);
    }

    public void goToRule() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_GOTO_RULE);
        getSelectedEditor().goToRule.display();
    }

    public void goToDeclaration() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_GOTO_DECLARATION);

        final ElementReference ref = getSelectedEditor().getCurrentReference();
        if(ref != null) {
            GrammarSyntax syntax = getSelectedEditor().getSyntaxEngine().getSyntax();
            List<String> grammars = syntax.getGrammarsDeclaringRule(ref.token.getAttribute());
            if(!grammars.isEmpty()) {
                getSelectedEditor().getContainer().selectGrammar(grammars.get(0));

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        GrammarSyntax syntax = getSelectedEditor().getSyntaxEngine().getSyntax();
                        getSelectedEditor().goToHistoryRememberCurrentPosition();
                        for(ATEToken decl : syntax.getDecls()) {
                            if(decl.getAttribute().equals(ref.token.getAttribute())) {
                                setCaretPosition(decl.start);
                                break;
                            }
                        }
                    }
                });

            }
            return;
        }

        ElementImport imp = getSelectedEditor().getImportAtPosition(getSelectedEditor().getCaretPosition());
        if(imp != null) {
            getSelectedEditor().getContainer().selectGrammar(imp.getName());
        }
    }

    public void goToBreakpoint(int direction) {
        if(direction == -1)
            StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_GOTO_PREV_BRKPT);
        else
            StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_GOTO_NEXT_BRKPT);

        Set<Integer> breakpoints = getSelectedEditor().breakpointManager.getBreakpoints();
        int line = getSelectedEditor().getTextEditor().getLineIndexAtTextPosition(getCaretPosition());
        if(line == -1) return;

        while(true) {
            line += direction;
            if(line < 0 || line > getSelectedEditor().syntaxEngine.getMaxLines()-1)
                break;

            if(breakpoints.contains(Integer.valueOf(line))) {
                moveCursorToLine(line);
                break;
            }
        }
    }

    public void goToLine() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_GOTO_LINE);
        String s = (String)JOptionPane.showInputDialog(getSelectedEditor().getJavaContainer(), "Line number:", "Go To Line",
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if(s != null) {
            moveCursorToLine(Integer.parseInt(s)-1);
        }
    }

    public void goToCharacter() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_GOTO_CHAR);
        String s = (String)JOptionPane.showInputDialog(getSelectedEditor().getJavaContainer(), "Character number:", "Go To Character",
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if(s != null) {
            int character = Integer.parseInt(s);
            if(character < 0 || character > getTextPane().getDocument().getLength()-1)
                return;

            getSelectedEditor().goToHistoryRememberCurrentPosition();

            setCaretPosition(character);
        }
    }

    public void goToBackward() {
        getSelectedEditor().goToBackward();
    }

    public void goToForward() {
        getSelectedEditor().goToForward();
    }

    public void moveCursorToLine(int lineIndex) {
        if(lineIndex < 0 || lineIndex > getSelectedEditor().getLines().size()-1)
            return;

        ATELine line = getSelectedEditor().getLines().get(lineIndex);
        getSelectedEditor().goToHistoryRememberCurrentPosition();
        setCaretPosition(line.position);
    }

}
