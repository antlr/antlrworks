package org.antlr.works.utils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
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

public class CustomSplitPanel extends JPanel {

    public static final int LEFT_INDEX = 0;
    public static final int MIDDLE_INDEX = 1;
    public static final int RIGHT_INDEX = 2;

    public JSplitPane leftSplitPane;
    public JSplitPane rightSplitPane;

    public Component left, middle, right;
    public final Map<Component,Float> widths = new HashMap<Component, Float>();

    public CustomSplitPanel() {
        super(new BorderLayout());
        leftSplitPane = createSplitPane();
        rightSplitPane = createSplitPane();
    }

    public void close() {
        left = null;
        middle = null;
        right = null;
        widths.clear();
    }

    public JSplitPane createSplitPane() {
        JSplitPane pane = new JSplitPane();
        pane.setBorder(null);
        pane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        pane.setContinuousLayout(true);
        pane.setOneTouchExpandable(true);
        pane.setResizeWeight(0.5);
        return pane;
    }

    public void setComponents(Component leftComponent, Component middleComponent, Component rightComponent) {
        leftSplitPane.setLeftComponent(left = leftComponent);
        leftSplitPane.setRightComponent(middle = middleComponent);
        rightSplitPane.setLeftComponent(leftSplitPane);
        rightSplitPane.setRightComponent(right = rightComponent);
        add(rightSplitPane, BorderLayout.CENTER);
        resize();
    }

    public void setComponentWidth(Component c, float width) {
        widths.put(c, width);
    }

    public void resize() {
        if(left != null && middle != null && right != null) {
            setDividerLocationToComponentWidth(rightSplitPane, getWidth(left)+getWidth(middle));
            setDividerLocationToComponentWidth(leftSplitPane, getWidth(left));
        } else if(left != null && middle != null) {
            setDividerLocationToComponentWidth(rightSplitPane, getWidth(left));
        } else if(left != null && right != null) {
            setDividerLocationToComponentWidth(rightSplitPane, getWidth(left));
        } else if(middle != null && right != null) {
            setDividerLocationToComponentWidth(rightSplitPane, getWidth(middle));
        }

        /* This is really ugly but if I don't do the resize later on again, it happens that if the right divider is
        moved and the middle panel hidden and then showed again, the left split pane's divider will be screwed up. Maybe
        a bug in my code but don't have time to investigate more. If someone finds the reason, let me know.
        */
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(left != null && middle != null && right != null) {
                    setDividerLocationToComponentWidth(rightSplitPane, getWidth(left)+getWidth(middle));
                    setDividerLocationToComponentWidth(leftSplitPane, getWidth(left));
                } else if(left != null && middle != null) {
                    setDividerLocationToComponentWidth(rightSplitPane, getWidth(left));
                } else if(left != null && right != null) {
                    setDividerLocationToComponentWidth(rightSplitPane, getWidth(left));
                } else if(middle != null && right != null) {
                    setDividerLocationToComponentWidth(rightSplitPane, getWidth(middle));
                }
            }
        });

    }

    public void setDividerLocationToComponentWidth(JSplitPane splitPane, int width) {
        splitPane.setDividerLocation(width);
    }

    public int getWidth(Component c) {
        Float width = widths.get(c);
        if(width != null)
            return (int)width.floatValue();
        else
            return 0;
    }

    public void setComponent(Component c, int index) {
        switch(index) {
            case LEFT_INDEX: setLeftComponent(c); break;
            case MIDDLE_INDEX: setMiddleComponent(c); break;
            case RIGHT_INDEX: setRightComponent(c); break;
        }
    }

    public Component getComponentAtIndex(int index) {
        switch(index) {
            case LEFT_INDEX: return left;
            case MIDDLE_INDEX: return middle;
            case RIGHT_INDEX: return right;
        }
        return null;
    }

    public void setLeftComponent(Component c) {
        if(c == null) {
            removeLeftComponent();
            return;
        }

        if(middle != null && right != null) {
            rightSplitPane.setLeftComponent(null);
            leftSplitPane.setLeftComponent(c);
            leftSplitPane.setRightComponent(middle);
            rightSplitPane.setLeftComponent(leftSplitPane);
        } else if(middle != null) {
            remove(middle);
            rightSplitPane.setLeftComponent(c);
            rightSplitPane.setRightComponent(middle);
            add(rightSplitPane);
        } else if(right != null) {
            remove(right);
            rightSplitPane.setLeftComponent(c);
            rightSplitPane.setRightComponent(right);
            add(rightSplitPane);
        } else if(left == null) {
            add(c);
        }
        left = c;
        resize();
    }

    public void removeLeftComponent() {
        if(middle != null && right != null) {
            leftSplitPane.setLeftComponent(null);
            leftSplitPane.setRightComponent(null);
            rightSplitPane.setLeftComponent(middle);
        } else if(middle != null) {
            rightSplitPane.setLeftComponent(null);
            rightSplitPane.setRightComponent(null);
            remove(rightSplitPane);
            add(middle);
        } else if(right != null) {
            rightSplitPane.setLeftComponent(null);
            rightSplitPane.setRightComponent(null);
            remove(rightSplitPane);
            add(right);
        }
        left = null;
        resize();
    }

    public void setMiddleComponent(Component c) {
        if(c == null) {
            removeMiddleComponent();
            return;
        }

        if(left != null && right != null) {
            rightSplitPane.setLeftComponent(leftSplitPane);
            leftSplitPane.setLeftComponent(left);
            leftSplitPane.setRightComponent(c);
        } else if(left != null) {
            remove(left);
            rightSplitPane.setLeftComponent(left);
            rightSplitPane.setRightComponent(c);
            add(rightSplitPane);
        } else if(right != null) {
            remove(right);
            rightSplitPane.setLeftComponent(c);
            rightSplitPane.setRightComponent(right);
            add(rightSplitPane);
        } else if(middle == null) {
            add(c);
        }
        middle = c;
        resize();
    }

    public void removeMiddleComponent() {
        if(left != null && right != null) {
            leftSplitPane.setLeftComponent(null);
            leftSplitPane.setRightComponent(null);
            rightSplitPane.setLeftComponent(left);
        } else if(left != null) {
            rightSplitPane.setLeftComponent(null);
            rightSplitPane.setRightComponent(null);
            remove(rightSplitPane);
            add(left);
        } else if(right != null) {
            rightSplitPane.setLeftComponent(null);
            rightSplitPane.setRightComponent(null);
            remove(rightSplitPane);
            add(right);
        }
        middle = null;
        resize();
    }

    public void setRightComponent(Component c) {
        if(c == null) {
            removeRightComponent();
            return;
        }

        if(left != null && middle != null) {
            rightSplitPane.setLeftComponent(null);
            rightSplitPane.setRightComponent(null);
            leftSplitPane.setLeftComponent(left);
            leftSplitPane.setRightComponent(middle);
            rightSplitPane.setLeftComponent(leftSplitPane);
            rightSplitPane.setRightComponent(c);
        } else if(left != null) {
            remove(left);
            rightSplitPane.setLeftComponent(left);
            rightSplitPane.setRightComponent(c);
            add(rightSplitPane);
        } else if(middle != null) {
            remove(middle);
            rightSplitPane.setLeftComponent(middle);
            rightSplitPane.setRightComponent(c);
            add(rightSplitPane);
        } else if(right == null) {
            add(c);
        }
        right = c;
        resize();
    }

    public void removeRightComponent() {
        if(left != null && middle != null) {
            leftSplitPane.setLeftComponent(null);
            leftSplitPane.setRightComponent(null);
            rightSplitPane.setLeftComponent(left);
            rightSplitPane.setRightComponent(middle);
        } else if(left != null) {
            rightSplitPane.setLeftComponent(null);
            rightSplitPane.setRightComponent(null);
            remove(rightSplitPane);
            add(left);
        } else if(middle != null) {
            rightSplitPane.setLeftComponent(null);
            rightSplitPane.setRightComponent(null);
            remove(rightSplitPane);
            add(middle);
        }
        right = null;
        resize();
    }

}
