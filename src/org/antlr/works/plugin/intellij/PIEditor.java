package org.antlr.works.plugin.intellij;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.antlr.works.components.grammar.CEditorGrammarDefaultDelegate;
import org.antlr.works.plugin.container.PluginContainer;
import org.antlr.works.plugin.container.PluginContainerDelegate;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
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

public class PIEditor implements FileEditor, PluginContainerDelegate {

    protected PluginContainer container;
    protected Project project;
    protected VirtualFile virtualFile;
    protected Document document;

    protected JSplitPane vertical;
    protected boolean layout = false;
    protected boolean synchronizedDoc = false;

    protected MyFileDocumentManagerAdapter fileDocumentAdapter;
    protected java.util.List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    protected static final List<PIEditor> editors = new ArrayList<PIEditor>();

    public PIEditor(Project project, VirtualFile file) {
        this.project = project;
        this.virtualFile = file;
        this.document = FileDocumentManager.getInstance().getDocument(this.virtualFile);

        container = new PluginContainer();
        container.load(VfsUtil.virtualToIoFile(virtualFile).getPath());
        container.setDelegate(this);

        assemble();
        register();
    }

    public void close() {
        save();
        unregister();
    }

    public static void saveAll() {
        synchronized(editors) {
            for(PIEditor e : editors) {
                e.save();
            }
        }
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

        container.setEditorGrammarDelegate(new CEditorGrammarDefaultDelegate(vertical));
        container.getContentPane().add(panel);
    }

    public void register() {
        container.getContentPane().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layout();
            }
        });

        fileDocumentAdapter = new MyFileDocumentManagerAdapter();
        FileDocumentManager.getInstance().addFileDocumentManagerListener(fileDocumentAdapter);
        synchronized(editors) {
            editors.add(this);
        }
    }

    public void unregister() {
        synchronized(editors) {
            editors.remove(this);
        }
        FileDocumentManager.getInstance().removeFileDocumentManagerListener(fileDocumentAdapter);
    }

    public void load(String file) {
        container.load(file);
    }

    public void save() {
        container.getDocument().performAutoSave();
        notifyFileModified();
    }

    /** Perform the layout only once, when the component resize for the first time.
     *
     */
    public void layout() {
        if(!layout) {
            vertical.setDividerLocation((int)(container.getContentPane().getHeight()*0.5));
            container.becomingVisibleForTheFirstTime();
            layout = true;
        }
    }

    public void pluginDocumentDidChange() {
        if(!document.isWritable()) {
            ReadonlyStatusHandler.OperationStatus op = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(virtualFile);
            if(!document.isWritable()) {
                System.err.println(op.getReadonlyFilesMessage());
                return;
            }
        }

        notifyFileModified();

        /*CommandProcessor.getInstance().runUndoTransparentAction( new Runnable() {
            public void run() {
                CommandProcessor.getInstance().executeCommand( project, new Runnable() {
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction( new Runnable() {
                            public void run() {
                                int offset = document.getTextLength();
                                document.insertString( offset, " " );
                                document.deleteString( offset, offset + 1 );
                            }
                        } );
                    }
                }, "", null, UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION );
            }
        } ); */
    }

    private void notifyFileModified() {
        for (PropertyChangeListener propertyChangeListener : listeners) {
            propertyChangeListener.propertyChange(new PropertyChangeEvent(this, FileEditor.PROP_MODIFIED, null, null));
        }
    }

    @NotNull
    public JComponent getComponent() {
        return container.getRootPane();
    }

    public JComponent getPreferredFocusedComponent() {
        return container.getEditorComponent();
    }

    @NotNull
    public String getName() {
        return virtualFile.getName();
    }

    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return new FileEditorState() {
            public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
                return false;
            }
        };
    }

    public void setState(@NotNull FileEditorState state) {
    }

    public boolean isModified() {
        return container.getDocument().isDirty();
    }

    public boolean isValid() {
        return true;
    }

    public void selectNotify() {
        container.activate();
    }

    public void deselectNotify() {
        container.deactivate();
        save();
    }

    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    public StructureViewBuilder getStructureViewBuilder() {
        return new PIStructureViewBuilder(container);
    }

    public <T> T getUserData(Key<T> tKey) {
        return null;
    }

    public <T> void putUserData(Key<T> tKey, T t) {

    }

    public void dispose() {

    }

    private class MyFileDocumentManagerAdapter extends FileDocumentManagerAdapter {

        @Override
        public void fileContentReloaded(VirtualFile file, Document document) {
            if(file.equals(PIEditor.this.virtualFile)) {
                load(VfsUtil.virtualToIoFile(virtualFile).getPath());
            }
        }

        @Override
        public void fileContentLoaded(VirtualFile file, Document document) {
            if(file.equals(PIEditor.this.virtualFile)) {
                load(VfsUtil.virtualToIoFile(virtualFile).getPath());
            }
        }

        @Override
        public void beforeDocumentSaving(Document document) {
            Document doc = FileDocumentManager.getInstance().getDocument(PIEditor.this.virtualFile);
            if(doc == document) {
                save();
            }
        }

    }


}
