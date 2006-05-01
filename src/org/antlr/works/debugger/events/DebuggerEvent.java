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

package org.antlr.works.debugger.events;

import org.antlr.runtime.Token;

public class DebuggerEvent {

    public static final int NO_EVENT = -1;

    public static final int ALL = 0;
    public static final int NONE = 1;
    public static final int COMMENCE = 2;
    public static final int TERMINATE = 3;
    public static final int LOCATION = 4;
    public static final int CONSUME_TOKEN = 5;
    public static final int CONSUME_HIDDEN_TOKEN = 6;
    public static final int LT = 7;
    public static final int ENTER_RULE = 8;
    public static final int EXIT_RULE = 9;
    public static final int ENTER_SUBRULE = 10;
    public static final int EXIT_SUBRULE = 11;
    public static final int ENTER_DECISION = 12;
    public static final int EXIT_DECISION = 13;
    public static final int ENTER_ALT = 14;
    public static final int MARK = 15;
    public static final int REWIND = 16;
    public static final int BEGIN_BACKTRACK = 17;
    public static final int END_BACKTRACK = 18;
    public static final int BEGIN_RESYNC = 19;
    public static final int END_RESYNC = 20;
    public static final int NIL_NODE = 21;
    public static final int CREATE_NODE = 22;
    public static final int BECOME_ROOT = 23;
    public static final int ADD_CHILD = 24;
    public static final int SET_TOKEN_BOUNDARIES = 25;
    public static final int RECOGNITION_EXCEPTION = 26;

    public static DebuggerEvent createLocation(int line, int pos) {
        return new DebuggerEventLocation(line, pos);
    }

    public static DebuggerEvent createConsumeToken(Token token) {
        return new DebuggerEventConsumeToken(token);
    }

    public static DebuggerEvent createConsumeHiddenToken(Token token) {
        return new DebuggerEventConsumeHiddenToken(token);
    }

    public static DebuggerEvent createLT(int i, Token token) {
        return new DebuggerEventLT(i, token);
    }

    public static DebuggerEvent createEnterRule(String name) {
        return new DebuggerEventEnterRule(name);
    }

    public static DebuggerEvent createExitRule(String name) {
        return new DebuggerEventExitRule(name);
    }

    public static DebuggerEvent createEnterSubRule(int decision) {
        return new DebuggerEventEnterSubRule(decision);
    }

    public static DebuggerEvent createExitSubRule(int decision) {
        return new DebuggerEventExitSubRule(decision);
    }

    public static DebuggerEvent createEnterDecision(int decision) {
        return new DebuggerEventEnterDecision(decision);
    }

    public static DebuggerEvent createExitDecision(int decision) {
        return new DebuggerEventExitDecision(decision);
    }

    public static DebuggerEvent createEnterAlt(int alt) {
        return new DebuggerEventEnterAlt(alt);
    }

    public static DebuggerEvent createMark(int i) {
        return new DebuggerEventMark(i);
    }

    public static DebuggerEvent createRewind(int i) {
        return new DebuggerEventRewind(i);
    }

    public static DebuggerEvent createBeginBacktrack(int level) {
        return new DebuggerEventBeginBacktrack(level);
    }

    public static DebuggerEvent createEndBacktrack(int level, boolean successful) {
        return new DebuggerEventEndBacktrack(level, successful);
    }

    public static DebuggerEvent createRecognitionException(Exception e) {
        return new DebuggerEventRecognitionException(e);
    }

    public static DebuggerEvent createBeginResync() {
        return new DebuggerEvent(BEGIN_RESYNC);
    }

    public static DebuggerEvent createEndResync() {
        return new DebuggerEvent(END_RESYNC);
    }

    public static DebuggerEvent createCommence() {
        return new DebuggerEvent(COMMENCE);
    }

    public static DebuggerEvent createTerminate() {
        return new DebuggerEvent(TERMINATE);
    }

    public int type;

    public DebuggerEvent(int type) {
        this.type = type;
    }

    public static String getEventName(int type) {
        switch(type) {
            case NO_EVENT: return "-";
            case CONSUME_TOKEN: return "Consume token";
            case CONSUME_HIDDEN_TOKEN: return "Consume hidden token";
            case ENTER_RULE:    return "Enter rule";
            case EXIT_RULE:     return "Exit rule";
            case ENTER_SUBRULE: return "Enter subrule";
            case EXIT_SUBRULE:  return "Exit subrule";
            case ENTER_DECISION:    return "Enter decision";
            case EXIT_DECISION:    return "Exit decision";
            case ENTER_ALT:     return "Enter alternative";
            case LOCATION:      return "Location";
            case LT:            return "LT";
            case MARK:     return "Mark";
            case REWIND:     return "Rewind";
            case BEGIN_BACKTRACK:     return "Begin backtrack";
            case END_BACKTRACK:     return "End backtrack";
            case RECOGNITION_EXCEPTION: return "Recognition exception";
            case BEGIN_RESYNC:     return "Begin resync";
            case END_RESYNC:     return "End resync";
            case NIL_NODE: return "Nil node";
            case CREATE_NODE: return "Create node";
            case BECOME_ROOT: return "Become root";
            case ADD_CHILD: return "Add child";
            case SET_TOKEN_BOUNDARIES: return "Set token boundaries";
            case COMMENCE:    return "Commence";
            case TERMINATE:     return "Terminate";
            case ALL:     return "All";
        }
        return "?";
    }

    public String toString() {
        switch(type) {
            case BEGIN_RESYNC:     return "Begin resync";
            case END_RESYNC:     return "End resync";
            case COMMENCE:    return "Commence";
            case TERMINATE:     return "Terminate";
        }
        return super.toString();
    }
}
