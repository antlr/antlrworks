package org.antlr.works.debugger;

import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.foundation.XJUtils;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.works.dialog.DialogDebugInput;
import org.antlr.works.dialog.DialogDelegate;
import org.antlr.works.dialog.DialogProgress;
import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.editor.code.CodeGenerate;

import javax.swing.*;
import java.io.*;
import java.net.URL;

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

public class DebuggerLocal implements Runnable, DialogDelegate {

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

    protected boolean build;
    protected boolean success;
    protected boolean cancelled;
    protected boolean generateGlueCode;

    protected CodeGenerate codeGenerator;
    protected Debugger debugger;

    protected String inputText = "public static void main() { // this is a comment\n" +
                "    System.out.println(\"Hello, world!\");\n" +
                "}";

    protected DialogProgress progress;

    public DebuggerLocal(Debugger debugger) {
        this.debugger = debugger;
        this.codeGenerator = new CodeGenerate(debugger.editor);
        this.progress = new DialogProgress(debugger.editor);

        setOutputPath(EditorPreferences.getOutputPath());
    }

    public void setOutputPath(String path) {
        codeGenerator.setOutputPath(path);
    }

    public void setStartRule(String rule) {
        this.startRule = rule;
    }

    public void prepareAndLaunch(boolean build, boolean generateGlueCode) {
        this.build = build;
        this.generateGlueCode = generateGlueCode;

        if(!build) {
            if(!askUserForInputText())
                return;
        }

        progress.setInfo("Preparing...");
        progress.setProgress(0);
        progress.setProgressMax(3);
        progress.setDelegate(this);

        progress.display();

        cancelled = false;

        new Thread(this).start();
    }

    public void dialogDidCancel() {
        cancel();
    }

    public boolean askUserForInputText() {
        DialogDebugInput dialog = new DialogDebugInput();
        dialog.setInputText(inputText);
        if(dialog.runModal() == XJDialog.BUTTON_OK) {
            inputText = dialog.getInputText();
            return true;
        } else
            return false;
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

    public void run() {
        success = perform();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progress.close();
                if(build && success) {
                    success = askUserForInputText() && generateGlueCode() && launchRemoteParser();
                }
                debugger.debuggerLocalDidRun(success);
            }
        });
    }

    protected boolean perform() {

        prepare();
        if(cancelled())
            return false;

        new File(outputFileDir).mkdirs();

        if(build || generateGlueCode) {
            // Create the glue-code remote parser
            progress.setInfo("Generating...");
            if(!generateGlueCode() || cancelled())
                return false;
        }

        if(build) {
            progress.setProgress(1);

            // Generate code
            if(!generateCode() || cancelled())
                return false;

            progress.setProgress(2);

            // Compile code
            progress.setInfo("Compiling...");
            if(!compileCode() || cancelled())
                return false;
        }

        progress.setProgress(3);

        // Launch the glue-code remote parser
        if(build)
            return true;

        progress.setInfo("Launching...");
        return launchRemoteParser();
    }

    public void prepare() {
        fileParser = codeGenerator.getGeneratedTextFileName(false);
        fileLexer = codeGenerator.getGeneratedTextFileName(true);

        fileRemoteParser = XJUtils.concatPath(codeGenerator.getOutputPath(), remoteParserClassName+".java");
        fileRemoteParserInputText = XJUtils.concatPath(codeGenerator.getOutputPath(), remoteParserClassName+"_input.txt");

        outputFileDir = XJUtils.concatPath(codeGenerator.getOutputPath(), "classes");
    }

    public boolean isRequiredFilesExisting() {
        prepare();
        return new File(fileParser).exists() && new File(fileLexer).exists() && new File(fileRemoteParser).exists()
                && new File(fileRemoteParserInputText).exists();
    }

    public boolean generateGlueCode() {
        String templateFile = remoteParserTemplateName+".st";

        try {
            URL file = getClass().getResource(templateFile);

            String path = file.getPath();
            path = path.substring(0, path.length()-templateFile.length());

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
            XJAlert.display(debugger.editor.getWindowComponent(), "Generate Error", "Cannot launch the local debugger.\nException while generating the glue-code: "+e);
            return false;
        }
        return true;
    }

    public boolean generateCode() {
        if(false && new File(fileParser).exists() && new File(fileParser).exists() && new File(fileRemoteParser).exists()) {
            switch(XJAlert.displayAlert(debugger.editor.getWindowComponent(), "Debugger", "Generated code files already exists. Do you want to continue, re-generate the grammar or cancel ?",
                    "Cancel", "Re-generate", "Continue", 2))
            {
                case 0: // cancel
                    return false;

                case 1: // regenerate
                    return generate();

                case 2:
                    return true;
            }
            return false;
        } else
            return generate();
    }

    public boolean generate() {
        try {
            codeGenerator.generate(true);  // debug
        } catch (Exception e) {
            e.printStackTrace();            
            XJAlert.display(debugger.editor.getWindowComponent(), "Generate Error", "Cannot launch the local debugger.\nException while generating code: "+e);
            return false;
        }
        return true;
    }

    public boolean compileCode() {

        File f = new File(outputFileDir);
        if(false && f.exists()) {
            switch(XJAlert.displayAlert(debugger.editor.getWindowComponent(), "Debugger", "Compiled code files already exists. Do you want to continue, re-compile or cancel ?",
                    "Cancel", "Re-compile", "Continue", 2))
            {
                case 0: // cancel
                    return false;

                case 1: // recompile
                    return compile();

                case 2:
                    return true;
            }
            return false;

        } else {
            f.mkdirs();
            return compile();
        }
    }

    public boolean compile() {
        int result = 0;
        try {
            String compiler = EditorPreferences.getCompiler();
            String[] args;

            if(compiler.equalsIgnoreCase(EditorPreferences.COMPILER_JAVAC)) {
                args = new String[] { "javac", "-classpath",  System.getProperty("java.class.path"), "-d", outputFileDir, fileParser, fileLexer, fileRemoteParser };
                Process p = Runtime.getRuntime().exec(args);
                new StreamWatcher(p.getErrorStream(), "Compiler").start();
                new StreamWatcher(p.getInputStream(), "Compiler").start();
                result = p.waitFor();
            } else if(compiler.equalsIgnoreCase(EditorPreferences.COMPILER_JIKES)) {
                String jikesPath = XJUtils.concatPath(EditorPreferences.getJikesPath(), "jikes");
                args = new String[] { jikesPath, "-classpath",  System.getProperty("java.class.path"), "-d", outputFileDir, fileParser, fileLexer, fileRemoteParser };
                Process p = Runtime.getRuntime().exec(args);
                new StreamWatcher(p.getErrorStream(), "Compiler").start();
                new StreamWatcher(p.getInputStream(), "Compiler").start();
                result = p.waitFor();
            } else if(compiler.equalsIgnoreCase(EditorPreferences.COMPILER_INTEGRATED)) {
                args = new String[] { "-d", outputFileDir, fileParser, fileLexer, fileRemoteParser };
                result = com.sun.tools.javac.Main.compile(args);
            }

        } catch(Error e) {
            XJAlert.display(debugger.editor.getWindowComponent(), "Compiler Error", "An error occurred:\n"+e);
            return false;
        } catch(Exception e) {
            XJAlert.display(debugger.editor.getWindowComponent(), "Compiler Error", "An exception occurred:\n"+e);
            return false;
        }
        if(result != 0) {
            XJAlert.display(debugger.editor.getWindowComponent(), "Compiler Error", "Cannot launch the local debugger.\nCompiler error: "+result);
            return false;
        }
        return true;
    }

    public boolean launchRemoteParser() {
        String classPath = outputFileDir+File.pathSeparatorChar+System.getProperty("java.class.path")+File.pathSeparatorChar+".";

        try {
            // Use an array rather than a single string because white-space
            // are not correctly handled in a single string (why?)
            remoteParserProcess = Runtime.getRuntime().exec(new String[] { "java", "-classpath", classPath, remoteParserClassName});
            new StreamWatcher(remoteParserProcess.getErrorStream(), "Launcher").start();
            new StreamWatcher(remoteParserProcess.getInputStream(), "Launcher").start();
        } catch (IOException e) {
            XJAlert.display(debugger.editor.getWindowComponent(), "Runtime Error", "Cannot launch the local debugger.\nCannot launch the remote parser: "+e);
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

    private class StreamWatcher extends Thread {

        InputStream is;
        String type;

        StreamWatcher(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line=null;
                while ( (line = br.readLine()) != null)
                    System.out.println(type + ">" + line);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
