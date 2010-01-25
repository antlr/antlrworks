package org.antlr.works.editor;

import org.antlr.works.ate.gutter.ATEGutterColumnManager;
import org.antlr.works.ate.gutter.ATEGutterItem;
import org.antlr.works.components.GrammarWindow;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.utils.IconManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
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

public class EditorGutterColumnManager extends ATEGutterColumnManager {

    private final GrammarWindow window;
    private final Map<Integer,ATEGutterItem> breakpoints = new HashMap<Integer,ATEGutterItem>();

    private static final String RULES = "rules";
    private static final String BREAKPOINTS = "breakpoints";

    private static final int DEFAULT_NO_BREAKPOINTS_WIDTH = 5; // to have enough room to click with the mouse if no breakpoints

    public EditorGutterColumnManager(GrammarWindow window) {
        super(window.textEditor);
        this.window = window;
    }

    @Override
    public String[] getColumns() {
        return new String[] {RULES, BREAKPOINTS};
    }

    public int getColumnWidth(String column) {
        int width = 0;
        if(column.equals(BREAKPOINTS) && breakpoints.isEmpty()) {
            width = DEFAULT_NO_BREAKPOINTS_WIDTH;
        }
        List<ATEGutterItem> items = getGutterItems(column);
        for (ATEGutterItem item : items) {
            width = Math.max(width, item.getItemWidth());
        }
        return width;
    }

    @Override
    public boolean handleClickInColumn(String column, int rowTextIndex) {
        if(column.equals(BREAKPOINTS)) {
            int line = window.getTextEditor().getLineIndexAtTextPosition(rowTextIndex);
            if(!breakpoints.containsKey(line)) {
                breakpoints.put(line, new BreakpointGutterItem(line));
                return true;
            }
        }
        return false;
    }

    public List<ATEGutterItem> getGutterItems(String column) {
        if(column.equals(RULES)) {
            List<ATEGutterItem> items = new ArrayList<ATEGutterItem>();
            List<ElementRule> rules = window.getGrammarEngine().getRules();
            if(rules != null) {
                for(ElementRule r : rules) {
                    items.add(r);
                }
            }
            return items;
        } else {
            List<Integer> sortedKeys = new ArrayList<Integer>(breakpoints.keySet());
            Collections.sort(sortedKeys);
            List<ATEGutterItem> sortedItems = new ArrayList<ATEGutterItem>();
            for(Integer k : sortedKeys) {
                sortedItems.add(breakpoints.get(k));
            }
            return sortedItems;
        }
    }

    public Set<Integer> getBreakpoints() {
        Set<Integer> lines = new HashSet<Integer>();
        for(ATEGutterItem item : getGutterItems(EditorGutterColumnManager.BREAKPOINTS)) {
            lines.add(((BreakpointGutterItem)item).line);
        }
        return lines;
    }

    private class BreakpointGutterItem implements ATEGutterItem {

        private int line;

        private BreakpointGutterItem(int line) {
            this.line = line;
        }

        public int getItemIndex() {
            Point index = window.getTextEditor().getLineTextPositionsAtLineIndex(line);
            return index.x;
        }

        public void setItemIndex(int index) {
            // todo handle newline/delete line
        }

        public List<Integer> getItemTypes() {
            return Arrays.asList(1);
        }

        public int getItemWidth() {
            int width = 0;
            for(int type : getItemTypes()) {
                width += getItemIcon(type).getIconWidth();
            }
            return width;
        }

        public int getItemHeight() {
            int height = 0;
            for(int type : getItemTypes()) {
                height = Math.max(height, getItemIcon(type).getIconHeight());
            }
            return height;
        }

        public ImageIcon getItemIcon(int type) {
            return IconManager.shared().getIconBreakpoint();
        }

        public String getItemTooltip(int type) {
            return "Breakpoint at line "+(line+1);
        }

        public void itemAction(int type) {
            breakpoints.remove(line);
        }
    }
}