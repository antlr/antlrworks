package org.antlr.works.editor.tips;

import java.awt.*;
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

public class TipsManager {

    protected List<TipsProvider> providers;
    protected TipsOverlay overlay;
    protected int lastPosition;
    protected boolean enabled;

    public TipsManager() {
        providers = new ArrayList<TipsProvider>();
        enabled = true;
    }

    public void setOverlay(TipsOverlay overlay) {
        this.overlay = overlay;
    }

    public void addProvider(TipsProvider provider) {
        providers.add(provider);
    }

    public void setEnabled(boolean flag) {
        this.enabled = flag;
        if(!enabled)
            hide();
    }

    public boolean enabled() {
        return enabled;
    }

    public void displayAnyTipsAvailable(int position, Point location) {
        if(!enabled)
            return;

        if(location == null) {
            hide();
            return;
        }
        
        if(position == lastPosition)
            return;

        lastPosition = position;

        List<String> tips = generateTips(position);
        if(tips == null || tips.isEmpty()) {
            hide();
        } else {
            overlay.setTips(tips);
            overlay.setLocation(location);
            overlay.display();
        }
    }

    public void hide() {
        overlay.hide();
        lastPosition = -1;
    }

    public List<String> generateTips(int position) {
        List<String> tips = new ArrayList<String>();
        for (TipsProvider provider : providers) {
            List<String> ptips = provider.tipsProviderGetTips(position);
            if (ptips != null && !ptips.isEmpty()) {
                tips.addAll(ptips);
            }
        }
        return tips;
    }

    public void close() {
        overlay.close();
    }
}
