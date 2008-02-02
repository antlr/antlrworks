package org.antlr.works.plugin.intellij;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.SettingsSavingComponent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
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

public class PIEditorProvider implements ApplicationComponent, FileEditorProvider, SettingsSavingComponent {

    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        String ext = file.getExtension();
        return ext != null && ext.equals("g");
    }

    @NotNull
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new PIEditor(project, file);
    }

    public void disposeEditor(@NotNull FileEditor editor) {
        ((PIEditor)editor).close();
    }

    @NotNull
    public FileEditorState readState(@NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile file) {
        return new FileEditorState() {
            public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
                return false;
            }
        };
    }

    public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
    }

    @NotNull
    public String getEditorTypeId() {
        return "grammar";
    }

    @NotNull
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

    @NotNull
    public String getComponentName() {
        return "GrammarEditor";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    public void save() {
        PIEditor.saveAll();
    }
}
