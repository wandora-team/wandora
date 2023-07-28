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
 * TopicTableSorter.java
 *
 * Created on 1. elokuuta 2006, 19:03
 *
 */

package org.wandora.application.gui.table;


import org.wandora.utils.swing.TableSorter;
import org.wandora.utils.swing.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 *
 * @author akivela
 */
public class TopicTableSorter extends TableSorter {

    
    public TopicTableSorter() {
        super();
    }
    
    public TopicTableSorter(TableModel tableModel) {
        super(tableModel);
    }

    public TopicTableSorter(TableModel tableModel, JTableHeader tableHeader) {
        super(tableModel, tableHeader);
    }
    
}
