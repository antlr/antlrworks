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

package org.antlr.works.editor.tool;

import org.antlr.works.editor.swing.AutoCompletionMenu;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class TTemplateRules {

    private JTextComponent textComponent;
    private AutoCompletionMenu autoCompletionMenu;

    private List templateRuleNames = new ArrayList();
    private List templateRuleTexts = new ArrayList();

    public TTemplateRules(JTextComponent textComponent, AutoCompletionMenu autoCompletionMenu) {
        this.textComponent = textComponent;
        this.autoCompletionMenu = autoCompletionMenu;

        initTemplateRules();
        addTemplateRuleBindings();
    }

    public void initTemplateRules() {
        templateRuleNames.add("INTEGER");
        templateRuleNames.add("ID");
        templateRuleNames.add("DIGIT");
        templateRuleNames.add("LETTER");
        templateRuleNames.add("WS (ignore)");
        templateRuleNames.add("WS");

        templateRuleTexts.add("INTEGER\t:\tDIGIT (DIGIT)*;");
        templateRuleTexts.add("ID\t:\tLETTER (LETTER | DIGIT)*;");
        templateRuleTexts.add("protected\nDIGIT\t:\t'0'..'9';");
        templateRuleTexts.add("protected\nLETTER\t:\t'a'..'z' | 'A'..'Z' ;");
        templateRuleTexts.add("WS\t:\t(' ' | '\\t' | '\\n' | '\\r') { $setType(Token.SKIP); };");
        templateRuleTexts.add("WS\t:\t(' ' | '\\t' | '\\n' | '\\r');");
    }

    public void addTemplateRuleBindings() {
        textComponent.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK), "CONTROL-R");
        textComponent.getActionMap().put("CONTROL-R", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                onDisplayTemplateRulesPopUp();
            }
        });
    }

    public void onDisplayTemplateRulesPopUp() {
        int position = textComponent.getCaretPosition();
        autoCompletionMenu.showAutoCompleteMenu(position, templateRuleNames, templateRuleTexts);
        autoCompletionMenu.setInsertionStartIndex(position);
        autoCompletionMenu.setInsertionEndIndex(position);
    }
}
