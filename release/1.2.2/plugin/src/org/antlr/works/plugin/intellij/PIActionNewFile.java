package org.antlr.works.plugin.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.antlr.xjlib.appkit.utils.XJAlertInput;
import org.antlr.xjlib.foundation.XJUtils;
import org.antlr.works.utils.IconManager;

import java.io.File;
import java.io.IOException;

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

public class PIActionNewFile extends AnAction {

    public PIActionNewFile() {
        super("Grammar File", "Create New ANTLR 3 Grammar File", IconManager.shared().getIconApplication16x16());
    }

    public void actionPerformed(AnActionEvent e) {
        String selectedFile = getSelectedFile(e);
        if(selectedFile == null) {
            Messages.showMessageDialog("Cannot get the selected file in IntelliJ!", "ANTLRWorks Plugin",
                    IconManager.shared().getIconApplication32x32());
            return;
        }

        String rootFolder;
        if(XJUtils.getLastPathComponent(selectedFile).contains("."))
            rootFolder = XJUtils.getPathByDeletingLastComponent(selectedFile);
        else
            rootFolder = selectedFile;

        String name = askUserForFile();
        if(name == null)
            return;

        String file = XJUtils.concatPath(rootFolder, name);
        try {
            XJUtils.writeStringToFile("grammar untitled;\n", file);
        } catch (IOException e1) {
            Messages.showMessageDialog("Cannot create the new grammar file:\n"+e1.toString(), "ANTLRWorks Plugin",
                    IconManager.shared().getIconApplication32x32());
            return;
        }

        Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
        openFile(project, file);
    }

    /** Returns the first selected file in the IntelliJ project panel
     *
     */
    private String getSelectedFile(AnActionEvent e) {
        VirtualFile[] files = (VirtualFile[]) e.getDataContext().getData(DataConstants.VIRTUAL_FILE_ARRAY);
        if(files != null && files.length > 0)
            return files[0].getPath();
        else
            return null;
    }

    /** Open the file in IntelliJ
     *
     */
    private void openFile(Project project, String file) {
        ApplicationManager.getApplication().runWriteAction(new MyShowFile(file, project));
    }

    /** Asks the user for the name of the file
     *
     */
    private String askUserForFile() {
        String name = XJAlertInput.showInputDialog(null, "New Grammar File",
                "Enter a new file name:", "");
        if(name == null)
            return name;

        if(name.contains(".")) {
            if(name.endsWith(".g"))
                return name;
            else
                return name+".g";
        } else
            return name+".g";
    }

    private static class MyShowFile implements Runnable {

        private String file;
        private final Project project;

        public MyShowFile(String file, Project project) {
            this.file = file;
            this.project = project;
        }

        public void run() {
            VirtualFile vfile = VirtualFileManager.getInstance().refreshAndFindFileByUrl("file://"+file.replace(File.separatorChar, '/'));
            if(vfile == null) {
                Messages.showMessageDialog("Cannot find the newly created grammar file:\n"+file,
                        "ANTLRWorks Plugin",
                        IconManager.shared().getIconApplication32x32());
                return;
            }
            FileEditorManager fem = FileEditorManager.getInstance(project);
            fem.openFile(vfile, true);
        }
    }
}
