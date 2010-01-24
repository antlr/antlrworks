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

package org.antlr.xjlib.appkit.utils;

import org.antlr.xjlib.appkit.frame.XJDialog;
import org.antlr.xjlib.appkit.frame.XJFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class XJDialogProgress extends XJDialog {

    protected XJDialogProgressDelegate delegate;

    protected JLabel infoLabel;
    protected JProgressBar progressBar;
    protected JButton cancelButton;

    public XJDialogProgress(XJFrame owner, boolean modal) {
        super(owner==null?null:owner.getJavaContainer(), modal);
        init();
    }

    public XJDialogProgress(XJFrame owner) {
        super(owner==null?null:owner.getJavaContainer(), false);
        init();
    }

    public XJDialogProgress(Container owner, boolean modal) {
        super(owner, modal);
        init();
    }

    public XJDialogProgress(Container owner) {
        super(owner, false);
        init();
    }

    public void init() {
        setResizable(false);
        setSize(400, 90);

        initComponents();

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(delegate != null) {
                    setInfo("Cancelling...");
                    delegate.dialogDidCancel();
                }
            }
        });
    }

    public void setCancellable(boolean flag) {
        cancelButton.setEnabled(flag);
    }

    public void setIndeterminate(boolean flag) {
        if(flag) {
            setProgress(0);
            setProgressMax(0);
        }
        progressBar.setIndeterminate(flag);
    }
    
    public void setDelegate(XJDialogProgressDelegate delegate) {
        this.delegate = delegate;
    }

    public void setInfo(String info) {
        infoLabel.setText(info);
    }

    public void setProgress(float value) {
        setProgress((int)value);
    }

    public void setProgress(int value) {
        progressBar.setValue(value);
    }

    public void setProgressMax(int value) {
        progressBar.setMaximum(value);
    }

    private void initComponents() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());

        infoLabel = new JLabel();
        progressBar = new JProgressBar();
        cancelButton = new JButton("Cancel");

        setTitle("Operation in progress");

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(20, 20, 0, 0);
        contentPane.add(infoLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 20, 20, 10);
        contentPane.add(progressBar, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 20, 20);
        contentPane.add(cancelButton, gbc);
    }

}
