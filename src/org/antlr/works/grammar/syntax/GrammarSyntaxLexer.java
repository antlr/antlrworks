package org.antlr.works.grammar.syntax;

import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.element.ElementToken;

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

public class GrammarSyntaxLexer extends ATESyntaxLexer {

    public static final int TOKEN_REFERENCE = 100;
    public static final int TOKEN_LABEL = 101;
    public static final int TOKEN_BLOCK_LABEL = 102;
    public static final int TOKEN_BLOCK_LIMIT = 103;
    public static final int TOKEN_REWRITE = 104;
    public static final int TOKEN_DECL = 105;
    public static final int TOKEN_OPEN_DOUBLE_ANGLE = 106;
    public static final int TOKEN_CLOSE_DOUBLE_ANGLE = 107;
    public static final int TOKEN_INTERNAL_REF = 108; // i.e. { ... $type ... }

    @Override
    protected ATEToken customMatch() {
        if(c0 == '@') {
            return matchID();
        } else if(c0 == '-' && c1 == '>') {
            int sp = position;
            position++;
            return createNewToken(TOKEN_REWRITE, sp, position);
        } else if(c0 == '<' && c1 == '<') {
            int sp = position;
            position++;
            return createNewToken(TOKEN_OPEN_DOUBLE_ANGLE, sp, position);
        } else if(c0 == '>' && c1 == '>') {
            int sp = position;
            position++;
            return createNewToken(TOKEN_CLOSE_DOUBLE_ANGLE, sp, position);
        } else {
            return null;
        }
    }

    @Override
    protected ATEToken matchID() {
        int sp = position;
        if(c0 == '@') {
            // This kind of ID can contain ':', for example:
            // @header::lexer
            while((isID(c1) || c1 == ':') && nextCharacter()) {
            }
        } else {
            // Match regular ID
            while(isID(c1) && nextCharacter()) {
            }
        }

        return createNewToken(TOKEN_ID, sp);
    }

    @Override
    public ATEToken createNewToken(int type, int start, int end,
                                   int startLineNumber, int endLineNumber,
                                   int startLineIndex, int endLineIndex)
    {
        return new ElementToken(type, start, end, startLineNumber, endLineNumber, startLineIndex, endLineIndex, text);
    }


}
