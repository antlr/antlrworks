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

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.antlr.works.IDE;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.debugger.DebuggerEngine;
import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.debugger.tivo.DBRecorder;
import org.antlr.works.dialog.DebuggerInputDialog;
import org.antlr.works.dialog.DialogTestTemplate;
import org.antlr.works.generate.CodeGenerate;
import org.antlr.works.grammar.element.ElementBlock;
import org.antlr.works.grammar.element.ElementGrammarName;
import org.antlr.works.grammar.syntax.GrammarSyntaxParser;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.*;
import org.antlr.xjlib.appkit.frame.XJDialog;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.utils.XJDialogProgress;
import org.antlr.xjlib.appkit.utils.XJDialogProgressDelegate;
import org.antlr.xjlib.foundation.XJUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DBLocal implements Runnable, XJDialogProgressDelegate, StreamWatcherDelegate {

    public static final String remoteParserClassName = "__Test__";
    public static final String parserGlueCodeTemplatePath = "templates/";
    public static final String parserGlueCodeTemplateName = "DBParserGlueCode";
    public static final String treeParserGlueCodeTemplateName = "DBTreeParserGlueCode";
    public static final String testRigTemplateSuffix = "__testrig";

    public static final String ST_ATTR_IMPORT = "import";
    public static final String ST_ATTR_CLASSNAME = "class_name";
    public static final String ST_ATTR_INPUT_FILE = "input_file";
    public static final String ST_ATTR_JAVA_PARSER = "java_parser";
    public static final String ST_ATTR_JAVA_LEXER = "java_lexer";
    public static final String ST_ATTR_START_SYMBOL = "start_symbol";
    public static final String ST_ATTR_GRAMMAR_NAME = "grammar_name";
    public static final String ST_ATTR_DEBUG_PORT = "port";

    protected String outputFileDir;

    protected List<String> grammarGeneratedFiles;
    protected String fileRemoteParser;
    protected String fileRemoteParserInputTextFile;
    protected String fileRemoteParserTemplateTextFile;

    protected String startRule;
    protected String lastStartRule;

    protected Process remoteParserProcess;

    protected boolean cancelled;
    protected int options;

    protected CodeGenerate codeGenerator;
    protected DebuggerTab debuggerTab;

    protected int inputMode;
    protected int lastInputMode;
    protected String testTemplateMode;
    protected String lastTestTemplateMode;
    protected String testTemplateClass;
    protected String lastTestTemplateClass;
    protected String inputFile;
    protected String lastInputFile;
    protected String testTemplateText;
    protected String lastTestTemplateText;
    protected String inputText;
    protected String rawInputText;

    protected XJDialogProgress progress;
    protected ErrorReporter error = new ErrorReporter();

    protected int debugPort = -1;
    protected boolean debugPortChanged = true;

    public DBLocal(DebuggerTab debuggerTab) {
        this.debuggerTab = debuggerTab;
    }

    public void close() {
        // todo same when creating it
        if(codeGenerator != null) {
            codeGenerator.close();
        }
        codeGenerator = null;
        debuggerTab = null;
    }

    public boolean canDebugAgain() {
        if(inputMode == 0) {
            return inputText != null;
        } else {
            return inputFile != null && new File(inputFile).exists();
        }
    }

    public void dialogDidCancel() {
        cancel();
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
            progress = new XJDialogProgress(debuggerTab.getContainer());
        progress.setInfo("Preparing...");
        progress.setIndeterminate(false);
        progress.setProgress(0);
        progress.setProgressMax(3);
        progress.setDelegate(this);
        progress.display();
    }

    public void hideProgress() {
        if(progress != null)
            progress.close();
    }

    private boolean optionBuild() {
        return (options & DebuggerTab.OPTION_BUILD) > 0 || debugPortChanged;
    }

    private boolean optionAgain() {
        return (options & DebuggerTab.OPTION_AGAIN) > 0;
    }

    private boolean optionRun() {
        return (options & DebuggerTab.OPTION_RUN) > 0;
    }

    public void showEditTestRig() {
        DialogTestTemplate dialog = new DialogTestTemplate(debuggerTab, debuggerTab.getContainer());
        dialog.runModal();
    }

    public void prepareAndLaunch(int options) {
        this.options = options;
        cancelled = false;

        if(debugPort != AWPrefs.getDebugDefaultLocalPort()) {
            debugPort = AWPrefs.getDebugDefaultLocalPort();
            debugPortChanged = true;
        } else {
            debugPortChanged = false;
        }

        if(optionBuild()) {
            showProgress();
        }

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
            if(optionBuild()) generateAndCompileGrammar();
            if(!cancelled() && !optionAgain()) askUserForInputText();
            if(!cancelled() && !AWPrefs.TEST_RIG_MODE_CLASS.equals(testTemplateMode)) generateAndCompileGlueCode(optionBuild());
            if(!cancelled()) generateInputText();
            if(!cancelled()) launchRemoteParser();
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

                    DebuggerInputDialog dialog = new DebuggerInputDialog(debuggerTab, debuggerTab.getContainer());
                    dialog.setInputText(rawInputText);
                    if(dialog.runModal() == XJDialog.BUTTON_OK) {
                        rawInputText = dialog.getRawInputText();
                        inputText = dialog.getInputText();
                        inputFile = dialog.getInputFile();
                        inputMode = dialog.getInputMode();
                        startRule = dialog.getRule();
                        showProgress();
                    } else
                        cancel();
                }
            });
        } catch (Exception e) {
            debuggerTab.getConsole().println(e);
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
                if(XJAlert.displayAlert(debuggerTab.getContainer(), error.title, error.message, "Show Console", "OK", 1, 1) == 0) {
                    debuggerTab.selectConsoleTab();
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
                boolean didRun = false;
                if(!cancelled()) {
                    didRun = debuggerTab.debuggerLocalDidRun(optionBuild());
                    if (optionRun() && didRun) {
                        runThroughRecorder();
                    }
                }
            }
        });
    }

    private void runThroughRecorder() {
        long t = System.currentTimeMillis();
        long timeout = AWPrefs.getDebugLaunchTimeout()*1000;

        // wait for debugger to be alive, but make sure to have a timeout.
        while (!debuggerTab.getRecorder().isAlive()) {
            if (System.currentTimeMillis()-t > timeout) return;
        }

        debuggerTab.getRecorder().goToEnd();

        // wait for the recorder to go to the end. Make sure it wasn't stopped
        while (!debuggerTab.getRecorder().isAtEnd() && debuggerTab.getRecorder().getStatus() != DBRecorder.STATUS_STOPPED);

        debuggerTab.getRecorder().requestStop();
        debuggerTab.selectConsoleTab();
    }

    protected boolean prepare() {
        String testRigFullPath = "";
        String grammarIdentifier = "";
        String qualifiedFileName = debuggerTab.getDelegate().getDocument().getDocumentPath();
        if (qualifiedFileName != null) {
            testRigFullPath = XJUtils.getPathByDeletingPathExtension(qualifiedFileName) + testRigTemplateSuffix + ".st";
            grammarIdentifier = qualifiedFileName.toUpperCase();
        }
        try {
            testTemplateMode = AWPrefs.getTestRigTemplateMode(grammarIdentifier);
            testTemplateText = getTestRigTemplateFromFile(testRigFullPath);
            testTemplateClass = AWPrefs.getTestRigTemplateClass(grammarIdentifier);
            codeGenerator = debuggerTab.getDelegate().getCodeGenerate();
            grammarGeneratedFiles = codeGenerator.getGeneratedFileNames();

            fileRemoteParser = XJUtils.concatPath(codeGenerator.getOutputPath(), remoteParserClassName+".java");
            fileRemoteParserInputTextFile = XJUtils.concatPath(codeGenerator.getOutputPath(), remoteParserClassName+"_input.txt");
            fileRemoteParserTemplateTextFile = XJUtils.concatPath(codeGenerator.getOutputPath(), remoteParserClassName+"_template.st");

            outputFileDir = XJUtils.concatPath(codeGenerator.getOutputPath(), "classes");
            new File(outputFileDir).mkdirs();
        } catch(Exception e) {
            debuggerTab.getConsole().println(e);
            String msg = ErrorListener.getThreadInstance().getFirstErrorMessage();
            StringBuilder sb = new StringBuilder("Error while preparing the grammar:\n");
            if(msg != null) {
                sb.append(msg);
                sb.append("\n");
            }
            sb.append(e.toString());
            reportError(sb.toString());
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
            debuggerTab.getDelegate().getGrammarEngine().analyze();
        } catch (Exception e) {
            debuggerTab.getConsole().println(e);
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
            debuggerTab.getConsole().println(e);
            errorMessage = e.toString();
        } catch (OutOfMemoryError e) {
            debuggerTab.getConsole().println(e);
            errorMessage = e.toString();
        }

        if(errorMessage != null) {
            reportError("Error while generating the grammar:\n"+errorMessage);
        }
    }

    protected void compileGrammar() {
        XJUtils.deleteDirectory(outputFileDir);
        new File(outputFileDir).mkdirs();
        compileFiles(grammarGeneratedFiles.toArray(new String[grammarGeneratedFiles.size()]));
    }

    protected void generateAndCompileGlueCode(boolean build) {
        progress.setInfo("Preparing...");
        progress.setIndeterminate(true);

        if(!build && lastStartRule != null && startRule.equals(lastStartRule) &&
                inputFile.equals(lastInputFile) && lastInputMode == inputMode &&
                testTemplateClass.equals(lastTestTemplateClass) && lastTestTemplateMode.equals(testTemplateMode) &&
                testTemplateText.equals(lastTestTemplateText))
            return;

        lastStartRule = startRule;
        lastTestTemplateMode = testTemplateMode;
        lastTestTemplateClass = testTemplateClass;
        lastTestTemplateText = testTemplateText;
        lastInputMode = inputMode;
        lastInputFile = inputFile;

        generateGlueCode();

        if(cancelled())
            return;

        compileGlueCode();
    }

    protected String getLexerName() throws Exception {
        String lexer = debuggerTab.getDelegate().getGrammarEngine().getGeneratedClassName(ElementGrammarName.LEXER);
        if(lexer == null) {
            // The lexer name can be null if the grammar is a treeparser or a parser
            // Try to lookup the name used by tokenVocab and use it as the lexer name
            lexer = debuggerTab.getDelegate().getTokenVocab();
        }
        return lexer;
    }

    protected void generateGlueCode() {
        try {
            boolean isTreeGrammar = debuggerTab.getDelegate().getGrammarEngine().getType() == ElementGrammarName.TREEPARSER;
            String templateName = isTreeGrammar ? treeParserGlueCodeTemplateName : parserGlueCodeTemplateName;
            String lexerName = isTreeGrammar ? (getLexerName()+"Lexer") : getLexerName();
            String parserName = isTreeGrammar ? (debuggerTab.getDelegate().getTokenVocab()+"Parser") :
                    (debuggerTab.getDelegate().getGrammarEngine().getGeneratedClassName(ElementGrammarName.PARSER));
            StringTemplateGroup group;
            StringTemplate glueCode;
            if (AWPrefs.TEST_RIG_MODE_TEXT.equals(testTemplateMode)) {
                if ("".equals(testTemplateText)) {
                    group = new StringTemplateGroup("DebuggerLocalGroup", DefaultTemplateLexer.class);
                    glueCode = group.getInstanceOf(parserGlueCodeTemplatePath +templateName);
                } else {
                    generateTestTemplateTextFile();
                    group = new StringTemplateGroup("DebuggerLocalGroup", codeGenerator.getOutputPath());
                    glueCode = group.getInstanceOf(remoteParserClassName + "_template");
                }
            } else {
                return;
            }
            glueCode.setAttribute(ST_ATTR_IMPORT, getCustomImports());
            glueCode.setAttribute(ST_ATTR_CLASSNAME, remoteParserClassName);
            if(inputMode == 0) {
                glueCode.setAttribute(ST_ATTR_INPUT_FILE, XJUtils.escapeString(fileRemoteParserInputTextFile));
            } else {
                glueCode.setAttribute(ST_ATTR_INPUT_FILE, XJUtils.escapeString(inputFile));
            }
            glueCode.setAttribute(ST_ATTR_JAVA_PARSER, parserName);
            glueCode.setAttribute(ST_ATTR_JAVA_LEXER, lexerName);
            glueCode.setAttribute(ST_ATTR_START_SYMBOL, startRule);
            glueCode.setAttribute(ST_ATTR_DEBUG_PORT, AWPrefs.getDebugDefaultLocalPort());
            glueCode.setAttribute(ST_ATTR_GRAMMAR_NAME, debuggerTab.getDelegate().getGrammarName());

            XJUtils.writeStringToFile(glueCode.toString(), fileRemoteParser);
        } catch(Exception e) {
            debuggerTab.getConsole().println(e);
            reportError("Error while generating the glue-code:\n"+e.toString());
        }
    }

    /**
     * Returns a string of import statement based on the package declaration inside any @header block
     */
    private String getCustomImports() {
        List<ElementBlock> blocks = debuggerTab.getBlocks();
        if(blocks == null || blocks.isEmpty()) {
            return "";
        }

        Set<String> imports = new HashSet<String>();
        for (ElementBlock block : blocks) {
            if (!block.name.equals(GrammarSyntaxParser.PARSER_HEADER_BLOCK_NAME) && !block.name.equals(GrammarSyntaxParser.LEXER_HEADER_BLOCK_NAME))
            {
                continue;
            }

            List<ATEToken> tokens = block.internalTokens;
            for(int j = 0; j < tokens.size(); j++) {
                ATEToken token = tokens.get(j);
                if (token.type == ATESyntaxLexer.TOKEN_ID && token.getAttribute().equals("package")) {
                    StringBuilder sb = new StringBuilder();
                    j++;
                    while (j < tokens.size()) {
                        ATEToken t = tokens.get(j);
                        String at = t.getAttribute();
                        if (at.equals(";"))
                            break;
                        sb.append(at);
                        j++;
                    }
                    imports.add(sb.toString());
                }
            }
        }

        if(imports.isEmpty()) {
            return "";
        }

        StringBuilder importLines = new StringBuilder();
        for (String importName : imports) {
            importLines.append("import ");
            importLines.append(importName);
            importLines.append(".*;\n");
        }
        return importLines.toString();
    }

    protected void compileGlueCode() {
        try {
            boolean isTreeGrammar = debuggerTab.getDelegate().getGrammarEngine().getType() == ElementGrammarName.TREEPARSER;
            String lexerName = XJUtils.concatPath(codeGenerator.getOutputPath(), (isTreeGrammar ? (getLexerName()+"Lexer.java") : (getLexerName()+".java")));
            String parserName = XJUtils.concatPath(codeGenerator.getOutputPath(), (isTreeGrammar ? (debuggerTab.getDelegate().getTokenVocab()+"Parser.java") :
                    (debuggerTab.getDelegate().getGrammarEngine().getGeneratedClassName(ElementGrammarName.PARSER)+".java")));
            compileFiles(new String[] { lexerName});
            compileFiles(new String[] { parserName});
            compileFiles(new String[] { fileRemoteParser});
        } catch (Exception e) {
            debuggerTab.getConsole().println(e);
            reportError("Error :\n"+e.toString());
        }
    }

    protected void compileFiles(String[] files) {
        String error = DebuggerEngine.compileFiles(debuggerTab.getConsole(), files, outputFileDir, this);
        if(error != null)
            reportError(error);
    }

    protected void generateInputText() {
        try {
            XJUtils.writeStringToFile(inputText, fileRemoteParserInputTextFile);
        } catch (IOException e) {
            debuggerTab.getConsole().println(e);
            reportError("Error while generating the input text:\n"+e.toString());
        }
    }

    protected void generateTestTemplateTextFile() {
        try {
            XJUtils.writeStringToFile(testTemplateText, fileRemoteParserTemplateTextFile);
        } catch (IOException e) {
            debuggerTab.getConsole().println(e);
            reportError("Error while generating the test template text file:\n"+e.toString());
        }
    }

    private String getTestRigTemplateFromFile(String testRigFullPath) {
        try {
            return Utils.stringFromFile(testRigFullPath);
        } catch (IOException ioe) {
            // do nothing. no template file found.
        }
        return "";
    }

    public boolean isRequiredFilesExisting() {
        if(!prepare()) return false;

        if(!new File(fileRemoteParser).exists()) return false;

        if(inputMode == 0 && !new File(fileRemoteParserInputTextFile).exists()) return false;
        if(inputMode == 1 && !new File(inputFile).exists()) return false;

        for(String file : grammarGeneratedFiles) {
            if(!new File(file).exists()) return false;
        }

        return true;
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

        String classPath = DebuggerEngine.getClassPath(outputFileDir);
        IDE.debugVerbose(debuggerTab.getConsole(), getClass(), "Launch with path: "+classPath);

        try {
            String classNameToRun = remoteParserClassName;
            if (AWPrefs.TEST_RIG_MODE_CLASS.equals(testTemplateMode)) {
                Class.forName(testTemplateClass);
                classNameToRun = testTemplateClass;
            }
            remoteParserProcess = Runtime.getRuntime().exec(new String[] { "java", "-classpath", classPath, classNameToRun});
            new StreamWatcher(remoteParserProcess.getErrorStream(), "Launcher", debuggerTab.getOutputPanel()).start();
            new StreamWatcher(remoteParserProcess.getInputStream(), "Launcher", debuggerTab.getOutputPanel()).start();
        } catch (IOException e) {
            reportError("Cannot launch the remote parser:\n"+e.toString()+"\nIt is possible that some errors prevented the parser from launching. Check the output panel of the debugger and any other output console in your system to see if an error has been reported from the parser and try again.");
            return false;
        } catch (ClassNotFoundException e) {
            reportError("Cannot launch the remote parser:\n"+e.toString()+"\nIt is possible that some errors prevented the parser from launching. Check the output panel of the debugger and any other output console in your system to see if an error has been reported from the parser and try again.");
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

    public void streamWatcherDidStart() {
    }

    public synchronized void streamWatcherDidReceiveString(String string) {
        debuggerTab.getConsole().print(string, Console.LEVEL_NORMAL);
    }

    public synchronized void streamWatcherException(Exception e) {
        debuggerTab.getConsole().println(e);
    }

    protected static class ErrorReporter {

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
