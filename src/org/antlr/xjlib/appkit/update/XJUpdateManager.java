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

package org.antlr.xjlib.appkit.update;

import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.frame.XJDialog;
import org.antlr.xjlib.appkit.utils.XJAlert;
import org.antlr.xjlib.appkit.utils.XJDialogProgress;
import org.antlr.xjlib.appkit.utils.XJDialogProgressDelegate;
import org.antlr.xjlib.foundation.XJUtils;

import java.awt.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class XJUpdateManager {

    public static final String KEY_VERSION = "KEY_VERSION";
    public static final String KEY_APP_NAME = "KEY_APP_NAME";
    public static final String KEY_DESCRIPTION = "KEY_DESCRIPTION";
    public static final String KEY_DOWNLOAD_FILE_URL = "KEY_DOWNLOAD_FILE_URL";
    public static final String KEY_DOWNLOAD_FILE_NAME = "KEY_DOWNLOAD_FILE_NAME";
    public static final String KEY_DOWNLOAD_SIZE = "KEY_DOWNLOAD_SIZE";

    protected Container parent;
    protected XJUpdateManagerDelegate delegate;
    protected Map updateInfoMap;

    protected boolean cancelDownload = false;

    public XJUpdateManager(Container parent, XJUpdateManagerDelegate delegate) {
        this.parent = parent == null?XJApplication.getActiveContainer():parent;
        this.delegate = delegate==null?new DefaultDownloadUpdateDelegate():delegate;
    }

    public Container getParentContainer() {
        return parent;
    }

    public String getApplicationName() {
        if(updateInfoMap == null)
            return null;
        else
            return (String)updateInfoMap.get(KEY_APP_NAME);
    }

    public String getDownloadVersion() {
        if(updateInfoMap == null)
            return null;
        else
            return (String)updateInfoMap.get(KEY_VERSION);
    }

    public String getDownloadFileName() {
        if(updateInfoMap == null)
            return null;
        else
            return (String)updateInfoMap.get(KEY_DOWNLOAD_FILE_NAME);
    }

    public String getDownloadFileURL() {
        if(updateInfoMap == null)
            return null;

        return (String)updateInfoMap.get(KEY_DOWNLOAD_FILE_URL);
    }

    public long getDownloadSize() {
        if(updateInfoMap == null)
            return 0;
        else
            return (Long) updateInfoMap.get(KEY_DOWNLOAD_SIZE);
    }

    public String getDescription() {
        if(updateInfoMap == null)
            return null;
        else
            return (String)updateInfoMap.get(KEY_DESCRIPTION);
    }

    public boolean writeUpdateXMLFile(String version, String appName, String description,
                                      String downloadFileName, String downloadFileURL,
                                      long downloadFileSize, String outputFile) {
        XMLEncoder encoder;
        try {
            encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(outputFile)));
        } catch (FileNotFoundException e) {
            XJAlert.display(getParentContainer(), "Update Manager", "Cannot write the update xml file because:\n"+e);
            return false;
        }

        Map<String,Serializable> update = new HashMap<String, Serializable>();
        update.put(KEY_VERSION, version);
        update.put(KEY_APP_NAME, appName);
        update.put(KEY_DESCRIPTION, description);
        update.put(KEY_DOWNLOAD_FILE_NAME, downloadFileName);
        update.put(KEY_DOWNLOAD_FILE_URL, downloadFileURL);
        update.put(KEY_DOWNLOAD_SIZE, downloadFileSize);

        encoder.writeObject(update);
        encoder.close();

        return true;
    }

    public void downloadUpdateToLocalDisk(String localFile) {
        downloadUpdateToLocalDisk(getDownloadFileURL(), localFile);
    }

    public void downloadUpdateToLocalDisk(String urlString, String localFile) {
        new File(localFile).delete();

        BufferedOutputStream localOutputStream;
        try {
            localOutputStream = new BufferedOutputStream(new FileOutputStream(localFile));
        } catch (FileNotFoundException e) {
            XJAlert.display(getParentContainer(), "Update Manager", "Cannot download the update because:\n"+e);
            return;
        }

        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            XJAlert.display(getParentContainer(), "Update Manager", "Cannot download the update because:\n"+e);
            return;
        }

        InputStream is;

        try {
            is = url.openStream();
        } catch (IOException e) {
            XJAlert.display(getParentContainer(), "Update Manager", "Cannot download the update because:\n"+e);
            return;
        }

        delegate.umDownloadBegin();

        new Thread(new BackgroundDownloader(is, localOutputStream, localFile)).start();
    }

    public void fetchRemoteUpdateInformation(String urlString, boolean silent) {
        updateInfoMap = null;

        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            if(!silent)
                XJAlert.display(getParentContainer(), "Update Manager", "Cannot check the update because:\n"+e);
            return;
        }

        InputStream is;

        try {
            is = url.openStream();
        } catch (IOException e) {
            if(!silent)
                XJAlert.display(getParentContainer(), "Update Manager", "Cannot check the update because:\n"+e);
            return;
        }

        XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(is));
        try {
            updateInfoMap = (Map)decoder.readObject();
        } catch(Exception e) {
            XJAlert.display(getParentContainer(), "Update Manager", "Cannot check the update because:\n"+e);
            return;            
        }
        decoder.close();
    }

    public boolean isUpdateAvailable(String version) {
        if(updateInfoMap == null)
            return false;

        return XJUtils.isVersionGreaterThan(getDownloadVersion(), version);
    }

    public void cancelDownload() {
        setCancelDownload(true);
    }

    public synchronized void setCancelDownload(boolean flag) {
        this.cancelDownload = flag;
    }

    public boolean isCancelDownload() {
        return cancelDownload;
    }

    public void checkForUpdates(String version, String remoteUpdateFile, String localDownloadPath, boolean silent) {
        fetchRemoteUpdateInformation(remoteUpdateFile, silent);
        if(isUpdateAvailable(version)) {
            if(new XJUpdateManagerDialogUpdateAvailable(this).runModal() == XJDialog.BUTTON_OK) {
                downloadUpdateToLocalDisk(XJUtils.concatPath(localDownloadPath, getDownloadFileName()));
            }
        } else if(updateInfoMap != null) {
            if(!silent)
                XJAlert.display(getParentContainer(), "Check for Updates", "You already have the latest version.\nCheck again later.");
        }
    }

    protected class DefaultDownloadUpdateDelegate extends XJUpdateManagerDelegate implements XJDialogProgressDelegate {

        protected XJDialogProgress progress;

        public void umDownloadBegin() {
            progress = new XJDialogProgress(parent);
            progress.setInfo("Downloading...");
            progress.setProgress(0);
            progress.setProgressMax(100);
            progress.setDelegate(this);

            progress.display();
        }

        public void umDownloadProgress(long current, long total) {
            progress.setProgress((float)current/total*100);
        }

        public void umDownloadCancelled() {
            progress.close();
            XJAlert.display(getParentContainer(), "Check for Updates", "The download has been cancelled.");
        }

        public void umDownloadCompleted(String localDownloadFile) {
            progress.close();
            XJAlert.display(getParentContainer(), "Check for Updates", "The new version has been downloaded and is available here:\n"+localDownloadFile);
        }

        public void dialogDidCancel() {
            cancelDownload();
        }
    }

    protected class BackgroundDownloader implements Runnable {

        protected InputStream is;
        protected OutputStream os;
        protected String localDownloadFile;

        public BackgroundDownloader(InputStream is, OutputStream os, String localDownloadFile) {
            this.is = is;
            this.os = os;
            this.localDownloadFile = localDownloadFile;
        }

        public void run() {
            byte[] b = new byte[1024*8];
            int r;
            long current = 0;
            long total = getDownloadSize();
            try {
                while((r = is.read(b)) > 0 && !isCancelDownload()) {
                    current += r;
                    delegate.umDownloadProgress(current, total);
                    os.write(b, 0, r);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
                try {
                    os.close();
                } catch (IOException e) {
                    // ignore
                }

                if(isCancelDownload())
                    delegate.umDownloadCancelled();
                else
                    delegate.umDownloadCompleted(localDownloadFile);
            }
        }
    }
}
