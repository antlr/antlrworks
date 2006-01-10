package org.antlr.works.engine;

import edu.usfca.xj.foundation.XJUtils;
import org.antlr.works.IDE;
import org.antlr.works.prefs.AWPrefs;

import java.io.File;
import java.lang.reflect.Method;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
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

public class EngineCompiler {

    public static String getClassPath(String outputPath) {
        String classPath = outputPath;
        classPath += File.pathSeparatorChar+ IDE.getApplicationPath();
        classPath += File.pathSeparatorChar+System.getProperty("java.class.path");
        return classPath;
    }

    public static String runANTLR(String file, String libPath, String outputPath, StreamWatcherDelegate delegate) {
        String error = null;
        int result = 0;
        try {
            String[] args = new String[9];
            args[0] = "java";
            args[1] = "-cp";
            args[2] = getClassPath(outputPath);
            args[3] = "org.antlr.Tool";
            args[4] = "-o";
            args[5] = outputPath;
            args[6] = "-lib";
            args[7] = libPath;
            args[8] = file;

            Process p = Runtime.getRuntime().exec(args);
            new StreamWatcher(p.getErrorStream(), "ANTLR[error]", delegate).start();
            new StreamWatcher(p.getInputStream(), "ANTLR[stdout]", delegate).start();
            result = p.waitFor();
        } catch(Exception e) {
            error = "ANTLR exception:\n"+e.getLocalizedMessage();
        }

        if(result != 0) {
            error = "Compiler failed result:\n"+result;
        }

        return error;
    }

    public static String compileFiles(String[] files, String outputFileDir, StreamWatcherDelegate delegate) {
        String error = null;

        int result = 0;
        try {
            String compiler = AWPrefs.getCompiler();
            String classPath = getClassPath(outputFileDir);

            if(compiler.equalsIgnoreCase(AWPrefs.COMPILER_JAVAC)) {
                String[] args = new String[5+files.length];
                if(AWPrefs.getJavaCCustomPath())
                    args[0] = XJUtils.concatPath(AWPrefs.getJavaCPath(), "javac");
                else
                    args[0] = "javac";
                args[1] = "-classpath";
                args[2] = classPath;
                args[3] = "-d";
                args[4] = outputFileDir;
                for(int i=0; i<files.length; i++)
                    args[5+i] = files[i];

                Process p = Runtime.getRuntime().exec(args);
                new StreamWatcher(p.getErrorStream(), "Compiler", delegate).start();
                new StreamWatcher(p.getInputStream(), "Compiler", delegate).start();
                result = p.waitFor();
            } else if(compiler.equalsIgnoreCase(AWPrefs.COMPILER_JIKES)) {
                String jikesPath = XJUtils.concatPath(AWPrefs.getJikesPath(), "jikes");

                String[] args = new String[5+files.length];
                args[0] = jikesPath;
                args[1] = "-classpath";
                args[2] = classPath;
                args[3] = "-d";
                args[4] = outputFileDir;
                for(int i=0; i<files.length; i++)
                    args[5+i] = files[i];

                Process p = Runtime.getRuntime().exec(args);
                new StreamWatcher(p.getErrorStream(), "Compiler", delegate).start();
                new StreamWatcher(p.getInputStream(), "Compiler", delegate).start();
                result = p.waitFor();
            } else if(compiler.equalsIgnoreCase(AWPrefs.COMPILER_INTEGRATED)) {
                String[] args = new String[2+files.length];
                args[0] = "-d";
                args[1] = outputFileDir;
                for(int i=0; i<files.length; i++)
                    args[2+i] = files[i];

                Class javac = Class.forName("com.sun.tools.javac.Main");
                Class[] p = new Class[] { String[].class };
                Method m = javac.getMethod("compile", p);
                Object[] a = new Object[] { args };
                Object r = m.invoke(javac.newInstance(), a);
                result = ((Integer)r).intValue();
                //result = com.sun.tools.javac.Main.compile(args);
            }

        } catch(Error e) {
            error = "Compiler error:\n"+e.getLocalizedMessage();
        } catch(Exception e) {
            error = "Compiler exception:\n"+e.getLocalizedMessage();
        }

        if(result != 0) {
            error = "Compiler failed result:\n"+result;
        }

        return error;
    }

}
