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

import org.antlr.runtime.Token;
import org.antlr.works.debugger.DebuggerTab;
import org.antlr.works.debugger.events.DBEventLocation;
import org.antlr.works.dialog.AWPrefsDialog;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.TextPane;
import org.antlr.works.utils.TextPaneDelegate;
import org.antlr.xjlib.foundation.XJSystem;
import org.antlr.xjlib.foundation.notification.XJNotificationCenter;
import org.antlr.xjlib.foundation.notification.XJNotificationObserver;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.*;

public class DBInputProcessorToken implements DBInputProcessor, TextPaneDelegate, XJNotificationObserver {

    public static final Color HIGHLIGHTED_COLOR = new Color(0, 0.5f, 1, 0.4f);
    public static final Color INPUT_BREAKPOINT_COLOR = new Color(1, 0.2f, 0, 0.5f);

    protected DebuggerTab debuggerTab;
    protected TextPane textPane;
    protected int mouseIndex = -1;

    protected LinkedList<Integer> inputTokenIndexes = new LinkedList<Integer>();
    protected Map<Integer,DBInputTextTokenInfo> indexToTokenInfoMap = new HashMap<Integer, DBInputTextTokenInfo>();
    protected Map<Integer,AttributeSet> indexToConsumeAttributeMap = new HashMap<Integer, AttributeSet>();
    protected Set<Integer> lookaheadTokenIndexes = new HashSet<Integer>();

    /** Current token index */
    protected int currentTokenIndex;

    /** Current token position in the input text */
    protected int currentTokenIndexInText;

    /** Last location event received by the debugger */
    protected DBEventLocation locationEvent;
    protected int locationCharInLine;

    /** Input breakpoints */
    protected Set<Integer> inputBreakpointIndexes = new HashSet<Integer>();

    protected SimpleAttributeSet attributeNonConsumed;
    protected SimpleAttributeSet attributeConsume;
    protected SimpleAttributeSet attributeConsumeHidden;
    protected SimpleAttributeSet attributeConsumeDead;
    protected SimpleAttributeSet attributeLookahead;

    protected boolean drawTokensBox;

    public DBInputProcessorToken(DebuggerTab debuggerTab, TextPane textPane) {
        this.debuggerTab = debuggerTab;

        this.textPane = textPane;
        this.textPane.setDelegate(this);
        this.textPane.addMouseListener(new MyMouseListener());
        this.textPane.addMouseMotionListener(new MyMouseMotionListener());

        drawTokensBox = false;

        reset();
        createTextAttributes();

        XJNotificationCenter.defaultCenter().addObserver(this, AWPrefsDialog.NOTIF_PREFS_APPLIED);
    }

    public void close() {
        debuggerTab = null;
        textPane.setDelegate(null);
        XJNotificationCenter.defaultCenter().removeObserver(this);
    }

    public void setDrawTokensBox(boolean flag) {
        drawTokensBox = flag;
        textPane.repaint();
    }

    public boolean isTokensBoxVisible() {
        return drawTokensBox;
    }

    public int getCurrentTokenIndex() {
        return currentTokenIndex;
    }

    public void setLocation(DBEventLocation event) {
        this.locationEvent = event;
    }

    public void consumeToken(Token token, int flavor) {
        if(ignoreToken(token))
            return;

        SimpleAttributeSet attr = null;
        switch(flavor) {
            case TOKEN_NORMAL: attr = attributeConsume; break;
            case TOKEN_HIDDEN: attr = attributeConsumeHidden; break;
            case TOKEN_DEAD: attr = attributeConsumeDead; break;
        }
        addToken(token);
        addConsumeAttribute(token, attr);
        removeTokenLT(token);
    }

    public void LT(Token token) {
        addToken(token);
        addTokenLT(token);
    }

    /**
     * On Windows, ignore the LF token following a CR because each
     * end of line is represented by two characters while Swing
     * renders the text using only LF (normalized).
     */
    public boolean ignoreToken(Token t) {
        if(!XJSystem.isWindows())
            return false;

        Token ct = getCurrentToken();
        if(ct == null)
            return false;

        return ct.getText().equals("\r") && t.getText().equals("\n");
    }

    public void addConsumeAttribute(Token token, AttributeSet attribute) {
        indexToConsumeAttributeMap.put(token.getTokenIndex(), attribute);
    }

    public void addTokenLT(Token token) {
        lookaheadTokenIndexes.add(token.getTokenIndex());
    }

    public void removeTokenLT(Token token) {
        lookaheadTokenIndexes.remove(Integer.valueOf(token.getTokenIndex()));
    }

    public void removeAllLT() {
        lookaheadTokenIndexes.clear();
    }

    public void stop() {
        inputBreakpointIndexes.clear();
    }

    public void reset() {
        textPane.setText("");
        textPane.setCharacterAttributes(SimpleAttributeSet.EMPTY, true);

        currentTokenIndex = -1;
        currentTokenIndexInText = 0;

        inputTokenIndexes.clear();
        indexToTokenInfoMap.clear();
        indexToConsumeAttributeMap.clear();
        lookaheadTokenIndexes.clear();
    }

    public void rewindAll() {
        rewind(-1);
    }

    public void rewind(int start) {
        currentTokenIndex = start;

        /** Remove any consume and lookahead attribute for any token with index
         * greater than start
         */
        for (Integer idx : inputTokenIndexes) {
            if (idx >= start) {
                indexToConsumeAttributeMap.remove(idx);
                lookaheadTokenIndexes.remove(idx);
            }
        }
    }

    public void addToken(Token token) {
        int index = token.getTokenIndex();
        if(index == -1) {
            // Ignore this index (it is used, for example, for EOF)
            return;
        }

        currentTokenIndex = index;

        /** Insert the index into the list of sorted indexes - used to render the token */

        if(!indexToTokenInfoMap.containsKey(index)) {
            if(inputTokenIndexes.isEmpty())
                inputTokenIndexes.add((Integer)index);
            else {
                for(int i=inputTokenIndexes.size()-1; i >= 0; i--) {
                    Integer n = inputTokenIndexes.get(i);
                    if(n < index) {
                        inputTokenIndexes.add(i+1, (Integer)index);
                        break;
                    }
                }
            }
        }

        /** Add the token even if it is already in the map because its position or attribute
         * may have changed
         */

        indexToTokenInfoMap.put(index, new DBInputTextTokenInfo(token, locationEvent));
    }

    public Token getCurrentToken() {
        DBInputTextTokenInfo info = indexToTokenInfoMap.get(getCurrentTokenIndex());
        if(info == null)
            return null;
        else
            return info.token;
    }

    public String renderTokensText() {
        currentTokenIndexInText = 0;
        StringBuilder text = new StringBuilder();
        for (Integer idx : inputTokenIndexes) {
            DBInputTextTokenInfo info = indexToTokenInfoMap.get(idx);
            info.setStart(text.length());
            text.append(info.getText());

            if (idx == getCurrentTokenIndex())
                currentTokenIndexInText = info.start;
        }
        return text.toString();
    }

    public void render() {
        /** Apply the text */

        String text = renderTokensText();
        textPane.setText(text);
        textPane.getStyledDocument().setCharacterAttributes(0, text.length(), SimpleAttributeSet.EMPTY, true);

        /** Apply the style for each token */
        for (Integer idx : inputTokenIndexes) {
            DBInputTextTokenInfo info = indexToTokenInfoMap.get(idx);
            AttributeSet attribute = indexToConsumeAttributeMap.get(idx);
            if (attribute == null)
                attribute = attributeNonConsumed;

            /** LT attribute override the other */
            if (lookaheadTokenIndexes.contains(idx))
                attribute = attributeLookahead;

            textPane.getStyledDocument().setCharacterAttributes(info.start, info.end, attribute, true);
        }
    }

    public void updateOnBreakEvent() {
        render();

        /** Scroll the text pane to the current token position. Invoke that later on
         * so the pane scrolls at the correct position (otherwise the scroll will be reset).
         */
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    Rectangle r = textPane.modelToView(currentTokenIndexInText);
                    if(r != null) {
                        textPane.scrollRectToVisible(r);                        
                    }
                } catch (BadLocationException e) {
                    debuggerTab.getConsole().println(e);
                }
            }
        });
    }

    public void createTextAttributes() {
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
        for (DBInputTextTokenInfo info : indexToTokenInfoMap.values()) {
            if (drawTokensBox)
                drawToken(info, (Graphics2D) g, Color.red, false);

            if (inputBreakpointIndexes.contains(Integer.valueOf(info.token.getTokenIndex())))
                drawToken(info, (Graphics2D) g, INPUT_BREAKPOINT_COLOR, true);
            else if (mouseIndex >= info.start && mouseIndex < info.end)
                drawToken(info, (Graphics2D) g, HIGHLIGHTED_COLOR, true);
        }
    }

    public void drawToken(DBInputTextTokenInfo info, Graphics2D g, Color c, boolean fill) {
        g.setColor(c);
        try {
            Rectangle r1 = textPane.modelToView(info.start);
            Rectangle r2 = textPane.modelToView(info.end);

            if(r2.y > r1.y) {
                // token spans more than one line
                GeneralPath gp = new GeneralPath();
                Area area = new Area();
                for(int index=info.start; index<info.end; index++) {
                    Rectangle r = textPane.modelToView(index);
                    // compute the width of the index
                    r.width = Math.max(0,textPane.modelToView(index+1).x - r.x);
                    area.add(new Area(r));
                }
                gp.append(area, true);
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

    public DBInputTextTokenInfo getTokenInfoAtTokenIndex(int index) {
        return indexToTokenInfoMap.get(index);
    }

    public DBInputTextTokenInfo getTokenInfoAtPositionIndex(int index) {
        for (DBInputTextTokenInfo info : indexToTokenInfoMap.values()) {
            if (index >= info.start && index < info.end)
                return info;
        }
        return null;
    }

    public boolean isBreakpointAtToken(Token token) {
        return inputBreakpointIndexes.contains(Integer.valueOf(token.getTokenIndex()));
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
        else
            highlightToken(-1);
    }

    public DBInputTextTokenInfo getTokenInfoForToken(Token t) {
        for (DBInputTextTokenInfo info : indexToTokenInfoMap.values()) {
            // FIX AW-61 - compare also the token type to avoid selecting the wrong one (e.g. imaginary)
            if (info.token.getTokenIndex() == t.getTokenIndex() &&
                    info.token.getType() == t.getType())
                return info;
        }
        return null;
    }

    public void notificationFire(Object source, String name) {
        if(name.equals(AWPrefsDialog.NOTIF_PREFS_APPLIED)) {
            createTextAttributes();
        }
    }

    protected class MyMouseListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            highlightToken(textPane.getTextIndexAtLocation(e.getPoint()));
            if(mouseIndex == -1)
                return;

            DBInputTextTokenInfo info = getTokenInfoAtPositionIndex(mouseIndex);
            if(info == null)
                return;

            boolean shiftKey = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK;
            if(e.getButton() == MouseEvent.BUTTON1 && !shiftKey) {
                debuggerTab.selectToken(info.token, info.getLocation());
            } else {
                Integer index = info.token.getTokenIndex();
                if(inputBreakpointIndexes.contains(index))
                    inputBreakpointIndexes.remove(index);
                else
                    inputBreakpointIndexes.add(index);
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
