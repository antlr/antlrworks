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

package org.antlr.works.debugger.tivo;

import org.antlr.runtime.Token;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.debugger.events.*;
import org.antlr.works.debugger.input.DBInputProcessor;
import org.antlr.works.debugger.input.DBInputTextTokenInfo;

import java.util.List;
import java.util.Stack;

public class DBPlayer {

    protected Debugger debugger;
    protected DBInputProcessor processor;

    protected DBPlayerContextInfo contextInfo;
    protected Stack<Integer> markStack;

    protected int resyncing = 0;
    protected int eventPlayedCount = 0;

    public DBPlayer(Debugger debugger) {
        this.debugger = debugger;
        contextInfo = new DBPlayerContextInfo();
        markStack = new Stack<Integer>();
    }

    public void setInputBuffer(DBInputProcessor processor) {
        this.processor = processor;
    }

    public DBPlayerContextInfo getContextInfo() {
        return contextInfo;
    }
    
    public synchronized void resetPlayEvents(boolean first) {
        debugger.resetGUI();

        /** Only reset the input text the first time
            the events are reset (when the debugger starts).
            Then, keep rewinding the input text so already received
            tokens are displayed */
        if(first)
            processor.reset();
        else
            processor.rewindAll();

        contextInfo.clear();
        markStack.clear();

        resyncing = 0;
        eventPlayedCount = 0;
    }

    public void playEvents(List events, boolean reset) {
        if(reset)
            resetPlayEvents(false);

        for(int i=eventPlayedCount; i<events.size(); i++) {
            DBEvent event = (DBEvent)events.get(i);

            try {
                playEvent(event);
            } catch(Exception e) {
                debugger.getConsole().print(e);
            }

            debugger.addEvent(event, contextInfo);
            if(i == events.size()-1) {
                // Last event, play the location
                playLocation();
            }
        }
        eventPlayedCount = events.size();        
    }

    public void playEvent(DBEvent event) {
        switch(event.getEventType()) {
            case DBEvent.ENTER_RULE:
                playEnterRule((DBEventEnterRule)event);
                break;

            case DBEvent.EXIT_RULE:
                playExitRule((DBEventExitRule)event);
                break;

            case DBEvent.ENTER_SUBRULE:
                playEnterSubrule((DBEventEnterSubRule)event);
                break;

            case DBEvent.EXIT_SUBRULE:
                playExitSubrule((DBEventExitSubRule)event);
                break;

            case DBEvent.ENTER_DECISION:
                playEnterDecision((DBEventEnterDecision)event);
                break;

            case DBEvent.EXIT_DECISION:
                playExitDecision((DBEventExitDecision)event);
                break;

            case DBEvent.ENTER_ALT:
                playEnterAlt((DBEventEnterAlt)event);
                break;

            case DBEvent.LT:
                playLT((DBEventLT)event);
                break;

            case DBEvent.CONSUME_TOKEN:
                playConsumeToken((DBEventConsumeToken)event);
                break;

            case DBEvent.CONSUME_HIDDEN_TOKEN:
                playConsumeToken((DBEventConsumeHiddenToken)event);
                break;

            case DBEvent.LOCATION:
                playLocation((DBEventLocation)event);
                break;

            case DBEvent.MARK:
                playMark((DBEventMark)event);
                break;

            case DBEvent.REWIND:
                playRewind((DBEventRewind)event);
                break;

            case DBEvent.BEGIN_BACKTRACK:
                playBeginBacktrack((DBEventBeginBacktrack)event);
                break;

            case DBEvent.END_BACKTRACK:
                playEndBacktrack((DBEventEndBacktrack)event);
                break;

            case DBEvent.RECOGNITION_EXCEPTION:
                playRecognitionException((DBEventRecognitionException)event);
                break;

            case DBEvent.BEGIN_RESYNC:
                playBeginResync();
                break;

            case DBEvent.END_RESYNC:
                playEndResync();
                break;

            case DBEvent.NIL_NODE:
                playNilNode((DBEventNilNode)event);
                break;

            case DBEvent.CREATE_NODE:
                playCreateNode((DBEventCreateNode)event);
                break;

            case DBEvent.BECOME_ROOT:
                playBecomeRoot((DBEventBecomeRoot)event);
                break;

            case DBEvent.ADD_CHILD:
                playAddChild((DBEventAddChild)event);
                break;

            case DBEvent.SET_TOKEN_BOUNDARIES:
                playSetTokenBoundaries((DBEventSetTokenBoundaries)event);
                break;

            case DBEvent.TERMINATE:
                break;
        }
    }

    public void playEnterRule(DBEventEnterRule event) {
        debugger.playerPushRule(event.name);
        processor.removeAllLT();
    }

    public void playExitRule(DBEventExitRule event) {
        debugger.playerPopRule(event.name);
        processor.removeAllLT();
    }

    public void playEnterSubrule(DBEventEnterSubRule event) {
        contextInfo.enterSubrule(event.decision);
        processor.removeAllLT();
    }

    public void playExitSubrule(DBEventExitSubRule event) {
        contextInfo.exitSubrule();
        processor.removeAllLT();
    }

    public void playEnterDecision(DBEventEnterDecision event) {
        contextInfo.enterDecision(event.decision);
        processor.removeAllLT();
    }

    public void playExitDecision(DBEventExitDecision event) {
        contextInfo.exitDecision();
        processor.removeAllLT();
    }

    public void playEnterAlt(DBEventEnterAlt event) {
        /* Currently ignored */
    }

    public void playLT(DBEventLT event) {
        /* Ignore EOF LT */
        if(event.token.getType() == Token.EOF)
            return;

        /* Ignore LT with negative index (i.e. LT(-1)) */
        if(event.index < 0)
            return;

        /* Ignore LT if they are not part of a decision. contextinfo returns
         -1 if the stack of decision is empty. */
        if(contextInfo.getDecision() == -1)
            return;

        processor.LT(event.token);
    }

    public void playConsumeToken(DBEventConsumeToken event) {
        playConsumeToken(event.token, false);
    }

    public void playConsumeToken(DBEventConsumeHiddenToken event) {
        playConsumeToken(event.token, true);
    }

    public void playConsumeToken(Token token, boolean hidden) {
        if(resyncing > 0) {
            processor.consumeToken(token, DBInputProcessor.TOKEN_DEAD);
            return;
        }

        /* If backtracking add token only */
        if(contextInfo.isBacktracking()) {
            debugger.playerConsumeToken(token);
            return;
        }

        /* Ignore consume token between mark/rewind */
        if(!markStack.isEmpty())
            return;

        /* Add only visible token */
        if(!hidden)
            debugger.playerConsumeToken(token);

        /* Consume the token */
        processor.consumeToken(token, hidden?DBInputProcessor.TOKEN_HIDDEN:DBInputProcessor.TOKEN_NORMAL);
    }

    protected int lastLocationLine;
    protected int lastLocationPos;

    public void playLocation(DBEventLocation event) {
        // Remember the last position in order to display
        // it when the events are all consumed. This allows
        // to remove the fast backward/forward movement of the cursor
        // in the grammar (not needed)
        lastLocationLine = event.line;
        lastLocationPos = event.pos;

        debugger.playerSetLocation(lastLocationLine, lastLocationPos);
        processor.setLocation(lastLocationLine, lastLocationPos);
    }

    public void playLocation() {
        debugger.resetMarkLocationInGrammar();

        int index = debugger.computeAbsoluteGrammarIndex(lastLocationLine, lastLocationPos);
        if(index < 0)
            return;

        debugger.markLocationInGrammar(index);
    }

    public void playMark(DBEventMark event) {
        contextInfo.mark(event.id);
        markStack.push(processor.getCurrentTokenIndex());
    }

    public void playRewind(DBEventRewind event) {
        processor.rewind(markStack.peek());
        if(!event.rewindToLastMark()) {
            markStack.pop();
            contextInfo.rewind();
        }
    }

    public void playBeginBacktrack(DBEventBeginBacktrack event) {
        contextInfo.beginBacktrack(event.level);

        /* Tell the debugger about the backtracking so the parse
        tree coloring can be properly done */
        debugger.playerBeginBacktrack(event.level);
    }

    public void playEndBacktrack(DBEventEndBacktrack event) {
        contextInfo.endBacktrack();

        /* Tell the debugger about the backtracking so the parse
        tree coloring can be properly done */
        debugger.playerEndBacktrack(event.level, event.successful);
    }

    public void playRecognitionException(DBEventRecognitionException event) {
        debugger.playerRecognitionException(event.e);
    }

    public void playBeginResync() {
        resyncing++;
    }

    public void playEndResync() {
        resyncing--;
    }

    public void playNilNode(DBEventNilNode event) {
        debugger.playerNilNode(event.id);
    }

    public void playCreateNode(DBEventCreateNode event) {
        if(event.tokenIndex == -1) {
            /** Imaginary token. Use the 'text' and 'type' info instead. */
            debugger.playerCreateNode(event.id, event.text, event.type);
        } else {
            DBInputTextTokenInfo info = processor.getTokenInfoAtTokenIndex(event.tokenIndex);
            if(info == null)
                debugger.getConsole().println("No token info for token index "+event.tokenIndex);
            else
                debugger.playerCreateNode(event.id, info.token);
        }
    }

    public void playBecomeRoot(DBEventBecomeRoot event) {
        debugger.playerBecomeRoot(event.newRootID, event.oldRootID);
    }

    public void playAddChild(DBEventAddChild event) {
        debugger.playerAddChild(event.rootID, event.childID);
    }

    public void playSetTokenBoundaries(DBEventSetTokenBoundaries event) {
        debugger.playerSetTokenBoundaries(event.id, event.startIndex, event.stopIndex);
    }

}
