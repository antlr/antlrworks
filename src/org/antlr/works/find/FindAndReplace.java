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


package org.antlr.works.find;

import org.antlr.works.dialog.FindAndReplaceDialog;
import org.antlr.xjlib.appkit.frame.XJFrame;
import org.antlr.xjlib.appkit.frame.XJFrameDelegate;
import org.antlr.xjlib.appkit.utils.XJAlert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindAndReplace implements XJFrameDelegate {

    public static final String BEGIN_QUOTE = "\\Q";
    public static final String END_QUOTE = "\\E";

    public FindAndReplaceDelegate delegate;
    public String findString;
    public String replaceString;
    public int flags;
    public String prefix = "";
    public String suffix = "";
    public String prefixRegex = BEGIN_QUOTE;
    public String suffixRegex = END_QUOTE;

    public FindAndReplaceDialog dialog;

    public FindAndReplace(FindAndReplaceDelegate delegate) {
        this.delegate = delegate;
    }

    public void find() {
        display();
    }

    public String getCompilableString() {
        return prefix+prefixRegex+findString+suffixRegex+suffix;
    }

    public Pattern getCompiledPattern() {
        Pattern p = null;
        try {
            p = Pattern.compile(getCompilableString(), flags);
        } catch(Exception e) {
            XJAlert.display(dialog.getJavaContainer(), "Regex Find", "Pattern error:\n"+e.toString());
        }
        return p;
    }

    public void setPositionToTop() {
        delegate.getTextPane().setSelectionStart(0);
        delegate.getTextPane().setSelectionEnd(0);
    }

    public void setPositionToBottom() {
        delegate.getTextPane().setSelectionStart(delegate.getText().length()-1);
        delegate.getTextPane().setSelectionEnd(delegate.getText().length()-1);
    }

    public boolean matching() {
        Pattern p = getCompiledPattern();
        if(p == null)
            return false;

        String text = delegate.getText();
        Matcher m = p.matcher(text);
        if(m.find(0)) {
            delegate.getTextEditor().selectTextRange(m.start(), m.end());
            return true;
        } else {
            return false;
        }
    }

    public boolean next() {
        if(findString == null || findString.length() == 0)
            return false;

        int position = delegate.getTextPane().getSelectionEnd();
        String text = delegate.getText();

        Pattern p = getCompiledPattern();
        if(p == null)
            return false;

        Matcher m = p.matcher(text);
        if(m.find(position)) {
            delegate.getTextEditor().selectTextRange(m.start(), m.end());
            return true;
        } else {
            return false;
        }
    }

    public boolean prev() {
        if(findString == null || findString.length() == 0)
            return false;

        int position = delegate.getTextPane().getSelectionStart();
        String text = delegate.getText();

        Pattern p = getCompiledPattern();
        if(p == null)
            return false;

        Matcher m = p.matcher(text.substring(0, position));
        int matchStart = 0;
        int matchEnd = 0;
        boolean matched = false;
        while(m.find(matchEnd)) {
            matchStart = m.start();
            matchEnd = m.end();
            matched = true;
        }
        if(matched) {
            delegate.getTextEditor().selectTextRange(matchStart, matchEnd);
            return true;
        } else {
            return false;
        }
    }

    public void replace() {
        String t = delegate.getTextEditor().getSelectedText();
        if(t != null && t.length() > 0) {
            delegate.getTextEditor().replaceSelectedText(replaceString);
        }
    }

    public void replaceAll() {
        Pattern p = getCompiledPattern();
        if(p == null)
            return;

        Matcher m = p.matcher(delegate.getText());
        String s = m.replaceAll(replaceString);

        int oldCursorPosition = delegate.getTextEditor().getCaretPosition();
        delegate.setText(s);
        delegate.getTextEditor().setCaretPosition(oldCursorPosition, false, false);
    }

    public void display() {
        if(dialog == null) {
            dialog = new FindAndReplaceDialog(this);
        }
        dialog.setDelegate(this);
        dialog.setFindText(delegate.getTextEditor().getSelectedText());
        dialog.show();
    }

    public void setFindString(String string) {
        this.findString = string;
    }

    public void setReplaceString(String string) {
        this.replaceString = string;
    }

    public void setIgnoreCase(boolean ignore) {
        if(ignore)
            flags = Pattern.CASE_INSENSITIVE;
        else
            flags = 0;
    }

    public void setRegex(boolean flag) {
        if(flag) {
            prefixRegex = "";
            suffixRegex = "";
        } else {
            prefixRegex = BEGIN_QUOTE;
            suffixRegex = END_QUOTE;
        }
    }

    public void setOptions(int options) {
        prefix = "";
        suffix = "";
        switch(options) {
            case 0: // contains
                break;
            case 1: // starts with
                prefix = "\\b";
                break;
            case 2: // whole word
                prefix = "\\b";
                suffix = "\\b";
                break;
            case 3: // ends with
                suffix = "\\b";
                break;
        }
    }

    public void frameDidClose(XJFrame frame) {
        dialog = null;
    }

}
