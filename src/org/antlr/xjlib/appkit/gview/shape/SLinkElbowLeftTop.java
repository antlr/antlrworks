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
import org.antlr.xjlib.appkit.gview.object.GElementRect;

public class SLinkElbowLeftTop {

    public static final int TOP_BOTTOM = 0;
    public static final int BOTTOM_TOP = 1;
    public static final int LABEL_OFFSET = 10;

    public SLinkElbow link = null;
    public Path2D path = null;

    public SLinkElbowLeftTop(SLinkElbow link) {
        this.link = link;
    }

    public void updateRightTop() {
        this.path = link.path;
        if(link.getEndWithOffset().y>link.start.y)
            buildVerticalPath();
        else if(link.start.x+link.outOffsetLength<link.end.x-GElementRect.DEFAULT_WIDTH+10)
            buildHorizontalPath();
        else
            buildHorizontalLeftPath(false);
    }

    public void updateLeftTop() {
        this.path = link.path;
        if(link.getEndWithOffset().y>link.start.y)
            buildVerticalPath();
        else if(link.start.x>link.end.x+GElementRect.DEFAULT_WIDTH+10)
            buildHorizontalPath();
        else
            buildHorizontalLeftPath(true);
    }

    public void buildVerticalPath() {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        if(link.start.x > link.end.x+link.outOffsetLength && link.startDirection == Anchor2D.DIRECTION_LEFT) {
            path.add(link.start);
            path.add(link.end.x, link.start.y);
            path.add(link.end);

            link.label.setPosition(link.end.x+(link.start.x-link.end.x)*0.5, link.start.y-LABEL_OFFSET);
        } else if(link.start.x < link.end.x-link.outOffsetLength && link.startDirection == Anchor2D.DIRECTION_RIGHT) {
            path.add(link.start);
            path.add(link.end.x, link.start.y);
            path.add(link.end);

            link.label.setPosition(link.end.x+(link.start.x-link.end.x)*0.5, link.start.y-LABEL_OFFSET);
        } else if(link.start.y+GElementRect.DEFAULT_HEIGHT*0.5>end_.y-10) {
            Vector2D p1 = new Vector2D(link.end.x, link.start.y-GElementRect.DEFAULT_HEIGHT*0.5-10);
            Vector2D p2 = new Vector2D(start_.x, p1.y);

            path.add(link.start);
            path.add(start_);
            path.add(p2);
            path.add(p1);
            path.add(link.end);

            link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(0, -LABEL_OFFSET));
        } else {
            Vector2D ab = start_.sub(link.end);
            Vector2D p1 = new Vector2D(link.end.x, link.end.y+ab.y*0.5);
            Vector2D p2 = new Vector2D(p1.x+ab.x, p1.y);
            Vector2D p3 = new Vector2D(p2.x, start_.y);

            path.add(link.start);
            path.add(start_);
            path.add(p3);
            path.add(p2);
            path.add(p1);
            path.add(link.end);

            link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(0, LABEL_OFFSET));
        }
    }

    public void buildHorizontalPath() {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        Vector2D ab = link.start.sub(end_);
        Vector2D p1 = new Vector2D(end_.x+ab.x*0.5, end_.y);
        Vector2D p2 = new Vector2D(p1.x, start_.y);

        path.add(link.start);
        path.add(start_);
        path.add(p2);
        path.add(p1);
        path.add(end_);
        path.add(link.end);

        link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(LABEL_OFFSET, 0));
    }

    public void buildHorizontalLeftPath(boolean left) {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        double farest_x;
        double farest_y = Math.min(end_.y, start_.y-GElementRect.DEFAULT_HEIGHT*0.5-10);

        if(left)
            farest_x = Math.min(end_.x-GElementRect.DEFAULT_WIDTH*0.5-10, start_.x);
        else
            farest_x = Math.max(end_.x+GElementRect.DEFAULT_WIDTH*0.5+10, start_.x);

        Vector2D p1 = new Vector2D(end_.x, farest_y);
        Vector2D p2 = new Vector2D(farest_x, p1.y);
        Vector2D p3 = new Vector2D(p2.x, start_.y);

        path.add(link.start);
        path.add(p3);
        path.add(p2);
        path.add(p1);
        path.add(end_);
        path.add(link.end);

        link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(0, -LABEL_OFFSET));
    }

}
