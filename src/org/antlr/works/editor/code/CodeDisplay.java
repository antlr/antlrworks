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

package org.antlr.works.editor.code;

import org.antlr.works.editor.ate.ATETextPane;
import org.antlr.works.util.Localizable;

import javax.swing.*;
import java.awt.*;

public class CodeDisplay {

    protected JPanel panel;
    protected ATETextPane textPane;

    public CodeDisplay() {

        panel = new JPanel(new BorderLayout());

        textPane = new ATETextPane();
        textPane.setFont(new Font(Localizable.getLocalizedString(Localizable.DEFAULT_FONT), Font.PLAIN, 12));
        textPane.setWordWrap(false);

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setWheelScrollingEnabled(true);

        panel.add(scrollPane, BorderLayout.CENTER);
    }

    public void setText(String text) {
        textPane.setText(text);
        textPane.setCaretPosition(0);
    }

    public Container getContainer() {
        return panel;
    }

}
