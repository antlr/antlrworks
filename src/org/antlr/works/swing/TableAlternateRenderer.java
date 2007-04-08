package org.antlr.works.swing;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author Copyright (c) 2007 by BEA Systems, Inc. All Rights Reserved.
 */
public class TableAlternateRenderer extends DefaultTableCellRenderer {

    private static final Color ALTERNATE_TABLE_COLOR = new Color(240, 240, 250);

    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column)
    {
        if(isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(row % 2 == 0? Color.white:ALTERNATE_TABLE_COLOR);
        }
        return super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
    }

}
