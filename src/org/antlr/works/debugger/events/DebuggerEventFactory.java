package org.antlr.works.debugger.events;

import org.antlr.runtime.Token;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
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

public class DebuggerEventFactory {

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
        return new DebuggerEvent(DebuggerEvent.BEGIN_RESYNC);
    }

    public static DebuggerEvent createEndResync() {
        return new DebuggerEvent(DebuggerEvent.END_RESYNC);
    }

    public static DebuggerEvent createCommence() {
        return new DebuggerEvent(DebuggerEvent.COMMENCE);
    }

    public static DebuggerEvent createTerminate() {
        return new DebuggerEvent(DebuggerEvent.TERMINATE);
    }

    public static DebuggerEvent createNilNode(int id) {
        return new DebuggerEventNilNode(id);
    }

    public static DebuggerEvent createCreateNode(int id, int tokenIndex) {
        return new DebuggerEventCreateNode(id, tokenIndex);
    }

    public static DebuggerEvent createBecomeRoot(int newRootID, int oldRootID) {
        return new DebuggerEventBecomeRoot(newRootID, oldRootID);
    }

    public static DebuggerEvent createAddChild(int rootID, int childID) {
        return new DebuggerEventAddChild(rootID, childID);
    }

    public static DebuggerEvent createSetTokenBoundaries(int id, int startIndex, int stopIndex) {
        return new DebuggerEventSetTokenBoundaries(id, startIndex, stopIndex);
    }

}
