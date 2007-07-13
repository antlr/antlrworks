package org.antlr.works.ate;

import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.HashMap;
import java.util.Map;
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

public abstract class ATEUnderlyingManager {

    protected ATEPanel textEditor;
    protected UnderlyingShape underlyingShape;
    protected boolean underlying = true;

    public ATEUnderlyingManager(ATEPanel textEditor) {
        this.textEditor = textEditor;
        underlyingShape = new UnderlyingShape();
    }

    public void setUnderlying(boolean flag) {
        underlying = flag;
    }

    public void reset() {
        underlyingShape.reset();
    }

    public void paint(Graphics g) {
        if(!underlying)
            return;

        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        if(underlyingShape.isReady()) {
            underlyingShape.draw(g2d);
            return;
        }

        underlyingShape.begin();

        render(g);

        underlyingShape.end();
        underlyingShape.draw(g2d);
    }

    public abstract void render(Graphics g);

    public static final int SHAPE_SAW_TOOTH = 1;
    public static final int SHAPE_LINE = 2;
    public static final int SHAPE_RECT = 3;

    public void drawUnderlineAtIndexes(Graphics g, Color c, int start, int end, int shape) {
        try {
            Rectangle r1 = textEditor.textPane.modelToView(start);
            Rectangle r2 = textEditor.textPane.modelToView(end);

            g.setColor(c);

            switch(shape) {
                case SHAPE_SAW_TOOTH: {
                    int width = r2.x-r1.x;
                    int triangle_size = 5;
                    for(int triangle=0; triangle<width/triangle_size; triangle++) {
                        int x = r1.x+triangle*triangle_size;
                        int y = r1.y+r1.height-1;
                        g.drawLine(x, y, x+triangle_size/2, y-triangle_size/2);
                        g.drawLine(x+triangle_size/2, y-triangle_size/2, x+triangle_size, y);


                        underlyingShape.addLine(c, x, y, x+triangle_size/2, y-triangle_size/2);
                        underlyingShape.addLine(c, x+triangle_size/2, y-triangle_size/2, x+triangle_size, y);
                    }
                }
                break;

                case SHAPE_LINE: {
                    g.drawLine(r1.x, r1.y+r1.height, r2.x, r2.y+r2.height);
                    underlyingShape.addLine(c, r1.x, r1.y+r1.height, r2.x, r2.y+r2.height);
                }
                break;

                case SHAPE_RECT: {
                    g.drawRect(r1.x, r1.y, r2.x+r2.width-r1.x, r1.height);
                    underlyingShape.addRect(c, r1.x, r1.y, r2.x+r2.width, r2.y+r2.height);
                }
                break;
            }
        } catch (BadLocationException e) {
            // Ignore
        }
    }


    public static class UnderlyingShape {

        public Map<Color,GeneralPath> shapes = new HashMap<Color, GeneralPath>();
        public boolean ready = false;

        public void addLine(Color c, int x1, int y1, int x2, int y2) {
            GeneralPath gp = shapes.get(c);
            if(gp == null) {
                gp = new GeneralPath();
                shapes.put(c, gp);
            }
            gp.moveTo(x1, y1);
            gp.lineTo(x2, y2);
        }

        public void addRect(Color c, int x1, int y1, int x2, int y2) {
            GeneralPath gp = shapes.get(c);
            if(gp == null) {
                gp = new GeneralPath();
                shapes.put(c, gp);
            }
            gp.moveTo(x1, y1);
            gp.lineTo(x2, y1);
            gp.lineTo(x2, y2);
            gp.lineTo(x1, y2);
            gp.lineTo(x1, y1);
        }

        public void draw(Graphics2D g) {
            for (Color c : shapes.keySet()) {
                g.setColor(c);
                g.draw(shapes.get(c));
            }
        }

        public void begin() {
            reset();
        }

        public void end() {
            ready = true;
        }

        public boolean isReady() {
            return ready;
        }

        public void reset() {
            shapes.clear();
            ready = false;
        }
    }

}
