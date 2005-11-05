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

package org.antlr.works.debugger;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;

public class DebuggerEvent {

    public static final int LOCATION = 0;
    public static final int CONSUME_TOKEN = 1;
    public static final int LT = 2;
    public static final int RECOGNITION_EXCEPTION = 3;
    public static final int ALL = 4;
    public static final int NONE = 5;
    public static final int CONSUME_HIDDEN_TOKEN = 6;
    public static final int ENTER_RULE = 7;
    public static final int EXIT_RULE = 8;
    public static final int ENTER_SUBRULE = 9;
    public static final int EXIT_SUBRULE = 10;
    public static final int ENTER_DECISION = 11;
    public static final int EXIT_DECISION = 12;
    public static final int ENTER_ALT = 13;
    public static final int MARK = 14;
    public static final int REWIND = 15;
    public static final int BEGIN_RESYNC = 16;
    public static final int END_RESYNC = 17;
    public static final int COMMENCE = 18;
    public static final int TERMINATE = 19;

    protected int type;
    protected String s;
    protected int int1;
    protected int int2;
    protected Token token;
    protected RecognitionException exception;

    public DebuggerEvent(int type) {
        this.type = type;
    }

    public DebuggerEvent(int type, String arg) {
        this.type = type;
        this.s = arg;
    }

    public DebuggerEvent(int type, int arg) {
        this.type = type;
        this.int1 = arg;
    }

    public DebuggerEvent(int type, int arg, Token t) {
        this.type = type;
        this.int1 = arg;
        this.token = t;
    }

    public DebuggerEvent(int type, int arg1, int arg2) {
        this.type = type;
        this.int1 = arg1;
        this.int2 = arg2;
    }

    public DebuggerEvent(int type, Token token) {
        this.type = type;
        this.token = token;
    }

    public DebuggerEvent(int type, RecognitionException e) {
        this.type = type;
        this.exception = e;
    }

    public static String getEventName(int type) {
        switch(type) {
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
            case RECOGNITION_EXCEPTION: return "Recognition exception";
            case BEGIN_RESYNC:     return "Begin resync";
            case END_RESYNC:     return "End resync";
            case COMMENCE:    return "Commence";
            case TERMINATE:     return "Terminate";
            case ALL:     return "All";
        }
        return "?";
    }

    public String toString() {
        switch(type) {
            case CONSUME_TOKEN: return "Consume "+token;
            case CONSUME_HIDDEN_TOKEN: return "Consume hidden "+token;
            case ENTER_RULE:    return "Enter rule "+s;
            case EXIT_RULE:     return "Exit rule "+s;
            case ENTER_SUBRULE: return "Enter subrule "+int1;
            case EXIT_SUBRULE:  return "Exit subrule "+int1;
            case ENTER_DECISION:    return "Enter decision "+int1;
            case EXIT_DECISION:    return "Exit decision "+int1;
            case ENTER_ALT:     return "Enter alt "+int1;
            case LOCATION:      return "Location "+int1+","+int2;
            case LT:            return "LT "+int1+" "+token.getText();
            case MARK:     return "Mark "+int1;
            case REWIND:     return "Rewind "+int1;
            case RECOGNITION_EXCEPTION: return "Recognition exception "+exception;
            case BEGIN_RESYNC:     return "Begin resync";
            case END_RESYNC:     return "End resync";
            case COMMENCE:    return "Commence";
            case TERMINATE:     return "Terminate";
        }
        return super.toString();
    }
}
