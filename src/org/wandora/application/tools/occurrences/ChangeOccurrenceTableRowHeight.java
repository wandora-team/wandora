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
 */


package org.wandora.application.tools.occurrences;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.OccurrenceTable;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Options;

/**
 *
 * @author akivela
 */


public class ChangeOccurrenceTableRowHeight extends AbstractWandoraTool implements WandoraTool {
    private int rowHeight = 1;
    private Options localOptions = null;

    
    /** Creates a new instance of ChangeOccurrenceTableRowHeight */
    public ChangeOccurrenceTableRowHeight(int newRowHeight) {
        this.rowHeight = newRowHeight;
    }
    public ChangeOccurrenceTableRowHeight(int newRowHeight, Options options) {
        this.rowHeight = newRowHeight;
        this.localOptions = options;
    }

    
    @Override
    public String getName() {
        return "Change occurrence table row height";
    }
    
    @Override
    public String getDescription() {
        return "Change occurrence table row height.";
    }

    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        if(localOptions != null) {
            localOptions.put(OccurrenceTable.ROW_HEIGHT_OPTIONS_KEY, ""+rowHeight);
        }
        if(wandora != null) {
            Options ops = wandora.getOptions();
            ops.put(OccurrenceTable.ROW_HEIGHT_OPTIONS_KEY, ""+rowHeight);
        }
    }
    

}

