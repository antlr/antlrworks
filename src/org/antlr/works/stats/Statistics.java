package org.antlr.works.stats;

import org.antlr.xjlib.appkit.app.XJApplicationDelegate;
import org.antlr.xjlib.appkit.swing.XJLookAndFeel;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.works.IDE;
import org.antlr.works.dialog.DialogPersonalInfo;
import org.antlr.works.dialog.DialogReports;
import org.antlr.works.dialog.DialogReportsDelegate;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.utils.Localizable;

import java.util.List;
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

/** This class is used to report manually the statistics without having to launch AW completely.
 * We still rely on the XJLibrary and most of AW settings but at least only one dialog is displayed.
 */

public class Statistics extends XJApplicationDelegate implements DialogReportsDelegate {

    public void appDidLaunch(String[] args, List<String> documentsToOpenAtStartup) {
        AWPrefs.setLookAndFeel(XJLookAndFeel.applyLookAndFeel(AWPrefs.getLookAndFeel()));

        if(args.length >= 2 && args[1].equals("-erase")) {
            StatisticsManager.reset(StatisticsReporter.TYPE_GUI);
            StatisticsManager.reset(StatisticsReporter.TYPE_GRAMMAR);
            StatisticsManager.reset(StatisticsReporter.TYPE_RUNTIME);
            XJAlert.display(null, "Statistics", "The statistics have been successfully erased.");
            System.exit(0);
        } else {
            /** Ask the user to register if it didn't register on this machine previously */
            if(!AWPrefs.isUserRegistered()) {
                AWPrefs.setServerID("");
                new DialogPersonalInfo(null).runModal();
                AWPrefs.setUserRegistered(true);
            }

            DialogReports reports = new DialogReports(null, false);
            reports.setDelegate(this);
            reports.runModal();
        }
    }

    public void reportsCancelled() {
        System.exit(0);
    }

    public void reportsSend(boolean success) {
        System.exit(0);
    }

    public String appVersionShort() {
        return IDE.VERSION;
    }

    public String appVersionLong() {
        return IDE.VERSION;
    }

    public Class appPreferencesClass() {
        return IDE.class;
    }

    public boolean appHasPreferencesMenuItem() {
        return false;
    }

    public boolean supportsPersistence() {
        return false;
    }

}
