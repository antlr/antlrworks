package org.antlr.works.editor.swing;

/* (swing1.1beta3) */

import javax.swing.*;
import javax.swing.plaf.metal.MetalToolTipUI;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @version 1.0 11/09/98
 */
public class MultiLineToolTipUI extends MetalToolTipUI {
    private String[] strs;
    private int maxWidth = 0;

    public void paint(Graphics g, JComponent c) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        Dimension size = c.getSize();
        g.setColor(c.getBackground());
        g.fillRect(0, 0, size.width, size.height);
        g.setColor(c.getForeground());
        if (strs != null) {
            for (int i=0;i<strs.length;i++) {
                g.drawString(strs[i], 3, (metrics.getHeight()) * (i+1));
            }
        }
    }

    public Dimension getPreferredSize(JComponent c) {
        FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(c.getFont());
        String tipText = ((JToolTip)c).getTipText();
        if (tipText == null) {
            tipText = "";
        }
        BufferedReader br = new BufferedReader(new StringReader(tipText));
        String line;
        int maxWidth = 0;
        Vector v = new Vector();
        try {
            while ((line = br.readLine()) != null) {
                int width = SwingUtilities.computeStringWidth(metrics,line);
                maxWidth = (maxWidth < width) ? width : maxWidth;
                v.addElement(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        int lines = v.size();
        if (lines < 1) {
            strs = null;
            lines = 1;
        } else {
            strs = new String[lines];
            int i=0;
            for (Enumeration e = v.elements(); e.hasMoreElements() ;i++) {
                strs[i] = (String)e.nextElement();
            }
        }
        int height = metrics.getHeight() * lines;
        this.maxWidth = maxWidth;
        return new Dimension(maxWidth + 6, height + 4);
    }
}

