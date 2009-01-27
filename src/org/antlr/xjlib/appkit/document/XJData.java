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

package org.antlr.xjlib.appkit.document;

import org.antlr.xjlib.foundation.XJObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class XJData extends XJObject {

    static final long serialVersionUID = 10275539472847495L;

    private static final int VERSION = 1;
    protected Map<String,Object> dictionary = new HashMap<String, Object>();
    protected String file;

    public XJData() {

    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    public void setDataForKey(Object sender, String key, Object value) {
        dictionary.put(key, value);
        keyValueChanged(sender, key, value);
    }

    public Object getDataForKey(String key) {
        return dictionary.get(key);
    }

    public void dataChanged() {
        for (String s : dictionary.keySet()) {
            keyValueChanged(this, s, dictionary.get(s));
        }
    }

    public void observeValueForKey(Object sender, String key, Object value) {
        dictionary.put(key, value);
        keyValueChanged(sender, key, value);
    }

    // *** Externalizable

    public static final int DATA_INPUTSTREAM = 1;
    public static final int DATA_OBJECTINPUTSTREAM = 2;
    public static final int DATA_PLAINTEXT = 3;
    public static final int DATA_XML = 4;

    public int dataType() {
        return DATA_OBJECTINPUTSTREAM;
    }

    public void readData() throws IOException {
    }

    public void writeData() throws IOException {
    }

    public void readData(InputStream is) throws IOException {
    }

    public void writeData(OutputStream os) throws IOException {
    }

    public void readData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.readInt();
        dictionary = (HashMap<String,Object>)ois.readObject();
        dataChanged();
    }

    public void writeData(ObjectOutputStream oos) throws IOException {
        oos.writeInt(VERSION);
        oos.writeObject(dictionary);
    }

}
