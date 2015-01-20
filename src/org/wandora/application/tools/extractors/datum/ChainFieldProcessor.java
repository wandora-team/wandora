/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * ChainFieldProcessor.java
 *
 * Created on 3. joulukuuta 2004, 11:58
 */

package org.wandora.application.tools.extractors.datum;

import org.wandora.application.tools.extractors.*;

/**
 *
 * @author  olli
 */
public class ChainFieldProcessor implements FieldProcessor {
    
    protected FieldProcessor[] processors;
    
    /** Creates a new instance of ChainFieldProcessor */
    public ChainFieldProcessor(FieldProcessor[] processors) {
        this.processors=processors;
    }
    
    public void processDatum(java.util.Map datum, String field, org.wandora.topicmap.TopicMap tm, org.wandora.piccolo.Logger logger) throws ExtractionException {
        for(int i=0;i<processors.length;i++){
            processors[i].processDatum(datum,field, tm, logger);
        }
    }
    
}
