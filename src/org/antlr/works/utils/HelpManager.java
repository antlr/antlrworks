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

package org.antlr.works.utils;

import org.antlr.Tool;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.works.IDE;
import org.antlr.works.dialog.DialogReports;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.update.XJUpdateManager;
import org.antlr.xjlib.appkit.utils.BrowserLauncher;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.foundation.XJSystem;
import org.antlr.xjlib.foundation.XJUtils;
import org.antlr.xjlib.foundation.timer.XJScheduledTimerDelegate;

import java.awt.*;
import java.io.IOException;
import java.util.Calendar;

public class HelpManager implements XJScheduledTimerDelegate {

    public void scheduledTimerFired(boolean startup) {
        checkUpdatesAuto(startup);
    }

    public static void submitStats(Container parent) {
        new DialogReports(parent, true).runModal();
    }

    public static void sendFeedback(Container parent) {
        StringBuilder url = new StringBuilder(Localizable.getLocalizedString(Localizable.FEEDBACK_URL));
        url.append("?ANTLRVersion=");
        url.append(XJUtils.encodeToURL(new Tool().VERSION));
        url.append("&StringTemplateVersion=");
        url.append(XJUtils.encodeToURL(StringTemplate.VERSION));
        url.append("&ANTLRWorksVersion=");
        url.append(XJUtils.encodeToURL(XJApplication.getAppVersionShort()));
        url.append("&OS=");
        url.append(XJUtils.encodeToURL(XJSystem.getOSName()));
        url.append("&JavaVersion=");
        url.append(XJUtils.encodeToURL(XJSystem.getJavaRuntimeVersion()));

        try {
            BrowserLauncher.openURL(url.toString());
        } catch (IOException e) {
            XJAlert.display(parent, "Error", "Cannot display the feedback page because:\n"+e+"\n\nTo report a feedback, go to "+url+".");
        }
    }

    public static void checkUpdates(Container parent, boolean automatic) {
        String url;
        if(XJSystem.isMacOS())
            url = Localizable.getLocalizedString(Localizable.UPDATE_OSX_XML_URL);
        else
            url = Localizable.getLocalizedString(Localizable.UPDATE_XML_URL);


        XJUpdateManager um = new XJUpdateManager(parent, null);
        um.checkForUpdates(IDE.VERSION,
                           url,
                           AWPrefs.getDownloadPath(),
                           automatic);
    }

    public static void checkUpdatesAuto(boolean atStartup) {
        int method = AWPrefs.getUpdateType();
        boolean check = false;

        if(method == AWPrefs.UPDATE_MANUALLY)
            check = false;
        else if(method == AWPrefs.UPDATE_AT_STARTUP && atStartup)
            check = true;
        else {
            Calendar currentCalendar = Calendar.getInstance();
            Calendar nextUpdateCalendar = AWPrefs.getUpdateNextDate();

            if(nextUpdateCalendar == null || currentCalendar.equals(nextUpdateCalendar) || currentCalendar.after(nextUpdateCalendar)) {

                switch(method) {
                    case AWPrefs.UPDATE_DAILY:
                        check = nextUpdateCalendar != null;
                        currentCalendar.add(Calendar.DATE, 1);
                        AWPrefs.setUpdateNextDate(currentCalendar);
                        break;
                    case AWPrefs.UPDATE_WEEKLY:
                        check = nextUpdateCalendar != null;
                        currentCalendar.add(Calendar.DATE, 7);
                        AWPrefs.setUpdateNextDate(currentCalendar);
                        break;
                }
            }
        }

        if(check) {
            checkUpdates(null, true);
        }
    }

}
