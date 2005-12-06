package org.antlr.works.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        int x= fis.available();
        if(x > 0) {
            byte b[]= new byte[x];
            int count = fis.read(b);
            return new String(b);
        } else
            return null;
    }

    public static String toString(String[] object) {
        StringBuffer sb = new StringBuffer();
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

}
