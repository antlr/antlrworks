package org.antlr.works.stats;

import edu.usfca.xj.foundation.XJUtils;
import org.antlr.works.dialog.DialogPersonalInfo;
import org.antlr.works.editor.EditorPreferences;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

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

public class StatisticsReporter {

    //http://www.antlr.org/stats/register?who=1&sector=2&devtool=3&yearslang=1&yearsprog=5&residing=bali&caffeine=coffee

    public static final String URL_REGISTER = "http://www.antlr.org/stats/register?";
    public static final String URL_SEND_STATS = "http://www.antlr.org/stats/notify?";

    public static final String TYPE_GUI = "antlrworks";
    public static final String TYPE_GRAMMAR = "grammar";
    public static final String TYPE_RUNTIME = "runtime";

    protected String error = null;

    protected Container parent;

    public StatisticsReporter(Container parent) {
        this.parent = parent;
    }

    public String getError() {
        return error;
    }

    public String fetchIDFromServer() {
        Map info = EditorPreferences.getPersonalInfo();

        StringBuffer s = new StringBuffer(URL_REGISTER);
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

        URLConnection urc = null;
        URL url = null;
        try {
            url = new URL(s.toString());
            urc = url.openConnection();
        } catch (Exception e) {
            error = e.toString();
            return null;
        }

        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new BufferedInputStream(urc.getInputStream()));
        } catch (IOException e) {
            error = e.toString();
            return null;
        }

        String id = null;

        try {
            id = dis.readLine();
            dis.close();
        } catch (IOException e) {
            error = e.toString();
            return null;
        }

        return id;
    }

    public String getID() {
        String id = EditorPreferences.getServerID();
        if(id == null || id.length() == 0) {
            id = fetchIDFromServer();
            if(id == null || id.length() == 0)
                return null;
            EditorPreferences.setServerID(id);
        }
        return id;
    }

    public boolean submitGUI(StatisticsManager sm) {
        return submitStats(sm, TYPE_GUI);
    }

    public boolean submitGrammar(StatisticsManager sm) {
        return submitStats(sm, TYPE_GRAMMAR);
    }

    public boolean submitRuntime(StatisticsManager sm) {
        return submitStats(sm, TYPE_RUNTIME);
    }

    public boolean submitStats(StatisticsManager sm, String type) {
        for(int index=0; index<sm.getStatsCount(); index++) {
            if(!submitStats(getID(), type, sm.getRawString(index)))
                return false;
        }

        return true;
    }

    protected boolean submitStats(String id, String type, String data) {
        if(id == null) {
            error = "cannot submit stat with a null id";
            return false;
        }

        StringBuffer param = new StringBuffer();
        param.append(URL_SEND_STATS);
        param.append("ID=");
        param.append(id);
        param.append("&type=");
        param.append(type);
        param.append("&data=");
        param.append(XJUtils.encodeToURL(data));

        URLConnection urc = null;
        URL url = null;
        DataInputStream dis = null;
        boolean success = false;
        try {
            url = new URL(param.toString());
            urc = url.openConnection();
            dis = new DataInputStream(new BufferedInputStream(urc.getInputStream()));
            success = dis.readLine().equalsIgnoreCase("OK");
            dis.close();
        } catch (IOException e) {
            error = e.toString();
        }

        return success;
    }

}
