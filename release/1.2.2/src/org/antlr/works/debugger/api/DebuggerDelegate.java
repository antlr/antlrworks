package org.antlr.works.debugger.api;

import org.antlr.works.generate.CodeGenerate;
import org.antlr.works.grammar.element.ElementBlock;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.utils.Console;
import org.antlr.xjlib.appkit.document.XJDocument;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;/*

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

public interface DebuggerDelegate {

    GrammarEngine getGrammarEngine();
    String getGrammarName();

    void debuggerStarted();
    void debuggerStopped();

    void debuggerSetLocation(String grammar, int line, int column);
    void debuggerSelectText(String grammar, int line, int column);

    Container getContainer();
    
    CodeGenerate getCodeGenerate();
    String getTokenVocab();
    Console getConsole();

    List<ElementBlock> getBlocks();

    Map<Integer,Set<String>> getBreakpoints();

    XJDocument getDocument();

    List<ElementRule> getRules();

    List<ElementRule> getSortedRules();

    boolean ensureDocumentSaved();

    ContextualMenuFactory createContextualMenuFactory();

    void selectConsoleTab();

}
