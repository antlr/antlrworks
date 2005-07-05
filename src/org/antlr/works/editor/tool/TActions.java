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

import org.antlr.works.editor.rules.Rules;
import org.antlr.works.editor.swing.MultiLineToolTip;
import org.antlr.works.parser.Parser;
import org.antlr.works.parser.ThreadedParser;
import org.antlr.works.parser.Token;
import org.antlr.works.util.IconManager;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

public class TActions implements ActionListener {

    private Rules rules = null;
    private ThreadedParser parser = null;

    private JTextPane textPane;

    public TActions(ThreadedParser parser, JTextPane textPane) {
        this.parser = parser;
        this.textPane = textPane;
    }

    public void setRules(Rules rules) {
        this.rules = rules;
    }

    public String getPlainText() {
        return getPlainText(0, Integer.MAX_VALUE);
    }

    public String getPlainText(int start, int end) {
        // Get the plain text content by inserting each hidden action text.

        StringBuffer plainText;
        if(end == Integer.MAX_VALUE)
            plainText = new StringBuffer(textPane.getText().substring(start));
        else
            plainText = new StringBuffer(textPane.getText().substring(start, end));

        int offset = 0;
        ElementIterator iter = new ElementIterator(textPane.getStyledDocument());
        for(Element elem = iter.first(); elem != null; elem = iter.next()) {
            if(!elem.isLeaf())
                continue;

            if(elem.getStartOffset()>end || elem.getEndOffset()<start)
                continue;

            Component c = StyleConstants.getComponent(elem.getAttributes());
            if(c instanceof JButton) {
                JButton b = (JButton)c;
                int elemStart = elem.getStartOffset()+offset;
                int elemEnd = elem.getEndOffset()+offset;
                String t = b.getToolTipText();
                plainText.replace(elemStart-start, elemEnd-start, t);

                // Because we are starting from the beginning of the text, we must store
                // the current offset in the StringBuffer after each new text insertion.
                // We substract 1 to the length because the JButton is represented with one
                // blank space that has been replaced.
                offset += t.length()-1;
            }
        }

        return plainText.toString();
    }

    public Token getBlockAtPosition(int pos) {
        Parser.Rule rule = rules.getRuleAtPosition(pos);
        if(rule == null)
            return null;

        List blocks = rule.getBlocks();
        Iterator iterator = blocks.iterator();
        while(iterator.hasNext()) {
            Token block = (Token)iterator.next();
            if(pos >= block.getStart() && pos <= block.getEnd())
                return block;
        }
        return null;
    }

    public void hideAction() {
        int position = textPane.getCaretPosition();
        Token block = getBlockAtPosition(position);
        if(block == null)
            return;
        hideBlock(block);
    }

    public void hideBlock(Token block) {
        try {
            textPane.getDocument().remove(block.getStart(), block.getEnd()-block.getStart());
            textPane.getDocument().insertString(block.getStart(), " ", createButtonStyle(block.getAttribute()));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void hideAllActions() {
        rules.setSkipParseRules(true);
        List rules = parser.getRules();
        for(int r=rules.size()-1; r>=0; r--) {
            Parser.Rule rule = (Parser.Rule)rules.get(r);
            List blocks = rule.getBlocks();
            for(int b=blocks.size()-1; b>=0; b--) {
                Token block = (Token)blocks.get(b);
                hideBlock(block);
            }
        }
        this.rules.setSkipParseRules(false);
        this.rules.parseRules();
    }

    public void showAction(JButton button) {
        ElementIterator iter = new ElementIterator(textPane.getDocument());
        for(Element elem = iter.first(); elem != null; elem = iter.next()) {
            if(!elem.isLeaf())
                continue;

            Component c = StyleConstants.getComponent(elem.getAttributes());
            if(c != button)
                continue;

            Document doc = textPane.getDocument();
            try {
                doc.remove(elem.getStartOffset(), elem.getEndOffset()-elem.getStartOffset());
                doc.insertString(elem.getStartOffset(), button.getToolTipText(), null);
                textPane.setCaretPosition(elem.getStartOffset());
                textPaneRequestFocusLater();
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void showAllActions() {
        rules.setSkipParseRules(true);

        ElementIterator iter = new ElementIterator(textPane.getDocument());
        for(Element elem = iter.first(); elem != null; elem = iter.next()) {
            if(!elem.isLeaf())
                continue;

            Component c = StyleConstants.getComponent(elem.getAttributes());
            if(!(c instanceof JButton))
                continue;

            JButton button = (JButton)c;
            Document doc = textPane.getDocument();
            try {
                doc.remove(elem.getStartOffset(), elem.getEndOffset()-elem.getStartOffset());
                doc.insertString(elem.getStartOffset(), button.getToolTipText(), null);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
        textPaneRequestFocusLater();
        rules.setSkipParseRules(false);
        rules.parseRules();
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("button")) {
            showAction((JButton)e.getSource());
        }
    }

    public void textPaneRequestFocusLater() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textPane.requestFocus();
            }
        });
    }

    public Style createButtonStyle(String tooltip) {
        StyledDocument doc = textPane.getStyledDocument();
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);
                  
        Style regular = doc.addStyle("regular", def);
        Style styleButton = doc.addStyle("button", regular);

        JButton button = new JButton() {
            public JToolTip createToolTip() {
                return new MultiLineToolTip();
            }
        };

        button.setCursor(Cursor.getDefaultCursor());
        button.setIcon(IconManager.shared().getIconHiddenAction());
        button.setToolTipText(tooltip);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setMargin(new Insets(0,0,0,0));
        button.setActionCommand("button");
        button.addActionListener(this);
        button.setAlignmentY(1);
        StyleConstants.setComponent(styleButton, button);

        return styleButton;
    }

}
