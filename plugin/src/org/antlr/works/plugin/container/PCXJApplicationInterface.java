package org.antlr.works.plugin.container;

import org.antlr.works.IDE;
import org.antlr.xjlib.appkit.app.XJApplicationInterface;
import org.antlr.xjlib.appkit.app.XJPreferences;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.appkit.frame.XJWindow;

import java.util.ArrayList;
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

public class PCXJApplicationInterface implements XJApplicationInterface {

    private PluginContainer container;
    private List<XJWindow> emptyList = new ArrayList<XJWindow>();
    private XJPreferences defaultPrefs = new XJPreferences(PluginContainer.class);

    public PCXJApplicationInterface(PluginContainer container) {
        this.container = container;
    }

    public String getApplicationName() {
        return "ANTLRWorks Plugin";
    }

    public XJPreferences getPreferences() {
        if(container == null) {
            return defaultPrefs;
        } else {
            return container.getPreferences();
        }
    }

    public List getDocumentExtensions() {
        return emptyList;
    }

    public XJDocument getActiveDocument() {
        if(container == null) {
            return null;
        } else {
            return container.getDocument();
        }
    }

    public XJDocument newDocument() {
        return null;
    }

    public boolean openDocument() {
        return false;
    }

    public List<XJWindow> getWindows() {
        return emptyList;
    }

    public List<XJWindow> getWindowsInWindowMenu() {
        return emptyList;
    }

    public boolean supportsPersistence() {
        return false;
    }

    public boolean hasPreferencesMenuItem() {
        return false;
    }

    public List recentFiles() {
        return emptyList;
    }

    public void clearRecentFiles() {
    }

    public void addRecentFile(String path) {
    }

    public void removeRecentFile(String file) {
    }

    public void performQuit() {
    }

    public void displayPrefs() {
    }

    public void displayAbout() {
        PluginContainer.showAbout();
    }

    public boolean useDesktopMode() {
        return false;
    }

    public void displayHelp() {
        IDE.showHelp(container==null?null:container.getParent());
    }

    public boolean openDocuments(List<String> files) {
        return false;
    }

    public boolean openDocument(String file) {
        return false;
    }

    public void addWindow(XJWindow window) {
    }

    public void removeWindow(XJWindow window) {
    }

    public void addDocument(XJDocument document) {
    }

    public XJDocument getDocumentForPath(String path) {
        return null;
    }

    public void removeDocument(XJDocument document) {
    }

    public List<XJDocument> getDocuments() {
        List<XJDocument> l = new ArrayList<XJDocument>();
        if(container != null) {
            l.add(container.getDocument());
        }
        return l;
    }

    public boolean openLastUsedDocument() {
        return false;
    }

    public XJWindow getActiveWindow() {
        return null;
    }

}
