package org.antlr.works.test.ut;

import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.antlr.works.test.AbstractTest;
import org.antlr.xjlib.foundation.XJUtils;
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

public class TestUtils extends AbstractTest {

    public static void main(String[] args) {
        new TestRunner().doRun(new TestSuite(TestUtils.class));
    }

    public void testNormalizeText() throws Exception {
        final String textNotNormalized = "ab\r\nc\ndef\rghj";
        final String textNormalized = "ab\nc\ndef\nghj";

        assertEquals("normalized", textNormalized, XJUtils.getNormalizedText(textNotNormalized));
    }

    public void testLocalizeText() throws Exception {
        final String textNormalized = "ab\nc\ndef\nghj";

        assertEquals("localize 1", textNormalized, XJUtils.getTextByReplacingEOL(textNormalized, "\n"));
        assertEquals("localize 2", "ab\rc\rdef\rghj", XJUtils.getTextByReplacingEOL(textNormalized, "\r"));
        assertEquals("localize 3", "ab\r\nc\r\ndef\r\nghj", XJUtils.getTextByReplacingEOL(textNormalized, "\r\n"));

        assertEquals("localize 4", textNormalized, XJUtils.getTextByReplacingEOL("ab\r\nc\r\ndef\r\nghj", "\n"));
        assertEquals("localize 5", "ab\rc\rdef\rghj", XJUtils.getTextByReplacingEOL("ab\r\nc\r\ndef\r\nghj", "\r"));
        assertEquals("localize 6", "ab\r\nc\r\ndef\r\nghj", XJUtils.getTextByReplacingEOL("ab\r\nc\r\ndef\r\nghj", "\r\n"));

        assertEquals("localize 7", textNormalized, XJUtils.getTextByReplacingEOL("ab\rc\r\ndef\rghj", "\n"));
        assertEquals("localize 8", "ab\rc\rdef\rghj", XJUtils.getTextByReplacingEOL("ab\rc\rdef\nghj", "\r"));
        assertEquals("localize 9", "ab\r\nc\r\ndef\r\nghj", XJUtils.getTextByReplacingEOL("ab\rc\rdef\r\nghj", "\r\n"));
    }

}