package org.antlr.works.dialog;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import edu.usfca.xj.appkit.app.XJApplication;
import edu.usfca.xj.appkit.frame.XJPanel;
import edu.usfca.xj.foundation.XJLib;
import org.antlr.Tool;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.works.util.IconManager;

import javax.swing.*;
import java.awt.*;

/*

[The "BSD licence"]
Copyright (c) 2004-05 Jean Bovet
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

public class DialogAbout extends XJPanel {

    public DialogAbout() {
        initComponents();

        appIconButton.setIcon(IconManager.shared().getIconApplication());
        versionLabel.setText("Version "+XJApplication.getAppVersionLong());
        guiVersionLabel.setText(XJApplication.getAppVersionShort());
        antlrVersionLabel.setText(Tool.Version);
        stringTemplateVersionLabel.setText(StringTemplate.VERSION);
        xjVersionLabel.setText(XJLib.stringVersion());

        setResizable(false);
        setSize(800, 430);
        center();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        appIconButton = new JButton();
        descriptionLabel = new JLabel();
        titleLabel = new JLabel();
        versionLabel = new JLabel();
        copyrightLabel = new JLabel();
        tabbedPane1 = new JTabbedPane();
        panel2 = new JPanel();
        label13 = new JLabel();
        label14 = new JLabel();
        label1 = new JLabel();
        label4 = new JLabel();
        label3 = new JLabel();
        label2 = new JLabel();
        panel1 = new JPanel();
        label5 = new JLabel();
        guiVersionLabel = new JLabel();
        antlrVersionLabel = new JLabel();
        label11 = new JLabel();
        stringTemplateVersionLabel = new JLabel();
        label12 = new JLabel();
        xjVersionLabel = new JLabel();
        label10 = new JLabel();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setResizable(false);
        setTitle("About");
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            new ColumnSpec[] {
                new ColumnSpec(Sizes.dluX(10)),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                new ColumnSpec(Sizes.dluX(10))
            },
            new RowSpec[] {
                new RowSpec(Sizes.dluY(10)),
                FormFactory.LINE_GAP_ROWSPEC,
                new RowSpec(RowSpec.BOTTOM, Sizes.DEFAULT, FormSpec.NO_GROW),
                FormFactory.LINE_GAP_ROWSPEC,
                new RowSpec("top:max(default;20dlu)"),
                FormFactory.LINE_GAP_ROWSPEC,
                new RowSpec("bottom:max(default;15dlu)"),
                FormFactory.LINE_GAP_ROWSPEC,
                new RowSpec("top:max(default;15dlu)"),
                FormFactory.LINE_GAP_ROWSPEC,
                new RowSpec("fill:max(default;60dlu):grow"),
                FormFactory.LINE_GAP_ROWSPEC,
                new RowSpec(Sizes.dluY(10))
            }));

        //---- appIconButton ----
        appIconButton.setBorderPainted(false);
        appIconButton.setContentAreaFilled(false);
        appIconButton.setDefaultCapable(false);
        appIconButton.setEnabled(true);
        appIconButton.setFocusPainted(false);
        appIconButton.setFocusable(false);
        appIconButton.setIcon(new ImageIcon("/Users/bovet/Dev/Projects/ANTLRWorks/classes/org/antlr/works/icons/app.png"));
        appIconButton.setMaximumSize(new Dimension(136, 144));
        appIconButton.setMinimumSize(new Dimension(136, 144));
        appIconButton.setPreferredSize(new Dimension(124, 144));
        contentPane.add(appIconButton, cc.xywh(3, 3, 1, 8));

        //---- descriptionLabel ----
        descriptionLabel.setHorizontalAlignment(SwingConstants.LEFT);
        descriptionLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        descriptionLabel.setText("A graphical development environment for developing and debugging ANTLR v3 grammars");
        descriptionLabel.setVerticalAlignment(SwingConstants.TOP);
        descriptionLabel.setVerticalTextPosition(SwingConstants.TOP);
        contentPane.add(descriptionLabel, cc.xywh(5, 7, 1, 2));

        //---- titleLabel ----
        titleLabel.setFont(new Font("Lucida Grande", Font.BOLD, 36));
        titleLabel.setText("ANTLRWorks");
        contentPane.add(titleLabel, cc.xy(5, 3));

        //---- versionLabel ----
        versionLabel.setText("Version 1.0 early access 1");
        contentPane.add(versionLabel, cc.xy(5, 5));

        //---- copyrightLabel ----
        copyrightLabel.setText("Copyright (c) 2005 Jean Bovet & Terence Parr");
        contentPane.add(copyrightLabel, cc.xy(5, 9));

        //======== tabbedPane1 ========
        {

            //======== panel2 ========
            {
                panel2.setLayout(new FormLayout(
                    new ColumnSpec[] {
                        new ColumnSpec(Sizes.dluX(10)),
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        new ColumnSpec(Sizes.dluX(10))
                    },
                    new RowSpec[] {
                        new RowSpec(Sizes.dluY(10)),
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        new RowSpec(Sizes.dluY(10))
                    }));

                //---- label13 ----
                label13.setText("ANTLR and StringTemplate are (c) 1989-2005 Terence Parr");
                panel2.add(label13, cc.xy(3, 3));

                //---- label14 ----
                label14.setText("XJLibrary is (c) 2004-2005 Jean Bovet");
                panel2.add(label14, cc.xy(3, 5));

                //---- label1 ----
                label1.setText("Portion of the GUI uses JGoodies, (c) 2002-2004 Karsten Lentzsch");
                panel2.add(label1, cc.xy(3, 7));

                //---- label4 ----
                label4.setText("Portion of the GUI was created using JFormDesigner, (c) 2004-2005 Karl Tauber");
                panel2.add(label4, cc.xy(3, 9));

                //---- label3 ----
                label3.setText("BrowserLauncher is (c) 2001 Eric Albert <ejalbert@cs.stanford.edu>");
                panel2.add(label3, cc.xy(3, 11));

                //---- label2 ----
                label2.setText("Application icon is (c) Matthew McClintock <matthew@mc.clintock.com>");
                panel2.add(label2, cc.xy(3, 13));
            }
            tabbedPane1.addTab("Acknowledgment", panel2);

            //======== panel1 ========
            {
                panel1.setLayout(new FormLayout(
                    new ColumnSpec[] {
                        FormFactory.GLUE_COLSPEC,
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC,
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC,
                        FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                        FormFactory.GLUE_COLSPEC
                    },
                    new RowSpec[] {
                        FormFactory.GLUE_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.LINE_GAP_ROWSPEC,
                        new RowSpec(Sizes.dluY(10)),
                        FormFactory.LINE_GAP_ROWSPEC,
                        FormFactory.GLUE_ROWSPEC
                    }));

                //---- label5 ----
                label5.setHorizontalAlignment(SwingConstants.RIGHT);
                label5.setText("ANTLRWorks:");
                panel1.add(label5, cc.xy(3, 3));

                //---- guiVersionLabel ----
                guiVersionLabel.setText("-");
                panel1.add(guiVersionLabel, cc.xy(5, 3));

                //---- antlrVersionLabel ----
                antlrVersionLabel.setText("3.0 early access 1");
                panel1.add(antlrVersionLabel, cc.xy(5, 5));

                //---- label11 ----
                label11.setHorizontalAlignment(SwingConstants.RIGHT);
                label11.setText("StringTemplate:");
                panel1.add(label11, cc.xy(3, 7));

                //---- stringTemplateVersionLabel ----
                stringTemplateVersionLabel.setText("2.1");
                panel1.add(stringTemplateVersionLabel, cc.xy(5, 7));

                //---- label12 ----
                label12.setHorizontalAlignment(SwingConstants.RIGHT);
                label12.setText("XJLibrary:");
                panel1.add(label12, cc.xy(3, 9));

                //---- xjVersionLabel ----
                xjVersionLabel.setText("1.2");
                panel1.add(xjVersionLabel, cc.xy(5, 9));

                //---- label10 ----
                label10.setHorizontalAlignment(SwingConstants.RIGHT);
                label10.setText("ANTLR:");
                panel1.add(label10, cc.xy(3, 5));
            }
            tabbedPane1.addTab("Version", panel1);
        }
        contentPane.add(tabbedPane1, cc.xywh(3, 11, 3, 1));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JButton appIconButton;
    private JLabel descriptionLabel;
    private JLabel titleLabel;
    private JLabel versionLabel;
    private JLabel copyrightLabel;
    private JTabbedPane tabbedPane1;
    private JPanel panel2;
    private JLabel label13;
    private JLabel label14;
    private JLabel label1;
    private JLabel label4;
    private JLabel label3;
    private JLabel label2;
    private JPanel panel1;
    private JLabel label5;
    private JLabel guiVersionLabel;
    private JLabel antlrVersionLabel;
    private JLabel label11;
    private JLabel stringTemplateVersionLabel;
    private JLabel label12;
    private JLabel xjVersionLabel;
    private JLabel label10;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}
