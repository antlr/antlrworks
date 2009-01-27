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

import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.foundation.XJSystem;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class XJAlert {

    public static final int YES = JOptionPane.YES_OPTION;
    public static final int NO = JOptionPane.NO_OPTION;
    public static final int CANCEL = JOptionPane.CANCEL_OPTION;

    private static Icon icon;

    private boolean displayDoNotShowAgainButton = false;
    private JCheckBox doNotShowAgainButton;

    public static void disableEscapeKey() {
        // Make sure the Escape key doesn't close any alert dialog using JOptionPane
        // because it closes the dialog by returning the Close option instead of the Cancel option
        Object[] mapping = {"ESCAPE", "cancel"};
        UIManager.put("OptionPane.windowBindings", mapping);
    }

    public static void enableEscapeKey() {
        // Make sure the Escape key doesn't close any alert dialog using JOptionPane
        // because it closes the dialog by returning the Close option instead of the Cancel option
        Object[] mapping = {"ESCAPE", "close"};
        UIManager.put("OptionPane.windowBindings", mapping);
    }

    public static Component getParent(Component parent) {
        return parent==null?XJApplication.getActiveContainer():parent;
    }

    public static XJAlert createInstance() {
        return new XJAlert();
    }

    public static void display(Component parent, String title, String message) {
        XJAlert.createInstance().displayAlert(parent, title, message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
    }

    public static int displayAlertYESNO(Component parent, String title, String message) {
        return XJAlert.createInstance().displayAlert(parent, title, message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
    }

    public static int displayAlertYESNOCANCEL(Component parent, String title, String message) {
        return XJAlert.createInstance().displayAlert(parent, title, message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
    }

    public static int displayAlert(Component parent, String title, String message, String b1, String b2, int def, int cancel) {
        return XJAlert.createInstance().displayCustomAlert(parent, title, message, new String[] { b1, b2}, def, cancel);
    }

    public static int displayAlert(Component parent, String title, String message, String b1, String b2, String b3, int def, int cancel) {
        return XJAlert.createInstance().displayCustomAlert(parent, title, message, new String[] { b1, b2, b3 }, def, cancel);
    }

    public void showSimple(Component parent, String title, String message) {
        displayAlert(parent, title, message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
    }

    public int showCustom(Component parent, String title, String message, String b1, String b2, int def, int cancel) {
        return displayCustomAlert(parent, title, message, new String[] { b1, b2}, def, cancel);
    }

    public static void setDefaultAlertIcon(Icon icon) {
        XJAlert.icon = icon;
    }

    public void setDisplayDoNotShowAgainButton(boolean displayDoNotShowAgainButton) {
        this.displayDoNotShowAgainButton = displayDoNotShowAgainButton;
    }

    public boolean isDoNotShowAgain() {
        return doNotShowAgainButton.isSelected();
    }
    
    public int displayCustomAlert(Component parent, String title, String message, String[] buttons, int def, int cancel) {
        if(XJSystem.isMacOS()) {
            String [] reverse = new String[buttons.length];
            for(int i=0; i<buttons.length; i++) {
                reverse[i] = buttons[buttons.length-i-1];
            }
            buttons = reverse;
            def = buttons.length-def-1;
            cancel = buttons.length-cancel-1;
        }
        int result = displayAlert(parent, title, message, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                buttons, buttons[def], buttons[cancel]);
        if(XJSystem.isMacOS()) {
            return buttons.length-result-1;
        } else
            return result;
    }

    private int displayAlert(Component parent, String title, String message, int messageType, int optionType) {
        return displayAlert(parent, title, message, messageType, optionType, null, null, null);
    }

    private int displayAlert(Component parent, String title, String message, int messageType, int optionType,
                                    Object[] options, Object defaultValue, Object escapeValue) {
        AlertOptionPane pane = new AlertOptionPane(message, messageType, optionType);
        if(options != null) {
            pane.setOptions(options);
            pane.setInitialValue(defaultValue);
        }
        if(icon != null) {
            pane.setIcon(icon);
        }
        JDialog dialog = pane.createDialog(getParent(parent), title);
        if(displayDoNotShowAgainButton) {
            doNotShowAgainButton = new JCheckBox("Do not show this alert again");
            dialog.getContentPane().add(doNotShowAgainButton, BorderLayout.SOUTH);
        }
        dialog.pack();
        dialog.setVisible(true);
        Object value = pane.getValue();
        if(value == null) {
            // Alert closed by the user
            return JOptionPane.CANCEL_OPTION;
        }
        if(options != null) {
            // Escape key returns an Integer with value -1
            if(value instanceof Integer && ((Integer)value) == -1) {
                return Arrays.asList(options).indexOf(escapeValue);
            }
            return Arrays.asList(options).indexOf(value);
        } else {
            return (Integer)value;
        }
    }


    private static class AlertOptionPane extends JOptionPane {

        public AlertOptionPane(Object o, int i, int i1) {
            super(o, i, i1);
        }

        @Override
        public int getMaxCharactersPerLineCount() {
            return 100;
        }

    }

}
