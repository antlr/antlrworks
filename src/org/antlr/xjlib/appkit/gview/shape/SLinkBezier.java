package org.antlr.xjlib.appkit.gview.shape;

import org.antlr.xjlib.appkit.gview.base.Rect;
import org.antlr.xjlib.appkit.gview.base.Vector2D;
import org.antlr.xjlib.foundation.XJXMLSerializable;

import java.awt.*;
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

public class SLinkBezier extends SLinkArc implements XJXMLSerializable {

    protected Vector2D controlPointsAbs[];
    protected Vector2D labelPositionAbs;

    protected Vector2D controlPointsRel[];
    protected Vector2D labelPositionRel;

    protected Vector2D oldStart, oldEnd;
    protected Vector2D originalEndPointOffset;
    protected double originalZLength;

    protected double x0 = 0;
    protected double y0 = 0;
    protected double x1 = 0;
    protected double y1 = 0;

    protected static final int MODE_END = 1;
    protected static final int MODE_NOSTRETCH = 0;
    protected static final int MODE_STRETCH = -1;

    public void setControlPoints(Vector2D points[]) {
        this.controlPointsAbs = points;
        resetFrame();
        for(int i=0; i<points.length; i++) {
            updateFrame(points[i]);
        }
    }

    public void setLabelPosition(Vector2D position) {
        this.labelPositionAbs = position;
    }

    public Rect getFrame() {
        return new Rect(x0, y0, x1-x0, y1-y0);
    }

    public boolean contains(double x, double y) {
        return false;
    }

    public Vector2D absToRel(Vector2D p) {
        if(selfLoop) {
            return p.sub(start);
        } else {
            Vector2D z = end.sub(start);
            Vector2D a = p.sub(start);

            double lb = a.dot(z)/z.length();
            Vector2D b = z.normalize().setLength(lb);

            Vector2D c = a.sub(b);
            double lc = c.length()*(a.crossSign(z));

            return new Vector2D(lb, lc);
        }
    }

    public Vector2D relToAbs(Vector2D p, int mode) {
        if(selfLoop) {
            return start.add(p);
        } else {
            Vector2D nz = end.sub(start);
            double f = nz.length()/originalZLength;
            double lb = p.getX();
            double lc = p.getY();
            if(mode == MODE_END) {
                // End-point is always at the same relative position
                return end.add(originalEndPointOffset);
            } else {
                if(mode == MODE_NOSTRETCH)
                    f = 1;
                Vector2D nb = nz.normalize().setLength(f*lb);
                Vector2D nc = nz.copy().rotate(-90).normalize().setLength(lc);
                return start.add(nb.add(nc));
            }
        }
    }

    public void update() {
        int max = controlPointsAbs.length;
        if(controlPointsRel == null) {
            controlPointsRel = new Vector2D[controlPointsAbs.length];
            for(int i=0; i<max; i++) {
                controlPointsRel[i] = absToRel(controlPointsAbs[i]);
            }
            if(labelPositionAbs != null)
                labelPositionRel = absToRel(labelPositionAbs);

            originalEndPointOffset = controlPointsAbs[max-1].sub(end);
            originalZLength = end.sub(start).length();
        } else if(!oldStart.equals(start) || !oldEnd.equals(end)) {
            resetFrame();
            for(int i=0; i<max; i++) {
                controlPointsAbs[i] = relToAbs(controlPointsRel[i], i == 0 ? MODE_NOSTRETCH: (i==max-1?MODE_END:MODE_STRETCH));
                updateFrame(controlPointsAbs[i]);
            }
            if(labelPositionRel != null)
                labelPositionAbs = relToAbs(labelPositionRel, MODE_STRETCH);
        }
        oldStart = start;
        oldEnd = end;
    }

    public void draw(Graphics2D g) {
        drawShape(g);
    }

    public void drawShape(Graphics2D g) {
        if(controlPointsAbs != null) {
            bspline(g);
            //lines(g);
        }

        if(labelPositionAbs != null) {
            label.setPosition(labelPositionAbs.getX(), labelPositionAbs.getY());
            label.draw(g);
        }
    }

    /** Debug method used to print only the control points
     *
     */

    public void lines(Graphics g) {

        g.setColor(color);

        for (int i = 0; i < controlPointsAbs.length; i++) {
            Vector2D pt = controlPointsAbs[i];
            double x = pt.x;
            double y = pt.y;
            g.fillRect((int)(x-2), (int)(y-2), 4, 4);
        }
    }

    /** This method comes from:
     *
    Code used from BSpline.java (c) Leen Ammeraal with its authorization (see e-mail below)
    http://home.wxs.nl/~ammeraal/grjava.html

     Hello Jean,
Thank you for your interest in my program Bspline.java. Yes, you can use it in your software,
preferably giving me credit in the about box, as you suggest. If this cannot be done, I trust you
will refer to my book in other ways, as you think appropriate.
Best wishes,
Leen Ammeraal        
    */

    public void bspline(Graphics g) {

        g.setColor(color);

        int n = controlPointsAbs.length;

        double xA, yA, xB, yB, xC, yC, xD, yD,
                a0, a1, a2, a3, b0, b1, b2, b3, x = 0, y = 0, x0, y0;
        // this chooses how many point to go trough the curve, smaller number, more points.
        double time_delta = 0.1;
        boolean first = true;
        double fx = 0, fy = 0;

        for (int i = 1; i < n - 2; i++) {  // Loop Through Control Points

            Vector2D p0 = controlPointsAbs[i - 1];
            Vector2D p1 = controlPointsAbs[i];
            Vector2D p2 = controlPointsAbs[i + 1];
            Vector2D p3 = controlPointsAbs[i + 2];

            xA = p0.x;
            xB = p1.x;
            xC = p2.x;
            xD = p3.x;

            yA = p0.y;
            yB = p1.y;
            yC = p2.y;
            yD = p3.y;

            a3 = (-xA + 3 * (xB - xC) + xD) / 6;
            b3 = (-yA + 3 * (yB - yC) + yD) / 6;
            a2 = (xA - 2 * xB + xC) / 2;
            b2 = (yA - 2 * yB + yC) / 2;
            a1 = (xC - xA) / 2;
            b1 = (yC - yA) / 2;
            a0 = (xA + 4 * xB + xC) / 6;
            b0 = (yA + 4 * yB + yC) / 6;


            for (double t = 0; t <=1.0; t += time_delta) {
                x0 = x;
                y0 = y;

                double x1 = ((a3 * t + a2) * t + a1) * t + a0;
                double y1 = ((b3 * t + b2) * t + b1) * t + b0;

                x = (int) Math.round(x1);
                y = (int) Math.round(y1);

                if (first) {
                    first = false;
                    fx = x;
                    fy = y;
                } else {
                    g.drawLine((int) x0, (int) y0, (int) x, (int) y);
                }

                //g.setColor(Color.red);
                //g.fillRect((int)(x-2), (int)(y-2), 4, 4);
            }
        }

        // Perimeter of the source to the first point of the bezier curve
        Vector2D p0 = controlPointsAbs[0];
        g.drawLine((int) p0.getX(), (int) p0.getY(), (int) fx, (int) fy);

        // Last point of the bezier curve to the perimeter of the target
        Vector2D p1 = controlPointsAbs[n-1];
        g.drawLine((int) x, (int) y, (int) p1.getX(), (int) p1.getY());

        arrow.setAnchor(p1.getX(), p1.getY());
        arrow.setDirection(new Vector2D(x-p1.getX(), y-p1.getY()));
        arrow.draw(g);
    }

    private void resetFrame() {
        if(start != null && end != null) {
            x0 = Math.min(start.x, end.x);
            y0 = Math.min(start.y, end.y);
            x1 = Math.max(start.x, end.x);
            y1 = Math.max(start.y, end.y);
        } else {
            x0 = Integer.MAX_VALUE;
            y0 = Integer.MAX_VALUE;
            x1 = Integer.MIN_VALUE;
            y1 = Integer.MIN_VALUE;
        }
    }

    private void updateFrame(Vector2D point) {
        updateFrame(point.x, point.y);
    }

    private void updateFrame(double x, double y) {
        x0 = Math.min(x0, x);
        y0 = Math.min(y0, y);
        x1 = Math.max(x1, x);
        y1 = Math.max(y1, y);
    }
}
