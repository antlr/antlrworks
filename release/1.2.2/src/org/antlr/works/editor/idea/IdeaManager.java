package org.antlr.works.editor.idea;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
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

public class IdeaManager {

    protected List<IdeaProvider> providers = new ArrayList<IdeaProvider>();
    protected Timer timer;
    protected IdeaOverlay overlay;
    protected IdeaManagerDelegate delegate;
    protected boolean enabled = true;
    protected int lastPosition;

    public IdeaManager() {
        timer = new Timer(1000, new TimerActionListener());
        timer.setRepeats(false);
    }

    public void setDelegate(IdeaManagerDelegate delegate) {
        this.delegate = delegate;
    }

    public void setOverlay(IdeaOverlay overlay) {
        this.overlay = overlay;
    }

    public void setEnabled(boolean flag) {
        this.enabled = flag;
        if(!enabled) {
            hide();
        }
    }

    public boolean enabled() {
        return enabled;
    }

    public void addProvider(IdeaProvider provider) {
        providers.add(provider);
    }

    public void close() {
        overlay.close();
        timer.stop();
    }

    public void hide() {
        timer.stop();
        overlay.hide();
    }

    public void displayAnyIdeasAvailable(int position) {
        if(!enabled)
            return;

        List<IdeaAction> ideas = generateIdeaActions(position);
        if(ideas == null || ideas.isEmpty())
            overlay.hide();
        else {
            lastPosition = position;
            overlay.setIdeas(ideas);
            timer.restart();
        }
    }

    public List<IdeaAction> generateIdeaActions(int position) {
        List<IdeaAction> actions = new ArrayList<IdeaAction>();
        for (IdeaProvider provider : providers) {
            List<IdeaAction> pactions = provider.ideaProviderGetActions(position);
            if (pactions != null && !pactions.isEmpty()) {
                actions.addAll(pactions);
            }
        }
        return actions;
    }

    protected class TimerActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            /** Make sure there is still some ideas to display */
            List<IdeaAction> ideas = generateIdeaActions(lastPosition);
            if(ideas.size() == 0)
                return;

            /** Update the current list in case it has changed since the timer
             * has been fired
             */
            overlay.setIdeas(ideas);

            if(delegate != null) {
                if(!delegate.ideaManagerWillDisplayIdea())
                    return;
            }

            overlay.display();
        }
    }
}
