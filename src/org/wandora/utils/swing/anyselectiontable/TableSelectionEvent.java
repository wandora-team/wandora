/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 */

package org.wandora.utils.swing.anyselectiontable;



import javax.swing.event.ListSelectionEvent;

/**
  * An event that characterizes a change in the current selection.
  * @author Jan-Friedrich Mutter (jmutter@bigfoot.de)
  */
public class TableSelectionEvent extends ListSelectionEvent {

    /**
    * The index of the column whose selection has changed.
    */
    protected int columnIndex;

    public TableSelectionEvent(Object source, int firstRowIndex, int lastRowIndex, int columnIndex, boolean isAdjusting) {
	super(source, firstRowIndex, lastRowIndex, isAdjusting);
        this.columnIndex = columnIndex;
    }
    

    /**
    * Returns the index of the column whose selection has changed.
    * @return The last column whose selection value has changed, where zero is the first column.
    */
    public int getColumnIndex() {
        return columnIndex;
    }
}
