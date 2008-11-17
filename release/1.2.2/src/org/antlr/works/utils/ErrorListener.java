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

import java.util.LinkedList;
import java.util.List;

public class ErrorListener implements ANTLRErrorListener {

    private static ThreadLocal<ErrorListener> threadLocalListener = new ThreadLocal<ErrorListener>();

    public List<String> infos = new LinkedList<String>();
    public List<Message> errors = new LinkedList<Message>();
    public List<Message> warnings = new LinkedList<Message>();

    public boolean printToConsole = true;
    public ErrorListener forwardListener = null;

    public static synchronized ErrorListener getThreadInstance() {
        if(threadLocalListener.get() == null) {
            threadLocalListener.set(new ErrorListener());
        }
        return threadLocalListener.get();
    }

    public ErrorListener() {

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
        return errors.size() > 0;
    }

    public boolean hasWarnings() {
        return warnings.size() > 0;
    }
    
    public String getFirstErrorMessage() {
        if(hasErrors()) {
            return errors.get(0).toString();
        } else {
            return null;
        }
    }

    public void info(String msg) {
        infos.add(msg);
        if(forwardListener != null)
            forwardListener.info(msg);
        print(msg, Console.LEVEL_NORMAL);
    }

    public void error(Message msg) {
        errors.add(msg);
        if(forwardListener != null)
            forwardListener.error(msg);
        print(msg.toString(), Console.LEVEL_ERROR);
    }

    public void warning(Message msg) {
        warnings.add(msg);
        if(forwardListener != null)
            forwardListener.warning(msg);
        print(msg.toString(), Console.LEVEL_WARNING);
    }

    public void error(ToolMessage msg) {
        errors.add(msg);
        if(forwardListener != null)
            forwardListener.error(msg);
        print(msg.toString(), Console.LEVEL_ERROR);
    }

    public void print(String msg, int level) {
        if(!printToConsole)
            return;

        boolean previousVerbose = DecisionProbe.verbose;
        DecisionProbe.verbose = false;
        try {
            ConsoleHelper.getCurrent().println(msg, level);
        } catch(Exception e) {
            e.printStackTrace();
        }
        DecisionProbe.verbose = previousVerbose;
    }
}
