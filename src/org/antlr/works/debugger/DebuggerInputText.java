package org.antlr.works.debugger;

import org.antlr.runtime.Token;
import org.antlr.works.debugger.DebuggerInputText.TokenInfo;
import org.antlr.works.editor.swing.TextEditorPane;
import org.antlr.works.editor.swing.TextEditorPaneDelegate;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

public class DebuggerInputText implements TextEditorPaneDelegate {

    public static final boolean USE_PERSISTENCE = true;

    public static final int TOKEN_NORMAL = 1;
    public static final int TOKEN_HIDDEN = 2;
    public static final int TOKEN_DEAD = 3;

    protected TextEditorPane textPane;
    // Location where the next token will be inserted
    protected int cursorIndex;
    protected Map tokens;

    protected boolean persistence;
    // Length of the persistent text (for which we have tokens)
    protected int persistenceTextLength;

    protected SimpleAttributeSet attributeNonConsumed;
    protected SimpleAttributeSet attributeConsume;
    protected SimpleAttributeSet attributeConsumeHidden;
    protected SimpleAttributeSet attributeConsumeDead;
    protected SimpleAttributeSet attributeLookahead;

    protected boolean drawTokensBox;

    public DebuggerInputText(TextEditorPane textPane) {
        tokens = new HashMap();
        drawTokensBox = false;
        this.textPane = textPane;
        this.textPane.setDelegate(this);
        reset();
        createTextAttributes();
    }

    public void setDrawTokensBox(boolean flag) {
        drawTokensBox = flag;
    }

    public boolean isDrawTokensBox() {
        return drawTokensBox;
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

    public void reset() {
        textPane.setText("");
        textPane.setCharacterAttributes(SimpleAttributeSet.EMPTY, true);
        cursorIndex = 0;

        tokens.clear();

        persistence = true;
        persistenceTextLength = 0;
    }

    public void rewindAll() {
        if(USE_PERSISTENCE && persistence) {
            textPane.selectAll();
            textPane.setCharacterAttributes(attributeNonConsumed, true);
        } else {
            textPane.setText("");
        }
        cursorIndex = 0;
    }

    public void rewind(int start) {
        if(USE_PERSISTENCE && persistence) {
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
        if(tokens.get(new Integer(index)) != null)
            return;

        tokens.put(new Integer(index), new TokenInfo(token, persistenceTextLength));
        persistenceTextLength += token.getText().length();
    }

    private void addText(Token token, AttributeSet attribute) {
        String text = token.getText();
        if(USE_PERSISTENCE && persistence) {
            TokenInfo info = (TokenInfo)tokens.get(new Integer(token.getTokenIndex()));
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
            e.printStackTrace();
        }
    }

    private void removeText(int start, int end) {
        try {
            textPane.getDocument().remove(start, end-start);
            cursorIndex = getTextLength();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void createTextAttributes() {
        attributeNonConsumed = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeNonConsumed, Color.lightGray);

        attributeConsume = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeConsume, Color.black);

        attributeConsumeHidden = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeConsumeHidden, Color.lightGray);

        attributeConsumeDead = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeConsumeDead, Color.red);

        attributeLookahead = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeLookahead, Color.blue);
        StyleConstants.setItalic(attributeLookahead, true);
    }

    public void paintTextEditorPane(Graphics g) {
        if(!drawTokensBox)
            return;

        g.setColor(Color.red);

        for (Iterator iterator = tokens.values().iterator(); iterator.hasNext();) {
            TokenInfo info = (TokenInfo) iterator.next();

            try {
                Rectangle r1 = textPane.modelToView(info.start);
                Rectangle r2 = textPane.modelToView(info.end);

                if(r2.y > r1.y) {
                    // Token is displayed on more than one line
                    // (currently only handle two lines wrapping ;-))

                    // Draw the first line
                    int index = info.start+1;
                    while(textPane.modelToView(index).y == r1.y) {
                        index++;
                    }
                    Rectangle r11 = textPane.modelToView(index-1);
                    g.drawRect(r1.x, r1.y, r11.x-r1.x, r1.height);

                    // Then the second one
                    Rectangle r20 = textPane.modelToView(index);
                    g.drawRect(r20.x, r20.y, r2.x-r20.x, r20.height);                    
                } else
                    g.drawRect(r1.x, r1.y, r2.x-r1.x, r1.height);
            } catch (BadLocationException e) {
            }
        }
    }

    private class TokenInfo {

        public Token token;
        public int start;
        public int end;

        public TokenInfo(Token token, int start) {
            this.token = token;
            this.start = start;
            this.end = start+token.getText().length();
        }
    }
}
