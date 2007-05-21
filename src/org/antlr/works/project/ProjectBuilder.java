package org.antlr.works.project;

import org.antlr.xjlib.appkit.utils.XJDialogProgress;
import org.antlr.xjlib.appkit.utils.XJDialogProgressDelegate;
import org.antlr.xjlib.foundation.XJUtils;
import org.antlr.works.components.project.CContainerProject;
import org.antlr.works.engine.EngineRuntime;
import org.antlr.works.utils.StreamWatcherDelegate;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

public class ProjectBuilder implements StreamWatcherDelegate, XJDialogProgressDelegate {

    protected CContainerProject project;
    protected XJDialogProgress progress;

    protected boolean cancel;
    protected int buildingProgress;

    protected ProjectFileItem fileToBuild;
    protected ThreadExecution currentThread;

    public ProjectBuilder(CContainerProject project) {
        this.project = project;
        this.progress = new XJDialogProgress(project.getXJFrame(), true);
        this.progress.setDelegate(this);
    }

    public List<ProjectBuildList.BuildFile> getListOfDirtyBuildFiles(String type) {
        return project.getBuildList().getDirtyBuildFilesOfType(type);
    }

    public List<ProjectBuildList.BuildFile> buildListOfBuildFilesOfType(List<String> filesOnDisk, String fileType) {
        ProjectBuildList buildList = project.getBuildList();

        // Update the build list with list of files on the disk

        File[] files = new File(project.getSourcePath()).listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String filePath = file.getAbsolutePath();
            if(!ProjectFileItem.getFileType(filePath).equals(fileType))
                continue;

            if(buildList.isFileExisting(filePath, fileType))
                buildList.handleExternalModification(filePath, fileType);
            else
                buildList.addFile(filePath, fileType);
        }

        // Remove all non-existent file on disk that are still in the the build list.

        for (Iterator<ProjectBuildList.BuildFile> iterator = buildList.getBuildFilesOfType(fileType).iterator(); iterator.hasNext();)
        {
            ProjectBuildList.BuildFile buildFile = iterator.next();
            if(!new File(buildFile.getFilePath()).exists()) {
                // The file doesn't exist anymore. Remove it from the build list.
                buildList.removeFile(buildFile.getFilePath(), fileType);
            }
        }

        return getListOfDirtyBuildFiles(fileType);
    }

    public List<ProjectBuildList.BuildFile> buildListOfGrammarBuildFiles() {
        List<String> filesOnDisk = new ArrayList<String>();
        for (Iterator iterator = project.getFileEditorItems().iterator(); iterator.hasNext();) {
            ProjectFileItem item = (ProjectFileItem) iterator.next();
            filesOnDisk.add(item.getFilePath());
        }
        return buildListOfBuildFilesOfType(filesOnDisk, ProjectFileItem.FILE_TYPE_GRAMMAR);
    }

    public List<ProjectBuildList.BuildFile> buildListOfJavaBuildFiles() {
        List<String> filesOnDisk = new ArrayList<String>();
        File[] files = new File(project.getSourcePath()).listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            filesOnDisk.add(file.getAbsolutePath());
        }
        return buildListOfBuildFilesOfType(filesOnDisk, ProjectFileItem.FILE_TYPE_JAVA);
    }

    public boolean generateGrammarBuildFiles(List<ProjectBuildList.BuildFile> buildFiles) {
        for (Iterator<ProjectBuildList.BuildFile> iterator = buildFiles.iterator(); iterator.hasNext() && !cancel;) {
            ProjectBuildList.BuildFile buildFile = iterator.next();

            String file = buildFile.getFilePath();
            String libPath = buildFile.getFileFolder();
            String outputPath = buildFile.getFileFolder();

            setProgressStepInfo("Generating \""+ XJUtils.getLastPathComponent(file)+"\"...");

            String error = EngineRuntime.runANTLR(null, file, libPath, outputPath, this);
            if(error != null) {
                project.buildReportError(error);
                return false;
            } else {
                buildFile.setDirty(false);
                project.changeDone();
            }
        }
        return true;
    }

    public boolean compileFile(String file) {
        String outputPath = project.getSourcePath();
        String error = EngineRuntime.compileFiles(null, new String[] { file }, outputPath, this);
        if(error != null) {
            project.buildReportError(error);
            return false;
        } else {
            return true;
        }
    }

    public boolean compileJavaBuildFiles(List<ProjectBuildList.BuildFile> buildFiles) {
        for (Iterator<ProjectBuildList.BuildFile> iterator = buildFiles.iterator(); iterator.hasNext() && !cancel;) {
            ProjectBuildList.BuildFile buildFile = iterator.next();
            String file = buildFile.getFilePath();
            setProgressStepInfo("Compiling \""+ XJUtils.getLastPathComponent(file)+"\"...");
            if(!compileFile(file))
                return false;
            else {
                buildFile.setDirty(false);
                project.changeDone();
            }
        }
        return true;
    }

    public void setProgressStepInfo(String info) {
        progress.setInfo(info);
        progress.setProgress(++buildingProgress);
    }

    public boolean performBuild() {
        List<ProjectBuildList.BuildFile> grammars = buildListOfGrammarBuildFiles();
        List<ProjectBuildList.BuildFile> javas = buildListOfJavaBuildFiles();

        int total = grammars.size()+javas.size();
        if(total == 0)
            return true;

        progress.setIndeterminate(false);
        progress.setProgress(0);
        progress.setProgressMax(total);

        if(generateGrammarBuildFiles(grammars) && !cancel) {
            if(grammars.size() > 0) {
                // Rebuild the list of Java files because ANTLR may have
                // generated some ;-)

                javas = buildListOfJavaBuildFiles();
                total = grammars.size()+javas.size();
                progress.setProgressMax(total);
            }
            if(compileJavaBuildFiles(javas))
                return true;
        }
        return false;
    }

    public void performBuildFile() {
        String type = fileToBuild.getFileType();
        List<ProjectBuildList.BuildFile> files;

        if(type.equals(ProjectFileItem.FILE_TYPE_GRAMMAR))
            files = buildListOfGrammarBuildFiles();
        else if(type.equals(ProjectFileItem.FILE_TYPE_JAVA))
            files = buildListOfJavaBuildFiles();
        else
            return;

        for (Iterator<ProjectBuildList.BuildFile> iterator = files.iterator(); iterator.hasNext();) {
            ProjectBuildList.BuildFile buildFile = iterator.next();
            if(buildFile.getFilePath().equals(fileToBuild.getFilePath())) {
                List<ProjectBuildList.BuildFile> f = Collections.singletonList(buildFile);
                if(type.equals(ProjectFileItem.FILE_TYPE_GRAMMAR))
                    generateGrammarBuildFiles(f);
                else if(type.equals(ProjectFileItem.FILE_TYPE_JAVA))
                    compileJavaBuildFiles(f);
            }
        }
    }

    public void prepare() {
        cancel = false;
        buildingProgress = 0;
        currentThread = null;
    }

    public void buildFile(ProjectFileItem fileItem) {
        fileToBuild = fileItem;

        progress.setCancellable(true);
        progress.setTitle("Build");
        progress.setInfo("Building...");
        progress.setIndeterminate(true);

        prepare();

        currentThread = new ThreadExecution(new Runnable() {
            public void run() {
                performBuildFile();
                progress.close();
            }
        });

        progress.runModal();
    }

    public void buildAll() {
        progress.setCancellable(true);
        progress.setTitle("Build");
        progress.setInfo("Preparing...");
        progress.setIndeterminate(true);

        prepare();

        currentThread = new ThreadExecution(new Runnable() {
            public void run() {
                performBuild();
                progress.close();
            }
        });

        progress.runModal();
    }

    public void performRun() {
        String error = EngineRuntime.runJava(null, project.getSourcePath(), project.getRunParameters(), ProjectBuilder.this);
        if(error != null) {
            project.buildReportError(error);
        }
    }

    public void run() {
        progress.setCancellable(true);
        progress.setTitle("Run");
        progress.setInfo("Preparing...");
        progress.setIndeterminate(true);

        prepare();

        currentThread = new ThreadExecution(new Runnable() {
            public void run() {
                if(performBuild() && !cancel) {
                    progress.setInfo("Running...");
                    progress.setIndeterminate(true);
                    performRun();
                }
                progress.close();
            }
        });

        progress.runModal();
    }

    /** This method cleans the project directory by removing the following files:
     * - *.class
     */

    public void clean() {
        File[] files = new File(project.getSourcePath()).listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String filePath = file.getAbsolutePath();
            if(filePath.endsWith(".class")) {
                file.delete();
            }
        }

        // Mark all files as dirty
        project.getBuildList().setAllFilesToDirty(true);

        // Mark the project as dirty
        project.changeDone();
    }

    public void dialogDidCancel() {
        if(cancel) {
            // The process may be blocked. Try to kill it.
            Process p = EngineRuntime.getProcess(currentThread.t);
            if(p != null)
                p.destroy();
        }
        cancel = true;
    }

    public void streamWatcherDidStarted() {

    }

    public void streamWatcherDidReceiveString(String string) {
        project.printToConsole(string);
    }

    public void streamWatcherException(Exception e) {
        project.printToConsole(e);
    }

    protected class ThreadExecution {

        protected Runnable r;
        protected Thread t;

        public ThreadExecution(Runnable r) {
            this.r = r;
            launch();
        }

        public void launch() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    t = new Thread(r);
                    t.start();
                }
            });
        }
    }
}
