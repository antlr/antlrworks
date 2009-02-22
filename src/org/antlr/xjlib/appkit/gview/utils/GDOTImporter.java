package org.antlr.xjlib.appkit.gview.utils;

import org.antlr.xjlib.appkit.gview.object.GElement;
import org.antlr.xjlib.appkit.gview.object.GElementCircle;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
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

public abstract class GDOTImporter {

    protected GElement graph;
    protected float height = 0;

    public GElement generateGraph(String dotFile) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(dotFile));
        try {
            graph = null;

            /** The problem here is that DOT sometime inserts a '\' at the end of a long
             * line so we have to skip it and continue to parse until a "real" EOL is reached.
             * Example:
             * 	statement -> compoundStatement [pos="e,3264,507 3271,2417 3293,2392 ... 3237,565 3234,560 32\
             39,545 3243,534 3249,523 3257,514"];
             */

            StringBuilder line = new StringBuilder();
            int c;  // current character
            int pc = -1;    // previous character
            while((c = br.read()) != -1) {
                if(c == '\n') {
                    if(pc == '\\') {
                        // Remove the last \ if it was part of the DOT wrapping character
                        line.deleteCharAt(line.length()-1);
                    } else {
                        GElement element = parseLine(line.toString());
                        if(element != null) {
                            if(graph == null)
                                graph = element;
                            else
                                graph.addElement(element);
                        }
                        line.delete(0, line.length());
                    }
                } else if(c != '\r') {
                    line.append((char)c);
                } else if(c == '\r') {
                    continue;
                }
                pc = c;
            }
        } finally {
            br.close();
        }
        return graph;
    }

    public String[] parseTokens(String line) throws IOException {
        List<String> tokens = new ArrayList<String>();

        /*StringTokenizer st = new StringTokenizer(line);
        String token;
        while((token = st.nextToken()) != null) {
            tokens.add(token);
        } */

        StreamTokenizer st = new StreamTokenizer(new StringReader(line));
        st.parseNumbers();
        st.wordChars('_', '_'); // A word can be THIS_IS_A_WORD

        int token = st.nextToken();
        while(token != StreamTokenizer.TT_EOF) {
            String element = null;
            switch(token) {
                case StreamTokenizer.TT_NUMBER:
                    element = String.valueOf(st.nval);
                    break;
                case StreamTokenizer.TT_WORD:
                    element = st.sval;
                    break;
                case '"':
                case '\'':
                    element = st.sval;
                    break;
                case StreamTokenizer.TT_EOL:
                    break;
                case StreamTokenizer.TT_EOF:
                    break;
                default:
                    element = String.valueOf((char)st.ttype);
                    break;
            }
            if(element != null)
                tokens.add(element);
            token = st.nextToken();
        }

        String[] result = new String[tokens.size()];
        for(int index=0; index<tokens.size(); index++)
            result[index] = tokens.get(index);
        return result;
    }

    public abstract GElement parseLine(String line) throws IOException;

    public abstract GElement createGraphNode(String[] tokens) throws IOException;
    public abstract GElement createGraphEdge(String[] tokens) throws IOException;

    public boolean isFloatString(String s) {
        try {
            Float.parseFloat(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static class Node extends GElementCircle {

        public boolean doublecircle;
        public float width, height;

        public Node() {

        }
        
        public void setDouble(boolean flag) {
            doublecircle = flag;
        }

        public void setSize(float width, float height) {
            this.width = width;
            this.height = height;
        }

        public void drawShape(Graphics2D g) {
            //super.drawShape(g);

            {
                int x = (int)(getPositionX()-width/2);
                int y = (int)(getPositionY()-height/2);

                g.drawOval(x, y, (int)width, (int)height);
            }

            if(doublecircle) {
                int x = (int)(getPositionX()-width/2);
                int y = (int)(getPositionY()-height/2);

                g.drawOval(x+3, y+3, (int)(width-6), (int)(height-6));
            }
        }

    }

    public static class StringTokenizer {

        public String s;
        public int position;

        public StringTokenizer(String s) {
            this.s = s;
            position = -1;
        }

        public String nextToken() {
            StringBuilder token = new StringBuilder();

            while(skipChar((char)getChar(1))) {
                nextChar();
            }

            while(nextChar()) {
                char c = getChar();

                if(c == '"') {
                    token.append(parseString());
                    break;
                }

                if(isWordChar(c)) {
                    token.append(c);
                } else {
                    if(token.length() == 0)
                        token.append(c);
                    else
                        position--;
                    break;
                }

            }

            if(token.length() == 0)
                return null;
            else
                return token.toString();
        }

        public String parseString() {
            StringBuilder string = new StringBuilder();
            boolean escaping = false;
            while(nextChar()) {
                char c = getChar();
                if(c == '\\' && !escaping) {
                    escaping = true;
                    continue;
                }

                if(c == '"' && !escaping)
                    break;
                else
                    string.append(c);

                escaping = false;
            }
            return string.toString();
        }

        public boolean nextChar() {
            position++;
            return position < s.length();
        }

        public char getChar() {
            return (char)getChar(0);
        }

        public int getChar(int offset) {
            int index = position+offset;
            if(index<s.length())
                return s.charAt(index);
            else
                return -1;
        }

        public boolean isWordChar(char c) {
            return Character.isLetterOrDigit(c) || c == '_' || c == '.';
        }

        public boolean skipChar(char c) {
            return c == ' ' || c == '\t';
        }
    }

}
