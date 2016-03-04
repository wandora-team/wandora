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
 * CutOccurrence.java
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
import org.wandora.*;



/**
 *
 * @author akivela
 */
public class CutOccurrence  extends AbstractWandoraTool implements WandoraTool {
    
    
    
    /** Creates a new instance of CutOccurrence */
    public CutOccurrence() {
    }
    public CutOccurrence(Context proposedContext) {
        this.setContext(proposedContext);
    }


    @Override
    public String getName() {
        return "Cut occurrence";
    }

    @Override
    public String getDescription() {
        return "Copy occurrence text to clipboard and delete occurrence.";
    }

    
    @Override
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        Object source = getContext().getContextSource();
        if(source instanceof OccurrenceTable) {
            OccurrenceTable ot = (OccurrenceTable) source;
            ot.cut();
        }
    }


}
