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

import edu.usfca.xj.foundation.XJUtils;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.utils.Console;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditorConsole extends EditorTab implements Console {

    protected CEditorGrammar editor;

    protected JPanel panel;
    protected JTextArea textArea;

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    protected Map modeByThread = new HashMap();

    protected static EditorConsole current = null;

    public static synchronized void setCurrent(EditorConsole console) {
        current = console;
    }

    public static synchronized EditorConsole getCurrent() {
        return current;
    }

    public EditorConsole(CEditorGrammar editor) {
        this.editor = editor;

        panel = new JPanel(new BorderLayout());
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
    }

    public void setMode(int mode) {
        modeByThread.put(Thread.currentThread(), new Integer(mode));
    }

    public int getMode() {
        Integer mode = (Integer)modeByThread.get(Thread.currentThread());
        if(mode == null)
            return Console.MODE_VERBOSE;
        else
            return mode.intValue();
    }

    public void makeCurrent() {
        EditorConsole.setCurrent(this);
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
    }

    public synchronized void println(String s) {
        println(s, Console.LEVEL_NORMAL);
    }

    public synchronized void println(String s, int level) {
        print(s+"\n", level);
    }

    public synchronized void print(String s, int level) {
        String t = "["+dateFormat.format(new Date())+"] "+s;
        textArea.setText(textArea.getText()+t);
        textArea.setCaretPosition(textArea.getText().length());
        System.out.println(s);

        if(getMode() == Console.MODE_VERBOSE)
            editor.consolePrint(s, level);
    }

    public synchronized void print(Exception e) {
        println(XJUtils.stackTrace(e), Console.LEVEL_ERROR);
    }

    public String getTabName() {
        return "Console";
    }

    public Component getTabComponent() {
        return getContainer();
    }

}
