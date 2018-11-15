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
 *
 */

package org.wandora.application.tools.occurrences;

import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;




/**
 * Open browser application and pass occurrence URL to it.
 *
 * @author akivela
 */
public class OpenURLOccurrence  extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;


	/** Creates a new instance of OpenURLOccurrence */
    public OpenURLOccurrence() {
    }
    public OpenURLOccurrence(Context proposedContext) {
        this.setContext(proposedContext);
    }


    @Override
    public String getName() {
        return "Open URL occurrence";
    }


    @Override
    public String getDescription() {
        return "Tool is used to fork external browser for URL occurrence.";
    }

    
    @Override
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        Object contextSource = context.getContextSource();
        
        if(contextSource instanceof OccurrenceTable) {
            OccurrenceTable ot = (OccurrenceTable) contextSource;
            ot.openURLOccurrence();
        }
    }   
    
    
    
}
