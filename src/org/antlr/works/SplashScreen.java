package org.antlr.works;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
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

public class SplashScreen extends JWindow {

	public SplashScreen() {
		initComponents();
        pack();
        setLocationRelativeTo(null);
	}

    // !!! Don't forget to update the icon path with a call to the IconManager
    // as well as the parametrized texts

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        button1 = new JButton();
        label1 = new JLabel();
        label3 = new JLabel();
        label4 = new JLabel();
        label2 = new JLabel();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            new ColumnSpec[] {
                new ColumnSpec(Sizes.dluX(15)),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                new ColumnSpec(ColumnSpec.LEFT, Sizes.DLUX5, FormSpec.NO_GROW),
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                new ColumnSpec(Sizes.dluX(15))
            },
            new RowSpec[] {
                new RowSpec(Sizes.dluY(10)),
                FormFactory.LINE_GAP_ROWSPEC,
                new RowSpec(RowSpec.TOP, Sizes.DEFAULT, FormSpec.NO_GROW),
                FormFactory.LINE_GAP_ROWSPEC,
                new RowSpec(RowSpec.TOP, Sizes.dluY(20), FormSpec.NO_GROW),
                FormFactory.LINE_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.LINE_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.LINE_GAP_ROWSPEC,
                new RowSpec(Sizes.dluY(10))
            }));

        //---- button1 ----
        button1.setBorder(null);
        button1.setBorderPainted(false);
        button1.setIcon(IconManager.shared().getIconApplication());
        contentPane.add(button1, cc.xywh(3, 3, 1, 7));

        //---- label1 ----
        label1.setFont(new Font("Lucida Grande", Font.PLAIN, 48));
        label1.setText("ANTLRWorks");
        contentPane.add(label1, cc.xy(5, 3));

        //---- label3 ----
        label3.setText("Edit, visualize and debug ANTLR grammar");
        label3.setVerticalAlignment(SwingConstants.TOP);
        contentPane.add(label3, cc.xy(5, 5));

        //---- label4 ----
        label4.setText("Version 1.0er1");
        contentPane.add(label4, cc.xy(5, 7));

        //---- label2 ----
        label2.setText("(c) 2005 Jean Bovet & Terence Parr");
        contentPane.add(label2, cc.xy(5, 9));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JButton button1;
    private JLabel label1;
    private JLabel label3;
    private JLabel label4;
    private JLabel label2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
