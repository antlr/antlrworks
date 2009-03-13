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

package org.antlr.xjlib.appkit.app.MacOS;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuBarDelegate;
import org.antlr.xjlib.appkit.menu.XJMenuItem;
import org.antlr.xjlib.appkit.utils.XJLocalizable;
import org.antlr.xjlib.foundation.XJSystem;

import javax.swing.*;

public class XJApplicationMacOS extends XJApplication implements XJMenuBarDelegate {

    protected JFrame invisibleFrame = null;
    protected XJMainMenuBar mainMenuBar = null;

    public XJApplicationMacOS() {

        System.setProperty("apple.laf.useScreenMenuBar","true");

        Application.getApplication().addApplicationListener(new com.apple.eawt.ApplicationAdapter() {
            public void handleAbout(ApplicationEvent e) {
                displayAbout();
                e.setHandled(true);
            }

            public void handleOpenApplication(ApplicationEvent e) {
            }

            public void handleOpenFile(ApplicationEvent e) {
                openDocument(e.getFilename());
            }

            public void handlePreferences(ApplicationEvent e) {
                performPreferences();
            }

            public void handlePrintFile(ApplicationEvent e) {
            }

            public void handleQuit(ApplicationEvent e) {
                performQuit();
            }
        }); 
    }

    @Override
    protected void startup() {
        super.startup();
        if(!XJSystem.isHeadless()) {
            createFramelessMenuBar();            
        }
    }

    protected void addPreferencesMenuItem() {
        Application.getApplication().addPreferencesMenuItem();
        Application.getApplication().setEnabledPreferencesMenu(true);
    }

    protected void removePreferencesMenuItem() {
        Application.getApplication().removePreferencesMenuItem();
    }

    private void createFramelessMenuBar() {
        invisibleFrame = new JFrame();
        invisibleFrame.setUndecorated(true);

        mainMenuBar = XJMainMenuBar.createInstance();
        mainMenuBar.createMenuBar();
        mainMenuBar.setDelegate(this);
        mainMenuBar.refreshState();
        invisibleFrame.setJMenuBar(mainMenuBar.getJMenuBar());

        invisibleFrame.setLocation(0, 0);
        invisibleFrame.setSize(0, 0);
        invisibleFrame.pack();
        invisibleFrame.setVisible(true);
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
