package org.antlr.works.project;

import edu.usfca.xj.foundation.XJUtils;

import java.io.File;
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

public class ProjectExplorerLoader {

    protected Map groups = new HashMap();
    protected String sourcePath;
    protected ProjectExplorer explorer;

    public ProjectExplorerLoader(ProjectExplorer projectExplorer) {
        this.explorer = projectExplorer;
    }

    public boolean reload() {
        boolean reset = false;

        if(sourcePath == null || !sourcePath.equals(explorer.project.getSourcePath())) {
            groups.clear();
            sourcePath = explorer.project.getSourcePath();
            reset = true;
        }

        if(sourcePath == null)
            return reset;

        List filesToRemove = getAllFiles();

        for (Iterator iterator = XJUtils.sortedFilesInPath(sourcePath).iterator(); iterator.hasNext();) {
            File f = (File) iterator.next();
            String name = f.getName();

            String type = ProjectFileItem.getFileType(name);
            if(type.equals(ProjectFileItem.FILE_TYPE_UNKNOWN))
                continue;

            ProjectFileItem item = addFileOfType(name, type);

            // Remove this file from the filesToRemove list because it exists
            filesToRemove.remove(item);
        }

        // Remove all files that where not existing on disk
        for (Iterator iterator = filesToRemove.iterator(); iterator.hasNext();) {
            ProjectFileItem fileItem = (ProjectFileItem) iterator.next();
            explorer.project.closeFileItem(fileItem);
            removeFileOfType(fileItem.getFileName(), fileItem.getFileType());
        }

        sortFilesInGroups();

        return reset;
    }

    public void sortFilesInGroups() {
        for (Iterator iterator = groups.keySet().iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            List files = (List)groups.get(name);
            if(files != null && !files.isEmpty())
                Collections.sort(files);
        }
    }

    public ProjectFileItem addFileOfType(String name, String type) {
        List files = getFiles(type);
        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            ProjectFileItem fileItem = (ProjectFileItem) iterator.next();
            if(fileItem.getFileName().equals(name))
                return fileItem;
        }
        ProjectFileItem fileItem = new ProjectFileItem(explorer.project, name);
        files.add(fileItem);
        return fileItem;
    }

    public void removeFileOfType(String name, String type) {
        List files = getFiles(type);
        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            ProjectFileItem fileItem = (ProjectFileItem) iterator.next();
            if(fileItem.getFileName().equals(name)) {
                files.remove(fileItem);
                break;
            }
        }
    }

    public List getGroups() {
        List sortedGroups = new ArrayList(groups.keySet());
        Collections.sort(sortedGroups);
        return sortedGroups;
    }

    public List getAllFiles() {
        List allFiles = new ArrayList();
        for (Iterator iterator = groups.values().iterator(); iterator.hasNext();) {
            List files = (List) iterator.next();
            allFiles.addAll(files);
        }
        return allFiles;
    }

    public ProjectFileItem getFileItemForFileName(String filename) {
        for (Iterator groupIterator = groups.values().iterator(); groupIterator.hasNext();) {
            List files = (List) groupIterator.next();
            for (Iterator fileIterator = files.iterator(); fileIterator.hasNext();) {
                ProjectFileItem item = (ProjectFileItem) fileIterator.next();
                if(item.getFileName().equals(filename))
                    return item;
            }
        }
        return null;
    }

    public List getFiles(String name) {
        List files = (List) groups.get(name);
        if(files == null) {
            files = new ArrayList();
            groups.put(name, files);
        }
        return files;
    }

}
