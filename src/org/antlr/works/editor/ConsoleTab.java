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

package org.antlr.works.editor;

import org.antlr.works.components.GrammarWindow;
import org.antlr.works.utils.Console;
import org.antlr.works.utils.ConsoleHelper;
import org.antlr.works.utils.Toolbar;
import org.antlr.xjlib.foundation.XJUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ConsoleTab extends GrammarWindowTab implements Console {

    protected JPanel panel;
    protected JTextArea textArea;

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    protected Map<Thread,Integer> modeByThread = new HashMap<Thread, Integer>();

    public ConsoleTab(GrammarWindow window) {
        super(window);

        panel = new JPanel(new BorderLayout());
        Toolbar box = Toolbar.createHorizontalToolbar();

        JButton clear = new JButton("Clear All");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
        box.addElement(clear);
        box.add(Box.createHorizontalGlue());

        panel.add(createTextArea(), BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);
    }

    public void setMode(int mode) {
        modeByThread.put(Thread.currentThread(), mode);
    }

    public void close() {
        if(ConsoleHelper.getCurrent() == this) {
            ConsoleHelper.setCurrent(null);
        }
    }

    public int getMode() {
        Integer mode = modeByThread.get(Thread.currentThread());
        if(mode == null)
            return Console.MODE_VERBOSE;
        else
            return mode;
    }

    public void makeCurrent() {
        ConsoleHelper.setCurrent(this);
    }

    public Container getContainer() {
        return panel;
    }

    public Container createTextArea() {
        textArea = new JTextArea();
        JScrollPane textAreaScrollPane = new JScrollPane(textArea);
        textAreaScrollPane.setWheelScrollingEnabled(true);
        return textAreaScrollPane;
    }

    public void clear() {
        textArea.setText("");
        window.clearConsoleStatus();
    }

    public synchronized void println(String s) {
        println(s, Console.LEVEL_NORMAL);
    }

    public synchronized void println(String s, int level) {
        print(s+"\n", level);
    }

    public synchronized void println(Throwable e) {
        println(XJUtils.stackTrace(e), Console.LEVEL_ERROR);
    }

    public synchronized void print(final String s, final int level) {
        if(!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    print(s, level);
                }
            });
            return;
        }
        
        String t = "["+dateFormat.format(new Date())+"] "+s;
        textArea.setText(textArea.getText()+t);
        textArea.setCaretPosition(textArea.getText().length());
        System.out.println(s);

        if(getMode() == Console.MODE_VERBOSE) {
            window.consolePrint(s, level);
        }
    }

    public synchronized void print(Throwable e) {
        print(XJUtils.stackTrace(e), Console.LEVEL_ERROR);
    }

    public String getTabName() {
        return "Console";
    }

    public Component getTabComponent() {
        return getContainer();
    }

}
