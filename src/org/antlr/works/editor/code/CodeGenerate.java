package org.antlr.works.editor.code;

import edu.usfca.xj.appkit.frame.XJFrame;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.foundation.XJUtils;
import org.antlr.Tool;
import org.antlr.codegen.CodeGenerator;
import org.antlr.tool.Grammar;
import org.antlr.works.dialog.DialogProgress;
import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.editor.EditorProvider;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

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

public class CodeGenerate implements Runnable {

    protected String outputPath;
    protected boolean debug = false;
    protected Grammar parserGrammar;
    protected Grammar lexerGrammar;

    protected EditorProvider provider;

    public CodeGenerate(EditorProvider provider) {
        this.provider = provider;
        this.outputPath = EditorPreferences.getOutputPath();
    }

    public void setOutputPath(String path) {
        this.outputPath = path;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public Grammar getParserGrammar() {
        if(parserGrammar == null) {
            try {
                parserGrammar = new Grammar(provider.getPlainText());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return parserGrammar;
    }

    public String getGrammarLanguage() {
        return (String)getParserGrammar().getOption("language");
    }

    public String getGrammarName() {
        return getParserGrammar().name;
    }

    public void generate(boolean debug) throws Exception {
        this.debug = debug;
        generateParser();
        generateLexer();
    }

    protected void generateParser() throws Exception {
        generateGrammar(getParserGrammar());
    }

    protected void generateLexer() throws Exception {
        String lexerGrammarStr = getParserGrammar().getLexerGrammar();
        if ( getParserGrammar().type==Grammar.COMBINED && lexerGrammarStr!=null ) {
            FileWriter fw = getOutputFile(XJUtils.concatPath(getOutputPath(), getParserGrammar().name+".lexer.g"));
            fw.write(lexerGrammarStr);
            fw.close();
            StringReader sr = new StringReader(lexerGrammarStr);
            Grammar lexerGrammar = new Grammar();
            lexerGrammar.setFileName("<internally-generated-lexer>");
            lexerGrammar.importTokenVocabulary(getParserGrammar());
            lexerGrammar.setGrammarContent(sr);
            sr.close();
            generateGrammar(lexerGrammar);
        }
    }

    protected void generateGrammar(Grammar grammar) throws IOException
    {
        String language = (String)grammar.getOption("language");
        if ( language!=null ) {
            CodeGenerator generator = new CodeGenerator(new MyTool(getOutputPath()), grammar, language);
            generator.setOutputDirectory(getOutputPath());
            grammar.setCodeGenerator(generator);
            generator.setDebug(debug);

            if ( grammar.type == Grammar.LEXER ) {
                grammar.addArtificialMatchTokensRule();
            }

            generator.genRecognizer();
        }
    }

    public FileWriter getOutputFile(String fileName) throws IOException {
		File outDir = new File(fileName).getParentFile();
		if( !outDir.exists() ) {
			outDir.mkdirs();
		}
        return new FileWriter(fileName);
    }

    public String getGeneratedClassName(boolean lexer) {
        String name;
        if(lexer)
            name = getParserGrammar().name+"Lexer";
        else
            name = getParserGrammar().name;
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

    public void generateInThread(XJFrame parent, boolean debug) {
        this.debug = debug;

        progress = new DialogProgress(parent);
        progress.setInfo("Generating...");
        progress.setCancellable(false);
        progress.setIndeterminate(true);
        progress.display();

        new Thread(this).start();
    }

    public void generateInThreadDidTerminate() {
        progress.close();
        if(generateException != null)
            XJAlert.display("Error", "Cannot generate the grammar because:\n"+generateException);
        else
            XJAlert.display("Success", "The grammar has been successfully generated in path:\n"+outputPath);
    }

    protected Exception generateException = null;
    protected DialogProgress progress;

    public void run() {
        generateException = null;

        try {
            generate(debug);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                generateInThreadDidTerminate();
            }
        });
    }

    protected class MyTool extends Tool {
        public MyTool(String outputPath) {
            this.outputDirectory = outputPath;
            this.debug = CodeGenerate.this.debug;
        }
    }
}
