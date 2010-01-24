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

package org.antlr.works.generate;

import org.antlr.works.ate.ATEPanel;
import org.antlr.works.ate.syntax.java.ATEJavaSyntaxEngine;
import org.antlr.works.editor.GrammarWindowTab;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.xjlib.appkit.document.XJFileMonitor;
import org.antlr.xjlib.appkit.frame.XJFrame;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.foundation.XJUtils;

import java.awt.*;
import java.io.File;

public class CodeDisplay extends GrammarWindowTab {

    private ATEPanel textEditor;
    private String rule;
    private String file;
    private final XJFileMonitor monitor = new XJFileMonitor();

    public CodeDisplay(XJFrame parentFrame) {
        super(null);
        textEditor = new ATEPanel(parentFrame);
        textEditor.setParserEngine(new ATEJavaSyntaxEngine());
        textEditor.setSyntaxColoring(true);
        textEditor.setAnalysisColumnVisible(false);
        textEditor.setFoldingEnabled(AWPrefs.getFoldingEnabled());
        textEditor.setLineNumberEnabled(AWPrefs.getLineNumberEnabled());
        textEditor.setHighlightCursorLine(AWPrefs.getHighlightCursorEnabled());
        textEditor.setEditable(false);
        applyFont();
    }

    public void applyFont() {
        textEditor.getTextPane().setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        textEditor.getTextPane().setTabSize(AWPrefs.getEditorTabSize());
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public void editorActivated() {
        if(file != null && monitor.isModifiedOnDisk(file) && new File(file).exists()) {
            load();
        }
    }

    public void load() {
        String text;
        try {
            text = XJUtils.getStringFromFile(file);
        } catch (Exception e) {
            XJAlert.display(null, "Error", "Exception while reading the generated file:\n"+e.toString());
            return;
        }

        if(rule != null) {
            int startIndex = text.indexOf("$ANTLR start \""+rule+"\"");
            startIndex = text.indexOf("\n", startIndex)+1;
            int stopIndex = text.indexOf("$ANTLR end \""+rule+"\"");
            while(stopIndex>0 && text.charAt(stopIndex) != '\n')
                stopIndex--;

            if(startIndex >= 0 && stopIndex >= 0) {
                text = text.substring(startIndex, stopIndex);
            } else {
                XJAlert.display(null, "Error", "Cannot find markers for rule \""+rule+"\"");
                return;
            }
        }

        monitor.synchronizeLastModifiedDate(file);
        textEditor.loadText(text);
    }

    public String getTabName() {
        String name = XJUtils.getLastPathComponent(file);
        if(rule != null) {
            return rule + " [" + name + "]";
        } else {
            return name;
        }
    }

    public Component getTabComponent() {
        return textEditor;
    }
}
