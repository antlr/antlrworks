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

package org.antlr.xjlib.appkit.frame;

import org.antlr.xjlib.appkit.XJControl;
import org.antlr.xjlib.appkit.app.XJApplication;
import org.antlr.xjlib.appkit.app.XJPreferences;
import org.antlr.xjlib.appkit.menu.*;
import org.antlr.xjlib.appkit.undo.XJUndo;
import org.antlr.xjlib.appkit.undo.XJUndoDelegate;
import org.antlr.xjlib.appkit.undo.XJUndoEngine;
import org.antlr.xjlib.appkit.utils.XJLocalizable;
import org.antlr.xjlib.foundation.XJSystem;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.io.File;

public class XJFrame extends XJControl implements XJMenuBarCustomizer, XJMenuBarDelegate {

    private static final String PROPERTY_WINDOW_MODIFIED = "windowModified";
    private static final String PREF_DESKTOP_BOUNDS = "xjdesktop_bounds";

    private static final boolean useDesktop = XJApplication.shared().useDesktopMode();

    private static JDesktopPane desktop;
    protected static JFrame desktopFrame;
    protected static XJMainMenuBar desktopDefaultMenuBar;

    private JInternalFrame jInternalFrame;
    private JFrame jFrame;
    private WindowListener wl;
    private InternalFrameAdapter ifa;

    protected XJMainMenuBar mainMenuBar;
    protected XJFrameDelegate delegate;
    protected XJUndoEngine undoEngine;
    protected boolean alreadyBecomeVisible = false;
    protected boolean dirty = false;

    private static void restoreDesktopBounds() {
        Rectangle r = (Rectangle) XJApplication.shared().getPreferences().getObject(PREF_DESKTOP_BOUNDS, null);
        if(r != null) {
            desktopFrame.setLocation(r.x, r.y);
            desktopFrame.setSize(r.width, r.height);
        }
    }

    private static void saveDesktopBounds() {
        Point pos = desktopFrame.getLocation();
        Dimension s = desktopFrame.getSize();
        Rectangle r = new Rectangle(pos.x, pos.y, s.width, s.height);
        XJPreferences prefs = XJApplication.shared().getPreferences();
        prefs.setObject(PREF_DESKTOP_BOUNDS, r);
    }

    public static void closeDesktop() {
        if(useDesktop) {
            saveDesktopBounds();
        }
    }

    public XJFrame() {
        if(useDesktop) {
            if(desktopFrame == null) {
                desktopFrame = new JFrame();
                desktopFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);                
                desktopFrame.setTitle(XJApplication.shared().getApplicationName());
                desktopFrame.addWindowListener(wl = new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        XJApplication.shared().performQuit();
                    }

                });

                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                dim = new Dimension((int)(dim.width*0.9), (int)(dim.height*0.9));
                desktopFrame.setSize(dim);
                desktopFrame.setPreferredSize(dim);
                desktopFrame.setLocationRelativeTo(null);
                restoreDesktopBounds();

                desktop = new JDesktopPane();
                desktopFrame.getContentPane().add(desktop, BorderLayout.CENTER);
                desktopFrame.setVisible(true);

                new XJInternalFrameHandling();
            }

            jInternalFrame = new JInternalFrame();
            jInternalFrame.setResizable(true);
            jInternalFrame.setClosable(true);
            jInternalFrame.setMaximizable(true);
            jInternalFrame.setIconifiable(true);

            desktop.add(jInternalFrame);

            jInternalFrame.addInternalFrameListener(ifa = new InternalFrameAdapter() {
                public void internalFrameActivated(InternalFrameEvent e) {
                    XJMainMenuBar mb = getMainMenuBar();
                    if(mb != null) {
                        desktopFrame.setJMenuBar(mb.getJMenuBar());
                    }
                    XJFrame.this.windowActivated();
                }

                public void internalFrameDeactivated(InternalFrameEvent e) {
                    // Don't send the deactivate event if the frame is closed
                    // (because this event is send also when the window just closed
                    // which is kind of weird)
                    if(jInternalFrame != null)
                        XJFrame.this.windowDeactivated();
                }

                public void internalFrameClosing(InternalFrameEvent e) {
                    close(false);
                }

            });
        } else {
            jFrame = new JFrame();
            jFrame.addWindowListener(wl = new WindowAdapter() {
                public void windowActivated(WindowEvent e) {
                    XJFrame.this.windowActivated();
                }

                public void windowDeactivated(WindowEvent e) {
                    // Don't send the deactivate event if the frame is closed
                    // (because this event is send also when the window just closed
                    // which is kind of weird)
                    if(jFrame != null)
                        XJFrame.this.windowDeactivated();
                }

                public void windowClosing(WindowEvent e) {
                    close(false);
                }

            });
        }
        setDefaultSize();
        undoEngine = new XJUndoEngine();
    }

    @Override
    public void awake() {
        if(shouldDisplayMainMenuBar()) {
            mainMenuBar = XJMainMenuBar.createInstance();
            mainMenuBar.setCustomizer(this);
            mainMenuBar.setDelegate(this);
            mainMenuBar.createMenuBar();
            setMainMenuBar(mainMenuBar);
            undoEngine.setMainMenuBar(mainMenuBar);
        }
    }

    public void setDelegate(XJFrameDelegate delegate) {
        this.delegate = delegate;
    }

    public XJFrameDelegate getDelegate() {
        return delegate;
    }

    public void setDefaultCloseOperation(int operation) {
        if(useDesktop) {
            jInternalFrame.setDefaultCloseOperation(operation);
        } else {
            jFrame.setDefaultCloseOperation(operation);
        }
    }

    public Container getContentPane() {
        if(useDesktop) {
            return jInternalFrame.getContentPane();
        } else {
            return jFrame.getContentPane();
        }
    }

    public JRootPane getRootPane() {
        if(useDesktop) {
            return jInternalFrame.getRootPane();
        } else {
            return jFrame.getRootPane();
        }
    }

    public JLayeredPane getLayeredPane() {
        if(useDesktop) {
            return jInternalFrame.getLayeredPane();
        } else {
            return jFrame.getLayeredPane();
        }
    }

    public Component getGlassPane() {
        if(useDesktop) {
            return jInternalFrame.getGlassPane();
        } else {
            return jFrame.getGlassPane();
        }
    }

    public void setMainMenuBar(XJMainMenuBar menubar) {
        this.mainMenuBar = menubar;
        if(useDesktop) {
            if(jInternalFrame.isSelected()) {
                desktopFrame.setJMenuBar(mainMenuBar.getJMenuBar());
            }
        } else {
            jFrame.setJMenuBar(mainMenuBar.getJMenuBar());
        }
    }

    public XJMainMenuBar getMainMenuBar() {
        return mainMenuBar;
    }

    public void menuItemStatusChanged(int tag) {
        if(mainMenuBar == null)
            return;

  //      XJMainMenuBar.refreshAllMenuBars();
        mainMenuBar.refresh();
//        mainMenuBar.refreshState();
    }

    private String title;

    public void setTitle(String title) {
        this.title = title;

        if(XJSystem.isMacOS()) {
            // set the path into the window's title so Mac OS 10.5 users can
            // benefit from the popup to navigate the file path hierarchy
            File f = null;
            if(title != null) {
                f = new File(title);
                if(!f.exists()) {
                    f = null;
                }
            }
            getRootPane().putClientProperty("Window.documentFile", f);
        }

        if(useDesktop) {
            jInternalFrame.setTitle(customizeWindowTitle(getTitle()));
        } else {
            jFrame.setTitle(customizeWindowTitle(getTitle()));
        }
    }

    public String getTitle() {
        return title==null?"Untitled":title;
    }

    public void updateTitle() {
        setTitle(title);
    }

    private String customizeWindowTitle(String title) {
        if(dirty() && !XJSystem.isMacOS()) {
            // indicator if the document is dirty
            return title+" *";
        } else {
            // Mac OS X has one by default
            return title;
        }
    }

    public void setLocation(Point loc) {
        if(useDesktop) {
            jInternalFrame.setLocation(loc);
        } else {
            jFrame.setLocation(loc);
        }
    }

    public Point getLocation() {
        if(useDesktop) {
            return jInternalFrame.getLocation();
        } else {
            return jFrame.getLocation();
        }
    }

    public void setSize(int dx, int dy) {
        if(useDesktop) {
            jInternalFrame.setSize(dx, dy);
        } else {
            jFrame.setSize(dx, dy);
        }
    }

    public void setPreferredSize(int dx, int dy) {
        setPreferredSize(new Dimension(dx, dy));
    }

    public void setPreferredSize(Dimension size) {
        if(useDesktop) {
            jInternalFrame.setPreferredSize(size);
        } else {
            jFrame.setPreferredSize(size);
        }
    }

    public Dimension getPreferredSize() {
        if(useDesktop) {
            return jInternalFrame.getPreferredSize();
        } else {
            return jFrame.getPreferredSize();
        }
    }
    
    public void setSize(Dimension size) {
        if(useDesktop) {
            jInternalFrame.setSize(size);
        } else {
            jFrame.setSize(size);
        }
    }

    public Dimension getSize() {
        if(useDesktop) {
            return jInternalFrame.getSize();
        } else {
            return jFrame.getSize();
        }
    }

    public void setDefaultSize() {
        if(useDesktop) {
            Dimension dim = desktop.getSize();
            setPreferredSize((int) (dim.width*0.8), (int) (dim.height*0.8));
        } else {
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            setPreferredSize((int)(dim.width*0.5), (int)(dim.height*0.5));
        }
    }

    public void setResizable(boolean flag) {
        if(useDesktop) {
            jInternalFrame.setResizable(flag);
        } else {
            jFrame.setResizable(flag);
        }
    }

    public void setMaximizable(boolean flag) {
        if(useDesktop) {
            jInternalFrame.setMaximizable(flag);
        } else {
            // not applicable
        }
    }

    public boolean isMaximized() {
        return useDesktop && jInternalFrame.isMaximum();
    }

    public void pack() {
        if(useDesktop) {
            jInternalFrame.pack();
        } else {
            jFrame.pack();
        }
    }

    public void bringToFront() {
        if(useDesktop) {
            if(jInternalFrame == null) return;

            jInternalFrame.moveToFront();
            try {
                jInternalFrame.setSelected(true);
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
        } else {
            jFrame.toFront();
        }
    }

    public void setVisible(boolean flag) {
        if(flag && !alreadyBecomeVisible) {
            alreadyBecomeVisible = true;
            restoreWindowBounds();
            ensureVisibility();
            becomingVisibleForTheFirstTime();
        }
        if(useDesktop) {
            jInternalFrame.setVisible(flag);
        } else {
            jFrame.setVisible(flag);
        }
        bringToFront();
    }

    /**
     * Ensures that the window is visible in one of the available screen.
     * Otherwise, move it to the center of the main screen.
     */
    public void ensureVisibility() {
        if(!isVisibleOnScreen()) {
            center();
        }
    }

    public void becomingVisibleForTheFirstTime() {

    }

    public String autosaveName() {
        return null;
    }

    public boolean isVisible() {
        if(useDesktop) {
            return jInternalFrame != null && jInternalFrame.isVisible();
        } else {
            return jFrame.isVisible();
        }
    }

    public boolean isActive() {
        if(useDesktop) {
            return jInternalFrame != null && jInternalFrame.isSelected();
        } else {
            return jFrame.isActive();
        }
    }

    public void show() {
        setVisible(true);
    }

    public void showModal() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    public boolean isCompletelyOnScreen() {
        return isVisibleOnScreen();
    }

    public boolean isVisibleOnScreen() {
        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();

        Rectangle fr = (useDesktop?jInternalFrame.getBounds():jFrame.getBounds());
        for (GraphicsDevice g : gs) {
            GraphicsConfiguration dc = g.getDefaultConfiguration();
            if (fr.intersects(dc.getBounds())) {
                return true;
            }
        }

        return false;
    }

    public void center() {
        if(useDesktop) {
            Dimension s = desktopFrame.getSize();
            Dimension id = jInternalFrame.getSize();
            jInternalFrame.setLocation(s.width/2 - id.width/2, s.height/2 - id.height/2);
        } else {
            jFrame.setLocationRelativeTo(null);
        }
    }

    public void setPosition(int x, int y) {
        setLocation(new Point(x, y));
    }

    public void offsetPosition(int dx, int dy) {
        Point p = getLocation();
        setPosition(p.x+dx, p.y+dy);
    }

    public boolean performClose(boolean force) {
       /* Cannot do that because when opening a document when another one is already open
        will cause the new document to be closed instead of the previous one
       if(FrameManager.activated != null && FrameManager.activated != this) {
            return FrameManager.activated.performClose();
        } else {
            return close();
        }*/
        return close(force);
    }

    protected boolean close(boolean force) {
        XJMainMenuBar.removeInstance(mainMenuBar);
        if(mainMenuBar != null) {
            mainMenuBar.setCustomizer(null);
            mainMenuBar.setDelegate(null);
            mainMenuBar = null;
        }

        saveWindowBounds();

        if(useDesktop) {
            if(jInternalFrame.isSelected()) {
                desktopFrame.setJMenuBar(desktopDefaultMenuBar.getJMenuBar());
            }

            jInternalFrame.removeInternalFrameListener(ifa);
            jInternalFrame.dispose();
            desktop.remove(jInternalFrame);
            jInternalFrame = null;

            // select the next frame available
            for (JInternalFrame f : desktop.getAllFrames()) {
                if(f.isVisible()) {
                    try {
                        f.setSelected(true);
                    } catch (PropertyVetoException e) {
                        // should not happen
                        e.printStackTrace();
                    }
                    break;
                }
            }

            //XJApplication.getActiveContainer()
            // AW-120: memory leak issue
            // BUG: the fCurrentFrame in AquaDesktopManager is still referencing the jInternalFrame
            // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4759312
        } else {
            jFrame.removeWindowListener(wl);
            jFrame.dispose();
            jFrame = null;
        }

        if(delegate != null)
            delegate.frameDidClose(this);

        return true;
    }

    public void setDirty() {
        if(!XJApplication.shared().supportsPersistence())
            return;

        // Use dirty member to speed up
        if(!dirty) {
            dirty = true;
            getRootPane().putClientProperty(PROPERTY_WINDOW_MODIFIED, Boolean.TRUE);
            menuItemStatusChanged(XJMainMenuBar.MI_SAVE);
            updateTitle();
            dirtyChanged();
        }
    }

    public void resetDirty() {
        if(!XJApplication.shared().supportsPersistence())
            return;

        if(dirty) {
            dirty = false;
            getRootPane().putClientProperty(PROPERTY_WINDOW_MODIFIED, Boolean.FALSE);
            menuItemStatusChanged(XJMainMenuBar.MI_SAVE);
            updateTitle();
            dirtyChanged();
        }
    }

    public void dirtyChanged() {
        // for subclass
    }

    public boolean dirty() {
        if(!XJApplication.shared().supportsPersistence())
            return false;

        Boolean b = (Boolean) getRootPane().getClientProperty(PROPERTY_WINDOW_MODIFIED);
        return b != null && b;
    }

    public void registerUndo(XJUndoDelegate delegate, JTextPane textPane) {
        undoEngine.registerUndo(new XJUndo(undoEngine, delegate), textPane);
    }

    public void unregisterUndo(XJUndoDelegate delegate) {
        undoEngine.unregisterUndo(delegate);
    }

    public void performUndo() {
        XJUndo undo = getCurrentUndo();
        if(undo != null) {
            undo.performUndo();
        }
    }

    public void performRedo() {
        XJUndo undo = getCurrentUndo();
        if(undo != null) {
            undo.performRedo();
        }
    }

    public XJUndo getUndo(JTextPane textPane) {
        return undoEngine.getUndo(textPane);
    }

    public XJUndo getCurrentUndo() {
        return undoEngine.getCurrentUndo();
    }

    public boolean shouldDisplayMainMenuBar() {
        return true;
    }

    public boolean shouldAppearsInWindowMenu() {
        return false;
    }

    public void windowActivated() {

    }

    public void windowDeactivated() {

    }

    public void customizeFileMenu(XJMenu menu) {

    }

    public void customizeEditMenu(XJMenu menu) {

    }

    public void customizeWindowMenu(XJMenu menu) {

    }

    public void customizeHelpMenu(XJMenu menu) {

    }

    public void customizeMenuBar(XJMainMenuBar menubar) {

    }

    public void menuItemState(XJMenuItem item) {
        switch(item.getTag()) {
            case XJMainMenuBar.MI_NEW:
                item.setTitle(XJLocalizable.getXJString("New")+((XJApplication.shared().getDocumentExtensions().size()>1)?"...":""));
                break;
            case XJMainMenuBar.MI_UNDO:
            case XJMainMenuBar.MI_REDO:
                getMainMenuBar().menuUndoRedoItemState(undoEngine.getCurrentUndo());
                break;
        }
    }

    public void handleMenuEvent(XJMenu menu, XJMenuItem item) {
        switch(item.getTag()) {
            case XJMainMenuBar.MI_UNDO:
                performUndo();
                break;
            case XJMainMenuBar.MI_REDO:
                performRedo();
                break;
            case XJMainMenuBar.MI_CUT:
                performActionOnFocusedJComponent(DefaultEditorKit.cutAction);
                break;
            case XJMainMenuBar.MI_COPY:
                performActionOnFocusedJComponent(DefaultEditorKit.copyAction);
                break;
            case XJMainMenuBar.MI_PASTE:
                performActionOnFocusedJComponent(DefaultEditorKit.pasteAction);
                break;
            case XJMainMenuBar.MI_SELECT_ALL:
                performActionOnFocusedJComponent(DefaultEditorKit.selectAllAction);
                break;
        }
    }

    public void handleMenuSelected(XJMenu menu) {
    }

    public static void performActionOnFocusedJComponent(String action) {
        JComponent c = getFocusedJComponent();
        if(c != null)
            c.getActionMap().get(action).actionPerformed(null);
    }

    public static JComponent getFocusedJComponent() {
        Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        if(c instanceof JComponent)
            return (JComponent)c;
        else
            return null;
    }

    public Container getJavaContainer() {
        if(useDesktop) {
            return jInternalFrame;
        } else {
            return jFrame;
        }
    }

    private void restoreWindowBounds() {
        String name = autosaveName();
        if(name == null) return;

        Rectangle r = restoreWindowBoundsNewWay(name);
        if(r == null) {
            r = (Rectangle) XJApplication.shared().getPreferences().getObject(name, null);
        }

        if(r != null) {
            setPosition(r.x, r.y);
            setSize(r.width, r.height);
        }
    }

    private static final String PREF_WINDOWS_BOUNDS_PREFIX = "xjframe_bounds.";
    private static final int MAX_WINDOWS_BOUND_KEYS = 100;

    private Rectangle restoreWindowBoundsNewWay(String name) {
        return (Rectangle) XJApplication.shared().getPreferences().getObject(PREF_WINDOWS_BOUNDS_PREFIX+name, null);
    }

    private void saveWindowBounds() {
        String name = autosaveName();
        if(name == null)
            return;

        Point pos = getLocation();
        Dimension s = getSize();
        Rectangle r = new Rectangle(pos.x, pos.y, s.width, s.height);
        XJPreferences prefs = XJApplication.shared().getPreferences();

        // cleanup the prefs by making sure no more than MAX_WINDOWS_BOUND_KEYS
        // are existing in the prefs.
        String[] keys = prefs.getKeys();
        if(keys != null) {
            int count = 0;
            for(String key : keys) {
                if(key.startsWith(PREF_WINDOWS_BOUNDS_PREFIX)) {
                    count++;
                    if(count > MAX_WINDOWS_BOUND_KEYS) {
                        prefs.remove(key);
                    }
                }
            }
        }
        prefs.setObject(PREF_WINDOWS_BOUNDS_PREFIX+name, r);
    }

}
