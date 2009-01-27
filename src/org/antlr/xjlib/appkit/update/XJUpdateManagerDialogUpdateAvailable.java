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

package org.antlr.xjlib.appkit.update;

import org.antlr.xjlib.appkit.frame.XJDialog;
import org.antlr.xjlib.foundation.XJSystem;

import javax.swing.*;
import java.awt.*;

public class XJUpdateManagerDialogUpdateAvailable extends XJDialog {

    protected XJUpdateManager um;

    protected JLabel infoLabel;
    protected JScrollPane scrollPane;
    protected JTextArea infoText;
    protected JButton downloadButton;
    protected JButton cancelButton;

	public XJUpdateManagerDialogUpdateAvailable(XJUpdateManager um) {
        super(um.getParentContainer(), true);

        this.um = um;

		initComponents();
        setSize(600, 300);
        center();

        setDefaultButton(downloadButton);
        setCancelButton(cancelButton);
        setOKButton(downloadButton);

        infoLabel.setText("A new version of "+um.getApplicationName()+" is available:");
        infoText.setFont(new Font("Courier", Font.PLAIN, 12));
        infoText.setText(um.getDescription());
        infoText.setCaretPosition(0);
    }

    private void initComponents() {
        infoLabel = new JLabel("");

        infoText = new JTextArea();
        infoText.setEditable(false);

        scrollPane = new JScrollPane(infoText);
        scrollPane.setWheelScrollingEnabled(true);

        downloadButton = new JButton("Download");
        cancelButton = new JButton("Cancel");

        setTitle("Check for Updates");
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(20, 20, 5, 0);
        contentPane.add(infoLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 20, 0, 20);
        contentPane.add(scrollPane, gbc);

        Box box = Box.createHorizontalBox();
        if(XJSystem.isMacOS()) {
            box.add(cancelButton);
            box.add(Box.createHorizontalStrut(10));
            box.add(downloadButton);
        } else {
            box.add(downloadButton);
            box.add(Box.createHorizontalStrut(10));
            box.add(cancelButton);
        }

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(5, 0, 20, 20);
        contentPane.add(box, gbc);
    }

}
