/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * 
 * SimpleTable.java
 *
 * Created on 14. lokakuuta 2005, 11:00
 */

package org.wandora.application.gui.simple;


import java.awt.*;
import javax.swing.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;


import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.utils.swing.anyselectiontable.AnySelectionTable;




/**
 *
 * @author akivela
 */
public class SimpleTable extends AnySelectionTable implements SimpleComponent {
    
    /** Creates a new instance of SimpleTable */
    public SimpleTable() {
        this.setFocusable(true);
        this.addFocusListener(this);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    
    
    
    @Override
    public void focusGained(java.awt.event.FocusEvent focusEvent) {
        Wandora w = Wandora.getWandora();
        if(w != null) {
            w.gainFocus(this);
        }
    }
    
    @Override
    public void focusLost(java.awt.event.FocusEvent focusEvent) {
    }
    
    
    
    @Override
    public void paint(Graphics g) {
        UIConstants.preparePaint(g);
        super.paint(g);
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // http://stackoverflow.com/questions/996948/live-sorting-of-jtable
    // -------------------------------------------------------------------------
    
    private UpdateHandler beforeSort;

    @Override
    public void sorterChanged(RowSorterEvent e) {
        super.sorterChanged(e);
        maybeRepaintOnSorterChanged(e);
    } 

    private void beforeUpdate(TableModelEvent e) {
        if (!isSorted()) return;
        beforeSort = new UpdateHandler(e);
    }

    private void afterUpdate() {
        beforeSort = null;
    }

    private void maybeRepaintOnSorterChanged(RowSorterEvent e) {
        if (beforeSort == null) return;
        if ((e == null) || (e.getType() != RowSorterEvent.Type.SORTED)) return;
        UpdateHandler afterSort = new UpdateHandler(beforeSort);
        if (afterSort.allHidden(beforeSort)) {
            return;
        } else if (afterSort.complex(beforeSort)) {
            repaint();
            return;
        }
        int firstRow = afterSort.getFirstCombined(beforeSort);
        int lastRow = afterSort.getLastCombined(beforeSort);
        Rectangle first = getCellRect(firstRow, 0, false);
        first.width = getWidth();
        Rectangle last = getCellRect(lastRow, 0, false);
        repaint(first.union(last));
    }

    private class UpdateHandler {
        private int firstModelRow;
        private int lastModelRow;
        private int viewRow;
        private boolean allHidden;

        public UpdateHandler(TableModelEvent e) {
            firstModelRow = e.getFirstRow();
            lastModelRow = e.getLastRow();
            convert();
        }

        public UpdateHandler(UpdateHandler e) {
            firstModelRow = e.firstModelRow;
            lastModelRow = e.lastModelRow;
            convert();
        }

        public boolean allHidden(UpdateHandler e) {
            return this.allHidden && e.allHidden;
        }

        public boolean complex(UpdateHandler e) {
            return (firstModelRow != lastModelRow);
        }

        public int getFirstCombined(UpdateHandler e) {
            if (allHidden) return e.viewRow;
            if (e.allHidden) return viewRow;
            return Math.min(viewRow, e.viewRow);
        }

        public int getLastCombined(UpdateHandler e) {
            if (allHidden || e.allHidden) return getRowCount() - 1;
            return Math.max(viewRow, e.viewRow);

        }

        private void convert() {
            // multiple updates
            if (firstModelRow != lastModelRow) {
                // don't bother too much - calculation not guaranteed to do anything good
                // just check if the all changed indices are hidden
                allHidden = true;
                for (int i = firstModelRow; i <= lastModelRow; i++) {
                    if (convertRowIndexToView(i) >= 0) {
                        allHidden = false;
                        break;
                    }
                }
                viewRow = -1;
                return;
            }
            // single update
            viewRow = convertRowIndexToView(firstModelRow);
            allHidden = viewRow < 0;
        }

    }

    private boolean isSorted() {
        // JW: not good enough - need a way to decide if there are any sortkeys which
        // constitute a sort or any effective filters  
        return getRowSorter() != null;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (isUpdate(e)) {
            beforeUpdate(e);
        }
        try {
            super.tableChanged(e);
        } finally {
            afterUpdate();
        }
    }

    /**
     * Convenience method to detect dataChanged table event type.
     * 
     * @param e the event to examine. 
     * @return true if the event is of type dataChanged, false else.
     */
    protected boolean isDataChanged(TableModelEvent e) {
        if (e == null) return false;
        return e.getType() == TableModelEvent.UPDATE && 
            e.getFirstRow() == 0 &&
            e.getLastRow() == Integer.MAX_VALUE;
    }

    /**
     * Convenience method to detect update table event type.
     * 
     * @param e the event to examine. 
     * @return true if the event is of type update and not dataChanged, false else.
     */
    protected boolean isUpdate(TableModelEvent e) {
        if (isStructureChanged(e)) return false;
        return e.getType() == TableModelEvent.UPDATE && 
            e.getLastRow() < Integer.MAX_VALUE;
    }

    /**
     * Convenience method to detect a structureChanged table event type.
     * @param e the event to examine.
     * @return true if the event is of type structureChanged or null, false else.
     */
    protected boolean isStructureChanged(TableModelEvent e) {
        return e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW;
    }

}
