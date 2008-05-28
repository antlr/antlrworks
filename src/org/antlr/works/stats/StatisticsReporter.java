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

package org.antlr.works.stats;

import org.antlr.works.dialog.DialogPersonalInfo;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.foundation.XJUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class StatisticsReporter {

    //http://www.antlr.org/stats/register?who=1&sector=2&devtool=3&yearslang=1&yearsprog=5&residing=bali&caffeine=coffee&version=1.0ea8

    public static final String URL_REGISTER = "http://www.antlr.org/stats/register?";
    public static final String URL_SEND_STATS = "http://www.antlr.org/stats/notify?";

    public static final String TYPE_GUI = "antlrworks";
    public static final String TYPE_GRAMMAR = "grammar";
    public static final String TYPE_RUNTIME = "runtime";

    protected String error = null;
    protected boolean cancel = false;

    public StatisticsReporter() {
    }

    public String getError() {
        return error;
    }

    public String fetchIDFromServer() {
        Map info = AWPrefs.getPersonalInfo();
        /** Send null info if we cannot get the personal info */
        if(info == null) {
            System.err.println("No personal info available. Sending blank data to request a new ID from ANTLR's server.");
            info = new HashMap();
        }

        StringBuilder s = new StringBuilder(URL_REGISTER);
        s.append("who=");
        s.append(info.get(DialogPersonalInfo.INFO_WHO));
        s.append("&sector=");
        s.append(info.get(DialogPersonalInfo.INFO_SECTOR));
        s.append("&devtool=");
        s.append(info.get(DialogPersonalInfo.INFO_DEVTOOL));
        s.append("&yearslang=");
        s.append(info.get(DialogPersonalInfo.INFO_YEARSLANG));
        s.append("&yearsprog=");
        s.append(info.get(DialogPersonalInfo.INFO_YEARSPROG));
        s.append("&residing=");
        s.append(XJUtils.encodeToURL((String)info.get(DialogPersonalInfo.INFO_RESIDING), "-"));
        s.append("&caffeine=");
        s.append(XJUtils.encodeToURL((String)info.get(DialogPersonalInfo.INFO_CAFFEINE), "-"));
        s.append("&version=");
        s.append(XJUtils.encodeToURL(XJApplication.getAppVersionShort(), "-"));

        URLConnection urc;
        URL url;
        try {
            url = new URL(s.toString());
            urc = url.openConnection();
        } catch (Exception e) {
            error = e.toString();
            return null;
        }

        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(urc.getInputStream()));
        } catch (IOException e) {
            error = e.toString();
            return null;
        }

        String id;

        try {
            id = br.readLine();
            br.close();
        } catch (IOException e) {
            error = e.toString();
            return null;
        }

        return id;
    }

    public String getID() {
        String id = AWPrefs.getServerID();
        if(id == null || id.length() == 0) {
            id = fetchIDFromServer();
            if(id == null || id.length() == 0)
                return null;
            AWPrefs.setServerID(id);
        }
        return id;
    }

    public void cancel() {
        cancel = true;
    }

    public boolean submitStats(StatisticsManager sm) {
        cancel = false;
        for(int index=0; index<sm.getStatsCount(); index++) {
            if(!submitStats(getID(), sm.type, sm.getRawString(index)))
                return false;
            if(cancel)
                return true;
        }

        return true;
    }

    protected boolean submitStats(String id, String type, String data) {
        if(id == null) {
            error = "cannot submit stat with a null id";
            return false;
        }

        StringBuilder param = new StringBuilder();
        param.append(URL_SEND_STATS);
        param.append("ID=");
        param.append(id);
        param.append("&type=");
        param.append(type);
        param.append("&data=");
        param.append(XJUtils.encodeToURL(data));

        URLConnection urc;
        URL url;
        BufferedReader br;
        boolean success = false;
        try {
            url = new URL(param.toString());
            urc = url.openConnection();
            br = new BufferedReader(new InputStreamReader(urc.getInputStream()));
            success = br.readLine().equalsIgnoreCase("OK");
            br.close();
        } catch (IOException e) {
            error = e.toString();
        }

        return success;
    }

}
