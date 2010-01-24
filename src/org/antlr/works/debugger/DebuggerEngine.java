package org.antlr.works.debugger;

import org.antlr.works.IDE;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.Console;
import org.antlr.works.utils.StreamWatcher;
import org.antlr.works.utils.StreamWatcherDelegate;
import org.antlr.works.utils.Utils;
import org.antlr.xjlib.foundation.XJSystem;
import org.antlr.xjlib.foundation.XJUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
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

public class DebuggerEngine {

    public static Map<Thread,Process> processPerThread = new HashMap<Thread, Process>();

    public static void setProcess(Process p) {
        processPerThread.put(Thread.currentThread(), p);
    }

    public static void removeProcess() {
        processPerThread.remove(Thread.currentThread());
    }

    public static Process getProcess(Thread t) {
        return processPerThread.get(t);
    }

    public static Process getProcess() {
        return processPerThread.get(Thread.currentThread());
    }

    public static String getClassPath(String outputPath) {
        String appPath = IDE.getApplicationPath();

        // Need to include the path of the application in order to be able
        // to compile the parser if the system classpath doesn't have ANTLR or ST
        String classPath = outputPath;
        if(appPath != null)
            classPath += File.pathSeparatorChar+Utils.unquotePath(appPath);

        if(AWPrefs.getUseSystemClassPath())
            classPath += File.pathSeparatorChar+Utils.unquotePath(System.getProperty("java.class.path"));

        if(AWPrefs.getUseCustomClassPath())
            classPath += File.pathSeparatorChar+Utils.unquotePath(AWPrefs.getCustomClassPath());

        classPath += File.pathSeparatorChar+".";

        // On Mac OS X, quoting the path works fine except within IntelliJ when
        // AW is working as a plugin. Without quoting, it works everywhere in Mac
        // OS X so I decided to quote only on Windows.
        if(XJSystem.isWindows())
            return Utils.quotePath(classPath);
        else
            return classPath;
    }

    public static String runANTLR(Console console, String file, String libPath, String outputPath, StreamWatcherDelegate delegate) {
        String error = null;
        StreamWatcher esw = null;
        int result = 0;
        try {
            String[] args = new String[9];
            args[0] = "java";
            args[1] = "-cp";
            args[2] = getClassPath(outputPath);
            args[3] = "org.antlr.Tool";
            args[4] = "-o";
            args[5] = Utils.quotePath(outputPath);
            args[6] = "-lib";
            args[7] = Utils.quotePath(libPath);
            args[8] = file;

            IDE.debugVerbose(console, DebuggerEngine.class, "Run ANTLR: "+Utils.toString(args));

            Process p = Runtime.getRuntime().exec(args);
            setProcess(p);
            esw = new StreamWatcher(p.getErrorStream(), "ANTLR[error]", delegate);
            esw.start();
            new StreamWatcher(p.getInputStream(), "ANTLR[stdout]", delegate).start();
            result = p.waitFor();
        } catch(Exception e) {
            error = "Failed to run ANTLR with exception:\n"+e.toString();
        } finally {
            removeProcess();
        }

        if(result != 0) {
            error = "Failed to run ANTLR with result:\n"+result;
        }

        /** Make sure ANTLR didn't return an error in the error string
         *
         */

        if(error == null) {
            for (String line : esw.getLines()) {
                if (line.startsWith("ANTLR Parser Generator"))
                    continue;

                if (line.startsWith("no such locale file"))
                    continue;

                error = line;
                break;
            }
        }
        return error;
    }

    public static String runJava(Console console, String currentPath, String[] params, StreamWatcherDelegate delegate) {
        String error = null;
        int result = 0;
        try {
            String[] args = new String[3+params.length];
            args[0] = "java";
            args[1] = "-cp";
            args[2] = getClassPath(currentPath);
            System.arraycopy(params, 0, args, 3, params.length);

            IDE.debugVerbose(console, DebuggerEngine.class, "Run Java: "+Utils.toString(args));

            Process p = Runtime.getRuntime().exec(args, null, new File(currentPath));
            setProcess(p);
            new StreamWatcher(p.getErrorStream(), "Java[error]", delegate).start();
            new StreamWatcher(p.getInputStream(), "Java[stdout]", delegate).start();
            result = p.waitFor();
        } catch(Exception e) {
            error = "Failed to run Java with exception:\n"+e.toString();
        } finally {
            removeProcess();
        }

        if(result != 0) {
            error = "Failed to run Java with result:\n"+result;
        }

        return error;
    }

    public static String compileFiles(Console console, String[] files, String outputFileDir, StreamWatcherDelegate delegate) {
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
                args[4] = Utils.quotePath(outputFileDir);
                System.arraycopy(files, 0, args, 5, files.length);

                IDE.debugVerbose(console, DebuggerEngine.class, "Compile: "+Utils.toString(args));

                Process p = Runtime.getRuntime().exec(args);
                setProcess(p);
                new StreamWatcher(p.getErrorStream(), "Compiler[error]", delegate).start();
                new StreamWatcher(p.getInputStream(), "Compiler[stdout]", delegate).start();
                result = p.waitFor();
            } else if(compiler.equalsIgnoreCase(AWPrefs.COMPILER_JIKES)) {
                String jikesPath = XJUtils.concatPath(AWPrefs.getJikesPath(), "jikes");

                String[] args = new String[5+files.length];
                args[0] = jikesPath;
                args[1] = "-classpath";
                args[2] = classPath;
                args[3] = "-d";
                args[4] = Utils.quotePath(outputFileDir);
                System.arraycopy(files, 0, args, 5, files.length);

                IDE.debugVerbose(console, DebuggerEngine.class, "Compile: "+Utils.toString(args));

                Process p = Runtime.getRuntime().exec(args);
                setProcess(p);
                new StreamWatcher(p.getErrorStream(), "Compiler[error]", delegate).start();
                new StreamWatcher(p.getInputStream(), "Compiler[stdout]", delegate).start();
                result = p.waitFor();
            } else if(compiler.equalsIgnoreCase(AWPrefs.COMPILER_INTEGRATED)) {
                String[] args = new String[2+files.length];
                args[0] = "-d";
                args[1] = outputFileDir;
                System.arraycopy(files, 0, args, 2, files.length);

                Class<?> javac = Class.forName("com.sun.tools.javac.Main");
                Class[] p = new Class[] { String[].class };
                Method m = javac.getMethod("compile", p);
                Object[] a = new Object[] { args };
                Object r = m.invoke(javac.newInstance(), a);
                result = (Integer) r;
                //result = com.sun.tools.javac.Main.compile(args);
            }

        } catch(Error e) {
            error = "Compiler error:\n"+e.toString();
            e.printStackTrace();
        } catch(Exception e) {
            error = "Compiler exception:\n"+e.toString();
            e.printStackTrace();
        } finally {
            removeProcess();
        }

        if(result != 0) {
            error = "Compiler failed with result code "+result;
        }

        return error;
    }

}
