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
 * AppendOccurrence.java
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



/**
 *
 * @author akivela
 */
public class AppendOccurrence extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;


	public AppendOccurrence() {
    }
    public AppendOccurrence(Context proposedContext) {
        this.setContext(proposedContext);
    }

    @Override
    public String getName() {
        return "Append occurrence";
    }

    @Override
    public String getDescription() {
        return "Appends clipboard text to given occurrence.";
    }

    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        Object source = getContext().getContextSource();
        if(source instanceof OccurrenceTable) {
            OccurrenceTable ot = (OccurrenceTable) source;
            ot.append();
        }
    }

}