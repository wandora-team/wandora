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
 * 
 * LocatorTableSorter.java
 *
 * Created on 1. elokuuta 2006, 19:03
 *
 */

package org.wandora.application.gui.table;


import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.wandora.utils.swing.TableSorter;

/**
 *
 * @author akivela
 */
public class LocatorTableSorter extends TableSorter {

    private static final long serialVersionUID = 1L;
    
    
    public LocatorTableSorter() {
        super();
    }
    
    public LocatorTableSorter(TableModel tableModel) {
        super(tableModel);
    }

    public LocatorTableSorter(TableModel tableModel, JTableHeader tableHeader) {
        super(tableModel, tableHeader);
    }
    
}
