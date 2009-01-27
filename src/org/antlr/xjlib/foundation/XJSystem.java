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

package org.antlr.xjlib.foundation;

import java.awt.*;

public class XJSystem {

    public static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().startsWith("mac os");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().startsWith("linux");
    }

    public static String getOSVersion() {
        return System.getProperty("os.version");
    }

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getOSArchitecture() {
        return System.getProperty("os.arch");
    }

    public static String getJavaRuntimeVersion() {
        return System.getProperty("java.runtime.version");
    }

    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }

    public static String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public static void setSystemProperties() {
        if(isMacOS())
            System.setProperty("apple.laf.useScreenMenuBar","true");
    }

    public static boolean isHeadless() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance();
    }
}
