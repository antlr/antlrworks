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

import org.antlr.works.components.container.ComponentContainer;
import org.antlr.works.components.editor.ComponentEditorGrammar;
import org.antlr.works.generate.CodeDisplay;
import org.antlr.works.generate.CodeGenerate;
import org.antlr.works.generate.CodeGenerateDelegate;
import org.antlr.works.grammar.CheckGrammar;
import org.antlr.works.grammar.CheckGrammarDelegate;
import org.antlr.works.grammar.antlr.GrammarResult;
import org.antlr.works.grammar.element.ElementGrammarName;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.foundation.XJUtils;

import java.io.File;

public class ActionGenerate extends ActionAbstract implements CodeGenerateDelegate, CheckGrammarDelegate {

    private String actionShowCodeRule;
    private int actionShowCodeType;
    private boolean actionShowCodeAfterGeneration = false;

    private CodeGenerate codeGenerate;
    private ComponentEditorGrammar rootGrammar;

    private boolean generating = false;

    public ActionGenerate(ComponentContainer editor) {
        super(editor);
    }

    public void awake() {
        rootGrammar = getSelectedEditor();
        codeGenerate = new CodeGenerate(rootGrammar, this);
    }

    @Override
    public void close() {
        super.close();
        codeGenerate.close();
    }

    public void generateCode() {
        actionShowCodeRule = null;
        generateCodeProcess();
    }

    public void showGeneratedCode(int type) {
        StatisticsAW.shared().recordEvent(type==ElementGrammarName.LEXER?StatisticsAW.EVENT_SHOW_LEXER_GENERATED_CODE:StatisticsAW.EVENT_SHOW_PARSER_GENERATED_CODE);

        checkAndShowGeneratedCode(null, type);
    }

    public void showRuleGeneratedCode() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_RULE_GENERATED_CODE);

        if(getSelectedEditor().getCurrentRule() == null) {
            XJAlert.display(getSelectedEditor().getWindowContainer(), "Error", "A rule must be selected first.");
        } else {
            ElementRule r = getSelectedEditor().getCurrentRule();
            checkAndShowGeneratedCode(r.name, r.lexer?ElementGrammarName.LEXER:ElementGrammarName.PARSER);
        }
    }

    private synchronized void generateCodeProcess() {
        if(generating) return;
        generating = true;
        
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_GENERATE_CODE);

        if(!getSelectedEditor().ensureDocumentSaved())
            return;

        CheckGrammar checkGrammar = new CheckGrammar(getSelectedEditor(), this);
        checkGrammar.check();
    }

    private void generateCodeProcessContinued() {
        if(!getSelectedEditor().getDocument().autoSave()) {
            generating = false;
            return;
        }

        codeGenerate.setDebug(false);
        codeGenerate.generateInThread(getSelectedEditor().getJavaContainer());
    }

    private void checkAndShowGeneratedCode(String rule, int type) {
        if(!isKnownLanguage()) {
            XJAlert.display(getSelectedEditor().getWindowContainer(), "Error", "Can only show generated grammar for Java language");
            return;
        }

        if(!codeGenerate.isGeneratedTextFileExisting(type)
                || codeGenerate.isFileModifiedSinceLastGeneration()
                || getSelectedEditor().getDocument().isDirty()) {
            // Generate automatically the code and call again
            // this method (using actionShowCodeRule as flag)
            actionShowCodeRule = rule;
            actionShowCodeType = type;
            actionShowCodeAfterGeneration = true;
            generateCodeProcess();
            return;
        }

        showGeneratedCode(rule, type);
    }

    private boolean isKnownLanguage() {
        String language = codeGenerate.getGrammarLanguage();
        return language != null && language.equals("Java");
    }

    private void showGeneratedCode(String rule, int type) {
        ComponentEditorGrammar editor = getSelectedEditor();

        String grammarName;
        try {
            grammarName = editor.getGrammarEngine().getGeneratedClassName(type);
        } catch (Exception e) {
            XJAlert.display(editor.getWindowContainer(), "Error", "Unable to get the generated class name:\n"+e.toString());
            return;
        }
        if(editor != rootGrammar) {
            // if the current grammar is not a root grammar, concat its name with the root grammar
            grammarName = rootGrammar.getGrammarEngine().getGrammarName()+"_"+grammarName;
        }

        String grammarFileName = grammarName+".java";
        String grammarFile = XJUtils.concatPath(codeGenerate.getOutputPath(), grammarFileName);
        if(!new File(grammarFile).exists()) {
            XJAlert.display(getSelectedEditor().getWindowContainer(), "Error",
                    "The generated code does not exist. It is probably not supported by the grammar.");
            return;
        }

        CodeDisplay cd = new CodeDisplay(editor.getXJFrame());
        cd.setFile(grammarFile);
        cd.setRule(rule);
        cd.load();
        
        editor.addTab(cd);
    }

    public boolean codeGenerateDisplaySuccess() {
        return !actionShowCodeAfterGeneration;
    }

    public void codeGenerateDidComplete() {
        if(actionShowCodeAfterGeneration) {
            actionShowCodeAfterGeneration = false;
            showGeneratedCode(actionShowCodeRule, actionShowCodeType);
        }
        generating = false;
    }

    public void checkGrammarDidBegin(CheckGrammar source) {
        // do nothing
    }

    public void checkGrammarDidEnd(CheckGrammar source, GrammarResult result) {
        if(result.getErrorCount() == 0) {
            generateCodeProcessContinued();
        } else {
            generating = false;
            XJAlert.display(getSelectedEditor().getWindowContainer(), "Error", "Check Grammar reported some errors:\n"+result.getFirstErrorMessage()+"\nConsult the console for more information.");
        }
        source.close();
    }
}
