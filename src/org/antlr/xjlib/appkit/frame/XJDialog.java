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

package org.antlr.xjlib.appkit.frame;

import org.antlr.xjlib.appkit.XJControl;
import org.antlr.xjlib.appkit.app.XJApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class XJDialog extends XJControl {

    public static final int BUTTON_CANCEL = 0;
    public static final int BUTTON_OK = 1;

    protected JDialog jDialog = null;
    protected JButton defaultButton = null;
    protected int returnCode = 0;
    protected Container parent;

    public static Container resolveOwner(Container owner) {
        Container parent = owner==null?XJApplication.getActiveContainer():owner;
        if(owner instanceof Dialog || owner instanceof Frame)
            return parent;
        else if(owner != null)
            return SwingUtilities.getWindowAncestor(owner);
        else
            return null;
    }

    public XJDialog(Container owner, boolean modal) {
        parent = resolveOwner(owner);
        if(parent instanceof Dialog)
            jDialog = new JDialog((Dialog)parent);
        else if(parent instanceof Frame)
            jDialog = new JDialog((Frame)parent);
        else
            jDialog = new JDialog();
        jDialog.setModal(modal);
        jDialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowActivated(WindowEvent event) {
                dialogActivated();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                dialogWillCloseCancel();
            }
        });
        addEscapeHandling();
        setDefaultSize();
    }

    public void setDefaultCloseOperation(int operation) {
        jDialog.setDefaultCloseOperation(operation);
    }

    public Container getContentPane() {
        return jDialog.getContentPane();
    }

    public void setTitle(String title) {
        jDialog.setTitle(title);
    }

    public String getTitle() {
        return jDialog.getTitle();
    }

    public void setSize(int dx, int dy) {
        jDialog.setSize(dx, dy);
    }

    public void setSize(Dimension size) {
        jDialog.setSize(size);
    }

    public Dimension getSize() {
        return jDialog.getSize();
    }

    public void setDefaultSize() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        jDialog.setSize((int)(dim.width*0.5), (int)(dim.height*0.5));
    }

    public void setResizable(boolean flag) {
        jDialog.setResizable(flag);
    }

    public void setDefaultButton(JButton button) {
        this.defaultButton = button;
    }

    public void setOKButton(JButton button) {
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(dialogCanCloseOK()) {
                    dialogWillCloseOK();
                    closeWithReturnCode(BUTTON_OK);
                }
            }
        });
    }

    public void setCancelButton(JButton button) {
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dialogWillCloseCancel();
                closeWithReturnCode(BUTTON_CANCEL);
            }
        });
    }

    /** This method add the necessary code to handle the escape key
     * in order to close the dialog
     */
    public void addEscapeHandling() {
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);

        ActionListener cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                closeWithReturnCode(BUTTON_CANCEL);
            }
        };

        jDialog.getRootPane().registerKeyboardAction(cancelAction, "CancelAction", ks,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void setReturnCode(int code) {
        this.returnCode = code;
    }

    public void bringToFront() {
        jDialog.toFront();
    }

    public void center() {
        jDialog.setLocationRelativeTo(parent);
    }

    public void setPosition(int x, int y) {
        jDialog.setLocation(x, y);
    }

    public void setPosition(Point p) {
        jDialog.setLocation(p);
    }

    public Point getPosition() {
        return jDialog.getLocation();
    }

    public void offsetPosition(int dx, int dy) {
        Point p = jDialog.getLocation();
        jDialog.setLocation(p.x+dx, p.y+dy);
    }

    public Component getJavaComponent() {
        return jDialog;
    }

    public void closeWithReturnCode(int code) {
        setReturnCode(code);
        close();
    }

    public void close() {
        jDialog.dispose();
    }

    public void pack() {
        // do nothing
    }

    public boolean dialogCanCloseOK() {
        return true;
    }

    public void dialogWillDisplay() {

    }

    public void dialogActivated() {
        
    }
    
    public void dialogWillCloseCancel() {

    }

    public void dialogWillCloseOK() {

    }

    public void setVisible(boolean flag) {
        jDialog.setVisible(flag);
    }

    public void display() {
        center();
        jDialog.setVisible(true);
    }

    public int runModal() {
        center();

        dialogWillDisplay();
        if(defaultButton != null)
            jDialog.getRootPane().setDefaultButton(defaultButton);

        jDialog.setVisible(true);

        return returnCode;
    }
}
