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

package org.antlr.works.debugger.input;

import edu.usfca.xj.foundation.notification.XJNotificationCenter;
import edu.usfca.xj.foundation.notification.XJNotificationObserver;
import org.antlr.runtime.Token;
import org.antlr.works.debugger.Debugger;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.prefs.AWPrefsDialog;
import org.antlr.works.utils.TextPane;
import org.antlr.works.utils.TextPaneDelegate;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.GeneralPath;
import java.util.*;

public class DBInputText implements TextPaneDelegate, XJNotificationObserver {

    public static final int TOKEN_NORMAL = 1;
    public static final int TOKEN_HIDDEN = 2;
    public static final int TOKEN_DEAD = 3;

    public static final Color HIGHLIGHTED_COLOR = new Color(0, 0.5f, 1, 0.4f);
    public static final Color INPUT_BREAKPOINT_COLOR = new Color(1, 0.2f, 0, 0.5f);

    protected Debugger debugger;
    protected TextPane textPane;
    protected int mouseIndex = -1;

    // Location where the next token will be inserted
    protected int cursorIndex;
    protected Map tokens;

    protected boolean persistence;
    // Length of the persistent text (for which we have tokens)
    protected int persistenceTextLength;

    // Last location event received by the debugger
    protected int locationLine;
    protected int locationCharInLine;

    // Input breakpoints
    protected Set inputBreakpoints = new HashSet();

    protected SimpleAttributeSet attributeNonConsumed;
    protected SimpleAttributeSet attributeConsume;
    protected SimpleAttributeSet attributeConsumeHidden;
    protected SimpleAttributeSet attributeConsumeDead;
    protected SimpleAttributeSet attributeLookahead;

    protected boolean drawTokensBox;

    public DBInputText(Debugger debugger, TextPane textPane) {
        this.debugger = debugger;
        this.textPane = textPane;
        this.textPane.setDelegate(this);
        this.textPane.addMouseListener(new MyMouseListener());
        this.textPane.addMouseMotionListener(new MyMouseMotionListener());

        tokens = new HashMap();
        drawTokensBox = false;

        reset();
        createTextAttributes();

        XJNotificationCenter.defaultCenter().addObserver(this, AWPrefsDialog.NOTIF_PREFS_APPLIED);
    }

    public void close() {
        XJNotificationCenter.defaultCenter().removeObserver(this);
    }

    public void notificationFire(Object source, String name) {
        if(name.equals(AWPrefsDialog.NOTIF_PREFS_APPLIED)) {
            createTextAttributes();
        }
    }

    public int getCursorIndex() {
        return cursorIndex;
    }

    public void setDrawTokensBox(boolean flag) {
        drawTokensBox = flag;
        textPane.repaint();
    }

    public boolean isDrawTokensBox() {
        return drawTokensBox;
    }

    public void setLocation(int line, int charInLine) {
        this.locationLine = line;
        this.locationCharInLine = charInLine;
    }

    public void consumeToken(Token token, int type) {
        SimpleAttributeSet attr = null;
        switch(type) {
            case TOKEN_NORMAL: attr = attributeConsume; break;
            case TOKEN_HIDDEN: attr = attributeConsumeHidden; break;
            case TOKEN_DEAD: attr = attributeConsumeDead; break;
        }
        addText(token, attr);
        addToken(token);
    }

    public void doLT(Token token) {
        addText(token, attributeLookahead);
        addToken(token);
    }

    public void stop() {
        inputBreakpoints.clear();
    }

    public void reset() {
        textPane.setText("");

        textPane.setCharacterAttributes(SimpleAttributeSet.EMPTY, true);
        cursorIndex = 0;

        tokens.clear();

        persistence = true;
        persistenceTextLength = 0;
    }

    public void rewindAll() {
        if(persistence) {
            textPane.selectAll();
            textPane.setCharacterAttributes(attributeNonConsumed, true);
            textPane.setCaretPosition(0);
        } else {
            textPane.setText("");
        }
        cursorIndex = 0;
    }

    public void rewind(int start) {
        if(persistence) {
            int persistentStart = Math.max(persistenceTextLength, start);
            if(getTextLength()>persistentStart) {
                removeText(persistentStart, getTextLength());
            }

            if(persistentStart>start) {
                textPane.getStyledDocument().setCharacterAttributes(start, persistentStart-start, attributeNonConsumed, true);
                cursorIndex = start;
            }
        } else {
            removeText(start, getTextLength());
        }
    }

    protected int getTextLength() {
        return textPane.getDocument().getLength();
    }

    private void addToken(Token token) {
        int index = token.getTokenIndex();
        // Don't add -1 and disable persistence mode if the token don't have a valid index
        if(index == -1 || !persistence) {
            persistence = false;
            return;
        }

        // Don't add if the token is not contiguous to the last one
        if(index>0 && tokens.get(new Integer(index-1)) == null)
            return;

        // Don't add if the token is already in the map
        DBInputTextTokenInfo info = (DBInputTextTokenInfo) tokens.get(new Integer(index));
        if(info != null) {
            // But update its position to its most recent value
            info.line = locationLine;
            info.charInLine = locationCharInLine;
            return;
        }

        tokens.put(new Integer(index), new DBInputTextTokenInfo(token, persistenceTextLength, locationLine, locationCharInLine));
        persistenceTextLength += token.getText().length();
    }

    private void addText(Token token, AttributeSet attribute) {
        String text = token.getText();
        if(persistence) {
            DBInputTextTokenInfo info = (DBInputTextTokenInfo)tokens.get(new Integer(token.getTokenIndex()));
            if(info != null) {
                textPane.getStyledDocument().setCharacterAttributes(info.start, info.end-info.start, attribute, true);
                cursorIndex = info.end;
            } else {
                insertText(text, attribute);
            }
        } else {
            insertText(text, attribute);
        }
    }

    private void insertText(String text, AttributeSet attribute) {
        try {
            textPane.getStyledDocument().insertString(getTextLength(), text, attribute);
            cursorIndex = getTextLength();
        } catch (BadLocationException e) {
            debugger.getConsole().print(e);
        }
    }

    private void removeText(int start, int end) {
        try {
            textPane.getDocument().remove(start, end-start);
            cursorIndex = getTextLength();
        } catch (BadLocationException e) {
            debugger.getConsole().print(e);
        }
    }

    private void createTextAttributes() {
        attributeNonConsumed = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeNonConsumed, AWPrefs.getNonConsumedTokenColor());

        attributeConsume = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeConsume, AWPrefs.getConsumedTokenColor());

        attributeConsumeHidden = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeConsumeHidden, AWPrefs.getHiddenTokenColor());

        attributeConsumeDead = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeConsumeDead, AWPrefs.getDeadTokenColor());

        attributeLookahead = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeLookahead, AWPrefs.getLookaheadTokenColor());
        StyleConstants.setItalic(attributeLookahead, true);
    }

    public void textPaneDidPaint(Graphics g) {
        for (Iterator iterator = tokens.values().iterator(); iterator.hasNext();) {
            DBInputTextTokenInfo info = (DBInputTextTokenInfo) iterator.next();

            if(drawTokensBox)
                drawToken(info, (Graphics2D)g, Color.red, false);

            if(inputBreakpoints.contains(info))
                drawToken(info, (Graphics2D)g, INPUT_BREAKPOINT_COLOR, true);
            else if(mouseIndex >= info.start && mouseIndex < info.end)
                drawToken(info, (Graphics2D)g, HIGHLIGHTED_COLOR, true);
        }
    }

    public void drawToken(DBInputTextTokenInfo info, Graphics2D g, Color c, boolean fill) {
        g.setColor(c);
        try {
            Rectangle r1 = textPane.modelToView(info.start);
            Rectangle r2 = textPane.modelToView(info.end);

            if(r2.y > r1.y) {
                // Token is displayed on more than one line
                // We will simply create a path containing the
                // outline of the token spanning on multiple lines

                GeneralPath gp = new GeneralPath();

                Rectangle r = null;
                Rectangle pr = null;
                int line_y = r1.y;

                gp.moveTo(r1.x, r1.y);
                for(int index=info.start; index<info.end; index++) {
                    r = textPane.modelToView(index);
                    if(r.y > line_y) {
                        // Draw a line between the last point of the path
                        // and the last rectangle of the line which is pr
                        // because r is already in the new line.
                        line_y = r.y;
                        if(pr != null) {
                            gp.lineTo(pr.x+pr.width, pr.y);
                            gp.lineTo(pr.x+pr.width, pr.y+pr.height);
                        }
                    }
                    pr = r;
                }
                if(r != null) {
                    gp.lineTo(r.x+r.width, r.y);
                    gp.lineTo(r.x+r.width, r.y+r.height);

                    gp.lineTo(r1.x, r.y+r.height);
                    gp.lineTo(r1.x, r1.y);
                }
                if(fill)
                    g.fill(gp);
                else
                    g.draw(gp);
            } else {
                if(fill)
                    g.fillRect(r1.x, r1.y, r2.x-r1.x, r1.height);
                else
                    g.drawRect(r1.x, r1.y, r2.x-r1.x, r1.height);
            }
        } catch (BadLocationException e) {
            // Ignore exception
        }
    }

    public DBInputTextTokenInfo getTokenInfoAtIndex(int index) {
        for (Iterator iter = tokens.values().iterator(); iter.hasNext();) {
            DBInputTextTokenInfo info = (DBInputTextTokenInfo) iter.next();

            if(index >= info.start && index < info.end)
                return info;
        }
        return null;
    }

    public boolean isBreakpointAtToken(Token token) {
        for(Iterator iter = inputBreakpoints.iterator(); iter.hasNext();) {
            DBInputTextTokenInfo info = (DBInputTextTokenInfo)iter.next();
            if(info.containsToken(token))
                return true;
        }
        return false;
    }

    /** This method highlights the token at the specified index
     * in the input stream.
     */
    public void highlightToken(int index) {
        mouseIndex = index;
        textPane.repaint();
    }

    /** This method selects the token t in both the grammar and the
     * input stream
     */
    public void selectToken(Token t) {
        if(t == null) {
            highlightToken(-1);
            return;
        }


        DBInputTextTokenInfo info = getTokenInfoForToken(t);
        if(info != null)
            highlightToken(info.start);
    }

    public DBInputTextTokenInfo getTokenInfoForToken(Token t) {
        for (Iterator iter = tokens.values().iterator(); iter.hasNext();) {
            DBInputTextTokenInfo info = (DBInputTextTokenInfo) iter.next();
            if(info.token.getTokenIndex() == t.getTokenIndex())
                return info;
        }
        return null;
    }

    protected class MyMouseListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            highlightToken(textPane.getTextIndexAtLocation(e.getPoint()));
            if(mouseIndex == -1)
                return;

            DBInputTextTokenInfo info = getTokenInfoAtIndex(mouseIndex);
            if(info == null)
                return;

            boolean controlKey = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK;
            if(e.getButton() == MouseEvent.BUTTON1 && !controlKey) {
                debugger.selectToken(info.token, info.line, info.charInLine);
            } else {
                if(inputBreakpoints.contains(info))
                    inputBreakpoints.remove(info);
                else
                    inputBreakpoints.add(info);
            }
        }

        public void mouseExited(MouseEvent e) {
            highlightToken(-1);
        }
    }

    protected class MyMouseMotionListener extends MouseMotionAdapter {

        public void mouseMoved(MouseEvent e) {
            mouseIndex = textPane.getTextIndexAtLocation(e.getPoint());
            textPane.repaint();
        }
    }
}
