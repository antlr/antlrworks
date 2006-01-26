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

import java.awt.*;
import java.util.Iterator;
import java.util.List;

public class GGraph extends GGraphAbstract {

    public GDimension dimension;
    public List nodes;

    public String name;

    public float offsetX = 0;
    public float offsetY = 0;

    public void setEnable(boolean flag) {
        
    }

    public void setContext(GContext context) {
        super.setContext(context);
        for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
            GNode node = (GNode) iterator.next();
            node.setContext(context);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDimension(GDimension dimension) {
        this.dimension = dimension;
    }

    public void setNodes(List nodes) {
        this.nodes = nodes;
    }

    public GDimension getDimension() {
        return dimension;
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

        Iterator iterator = nodes.iterator();
        while(iterator.hasNext()) {
            GNode node = (GNode)iterator.next();
            node.render(ox, oy);
        }

        offsetX = ox;
        offsetY = oy;

        setRendered(true);
    }

    public void draw() {
        context.nodeColor = Color.black;
        context.linkColor = Color.black;
        context.setLineWidth(1);
        
        Iterator iterator = nodes.iterator();
        while(iterator.hasNext()) {
            GNode node = (GNode)iterator.next();
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
        for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
            GNode node = (GNode) iterator.next();
            for (Iterator iterator1 = node.links.iterator(); iterator1.hasNext();) {
                GLink link = (GLink) iterator1.next();
                /** Only non-null transition label has to be tested (that is, visible
                 * syntax diagram box, not simple line)
                 */
                if(link.containsPoint(new Point(x, y)) && link.transition.label != null)
                    return link;
            }
        }
        return null;
    }

    public GNode findNodeForStateNumber(int stateNumber) {
        for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
            GNode node = (GNode) iterator.next();
            if(node.state.containsStateNumber(stateNumber)) {
                return node;
            }
        }
        return null;
    }

    public boolean containsAtLeastOneState(List states) {
        for (Iterator nodeIterator = nodes.iterator(); nodeIterator.hasNext();) {
            GNode node = (GNode) nodeIterator.next();
            for (Iterator stateIterator = states.iterator(); stateIterator.hasNext();) {
                NFAState state = (NFAState) stateIterator.next();
                if(state.stateNumber == node.state.stateNumber)
                    return true;
            }
        }
        return false;
    }
}
