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

package org.antlr.works.editor.textpane;

import org.antlr.works.editor.textpane.folding.EntityProxy;
import org.antlr.works.editor.textpane.folding.Provider;
import org.antlr.works.parser.Line;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.util.IconManager;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.List;

public class EditorGutter extends JComponent {

    public static final int BREAKPOINT_WIDTH = 9;
    public static final int BREAKPOINT_HEIGHT = 9;
    public static final int FOLDING_ICON_WIDTH = 9;
    public static final int FOLDING_ICON_HEIGHT = 9;

    public static final int OFFSET_FROM_TEXT = 2;

    protected static final Color BACKGROUND_COLOR = new Color(240,240,240);
    protected static final Stroke FOLDING_DASHED_STROKE = new BasicStroke(0.0f, BasicStroke.CAP_BUTT,
                                                    BasicStroke.JOIN_MITER, 1.0f, new float[] { 1.0f}, 0.0f);

    protected Map bps = new HashMap();
    protected List rules = null;
    protected List lines = null;
    protected boolean folding = false;

    protected EditorTextPane editorTextPane;
    protected List markerInfos = new ArrayList();
    protected Provider provider;

    protected Image collapseDown;
    protected Image collapseUp;
    protected Image expand;

    public EditorGutter(EditorTextPane textPane) {
        super();

        this.editorTextPane = textPane;

        collapseDown = IconManager.shared().getIconCollapseDown().getImage();
        collapseUp = IconManager.shared().getIconCollapseUp().getImage();
        expand = IconManager.shared().getIconExpand().getImage();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                MarkerInfo info = getMarkerInfoAtPoint(e.getPoint(), false);
                if(info != null) {
                    toggleBreakpoint(info.line);
                    return;
                }

                info = getMarkerInfoAtPoint(e.getPoint(), true);
                if(info != null) {
                    toggleFolding(info.line);
                }
            }

            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                MarkerInfo info = getMarkerInfoAtPoint(e.getPoint(), true);
                if(info != null && getCollapsableRuleAtLine(info.line) != null)
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                else
                    setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public void setRules(List rules, List lines) {
        this.rules = rules;
        this.lines = lines;
        repaint();
    }

    public void setFolding(boolean folding) {
        this.folding = folding;
    }

    public Set getBreakpoints() {
        Set set = new HashSet();
        for (Iterator iterator = bps.keySet().iterator(); iterator.hasNext();) {
            Integer line = (Integer) iterator.next();
            Boolean bp = (Boolean)bps.get(line);
            if(bp.booleanValue())
                set.add(line);
        }
        return set;
    }

    protected MarkerInfo getMarkerInfoAtPoint(Point m, boolean folding) {
        for(int i=0; i<markerInfos.size(); i++) {
            MarkerInfo info = (MarkerInfo)markerInfos.get(i);
            if(m.y >= info.coord_y-BREAKPOINT_HEIGHT*0.5 && m.y <= info.coord_y+BREAKPOINT_HEIGHT*0.5) {
                if(m.x <= BREAKPOINT_WIDTH && !folding)
                    return info;
                else if(m.x > BREAKPOINT_WIDTH && folding)
                    return info;
            }
        }
        return null;
    }

    protected void toggleBreakpoint(int line) {
        Integer value = new Integer(line);
        Boolean state = (Boolean)bps.get(value);
        if(state != null)
            bps.put(value, Boolean.valueOf(!state.booleanValue()));
        else
            bps.put(value, Boolean.TRUE);
        repaint();
    }

    protected void toggleFolding(int line) {
        ParserRule rule = getCollapsableRuleAtLine(line);
        if(rule != null) {
            editorTextPane.toggleFolding(new EntityProxy(provider, rule.name));
            repaint();
        }
    }

    protected ParserRule getCollapsableRuleAtLine(int line) {
        for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
            ParserRule rule = (ParserRule) iterator.next();
            if(rule.start.line == line && rule.canBeCollapsed() && folding)
                return rule;
        }
        return null;
    }

    protected int getLineYPixelPosition(int indexInText) {
        try {
            //int rowStartIndex = Utilities.getRowStart(editorTextPane, indexInText);
            Rectangle r = editorTextPane.modelToView(indexInText);
            return r.y + r.height;
        } catch (BadLocationException e) {
            return -1;
        }
    }

    protected int getLineY(int lineIndex) {
        Line line;
        Rectangle lineRectangle;
        try {
            line = (Line)lines.get(lineIndex);
            lineRectangle = editorTextPane.modelToView(line.position);
        } catch (BadLocationException e) {
            return 0;
        }
        return (int) (lineRectangle.y+lineRectangle.height*0.5);
    }

    public void updateMarkerInfo() {
        markerInfos.clear();
        for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
            ParserRule rule = (ParserRule) iterator.next();
            int rule_y = getLineY(rule.start.line);
            markerInfos.add(new MarkerInfo(rule.start.line, rule_y));
        }
    }

    public void paintComponent(Graphics g) {
        Rectangle r = g.getClipBounds();

        paintGutter(g, r);

        if(rules != null && lines != null) {
            updateMarkerInfo();

            paintFolding((Graphics2D)g, r);
            paintBreakpoints((Graphics2D)g, r);
        }
    }

    protected void paintGutter(Graphics g, Rectangle r) {
        g.setColor(editorTextPane.getBackground());
        g.fillRect(r.x+r.width-FOLDING_ICON_WIDTH/2-OFFSET_FROM_TEXT, r.y, FOLDING_ICON_WIDTH/2+OFFSET_FROM_TEXT, r.height);

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(r.x, r.y, r.width-FOLDING_ICON_WIDTH/2-OFFSET_FROM_TEXT, r.height);

        g.setColor(Color.lightGray);
        g.drawLine(r.x+r.width-FOLDING_ICON_WIDTH/2-1-OFFSET_FROM_TEXT, r.y, r.x+r.width-FOLDING_ICON_WIDTH/2-1-OFFSET_FROM_TEXT, r.y+r.height);
    }

    protected void paintBreakpoints(Graphics2D g, Rectangle r) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.red);
        for (Iterator iterator = bps.keySet().iterator(); iterator.hasNext();) {
            Integer line = (Integer) iterator.next();
            Boolean bp = (Boolean)bps.get(line);

            int line_y = getLineY(line.intValue());
            if(line_y < r.y || line_y > r.y+r.height)
                continue;

            if(bp.booleanValue()) {
                g.fillArc(r.x+1, (int) (line_y-BREAKPOINT_HEIGHT*0.5), BREAKPOINT_WIDTH, BREAKPOINT_HEIGHT, 0, 360);
            }
        }
    }

    protected void paintFolding(Graphics2D g, Rectangle r) {
        // Do not alias otherwise the dotted line between collapsed icon doesn't show up really well
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);

        for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
            ParserRule rule = (ParserRule) iterator.next();
            int rule_y = getLineY(rule.start.line);
            //int rule_y = getLineYPixelPosition(rule.getStartIndex());
            if(rule_y < r.y || rule_y > r.y+r.height)
                continue;

            if(folding && rule.canBeCollapsed()) {
                if(rule.isExpanded()) {
                    //int bottom_rule_y = getLineYPixelPosition(rule.getEndIndex());
                    int bottom_rule_y = getLineY(rule.end.line);

                    g.setColor(Color.white);
                    g.drawLine(r.x+r.width-FOLDING_ICON_WIDTH/2-1-OFFSET_FROM_TEXT, rule_y, r.x+r.width-FOLDING_ICON_WIDTH/2-1-OFFSET_FROM_TEXT, bottom_rule_y);

                    Stroke s = g.getStroke();
                    g.setStroke(FOLDING_DASHED_STROKE);
                    g.setColor(Color.black);
                    g.drawLine(r.x+r.width-FOLDING_ICON_WIDTH/2-1-OFFSET_FROM_TEXT, rule_y, r.x+r.width-FOLDING_ICON_WIDTH/2-1-OFFSET_FROM_TEXT, bottom_rule_y);
                    g.setStroke(s);

                    g.drawImage(collapseUp, r.x+r.width-FOLDING_ICON_WIDTH-OFFSET_FROM_TEXT, (int) (rule_y-collapseUp.getHeight(null)*0.5), null);
                    g.drawImage(collapseDown, r.x+r.width-FOLDING_ICON_WIDTH-OFFSET_FROM_TEXT, (int) (bottom_rule_y-collapseDown.getHeight(null)*0.5), null);
                } else {
                    g.drawImage(expand, r.x+r.width-FOLDING_ICON_WIDTH-OFFSET_FROM_TEXT, (int) (rule_y-expand.getHeight(null)*0.5), null);
                }
            } else {
                g.setColor(Color.white);
                g.fillRect(r.x+r.width-(FOLDING_ICON_WIDTH+3), (int) (rule_y-FOLDING_ICON_HEIGHT*0.5), FOLDING_ICON_WIDTH, FOLDING_ICON_HEIGHT);
                g.setColor(Color.lightGray);
                g.drawRect(r.x+r.width-(FOLDING_ICON_WIDTH+3), (int) (rule_y-FOLDING_ICON_HEIGHT*0.5), FOLDING_ICON_WIDTH, FOLDING_ICON_HEIGHT);
            }
        }
    }

    protected class MarkerInfo {
        public int line;
        public int coord_y;

        public MarkerInfo(int line, int coord_y) {
            this.line = line;
            this.coord_y = coord_y;
        }
    }
}
