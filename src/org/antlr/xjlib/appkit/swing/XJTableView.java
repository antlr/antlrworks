package org.antlr.xjlib.appkit.swing;

import org.antlr.xjlib.foundation.XJSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
/*

[The "BSD licence"]
Copyright (c) 2005-2007 Jean Bovet
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

public class XJTableView extends JScrollPane {

    private XJTable table = new XJTable();
    private boolean alternateBackground = true;

    public XJTableView() {
        setViewportView(table);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                autoresizeColumns();
            }
        });

        table.setDefaultRenderer(Object.class, new XJTableAlternateRenderer());
        table.setShowGrid(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setWheelScrollingEnabled(true);
        getViewport().setBackground(Color.white);
    }

    public XJTable getTable() {
        return table;
    }

    public boolean isAlternateBackground() {
        return alternateBackground;
    }

    public void setAlternateBackground(boolean alternateBackground) {
        this.alternateBackground = alternateBackground;
        if(alternateBackground) {
            table.setDefaultRenderer(Object.class, new XJTableAlternateRenderer());
        } else {
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
        }
    }

    public void autoresizeColumns() {
        resizeTableColumnsToFitContent(table, 20);

        TableColumnModel model = table.getColumnModel();
        int columnTotalWidth = 0;
        for(int i=0; i<model.getColumnCount()-1; i++) {
            columnTotalWidth += model.getColumn(i).getPreferredWidth();
        }

        // Note: this offset should be UI dependent
        int offset;
        if(getVerticalScrollBar().isVisible()) {
            if(XJSystem.isWindows()) {
                offset = 20;
            } else {
                offset = 20;
            }
        } else {
            if(XJSystem.isWindows()) {
                offset = 3;
            } else {
                offset = 4;
            }
        }

        int spWidth = getWidth();
        if(spWidth == 0) {
            spWidth = getPreferredSize().width;
        }

        if(model.getColumnCount() > 0) {
            TableColumn c = model.getColumn(model.getColumnCount()-1);
            int prefWidth = c.getPreferredWidth();
            c.setPreferredWidth(Math.max(spWidth - columnTotalWidth - offset, prefWidth));
        }
    }

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

    public void scrollToLastRow() {
        table.scrollRectToVisible(table.getCellRect(table.getRowCount()-1, 0, true));
    }
}
