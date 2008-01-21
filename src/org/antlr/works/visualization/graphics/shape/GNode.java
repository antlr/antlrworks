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

package org.antlr.works.visualization.graphics.shape;

import org.antlr.works.visualization.fa.FAState;
import org.antlr.works.visualization.fa.FATransition;
import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.GObject;
import org.antlr.works.visualization.graphics.primitive.GDimension;
import org.antlr.works.visualization.graphics.primitive.GLiteral;
import org.antlr.works.visualization.graphics.primitive.GPoint;
import org.antlr.works.visualization.serializable.SEncoder;
import org.antlr.works.visualization.serializable.SSerializable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GNode extends GObject implements SSerializable {

    public FAState state;
    public List<GLink> links = new ArrayList<GLink>();

    public GPoint position;
    public GDimension nodeDimension = new GDimension(GContext.NODE_WIDTH, GContext.NODE_DOWN, GContext.NODE_UP);
    public GDimension linkDimension = new GDimension();
    public GDimension globalDimension;

    // This field is true if this node is the last one of the rule
    public boolean lastNodeOfRule = false;

    public GNode() {

    }

    public void setContext(GContext context) {
        super.setContext(context);
        for (GLink link : links) {
            link.setContext(context);
        }
    }

    public void setState(FAState state) {
        this.state = state;
    }

    public void setPosition(GPoint position) {
        this.position = new GPoint(position);
    }

    public boolean containsStateNumber(int n) {
        if(state.stateNumber == state.stateNumber)
            return true;

        // Look into each transition to see if the state number
        // has been skipped during NFA optimization: in this case,
        // any skipped state will be stored in the transition.
        for (GLink link : links) {
            if (link.containsStateNumber(n))
                return true;
        }
        return false;
    }

    public void addLink(GLink link) {
        link.setSource(this);
        links.add(link);
    }

    public GLink getLink(FATransition transition) {
        for (GLink link : links) {
            if (link.transition == transition)
                return link;
        }
        return null;
    }

    public float getX() {
        return position.getX(null);
    }

    public float getY() {
        return position.getY(null);
    }

    public float getCenterX() {
        return getX()+nodeDimension.getPixelWidth(null)/2;
    }

    public float getCenterY() {
        return getY();
    }

    public float getBeginX() {
        return getX();
    }

    public float getBeginY() {
        return getY();
    }

    public float getEndX() {
        return getX()+nodeDimension.getPixelWidth(null);
    }

    public float getEndY() {
        return getY();
    }

    public Rectangle getBounds() {
        float width = globalDimension.getPixelWidth(context);
        float up = globalDimension.getPixelUp(context);
        float down = globalDimension.getPixelDown(context);
        
        int x1 = (int) getX();
        int y1 = (int) (getY()-up);
        int x2 = (int) width;
        int y2 = (int) (up+down);
        return new Rectangle(x1, y1, x2-x1, y2-y1);
    }

    public boolean containsPoint(Point p) {
        return context.objectContainsPoint(this, p);
    }

    private void cacheGlobalDimension(GContext context) {
        globalDimension = new GDimension();
        globalDimension.addWidth(nodeDimension.width+linkDimension.width);
        globalDimension.addUp(GLiteral.max(nodeDimension.up, linkDimension.up));
        globalDimension.addDown(GLiteral.max(nodeDimension.down, linkDimension.down));
        globalDimension.cache(context);
    }

    public void render(float ox, float oy) {

        position.cache(context, ox, oy);
        nodeDimension.cache(context);
        linkDimension.cache(context);
        cacheGlobalDimension(context);

        for (GLink link : links) {
            link.render(ox, oy);
        }
    }

    public void draw() {
        context.drawNode(this);

        if(context.drawnode) {
            context.setColor(Color.red);
            context.fillRect(getX()-1, getY()-1, 3, 3);
        }
    }

    public void drawNodeAndLink() {

        for (GLink link : links) {
            link.draw();
        }

        draw();

        if(context.drawdimension) {
            context.setColor(Color.lightGray);
            float width = globalDimension.getPixelWidth(context);
            float up = globalDimension.getPixelUp(context);
            float down = globalDimension.getPixelDown(context);
            if(up+down>0)
                context.drawRect(getX(), getY()-up, width, up+down, false);
        }
    }

    public String toString() {
        return String.valueOf(state.stateNumber);
    }

    public void encode(SEncoder encoder) {
        encoder.write(state);
        for(GLink link : links) {
            encoder.write(link);
        }
        encoder.write(lastNodeOfRule);
    }
}
