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

public class SLinkElbowTopBottom {

    public static final int TOP_BOTTOM = 0;
    public static final int BOTTOM_TOP = 1;
    public static final int LABEL_OFFSET = 10;

    public SLinkElbow link = null;
    public Path2D path = null;

    public SLinkElbowTopBottom(SLinkElbow link) {
        this.link = link;
    }

    public void updateTopBottom() {
        this.path = link.path;
        if(link.getStartWithOffset().getY()>link.getEndWithOffset().getY()) {
            buildVerticalPath();
        } else {
            if(Math.abs(link.end.x-link.start.x)>=GElementRect.DEFAULT_WIDTH+10) {
                buildHorizontalPath();
            } else {
                buildHorizontalRightPath(TOP_BOTTOM);
            }
        }
    }

    public void updateBottomTop() {
        this.path = link.path;
        if(link.getStartWithOffset().getY()<=link.getEndWithOffset().getY()) {
            buildVerticalPath();
        } else {
            if(Math.abs(link.end.x-link.start.x)>=GElementRect.DEFAULT_WIDTH+10) {
                buildHorizontalPath();
            } else {
                buildHorizontalRightPath(BOTTOM_TOP);
            }
        }
    }

    public void buildVerticalPath() {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        Vector2D ab = end_.sub(start_);
        Vector2D p1 = start_.add(new Vector2D(0, ab.getY()*0.5));
        Vector2D p2 = p1.add(new Vector2D(ab.getX(), 0));
        Vector2D p3 = p2.add(new Vector2D(0, ab.getY()*0.5));

        path.add(link.start);
        path.add(start_);
        path.add(p1);
        path.add(p2);
        path.add(p3);
        path.add(end_);
        path.add(link.end);

        link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(0, -LABEL_OFFSET));
    }

    public void buildHorizontalPath() {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        Vector2D ab = end_.sub(start_);
        Vector2D p1 = start_.add(new Vector2D(ab.getX()*0.5, 0));
        Vector2D p2 = p1.add(new Vector2D(0, ab.getY()));
        Vector2D p3 = p2.add(new Vector2D(ab.getX()*0.5, 0));

        path.add(link.start);
        path.add(start_);
        path.add(p1);
        path.add(p2);
        path.add(p3);
        path.add(end_);
        path.add(link.end);

        link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(LABEL_OFFSET, 0));
    }

    public void buildHorizontalRightPath(int direction) {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        Vector2D start = link.start;
        Vector2D end = link.end;

        double farest_x = Math.max(start_.x, end_.x)+40;


        if(direction == BOTTOM_TOP) {
            if(start_.y < end.y+GElementRect.DEFAULT_HEIGHT+link.outOffsetLength) {

                // Extend start out offset only if the end box is on the RIGHT
                double end_box_right_edge = end.x+GElementRect.DEFAULT_WIDTH*0.5;
                if(start_.x<=end_box_right_edge+5)
                    start_.y = end.y+GElementRect.DEFAULT_HEIGHT+link.outOffsetLength;
            }

            if(end_.y > start.y-GElementRect.DEFAULT_HEIGHT-link.outOffsetLength) {

                // Extend end out offset only if the start box is on the RIGHT
                double start_box_right_edge = start.x+GElementRect.DEFAULT_WIDTH*0.5;
                if(end_.x<=start_box_right_edge+5)
                    end_.y = start.y-GElementRect.DEFAULT_HEIGHT-link.outOffsetLength;
            }
        } else if(direction == TOP_BOTTOM) {
            if(start_.y > end.y-GElementRect.DEFAULT_HEIGHT-link.outOffsetLength) {

                // Extend start out offset only if the end box is on the RIGHT
                double end_box_right_edge = end.x+GElementRect.DEFAULT_WIDTH*0.5;
                if(start_.x<=end_box_right_edge+5)
                    start_.y = end.y-GElementRect.DEFAULT_HEIGHT-link.outOffsetLength;
            }

            if(end_.y < start.y+GElementRect.DEFAULT_HEIGHT+link.outOffsetLength) {

                // Extend end out offset only if the start box is on the RIGHT
                double start_box_right_edge = start.x+GElementRect.DEFAULT_WIDTH*0.5;
                if(end_.x<=start_box_right_edge+5)
                    end_.y = start.y+GElementRect.DEFAULT_HEIGHT+link.outOffsetLength;
            }
        }

        // Extend the link to the RIGHT

        Vector2D p1 = start_.add(new Vector2D(farest_x-start.x, 0));
        Vector2D p2 = end_.add(new Vector2D(farest_x-end.x, 0));

        path.add(start);
        path.add(start_);
        path.add(p1);
        path.add(p2);
        path.add(end_);
        path.add(end);

        link.label.setPosition(p1.add(p2.sub(p1).stretch(0.5)).shift(LABEL_OFFSET, 0));
    }

}
