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
import org.antlr.xjlib.appkit.gview.base.Rect;
import org.antlr.xjlib.appkit.gview.base.Vector2D;
import org.antlr.xjlib.foundation.XJXMLSerializable;

import java.awt.*;

public abstract class SLink implements XJXMLSerializable {

    protected Vector2D start;
    protected Vector2D end;

    protected Vector2D startDirection;
    protected Vector2D endDirection;

    protected Vector2D startOffset;
    protected Vector2D endOffset;

    protected double startTangentOffset;
    protected double endTangentOffset;

    protected Vector2D direction;
    protected double flateness;

    protected SArrow arrow = new SArrow();
    protected SLabel label = new SLabel();

    protected boolean selfLoop = false;
    protected boolean arrowVisible = true;

    protected Color color = Color.black;

    // Temporary

    protected transient Vector2D startWithOffset = null;
    protected transient Vector2D endWithOffset = null;

    public SLink() {
    }

    public void setStartAnchor(Anchor2D anchor) {
        setStart(anchor.position);
        setStartDirection(anchor.direction);
    }

    public void setEndAnchor(Anchor2D anchor) {
        setEnd(anchor.position);
        setEndDirection(anchor.direction);
    }

    public void setStart(Vector2D start) {
        this.start = start.copy();
        computeOffsets();
    }

    public void setStart(double x, double y) {
        setStart(new Vector2D(x, y));
    }

    public Vector2D getStart() {
        return start;
    }

    public void setEnd(Vector2D end) {
        this.end = end.copy();
        computeOffsets();
    }

    public void setEnd(double x, double y) {
        setEnd(new Vector2D(x, y));
    }

    public void setEnd(Point p) {
        end = Vector2D.vector(p);
    }

    public Vector2D getEnd() {
        return end;
    }

    public void setDirection(Vector2D direction) {
        this.direction = direction;
    }

    public Vector2D getDirection() {
        return direction;
    }

    public void setFlateness(double flatness) {
        this.flateness = flatness;
    }

    public double getFlateness() {
        return flateness;
    }

    public void setStartDirection(Vector2D direction) {
        this.startDirection = direction;
    }

    public Vector2D getStartDirection() {
        return startDirection;
    }

    public void setEndDirection(Vector2D direction) {
        this.endDirection = direction;
    }

    public Vector2D getEndDirection() {
        return endDirection;
    }

    public void setStartTangentOffset(double offset) {
        this.startTangentOffset = offset;
    }

    public double getStartTangentOffset() {
        return startTangentOffset;
    }

    public void setEndTangentOffset(double offset) {
        this.endTangentOffset = offset;
    }

    public double getEndTangentOffset() {
        return endTangentOffset;
    }

    public void setStartOffset(Vector2D offset) {
        this.startOffset = offset;
        computeOffsets();
    }

    public Vector2D getStartOffset() {
        return startOffset;
    }

    public void setEndOffset(Vector2D offset) {
        this.endOffset = offset;
        computeOffsets();
    }

    public Vector2D getEndOffset() {
        return endOffset;
    }

    public void computeOffsets() {
        startWithOffset = start;
        endWithOffset = end;

        if(start != null && startOffset != null)
            startWithOffset = start.add(startOffset);
        if(end != null && endOffset != null)
            endWithOffset = end.add(endOffset);
    }

    public Vector2D getStartWithOffset() {
        return startWithOffset;
    }

    public Vector2D getEndWithOffset() {
        return endWithOffset;
    }

    public void setArrow(SArrow arrow) {
        this.arrow = arrow;
    }

    public SArrow getArrow() {
        return arrow;
    }

    public void setArrowVisible(boolean flag) {
        this.arrowVisible = flag;
    }

    public void setLabel(String label) {
        this.label.setTitle(label);
    }

    public void setLabel(SLabel label) {
        this.label = label;
    }

    public SLabel getLabel() {
        return label;
    }

    public void setSelfLoop(boolean selfLoop) {
        this.selfLoop = selfLoop;
    }

    public boolean isSelfLoop() {
        return selfLoop;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setLabelColor(Color color) {
        label.setColor(color);
    }

    public void setLabelVisible(boolean flag) {
        label.setVisible(flag);
    }

    public boolean isLabelVisible() {
        return label.isVisible();
    }

    public Rect getFrame() {
        return new Rect(start, end);
    }

    public void setMousePosition(Vector2D position) {

    }

    public abstract boolean contains(double x, double y);

    public abstract void update();
    public abstract void draw(Graphics2D g);
    public abstract void drawShape(Graphics2D g);

}
