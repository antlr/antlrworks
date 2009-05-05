package org.antlr.works.stringtemplate;

import org.antlr.xjlib.appkit.swing.XJTableView;
import org.antlr.works.stringtemplate.element.ElementTemplateRule;
import org.antlr.works.stringtemplate.syntax.ATEStringTemplateSyntaxParser;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;/*

[The "BSD licence"]
Copyright (c) 2009 Jean Bovet
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

public class STRulePanel extends JPanel {

    protected ComponentEditorStringTemplate editor;

    private STRuleModel model;
    private final XJTableView tableView = new XJTableView();

    public STRulePanel(ComponentEditorStringTemplate editor) {
        this.editor = editor;

        tableView.setAlternateBackground(true);

        refreshRules();

        setLayout(new BorderLayout());
        tableView.autoresizeColumns();
        add(tableView, BorderLayout.CENTER);
    }

    public void refreshRules() {
        model = new STRuleModel();
        List<ElementTemplateRule> rules = ((ATEStringTemplateSyntaxParser)editor.getTextEditor().getParserEngine().getParser()).templateRules;
        for (ElementTemplateRule rule : rules) {
            model.addRule(rule.name);
        }
        tableView.getTable().setModel(model);
        tableView.autoresizeColumns();
    }

    private static class STRuleModel extends AbstractTableModel {

        private final List<String> rules = new ArrayList<String>();

        public void addRule(String rule) {
            rules.add(rule);
            final int index = rules.size()-1;
            fireTableRowsInserted(index, index);
        }

        public int getRowCount() {
            return rules.size();
        }

        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return "Rules";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return rules.get(rowIndex);
        }
    }
}
