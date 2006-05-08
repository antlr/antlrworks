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

import edu.usfca.xj.appkit.utils.XJDialogProgress;
import edu.usfca.xj.appkit.utils.XJDialogProgressDelegate;
import org.antlr.runtime.Token;
import org.antlr.runtime.debug.RemoteDebugEventSocketListener;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.debugger.events.DBEvent;
import org.antlr.works.debugger.events.DBEventConsumeToken;
import org.antlr.works.debugger.events.DBEventLocation;
import org.antlr.works.utils.Console;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DBRecorder implements Runnable, XJDialogProgressDelegate {

    public static final int STATUS_STOPPED = 0;
    public static final int STATUS_STOPPING = 1;
    public static final int STATUS_LAUNCHING = 2;
    public static final int STATUS_RUNNING = 3;
    public static final int STATUS_BREAK = 4;

    public static final int MAX_RETRY = 12;

    protected Debugger debugger;
    protected int status = STATUS_STOPPED;
    protected boolean cancelled;

    protected String address;
    protected int port;

    protected ArrayList events;
    protected int position;
    protected int breakType = DBEvent.NONE;
    protected int stoppedOnEvent = DBEvent.NO_EVENT;
    protected boolean ignoreBreakpoints = false;

    protected DBRecorderEventListener eventListener;
    protected RemoteDebugEventSocketListener listener;

    protected XJDialogProgress progress;

    protected boolean debuggerReceivedTerminateEvent;

    public DBRecorder(Debugger debugger) {
        this.debugger = debugger;
        this.progress = new XJDialogProgress(debugger.getWindowComponent());
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

    /** Return true if the debugger is running */
    public synchronized boolean isRunning() {
        return status == DBRecorder.STATUS_RUNNING;
    }

    /** Return true if the debugger is alive (i.e. not stopped, stopping, starting) */
    public synchronized boolean isAlive() {
        return status == DBRecorder.STATUS_RUNNING ||
               status == DBRecorder.STATUS_BREAK;
    }

    public synchronized void reset() {
        if(events == null)
            events = new ArrayList();
        else
            events.clear();
        position = -1;
    }

    public synchronized DBEvent getEvent() {
        if(position<0 || position>=events.size())
            return null;
        else
            return (DBEvent)events.get(position);
    }

    public synchronized DBEvent getLastEvent() {
        return (DBEvent)events.get(events.size()-1);
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

    public void setStoppedOnEvent(int event) {
        stoppedOnEvent = event;
    }

    public int getStoppedOnEvent() {
        return stoppedOnEvent;
    }

    public void setIgnoreBreakpoints(boolean flag) {
        this.ignoreBreakpoints = flag;
    }

    public boolean ignoreBreakpoints() {
        return ignoreBreakpoints;
    }

    public void queryGrammarBreakpoints() {
        // Get the current breakpoints in the grammar text
        // because they can be set/unset during a debugging
        // session of course ;-)
        debugger.queryGrammarBreakpoints();
    }

    /** Return true if the debugger hitted a break event */
    public boolean isOnBreakEvent() {
        int breakEvent = getOnBreakEvent();
        if(breakEvent != DBEvent.NO_EVENT) {
            setStoppedOnEvent(breakEvent);
            setStatus(STATUS_BREAK);
            return true;
        } else
            return false;
    }

    /** Return the event type that causes the break */
    public int getOnBreakEvent() {
        if(breakType == DBEvent.NONE)
            return DBEvent.NO_EVENT;

        if(breakType == DBEvent.ALL)
            return breakType;

        DBEvent event = getEvent();
        if(event == null)
            return DBEvent.NO_EVENT;

        // Stop on debugger breakpoints
        if(event.type == DBEvent.LOCATION && !ignoreBreakpoints())
            if(debugger.isBreakpointAtLine(((DBEventLocation)event).line-1))
                return event.type;

        // Stop on input text breakpoint
        if(event.type == DBEvent.CONSUME_TOKEN && !ignoreBreakpoints())
            if(debugger.isBreakpointAtToken(((DBEventConsumeToken)event).token))
                return event.type;

        if(event.type == DBEvent.CONSUME_TOKEN && breakType == DBEvent.CONSUME_TOKEN) {
            // Breaks only on consume token from channel 0
            return ((DBEventConsumeToken)event).token.getChannel() == Token.DEFAULT_CHANNEL?event.type:DBEvent.NO_EVENT;
        } else
            return event.type == breakType?event.type:DBEvent.NO_EVENT;
    }

    public synchronized void setStatus(int status) {
        if(this.status != status) {
            this.status = status;
            debugger.recorderStatusDidChange();            
        }
    }

    public synchronized int getStatus() {
        return status;
    }

    public boolean isAtBeginning() {
        return position == 0;
    }

    public boolean isAtEnd() {
        DBEvent e = getEvent();
        if(e == null)
            return true;
        else
            return e.type == DBEvent.TERMINATE;
    }

    public void stepBackward(int breakEvent) {
        setIgnoreBreakpoints(false);
        stepContinue(breakEvent);
        if(stepMove(-1))
            playEvents(true);
    }

    public void stepForward(int breakEvent) {
        setIgnoreBreakpoints(false);
        stepContinue(breakEvent);
        if(stepMove(1))
            playEvents(false);
        else
            threadNotify();
    }

    public void stepContinue(int breakEvent) {
        setBreaksOnEventType(breakEvent);
        queryGrammarBreakpoints();
        setStatus(STATUS_RUNNING);
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

        DBEvent event;
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
        setIgnoreBreakpoints(false);
        playEvents(true);
    }

    public void goToEnd() {
        setIgnoreBreakpoints(true);
        stepContinue(DBEvent.TERMINATE);
        if(stepMove(1))
            playEvents(false);
        else
            threadNotify();
    }

    public void fastForward() {
        stepForward(DBEvent.TERMINATE);
    }

    public void connect(String address, int port) {
        this.address = address;
        this.port = port;

        new Thread(this).start();
    }

    public void run() {
        eventListener = new DBRecorderEventListener(this);
        cancelled = false;

        boolean connected = false;
        int retryCount = MAX_RETRY;
        while(retryCount-- > 0 && !cancelled) {
            listener = null;
            try {
                listener = new RemoteDebugEventSocketListener(eventListener,
                        DBRecorder.this.address, DBRecorder.this.port);
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
        threadNotify();

        if(debuggerReceivedTerminateEvent)
            forceStop();
    }

    public void forceStop() {
        setStatus(STATUS_STOPPED);
        debugger.recorderDidStop();
    }

    /** This method is called by DBRecorderEventListener for each event received from
     * the remote parser. It is running on another thread than the event thread.
     */

    public synchronized void listenerEvent(DBEvent event) {
        events.add(event);
        setPositionToEnd();

        switch(getStatus()) {
            case STATUS_LAUNCHING:
                setStatus(STATUS_RUNNING);
                break;

            case STATUS_STOPPING:
                // @todo comment this
                if(event.type == DBEvent.TERMINATE || debuggerReceivedTerminateEvent)
                    forceStop();
                break;
        }

        if(isRunning()) {
            switch(event.type) {
                case DBEvent.TERMINATE:
                    setStoppedOnEvent(DBEvent.TERMINATE);
                    breaksOnEvent(false);
                    debuggerReceivedTerminateEvent = true;
                    break;

                case DBEvent.COMMENCE:
                    setStoppedOnEvent(DBEvent.COMMENCE);
                    breaksOnEvent(true);
                    break;

                default:
                    if(isOnBreakEvent())
                        breaksOnEvent(true);
                    break;
            }
        }
    }

    public synchronized void threadNotify() {
        notify();
    }

    public synchronized void threadWait() {
        try {
            wait();
        } catch (InterruptedException e) {
            debugger.getConsole().println("recorderThreadBreaksOnEvent: interrupted", Console.LEVEL_WARNING);
        }
    }

    public synchronized void breaksOnEvent(boolean wait) {
        setStatus(STATUS_BREAK);
        playEvents(false);
        if(wait)
            threadWait();
    }

    protected synchronized void playEvents(boolean reset) {
        /** Make sure this method is called on the event dispatch thread */
        if(!SwingUtilities.isEventDispatchThread())
            SwingUtilities.invokeLater(new PlayEventRunnable(reset));
        else
            debugger.playEvents(getCurrentEvents(), reset);
    }

    public void dialogDidCancel() {
        cancelled = true;
    }

    public class PlayEventRunnable implements Runnable {

        public boolean reset;

        public PlayEventRunnable(boolean reset) {
            this.reset = reset;
        }

        public void run() {
            playEvents(reset);
        }
    }
}
