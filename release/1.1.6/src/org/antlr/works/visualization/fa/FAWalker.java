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

package org.antlr.works.visualization.fa;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** This class is used to "walk" through an NFA and is used, for example, by the DOTGenerator.
 * It sends "event" to its delegate for each state and transition found.
 */

public class FAWalker {

    private FAWalkerDelegate delegate;
    private int mode;
    private Set<FAState> visitedStates = new HashSet<FAState>();

    public FAWalker(FAWalkerDelegate delegate, int mode) {
        this.delegate = delegate;
        this.mode = mode;
    }

    public void walk(FAState state) {
        if(visitedStates.contains(state))
            return;
        visitedStates.add(state);
        
        delegate.walkerState(state, mode);
        Iterator<FATransition> iterator = state.transitions.iterator();
        while(iterator.hasNext()) {
            FATransition transition = iterator.next();
            delegate.walkerTransition(transition, mode);
            walk(transition.target);
        }
    }
}
