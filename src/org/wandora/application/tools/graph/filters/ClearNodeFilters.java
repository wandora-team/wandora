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
 * ClearNodeFilters.java
 *
 * Created on 21. elokuuta 2007, 17:01
 *
 */

package org.wandora.application.tools.graph.filters;

import org.wandora.application.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import org.wandora.application.tools.graph.*;
import org.wandora.application.contexts.*;

/**
 *
 * @author akivela
 */
public class ClearNodeFilters extends AbstractGraphTool {
    GraphFilter filter = null;
    
    
    
    @Override
    public String getName() {
        return "Clear all node filters";
    }
    
    @Override
    public String getDescription() {
        return "Clear all node filters.";
    }
    
    
    
    
    /** Creates a new instance of ClearNodeFilters */
    public ClearNodeFilters(GraphFilter filter) {
        this.filter = filter;
    }

    public void executeSynchronized(Wandora admin, Context context) {
        try {
            filter.clearNodeFilters();
            filter.clearNodeTypeFilters();
        }
        catch (Exception e) {
            singleLog(e);
        }
    }
}

