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

package org.antlr.works.debugger.local;

import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.utils.XJDialogProgress;
import edu.usfca.xj.appkit.utils.XJDialogProgressDelegate;
import edu.usfca.xj.foundation.XJUtils;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.antlr.works.IDE;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.debugger.DebuggerInputDialog;
import org.antlr.works.engine.EngineRuntime;
import org.antlr.works.generate.CodeGenerate;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.Console;
import org.antlr.works.utils.StreamWatcher;
import org.antlr.works.utils.StreamWatcherDelegate;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class DBLocal implements Runnable, XJDialogProgressDelegate, StreamWatcherDelegate {

    public static final String remoteParserClassName = "__Test__";
    // @todo put this in the check-list
    public static final String parserGlueCodeTemplatePath = "org/antlr/works/debugger/local/";
    public static final String parserGlueCodeTemplateName = "DBParserGlueCode";

    public static final String ST_ATTR_CLASSNAME = "class_name";
    public static final String ST_ATTR_INPUT_FILE = "input_file";
    public static final String ST_ATTR_JAVA_PARSER = "java_parser";
    public static final String ST_ATTR_JAVA_LEXER = "java_parser_lexer";
    public static final String ST_ATTR_START_SYMBOL = "start_symbol";

    protected String outputFileDir;

    protected String fileParser;
    protected String fileLexer;
    protected String fileRemoteParser;
    protected String fileRemoteParserInputText;

    protected String startRule;
    protected String lastStartRule;

    protected Process remoteParserProcess;

    protected boolean cancelled;
    protected boolean buildAndDebug;

    protected CodeGenerate codeGenerator;
    protected Debugger debugger;

    protected String inputText;

    protected XJDialogProgress progress;
    protected ErrorReporter error = new ErrorReporter();

    public DBLocal(Debugger debugger) {
        this.debugger = debugger;
        this.codeGenerator = new CodeGenerate(debugger.getProvider(), null);
    }

    public void setOutputPath(String path) {
        codeGenerator.setOutputPath(path);
    }

    public void setStartRule(String rule) {
        this.startRule = rule;
    }

    public String getStartRule() {
        return startRule;
    }

    public void dialogDidCancel() {
        cancel();
    }

    public String getInputText() {
        return inputText;
    }

    public void forceStop() {
        if(remoteParserProcess != null)
            remoteParserProcess.destroy();
    }

    public synchronized void cancel() {
        cancelled = true;
    }

    public synchronized boolean cancelled() {
        return cancelled;
    }

    public void showProgress() {
        if(progress == null)
            progress = new XJDialogProgress(debugger.getWindowComponent());
        progress.setInfo("Preparing...");
        progress.setIndeterminate(false);
        progress.setProgress(0);
        progress.setProgressMax(3);
        progress.setDelegate(this);
        progress.display();
    }

    public void hideProgress() {
        progress.close();
    }

    public void prepareAndLaunch(boolean buildAndDebug) {
        this.buildAndDebug = buildAndDebug;
        cancelled = false;

        if(buildAndDebug)
            showProgress();

        // Start the thread a little bit later to let
        // the progress dialog displays first
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                startThread();
            }
        });
    }

    public void startThread() {
        new Thread(this).start();
    }

    public void run() {
        resetErrors();

        if(prepare()) {
            if(buildAndDebug)
                generateAndCompileGrammar();

            if(!cancelled())
                askUserForInputText();

            if(!cancelled())
                generateAndCompileGlueCode(buildAndDebug);

            if(!cancelled())
                generateInputText();

            if(!cancelled())
                launchRemoteParser();
        }

        if(hasErrors())
            notifyErrors();
        else if(cancelled())
            notifyCancellation();
        else
            notifyCompletion();
    }

    protected void askUserForInputText() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    hideProgress();

                    DebuggerInputDialog dialog = new DebuggerInputDialog(debugger, debugger.getWindowComponent());
                    dialog.setInputText(inputText);
                    if(dialog.runModal() == XJDialog.BUTTON_OK) {
                        inputText = dialog.getInputText();
                        setStartRule(dialog.getRule());
                        showProgress();
                    } else
                        cancel();
                }
            });
        } catch (Exception e) {
            debugger.getConsole().print(e);
        }
    }

    protected void reportError(String message) {
        error.setTitle("Error");
        error.setMessage(message);
        error.enable();
        cancel();
    }

    protected void resetErrors() {
        error.reset();
    }

    protected boolean hasErrors() {
        return error.hasErrors;
    }

    protected void notifyErrors() {
        hideProgress();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(XJAlert.displayAlert(debugger.getWindowComponent(), error.title, error.message, "Show Console", "OK", 1) == 0) {
                    debugger.selectConsoleTab();
                }
            }
        });
    }

    protected void notifyCancellation() {
        hideProgress();
    }

    protected void notifyCompletion() {
        hideProgress();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(!cancelled())
                    debugger.debuggerLocalDidRun(buildAndDebug);
            }
        });
    }

    protected boolean prepare() {
        try {

            setOutputPath(AWPrefs.getOutputPath());
            setStartRule(AWPrefs.getStartSymbol());

            fileParser = codeGenerator.getGeneratedTextFileName(false);
            fileLexer = codeGenerator.getGeneratedTextFileName(true);

            fileRemoteParser = XJUtils.concatPath(codeGenerator.getOutputPath(), remoteParserClassName+".java");
            fileRemoteParserInputText = XJUtils.concatPath(codeGenerator.getOutputPath(), remoteParserClassName+"_input.txt");

            outputFileDir = XJUtils.concatPath(codeGenerator.getOutputPath(), "classes");
            new File(outputFileDir).mkdirs();
        } catch(Exception e) {
            debugger.getConsole().print(e);
            reportError("Error while preparing the grammar:\n"+e.toString());
            return false;
        }
        return true;
    }

    protected void generateAndCompileGrammar() {
        progress.setInfo("Analyzing...");
        progress.setProgress(1);
        analyzeGrammar();

        if(cancelled())
            return;

        progress.setInfo("Generating...");
        progress.setProgress(2);
        generateGrammar();

        if(cancelled())
            return;

        progress.setInfo("Compiling...");
        progress.setProgress(3);
        compileGrammar();
    }

    protected void analyzeGrammar() {
        String errorMessage = null;
        try {
            debugger.getGrammar().analyze();
        } catch (Exception e) {
            debugger.getConsole().print(e);
            errorMessage = e.getLocalizedMessage();
        }
        if(errorMessage != null) {
            reportError("Error while analyzing the grammar:\n"+errorMessage);
        }
    }

    protected void generateGrammar() {
        String errorMessage = null;
        try {
            if(!codeGenerator.generate())
                errorMessage = codeGenerator.getLastError();
        } catch (Exception e) {
            debugger.getConsole().print(e);
            errorMessage = e.getLocalizedMessage();
        }

        if(errorMessage != null) {
            reportError("Error while generating the grammar:\n"+errorMessage);
        }
    }

    protected void compileGrammar() {
        new File(outputFileDir).mkdirs();
        compileFiles(new String[] { fileParser, fileLexer });
    }

    protected void generateAndCompileGlueCode(boolean buildAndDebug) {
        progress.setInfo("Preparing...");
        progress.setIndeterminate(true);

        if(!buildAndDebug && lastStartRule != null && startRule.equals(lastStartRule))
            return;

        lastStartRule = startRule;

        generateGlueCode();

        if(cancelled())
            return;

        compileGlueCode();
    }

    protected void generateGlueCode() {
        try {
            StringTemplateGroup group = new StringTemplateGroup("DebuggerLocalGroup", DefaultTemplateLexer.class);
            StringTemplate glueCode = group.getInstanceOf(parserGlueCodeTemplatePath +parserGlueCodeTemplateName);
            glueCode.setAttribute(ST_ATTR_CLASSNAME, remoteParserClassName);
            glueCode.setAttribute(ST_ATTR_INPUT_FILE, XJUtils.escapeString(fileRemoteParserInputText));
            glueCode.setAttribute(ST_ATTR_JAVA_PARSER, codeGenerator.getGeneratedClassName(false));
            glueCode.setAttribute(ST_ATTR_JAVA_LEXER, codeGenerator.getGeneratedClassName(true));
            glueCode.setAttribute(ST_ATTR_START_SYMBOL, startRule);

            XJUtils.writeStringToFile(glueCode.toString(), fileRemoteParser);
        } catch(Exception e) {
            debugger.getConsole().print(e);
            reportError("Error while generating the glue-code:\n"+e.getLocalizedMessage());
        }
    }

    protected void compileGlueCode() {
        compileFiles(new String[] { fileRemoteParser });
    }

    protected void compileFiles(String[] files) {
        String error = EngineRuntime.compileFiles(debugger.getConsole(), files, outputFileDir, this);
        if(error != null)
            reportError(error);
    }

    protected void generateInputText() {
        try {
            XJUtils.writeStringToFile(getInputText(), fileRemoteParserInputText);
        } catch (IOException e) {
            debugger.getConsole().print(e);
            reportError("Error while generating the input text:\n"+e.getLocalizedMessage());
        }
    }

    public boolean isRequiredFilesExisting() {
        return prepare() && new File(fileParser).exists() && new File(fileLexer).exists() && new File(fileRemoteParser).exists()
                && new File(fileRemoteParserInputText).exists();
    }

    public boolean checkForLaunch() {
        boolean success = true;
        try {
            ServerSocket serverSocket = new ServerSocket(AWPrefs.getDebugDefaultLocalPort());
            serverSocket.close();
        } catch (IOException e) {
            reportError("Cannot launch the remote parser because port "+AWPrefs.getDebugDefaultLocalPort()+" is already in use.");
            success = false;
        }
        return success;
    }

    public boolean launchRemoteParser() {
        if(!checkForLaunch())
            return false;

        String classPath = EngineRuntime.getClassPath(outputFileDir);        
        IDE.debugVerbose(debugger.getConsole(), getClass(), "Launch with path: "+classPath);

        try {
            remoteParserProcess = Runtime.getRuntime().exec(new String[] { "java", "-classpath", classPath, remoteParserClassName});
            new StreamWatcher(remoteParserProcess.getErrorStream(), "Launcher", debugger.getOutputPanel()).start();
            new StreamWatcher(remoteParserProcess.getInputStream(), "Launcher", debugger.getOutputPanel()).start();
        } catch (IOException e) {
            reportError("Cannot launch the remote parser:\n"+e.getLocalizedMessage());
            return false;
        }

        // Wait 1 second at least to let the process get started.
        // The Debugger class will then try several times to connect
        // to the remote parser
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // We don't care here if the sleep has been interrupted
        }

        return true;
    }

    public void streamWatcherDidStarted() {
    }

    public void streamWatcherDidReceiveString(String string) {
        debugger.getConsole().print(string, Console.LEVEL_NORMAL);
    }

    public void streamWatcherException(Exception e) {
        debugger.getConsole().print(e);
    }

    protected class ErrorReporter {

        public String title;
        public String message;
        public boolean hasErrors;

        public void setTitle(String title) {
            this.title = title;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void enable() {
            hasErrors = true;
        }

        public void reset() {
            hasErrors = false;
        }
    }


}
