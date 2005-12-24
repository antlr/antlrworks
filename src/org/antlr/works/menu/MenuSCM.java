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


package org.antlr.works.menu;

import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.utils.XJDialogProgress;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.scm.SCM;
import org.antlr.works.scm.SCMDelegate;
import org.antlr.works.scm.p4.P4;
import org.antlr.works.scm.p4.P4SubmitDialog;

public class MenuSCM extends MenuAbstract implements SCMDelegate {

    protected XJDialogProgress progress;
    protected SCM scm;
    protected boolean silent;

    public MenuSCM(EditorWindow editor) {
        super(editor);
    }

    public void awake() {
        scm = new P4(this, editor.console);
        progress = new XJDialogProgress(editor.getWindowContainer());
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public void queryFileStatus() {
        if(getFilePath() != null && AWPrefs.getP4Enabled())
            scm.queryFileStatus(getFilePath());
    }

    public boolean isFileWritable() {
        return scm.isFileWritable();
    }

    public void editFile() {
        if(check()) {
            showProgress("Open for Edit");
            scm.editFile(getFilePath());
        }
    }

    public void addFile() {
        if(check()) {
            showProgress("Mark for Add");
            scm.addFile(getFilePath());
        }
    }

    public void deleteFile() {
        if(check()) {
            showProgress("Mark for Delete");
            scm.deleteFile(getFilePath());
        }
    }

    public void revertFile() {
        if(check()) {
            if(XJAlert.displayAlertYESNO(editor.getWindowContainer(), "Revert", "Are you sure you want to discard your changes to the file ?") == XJAlert.YES) {
                showProgress("Revert");
                scm.revertFile(getFilePath());
            }
        }
    }

    public void submitFile() {
        if(check()) {
            P4SubmitDialog dialog = new P4SubmitDialog(editor.getWindowContainer());
            if(dialog.runModal() == P4SubmitDialog.BUTTON_OK) {
                showProgress("Submit");
                scm.submitFile(getFilePath(), dialog.getDescription(), dialog.getRemainOpen());
            }
        }
    }

    public void sync() {
        if(check()) {
            showProgress("Sync");
            scm.sync();
        }
    }

    protected boolean check() {
        if(getFilePath() == null) {
            XJAlert.display(editor.getWindowContainer(), "SCM Error", "The file must be saved to the disk before any SCM command can be executed.");
            return false;
        }
        return true;
    }

    protected void displayErrors() {
        if(scm.hasErrors()) {
            XJAlert.display(editor.getWindowContainer(), "SCM Error", scm.getErrorsDescription());
            scm.resetErrors();
        }
    }

    protected void showProgress(String title) {
        progress.setInfo(title);
        progress.setCancellable(false);
        progress.setIndeterminate(true);
        progress.display();
        setSilent(false);
    }

    protected void hideProgress() {
        progress.close();
    }

    protected String getFilePath() {
        return editor.getFilePath();
    }

    public void scmCommandsDidComplete() {
        if(!silent) {
            hideProgress();
            displayErrors();
        }
        editor.scmCommandsDidComplete();
    }

    public void scmFileStatusDidChange(String status) {
        editor.updateSCMStatus(status);
    }

    public void scmLog(String log) {
        editor.console.println(log);
    }

}
