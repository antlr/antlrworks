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

package org.antlr.works;

import edu.usfca.xj.appkit.app.XJApplication;
import edu.usfca.xj.appkit.app.XJApplicationDelegate;
import edu.usfca.xj.appkit.document.XJDataPlainText;
import edu.usfca.xj.appkit.frame.XJPanel;
import edu.usfca.xj.appkit.swing.XJLookAndFeel;
import edu.usfca.xj.appkit.utils.BrowserLauncher;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.foundation.XJSystem;
import org.antlr.works.dialog.DialogAbout;
import org.antlr.works.dialog.DialogPersonalInfo;
import org.antlr.works.dialog.DialogPrefs;
import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.stats.Statistics;
import org.antlr.works.util.HelpManager;
import org.antlr.works.util.Localizable;

import javax.swing.*;
import java.io.IOException;

public class IDE extends XJApplicationDelegate {

    public static SplashScreen sc;

    public static void main(String[] args) {

        // Needs to specify the Mac OS X property here (starting from Tiger)
        // before any other line of code (the usual XJApplication won't work
        // because we are instanciating a SplashScreen before it)

        XJSystem.setSystemProperties();
        XJApplication.setPropertiesPath("org/antlr/works/properties/");

        sc = new SplashScreen();

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    sc.setVisible(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        XJApplication.run(new IDE(), args);
    }

    public void appDidLaunch(String[] args) {

        XJLookAndFeel.applyLookAndFeel(EditorPreferences.getLookAndFeel());
        XJApplication.addDocumentType(Document.class, EditorWindow.class, XJDataPlainText.class, "g", Localizable.getLocalizedString(Localizable.DOCUMENT_TYPE));

        XJApplication.addScheduledTimer(new HelpManager(), 5*60, true);

        registerUser();
        checkLibraries();

        if(args.length == 2 && args[0].equals("-f")) {
            XJApplication.shared().openDocument(args[1]);
        } else {
            switch (EditorPreferences.getStartupAction()) {
                case EditorPreferences.STARTUP_NEW_DOC:
                    XJApplication.shared().newDocument();
                    break;
                case EditorPreferences.STARTUP_OPEN_LAST_DOC:
                    if (XJApplication.shared().getDocuments().size() == 0) {
                        if (XJApplication.shared().openLastUsedDocument() == false) {
                            XJApplication.shared().newDocument();
                        }
                    }
                    break;
            }
        }

        sc.setVisible(false);
    }

    public void registerUser() {
        if(!EditorPreferences.isUserRegistered()) {
            sc.setVisible(false);
            EditorPreferences.setServerID("");
            new DialogPersonalInfo(null).runModal();
            EditorPreferences.setUserRegistered(true);
        }
    }

    public void checkLibraries() {
        StringBuffer missing = new StringBuffer();

        try {
            Class.forName("org.antlr.Tool");
        } catch (ClassNotFoundException e) {
            missing.append("\n- ANTLR 3.x");
        }

        try {
            Class.forName("org.antlr.stringtemplate.StringTemplate");
        } catch (ClassNotFoundException e) {
            missing.append("\n- StringTemplate");
        }

        if(missing.length()>0) {
            sc.setVisible(false);
            missing.insert(0, "ANTLRWorks cannot find the following libraries:");
            missing.append("\nThey are required in order to use all the features of ANTLRWorks.\nDownload them from www.antlr.org and put them in the same directory as ANTLRWorks.");
            XJAlert.display(null, "Missing Libraries", missing.toString());
            System.exit(0);
        }
    }

    public void appShowHelp() {
        String url = Localizable.getLocalizedString(Localizable.DOCUMENTATION_URL);
        try {
            BrowserLauncher.openURL(url);
        } catch (IOException e) {
            XJAlert.display(null, "Cannot access the online help file", "Browse "+url+" to download the PDF manual.");
        }
        Statistics.shared().recordEvent(Statistics.EVENT_SHOW_HELP);
    }

    public void appWillTerminate() {
        Statistics.shared().close();
    }

    public Class appPreferencesPanelClass() {
        return DialogPrefs.class;
    }

    public XJPanel appInstanciateAboutPanel() {
        return new DialogAbout();
    };

    public boolean appHasPreferencesMenuItem() {
        return true;
    }

    public boolean appShouldQuitAfterLastWindowClosed() {
        return false;
    }

    public Class appPreferencesClass() {
        return IDE.class;
    }

    public String appVersionShort() {
        return Localizable.getLocalizedString(Localizable.APP_VERSION_SHORT);
    }

    public String appVersionLong() {
        return Localizable.getLocalizedString(Localizable.APP_VERSION_LONG);
    }
}
