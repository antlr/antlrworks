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

package org.antlr.works.visualization.graphics.path;

import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.GObject;
import org.antlr.works.visualization.graphics.shape.GLink;
import org.antlr.works.visualization.graphics.shape.GNode;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

public class GPathElement extends GObject {

    public GObject nodeOrLink;
    public GObject source;
    public GNode target;

    public boolean isRuleLink = false;

    public static GPathElement createLink(GNode node, GNode nextNode) {
        GPathElement element = new GPathElement(node, nextNode);
        element.setRuleLink(true);
        return element;
    }

    public static GPathElement createElement(GObject object) {
        return new GPathElement(object);
    }

    public GPathElement(GObject object) {
        this.nodeOrLink = object;
    }

    public GPathElement(GObject source, GNode target) {
        this.source = source;
        this.target = target;
    }

    public void setContext(GContext context) {
        super.setContext(context);
        if(nodeOrLink != null)
            nodeOrLink.setContext(context);
        if(source != null)
            source.setContext(context);
        if(target != null)
            target.setContext(context);
    }

    public boolean containsPoint(Point p) {
        if(nodeOrLink != null)
            return nodeOrLink.containsPoint(p);

        return false;
    }

    public void setRuleLink(boolean flag) {
        this.isRuleLink = flag;
    }

    public boolean isVisible() {
        if(nodeOrLink != null)
            return context.isObjectVisible(nodeOrLink);
        else
            return true;
    }

    public Set<GObject> getObjects() {
        Set<GObject> objects = new HashSet<GObject>();

        if(nodeOrLink != null)
            objects.add(nodeOrLink);

        if(source != null)
            objects.add(source);
        if(target != null)
            objects.add(target);

        return objects;
    }

    protected Point2D getBeginPoint() {
        float x0;
        float y0;
        if(source instanceof GNode) {
            GNode node = (GNode)source;
            x0 = node.getCenterX();
            y0 = node.getCenterY();
        } else {
            GLink link = (GLink)source;
            x0 = link.target.getBeginX();
            y0 = link.target.getBeginY();
        }
        return new Point2D.Float(x0, y0);
    }

    public Rectangle getBounds() {
        if(nodeOrLink != null) {
            return nodeOrLink.getBounds();
        } else {
            Point2D a = getBeginPoint();
            int x1 = (int)a.getX();
            int y1 = (int)a.getY();
            int x2 = (int)target.getCenterX();
            int y2 = (int)target.getCenterY();
            return new Rectangle(x1, y1, x2-x1, y2-y1);
        }
    }

    public void draw() {
        if(nodeOrLink != null)
            nodeOrLink.draw();

        if(source != null && target != null) {
            Point2D p = getBeginPoint();
            context.setColor(context.linkColor);
            context.drawSpline((float)p.getX(), (float)p.getY(), target.getCenterX(), target.getCenterY(),
                    0, context.getEndOffset(), 0, true);
        }
    }

}
