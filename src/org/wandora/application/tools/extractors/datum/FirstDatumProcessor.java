/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * FirstDatumProcessor.java
 *
 * Created on 13. joulukuuta 2004, 11:13
 */

package org.wandora.application.tools.extractors.datum;

/**
 *
 * @author  olli
 */
public class FirstDatumProcessor implements DatumProcessor {
    
    protected boolean first;
    protected DatumProcessor firstProcessor;
    protected DatumProcessor allProcessor;
    
    
    /** Creates a new instance of FirstDatumProcessor */
    public FirstDatumProcessor(DatumProcessor firstProcessor,DatumProcessor allProcessor) {
        this.firstProcessor=firstProcessor;
        this.allProcessor=allProcessor;
        first=true;
    }
    
    public void processDatum(java.util.Map datum, org.wandora.topicmap.TopicMap tm, org.wandora.piccolo.Logger logger) throws ExtractionException {
        if(first){
            first=false;
            firstProcessor.processDatum(datum, tm, logger);
        }
        allProcessor.processDatum(datum, tm, logger);
    }
    
}
