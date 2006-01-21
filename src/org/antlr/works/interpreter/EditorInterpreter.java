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

package org.antlr.works.interpreter;

import edu.usfca.xj.appkit.gview.GView;
import edu.usfca.xj.appkit.utils.XJDialogProgress;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.ParseTree;
import org.antlr.tool.ErrorManager;
import org.antlr.tool.Grammar;
import org.antlr.tool.Interpreter;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.parsetree.ParseTreePanel;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.Statistics;
import org.antlr.works.syntax.GrammarSyntaxRule;
import org.antlr.works.utils.ErrorListener;
import org.antlr.works.utils.IconManager;
import org.antlr.works.utils.TextUtils;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

public class EditorInterpreter implements Runnable, EditorTab {

    protected JPanel panel;
    protected JSplitPane splitPane;
    protected JTextPane textPane;
    protected JScrollPane textScrollPane;
    protected EditorInterpreterTreeModel treeModel;
    protected ParseTreePanel parseTreePanel;
    protected JComboBox rulesCombo;

    protected XJDialogProgress progress;

    protected String startSymbol = null;

    protected CEditorGrammar editor;

    public EditorInterpreter(CEditorGrammar editor) {
        this.editor = editor;
    }

    public void awake() {
        progress = new XJDialogProgress(editor.getJFrame());

        panel = new JPanel(new BorderLayout());

        textPane = new JTextPane();
        textPane.setBackground(Color.white);
        textPane.setBorder(null);
        textPane.setPreferredSize(new Dimension(300, 100));

        textPane.setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        TextUtils.createTabs(textPane);

        textScrollPane = new JScrollPane(textPane);
        textScrollPane.setWheelScrollingEnabled(true);

        treeModel = new EditorInterpreterTreeModel();
        parseTreePanel = new ParseTreePanel(treeModel);

        splitPane = new JSplitPane();
        splitPane.setBorder(null);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(textScrollPane);
        splitPane.setRightComponent(parseTreePanel);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(200);

        panel.add(createControlPanel(), BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        editor.getXJFrame().registerUndo(null, textPane);
    }

    public Box createControlPanel() {
        Box box = Box.createHorizontalBox();
        box.add(createRunButton());
        box.add(createRulesPopUp());
        box.add(Box.createHorizontalGlue());
        return box;
    }

    public JButton createRunButton() {
        JButton button = new JButton(IconManager.shared().getIconRun());
        button.setToolTipText("Run");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Statistics.shared().recordEvent(Statistics.EVENT_INTERPRETER_BUTTON);
                interpret();
            }
        });
        return button;
    }

    public JComboBox createRulesPopUp() {
        rulesCombo = new JComboBox();
        rulesCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                GrammarSyntaxRule rule = (GrammarSyntaxRule)rulesCombo.getSelectedItem();
                if(rule != null)
                    startSymbol = rule.name;
            }
        });
        return rulesCombo;
    }

    public Container getContainer() {
        return panel;
    }

    public void setRules(List rules) {
        rulesCombo.removeAllItems();
        for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
            rulesCombo.addItem(iterator.next());
        }
    }

    public void interpret() {
        editor.console.makeCurrent();

        if(editor.getGrammar().isDirty()) {
            progress.setInfo("Preparing...");
        } else {
            progress.setInfo("Interpreting...");
        }

        progress.setCancellable(false);
        progress.setIndeterminate(true);
        progress.display();

        new Thread(this).start();
    }

    public void run() {

        ErrorManager.setErrorListener(ErrorListener.shared());

        editor.getGrammar().createGrammars();
        editor.getGrammar().analyze();

        process();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progress.close();
            }
        });
    }

    protected void process() {
        progress.setInfo("Interpreting...");

        CharStream input = new ANTLRStringStream(textPane.getText());

        Grammar parser = editor.getGrammar().getParserGrammar();
        Grammar lexer = editor.getGrammar().getLexerGrammar();

        Interpreter lexEngine = new Interpreter(lexer, input);
        CommonTokenStream tokens = new CommonTokenStream(lexEngine);
        // @todo GUI for that
        tokens.setTokenTypeChannel(lexer.getTokenType("WS"), 99);
        tokens.setTokenTypeChannel(lexer.getTokenType("SL_COMMENT"), 99);
        tokens.setTokenTypeChannel(lexer.getTokenType("ML_COMMENT"), 99);

        Interpreter parseEngine = new Interpreter(parser, tokens);

        ParseTree t = null;
        try {
            if(ATEToken.isLexerName(startSymbol)) {
                t = lexEngine.parse(startSymbol);
            } else {
                t = parseEngine.parse(startSymbol);
            }
        } catch (Exception e) {
            editor.console.print(e);
        }

        if(parser != null && t != null) {
            treeModel.setGrammar(parser);
            treeModel.setTree(t);

            parseTreePanel.setRoot((TreeNode)treeModel.getRoot());
        }
    }

    public boolean hasExportableGView() {
        return true;
    }

    public GView getExportableGView() {
        return parseTreePanel.getGraphView();
    }

    public String getTabName() {
        return "Interpreter";
    }

    public Component getTabComponent() {
        return getContainer();
    }

}
