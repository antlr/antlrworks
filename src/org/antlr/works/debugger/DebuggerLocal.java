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
import org.antlr.works.dialog.DialogDebugInput;
import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.editor.code.CodeGenerate;
import org.antlr.works.util.Utils;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Method;
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

    protected Process remoteParserProcess;
    protected boolean remoteParserLaunched;

    protected boolean cancelled;
    protected boolean buildAndDebug;

    protected CodeGenerate codeGenerator;
    protected Debugger debugger;

    protected String inputText = "";

    protected XJDialogProgress progress;

    public DebuggerLocal(Debugger debugger) {
        this.debugger = debugger;
        this.codeGenerator = new CodeGenerate(debugger.editor);
        this.progress = new XJDialogProgress(debugger.editor);
    }

    public void setOutputPath(String path) {
        codeGenerator.setOutputPath(path);
    }

    public void setStartRule(String rule) {
        this.startRule = rule;
    }

    public void grammarChanged() {
        codeGenerator.grammarChanged();
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

        new Thread(this).start();
    }

    public void run() {
        prepare();

        if(buildAndDebug) {
            showProgress();
            generateAndCompileGrammar();
        }

        if(!cancelled())
            askUserForInputText();

        if(!cancelled())
            generateAndCompileGlueCode();

        if(!cancelled())
            launchRemoteParser();

        if(cancelled())
            notifyCancellation();
        else
            notifyCompletion();
    }

    protected void askUserForInputText() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    hideProgress();

                    DialogDebugInput dialog = new DialogDebugInput(debugger, debugger.getWindowComponent());
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
            e.printStackTrace();
        }
    }

    protected void notifyCancellation() {
        hideProgress();
    }

    protected void notifyCompletion() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                hideProgress();
                if(!cancelled())
                    debugger.debuggerLocalDidRun(buildAndDebug);
            }
        });
    }

    protected void prepare() {
        setOutputPath(EditorPreferences.getOutputPath());
        setStartRule(EditorPreferences.getStartSymbol());

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

    protected void generateAndCompileGlueCode() {
        progress.setInfo("Preparing...");
        progress.setIndeterminate(true);
        generateGlueCode();

        if(cancelled())
            return;

        compileGlueCode();
    }

    protected void generateGlueCode() {
        try {
            XJUtils.writeStringToFile(getInputText(), fileRemoteParserInputText);

            StringTemplateGroup group = new StringTemplateGroup("DebuggerLocalGroup");
            StringTemplate glueCode = group.getInstanceOf(remoteParserTemplatePath+remoteParserTemplateName);
            glueCode.setAttribute(ST_ATTR_CLASSNAME, remoteParserClassName);
            glueCode.setAttribute(ST_ATTR_INPUT_FILE, XJUtils.escapeString(fileRemoteParserInputText));
            glueCode.setAttribute(ST_ATTR_JAVA_PARSER, codeGenerator.getGeneratedClassName(false));
            glueCode.setAttribute(ST_ATTR_JAVA_LEXER, codeGenerator.getGeneratedClassName(true));
            glueCode.setAttribute(ST_ATTR_START_SYMBOL, startRule);

            XJUtils.writeStringToFile(glueCode.toString(), fileRemoteParser);
        } catch(Exception e) {
            e.printStackTrace();
            XJAlert.display(debugger.editor.getWindowContainer(), "Generate Error", "Cannot launch the local debugger.\nException while generating the glue-code: "+e);
            cancel();
        }
    }

    protected void compileGlueCode() {
        compileFiles(new String[] { fileRemoteParser });
    }

    protected void generateGrammar() {
        String error = null;
        try {
            if(!codeGenerator.generate(true))
                error = codeGenerator.getLastError();
        } catch (Exception e) {
            e.printStackTrace();
            error = e.toString();
        }

        if(error != null) {
            XJAlert.display(debugger.editor.getWindowContainer(), "Generate Error", "Cannot launch the local debugger.\nException while generating code: "+error);
            cancel();
        }
    }

    protected void compileGrammar() {
        new File(outputFileDir).mkdirs();
        compileFiles(new String[] { fileParser, fileLexer });
    }

    protected void compileFiles(String[] files) {
        int result = 0;
        try {
            String compiler = EditorPreferences.getCompiler();
            String classPath = System.getProperty("java.class.path")+":"+outputFileDir;

            if(compiler.equalsIgnoreCase(EditorPreferences.COMPILER_JAVAC)) {
                String[] args = new String[5+files.length];
                args[0] = "javac";
                args[1] = "-classpath";
                args[2] = classPath;
                args[3] = "-d";
                args[4] = outputFileDir;
                for(int i=0; i<files.length; i++)
                    args[5+i] = files[i];

                //args = new String[] { "javac", "-classpath",  System.getProperty("java.class.path"), "-d", outputFileDir, fileParser, fileLexer, fileRemoteParser };
                System.out.println("Compile:"+ Utils.toString(args));
                Process p = Runtime.getRuntime().exec(args);
                new StreamWatcher(p.getErrorStream(), "Compiler").start();
                new StreamWatcher(p.getInputStream(), "Compiler").start();
                result = p.waitFor();
            } else if(compiler.equalsIgnoreCase(EditorPreferences.COMPILER_JIKES)) {
                String jikesPath = XJUtils.concatPath(EditorPreferences.getJikesPath(), "jikes");

                String[] args = new String[5+files.length];
                args[0] = jikesPath;
                args[1] = "-classpath";
                args[2] = classPath;
                args[3] = "-d";
                args[4] = outputFileDir;
                for(int i=0; i<files.length; i++)
                    args[5+i] = files[i];

                //args = new String[] { jikesPath, "-classpath",  System.getProperty("java.class.path"), "-d", outputFileDir, fileParser, fileLexer, fileRemoteParser };
                Process p = Runtime.getRuntime().exec(args);
                new StreamWatcher(p.getErrorStream(), "Compiler").start();
                new StreamWatcher(p.getInputStream(), "Compiler").start();
                result = p.waitFor();
            } else if(compiler.equalsIgnoreCase(EditorPreferences.COMPILER_INTEGRATED)) {
                String[] args = new String[2+files.length];
                args[0] = "-d";
                args[1] = outputFileDir;
                for(int i=0; i<files.length; i++)
                    args[2+i] = files[i];

//                args = new String[] { "-d", outputFileDir, fileParser, fileLexer, fileRemoteParser };
                Class javac = Class.forName("com.sun.tools.javac.Main");
                Class[] p = new Class[] { String[].class };
                Method m = javac.getMethod("compile", p);
                Object[] a = new Object[] { args };
                Object r = m.invoke(javac.newInstance(), a);
                result = ((Integer)r).intValue();
                //result = com.sun.tools.javac.Main.compile(args);
            }

        } catch(Error e) {
            XJAlert.display(debugger.editor.getWindowContainer(), "Compiler Error", "An error occurred:\n"+e);
            cancel();
        } catch(Exception e) {
            XJAlert.display(debugger.editor.getWindowContainer(), "Compiler Error", "An exception occurred:\n"+e);
            cancel();
        }
        if(result != 0) {
            XJAlert.display(debugger.editor.getWindowContainer(), "Compiler Error", "Cannot launch the local debugger.\nCompiler error: "+result);
            cancel();
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
            XJAlert.display(debugger.editor.getWindowContainer(), "Launch Error", "Cannot launch the remote parser because port "+Debugger.DEFAULT_LOCAL_PORT+" is already in use.");
            success = false;
        }
        return success;
    }

    public boolean launchRemoteParser() {
        if(!checkForLaunch())
            return false;

        String classPath = outputFileDir+File.pathSeparatorChar+System.getProperty("java.class.path")+File.pathSeparatorChar+".";

        try {
            // Use an array rather than a single string because white-space
            // are not correctly handled in a single string (why?)
            remoteParserProcess = Runtime.getRuntime().exec(new String[] { "java", "-classpath", classPath, remoteParserClassName});
            new StreamWatcher(remoteParserProcess.getErrorStream(), "Launcher").start();
            StreamWatcher sw = new StreamWatcher(remoteParserProcess.getInputStream(), "Launcher");
            sw.setDelegate(debugger);
            sw.start();
        } catch (IOException e) {
            XJAlert.display(debugger.editor.getWindowContainer(), "Runtime Error", "Cannot launch the local debugger.\nCannot launch the remote parser: "+e);
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

    public interface StreamWatcherDelegate {
        public void streamWatcherDidStarted();
        public void streamWatcherDidReceiveString(String string);
    }

    public class StreamWatcher extends Thread {

        protected InputStream is;
        protected String type;
        protected StreamWatcherDelegate delegate;

        public StreamWatcher(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void setDelegate(StreamWatcherDelegate delegate) {
            this.delegate = delegate;
        }

        public void run() {
            try {
                if(delegate != null)
                    delegate.streamWatcherDidStarted();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ( (line = br.readLine()) != null) {
                    debugger.editor.console.println(type + ">" + line);
                    if(delegate != null)
                        delegate.streamWatcherDidReceiveString(line+"\n");
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
