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
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XJMenu extends XJMenuItem {

    protected JMenu jMenu = new JMenu();
    protected List<XJMenuItem> items = new ArrayList<XJMenuItem>();
    protected XJMainMenuBar mainMenuBar;

    public XJMenu() {
        jMenu.addMenuListener(new XJMenuListener());
    }

    public void setMainMenuBar(XJMainMenuBar mainMenuBar) {
        this.mainMenuBar = mainMenuBar;
        for(int index=0; index<items.size(); index++) {
            Object item = items.get(index);
            if(item instanceof XJMenu) {       
                XJMenu menu = (XJMenu)item;
                menu.setMainMenuBar(mainMenuBar);
            }
        }
    }

    public void setTitle(String title) {
        jMenu.setText(title);
    }

    public void addSeparator() {
        items.add(new XJMenuItemSeparator());
        jMenu.addSeparator();
    }

    public void addItem(XJMenuItem item) {
        item.setMenu(this);
        items.add(item);
        jMenu.add(item.getSwingComponent());
    }

    public void addItem(XJMenu menu) {
        menu.setMenu(this);
        items.add(menu);
        jMenu.add(menu.getSwingComponent());
    }

    public void insertSeparatorBefore(int menuItemTag) {
        XJMenuItem menuItem = getItemForTag(menuItemTag);
        if(menuItem == null)
            return;

        insertSeparatorAtIndex(items.indexOf(menuItem));
    }

    public void insertSeparatorAfter(int menuItemTag) {
        XJMenuItem menuItem = getItemForTag(menuItemTag);
        if(menuItem == null)
            return;

        insertSeparatorAtIndex(items.indexOf(menuItem)+1);
    }

    public void insertSeparatorAtIndex(int index) {
        items.add(index, new XJMenuItemSeparator());
        jMenu.insertSeparator(index);
    }

    public void insertItemBefore(XJMenuItem item, int menuItemTag) {
        XJMenuItem menuItem = getItemForTag(menuItemTag);
        if(menuItem == null)
            return;

        insertItemAtIndex(item, items.indexOf(menuItem));
    }

    public void insertItemAfter(XJMenuItem item, int menuItemTag) {
        XJMenuItem menuItem = getItemForTag(menuItemTag);
        if(menuItem == null)
            return;

        insertItemAtIndex(item, items.indexOf(menuItem)+1);
    }

    public void insertItemAtIndex(XJMenuItem item, int index) {
        items.add(index, item);
        jMenu.add(item.getSwingComponent(), index);
    }

    public void removeItem(int index) {
        jMenu.remove(index);
    }

    public XJMenuItem getItemForTag(int tag) {
        Iterator<XJMenuItem> iterator = items.iterator();
        while(iterator.hasNext()) {
            XJMenuItem menuItem = iterator.next();
            if(menuItem.getTag() == tag)
                return menuItem;
        }
        return null;
    }

    public XJMenuItem getItemAtIndex(int index) {
        if(index >=0 && index < items.size())
            return items.get(index);
        else
            return null;
    }

    public int getItemCount() {
        return jMenu.getItemCount();
    }

    public Iterator<XJMenuItem> itemIterator() {
        return items.iterator();
    }

    public void clear() {
        items.clear();
        jMenu.removeAll();
    }

    public JComponent getSwingComponent() {
        return jMenu;
    }

    public class XJMenuListener implements MenuListener {

        public void menuSelected(MenuEvent e) {
            if(mainMenuBar != null)
                mainMenuBar.menuSelected(XJMenu.this);
        }

        public void menuDeselected(MenuEvent e) {
        }

        public void menuCanceled(MenuEvent e) {
        }
    }
}
