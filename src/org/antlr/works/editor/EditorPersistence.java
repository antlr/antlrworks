package org.antlr.works.editor;

import org.antlr.works.components.editor.ComponentEditorGrammar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class EditorPersistence {

    private static final String KEY_RULES = "rules";
    private static final String KEY_ACTIONS = "actions";

    private Map<String,Map<Object,EditorPersistentObject>> persistence = new HashMap<String, Map<Object,EditorPersistentObject>>();
    private boolean stored = false;
    private ComponentEditorGrammar editor;

    public EditorPersistence(ComponentEditorGrammar editor) {
        this.editor = editor;
    }

    public void close() {
        editor = null;
    }

    public void store() {
        if(stored)
            return;
        
        stored = true;
        store(editor.getSyntaxEngine().getSyntax().getRules(), KEY_RULES);
        store(editor.getSyntaxEngine().getSyntax().getActions(), KEY_ACTIONS);
    }

    public void restore() {
        if(stored) {
            stored = false;
            restore(editor.getSyntaxEngine().getSyntax().getRules(), KEY_RULES);
            restore(editor.getSyntaxEngine().getSyntax().getActions(), KEY_ACTIONS);
        }
    }
    
    public void store(List objects, String key) {
        Map<Object,EditorPersistentObject> m = persistence.get(key);
        if(m == null) {
            m = new HashMap<Object, EditorPersistentObject>();
            persistence.put(key, m);
        }

        m.clear();
        if(objects == null)
            return;

        for (Object object : objects) {
            EditorPersistentObject o = (EditorPersistentObject) object;
            m.put(o.getPersistentID(), o);
        }
    }

    public void restore(List objects, String key) {
        Map<Object,EditorPersistentObject> m = persistence.get(key);
        if(m == null)
            return;

        if(objects == null)
            return;

        for (Object object : objects) {
            EditorPersistentObject o = (EditorPersistentObject) object;
            EditorPersistentObject oldObject = m.get(o.getPersistentID());
            if (oldObject != null)
                o.persistentAssign(oldObject);
        }
    }

}
