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


package org.antlr.works.scm.p4;

import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.editor.helper.Console;
import org.antlr.works.scm.SCM;
import org.antlr.works.scm.SCMDelegate;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class P4 implements SCM {

    protected static final int CMD_EDIT = 1;
    protected static final int CMD_ADD = 2;
    protected static final int CMD_REVERT = 3;
    protected static final int CMD_DELETE = 4;
    protected static final int CMD_SUBMIT = 5;
    protected static final int CMD_SYNC = 6;
    protected static final int CMD_FSTAT = 7;

    protected Console console = null;
    protected SCMDelegate delegate = null;
    protected String fileStatus = null;

    protected P4Scheduler scheduler = new P4Scheduler();

    public P4(Console console, SCMDelegate delegate) {
        this.console = console;
        this.delegate = delegate;
    }

    public synchronized void queryFileStatus(String file) {
        runCommand(new P4Command(CMD_FSTAT, new String[] { "fstat", file }, null));
    }

    public synchronized void editFile(String file) {
        openConsoleGroup("P4: edit file");
        runCommand(new P4Command(CMD_EDIT, new String[] { "edit", file }, null));
        queryFileStatus(file);
    }

    public synchronized void addFile(String file) {
        openConsoleGroup("P4: add file");
        runCommand(new P4Command(CMD_ADD, new String[] { "add", file }, null));
        queryFileStatus(file);
    }

    public synchronized void deleteFile(String file) {
        openConsoleGroup("P4: delete file");
        runCommand(new P4Command(CMD_DELETE, new String[] { "delete", file }, null));
        queryFileStatus(file);
    }

    public synchronized void revertFile(String file) {
        openConsoleGroup("P4: revert file");
        runCommand(new P4Command(CMD_REVERT, new String[] { "revert", file }, null));
        queryFileStatus(file);
    }

    public synchronized void submitFile(String file, String description, boolean remainOpen) {
        openConsoleGroup("P4: submit file");
        scheduleCommand(new P4Command(CMD_FSTAT, new String[] { "fstat", file }, null));
        scheduleCommand(new P4CommandSubmit(file, description, remainOpen));
        queryFileStatus(file);
        scheduleLaunch();
    }

    public synchronized void sync() {
        openConsoleGroup("P4: sync");
        runCommand(new P4Command(CMD_SYNC, new String[] { "sync" }, null));
    }

    public synchronized String getFileStatus() {
        return fileStatus;
    }

    protected synchronized void openConsoleGroup(String name) {
        // When the two StreamWatcher thread
        // terminate, the console group will be closed.
        console.openGroup(name);
    }

    protected void runCommand(P4Command command) {
        scheduleCommand(command);
        scheduleLaunch();
    }

    protected void scheduleCommand(P4Command command) {
        scheduler.scheduleCommand(command);
    }

    protected synchronized void scheduleLaunch() {
        if(!scheduler.isRunning())
            scheduler.start();
    }

    protected class P4Scheduler implements Runnable, P4CommandCompletionDelegate {

        protected List scheduledCommands = new ArrayList();
        protected P4Command runningCommand = null;

        public void start() {
            new Thread(this).start();
        }

        public void run() {
            scheduleRun(null);
        }

        protected void scheduleCommand(P4Command command) {
            synchronized(scheduledCommands) {
                scheduledCommands.add(command);
            }
        }

        protected synchronized boolean isRunning() {
            return runningCommand != null;
        }

        protected synchronized void scheduleRun(P4Command previousCommand) {
            if(runningCommand != null) {
                if(runningCommand != previousCommand) {
                    // A command is already running. The next command
                    // will be launched later.
                    return;
                }
            }

            synchronized(scheduledCommands) {
                if(scheduledCommands.size() > 0) {
                    runningCommand = (P4Command)scheduledCommands.get(0);
                    scheduledCommands.remove(0);
                } else {
                    runningCommand = null;
                }
            }
            if(runningCommand == null) {
                if(delegate != null)
                    delegate.scmCommandsDidComplete();

            } else
                runningCommand.run(previousCommand, this);
        }

        public void commandDidComplete(P4CommandCompletion completion) {
            if(completion.commandID == CMD_FSTAT) {
                // Update the file status only after "fstat" command only
                fileStatus = completion.getObjectForKey(P4Results.OTHER, "action");
                if(fileStatus == null) {
                    if(completion.hasErrors())
                        fileStatus = "?";
                    else
                        fileStatus = "closed";
                }

                if(delegate != null)
                    delegate.scmFileStatusDidChange(fileStatus);
            }

            scheduleRun(runningCommand);
        }

    }

    protected class P4Command {

        public int commandID;
        public String[] commands;
        public String[] inputArguments;
        public P4CommandCompletion completion = null;

        public P4Command(int commandID, String[] commands, String[] inputArguments) {
            this.commandID = commandID;
            this.commands = commands;
            this.inputArguments = inputArguments;
        }

        public void run(P4Command previousCommand, P4CommandCompletionDelegate delegate) {
            runCommand(commandID, commands, inputArguments, delegate);
        }

        protected boolean runCommand(int commandID, String[] params, String[] inputArguments, P4CommandCompletionDelegate delegate) {
            String[] command = buildCommand(params);
            boolean success = false;

            try {
                Process p = Runtime.getRuntime().exec(command);

                completion = new P4CommandCompletion(p, commandID, delegate);

                if(inputArguments != null) {
                    OutputStream os = p.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os);
                    BufferedWriter out = new BufferedWriter(osw);
                    for(int i=0; i<inputArguments.length; i++) {
                        out.write(inputArguments[i]);
                        out.newLine();
                    }
                    out.flush();
                    out.close();
                }

                success = (completion.processExitCode = p.waitFor()) == 0;
                completion.processTerminated();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return success;
        }

        protected String[] buildCommand(String[] params) {
            String[] command = new String[10+params.length];

            int i = 0;
            command[i++] = EditorPreferences.getP4ExecPath();
            command[i++] = "-s";
            command[i++] = "-p";
            command[i++] = EditorPreferences.getP4Port();
            command[i++] = "-u";
            command[i++] = EditorPreferences.getP4User();
            command[i++] = "-P";
            command[i++] = EditorPreferences.getP4Password();
            command[i++] = "-c";
            command[i++] = EditorPreferences.getP4Client();

            for(int j=0; j<params.length; j++)
                command[i++] = params[j];

            return command;
        }
    }

    protected class P4CommandSubmit extends P4Command {

        public String file;
        public String description;
        public boolean remainOpen;

        public P4CommandSubmit(String file, String description, boolean remainOpen) {
            super(CMD_SUBMIT, null, null);

            this.file = file;
            this.description = description;
            this.remainOpen = remainOpen;
        }

        public void run(P4Command previousCommand, P4CommandCompletionDelegate delegate) {
            // To submit, we have to get the depot file corresponding to the local file
            String depotFile = previousCommand.completion.getObjectForKey(P4Results.OTHER, "depotFile");
            if(depotFile != null) {
                String[] commands = null;
                if(remainOpen)
                    commands = new String[] { "submit", "-r", "-i" };
                else
                    commands = new String[] { "submit", "-i" };

                // Change: new - new means "default changelist"
                runCommand(CMD_SUBMIT,
                            commands,
                            new String[] { "Change: new",
                                       "Client: "+EditorPreferences.getP4Client(),
                                       "User: "+EditorPreferences.getP4User(),
                                       "Description:\n\t"+description,
                                       "Files:\n\t"+depotFile},
                            delegate);
            }
        }
    }

    protected class P4Results {

        public static final int ERROR = 0;
        public static final int WARNING = 1;
        public static final int TEXT = 2;
        public static final int INFO = 3;
        public static final int EXIT = 4;
        public static final int OTHER = 5;

        protected List[] texts = new List[6];

        public P4Results() {
            for(int i=0; i<texts.length; i++)
                texts[i] = new ArrayList();
        }

        public void reset() {
            for(int i=0; i<texts.length; i++)
                texts[i].clear();
        }

        public void add(int index, String error) {
            texts[index].add(error.trim());
        }

        public List get(int index) {
            return texts[index];
        }
    }

    protected interface P4CommandCompletionDelegate {
        public void commandDidComplete(P4CommandCompletion completion);
    }

    protected class P4CommandCompletion implements StreamWatcherDelegate {

        public P4Results results = new P4Results();
        public int commandID = 0;
        public P4CommandCompletionDelegate delegate = null;

        public StreamWatcher errorStreamWatcher = null;
        public StreamWatcher inputStreamWatcher = null;

        public int processExitCode = 0;
        public boolean processTerminated = false;

        public P4CommandCompletion(Process p, int commandID, P4CommandCompletionDelegate delegate) {
            results.reset();

            this.commandID = commandID;
            this.delegate = delegate;

            errorStreamWatcher = new StreamWatcher(p.getErrorStream(), "error", this);
            errorStreamWatcher.start();

            inputStreamWatcher = new StreamWatcher(p.getInputStream(), "input", this);
            inputStreamWatcher.start();
        }

        public boolean hasErrors() {
            return results.get(P4Results.ERROR).size() > 0;
        }

        public String getObjectForKey(int index, String key) {
            Iterator iter = results.get(index).iterator();
            while(iter.hasNext()) {
                String text = (String)iter.next();
                if(text.startsWith(key)) {
                    return text.substring(key.length()+1).trim();
                }
            }
            return null;
        }

        public synchronized void processTerminated() {
            processTerminated = true;
            notifyIfCommandCompleted();
        }

        public synchronized void notifyIfCommandCompleted() {
            if(errorStreamWatcher == null && inputStreamWatcher == null && processTerminated) {
                // Command is completed once all stream watcher have terminated AND
                // the process has terminated
                if(delegate != null)
                    delegate.commandDidComplete(this);
            }
        }

        public synchronized void streamWatcherDidReceiveText(StreamWatcher sw, String line) {
            if(console != null)
                console.println(line);

            int index = line.indexOf(':');
            String text = line;
            if(index != -1)
                text = line.substring(index+1);

            if(line.startsWith("error:"))
                results.add(P4Results.ERROR, text);
            else if(line.startsWith("warning:"))
                results.add(P4Results.WARNING, text);
            else if(line.startsWith("text:"))
                results.add(P4Results.TEXT, text);
            else if(line.startsWith("info:"))
                results.add(P4Results.INFO, text);
            else if(line.startsWith("exit:"))
                results.add(P4Results.EXIT, text);
            else
                results.add(P4Results.OTHER, text);
        }

        public synchronized void streamWatcherDidEnd(StreamWatcher sw) {
            if(sw == errorStreamWatcher)
                errorStreamWatcher = null;
            if(sw == inputStreamWatcher)
                inputStreamWatcher = null;

            notifyIfCommandCompleted();
        }
    }

    protected interface StreamWatcherDelegate {
        public void streamWatcherDidReceiveText(StreamWatcher sw, String text);
        public void streamWatcherDidEnd(StreamWatcher sw);
    }

    protected class StreamWatcher extends Thread {

        public InputStream is;
        public String type;
        public StreamWatcherDelegate delegate;

        public StreamWatcher(InputStream is, String type, StreamWatcherDelegate delegate) {
            this.is = is;
            this.type = type;
            this.delegate = delegate;
        }

        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line=null;
                while ( (line = br.readLine()) != null) {
                    if(delegate != null)
                        delegate.streamWatcherDidReceiveText(this, line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            if(delegate != null)
                delegate.streamWatcherDidEnd(this);
        }
    }

}
