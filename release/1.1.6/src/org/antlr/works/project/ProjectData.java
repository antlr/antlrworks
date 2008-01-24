package org.antlr.works.project;

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

public class ProjectData {

    protected static final String KEY_SOURCE_PATH = "KEY_SOURCE_PATH";
    protected static final String KEY_RUN_PARAMETERS = "KEY_RUN_PARAMETERS";
    protected static final String KEY_SHOW_BEFORE_RUNNING = "KEY_SHOW_BEFORE_RUNNING";

    protected static final String KEY_EDITORZONE_DATA = "KEY_EDITORZONE_DATA";
    protected static final String KEY_EXPLORER_DATA = "KEY_EXPLORER_DATA";
    protected static final String KEY_BUILD_LIST = "KEY_BUILD_LIST";
    protected static final String KEY_CONTAINER_DATA = "KEY_CONTAINER_DATA";

    protected CContainerProject project;

    protected Map data = new HashMap();

    public ProjectData() {
        setShowBeforeRunning(true);
    }

    public void setProject(CContainerProject project) {
        this.project = project;
    }

    public void setSourcePath(String path) {
        data.put(KEY_SOURCE_PATH, path);
    }

    public String getSourcePath() {
        return (String)data.get(KEY_SOURCE_PATH);
    }

    public void setRunParametersString(String s) {
        data.put(KEY_RUN_PARAMETERS, s);
    }

    public String getRunParametersString() {
        return (String)data.get(KEY_RUN_PARAMETERS);
    }

    public void setShowBeforeRunning(boolean flag) {
        data.put(KEY_SHOW_BEFORE_RUNNING, Boolean.valueOf(flag));
    }

    public boolean getShowBeforeRunning() {
        Boolean b = (Boolean)data.get(KEY_SHOW_BEFORE_RUNNING);
        if(b != null)
            return b.booleanValue();
        else
            return false;
    }

    public void setBuildListData(Object inData) {
        data.put(KEY_BUILD_LIST, inData);
    }

    public Object getBuildListData() {
        return data.get(KEY_BUILD_LIST);
    }

    public void setExplorerData(Object inData) {
        data.put(KEY_EXPLORER_DATA, inData);
    }

    public Object getExplorerData() {
        return data.get(KEY_EXPLORER_DATA);
    }

    public void setEditorZoneData(Object inData) {
        data.put(KEY_EDITORZONE_DATA, inData);
    }

    public Object getEditorZoneData() {
        return data.get(KEY_EDITORZONE_DATA);
    }

    public void setContainerData(Map inData) {
        data.put(KEY_CONTAINER_DATA, inData);
    }

    public Object getContainerData() {
        return data.get(KEY_CONTAINER_DATA);                    
    }

    /** Set the data stored on disk to our new class
     *
     */

    public void setPersistentData(Map inData) {
        data.clear();
        data.putAll(inData);
    }

    /** Returns the data that will be saved on the disk. Make
     * sure to use Java only structure to be isolated from
     * any future changes.
     */

    public Map getPersistentData() {
        return data;
    }

}
