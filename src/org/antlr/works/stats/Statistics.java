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

package org.antlr.works.stats;

import edu.usfca.xj.appkit.app.XJApplication;
import org.antlr.works.prefs.AWPrefs;

import java.util.*;

public class Statistics {

    protected static Statistics shared = null;

    /* Last change: version 1.0ea9 */
    protected static final String PREF_KEY = "CURSOR_BLINK_2";
    protected static final String PREF_KEY_FROM_DATE = "DATE_BLINK_2";

    /* Menu File */
    public static final int EVENT_EXPORT_AS_BITMAP = 0;
    public static final int EVENT_EXPORT_AS_EPS = 1;
    public static final int EVENT_EXPORT_AS_DOT = 2;
    public static final int EVENT_EXPORT_EVENTS_AS_TEXT = 3;

    /* Menu Find */
    public static final int EVENT_FIND_DIALOG = 10;
    public static final int EVENT_FIND_NEXT = 11;
    public static final int EVENT_FIND_PREVIOUS = 12;
    public static final int EVENT_FIND_TEXT_AT_CARET = 13;
    public static final int EVENT_FIND_USAGES = 14;

    /* Menu GoTo */
    public static final int EVENT_GOTO_RULE = 20;
    public static final int EVENT_GOTO_DECLARATION = 21;
    public static final int EVENT_GOTO_LINE = 22;
    public static final int EVENT_GOTO_CHAR = 23;
    public static final int EVENT_GOTO_BACK = 24;
    public static final int EVENT_GOTO_FORWARD = 25;
    public static final int EVENT_GOTO_PREV_BRKPT = 26;
    public static final int EVENT_GOTO_NEXT_BRKPT = 27;

    /* Menu Grammar */
    public static final int EVENT_SHOW_TOKEN_SD = 40;
    public static final int EVENT_SHOW_DECISION_DFA = 41;
    public static final int EVENT_SHOW_RULE_DEPENDENCY = 42;
    public static final int EVENT_INSERT_RULE_TEMPLATE = 43;
    public static final int EVENT_RULE_GROUP = 44;
    public static final int EVENT_RULE_UNGROUP = 45;
    public static final int EVENT_CHECK_GRAMMAR = 46;

    /* Menu Refactor */
    public static final int EVENT_RENAME = 60;
    public static final int EVENT_REPLACE_LITERALS = 61;
    public static final int EVENT_REMOVE_LEFT_RECURSION = 62;
    public static final int EVENT_REMOVE_ALL_LEFT_RECURSION = 63;

    public static final int EVENT_EXTRACT_RULE = 64;
    public static final int EVENT_INLINE_RULE = 65;

    public static final int EVENT_CONVERT_LITERALS_TO_SINGLE = 66;
    public static final int EVENT_CONVERT_LITERALS_TO_DOUBLE = 67;
    public static final int EVENT_CONVERT_LITERALS_TO_CSTYLE = 68;

    /* Menu Generate */
    public static final int EVENT_GENERATE_CODE = 80;
    public static final int EVENT_SHOW_PARSER_GENERATED_CODE = 81;
    public static final int EVENT_SHOW_LEXER_GENERATED_CODE = 82;
    public static final int EVENT_SHOW_RULE_GENERATED_CODE = 83;

    /* Menu Run */
    public static final int EVENT_INTERPRETER_MENU = 90;
    public static final int EVENT_INTERPRETER_BUTTON = 91;

    public static final int EVENT_LOCAL_DEBUGGER = 92;
    public static final int EVENT_LOCAL_DEBUGGER_BUILD = 93;
    public static final int EVENT_REMOTE_DEBUGGER = 94;

    /* Menu SCM */
    public static final int EVENT_SCM_EDIT = 100;
    public static final int EVENT_SCM_ADD = 101;
    public static final int EVENT_SCM_REVERT = 102;
    public static final int EVENT_SCM_DELETE = 103;
    public static final int EVENT_SCM_SUBMIT = 104;
    public static final int EVENT_SCM_SYNC = 105;

    /* Toolbar toggle */
    public static final int EVENT_TOGGLE_SYNTAX_COLORING = 120;
    public static final int EVENT_TOGGLE_SYNTAX_DIAGRAM = 121;
    public static final int EVENT_TOGGLE_IDEAS = 122;
    public static final int EVENT_TOGGLE_RULE_SORT = 123;

    /* Misc */
    public static final int EVENT_SHOW_PREFERENCES = 130;
    public static final int EVENT_SHOW_HELP = 131;
    public static final int EVENT_CHECK_FOR_UPDATES = 132;
    public static final int EVENT_SHOW_AUTO_COMPLETION_MENU = 133;
    public static final int EVENT_DROP_RULE = 134;
    public static final int EVENT_TOGGLE_SD_NFA = 135;

    /* Debugger event */
    public static final int EVENT_DEBUGGER_STOP = 140;
    public static final int EVENT_DEBUGGER_STEP_BACK = 141;
    public static final int EVENT_DEBUGGER_STEP_FORWARD = 142;
    public static final int EVENT_DEBUGGER_FAST_FORWARD = 143;
    public static final int EVENT_DEBUGGER_GOTO_START = 144;
    public static final int EVENT_DEBUGGER_GOTO_END = 145;
    public static final int EVENT_DEBUGGER_TOGGLE_INPUT_TOKENS = 146;

    static final Map eventNames = new HashMap();
    static final List eventList = new ArrayList();

    static void register(int eventID, String eventName) {
        eventNames.put(new Integer(eventID), eventName);
        eventList.add(new Integer(eventID));
    }

    static {
        register(EVENT_EXPORT_AS_BITMAP, "Export as bitmap");
        register(EVENT_EXPORT_AS_EPS, "Export as EPS");
        register(EVENT_EXPORT_AS_DOT, "Export as DOT");
        register(EVENT_EXPORT_EVENTS_AS_TEXT, "Export events as text");

        register(EVENT_FIND_DIALOG, "Find dialog");
        register(EVENT_FIND_NEXT, "Find next");
        register(EVENT_FIND_PREVIOUS, "Find previous");
        register(EVENT_FIND_TEXT_AT_CARET, "Find text at caret");
        register(EVENT_FIND_USAGES, "Find usages");

        register(EVENT_GOTO_RULE, "Goto rule");
        register(EVENT_GOTO_DECLARATION, "Goto declaration");
        register(EVENT_GOTO_LINE, "Goto line");
        register(EVENT_GOTO_CHAR, "Goto character");
        register(EVENT_GOTO_BACK, "Goto back");
        register(EVENT_GOTO_FORWARD, "Goto forward");
        register(EVENT_GOTO_PREV_BRKPT, "Goto previous breakpoint");
        register(EVENT_GOTO_NEXT_BRKPT, "Goto next breakpoint");

        register(EVENT_SHOW_TOKEN_SD, "Show tokens syntax diagram");
        register(EVENT_SHOW_DECISION_DFA, "Show decision DFA");
        register(EVENT_SHOW_RULE_DEPENDENCY, "Show rule dependency");
        register(EVENT_INSERT_RULE_TEMPLATE, "Insert rule from template");
        register(EVENT_RULE_GROUP, "Group rule");
        register(EVENT_RULE_UNGROUP, "Ungroup rule");
        register(EVENT_CHECK_GRAMMAR, "Check grammar");

        register(EVENT_RENAME, "Rename");
        register(EVENT_REPLACE_LITERALS, "Replace literals");
        register(EVENT_REMOVE_LEFT_RECURSION, "Remove left recursion");
        register(EVENT_REMOVE_ALL_LEFT_RECURSION, "Remove all left recursion");
        register(EVENT_EXTRACT_RULE, "Extract rule");
        register(EVENT_INLINE_RULE, "Inline rule");
        register(EVENT_CONVERT_LITERALS_TO_SINGLE, "Convert literals to single quote");
        register(EVENT_CONVERT_LITERALS_TO_DOUBLE, "Convert literals to double quote");
        register(EVENT_CONVERT_LITERALS_TO_CSTYLE, "Convert literals to C-style quote");

        register(EVENT_GENERATE_CODE, "Generate code");
        register(EVENT_SHOW_PARSER_GENERATED_CODE, "Show parser code");
        register(EVENT_SHOW_LEXER_GENERATED_CODE, "Show lexer code");
        register(EVENT_SHOW_RULE_GENERATED_CODE, "Show rule code");

        register(EVENT_INTERPRETER_MENU, "Interpreter (menu)");
        register(EVENT_INTERPRETER_BUTTON, "Interpreter (button)");

        register(EVENT_LOCAL_DEBUGGER, "Debug");
        register(EVENT_LOCAL_DEBUGGER_BUILD, "Build and debug");
        register(EVENT_REMOTE_DEBUGGER, "Remote debug");

        register(EVENT_SCM_EDIT, "SCM edit");
        register(EVENT_SCM_ADD, "SCM add");
        register(EVENT_SCM_REVERT, "SCM revert");
        register(EVENT_SCM_DELETE, "SCM delete");
        register(EVENT_SCM_SUBMIT, "SCM submit");
        register(EVENT_SCM_SYNC, "SCM sync");

        register(EVENT_TOGGLE_SYNTAX_COLORING, "Toggle syntax coloring");
        register(EVENT_TOGGLE_SYNTAX_DIAGRAM, "Toggle syntax diagram");
        register(EVENT_TOGGLE_IDEAS, "Toggle ideas");
        register(EVENT_TOGGLE_RULE_SORT, "Toggle rule sort");

        register(EVENT_SHOW_PREFERENCES, "Show preferences");
        register(EVENT_SHOW_HELP, "Show help");
        register(EVENT_CHECK_FOR_UPDATES, "Check for updates");
        register(EVENT_SHOW_AUTO_COMPLETION_MENU, "Show auto-completion");
        register(EVENT_DROP_RULE, "Drop rule (tree)");
        register(EVENT_TOGGLE_SD_NFA, "Toggle syntax diagram/NFA");

        register(EVENT_DEBUGGER_STOP, "Debugger stop");
        register(EVENT_DEBUGGER_STEP_BACK, "Debugger step back");
        register(EVENT_DEBUGGER_STEP_FORWARD, "Debugger step forward");
        register(EVENT_DEBUGGER_FAST_FORWARD, "Debugger fast forward");
        register(EVENT_DEBUGGER_GOTO_START, "Debugger goto start");
        register(EVENT_DEBUGGER_GOTO_END, "Debugger goto end");
        register(EVENT_DEBUGGER_TOGGLE_INPUT_TOKENS, "Debugger toggle input tokens");
    }

    protected Map events = null;

    public static synchronized Statistics shared() {
        if(shared == null)
            shared = new Statistics();
        return shared;
    }

    public void reset() {
        if(events != null)
            events.clear();
        initDate();
    }

    public void initDate() {
        setFromDate(new Date().toString());
    }

    public void setFromDate(String date) {
        AWPrefs.getPreferences().setString(PREF_KEY_FROM_DATE, date);
    }

    public String getFromDate() {
        return AWPrefs.getPreferences().getString(PREF_KEY_FROM_DATE, null);
    }

    public void close() {
        AWPrefs.getPreferences().setObject(PREF_KEY, events);
    }

    public void recordEvent(int event) {
        setCount(event, getCount(event)+1);
    }

    protected void setCount(int event, int count) {
        getEvents().put(new Integer(event), new Integer(count));
    }

    protected int getCount(int event) {
        Integer count = (Integer)getEvents().get(new Integer(event));
        if(count == null)
            return 0;
        else
            return count.intValue();
    }

    protected synchronized Map getEvents() {
        if(events == null) {
            try {
                events = (Map)AWPrefs.getPreferences().getObject(PREF_KEY, null);
            } catch(Exception e) {
                events = null;
                System.err.println("Statistics: "+e);
            }
            if(events == null) {
                events = new HashMap();
                initDate();
            }
        }
        return events;
    }

    public String getRawString() {
        StringBuffer s = new StringBuffer();
        s.append(XJApplication.getAppVersionShort());
        s.append('\t');
        for (Iterator iterator = eventList.iterator(); iterator.hasNext();) {
            Integer key = (Integer) iterator.next();
            s.append(getCount(key.intValue()));
            if(iterator.hasNext())
                s.append('\t');
        }
        return s.toString();
    }

    public String getReadableString() {
        StringBuffer s = new StringBuffer();
        s.append("Version: ");
        s.append(XJApplication.getAppVersionShort());
        s.append('\n');

        for (Iterator iterator = eventList.iterator(); iterator.hasNext();) {
            Integer key = (Integer) iterator.next();

            s.append(eventNames.get(key));
            s.append(": ");
            s.append(getCount(key.intValue()));

            if(iterator.hasNext())
                s.append('\n');
        }

        return s.toString();
    }
}
