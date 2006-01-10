package org.antlr.works.project;

import org.antlr.works.components.project.CContainerProject;
import org.antlr.works.engine.EngineCompiler;
import org.antlr.works.engine.StreamWatcherDelegate;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
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

public class ProjectBuilder implements StreamWatcherDelegate {

    protected CContainerProject project;

    public ProjectBuilder(CContainerProject project) {
        this.project = project;
    }

    public List buildListOfGrammarItems(List items) {
        List itemsToBuild = new ArrayList();
        for (Iterator iterator = items.iterator(); iterator.hasNext();) {
            ProjectFileItem item = (ProjectFileItem) iterator.next();
            if(item.getFileName().endsWith(ProjectFileItem.FILE_GRAMMAR_EXTENSION) && item.buildDirty()) {
                itemsToBuild.add(item);
            }
        }
        return itemsToBuild;
    }

    public boolean generateGrammarItems(List items) {
        for (Iterator iterator = items.iterator(); iterator.hasNext();) {
            ProjectFileItem item = (ProjectFileItem) iterator.next();

            String file = item.getFilePath();
            String libPath = item.getFileFolder();
            String outputPath = item.getFileFolder();
            String error = EngineCompiler.runANTLR(file, libPath, outputPath, this);
            if(error != null) {
                project.buildReportError(error);
                return false;
            } else {
                item.setBuildDirty(false);
            }
        }
        return true;
    }

    public boolean compileFile(String file) {
        String outputPath = project.getProjectFolder();
        String error = EngineCompiler.compileFiles(new String[] { file }, outputPath, this);
        if(error != null) {
            project.buildReportError(error);
            return false;
        } else {
            return true;
        }
    }

    public boolean compileAllJavaFiles() {
        File[] files = new File(project.getProjectFolder()).listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if(!file.getName().endsWith(ProjectFileItem.FILE_JAVA_EXTENSION))
                continue;

            if(!compileFile(file.getAbsolutePath())) {
                return false;
            }
        }
        return true;
    }

    public boolean build() {
        List items = project.getFileEditorItems();
        List grammars = buildListOfGrammarItems(items);

        if(!generateGrammarItems(grammars))
            return false;

        compileAllJavaFiles();

        return true;
    }

    public void streamWatcherDidStarted() {

    }

    public void streamWatcherDidReceiveString(String string) {
        project.buildPrint(string);
    }

    public void streamWatcherException(Exception e) {
        project.buildPrint(e);
    }
}
