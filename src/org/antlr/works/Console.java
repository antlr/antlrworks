package org.antlr.works;

import org.antlr.Tool;
import org.antlr.works.ate.syntax.generic.ATESyntaxEngineDelegate;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.works.grammar.engine.GrammarEngineDelegate;
import org.antlr.works.grammar.engine.GrammarEngineImpl;
import org.antlr.works.grammar.syntax.GrammarSyntaxEngine;
import org.antlr.works.utils.ConsoleHelper;
import org.antlr.works.visualization.SDGenerator;
import org.antlr.works.visualization.serializable.SEncoder;
import org.antlr.works.visualization.serializable.SXMLEncoder;
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
    private String outputFile;
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
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: java -cp antlrworks.jar org.antlr.works.Console [args]\n");
        sb.append(" -f grammarFile : ").append("specify the input grammar file (*.g)\n");
        sb.append(" -sd format : ").append("specify the format of the syntax diagram output file. For EPS, use 'eps'. For bitmap, use either 'png' or any available extensions\n");
        sb.append(" -serialize outputFile : ").append("serialize all the syntax diagram of the specified files");
        sb.append(" -o outputDir : ").append("specify the output directory\n");
        sb.append(" -verbose : ").append("prints the operations\n");
        System.out.println(sb.toString());
    }

    public Console() {
        ConsoleHelper.setCurrent(new ConsoleConsole());
    }

    private void process(String args[]) throws Exception {
        readArguments(args);

        String sdFormat = getArgumentValue(args, "-sd");
        if(sdFormat != null) {
            if(verbose) System.out.println("Generating syntax diagram in "+sdFormat);
            generateSyntaxDiagrams(sdFormat);
        }
        if(hasArgument(args, "-serialize")) {
            if(verbose) System.out.println("Serializing syntax diagram ");
            serializeSyntaxDiagrams();
        }
    }

    private void readArguments(String args[]) {
        file = getArgumentValue(args, "-f");
        if(file == null) {
            System.err.println("File not specified (-f)");
            return;
        }
        file = new File(file).getAbsolutePath();

        outputDirectory = getArgumentValue(args, "-o");
        if(outputDirectory == null && hasArgument(args, "-sd")) {
            System.err.println("Output directory not specified (-o)");
            return;
        }

        outputFile = getArgumentValue(args, "-serialize");
        if(outputFile == null && hasArgument(args, "-serialize")) {
            System.err.println("Output file not specified (-serialize)");
            return;
        }
        String v = getArgumentValue(args, "-verbose");
        if(v != null) {
            verbose = true;
        }
    }

    private void generateSyntaxDiagrams(String format) throws Exception {
        processSyntaxDiagram(new GeneratePSDDelegate(format));
    }

    private void serializeSyntaxDiagrams() throws Exception {
        processSyntaxDiagram(new SerializePSDDelegate());
    }

    private void processSyntaxDiagram(ProcessSyntaxDiagramDelegate delegate) throws Exception {
        GrammarEngine engine = new GrammarEngineImpl(new EngineDelegate());
        GrammarSyntaxEngine syntaxEngine = engine.getSyntaxEngine();

        syntaxEngine.setDelegate(new SyntaxDelegate());
        syntaxEngine.processSyntax();

        engine.parserCompleted();
        
        SDGenerator gen = new SDGenerator(engine);

        delegate.beginProcess();
        for(String name : engine.getRuleNames()) {
            delegate.processRule(name, gen);
        }
        delegate.endProcess();
    }

    private interface ProcessSyntaxDiagramDelegate {

        void beginProcess();
        void endProcess() throws Exception;

        void processRule(String name, SDGenerator gen) throws Exception;

    }

    private class GeneratePSDDelegate implements ProcessSyntaxDiagramDelegate {

        private String format;

        public GeneratePSDDelegate(String format) {
            this.format = format;
        }

        public void beginProcess() {
            if(verbose) System.out.println("Begin");
            new File(outputDirectory).mkdirs();
        }

        public void endProcess() throws IOException {
            if(verbose) System.out.println("Done");
        }

        public void processRule(String name, SDGenerator gen) throws Exception {
            if(verbose) System.out.println("Generate rule "+name);

            String file = XJUtils.concatPath(outputDirectory, name+"."+format);
            if(format.equals("eps")) {
                gen.renderRuleToEPSFile(name, file);
            } else {
                gen.renderRuleToBitmapFile(name, format, file);
            }
        }
    }

    private class SerializePSDDelegate implements ProcessSyntaxDiagramDelegate {

        private StringBuilder content;

        public void beginProcess() {
            if(verbose) System.out.println("Begin");
            new File(XJUtils.getPathByDeletingLastComponent(outputFile)).mkdirs();
            content = new StringBuilder();
        }

        public void endProcess() throws IOException {
            XJUtils.writeStringToFile(content.toString(), outputFile);
            if(verbose) System.out.println("Done");
        }

        public void processRule(String name, SDGenerator gen) throws Exception {
            if(verbose) System.out.println("Generate rule "+name);
            SEncoder encoder = new SXMLEncoder();
            gen.serializeRule(name, encoder);
            content.append("\n\n------").append(name).append("------\n\n");
            content.append(encoder.toString());
        }
    }

    private static String getArgumentValue(String[] args, String name) {
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

    private static boolean hasArgument(String[] args, String name) {
        for (String a : args) {
            if (a.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private String getGrammarText() {
        try {
            return XJUtils.getStringFromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public class SyntaxDelegate implements ATESyntaxEngineDelegate {


        public String getText() {
            return getGrammarText();
        }

        public void ateEngineBeforeParsing() {
            // ignored
        }

        public void ateEngineAfterParsing() {
            // ignored
        }
    }

    private class EngineDelegate implements GrammarEngineDelegate {
        
        public void engineAnalyzeCompleted() {
            // ignored
        }

        public String getOutputPath() {
            return XJUtils.concatPath(XJUtils.getPathByDeletingLastComponent(file), "output");
        }

        public Tool getANTLRTool() {
            Tool t = new Tool();
            t.setOutputDirectory(getOutputPath());
            return t;
        }

        public String getGrammarFileName() {
            return XJUtils.getLastPathComponent(file);
        }

        public String getGrammarText() {
            return Console.this.getGrammarText();
        }

        public String getTokenVocabFile(String name) {
            return null;
        }

        public void gotoToRule(String grammar, String name) {
            // ignored
        }

        public void reportError(Exception e) {
            e.printStackTrace();
        }

        public void reportError(String error) {
            System.err.println(error);
        }
    }

    private class ConsoleConsole implements org.antlr.works.utils.Console {

        public void setMode(int mode) {
            // ignore
        }

        public void println(String s) {
            if(verbose) System.out.println(s);
        }

        public void println(String s, int level) {
            if(level != org.antlr.works.utils.Console.LEVEL_NORMAL) {
                System.err.println(s);
            } else if(verbose) {
                println(s);
            }
        }

        public void println(Throwable e) {
            e.printStackTrace();
        }

        public void print(String string, int level) {
            if(level != org.antlr.works.utils.Console.LEVEL_NORMAL) {
                System.err.print(string);
            } else if(verbose) {
                System.out.print(string);
            }
        }

        public void print(Throwable e) {
            e.printStackTrace();
        }
    }
}
