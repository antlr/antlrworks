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

package org.antlr.works.ate.syntax;

import org.antlr.works.ate.ATEStyledDocument;
import org.antlr.works.components.grammar.CEditorGrammar;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ATEColoring extends ATEThread {

    private CEditorGrammar editor;

    private int colorizeOffset = -1;
    private int colorizeLength = -1;

    private SimpleAttributeSet commentAttr;
    private SimpleAttributeSet stringAttr;
    private SimpleAttributeSet parserRefAttr;
    private SimpleAttributeSet lexerRefAttr;
    private SimpleAttributeSet labelAttr;
    private SimpleAttributeSet standardAttr;

    private List tokens;

    private List offsets;
    private final Object offsetLock = new Object();

    private boolean enable = false;

    public ATEColoring(CEditorGrammar editor) {
        super(editor.console);

        this.editor = editor;

        commentAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(commentAttr, Color.lightGray);
        StyleConstants.setItalic(commentAttr, true);

        stringAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(stringAttr, new Color(0, 0.5f, 0));
        StyleConstants.setBold(stringAttr, true);

        parserRefAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(parserRefAttr, new Color(0.42f, 0, 0.42f));
        StyleConstants.setBold(parserRefAttr, true);

        lexerRefAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(lexerRefAttr, new Color(0, 0, 0.5f));
        StyleConstants.setBold(lexerRefAttr, true);

        labelAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(labelAttr, Color.black);
        StyleConstants.setItalic(labelAttr, true);

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
        // in the ateParserDidParse() method of EditorWindow
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

    private ATEToken threadFindOldToken(int offset, ATEToken newToken) {
        for(int t = offset; t<tokens.size(); t++) {
            ATEToken oldToken = (ATEToken)tokens.get(t);
            if(oldToken.equals(newToken))
                return oldToken;
            else if(oldToken.getStartIndex()>newToken.getStartIndex())
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
            ATEToken newToken = (ATEToken)newTokens.get(nt);
            if(tokens != null && ot < tokens.size()) {
                ATEToken oldToken = (ATEToken)tokens.get(ot);
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
        List modifiedTokens = threadFetchModifiedTokens();
        if(modifiedTokens.size() == 0)
            return;

        editor.disableTextPane(true);
        ATEStyledDocument doc = (ATEStyledDocument) editor.getTextPane().getDocument();
        doc.lock();

        try {
            // Walk the list of modified tokens and apply the corresponding color
            // for each of them
            for(int t = 0; t<modifiedTokens.size(); t++) {
                ATEToken token = (ATEToken) modifiedTokens.get(t);
                AttributeSet attr = null;
                switch(token.type) {
                    case ATEGenericLexer.TOKEN_COMPLEX_COMMENT:
                    case ATEGenericLexer.TOKEN_SINGLE_COMMENT:
                        attr = commentAttr;
                        break;
                    case ATEGenericLexer.TOKEN_DOUBLE_QUOTE_STRING:
                    case ATEGenericLexer.TOKEN_SINGLE_QUOTE_STRING:
                        attr = stringAttr;
                        break;
                    case ATEGenericLexer.TOKEN_REFERENCE:
                    case ATEGenericLexer.TOKEN_RULE:
                        if(token.lexer)
                            attr = lexerRefAttr;
                        else
                            attr = parserRefAttr;
                        break;
                    case ATEGenericLexer.TOKEN_LABEL:
                        attr = labelAttr;
                        break;
                }

                // Apply the standard attribute first
                doc.setCharacterAttributes(token.getStartIndex(), token.getEndIndex()-token.getStartIndex(), standardAttr, false);

                // Then any specific attribute if available
                if(attr != null)
                    doc.setCharacterAttributes(token.getStartIndex(), token.getEndIndex()-token.getStartIndex(), attr, false);
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
