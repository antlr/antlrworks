package org.antlr.xjlib.appkit.frame;

import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuBarDelegate;
import org.antlr.xjlib.appkit.menu.XJMenuItem;
import org.antlr.xjlib.appkit.utils.XJLocalizable;

/**
 * @author Copyright (c) 2008 by BEA Systems, Inc. All Rights Reserved.
*/
public class XJInternalFrameHandling implements XJMenuBarDelegate {

    public XJInternalFrameHandling() {
        XJFrame.desktopDefaultMenuBar = XJMainMenuBar.createInstance();
        XJFrame.desktopDefaultMenuBar.createMenuBar();
        XJFrame.desktopDefaultMenuBar.setDelegate(this);
        XJFrame.desktopDefaultMenuBar.refreshState();
    }

    public void menuItemState(XJMenuItem item) {
        int tag = item.getTag();
        switch(tag) {
            case XJMainMenuBar.MI_NEW:
                item.setTitle(XJLocalizable.getXJString("New")+((XJApplication.shared().getDocumentExtensions().size()>1)?"...":""));

            case XJMainMenuBar.MI_OPEN:
            case XJMainMenuBar.MI_RECENT_FILES:
            case XJMainMenuBar.MI_CLEAR_RECENT_FILES:
                item.setEnabled(true);
                break;

            case XJMainMenuBar.MI_QUIT:
                item.setEnabled(true);
                break;
            
            default:
                if(XJMainMenuBar.isRecentFilesItem(item))
                    item.setEnabled(true);
                else
                    item.setEnabled(false);
                break;
        }
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {

    }

    public void handleMenuSelected(XJMenu menu) {

    }
}
