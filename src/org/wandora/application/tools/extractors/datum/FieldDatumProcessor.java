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
 * FieldDatumProcessor.java
 *
 * Created on 24. marraskuuta 2004, 16:25
 */

package org.wandora.application.tools.extractors.datum;
import org.wandora.application.tools.extractors.*;
import java.util.*;
/**
 *
 * @author  olli
 */
public class FieldDatumProcessor implements DatumProcessor {
    
    protected Map fieldMap;
    
    public FieldDatumProcessor(Map fieldMap){
        this.fieldMap=fieldMap;
    }
    
    
    public void processDatum(java.util.Map datum, org.wandora.topicmap.TopicMap tm, org.wandora.piccolo.Logger logger) throws ExtractionException {
        Iterator iter=datum.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            String field=(String)e.getKey();
            FieldProcessor fp=getFieldProcessor(field);
            if(fp==null) continue;
            fp.processDatum(datum, field,tm,logger);
        }
    }    
    
    public FieldProcessor getFieldProcessor(String field){
        return (FieldProcessor)fieldMap.get(field);
    }
}
