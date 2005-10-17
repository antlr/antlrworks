package org.antlr.works.editor.actions;

import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.tool.TUsage;
import org.antlr.works.parser.Parser;
import org.antlr.works.parser.Token;
import org.antlr.works.stats.Statistics;

import java.util.Iterator;
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

public class MenuFind extends AbstractActions {

    public MenuFind(EditorWindow editor) {
        super(editor);
    }

    public void find() {
        editor.findAndReplace.find();
    }

    public void findNext() {
        editor.findAndReplace.next();
    }

    public void findPrev() {
        editor.findAndReplace.prev();
    }

    public void findUsage() {
        Token token = editor.getTokenAtPosition(getCaretPosition());
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
                Parser.Rule matchedRule = editor.rules.getEnclosingRuleAtPosition(candidate.getStartIndex());
                if(matchedRule != null)
                    usage.addMatch(matchedRule, candidate);
            }
        }

        Statistics.shared().recordEvent(Statistics.EVENT_FIND_USAGES);
    }

}
