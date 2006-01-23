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

package org.antlr.works.utils;

import org.antlr.analysis.DecisionProbe;
import org.antlr.tool.ANTLRErrorListener;
import org.antlr.tool.Message;
import org.antlr.tool.ToolMessage;
import org.antlr.works.editor.EditorConsole;

import java.util.LinkedList;
import java.util.List;

public class ErrorListener implements ANTLRErrorListener {

    protected static ErrorListener shared = null;

    public List infos = new LinkedList();
    public List errors = new LinkedList();
    public List warnings = new LinkedList();

    public boolean printToConsole = false;
    public ErrorListener forwardListener = null;

    public static synchronized ErrorListener shared() {
        if(shared == null) {
            shared = new ErrorListener();
            shared.setPrintToConsole(true);
        }
        return shared;
    }

    public void setPrintToConsole(boolean flag) {
        this.printToConsole = flag;
    }

    public void setForwardListener(ErrorListener listener) {
        this.forwardListener = listener;
    }

    public void clear() {
        infos.clear();
        errors.clear();
        warnings.clear();
    }

    public boolean hasErrors() {
        return size() > 0;
    }

    public int size() {
        return infos.size() + errors.size() + warnings.size();
    }

    public void info(String msg) {
        infos.add(msg);
        if(forwardListener != null)
            forwardListener.info(msg);
        print(msg);
    }

    public void error(Message msg) {
        errors.add(msg);
        if(forwardListener != null)
            forwardListener.error(msg);
        print(msg);
    }

    public void warning(Message msg) {
        warnings.add(msg);
        if(forwardListener != null)
            forwardListener.warning(msg);
        print(msg);
    }

    public void error(ToolMessage msg) {
        errors.add(msg);
        if(forwardListener != null)
            forwardListener.error(msg);
        print(msg);
    }

    public void print(String msg) {
        if(printToConsole)
            EditorConsole.getCurrent().println(msg);
    }

    public void print(Message msg) {
        boolean previousVerbose = DecisionProbe.verbose;
        DecisionProbe.verbose = false;
        try {
            EditorConsole.getCurrent().println(msg.toString());
        } catch(Exception e) {
            e.printStackTrace();
        }
        DecisionProbe.verbose = previousVerbose;
    }
}
