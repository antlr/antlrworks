package org.antlr.works.grammar.engine;

import org.antlr.works.ate.syntax.generic.ATESyntaxEngine;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.antlr.ANTLRGrammarEngine;
import org.antlr.works.grammar.element.*;

import java.util.List;/*

[The "BSD licence"]
Copyright (c) 2005-07 Jean Bovet
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

public interface GrammarEngine {

    void close();

    ANTLRGrammarEngine getANTLRGrammarEngine();
    ATESyntaxEngine getSyntaxEngine();

    ElementGrammarName getElementName();
    String getGrammarName();

    List<ElementRule> getRules();
    List<ElementRule> getDuplicateRules();
    ElementRule getRuleWithName(String name);
    ElementRule getRuleAtIndex(int index);

    List<ElementReference> getReferences();
    List<ElementReference> getUndefinedReferences();

    List<ElementImport> getImports();
    List<ElementAction> getActions();
    List<ElementGroup> getGroups();

    int getNumberOfLines();
    int getNumberOfRules();
    int getNumberOfErrors();

    int getFirstDeclarationPosition(String name);
    List<String> getGrammarsOverriddenByRule(String name);

    List<ATEToken> getTokens();

    void analyze() throws Exception;
    void computeRuleErrors(ElementRule rule);

    // todo needed?
    void parseDidParse();
    void markDirty();
    void reset();

    boolean isVersion2();
    boolean isCombinedGrammar();
}
