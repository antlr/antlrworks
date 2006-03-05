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

import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GPath extends GObject {

    public static int MIN_PATH_BLINK_WIDTH = 2;
    public static int MAX_PATH_BLINK_WIDTH = 4;
    
    protected List elements;
    protected boolean disabled;
    protected boolean visible;

    protected int currentIndex = -1;
    protected float step = 0.2f;
    protected float currentLineWidth = 1;
    protected boolean showRuleLinks = true;

    public GPath() {

    }

    public GPath(List elements, boolean disabled) {
        this.elements = elements;
        this.disabled = disabled;
    }

    public void setContext(GContext context) {
        super.setContext(context);
        for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
            GPathElement element = (GPathElement) iterator.next();
            element.setContext(context);
        }
    }

    public void setVisible(boolean flag) {
        this.visible = flag;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isEnabled() {
        return !disabled;
    }
    
    public void setShowRuleLinks(boolean flag) {
        this.showRuleLinks = flag;
    }

    public int getNumberOfVisibleElements() {
        int count = 0;
        for(int i=0; i<elements.size(); i++) {
            GPathElement element = (GPathElement)elements.get(i);
            if(element.isVisible())
                count++;
        }
        return count;
    }

    public void draw(float width, List ignoreElements) {
        if(showRuleLinks)
            drawElements(width, ignoreElements, true);
        drawElements(width, ignoreElements, false);
    }

    public void drawElements(float width, List ignoreElements, boolean ruleLink) {
        context.nodeColor = disabled?Color.red:Color.green;
        context.linkColor = context.nodeColor;
        context.setLineWidth(width);

        for(int i=0; i<elements.size(); i++) {
            GPathElement element = (GPathElement)elements.get(i);
            if(ignoreElements != null && ignoreElements.contains(element))
                continue;

            if(!element.isVisible())
                continue;

            if(element.isRuleLink && !ruleLink || ruleLink && !element.isRuleLink)
                continue;

            element.draw();
        }
    }

    public void drawSelectedElement() {
        if(currentIndex == -1)
            return;

        context.nodeColor = disabled?Color.red:Color.green;
        context.linkColor = context.nodeColor;
        context.setLineWidth(currentLineWidth);
        GPathElement element = (GPathElement)elements.get(currentIndex);
        element.draw();
    }

    public Rectangle getBoundsOfSelectedElement() {
        if(currentIndex == -1)
            return null;

        GPathElement element = (GPathElement)elements.get(currentIndex);
        return element.getBounds();
    }

    public boolean containsPoint(Point p) {
        for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
            GPathElement element = (GPathElement) iterator.next();
            if(element.containsPoint(p))
                return true;
        }
        return false;
    }

    public Set getObjects() {
        Set objects = new HashSet();
        for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
            GPathElement element = (GPathElement) iterator.next();
            objects.addAll(element.getObjects());
        }
        return objects;
    }

    public boolean isCurrentElementVisible() {
        GPathElement element = (GPathElement)elements.get(currentIndex);
        if(element.isRuleLink)
            return showRuleLinks;
        else
            return element.isVisible();
    }

    public void incrementWidth() {
        currentLineWidth += step;
        if(currentLineWidth >= MAX_PATH_BLINK_WIDTH || currentLineWidth <= MIN_PATH_BLINK_WIDTH)
            step = -step;
    }

    public void selectElement() {
        if(currentIndex == -1)
            nextElement();
    }

    public void deselectElement() {
        currentIndex = -1;
    }

    public void nextElement() {
        // looping prevents the while loop from looping indefinitely
        // in case no visible element exists
        int looping = currentIndex;
        do {
            currentIndex++;
            if(currentIndex >= elements.size())
                currentIndex = 0;
            if(looping == -1)
                looping = 0;
            else if(looping == currentIndex)
                break;
        } while(!isCurrentElementVisible());
                
        currentLineWidth = 3;
        context.repaint();
    }

    public void previousElement() {
        // looping prevents the while loop from looping indefinitely
        // in case no visible element exists
        int looping = currentIndex;
        do {
            currentIndex--;
            if(currentIndex<0)
                currentIndex = elements.size()-1;
            if(looping == -1)
                looping = 0;
            else if(looping == currentIndex)
                break;
        } while(!isCurrentElementVisible());

        currentLineWidth = 3;
        context.repaint();
    }

    public void firstElement() {
        currentIndex = -1;
        nextElement();
    }

    public void lastElement() {
        currentIndex = elements.size();
        previousElement();
    }

}
