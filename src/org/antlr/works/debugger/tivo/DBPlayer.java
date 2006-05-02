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
import org.antlr.works.debugger.input.DBInputText;
import org.antlr.works.utils.Console;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class DBPlayer {

    protected Debugger debugger;
    protected DBInputText inputText;

    protected DBPlayerContextInfo contextInfo;
    protected Stack lookAheadTextStack;
    protected LookAheadText lastLookAheadText;

    protected int resyncing = 0;
    protected int eventPlayedCount = 0;

    public DBPlayer(Debugger debugger, DBInputText inputText) {
        this.debugger = debugger;
        this.inputText = inputText;
        contextInfo = new DBPlayerContextInfo();
        lookAheadTextStack = new Stack();
    }

    public void close() {
        inputText.close();        
    }

    public void toggleInputTextTokensBox() {
        inputText.setDrawTokensBox(!inputText.isDrawTokensBox());
    }

    /** Called by playEnterDecision() */

    public void pushLookAheadText(Object id) {
        lookAheadTextStack.push(new LookAheadText(id));
    }

    /** Called by playExitDecision() */

    public void popLookAheadText(Object id) {
        if(lookAheadTextStack.empty()) {
            debugger.getConsole().println("Lookahead text stack is empty while trying to popup object id "+id, Console.LEVEL_WARNING);
            return;
        }

        LookAheadText lat = (LookAheadText)lookAheadTextStack.peek();
        if(lat.id.equals(id)) {
            lastLookAheadText = (LookAheadText)lookAheadTextStack.pop();
        } else
            debugger.getConsole().println("The top-of-stack LookAheadText doesn't correspond to id "+id+" ("+lat.id+")", Console.LEVEL_WARNING);
    }

    /** Called by playRewind() */

    public void rewindLookAheadText() {
        if(lastLookAheadText != null) {
            if(lastLookAheadText.enable) {
                lastLookAheadText.rewind();
                lastLookAheadText.disable();
            }
            lastLookAheadText = null;
        }
    }

    public LookAheadText getLookAheadText() {
        if(lookAheadTextStack.empty())
            return null;
        else
            return (LookAheadText)lookAheadTextStack.peek();
    }

    public synchronized void resetPlayEvents(boolean first) {
        debugger.resetGUI();

        // Only reset the input text the first time
        // the events are reset (when the debugger starts).
        // Then, keep rewinding the input text so already received
        // tokens are displayed
        if(first)
            inputText.reset();
        else
            inputText.rewindAll();

        contextInfo.clear();

        lastLookAheadText = null;
        lookAheadTextStack.clear();

        resyncing = 0;
        eventPlayedCount = 0;
    }

    public void playEvents(List events, boolean reset) {
        if(reset)
            resetPlayEvents(false);

        for(int i=eventPlayedCount; i<events.size(); i++) {
            try {
                DBEvent event = (DBEvent)events.get(i);
                boolean lastEvent = i == events.size()-1;
                playEvent(event, lastEvent);
            } catch(Exception e) {
                debugger.getConsole().print(e);
            }
        }
        eventPlayedCount = events.size();        
    }

    public void playEvent(DBEvent event, boolean lastEvent) {
        switch(event.type) {
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
                if(lookAheadTextStack.size() > 0) {
                    debugger.getConsole().println("Lookahead text stack not empty", Console.LEVEL_WARNING);
                }
                break;
        }

        debugger.addEvent(event, contextInfo);

        if(lastEvent) {
            playLocation();
        }
    }

    public void playEnterRule(DBEventEnterRule event) {
        debugger.pushRule(event.name, lastLocationLine, lastLocationPos);
    }

    public void playExitRule(DBEventExitRule event) {
        debugger.popRule(event.name);
    }

    public void playEnterSubrule(DBEventEnterSubRule event) {
        contextInfo.enterSubrule(event.decision);
    }

    public void playExitSubrule(DBEventExitSubRule event) {
        contextInfo.exitSubrule();
    }

    public void playEnterDecision(DBEventEnterDecision event) {
        contextInfo.enterDecision(event.decision);
        pushLookAheadText(new Integer(event.decision));
    }

    public void playExitDecision(DBEventExitDecision event) {
        contextInfo.exitDecision();
        popLookAheadText(new Integer(event.decision));
    }

    public void playEnterAlt(DBEventEnterAlt event) {
        /* Currently ignored */
    }

    public void playLT(DBEventLT event) {
        if(getLookAheadText() != null) {
            getLookAheadText().LT(event.index, event.token);
        }
    }

    public void playConsumeToken(DBEventConsumeToken event) {
        playConsumeToken(event.token, false);
    }

    public void playConsumeToken(DBEventConsumeHiddenToken event) {
        playConsumeToken(event.token, true);
    }

    public void playConsumeToken(Token token, boolean hidden) {
        if(resyncing > 0) {
            inputText.consumeToken(token, DBInputText.TOKEN_DEAD);
            return;
        }

        if(getLookAheadText() != null) {
            getLookAheadText().consumeToken(token, hidden);
            // If backtracking, also add the token to the parse tree
            if(contextInfo.isBacktracking())
                debugger.addToken(token);
            return;
        }

        // Build the parse tree only for visible token
        if(!hidden) {
            debugger.addToken(token);
        }

        inputText.consumeToken(token, hidden?DBInputText.TOKEN_HIDDEN:DBInputText.TOKEN_NORMAL);
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
        inputText.setLocation(lastLocationLine, lastLocationPos);
    }

    public void playLocation() {
        debugger.restorePreviousGrammarAttributeSet();

        int index = debugger.computeAbsoluteGrammarIndex(lastLocationLine, lastLocationPos);
        if(index < 0)
            return;

        debugger.markLocationInGrammar(index);
    }

    public void playMark(DBEventMark event) {
        contextInfo.mark(event.id);
        if(getLookAheadText() != null) {
            getLookAheadText().setEventMark(event.id);
        }
    }

    public void playRewind(DBEventRewind event) {
        rewindLookAheadText();

        contextInfo.rewind();
        if(getLookAheadText() != null) {
            getLookAheadText().setEventRewind(event.id);
        }
    }

    public void playBeginBacktrack(DBEventBeginBacktrack event) {
        contextInfo.beginBacktrack(event.level);

        /* Tell the debugger about the backtracking so the parse
        tree coloring can be properly done */
        debugger.beginBacktrack(event.level);
    }

    public void playEndBacktrack(DBEventEndBacktrack event) {
        contextInfo.endBacktrack();

        /* Tell the debugger about the backtracking so the parse
        tree coloring can be properly done */
        debugger.endBacktrack(event.level, event.successful);
    }

    public void playRecognitionException(DBEventRecognitionException event) {
        debugger.addException(event.e);
    }

    public void playBeginResync() {
        resyncing++;
    }

    public void playEndResync() {
        resyncing--;
    }

    public void playNilNode(DBEventNilNode event) {
        debugger.astNilNode(event.id);
    }

    public void playCreateNode(DBEventCreateNode event) {
        debugger.astCreateNode(event.id, inputText.getTokenInfoAtIndex(event.tokenIndex).token);
    }

    public void playBecomeRoot(DBEventBecomeRoot event) {
        debugger.astBecomeRoot(event.newRootID, event.oldRootID);
    }

    public void playAddChild(DBEventAddChild event) {
        debugger.astAddChild(event.rootID, event.childID);
    }

    public void playSetTokenBoundaries(DBEventSetTokenBoundaries event) {
        debugger.astSetTokenBoundaries(event.id, event.startIndex, event.stopIndex);
    }

    protected class LookAheadText {

        public int start;
        public Object id;

        protected List ltTokens;

        protected boolean enable;
        protected boolean mark;

        public LookAheadText(Object id) {
            this.start = inputText.getCursorIndex();
            this.id = id;

            ltTokens = new ArrayList();
            enable = false;
            mark = false;

            enable();
        }

        public void enable() {
            if(enable)
                debugger.getConsole().println("Enabling an already enabled LookAheadText", Console.LEVEL_WARNING);

            enable = true;
        }

        public void disable() {
            if(!enable)
                debugger.getConsole().println("Disabling an already disabled LookAheadText", Console.LEVEL_WARNING);

            enable = false;
        }

        public boolean existsToken(Token token) {
            for (Iterator iterator = ltTokens.iterator(); iterator.hasNext();) {
                Token t = (Token) iterator.next();
                if(t.getTokenIndex() == token.getTokenIndex())
                    return true;
            }
            return false;
        }

        public void consumeToken(Token token, boolean hidden) {
            if(!mark)
                inputText.consumeToken(token, hidden?DBInputText.TOKEN_HIDDEN:DBInputText.TOKEN_NORMAL);
        }

        public void LT(int index, Token token) {
            if(!enable)
                return;

            // @todo what if between mark and rewind ? Use token index instead ?
            // Rewind when LT 1 and only if not between mark/rewind events
            if(index == 1 && !mark)
                rewind();

            inputText.doLT(token);
        }

        public void setEventMark(int id) {
            mark = true;
        }

        public void setEventRewind(int id) {
            mark = false;
        }

        public void rewind() {
            inputText.rewind(start);
        }
    }

}
