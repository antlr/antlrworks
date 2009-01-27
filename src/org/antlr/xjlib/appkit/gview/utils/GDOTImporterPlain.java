package org.antlr.xjlib.appkit.gview.utils;

import org.antlr.xjlib.appkit.gview.base.Vector2D;
import org.antlr.xjlib.appkit.gview.object.GElement;
import org.antlr.xjlib.appkit.gview.object.GElementCircle;
import org.antlr.xjlib.appkit.gview.object.GLink;

import java.io.IOException;
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

public class GDOTImporterPlain extends GDOTImporter {

    public static int factor = 80;

    public GElement parseLine(String line) throws IOException {
        /*
            graph 1.000 2.583 9.056
        */

        String[] tokens = parseTokens(line);
        if(tokens[0].equals("graph"))
            height = Float.parseFloat(tokens[3]);
        else if(tokens[0].equals("node"))
            return createGraphNode(tokens);
        else if(tokens[0].equals("edge"))
            return createGraphEdge(tokens);
//        else if(tokens[0].equals("stop"))

        return null;
    }

    public GElement createGraphNode(String[] tokens) {
        /*
node s0  1.097 1.917 0.506 0.506 s0 solid circle black lightgrey
node "s1=>2"  0.486 0.486 0.969 0.969 "s1=>2" solid doublecircle black lightgrey
        */

        float x = Float.parseFloat(tokens[2])*factor;
        float y = (height - Float.parseFloat(tokens[3]))*factor;
        float w = Float.parseFloat(tokens[4])*factor;
        float h = Float.parseFloat(tokens[5])*factor;

        Node node = new Node();
        node.setDraggable(true);
        node.setPosition(x, y);
        node.setSize(w, h);
        node.setRadius(w/2);
        node.setLabel(tokens[6]);
        node.setDouble(tokens[8].equals("doublecircle"));

        return node;
    }

    public GElement createGraphEdge(String[] tokens) {
//  0    1     2  3 4
//  edge start n1 7 1.153 8.556 1.125 8.417 1.097 8.236 1.111 8.083 1.111 8.042 1.125 8.014 1.125 7.972
//  g 1.194 8.194 solid black
//  edge rule foo 4 0.375 1.000 0.375 0.889 0.375 0.764 0.375 0.639 solid black

        int controlCount = (int) Float.parseFloat(tokens[3]);
        Vector2D points[] = new Vector2D[controlCount];
        for(int index=0; index<controlCount; index++) {
            points[index] = new Vector2D(Float.parseFloat(tokens[4+index*2])*factor,
                    (height-Float.parseFloat(tokens[4+index*2+1]))*factor);
        }

        int labelIndex = 3+2*controlCount+1;
        String label = null;
        Vector2D labelPosition = null;
        if(isFloatString(tokens[labelIndex+1])) {
            // Apparently there is a label because there is a float coordinate
            label = tokens[labelIndex];
            labelPosition = new Vector2D(Float.parseFloat(tokens[labelIndex+1])*factor,
                                (height-Float.parseFloat(tokens[labelIndex+2]))*factor);
        }

        GElement source = graph.findElementWithLabel(tokens[1]);
        GElement target = graph.findElementWithLabel(tokens[2]);

        GLink link = new GLink(source, GElementCircle.ANCHOR_CENTER,
                target, GElementCircle.ANCHOR_CENTER,
                GLink.SHAPE_BEZIER, label, 0);

        link.setBezierControlPoints(points);
        link.setBezierLabelPosition(labelPosition);

        return link;
    }

}
