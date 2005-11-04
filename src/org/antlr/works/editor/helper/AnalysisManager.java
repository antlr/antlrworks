package org.antlr.works.editor.helper;

import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.ate.ATEAnalysisItem;
import org.antlr.works.editor.ate.ATEAnalysisManager;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.parser.Token;
import org.antlr.works.parser.ParserReference;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
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

public class AnalysisManager extends ATEAnalysisManager {

    protected static final int ANALYSIS_ITEM_ERROR = 0;
    protected static final int ANALYSIS_ITEM_WARNING = 1;
    protected static final int ANALYSIS_ITEM_REFACTOR = 2;

    protected final Color greenColor = new Color(0f, 0.9f, 0.25f, 1.0f);

    protected EditorWindow editor;

    protected int numberOfErrors;
    protected int numberOfWarnings;
    protected int numberOfRefactorings;

    public AnalysisManager(EditorWindow editor) {
        this.editor = editor;
    }

    public int[] getAvailableTypes() {
        return new int[] { ANALYSIS_ITEM_ERROR, ANALYSIS_ITEM_WARNING, ANALYSIS_ITEM_REFACTOR };
    }

    public List getItemsForType(int type) {
        switch(type) {
            case ANALYSIS_ITEM_ERROR:
                return getErrors();
            case ANALYSIS_ITEM_WARNING:
                return getWarnings();
            case ANALYSIS_ITEM_REFACTOR:
                return getRefactors();
        }
        return null;
    }

    public Color getItemColorForType(int type) {
        switch(type) {
            case ANALYSIS_ITEM_ERROR:
                return Color.red;
            case ANALYSIS_ITEM_WARNING:
                return Color.blue;
            case ANALYSIS_ITEM_REFACTOR:
                return Color.green;
        }
        return null;
    }

    public int getLinesCount() {
        return editor.parser.getMaxLines();
    }

    public Color getAnalysisColor() {
        if(numberOfErrors == 0 && numberOfWarnings == 0 && numberOfRefactorings == 0)
            return greenColor;
        else if(numberOfErrors == 0)
            return Color.yellow;
        else
            return Color.red;
    }

    public String getAnalysisDescription() {
        StringBuffer sb = new StringBuffer();
        if(numberOfErrors == 0)
            sb.append("No errors");
        else {
            sb.append(numberOfErrors);
            sb.append(" errors found");
        }
        if(numberOfWarnings > 0) {
            sb.append("\n");
            sb.append(numberOfWarnings);
            sb.append(" warnings found");
        }
        if(numberOfRefactorings > 0) {
            sb.append("\n");
            sb.append(numberOfRefactorings);
            sb.append(" refactoring suggestions found");
        }
        return sb.toString();
    }

    public void refresh() {
        numberOfErrors = getErrors().size();
        numberOfWarnings = getWarnings().size();
        numberOfRefactorings = getRefactors().size();
    }

    public List getErrors() {
        List errors = new ArrayList();
        for(Iterator iter = editor.rules.getUndefinedReferences().iterator(); iter.hasNext(); ) {
            ParserReference ref = (ParserReference)iter.next();
            errors.add(new ATEAnalysisItem(ANALYSIS_ITEM_ERROR, ref.token.line, ref.token.getStartIndex(), "Undefined reference \""+ref.token.getAttribute()+"\""));
        }
        return errors;
    }

    public List getWarnings() {
        List warnings = new ArrayList();
        for(Iterator iter = editor.rules.getDuplicateRules().iterator(); iter.hasNext(); ) {
            ParserRule rule = (ParserRule) iter.next();
            warnings.add(new ATEAnalysisItem(ANALYSIS_ITEM_WARNING, rule.start.line, rule.getStartIndex(), "Duplicate rule \""+rule.name+"\""));
        }
        return warnings;
    }

    public List getRefactors() {
        List refactors = new ArrayList();
        for(Iterator iter = editor.rules.getHasLeftRecursionRules().iterator(); iter.hasNext(); ) {
            ParserRule rule = (ParserRule) iter.next();
            refactors.add(new ATEAnalysisItem(ANALYSIS_ITEM_REFACTOR, rule.start.line, rule.getStartIndex(), "Left recursion in rule \""+rule.name+"\""));
        }
        return refactors;
    }

}
