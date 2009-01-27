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

package org.antlr.xjlib.appkit.gview.shape;

import org.antlr.xjlib.appkit.gview.base.Anchor2D;
import org.antlr.xjlib.appkit.gview.base.Path2D;
import org.antlr.xjlib.appkit.gview.base.Vector2D;
import org.antlr.xjlib.foundation.XJXMLSerializable;

import java.awt.*;

public class SLinkElbow extends SLink implements XJXMLSerializable {

    protected SLinkElbowTopBottom topbottom = new SLinkElbowTopBottom(this);
    protected SLinkElbowLeftRight leftright = new SLinkElbowLeftRight(this);
    protected SLinkElbowBottomBottom bottombottom = new SLinkElbowBottomBottom(this);
    protected SLinkElbowLeftLeft leftleft = new SLinkElbowLeftLeft(this);
    protected SLinkElbowBottomLeft bottomleft = new SLinkElbowBottomLeft(this);
    protected SLinkElbowTopLeft topleft = new SLinkElbowTopLeft(this);
    protected SLinkElbowLeftTop lefttop = new SLinkElbowLeftTop(this);
    protected SLinkElbowLeftBottom leftbottom = new SLinkElbowLeftBottom(this);

    protected transient Path2D path = new Path2D();
    protected Vector2D offsetToMouse = null;

    protected int outOffsetLength = 15;

    public SLinkElbow() {
        super();
    }

    public void setOutOffsetLength(int length) {
        this.outOffsetLength = length;
    }

    public Vector2D getStartWithOffset() {
        return start.add(startDirection.vectorLength(outOffsetLength));
    }

    public Vector2D getEndWithOffset() {
        return end.add(endDirection.vectorLength(outOffsetLength));
    }

    public void setMousePosition(Vector2D position) {
        setOffsetToMouse(position.sub(start));
    }

    public void setOffsetToMouse(Vector2D offset) {
        this.offsetToMouse = offset;
    }

    public Vector2D getOffsetToMouse() {
        return offsetToMouse;
    }

    public boolean contains(double x, double y) {
        return path.contains(x, y);
    }

    public void update() {
        path.clear();

        if(startDirection == Anchor2D.DIRECTION_BOTTOM && endDirection == Anchor2D.DIRECTION_TOP)
            topbottom.updateBottomTop();
        else if(startDirection == Anchor2D.DIRECTION_TOP && endDirection == Anchor2D.DIRECTION_BOTTOM)
            topbottom.updateTopBottom();
        else if(startDirection == Anchor2D.DIRECTION_LEFT && endDirection == Anchor2D.DIRECTION_RIGHT)
            leftright.updateLeftRight();
        else if(startDirection == Anchor2D.DIRECTION_RIGHT && endDirection == Anchor2D.DIRECTION_LEFT)
            leftright.updateRightLeft();
        else if(startDirection == Anchor2D.DIRECTION_BOTTOM && endDirection == Anchor2D.DIRECTION_BOTTOM)
            bottombottom.updateBottomBottom();
        else if(startDirection == Anchor2D.DIRECTION_TOP && endDirection == Anchor2D.DIRECTION_TOP)
            bottombottom.updateTopTop();
        else if(startDirection == Anchor2D.DIRECTION_LEFT && endDirection == Anchor2D.DIRECTION_LEFT)
            leftleft.updateLeftLeft();
        else if(startDirection == Anchor2D.DIRECTION_RIGHT && endDirection == Anchor2D.DIRECTION_RIGHT)
            leftleft.updateRightRight();
        else if(startDirection == Anchor2D.DIRECTION_BOTTOM && endDirection == Anchor2D.DIRECTION_LEFT)
            bottomleft.updateBottomLeft();
        else if(startDirection == Anchor2D.DIRECTION_BOTTOM && endDirection == Anchor2D.DIRECTION_RIGHT)
            bottomleft.updateBottomRight();
        else if(startDirection == Anchor2D.DIRECTION_TOP && endDirection == Anchor2D.DIRECTION_LEFT)
            topleft.updateTopLeft();
        else if(startDirection == Anchor2D.DIRECTION_TOP && endDirection == Anchor2D.DIRECTION_RIGHT)
            topleft.updateTopRight();
        else if(startDirection == Anchor2D.DIRECTION_LEFT && endDirection == Anchor2D.DIRECTION_TOP)
            lefttop.updateLeftTop();
        else if(startDirection == Anchor2D.DIRECTION_RIGHT && endDirection == Anchor2D.DIRECTION_TOP)
            lefttop.updateRightTop();
        else if(startDirection == Anchor2D.DIRECTION_LEFT && endDirection == Anchor2D.DIRECTION_BOTTOM)
            leftbottom.updateLeftBottom();
        else if(startDirection == Anchor2D.DIRECTION_RIGHT && endDirection == Anchor2D.DIRECTION_BOTTOM)
            leftbottom.updateRightBottom();
        else
            topleft.updateTopLeft();
    }

    public void draw(Graphics2D g) {
        if(path == null || arrow == null || label == null)
            return;

        g.setColor(color);

        drawShape(g);
        label.draw(g);
    }

    public void drawShape(Graphics2D g) {
        if(path == null || arrow == null || label == null)
            return;

        path.draw(g);

        arrow.setAnchor(end);
        arrow.setDirection(path.getEndDirection());
        arrow.draw(g);
    }

}
