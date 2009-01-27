package org.antlr.xjlib.appkit.swing;

import org.antlr.xjlib.appkit.frame.XJDialog;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

public class XJColorChooser extends XJDialog {

    JColorChooser cc;

    JPanel targetPanel;
    Color oldTargetColor = Color.black;

    public XJColorChooser(Container owner, boolean modal) {
        this(owner, modal, null);
    }

    public XJColorChooser(Container owner, boolean modal, JPanel targetPanel) {
        super(owner, modal);

        setTitle("Choose a color");
        setSize(500, 400);

        if(targetPanel != null) {
            this.targetPanel = targetPanel;
            this.oldTargetColor = targetPanel.getBackground();
        }

        cc = new JColorChooser(oldTargetColor);
        cc.getSelectionModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateTargetColor();
            }
        });
        getContentPane().add(cc, BorderLayout.CENTER);

        JButton cancel = new JButton("Cancel");
        setCancelButton(cancel);

        JButton ok = new JButton("OK");
        setOKButton(ok);
        setDefaultButton(ok);

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(cancel);
        box.add(ok);
        box.add(Box.createHorizontalStrut(15));
        getContentPane().add(box, BorderLayout.SOUTH);
    }

    protected void updateTargetColor() {
        updateTargetColor(cc.getColor());
    }

    protected void updateTargetColor(Color c) {
        if(targetPanel != null)
            targetPanel.setBackground(c);
    }

    public void dialogWillCloseCancel() {
        updateTargetColor(oldTargetColor);
    }

    public void dialogWillCloseOK() {
        updateTargetColor();
    }

    public Color getColor() {
        return cc.getColor();
    }
}
