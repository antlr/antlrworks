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

package org.antlr.works.visualization.skin.nfa;

import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.shape.GLink;

import java.awt.*;

public class NFALink {

    public static void draw(GLink link) {
        GContext context = link.getContext();
        
        float sx = link.source.getX()+context.getPixelNodeWidth()/2;
        float sy = link.source.getY();

        float tx = link.target.getX()+context.getPixelNodeWidth()/2;
        float ty = link.target.getY();

        float startOffset = context.getPixelNodeWidth()/2;
        float endOffset = startOffset;

        float sloopBaseWidth = context.getPixelValue(GContext.EPSILON_WIDTH);

        context.setColor(context.linkColor);

        if(link.virtualPosition != null) {
            context.drawArcConnector(sx+(tx-sx)/2, link.getVirtualY(), sx, sy, startOffset, endOffset,
                    sloopBaseWidth, 0.25f*sloopBaseWidth, link.transition.loop);
            context.drawArcConnector(sx+(tx-sx)/2, link.getVirtualY(), tx, ty, startOffset, endOffset,
                    sloopBaseWidth, 0.25f*sloopBaseWidth, !link.transition.loop);
        } else if(sy > ty) {
            // Draw link upward
            if((tx-sx>sloopBaseWidth+startOffset+0.25f*sloopBaseWidth) && sloopBaseWidth>0) {
                context.drawArcConnector(sx, sy, tx, ty, startOffset, endOffset,
                        sloopBaseWidth, 0.25f*sloopBaseWidth, true);
            } else {
                context.drawSpline(sx, sy, tx, ty, startOffset, endOffset, 0, true);
            }
        } else {
            context.drawSpline(sx, sy, tx, ty, startOffset, endOffset, 0, true);
        }

        if(!link.transition.isEpsilon()) {
            Font font;
            if(link.transition.externalRuleRef)
                font = context.getRuleFont();
            else
                font = context.getBoxFont();

            context.setColor(context.getColorForLabel(link.transition.label));            
            context.drawString(font, link.transition.label, sx+(tx-sx)/2, sy-2, GContext.ALIGN_CENTER_UP);
        }
    }

}
