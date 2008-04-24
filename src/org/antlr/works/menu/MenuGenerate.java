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

import org.antlr.works.components.container.ComponentContainerGrammar;
import org.antlr.works.generate.CodeDisplay;
import org.antlr.works.generate.CodeGenerate;
import org.antlr.works.generate.CodeGenerateDelegate;
import org.antlr.works.grammar.CheckGrammar;
import org.antlr.works.grammar.CheckGrammarDelegate;
import org.antlr.works.grammar.antlr.ANTLRGrammarResult;
import org.antlr.works.grammar.element.ElementGrammarName;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.xjlib.appkit.utils.XJAlert;

public class MenuGenerate extends MenuAbstract implements CodeGenerateDelegate, CheckGrammarDelegate {

    private CodeGenerate generateCode;
    private CheckGrammar checkGrammar;

    private String actionShowCodeRule;
    private int actionShowCodeType;
    private boolean actionShowCodeAfterGeneration = false;

    public MenuGenerate(ComponentContainerGrammar editor) {
        super(editor);
    }

    public void awake() {
        generateCode = new CodeGenerate(getSelectedEditor(), this);
        checkGrammar = new CheckGrammar(getSelectedEditor(), this);
    }

    @Override
    public void close() {
        super.close();
        generateCode.close();
        checkGrammar.close();
    }

    public void generateCode() {
        actionShowCodeRule = null;
        generateCodeProcess();
    }

    protected void generateCodeProcess() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_GENERATE_CODE);

        if(!getSelectedEditor().ensureDocumentSaved())
            return;

        checkGrammar.check();
    }

    protected void generateCodeProcessContinued() {
        if(!getSelectedEditor().getDocument().autoSave())
            return;

        generateCode.setDebug(false);
        generateCode.generateInThread(getSelectedEditor().getJavaContainer());
    }

    public boolean checkLanguage() {
        if(!isKnownLanguage()) {
            XJAlert.display(getSelectedEditor().getWindowContainer(), "Error", "Can only show generated grammar for Java language");
            return false;
        } else
            return true;
    }

    public boolean isKnownLanguage() {
        String language = generateCode.getGrammarLanguage();
        return language != null && language.equals("Java");
    }

    public void showGeneratedCode(int type) {
        StatisticsAW.shared().recordEvent(type==ElementGrammarName.LEXER?StatisticsAW.EVENT_SHOW_LEXER_GENERATED_CODE:StatisticsAW.EVENT_SHOW_PARSER_GENERATED_CODE);

        if(type == ElementGrammarName.LEXER) {
            if(!generateCode.supportsLexer()) {
                XJAlert.display(getSelectedEditor().getWindowContainer(), "Error", "Cannot generate the lexer because there is no lexer in this grammar.");
                return;
            }
        } else {
            if(!generateCode.supportsParser()) {
                XJAlert.display(getSelectedEditor().getWindowContainer(), "Error", "Cannot generate the parser because there is no parser in this grammar.");
                return;
            }
        }

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

    public void checkAndShowGeneratedCode(String rule, int type) {
        if(!checkLanguage())
            return;

        if(!generateCode.isGeneratedTextFileExisting(type)
                || generateCode.isFileModifiedSinceLastGeneration()
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

    private void showGeneratedCode(String rule, int type) {
        CodeDisplay dc = new CodeDisplay(getSelectedEditor().getXJFrame());
        String title;
        try {
            title = getSelectedEditor().getGrammarEngine().getGeneratedClassName(type)+".java";
        } catch (Exception e) {
            XJAlert.display(getSelectedEditor().getWindowContainer(), "Error", "Cannot cannot get the name of the generated file:\n"+e.toString());
            return;
        }

        String text;
        try {
            text = generateCode.getGeneratedText(type);
        } catch (Exception e) {
            XJAlert.display(getSelectedEditor().getWindowContainer(), "Error", "Exception while reading the generated file:\n"+e.toString());
            return;
        }

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
                XJAlert.display(getSelectedEditor().getWindowContainer(), "Error", "Cannot find markers for rule \""+rule+"\"");
                return;
            }
        }
        dc.setText(text);
        dc.setTitle(title);

        getSelectedEditor().addTab(dc);
        getSelectedEditor().makeBottomComponentVisible();
    }

    public boolean codeGenerateDisplaySuccess() {
        return !actionShowCodeAfterGeneration;
    }

    public void codeGenerateDidComplete() {
        if(actionShowCodeAfterGeneration) {
            actionShowCodeAfterGeneration = false;
            showGeneratedCode(actionShowCodeRule, actionShowCodeType);
        }
    }

    public void checkGrammarDidBegin() {
        // do nothing
    }

    public void checkGrammarDidEnd(ANTLRGrammarResult result) {
        if(result.getErrorCount() == 0) {
            generateCodeProcessContinued();
        } else {
            XJAlert.display(getSelectedEditor().getWindowContainer(), "Error", "Check Grammar reported some errors:\n"+result.getFirstErrorMessage()+"\nConsult the console for more information.");
        }
    }
}
