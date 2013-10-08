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

import org.antlr.Tool;
import org.antlr.tool.ErrorManager;
import org.antlr.works.components.GrammarDocument;
import org.antlr.works.components.GrammarDocumentFactory;
import org.antlr.works.components.GrammarWindow;
import org.antlr.works.components.GrammarWindowMenu;
import org.antlr.works.dialog.AWPrefsDialog;
import org.antlr.works.dialog.DialogAbout;
import org.antlr.works.dialog.DialogPersonalInfo;
import org.antlr.works.dialog.NewWizardDialog;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.stats.Statistics;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.stringtemplate.STDocumentFactory;
import org.antlr.works.stringtemplate.STWindow;
import org.antlr.works.utils.Console;
import org.antlr.works.utils.*;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.app.XJApplicationDelegate;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.appkit.frame.XJDialog;
import org.antlr.xjlib.appkit.frame.XJPanel;
import org.antlr.xjlib.appkit.frame.XJWindow;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuItem;
import org.antlr.xjlib.appkit.menu.XJMenuItemDelegate;
import org.antlr.xjlib.appkit.swing.XJLookAndFeel;
import org.antlr.xjlib.appkit.utils.BrowserLauncher;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.foundation.XJSystem;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class IDE extends XJApplicationDelegate implements XJMenuItemDelegate {

    public static final String PROPERTIES_PATH = "properties/";

    public static SplashScreen sc;

	public static final String VERSION;
	static {
		String version = IDE.class.getPackage().getImplementationVersion();
		VERSION = version != null ? version : "1.x";
	}

    public static void main(String[] args) {
        // Needs to specify the Mac OS X property here (starting from Tiger)
        // before any other line of code (the usual XJApplication won't work
        // because we are instanciating a SplashScreen before it)
        XJSystem.setSystemProperties();
        XJApplication.setPropertiesPath(PROPERTIES_PATH);
        XJAlert.setDefaultAlertIcon(IconManager.shared().getIconApplication64x64());
        
        if(args.length >= 1 && args[0].equals("-stats")) {
            XJApplication.run(new Statistics(), args, "Statistics");
        } else {
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
    }

    public void closeSplashScreen() {
        if(sc != null) {
            sc.setVisible(false);
            sc.dispose();
            sc = null;            
        }
    }

    @Override
    public void appDidLaunch(String[] args, List<String> documentsToOpenAtStartup) {
        AWPrefs.setLookAndFeel(XJLookAndFeel.applyLookAndFeel(AWPrefs.getLookAndFeel()));

        XJApplication.addDocumentFactory(new GrammarDocumentFactory(GrammarWindow.class));
        XJApplication.addDocumentFactory(new STDocumentFactory(STWindow.class));

        XJApplication.addScheduledTimer(new HelpManager(), 1, true);

        AWPrefsDialog.applyCommonPrefs();

        registerUser();
        checkLibraries();
        checkEnvironment();

        if(args.length >= 2 && args[0].equals("-f")) {
            XJApplication.shared().openDocument(args[1]);
        } else if(documentsToOpenAtStartup != null && documentsToOpenAtStartup.size() > 0) {
            XJApplication.shared().openDocuments(documentsToOpenAtStartup);
        } else {
            switch (AWPrefs.getStartupAction()) {
                case AWPrefs.STARTUP_NEW_DOC:
                    closeSplashScreen();
                    XJApplication.shared().newDocument();
                    break;

                case AWPrefs.STARTUP_OPEN_LAST_OPENED_DOC:
                    if(XJApplication.shared().getWindows().isEmpty()) {
                        if(!XJApplication.shared().openLastUsedDocument()) {
                            closeSplashScreen();
                            XJApplication.shared().newDocument();
                        }
                    }
                    break;

                case AWPrefs.STARTUP_OPEN_LAST_SAVED_DOC:
                    if(XJApplication.shared().getWindows().isEmpty()) {
                        if(!XJApplication.shared().openDocument(AWPrefs.getLastSavedDocument())) {
                            closeSplashScreen();
                            XJApplication.shared().newDocument();
                        }
                    }
                    break;

                case AWPrefs.STARTUP_OPEN_ALL_OPENED_DOC:
                    closeSplashScreen();
                    if(!restoreAllOpenedDocuments()) {
                        XJApplication.shared().newDocument();
                    }
                    break;
            }
        }

        closeSplashScreen();
    }

    public void registerUser() {
        if(!AWPrefs.isUserRegistered()) {
            closeSplashScreen();
            AWPrefs.setServerID("");
            new DialogPersonalInfo(null).runModal();
            AWPrefs.setUserRegistered(true);
        }
    }

    public void checkLibraries() {
        StringBuilder missing = new StringBuilder();

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
            closeSplashScreen();
            missing.insert(0, "ANTLRWorks cannot find the following libraries:");
            missing.append("\nThey are required in order to use all the features of ANTLRWorks.\nDownload them from www.antlr.org and put them in the same directory as ANTLRWorks.");
            XJAlert.display(null, "Missing Libraries", missing.toString());
            System.exit(0);
        }
    }

    public void checkEnvironment() {
        // todo give a hint in the message - like "check that no previous version of ANTLR is in your classpath..."
        // todo message for first-time user to go to tutorial
        ErrorListener el = ErrorListener.getThreadInstance();
        CheckStream bos = new CheckStream(System.err);
        PrintStream ps = new PrintStream(bos);
        PrintStream os = System.err;
        System.setErr(ps);
        try {
            ErrorManager.setTool(new Tool());
            ErrorManager.setErrorListener(el);
        } catch (Throwable e) {
            XJAlert.display(null, "Fatal Error", "ANTLRWorks will quit now because ANTLR reported an error:\n"+bos.getMessage());
            System.exit(0);
        }

        if(el.hasErrors()) {
            XJAlert.display(null, "Error", "ANTLRWorks will continue to launch but ANTLR reported an error:\n"+bos.getMessage());
        }
        el.clear();
        System.setErr(os);
        ps.close();
        ErrorManager.removeErrorListener();
    }

    private class CheckStream extends ByteArrayOutputStream {

        private PrintStream errorStream;
        private StringBuilder sb = new StringBuilder();

        public CheckStream(PrintStream errorStream) {
            this.errorStream = errorStream;
        }

        public synchronized void write(int b) {
            super.write(b);
            record();
        }

        public synchronized void write(byte b[], int off, int len) {
            super.write(b, off, len);
            record();
        }

        public synchronized void writeTo(OutputStream out) throws IOException {
            super.writeTo(out);
            record();
        }

        public void write(byte b[]) throws IOException {
            super.write(b);
            record();
        }

        private void record() {
            errorStream.println(toString());
            sb.append(toString());
            reset();
        }

        public String getMessage() {
            return sb.toString();
        }
    }

    public static String getApplicationPath() {
        Class c = XJApplication.getAppDelegate().getClass();
        URL url = c.getProtectionDomain().getCodeSource().getLocation();
        String p;
        if(url == null) {
            // url can be null in some situation (i.e. plugin in IntelliJ). Let's try another
            // way using getResource().
            // Warning: use only '/' for the resource name even on Windows because internally
            // Java uses '/'!
            // See AW-35
            String name = c.getName().replace('.', '/').concat(".class");
            url = c.getClassLoader().getResource(name);
            if(url == null) {
                System.err.println("IDE: unable to get the location of the XJApplicationDelegate");
                return null;
            } else {
                // Remove the class fully qualified path from the path
                p = url.getPath();
                p = p.substring(0, p.length()-name.length());
            }
        } else {
            p = url.getPath();
        }

        if(p.startsWith("jar:"))
            p = p.substring("jar:".length());

        if(p.startsWith("file:"))
            p = p.substring("file:".length());

        int index = p.lastIndexOf("!");
        if(index != -1)
            p = p.substring(0, index);

        if(XJSystem.isWindows()) {
            // Note: on Windows, we can have something like "/C:/Document..."
            if(p.charAt(0) == '/')
                p = p.substring(1);

            // Change all '/' to '\'
            StringBuilder sb = new StringBuilder(p);
            for(int i=0; i<sb.length(); i++) {
                if(sb.charAt(i) == '/')
                    sb.replace(i, i+1, "\\");
            }
            p = sb.toString();
        }

        // Replace all %20 with white space. Apparently inside IntelliJ 6, white space in a path
        // are replaced by %20 but not in IntelliJ 5
        p = p.replaceAll("%20", " ");

        return p;
    }

    public static void debugVerbose(Console console, Class c, String s) {
        if(AWPrefs.getDebugVerbose()) {
            String message = c.getName()+": "+s;
            if(console != null)
                console.println(message);
            System.out.println(message);
        }
    }

    public void customizeHelpMenu(XJMenu menu) {
        menu.insertItemAfter(new XJMenuItem("Check for Updates", GrammarWindowMenu.MI_CHECK_UPDATES, this), XJMainMenuBar.MI_HELP);
        menu.insertItemAfter(new XJMenuItem("Send Feedback", GrammarWindowMenu.MI_SEND_FEEDBACK, this), XJMainMenuBar.MI_HELP);
        menu.insertItemAfter(new XJMenuItem("Submit Statistics...", GrammarWindowMenu.MI_SUBMIT_STATS, this), XJMainMenuBar.MI_HELP);
        menu.insertSeparatorAfter(XJMainMenuBar.MI_HELP);
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        switch(item.getTag()) {
            case GrammarWindowMenu.MI_SUBMIT_STATS:
                submitStats(getDefaultParent());
                break;
            case GrammarWindowMenu.MI_SEND_FEEDBACK:
                submitFeedback(getDefaultParent());
                break;
            case GrammarWindowMenu.MI_CHECK_UPDATES:
                checkUpdates(getDefaultParent());
                break;
        }
    }

    public Container getDefaultParent() {
        return XJApplication.shared().getActiveWindow().getJavaContainer();
    }

    public static void checkUpdates(Container parent) {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_CHECK_FOR_UPDATES);
        HelpManager.checkUpdates(parent, false);
    }

    public static void submitFeedback(Container parent) {
        HelpManager.sendFeedback(parent);
    }

    public static void submitStats(Container parent) {
        HelpManager.submitStats(parent);
    }

    public static void showHelp(Container parent) {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_HELP);
        String url = Localizable.getLocalizedString(Localizable.DOCUMENTATION_URL);
        try {
            BrowserLauncher.openURL(url);
        } catch (IOException e) {
            XJAlert.display(parent, "Cannot access the online help", "An error occurred when accessing the online help:\n"+e.toString()+"\n\nBrowse the online help at "+url+".");
        }
    }

    public void appShowHelp() {
        showHelp(getDefaultParent());
    }

    public void appWillTerminate() {
        rememberAllOpenedDocuments();
        StatisticsAW.shared().close();
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

    public boolean useDesktopMode() {
        return AWPrefs.getUseDesktopMode();
    }

    public Class appPreferencesClass() {
        return IDE.class;
    }

    public String appName() {
        return Localizable.getLocalizedString(Localizable.APP_NAME)+" "+appVersionShort();
    }

    public String appVersionShort() {
        return VERSION;
    }

    public String appVersionLong() {
        return VERSION;
    }

    public boolean displayNewDocumentWizard(XJDocument document) {
        // only display for grammar (*.g) files
        if (document != null && document instanceof GrammarDocument) {
            NewWizardDialog dialog = new NewWizardDialog(document.getWindow().getJavaContainer());

            if(dialog.runModal() == XJDialog.BUTTON_OK) {
                ((GrammarWindow)document.getWindow()).loadText(dialog.getGeneratedText());
                return true;
            }
        }
        return false;
    }

    private boolean restoreAllOpenedDocuments() {
        List<String> documents = AWPrefs.getAllOpenedDocuments();
        if(documents == null) return false;

        boolean success = false;
        for (String docPath : documents) {
            if(XJApplication.shared().openDocument(docPath)) {
                success = true;
            }
        }
        return success;
    }

    private void rememberAllOpenedDocuments() {
        final List<String> docPath = new ArrayList<String>();
        for (XJWindow window : XJApplication.shared().getWindows()) {
            final XJDocument document = window.getDocument();
            if(XJApplication.handlesDocument(document)) {
                docPath.add(document.getDocumentPath());
            }
        }
        AWPrefs.setAllOpenedDocuments(docPath);
    }

    /** Localized resource bundle */
    protected static ResourceBundle resourceMenusBundle = ResourceBundle.getBundle("properties.menus");

    public static ResourceBundle getMenusResourceBundle() {
        return resourceMenusBundle;
    }

}
