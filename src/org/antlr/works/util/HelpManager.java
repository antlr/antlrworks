package org.antlr.works.util;

import edu.usfca.xj.appkit.app.XJApplication;
import edu.usfca.xj.appkit.frame.XJFrame;
import edu.usfca.xj.appkit.update.XJUpdateManager;
import edu.usfca.xj.appkit.utils.BrowserLauncher;
import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.utils.XJLocalizable;
import edu.usfca.xj.foundation.timer.XJScheduledTimerDelegate;
import org.antlr.works.editor.EditorPreferences;

import java.awt.*;
import java.io.IOException;
import java.util.Calendar;

/*

[The "BSD licence"]
Copyright (c) 2004-05 Jean Bovet
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

public class HelpManager implements XJScheduledTimerDelegate {

    public static final String PROPERTIES_FILE = "info";

    public static final String UPDATE_XML_URL = "UpdateXMLURL";
    public static final String FEEDBACK_URL = "FeedbackURL";
    public static final String APP_VERSION = "AppVersion";

    public void scheduledTimerFired(boolean startup) {
        checkUpdatesAuto(startup);
    }

    public static void submitStats() {

    }

    public static void sendFeedback(XJFrame parent) {
        try {
            BrowserLauncher.openURL(XJLocalizable.getString(PROPERTIES_FILE, FEEDBACK_URL));
        } catch (IOException e) {
            XJAlert.display(parent.getJavaContainer(), "Error", "Cannot display the feedback page because:\n"+e);
        }
    }

    public static void checkUpdates(Container parent, boolean automatic) {
        XJUpdateManager um = new XJUpdateManager(parent, null);
        um.checkForUpdates(XJLocalizable.getString(PROPERTIES_FILE, APP_VERSION),
                           XJLocalizable.getString(PROPERTIES_FILE, UPDATE_XML_URL),
                           EditorPreferences.getDownloadPath(),
                           automatic);
    }

    public static void checkUpdatesAuto(boolean atStartup) {
        int method = EditorPreferences.getUpdateType();
        boolean check = false;

        if(method == EditorPreferences.UPDATE_MANUALLY)
            check = false;
        else if(method == EditorPreferences.UPDATE_AT_STARTUP && atStartup)
            check = true;
        else {
            Calendar currentCalendar = Calendar.getInstance();
            Calendar nextUpdateCalendar = EditorPreferences.getUpdateNextDate();

            if(nextUpdateCalendar == null || currentCalendar.equals(nextUpdateCalendar) || currentCalendar.after(nextUpdateCalendar)) {

                switch(method) {
                    case EditorPreferences.UPDATE_DAILY:
                        check = true;
                        currentCalendar.add(Calendar.DATE, 1);
                        EditorPreferences.setUpdateNextDate(currentCalendar);
                        break;
                    case EditorPreferences.UPDATE_WEEKLY:
                        check = true;
                        currentCalendar.add(Calendar.DATE, 7);
                        EditorPreferences.setUpdateNextDate(currentCalendar);
                        break;
                }
            }
        }

        if(check) {
            checkUpdates(XJApplication.getActiveContainer(), true);
        }
    }
}
