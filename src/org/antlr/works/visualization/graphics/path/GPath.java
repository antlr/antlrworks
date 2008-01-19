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
import java.util.List;
import java.util.Set;

public class GPath extends GObject {

    public static int MIN_PATH_BLINK_WIDTH = 2;
    public static int MAX_PATH_BLINK_WIDTH = 4;

    /** List of all elements composing the path */
    protected List<GPathElement> elements;

    /** A disable path will be displayed in red */
    protected boolean disabled = false;

    /** A visible path will be displayed */
    protected boolean visible = false;

    /** A selectable path can be selected and each
     * segment can be highlighted in turn using the
     * arrow (the current segment will blink)
     */
    protected boolean selectable = true;

    protected int currentIndex = -1;
    protected float step = 0.2f;
    protected float currentLineWidth = 1;
    protected boolean showRuleLinks = true;

    public GPath() {

    }

    public GPath(List<GPathElement> elements, boolean disabled) {
        this.elements = elements;
        this.disabled = disabled;
    }

    public void setContext(GContext context) {
        super.setContext(context);
        for (GPathElement element : elements) {
            element.setContext(context);
        }
    }

    public void setVisible(boolean flag) {
        this.visible = flag;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setSelectable(boolean flag) {
        this.selectable = flag;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public boolean isEnabled() {
        return !disabled;
    }
    
    public void setShowRuleLinks(boolean flag) {
        this.showRuleLinks = flag;
    }

    public int getNumberOfVisibleElements() {
        int count = 0;
        for (GPathElement element : elements) {
            if (element.isVisible())
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
        context.nodeColor = disabled?Color.red:Color.green.darker();
        context.linkColor = context.nodeColor;
        context.setLineWidth(width);

        for (GPathElement element : elements) {
            if (ignoreElements != null && ignoreElements.contains(element))
                continue;

            if (!element.isVisible())
                continue;

            if (element.isRuleLink && !ruleLink || ruleLink && !element.isRuleLink)
                continue;

            element.draw();
        }
    }

    public void drawSelectedElement() {
        if(currentIndex == -1)
            return;

        context.nodeColor = disabled?Color.red:Color.green.darker();
        context.linkColor = context.nodeColor;
        context.setLineWidth(currentLineWidth);
        GPathElement element = elements.get(currentIndex);
        element.draw();
    }

    public Rectangle getBoundsOfSelectedElement() {
        if(currentIndex == -1)
            return null;

        GPathElement element = elements.get(currentIndex);
        return element.getBounds();
    }

    public boolean containsPoint(Point p) {
        for (GPathElement element : elements) {
            if (element.containsPoint(p))
                return true;
        }
        return false;
    }

    public Set<GObject> getObjects() {
        Set<GObject> objects = new HashSet<GObject>();
        for (GPathElement element : elements) {
            objects.addAll(element.getObjects());
        }
        return objects;
    }

    public boolean isCurrentElementVisible() {
        GPathElement element = elements.get(currentIndex);
        if(element.isRuleLink)
            return showRuleLinks;
        else
            return element.isVisible();
    }

    public void setMaxWidth() {
        currentLineWidth = MAX_PATH_BLINK_WIDTH;
    }

    public void setMinWidth() {
        currentLineWidth = MIN_PATH_BLINK_WIDTH;
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
        if(elements.isEmpty()) {
            currentIndex = -1;
            return;
        }

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
        if(elements.isEmpty()) {
            currentIndex = -1;
            return;
        }

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
