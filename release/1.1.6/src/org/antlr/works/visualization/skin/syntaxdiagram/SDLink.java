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

package org.antlr.works.visualization.skin.syntaxdiagram;

import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.shape.GLink;

import java.awt.*;

public class SDLink {

    public static boolean linkContainsPoint(GLink link, Point p) {
        GContext context = link.getContext();
        float ox = link.source.getX();
        float oy = link.source.getY();
        float width = link.source.linkDimension.getPixelWidth(context);

        return p.x>=ox && p.x<=ox+width &&
                p.y>=oy-context.getPixelBoxDown() && p.y<=oy+context.getPixelBoxDown()+context.getPixelBoxUp();
    }

    public static void draw(GLink link) {
        GContext context = link.getContext();

        float sx = link.source.getX();
        float sy = link.source.getY();

        float tx = link.target.getX();
        float ty = link.target.getY();

        float sloopBaseWidth = context.getPixelValue(GContext.EPSILON_WIDTH);

        context.setColor(context.linkColor);

        if(link.transition.isEpsilon()) {
            if(link.virtualPosition != null) {
                drawDownSloop(context, link, sx, sy, sx+sloopBaseWidth, link.getVirtualY());
                context.drawLine(sx+sloopBaseWidth, link.getVirtualY(), tx-sloopBaseWidth, link.getVirtualY());
                drawUpSloop(context, link, tx-sloopBaseWidth, link.getVirtualY(), tx, ty);
            } else if(sy > ty) {
                // Draw link upward
                if((tx-sx>sloopBaseWidth) && sloopBaseWidth>0) {
                    context.drawLine(sx, sy, tx-sloopBaseWidth, sy);
                    drawUpSloop(context, link, tx-sloopBaseWidth, sy, tx, ty);
                } else
                    drawUpSloop(context, link, sx, sy, tx, ty);
            } else if(ty > sy) {
                // Draw link downward
                drawDownSloop(context, link, sx, sy, tx, ty);
            } else {
                // Single horizontal link
                context.drawLine(sx, sy, tx, ty);

                // Draw an arrow if the link's target is the last node of the rule
                if(link.target.lastNodeOfRule)
                    context.drawRightArrow(tx, ty, context.getPixelArrowWidth(), context.getPixelArrowHeight());
            }

        } else {
            drawBox(context, link);
        }
    }

    public static void drawUpSloop(GContext context, GLink link, float x0, float y0, float x1, float y1) {
        float xm = x1;
        if(link.last) {
            float r = (x1-x0)/4;
            if(link.transition.loop) {
                context.drawLine(x0, y0, xm-r, y0);
                context.drawArc(xm-2*r, y0, 2*r, 2*r, 0, 90);
                context.drawLine(xm, y0+r, xm, y1);
            } else {
                context.drawLine(x0, y0, xm-r, y0);
                context.drawArc(xm-2*r, y0-2*r, 2*r, 2*r, 0, -90);
                context.drawLine(xm, y0-r, xm, y1);
            }
        } else {
            context.drawLine(x0, y0, xm, y0);
            context.drawLine(xm, y0, xm, y1);

            context.drawRightArrow(xm, y0, context.getPixelArrowWidth(), context.getPixelArrowHeight());
        }

        if(!link.transition.loop)
            context.drawUpArrow(xm, y1, context.getPixelArrowWidth(), context.getPixelArrowHeight());
    }

    public static void drawDownSloop(GContext context, GLink link, float x0, float y0, float x1, float y1) {
       // float xm = x0+(x1-x0)/2;
        float r = (x1-x0)/4;
        if(link.last) {
            if(link.transition.loop) {
                context.drawLine(x0, y0, x0, y1+r);
                context.drawArc(x0, y1, 2*r, 2*r, 90, 90);
                context.drawLine(x0+r, y1, x1, y1);
                context.drawDownArrow(x0, y0, context.getPixelArrowWidth(), context.getPixelArrowHeight());
            } else {
                //context.drawArc(xm-2*r, y0, 2*r, 2*r, 0, 90);
                //context.drawLine(xm, y0+r, xm, y1-r);
                context.drawLine(x0, y0, x0, y1-r);
                context.drawArc(x0, y1-2*r, 2*r, 2*r, -90, -90);
                context.drawLine(x0+r, y1, x1, y1);
            }
        } else {
            context.drawLine(x0, y0+r, x0, y1);
            context.drawLine(x0, y1, x1, y1);
        }
    }

    public static void drawBox(GContext context, GLink link) {
        float ox = link.source.getX();
        float oy = link.source.getY();
        float width = link.source.linkDimension.getPixelWidth(context);

        context.drawLine(ox, oy, ox+context.getPixelBoxEdge(), oy);
        context.drawRightArrow(ox+context.getPixelBoxEdge(), oy, context.getPixelArrowWidth(), context.getPixelArrowHeight());

        Font font;
        if(link.transition.externalRuleRef) {
            font = context.getRuleFont();
//            context.drawOval(ox+context.getPixelBoxEdge(), oy-context.getPixelBoxDown(),
//                        width-2*context.getPixelBoxEdge(), context.getPixelBoxDown()+context.getPixelBoxUp(), true);
            context.drawRoundRect(ox+context.getPixelBoxEdge(), oy-context.getPixelBoxDown(),
                        width-2*context.getPixelBoxEdge(), context.getPixelBoxDown()+context.getPixelBoxUp(),
                        8, 8, true);
        } else {
            font = context.getBoxFont();
            context.drawRect(ox+context.getPixelBoxEdge(), oy-context.getPixelBoxDown(),
                        width-2*context.getPixelBoxEdge(), context.getPixelBoxDown()+context.getPixelBoxUp(), true);
        }

        context.drawLine(ox+width-context.getPixelBoxEdge(), oy, ox+width, oy);

        context.setColor(context.getColorForLabel(link.transition.label));
        context.drawString(font, link.transition.label, ox+width/2, oy, GContext.ALIGN_CENTER);
    }

}
