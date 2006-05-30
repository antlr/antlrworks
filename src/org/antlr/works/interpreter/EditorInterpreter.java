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
import org.antlr.works.awtree.AWTreePanel;
import org.antlr.works.awtree.AWTreePanelDelegate;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.editor.EditorMenu;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.menu.ContextualMenuFactory;
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
import java.util.*;
import java.util.List;

public class EditorInterpreter extends EditorTab implements Runnable, AWTreePanelDelegate {

    protected JPanel panel;
    protected JSplitPane splitPane;
    protected JTextPane textPane;
    protected JScrollPane textScrollPane;
    protected EditorInterpreterTreeModel treeModel;
    protected AWTreePanel awTreePanel;
    protected JComboBox rulesCombo;
    protected JLabel tokensToIgnoreLabel;

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
        Box box = Box.createHorizontalBox();
        box.add(createRunButton());
        box.add(createRulesPopUp());
        box.add(Box.createHorizontalStrut(20));
        box.add(createTokensToIgnoreField());
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
        box.add(new JLabel("Ignore rules:"));
        box.add(Box.createHorizontalStrut(5));

        tokensToIgnoreLabel = new JLabel();
        tokensToIgnoreLabel.setFont(tokensToIgnoreLabel.getFont().deriveFont(Font.ITALIC));
        box.add(tokensToIgnoreLabel);

        JButton button = new JButton("Guess");
        button.setToolTipText("Find the name of all rules containing an action with channel=99");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                editor.findTokensToIgnore();
            }
        });
        box.add(Box.createHorizontalGlue());
        box.add(button);

        return box;
    }

    public Container getContainer() {
        return panel;
    }

    public void setRules(List rules) {
        updateRulesCombo(rules);
        updateIgnoreTokens(rules);
    }

    public void updateRulesCombo(List rules) {
        Object selectedItem =  rulesCombo.getSelectedItem();

        rulesCombo.removeAllItems();
        if(rules != null) {
            for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
                rulesCombo.addItem(iterator.next().toString());
            }
        }

        if(selectedItem != null)
            rulesCombo.setSelectedItem(selectedItem);
    }

    public void updateIgnoreTokens(List rules) {
        StringBuffer sb = new StringBuffer();
        if(rules != null) {
            for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
                GrammarSyntaxRule r = (GrammarSyntaxRule) iterator.next();
                if(r.ignored) {
                    if(sb.length() > 0)
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
            process();
        } catch(Exception e) {
            editor.console.print(e);
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
            editor.console.print(e);
        }

        if(parser != null && t != null) {
            treeModel.setGrammar(parser);
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
        ContextualMenuFactory factory = new ContextualMenuFactory(editor.editorMenu);
        factory.addItem(EditorMenu.MI_EXPORT_AS_EPS);
        factory.addItem(EditorMenu.MI_EXPORT_AS_IMAGE);
        return factory.menu;
    }

    public static final String KEY_SPLITPANE_A = "KEY_SPLITPANE_A";

    public void setPersistentData(Map data) {
        if(data == null)
            return;

        Integer i = (Integer)data.get(KEY_SPLITPANE_A);
        if(i != null)
            splitPane.setDividerLocation(i.intValue());
    }

    public Map getPersistentData() {
        Map data = new HashMap();
        data.put(KEY_SPLITPANE_A, new Integer(splitPane.getDividerLocation()));
        return data;
    }

}
