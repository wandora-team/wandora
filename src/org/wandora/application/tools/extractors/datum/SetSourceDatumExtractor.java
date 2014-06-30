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
 * SetSourceDatumExtractor.java
 *
 * Created on 3. joulukuuta 2004, 12:37
 */

package org.wandora.application.tools.extractors.datum;

/**
 *
 * @author  olli
 */
public class SetSourceDatumExtractor implements DatumExtractor {
    
    protected DatumExtractor nextDE;
    
    /** Creates a new instance of SetSourceDatumExtractor */
    public SetSourceDatumExtractor(DatumExtractor nextDE) {
        this.nextDE=nextDE;
    }
    
    public double getProgress() {
        return nextDE.getProgress();
    }
    
    public java.util.Map next(DataStructure data, org.wandora.piccolo.Logger logger) throws ExtractionException {
        java.util.Map map=nextDE.next(data,logger);
        map.put("source",data.handle.toString());
        return map;
    }
    
}
