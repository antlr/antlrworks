package org.antlr.works.plugin;

import org.antlr.works.IDE;
import org.antlr.works.components.editor.ComponentEditorGrammarDefaultDelegate;
import org.antlr.works.plugin.container.PCXJApplicationDelegate;
import org.antlr.works.plugin.container.PluginContainer;
import org.antlr.xjlib.appkit.app.XJApplication;

import javax.swing.*;
import java.awt.*;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
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

public class PluginTester {

    public PluginContainer container;
    protected JSplitPane vertical;

    private void createAndShowGUI() {
        XJApplication.setDelegate(new PCXJApplicationDelegate());
        XJApplication.setPropertiesPath(IDE.PROPERTIES_PATH);
        
        container = new PluginContainer();
        container.load("/Users/bovet/Grammars/syntax.g");
        assemble();

        JFrame.setDefaultLookAndFeelDecorated(true);

        JFrame frame = new JFrame("Plugin Tester");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);

        frame.add(container.getRootPane());

        frame.pack();
        frame.setVisible(true);
        container.becomingVisibleForTheFirstTime();
        vertical.setDividerLocation((int)(container.getContentPane().getHeight()*0.5));
    }

    public void assemble() {
        JSplitPane horizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        horizontal.setLeftComponent(container.getRulesComponent());
        horizontal.setRightComponent(container.getEditorComponent());
        horizontal.setBorder(null);
        horizontal.setContinuousLayout(true);
        horizontal.setOneTouchExpandable(true);

        vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        vertical.setTopComponent(horizontal);
        vertical.setBottomComponent(container.getTabbedComponent());
        vertical.setBorder(null);
        vertical.setContinuousLayout(true);
        vertical.setOneTouchExpandable(true);

        JPanel upperPanel = new JPanel(new BorderLayout());
        upperPanel.add(container.getMenubarComponent(), BorderLayout.NORTH);
        upperPanel.add(container.getToolbarComponent(), BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(upperPanel, BorderLayout.NORTH);
        panel.add(vertical, BorderLayout.CENTER);
        panel.add(container.getStatusComponent(), BorderLayout.SOUTH);

        container.setEditorGrammarDelegate(new ComponentEditorGrammarDefaultDelegate(vertical));

        container.getContentPane().add(panel);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new PluginTester().createAndShowGUI();
            }
        });
    }

}
