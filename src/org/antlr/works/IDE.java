package org.antlr.works;

/*

[The "BSD licence"]
Copyright (c) 2004 Jean Bovet
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

import edu.usfca.xj.appkit.app.XJApplication;
import edu.usfca.xj.appkit.app.XJApplicationDelegate;
import edu.usfca.xj.appkit.document.XJDataPlainText;
import edu.usfca.xj.appkit.swing.XJLookAndFeel;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.utils.XJLocalizable;
import org.antlr.works.dialog.DialogPersonalInfo;
import org.antlr.works.dialog.DialogPrefs;
import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.stats.Statistics;
import org.antlr.works.util.HelpManager;

import javax.swing.*;

public class IDE extends XJApplicationDelegate {

    public static SplashScreen sc;

   /* private static Object  getBasicServiceObject ( )
    {
        try
        {
            Class  serviceManagerClass
                    = Class.forName ( "javax.jnlp.ServiceManager" );

            Method  lookupMethod = serviceManagerClass.getMethod (
                    "lookup", new Class [ ] { String.class } );

            return lookupMethod.invoke (
                    null, new Object [ ] { "javax.jnlp.BasicService" } );
        }
        catch ( Exception  ex )
        {
            return null;
        }
    }      */

    public static void main(String[] args) {

        // Needs to specify the Mac OS X property here (starting from Tiger)
        // before any other line of code (the usual XJApplication won't work
        // because we are instanciating a SplashScreen before it)

        System.setProperty("apple.laf.useScreenMenuBar","true");

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

        XJApplication.run(new IDE());
    }

    public void appDidLaunch() {

        XJLookAndFeel.applyLookAndFeel(EditorPreferences.getLookAndFeel());

        XJApplication.setPropertiesPath("org/antlr/works/properties/");
        XJApplication.addDocumentType(Document.class, EditorWindow.class, XJDataPlainText.class, "g", XJLocalizable.getString("strings", "GrammarDocumentType"));

        XJApplication.addScheduledTimer(new HelpManager(), 5*60, true);

        registerUser();
        checkLibraries();

        switch (EditorPreferences.getStartupAction()) {
            case EditorPreferences.STARTUP_NEW_DOC:
                XJApplication.shared().newDocument();
                break;
            case EditorPreferences.STARTUP_OPEN_LAST_DOC:
                if (XJApplication.shared().getDocuments().size() == 0) {
                    if (XJApplication.shared().openLastUsedDocument() == false)
                        XJApplication.shared().newDocument();
                }
                break;
        }

        sc.setVisible(false);
    }

    public void registerUser() {

        if(!EditorPreferences.isUserRegistered()) {
            sc.setVisible(false);
            new DialogPersonalInfo(XJApplication.getActiveContainer()).runModal();
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
            missing.insert(0, "ANTLRWorks cannot find the following libraries:");
            missing.append("\nThey are required in order to use all the features of ANTLRWorks.\nDownload them from www.antlr.org and put them in the same directory as ANTLRWorks.");
            XJAlert.display(XJApplication.getActiveContainer(), "Missing Libraries", missing.toString());
        }
    }

    public void appShowHelp() {
        Statistics.shared().recordEvent(Statistics.EVENT_SHOW_HELP);
    }

    public void appWillTerminate() {
        Statistics.shared().close();
    }

    public Class appPreferencesPanelClass() {
        return DialogPrefs.class;
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

}
