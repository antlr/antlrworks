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

package org.antlr.works.ate.syntax.misc;

import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.swing.ATEStyledDocument;
import org.antlr.works.ate.syntax.generic.ATESyntaxEngine;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ATEColoring extends ATEThread {

    protected ATEPanel textEditor;
    protected ATESyntaxEngine engine;

    protected int colorizeOffset = -1;
    protected int colorizeLength = -1;

    protected SimpleAttributeSet standardAttr;

    protected List tokens;

    protected final Object offsetLock = new Object();

    protected boolean enable = false;

    public ATEColoring(ATEPanel textEditor) {
        this.textEditor = textEditor;

        standardAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(standardAttr, Color.black);
        StyleConstants.setBold(standardAttr, false);
        StyleConstants.setItalic(standardAttr, false);

        tokens = null;
        colorizeOffset = -1;
        colorizeLength = -1;

        start();
    }

    public void setSyntaxEngine(ATESyntaxEngine engine) {
        this.engine = engine;
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
        textEditor.ateColoringWillColorize();
        StyledDocument doc = (StyledDocument) textEditor.getTextPane().getDocument();
        doc.setCharacterAttributes(0, doc.getLength(), standardAttr, false);
        textEditor.ateColoringDidColorize();
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
        List newTokens = textEditor.getParserEngine().getTokens();

        int nt = 0;
        int ot = 0;
        for(; nt<newTokens.size(); nt++) {
            ATEToken newToken = (ATEToken)newTokens.get(nt);
            if(tokens != null && ot < tokens.size()) {
                /** The modified attribute of the token indicates that
                 * the token has been modified. It may happen that it is
                 * still the same after modification (i.e. you past the same
                 * token over itself) but in the Swing text area, the attribute
                 * may have been lost so we *must* redraw this token.
                 */
                ATEToken oldToken = (ATEToken)tokens.get(ot);
                if(oldToken.equals(newToken) && !oldToken.modified) {
                    ot++;
                } else {
                    oldToken = threadFindOldToken(ot, newToken);
                    if(oldToken == null || oldToken.modified) {
                        modifiedTokens.add(newToken);
                    } else {
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

        if(engine == null)
            return;

        textEditor.ateColoringWillColorize();
        ATEStyledDocument doc = (ATEStyledDocument) textEditor.getTextPane().getDocument();

        doc.lock();

        try {
            // Walk the list of modified tokens and apply the corresponding color to each of them
            for(int t = 0; t<modifiedTokens.size(); t++) {
                ATEToken token = (ATEToken) modifiedTokens.get(t);

                // Apply the standard attribute first
                doc.setCharacterAttributes(token.getStartIndex(), token.getEndIndex()-token.getStartIndex(), standardAttr, false);

                // Then any specific attribute if available
                AttributeSet attr = engine.getAttributeForToken(token);
                if(attr != null)
                    doc.setCharacterAttributes(token.getStartIndex(), token.getEndIndex()-token.getStartIndex(), attr, false);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        doc.unlock();

        textEditor.ateColoringDidColorize();
    }

    public void threadRun() {
        threadColorize();
    }

}
