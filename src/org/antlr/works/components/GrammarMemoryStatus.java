package org.antlr.works.components;

import org.antlr.xjlib.appkit.swing.XJRollOverButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*

[The "BSD licence"]
Copyright (c) 2005-08 Jean Bovet
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
public class GrammarMemoryStatus extends JPanel {

    private final JButton gcButton = XJRollOverButton.createTextButton("");

    private Timer timer;

    public GrammarMemoryStatus() {
        super(new BorderLayout());
        setOpaque(false);

        Box b = Box.createHorizontalBox();
        b.setBorder(BorderFactory.createEmptyBorder(2, 0, 1, 0));
        b.add(Box.createHorizontalGlue());
        b.add(gcButton);

        add(b, BorderLayout.CENTER);

        gcButton.setFocusable(false);
        gcButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Runtime.getRuntime().gc();
            }
        });

        timer = new Timer(4000, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                refresh();
            }
        });
        timer.start();

        refresh();
    }

    public void close() {
        timer.stop();
        timer = null;
    }

    public void refresh() {
        /*System.out.println("Free: "+displayableMemory(Runtime.getRuntime().freeMemory()));
        System.out.println("Max: "+displayableMemory(Runtime.getRuntime().maxMemory()));
        System.out.println("Total: "+displayableMemory(Runtime.getRuntime().totalMemory()));*/

        String usedMemory = displayableMemory(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
        String maxMemory = displayableMemory(Runtime.getRuntime().maxMemory());
        gcButton.setText(String.format("%s of %s", usedMemory, maxMemory));
    }

    private String displayableMemory(long bytes) {
        if(bytes > 1e6) {
            return String.format("%dM", (int)(bytes/1e6));
        }
        if(bytes > 1e3) {
            return String.format("%dKB", (int)(bytes/1e3));
        }
        return String.format("%db", bytes);
    }
}
