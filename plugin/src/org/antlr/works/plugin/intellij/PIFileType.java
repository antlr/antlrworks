package org.antlr.works.plugin.intellij;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.antlr.works.utils.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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

public class PIFileType implements FileType {

    public String getName() {
        return "ANTLR Grammar";
    }

    public String getDescription() {
        return "ANTLR Grammar File";
    }

    public String getDefaultExtension() {
        return "g";
    }

    public Icon getIcon() {
        return IconManager.shared().getIconApplication16x16();
    }

    public boolean isBinary() {
        return false;
    }

    public boolean isReadOnly() {
        return false;
    }

    // version 8 method
    public String getCharset(@NotNull VirtualFile virtualFile, byte[] bytes) {
        return null;
    }

    // version 7 method
    public String getCharset(VirtualFile file) {
        return null;
    }

    // version 7 method
    public SyntaxHighlighter getHighlighter(Project project, VirtualFile virtualFile) {
        return null;
    }

    public SyntaxHighlighter getHighlighter(Project project) {
        return null;
    }

    // version 7 method
    public StructureViewBuilder getStructureViewBuilder(VirtualFile file, Project project) {
        return null;
    }
}
