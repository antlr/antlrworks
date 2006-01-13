package org.antlr.works.ate.syntax.language;

import org.antlr.works.ate.syntax.generic.ATESyntaxEngine;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.generic.ATESyntaxParser;
import org.antlr.works.ate.syntax.misc.ATEToken;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
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

public class ATELanguageSyntaxEngine extends ATESyntaxEngine {

    protected SimpleAttributeSet commentAttr;
    protected SimpleAttributeSet stringAttr;
    protected SimpleAttributeSet keywordAttr;

    public ATELanguageSyntaxEngine() {
        commentAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(commentAttr, Color.lightGray);
        StyleConstants.setItalic(commentAttr, true);

        stringAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(stringAttr, new Color(0, 0.5f, 0));
        StyleConstants.setBold(stringAttr, true);

        keywordAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(keywordAttr, new Color(0, 0, 0.5f));
        StyleConstants.setBold(keywordAttr, true);
    }

    public ATESyntaxLexer createLexer() {
        return new ATELanguageSyntaxLexer();
    }

    public ATESyntaxParser createParser() {
        // No parser need for generic computer language
        return null;
    }

    public Set getKeywords() {
        return new HashSet();
    }

    public AttributeSet getAttributeForToken(ATEToken token) {
        AttributeSet attr = null;
        switch(token.type) {
            case ATESyntaxLexer.TOKEN_COMPLEX_COMMENT:
            case ATESyntaxLexer.TOKEN_SINGLE_COMMENT:
                attr = commentAttr;
                break;
            case ATESyntaxLexer.TOKEN_DOUBLE_QUOTE_STRING:
            case ATESyntaxLexer.TOKEN_SINGLE_QUOTE_STRING:
                attr = stringAttr;
                break;
            default:
                if(getKeywords().contains(token.getAttribute()))
                    attr = keywordAttr;
                break;
        }
        return attr;
    }

}
