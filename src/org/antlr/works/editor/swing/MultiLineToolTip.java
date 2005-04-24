/* (swing1.1beta3) */
package org.antlr.works.editor.swing;

import javax.swing.*;

/**
 * @version 1.0 11/09/98
 */

public class MultiLineToolTip extends JToolTip {
    public MultiLineToolTip() {
        setUI(new MultiLineToolTipUI());
    }
}

