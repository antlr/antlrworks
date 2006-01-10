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

import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.utils.XJDialogProgress;
import edu.usfca.xj.appkit.utils.XJDialogProgressDelegate;
import edu.usfca.xj.foundation.XJUtils;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.works.IDE;
import org.antlr.works.engine.EngineCompiler;
import org.antlr.works.generate.CodeGenerate;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.StreamWatcher;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class DebuggerLocal implements Runnable, XJDialogProgressDelegate {

    public static final String remoteParserClassName = "Test";
    // @todo put this in the check-list
    public static final String remoteParserTemplatePath = "org/antlr/works/debugger/";
    public static final String remoteParserTemplateName = "RemoteParserGlueCode";

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
    protected boolean remoteParserLaunched;

    protected boolean cancelled;
    protected boolean buildAndDebug;

    protected CodeGenerate codeGenerator;
    protected Debugger debugger;

    protected String inputText;

    protected XJDialogProgress progress;
    protected ErrorReporter error = new ErrorReporter();

    public DebuggerLocal(Debugger debugger) {
        this.debugger = debugger;
        this.codeGenerator = new CodeGenerate(debugger.editor, null);
        this.progress = new XJDialogProgress(debugger.editor.getJavaContainer());
    }

    public void setOutputPath(String path) {
        codeGenerator.setOutputPath(path);
    }

    public void setStartRule(String rule) {
        this.startRule = rule;
    }

    public void dialogDidCancel() {
        cancel();
    }

    public String getInputText() {
        return inputText;
    }

    public synchronized void cancel() {
        cancelled = true;
    }

    public synchronized boolean cancelled() {
        return cancelled;
    }

    public void showProgress() {
        progress.setInfo("Preparing...");
        progress.setIndeterminate(false);
        progress.setProgress(0);
        progress.setProgressMax(2);
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

        prepare();

        if(buildAndDebug)
            generateAndCompileGrammar();

        if(!cancelled())
            askUserForInputText();

        if(!cancelled())
            generateAndCompileGlueCode();

        if(!cancelled())
            generateInputText();

        if(!cancelled())
            launchRemoteParser();

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
            debugger.editor.console.print(e);
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
                XJAlert.display(debugger.editor.getWindowContainer(), error.title, error.message);
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

    protected void prepare() {
        setOutputPath(AWPrefs.getOutputPath());
        setStartRule(AWPrefs.getStartSymbol());

        fileParser = codeGenerator.getGeneratedTextFileName(false);
        fileLexer = codeGenerator.getGeneratedTextFileName(true);

        fileRemoteParser = XJUtils.concatPath(codeGenerator.getOutputPath(), remoteParserClassName+".java");
        fileRemoteParserInputText = XJUtils.concatPath(codeGenerator.getOutputPath(), remoteParserClassName+"_input.txt");

        outputFileDir = XJUtils.concatPath(codeGenerator.getOutputPath(), "classes");
        new File(outputFileDir).mkdirs();
    }

    protected void generateAndCompileGrammar() {
        progress.setInfo("Generating...");
        progress.setProgress(1);
        generateGrammar();

        if(cancelled())
            return;

        progress.setInfo("Compiling...");
        progress.setProgress(2);
        compileGrammar();
    }

    protected void generateGrammar() {
        String errorMessage = null;
        try {
            if(!codeGenerator.generate())
                errorMessage = codeGenerator.getLastError();
        } catch (Exception e) {
            debugger.editor.console.print(e);
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

    protected void generateAndCompileGlueCode() {
        progress.setInfo("Preparing...");
        progress.setIndeterminate(true);

        if(lastStartRule != null && startRule.equals(lastStartRule))
            return;
        lastStartRule = startRule;

        generateGlueCode();

        if(cancelled())
            return;

        compileGlueCode();
    }

    protected void generateGlueCode() {
        try {
            StringTemplateGroup group = new StringTemplateGroup("DebuggerLocalGroup");
            StringTemplate glueCode = group.getInstanceOf(remoteParserTemplatePath+remoteParserTemplateName);
            glueCode.setAttribute(ST_ATTR_CLASSNAME, remoteParserClassName);
            glueCode.setAttribute(ST_ATTR_INPUT_FILE, XJUtils.escapeString(fileRemoteParserInputText));
            glueCode.setAttribute(ST_ATTR_JAVA_PARSER, codeGenerator.getGeneratedClassName(false));
            glueCode.setAttribute(ST_ATTR_JAVA_LEXER, codeGenerator.getGeneratedClassName(true));
            glueCode.setAttribute(ST_ATTR_START_SYMBOL, startRule);

            XJUtils.writeStringToFile(glueCode.toString(), fileRemoteParser);
        } catch(Exception e) {
            debugger.editor.console.print(e);
            reportError("Error while generating the glue-code:\n"+e.getLocalizedMessage());
        }
    }

    protected void compileGlueCode() {
        compileFiles(new String[] { fileRemoteParser });
    }

    protected void compileFiles(String[] files) {
        String error = EngineCompiler.compileFiles(files, outputFileDir, debugger);
        if(error != null)
            reportError(error);
    }

    protected void generateInputText() {
        try {
            XJUtils.writeStringToFile(getInputText(), fileRemoteParserInputText);
        } catch (IOException e) {
            debugger.editor.console.print(e);
            reportError("Error while generating the input text:\n"+e.getLocalizedMessage());
        }
    }

    public boolean isRequiredFilesExisting() {
        prepare();
        return new File(fileParser).exists() && new File(fileLexer).exists() && new File(fileRemoteParser).exists()
                && new File(fileRemoteParserInputText).exists();
    }

    public boolean checkForLaunch() {
        boolean success = true;
        try {
            ServerSocket serverSocket = new ServerSocket(Debugger.DEFAULT_LOCAL_PORT);
            serverSocket.close();
        } catch (IOException e) {
            reportError("Cannot launch the remote parser because port "+Debugger.DEFAULT_LOCAL_PORT+" is already in use.");
            success = false;
        }
        return success;
    }

    public boolean launchRemoteParser() {
        if(!checkForLaunch())
            return false;

        String classPath = outputFileDir;
        classPath += File.pathSeparatorChar+ IDE.getApplicationPath();
        classPath += File.pathSeparatorChar+System.getProperty("java.class.path");
        classPath += File.pathSeparatorChar+".";

        debugger.editor.console.println("Launch with path ="+classPath);

        try {
            // Use an array rather than a single string because white-space
            // are not correctly handled in a single string (why?)
            remoteParserProcess = Runtime.getRuntime().exec(new String[] { "java", "-classpath", classPath, remoteParserClassName});
            new StreamWatcher(remoteParserProcess.getErrorStream(), "Launcher", debugger).start();
            new StreamWatcher(remoteParserProcess.getInputStream(), "Launcher", debugger).start();
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
