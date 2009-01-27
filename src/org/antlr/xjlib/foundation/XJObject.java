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

package org.antlr.xjlib.foundation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XJObject {

    private List<Object> observers = new ArrayList<Object>();
    private Map<Object, List<Object>> keyObservers = new HashMap<Object, List<Object>>();

    public void awake() {

    }

    public void addObserver(Object o) {
        observers.add(o);
    }

    public void removeObserver(Object o) {
        observers.remove(o);
    }

    public void addObserverForKey(Object o, String key) {
        List<Object> list = keyObservers.get(key);
        if(list == null) {
            list = new ArrayList<Object>();
            keyObservers.put(key, list);
        }
        list.add(o);
    }

    public void removeObserverForKey(Object o, String key) {
        List list = keyObservers.get(key);
        if(list == null)
            return;

        list.remove(o);
    }

    public void bindObserverForKey(XJObject o, String key) {
        addObserverForKey(o, key);
        o.addObserverForKey(this, key);
    }

    public void keyValueChanged(Object sender, String key, Object value) {
        keyValueChangedToObservers(observers, sender, key, value);
        keyValueChangedToObservers(keyObservers.get(key), sender, key, value);
    }

    private void keyValueChangedToObservers(List observers, Object sender, String key, Object value) {
        if(observers == null)
            return;

        for (Object observer : observers) {
            if (observer == sender)
                continue;

            try {
                XJObject object = (XJObject) observer;
                object.observeValueForKey(sender, key, value);
            } catch (Exception e) {
                try {
                    Method m = observer.getClass().getMethod("observeValueForKey", Object.class, String.class, Object.class);
                    m.invoke(observer, sender, key, value);
                } catch (Exception e2) {
                    // ignore
                }
            }
        }
    }

    public void observeValueForKey(Object sender, String key, Object value) {

    }
}
