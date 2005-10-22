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

package org.antlr.works.editor.actions;

import edu.usfca.xj.appkit.utils.XJAlert;
import edu.usfca.xj.appkit.utils.XJFileChooser;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.stats.Statistics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ActionsExport extends AbstractActions {

    public ActionsExport(EditorWindow editor) {
        super(editor);
    }

    public void exportEventsAsTextFile() {
        if(!XJFileChooser.shared().displaySaveDialog(editor.getWindowContainer(), "TXT", "Text file", false))
            return;

        String file = XJFileChooser.shared().getSelectedFilePath();
        if(file == null)
            return;

        StringBuffer text = new StringBuffer();
        List events = editor.debugger.getEvents();
        for(int i=0; i<events.size(); i++) {
            text.append(i + 1);
            text.append(": ");
            text.append(events.get(i).toString());
            text.append("\n");
        }

        try {
            FileWriter writer = new FileWriter(file);
            writer.write(text.toString());
            writer.close();
        } catch (IOException e) {
            XJAlert.display(editor.getWindowContainer(), "Error", "Cannot save text file: "+file+"\nError: "+e);
        }

        Statistics.shared().recordEvent(Statistics.EVENT_EXPORT_EVENTS_TEXT);
    }

}
