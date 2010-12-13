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
import org.antlr.runtime.debug.DebugEventListener;
import org.antlr.runtime.debug.RemoteDebugEventSocketListener;
import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.debugger.events.*;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.Console;
import org.antlr.works.utils.NumberSet;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.utils.XJDialogProgress;
import org.antlr.xjlib.appkit.utils.XJDialogProgressDelegate;
import org.antlr.xjlib.foundation.XJUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

public class DBRecorder implements Runnable, XJDialogProgressDelegate {

    public static final int STATUS_STOPPED = 0;
    public static final int STATUS_STOPPING = 1;
    public static final int STATUS_LAUNCHING = 2;
    public static final int STATUS_RUNNING = 3;
    public static final int STATUS_BREAK = 4;

    public static final int MAX_RETRY = 12;

    protected DebuggerTab debuggerTab;
    protected int status = STATUS_STOPPED;
    protected boolean cancelled;

	public static class FixBugRemoteDebugEventSocketListener
		extends RemoteDebugEventSocketListener
	{
		public FixBugRemoteDebugEventSocketListener(DebugEventListener listener,
													String machine,
													int port)
			throws java.io.IOException
		{
			super(listener, machine, port);

		}

		protected void dispatch(String line) {
			//System.out.println("event: "+line);
			String[] elements = getEventElements(line);
			if ( elements==null || elements[0]==null ) {
				return;
			}
			if ( elements[0].equals("enterDecision") ) {
				line += "\tfalse"; // protocol doesn't send in 3.3 antlr; pretend
			}
			super.dispatch(line);
		}
	}

    protected String address;
    protected int port;

    protected List<DBEvent> events;
    protected int position;
    protected NumberSet breakEvents = new NumberSet();
    protected int stoppedOnEvent = DBEvent.NO_EVENT;
    protected boolean ignoreBreakpoints = false;
    protected StepOver stepOver = new StepOver();

    protected int lastTokenIndexEventNumber;
    protected int currentTokenIndexEventNumber;
    protected int currentTokenIndex;

    protected DBRecorderEventListener eventListener;
    protected RemoteDebugEventSocketListener listener;

    protected XJDialogProgress progress;

    /** This flag is used to indicate that the debugger received the terminate event.
     * It is used to force stop the debugger if it cannot be stopped by the normal method.
     */
    protected boolean debuggerReceivedTerminateEvent;

    /** Flag used to indicate if the user has been warning about a problem
     * with the remote parser state. It ensure the message is only displayed once
     * during a debugging session.
     */
    protected boolean remoteParserStateWarned = false;

    /**
     * Current grammar the recorder is in
     */
    protected Stack<String> grammarNamesStack = new Stack<String>();

    public DBRecorder(DebuggerTab debuggerTab) {
        this.debuggerTab = debuggerTab;
        reset();
    }

    public void close() {
        debuggerTab = null;
    }

    public void showProgress() {
        if(progress == null)
            progress = new XJDialogProgress(debuggerTab.getContainer());
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
        events = Collections.synchronizedList(new ArrayList<DBEvent>());
        position = -1;
        currentTokenIndex = -1;
        remoteParserStateWarned = false;
    }

    public synchronized DBEvent getEvent() {
        if(position<0 || position>=events.size())
            return null;
        else
            return events.get(position);
    }

    public synchronized DBEvent getLastEvent() {
        return events.get(events.size()-1);
    }

    public synchronized List<DBEvent> getCurrentEvents() {
        if(events.size() == 0)
            return events;

        int toIndex = position+1;
        if(toIndex >= events.size())
            toIndex = events.size();

        return events.subList(0, toIndex);
    }

    public synchronized int getCurrentEventPosition() {
        if(events.size() == 0)
            return 0;

        int toIndex = position+1;
        if(toIndex >= events.size())
            toIndex = events.size();

        return(toIndex);
    }

    public void setPositionToEnd() {
        position = events.size()-1;
    }

    public void setBreakEvents(Set events) {
        this.breakEvents.replaceAll(events);
    }

    public Set getBreakEvents() {
        return breakEvents;
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
        debuggerTab.queryGrammarBreakpoints();
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
        DBEvent event = getEvent();
        if(event == null)
            return DBEvent.NO_EVENT;

        /** If we are stepping over handle it here */
        if(stepOver.isSteppingOver()) {
            if(stepOver.shouldStop(event)) {
                stepOver.endStepOver();
                return event.getEventType();
            } else
                return DBEvent.NO_EVENT;
        }

        if(event.getEventType() == DBEvent.COMMENCE)
            return event.getEventType();

        if(breakEvents.contains(DBEvent.convertToInteger(DBEvent.ALL)))
            return event.getEventType();

        // Stop on debugger breakpoints
        if(event.getEventType() == DBEvent.LOCATION && !ignoreBreakpoints())
            if(debuggerTab.isBreakpointAtLine(((DBEventLocation)event).line-1, event.getGrammarName())) {
                return event.getEventType();
            }

        // Stop on input text breakpoint
        if(event.getEventType() == DBEvent.CONSUME_TOKEN && !ignoreBreakpoints())
            if(debuggerTab.isBreakpointAtToken(((DBEventConsumeToken)event).token))
                return event.getEventType();

        if(event.getEventType() == DBEvent.CONSUME_TOKEN && breakEvents.contains(DBEvent.convertToInteger(DBEvent.CONSUME_TOKEN))) {
            // Breaks only on consume token from channel 0
            return ((DBEventConsumeToken)event).token.getChannel() == Token.DEFAULT_CHANNEL?event.getEventType() :DBEvent.NO_EVENT;
        } else
            return breakEvents.contains(DBEvent.convertToInteger(event.getEventType()))?event.getEventType() :DBEvent.NO_EVENT;
    }

    public synchronized void setStatus(int status) {
        if(this.status != status) {
            this.status = status;
            debuggerTab.recorderStatusDidChange();
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
            return e.getEventType() == DBEvent.TERMINATE;
    }

    public void stepBackward(Set breakEvents) {
        setIgnoreBreakpoints(false);
        stepContinue(breakEvents);
        stepMove(-1);
        /* Play the events in any case. Otherwise the debugger might not get notified
         correctly of a backward step. */
        playEvents(true);
    }

    public synchronized void stepForward(Set breakEvents) {
        setIgnoreBreakpoints(false);
        stepContinue(breakEvents);
        if(stepMove(1)) {
            /* There is some events left, play them */
            playEvents(false);
        } else {
            /* No more events. If the debugger received the terminate event,
               play the events so far. Otherwise, notify the thread that it can
               continue to receive more events from the remote parser. */
            if(debuggerReceivedTerminateEvent)
                playEvents(false);
            else
                threadNotify();
        }
    }

    public void stepOver() {
        stepOver.beginStepOver();
        fastForward();
    }

    public void stepContinue(Set breakEvents) {
        setBreakEvents(breakEvents);
        queryGrammarBreakpoints();
        setStatus(STATUS_RUNNING);
    }


    /** This method returns false if no more event is available */
    public synchronized boolean stepMove(int direction) {
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
        stepContinue(new NumberSet(DBEvent.TERMINATE));
        if(stepMove(1))
            playEvents(false);
        else
            threadNotify();
    }

    public void fastForward() {
        stepForward(new NumberSet(DBEvent.TERMINATE));
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
        boolean showProgress = false;

        long t = System.currentTimeMillis();
        long timeout = AWPrefs.getDebugLaunchTimeout()*1000;

        while((System.currentTimeMillis()-t) < timeout && !cancelled) {
            listener = null;
            try {
                listener = new FixBugRemoteDebugEventSocketListener(eventListener,
                        DBRecorder.this.address, DBRecorder.this.port);
            } catch (IOException e) {
                listener = null;
            }

            if(listener != null) {
                connected = true;
                break;
            }

            if((System.currentTimeMillis()-t) >= 2 && !showProgress) {
                showProgress();
                showProgress = true;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // We don't care if the sleep has been interrupted
            }
        }

        if(showProgress)
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
                debuggerTab.connectionSuccess();
            }
        });
    }

    public void connectionFailed() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                debuggerTab.connectionFailed();
            }
        });
    }

    public void connectionCancelled() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                debuggerTab.connectionCancelled();
            }
        });
    }

    public synchronized void requestStop() {
        setStatus(STATUS_STOPPING);
        threadNotify();

        if(debuggerReceivedTerminateEvent)
            stop();
    }

    public void stop() {
        // if the window is closed, the debugger does not exist anymore
        // and this event can be ignored
        if(debuggerTab == null) return;

        setStatus(STATUS_STOPPED);
        debuggerTab.recorderDidStop();
    }

    /** This method checks that the remote parser's states are in sync with
     * the state of the debugger (i.e. name of the grammar). This method
     * is called in the event dispatch thread.
     */
    public void checkRemoteParserHeaders() {
        //Tool.VERSION
        //System.out.println(listener.version);

        String grammarFileName = debuggerTab.getDelegate().getGrammarEngine().getGrammarFileName();
        String remoteParserGrammarFileName = XJUtils.getLastPathComponent(listener.grammarFileName);

        if(!grammarFileName.equals(remoteParserGrammarFileName)) {
            String message = "Warning: the grammar used by the remote parser is not the same ("+remoteParserGrammarFileName+").";
            XJAlert.display(debuggerTab.getContainer(), "Grammar Mismatch", message);
        }
    }

    /** Check any error coming from the remote parser. Return true if the debuggerTab needs
     to be paused
     */
    public boolean checkRemoteParserState() {
        if(remoteParserStateWarned)
            return false;

        if(listener.tokenIndexesAreInvalid()) {
            remoteParserStateWarned = true;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    String message = "Invalid token indexes (current index is "+currentTokenIndex+" at event "+currentTokenIndexEventNumber+" while the same index was used at event "+lastTokenIndexEventNumber+"). Make sure that the remote parser implements the getTokenIndex() method of Token. The indexes must be unique for each consumed token.";
                    XJAlert.display(debuggerTab.getContainer(), "Invalid Token Indexes", message);
                }
            });

            return true;
        }
        return false;
    }

    /** This method keeps track of the last consumed index in order to display
     * useful information if an invalid index is detected
     */
    public void recordIndexes(DBEvent event) {
        Token t = null;
        if(event instanceof DBEventConsumeToken) {
            DBEventConsumeToken e = (DBEventConsumeToken) event;
            t = e.token;
        }
        if(event instanceof DBEventConsumeHiddenToken) {
            DBEventConsumeHiddenToken e = (DBEventConsumeHiddenToken) event;
            t = e.token;
        }

        if(t != null) {
            lastTokenIndexEventNumber = currentTokenIndexEventNumber;
            currentTokenIndexEventNumber = events.size()-1;
            currentTokenIndex = t.getTokenIndex();
        }
    }

    public void handleGrammarName(DBEvent event) {
        if(event instanceof DBEventEnterRule) {
            grammarNamesStack.push(event.getGrammarName());
        }
        if(event instanceof DBEventExitRule) {
            grammarNamesStack.pop();
        }
        if(grammarNamesStack.isEmpty()) {
            event.setGrammarName(null);
        } else {
            event.setGrammarName(grammarNamesStack.peek());
        }
    }

    /** This method is called by DBRecorderEventListener for each event received from
     * the remote parser. It is running on another thread than the event thread.
     */
    public synchronized void listenerEvent(DBEvent event) {
        events.add(event);
        handleGrammarName(event);
        recordIndexes(event);
        setPositionToEnd();

        switch(getStatus()) {
            case STATUS_LAUNCHING:
                setStatus(STATUS_RUNNING);
                break;

            case STATUS_STOPPING:
                /* Stop the debugger if the terminate event is reached or if the flag
                debuggerReceivedTerminateEvent is true
                */
                if(event.getEventType() == DBEvent.TERMINATE || debuggerReceivedTerminateEvent)
                    stop();
                break;
        }

        if(isRunning()) {
            switch(event.getEventType()) {
                case DBEvent.TERMINATE:
                    setStoppedOnEvent(DBEvent.TERMINATE);
                    breaksOnEvent(false);
                    debuggerReceivedTerminateEvent = true;
                    break;

                case DBEvent.COMMENCE:
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            checkRemoteParserHeaders();
                        }
                    });
                    setStoppedOnEvent(DBEvent.COMMENCE);
                    breaksOnEvent(true);
                    break;

                default:
                    if(checkRemoteParserState() || isOnBreakEvent())
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
            debuggerTab.getConsole().println("recorderThreadBreaksOnEvent: interrupted", Console.LEVEL_WARNING);
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
            debuggerTab.playEvents(events, getCurrentEventPosition(), reset);
    }

    public void dialogDidCancel() {
        cancelled = true;
    }

    public class StepOver {

        public static final int MODE_DISABLED = 0;
        public static final int MODE_WAIT_ENTER_RULE = 1;
        public static final int MODE_WAIT_EXIT_RULE = 2;
        public static final int MODE_WAIT_LOCATION = 3;

        public int mode = MODE_DISABLED;

        /** Count the number of nested stepped over rule name */
        public int nested;
        /** Name of the stepped over rule */
        public String ruleName;

        public void beginStepOver() {
            mode = MODE_WAIT_ENTER_RULE;
        }

        public void endStepOver() {
            mode = MODE_DISABLED;
        }

        public boolean isSteppingOver() {
            return mode != MODE_DISABLED;
        }

        public boolean shouldStop(DBEvent event) {
            switch(mode) {
                case MODE_WAIT_ENTER_RULE:
                    if(event instanceof DBEventEnterRule) {
                        DBEventEnterRule e = (DBEventEnterRule)event;
                        ruleName = e.name;
                        mode = MODE_WAIT_EXIT_RULE;
                        nested = 0;
                    }
                    break;

                case MODE_WAIT_EXIT_RULE:
                    if(event instanceof DBEventEnterRule) {
                        DBEventEnterRule e = (DBEventEnterRule)event;
                        if(e.name.equals(ruleName))
                            nested++;
                    } else if(event instanceof DBEventExitRule) {
                        DBEventExitRule e = (DBEventExitRule)event;
                        if(e.name.equals(ruleName)) {
                            if(nested == 0) {
                                mode = MODE_WAIT_LOCATION;
                            } else {
                                nested--;
                            }
                        }
                    }
                    break;

                case MODE_WAIT_LOCATION:
                    if(event instanceof DBEventLocation)
                        return true;
                    break;
            }
            return false;
        }
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
