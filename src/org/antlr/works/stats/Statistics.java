package org.antlr.works.stats;

import edu.usfca.xj.appkit.app.XJApplication;
import org.antlr.works.editor.EditorPreferences;

import java.util.Date;

/*

[The "BSD licence"]
Copyright (c) 2004-05 Jean Bovet
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

public class Statistics {

    protected static Statistics shared = null;

    protected static final String PREF_KEY = "CURSOR_BLINK";
    protected static final String PREF_KEY_FROM_DATE = "DATE_BLINK";

    public static final int EVENT_INTERPRETER_MENU = 0;
    public static final int EVENT_INTERPRETER_BUTTON = 1;

    public static final int EVENT_REMOTE_DEBUGGER = 2;
    public static final int EVENT_LOCAL_DEBUGGER = 3;
    public static final int EVENT_LOCAL_DEBUGGER_BUILD = 4;

    public static final int EVENT_FIND_USAGES = 5;
    public static final int EVENT_GOTO_DECLARATION = 6;
    public static final int EVENT_RENAME = 7;
    public static final int EVENT_GOTO_LINE = 8;
    public static final int EVENT_GOTO_CHAR = 9;
    public static final int EVENT_CHECK_GRAMMAR = 10;

    public static final int EVENT_EXPORT_RULE_IMAGE = 11;
    public static final int EVENT_EXPORT_ANTLRNFA_DOT = 12;
    public static final int EVENT_EXPORT_OPTIMIZEDNFA_DOT = 13;
    public static final int EVENT_EXPORT_RAWNFA_DOT = 14;
    public static final int EVENT_EXPORT_EVENTS_TEXT = 15;

    public static final int EVENT_SHOW_PREFERENCES = 16;
    public static final int EVENT_SHOW_AUTO_COMPLETION_MENU = 17;
    public static final int EVENT_TOGGLE_SYNTAX_COLORING = 18;
    public static final int EVENT_TOGGLE_SYNTAX_DIAGRAM = 19;
    public static final int EVENT_TOGGLE_NFA_OPTIMIZATION = 20;

    public static final int EVENT_HIDE_SINGLE_ACTION = 21;
    public static final int EVENT_SHOW_ALL_ACTIONS = 22;
    public static final int EVENT_HIDE_ALL_ACTIONS = 23;

    public static final int EVENT_GENERATE_CODE = 24;
    public static final int EVENT_SHOW_RULE_GENERATED_CODE = 25;
    public static final int EVENT_SHOW_PARSER_GENERATED_CODE = 26;
    public static final int EVENT_SHOW_LEXER_GENERATED_CODE = 27;

    public static final int EVENT_SHOW_HELP = 28;

    public static final int EVENT_DROP_RULE = 29;
    public static final int EVENT_TOGGLE_SD_NFA = 30;
    public static final int EVENT_DEBUGGER_STOP = 31;
    public static final int EVENT_DEBUGGER_STEP_BACKWARD = 32;
    public static final int EVENT_DEBUGGER_STEP_FORWARD = 33;
    public static final int EVENT_DEBUGGER_GOTO_START = 34;
    public static final int EVENT_DEBUGGER_GOTO_END = 35;
    public static final int EVENT_DEBUGGER_TOGGLE_INPUT_TOKENS = 36;
    public static final int EVENT_DEBUGGER_SHOW_EVENTS_LIST = 37;
    public static final int EVENT_DEBUGGER_SHOW_RULES_STACK = 38;

    public static final int EVENT_COUNT = 39;

    protected static final String[] eventNames = { "Intepreter (menu)" ,
                                                   "Interpreter (button)",
                                                   "Debugger (remote)",
                                                   "Debugger (local)",
                                                   "Debugger (local and build)",
                                                   "Find usages",
                                                   "Go to declaration",
                                                   "Rename",
                                                   "Go to line",
                                                   "Go to character",
                                                   "Check grammar",
                                                   "Export rule as image",
                                                   "Export ANTLR NFA as DOT",
                                                   "Export optimized NFA as DOT",
                                                   "Export raw NFA as DOT",
                                                   "Export events as text",
                                                   "Show preferences dialog",
                                                   "Show auto-completion menu",
                                                   "Toggle syntax coloring",
                                                   "Toggle syntax diagram",
                                                   "Toggle NFA optimization",
                                                   "Hide action",
                                                   "Show all actions",
                                                   "Hide all actions",
                                                   "Generate code",
                                                   "Show rule generated code",
                                                   "Show parser generated code",
                                                   "Show lexer generated code",
                                                   "Show help",
                                                   "Drag and drop rule",
                                                   "Toggle syntax diagram/NFA",
                                                   "Debugger stop",
                                                   "Debugger step backward",
                                                   "Debugger step forward",
                                                   "Debugger go to start",
                                                   "Debugger go to end",
                                                   "Debugger toggle input tokens",
                                                   "Debugger show events list",
                                                   "Debugger show rules stack"
    };

    protected int[] events = null;
    protected String date = null;

    public static Statistics shared() {
        if(shared == null)
            shared = new Statistics();
        return shared;
    }

    public void reset() {
        for (int i = 0; i < events.length; i++) {
            events[i] = 0;
        }
        initDate();
    }

    public void initDate() {
        setFromDate(new Date().toString());
    }

    public void setFromDate(String date) {
        EditorPreferences.getPreferences().setString(PREF_KEY_FROM_DATE, date);
    }

    public String getFromDate() {
        return EditorPreferences.getPreferences().getString(PREF_KEY_FROM_DATE, null);
    }

    public int getCount() {
        return getEvents().length;
    }

    public Object getEventName(int index) {
        return eventNames[index];
    }

    public int getEventCount(int index) {
        return getEvents()[index];
    }

    public void recordEvent(int event) {
        setCount(event, getCount(event)+1);
    }

    public void close() {
        EditorPreferences.getPreferences().setObject(PREF_KEY, events);
    }

    protected int[] getEvents() {
        if(events == null) {
            try {
                events = (int[])EditorPreferences.getPreferences().getObject(PREF_KEY, null);
            } catch(Exception e) {
                events = null;
                System.err.println("Statistics: "+e);
            }
            if(events == null || events.length != EVENT_COUNT) {
                events = new int[EVENT_COUNT];
                initDate();
            }
        }
        return events;
    }

    protected void setCount(int event, int count) {
        getEvents()[event] = count;
    }

    protected int getCount(int event) {
        Integer count = (Integer)getObject(event);
        if(count == null)
            return 0;
        else
            return count.intValue();
    }

    protected Object getObject(int event) {
        return new Integer(getEvents()[event]);
    }

    public String getRawString() {
        StringBuffer s = new StringBuffer();
        s.append(XJApplication.getAppVersionShort());
        s.append('\t');
        for (int i = 0; i < getEvents().length; i++) {
            s.append(events[i]);
            if(i<events.length-1)
                s.append('\t');
        }
        return s.toString();
    }

    public String getReadableString() {
        StringBuffer s = new StringBuffer();
        s.append("Version: ");
        s.append(XJApplication.getAppVersionShort());
        s.append('\n');
        for (int i = 0; i < getEvents().length; i++) {
            s.append(eventNames[i]);
            s.append(": ");
            s.append(events[i]);
            if(i<events.length-1)
                s.append('\n');
        }
        return s.toString();
    }
}
