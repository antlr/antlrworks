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

package org.antlr.works.editor.tool;

import org.antlr.works.editor.EditorThread;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.swing.EditorStyledDocument;
import org.antlr.works.parser.Lexer;
import org.antlr.works.parser.Token;
import org.antlr.works.parser.Parser;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TColorize extends EditorThread {

    private EditorWindow editor;

    private int colorizeOffset = -1;
    private int colorizeLength = -1;

    private SimpleAttributeSet commentAttr;
    private SimpleAttributeSet stringAttr;
    private SimpleAttributeSet tokenAttr;
    private SimpleAttributeSet standardAttr;
    private SimpleAttributeSet undefinedRuleAttr;

    private List tokens;

    private List offsets;
    private Object offsetLock = new Object();

    private boolean enable = true;

    public TColorize(EditorWindow editor) {
        this.editor = editor;

        commentAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(commentAttr, Color.lightGray);
        StyleConstants.setItalic(commentAttr, true);

        stringAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(stringAttr, new Color(0, 0.5f, 0));
        StyleConstants.setBold(stringAttr, true);

        tokenAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(tokenAttr, new Color(0, 0, 0.5f));
        StyleConstants.setBold(tokenAttr, true);

        undefinedRuleAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(undefinedRuleAttr, new Color(1f, 0, 0));
        StyleConstants.setItalic(undefinedRuleAttr, true);

        standardAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(standardAttr, Color.black);
        StyleConstants.setBold(standardAttr, false);
        StyleConstants.setItalic(standardAttr, false);

        tokens = null;
        offsets = new ArrayList();
        colorizeOffset = -1;
        colorizeLength = -1;

        start();
    }

    public synchronized void setEnable(boolean flag) {
        this.enable = flag;
    }

    public synchronized boolean isEnable() {
        return enable;
    }

    public synchronized void reset() {
        colorizeOffset = -1;
        colorizeLength = -1;
        tokens = null;
    }

    public synchronized void setColorizeLocation(int offset, int length) {
        colorizeOffset = offset;
        colorizeLength = length;
        adjustTokens();
        // skip any job in the thread to be executed
        // because colorize() has been called probably a while ago
        // in the parserDidComplete() method of EditorWindow
        skip();
    }

    public synchronized void colorize() {
        if(isEnable())
            awakeThread(150);
    }

    public void removeColorization() {
        editor.disableTextPane(true);
        StyledDocument doc = (StyledDocument) editor.getTextPane().getDocument();
        doc.setCharacterAttributes(0, doc.getLength(), standardAttr, false);
        editor.enableTextPane(true);
    }

    private void adjustTokens() {
        if(tokens == null)
            return;

        if(colorizeOffset == -1)
            return;

        int startLocation = colorizeLength<0?colorizeOffset-colorizeLength:colorizeOffset;
        synchronized(offsetLock) {
            offsets.add(new Offset(startLocation, colorizeLength));
        }
    }

    private void threadAdjustTokens() {
        List copyOffsets = new ArrayList();
        synchronized(offsetLock) {
            copyOffsets.addAll(offsets);
            offsets.clear();
        }
        for(int i = 0; i<copyOffsets.size(); i++) {
            Offset offset = (Offset)copyOffsets.get(i);
            threadAdjustTokens(offset.location, offset.length);
        }
    }

    private void threadAdjustTokens(int location, int length) {
        for(int t=0; t<tokens.size(); t++) {
            Token token = (Token) tokens.get(t);
            if(token.getStart() > location) {
                token.offsetPositionBy(length);
            }
        }
    }

    private Token threadFindOldToken(int offset, Token newToken) {
        for(int t = offset; t<tokens.size(); t++) {
            Token oldToken = (Token)tokens.get(t);
            if(oldToken.equals(newToken))
                return oldToken;
            else if(oldToken.getStart()>newToken.getStart())
                break;
        }
        return null;
    }

    private List threadFetchModifiedTokens() {
        List modifiedTokens = new ArrayList();
        List newTokens = editor.getTokens();

        int nt = 0;
        int ot = 0;
        for(; nt<newTokens.size(); nt++) {
            Token newToken = (Token)newTokens.get(nt);
            if(tokens != null && ot < tokens.size()) {
                Token oldToken = (Token)tokens.get(ot);
                if(oldToken.equals(newToken)) {
                    ot++;
                } else {
                    oldToken = threadFindOldToken(ot, newToken);
                    if(oldToken == null)
                        modifiedTokens.add(newToken);
                    else {
                        ot = tokens.indexOf(oldToken)+1;
                    }
                }
            } else
                modifiedTokens.add(newToken);
        }
        tokens = newTokens;
        return modifiedTokens;
    }

    private void threadColorize() {
        threadAdjustTokens();

        List modifiedTokens = threadFetchModifiedTokens();
        if(modifiedTokens.size() == 0)
            return;

        editor.disableTextPane(true);
        EditorStyledDocument doc = (EditorStyledDocument) editor.getTextPane().getDocument();
        doc.lock();

        try {
            // Note: cannot remove the attribute because it may remove hidden
            // action attribute (and the hidden action cannot be expaned anymore)
            // So we simply apply a standard attribute that will reset the style without
            // removing the hidden action (putting the second parameter to false)

            Token ta = (Token)modifiedTokens.get(0);
            Token tb = (Token)modifiedTokens.get(modifiedTokens.size()-1);

            doc.setCharacterAttributes(ta.getStart(), tb.getEnd()-ta.getStart(), standardAttr, false);

            for(int t = 0; t<modifiedTokens.size(); t++) {
                Token token = (Token) modifiedTokens.get(t);
                switch(token.type) {
                    case Lexer.TOKEN_COMPLEX_COMMENT:
                    case Lexer.TOKEN_SINGLE_COMMENT:
                        doc.setCharacterAttributes(token.getStart(), token.getEnd()-token.getStart(), commentAttr, false);
                        break;
                    case Lexer.TOKEN_DOUBLE_QUOTE_STRING:
                    case Lexer.TOKEN_SINGLE_QUOTE_STRING:
                        doc.setCharacterAttributes(token.getStart(), token.getEnd()-token.getStart(), stringAttr, false);
                        break;
                    case Lexer.TOKEN_ID:
                        if(token.isAllUpperCase())
                            // Lexer rule
                            doc.setCharacterAttributes(token.getStart(), token.getEnd()-token.getStart(), tokenAttr, false);
                        else {
                            // Figure out if the rule is already defined. If not, display the undefined rule in red
//                            if(editor.rules.isRuleAtIndex(token.getStart()) && !editor.rules.isRuleName(token.getAttribute()))
//                                doc.setCharacterAttributes(token.getStart(), token.getEnd()-token.getStart(), undefinedRuleAttr, false);
                        }
                        break;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        doc.unlock();
        editor.enableTextPane(true);
    }

    public void threadRun() {
        threadColorize();
    }

    private class Offset {
        public int location;
        public int length;

        public Offset(int location, int length) {
            this.location = location;
            this.length = length;
        }
    }
}
