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


package org.antlr.works.navigation;

import java.util.ArrayList;
import java.util.List;

public class GoToHistory {

    protected List history = new ArrayList();
    protected int currentIndex = 0;

    public GoToHistory() {

    }

    public void addPosition(int pos) {
        // Erase the remaining portion of the history
        // when adding a new position (forward
        // doesn't make sense anymore)
        int index = history.size()-1;
        while(index >= currentIndex) {
            history.remove(index);
            index--;
        }
        history.add(new Integer(pos));
        currentIndex = history.size();
    }

    public boolean canGoBack() {
        return !history.isEmpty() && currentIndex > 0;
    }

    public boolean canGoForward() {
        return !history.isEmpty() && currentIndex < history.size()-1;
    }

    public int getBackPosition(int currentPosition) {
        if(currentIndex == history.size()) {
            // Add the current position if the currentIndex
            // is at the end of the history (so Forward is able
            // to come back)
            history.add(new Integer(currentPosition));
        }

        currentIndex--;
        if(currentIndex < 0)
            currentIndex = 0;
        return ((Integer)history.get(currentIndex)).intValue();
    }

    public int getForwardPosition() {
        currentIndex++;
        if(currentIndex >= history.size())
            currentIndex = history.size()-1;
        return ((Integer)history.get(currentIndex)).intValue();
    }
}
