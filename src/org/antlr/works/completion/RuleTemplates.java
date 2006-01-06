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

package org.antlr.works.completion;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class RuleTemplates extends AutoCompletionMenu {

    private List templateRuleNames = new ArrayList();
    private List templateRuleTexts = new ArrayList();

    public RuleTemplates(AutoCompletionMenuDelegate delegate, JTextComponent textComponent, JFrame frame) {
        super(delegate, textComponent, frame);

        this.list.addMouseMotionListener(new ListMouseMotionAdapter());
        this.list.addMouseListener(new ListMouseAdapter());

        initTemplateRules();
    }

    public void initTemplateRules() {
        templateRuleNames.add("INTEGER");
        templateRuleNames.add("ID");
        templateRuleNames.add("DIGIT");
        templateRuleNames.add("LETTER");
        templateRuleNames.add("WS (ignore)");
        templateRuleNames.add("WS");

        templateRuleTexts.add("INTEGER\n\t:\tDIGIT (DIGIT)*\n\t;\n");
        templateRuleTexts.add("ID\t:\tLETTER (LETTER | DIGIT)*\n\t;\n");
        templateRuleTexts.add("DIGIT\t:\t'0'..'9'\n\t;\n");
        templateRuleTexts.add("LETTER\n\t:\t'a'..'z' | 'A'..'Z'\n\t;\n");
        templateRuleTexts.add("WS\t:\t(' ' | '\\t' | '\\n' | '\\r') { $setType(Token.SKIP); }\n\t;\n");
        templateRuleTexts.add("WS\t:\t(' ' | '\\t' | '\\n' | '\\r')\n\t;\n");
    }

    public KeyStroke overlayDisplayKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.META_MASK);
    }

    public String overlayDisplayKeyStrokeMappingName() {
        return "META-T";
    }

    public boolean overlayWillDisplay() {
        int position = getTextComponent().getCaretPosition();
        setDisplayIndex(position);
        setWordLists(templateRuleNames, templateRuleTexts);
        setInsertionStartIndex(position);
        setInsertionEndIndex(position);
        return true;
    }

    public class ListMouseMotionAdapter extends MouseMotionAdapter {
        public void mouseMoved(MouseEvent e) {
            list.setSelectedIndex(list.locationToIndex(e.getPoint()));
        }
    }

    public class ListMouseAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            autoComplete();
            hide();
        }
    }

}
