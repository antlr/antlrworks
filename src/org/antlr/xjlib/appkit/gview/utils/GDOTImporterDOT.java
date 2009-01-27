package org.antlr.xjlib.appkit.gview.utils;

import org.antlr.xjlib.appkit.gview.base.Vector2D;
import org.antlr.xjlib.appkit.gview.object.GElement;
import org.antlr.xjlib.appkit.gview.object.GElementCircle;
import org.antlr.xjlib.appkit.gview.object.GLink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

public class GDOTImporterDOT extends GDOTImporter {

    public static final int INCH_TO_PIXEL = 72;

    /** Example of DOT output:
     digraph {
     node [label="\N"];
     graph [bb="0,0,229,324"];
     compilationUnit [pos="162,306", width="1.64", height="0.50"];
     packageDefinition [pos="70,234", width="1.78", height="0.50"];
     identifier [pos="83,162", width="1.06", height="0.50"];
     IDENT [pos="144,90", width="0.94", height="0.50"];
     DOT [pos="65,90", width="0.75", height="0.50"];
     A [pos="65,18", width="0.75", height="0.50"];
     compilationUnit -> packageDefinition [pos="e,92,251 140,289 128,280 113,268 100,258"];
     packageDefinition -> identifier [pos="e,80,180 73,216 74,208 76,199 78,190"];
     identifier -> IDENT [pos="e,126,105 92,144 99,134 109,123 119,112"];
     identifier -> DOT [pos="e,69,108 78,144 76,136 74,127 72,118"];
     identifier -> IDENT [pos="e,135,108 102,146 111,137 120,126 129,116"];
     IDENT -> compilationUnit [pos="e,160,288 145,108 149,146 156,233 159,278"];
     DOT -> A [pos="e,65,36 65,72 65,64 65,55 65,46"];
     A -> compilationUnit [pos="e,171,288 92,21 121,26 166,40 187,72 229,135 196,232 175,279"];
     A -> packageDefinition [pos="e,56,216 51,34 43,44 33,58 29,72 15,118 20,134 36,180 39,190 45,199 50,208"];
     }
     */

    public GElement parseLine(String line) throws IOException {
        String[] tokens = parseTokens(line);
        if(tokens[0].equals("graph"))
            parseGraphHeight(tokens);
        else if(tokens.length >= 2 && !tokens[0].equals("node") && tokens[1].equals("["))
            return createGraphNode(tokens);
        else if(tokens.length >= 3 && tokens[1].equals("-") && tokens[2].equals(">"))
            return createGraphEdge(tokens);

        return null;
    }

    public void parseGraphHeight(String[] tokens) throws IOException {
        // graph [bb="0,0,229,324"];
        if(tokens[2].equals("bb")) {
            String[] t = parseTokens(tokens[4]);
            height = Float.parseFloat(t[6]);
        }
    }

    public GElement createGraphNode(String[] tokens) throws IOException {
        // identifier [pos="83,162", width="1.06", height="0.50"];
        //      0     1 2 3 4      5  6   7  8   9  10   11  12 13 14
        // "s2=>2" [shape=doublecircle, pos="35,35", width="0.97", height="0.97"];

        float x = 0;
        float y = 0;
        float w = 0;
        float h = 0;
        boolean doublecircle = false;

        int index = 0;
        while(index < tokens.length-1) {
            index++;
            if(tokens[index].equals("pos")) {
                String[] posTokens = parseTokens(tokens[index+=2]);
                x = Float.parseFloat(posTokens[0]);
                y = (height - Float.parseFloat(posTokens[2]));
            } else if(tokens[index].equals("width")) {
                w = Float.parseFloat(tokens[index+=2])*INCH_TO_PIXEL;
            } else if(tokens[index].equals("height")) {
                h = Float.parseFloat(tokens[index+=2])*INCH_TO_PIXEL;
            } else if(tokens[index].equals("shape")) {
                doublecircle = tokens[index+=2].equals("doublecircle");
            }
        }

        Node node = new Node();
        node.setDraggable(true);
        node.setPosition(x, y);
        node.setSize(w, h);
        node.setRadius(w/2);
        node.setLabel(tokens[0]);
        node.setDouble(doublecircle);

        return node;
    }

    public GElement createGraphEdge(String[] tokens) throws IOException {
        //  DOT -> foo_bar [pos="e,65,36 65,72 65,64 65,55 65,46"];
        //   0  12     3   4 5 6               7                 8 9
        //  DOT -> A [label=foo, pos="e,153,33 119,88 124,78 129,65 136,54 139,49 142,45 146,40", lp="146,62"];
        //   0  12 3 4 5   6 7 8  9 10     11

        String sourceName = tokens[0];
        String targetName = tokens[3];

        String labelName = null;
        Vector2D labelPosition = null;
        Vector2D points[] = null;

        int index = 4;
        while(index < tokens.length-1) {
            index++;

            if(tokens[index].equals("label")) {
                // Label name
                labelName = tokens[index+=2];
            } else if(tokens[index].equals("lp")) {
                // Label position
                String[] lpos = parseTokens(tokens[index+=2]);
                labelPosition = new Vector2D(Float.parseFloat(lpos[0]), height-Float.parseFloat(lpos[2]));
            } else if(tokens[index].equals("pos")) {
                // Edge control points
                points = parseControlPoints(tokens[index+=2]);
            } else if(tokens[index].equals(";"))
                break;
        }

        GElement source = graph.findElementWithLabel(sourceName);
        GElement target = graph.findElementWithLabel(targetName);

        GLink link = new GLink(source, GElementCircle.ANCHOR_CENTER,
                target, GElementCircle.ANCHOR_CENTER,
                GLink.SHAPE_BEZIER, labelName, 0);

        if(points == null) {
            System.err.println("No points for "+sourceName+", "+targetName+", "+tokens.length);
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i];
                System.out.println(token);
            }
        }

        link.setBezierControlPoints(points);
        link.setBezierLabelPosition(labelPosition);

        return link;
    }

    public Vector2D[] parseControlPoints(String s) throws IOException {
        // e,56,216 51,34 43,44 33,58 29,72 15,118 20,134 36,180 39,190 45,199 50,208
        List<Vector2D> points = new ArrayList<Vector2D>();
        Vector2D endPoint = null;

        String[] pts = parseTokens(s);
        int index = -1;
        while(++index < pts.length) {
            if(pts[index].equals("e")) {
                // Arrow at the end
                if(index+2 >= pts.length) {
                    System.err.println(String.format("Expected x arrow position at %d but reached the end at %d", index+2, pts.length));
                    continue;
                }
                if(index+4 >= pts.length) {
                    System.err.println(String.format("Expected y arrow position at %d but reached the end at %d", index+4, pts.length));
                    continue;
                }
                String x = pts[index+=2];
                String y = pts[index+=2];
                endPoint = new Vector2D(Float.parseFloat(x), height-Float.parseFloat(y));
            } else if(isFloatString(pts[index]) && index+2 < pts.length && isFloatString(pts[index+2])) {
                // Assume pair of numbers
                if(index+2 >= pts.length) {
                    System.err.println(String.format("Expected y position at %d but reached the end at %d", index+2, pts.length));
                    continue;
                }
                String x = pts[index];
                String y = pts[index+=2];
                points.add(new Vector2D(Float.parseFloat(x), height-Float.parseFloat(y)));
            }
        }

        if(endPoint != null)
            points.add(endPoint);

        Vector2D p[] = new Vector2D[points.size()];
        for(int i=0; i<points.size(); i++) {
            p[i] = points.get(i);
        }
        return p;
    }
}
