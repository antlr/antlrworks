package org.antlr.xjlib.appkit.utils;

import org.antlr.xjlib.appkit.gview.base.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class XJSmoothScrolling implements ActionListener {

    protected static final int steps = 10;

    protected JComponent c;
    protected Rectangle source;
    protected Rectangle dest;
    protected Vector2D v;
    protected int k;
    protected Timer timer;
    protected ScrollingDelegate delegate;

    public XJSmoothScrolling(JComponent c, ScrollingDelegate delegate) {
        this.c = c;
        this.delegate = delegate;
    }

    public synchronized void scrollTo(Rectangle dest) {
        this.dest = dest;
        computeSource();
        computeVector();
        if(c.getVisibleRect().intersects(dest)) {
            c.scrollRectToVisible(dest);
            completed();
        } else {
            startTimer();
        }
    }

    public synchronized void startTimer() {
        if(timer != null)
            timer.stop();

        timer = new Timer(30, this);
        timer.start();
        k = 0;
    }

    public void completed() {
        if(delegate != null)
            delegate.smoothScrollingDidComplete();
    }

    public void computeVector() {
        v = new Vector2D(dest.x-source.x, dest.y-source.y);
    }

    public void computeSource() {
        Rectangle vr = c.getVisibleRect();
        source = new Rectangle(dest);
        if(source.x < vr.x)
            source.x = vr.x;
        else if(source.x > vr.x+vr.width)
            source.x = vr.x+vr.width-source.width;

        if(source.y < vr.y)
            source.y = vr.y;
        else if(source.y > vr.y+vr.height)
            source.y = vr.y+vr.height-source.height;
    }

    public void actionPerformed(ActionEvent e) {
        k++;
        if(k>steps) {
            c.scrollRectToVisible(dest);
            timer.stop();
            timer = null;
            completed();
        } else {
            Rectangle current = new Rectangle(source);
            current.x += v.x*k/steps;
            current.y += v.y*k/steps;
            c.scrollRectToVisible(current);
        }
    }

    public interface ScrollingDelegate {
        public void smoothScrollingDidComplete();
    }
}
