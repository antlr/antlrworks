package org.antlr.works.grammar.engine;

import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.antlr.ANTLRGrammarEngine;
import org.antlr.works.grammar.element.*;
import org.antlr.works.grammar.syntax.GrammarSyntaxEngine;

import java.util.List;
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

public interface GrammarProperties {

    void setGrammarEngine(GrammarEngine engine);
    void setSyntaxEngine(GrammarSyntaxEngine syntaxEngine);
    void setAntlrEngine(ANTLRGrammarEngine antlrEngine);

    List<ElementRule> getRules();
    List<ElementRule> getDuplicateRules();
    ElementRule getRuleWithName(String name);
    ElementRule getRuleAtIndex(int index);
    List<String> getRuleNames();

    List<ElementReference> getUndefinedReferences();

    List<ElementGroup> getGroups();
    List<ElementBlock> getBlocks();
    List<ElementAction> getActions();
    List<ElementReference> getReferences();
    List<ElementImport> getImports();
    List<ATEToken> getDecls();

    ElementGrammarName getElementName();
    String getName();

    List<String> getAllGeneratedNames() throws Exception;

    String getTokenVocab();

    int getNumberOfRulesWithErrors();
    int getNumberOfErrors();

    int getFirstDeclarationPosition(String name);

    int getType();

    boolean isParserGrammar();
    boolean isLexerGrammar();
    boolean isCombinedGrammar();
    boolean isTreeParserGrammar();

    void reset();
    void updateAll();
    void parserCompleted();

}
