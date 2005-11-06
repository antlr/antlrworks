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

package org.antlr.works.parser;

import org.antlr.works.editor.helper.EditorProvider;
import org.antlr.works.editor.helper.EditorThread;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ThreadedParser extends EditorThread {

    protected EditorProvider provider = null;

    protected Parser parser = null;
    protected List observers = null;

    protected List rules;
    protected List groups;
    protected List blocks;
    protected List actions;
    protected List references;
    protected List tokens;

    protected ParserName name;

    protected static int delay = 250;

    public ThreadedParser(EditorProvider provider) {
        this.provider = provider;
        parser = new Parser();
        observers = new ArrayList();
    }

    public void awake() {
        start();
    }

    public static void setDelay(int delay) {
        ThreadedParser.delay = delay;
    }

    public void addObserver(ThreadedParserObserver observer) {
        observers.add(observer);
    }

    public synchronized List getRules() {
        return rules;
    }

    public synchronized List getGroups() {
        return groups;
    }

    public synchronized List getBlocks() {
        return blocks;
    }

    public synchronized List getActions() {
        return actions;
    }

    public synchronized List getReferences() {
        return references;
    }

    public synchronized List getTokens() {
        return tokens;
    }

    public synchronized List getLines() {
        return parser.getLines();
    }

    public synchronized int getMaxLines() {
        return parser.getMaxLines();
    }

    public synchronized ParserName getName() {
        return name;
    }

    public synchronized List getDeclaredTokenNames() {
        List names = new ArrayList();
        for(int index=0; index<blocks.size(); index++) {
            ParserBlock block = (ParserBlock)blocks.get(index);
            if(block.isTokenBlock) {
                List internalTokens = block.getInternalTokens();
                for(int t=0; t<internalTokens.size(); t++) {
                    Token token = (Token)internalTokens.get(t);
                    names.add(token.getAttribute());
                }
            }
        }
        return names;
    }

    public synchronized List getRuleNames() {
        List names = new ArrayList();
        if(rules != null) {
            for (int index=0; index<rules.size(); index++) {
                ParserRule rule = (ParserRule) rules.get(index);
                names.add(rule.name);
            }
        }
        return names;
    }

    public synchronized ParserRule getRuleAtIndex(int index) {
        if(index < 0 || index >= rules.size())
            return null;
        else
            return (ParserRule)rules.get(index);
    }

    public void parse() {
        awakeThread(delay);
    }

    protected void notifyWillParse() {
        for(int i=0; i<observers.size(); i++) {
            ThreadedParserObserver obs = (ThreadedParserObserver)observers.get(i);
            obs.parserWillParse();
        }
    }

    protected void notifyDidParse() {
        for(int i=0; i<observers.size(); i++) {
            ThreadedParserObserver obs = (ThreadedParserObserver)observers.get(i);
            obs.parserDidParse();
        }
    }

    protected synchronized void setParserAttribute(Parser parser) {
        this.rules = copyArray(parser.rules);
        this.groups = copyArray(parser.groups);
        this.blocks = copyArray(parser.blocks);
        this.actions = copyArray(parser.actions);
        this.references = copyArray(parser.references);
        this.tokens = copyArray(parser.tokens);
        this.name = parser.name;
    }

    protected List copyArray(List list) {
        ArrayList array = (ArrayList)list;
        return (List)array.clone();
    }

    public void threadRun() throws Exception {
        notifyWillParse();
        
        parser.parse(provider.getText());
        setParserAttribute(parser);

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                notifyDidParse();
            }
        });
    }
}
