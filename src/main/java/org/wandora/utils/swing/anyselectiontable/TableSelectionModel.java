/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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




import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
  * This class represents the current state of the selection of
  * the AnySelectionTable.
  * It keeps the information in a list of ListSelectionModels, where
  * each Model represents the selection of a column.
  * @author Jan-Friedrich Mutter (jmutter@bigfoot.de)
  */
  // This class is added to the Table as aPropertyChangeListener. It
  //  will be noticed when the Table has a new TableModel. So it can
  //  adjust itself to the new TableModel and can add itself
  //  as a TableModelListener to the new TableModel.
  // This class is added to the TableModel as a TableModelListener to
  //  be noticed when the TableModel changes. The number of
  //  ListSelectionModels in this class must be the same as the
  //  number of rows in the TableModel.
  // This class implements a ListSelectionListener and is added to
  //  all its ListSelectionModels. If this class
  //  changes one of its ListSelectionModels, the ListSelectionModel
  //  fires a new ListSelectionEvent which is received by this class.
  //  After receiving that event, this class fires a TableSelectionEvent.
  //  That approach saves me from calculating 'firstIndex' and 'lastIndex'
  //  for myself and makes sure that a TableSelectionEvent is fired
  //  whenever the selection changes.
public class TableSelectionModel implements PropertyChangeListener, ListSelectionListener, TableModelListener {

    /** List of Listeners which will be notified when the selection value changes */
    protected EventListenerList listenerList = new EventListenerList();
    /** contains a ListSelectionModel for each column */
    protected ArrayList listSelectionModels = new ArrayList();

  
  
  
    public TableSelectionModel() {
    }
    

    /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
    public void addSelection(int row, int column) {
        ListSelectionModel lsm = getListSelectionModelAt(column);
        lsm.addSelectionInterval(row, row);
    }

    /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
    public void setSelection(int row, int column) {
        ListSelectionModel lsm = getListSelectionModelAt(column);
        lsm.setSelectionInterval(row, row);
    }

    /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
    public void setSelectionInterval(int row1, int row2, int column) {
        ListSelectionModel lsm = getListSelectionModelAt(column);
        lsm.setSelectionInterval(row1, row2);
    }

    /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
    public void setLeadSelectionIndex(int row, int column) {
        ListSelectionModel lsm = getListSelectionModelAt(column);
        if (lsm.isSelectionEmpty())
            lsm.setSelectionInterval(row, row);
        else
            //calling that method throws an IndexOutOfBoundsException when selection is empty (?, JDK 1.1.8, Swing 1.1)
            lsm.setLeadSelectionIndex(row);
    }

    /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
    public void removeSelection(int row, int column) {
        ListSelectionModel lsm = getListSelectionModelAt(column);
        lsm.removeSelectionInterval(row, row);
    }

    /**
    * Calls clearSelection() of all ListSelectionModels.
    */
    public void clearSelection() {
        for(Iterator enu=listSelectionModels.iterator(); enu.hasNext();) {
            ListSelectionModel lm = (ListSelectionModel)(enu.next());
            lm.clearSelection();
        }
    }

    /**
    * @return true, if the specified cell is selected.
    */
    public boolean isSelected(int row, int column) {
        ListSelectionModel lsm = getListSelectionModelAt(column);
        if(lsm != null) return lsm.isSelectedIndex(row);
        return false;
    }

  
    /**
    * Returns the ListSelectionModel at the specified column
    * @param index the column
    */
    public ListSelectionModel getListSelectionModelAt(int index) {
        if(index < listSelectionModels.size())
            return (ListSelectionModel)(listSelectionModels.get(index));
        else
            return null;
    }

    /**
    * Set the number of columns.
    * @param count the number of columns
    */
    public void setColumns(int count) {
        listSelectionModels = new ArrayList();
        for (int i=0; i<count; i++) {
            addColumn();
        }
    }

    /**
    * Add a column to the end of the model.
    */
    protected void addColumn() {
        DefaultListSelectionModel newListModel = new DefaultListSelectionModel();
        listSelectionModels.add(newListModel);
        newListModel.addListSelectionListener(this);
    }

    /**
    * Remove last column from model.
    */
    protected void removeColumn() {
        //get last element
        DefaultListSelectionModel removedModel = (DefaultListSelectionModel)listSelectionModels.get(listSelectionModels.size()-1);
        removedModel.removeListSelectionListener(this);
        listSelectionModels.remove(removedModel);
    }

    /**
    * When the TableModel changes, the TableSelectionModel
    * has to adapt to the new Model. This method is called
    * if a new TableModel is set to the JTable.
    */
    // implements PropertyChangeListener
    public void propertyChange(PropertyChangeEvent evt) {
        if ("model".equals(evt.getPropertyName())) {
            TableModel newModel = (TableModel)(evt.getNewValue());
            setColumns(newModel.getColumnCount());
            TableModel oldModel = (TableModel)(evt.getOldValue());
            if (oldModel != null)
                oldModel.removeTableModelListener(this);
            //TableSelectionModel must be aware of changes in the TableModel
            newModel.addTableModelListener(this);
        }
    }

    /**
    * Add a listener to the list that's notified each time a
    * change to the selection occurs.
    */
    public void addTableSelectionListener(TableSelectionListener l) {
        listenerList.add(TableSelectionListener.class, l);
    }

    /**
    * Remove a listener from the list that's notified each time a
    * change to the selection occurs.
    */
    public void removeTableSelectionListener(TableSelectionListener l) {
        listenerList.remove(TableSelectionListener.class, l);
    }

    /**
    * Is called when the TableModel changes. If the number of columns
    * had changed this class will adapt to it.
    */
    //implements TableModelListener
    public void tableChanged(TableModelEvent e) {
        TableModel tm = (TableModel)e.getSource();
        int count = listSelectionModels.size();
        int tmCount = tm.getColumnCount();
        //works, because you can't insert columns into a TableModel (only add/romove(?)):
        //if columns were removed from the TableModel
        while (count-- > tmCount) {
            removeColumn();
        }
        //count == tmCount if was in the loop, else count < tmCount
        //if columns were added to the TableModel
        while (tmCount > count++) {
            addColumn();
        }
    }

    /**
    * Is called when the selection of a ListSelectionModel
    * of a column has changed.
    * @see #fireValueChanged(Object source, int firstIndex, int lastIndex, int columnIndex, boolean isAdjusting)
    */
    //implements ListSelectionListener
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        int columnIndex = listSelectionModels.lastIndexOf(lsm);
        if (columnIndex > -1) {
            fireValueChanged(this, e.getFirstIndex(), e.getLastIndex(), columnIndex, e.getValueIsAdjusting());
        }
    }

    /**
    * Notify listeners that we have ended a series of adjustments.
    */
    protected void fireValueChanged(Object source, int firstIndex, int lastIndex, int columnIndex, boolean isAdjusting) {
        Object[] listeners = listenerList.getListenerList();
        TableSelectionEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TableSelectionListener.class) {
                if (e == null) {
                    e = new TableSelectionEvent(source, firstIndex, lastIndex, columnIndex, false);
                } 
                ((TableSelectionListener)listeners[i+1]).valueChanged(e);
            }
        }
    }

    
    
    @Override
    public String toString() {
        String ret = "[\n";
        for (int col=0; col<listSelectionModels.size(); col++) {
            ret += "\'"+col+"\'={";
            ListSelectionModel lsm = getListSelectionModelAt(col);
            int startRow = lsm.getMinSelectionIndex();
            int endRow = lsm.getMaxSelectionIndex();
            for(int row=startRow; row<endRow; row++) {
            if(lsm.isSelectedIndex(row))
                ret += row + ", ";
            }
            if(lsm.isSelectedIndex(endRow))
                ret += endRow;
            ret += "}\n";
        }
        ret += "]";
        /*String ret = "";
        for (int col=0; col<listSelectionModels.size(); col++) {
        ret += "\'"+col+"\'={"+getListSelectionModelAt(col)+"}";
        }*/
        return ret;
    }

}
