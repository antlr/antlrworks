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
import org.antlr.works.visualization.graphics.GObject;
import org.antlr.works.visualization.graphics.shape.GLink;
import org.antlr.works.visualization.graphics.shape.GNode;
import org.antlr.works.visualization.skin.Skin;

import java.awt.*;

public class NFASkin extends Skin {

    public NFASkin() {
        value_epsilon_width = 10;
        value_epsilon_up = 0;
        value_epsilon_down = 0;

        value_box_width = 20;
        value_box_up = 0;
        value_box_down = 0;

        value_node_width = 10;
        value_node_up = 5;
        value_node_down = 5;

        value_char_width = 3.5f;
        value_line_space = 2;
    }

    public boolean isLinkVisible() {
        return true;
    }

    public boolean isNodeVisible() {
        return true;
    }

    public float getStartOffset(GContext context) {
        return context.getPixelNodeWidth()/2;
    }

    public float getEndOffset(GContext context) {
        return context.getPixelNodeWidth()/2;
    }

    public boolean objectContainsPoint(GObject object, Point p) {
        if(object instanceof GNode)
            return NFANode.nodeContainsPoint((GNode)object, p);
        return false;
    }

    public void drawNode(GNode node) {
        NFANode.draw(node);
    }

    public void drawLink(GLink link) {
        NFALink.draw(link);
    }


}
