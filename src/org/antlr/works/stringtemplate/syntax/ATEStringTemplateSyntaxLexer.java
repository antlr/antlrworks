package org.antlr.works.stringtemplate.syntax;

import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.ate.syntax.misc.ATELine;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;

/*

[The "BSD licence"]
Copyright (c) 2009
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

public class ATEStringTemplateSyntaxLexer extends ATESyntaxLexer {

    public static final int TOKEN_DEFINED_TO_BE = 101;
    public static final int TOKEN_OPEN_DOUBLE_ANGLE = 102;
    public static final int TOKEN_CLOSE_DOUBLE_ANGLE = 103;
    public static final int TOKEN_OPEN_SINGLE_ANGLE = 104;
    public static final int TOKEN_CLOSE_SINGLE_ANGLE = 105;
    public static final int TOKEN_DOUBLE_QUOTE = 106;
    public static final int TOKEN_ANGLE_COMMENT = 107;
    public static final int TOKEN_DOLLAR_COMMENT = 108;
    public static final int TOKEN_DOLLAR = 109;
    public static final int TOKEN_COMMA = 112;
    public static final int TOKEN_DECL = 113;
    public static final int TOKEN_ESCAPED_DELIMITER = 114;
    public static final int TOKEN_LITERAL = 115;
    public static final int TOKEN_REFERENCE = 116;
    public static final int TOKEN_NEWLINE = 117;
    public static final int TOKEN_ARG_REFERENCE = 118;
    public static final int TOKEN_ARG_DECL = 119;
    public static final int TOKEN_MAP_REFERENCE = 120;
    public static final int TOKEN_MAP_DECL = 121;
    public static final int TOKEN_START_SINGLE_COMMENT = 122;
    public static final int TOKEN_START_COMPLEX_COMMENT = 123;
    public static final int TOKEN_END_COMPLEX_COMMENT = 124;
    public static final int TOKEN_SINGLE_QUOTE = 125;
    public static final int TOKEN_EQUAL = 126;

    public ATEStringTemplateSyntaxLexer() {
    }

    @Override
    protected ATEToken customMatch() {
        if(c0 == '@') {
            return matchID();
        } else if(c0 == '<' && c1 == '<') {
            int sp = position;
            position++;
            return createNewToken(TOKEN_OPEN_DOUBLE_ANGLE, sp, position+1);
        } else if(c0 == '>' && c1 == '>') {
            int sp = position;
            position++;
            return createNewToken(TOKEN_CLOSE_DOUBLE_ANGLE, sp, position+1);
        } else if(c0 == '\\' && c1 == '<') {
            int sp = position;
            position++;
            return createNewToken(TOKEN_ESCAPED_DELIMITER, sp, position+1);
        } else if(c0 == '\\' && c1 == '$') {
            int sp = position;
            position++;
            return createNewToken(TOKEN_ESCAPED_DELIMITER, sp, position+1);
        } else if(c0 == '/' && c1 == '/') {
            int sp = position;
            position++;
            return createNewToken(TOKEN_START_SINGLE_COMMENT, sp, position+1);
       } else if(c0 == '/' && c1 == '*') {
            int sp = position;
            position++;
            return createNewToken(TOKEN_START_COMPLEX_COMMENT, sp, position+1);
        } else if(c0 == '*' && c1 == '/') {
             int sp = position;
             position++;
             return createNewToken(TOKEN_END_COMPLEX_COMMENT, sp, position+1);
        } else if(c0 == ':' && c1 == ':') {
            return matchDefinedToBe();
        } else if(c0 == '<' && c1 == '!') {
            return matchAngleComment();
        } else if(c0 == '$' && c1 == '!') {
            return matchDollarComment();
        } else if(c0 == '<') {
            return createNewToken(TOKEN_OPEN_SINGLE_ANGLE);
        } else if(c0 == '>') {
            return createNewToken(TOKEN_CLOSE_SINGLE_ANGLE);
        } else if(c0 == '$') {
            return createNewToken(TOKEN_DOLLAR);
        } else if(c0 == ',') {
            return createNewToken(TOKEN_COMMA);
        } else if(c0 == '\'') {
            return createNewToken(TOKEN_SINGLE_QUOTE);
        } else if(c0 == '"') {
            return createNewToken(TOKEN_DOUBLE_QUOTE);
        } else if(c0 == '=') {
            return createNewToken(TOKEN_EQUAL);
        } else if(matchNewLine()) {
            return createNewToken(TOKEN_NEWLINE);
        } else {
            return null;
        }
    }

    @Override
    protected ATEToken matchID() {
        int sp = position;
        if(c0 == '@') {
            // This kind of ID can contain '.', for example:
            // @ID.attr
            while((isID(c1) || c1 == '.') && nextCharacter()) {}
        } else {
            // Match regular ID
            while(isID(c1) && nextCharacter()) {}
        }
        return createNewToken(TOKEN_ID, sp);
    }

    public ATEToken matchAngleComment() {
        int sp = position;
        while(nextCharacter()) {
            if((c0 == '!' && c1 == '>') || matchNewLine()) {
                // Don't forget to eat the next character
                if ((c0 == '!' && c1 == '>')) nextCharacter();
                return createNewToken(TOKEN_ANGLE_COMMENT, sp);
            }
        }
        // Complex comment terminates also at the end of the text
        return createNewToken(TOKEN_ANGLE_COMMENT, sp, position);
    }

    public ATEToken matchDollarComment() {
        int sp = position;
        while(nextCharacter()) {
            if((c0 == '!' && c1 == '$') || matchNewLine()) {
                // Don't forget to eat the next character
                if ((c0 == '!' && c1 == '$')) nextCharacter();
                return createNewToken(TOKEN_DOLLAR_COMMENT, sp);
            }
        }
        // Complex comment terminates also at the end of the text
        return createNewToken(TOKEN_DOLLAR_COMMENT, sp, position);
    }

    public ATEToken matchDefinedToBe() {
        int sp = position;
        nextCharacter(); // consume first ':'
        nextCharacter(); // consume second ':'
        if (c0 == '=') {
            return createNewToken(TOKEN_DEFINED_TO_BE, sp, Math.min(position+1, text.length()));
        } else {
            while (nextCharacter()) {
                if(isWhitespace()) {
                    return createNewToken(TOKEN_OTHER, sp, Math.min(position+1, text.length()));
                }
            }
        }

        return createNewToken(TOKEN_OTHER, sp, Math.min(position+1, text.length()));
    }

    @Override
    public boolean nextCharacter() {
        boolean valid = false;
        final int length = text.length();
        controlCharacter = false;

        c0 = c1 = 0;
        position++;
        if(position < length) {
            valid = position < length;
            if(valid) {
                c0 = text.charAt(position);
                if(position + 1 < length)
                    c1 = text.charAt(position+1);
            }

            if(matchNewLine()) {
                lineNumber++;
                lineIndex = position+1;
                lines.add(new ATELine(lineIndex));
            }
        }
        return valid;
    }

    @Override
    public ATEToken matchSingleQuoteString() {
        return null;
    }

    @Override
    public ATEToken matchDoubleQuoteString() {
        return null;
    }
}
