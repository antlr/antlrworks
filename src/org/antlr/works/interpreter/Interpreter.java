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
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.editor.swing.TreeUtilities;
import org.antlr.works.editor.undo.Undo;
import org.antlr.works.parser.Parser;
import org.antlr.works.stats.Statistics;
import org.antlr.works.util.ErrorListener;
import org.antlr.works.util.IconManager;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

public class Interpreter implements Runnable {

    private JPanel panel;
    private JSplitPane splitPane;
    private JTextPane textPane;
    private JScrollPane textScrollPane;
    private JTree tree;
    private JScrollPane treeScrollPane;
    private InterpreterTreeModel treeModel;
    private JComboBox rulesCombo;

    private XJDialogProgress progress;

    private boolean grammarDirty = true;

    private String startSymbol = null;

    private Grammar parser;
    private Grammar lexer;

    private EditorWindow editor;

    public Interpreter(EditorWindow editor) {
        this.editor = editor;

        progress = new XJDialogProgress(editor);

        panel = new JPanel(new BorderLayout());

        textPane = new JTextPane();
        textPane.setBackground(Color.white);
        textPane.setBorder(null);
        textPane.setPreferredSize(new Dimension(300, 100));

        textScrollPane = new JScrollPane(textPane);
        textScrollPane.setWheelScrollingEnabled(true);

        treeModel = new InterpreterTreeModel();
        tree = new JTree(treeModel);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
        treeRenderer.setClosedIcon(null);
        treeRenderer.setLeafIcon(null);
        treeRenderer.setOpenIcon(null);

        tree.setCellRenderer(treeRenderer);

        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 2) {
                        displayNodeInfo(selPath.getLastPathComponent());
                        e.consume();
                    }
                }
            }
        });

        treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setWheelScrollingEnabled(true);

        splitPane = new JSplitPane();
        splitPane.setBorder(null);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(textScrollPane);
        splitPane.setRightComponent(treeScrollPane);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);

        panel.add(createControlPanel(), BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        editor.registerUndo(new Undo(editor.editorGUI), textPane);
    }

    public Box createControlPanel() {
        Box box = Box.createHorizontalBox();
        box.add(createRunButton());
        box.add(createRulesPopUp());
        box.add(Box.createHorizontalGlue());
        box.add(createExpandAllButton());
        box.add(createCollapseAllButton());
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
                Parser.Rule rule = (Parser.Rule)rulesCombo.getSelectedItem();
                if(rule != null)
                    startSymbol = rule.name;
            }
        });
        return rulesCombo;
    }

    public JButton createExpandAllButton() {
        JButton button = new JButton(IconManager.shared().getIconExpandAll());
        button.setToolTipText("Expand All");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                TreeUtilities.expandAll(tree);
            }
        });
        return button;
    }

    public JButton createCollapseAllButton() {
        JButton button = new JButton(IconManager.shared().getIconCollapseAll());
        button.setToolTipText("Collapse All");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                TreeUtilities.collapseAll(tree);
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

    public void displayNodeInfo(Object node) {
        InterpreterTreeModel.NodeWrapper wrapper = (InterpreterTreeModel.NodeWrapper)node;
        XJAlert.display(editor.getWindowContainer(), "Node info", wrapper.getInfoString());
    }

    public void interpret() {
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
            t = parseEngine.parse(startSymbol);
        } catch (Exception e) {
            e.printStackTrace();
            XJAlert.display(editor.getWindowContainer(), "Error while interpreting", "The interpreter has generated the following exception:\n"+e);
        }

        treeModel.setGrammar(parser);
        treeModel.setTree(t);
        tree.updateUI();
        TreeUtilities.expandAll(tree);
    }

    public void run() {
        if(grammarDirty) {
            grammarDirty = false;

            ErrorManager.setErrorListener(ErrorListener.shared());

            try {
                parser = new Grammar(editor.getFileName(), editor.getPlainText());

                String lexerGrammarText = parser.getLexerGrammar();
                lexer = new Grammar();
                lexer.setFileName(editor.getFileName());
                lexer.importTokenVocabulary(parser); // make sure token types line up
                lexer.setGrammarContent(lexerGrammarText);
            } catch (Exception e) {
                e.printStackTrace();
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

}
