package org.antlr.works.grammar.generation;

import org.antlr.works.grammar.antlr.AntlrEngineGrammar;
import org.antlr.works.grammar.element.ElementGrammarName;
import org.antlr.works.grammar.syntax.GrammarSyntax;

import java.util.ArrayList;
import java.util.List;

/*

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

public abstract class GrammarGeneratedFiles {

    protected GrammarSyntax syntax;

    public static GrammarGeneratedFiles getInstance(GrammarSyntax syntax) throws Exception {
        if(syntax.isCombinedGrammar()) {
            return new GrammarGeneratedFilesCombined(syntax);
        }
        if(syntax.isLexerGrammar()) {
            return new GrammarGeneratedFilesLexer(syntax);
        }
        if(syntax.isParserGrammar()) {
            return new GrammarGeneratedFilesParser(syntax);
        }
        // todo tree grammar
        throw new IllegalArgumentException("Invalid grammar type "+ syntax.getType());
    }

    protected GrammarGeneratedFiles(GrammarSyntax syntax) throws Exception {
        this.syntax = syntax;
        // todo see if really needed
        syntax.getAntlrGrammar().createGrammars();
    }

    protected AntlrEngineGrammar getAntlrGrammar() {
        return syntax.getAntlrGrammar();
    }

    /**
     * Returns true if the grammar type needs a suffix for the generated class files.
     * Only combined grammars need a suffix.
     *
     * @return true if the grammar generated files need a suffix
     */
    public boolean hasSuffix() {
        return syntax.isCombinedGrammar();
    }

    public String getSuffix(int type) {
        if(hasSuffix()) {
            switch(type) {
                case ElementGrammarName.LEXER:
                    return "Lexer";
                case ElementGrammarName.PARSER:
                    return "Parser";
            }
        }
        return "";
    }

    public List<String> getGeneratedNames() {
        List<String> names = new ArrayList<String>();
        buildGeneratedNames(names);
        return names;
    }

    protected abstract void buildGeneratedNames(List<String> names);

}
