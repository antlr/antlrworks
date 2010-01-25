package org.antlr.works.components;

import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.debugger.api.DebuggerDelegate;
import org.antlr.works.editor.ConsoleTab;
import org.antlr.works.generate.CodeGenerate;
import org.antlr.works.grammar.element.ElementBlock;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.utils.Console;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.foundation.XJUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

/*

[The "BSD licence"]
Copyright (c) 2005-08 Jean Bovet
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
public class GrammarDebuggerDelegate implements DebuggerDelegate {

    private final GrammarWindow window;

    public GrammarDebuggerDelegate(GrammarWindow window) {
        this.window = window;
    }

    public GrammarEngine getGrammarEngine() {
        return window.getGrammarEngine();
    }

    public String getGrammarName() {
        return XJUtils.getPathByDeletingPathExtension(window.getGrammarFileName());
    }

    public void debuggerStarted() {
        window.selectDebuggerTab();

        ((ConsoleTab)getConsole()).makeCurrent();

        window.setEditable(false);
        window.refreshMainMenuBar();
    }

    public void debuggerStopped() {
        window.setDebuggerLocation(-1);
        window.setEditable(true);
        window.refreshMainMenuBar();
    }

    public void debuggerSetLocation(String grammar, int line, int column) {
        int grammarIndex = computeAbsoluteGrammarIndex(line, column);
        if(grammarIndex >= 0) {
            window.setDebuggerLocation(grammarIndex);
        }
    }

    public void debuggerSelectText(String grammar, int line, int column) {
        int grammarIndex = computeAbsoluteGrammarIndex(line, column);
        if(grammarIndex >= 0) {
            window.selectTextRange(grammarIndex, grammarIndex+1);
        }
    }

    public XJDocument getDocument() {
        return window.getDocument();
    }

    public List<ElementRule> getRules() {
        return window.getRules();
    }

    public List<ElementRule> getSortedRules() {
        return window.getSortedRules();
    }

    public boolean ensureDocumentSaved() {
        return window.ensureDocumentSaved();
    }

    public CodeGenerate getCodeGenerate() {
        return new CodeGenerate(window, null);
    }

    public String getTokenVocab() {
        return getGrammarEngine().getTokenVocab();
    }

    public Container getContainer() {
        return window.getJavaContainer();
    }

    public Console getConsole() {
        return window.getConsoleTab();
    }

    public List<ElementBlock> getBlocks() {
        return getGrammarEngine().getBlocks();
    }

    public Map<Integer, Set<String>> getBreakpoints() {
        Map<Integer,Set<String>> breakpoints = new HashMap<Integer, Set<String>>();
        for(Integer line : window.getBreakpoints()) {
            Set<String> names = breakpoints.get(line);
            if(names == null) {
                names = new HashSet<String>();
                breakpoints.put(line, names);
            }
            names.add(XJUtils.getPathByDeletingPathExtension(window.getGrammarFileName()));
        }
        return breakpoints;
    }

    public ContextualMenuFactory createContextualMenuFactory() {
        return window.createContextualMenuFactory();
    }

    public void selectConsoleTab() {
        window.selectConsoleTab();
    }

    private int computeAbsoluteGrammarIndex(int lineIndex, int column) {
        List<ATELine> lines = window.getLines();
        if(lineIndex-1<0 || lineIndex-1 >= lines.size())
            return -1;

        ATELine line = lines.get(lineIndex-1);
        return line.position+column-1;
    }

}
