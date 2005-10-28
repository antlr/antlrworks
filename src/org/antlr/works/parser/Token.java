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

package org.antlr.works.parser;

import org.antlr.works.editor.textpane.folding.Entity;

public class Token implements Comparable, Entity {

    public int type;

    public int line;
    protected int linePosition;
    public String text;

    protected int start;
    protected int end;

    protected String attribute;
    protected boolean isAllUpperCase;
    protected boolean expanded = true;

    public Token(int type, int start, int end, int line, int linePosition, String text) {
        this.type = type;

        this.start = start;
        this.end = end;

        this.line = line;
        this.linePosition = linePosition;

        this.text = text;

        this.attribute = text.substring(start, end);
        this.isAllUpperCase =  attribute.equals(attribute.toUpperCase());
    }

    public String getAttribute() {
        return attribute;
    }

    public int getLinePosition() {
        return linePosition;
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

    public boolean isAllUpperCase() {
        return isAllUpperCase;
    }

    public boolean equals(Object otherObject) {
        Token otherToken = (Token)otherObject;
        return type == otherToken.type && start == otherToken.start && end == otherToken.end;
    }

    public int compareTo(Object o) {
        Token otherToken = (Token)o;
        return this.getAttribute().compareTo(otherToken.getAttribute());
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void foldingEntitySetExpanded(boolean expanded) {
        setExpanded(expanded);
    }

    public boolean foldingEntityIsExpanded() {
        return isExpanded();
    }

    public int foldingEntityGetStartParagraphIndex() {
        return getStartIndex();
    }

    public int foldingEntityGetStartIndex() {
        return getStartIndex();
    }

    public int foldingEntityGetEndIndex() {
        return getEndIndex();
    }

    public String getFoldedPlaceholderString() {
        return "{...}";
    }

    public String toString() {
        return "{ "+getAttribute()+" = "+type+" ("+start+", "+end+") }";
    }

}
