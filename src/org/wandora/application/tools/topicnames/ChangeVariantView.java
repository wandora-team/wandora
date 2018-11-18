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
 * ChangeVariantView.java
 *
 * Created on 2008-11-14
 *
 */


package org.wandora.application.tools.topicnames;

import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.topicpanels.*;
import org.wandora.application.contexts.*;


import org.wandora.utils.*;


/**
 * Changes Wandora application options to reflect user selected variant name
 * view.
 *
 * @author akivela
 */
public class ChangeVariantView extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	private String viewType = OccurrenceTable.VIEW_SCHEMA;
    private Options localOptions = null;
    
    
    /** Creates a new instance of ChangeVariantView */
    public ChangeVariantView(String type) {
        viewType = type;
    }
    public ChangeVariantView(String type, Options localOpts) {
        viewType = type;
        localOptions = localOpts;
    }

    @Override
    public String getName() {
        return "Change variant name table view";
    }
    
    @Override
    public String getDescription() {
        return "Change variant name table view.";
    }

    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        if(localOptions != null) {
            localOptions.put(TraditionalTopicPanel.VARIANT_GUITYPE_OPTIONS_KEY, viewType);
        }
        if(wandora != null) {
            Options ops = wandora.getOptions();
            ops.put(TraditionalTopicPanel.VARIANT_GUITYPE_OPTIONS_KEY, viewType);
            //System.out.println("ops == " + ops.get(TraditionalTopicPanel.VARIANT_GUITYPE_OPTIONS_KEY));
        }
    }
    
    @Override
    public boolean requiresRefresh() {
        return true;
    }
    
    

}
