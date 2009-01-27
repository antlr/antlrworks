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

package org.antlr.xjlib.appkit.swing;

import org.antlr.xjlib.foundation.XJSystem;

import javax.swing.*;
import java.awt.*;

public class XJLookAndFeel {

    public static String applyLookAndFeel(String name) {
        String className = getLookAndFeelClassName(name);
        if(className == null) {
            System.err.println("No LAF class name for name '"+name+"', using default LAF.");
            className = getLookAndFeelClassName(null);
        }

        try {
            UIManager.setLookAndFeel(className);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Frame[] frame = Frame.getFrames();
        for (Frame aFrame : frame) {
            SwingUtilities.updateComponentTreeUI(aFrame);
            aFrame.pack();
        }

        return getLookAndFeelName(className);
    }

    public static String getLookAndFeelClassName(String name) {
        if(name == null) {
            if(XJSystem.isLinux()) {
                return UIManager.getCrossPlatformLookAndFeelClassName();
            } else {
                return UIManager.getSystemLookAndFeelClassName();                
            }
        }

        UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo anInfo : info) {
            if (anInfo.getName().equalsIgnoreCase(name))
                return anInfo.getClassName();
        }
        return null;
    }

    public static String getLookAndFeelName(String className) {
        UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo anInfo : info) {
            if (anInfo.getClassName().equalsIgnoreCase(className))
                return anInfo.getName();
        }
        return null;
    }

    public static String getDefaultLookAndFeelName() {
        String name = UIManager.getSystemLookAndFeelClassName();
        UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo anInfo : info) {
            if (anInfo.getClassName().equalsIgnoreCase(name))
                return anInfo.getName();
        }
        return name;
    }

}
