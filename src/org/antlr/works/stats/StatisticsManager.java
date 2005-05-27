package org.antlr.works.stats;

import org.antlr.tool.GrammarReport;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

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

public class StatisticsManager {

    public static final int TYPE_GUI = 0;
    public static final int TYPE_GRAMMAR = 1;
    public static final int TYPE_RUNTIME = 2;

    protected int type;
    protected List readableLines = new ArrayList();
    protected List rawLines = new ArrayList();

    public StatisticsManager(int type) {
        this.type = type;
        load();
    }

    public int getStatsCount() {
        return readableLines.size();
    }

    public String getReadableString(int index) {
        if(index < 0 || index >= readableLines.size())
            return null;
        else
            return (String)readableLines.get(index);
    }

    public String getRawString(int index) {
        if(index < 0 || index >= rawLines.size())
            return null;
        else
            return (String)rawLines.get(index);
    }

    public boolean load() {
        readableLines.clear();
        rawLines.clear();
        switch(type) {
            case TYPE_GUI:  return loadGUI();
            case TYPE_GRAMMAR:  return loadGrammar();
            case TYPE_RUNTIME:  return loadRuntime();
        }
        return false;
    }

    protected boolean loadGUI() {
        addReadableLine(Statistics.shared().getReadableString());
        addRawLine(Statistics.shared().getRawString());
        return true;
    }

    protected boolean loadGrammar() {
        return loadFromFile(GrammarReport.getAbsoluteFileName(GrammarReport.GRAMMAR_STATS_FILENAME));
    }

    protected boolean loadRuntime() {
        return loadFromFile(GrammarReport.getAbsoluteFileName(GrammarReport.RUNTIME_STATS_FILENAME));
    }

    protected boolean loadFromFile(String file) {
        if(file == null)
            return false;

        File f = new File(file);
        if(!f.exists())
            return false;

        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
            String line;
            while((line = dis.readLine()) != null) {
                addRawLine(line);
                addReadableLine(GrammarReport.toString(line));
            }
            dis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected void addReadableLine(String line) {
        readableLines.add(line);
    }

    protected void addRawLine(String line) {
        rawLines.add(line);
    }

    public void reset() {
        // @todo implement
    }
}
