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

package org.antlr.works.ate.syntax.misc;

public class ATEToken implements Comparable {

    public int type;

    public int startLineNumber; // starting line number
    public int endLineNumber; // ending line number
    protected int startLineIndex; // starting line character index
    protected int endLineIndex; // ending line character index
    public String text;

    protected int start;
    protected int end;

    public boolean lexer;
    protected String attribute;

    public int index;   // index inside the tokens list
    public boolean modified;    // true if the token has been modified in the text editor

    public ATEToken(int type, int start, int end,
                    int startLineNumber, int endLineNumber,
                    int startLineIndex, int endLineIndex,
                    String text) {
        this.type = type;

        this.start = start;
        this.end = end;

        this.startLineNumber = startLineNumber;
        this.endLineNumber = endLineNumber;
        this.startLineIndex = startLineIndex;
        this.endLineIndex = endLineIndex;

        this.text = text;

        this.attribute = text.substring(start, end);
        this.lexer = isLexerName(attribute);
    }

    public String getAttribute() {
        return attribute;
    }

    public int getStartLineIndex() {
        return startLineIndex;
    }

    public int getStartIndex() {
        return start;
    }

    public int getEndIndex() {
        return end;
    }

    public void offsetPositionBy(int value) {
        start += value;
        end += value;
    }

    public boolean containsIndex(int index) {
        return index >= getStartIndex() && index <= getEndIndex();
    }

    public boolean equals(Object otherObject) {
        ATEToken otherToken = (ATEToken)otherObject;
        return type == otherToken.type &&
                start == otherToken.start &&
                end == otherToken.end;
    }

    public int compareTo(Object o) {
        ATEToken otherToken = (ATEToken)o;
        return this.getAttribute().compareTo(otherToken.getAttribute());
    }

    public String toString() {
        return "{ "+getAttribute()+" = "+type+" ("+start+", "+end+") }";
    }

    public static boolean isLexerName(String name) {
        if(name == null || name.length() < 1)
            return false;
        else
            return name.charAt(0) == name.toUpperCase().charAt(0);
    }
}
