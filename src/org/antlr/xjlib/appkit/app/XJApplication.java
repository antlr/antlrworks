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

package org.antlr.xjlib.appkit.app;

import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.appkit.document.XJDocumentFactory;
import org.antlr.xjlib.appkit.frame.XJFrame;
import org.antlr.xjlib.appkit.frame.XJFrameDelegate;
import org.antlr.xjlib.appkit.frame.XJPanel;
import org.antlr.xjlib.appkit.frame.XJWindow;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.utils.XJAlertInput;
import org.antlr.xjlib.appkit.utils.XJFileChooser;
import org.antlr.xjlib.appkit.utils.XJLocalizable;
import org.antlr.xjlib.foundation.XJObject;
import org.antlr.xjlib.foundation.XJSystem;
import org.antlr.xjlib.foundation.timer.XJScheduledTimer;
import org.antlr.xjlib.foundation.timer.XJScheduledTimerDelegate;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class XJApplication extends XJObject implements XJApplicationInterface, XJFrameDelegate {

    public static final boolean YES = true;
    public static final boolean NO = false;

    public static final String XJ_PREFS_RECENT_FILES = "XJ_PREFS_RECENT_FILES";

    protected static XJApplicationInterface shared = null;
    protected static XJApplicationDelegate delegate = null;

    protected static List<XJScheduledTimer> scheduledTimers = new ArrayList<XJScheduledTimer>();
    protected static final long SCHEDULED_TIMER_MINUTES = 1;

    protected static List<XJWindow> windows = new ArrayList<XJWindow>();

    public static final int MAX_RECENT_FILES = 10;
    protected static List<String> recentFiles = null;

    protected static int documentAbsoluteCount = 0;
    protected static int documentAbsPositionCount = 0;
    protected static final int DOCUMENT_OFFSET_PIXELS = 20;

    protected static List<XJDocumentFactory> documentFactories = new ArrayList<XJDocumentFactory>();
    protected static String propertiesPath = "";

    protected static boolean startingUp = true;
    protected static String[] launchArguments = null;
    protected static List<String> documentsToOpenAtStartup = new ArrayList<String>();

    protected static String appName = "";

    protected XJPreferences userPrefs = null;

    protected XJFrame about = null;
    protected XJPanel prefs = null;

    protected static XJScheduledTimerDelegate autoSaveTimer = null;
    private static boolean useDesktopMode;

    public static synchronized void setShared(XJApplicationInterface shared) {
        XJApplication.shared = shared;
    }

    public static synchronized XJApplicationInterface shared() {
        if(shared == null) {
            if(XJSystem.isMacOS()) {
                try {
                    shared = (XJApplication)Class.forName("org.antlr.xjlib.appkit.app.MacOS.XJApplicationMacOS").newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("XJApplication: cannot instanciate the MacOS application ("+e+")");
                }
            }

            if(shared == null)
                shared = new XJApplication();

            ((XJApplication)shared).startup();
        }
        return shared;
    }

    public static String getAppVersionShort() {
        return delegate.appVersionShort();
    }

    public static String getAppVersionLong() {
        return delegate.appVersionLong();
    }

    public static XJApplicationDelegate getAppDelegate() {
        return delegate;
    }

    public static Container getActiveContainer() {
        Frame[] frame = Frame.getFrames();
        for (Frame f : frame) {
            if (f.isActive() && f.isVisible()) {
                if (f.getSize().width != 0 && f.getSize().height != 0)
                    return f;
            }
        }
        return null;
    }

    public XJApplication() {
        userPrefs = new XJPreferences(getPreferencesClass());
        recentFiles = userPrefs.getList(XJ_PREFS_RECENT_FILES);
        if(recentFiles == null)
            recentFiles = new ArrayList<String>();
        XJMainMenuBar.refreshAllRecentFilesMenu();
    }

    public static void run(XJApplicationDelegate delegate, String[] args, String applicationName) {
        XJApplication.appName = applicationName;

        if(XJSystem.isMacOS()) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", applicationName);
        }

        run(delegate, args);
    }

    public static void run(XJApplicationDelegate delegate, String[] args) {
        setDelegate(delegate);
        launchArguments = args;
        useDesktopMode = XJApplication.delegate.useDesktopMode();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                XJApplication.startingUp = false;
                XJApplication.delegate.appDidLaunch(launchArguments, documentsToOpenAtStartup);
                documentsToOpenAtStartup.clear();
                new java.util.Timer().schedule(new ScheduledTimer(), 1000, 1000*60*SCHEDULED_TIMER_MINUTES);
            }
        });
    }

    protected void startup() {
        if(hasPreferencesMenuItem())
            addPreferencesMenuItem();
        else
            removePreferencesMenuItem();
    }

    protected void updateRecentFilesPrefs() {
        userPrefs.setList(XJ_PREFS_RECENT_FILES, recentFiles);
        userPrefs.flush();
    }

    protected void shutdown() {
        updateRecentFilesPrefs();
        System.exit(0);
    }

    public static void setDelegate(XJApplicationDelegate delegate) {
        XJApplication.delegate = delegate;
    }

    public static void addScheduledTimer(XJScheduledTimerDelegate delegate, long minutes, boolean scheduleAtStartup) {
        scheduledTimers.add(new XJScheduledTimer(delegate, minutes, scheduleAtStartup));
    }

    public static void removeScheduledTimer(XJScheduledTimerDelegate delegate) {
        for(int index = scheduledTimers.size()-1; index >= 0; index--) {
            XJScheduledTimer timer = scheduledTimers.get(index);
            if(timer.getDelegate() == delegate) {
                scheduledTimers.remove(timer);
            }
        }
    }

    protected static class ScheduledTimer extends TimerTask {

        protected boolean startup = true;

        public void run() {
            for (XJScheduledTimer timer : scheduledTimers) {
                timer.fire(startup, SCHEDULED_TIMER_MINUTES);
            }

            startup = false;
        }
    }

    public static void setPropertiesPath(String path) {
        propertiesPath = path;
    }

    public static String getPropertiesPath() {
        return propertiesPath;
    }

    public String getApplicationName() {
        if(delegate != null) {
            return delegate.appName();
        } else {
            return "";
        }
    }

    public XJPreferences getPreferences() {
        return userPrefs;
    }

    public void displayAbout() {
        boolean awake = (about == null);

        if(about == null)
            about = delegate.appInstanciateAboutPanel();

        if(about == null)
            return;

        if(awake) {
            about.setDelegate(this);
            about.awake();
        }
        about.setVisible(true);
    }

    public void displayPrefs() {
        if(prefs == null) {
            try {
                prefs = (XJPanel)delegate.appPreferencesPanelClass().newInstance();
                prefs.setDelegate(this);
                prefs.awake();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Cannot instanciate the Preferences panel: "+e);
                return;
            }
            prefs.center();
        }
        prefs.setVisible(true);
    }

    public void displayHelp() {
        delegate.appShowHelp();
    }

    public void frameDidClose(XJFrame frame) {
        if(frame == prefs)
            prefs = null;
        if(frame == about)
            about = null;
    }

    public void addRecentFile(String file) {
        if(file == null) return;

        if(recentFiles.contains(file))
            recentFiles.remove(file);

        if(recentFiles.size() > MAX_RECENT_FILES)
            recentFiles.remove(recentFiles.size()-1);

        recentFiles.add(0, file);
        updateRecentFilesPrefs();

        XJMainMenuBar.refreshAllRecentFilesMenu();
    }

    public void removeRecentFile(String file) {
        recentFiles.remove(file);
        updateRecentFilesPrefs();
        XJMainMenuBar.refreshAllRecentFilesMenu();
    }

    public void clearRecentFiles() {
        recentFiles.clear();
        updateRecentFilesPrefs();
        XJMainMenuBar.refreshAllRecentFilesMenu();
    }

    public List recentFiles() {
        return recentFiles;
    }

    // *** Menu

    protected void addPreferencesMenuItem() {
    }

    protected void removePreferencesMenuItem() {
    }

    // *** XJDocument

    public static void addDocumentFactory(Class documentClass, Class windowClass, Class dataClass, String ext, String description) {
        addDocumentFactory(new XJDocumentFactory(documentClass, windowClass, dataClass, ext, description));
    }

    public static void addDocumentFactory(XJDocumentFactory factory) {
        documentFactories.add(factory);
    }

    public static boolean handlesDocument(XJDocument doc) {
        if(doc == null) return false;
        
        for(XJDocumentFactory factory : documentFactories) {
            if(factory.handlesPath(doc.getDocumentPath())) return true;
        }
        return false;
    }

    public XJDocumentFactory getDocumentTypeForPath(String path) {
        for (XJDocumentFactory factory : documentFactories) {
            if(factory.handlesPath(path)) {
                return factory;
            }
        }
        return null;
    }

    public List getDocumentExtensions() {
        List<List> ext = new ArrayList<List>();
        for (XJDocumentFactory factory : documentFactories) {
            ext.add(factory.getExtensions());
        }
        return ext;
    }

    public List<String> getDocumentDescriptions() {
        List<String> descr = new ArrayList<String>();
        for (XJDocumentFactory factory : documentFactories) {
            descr.add(factory.getDescriptionString());
        }
        return descr;
    }

    public XJDocumentFactory askForDocumentType() {
        if(documentFactories.size() == 1) {
            return documentFactories.get(0);
        }

        int index = XJAlertInput.showInputDialog(null, XJLocalizable.getXJString("AppNewDocTitle"),
                XJLocalizable.getXJString("AppNewDocMessage"), getDocumentDescriptions(), getDocumentDescriptions().get(0));
        if(index == -1)
            return null;
        else
            return documentFactories.get(index);
    }

    public XJDocument newDocument(boolean visible, XJDocumentFactory docFactory) {
        if(documentFactories.size() == 0) {
            XJAlert.display(null, XJLocalizable.getXJString("AppNewDocErrTitle"), XJLocalizable.getXJString("AppNewDocErrMessage"));
            return null;
        }

        if(docFactory == null) {
            docFactory = askForDocumentType();
            if(docFactory == null)
                return null;
        }

        XJDocument document;
        try {
            document = docFactory.createDocument();
            document.awake();

            if(supportsPersistence())
                document.setTitle(XJLocalizable.getXJString("AppUntitled")+" "+documentAbsoluteCount);
            else
                document.setTitle(documentAbsoluteCount > 0 ?appName+" "+documentAbsoluteCount:appName);

            XJWindow window = document.getWindow();
            if(!window.isMaximized() && useDesktopMode()) {
                documentAbsoluteCount++;

                window.offsetPosition(documentAbsPositionCount*DOCUMENT_OFFSET_PIXELS,
                        documentAbsPositionCount*DOCUMENT_OFFSET_PIXELS);

                if(window.isCompletelyOnScreen())
                    documentAbsPositionCount++;
                else
                    documentAbsPositionCount = 0;
            }
        } catch(Exception e) {
            e.printStackTrace();
            XJAlert.display(null, XJLocalizable.getXJString("AppNewDocError"), e.toString());
            return null;
        }

        if(visible) {
            document.showWindow();
        }

        return document;
    }

    public XJDocument newDocument() {
        XJDocument document = newDocument(true, null);

        if (document != null) {
            delegate.displayNewDocumentWizard(document);
        }

        return document;
    }

    public boolean openDocuments(List<String> files) {
        boolean success = true;
        for(String file : files) {
            if(!openDocument(file)) success = false;
        }
        return success;
    }

    public boolean openDocument(String file) {
        if(file == null)
            return false;

        // FIX AW-63:
        // On Mac OS X, openDocument might be called during startup which causes
        // some issue because the application didn't have time to register its document
        // class type. Accumulate the documents in a list that will be provided to the
        // application in the didRun method (see above).
        if(startingUp) {
            documentsToOpenAtStartup.add(file);
            return true;
        }

        XJWindow window = getWindowContainingDocumentForPath(file);
        if(window != null) {
            window.selectDocument(window.getDocumentForPath(file));
            window.bringToFront();
            return true;
        } else {
            XJDocument document = newDocument(false, getDocumentTypeForPath(file));
            if(document == null)
                return false;
            else if(loadDocument(file, document)) {
                addRecentFile(file);
                document.showWindow();
                closeFirstCreatedWindowIfNonDirty(document.getWindow());
                return true;
            } else {
                document.getWindow().performClose(true);
                return false;
            }
        }
    }

    public boolean loadDocument(String file, XJDocument doc) {
        try {
            return doc.load(file);
        } catch (Exception e) {
            e.printStackTrace();
            XJAlert.display(null, XJLocalizable.getXJString("DocError"), XJLocalizable.getXJString("DocLoadError")+" "+e.toString());
            return XJApplication.NO;
        }
    }

    public boolean openDocument() {
        return XJFileChooser.shared().displayOpenDialog(null, getDocumentExtensions(), getDocumentDescriptions(), false)
                && openDocument(XJFileChooser.shared().getSelectedFilePath());

    }

    public boolean openLastUsedDocument() {
        if(recentFiles.isEmpty())
            return false;

        String file = recentFiles.get(0);
        while(!new File(file).exists()) {
            recentFiles.remove(0);
            if(recentFiles.isEmpty()) {
                updateRecentFilesPrefs();
                return false;
            }

            file = recentFiles.get(0);
        }
        updateRecentFilesPrefs();

        if(openDocument(file))
            return true;

        removeRecentFile(file);
        return false;
    }

    public void closeFirstCreatedWindowIfNonDirty(XJWindow excludeWindow) {
        for(XJWindow window : windows) {
            // close the window only if it has:
            // - no document associated with file
            // - no dirty document
            if(!window.hasDirtyDocument() && !window.hasDocumentsWithFileAssociated() && window != excludeWindow) {
                window.performClose(false);
                break;
            }
        }
    }

    public XJWindow getWindowContainingDocumentForPath(String path) {
        for (XJWindow window : windows) {
            XJDocument doc = window.getDocumentForPath(path);
            if(doc != null) {
                return window;
            }
        }
        return null;
    }

    public XJWindow getActiveWindow() {
        for (XJWindow window : windows) {
            if (window.isActive())
                return window;
        }
        return null;
    }

    // ** XJWindow

    public void addWindow(XJWindow window) {
        windows.add(window);
    }

    public int getNumberOfNonAuxiliaryWindows() {
        int count = 0;
        for (XJWindow window : windows) {
            if (!window.isAuxiliaryWindow())
                count++;
        }
        return count;
    }

    public void removeWindow(XJWindow window) {
        windows.remove(window);
        //refreshMainMenuBar();
        if(getNumberOfNonAuxiliaryWindows() == 0 && !startingUp) {
            if((!XJSystem.isMacOS() && !useDesktopMode()) || shouldQuitAfterLastWindowClosed()) {
                // Invoke the application quit method later in time to allow
                // the code closing the window to complete its execution.
                // For example, to store the window size and position before
                // the application is exited.
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        performQuit();
                    }
                });
            }
        }
    }

    public List<XJWindow> getWindows() {
        return windows;
    }

    public List<XJWindow> getWindowsInWindowMenu() {
        List<XJWindow> wim = new ArrayList<XJWindow>();
        for(XJWindow w : windows) {
            if(w.shouldAppearsInWindowMenu()) {
                wim.add(w);
            }
        }
        return wim;
    }

    // *** Auto-save feature

    public static void setAutoSave(boolean enabled, int delayInMinutes) {
        if(autoSaveTimer == null)
            autoSaveTimer = new AutoSaveTimer();

        removeScheduledTimer(autoSaveTimer);

        if(enabled)
            addScheduledTimer(autoSaveTimer, delayInMinutes, false);
    }

    public static class AutoSaveTimer implements XJScheduledTimerDelegate {
        public void scheduledTimerFired(boolean startup) {
            for(XJWindow window : windows) {
                window.saveAll();
            }
        }
    }

    // *** Events

    public void performPreferences() {
        displayPrefs();
    }

    public void performQuit() {
        delegate.appWillTerminate();
        for(XJWindow window : new ArrayList<XJWindow>(windows)) {
            if(!window.performClose(false)) {
                // cancel quit if any document cannot or don't want to be closed
                return;
            }
        }
        XJFrame.closeDesktop();
        shutdown();
    }

    // Properties

    public boolean supportsPersistence() {
        return delegate == null || delegate.supportsPersistence();
    }

    public boolean hasPreferencesMenuItem() {
        return delegate == null || delegate.appHasPreferencesMenuItem();
    }

    public boolean shouldQuitAfterLastWindowClosed() {
        return delegate != null && delegate.appShouldQuitAfterLastWindowClosed();
    }

    public boolean useDesktopMode() {
        return useDesktopMode;
    }

    public Class getPreferencesClass() {
        if(delegate == null)
            return XJApplication.class;
        else
            return delegate.appPreferencesClass();
    }

}
