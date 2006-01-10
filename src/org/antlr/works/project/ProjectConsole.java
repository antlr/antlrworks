package org.antlr.works.project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class ProjectConsole {

    protected Container container;
    protected JTextArea textArea;

    public ProjectConsole() {
        create();
    }

    public Container getContainer() {
        return container;
    }

    public void create() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);

        JPanel panel = new JPanel(new BorderLayout());

        Box box = Box.createHorizontalBox();

        JButton clear = new JButton("Clear All");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
        box.add(clear);
        box.add(Box.createHorizontalGlue());

        panel.add(createTextArea(), BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);

        tabbedPane.add("Project", panel);

        container = tabbedPane;
    }

    public Container createTextArea() {
        textArea = new JTextArea();
        JScrollPane textAreaScrollPane = new JScrollPane(textArea);
        textAreaScrollPane.setWheelScrollingEnabled(true);
        return textAreaScrollPane;
    }

    public void clear() {
        textArea.setText("");
    }

    public synchronized void print(String s) {
        textArea.setText(textArea.getText()+s);
        textArea.setCaretPosition(textArea.getText().length());
        System.out.println(s);
    }
}
