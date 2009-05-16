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


package org.antlr.works.editor.navigation;

import java.util.ArrayList;
import java.util.List;

public class GoToHistory {

    protected List<Integer> history = new ArrayList<Integer>();
    protected int currentIndex = 0;

    public GoToHistory() {

    }

    public void addPosition(int pos) {
        // Erase the remaining portion of the history
        // when adding a new position (forward
        // doesn't make sense anymore)
        int index = history.size()-1;
        while(index > currentIndex) {
            history.remove(index);
            index--;
        }

        if(history.isEmpty()) {
            history.add(pos);
        } else {
            // add only if the last position is not the same
            if(!history.get(history.size()-1).equals(pos)) {
                history.add(pos);
            }
        }
        currentIndex = history.size()-1;
    }

    public boolean canGoBack() {
        return !history.isEmpty() && currentIndex > 0;
    }

    public boolean canGoForward() {
        return !history.isEmpty() && currentIndex < history.size()-1;
    }

    public int getBackPosition(int currentPosition) {
        if(currentIndex == history.size()-1) {
            // Add the current position if the currentIndex
            // is at the end of the history (so Forward is able
            // to come back)
            addPosition(currentPosition);
        }

        currentIndex--;
        if(currentIndex < 0)
            currentIndex = 0;
        return history.get(currentIndex);
    }

    public int getForwardPosition() {
        currentIndex++;
        if(currentIndex > history.size()-1)
            currentIndex = history.size()-1;
        return history.get(currentIndex);
    }
}
