/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Sam Harwell
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.antlr.works.ate;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Sam Harwell
 */
public abstract class ATEUtilities {

    private static final boolean antialias = Boolean.getBoolean("swing.aatext");

    private static Map<?, ?> hintsMap;

    /**
     * Prepare a {@link Graphics} instance for rendering text. When {@code g} is
     * an instance of {@link Graphics2D}, this methods sets the
     * {@link RenderingHints} for text anti-aliasing to the default values
     * requested by the system, or passed to the application in the
     * {@code swing.aatext} property.
     *
     * @param g The {@link Graphics} instance
     */
    public static void prepareForText(Graphics g) {
        if (g instanceof Graphics2D) {
            ((Graphics2D)g).setRenderingHints(getRenderingHints());
        }
    }

    private static Map<?, ?> getRenderingHints() {
        if (hintsMap == null) {
            hintsMap = (Map)Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"); //NOI18N
            if (hintsMap == null) {
                if (antialias) {
                    hintsMap = Collections.singletonMap(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                } else {
                    hintsMap = Collections.emptyMap();
                }
            }
        }

        return hintsMap;
    }

    private ATEUtilities() {
    }
}
