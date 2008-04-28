package org.antlr.works.components.container;

import org.antlr.works.components.document.ComponentDocument;
import org.antlr.works.components.editor.ComponentEditor;
import org.antlr.works.components.editor.ComponentEditorGrammar;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.xjlib.appkit.frame.XJFrameInterface;
import org.antlr.xjlib.appkit.menu.XJMainMenuBar;

import javax.swing.*;
import java.awt.*;/*

[The "BSD licence"]
Copyright (c) 2005-07 Jean Bovet
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

public class ComponentContainerInternal implements ComponentContainer {

    private ComponentDocument document;
    private ComponentContainer mainContainer;
    private ComponentEditor editor;

    public ComponentContainerInternal(ComponentContainer mainContainer) {
        this.mainContainer = mainContainer;
    }

    public ComponentContainer getMainContainer() {
        return mainContainer;
    }

    public void setEditor(ComponentEditor editor) {
        this.editor = editor;
    }

    public ComponentEditor getEditor() {
        return editor;
    }

    public void setDocument(ComponentDocument document) {
        this.document = document;
    }

    public ComponentDocument getDocument() {
        return document;
    }

    public Dimension getSize() {
        return mainContainer.getSize();
    }

    public XJFrameInterface getXJFrame() {
        return mainContainer.getXJFrame();
    }

    public XJMainMenuBar getMainMenuBar() {
        return mainContainer.getMainMenuBar();
    }

    public void becomingVisibleForTheFirstTime() {
        getEditor().becomingVisibleForTheFirstTime();
    }

    public boolean close() {
        getEditor().close();
        return true;
    }

    public void setDirty() {
        mainContainer.setDirty();
    }

    public ContextualMenuFactory createContextualMenuFactory() {
        return mainContainer.createContextualMenuFactory();
    }

    public JPopupMenu getContextualMenu(int textIndex) {
        return mainContainer.getContextualMenu(textIndex);
    }

    public EditorTab getSelectedTab() {
        return mainContainer.getSelectedTab();
    }

    public void selectTab(Component c) {
        mainContainer.selectTab(c);
    }

    public void addTab(EditorTab tab) {
        mainContainer.addTab(tab);
    }

    public void selectConsoleTab(ComponentEditor editor) {
        mainContainer.selectConsoleTab(editor);
    }

    public void selectInterpreterTab(ComponentEditor editor) {
        mainContainer.selectInterpreterTab(editor);
    }

    public void selectSyntaxDiagramTab(ComponentEditor editor) {
        mainContainer.selectSyntaxDiagramTab(editor);
    }

    public ComponentEditorGrammar selectGrammar(String name) {
        return mainContainer.selectGrammar(name);
    }

    public void documentLoaded(ComponentDocument document) {
        System.out.println("Document "+document.getDocumentName()+" loaded");
    }

    public void editorParsed(ComponentEditor editor) {
        mainContainer.editorParsed(editor);
    }

    public void editorContentChanged() {
        mainContainer.editorContentChanged();
    }
}
