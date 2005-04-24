package org.antlr.works.debugger;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.debug.DebugEventListener;
import org.antlr.runtime.debug.RemoteDebugEventSocketListener;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class DebuggerRecorder {

    public static final int STATUS_STOPPED = 0;
    public static final int STATUS_STOPPING = 1;
    public static final int STATUS_LAUNCHING = 2;
    public static final int STATUS_RUNNING = 3;

    protected Debugger debugger;
    protected int status = STATUS_STOPPED;
    protected boolean debuggerStop = false;

    protected ArrayList events;
    protected int position;
    protected int breakType = DebuggerEvent.NONE;

    protected EventListener eventListener;
    protected RemoteDebugEventSocketListener listener;

    protected boolean debuggerReceivedTerminateEvent;

    public DebuggerRecorder(Debugger debugger) {
        this.debugger = debugger;
        reset();
    }

    public synchronized void reset() {
        if(events == null)
            events = new ArrayList();
        else
            events.clear();
        position = -1;
    }

    public synchronized void addEvent(DebuggerEvent event) {
        events.add(event);
        setPositionToEnd();
    }

    public synchronized DebuggerEvent getEvent() {
        if(position<0 || position>=events.size())
            return null;
        else
            return (DebuggerEvent)events.get(position);
    }

    public synchronized DebuggerEvent getLastEvent() {
        return (DebuggerEvent)events.get(events.size()-1);
    }
    
    public synchronized List getCurrentEvents() {
        if(events.size() == 0)
            return events;

        int toIndex = position+1;
        if(toIndex >= events.size())
            toIndex = events.size();

        // Note: clone the list first in order to return
        // a sublist that can be modified concurrently of the
        // events list.
        // Note that toIndex is exclusive for subList();
        return ((List)events.clone()).subList(0, toIndex);
    }

    public void setPositionToEnd() {
        position = events.size()-1;
    }

    public void setBreaksOnEventType(int breakType) {
        this.breakType = Math.max(breakType, DebuggerEvent.NONE);
    }

    public int getBreaksEventType() {
        return breakType;
    }

    public boolean isOnBreakEvent() {
        if(breakType == DebuggerEvent.NONE)
            return false;

        if(breakType == DebuggerEvent.ALL)
            return true;

        DebuggerEvent event = getEvent();
        if(event == null)
            return false;

        if(event.type == DebuggerEvent.CONSUME_TOKEN && breakType == DebuggerEvent.CONSUME_TOKEN) {
            // Breaks only on consume token from channel 0
            return event.token.getChannel() == Token.DEFAULT_CHANNEL;
        } else
            return event.type == breakType;
    }

    public synchronized void setStatus(int status) {
        this.status = status;
        debugger.recorderStatusDidChange();
    }

    public synchronized int getStatus() {
        return status;
    }

    public void stepBackward() {
        if(stepMove(-1))
            playEvents();
    }

    public void stepForward() {
        if(stepMove(1))
            playEvents();
        else
            threadNotify();
    }

    public boolean stepMove(int direction) {
        position += direction;
        if(position<0) {
            position = 0;
            return false;
        }
        if(position >= events.size()) {
            position = events.size()-1;
            return false;
        }

        DebuggerEvent event;
        while((event = getEvent()) != null) {
            if(isOnBreakEvent())
                break;
            position += direction;
        }
        if(event == null)
            position -= direction;

        return event != null;
    }

    public void goToStart() {
        position = 0;
        playEvents();
    }

    public void goToEnd() {
        setPositionToEnd();
        playEvents();
    }

    public boolean start(CharStream input, String address, int port) {
        eventListener = new EventListener(input);

        boolean connected = false;
        int retryCount = 4;
        while(!connected && retryCount-- > 0) {
            listener = null;
            try {
                listener = new RemoteDebugEventSocketListener(eventListener, address, port);
            } catch (IOException e) {
                listener = null;
            }

            if(listener != null) {
                connected = true;
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }

        if(!connected) {
            setStatus(STATUS_STOPPED);
            return false;
        } else {
            debuggerStop = false;
            setStatus(STATUS_LAUNCHING);

            debuggerReceivedTerminateEvent = false;

            reset();
            listener.start();

            return true;
        }
    }

    public synchronized void stop() {
        setStatus(STATUS_STOPPING);
        debuggerStop = true;
        notify();

        if(debuggerReceivedTerminateEvent) {
            forceStop();
        }
    }

    public void forceStop() {
        setStatus(STATUS_STOPPED);
        debugger.recorderDidStop();
    }

    public void eventOccurred(DebuggerEvent event) {
        switch(getStatus()) {
            case STATUS_LAUNCHING:
                setStatus(STATUS_RUNNING);
                break;

            case STATUS_STOPPING:
                if(event.type == DebuggerEvent.TERMINATE || debuggerReceivedTerminateEvent) {
                    forceStop();
                }
                break;
        }

        if(getStatus() == STATUS_RUNNING) {
            if(event.type == DebuggerEvent.TERMINATE) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        playEvents();
                    }
                });
                debuggerReceivedTerminateEvent = true;
            }

            if(event.type == DebuggerEvent.LOCATION) {
                if(debugger.isBreakpointAtLine(event.int1-1)) {
                    breaksOnEvent();
                    return;
                }
            }

            if(event.type == DebuggerEvent.COMMENCE || isOnBreakEvent()) {
                breaksOnEvent();
                return;
            }
        }
    }

    public synchronized void threadNotify() {
        notify();
    }

    public synchronized void breaksOnEvent() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                playEvents();
            }
        });

        try {
            wait();
        } catch (InterruptedException e) {
            System.err.println("recorderThreadBreaksOnEvent: interrupted");
        }
    }

    protected synchronized void playEvents() {
        debugger.playEvents(getCurrentEvents());
    }

    protected class EventListener implements DebugEventListener {

        public CharStream input;

        public EventListener(CharStream input) {
            this.input = input;
        }

        public void event(DebuggerEvent event) {
            addEvent(event);
            DebuggerRecorder.this.eventOccurred(event);
        }

        public void enterRule(String ruleName) {
            event(new DebuggerEvent(DebuggerEvent.ENTER_RULE, ruleName));
        }

        public void exitRule(String ruleName) {
            event(new DebuggerEvent(DebuggerEvent.EXIT_RULE, ruleName));
        }

        public void enterSubRule(int decisionNumber) {
            event(new DebuggerEvent(DebuggerEvent.ENTER_SUBRULE, decisionNumber));
        }

        public void exitSubRule(int decisionNumber) {
            event(new DebuggerEvent(DebuggerEvent.EXIT_SUBRULE, decisionNumber));
        }

        public void enterDecision(int decisionNumber) {
            event(new DebuggerEvent(DebuggerEvent.ENTER_DECISION, decisionNumber));
        }

        public void exitDecision(int decisionNumber) {
            event(new DebuggerEvent(DebuggerEvent.EXIT_DECISION, decisionNumber));
        }

        public void enterAlt(int alt) {
            event(new DebuggerEvent(DebuggerEvent.ENTER_ALT, alt));
        }

        public void location(int line, int pos) {
            event(new DebuggerEvent(DebuggerEvent.LOCATION, line, pos));
        }

        public void consumeToken(Token token) {
            event(new DebuggerEvent(DebuggerEvent.CONSUME_TOKEN, token));
        }

        public void consumeHiddenToken(Token token) {
            event(new DebuggerEvent(DebuggerEvent.CONSUME_HIDDEN_TOKEN, token));
        }

        public void LT(int i, Token token) {
            event(new DebuggerEvent(DebuggerEvent.LT, i, token));
        }

        public void mark(int i) {
            event(new DebuggerEvent(DebuggerEvent.MARK, i));
        }

        public void rewind(int i) {
            event(new DebuggerEvent(DebuggerEvent.REWIND, i));
        }

        public void recognitionException(RecognitionException e) {
            event(new DebuggerEvent(DebuggerEvent.RECOGNITION_EXCEPTION, e));
        }

        public void recover() {
            event(new DebuggerEvent(DebuggerEvent.RECOVER));
        }

        public void recovered() {
            event(new DebuggerEvent(DebuggerEvent.RECOVERED));
        }

        public void commence() {
            event(new DebuggerEvent(DebuggerEvent.COMMENCE));
        }

        public void terminate() {
            event(new DebuggerEvent(DebuggerEvent.TERMINATE));
        }
    }

}
