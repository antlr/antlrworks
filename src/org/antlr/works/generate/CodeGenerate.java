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

import org.antlr.Tool;
import org.antlr.tool.ErrorManager;
import org.antlr.works.components.GrammarWindow;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.Console;
import org.antlr.works.utils.ErrorListener;
import org.antlr.works.utils.Utils;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.utils.XJDialogProgress;
import org.antlr.xjlib.foundation.XJUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CodeGenerate implements Runnable {

    private boolean debug = true;

    protected GrammarWindow window;
    protected CodeGenerateDelegate delegate;

    protected long dateOfModificationOnDisk = 0;
    protected String lastError;

    public CodeGenerate(GrammarWindow window, CodeGenerateDelegate delegate) {
        this.window = window;
        this.delegate = delegate;
    }

    public void close() {
        window = null;
        delegate = null;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getOutputPath() {
        return window.getOutputPath();
    }

    public String getGrammarLanguage() {
        return window.getGrammarEngine().getGrammarLanguage();
    }

    public String getGrammarName() {
        return window.getGrammarEngine().getGrammarName();
    }

    public String getLastError() {
        return lastError;
    }

    public boolean generate() {
        ErrorListener el = ErrorListener.getThreadInstance();
        ErrorManager.setErrorListener(el);

        String[] params;
        if(debug)
            params = new String[] { "-debug", "-o", getOutputPath(), "-lib", window.getFileFolder(), window.getFilePath() };
        else
            params = new String[] { "-o", getOutputPath(), "-lib", window.getFileFolder(), window.getFilePath() };

        new File(getOutputPath()).mkdirs();

        Tool antlr = new Tool(Utils.concat(params, AWPrefs.getANTLR3Options()));
        antlr.process();

        boolean success = !el.hasErrors();
        if(success) {
            dateOfModificationOnDisk = window.getDocument().getDateOfModificationOnDisk();
        }
        lastError = el.getFirstErrorMessage();
        el.clear();
        ErrorManager.removeErrorListener();
        return success;
    }

    public List<String> getGeneratedFileNames() throws Exception {
        List<String> files = new ArrayList<String>();
        for(String name : window.getGrammarEngine().getAllGeneratedNames()) {
            files.add(XJUtils.concatPath(getOutputPath(), name+".java"));
        }
        return files;
    }

    public String getGeneratedFileName(int type) throws Exception {
        String className = window.getGrammarEngine().getGeneratedClassName(type);
        if(className == null) return null;
        return XJUtils.concatPath(getOutputPath(), className+".java");
    }

    public boolean isGeneratedTextFileExisting(int type) {
        try {
            String file = getGeneratedFileName(type);
            return file == null || new File(file).exists();
        } catch (Exception e) {
            window.getConsoleTab().println(e);
        }
        return false;
    }

    public boolean isFileModifiedSinceLastGeneration() {
        return dateOfModificationOnDisk != window.getDocument().getDateOfModificationOnDisk();
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
        if(generateError != null) {
            XJAlert.display(window.getJavaContainer(), "Error", "Cannot generate the grammar because:\n"+generateError);
            if(delegate != null) {
                delegate.codeGenerateDidCompleteWithError(generateError);
            }
        } else {
            if(delegate == null || delegate.codeGenerateDisplaySuccess()) {
                if(AWPrefs.isAlertGenerateCodeSuccess()) {
                    XJAlert alert = XJAlert.createInstance();
                    alert.setDisplayDoNotShowAgainButton(true);
                    alert.showSimple(window.getJavaContainer(), "Success", "The grammar has been successfully generated in path:\n"+getOutputPath());
                    AWPrefs.setAlertGenerateCodeSuccess(!alert.isDoNotShowAgain());
                }
            }
            if(delegate != null) {
                delegate.codeGenerateDidComplete();
            }
        }
    }

    protected String generateError = null;
    protected XJDialogProgress progress;

    public void run() {
        generateError = null;

        window.getConsoleTab().setMode(Console.MODE_VERBOSE);

        try {
            if(!generate()) {
                generateError = getLastError();
            }
        } catch (Exception e) {
            generateError = e.toString();
            window.getConsoleTab().println(e);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                generateInThreadDidTerminate();
            }
        });
    }

}
