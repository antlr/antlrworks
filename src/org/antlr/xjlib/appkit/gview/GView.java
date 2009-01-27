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

package org.antlr.xjlib.appkit.gview;

import org.antlr.xjlib.appkit.frame.XJView;
import org.antlr.xjlib.appkit.gview.base.Rect;
import org.antlr.xjlib.appkit.gview.base.Vector2D;
import org.antlr.xjlib.appkit.gview.event.*;
import org.antlr.xjlib.appkit.gview.object.GElement;
import org.antlr.xjlib.appkit.gview.object.GLink;
import org.antlr.xjlib.appkit.gview.timer.GTimer;
import org.antlr.xjlib.appkit.gview.timer.GTimerDelegate;
import org.antlr.xjlib.appkit.gview.utils.GAlphaVariator;
import org.antlr.xjlib.appkit.gview.utils.GMagnetic;
import org.antlr.xjlib.appkit.menu.XJMenu;
import org.antlr.xjlib.appkit.menu.XJMenuItem;
import org.antlr.xjlib.appkit.menu.XJMenuItemDelegate;
import org.antlr.xjlib.appkit.swing.XJGraphics2DPS;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.*;
import java.util.HashSet;
import java.util.Set;

public class GView extends XJView implements XJMenuItemDelegate, GTimerDelegate, GEventDelegate {

    public static final double DEFAULT_LINK_FLATENESS = 40;
    public static final int SCROLL_TO_VISIBLE_MARGIN = 10;

    protected GViewDelegate delegate = null;
    protected GEventManager eventManager = new GEventManager(this);

    protected GElement rootElement = null;

    protected Point lastMousePosition = null;
    protected boolean smoothGraphics = true;
    protected float zoom = 1;
    protected boolean autoAdjustSize = false;
    protected boolean drawBorder = true;
    protected int sizeMargin = 0;

    // ** Selected/focused elements

    protected GTimer selectionTimer = new GTimer(this);
    protected GTimer focusTimer = new GTimer(this);

    protected GAlphaVariator selectionAlphaVariator = new GAlphaVariator();
    protected GAlphaVariator focusAlphaVariator = new GAlphaVariator();

    // ** Magnetics layout

    protected Set<GMagnetic> magnetics = new HashSet<GMagnetic>();
    protected boolean magneticsVisible = false;

    public GView () {
        setFocusable(false);

        setBackground(Color.gray);
        setPreferredSize(new Dimension(1024, 600));

        addDefaultEventManager();
    }

    public void addDefaultEventManager() {
        // !!! ORDER OF THE FOLLOWING METHODS CALLS IS IMPORTANT !!!

        eventManager.add(new GEventDragElement(this));
        eventManager.add(new GEventDragRootElement(this));
        eventManager.add(new GEventDragSelection(this));
        eventManager.add(new GEventEditElement(this));
        eventManager.add(new GEventCreateLinkElement(this));
        eventManager.add(new GEventCreateElement(this));
        eventManager.add(new GEventFocusElement(this));
        eventManager.add(new GEventModifyLinkElement(this));
    }

    public int defaultLinkShape() {
        return GLink.SHAPE_ARC;
    }

    public void setRootElement(GElement element) {
        this.rootElement = element;
        if(rootElement != null) {
            this.rootElement.setPanel(this);
            autoAdjustSize();
        }
    }

    public GElement getRootElement() {
        return rootElement;
    }

    public GEventManager getEventManager() {
        return eventManager;
    }

    public void setDelegate(GViewDelegate delegate) {
        this.delegate = delegate;
    }

    public void setSmoothGraphics(boolean flag) {
        smoothGraphics = flag;
    }

    public boolean getSmoothGraphics() {
        return smoothGraphics;
    }

    public void setAutoAdjustSize(boolean flag) {
        this.autoAdjustSize = flag;
    }

    public boolean getAutoAdjustSize() {
        return autoAdjustSize;
    }

    public float getSelectionAlphaValue() {
        return selectionAlphaVariator.getAlphaValue();
    }

    public float getFocusAlphaValue() {
        return focusAlphaVariator.getAlphaValue();
    }

    public Point getMousePosition(MouseEvent e) {
        Point p = e.getPoint();
        if(zoom != 1)
            return new Point((int)(p.x/zoom), (int)(p.y/zoom));
        else
            return p;
    }

    public Point getLastMousePosition() {
        return lastMousePosition;
    }

    public void setDrawBorder(boolean flag) {
        this.drawBorder = flag;
    }

    public boolean getDrawBorder() {
        return drawBorder;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        autoAdjustSize();
    }

    public float getZoom() {
        return zoom;
    }

    public void setSizeMargin(int margin) {
        this.sizeMargin = margin;
    }

    public void setRealSize(Dimension d) {
        if(sizeMargin > 0) {
            d.width += sizeMargin;
            d.height += sizeMargin;
        }
        setPreferredSize(d);
        setMaximumSize(d);
        revalidate();
    }

    public void setRealSize(int dx, int dy) {
        setRealSize(new Dimension(dx, dy));
    }

    public Dimension getRealSize() {
        return getPreferredSize();
    }

    public void autoAdjustSize() {
        if(rootElement == null || !autoAdjustSize)
            return;

        Rect bounds = rootElement.bounds();
        setRealSize((int) ((bounds.r.x+bounds.r.width)*zoom),
                (int) ((bounds.r.y+bounds.r.height)*zoom));

        if(delegate != null)
            delegate.viewSizeDidChange();
    }

    public void centerAll() {
        if(rootElement == null)
            return;

        Dimension d = getRealSize();
        if(d.width == 0 && d.height == 0) {
            // Note: when the view has not been displayed, the current size seems to be (0, 0)
            d = this.getPreferredSize();
        }

        Rectangle r = rootElement.bounds().rectangle();

        double x = (d.width-r.width)*0.5;
        double y = (d.height-r.height)*0.5;

        rootElement.move(x-r.x, y-r.y);
    }

    public void setMagneticsVisible(boolean flag) {
        this.magneticsVisible = flag;
        repaint();
    }

    public boolean isMagneticsVisible() {
        return magneticsVisible;
    }

    public void toggleShowMagnetics() {
        setMagneticsVisible(!isMagneticsVisible());
    }

    public void createMagnetics() {
        magnetics.clear();
        double f = 0;
        for(int i=0; i<delegate.getHorizontalMagnetics(); i++) {
            f += 1.0/(delegate.getHorizontalMagnetics()+1);
            magnetics.add(GMagnetic.createHorizontal(f));
        }

        f = 0;
        for(int i=0; i<delegate.getVerticalMagnetics(); i++) {
            f += 1.0/(delegate.getVerticalMagnetics()+1);
            magnetics.add(GMagnetic.createVertical(f));
        }
    }

    public void showAndAjustPositionToMagnetics(Vector2D position) {
        for (GMagnetic magnetic : magnetics) {
            magnetic.showAndAjust(position, getRealSize());
        }
    }

    public void hideAllMagnetics() {
        for (GMagnetic magnetic : magnetics) {
            magnetic.setVisible(false);
        }
    }

    public void scrollElementToVisible(GElement element) {
        Rectangle r;
        Rect frame = element.getFrame();
        if(frame == null)
            r = new Rectangle((int)element.getPositionX(), (int)element.getPositionY(), 1, 1);
        else
            r = frame.r;

        // Scale according to the current zoom
        r.x *= zoom;
        r.y *= zoom;
        r.width *= zoom;
        r.height *= zoom;

        // Add some margin to make the element "more" visible
        r.x -= SCROLL_TO_VISIBLE_MARGIN;
        r.y -= SCROLL_TO_VISIBLE_MARGIN;
        r.width += 2*SCROLL_TO_VISIBLE_MARGIN;
        r.height += 2*SCROLL_TO_VISIBLE_MARGIN;

        scrollRectToVisible(r);
        //new XJSmoothScrolling(this, r, null);
    }

    public void addSelectedElement(GElement element) {
        selectionTimer.add(element);
    }

    public void removeSelectedElement(GElement element) {
        selectionTimer.remove(element);
    }

    public void addFocusedElement(GElement element) {
        focusTimer.add(element);
    }

    public void removeFocusedElement(GElement element) {
        focusTimer.remove(element);
    }

    public BufferedImage getImage() {
        int width = getPreferredSize().width;
        int height = getPreferredSize().height;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D)image.getGraphics();
        super.paintComponent(g2d);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, getPreferredSize().width, getPreferredSize().height);

        rootElement.drawRecursive(g2d);

        g2d.dispose();

        return image; //makeColorTransparent(image, Color.white);
    }

    public String getEPS() {
        XJGraphics2DPS g2d = new XJGraphics2DPS();
        g2d.setMargins(6, 6);
        rootElement.drawRecursive(g2d);
        return g2d.getPSText();
    }

    // Does not work currently - don't know why...
    public Image makeColorTransparent(Image im, final Color color) {
        ImageFilter filter = new RGBImageFilter() {
            public int markerRGB = color.getRGB() | 0xFF000000;

            public int filterRGB(int x, int y, int rgb) {
                if ( ( rgb | 0xFF000000 ) == markerRGB ) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                }
                else {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

    public void drawMagnetics(Graphics2D g2d) {
        g2d.setColor(Color.yellow);

        for (GMagnetic magnetic : magnetics) {
            if (magneticsVisible || magnetic.isVisible())
                magnetic.draw(g2d, getRealSize());
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D)g;
        if(smoothGraphics)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        if(drawBorder) {
            g2d.setColor(Color.white);
            g2d.fillRect(0, 0, getPreferredSize().width, getPreferredSize().height);
            g2d.setColor(Color.darkGray);
            g2d.drawRect(0, 0, getPreferredSize().width, getPreferredSize().height);
            g2d.clipRect(0, 0, getPreferredSize().width, getPreferredSize().height);
        }

        g2d.scale(zoom, zoom);

        drawMagnetics(g2d);
        if(rootElement != null)
            rootElement.drawRecursive(g2d);

        eventManager.performEventObjects(GEventManager.EVENT_DRAW, null, null, g);
    }

    public void addMenuItem(JPopupMenu menu, String title, int tag, Object object) {
        XJMenuItem item = new XJMenuItem();
        item.setTitle(title);
        item.setTag(tag);
        item.setObject(object);
        item.setDelegate(this);

        menu.add(item.getSwingComponent());
    }

    public JPopupMenu getContextualMenu(GElement element) {
        return null;
    }

    public java.util.List<GElement> getSelectedElements() {
        return selectionTimer.getElements();
    }

    public GElement getElementAtMousePosition(MouseEvent e) {
        return getElementAtPoint(getMousePosition(e));
    }

    public GElement getElementAtPoint(Point p) {
        if(rootElement != null)
            return rootElement.match(p);
        else
            return null;
    }

    public void changeDone() {
        if(delegate == null)
            return;

        delegate.changeOccured();
    }

    public void selectElementsInRect(int x, int y, int dx, int dy) {
        Rect rectangle = new Rect(x, y, dx, dy);
        if(rootElement == null || rootElement.getElements() == null)
            return;

        for (GElement element : rootElement.getElements()) {
            boolean selected = Rect.intersect(rectangle, element.bounds());
            element.setSelected(selected);
            if (selected)
                addSelectedElement(element);
            else
                removeSelectedElement(element);
        }
    }

    public void selectAllElements(boolean select) {
        if(rootElement == null)
            return;

        for (GElement element : rootElement.getElements()) {
            element.setSelected(select);
            if (select)
                addSelectedElement(element);
            else
                removeSelectedElement(element);
        }
    }

    public void moveSelectedElements(double dx, double dy) {
        for (GElement element : selectionTimer.getElements()) {
            element.move(dx, dy);
        }
        autoAdjustSize();
    }

    public void timerFired(GTimer timer) {
        if(timer == selectionTimer)
            selectionAlphaVariator.run();
        else if(timer == focusTimer)
            focusAlphaVariator.run();

        repaint();
    }

    public void processMouseEvent(MouseEvent e) {
        if(e.isPopupTrigger()) {
            JPopupMenu menu = getContextualMenu(getElementAtPoint(getMousePosition(e)));
            if(menu != null) {
                this.requestFocus();
                menu.show(this, e.getX(), e.getY());
                lastMousePosition = getMousePosition(e);
            }
        } else
            super.processMouseEvent(e);
    }

    public void handleMousePressed(MouseEvent e) {
        eventManager.performEventObjects(GEventManager.EVENT_MOUSE_PRESSED, e, getMousePosition(e), null);
    }

    public void handleMouseReleased(MouseEvent e) {
        eventManager.performEventObjects(GEventManager.EVENT_MOUSE_RELEASED, e, getMousePosition(e), null);
    }

    public void handleMouseDragged(MouseEvent e) {
        eventManager.performEventObjects(GEventManager.EVENT_MOUSE_DRAGGED, e, getMousePosition(e), null);
    }

    public void handleMouseMoved(MouseEvent e) {
        eventManager.performEventObjects(GEventManager.EVENT_MOUSE_MOVED, e, getMousePosition(e), null);
        if(delegate != null)
            delegate.contextualHelp(getElementAtPoint(getMousePosition(e)));
    }

    public void handleMouseEntered(MouseEvent e) {
        selectionTimer.refresh();
        focusTimer.refresh();
    }

    public void handleMouseExited(MouseEvent e) {
        selectionTimer.stop();
        focusTimer.stop();
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
    }

    public class MyContextualMenuListener implements PopupMenuListener {

        public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
            repaint();
        }

        public void popupMenuCanceled(PopupMenuEvent event) {
        }
    }

    // *** GEventDelegate methods

    public void eventChangeDone() {
        autoAdjustSize();
        changeDone();
    }

    public void eventShouldRepaint() {
        repaint();
    }

    public GElement eventQueryElementAtPoint(Point p) {
        return getElementAtPoint(p);
    }

    public GElement eventQueryRootElement() {
        return rootElement;
    }

    public void eventSouldSelectAllElements(boolean flag) {
        selectAllElements(flag);
    }

    public void eventMoveSelectedElements(int dx, int dy) {
        moveSelectedElements(dx, dy);
    }

    public void eventCreateElement(Point p, boolean doubleclick) {

    }

    public void eventEditElement(GElement e) {

    }

    public boolean eventCanCreateLink() {
        return false;
    }

    public double eventLinkFlateness() {
        return DEFAULT_LINK_FLATENESS;
    }

    public void eventCreateLink(GElement source, String sourceAnchorKey, GElement target, String targetAnchorKey, int shape, Point p) {
        rootElement.addElement(new GLink(source, sourceAnchorKey, target, targetAnchorKey, shape, "", p, GView.DEFAULT_LINK_FLATENESS));
    }

    public void eventSelectElementsInRect(int x, int y, int dx, int dy) {
        selectElementsInRect(x, y, dx, dy);
    }

    public void eventAddFocusedElement(GElement element) {
        addFocusedElement(element);
    }

    public void eventRemoveFocusedElement(GElement element) {
        removeFocusedElement(element);
    }

    public boolean eventIsSelectedElement(GElement element) {
        return selectionTimer.contains(element);
    }

}
