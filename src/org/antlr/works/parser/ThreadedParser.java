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

import org.antlr.works.editor.EditorProvider;
import org.antlr.works.editor.EditorThread;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ThreadedParser extends EditorThread {

    private EditorProvider provider = null;

    private Parser parser = null;
    private List observers = null;

    private List rules = null;
    private List groups = null;

    public ThreadedParser(EditorProvider provider) {
        this.provider = provider;
        parser = new Parser();
        observers = new ArrayList();
        start();
    }

    public void addObserver(ThreadedParserObserver observer) {
        observers.add(observer);
    }

    private synchronized void setRules(List rules, List groups) {
        this.rules = rules;
        this.groups = groups;
    }

    public synchronized List getRules() {
        return rules;
    }

    public synchronized List getGroups() {
        return groups;
    }

    public synchronized List getRuleNames() {
        List names = new ArrayList();
        if(rules != null) {
            for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
                Parser.Rule rule = (Parser.Rule) iterator.next();
                names.add(rule.name);
            }
        }
        return names;
    }

    public synchronized Parser.Rule getRuleAtIndex(int index) {
        if(index < 0 || index >= rules.size())
            return null;
        else
            return (Parser.Rule)rules.get(index);
    }

    public synchronized List getTokens() {
        return parser.tokens;
    }

    public synchronized List getLines() {
        return parser.getLines();
    }

    public synchronized int getMaxLines() {
        return parser.getMaxLines();
    }

    public void parse() {
        awakeThread(250);
    }

    public void threadRun() throws Exception {
        //long t = System.currentTimeMillis();
        parser.parse(provider.getText());
        setRules(parser.rules, parser.groups);

        //System.out.println("Parsing in "+(System.currentTimeMillis()-t));
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                for(int i=0; i<observers.size(); i++) {
                    ThreadedParserObserver obs = (ThreadedParserObserver)observers.get(i);
                    obs.parserDidComplete();
                }
            }
        });
    }
}
