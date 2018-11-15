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
 * ChangeOccurrenceView.java
 *
 * Created on 3. Maaliskuuta 2008
 *
 */
package org.wandora.application.tools.occurrences;

import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;

import org.wandora.utils.*;



/**
 * Changes Wandora application options to reflect user selected occurrence
 * view.
 * 
 * @author akivela
 */
public class ChangeOccurrenceView extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
	private String newView = OccurrenceTable.VIEW_SCHEMA;
    private Options localOptions = null;
    
    
    
    /** Creates a new instance of ChangeOccurrenceView */
    public ChangeOccurrenceView(String view) {
        this.newView = view;
    }
    public ChangeOccurrenceView(String view, Options options) {
        this.newView = view;
        this.localOptions = options;
    }
    
    
    @Override
    public String getName() {
        return "Change occurrence table view";
    }
    
    @Override
    public String getDescription() {
        return "Change occurrence table view.";
    }

    @Override
    public boolean requiresRefresh() {
        return true;
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        if(localOptions != null) {
            localOptions.put(OccurrenceTable.VIEW_OPTIONS_KEY, newView);
        }
        if(wandora != null) {
            Options ops = wandora.getOptions();
            ops.put(OccurrenceTable.VIEW_OPTIONS_KEY, newView);
        }
    }
    

}
