package org.antlr.works.plugin.intellij;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.antlr.works.plugin.PluginContainer;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeListener;

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

public class AWEditor implements FileEditor {

    protected PluginContainer container;
    protected Project project;
    protected VirtualFile file;

    protected JSplitPane vertical;
    protected Timer synchronizeTimer;

    public AWEditor(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;


        container = new PluginContainer();
        container.load(file.getPath());
        assemble();
        startTimer();
    }

    public void assemble() {
        vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        vertical.setTopComponent(container.getEditorComponent());
        vertical.setBottomComponent(container.getTabbedComponent());
        vertical.setBorder(null);
        vertical.setContinuousLayout(true);
        vertical.setOneTouchExpandable(true);

        JPanel upperPanel = new JPanel(new BorderLayout());
        upperPanel.add(container.getMenubarComponent(), BorderLayout.NORTH);
        upperPanel.add(container.getToolbarComponent(), BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(upperPanel, BorderLayout.NORTH);
        panel.add(vertical, BorderLayout.CENTER);
        panel.add(container.getStatusComponent(), BorderLayout.SOUTH);

        container.getContentPane().add(panel);

        container.getContentPane().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                layout();
            }
        });

        FileDocumentManager.getInstance().addFileDocumentManagerListener(new FileDocumentManagerAdapter() {
            public void fileContentReloaded(VirtualFile file, Document document) {
                if(file.equals(AWEditor.this.file))
                    load(file.getPath());
            }

            public void fileContentLoaded(VirtualFile file, Document document) {
                if(file.equals(AWEditor.this.file))
                    load(file.getPath());
            }
        });
    }

    public void startTimer() {
        synchronizeTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(container.getDocument().isDirty()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            synchronizeDocuments();
                        }
                    });
                }
            }
        });
        synchronizeTimer.start();
    }

    public void stopTimer() {
        synchronizeTimer.stop();
    }

    public void load(String file) {
        container.load(file);
    }

    public void synchronizeDocuments() {
        CommandProcessor.getInstance().executeCommand(project, new SynchronizeDocumentsCommand(), null, null);
    }

    public class SynchronizeDocumentsCommand implements Runnable {
        public void run() {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run() {
                    Document doc = FileDocumentManager.getInstance().getDocument(AWEditor.this.file);
                    doc.replaceString(0, doc.getTextLength()-1, container.getText());
                }
            });
        }
    }

    public JComponent getComponent() {
        return container.getRootPane();
    }

    public JComponent getPreferredFocusedComponent() {
        return container.getEditorComponent();
    }

    @NonNls
    public String getName() {
        return file.getName();
    }

    public FileEditorState getState(FileEditorStateLevel level) {
        return new FileEditorState() {
            public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
                return false;
            }
        };
    }

    public void setState(FileEditorState state) {
    }

    public boolean isModified() {
        return container.getDocument().isDirty();
    }

    public boolean isValid() {
        return true;
    }

    public boolean layout = false;
    public void layout() {
        if(!layout) {
            vertical.setDividerLocation((int)(container.getContentPane().getHeight()*0.5));
            container.becomingVisibleForTheFirstTime();
            layout = true;
        }
    }

    public void selectNotify() {
    }

    public void deselectNotify() {
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    public StructureViewBuilder getStructureViewBuilder() {
        return new AWStructureViewBuilder(container);
    }

    public <T> T getUserData(Key<T> key) {
        return null;
    }

    public <T> void putUserData(Key<T> key, T value) {
    }

}
