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

package org.antlr.xjlib.appkit.frame;

import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.document.XJDocument;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuItem;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class XJWindow extends XJFrame {

    private final List<XJDocument> documents = new ArrayList<XJDocument>();

    public XJWindow() {
        XJApplication.shared().addWindow(this);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    public boolean isAuxiliaryWindow() {
        return false;
    }

    @Override
    public boolean shouldAppearsInWindowMenu() {
        return true;
    }

    /**
     * Sets the main document of the window. All previous attached documents
     * are removed.
     *
     * @param document The main document
     */
    public void setDocument(XJDocument document) {
        documents.clear();
        documents.add(document);
    }

    /**
     * Adds an additional document to the window. The main document must be set using
     * setDocument().
     *
     * @param document The additonal document to add to the window
     */
    public void addDocument(XJDocument document) {
        if(!documents.contains(document)) {
            documents.add(document);
        }
    }

    public XJDocument getDocumentForPath(String path) {
        for(XJDocument document : documents) {
            String docPath = document.getDocumentPath();
            if (docPath != null && docPath.equals(path))
                return document;
        }
        return null;
    }
    
    public List<XJDocument> getDocuments() {
        return documents;
    }

    public XJDocument getDocument() {
        if(documents.isEmpty()) {
            return null;
        } else {
            return documents.get(0);
        }
    }
    
    public boolean hasDocuments() {
        return !documents.isEmpty();
    }

    public boolean hasDirtyDocument() {
        for(XJDocument doc : documents) {
            if(doc.isDirty()) return true;
        }
        return false;
    }

    public boolean hasDocumentsWithFileAssociated() {
        for(XJDocument doc : documents) {
            if(doc.getDocumentPath() != null) return true;
        }
        return false;
    }

    public void reloadDocuments() {
        for(XJDocument doc : documents) {
            if(doc.isModifiedOnDisk()) {
                windowDocumentPathDidChange(doc);
                doc.synchronizeLastModifiedDate();
            }
        }
    }

    public void clearDocuments() {
        documents.clear();
    }

    public void saveAll() {
        for (XJDocument document : documents) {
            if (document.isDirty() && document.getDocumentPath() != null) {
                document.save(false);
            }
        }
    }

    public boolean closeDocuments(boolean force) {
        for(XJDocument doc : new ArrayList<XJDocument>(documents)) {
            if(!doc.close(force)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean close(boolean force) {
        if(hasDocuments()) {
            if(!closeDocuments(force)) {
                return false;
            }
        }

        super.close(force);

        XJApplication.shared().removeWindow(this);
        clearDocuments();
        return true;
    }

    @Override
    public void menuItemState(XJMenuItem item) {
        super.menuItemState(item);

        if(!hasDocuments()) {
            if(item.getTag() == XJMainMenuBar.MI_CLOSE)
                item.setEnabled(true);
            else
                item.setEnabled(false);
        }

        switch(item.getTag()) {
            case XJMainMenuBar.MI_SAVE:
                item.setEnabled(hasDirtyDocument());
                break;
        }
    }

    @Override
    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        super.handleMenuEvent(menu, item);

        if(item.getTag() == XJMainMenuBar.MI_CLOSE) {
            performClose(false);
        }
    }

    @Override
    public void windowActivated() {
        final XJMainMenuBar mmb = getMainMenuBar();
        if(mmb != null) {
            mmb.refresh();
        }

        if(hasDocuments()) {
            reloadDocuments();
        }
    }

    public void windowDocumentPathDidChange(XJDocument doc) {
        // can be used by subclasses to perform something when the document
        // associated file has changed (based on the file modification date)
    }

    public void selectDocument(XJDocument doc) {
        // can be used by subclasses to select the document visually.
    }
}
