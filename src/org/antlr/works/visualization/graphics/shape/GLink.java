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

import org.antlr.works.visualization.fa.FATransition;
import org.antlr.works.visualization.graphics.GObject;
import org.antlr.works.visualization.graphics.primitive.GDimension;
import org.antlr.works.visualization.graphics.primitive.GPoint;
import org.antlr.works.visualization.serializable.SEncoder;
import org.antlr.works.visualization.serializable.SSerializable;

import java.awt.*;

public class GLink extends GObject implements SSerializable {

    public FATransition transition;
    public GNode source;
    public GNode target;

    // Global dimension of a branch in an alternative
    public GDimension branchDim;

    // Position for a single-link when both source and target state are on the same y-axis value
    public GPoint virtualPosition;

    // This flag indicates that this link is the last transition and should
    // be displayed differently
    public boolean last = false;

    public GLink() {

    }
    
    public GLink(FATransition transition, GNode target) {
        this.transition = transition;
        this.target = target;
    }

    public void setSource(GNode source) {
        this.source = source;
    }

    public void setVirtualPosition(GPoint position) {
        this.virtualPosition = new GPoint(position);
    }

    public void setBranchDimension(GDimension dimension) {
        this.branchDim = new GDimension(dimension);
    }

    public void setLast(boolean flag) {
        this.last = flag;
    }

    public float getVirtualY() {
        return virtualPosition.getY(null);
    }

    public boolean containsStateNumber(int n) {
        return transition.containsStateNumber(n);
    }

    public boolean containsPoint(Point p) {
        return context.objectContainsPoint(this, p);
    }

    public void render(float ox, float oy) {
        if(branchDim != null)
            branchDim.cache(context);
        if(virtualPosition != null)
            virtualPosition.cache(context, ox, oy);
    }

    public Rectangle getBounds() {
        int x1 = (int) source.getBeginX();
        int y1 = (int) source.getBeginY();
        int x2 = (int) target.getEndX();
        int y2 = (int) target.getEndY();
        return new Rectangle(x1, y1, Math.max(1, x2-x1), Math.max(1, y2-y1));
    }

    public void draw() {
        context.drawLink(this);
    }

    public void encode(SEncoder encoder) {
        encoder.write(transition);
        encoder.write(source);
        encoder.write(target);
        encoder.write(last);
    }

}
