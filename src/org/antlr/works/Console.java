package org.antlr.works;

import org.antlr.Tool;
import org.antlr.works.ate.syntax.generic.ATESyntaxEngineDelegate;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.grammar.EngineGrammar;
import org.antlr.works.grammar.EngineGrammarDelegate;
import org.antlr.works.syntax.GrammarSyntaxEngine;
import org.antlr.works.visualization.SDGenerator;
import org.antlr.xjlib.foundation.XJUtils;

import java.io.File;
import java.io.IOException;

/*

[The "BSD licence"]
Copyright (c) 2005-2007 Jean Bovet
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

public class Console {

    private String file;
    private String outputDirectory;
    private boolean verbose;

    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            printUsage();
            return;
        }

        System.setProperty("java.awt.headless", "true");
        
        Console c = new Console();
        c.process(args);
    }

    private static void printUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append("Usage: java -cp antlrworks.jar org.antlr.works.Console [args]\n");
        sb.append(" -f grammarFile : ").append("specify the input grammar file (*.g)\n");
        sb.append(" -sd format : ").append("specify the format of the syntax diagram output file. For EPS, use 'eps'. For bitmap, use either 'png' or any available extensions\n");
        sb.append(" -o outputDir : ").append("specify the output directory\n");
        sb.append(" -verbose : ").append("prints the operations\n");
        System.out.println(sb.toString());
    }

    public Console() {
    }

    private void process(String args[]) throws Exception {
        readArguments(args);

        String sdFormat = getArgument(args, "-sd");
        if(sdFormat != null) {
            if(verbose) System.out.println("Generating syntax diagram in "+sdFormat);
            generateSyntaxDiagrams(sdFormat);
        }
    }

    private void readArguments(String args[]) {
        file = getArgument(args, "-f");
        if(file == null) {
            System.err.println("File not specified (-f)");
            return;
        }
        outputDirectory = getArgument(args, "-o");
        if(outputDirectory == null) {
            System.err.println("Output directory not specified (-o)");
            return;
        }
        String v = getArgument(args, "-verbose");
        if(v != null) {
            verbose = true;
        }
    }

    private void generateSyntaxDiagrams(String format) throws Exception {
        GrammarSyntaxEngine se = new GrammarSyntaxEngine();
        se.setDelegate(new SyntaxDelegate());
        se.processSyntax();

        CEditorGrammar ceg = new CEditorGrammar(null);
        ceg.parserEngine = new GrammarSyntaxEngine();
        EngineGrammar eg = new EngineGrammar(ceg);
        eg.setDelegate(new ConsoleEngineGrammarDelegate());
        SDGenerator gen = new SDGenerator(eg);

        if(verbose) System.out.println("Begin");
        new File(outputDirectory).mkdirs();
        for(String name : se.getRuleNames()) {
            if(verbose) System.out.println("Generate rule "+name);

            String file = XJUtils.concatPath(outputDirectory, name+"."+format);
            if(format.equals("eps")) {
                gen.renderRuleToEPSFile(name, file);
            } else {
                gen.renderRuleToBitmapFile(name, format, file);
            }
        }
        if(verbose) System.out.println("Done");
    }

    private static String getArgument(String[] args, String name) {
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.equals(name)) {
                if(i+1 < args.length) {
                    return args[i+1];
                } else {
                    return a;
                }
            }
        }
        return null;
    }

    private String getGrammarText() {
        try {
            return XJUtils.getStringFromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public class ConsoleEngineGrammarDelegate implements EngineGrammarDelegate {

        public String getFileName() {
            return file;
        }

        public String getText() {
            return getGrammarText();
        }


        public Tool getANTLRTool() {
            return new Tool();
        }
    }

    public class SyntaxDelegate implements ATESyntaxEngineDelegate {


        public String getText() {
            return getGrammarText();
        }

        public void ateEngineWillParse() {
        }

        public void ateEngineDidParse() {
        }
    }

}
