package org.antlr.works.project;

import org.antlr.xjlib.foundation.XJUtils;
import org.antlr.works.components.ComponentContainer;
import org.antlr.works.components.project.CContainerProject;

import javax.swing.*;
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

public class ProjectFileItem implements Comparable {

    public static final String FILE_GRAMMAR_EXTENSION = ".g";
    public static final String FILE_JAVA_EXTENSION = ".java";
    public static final String FILE_STG_EXTENSION = ".stg";
    public static final String FILE_ST_EXTENSION = ".st";
    public static final String FILE_MANTRA_EXTENSION = ".om";
    public static final String FILE_TEXT_EXTENSION = ".txt";

    public static final String FILE_TYPE_GRAMMAR = "FILE_TYPE_GRAMMAR";
    public static final String FILE_TYPE_JAVA = "FILE_TYPE_JAVA";
    public static final String FILE_TYPE_STG = "FILE_TYPE_STG";
    public static final String FILE_TYPE_ST = "FILE_TYPE_ST";
    public static final String FILE_TYPE_MANTRA = "FILE_TYPE_MANTRA";
    public static final String FILE_TYPE_TEXT = "FILE_TYPE_TEXT";
    public static final String FILE_TYPE_UNKNOWN = "FILE_TYPE_UNKNOWN";

    protected CContainerProject project;
    protected ComponentContainer container;

    protected String fileName;
    protected String fileType;
    protected boolean opened;
    protected int tabIndex;
    protected Map containerData;

    public ProjectFileItem(CContainerProject project, String name) {
        setFileName(name);
        this.project = project;
    }

    public static String getFileType(String filePath) {
        if(filePath.endsWith(FILE_GRAMMAR_EXTENSION))
            return FILE_TYPE_GRAMMAR;
        if(filePath.endsWith(FILE_STG_EXTENSION))
            return FILE_TYPE_STG;
        if(filePath.endsWith(FILE_ST_EXTENSION))
            return FILE_TYPE_ST;
        if(filePath.endsWith(FILE_JAVA_EXTENSION))
            return FILE_TYPE_JAVA;
        if(filePath.endsWith(FILE_MANTRA_EXTENSION))
            return FILE_TYPE_MANTRA;
        if(filePath.endsWith(FILE_TEXT_EXTENSION))
            return FILE_TYPE_TEXT;

        return FILE_TYPE_UNKNOWN;
    }

    public static String getFileTypeName(String type) {
        if(type.equals(FILE_TYPE_GRAMMAR))
            return "Grammar";
        if(type.equals(FILE_TYPE_STG))
            return "ST Group";
        if(type.equals(FILE_TYPE_ST))
            return "ST";
        if(type.equals(FILE_TYPE_JAVA))
            return "Java";
        if(type.equals(FILE_TYPE_MANTRA))
            return "Mantra";
        if(type.equals(FILE_TYPE_TEXT))
            return "Text";

        return "Unkown";
    }

    public void setOpened(boolean flag) {
        opened = flag;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setTabIndex(int index) {
        this.tabIndex = index;
    }

    public int getTabIndex() {
        return tabIndex;
    }

    public boolean isDirty() {
        if(container != null)
            return container.getDocument().isDirty();
        else
            return false;
    }

    public boolean save() {
        if(container != null && container.getDocument().isDirty())
            return container.getDocument().performSave(false);
        else
            return false;
    }

    public void close() {
        if(container != null)
            container.close();
    }

    public void setComponentContainer(ComponentContainer container) {
        this.container = container;
        this.container.setPersistentData(containerData);
    }

    public ComponentContainer getComponentContainer() {
        return container;
    }

    public void setContainerPersistentData(Map data) {
        this.containerData = data;
    }

    public Map getContainerPersistentData() {
        if(container == null)
            return null;
        else
            return container.getPersistentData();
    }

    public JPanel getEditorPanel() {
        if(container == null)
            return null;
        else
            return container.getEditor().getPanel();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        this.fileType = getFileType(fileName);
    }

    public String getFilePath() {
        return XJUtils.concatPath(project.getSourcePath(), fileName);
    }

    public String getFileName() {
        return fileName;
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

    protected static final String KEY_FILE_NAME = "KEY_FILE_NAME";
    protected static final String KEY_FILE_OPENED = "KEY_FILE_OPENED";
    protected static final String KEY_TAB_INDEX = "KEY_TAB_INDEX";
    protected static final String KEY_CONTAINER_DATA = "KEY_CONTAINER_DATA";

    public void setPersistentData(Map data) {
        setFileName((String)data.get(KEY_FILE_NAME));
        setOpened((Boolean) data.get(KEY_FILE_OPENED));
        setTabIndex((Integer) data.get(KEY_TAB_INDEX));
        setContainerPersistentData((Map)data.get(KEY_CONTAINER_DATA));
    }

    public Map<String,Object> getPersistentData() {
        Map<String,Object> data = new HashMap<String, Object>();
        data.put(KEY_FILE_NAME, fileName);
        data.put(KEY_FILE_OPENED, opened);
        data.put(KEY_TAB_INDEX, tabIndex);

        Map d = getContainerPersistentData();
        if(d != null)
            data.put(KEY_CONTAINER_DATA, d);

        return data;
    }

    public int hashCode() {
        return fileName.hashCode();
    }

    public boolean equals(Object o) {
        if(o instanceof ProjectFileItem) {
            ProjectFileItem otherItem = (ProjectFileItem)o;
            return fileName.equals(otherItem.fileName);
        } else {
            return false;
        }
    }

    public int compareTo(Object o) {
        if(o instanceof ProjectFileItem) {
            ProjectFileItem otherItem = (ProjectFileItem)o;
            return fileName.compareTo(otherItem.fileName);
        } else {
            return 1;
        }
    }

    /** Called by the XJTree to display the cell content. Use only the last path component
     * (that is the name of file) only.
     */

    public String toString() {
        return getFileName();
    }

}
