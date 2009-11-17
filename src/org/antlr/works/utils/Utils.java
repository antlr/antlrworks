package org.antlr.works.utils;

import org.antlr.xjlib.foundation.XJSystem;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
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

public class Utils {

    public static String stringFromFile(String file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            int x = fis.available();
            if(x > 0) {
                byte b[]= new byte[x];
                int c = 0;
                while(c < x) {
                    c += fis.read(b, c, x-c);
                }
                return new String(b);
            } else {
                return "";                
            }
        } finally {
            fis.close();
        }
    }

    public static String toString(String[] object) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<object.length; i++) {
            sb.append(object[i]);
            if(i < object.length - 1)
                sb.append(" ");
        }
        return sb.toString();
    }

    public static String trimString(String s) {
        int a = 0;
        while(a < s.length() && s.charAt(a) == ' ' || s.charAt(a) == '\n' || s.charAt(a) == '\t') {
            a++;
        }

        int b = s.length()-1;
        while(b >0 && s.charAt(b) == ' ' || s.charAt(b) == '\n' || s.charAt(b) == '\t') {
            b--;
        }

        if(a == s.length() || b == 0)
            return "";
        else
            return s.substring(a, b+1);
    }

    public static boolean isComponentChildOf(Component child, Component parent) {
        if(child == null)
            return false;
        else if(child == parent)
            return true;
        else
            return isComponentChildOf(child.getParent(), parent);
    }

    /** Quote a path if needed (i.e. white space detected)
     *
     * @param path The path to quote
     * @return The quoted path if needed
     */
    public static String quotePath(String path) {
        if(path == null || path.length() == 0 || XJSystem.isMacOS())
            return path;

        path = unquotePath(path);

        if(path.indexOf(' ') != -1) {
            path = "\""+path+"\"";
        }

        return path;
    }

    /** Unquote a path if it has quote (") at the beginning or at the end
     * of it.
     * @param path The path to unquote
     * @return The unquoted path
     */
    public static String unquotePath(String path) {
        if(path == null || path.length() == 0)
            return path;

        if(path.charAt(0) == '"') {
            path = path.substring(1);
        }
        if(path.charAt(path.length()-1) == '"') {
            path = path.substring(0, path.length()-1);
        }

        return path;
    }

    public static void fillComboWithEOL(JComboBox combo) {
        combo.removeAllItems();
        combo.addItem("Unix (LF)");
        combo.addItem("Mac (CR)");
        combo.addItem("Windows (CRLF)");
    }

    public static String convertRawTextWithEOL(String rawText, JComboBox eolCombo) {
        return rawText.replaceAll("\n", getEOL(eolCombo));
    }
    
    private static String getEOL(JComboBox eolCombo) {
        switch(eolCombo.getSelectedIndex()) {
            case 0: return "\n";
            case 1: return "\r";
            case 2: return "\r\n";
        }
        return "\n";
    }

    public static String[] concat(String[] a, String[] b) {
        String[] n = new String[a.length+b.length];
        System.arraycopy(a, 0, n, 0, a.length);
        System.arraycopy(b, 0, n, a.length, b.length);
        return n;
    }
}
