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

package org.antlr.xjlib.appkit.gview.object;

import org.antlr.xjlib.appkit.gview.GView;
import org.antlr.xjlib.appkit.gview.base.Anchor2D;
import org.antlr.xjlib.appkit.gview.base.Rect;
import org.antlr.xjlib.appkit.gview.base.Vector2D;
import org.antlr.xjlib.foundation.XJXMLSerializable;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GElement implements XJXMLSerializable {

    public static final String ANCHOR_CENTER = "CENTER";
    public static final String ANCHOR_TOP = "TOP";
    public static final String ANCHOR_BOTTOM = "BOTTOM";
    public static final String ANCHOR_LEFT = "LEFT";
    public static final String ANCHOR_RIGHT = "RIGHT";

    protected transient GView view = null;
    protected List<GElement> elements = new ArrayList<GElement>();

    protected Vector2D position = new Vector2D();
    protected transient Vector2D oldPosition = null;

    protected transient Map<String,Anchor2D> anchors = new HashMap<String, Anchor2D>();

    protected String label = null;
    protected Color labelColor = Color.black;
    protected boolean labelVisible = true;

    protected transient boolean selected = false;
    protected transient boolean focused = false;

    protected transient Color color = Color.black;
    protected transient int penSize = 1;

    protected transient BasicStroke strokeSize = new BasicStroke(penSize);
    protected transient BasicStroke strokeNormal = new BasicStroke(1);
    protected transient BasicStroke strokeBold = new BasicStroke(3);

    protected boolean draggable = false;

    protected final Object lock = new Object();

    public GElement () {
    }

    public void setPanel(GView view) {
        this.view = view;
        synchronized(lock) {
            for (GElement element : elements) {
                element.setPanel(view);
            }
        }
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isLabelEqualsTo(String otherLabel) {
        if(label == null)
            return otherLabel == null;
        else
            return label.equals(otherLabel);
    }

    public void setLabelColor(Color color) {
        this.labelColor = color;
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public void setLabelVisible(boolean flag) {
        this.labelVisible = flag;
    }

    public boolean isLabelVisible() {
        return labelVisible;
    }

    public void setPosition(double x, double y) {
        // This is the position of the center of the element
        position.setX(x);
        position.setY(y);
        elementPositionDidChange();
    }

    public double getPositionX() {
        return position.getX();
    }

    public double getPositionY() {
        return position.getY();
    }

    public void setPosition(Vector2D position) {
        this.position = position;
        elementPositionDidChange();
    }

    public Vector2D getPosition() {
        return position;
    }

    public void setElements(List<GElement> elements) {
        this.elements = elements;
    }

    public List<GElement> getElements() {
        return elements;
    }

    public void addElement(GElement element) {
        element.setPanel(view);
        synchronized(lock) {
            elements.add(element);
        }
    }

    public void removeElement(GElement element) {
        synchronized(lock) {
            elements.remove(element);
        }
    }

    public GElement getFirstElement() {
        if(elements == null || elements.isEmpty())
            return null;
        else
            return elements.get(0);
    }

    public GElement getLastElement() {
        if(elements == null || elements.isEmpty())
            return null;
        else
            return elements.get(elements.size()-1);
    }

    public GElement findElementWithLabel(String label) {
        if(isLabelEqualsTo(label))
            return this;

        if(elements == null)
            return null;

        for (GElement element : elements) {
            if (element.isLabelEqualsTo(label))
                return element;
            else {
                element = element.findElementWithLabel(label);
                if (element != null)
                    return element;
            }
        }

        return null;
    }

    public void updateAnchors() {
    }

    public void setAnchor(String key, Vector2D position, Vector2D direction) {
        Anchor2D anchor = getAnchor(key);
        if(anchor == null) {
            anchor = new Anchor2D();
            anchors.put(key, anchor);
        }
        anchor.setPosition(position);
        anchor.setDirection(direction);
    }

    public double getDefaultAnchorOffset(String anchorKey) {
        return 0;
    }

    public Anchor2D getAnchor(String key) {
        return anchors.get(key);
    }

    public String getAnchorKeyClosestToPoint(Point p) {
        Anchor2D anchor = getAnchorClosestToPoint(p);
        for (String key : anchors.keySet()) {
            if (anchors.get(key) == anchor)
                return key;
        }
        return null;
    }

    public Anchor2D getAnchorClosestToPoint(Point p) {
        double smallest_distance = Integer.MAX_VALUE;
        Anchor2D closest_anchor = null;

        for (Anchor2D anchor : anchors.values()) {
            double dx = anchor.position.getX() - p.x;
            double dy = anchor.position.getY() - p.y;
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d < smallest_distance) {
                smallest_distance = d;
                closest_anchor = anchor;
            }
        }

        return closest_anchor;
    }

    public Rect bounds() {
        Rect r = getFrame();
        synchronized(lock) {
            for (GElement element : elements) {
                if (element == this)
                    continue;

                if (r == null)
                    r = element.bounds();
                else
                    r = r.union(element.bounds());
            }
        }
        return r;
    }

    public Rect getFrame() {
        return null;
    }

    public void setFocused(boolean flag) {
        focused = flag;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setSelected(boolean flag) {
        selected = flag;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean acceptIncomingLink() {
        return false;
    }

    public boolean acceptOutgoingLink() {
        return false;
    }

    public void setDraggable(boolean flag) {
        this.draggable = flag;
    }

    public boolean isDraggable() {
        return draggable;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }

    public void setPenSize(int size) {
        penSize = size;
        strokeSize = new BasicStroke(penSize);
    }

    public boolean isInside(Point p) {
        return false;
    }

    public void move(double dx, double dy) {
        position.shift(dx, dy);

        // Recursively move every other children objects
        synchronized(lock) {
            for (GElement element : elements) {
                element.move(dx, dy);
            }
        }

        elementPositionDidChange();
    }

    public void moveToPosition(Vector2D position) {
        double dx = position.x-getPosition().x;
        double dy = position.y-getPosition().y;
        move(dx, dy);
    }

    public GElement match(Point p) {
        synchronized(lock) {
            for (GElement element : elements) {
                GElement match = element.match(p);
                if (match != null)
                    return match;
            }
        }

        if(isInside(p))
            return this;
        else
            return null;
    }

    public void beginDrag() {
        oldPosition = null;
    }

    public Vector2D dragElementPosition(Vector2D p) {
        Vector2D ep = p.copy();
        if(oldPosition != null) {
            ep.x += p.x-oldPosition.x;
            ep.y += p.y-oldPosition.y;
        }
        return ep;
    }

    public void drag(Vector2D p) {
        double dx = 0;
        double dy = 0;

        if(oldPosition == null) {
            oldPosition = new Vector2D();
        }   else {
            dx = p.x-oldPosition.x;
            dy = p.y-oldPosition.y;
        }

        oldPosition.x = p.x;
        oldPosition.y = p.y;

        move(dx, dy);
    }

    public void drawRecursive(Graphics2D g) {
        synchronized(lock) {
            for (GElement element : elements) {
                element.drawRecursive(g);
            }
        }

        draw(g);
        if(isSelected())
            drawSelected(g);
        else if(isFocused())
            drawFocused(g);
    }

    public boolean isVisibleInClip(Graphics2D g) {
        Rectangle clip = g.getClipBounds();
        if(clip == null) return true;

        Rectangle r = getFrame().rectangle();
        // some margin to avoid clipped drawing
        r.width++;
        r.height++;

        return clip.intersects(r);
    }

    public void draw(Graphics2D g) {

    }

    public void drawShape(Graphics2D g) {

    }

    private void drawSelected(Graphics2D g) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, view.getSelectionAlphaValue()));
        g.setColor(Color.gray);
        g.setStroke(strokeBold);

        drawShape(g);

        g.setStroke(strokeNormal);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f ));
    }

    private void drawFocused(Graphics2D g) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, view.getFocusAlphaValue()));
        g.setColor(Color.blue);
        g.setStroke(strokeBold);

        drawShape(g);

        g.setStroke(strokeNormal);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f ));
    }

    // *** Notifications

    public void elementPositionDidChange() {
        updateAnchors();
    }

    public void elementDimensionDidChange() {
        updateAnchors();
    }

    /**
     * Method invoked when the element has been loaded from disk
     */
    public void elementDidLoad() {
        synchronized(lock) {
            for (GElement element : elements) {
                element.elementDidLoad();
            }
        }

        // Update the anchors
        updateAnchors();
    }

    public String toString() {
        return getClass().getName()+": "+position.x+"/"+position.y;
    }

}
