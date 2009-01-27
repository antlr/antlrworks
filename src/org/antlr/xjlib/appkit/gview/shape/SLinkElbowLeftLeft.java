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

public class SLinkElbowLeftLeft {

    public static final int LEFT_LEFT = 0;
    public static final int RIGHT_RIGHT = 1;
    public static final int LABEL_OFFSET = 10;

    public SLinkElbow link = null;
    public Path2D path = null;

    public SLinkElbowLeftLeft(SLinkElbow link) {
        this.link = link;
    }

    public void updateLeftLeft() {
        this.path = link.path;
        if(Math.abs(link.end.y-link.start.y)>=GElementRect.DEFAULT_WIDTH*0.5+10) {
            buildVerticalPath(LEFT_LEFT);
        } else {
            // Objects are too close
            if(link.start.x<link.end.x)
                buildHorizontalRightBelowPath();
            else
                buildHorizontalRightAbovePath();
        }
    }

    public void updateRightRight() {
        this.path = link.path;
        if(Math.abs(link.end.y-link.start.y)>=GElementRect.DEFAULT_WIDTH*0.5+10) {
            buildVerticalPath(RIGHT_RIGHT);
        } else {
            // Objects are too close
            if(link.start.x<link.end.x)
                buildHorizontalRightAbovePath();
            else
                buildHorizontalRightBelowPath();
        }
    }

    public void buildVerticalPath(int direction) {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        double farest_x = 0;
        if(direction == LEFT_LEFT)
            farest_x = Math.min(start_.x,  end_.x);
        else
            farest_x = Math.max(start_.x, end_.x);

        Vector2D p1 = start_.add(new Vector2D(farest_x-start_.x, 0));
        Vector2D p2 = end_.add(new Vector2D(farest_x-end_.x, 0));

        path.add(link.start);
        path.add(start_);
        path.add(p1);
        path.add(p2);
        path.add(end_);
        path.add(link.end);

        link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(-LABEL_OFFSET, 0));
    }

    public void buildHorizontalRightBelowPath() {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        Vector2D start = link.start;
        Vector2D end = link.end;

        double farest_y = start_.y+GElementRect.DEFAULT_WIDTH*0.5+10;

        // Extend the link to the RIGHT

        Vector2D p1 = start_.add(new Vector2D(0, farest_y-start_.y));
        Vector2D p2 = end_.add(new Vector2D(0, farest_y-end_.y));

        path.add(start);
        path.add(start_);
        path.add(p1);
        path.add(p2);
        path.add(end_);
        path.add(end);

        link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(-LABEL_OFFSET, 0));
    }

    public void buildHorizontalRightAbovePath() {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        Vector2D start = link.start;
        Vector2D end = link.end;

        double farest_y = end_.y+GElementRect.DEFAULT_WIDTH*0.5+10;

        // Extend the link to the RIGHT

        Vector2D p1 = start_.add(new Vector2D(0, farest_y-start_.y));
        Vector2D p2 = end_.add(new Vector2D(0, farest_y-end_.y));

        path.add(start);
        path.add(start_);
        path.add(p1);
        path.add(p2);
        path.add(end_);
        path.add(end);

        link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(-LABEL_OFFSET, 0));
    }

}
