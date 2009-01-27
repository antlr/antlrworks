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

import org.antlr.xjlib.appkit.gview.base.Path2D;
import org.antlr.xjlib.appkit.gview.base.Vector2D;
import org.antlr.xjlib.appkit.gview.object.GElementRect;

public class SLinkElbowBottomBottom {

    public static final int BOTTOM_BOTTOM = 0;
    public static final int TOP_TOP = 1;
    public static final int LABEL_OFFSET = 10;

    public SLinkElbow link = null;
    public Path2D path = null;

    public SLinkElbowBottomBottom(SLinkElbow link) {
        this.link = link;
    }

    public void updateBottomBottom() {
        this.path = link.path;
        if(Math.abs(link.end.x-link.start.x)>=GElementRect.DEFAULT_WIDTH*0.5+10) {
            buildHorizontalPath(BOTTOM_BOTTOM);
        } else {
            // Objects are too close
            if(link.start.y>=link.end.y)
                buildHorizontalRightBelowPath();
            else
                buildHorizontalRightAbovePath();
        }
    }

    public void updateTopTop() {
        this.path = link.path;
        if(Math.abs(link.end.x-link.start.x)>=GElementRect.DEFAULT_WIDTH*0.5+10) {
            buildHorizontalPath(TOP_TOP);
        } else {
            // Objects are too close
            if(link.start.y>=link.end.y)
                buildHorizontalRightAbovePath();
            else
                buildHorizontalRightBelowPath();
        }
    }

    public void buildHorizontalPath(int direction) {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        if(link.offsetToMouse != null) {
            double y = link.offsetToMouse.y+link.start.y;
            start_.y = end_.y = y;
        }

        double farest_y = 0;
        if(direction == BOTTOM_BOTTOM)
            farest_y = Math.max(start_.y,  end_.y);
        else
            farest_y = Math.min(start_.y, end_.y);

        Vector2D p1 = start_.add(new Vector2D(0, farest_y-start_.y));
        Vector2D p2 = end_.add(new Vector2D(0, farest_y-end_.y));

        path.add(link.start);
        path.add(start_);
        path.add(p1);
        path.add(p2);
        path.add(end_);
        path.add(link.end);

        if(direction == BOTTOM_BOTTOM)
            link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(0, LABEL_OFFSET));
        else
            link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(0, -LABEL_OFFSET));
    }

    public void buildHorizontalRightBelowPath() {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        Vector2D start = link.start;
        Vector2D end = link.end;

        double farest_x = start_.x+GElementRect.DEFAULT_WIDTH*0.5+10;

        // Extend the link to the RIGHT

        Vector2D p1 = start_.add(new Vector2D(farest_x-start_.x, 0));
        Vector2D p2 = end_.add(new Vector2D(farest_x-end_.x, 0));

        path.add(start);
        path.add(start_);
        path.add(p1);
        path.add(p2);
        path.add(end_);
        path.add(end);

        link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(LABEL_OFFSET, 0));
    }

    public void buildHorizontalRightAbovePath() {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        Vector2D start = link.start;
        Vector2D end = link.end;

        double farest_x = end_.x+GElementRect.DEFAULT_WIDTH*0.5+10;

        // Extend the link to the RIGHT

        Vector2D p1 = start_.add(new Vector2D(farest_x-start_.x, 0));
        Vector2D p2 = end_.add(new Vector2D(farest_x-end_.x, 0));

        path.add(start);
        path.add(start_);
        path.add(p1);
        path.add(p2);
        path.add(end_);
        path.add(end);

        link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(LABEL_OFFSET, 0));
    }

}
