/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * SelectColumns.java
 *
 * Created on 12. huhtikuuta 2006, 10:21
 *
 */

package org.wandora.application.tools.selections;

import org.wandora.application.gui.table.TopicTable;
import org.wandora.application.*;
import org.wandora.application.gui.table.SITable;
import org.wandora.application.gui.table.TopicGrid;


/**
 * Implements <code>WandoraAdminTool</code> used to select 
 * topic table columns. Class has no defined behavior in any other context.
 *
 * @author akivela
 */

public class SelectColumns extends DoSelection {
    
    
    /** Creates a new instance of SelectColumns */
    public SelectColumns() {
    }
 
    
    
    @Override
    public void doTableSelection(Wandora wandora, SITable siTable) {
        siTable.selectColumns();
    }
    
    @Override
    public void doTableSelection(Wandora admin, TopicTable table) {
        table.selectColumns();
    }
    
    @Override
    public void doGridSelection(Wandora wandora, TopicGrid grid) {
        grid.selectColumns();
    }
    
    
    @Override
    public String getName() {
        return "Select columns in topic table";
    }
    
}
