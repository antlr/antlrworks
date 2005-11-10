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
import org.antlr.runtime.debug.DebugEventListener;
import org.antlr.runtime.debug.RemoteDebugEventSocketListener;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.usfca.xj.appkit.utils.XJDialogProgress;
import edu.usfca.xj.appkit.utils.XJDialogProgressDelegate;

public class DebuggerRecorder implements Runnable, XJDialogProgressDelegate {

    public static final int STATUS_STOPPED = 0;
    public static final int STATUS_STOPPING = 1;
    public static final int STATUS_LAUNCHING = 2;
    public static final int STATUS_RUNNING = 3;

    public static final int MAX_RETRY = 6;

    protected Debugger debugger;
    protected int status = STATUS_STOPPED;
    protected boolean debuggerStop = false;
    protected boolean cancelled;

    protected String address;
    protected int port;

    protected ArrayList events;
    protected int position;
    protected int breakType = DebuggerEvent.NONE;

    protected EventListener eventListener;
    protected RemoteDebugEventSocketListener listener;

    protected XJDialogProgress progress;

    protected boolean debuggerReceivedTerminateEvent;

    public DebuggerRecorder(Debugger debugger) {
        this.debugger = debugger;
        this.progress = new XJDialogProgress(debugger.editor);
        reset();
    }

    public void showProgress() {
        progress.setInfo("Connecting...");
        progress.setIndeterminate(true);
        progress.setDelegate(this);
        progress.display();
    }

    public void hideProgress() {
        progress.close();
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
            return (List)events.clone();

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
        this.breakType = breakType;
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

    public void stepBackward(int breakEvent) {
        setBreaksOnEventType(breakEvent);
        if(stepMove(-1))
            playEvents(true);
    }

    public void stepForward(int breakEvent) {
        setBreaksOnEventType(breakEvent);
        if(stepMove(1))
            playEvents(false);
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
        playEvents(true);
    }

    public void goToEnd() {
        stepForward(DebuggerEvent.TERMINATE);
    }

    public void connect(String address, int port) {
        this.address = address;
        this.port = port;

        new Thread(this).start();
    }

    public void run() {
        eventListener = new EventListener();
        cancelled = false;

        boolean connected = false;
        int retryCount = MAX_RETRY;
        while(retryCount-- > 0 && !cancelled) {
            listener = null;
            try {
                listener = new RemoteDebugEventSocketListener(eventListener,
                        DebuggerRecorder.this.address, DebuggerRecorder.this.port);
            } catch (IOException e) {
                listener = null;
            }

            if(listener != null) {
                connected = true;
                break;
            }

            if(retryCount == MAX_RETRY-1)
                showProgress();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // We don't care if the sleep has been interrupted
            }
        }

        hideProgress();

        if(cancelled) {
            setStatus(STATUS_STOPPED);
            connectionCancelled();
        } else if(!connected) {
            setStatus(STATUS_STOPPED);
            connectionFailed();
        } else {
            debuggerStop = false;
            setStatus(STATUS_LAUNCHING);

            debuggerReceivedTerminateEvent = false;

            reset();
            listener.start();

            connectionSuccess();
        }
    }

    public void connectionSuccess() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                debugger.connectionSuccess();
            }
        });
    }

    public void connectionFailed() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                debugger.connectionFailed();
            }
        });
    }

    public void connectionCancelled() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                debugger.connectionCancelled();
            }
        });
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
                        playEvents(false);
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
                playEvents(false);
            }
        });

        try {
            wait();
        } catch (InterruptedException e) {
            System.err.println("recorderThreadBreaksOnEvent: interrupted");
        }
    }

    protected synchronized void playEvents(boolean reset) {
        debugger.playEvents(getCurrentEvents(), reset);
    }

    public void dialogDidCancel() {
        cancelled = true;
    }

    protected class EventListener implements DebugEventListener {

        public EventListener() {
        }

        public void event(DebuggerEvent event) {
            addEvent(event);
            eventOccurred(event);
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

        public void beginResync() {
            event(new DebuggerEvent(DebuggerEvent.BEGIN_RESYNC));
        }

        public void endResync() {
            event(new DebuggerEvent(DebuggerEvent.END_RESYNC));
        }

        public void semanticPredicate(boolean result, String predicate) {
            // @todo implement in the future
        }

        public void commence() {
            event(new DebuggerEvent(DebuggerEvent.COMMENCE));
        }

        public void terminate() {
            event(new DebuggerEvent(DebuggerEvent.TERMINATE));
        }
    }

}
