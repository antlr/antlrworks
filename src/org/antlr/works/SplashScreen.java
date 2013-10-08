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

package org.antlr.works;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import org.antlr.works.utils.IconManager;
import org.antlr.works.utils.Localizable;
import org.antlr.xjlib.foundation.XJSystem;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {

    protected JPanel backgroundPanel;

    public SplashScreen() {
        backgroundPanel = new JPanel();

        initComponents();

        iconButton.setIcon(IconManager.shared().getIconApplication());
        iconButton.setContentAreaFilled(false);

        appNameLabel.setText(Localizable.getLocalizedString(Localizable.APP_NAME));
        infoLabel.setText(Localizable.getLocalizedString(Localizable.SPLASH_INFO));
        versionLabel.setText(String.format(Localizable.getLocalizedString(Localizable.SPLASH_VERSION), IDE.VERSION));
        copyrightLabel.setText(Localizable.getLocalizedString(Localizable.SPLASH_COPYRIGHT));

        backgroundPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        if(!XJSystem.isMacOS())
            backgroundPanel.setBackground(Color.white);
        getContentPane().add(backgroundPanel);

        pack();
        setLocationRelativeTo(null);
    }

    // !!!!! Replace getContentPane() with backgroundPanel after re-generation
    // with JFormDesigner !!!!

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        iconButton = new JButton();
        appNameLabel = new JLabel();
        infoLabel = new JLabel();
        versionLabel = new JLabel();
        copyrightLabel = new JLabel();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        Container contentPane = backgroundPanel; //getContentPane();
        contentPane.setLayout(new FormLayout(
            new ColumnSpec[] {
                new ColumnSpec(Sizes.dluX(0)),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                new ColumnSpec(ColumnSpec.LEFT, Sizes.DLUX5, FormSpec.NO_GROW),
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                new ColumnSpec(Sizes.dluX(10))
            },
            new RowSpec[] {
                new RowSpec(Sizes.dluY(10)),
                FormFactory.LINE_GAP_ROWSPEC,
                new RowSpec(RowSpec.TOP, Sizes.DEFAULT, FormSpec.NO_GROW),
                FormFactory.LINE_GAP_ROWSPEC,
                new RowSpec(RowSpec.TOP, Sizes.dluY(10), FormSpec.NO_GROW),
                FormFactory.LINE_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.LINE_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.LINE_GAP_ROWSPEC,
                new RowSpec(Sizes.dluY(10))
            }));

        //---- button1 ----
        iconButton.setBorder(null);
        iconButton.setBorderPainted(false);
        iconButton.setIcon(IconManager.shared().getIconApplication());
        contentPane.add(iconButton, cc.xywh(3, 3, 1, 7));

        //---- label1 ----
        appNameLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 48));
        appNameLabel.setText("ANTLRWorks");
        contentPane.add(appNameLabel, cc.xy(5, 3));

        //---- label3 ----
        infoLabel.setText("Edit, visualize and debug ANTLR grammars");
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        contentPane.add(infoLabel, cc.xy(5, 5));

        //---- label4 ----
        versionLabel.setText("Version 1.0er1");
        contentPane.add(versionLabel, cc.xy(5, 7));

        //---- label2 ----
        copyrightLabel.setText("(c) 2005 Jean Bovet & Terence Parr");
        contentPane.add(copyrightLabel, cc.xy(5, 9));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JButton iconButton;
    private JLabel appNameLabel;
    private JLabel infoLabel;
    private JLabel versionLabel;
    private JLabel copyrightLabel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
