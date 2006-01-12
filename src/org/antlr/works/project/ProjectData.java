package org.antlr.works.project;

import org.antlr.works.components.project.CContainerProject;

import java.util.*;
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

    protected static final String KEY_RUN_PARAMETERS = "KEY_RUN_PARAMETERS";
    protected static final String KEY_SHOW_BEFORE_RUNNING = "KEY_SHOW_BEFORE_RUNNING";

    protected static final String KEY_PROJECT_FILES = "KEY_PROJECT_FILES";
    protected static final String KEY_BUILD_LIST = "KEY_BUILD_LIST";

    protected CContainerProject project;

    protected Map data = new HashMap();
    protected List projectFiles = new ArrayList();
    protected ProjectBuildList buildList = new ProjectBuildList();

    public ProjectData() {
        setShowBeforeRunning(true);
    }

    public void setProject(CContainerProject project) {
        this.project = project;
    }

    public ProjectBuildList getBuildList() {
        return buildList;
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

    public ProjectFileItem addProjectFile(String filePath) {
        ProjectFileItem item = new ProjectFileItem(project, filePath);
        addProjectFile(item);
        return item;
    }

    public void addProjectFile(ProjectFileItem item) {
        projectFiles.add(item);
    }

    public void removeProjectFile(ProjectFileItem item) {
        projectFiles.remove(item);
    }

    public List getProjectFiles() {
        return projectFiles;
    }

    public void setProjectFilesData(List files) {
        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            Map data = (Map)iterator.next();
            ProjectFileItem item = new ProjectFileItem(project);
            item.setPersistentData(data);
            addProjectFile(item);
        }
    }

    public List getProjectFilesPersistentData() {
        List files = new ArrayList();
        for (Iterator iterator = getProjectFiles().iterator(); iterator.hasNext();) {
            ProjectFileItem item = (ProjectFileItem) iterator.next();
            files.add(item.getPersistentData());
        }
        return files;
    }

    /** Set the data stored on disk to our new class
     *
     */

    public void setPersistentData(Map inData) {
        data.clear();
        data.putAll(inData);

        setProjectFilesData((List)inData.get(KEY_PROJECT_FILES));
        buildList.setPersistentData((Map)inData.get(KEY_BUILD_LIST));
    }

    /** Returns the data that will be saved on the disk. Make
     * sure to use Java only structure to be isolated from
     * any future changes.
     */

    public Map getPersistentData() {
        data.put(KEY_PROJECT_FILES, getProjectFilesPersistentData());
        data.put(KEY_BUILD_LIST, buildList.getPersistentData());
        return data;
    }

}
