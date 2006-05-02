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

package org.antlr.works.visualization.graphics.path;

import edu.usfca.xj.appkit.gview.timer.GTimer;
import edu.usfca.xj.appkit.gview.timer.GTimerDelegate;
import edu.usfca.xj.foundation.notification.XJNotificationCenter;
import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.GObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GPathGroup extends GObject implements GTimerDelegate {

    public static final String NOTIF_CURRENT_PATH_DID_CHANGE = "NOTIF_CURRENT_PATH_DID_CHANGE";
    public static int DEFAULT_PATH_WIDTH = 1;

    protected List graphicPaths = new ArrayList();
    protected int selectedPathIndex = 0;
    protected GTimer timer = new GTimer(this);

    protected boolean showRuleLinks = true;

    public GPathGroup() {
    }

    public void setEnable(boolean flag) {
        if(flag)
            timer.start();
        else
            timer.stop();
    }

    public void addPath(GPath path) {
        graphicPaths.add(path);
    }

    public List getPaths() {
        return graphicPaths;
    }

    public GPath getPath(int index) {
        return (GPath)graphicPaths.get(index);
    }

    public int getNumberOfPaths() {
        return graphicPaths.size();
    }

    public void setSelectedPath(int index) {
        if(index<0)
            selectedPathIndex = graphicPaths.size()-1;
        else if(index>=graphicPaths.size())
            selectedPathIndex = 0;
        else
            selectedPathIndex = index;

        updateShowRuleLinks();        
        XJNotificationCenter.defaultCenter().postNotification(this, NOTIF_CURRENT_PATH_DID_CHANGE);
    }

    public void setPathVisible(int index, boolean flag) {
        getPath(index).setVisible(flag);
    }

    public boolean isPathVisible(int index) {
        return getPath(index).isVisible();
    }

    public void makeSureCurrentPathIsVisible() {
        if(getCurrentPath().isVisible())
            return;

        for (Iterator iterator = graphicPaths.iterator(); iterator.hasNext();) {
            GPath path = (GPath) iterator.next();
            if(path.isVisible()) {
                setSelectedPath(graphicPaths.indexOf(path));
                break;
            }
        }
    }

    public void selectPreviousPath() {
        setSelectedPath(selectedPathIndex-1);
    }

    public void selectNextPath() {
        setSelectedPath(selectedPathIndex+1);
    }

    public GPath getCurrentPath() {
        if(graphicPaths.size() > 0)
            return (GPath)graphicPaths.get(selectedPathIndex);
        else
            return null;
    }

    public int getSelectedPathIndex() {
        return selectedPathIndex;
    }

    public void setContext(GContext context) {
        super.setContext(context);
        for (Iterator iterator = graphicPaths.iterator(); iterator.hasNext();) {
            GPath path = (GPath) iterator.next();
            path.setContext(context);
        }

        // Start timer here because no the context has been applied to all graphs
        // (otherwise, a node can be displayed without having a context associated)
        timer.start();
    }

    public void toggleShowRuleLinks() {
        showRuleLinks = !showRuleLinks;
        updateShowRuleLinks();
    }

    public void updateShowRuleLinks() {
        for (Iterator iterator = graphicPaths.iterator(); iterator.hasNext();) {
            GPath path = (GPath) iterator.next();
            path.setShowRuleLinks(showRuleLinks);
        }
    }

    public void selectPath(Point p) {
        List paths = getPathsAtPoint(p);
        if(paths.size() == 0)
            return;

        GPath selectPath = (GPath)paths.get(0);
        if(paths.size()>1) {
            int i = 1;
            while(!selectPath.isVisible() && i<paths.size()) {
                selectPath = (GPath)paths.get(i++);
            }
        }

        setSelectedPath(graphicPaths.indexOf(selectPath));
        context.repaint();
    }

    public List getPathsAtPoint(Point p) {
        List paths = new ArrayList();
        for (Iterator iterator = graphicPaths.iterator(); iterator.hasNext();) {
            GPath path = (GPath) iterator.next();
            if(path.containsPoint(p))
                paths.add(path);
        }
        return paths;
    }

    public void draw() {
        GPath currentPath = getCurrentPath();

        for (Iterator iterator = graphicPaths.iterator(); iterator.hasNext();) {
            GPath path = (GPath) iterator.next();
            if(path != currentPath) {
                path.deselectElement();
            } else {
                currentPath.selectElement();
            }

            if(path.isVisible() && path != currentPath)
                path.draw(DEFAULT_PATH_WIDTH, null);
        }

        if(currentPath.isVisible())
            currentPath.draw(DEFAULT_PATH_WIDTH, null);
    }

    public void drawSelectedElement() {
        if(getCurrentPath().isVisible() && getCurrentPath().isSelectable())
            getCurrentPath().drawSelectedElement();
    }

    public void timerFired(GTimer timer) {
        if(getCurrentPath() == null || context == null || !getCurrentPath().isSelectable())
            return;

        getCurrentPath().incrementWidth();
        context.repaint();
    }

}
