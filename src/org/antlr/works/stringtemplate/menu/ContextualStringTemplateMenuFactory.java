package org.antlr.works.stringtemplate.menu;

import org.antlr.works.stringtemplate.STWindowMenu;
import org.antlr.xjlib.appkit.menu.XJMenuItem;

import javax.swing.*;
/*

[The "BSD licence"]
Copyright (c)
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

public class ContextualStringTemplateMenuFactory {

    public JPopupMenu menu = new JPopupMenu();
    public boolean shouldInsertSeparator = false;
    private STWindowMenu stWindowMenu;

    public ContextualStringTemplateMenuFactory(STWindowMenu stWindowMenu) {
        this.stWindowMenu = stWindowMenu;
    }

    public void addSeparator() {
        shouldInsertSeparator = true;
    }

    public XJMenuItem addItem(int tag) {
        if(shouldInsertSeparator) {
            menu.addSeparator();
            shouldInsertSeparator = false;
        }
        XJMenuItem item = stWindowMenu.createMenuItem(tag, true);
        menu.add(item.getSwingComponent());
        return item;
    }
}
