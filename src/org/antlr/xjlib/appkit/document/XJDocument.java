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

package org.antlr.xjlib.appkit.document;

import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.frame.XJWindow;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.utils.XJFileChooser;
import org.antlr.xjlib.appkit.utils.XJLocalizable;
import org.antlr.xjlib.foundation.XJObject;
import org.antlr.xjlib.foundation.XJUtils;

import java.awt.*;
import java.io.*;
import java.util.List;

/**
 * A document always has data and a window to display the data.
 */
public class XJDocument extends XJObject {

    protected XJData documentData;
    protected XJWindow documentWindow;

    protected String documentTitle = XJLocalizable.getXJString("DocUntitled");
    protected String documentPath;

    protected List documentFileExts;
    protected String documentFileExtDescription;

    protected boolean dirty = false;
    protected boolean firstDocument = false;
    protected boolean writing = false;

    protected Component javaContainer;

    protected static int absoluteCounter = 0;

    protected final XJFileMonitor fileMonitor = new XJFileMonitor();

    public XJDocument() {
        this.firstDocument = absoluteCounter == 0;
        absoluteCounter++;
    }

    @Override
    public void awake() {
        super.awake();
        if(documentWindow != null) {
            documentWindow.awake();
        }
    }

    /**
     * Returns true if the document is internal: if this is the case, it will not be
     * added and handled by XJApplication.
     */
    public boolean isInternalOnly() {
        return false;
    }

    public boolean isFirstDocument() {
        return firstDocument;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void changeDone() {
        if(isDirty())
            return;

        setDirty(true);
        if(documentWindow != null)
            documentWindow.setDirty();
    }

    public void changeReset() {
        setDirty(false);
        if(documentWindow != null)
            documentWindow.resetDirty();
    }

    public void setTitle(String title) {
        documentTitle = title;
        if(documentWindow != null)
            documentWindow.setTitle(documentTitle);
    }

    public void setWindow(XJWindow window) {
        documentWindow = window;
        if(documentWindow != null) {
            documentWindow.addDocument(this);
            documentWindow.setTitle(documentTitle);
        }
    }

    public XJWindow getWindow() {
        return documentWindow;
    }

    public void setJavaContainer(Component container) {
        this.javaContainer = container;
    }

    /**
     * Returns the original Swing container
     */
    public Component getSwingComponent() {
        if(javaContainer == null)
            return getWindow() == null ? null : getWindow().getJavaContainer();
        else
            return javaContainer;
    }

    public void showWindow() {
        if(documentWindow != null)
            documentWindow.show();
    }

    public void setDocumentData(XJData data) {
        this.documentData = data;
        if(documentData != null) {
            documentData.addObserver(this);
        }
    }

    public XJData getDocumentData() {
        return documentData;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public String getDocumentFolder() {
        return XJUtils.getPathByDeletingLastComponent(getDocumentPath());
    }

    public String getDocumentName() {
        return XJUtils.getLastPathComponent(documentPath);
    }

    public String getDocumentNameWithoutExtension() {
        return XJUtils.getPathByDeletingPathExtension(getDocumentName());
    }

    public void setDocumentFileType(List ext, String description) {
        documentFileExts = ext;
        documentFileExtDescription = description;
    }

    @Override
    public void observeValueForKey(Object sender, String key, Object object) {
        if(!writing)
            changeDone();
    }

    public boolean load(String file) throws Exception {
        documentPath = file;
        return reload();
    }

    public boolean reload() throws Exception {
        readDocument(documentPath);

        setTitle(documentPath);
        changeReset();

        return XJApplication.YES;
    }

    public boolean autoSave() {
        return !(getDocumentPath() != null && isDirty()) || save(false);
    }

    public boolean save(boolean saveAs) {
        if(documentPath == null || saveAs) {
            if(!XJFileChooser.shared().displaySaveDialog(getSwingComponent(), documentFileExts, documentFileExtDescription, true))
                return XJApplication.NO;

            documentPath = XJFileChooser.shared().getSelectedFilePath();
            XJApplication.shared().addRecentFile(documentPath);
        }

        try {
            writeDocument(documentPath);
        } catch(Exception e) {
            e.printStackTrace();
            XJAlert.display(getSwingComponent(), XJLocalizable.getXJString("DocError"), XJLocalizable.getXJString("DocSaveError")+" "+e.toString());
            return XJApplication.NO;
        }

        setTitle(documentPath);
        changeReset();
        return XJApplication.YES;
    }

    public boolean close(boolean force) {
        if(performClose(force)) {
            if(!isInternalOnly()) {
                XJApplication.shared().addRecentFile(getDocumentPath());
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean isModifiedOnDisk() {
        return fileMonitor.isModifiedOnDisk(getDocumentPath());
    }

    public void synchronizeLastModifiedDate() {
        fileMonitor.synchronizeLastModifiedDate(getDocumentPath());
    }

    public long getDateOfModificationOnDisk() {
        return fileMonitor.getDateOfModificationOnDisk(getDocumentPath());
    }

    private boolean performClose(boolean force) {
        if(force || !isDirty() || !XJApplication.shared().supportsPersistence())
            return XJApplication.YES;

        if(documentWindow != null)
            documentWindow.bringToFront();

        XJAlert.disableEscapeKey();
        try {
            int r = XJAlert.displayAlertYESNOCANCEL(getSwingComponent(), XJLocalizable.getXJString("DocCloseTitle"), XJLocalizable.getStringFormat("DocCloseMessage", documentTitle));
            switch(r) {
                case XJAlert.YES:
                    return save(false);

                case XJAlert.NO:
                    return XJApplication.YES;

                case XJAlert.CANCEL:
                    return XJApplication.NO;
            }
        } finally {
            XJAlert.enableEscapeKey();
        }
        return XJApplication.YES;
    }

    private void beginWrite() {
        writing = true;
    }

    private void endWrite() {
        writing = false;
    }

    private void writeDocument(String file) throws IOException {
        beginWrite();
        try {
            documentWillWriteData();
            documentData.setFile(file);
            switch(documentData.dataType()) {
                case XJData.DATA_INPUTSTREAM: {
                    OutputStream os = new FileOutputStream(file);
                    documentData.writeData(os);
                    os.close();
                    break;
                }

                case XJData.DATA_OBJECTINPUTSTREAM: {
                    OutputStream os = new FileOutputStream(file);
                    documentData.writeData(new ObjectOutputStream(os));
                    os.close();
                    break;
                }

                case XJData.DATA_PLAINTEXT:
                    documentData.writeData();
                    break;

                case XJData.DATA_XML:
                    documentData.writeData();
                    break;
            }
            synchronizeLastModifiedDate();
        } finally {
            endWrite();
        }
    }

    private void readDocument(String file) throws Exception {
        documentWillReadData();
        documentData.setFile(file);
        switch(documentData.dataType()) {
            case XJData.DATA_INPUTSTREAM: {
                InputStream is = new FileInputStream(file);
                documentData.readData(is);
                is.close();
                break;
            }

            case XJData.DATA_OBJECTINPUTSTREAM: {
                InputStream is = new FileInputStream(file);
                documentData.readData(new ObjectInputStream(is));
                is.close();
                break;
            }

            case XJData.DATA_PLAINTEXT:
                documentData.readData();
                break;

            case XJData.DATA_XML:
                documentData.readData();
                break;
        }
        documentDidReadData();
        synchronizeLastModifiedDate();
    }

    // Subclasses only

    public void documentWillWriteData() {

    }

    public void documentWillReadData() {

    }

    public void documentDidReadData() {

    }

}
