package org.antlr.works.visualization.serializable;

import java.util.*;/*

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

public class SXMLEncoder implements SEncoder {

    private final Map<SSerializable, Entry> cache = new HashMap<SSerializable, Entry>();
    private final Stack<Entry> stack = new Stack<Entry>();
    private long uid;
    private Entry root;

    public SXMLEncoder() {
    }

    public String toString() {
        uid = 0;
        XMLWriter writer = new XMLWriter();
        root.toXML(writer);
        return writer.toString();
    }

    public void write(SSerializable object) {
        Entry entry = cache.get(object);
        if(entry == null) {
            cache.put(object, entry = new Entry(object, uid++));

            if(root == null) {
                root = entry;
                stack.push(root);
            } else {
                stack.peek().write(entry);                
            }

            // serialize this object
            stack.push(entry);
            object.encode(this);
            stack.pop();
        } else {
            // already being serialized
            stack.peek().write(entry.getUID());
        }
    }

    public void write(String value) {
        stack.peek().write(value);
    }

    public void write(int value) {
        stack.peek().write(value);
    }

    public void write(boolean value) {
        stack.peek().write(value);
    }

    public static class Entry {

        final List<Object> stream = new ArrayList<Object>();
        final Object object;
        final long uid;

        public Entry(Object object, long uuid) {
            this.object = object;
            this.uid = uuid;
        }

        public void write(Entry entry) {
            stream.add(entry);
        }

        public void write(String value) {
            stream.add(value);
        }

        public void write(int value) {
            stream.add(value);
        }

        public void write(long value) {
            stream.add(value);
        }

        public void write(boolean value) {
            stream.add(value);
        }

        public void toXML(XMLWriter writer) {
            writer.open(object.getClass().getSimpleName());
            for(Object e : stream) {
                if(e instanceof Entry) {
                    ((Entry)e).toXML(writer);
                } else if(e != null) {
                    String name = e.getClass().getSimpleName();
                    if(e instanceof Integer) {
                        name = "int";
                    }
                    if(e instanceof Long) {
                        name = "long";
                    }
                    if(e instanceof Boolean) {
                        name = "bool";
                    }
                    if(e instanceof String) {
                        name = "str";
                    }

                    writer.open(name);
                    writer.write(e.toString());
                    writer.close();
                }
            }
            writer.close();
        }

        public long getUID() {
            return uid;
        }
    }

    public static class XMLWriter {

        StringBuilder sb = new StringBuilder();
        Stack<String> elements = new Stack<String>();

        public void open(String name) {
            elements.push(name);
            sb.append("<");
            sb.append(name);
            sb.append(">");
        }

        public void close() {
            String name = elements.pop();
            sb.append("</");
            sb.append(name);
            sb.append(">");
        }

        public void write(String value) {
            sb.append(value);
        }

        public String toString() {
            return sb.toString();
        }
    }
}
