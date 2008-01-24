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

package org.antlr.works.visualization.skin.syntaxdiagram;

import org.antlr.works.visualization.graphics.GObject;
import org.antlr.works.visualization.graphics.shape.GLink;
import org.antlr.works.visualization.graphics.shape.GNode;
import org.antlr.works.visualization.skin.Skin;

import java.awt.*;

public class SDSkin extends Skin {

    public SDSkin() {
        value_epsilon_width = 5;
        value_epsilon_up = 0;
        value_epsilon_down = 0;

        value_box_width = 20;
        value_box_up = 3;
        value_box_down = 3;

        value_char_width = 3f;
        value_line_space = 4;
    }

    public boolean isLinkVisible() {
        return true;
    }

    public boolean isNodeVisible() {
        return false;
    }

    public boolean objectContainsPoint(GObject object, Point p) {
        if(object instanceof GLink)
            return SDLink.linkContainsPoint((GLink)object, p);
        return false;
    }

    public void drawNode(GNode node) {
    }

    public void drawLink(GLink link) {
        SDLink.draw(link);
    }
}
