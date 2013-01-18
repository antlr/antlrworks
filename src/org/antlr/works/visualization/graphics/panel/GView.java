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

package org.antlr.works.visualization.graphics.panel;

import org.antlr.works.ate.ATEUtilities;
import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.graph.GGraphAbstract;
import org.antlr.works.visualization.graphics.graph.GGraphGroup;
import org.antlr.works.visualization.graphics.path.GPath;
import org.antlr.works.visualization.graphics.path.GPathGroup;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuItem;
import org.antlr.xjlib.appkit.menu.XJMenuItemDelegate;
import org.antlr.xjlib.appkit.utils.XJSmoothScrolling;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GView extends JPanel implements XJMenuItemDelegate {

    protected boolean useCachedImage = true;
    protected boolean cachedImageRerender = false;
    protected boolean cachedImageResize = false;

    protected String placeholder;
    protected BufferedImage cachedImage = null;

    protected List graphs = new ArrayList();
    protected int currentGraphIndex = 0;
    protected GPanel panel;
    protected GContext context;
    protected XJSmoothScrolling smoothScrolling;
                              
    protected Point lastMouse;

    protected static final Font DEFAULT_FONT = new Font("Courier", Font.BOLD, 18);

    public int offset_x = 10;
    public int offset_y = 10;

    public int prev_offset_x = 0;
    public int prev_offset_y = 0;

    public GView(GPanel panel, GContext context) {
        this.panel = panel;
        this.context = context;
        this.context.setContainer(this);

        smoothScrolling = new XJSmoothScrolling(this, null);

        setFocusable(true);

        setBackground(Color.white);
        adjustSize();

        addMouseMotionListener(new DefaultMouseMotionListener());
        addMouseListener(new DefaultMouseListener());
        addKeyListener(new DefaultKeyListener());
    }

    public void setEnable(boolean flag) {
        for (Object graph1 : graphs) {
            GGraphAbstract graph = (GGraphAbstract) graph1;
            graph.setEnable(flag);
        }
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @SuppressWarnings("unchecked")
    public void setGraphs(List graphs) {
        this.graphs.clear();

        if(graphs == null)
            return;

        this.graphs.addAll(graphs);

        if(currentGraphIndex >= graphs.size())
            currentGraphIndex = graphs.size()-1;

        applyContext();
    }

    public List getGraphs() {
        return graphs;
    }

    public void applyContext() {
        for (Object graph1 : graphs) {
            GGraphAbstract graph = (GGraphAbstract) graph1;
            graph.setContext(context);
        }
    }

    public void setCacheEnabled(boolean flag) {
        if(useCachedImage != flag) {
            useCachedImage = flag;
            cacheInvalidate();
        }
    }

    public boolean isCachedEnabled() {
        return useCachedImage;
    }

    public void cacheInvalidate() {
        cachedImage = null;
    }

    public void cacheRerender() {
        cachedImageRerender = true;
    }

    public void setCacheResizeImage(boolean flag) {
        cachedImageResize = flag;
    }

    public BufferedImage getImage() {
        if(getCurrentGraph() == null) return null;
        
        BufferedImage image = new BufferedImage(getPaintWidth(), getPaintHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, getPaintWidth(), getPaintHeight());
        render(g);
        g.dispose();
        return image;
    }

    public boolean setNextGraph() {
        currentGraphIndex++;
        if(currentGraphIndex>=graphs.size()) {
            currentGraphIndex = graphs.size()-1;
            return false;
        } else
            return true;
    }

    public boolean setPrevGraph() {
        currentGraphIndex--;
        if(currentGraphIndex<0) {
            currentGraphIndex = 0;
            return false;
        } else
            return true;
    }

    public int getCurrentGraphIndex() {
        return currentGraphIndex;
    }

    public GGraphAbstract getCurrentGraph() {
        if(graphs.size()>0)
            return (GGraphAbstract)graphs.get(currentGraphIndex);
        else
            return null;
    }

    public GGraphGroup getCurrentGraphGroup() {
        return (GGraphGroup)getCurrentGraph();
    }

    public GPathGroup getCurrentPathGroup() {
        return getCurrentGraphGroup().getPathGroup();
    }

    public GPath getCurrentPath() {
        return getCurrentPathGroup().getCurrentPath();
    }

    public void refresh() {
        if(getCurrentGraph() != null)
            getCurrentGraph().render(0, 0);

        cacheInvalidate();
        adjustSize();
        repaint();
    }

    public void refreshSizeChanged(boolean useCacheImageResize) {
        if(useCachedImage) {
            setCacheResizeImage(useCacheImageResize);
            if(!useCacheImageResize) {
                if(getCurrentGraph() != null)
                    getCurrentGraph().render(0, 0);
                cacheInvalidate();
            }
            adjustSize();
            repaint();
        } else {
            refresh();
        }
    }

    public void adjustSize() {
        if(getCurrentGraph() == null || context == null)
            return;

        Dimension dimension = new Dimension(getGraphWidth()+2*offset_x, getGraphHeight()+2*offset_y);
        setPreferredSize(dimension);
        revalidate();
    }

    public int getGraphWidth() {
        if(getCurrentGraph().getDimension() == null)
            return 400;
        else
            return (int)getCurrentGraph().getWidth()+20;
    }

    public int getGraphHeight() {
        if(getCurrentGraph().getDimension() == null)
            return 200;
        else
            return (int)getCurrentGraph().getHeight()+20;
    }

    public void addMenuItem(JPopupMenu menu, String title, int tag, Object object) {
        XJMenuItem item = new XJMenuItem();
        item.setTitle(title);
        item.setTag(tag);
        item.setObject(object);
        item.setDelegate(this);

        menu.add(item.getSwingComponent());
    }

    public JPopupMenu getContextualMenu() {
        return null;
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
    }

    public void processMouseEvent(MouseEvent e) {
        if(e.isPopupTrigger()) {
            JPopupMenu menu = getContextualMenu();
            if(menu != null)
                menu.show(this, e.getX(), e.getY());
        } else
            super.processMouseEvent(e);
    }

    public void pathCurrentElementDidChange() {
        GPath path = getCurrentPath();
        Rectangle rect = path.getBoundsOfSelectedElement();
        if(!rect.isEmpty()) {
            // Expand the rectangle a little bit so the rectangle is "more" visible
            rect.x -= 50;
            rect.y -= 50;
            rect.width += 100;
            rect.height += 100;
            smoothScrolling.scrollTo(rect);
        }
    }

    public boolean canDraw() {
        return getCurrentGraph() != null && getCurrentGraph().getDimension() != null && getCurrentGraph().isRendered();
    }

    public void render(Graphics2D g2d) {
        context.offsetX = offset_x;
        context.offsetY = offset_y;
        context.setGraphics2D(g2d);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        getCurrentGraph().draw();
    }

    public int getPaintWidth() {
        return getGraphWidth()+offset_x;
    }

    public int getPaintHeight() {
        return getGraphHeight()+offset_y+1;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        ATEUtilities.prepareForText(g);
        if(!canDraw()) {
            paintPlaceholder(g);
            return;
        }

        int width = getPaintWidth();
        int height = getPaintHeight();

        if(useCachedImage) {
            boolean sizeChanged = cachedImage != null && (cachedImage.getWidth() != width || cachedImage.getHeight() != height);

            if(sizeChanged) {
                // Discard the cache image only if it already exists and if the cachedImageResize flag is false.
                // The cachedImageResize flag indicates, if true, that we should use the cachedImage
                // instead of re-creating a new one (useful for fast live resize).
                if(!cachedImageResize && cachedImage != null) {
                    cachedImage.flush();
                    cachedImage = null;
                }
            }

            if(cachedImage == null) {
                // Create a new cache image.
                cachedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D gCache = (Graphics2D)cachedImage.getGraphics();
                ATEUtilities.prepareForText(gCache);
                gCache.setColor(Color.white);
                gCache.fillRect(0, 0, width, height);
                render(gCache);
                gCache.dispose();
            } else if(cachedImageRerender) {
                // Only render the cachedImage without re-creating it again
                Graphics2D gCache = (Graphics2D)cachedImage.getGraphics();
                ATEUtilities.prepareForText(gCache);
                gCache.setColor(Color.white);
                gCache.fillRect(0, 0, width, height);
                render(gCache);
                gCache.dispose();
                cachedImageRerender = false;
            }
        }

        if(cachedImage == null)
            render((Graphics2D)g);
        else
            g.drawImage(cachedImage, 0, 0, width, height, null);

        if(!cachedImageResize && getCurrentGraph() instanceof GGraphGroup) {
            // Draw the selected segment of a path (and only if we are not resizing using only the cached image)
            Graphics2D g2d = (Graphics2D)g;
            context.offsetX = offset_x;
            context.offsetY = offset_y;
            context.setGraphics2D(g2d);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            getCurrentPathGroup().drawSelectedElement();
        }
    }

    public void paintPlaceholder(Graphics g) {
        if(placeholder == null)
            return;

        g.setFont(DEFAULT_FONT);

        FontMetrics fm = g.getFontMetrics();
        Rectangle r = getVisibleRect();
        int x = r.x+r.width/2-fm.stringWidth(placeholder)/2;
        int y = r.y+r.height/2+fm.getHeight()/2;

        g.setColor(Color.gray);
        g.drawString(placeholder, x, y);
    }

    public class DefaultMouseMotionListener extends MouseMotionAdapter {

        public void mouseDragged(MouseEvent e) {
            if(lastMouse == null)
                return;

            Point mouse = e.getPoint();
            int dx = mouse.x - lastMouse.x;
            int dy = mouse.y - lastMouse.y;
            offset_x = prev_offset_x+dx;
            offset_y = prev_offset_y+dy;
            refresh();
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    public class DefaultMouseListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            if(!isFocusOwner())
                requestFocusInWindow();

            setCacheEnabled(false);
            prev_offset_x = offset_x;
            prev_offset_y = offset_y;
            lastMouse = e.getPoint();

            if(getCurrentGraph() instanceof GGraphGroup)
                handleMousePressedInGraphGroup(e);

            /** In the future, if someone wants to know which link
             * is under the mouse location, use the following code:
             *  GGraph g = (GGraph)getCurrentGraph();
                GLink link = g.findLinkAtPosition(e.getX(), e.getY());
                if(link != null) {
                    // Do something with link
                }
             */
        }

        public void mouseReleased(MouseEvent e) {
            lastMouse = null;
            setCacheEnabled(true);
            // FIX AW-76. Need to trigger a repaint so the cache is created again. Otherwise
            // it is null and will prevent exporting the image into the bitmap
            repaint();
        }

        public void handleMousePressedInGraphGroup(MouseEvent e) {
            getCurrentPathGroup().selectPath(e.getPoint());
        }
    }

    public class DefaultKeyListener extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
            if(getCurrentGraph() instanceof GGraphGroup)
                handleKeyPressedInGraphGroup(e);
        }

        public void handleKeyPressedInGraphGroup(KeyEvent e) {
            GPath path = getCurrentPath();
            switch(e.getKeyCode()) {
                case KeyEvent.VK_RIGHT:
                    path.nextElement();
                    pathCurrentElementDidChange();
                    e.consume();
                    break;
                case KeyEvent.VK_LEFT:
                    path.previousElement();
                    pathCurrentElementDidChange();
                    e.consume();
                    break;
                case KeyEvent.VK_UP:
                    path.lastElement();
                    pathCurrentElementDidChange();
                    e.consume();
                    break;
                case KeyEvent.VK_DOWN:
                    path.firstElement();
                    pathCurrentElementDidChange();
                    e.consume();
                    break;

                case KeyEvent.VK_A:
                    getCurrentPathGroup().toggleShowRuleLinks();
                    cacheRerender();
                    e.consume();
                    break;
            }

            if(e.isConsumed())
                repaint();
        }
    }
}
