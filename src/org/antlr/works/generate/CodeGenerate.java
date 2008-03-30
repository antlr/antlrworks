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
import org.antlr.tool.Grammar;
import org.antlr.works.components.editor.ComponentEditorGrammar;
import org.antlr.works.grammar.antlr.AntlrEngineGrammar;
import org.antlr.works.grammar.element.ElementGrammarName;
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

    protected ComponentEditorGrammar editor;
    protected CodeGenerateDelegate delegate;

    protected long dateOfModificationOnDisk = 0;
    protected String lastError;

    public CodeGenerate(ComponentEditorGrammar editor, CodeGenerateDelegate delegate) {
        this.editor = editor;
        this.delegate = delegate;
    }

    public void close() {
        editor = null;
        delegate = null;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getOutputPath() {
        return editor.getOutputPath();
    }

    public String getGrammarLanguage() {
        try {
            AntlrEngineGrammar eg = editor.getSyntaxEngine().getAntlrGrammar();
            eg.createGrammars();
            Grammar g = eg.getParserGrammar();
            if(g == null) {
                g = eg.getLexerGrammar();
            }
            if(g != null) {
                return (String)g.getOption("language");
            } else {
                return null;
            }
        } catch (Exception e) {
            editor.getConsole().println(e);
        }
        return null;
    }

    public String getGrammarName() {
        return editor.getSyntaxEngine().getSyntax().getName();
    }

    public String getLastError() {
        return lastError;
    }

    public boolean generate() {
        ErrorListener el = ErrorListener.getThreadInstance();
        ErrorManager.setErrorListener(el);

        String[] params;
        if(debug)
            params = new String[] { "-debug", "-o", getOutputPath(), "-lib", editor.getFileFolder(), editor.getFilePath() };
        else
            params = new String[] { "-o", getOutputPath(), "-lib", editor.getFileFolder(), editor.getFilePath() };

        Tool antlr = new Tool(Utils.concat(params, AWPrefs.getANTLR3Options()));
        antlr.process();

        boolean success = !el.hasErrors();
        if(success) {
            dateOfModificationOnDisk = editor.getDocument().getDateOfModificationOnDisk();
        }
        lastError = el.getFirstErrorMessage();
        el.clear();
        return success;
    }

    /**
     * Returns true if the grammar type needs a suffix for the generated class files.
     * Only combined grammars need a suffix.
     *
     * @return true if the grammar generated files need a suffix
     */
    public boolean hasSuffix() {
        return editor.getSyntaxEngine().getSyntax().isCombinedGrammar();
    }

    public String getSuffix(int type) {
        if(hasSuffix()) {
            switch(type) {
                case ElementGrammarName.LEXER:
                    return "Lexer";
                case ElementGrammarName.PARSER:
                    return "Parser";
            }
        }
        return "";
    }

    public String getGeneratedClassName(int type) throws Exception {
        String name = null;
        AntlrEngineGrammar antlrEngineGrammar = editor.getSyntaxEngine().getAntlrGrammar();
        antlrEngineGrammar.createGrammars();
        if(type == ElementGrammarName.LEXER) {
            Grammar g = antlrEngineGrammar.getLexerGrammar();
            if(g == null) return null;
            name = g.name+getSuffix(type);
        } else if(type == ElementGrammarName.PARSER) {
            Grammar g = antlrEngineGrammar.getParserGrammar();
            if(g == null) return null;
            name = g.name+getSuffix(type);
        } else if(type == ElementGrammarName.TREEPARSER) {
            Grammar g = antlrEngineGrammar.getParserGrammar();
            if(g == null) return null;
            if(!editor.getSyntaxEngine().getSyntax().isTreeParserGrammar()) return null;
            name = g.name+getSuffix(type);
        }
        return name;
    }

    public List<String> getGeneratedFileNames() throws Exception {
        List<String> files = new ArrayList<String>();
        for(String name : editor.getSyntaxEngine().getSyntax().getAllGeneratedNames()) {
            files.add(XJUtils.concatPath(getOutputPath(), name+".java"));
        }
        System.out.println(files);
        return files;
    }

    public String getGeneratedFileName(int type) throws Exception {
        String className = getGeneratedClassName(type);
        if(className == null) return null;
        return XJUtils.concatPath(getOutputPath(), className+".java");
    }

    public boolean isGeneratedTextFileExisting(int type) {
        try {
            String file = getGeneratedFileName(type);
            return file == null || new File(file).exists();
        } catch (Exception e) {
            editor.getConsole().println(e);
        }
        return false;
    }

    public boolean isFileModifiedSinceLastGeneration() {
        return dateOfModificationOnDisk != editor.getDocument().getDateOfModificationOnDisk();
    }

    public boolean supportsLexer() {
        int type = editor.getSyntaxEngine().getSyntax().getType();
        return type == ElementGrammarName.COMBINED || type == ElementGrammarName.LEXER;
    }

    public boolean supportsParser() {
        int type = editor.getSyntaxEngine().getSyntax().getType();
        return type == ElementGrammarName.COMBINED || type == ElementGrammarName.PARSER || type == ElementGrammarName.TREEPARSER;
    }

    public String getGeneratedText(int type) throws Exception {
        return XJUtils.getStringFromFile(getGeneratedFileName(type));
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
            XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the grammar because:\n"+generateError);
        else {
            if(delegate == null || delegate.codeGenerateDisplaySuccess())
                XJAlert.display(editor.getWindowContainer(), "Success", "The grammar has been successfully generated in path:\n"+getOutputPath());
            else
                delegate.codeGenerateDidComplete();
        }
    }

    protected String generateError = null;
    protected XJDialogProgress progress;

    public void run() {
        generateError = null;

        editor.getConsole().setMode(Console.MODE_VERBOSE);

        try {
            if(!generate()) {
                generateError = getLastError();
            }
        } catch (Exception e) {
            generateError = e.toString();
            editor.getConsole().println(e);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                generateInThreadDidTerminate();
            }
        });
    }

}
