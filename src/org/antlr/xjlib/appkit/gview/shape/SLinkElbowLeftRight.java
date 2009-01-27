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

public class SLinkElbowLeftRight {

    public static final int LEFT_RIGHT = 0;
    public static final int RIGHT_LEFT = 1;
    public static final int LABEL_OFFSET = 10;

    public SLinkElbow link = null;
    public Path2D path = null;

    public SLinkElbowLeftRight(SLinkElbow link) {
        this.link = link;    
    }

    public void updateRightLeft() {
        this.path = link.path;
        if(link.getStartWithOffset().getX()<=link.getEndWithOffset().getX()) {
            buildHorizontalPath();
        } else {
            if(Math.abs(link.end.y-link.start.y)>=GElementRect.DEFAULT_WIDTH+10) {
                buildVerticalPath();
            } else {
                buildVerticalBottomPath(RIGHT_LEFT);
            }
        }
    }

    public void updateLeftRight() {
        this.path = link.path;
        if(link.getStartWithOffset().getX()>link.getEndWithOffset().getX()) {
            buildHorizontalPath();
        } else {
            if(Math.abs(link.end.y-link.start.y)>=GElementRect.DEFAULT_WIDTH+10) {
                buildVerticalPath();
            } else {
                buildVerticalBottomPath(LEFT_RIGHT);
            }
        }
    }


    public void buildHorizontalPath() {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        Vector2D ab = end_.sub(start_);
        Vector2D p1 = start_.add(new Vector2D(ab.getX()*0.5, 0));
        Vector2D p2 = p1.add(new Vector2D(0, ab.getY()));
        Vector2D p3 = p2.add(new Vector2D(ab.getX()*0.5, 0));

        path.clear();
        if(ab.getY()==0) {
            path.add(link.start);
            path.add(link.end);
        } else {
            path.add(link.start);
            path.add(p1);
            path.add(p2);
            path.add(p3);
            path.add(link.end);
        }

        link.label.setPosition(p2.copy().shift(-LABEL_OFFSET, -LABEL_OFFSET));
    }

    public void buildVerticalPath() {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        Vector2D ab = end_.sub(start_);
        Vector2D p1 = start_.add(new Vector2D(0, ab.getY()*0.5));
        Vector2D p2 = p1.add(new Vector2D(ab.getX(), 0));
        Vector2D p3 = p2.add(new Vector2D(0, ab.getY()*0.5));

        path.clear();
        path.add(link.start);
        if(ab.getX()!=0) {
            path.add(start_);
            path.add(p1);
            path.add(p2);
            path.add(p3);
            path.add(end_);
        }
        path.add(link.end);

        link.label.setPosition(p2.copy().shift(-LABEL_OFFSET, -LABEL_OFFSET));
    }

    public void buildVerticalBottomPath(int direction) {
        Vector2D start_ = link.getStartWithOffset();
        Vector2D end_ = link.getEndWithOffset();

        Vector2D start = link.start;
        Vector2D end = link.end;

        double farest_y = Math.max(start_.y, end_.y)+40;

        if(direction == LEFT_RIGHT) {
            if(start_.x > end.x-GElementRect.DEFAULT_HEIGHT-link.outOffsetLength) {

                // Extend start out offset only if the end box is on the RIGHT
                double end_box_bottom_edge = end.y+GElementRect.DEFAULT_WIDTH*0.5;
                if(start_.y<=end_box_bottom_edge+5)
                    start_.x = end.x-GElementRect.DEFAULT_HEIGHT-link.outOffsetLength;
            }

            if(end_.x < start.x+GElementRect.DEFAULT_HEIGHT+link.outOffsetLength) {

                // Extend end out offset only if the start box is on the RIGHT
                double start_box_bottom_edge = start.y+GElementRect.DEFAULT_WIDTH*0.5;
                if(end_.y<=start_box_bottom_edge+5)
                    end_.x = start.x+GElementRect.DEFAULT_HEIGHT+link.outOffsetLength;
            }
        } else {
            if(start_.x < end.x+GElementRect.DEFAULT_HEIGHT+link.outOffsetLength) {

                // Extend start out offset only if the end box is on the RIGHT
                double end_box_bottom_edge = end.y+GElementRect.DEFAULT_WIDTH*0.5;
                if(start_.y<=end_box_bottom_edge+5)
                    start_.x = end.x+GElementRect.DEFAULT_HEIGHT+link.outOffsetLength;
            }

            if(end_.x > start.x-GElementRect.DEFAULT_HEIGHT-link.outOffsetLength) {

                // Extend end out offset only if the start box is on the RIGHT
                double start_box_bottom_edge = start.y+GElementRect.DEFAULT_WIDTH*0.5;
                if(end_.y<=start_box_bottom_edge+5)
                    end_.x = start.x-GElementRect.DEFAULT_HEIGHT-link.outOffsetLength;
            }
        }

        // Extend the link to the RIGHT

        Vector2D p1 = start_.add(new Vector2D(0, farest_y-start.y));
        Vector2D p2 = end_.add(new Vector2D(0, farest_y-end.y));

        path.clear();
        path.add(start);
        path.add(start_);
        path.add(p1);
        path.add(p2);
        path.add(end_);
        path.add(end);

        Vector2D labelv = start_.sub(p2);
        labelv.stretch(0.5);
        link.label.setPosition(p2.add(labelv).shift(0, LABEL_OFFSET));
    }
}
