package org.antlr.works.debugger.events;

import org.antlr.runtime.Token;
import org.antlr.runtime.RecognitionException;
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

public class DBEventFactory {

    public static DBEvent createLocation(int line, int pos) {
        return new DBEventLocation(line, pos);
    }

    public static DBEvent createConsumeToken(Token token) {
        return new DBEventConsumeToken(token);
    }

    public static DBEvent createConsumeHiddenToken(Token token) {
        return new DBEventConsumeHiddenToken(token);
    }

    public static DBEvent createLT(int i, Token token) {
        return new DBEventLT(i, token);
    }

    public static DBEvent createEnterRule(String grammarFileName, String name) {
        return new DBEventEnterRule(grammarFileName, name);
    }

    public static DBEvent createExitRule(String grammarFileName, String name) {
        return new DBEventExitRule(grammarFileName, name);
    }

    public static DBEvent createEnterSubRule(int decision) {
        return new DBEventEnterSubRule(decision);
    }

    public static DBEvent createExitSubRule(int decision) {
        return new DBEventExitSubRule(decision);
    }

    public static DBEvent createEnterDecision(int decision) {
        return new DBEventEnterDecision(decision);
    }

    public static DBEvent createExitDecision(int decision) {
        return new DBEventExitDecision(decision);
    }

    public static DBEvent createEnterAlt(int alt) {
        return new DBEventEnterAlt(alt);
    }

    public static DBEvent createMark(int i) {
        return new DBEventMark(i);
    }

    public static DBEvent createRewind(int i) {
        return new DBEventRewind(i);
    }

    public static DBEvent createRewind() {
        return new DBEventRewind();
    }

    public static DBEvent createBeginBacktrack(int level) {
        return new DBEventBeginBacktrack(level);
    }

    public static DBEvent createEndBacktrack(int level, boolean successful) {
        return new DBEventEndBacktrack(level, successful);
    }

    public static DBEvent createRecognitionException(RecognitionException e) {
        return new DBEventRecognitionException(e);
    }

    public static DBEvent createBeginResync() {
        return new DBEvent(DBEvent.BEGIN_RESYNC);
    }

    public static DBEvent createEndResync() {
        return new DBEvent(DBEvent.END_RESYNC);
    }

    public static DBEvent createCommence() {
        return new DBEvent(DBEvent.COMMENCE);
    }

    public static DBEvent createTerminate() {
        return new DBEvent(DBEvent.TERMINATE);
    }

	public static DBEvent createNilNode(int id) {
		return new DBEventNilNode(id);
	}

	public static DBEvent createErrorNode(int id, String text, int type) {
		return new DBEventErrorNode(id, text, type);
	}

    public static DBEvent createCreateNode(int id, int tokenIndex) {
        return new DBEventCreateNode(id, tokenIndex);
    }

    public static DBEvent createCreateNode(int id, String text, int type) {
        return new DBEventCreateNode(id, text, type);
    }

    public static DBEvent createBecomeRoot(int newRootID, int oldRootID) {
        return new DBEventBecomeRoot(newRootID, oldRootID);
    }

    public static DBEvent createAddChild(int rootID, int childID) {
        return new DBEventAddChild(rootID, childID);
    }

    public static DBEvent createSetTokenBoundaries(int id, int startIndex, int stopIndex) {
        return new DBEventSetTokenBoundaries(id, startIndex, stopIndex);
    }

}
