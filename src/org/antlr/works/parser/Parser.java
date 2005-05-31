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

import org.antlr.works.visualization.grammar.GrammarEngineError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Parser {

    public List tokens;
    public int position;

    public Token currentToken;

    public static final String ATTRIBUTE_FRAGMENT = "fragment";

    protected Lexer lexer;

    public Parser() {
    }

    public List parse(String text) {
       // long t = System.currentTimeMillis();

        List rules = new ArrayList();
        lexer = new Lexer(text);
        tokens = lexer.parseTokens();
        position = -1;
        while(nextToken()) {
            if(!matchOptionsTokensBlock()) {
                if(T(0).type == Lexer.TOKEN_ID) {
                    Rule rule = matchRule();
                    if(rule != null)
                        rules.add(rule);
                }
            }
        }

        //System.out.println((float)(System.currentTimeMillis()-t)/1000);

        return rules;
    }

    public List getLines() {
        if(lexer == null)
            return null;
        else
            return lexer.lines;
    }

    public int getMaxLines() {
        return lexer.line;
    }

    public boolean matchOptionsTokensBlock() {
        if(T(0).type != Lexer.TOKEN_ID)
            return false;

        if(T(0).getAttribute().equals("options") || T(0).getAttribute().equals("tokens")) {
            if(T(1) == null)
                return false;

            if(T(1).type != Lexer.TOKEN_BLOCK)
                return false;

            nextToken();
            return true;
        } else
            return false;
    }

    public Rule matchRule() {
        Token start = T(0);
        String name = start.getAttribute();

        if(start.getAttribute().equals(ATTRIBUTE_FRAGMENT)) {
            nextToken();
            name = T(0).getAttribute();
        }

        boolean colonFound = false;
        while(nextToken()) {
            if(T(0).type == Lexer.TOKEN_SEMI) {
                break;
            }
            if(T(0).type == Lexer.TOKEN_COLON) {
                colonFound = true;
                break;
            }
        }
        if(!colonFound)
            return null;

        while(nextToken()) {
            if(T(0).type == Lexer.TOKEN_SEMI)
                return new Rule(name, start, T(0));
        }
        return null;
    }

    public boolean nextToken() {
        position++;
        return position<tokens.size();
    }

    public Token T(int index) {
        return (Token)tokens.get(position+index);
    }

    public class Rule {

        public String name;
        public Token start;
        public Token end;

        public List errors;

        public Rule(String name, Token start, Token end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

        public int getStartIndex() {
            return start.start;
        }

        public int getEndIndex() {
            return end.end;
        }

        public int getLength() {
            return getEndIndex()-getStartIndex();
        }
        
        public List getBlocks() {
            List blocks = new ArrayList();
            Token lastToken = null;
            for(int index=tokens.indexOf(start); index<tokens.indexOf(end); index++) {
                Token token = (Token)tokens.get(index);
                if(token.type == Lexer.TOKEN_BLOCK) {
                    if(lastToken != null && lastToken.type == Lexer.TOKEN_ID && lastToken.getAttribute().equals("options"))
                        continue;

                    blocks.add(token);
                }
                lastToken = token;
            }
            return blocks;
        }

        public void setErrors(List errors) {
            this.errors = errors;
        }

        public boolean hasErrors() {
            if(errors == null)
                return false;
            else
                return errors.size()>0;
        }

        public String getErrorMessageString(int index) {
            GrammarEngineError error = (GrammarEngineError) errors.get(index);
            return error.message;
        }

        public String getErrorMessageHTML() {
            StringBuffer message = new StringBuffer();
            message.append("<html>");
            for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
                GrammarEngineError error = (GrammarEngineError) iterator.next();
                message.append(error.message);
                if(iterator.hasNext())
                    message.append("<br>");
            }
            message.append("</html>");
            return message.toString();
        }

        public String toString() {
            return name;
        }
    }

}
