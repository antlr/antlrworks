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

import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.appkit.frame.XJWindow;
import org.antlr.xjlib.appkit.undo.XJUndo;
import org.antlr.xjlib.appkit.utils.XJLocalizable;
import org.antlr.xjlib.foundation.XJSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XJMainMenuBar implements XJMenuItemDelegate {

    public static final int MI_NEW = 10000;
    public static final int MI_OPEN = 10001;
    public static final int MI_SAVE = 10002;
    public static final int MI_SAVEAS = 10003;
    public static final int MI_CLOSE = 10004;
    public static final int MI_QUIT = 10005;
    public static final int MI_HELP = 10020;
    public static final int MI_ABOUT = 10021;
    public static final int MI_PREFS = 10022;
    public static final int MI_UNDO = 10023;
    public static final int MI_REDO = 10024;
    public static final int MI_CUT = 10025;
    public static final int MI_COPY = 10026;
    public static final int MI_PASTE = 10027;
    public static final int MI_SELECT_ALL = 10028;
    public static final int MI_GC = 11000;

    public static final int MI_CLEAR_RECENT_FILES = 20000;
    public static final int MI_RECENT_FILES = 20001; // + any number of recent files!

    public static final int MI_NO_WINDOW = 21000;
    public static final int MI_WINDOW = 21001; // + any number of windows!

    // !!! Do not use any number above MI_WINDOW !!!

    protected JMenuBar menuBar = null;
    protected XJMenu menuFile = null;
    protected XJMenu menuRecentFiles = null;
    protected XJMenu menuEdit = null;
    protected XJMenu menuWindow = null;
    protected XJMenu menuHelp = null;

    protected XJMenuItem menuItemUndo = null;
    protected XJMenuItem menuItemRedo = null;

    protected XJMenuBarCustomizer customizer = null;
    protected XJMenuBarDelegate delegate = null;

    protected List<XJMenu> customMenus = new ArrayList<XJMenu>();

    protected static List<XJMainMenuBar> mmbs = new ArrayList<XJMainMenuBar>();

    public synchronized static XJMainMenuBar createInstance() {
        XJMainMenuBar mmb = new XJMainMenuBar();
        mmbs.add(mmb);
        return mmb;
    }

    public synchronized static void removeInstance(XJMainMenuBar mmb) {
        mmbs.remove(mmb);
    }

    public synchronized static void refreshAllRecentFilesMenu() {
        for (XJMainMenuBar mmb : mmbs) {
            mmb.rebuildRecentFilesMenu();
        }
    }

    public synchronized static void refreshAllMenuBars() {
        for (XJMainMenuBar mmb : mmbs) {
            mmb.refresh();
        }
    }

    public synchronized static void setActiveWindowToAllMenuBar(XJWindow window) {
        for (XJMainMenuBar mmb : mmbs) {
            mmb.setActiveWindow(window);
        }
    }

    public static boolean isRecentFilesItem(XJMenuItem item) {
        return item.getTag() >= XJMainMenuBar.MI_CLEAR_RECENT_FILES && item.getTag() < XJMainMenuBar.MI_NO_WINDOW;
    }
    
    public XJMainMenuBar() {
    }

    public void setCustomizer(XJMenuBarCustomizer customizer) {
        this.customizer = customizer;
    }

    public void setDelegate(XJMenuBarDelegate delegate) {
        this.delegate = delegate;
    }

    public JMenuBar getJMenuBar() {
        return menuBar;
    }

    public XJMenu getFileMenu() {
        return menuFile;
    }

    public void refresh() {
        refreshContent();
        refreshState();
    }

    public void refreshState() {
        refreshMenuState(menuFile);
        refreshMenuState(menuEdit);
        refreshMenuState(menuHelp);

        for (XJMenu customMenu : customMenus) refreshMenuState(customMenu);
    }

    public void refreshMenuEditState() {
        refreshMenuState(menuEdit);
    }

    public void refreshMenuState(XJMenu menu) {
        if(menu == null)
            return;
        
        for(int i=0; i<menu.getItemCount(); i++) {
            XJMenuItem item = menu.getItemAtIndex(i);
            if(item instanceof XJMenu)
                refreshMenuState((XJMenu)item);
            else
                refreshMenuItemState(item);
        }
    }

    public void refreshMenuItemState(XJMenuItem item) {
        if(delegate != null)
            delegate.menuItemState(item);
    }

    public void refreshContent() {
        rebuildRecentFilesMenu();
        rebuildWindowMenu();
    }

    public void menuUndoRedoItemState(XJUndo undo) {
        if(undo == null) {
            menuItemUndo.setEnabled(false);
            menuItemRedo.setEnabled(false);
        } else {
            menuItemUndo.setEnabled(undo.canUndo());
            menuItemRedo.setEnabled(undo.canRedo());
        }
    }

    public void setActiveWindow(XJWindow window) {
        if(menuWindow == null)
            return;

        int index = XJApplication.shared().getWindows().indexOf(window);
        if(index>=0) {
            XJMenuItem item = menuWindow.getItemAtIndex(index);
            if(item != null)
                item.setSelected(true);
        }
    }

    public void setupMenuItem(XJMenuItem item, String name, int keystroke, int modifiers, int tag) {
        item.setTitle(name);
        item.setTag(tag);

        if(keystroke > 0 && modifiers > 0)
            item.setAccelerator(keystroke, modifiers);
        else if(keystroke >0)
            item.setAccelerator(keystroke);

        item.setDelegate(this);
    }

    public XJMenuItem buildMenuItem(String name, int keystroke, int modifiers, int tag) {
        XJMenuItem item = new XJMenuItem();
        setupMenuItem(item, name, keystroke, modifiers, tag);
        return item;
    }

    public XJMenuItem buildMenuItem(String name, int keystroke, int tag) {
        XJMenuItem item = new XJMenuItem();
        setupMenuItem(item, name, keystroke, -1, tag);
        return item;
    }

    public XJMenuItemCheck buildMenuItemCheck(String name, int keystroke, int tag) {
        XJMenuItemCheck item = new XJMenuItemCheck();
        setupMenuItem(item, name, keystroke, -1, tag);
        return item;
    }

    public XJMenuItem buildMenuItem(String name, int tag) {
        return buildMenuItem(name, -1, tag);
    }

    public static final int IGNORE_FILEMENU = 1;
    public static final int IGNORE_EDITMENU = 2;
    public static final int IGNORE_WINDOWMENU = 4;
    public static final int IGNORE_HELPMENU = 8;

    public void createMenuBar() {
        createMenuBar(0);
    }

    public void createMenuBar(int ignore) {
        customMenus.clear();

        menuBar = new JMenuBar();

        if((ignore & IGNORE_FILEMENU) == 0)
            addMenu(createFileMenu());
        if((ignore & IGNORE_EDITMENU) == 0)
            addMenu(createEditMenu());

        // Customization between menu Edit and menu Help
        if(customizer != null)
            customizer.customizeMenuBar(this);

        if((ignore & IGNORE_WINDOWMENU) == 0)
            addMenu(createWindowMenu());
        if((ignore & IGNORE_HELPMENU) == 0)
            addMenu(createHelpMenu());

        if("true".equals(System.getProperty("org.antlr.xjlib.debug.menu"))) {
            createDebugMenu();
        }
    }

    private void createDebugMenu() {
        XJMenu menu;
        menu = new XJMenu();
        menu.setTitle("*");
        menu.addItem(buildMenuItem("Garbage Collector", MI_GC));
        addMenu(menu);
    }

    public XJMenu createFileMenu() {
        XJMenu menu = buildFileMenu();
        if(customizer != null)
            customizer.customizeFileMenu(menu);
        XJApplication.getAppDelegate().customizeFileMenu(menu);
        return menu;
    }

    public XJMenu createEditMenu() {
        XJMenu menu = buildEditMenu();
        if(customizer != null)
            customizer.customizeEditMenu(menu);
        XJApplication.getAppDelegate().customizeEditMenu(menu);
        return menu;
    }

    public XJMenu createWindowMenu() {
        XJMenu menu = buildWindowMenu();
        if(customizer != null)
            customizer.customizeWindowMenu(menu);
        XJApplication.getAppDelegate().customizeWindowMenu(menu);
        return menu;
    }

    public XJMenu createHelpMenu() {
        XJMenu menu = buildHelpMenu();
        if(customizer != null)
            customizer.customizeHelpMenu(menu);
        XJApplication.getAppDelegate().customizeHelpMenu(menu);
        return menu;
    }

    public void addCustomMenu(XJMenu menu) {
        if(menu == null)
            return;

        customMenus.add(menu);
        addMenu(menu);
    }

    private void addMenu(XJMenu menu) {
        menuBar.add(menu.getSwingComponent());
        menu.setMainMenuBar(this);
    }

    private XJMenu buildFileMenu() {
        boolean persistence = XJApplication.shared().supportsPersistence();

        menuFile = new XJMenu();
        menuFile.setTitle(XJLocalizable.getXJString("File"));
        menuFile.addItem(buildMenuItem(XJLocalizable.getXJString("New"), KeyEvent.VK_N, MI_NEW));
        if(persistence) {
            menuFile.addItem(buildMenuItem(XJLocalizable.getXJString("Open"), KeyEvent.VK_O, MI_OPEN));
            menuFile.addItem(createRecentFilesMenu());
        }
        menuFile.addSeparator();
        menuFile.addItem(buildMenuItem(XJLocalizable.getXJString("Close"), KeyEvent.VK_W, MI_CLOSE));
        if(persistence) {
            menuFile.addItem(buildMenuItem(XJLocalizable.getXJString("Save"), KeyEvent.VK_S, MI_SAVE));
            menuFile.addItem(buildMenuItem(XJLocalizable.getXJString("SaveAs"), MI_SAVEAS));
        }

        if(!XJSystem.isMacOS()) {
            menuFile.addSeparator();
            if(XJApplication.shared().hasPreferencesMenuItem()) {
                menuFile.addItem(buildMenuItem(XJLocalizable.getXJString("Preferences"), KeyEvent.VK_COMMA, MI_PREFS));
                menuFile.addSeparator();                
            }
            menuFile.addItem(buildMenuItem(XJLocalizable.getXJString("Quit"), KeyEvent.VK_Q, MI_QUIT));
        }
        return menuFile;
    }

    public XJMenu createRecentFilesMenu() {
        menuRecentFiles = new XJMenu();
        rebuildRecentFilesMenu();
        return menuRecentFiles;
    }

    public void rebuildRecentFilesMenu() {
        if(menuRecentFiles == null)
            return;

        menuRecentFiles.clear();
        menuRecentFiles.setTitle(XJLocalizable.getXJString("OpenRecent"));

        int f = 0;
        for (Object o : XJApplication.shared().recentFiles()) {
            menuRecentFiles.addItem(buildMenuItem((String) o, MI_RECENT_FILES + f++));
        }
        menuRecentFiles.addSeparator();
        menuRecentFiles.addItem(buildMenuItem(XJLocalizable.getXJString("ClearMenu"), MI_CLEAR_RECENT_FILES));
    }

    private XJMenu buildEditMenu() {
        menuEdit = new XJMenu();
        menuEdit.setTitle(XJLocalizable.getXJString("Edit"));
        menuEdit.addItem(menuItemUndo = buildMenuItem(XJLocalizable.getXJString("Undo"), KeyEvent.VK_Z, MI_UNDO));
        menuEdit.addItem(menuItemRedo = buildMenuItem(XJLocalizable.getXJString("Redo"), KeyEvent.VK_Z, XJMenuItem.getKeyModifier() | Event.SHIFT_MASK, MI_REDO));
        menuEdit.addSeparator();
        menuEdit.addItem(buildMenuItem(XJLocalizable.getXJString("Cut"), KeyEvent.VK_X, MI_CUT));
        menuEdit.addItem(buildMenuItem(XJLocalizable.getXJString("Copy"), KeyEvent.VK_C, MI_COPY));
        menuEdit.addItem(buildMenuItem(XJLocalizable.getXJString("Paste"), KeyEvent.VK_V, MI_PASTE));
        menuEdit.addSeparator();
        menuEdit.addItem(buildMenuItem(XJLocalizable.getXJString("SelectAll"), KeyEvent.VK_A, MI_SELECT_ALL));
        return menuEdit;
    }

    private void buildWindowMenu_() {
        Iterator iterator = XJApplication.shared().getWindows().iterator();
        int count = 0;
        while(iterator.hasNext()) {
            XJWindow window = (XJWindow)iterator.next();
            if(window.shouldAppearsInWindowMenu()) {
                XJMenuItemCheck item = buildMenuItemCheck(window.getTitle(), count<10?KeyEvent.VK_0+count:-1, MI_WINDOW+count);
                item.setSelected(window.isActive());
                menuWindow.addItem(item);
                count++;
            }
        }

        if(count == 0) {
            XJMenuItem item = new XJMenuItem(XJLocalizable.getXJString("NoWindows"), MI_NO_WINDOW, null);
            item.setEnabled(false);
            menuWindow.addItem(item);
        }
    }

    private XJMenu buildWindowMenu() {
        menuWindow = new XJMenu();
        menuWindow.setTitle(XJLocalizable.getXJString("Window"));
        buildWindowMenu_();
        return menuWindow;
    }

    private void rebuildWindowMenu() {
        if(menuWindow == null)
            return;

        for(int index = menuWindow.getItemCount()-1; index>=0; index--) {
            XJMenuItem item = menuWindow.getItemAtIndex(index);
            if(item != null && item.getTag() >= MI_NO_WINDOW)
                menuWindow.removeItem(index);
        }
        buildWindowMenu_();
    }

    private XJMenu buildHelpMenu() {
        menuHelp = new XJMenu();
        menuHelp.setTitle(XJLocalizable.getXJString("Help"));
        menuHelp.addItem(buildMenuItem(XJLocalizable.getXJString("Help"), MI_HELP));
        if(!XJSystem.isMacOS()) {
            menuHelp.addSeparator();
            menuHelp.addItem(buildMenuItem(XJLocalizable.getXJString("About"), MI_ABOUT));
        }
        return menuHelp;
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        //XJDocument document = XJApplication.shared().getActiveDocument();
        XJWindow activeWindow = XJApplication.shared().getActiveWindow();
        switch(item.tag) {
            case MI_NEW:
                XJApplication.shared().newDocument();
                break;

            case MI_OPEN:
                XJApplication.shared().openDocument();
                break;

            case MI_SAVE:
                if(activeWindow != null) {
                    for(XJDocument doc : activeWindow.getDocuments()) {
                        if(doc.save(false)) {
                            doc.changeReset();
                        }
                    }
                }
                break;

            case MI_SAVEAS:
                if(activeWindow != null) {
                    for(XJDocument doc : activeWindow.getDocuments()) {
                        if(doc.save(true)) {
                            doc.changeReset();
                        }
                    }
                }
                break;

            case MI_CLEAR_RECENT_FILES:
                XJApplication.shared().clearRecentFiles();
                break;

            case MI_QUIT:
                XJApplication.shared().performQuit();
                break;

            case MI_PREFS:
                XJApplication.shared().displayPrefs();
                break;

            case MI_ABOUT:
                XJApplication.shared().displayAbout();
                break;

            case MI_HELP:
                XJApplication.shared().displayHelp();
                break;

            case MI_GC:
                System.gc();
                break;
            
            default:
                if(item.tag>=MI_WINDOW) {
                    XJWindow window = XJApplication.shared().getWindowsInWindowMenu().get(item.tag-MI_WINDOW);
                    window.bringToFront();
                    item.setSelected(true);
                } else if(item.tag>=MI_RECENT_FILES && item.tag<=MI_RECENT_FILES+XJApplication.MAX_RECENT_FILES) {
                    if(!XJApplication.shared().openDocument(item.getTitle())) {
                        XJApplication.shared().removeRecentFile(item.getTitle());
                    }
                } else if(delegate != null)
                    delegate.handleMenuEvent(menu, item);
                break;                
        }
    }

    public void menuSelected(XJMenu menu) {
        if(delegate != null)
            delegate.handleMenuSelected(menu);
    }
}
