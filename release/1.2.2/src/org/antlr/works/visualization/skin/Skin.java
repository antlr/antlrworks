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

package org.antlr.works.visualization.skin;

import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.GObject;
import org.antlr.works.visualization.graphics.shape.GLink;
import org.antlr.works.visualization.graphics.shape.GNode;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Skin {

    protected float value_epsilon_width = 0;
    protected float value_epsilon_up = 0;
    protected float value_epsilon_down = 0;

    protected float value_box_width = 0;
    protected float value_box_up = 0;
    protected float value_box_down = 0;

    protected float value_node_width = 0;
    protected float value_node_up = 0;
    protected float value_node_down = 0;

    protected float value_char_width = 0;
    protected float value_line_space = 0;

    protected Map<String,Float> values = null;

    public Skin() {
    }

    public Map<String,Float> getValuesMap() {
        if(values == null) {
            values = new HashMap<String, Float>();

            values.put(GContext.EPSILON_WIDTH, value_epsilon_width);
            values.put(GContext.EPSILON_UP, value_epsilon_up);
            values.put(GContext.EPSILON_DOWN, value_epsilon_down);

            values.put(GContext.BOX_WIDTH, value_box_width);
            values.put(GContext.BOX_UP, value_box_up);
            values.put(GContext.BOX_DOWN, value_box_down);

            values.put(GContext.NODE_WIDTH, value_node_width);
            values.put(GContext.NODE_UP, value_node_up);
            values.put(GContext.NODE_DOWN, value_node_down);

            values.put(GContext.CHAR_WIDTH, value_char_width);
            values.put(GContext.LINE_SPACE, value_line_space);
        }
        return values;
    }

    public void resetValues() {
        values = null;
    }
    
    public void setValueLineSpace(float value) {
        value_line_space = value;
        resetValues();
    }

    public float getValueLineSpace() {
        return value_line_space;
    }

    public void setValueEpsilonWidth(float value) {
        value_epsilon_width = value;
        resetValues();
    }

    public float getValueEpsilonWidth() {
        return value_epsilon_width;
    }

    public float getStartOffset(GContext context) { return 0; }
    public float getEndOffset(GContext context) { return 0; }

    public abstract void drawLink(GLink link);
    public abstract void drawNode(GNode node);

    public abstract boolean isLinkVisible();
    public abstract boolean isNodeVisible();

    public boolean objectContainsPoint(GObject object, Point point) { return false; }
}
