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

import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.utils.XJDialogProgress;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.ParseTree;
import org.antlr.tool.ErrorManager;
import org.antlr.tool.Grammar;
import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.swing.TextUtils;
import org.antlr.works.editor.undo.Undo;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.parser.Token;
import org.antlr.works.parsetree.ParseTreePanel;
import org.antlr.works.stats.Statistics;
import org.antlr.works.util.ErrorListener;
import org.antlr.works.util.IconManager;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

public class Interpreter implements Runnable, EditorTab {

    protected JPanel panel;
    protected JSplitPane splitPane;
    protected JTextPane textPane;
    protected JScrollPane textScrollPane;
    protected InterpreterTreeModel treeModel;
    protected ParseTreePanel parseTreePanel;
    protected JComboBox rulesCombo;

    protected XJDialogProgress progress;

    protected boolean grammarDirty = true;

    protected String startSymbol = null;

    protected Grammar parser;
    protected Grammar lexer;

    protected EditorWindow editor;

    public Interpreter(EditorWindow editor) {
        this.editor = editor;
    }

    public void awake() {
        progress = new XJDialogProgress(editor);

        panel = new JPanel(new BorderLayout());

        textPane = new JTextPane();
        textPane.setBackground(Color.white);
        textPane.setBorder(null);
        textPane.setPreferredSize(new Dimension(300, 100));

        textPane.setFont(new Font(EditorPreferences.getEditorFont(), Font.PLAIN, EditorPreferences.getEditorFontSize()));
        TextUtils.createTabs(textPane);        

        textScrollPane = new JScrollPane(textPane);
        textScrollPane.setWheelScrollingEnabled(true);

        treeModel = new InterpreterTreeModel();
        parseTreePanel = new ParseTreePanel(treeModel);

        splitPane = new JSplitPane();
        splitPane.setBorder(null);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(textScrollPane);
        splitPane.setRightComponent(parseTreePanel);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);

        panel.add(createControlPanel(), BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        editor.registerUndo(new Undo(editor, editor.console), textPane);
    }

    public Box createControlPanel() {
        Box box = Box.createHorizontalBox();
        box.add(createRunButton());
        box.add(createRulesPopUp());
        box.add(Box.createHorizontalGlue());
        box.add(createToggleGraphButton());
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
                ParserRule rule = (ParserRule)rulesCombo.getSelectedItem();
                if(rule != null)
                    startSymbol = rule.name;
            }
        });
        return rulesCombo;
    }

    public JButton createToggleGraphButton() {
        JButton button = new JButton(IconManager.shared().getIconGraph());
        button.setToolTipText("Toggle Graph");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                parseTreePanel.toggleGraph();
            }
        });
        return button;
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

    public void grammarChanged() {
        grammarDirty = true;
    }

    public void interpret() {
        editor.console.makeCurrent();

        ErrorManager.setErrorListener(ErrorListener.shared());

        if(grammarDirty) {
            progress.setCancellable(false);
            progress.setIndeterminate(true);
            progress.setInfo("Preparing...");
            progress.display();
            new Thread(this).start();
        } else {
            run_();
        }
    }

    protected void run_() {
        CharStream input = new ANTLRStringStream(textPane.getText());

        org.antlr.tool.Interpreter lexEngine = new org.antlr.tool.Interpreter(lexer, input);
        CommonTokenStream tokens = new CommonTokenStream(lexEngine);
        tokens.setTokenTypeChannel(lexer.getTokenType("WS"), 99);
        tokens.setTokenTypeChannel(lexer.getTokenType("SL_COMMENT"), 99);
        tokens.setTokenTypeChannel(lexer.getTokenType("ML_COMMENT"), 99);

        org.antlr.tool.Interpreter parseEngine = new org.antlr.tool.Interpreter(parser, tokens);

        ParseTree t = null;
        try {
            if(Token.isLexerName(startSymbol)) {
                t = lexEngine.parse(startSymbol);
            } else {                
                t = parseEngine.parse(startSymbol);
            }
        } catch (Exception e) {
            editor.console.print(e);
            XJAlert.display(editor.getWindowContainer(), "Error while interpreting", "The interpreter has generated the following exception:\n"+e);
        }

        treeModel.setGrammar(parser);
        treeModel.setTree(t);

        parseTreePanel.setRoot((TreeNode)treeModel.getRoot());

    }

    public void run() {
        if(grammarDirty) {
            grammarDirty = false;

            ErrorManager.setErrorListener(ErrorListener.shared());

            try {
                parser = new Grammar(editor.getFileName(), editor.getText());

                String lexerGrammarText = parser.getLexerGrammar();
                lexer = new Grammar();
                lexer.setFileName(editor.getFileName());
                lexer.importTokenVocabulary(parser); // make sure token types line up
                lexer.setGrammarContent(lexerGrammarText);
            } catch (Exception e) {
                editor.console.print(e);
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    run_();
                } finally {
                    progress.close();
                }
            }
        });
    }

    public String getTabName() {
        return "Interpreter";
    }

    public Component getTabComponent() {
        return getContainer();
    }

}
