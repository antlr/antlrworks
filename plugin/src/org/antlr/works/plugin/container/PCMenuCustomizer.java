package org.antlr.works.plugin.container;

import org.antlr.works.components.container.ComponentContainerGrammarMenu;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuBarCustomizer;
import org.antlr.xjlib.appkit.menu.XJMenuItem;
import org.antlr.xjlib.foundation.XJSystem;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
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

public class PCMenuCustomizer implements XJMenuBarCustomizer {

    private PluginWindow window;

    public PCMenuCustomizer(PluginWindow pluginWindow) {
        this.window = pluginWindow;
    }

    public void customizeFileMenu(XJMenu menu) {
    }

    public void customizeEditMenu(XJMenu menu) {
    }

    public void customizeWindowMenu(XJMenu menu) {
    }

    public void customizeHelpMenu(XJMenu menu) {
        if(XJSystem.isMacOS()) {
            // XJMainMenuBar adds the About item for non-MacOS system
            menu.insertItemAfter(new XJMenuItem("About", XJMainMenuBar.MI_ABOUT, window.getMenuHelpDelegate()), XJMainMenuBar.MI_HELP);
            menu.insertSeparatorAfter(XJMainMenuBar.MI_HELP);
        }
        menu.insertItemAfter(new XJMenuItem("Check for Updates", ComponentContainerGrammarMenu.MI_CHECK_UPDATES, window.getMenuHelpDelegate()), XJMainMenuBar.MI_HELP);
        menu.insertItemAfter(new XJMenuItem("Send Feedback", ComponentContainerGrammarMenu.MI_SEND_FEEDBACK, window.getMenuHelpDelegate()), XJMainMenuBar.MI_HELP);
        menu.insertItemAfter(new XJMenuItem("Submit Statistics...", ComponentContainerGrammarMenu.MI_SUBMIT_STATS, window.getMenuHelpDelegate()), XJMainMenuBar.MI_HELP);
        menu.insertSeparatorAfter(XJMainMenuBar.MI_HELP);
    }

    public void customizeMenuBar(XJMainMenuBar menubar) {
        window.getComponentContainer().customizeMenuBar(menubar);
    }
}
