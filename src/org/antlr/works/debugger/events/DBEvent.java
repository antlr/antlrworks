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

public class DBEvent {

    public static final int NO_EVENT = -1;

    public static final Integer negOne = -1;
    public static final Integer ints[] = new Integer[28];

    static {
        for(int i = 0, maxI = ints.length; i < maxI; i++) {
            ints[i] = i;
        }
    }

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
    public static final int ERROR_NODE = 27;

    private int eventType;
    private String grammarName;

    public DBEvent(int eventType) {
        this.eventType = eventType;
    }

    public static String getEventName(int type) {
        switch(type) {
            case NO_EVENT: return "-";
            case CONSUME_TOKEN: return "Consume";
            case CONSUME_HIDDEN_TOKEN: return "Consume hidden";
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
            case ERROR_NODE: return "Error node";
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

    public static Integer convertToInteger(int eventType) {
        Integer retInt;
        if(eventType < ints.length) {
            retInt = ints[eventType];
        } else if (eventType == -1) {
            retInt = negOne;
        } else {
            retInt = eventType;
        }

        return(retInt);
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getGrammarName() {
        return grammarName;
    }

    public void setGrammarName(String grammarName) {
        this.grammarName = grammarName;
    }

    public String toString() {
        switch(getEventType()) {
            case BEGIN_RESYNC:  return "Begin resync";
            case END_RESYNC:    return "End resync";
            case COMMENCE:      return "Commence";
            case TERMINATE:     return "Terminate";
        }
        return super.toString();
    }

}
