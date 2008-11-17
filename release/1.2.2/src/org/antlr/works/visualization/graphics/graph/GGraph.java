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

package org.antlr.works.visualization.graphics.graph;

import org.antlr.analysis.NFAState;
import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.primitive.GDimension;
import org.antlr.works.visualization.graphics.shape.GLink;
import org.antlr.works.visualization.graphics.shape.GNode;
import org.antlr.works.visualization.serializable.SEncoder;
import org.antlr.works.visualization.serializable.SSerializable;

import java.awt.*;
import java.util.List;

public class GGraph extends GGraphAbstract implements SSerializable {

    private GDimension dimension;
    public List<GNode> nodes;

    public String name;
    public String nameWidth;

    public float offsetX = 0;
    public float offsetY = 0;

    public void setEnable(boolean flag) {
        
    }

    public void setContext(GContext context) {
        super.setContext(context);
        for (GNode node : nodes) {
            node.setContext(context);
        }
    }

    public void setName(String name) {
        this.name = name;
        nameWidth = GContext.getStringWidth(name);
    }

    public void setDimension(GDimension dimension) {
        this.dimension = dimension;
    }

    public void setNodes(List<GNode> nodes) {
        this.nodes = nodes;
    }

    public GDimension getDimension() {
        GDimension d = new GDimension(dimension);
        if(context.isShowRuleName()) {
            d.addWidth(nameWidth);            
        }
        return d;
    }

    public float getHeight() {
        /** Make sure that the height is at least the one of a single arrow because
         * for an empty rule, there is at least one horizontal link with an arrow at
         * the end.
         */
        return Math.max(getDimension().getPixelHeight(context), context.getPixelArrowHeight());
    }

    public float getWidth() {
        return getDimension().getPixelWidth(context);
    }

    public void render(float ox, float oy) {
        oy += getDimension().getPixelUp(context);

        float titleOffset = context.isShowRuleName()?context.getPixelValue(nameWidth):0;
        for (GNode node : nodes) {
            node.render(ox+titleOffset, oy);
        }

        offsetX = ox;
        offsetY = oy;

        setRendered(true);
    }

    public static final int TITLE_OFFSET = 100;

    public void draw() {
        context.nodeColor = Color.black;
        context.linkColor = Color.black;
        context.setLineWidth(1);

        context.setColor(Color.black);
        if(context.isShowRuleName()) {
            context.drawString(context.getRuleFont(), name, offsetX, offsetY, GContext.ALIGN_LEFT);            
        }

        for (GNode node : nodes) {
            node.drawNodeAndLink();
        }

        if(context.drawdimension) {
            context.setColor(Color.lightGray);
            float width = getDimension().getPixelWidth(context);
            float up = getDimension().getPixelUp(context);
            float down = getDimension().getPixelDown(context);
            if(up+down>0)
                context.drawRect(offsetX, offsetY-up, width, up+down, false);
        }
    }

    public GLink findLinkAtPosition(int x, int y) {
        for (GNode node : nodes) {
            for (GLink link : node.links) {
                /** Only non-null transition label has to be tested (that is, visible
                 * syntax diagram box, not simple line)
                 */
                if (link.containsPoint(new Point(x, y)) && link.transition.label != null)
                    return link;
            }
        }
        return null;
    }

    public GNode findNodeForStateNumber(int stateNumber) {
        for (GNode node : nodes) {
            if (node.state.stateNumber == stateNumber) {
                return node;
            }
        }
        return null;
    }

    public boolean containsAtLeastOneState(List states) {
        for (GNode node : nodes) {
            for (Object state1 : states) {
                NFAState state = (NFAState) state1;
                if (node.containsStateNumber(state.stateNumber))
                    return true;
            }
        }
        return false;
    }

    public void encode(SEncoder encoder) {
        encoder.write(dimension);
        for(GNode node : nodes) {
            encoder.write(node);
        }
    }

}
