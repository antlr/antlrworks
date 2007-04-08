package org.antlr.works.swing;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * @author Copyright (c) 2007 by BEA Systems, Inc. All Rights Reserved.
 */
public class TableResizer {

    public static void resizeTableColumnsToFitContent(JTable table, int margin) {
        for(int c = 0; c < table.getColumnCount(); c++) {
            resizeColumnToFitContent(table, c, margin);
        }
    }

    public static void resizeColumnToFitContent(JTable table, int columnIndex, int margin) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        TableCellRenderer renderer = column.getHeaderRenderer();
        if (renderer == null) {
            renderer = table.getTableHeader().getDefaultRenderer();
        }
        Component c = renderer.getTableCellRendererComponent(table,
                column.getHeaderValue(),
                false, false,
                0, 0);
        int maxWidth = c.getPreferredSize().width;

        for(int row=0; row<table.getRowCount(); row++) {
            renderer = table.getCellRenderer(row, columnIndex);
            c = renderer.getTableCellRendererComponent(table,
                    table.getValueAt(row, columnIndex),
                    false, false,
                    row, columnIndex);
            maxWidth = Math.max(maxWidth, c.getPreferredSize().width);
        }

        column.setPreferredWidth(maxWidth+margin);
    }

    public static void resizeColumnsToFitContent(JTable table, JScrollPane sp, int margin) {
        resizeTableColumnsToFitContent(table, margin);

        TableColumnModel model = table.getColumnModel();
        int columnTotalWidth = 0;
        for(int i=0; i<model.getColumnCount()-1; i++) {
            columnTotalWidth += model.getColumn(i).getPreferredWidth();
        }

        // FIXME this offset should be UI dependent
        int offset = 3;
        if(sp.getVerticalScrollBar().isVisible()) {
            offset = 20;
        }
        TableColumn c = model.getColumn(model.getColumnCount()-1);
        int prefWidth = c.getPreferredWidth();
        c.setPreferredWidth(Math.max(sp.getWidth() - columnTotalWidth - offset, prefWidth));
    }

}
