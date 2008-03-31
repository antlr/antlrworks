package org.antlr.works.ate.folding;

import org.antlr.works.ate.ATEPanel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public abstract class ATEFoldingManager {

    protected ATEPanel textEditor;
    protected Set<Integer> usedEntityLines = new HashSet<Integer>();
    protected List<ATEFoldingEntity> entities = new ArrayList<ATEFoldingEntity>();

    public ATEFoldingManager(ATEPanel textEditor) {
        this.textEditor = textEditor;
    }

    public void close() {      
        textEditor = null;
    }

    public void textPaneWillFold() {
        
    }

    public void textPaneDidFold() {
        textEditor.refresh();
    }

    public abstract ATEFoldingEntityProxy createEntityProxy(ATEFoldingEntity entity);
    public abstract ATEFoldingEntity getEntityForKey(Object key, int tag);

    public void addEntity(ATEFoldingEntity entity) {
        Integer start = entity.foldingEntityGetStartLine();
        Integer end = entity.foldingEntityGetEndLine();
        if(usedEntityLines.contains(start) || usedEntityLines.contains(end))
            return;

        usedEntityLines.add(start);
        usedEntityLines.add(end);

        entities.add(entity);
    }

    public List<ATEFoldingEntity> getFoldingEntities() {
        usedEntityLines.clear();
        entities.clear();
        provideFoldingEntities();
        return entities;
    }

    public abstract void provideFoldingEntities();
    
    public void toggleFolding(ATEFoldingEntity entity) {
        //textEditor.getTextPane().toggleFolding(createEntityProxy(entity));
    }

}
