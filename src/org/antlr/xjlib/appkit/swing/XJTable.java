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

package org.antlr.xjlib.appkit.swing;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.dnd.Autoscroll;

public class XJTable extends JTable implements Autoscroll {

    private int margin = 12;

    public XJTableDelegate delegate;

    public int selectionRow = -1;
    public boolean ignoreSelectionEvent = false;

    public boolean allowEmptySelection = true;
    public boolean rememberSelection = false;
    public boolean autoresizeColumn = false;

    public XJTable() {
        super();
        init();
    }

    public XJTable(DefaultTableModel model) {
        super(model);
        init();
    }

    public void init() {
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting())
                    return;

                if(!ignoreSelectionEvent) {
                    setSelectedRow(getSelectedRow());
                    if(delegate != null)
                        delegate.tableSelectionChanged(XJTable.this, selectionRow);
                }
            }
        });
    }

    public void setDelegate(XJTableDelegate delegate) {
        this.delegate = delegate;
    }

    public void setAllowEmptySelection(boolean flag) {
        allowEmptySelection = flag;
    }

    public void setRememberSelection(boolean flag) {
        rememberSelection = flag;
    }

    public void setAutoresizeColumn(boolean flag) {
        autoresizeColumn = flag;
    }

    public void setSelectedRow(int row) {
        if(row == -1 && !allowEmptySelection)
            row = 0;

        selectionRow = Math.min(row, getRowCount()-1);
        if(selectionRow >= 0) {
            setRowSelectionInterval(selectionRow, selectionRow);
        }
    }

    public void reload() {
        ignoreSelectionEvent = true;
        try {
            DefaultTableModel model = (DefaultTableModel) getModel();
            model.fireTableDataChanged();

            setSelectedRow(selectionRow);
        } finally {
            ignoreSelectionEvent = false;
        }
    }

    public Insets getAutoscrollInsets() {
        Rectangle outer = getBounds();
        Rectangle inner = getParent().getBounds();

        return new Insets(inner.y-outer.y+margin, inner.x-outer.x+margin,
                outer.height-inner.height+margin,
                outer.width-inner.width+margin);
    }

    public void autoscroll(Point point) {
        int row = rowAtPoint(point);
        Rectangle outer = getBounds();
        Rectangle r2 = getCellRect(point.y+outer.y<=margin?row-1:row+1, 0, true);
        scrollRectToVisible(r2);
    }

    public void selectLastRow() {
        setSelectedRow(getRowCount()-1);
        scrollRectToVisible(getCellRect(getRowCount()-1, 0, true));
    }

}
