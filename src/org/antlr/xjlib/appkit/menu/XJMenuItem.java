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

package org.antlr.xjlib.appkit.menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class XJMenuItem {

    protected JMenuItem jMenuItem = null;

    protected int tag = 0;
    protected Object object = null;
    protected XJMenuItemDelegate delegate = null;

    protected XJMenu parentMenu = null;

    public XJMenuItem() {
        init();
    }

    public XJMenuItem(String title, int accelerator, int tag, XJMenuItemDelegate delegate) {
        init();
        setTag(tag);
        setTitle(title);
        setAccelerator(accelerator);
        setDelegate(delegate);
    }

    public XJMenuItem(String title, Icon icon, int accelerator, int tag, XJMenuItemDelegate delegate) {
        init();
        setTag(tag);
        setTitle(title);
        setIcon(icon);
        setAccelerator(accelerator);
        setDelegate(delegate);
    }

    public XJMenuItem(String title, int accelerator, int modifiers, int tag, XJMenuItemDelegate delegate) {
        init();
        setTag(tag);
        setTitle(title);
        setAccelerator(accelerator, modifiers);
        setDelegate(delegate);
    }

    public XJMenuItem(String title, int tag, XJMenuItemDelegate delegate) {
        init();
        setTag(tag);
        setTitle(title);
        setDelegate(delegate);
    }

    public void init() {
        jMenuItem = new JMenuItem();
        jMenuItem.addActionListener(new MenuActionListener());
    }

    public void setMenu(XJMenu menu) {
        this.parentMenu = menu;
    }

    public void setDelegate(XJMenuItemDelegate delegate) {
        this.delegate = delegate;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setTitle(String title) {
        jMenuItem.setText(title);
    }

    public String getTitle() {
        return jMenuItem.getText();
    }

    public void setEnabled(boolean enabled) {
        boolean previous = isEnabled();
        if(enabled && !previous || !enabled && previous)
            jMenuItem.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return jMenuItem.isEnabled();
    }

    public void setSelected(boolean selected) {
        boolean previous = isSelected();
        if(selected && !previous || !selected && previous)
            jMenuItem.setSelected(selected);
    }

    public boolean isSelected() {
        return jMenuItem.isSelected();
    }

    public void setAccelerator(int keystroke) {
        int keyModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        jMenuItem.setAccelerator(KeyStroke.getKeyStroke(keystroke, keyModifier));
    }

    public void setAccelerator(int keystroke, int modifiers) {
        jMenuItem.setAccelerator(KeyStroke.getKeyStroke(keystroke, modifiers));
    }

    public void setIcon(Icon icon) {
        jMenuItem.setIcon(icon);
    }

    public JComponent getSwingComponent() {
        return jMenuItem;
    }

    public static int getKeyModifier() {
        return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }

    public class MenuActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(delegate != null)
                delegate.handleMenuEvent(parentMenu, XJMenuItem.this);
        }
    }

}
