package org.antlr.works.editor.analysis;

import edu.usfca.xj.appkit.gview.timer.GTimer;
import edu.usfca.xj.appkit.gview.timer.GTimerDelegate;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.parser.Token;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Iterator;
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

public class AnalysisStrip extends JPanel {

    protected EditorWindow editor;
    protected AnalysisBox analysisBox;
    protected AnalysisStripOverlay overlay;

    protected int topOffset = 30;
    protected int bottomOffset = 50;
    protected int lineCount;
    protected int numberOfErrors;
    protected int numberOfWarnings;

    public AnalysisStrip(EditorWindow editor) {
        this.editor = editor;

        setFocusable(false);

        analysisBox = new AnalysisBox();
        overlay = new AnalysisStripOverlay(editor.getJFrame(), this);

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                StringBuffer sb = new StringBuffer();
                if(analysisBox.r.contains(e.getPoint())) {
                    if(numberOfErrors == 0)
                        sb.append("No errors");
                    else {
                        sb.append(numberOfErrors);
                        sb.append(" errors found");
                    }
                    if(numberOfWarnings > 0) {
                        sb.append("\n");
                        sb.append(numberOfWarnings);
                        sb.append(" warnings found");
                    }
                } else {
                    Token undefToken = getUndefinedTokenAtPoint(e.getPoint());
                    if(undefToken != null) {
                        if(sb.length() > 0)
                            sb.append("\n");
                        sb.append("Undefined token \"");
                        sb.append(undefToken.getAttribute());
                        sb.append("\"");
                    }

                    ParserRule dupRule = getDuplicateRuleAtPoint(e.getPoint());
                    if(dupRule != null) {
                        if(sb.length() > 0)
                            sb.append("\n");
                        sb.append("Duplicate rule \"");
                        sb.append(dupRule.name);
                        sb.append("\"");
                    }

                    ParserRule leftRecurRule = getHasLeftRecursionRuleAtPoint(e.getPoint());
                    if(leftRecurRule != null) {
                        if(sb.length() > 0)
                            sb.append("\n");
                        sb.append("Left recursion in rule \"");
                        sb.append(leftRecurRule.name);
                        sb.append("\"");
                    }
                }

                if(sb.length() > 0) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    overlay.setLocation(e.getPoint());
                    overlay.setText(sb.toString());
                    overlay.display();
                } else {
                    setCursor(Cursor.getDefaultCursor());
                    overlay.hide();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            public int getIndexOfFirstErrors(Point p) {
                Token t = getUndefinedTokenAtPoint(p);
                if(t != null)
                    return t.getStartIndex();

                ParserRule r = getDuplicateRuleAtPoint(p);
                if(r != null)
                    return r.getStartIndex();

                r = getHasLeftRecursionRuleAtPoint(p);
                if(r != null)
                    return r.getStartIndex();

                return -1;
            }

            public void mousePressed(MouseEvent e) {
                int index = getIndexOfFirstErrors(e.getPoint());                
                if(index > -1) {
                    overlay.hide();
                    AnalysisStrip.this.editor.setCaretPosition(index);
                }
            }

            public void mouseExited(MouseEvent e) {
                overlay.hide();
            }
        });
    }

    public Token getUndefinedTokenAtPoint(Point p) {
        for(Iterator iter = editor.rules.getUndefinedTokens().iterator(); iter.hasNext(); ) {
            Token token = (Token)iter.next();
            if(composeIndicatorRectangle(token.line, 2).contains(p))
                return token;
        }
        return null;
    }

    public ParserRule getDuplicateRuleAtPoint(Point p) {
        for(Iterator iter = editor.rules.getDuplicateRules().iterator(); iter.hasNext(); ) {
            ParserRule rule = (ParserRule) iter.next();
            if(composeIndicatorRectangle(rule.start.line, 2).contains(p))
                return rule;
        }
        return null;
    }

    public ParserRule getHasLeftRecursionRuleAtPoint(Point p) {
        for(Iterator iter = editor.rules.getHasLeftRecursionRules().iterator(); iter.hasNext(); ) {
            ParserRule rule = (ParserRule) iter.next();
            if(composeIndicatorRectangle(rule.start.line, 2).contains(p))
                return rule;
        }
        return null;
    }

    public Rectangle getDrawingBounds() {
        Rectangle r = getBounds();
        r.y = topOffset;
        r.height -= topOffset + bottomOffset;
        return r;
    }

    public Rectangle composeIndicatorRectangle(int line, int coarse) {
        Rectangle r = getDrawingBounds();
        float position = (float)line / lineCount;
        int y = (int)(r.y + r.height * position);
        return new Rectangle(3, y-coarse, r.width-6, 2+2*coarse);
    }

    public void paint(Graphics g) {
        super.paint(g);

        lineCount = editor.parser.getMaxLines();

        numberOfErrors = 0;
        numberOfWarnings = 0;

        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.red);
        for(Iterator iter = editor.rules.getUndefinedTokens().iterator(); iter.hasNext(); ) {
            Token token = (Token)iter.next();
            g2d.fill(composeIndicatorRectangle(token.line, 0));
            numberOfErrors++;
        }

        g2d.setColor(Color.blue);
        for(Iterator iter = editor.rules.getDuplicateRules().iterator(); iter.hasNext(); ) {
            ParserRule rule = (ParserRule) iter.next();
            g2d.fill(composeIndicatorRectangle(rule.start.line, 0));
            numberOfErrors++;
        }

        g2d.setColor(Color.green);
        for(Iterator iter = editor.rules.getHasLeftRecursionRules().iterator(); iter.hasNext(); ) {
            ParserRule rule = (ParserRule) iter.next();
            g2d.fill(composeIndicatorRectangle(rule.start.line, 0));
            numberOfWarnings++;
        }

        analysisBox.paint(g);
    }

    protected class AnalysisBox implements GTimerDelegate {

        private GTimer timer = new GTimer(this, 500);
        private boolean color = false;
        private boolean activity = false;
        private final Color greenColor = new Color(0f, 0.9f, 0.25f, 1.0f);
        private final Color redColor = Color.red;
        protected final Rectangle r = new Rectangle(2, 2, 14, 14);

        public AnalysisBox() {
        }

        public void start() {
            activity = true;
            timer.start();
        }

        public void stop() {
            timer.stop();
            activity = false;
            repaint();
        }

        public void paint(Graphics g) {
            BorderFactory.createEtchedBorder().paintBorder(AnalysisStrip.this, g, r.x, r.y, r.width, r.height);

            if(activity)
                g.setColor(color?Color.white:Color.yellow);
            else {
                if(numberOfErrors == 0 && numberOfWarnings == 0)
                    g.setColor(greenColor);
                else if(numberOfErrors == 0)
                    g.setColor(Color.yellow);
                else
                    g.setColor(redColor);
            }

            g.fillRect(r.x+2, r.y+2, r.width-5, r.height-5);
        }

        public void timerFired(GTimer timer) {
            color = !color;
            repaint();
        }
    }

}
