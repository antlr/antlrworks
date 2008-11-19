package org.antlr.works.test.ut;

import org.antlr.works.test.AbstractTest;
import org.antlr.works.test.TestConstants;
import org.antlr.xjlib.foundation.XJUtils;

import java.io.File;/*

[The "BSD licence"]
Copyright (c) 2005-07 Jean Bovet
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

public class TestSyntaxDiagram extends AbstractTest {

    public void testVSQL() throws Exception {
    //    todo redo these syntax diagram
//        assertSD("vsql", "vsql");
    }

    public void testExpr() throws Exception {
        //    todo redo these syntax diagram
  //      assertSD("expr", "expr");
    }

    private void assertSD(String folder, String name) throws Exception {
        // Read the grammar file
        String source = getResourceFile(TestConstants.PREFIX+"sd/"+folder+"/"+name+".g");
        String target = File.createTempFile(name+"-sd", ".txt").getAbsolutePath();

        // Generate the XML-string representation of the syntax diagram
        String[] args = new String[] { "-f", source, "-serialize", target};
        org.antlr.works.Console.main(args);

        // Compare the result with the one that has been pre-generated
        String expected = getTextFromFile(TestConstants.PREFIX+"sd/"+folder+"/"+name+".txt");
        String actual = XJUtils.getStringFromFile(target);
        // normalize the line ending
        expected = expected.replaceAll("\\\r\\\n", "\\\n");
        assertEquals(name, expected, actual);
    }

}
