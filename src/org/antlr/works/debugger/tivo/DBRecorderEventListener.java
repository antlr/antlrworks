package org.antlr.works.debugger.tivo;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.debug.DebugEventListener;
import org.antlr.works.debugger.events.DBEvent;
import org.antlr.works.debugger.events.DBEventFactory;
import org.antlr.works.debugger.tree.DBTreeToken;
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

public class DBRecorderEventListener implements DebugEventListener {

    public DBRecorder recorder;

    public DBRecorderEventListener(DBRecorder recorder) {
        this.recorder = recorder;
    }

    public void event(DBEvent event) {
        recorder.listenerEvent(event);
    }

    public void commence() {
        event(DBEventFactory.createCommence());
    }

    public void terminate() {
        event(DBEventFactory.createTerminate());
    }

    public void enterRule(String ruleName) {
        event(DBEventFactory.createEnterRule(ruleName));
    }

    public void exitRule(String ruleName) {
        event(DBEventFactory.createExitRule(ruleName));
    }

    public void enterSubRule(int decisionNumber) {
        event(DBEventFactory.createEnterSubRule(decisionNumber));
    }

    public void exitSubRule(int decisionNumber) {
        event(DBEventFactory.createExitSubRule(decisionNumber));
    }

    public void enterDecision(int decisionNumber) {
        event(DBEventFactory.createEnterDecision(decisionNumber));
    }

    public void exitDecision(int decisionNumber) {
        event(DBEventFactory.createExitDecision(decisionNumber));
    }

    public void enterAlt(int alt) {
        event(DBEventFactory.createEnterAlt(alt));
    }

    public void location(int line, int pos) {
        event(DBEventFactory.createLocation(line, pos));
    }

    public void consumeToken(Token token) {
        event(DBEventFactory.createConsumeToken(token));
    }

    public void consumeHiddenToken(Token token) {
        event(DBEventFactory.createConsumeHiddenToken(token));
    }

    public void LT(int i, Token token) {
        event(DBEventFactory.createLT(i, token));
    }

    public void mark(int i) {
        event(DBEventFactory.createMark(i));
    }

    public void rewind(int i) {
        event(DBEventFactory.createRewind(i));
    }

    public void rewind() {
        event(DBEventFactory.createRewind());
    }

    public void beginBacktrack(int level) {
        event(DBEventFactory.createBeginBacktrack(level));
    }

    public void endBacktrack(int level, boolean successful) {
        event(DBEventFactory.createEndBacktrack(level, successful));
    }

    public void recognitionException(RecognitionException e) {
        event(DBEventFactory.createRecognitionException(e));
    }

    public void beginResync() {
        event(DBEventFactory.createBeginResync());
    }

    public void endResync() {
        event(DBEventFactory.createEndResync());
    }

    public void semanticPredicate(boolean result, String predicate) {
        /** Currently ignored */
    }

    /** AST events */

    public void nilNode(int ID) {
        event(DBEventFactory.createNilNode(ID));
    }

    public void createNode(int ID, String text, int type) {
        event(DBEventFactory.createCreateNode(ID, text, type));
    }

    public void createNode(int ID, int tokenIndex) {
        event(DBEventFactory.createCreateNode(ID, tokenIndex));
    }

    public void becomeRoot(int newRootID, int oldRootID) {
        event(DBEventFactory.createBecomeRoot(newRootID, oldRootID));
    }

    public void addChild(int rootID, int childID) {
        event(DBEventFactory.createAddChild(rootID, childID));
    }

    public void setTokenBoundaries(int ID, int tokenStartIndex, int tokenStopIndex) {
        event(DBEventFactory.createSetTokenBoundaries(ID, tokenStartIndex, tokenStopIndex));
    }

    /** Tree parsing */

    public void consumeNode(int ID, String text, int type) {
        /** Create a special kind of token holding information about the tree node. This allow
         * us to use the same method for token parser and tree parser.
         */
        event(DBEventFactory.createConsumeToken(new DBTreeToken(ID, text, type)));
    }

    public void LT(int i, int ID, String text, int type) {
        /** See consumeNode() comment */
        event(DBEventFactory.createLT(i, new DBTreeToken(ID, text, type)));
    }

}
