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

package org.antlr.works.visualization.skin.nfa;

import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.shape.GNode;

import java.awt.*;

public class NFANode {

    public static void draw(GNode node) {
        GContext context = node.getContext();
        context.setColor(context.nodeColor);
        float r = context.getPixelNodeWidth()/2;
        context.drawCircle(node.getX()+r, node.getY(), r, true);
        context.drawString(context.getBoxFont(), node.state.toString(), node.getX()+r, node.getY(), GContext.ALIGN_CENTER);
    }

    public static boolean nodeContainsPoint(GNode node, Point p) {
        float nr = node.getContext().getPixelNodeWidth()/2;
        double r = Math.sqrt(Math.pow(p.x-node.getX()-nr, 2)+Math.pow(p.y-node.getY(), 2));
        return r<=nr;
    }
}
