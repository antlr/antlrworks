package org.antlr.works.menu;

import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.container.ComponentContainer;
import org.antlr.works.find.Usages;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.stats.StatisticsAW;
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

public class ActionFind extends ActionAbstract {

    public ActionFind(ComponentContainer editor) {
        super(editor);
    }

    public void find() {
        getSelectedEditor().find();
    }

    public void findNext() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_FIND_NEXT);
        getSelectedEditor().findAndReplace.next();
    }

    public void findPrev() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_FIND_PREVIOUS);
        getSelectedEditor().findAndReplace.prev();
    }

    public void findSelection() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_FIND_TEXT_AT_CARET);
        getSelectedEditor().findAndReplace.setFindString(getSelectedEditor().getTextPane().getSelectedText());
        getSelectedEditor().findAndReplace.next();
    }

    public void findUsage() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_FIND_USAGES);

        ATEToken token = getSelectedEditor().getCurrentToken();
        if(token == null)
            return;

        Usages usage = new Usages(getSelectedEditor(), token);
        getSelectedEditor().addTab(usage);

        for (ATEToken ateToken : getSelectedEditor().getTokens()) {
            if (ateToken.getAttribute().equals(token.getAttribute())) {
                ElementRule matchedRule = getSelectedEditor().rules.getEnclosingRuleAtPosition(ateToken.getStartIndex());
                if (matchedRule != null)
                    usage.addMatch(matchedRule, ateToken);
            }
        }

        getSelectedEditor().makeBottomComponentVisible();
    }

}
