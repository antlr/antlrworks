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

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.ParseTree;
import org.antlr.tool.Grammar;
import org.antlr.tool.Interpreter;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.awtree.AWTreePanel;
import org.antlr.works.awtree.AWTreePanelDelegate;
import org.antlr.works.components.container.ComponentContainerGrammarMenu;
import org.antlr.works.components.editor.ComponentEditorGrammar;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.grammar.antlr.ANTLRGrammarEngine;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.utils.IconManager;
import org.antlr.works.utils.TextUtils;
import org.antlr.works.utils.Toolbar;
import org.antlr.works.utils.Utils;
import org.antlr.xjlib.appkit.gview.GView;
import org.antlr.xjlib.appkit.utils.XJDialogProgress;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.StringTokenizer;

public class EditorInterpreter extends EditorTab implements Runnable, AWTreePanelDelegate {

    protected JPanel panel;
    protected JSplitPane splitPane;
    protected JTextPane textPane;
    protected JScrollPane textScrollPane;
    protected EditorInterpreterTreeModel treeModel;
    protected AWTreePanel awTreePanel;
    protected JComboBox rulesCombo;
    protected JComboBox eolCombo;
    protected JLabel tokensToIgnoreLabel;

    protected XJDialogProgress progress;

    protected String startSymbol = null;

    protected ComponentEditorGrammar editor;

    public EditorInterpreter(ComponentEditorGrammar editor) {
        this.editor = editor;
    }

    public void close() {
        awTreePanel.setDelegate(null);
        editor = null;
    }

    public void awake() {
        panel = new JPanel(new BorderLayout());

        textPane = new JTextPane();
        textPane.setBackground(Color.white);
        textPane.setBorder(null);
        textPane.setPreferredSize(new Dimension(300, 100));

        textPane.setFont(new Font(AWPrefs.getEditorFont(), Font.PLAIN, AWPrefs.getEditorFontSize()));
        TextUtils.createTabs(textPane);
        TextUtils.setDefaultTextPaneProperties(textPane);

        textScrollPane = new JScrollPane(textPane);
        textScrollPane.setWheelScrollingEnabled(true);

        treeModel = new EditorInterpreterTreeModel();
        awTreePanel = new AWTreePanel(treeModel);
        awTreePanel.setDelegate(this);

        splitPane = new JSplitPane();
        splitPane.setBorder(null);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(textScrollPane);
        splitPane.setRightComponent(awTreePanel);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);

        panel.add(createControlPanel(), BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        editor.getXJFrame().registerUndo(null, textPane);
    }

    public void componentShouldLayout() {
        splitPane.setDividerLocation(0.2);
    }

    public Box createControlPanel() {
        Toolbar box = Toolbar.createHorizontalToolbar();
        box.addElement(createRunButton());
        box.addElement(createRulesPopUp());
        box.addGroupSeparator();
        box.addElement(new JLabel("Line Endings:"));
        box.addElement(createEOLCombo());
        box.addGroupSeparator();
        createTokensToIgnoreField(box);
        return box;
    }

    public JButton createRunButton() {
        JButton button = new JButton(IconManager.shared().getIconRun());
        button.setToolTipText("Run");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_INTERPRETER_BUTTON);
                interpret();
            }
        });
        return button;
    }

    public JComboBox createRulesPopUp() {
        rulesCombo = new JComboBox();
        rulesCombo.setFocusable(false);
        rulesCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, rulesCombo.getPreferredSize().height));
        rulesCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String rule = (String)rulesCombo.getSelectedItem();
                if(rule != null)
                    startSymbol = rule;
            }
        });
        return rulesCombo;
    }

    public JComboBox createEOLCombo() {
        eolCombo = new JComboBox();
        eolCombo.setFocusable(false);
        eolCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, eolCombo.getPreferredSize().height));
        Utils.fillComboWithEOL(eolCombo);
        return eolCombo;
    }

    public Box createTokensToIgnoreField(Toolbar box) {
        box.addElement(new JLabel("Ignore rules:"));

        tokensToIgnoreLabel = new JLabel();
        tokensToIgnoreLabel.setFont(tokensToIgnoreLabel.getFont().deriveFont(Font.ITALIC));
        box.addElement(tokensToIgnoreLabel);

        JButton button = new JButton("Guess");
        button.setFocusable(false);
        button.setToolTipText("Find the name of all rules containing an action with channel=99");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                editor.findTokensToIgnore(true);
            }
        });
        box.add(Box.createHorizontalGlue());
        box.addElement(button);

        return box;
    }

    public Container getContainer() {
        return panel;
    }

    public void setRules(List<ElementRule> rules) {
        updateRulesCombo(rules);
        updateIgnoreTokens(rules);
    }

    public void updateRulesCombo(List<ElementRule> rules) {
        Object selectedItem =  rulesCombo.getSelectedItem();

        rulesCombo.removeAllItems();
        if(rules != null) {
            for (ElementRule rule : rules) {
                rulesCombo.addItem(rule.toString());
            }
        }

        if(selectedItem != null)
            rulesCombo.setSelectedItem(selectedItem);
    }

    public void updateIgnoreTokens(List<ElementRule> rules) {
        StringBuffer sb = new StringBuffer();
        if(rules != null) {
            for (ElementRule r : rules) {
                if (r.ignored) {
                    if (sb.length() > 0)
                        sb.append(" ");
                    sb.append(r.name);
                }
            }
        }
        if(sb.length() == 0)
            tokensToIgnoreLabel.setText("-");
        else
            tokensToIgnoreLabel.setText(sb.toString());
    }

    public void interpret() {
        editor.console.makeCurrent();

        if(progress == null)
            progress = new XJDialogProgress(editor.getXJFrame());

        progress.setInfo("Interpreting...");

        // AW-42: guess always before running the interpreter
        editor.findTokensToIgnore(false);

        progress.setCancellable(false);
        progress.setIndeterminate(true);
        progress.display();

        new Thread(this).start();
    }

    public void run() {
        try {
            editor.getGrammarEngine().analyze();
            process();
        } catch(Exception e) {
            editor.console.println(e);
        } finally {
            runEnded();
        }
    }

    public void runEnded() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progress.close();
            }
        });
    }

    protected void process() {
        progress.setInfo("Interpreting...");
        editor.console.println("Interpreting...");

        CharStream input = new ANTLRStringStream(Utils.convertRawTextWithEOL(textPane.getText(), eolCombo));

        ANTLRGrammarEngine eg = editor.getGrammarEngine().getANTLRGrammarEngine();
        try {
            eg.createGrammars();
        } catch (Exception e) {
            editor.console.println(e);
            return;
        }

        Grammar parser = eg.getParserGrammar();
        Grammar lexer = eg.getLexerGrammar();
        if(lexer == null) {
            throw new RuntimeException("Lexer is null. Check the grammar before running the interpreter.");
        }

        Interpreter lexEngine = new Interpreter(lexer, input);
        CommonTokenStream tokens = new CommonTokenStream(lexEngine);

        StringTokenizer tk = new StringTokenizer(tokensToIgnoreLabel.getText(), " ");
        while ( tk.hasMoreTokens() ) {
            String tokenName = tk.nextToken();
            tokens.setTokenTypeChannel(lexer.getTokenType(tokenName), 99);
        }

        Interpreter parseEngine = new Interpreter(parser, tokens);

        ParseTree t = null;
        try {
            if(ATEToken.isLexerName(startSymbol)) {
                t = lexEngine.parse(startSymbol);
            } else {
                t = parseEngine.parse(startSymbol);
            }
        } catch (Exception e) {
            editor.console.println(e);
        }

        if(parser != null && t != null) {
            SwingUtilities.invokeLater(new Refresh(parser, t));
        }
    }

    public class Refresh implements Runnable {
        Grammar g;
        ParseTree t;

        public Refresh(Grammar grammar, ParseTree t) {
            this.g = grammar;
            this.t = t;
        }

        public void run() {
            treeModel.setGrammar(g);
            treeModel.setTree(t);

            awTreePanel.setRoot((TreeNode)treeModel.getRoot());
            awTreePanel.refresh();
        }
    }

    public boolean canExportToBitmap() {
        return true;
    }

    public boolean canExportToEPS() {
        return true;
    }

    public GView getExportableGView() {
        return awTreePanel.getGraphView();
    }

    public String getTabName() {
        return "Interpreter";
    }

    public Component getTabComponent() {
        return getContainer();
    }

    public void awTreeDidSelectTreeNode(TreeNode node, boolean shiftKey) {
        // not implemented
    }

    public JPopupMenu awTreeGetContextualMenu() {
        ContextualMenuFactory factory = editor.createContextualMenuFactory();
        factory.addItem(ComponentContainerGrammarMenu.MI_EXPORT_AS_EPS);
        factory.addItem(ComponentContainerGrammarMenu.MI_EXPORT_AS_IMAGE);
        return factory.menu;
    }

}
