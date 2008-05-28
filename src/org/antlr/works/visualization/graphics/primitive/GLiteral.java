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

package org.antlr.works.visualization.graphics.primitive;

import java.util.Map;

/** This class is used to manipulate literal value in position/dimension.
 * Very useful in order to quickly redraw a graph without having to walk recursively
 * the NFA (this allows to remove the associated NFA and to be more independant of it)
 *
 */

public class GLiteral {

    public static final String OP_SUB = "-";
    public static final String OP_MAX = "#";
    public static final String OP_LPAREN = "(";
    public static final String OP_RPAREN = ")";
    public static final String OP_COMA = ",";
    public static final String OP_NULL = "!";

    public static String substract(String a, String b) {
        return a+OP_SUB+OP_LPAREN+b+OP_RPAREN;
    }

    public static String add(String a, String b) {
        return a+b;
    }

    public static String max(String a, String b) {
        boolean anull = a == null || a.length() == 0;
        boolean bnull = b == null || b.length() == 0;
        if(anull && bnull)
            return "";
        else if(anull)
            return b;
        else if(bnull)
            return a;
        else
            return OP_MAX+OP_LPAREN+a+OP_COMA+b+OP_RPAREN;
    }

    public static float evaluate(String s, Map<String,Float> values) {
        if(s == null)
            return 0;

        return new Evaluator(s, values).evaluate();
    }

    private static class Evaluator {

        public String s;
        public Map<String,Float> values;
        public int position;

        public Evaluator(String s, Map<String,Float> values) {
            this.s = s;
            this.values = values;
        }

        public float evaluate() {
            position = 0;
            return eval();
        }

        private float eval() {
            float _value = 0;

            do {
                Float v = values.get(c());
                if(v != null) {
                    _value += v;
                } else {
                    if(c().equals(OP_SUB))
                        _value -= evaluate_sub();
                    else if(c().equals(OP_MAX))
                        _value += evaluate_max();
                    else if(c().equals(OP_RPAREN))
                        break;
                    else if(c().equals(OP_COMA))
                        break;
                    else
                        System.err.println("** Evaluator: unexpected token \""+c()+"\"");
                }
            } while(nextChar());

            return _value;
        }

        private float evaluate_sub() {
            match(OP_SUB);
            match(OP_LPAREN);
            return eval();
        }

        private float evaluate_max() {
            match(OP_MAX);
            match(OP_LPAREN);
            float a = eval();
            match(OP_COMA);
            float b = eval();
            return Math.max(a,b);
        }

        private boolean nextChar() {
            position++;
            return position<s.length();
        }

        private void match(String token) {
            if(c().equals(token))
                nextChar();
            else
                System.err.println("** Evaluator: unexpected token \""+c()+"\"");
        }

        private String c() {
            return s.substring(position, position+1);
        }
    }
}
