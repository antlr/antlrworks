package org.antlr.works.utils;

import javax.swing.*;
import java.awt.*;

/**
 * @author Copyright (c) 2007 by BEA Systems, Inc. All Rights Reserved.
 */
public class Toolbar extends Box {

    private boolean horizontal;

    public static Toolbar createHorizontalToolbar() {
        return new Toolbar(BoxLayout.X_AXIS);
    }

    public static Toolbar createVerticalToolbar() {
        return new Toolbar(BoxLayout.Y_AXIS);
    }

    public Toolbar(int axis) {
        super(axis);
        horizontal = axis == BoxLayout.X_AXIS;
        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    }

    public Component addElement(Component c) {
        if(getComponentCount() > 0) {
            if(horizontal) {
                add(Box.createHorizontalStrut(getOffset()));
            } else {
                add(Box.createVerticalStrut(getOffset()));
            }
        }
        return add(c);
    }

    public void addGroupSeparator() {
        if(horizontal) {
            add(Box.createHorizontalStrut(getGroupOffset()));
        } else {
            add(Box.createVerticalStrut(getGroupOffset()));
        }
    }

    private int getOffset() {
        return 2;
    }

    private int getGroupOffset() {
        return 12;
    }
}
