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

package org.antlr.works.generate;

import edu.usfca.xj.appkit.frame.XJFrame;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.utils.XJDialogProgress;
import edu.usfca.xj.foundation.XJUtils;
import org.antlr.Tool;
import org.antlr.works.editor.EditorProvider;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.ErrorListener;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class CodeGenerate implements Runnable {

    protected String outputPath;
    protected boolean debug = true;

    protected EditorProvider provider;
    protected CodeGenerateDelegate delegate;
    protected ErrorListener errorListener;

    public CodeGenerate(EditorProvider provider, CodeGenerateDelegate delegate) {
        this.provider = provider;
        this.delegate = delegate;
        this.outputPath = AWPrefs.getOutputPath();

        errorListener = new ErrorListener();
        errorListener.setPrintToConsole(false);
        errorListener.setForwardListener(ErrorListener.shared());
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setOutputPath(String path) {
        this.outputPath = path;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getGrammarLanguage() {
        return (String)provider.getGrammar().getParserGrammar().getOption("language");
    }

    public String getGrammarName() {
        return provider.getGrammar().getName();
    }

    public String getLastError() {
        if(errorListener.errors.isEmpty())
            return null;
        else
            return errorListener.errors.get(0).toString();
    }

    public boolean generate() throws Exception {
        errorListener.clear();

        String[] params;
        if(debug)
            params = new String[] { "-debug", "-o", getOutputPath(), provider.getFilePath() };
        else
            params = new String[] { "-o", getOutputPath(), provider.getFilePath() };

        Tool antlr = new Tool(params);
        antlr.process();

        return !errorListener.hasErrors();
    }

    public String getGeneratedClassName(boolean lexer) {
        String name;
        if(lexer)
            name = provider.getGrammar().getParserGrammar().name+"Lexer";
        else
            name = provider.getGrammar().getParserGrammar().name;
        return name;
    }

    public String getGeneratedTextFileName(boolean lexer) {
        return XJUtils.concatPath(getOutputPath(), getGeneratedClassName(lexer)+".java");
    }

    public boolean isGeneratedTextFileExisting(boolean lexer) {
        return new File(getGeneratedTextFileName(lexer)).exists();
    }

    public String getGeneratedText(boolean lexer) {
        try {
            return XJUtils.getStringFromFile(getGeneratedTextFileName(lexer));
        } catch (IOException e) {
            return null;
        }
    }

    public void generateInThread(XJFrame parent) {
        progress = new XJDialogProgress(parent);
        progress.setInfo("Generating...");
        progress.setCancellable(false);
        progress.setIndeterminate(true);
        progress.display();

        new Thread(this).start();
    }

    public void generateInThreadDidTerminate() {
        progress.close();
        if(generateError != null)
            XJAlert.display(provider.getWindowContainer(), "Error", "Cannot generate the grammar because:\n"+generateError);
        else {
            if(delegate == null || delegate.codeGenerateDisplaySuccess())
                XJAlert.display(provider.getWindowContainer(), "Success", "The grammar has been successfully generated in path:\n"+outputPath);
            else
                delegate.codeGenerateDidComplete();
        }
    }

    protected String generateError = null;
    protected XJDialogProgress progress;

    public void run() {
        generateError = null;

        try {
            if(!generate()) {
                generateError = getLastError();
            }
        } catch (Exception e) {
            generateError = e.toString();
            provider.getConsole().print(e);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                generateInThreadDidTerminate();
            }
        });
    }

}
