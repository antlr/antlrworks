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
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.editor.EditorMenu;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.parsetree.ParseTreePanel;
import org.antlr.works.parsetree.ParseTreePanelDelegate;
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
import java.util.StringTokenizer;

public class EditorInterpreter implements Runnable, EditorTab, ParseTreePanelDelegate {

    protected JPanel panel;
    protected JSplitPane splitPane;
    protected JTextPane textPane;
    protected JScrollPane textScrollPane;
    protected EditorInterpreterTreeModel treeModel;
    protected ParseTreePanel parseTreePanel;
    protected JComboBox rulesCombo;
    protected JTextField tokensToIgnoreField;

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
        parseTreePanel.setDelegate(this);

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
        box.add(Box.createHorizontalStrut(20));
        box.add(createTokensToIgnoreField());
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
                String rule = (String)rulesCombo.getSelectedItem();
                if(rule != null)
                    startSymbol = rule;
            }
        });
        return rulesCombo;
    }

    public Box createTokensToIgnoreField() {
        Box box = Box.createHorizontalBox();
        box.add(new JLabel("Ignore tokens:"));

        tokensToIgnoreField = new JTextField();
        tokensToIgnoreField.setText("WS COMMENT");
        box.add(tokensToIgnoreField);

        JButton button = new JButton("Fetch");
        button.setToolTipText("Fetch the names of all rules containing an action with channel=99");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                fetchTokensToIgnore();
            }
        });

        box.add(button);

        return box;
    }

    public Container getContainer() {
        return panel;
    }

    public void setRules(List rules) {
        Object selectedItem =  rulesCombo.getSelectedItem();

        rulesCombo.removeAllItems();
        for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
            rulesCombo.addItem(iterator.next().toString());
        }

        if(selectedItem != null)
            rulesCombo.setSelectedItem(selectedItem);
    }

    /** This method iterates over all rules and all blocks inside each rule to
     * find a sequence of token equals to "channel=99".
     */

    public void fetchTokensToIgnore() {
        List rules = editor.getRules();
        if(rules == null || rules.isEmpty())
            return;

        StringBuffer tokensToIgnore = new StringBuffer();

        ATESyntaxLexer lexer = new ATESyntaxLexer();
        for (Iterator ruleIter = rules.iterator(); ruleIter.hasNext();) {
            GrammarSyntaxRule rule = (GrammarSyntaxRule) ruleIter.next();
            List blocks = rule.getBlocks();
            if(blocks == null || blocks.isEmpty())
                continue;

            for (Iterator blockIter = blocks.iterator(); blockIter.hasNext();) {
                ATEToken block = (ATEToken) blockIter.next();
                lexer.tokenize(block.getAttribute());

                List tokens = lexer.getTokens();
                for(int t=0; t<tokens.size(); t++) {
                    ATEToken token = (ATEToken)tokens.get(t);
                    if(token.type == ATESyntaxLexer.TOKEN_ID && token.getAttribute().equals("channel") && t+3 < tokens.size()) {
                        ATEToken t1 = (ATEToken)tokens.get(t+1);
                        ATEToken t2 = (ATEToken)tokens.get(t+2);
                        ATEToken t3 = (ATEToken)tokens.get(t+3);
                        if(t1.type != ATESyntaxLexer.TOKEN_CHAR || !t1.getAttribute().equals("="))
                            continue;

                        if(t2.type != ATESyntaxLexer.TOKEN_CHAR || !t2.getAttribute().equals("9"))
                            continue;

                        if(t3.type != ATESyntaxLexer.TOKEN_CHAR || !t3.getAttribute().equals("9"))
                            continue;

                        if(tokensToIgnore.length() > 0)
                            tokensToIgnore.append(" ");
                        tokensToIgnore.append(rule.name);
                        break;
                    }
                }
            }
        }

        tokensToIgnoreField.setText(tokensToIgnore.toString());
    }

    public void interpret() {
        editor.console.makeCurrent();

        if(editor.getEngineGrammar().isDirty()) {
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

        try {
            editor.getEngineGrammar().analyze();
        } catch(Exception e) {
            editor.console.print(e);
            runEnded();
            return;
        }

        process();
        runEnded();
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

        CharStream input = new ANTLRStringStream(textPane.getText());

        Grammar parser;
        Grammar lexer;
        try {
            parser = editor.getEngineGrammar().getParserGrammar();
            lexer = editor.getEngineGrammar().getLexerGrammar();
        } catch (Exception e) {
            editor.console.print(e);
            return;
        }

        Interpreter lexEngine = new Interpreter(lexer, input);
        CommonTokenStream tokens = new CommonTokenStream(lexEngine);

        StringTokenizer tk = new StringTokenizer(tokensToIgnoreField.getText(), " ");
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

    public JPopupMenu getContextualMenu() {
        ContextualMenuFactory factory = new ContextualMenuFactory(editor.editorMenu);
        factory.addItem(EditorMenu.MI_EXPORT_AS_EPS);
        factory.addItem(EditorMenu.MI_EXPORT_AS_IMAGE);
        return factory.menu;
    }

}
