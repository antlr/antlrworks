package org.antlr.works.editor.actions;

import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.appkit.utils.XJAlert;
import org.antlr.works.dialog.DialogGenerate;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.code.CodeDisplay;
import org.antlr.works.editor.code.CodeGenerate;

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

public class MenuGenerate extends AbstractActions {

    public CodeGenerate generateCode = null;

    public MenuGenerate(EditorWindow editor) {
        super(editor);
        generateCode = new CodeGenerate(editor);
    }

    public void generateCode() {
        if(!checkLanguage())
            return;

        DialogGenerate dialog = new DialogGenerate();
        if(dialog.runModal() == XJDialog.BUTTON_OK) {
            generateCode.setOutputPath(dialog.getOutputPath());
            generateCode.generateInThread(editor, true);
        }
    }

    public boolean checkLanguage() {
        if(!isKnownLanguage()) {
            XJAlert.display(editor.getWindowComponent(), "Error", "Can only generate grammar for Java language");
            return false;
        } else
            return true;
    }

    public boolean isKnownLanguage() {
        String language = generateCode.getGrammarLanguage();
        return language != null && language.equals("Java");
    }

    public void showGeneratedCode(boolean lexer) {
        showGeneratedCode(null, lexer);
    }

    public void showRuleGeneratedCode() {
        if(editor.getCurrentRule() == null)
            XJAlert.display(editor.getWindowComponent(), "Error", "A rule must be selected first.");
        else
            showGeneratedCode(editor.getCurrentRule().name, false);
    }

    public void showGeneratedCode(String rule, boolean lexer) {
        if(!checkLanguage())
            return;

        if(!generateCode.isGeneratedTextFileExisting(lexer)) {
            XJAlert.display(editor.getWindowComponent(), "Error", "No generated files found. Please generate the file before perform this action.");
            return;
        }

        CodeDisplay dc = new CodeDisplay();
        String name = generateCode.getGrammarName();
        if(lexer)
            name += "Lexer";

        String title = name+".java";
        String text = generateCode.getGeneratedText(lexer);
        if(rule != null) {
            int startIndex = text.indexOf("$ANTLR start "+rule);
            startIndex = text.indexOf("\n", startIndex)+1;
            int stopIndex = text.indexOf("$ANTLR end "+rule);
            while(stopIndex>0 && text.charAt(stopIndex) != '\n')
                stopIndex--;

            if(startIndex >= 0 && stopIndex >= 0) {
                text = text.substring(startIndex, stopIndex);
                title = rule;
            } else {
                XJAlert.display(editor.getWindowComponent(), "Error", "Cannot find markers for rule \""+rule+"\"");
                return;
            }
        }
        dc.setText(text);

        editor.getTabbedPane().add(title, dc.getContainer());
        editor.getTabbedPane().setSelectedIndex(editor.getTabbedPane().getTabCount()-1);
    }

}
