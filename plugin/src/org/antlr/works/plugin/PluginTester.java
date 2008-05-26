package org.antlr.works.plugin;

import org.antlr.works.IDE;
import org.antlr.works.components.container.ComponentContainer;
import org.antlr.works.components.document.ComponentDocument;
import org.antlr.works.plugin.container.PCXJApplicationDelegate;
import org.antlr.works.plugin.container.PluginFactory;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.frame.XJWindowInterface;

import javax.swing.*;
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

    private void createAndShowGUI() throws Exception {
        XJApplication.setDelegate(new PCXJApplicationDelegate());
        XJApplication.setPropertiesPath(IDE.PROPERTIES_PATH);

        ComponentDocument document = PluginFactory.getInstance().createDocument();
        document.awake();
        document.load("/Users/bovet/Grammars/split_parser_lexer/ExprPars.g");

        XJWindowInterface window = document.getWindow();

        ComponentContainer container = document.getContainer();

        JSplitPane vertical = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        vertical.setLeftComponent(container.getRulesComponent());
        vertical.setRightComponent(window.getRootPane());
        vertical.setBorder(null);
        vertical.setContinuousLayout(true);
        vertical.setOneTouchExpandable(true);

        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Plugin Tester");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.add(vertical);
        frame.setVisible(true);

        window.becomingVisibleForTheFirstTime();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new PluginTester().createAndShowGUI();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });
    }

}
