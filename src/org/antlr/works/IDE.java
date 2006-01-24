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
import edu.usfca.xj.appkit.document.XJDataXML;
import edu.usfca.xj.appkit.frame.XJPanel;
import edu.usfca.xj.appkit.menu.XJMainMenuBar;
import edu.usfca.xj.appkit.menu.XJMenu;
import edu.usfca.xj.appkit.menu.XJMenuItem;
import edu.usfca.xj.appkit.menu.XJMenuItemDelegate;
import edu.usfca.xj.appkit.swing.XJLookAndFeel;
import edu.usfca.xj.appkit.utils.BrowserLauncher;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.foundation.XJSystem;
import edu.usfca.xj.foundation.XJUtils;
import org.antlr.Tool;
import org.antlr.tool.ErrorManager;
import org.antlr.works.components.grammar.CContainerGrammar;
import org.antlr.works.components.grammar.CDocumentGrammar;
import org.antlr.works.components.project.CContainerProject;
import org.antlr.works.components.project.CDocumentProject;
import org.antlr.works.dialog.DialogAbout;
import org.antlr.works.dialog.DialogPersonalInfo;
import org.antlr.works.editor.EditorMenu;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.prefs.AWPrefsDialog;
import org.antlr.works.stats.Statistics;
import org.antlr.works.utils.HelpManager;
import org.antlr.works.utils.Localizable;
import org.antlr.works.utils.Utils;

import javax.swing.*;
import java.io.*;
import java.net.URL;

public class IDE extends XJApplicationDelegate implements XJMenuItemDelegate {

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

        XJLookAndFeel.applyLookAndFeel(AWPrefs.getLookAndFeel());
        XJApplication.addDocumentType(CDocumentGrammar.class, CContainerGrammar.class, XJDataPlainText.class, "g", Localizable.getLocalizedString(Localizable.DOCUMENT_TYPE));
        XJApplication.addDocumentType(CDocumentProject.class, CContainerProject.class, XJDataXML.class, "awp", Localizable.getLocalizedString(Localizable.PROJECT_TYPE));

        XJApplication.addScheduledTimer(new HelpManager(), 1, true);

        AWPrefsDialog.applyCommonPrefs();

        registerUser();
        checkLibraries();
        checkEnvironment();

        if(args.length == 2 && args[0].equals("-f")) {
            XJApplication.shared().openDocument(args[1]);
        } else {
            switch (AWPrefs.getStartupAction()) {
                case AWPrefs.STARTUP_NEW_DOC:
                    sc.setVisible(false);
                    XJApplication.shared().newDocument();
                    break;
                case AWPrefs.STARTUP_OPEN_LAST_DOC:
                    if (XJApplication.shared().getDocuments().size() == 0) {
                        if (!XJApplication.shared().openLastUsedDocument()) {
                            sc.setVisible(false);
                            XJApplication.shared().newDocument();
                        }
                    }
                    break;
            }
        }

        sc.setVisible(false);
    }

    public void registerUser() {
        if(!AWPrefs.isUserRegistered()) {
            sc.setVisible(false);
            AWPrefs.setServerID("");
            new DialogPersonalInfo(null).runModal();
            AWPrefs.setUserRegistered(true);
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
        } catch (NoClassDefFoundError e) {
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

    public void checkEnvironment() {
        String tempFile = XJUtils.concatPath(XJSystem.getTempDir(), "redirect.err");

        // Make sure the temporary directory exists
        File f = new File(XJSystem.getTempDir());
        if(!f.exists())
            f.mkdirs();

        PrintStream originalErr = System.err;
        PrintStream ps = null;
        try {
            try {
                ps = new PrintStream(new FileOutputStream(tempFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                // Give up if we cannot open the redirect file
                System.err.println("Cannot check if ANTLR is correctly installed. Continue...");
                return;
            }
            System.setErr(ps);
            ErrorManager.setTool(new Tool());
            ErrorManager.setErrorListener(ErrorManager.getErrorListener());
        } catch(Error error) {
            String s = null;
            try {
                s = Utils.stringFromFile(tempFile);
            } catch (IOException e) {
                XJAlert.display(null, "Fatal Error", "ANTLRWorks will quit now because the installation of ANTLR is corrupted.");
                System.exit(0);
            }
            XJAlert.display(null, "Fatal Error", "ANTLRWorks will quit now because ANTLR has the following problem:\n"+s);
            System.exit(0);
        }

        // Restore the original output stream
        System.setErr(originalErr);

        // Close the previous one
        if(ps != null)
            ps.close();

        // Delete the redirect.err file
        new File(tempFile).delete();
    }

    public static String getApplicationPath() {
        String classPath = "org/antlr/works/IDE.class";
        URL url = XJApplication.getAppDelegate().getClass().getClassLoader().getResource(classPath);
        if(url == null)
            return null;

        String p = url.getPath();
        if(p.startsWith("file:"))
            p = p.substring("file:".length());

        p = p.substring(0, p.length()-classPath.length());
        if(p.endsWith("jar!/"))
            p = p.substring(0, p.length()-2);

        return p;
    }

    public void customizeHelpMenu(XJMenu menu) {
        menu.insertItemAfter(new XJMenuItem("Check for Updates", EditorMenu.MI_CHECK_UPDATES, this), XJMainMenuBar.MI_HELP);
        menu.insertItemAfter(new XJMenuItem("Send Feedback", EditorMenu.MI_SEND_FEEDBACK, this), XJMainMenuBar.MI_HELP);
        menu.insertItemAfter(new XJMenuItem("Submit Statistics...", EditorMenu.MI_SUBMIT_STATS, this), XJMainMenuBar.MI_HELP);
        menu.insertSeparatorAfter(XJMainMenuBar.MI_HELP);
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        switch(item.getTag()) {
            case EditorMenu.MI_SUBMIT_STATS:
                HelpManager.submitStats(XJApplication.shared().getActiveWindow().getJavaContainer());
                break;
            case EditorMenu.MI_SEND_FEEDBACK:
                HelpManager.sendFeedback(XJApplication.shared().getActiveWindow().getJavaContainer());
                break;
            case EditorMenu.MI_CHECK_UPDATES:
                HelpManager.checkUpdates(XJApplication.shared().getActiveWindow().getJavaContainer(), false);
                break;
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
        return AWPrefsDialog.class;
    }

    public XJPanel appInstanciateAboutPanel() {
        return new DialogAbout();
    }

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
