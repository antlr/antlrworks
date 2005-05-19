package org.antlr.works.editor.swing;

import org.antlr.works.parser.Parser;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/*

[The "BSD licence"]
Copyright (c) 2004-05 Jean Bovet
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

public class Gutter extends JComponent {

    public static final int MARKER_HEIGHT = 6;

    protected Map bps = new HashMap();
    protected List rules = null;
    protected List lines = null;

    protected JTextPane textPane = null;
    protected List markerInfos = new ArrayList();

    public Gutter(JTextPane textPane) {
        super();

        this.textPane = textPane;

        setFont(textPane.getFont());

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Point m = e.getPoint();
                for(int i=0; i<markerInfos.size(); i++) {
                    MarkerInfo info = (MarkerInfo)markerInfos.get(i);
                    if(m.y >= info.coord_y && m.y <= info.coord_y+MARKER_HEIGHT) {
                        toggleBreakpoint(info.line);
                        break;
                    }
                }
            }
        });
    }

    public void setRules(List rules, List lines) {
        this.rules = rules;
        this.lines = lines;
        repaint();
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

    public void toggleBreakpoint(int line) {
        Integer value = new Integer(line);
        Boolean state = (Boolean)bps.get(value);
        if(state != null)
            bps.put(value, new Boolean(!state.booleanValue()));
        else
            bps.put(value, Boolean.TRUE);
        repaint();
    }

    public double getLineY(int line) {
        Integer lineCharacterPosition;
        Rectangle lineRectangle = null;
        try {
            lineCharacterPosition = (Integer)lines.get(line);
            lineRectangle = textPane.modelToView(lineCharacterPosition.intValue());
        } catch (BadLocationException e) {
            return 0;
        }
        return lineRectangle.y+lineRectangle.height*0.5;
    }

    public void paintComponent(Graphics g) {
        Rectangle r = g.getClipBounds();

        g.setColor(new Color(240,240,240));
        g.fillRect(r.x, r.y, r.width, r.height);

        g.setColor(Color.lightGray);
        g.drawLine(r.x+r.width-1, r.y, r.x+r.width-1, r.y+r.height);

        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.black);

        if(rules == null || lines == null)
            return;

        markerInfos.clear();

        for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
            Parser.Rule rule = (Parser.Rule) iterator.next();

            int rule_y = (int)getLineY(rule.start.line);

            markerInfos.add(new MarkerInfo(rule.start.line, rule_y));

            if(rule_y < r.y || rule_y > r.y+r.height)
                continue;

            Boolean bp = (Boolean)bps.get(new Integer(rule.start.line));
            if(bp != null && bp.booleanValue())
                continue;

            g.fillRect(r.x+r.width-(MARKER_HEIGHT+3), (int) (rule_y-MARKER_HEIGHT*0.5), MARKER_HEIGHT, MARKER_HEIGHT);
        }

        g.setColor(Color.red);
        for (Iterator iterator = bps.keySet().iterator(); iterator.hasNext();) {
            Integer line = (Integer) iterator.next();
            Boolean bp = (Boolean)bps.get(line);

            int line_y = (int)getLineY(line.intValue());
            if(line_y < r.y || line_y > r.y+r.height)
                continue;

            if(bp.booleanValue()) {
                g.fillArc(r.x+r.width-(MARKER_HEIGHT+3), (int) (line_y-MARKER_HEIGHT*0.5), MARKER_HEIGHT, MARKER_HEIGHT, 0, 360);
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
