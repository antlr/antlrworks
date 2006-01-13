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

package org.antlr.works.ate;

import org.antlr.works.ate.breakpoint.ATEBreakpointEntity;
import org.antlr.works.ate.folding.ATEFoldingEntity;
import org.antlr.works.utils.IconManager;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.List;

public class ATEGutter extends JComponent {

    public static final int BREAKPOINT_WIDTH = 9;
    public static final int BREAKPOINT_HEIGHT = 9;
    public static final int FOLDING_ICON_WIDTH = 9;
    public static final int FOLDING_ICON_HEIGHT = 9;

    public static final int OFFSET_FROM_TEXT = 2;

    protected static final Color BACKGROUND_COLOR = new Color(240,240,240);
    protected static final Stroke FOLDING_DASHED_STROKE = new BasicStroke(0.0f, BasicStroke.CAP_BUTT,
                                                    BasicStroke.JOIN_MITER, 1.0f, new float[] { 1.0f}, 0.0f);

    protected ATEPanel textEditor;

    protected List breakpoints = new ArrayList();

    protected List folding = new ArrayList();
    protected boolean foldingEnabled = false;

    protected Image collapseDown;
    protected Image collapseUp;
    protected Image collapse;
    protected Image expand;

    protected boolean dirty = true;

    public ATEGutter(ATEPanel textEditor) {
        this.textEditor = textEditor;

        collapseDown = IconManager.shared().getIconCollapseDown().getImage();
        collapseUp = IconManager.shared().getIconCollapseUp().getImage();
        collapse = IconManager.shared().getIconCollapse().getImage();
        expand = IconManager.shared().getIconExpand().getImage();

        addMouseListener(new MyMouseAdapter());
        addMouseMotionListener(new MyMouseMotionAdapter());
    }

    public void setFoldingEnabled(boolean foldingEnabled) {
        this.foldingEnabled = foldingEnabled;
    }

    public void markDirty() {
        dirty = true;
        repaint();
    }

    public Set getBreakpoints() {
        // Returns a set containing all lines which contains a breakpoint
        Set set = new HashSet();
        for (Iterator iterator = breakpoints.iterator(); iterator.hasNext();) {
            BreakpointInfo info = (BreakpointInfo) iterator.next();
            if(info.entity.breakpointEntityIsBreakpoint())
                set.add(new Integer(info.entity.breakpointEntityLine()));
        }
        return set;
    }

    protected void toggleBreakpoint(BreakpointInfo info) {
        if(info == null)
            return;

        info.entity.breakpointEntitySetBreakpoint(!info.entity.breakpointEntityIsBreakpoint());
        repaint();
    }

    protected void toggleFolding(FoldingInfo info) {
        if(info == null || !info.entity.foldingEntityCanBeCollapsed())
            return;

        textEditor.foldingManager.toggleFolding(info.entity);
        markDirty();
    }

    protected int getLineYPixelPosition(int indexInText) {
        try {
            Rectangle r = textEditor.textPane.modelToView(indexInText);
            return r.y + r.height / 2;
        } catch (BadLocationException e) {
            return -1;
        }
    }

    public void updateInfo() {
        breakpoints.clear();
        if(textEditor.breakpointManager != null) {
            List entities = textEditor.breakpointManager.getBreakpointEntities();
            for (int i=0; i<entities.size(); i++) {
                ATEBreakpointEntity entity = (ATEBreakpointEntity)entities.get(i);
                int y = getLineYPixelPosition(entity.breakpointEntityIndex());
                Rectangle r = new Rectangle(0, y-BREAKPOINT_HEIGHT/2, BREAKPOINT_WIDTH, BREAKPOINT_HEIGHT);
                breakpoints.add(new BreakpointInfo(entity, r));
            }
        }

        folding.clear();
        if(textEditor.foldingManager != null) {
            List entities = textEditor.foldingManager.getFoldingEntities();
            for(int i=0; i<entities.size(); i++) {
                ATEFoldingEntity entity = (ATEFoldingEntity)entities.get(i);

                int top_y = getLineYPixelPosition(entity.foldingEntityGetStartIndex());
                int bottom_y = getLineYPixelPosition(entity.foldingEntityGetEndIndex());

                Point top = new Point(getWidth()-getOffsetFromText(), top_y);
                Point bottom = new Point(getWidth()-getOffsetFromText(), bottom_y);
                folding.add(new FoldingInfo(entity, top, bottom));
            }
        }
    }

    public BreakpointInfo getBreakpointInfoAtPoint(Point p) {
        for(int i=0; i<breakpoints.size(); i++) {
            BreakpointInfo info = (BreakpointInfo)breakpoints.get(i);
            if(info.contains(p))
                return info;
        }
        return null;
    }

    public FoldingInfo getFoldingInfoAtPoint(Point p) {
        for(int i=0; i<folding.size(); i++) {
            FoldingInfo info = (FoldingInfo)folding.get(i);
            if(info.contains(p))
                return info;
        }
        return null;
    }

    public int getOffsetFromText() {
        return OFFSET_FROM_TEXT+FOLDING_ICON_WIDTH/2;
    }

    public void paintComponent(Graphics g) {
        Rectangle r = g.getClipBounds();

        paintGutter(g, r);

        if(dirty) {
            updateInfo();
            dirty = false;
        }

        paintFolding((Graphics2D)g, r);
        paintBreakpoints((Graphics2D)g, r);
    }

    public void paintGutter(Graphics g, Rectangle r) {
        g.setColor(textEditor.textPane.getBackground());
        g.fillRect(r.x+r.width-getOffsetFromText(), r.y, getOffsetFromText(), r.height);

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(r.x, r.y, r.width-getOffsetFromText(), r.height);

        g.setColor(Color.lightGray);
        g.drawLine(r.x+r.width-getOffsetFromText(), r.y, r.x+r.width-getOffsetFromText(), r.y+r.height);
    }

    public void paintBreakpoints(Graphics2D g, Rectangle clip) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.red);
        for (int index = 0; index < breakpoints.size(); index++) {
            BreakpointInfo info = (BreakpointInfo) breakpoints.get(index);
            if(info.entity.breakpointEntityIsBreakpoint()) {
                Rectangle r = info.r;
                if(clip.intersects(r))
                    g.fillArc(r.x, r.y, r.width, r.height, 0, 360);
            }
        }
    }

    public void paintFolding(Graphics2D g, Rectangle clip) {
        // Do not alias otherwise the dotted line between collapsed icon doesn't show up really well
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);

        for (int index = 0; index < folding.size(); index++) {
            FoldingInfo info = (FoldingInfo) folding.get(index);

            if(!clip.intersects(info.top_r) && !clip.intersects(info.bottom_r))
                continue;

            Point top = info.top;
            Point bottom = info.bottom;
            if(foldingEnabled && info.entity.foldingEntityCanBeCollapsed()) {
                if(info.entity.foldingEntityIsExpanded()) {
                    g.setColor(Color.white);
                    g.drawLine(top.x, top.y, bottom.x, bottom.y);

                    Stroke s = g.getStroke();
                    g.setStroke(FOLDING_DASHED_STROKE);
                    g.setColor(Color.black);
                    g.drawLine(top.x, top.y, bottom.x, bottom.y);
                    g.setStroke(s);

                    if(top.equals(bottom)) {
                        drawCenteredImageAtPoint(g, collapse, top);
                    } else {
                        drawCenteredImageAtPoint(g, collapseUp, top);
                        drawCenteredImageAtPoint(g, collapseDown, bottom);
                    }
                } else {
                    drawCenteredImageAtPoint(g, expand, top);
                }
            } else {
                g.setColor(Color.white);
                g.fill(info.top_r);
                g.setColor(Color.lightGray);
                g.draw(info.top_r);
            }
        }
    }

    public Dimension getPreferredSize() {
        Dimension d = textEditor.textPane.getSize();
        d.width = 25;
        return d;
    }

    public static void drawCenteredImageAtPoint(Graphics g, Image image, Point p) {
        g.drawImage(image, p.x-image.getWidth(null)/2, p.y-image.getHeight(null)/2, null);
    }

    protected class BreakpointInfo {
        public ATEBreakpointEntity entity;
        public Rectangle r;

        public BreakpointInfo(ATEBreakpointEntity entity, Rectangle r) {
            this.entity = entity;
            this.r = r;
        }

        public boolean contains(Point p) {
            return r.contains(p);
        }
    }

    protected class FoldingInfo {
        public ATEFoldingEntity entity;

        public Point top;
        public Point bottom;

        public Rectangle top_r;
        public Rectangle bottom_r;

        public FoldingInfo(ATEFoldingEntity entity, Point top, Point bottom) {
            this.entity = entity;
            this.top = top;
            this.bottom = bottom;
            this.top_r = new Rectangle(top.x-FOLDING_ICON_WIDTH/2, top.y-FOLDING_ICON_HEIGHT/2, FOLDING_ICON_WIDTH, FOLDING_ICON_HEIGHT);
            this.bottom_r = new Rectangle(bottom.x-FOLDING_ICON_WIDTH/2, bottom.y-FOLDING_ICON_HEIGHT/2, FOLDING_ICON_WIDTH, FOLDING_ICON_HEIGHT);
        }

        public boolean contains(Point p) {
            if(entity.foldingEntityIsExpanded())
                return top_r.contains(p) || bottom_r.contains(p);
            else
                return top_r.contains(p);
        }
    }

    protected class MyMouseAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            toggleBreakpoint(getBreakpointInfoAtPoint(e.getPoint()));
            toggleFolding(getFoldingInfoAtPoint(e.getPoint()));
        }

        public void mouseExited(MouseEvent e) {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    protected class MyMouseMotionAdapter extends MouseMotionAdapter {
        public void mouseMoved(MouseEvent e) {
            FoldingInfo info = getFoldingInfoAtPoint(e.getPoint());
            if(info != null && info.entity.foldingEntityCanBeCollapsed())
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            else
                setCursor(Cursor.getDefaultCursor());
        }
    }
}
