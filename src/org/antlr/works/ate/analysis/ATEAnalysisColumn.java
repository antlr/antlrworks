package org.antlr.works.ate.analysis;

import org.antlr.works.ate.ATEPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
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

/** This class handles the analysis column located at the right of the ATEPanel.
 * It purposes is to display information about errors/warnings in the text.
 * It uses the ATEAnalysisManager to find the errors/warnings to display.
 */

public class ATEAnalysisColumn extends JPanel {

    protected ATEPanel textEditor;
    protected ATEAnalysisColumnOverlay overlay;
    protected ColumnAnalysisBox analysisBox;

    protected int topOffset = 30;
    protected int bottomOffset = 50;
    protected int lineCount;

    public ATEAnalysisColumn(ATEPanel textEditor) {
        this.textEditor = textEditor;

        setFocusable(false);

        analysisBox = new ColumnAnalysisBox();
        overlay = new ATEAnalysisColumnOverlay(textEditor.getParentFrame(), this);

        addMouseMotionListener(new ColumnMouseMotionAdapter());
        addMouseListener(new ColumnMouseAdapter());
    }

    public void close() {
        overlay.close();
        overlay = null;
    }
    
    public ATEAnalysisManager getAnalysisManager() {
        return textEditor.getAnalysisManager();
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

        ATEAnalysisManager manager = getAnalysisManager();
        if(manager == null)
            return;

        lineCount = manager.getLinesCount();

        Graphics2D g2d = (Graphics2D)g;
        int[] types = manager.getAvailableTypes();
        for(int type=0; type<types.length; type++) {
            paintStrips(g2d, manager.getItemsForType(type));
        }

        analysisBox.paint(g);
    }

    protected void paintStrips(Graphics2D g, List<ATEAnalysisItem> items) {
        for (ATEAnalysisItem item : items) {
            g.setColor(item.color);
            g.fill(composeIndicatorRectangle(item.line, 0));
        }
    }

    /** This class is used to draw the little analysis colored box at the top
     * of the analysis column.
     */

    protected class ColumnAnalysisBox {

        protected final Rectangle r = new Rectangle(2, 2, 14, 14);

        public ColumnAnalysisBox() {
        }

        public void paint(Graphics g) {
            BorderFactory.createEtchedBorder().paintBorder(ATEAnalysisColumn.this, g, r.x, r.y, r.width, r.height);
            g.setColor(getAnalysisManager().getAnalysisColor());
            g.fillRect(r.x+2, r.y+2, r.width-5, r.height-5);
        }
    }

    protected class ColumnMouseAdapter extends MouseAdapter {

        public int getIndexOfFirstErrors(Point p) {
            int[] types = getAnalysisManager().getAvailableTypes();
            for(int type=0; type<types.length; type++) {
                List<ATEAnalysisItem> items = getAnalysisManager().getItemsForType(type);
                for (ATEAnalysisItem ai : items) {
                    if (composeIndicatorRectangle(ai.line, 2).contains(p)) {
                        return ai.index;
                    }
                }
            }
            return -1;
        }

        /** Jump into the text at the location of the current error/warning under
         * the mouse.
         */

        public void mousePressed(MouseEvent e) {
            int index = getIndexOfFirstErrors(e.getPoint());
            if(index > -1) {
                overlay.hide();
                textEditor.setCaretPosition(index);
            }
        }

        public void mouseExited(MouseEvent e) {
            overlay.hide();
        }
    }

    /** This class is used to display any tooltip describing the errors/warnings
     * under the cursor position.
     */

    protected class ColumnMouseMotionAdapter extends MouseMotionAdapter {

        public void mouseMoved(MouseEvent e) {
            ATEAnalysisManager manager = getAnalysisManager();
            if(manager == null)
                return;

            StringBuilder sb = new StringBuilder();
            if(analysisBox.r.contains(e.getPoint())) {
                sb.append(manager.getAnalysisDescription());
            } else {
                sb.append(getItemDescriptionsAtPoint(e.getPoint()));
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

        protected String getItemDescriptionsAtPoint(Point point) {
            StringBuilder sb = new StringBuilder();
            ATEAnalysisManager manager = getAnalysisManager();
            if(manager != null) {
                int[] types = manager.getAvailableTypes();
                for(int type=0; type<types.length; type++) {
                    List<ATEAnalysisItem> items = manager.getItemsForType(type);
                    for (ATEAnalysisItem ai : items) {
                        if (composeIndicatorRectangle(ai.line, 2).contains(point)) {
                            sb.append(ai.description);
                            sb.append("\n");
                        }
                    }
                }
            }
            return sb.toString();
        }
    }
}
