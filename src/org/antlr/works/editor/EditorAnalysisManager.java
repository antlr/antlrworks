package org.antlr.works.editor;

import org.antlr.works.ate.analysis.ATEAnalysisItem;
import org.antlr.works.ate.analysis.ATEAnalysisManager;
import org.antlr.works.components.GrammarWindow;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
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

public class EditorAnalysisManager extends ATEAnalysisManager {

    protected static final int ANALYSIS_ITEM_ERROR = 0;
    protected static final int ANALYSIS_ITEM_WARNING = 1;
    protected static final int ANALYSIS_ITEM_OTHER = 2;

    protected final Color greenColor = new Color(0f, 0.9f, 0.25f, 1.0f);

    protected GrammarWindow window;

    protected int numberOfErrors;
    protected int numberOfWarnings;

    public EditorAnalysisManager(GrammarWindow window) {
        this.window = window;
    }

    public void close() {
        window = null;
    }

    public int[] getAvailableTypes() {
        return new int[] { ANALYSIS_ITEM_WARNING, ANALYSIS_ITEM_ERROR, ANALYSIS_ITEM_OTHER };
    }

    public List<ATEAnalysisItem> getItemsForType(int type) {
        switch(type) {
            case ANALYSIS_ITEM_ERROR:
                return getErrors();
            case ANALYSIS_ITEM_WARNING:
                return getWarnings();
            case ANALYSIS_ITEM_OTHER:
                return getOthers();
        }
        return null;
    }

    public int getLinesCount() {
        return window.getGrammarEngine().getNumberOfLines();
    }

    public Color getAnalysisColor() {
        if(numberOfErrors == 0 && numberOfWarnings == 0)
            return greenColor;
        else if(numberOfErrors == 0)
            return Color.yellow;
        else
            return Color.red;
    }

    public String getAnalysisDescription() {
        StringBuilder sb = new StringBuilder();
        if(numberOfErrors == 0)
            sb.append("No error");
        else {
            sb.append(numberOfErrors);
            if(numberOfErrors > 1)
                sb.append(" errors found");
            else
                sb.append(" error found");
        }
        if(numberOfWarnings > 0) {
            sb.append("\n");
            sb.append(numberOfWarnings);
            if(numberOfWarnings > 1)
                sb.append(" warnings found");
            else
                sb.append(" warning found");
        }
        return sb.toString();
    }

    public List<ATEAnalysisItem> getErrors() {
        List<ATEAnalysisItem> errors = new ArrayList<ATEAnalysisItem>();
        for (EditorInspectorItem item : window.editorInspector.getErrors()) {
            errors.add(new ATEAnalysisItem(ANALYSIS_ITEM_ERROR, item.color, item.startLineNumber, item.startIndex, item.description));
        }
        numberOfErrors = errors.size();
        return errors;
    }

    public List<ATEAnalysisItem> getWarnings() {
        List<ATEAnalysisItem> warnings = new ArrayList<ATEAnalysisItem>();
        for (EditorInspectorItem item : window.editorInspector.getWarnings()) {
            warnings.add(new ATEAnalysisItem(ANALYSIS_ITEM_WARNING, item.color, item.startLineNumber, item.startIndex, item.description));
        }
        numberOfWarnings = warnings.size();
        return warnings;
    }

    public List<ATEAnalysisItem> getOthers() {
        List<ATEAnalysisItem> others = new ArrayList<ATEAnalysisItem>();
        for (EditorInspectorItem item : window.editorInspector.getDecisionDFAs()) {
            others.add(new ATEAnalysisItem(ANALYSIS_ITEM_OTHER, item.color, item.startLineNumber, item.startIndex, item.description));
        }
        return others;
    }

}
