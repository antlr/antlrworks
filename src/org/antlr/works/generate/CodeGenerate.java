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

import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.utils.XJDialogProgress;
import edu.usfca.xj.foundation.XJUtils;
import org.antlr.Tool;
import org.antlr.tool.ErrorManager;
import org.antlr.tool.Grammar;
import org.antlr.works.editor.EditorProvider;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.syntax.element.ElementGrammarName;
import org.antlr.works.utils.Console;
import org.antlr.works.utils.ErrorListener;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class CodeGenerate implements Runnable {

    protected String outputPath;
    protected boolean debug = true;

    protected EditorProvider provider;
    protected CodeGenerateDelegate delegate;
    protected ErrorListener errorListener;

    protected long dateOfModificationOnDisk = 0;

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
        try {
            Grammar g = provider.getEngineGrammar().getParserGrammar();
            if(g == null) {
                g = provider.getEngineGrammar().getLexerGrammar();
            }
            if(g != null) {
                return (String)g.getOption("language");
            } else {
                return null;
            }
        } catch (Exception e) {
            provider.getConsole().print(e);
        }
        return null;
    }

    public String getGrammarName() {
        return provider.getEngineGrammar().getName();
    }

    public String getLastError() {
        return errorListener.getFirstErrorMessage();
    }

    public boolean generate() throws Exception {
        errorListener.clear();
        ErrorManager.setErrorListener(errorListener);

        String[] params;
        if(debug)
            params = new String[] { "-debug", "-o", getOutputPath(), "-lib", provider.getFileFolder(), provider.getFilePath() };
        else
            params = new String[] { "-o", getOutputPath(), "-lib", provider.getFileFolder(), provider.getFilePath() };

        Tool antlr = new Tool(params);
        antlr.process();

        boolean success = !errorListener.hasErrors();
        if(success) {
            dateOfModificationOnDisk = provider.getDocument().getDateOfModificationOnDisk();
        }
        return success;
    }

    public String getGeneratedClassName(boolean lexer) throws Exception {
        String name;
        if(lexer) {
            name = provider.getEngineGrammar().getLexerGrammar().name+"Lexer";
        } else {
            name = provider.getEngineGrammar().getParserGrammar().name+"Parser";
        }
        return name;
    }

    public String getGeneratedTextFileName(boolean lexer) throws Exception {
        return XJUtils.concatPath(getOutputPath(), getGeneratedClassName(lexer)+".java");
    }

    public boolean isGeneratedTextFileExisting(boolean lexer) {
        try {
            return new File(getGeneratedTextFileName(lexer)).exists();
        } catch (Exception e) {
            provider.getConsole().print(e);
        }
        return false;
    }

    public boolean isFileModifiedSinceLastGeneration() {
        return dateOfModificationOnDisk != provider.getDocument().getDateOfModificationOnDisk();
    }

    public boolean supportsLexer() {
        int type = provider.getEngineGrammar().getType();
        return type == ElementGrammarName.COMBINED || type == ElementGrammarName.LEXER;
    }

    public boolean supportsParser() {
        int type = provider.getEngineGrammar().getType();
        return type == ElementGrammarName.COMBINED || type == ElementGrammarName.PARSER || type == ElementGrammarName.TREEPARSER;
    }

    public String getGeneratedText(boolean lexer) throws Exception {
        return XJUtils.getStringFromFile(getGeneratedTextFileName(lexer));
    }

    public void generateInThread(Container parent) {
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

        provider.getConsole().setMode(Console.MODE_VERBOSE);

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
