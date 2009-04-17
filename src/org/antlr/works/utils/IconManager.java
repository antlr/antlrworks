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

package org.antlr.works.utils;

import org.antlr.xjlib.appkit.swing.XJIconManager;

import javax.swing.*;

public class IconManager {

    private static final String path = "icons/";

    private static IconManager shared = new IconManager();

    public static IconManager shared() {
        return shared;
    }

    public ImageIcon createImageIcon(String path) {
        return XJIconManager.shared().createImageIcon(path);
    }

    public ImageIcon getIconApplication() {
        return createImageIcon(path+"app.png");
    }

    public ImageIcon getIconApplication32x32() {
        return createImageIcon(path+"app_32x32.png");
    }

    public ImageIcon getIconApplication64x64() {
        return createImageIcon(path+"app_64x64.png");
    }

    public ImageIcon getIconApplication16x16() {
        return createImageIcon(path+"app_16x16.png");
    }

    public ImageIcon getIconWarning() {
        return createImageIcon(path+"warning.png");
    }

    public ImageIcon getIconColoring() {
        return createImageIcon(path+"coloring.png");
    }

    public ImageIcon getIconSort() {
        return createImageIcon(path+"sort.png");
    }

    public ImageIcon getIconSyntaxDiagram() {
        return createImageIcon(path+"sd.png");
    }

    public ImageIcon getIconGraph() {
        return createImageIcon(path+"graph.png");
    }

    public ImageIcon getIconListTree() {
        return createImageIcon(path+"listtree.png");
    }

    public ImageIcon getIconIdea() {
        return createImageIcon(path+"warning.png");
    }

    public ImageIcon getIconFind() {
        return createImageIcon(path+"find.png");
    }

    public ImageIcon getIconBackward() {
        return createImageIcon(path+"back.png");
    }

    public ImageIcon getIconForward() {
        return createImageIcon(path+"forward.png");
    }

    public ImageIcon getIconDebug() {
        return createImageIcon(path+"debug.png");
    }

    public ImageIcon getIconDebugAgain() {
        return createImageIcon(path+"debug_again.png");
    }

    public ImageIcon getIconShowLinks() {
        return createImageIcon(path+"show_links.png");
    }

    public ImageIcon getIconRun() {
        return createImageIcon(path+"run.png");
    }

    public ImageIcon getIconAttach() {
        return createImageIcon(path+"attach.png");
    }

    public ImageIcon getIconDetach() {
        return createImageIcon(path+"detach.png");
    }

    public ImageIcon getIconExpandAll() {
        return createImageIcon(path+"expandall.png");
    }

    public ImageIcon getIconCollapseAll() {
        return createImageIcon(path+"collapseall.png");
    }

    public ImageIcon getIconCollapse() {
        return createImageIcon(path+"collapse.png");
    }

    public ImageIcon getIconCollapseDown() {
        return createImageIcon(path+"collapsedown.png");
    }

    public ImageIcon getIconCollapseUp() {
        return createImageIcon(path+"collapseup.png");
    }

    public ImageIcon getIconExpand() {
        return createImageIcon(path+"expand.png");
    }

    public ImageIcon getIconDelimiter() {
        return createImageIcon(path+"delimiter.png");
    }

    public ImageIcon getIconDelimiterUp() {
        return createImageIcon(path+"delimiterup.png");
    }

    public ImageIcon getIconDelimiterDown() {
        return createImageIcon(path+"delimiterdown.png");
    }

    public ImageIcon getIconStop() {
        return createImageIcon(path+"stop.png");
    }

    public ImageIcon getIconStepForward() {
        return createImageIcon(path+"stepforward.png");
    }

    public ImageIcon getIconStepBackward() {
        return createImageIcon(path+"stepbackward.png");
    }

    public ImageIcon getIconStepOver() {
        return createImageIcon(path+"stepover.png");
    }

    public ImageIcon getIconGoToStart() {
        return createImageIcon(path+"gotostart.png");
    }

    public ImageIcon getIconGoToEnd() {
        return createImageIcon(path+"gotoend.png");
    }

    public ImageIcon getIconFastForward() {
        return createImageIcon(path+"fastforward.png");
    }

    public ImageIcon getIconParser() {
        return createImageIcon(path+"parser.png");
    }

    public ImageIcon getIconLexer() {
        return createImageIcon(path+"lexer.png");
    }

    public ImageIcon getIconBreakpoint() {
        return createImageIcon(path+"breakpoint.png");
    }

    public ImageIcon getIconOverride() {
        return createImageIcon(path+"override.png");
    }

    public ImageIcon getIconOverridden() {
        return createImageIcon(path+"overridden.png");
    }

}
