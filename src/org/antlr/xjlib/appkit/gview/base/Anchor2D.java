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

package org.antlr.xjlib.appkit.gview.base;

import org.antlr.xjlib.foundation.XJXMLSerializable;

public class Anchor2D implements XJXMLSerializable {

    public static final Vector2D DIRECTION_FREE = new Vector2D(0, 0);
    public static final Vector2D DIRECTION_BOTTOM = new Vector2D(0, 1);
    public static final Vector2D DIRECTION_TOP = new Vector2D(0, -1);
    public static final Vector2D DIRECTION_LEFT = new Vector2D(-1, 0);
    public static final Vector2D DIRECTION_RIGHT = new Vector2D(1, 0);

    public Vector2D position = null;
    public Vector2D direction = null;

    public Anchor2D() {

    }
    
    public Anchor2D(Vector2D position, Vector2D direction) {
        setPosition(position);
        setDirection(direction);
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public Vector2D getPosition() {
        return position;
    }

    public void setDirection(Vector2D direction) {
        this.direction = direction;
    }

    public Vector2D getDirection() {
        return direction;
    }

    public boolean equals(Object otherObject) {
        if(otherObject == null) {
            return false;
        }

        if(otherObject instanceof Anchor2D) {
            Anchor2D otherAnchor = (Anchor2D) otherObject;
            return position.equals(otherAnchor.position) && direction.equals(otherAnchor.direction);
        } else {
            return false;
        }
    }
}
