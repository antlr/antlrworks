package org.antlr.works.project;

import edu.usfca.xj.foundation.XJUtils;
import org.antlr.works.components.ComponentContainer;
import org.antlr.works.components.project.CContainerProject;

import java.util.HashMap;
import java.util.Map;
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

public class ProjectFileItem {

    protected static final String KEY_FILE_PATH = "KEY_FILE_PATH";

    protected CContainerProject project;
    protected ComponentContainer container;
    protected String filePath;
    protected String fileType;

    public ProjectFileItem(CContainerProject project) {
        this.project = project;
    }

    public ProjectFileItem(CContainerProject project, String filePath) {
        setFilePath(filePath);
        this.project = project;
    }

    public boolean isDirty() {
        if(container != null)
            return container.getDocument().isDirty();
        else
            return false;
    }

    public void save() {
        if(container != null)
            container.getDocument().performSave(false);
    }

    public void close() {
        if(container != null)
            container.close();
    }

    public void setComponentContainer(ComponentContainer container) {
        this.container = container;
    }
    
    public ComponentContainer getComponentContainer() {
        return container;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        this.fileType = CContainerProject.getFileType(filePath);
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return XJUtils.getLastPathComponent(getFilePath());
    }

    public String getFileFolder() {
        return XJUtils.getPathByDeletingLastComponent(getFilePath());
    }

    public String getFileType() {
        return fileType;
    }
    
    public void windowActivated() {
        if(container != null)
            container.getEditor().componentActivated();
    }

    public boolean handleExternalModification() {
        if(container == null)
            return false;

        if(container.getDocument().isModifiedOnDisk()) {
            container.getEditor().componentDocumentContentChanged();
            container.getDocument().synchronizeLastModifiedDate();
            return true;
        } else
            return false;
    }

    public void setPersistentData(Map data) {
        setFilePath((String)data.get(KEY_FILE_PATH));
    }

    public Map getPersistentData() {
        Map data = new HashMap();
        data.put(KEY_FILE_PATH, filePath);
        return data;
    }

    /** Called by the XJTree to display the cell content. Use only the last path component
     * (that is the name of file) only.
     */

    public String toString() {
        return getFileName();
    }

}
