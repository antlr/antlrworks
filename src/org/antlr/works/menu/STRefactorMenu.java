package org.antlr.works.menu;

import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.RefactorMutator;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.stringtemplate.STWindow;
import org.antlr.works.stringtemplate.element.ElementTemplateRule;
import org.antlr.works.stringtemplate.syntax.ATEStringTemplateSyntaxLexer;
import org.antlr.xjlib.appkit.undo.XJUndo;

import javax.swing.*;
import javax.swing.undo.AbstractUndoableEdit;
import java.util.List;

/*
[The "BSD licence"]
Copyright (c) 2009
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

public class STRefactorMenu implements ActionRefactor {
    
    private final STWindow window;
    private EditorTextMutator mutator;

    public STRefactorMenu(STWindow window) {
        this.window = window;
    }

    public void rename() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_RENAME);

        ATEToken token = window.getCurrentToken();
        if(token == null)
            return;

        String s = (String) JOptionPane.showInputDialog(window.getJavaContainer(), "Rename '"+token.getAttribute()+"' and its usages to:", "Rename",
                JOptionPane.QUESTION_MESSAGE, null, null, token.getAttribute());
        if(s != null && !s.equals(token.getAttribute())) {
            beginRefactor("Rename");
            renameToken(token, s);
            endRefactor();
        }
    }

    public boolean renameToken(ATEToken t, String name) {
        String attr = t.getAttribute();
        List<ATEToken> tokens = window.getTokens();

        boolean isArg = t.type == ATEStringTemplateSyntaxLexer.TOKEN_ARG_DECL ||
                t.type == ATEStringTemplateSyntaxLexer.TOKEN_ARG_REFERENCE;
        boolean renameRefRule = t.type == ATEStringTemplateSyntaxLexer.TOKEN_REFERENCE ||
                t.type == ATEStringTemplateSyntaxLexer.TOKEN_DECL;

        if (renameRefRule) {
            for(int index = tokens.size()-1; index>0; index--) {
                ATEToken token = tokens.get(index);
                if(!token.getAttribute().equals(attr)) continue;

                if(token.type == ATEStringTemplateSyntaxLexer.TOKEN_REFERENCE ||
                        token.type == ATEStringTemplateSyntaxLexer.TOKEN_DECL) {
                    mutator.replace(token.getStartIndex(), token.getEndIndex(), name);
                }
            }
        } else if (isArg) {
            ElementTemplateRule rule = getRuleAtPosition(window.getCaretPosition());
            for(int index = tokens.size()-1; index>0; index--) {
                ATEToken token = tokens.get(index);
                if(!token.getAttribute().equals(attr)) continue;

                if (rule.containsIndex(token.start)) {
                    if (token.type == ATEStringTemplateSyntaxLexer.TOKEN_ARG_DECL ||
                            token.type == ATEStringTemplateSyntaxLexer.TOKEN_ARG_REFERENCE) {
                        mutator.replace(token.getStartIndex(), token.getEndIndex(), name);
                    }
                }
            }
        } else {
            mutator.replace(t.getStartIndex(), t.getEndIndex(), name);
        }

        return true;
    }

    protected ElementTemplateRule getRuleAtPosition(int pos) {
        List<ElementTemplateRule> rules = window.getRules();
        if(rules == null)
            return null;

        for (ElementTemplateRule rule : rules) {
            if (rule.containsIndex(pos))
                return rule;
        }
        return null;
    }

    protected void beginRefactor(String name) {
        window.beginGroupChange(name);
        mutator = new EditorTextMutator();
    }

    protected void endRefactor() {
        mutator.apply();
        mutator = null;
        window.endGroupChange();
    }

    public boolean canReplaceLiteralWithTokenLabel(){return false;}
    public void replaceLiteralWithTokenLabel(){}
    public void replaceLiteralTokenWithTokenLabel(ATEToken t, String name){}

    public void convertLiteralsToSingleQuote(){}
    public void convertLiteralsToDoubleQuote(){}
    public void convertLiteralsToCStyleQuote(){}

    public void removeLeftRecursion(){}
    public void removeAllLeftRecursion(){}

    public boolean canExtractRule(){return false;}
    public void extractRule(){}

    public boolean canInlineRule(){return false;}
    public void inlineRule(){}

    public void createRuleAtIndex(boolean lexer, String name, String content){}
    public void deleteRuleAtIndex(int index){}
    public int insertionIndexForRule(boolean lexer){return -1;}
    public String createRule(String name, String content){return null;}

    protected void refactorReplaceEditorText(String text) {
        int oldCaretPosition = window.getCaretPosition();
        window.disableTextPaneUndo();
        window.setText(text);
        window.enableTextPaneUndo();
        window.getTextEditor().setCaretPosition(Math.min(oldCaretPosition, text.length()), false, false);
    }

    public class EditorTextMutator implements RefactorMutator {

        public StringBuilder mutableText;

        public EditorTextMutator() {
            mutableText = new StringBuilder(window.getText());
        }

        public void replace(int start, int end, String s) {
            mutableText.replace(start, end, s);
        }

        public void insert(int index, String s) {
            mutableText.insert(index, s);
        }

        public void insertAtLinesBoundary(int index, String s) {
            if(!(mutableText.charAt(index) == '\n' && mutableText.charAt(index-1) == '\n')) {
                mutableText.insert(index++, '\n');
            }
            mutableText.insert(index, s);
            int end = index+s.length();
            if(!(mutableText.charAt(end) == '\n' && end+1 < mutableText.length() && mutableText.charAt(end+1) == '\n'))
            {
                mutableText.insert(end, '\n');
            }
        }

        public void delete(int start, int end) {
            mutableText.delete(start, end);
        }

        public void apply() {
            String text = mutableText.toString();
            String oldContent = window.getText();

            refactorReplaceEditorText(text);

            XJUndo undo = window.getUndo(window.getTextPane());
            undo.addEditEvent(new UndoableRefactoringEdit(oldContent, text));
        }
    }

    protected class UndoableRefactoringEdit extends AbstractUndoableEdit {

        public String oldContent;
        public String newContent;

        public UndoableRefactoringEdit(String oldContent, String newContent) {
            this.oldContent = oldContent;
            this.newContent = newContent;
        }

        public void redo() {
            super.redo();
            refactorReplaceEditorText(newContent);
        }

        public void undo() {
            super.undo();
            refactorReplaceEditorText(oldContent);
        }
    }
}
