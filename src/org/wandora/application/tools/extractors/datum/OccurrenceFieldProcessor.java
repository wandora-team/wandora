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
 * OccurrenceFieldProcessor.java
 *
 * Created on 24. marraskuuta 2004, 18:20
 */

package org.wandora.application.tools.extractors.datum;


import org.wandora.topicmap.*;
import java.util.*;


/**
 *
 * @author  olli
 */
public class OccurrenceFieldProcessor implements FieldProcessor {
    
    protected ExtractionHelper helper;
    protected String targetField;
    protected String occurrenceType;
    protected String occurrenceVersion;
    protected String value;
    
    /** Creates a new instance of OccurrenceFieldProcessor */
    public OccurrenceFieldProcessor(ExtractionHelper helper,String targetField,String occurrenceType,String occurrenceVersion) {
        this(helper,targetField,occurrenceType,occurrenceVersion,null);
    }
    public OccurrenceFieldProcessor(ExtractionHelper helper,String targetField,String occurrenceType,String occurrenceVersion,String value) {
        this.helper=helper;
        this.targetField=targetField;
        this.occurrenceType=occurrenceType;
        this.occurrenceVersion=occurrenceVersion;
        this.value=value;
    }
    
    public void processDatum(java.util.Map datum, String field, org.wandora.topicmap.TopicMap tm, org.wandora.piccolo.Logger logger) throws ExtractionException {
        try{
            Collection targetts=helper.getOrCreateTopics(datum, targetField, tm);
            if(targetts==null){
                logger.writelog("WRN","Null value for field "+targetField);
                return;
            }
            Object o=datum.get(field);
            Collection vals;
            if(value!=null) {vals=new Vector(); ((Vector)vals).add(value); }
            else{
                if(o instanceof String){vals=new Vector(); ((Vector)vals).add(o);}
                else vals=(Collection)o;
            }

            Iterator iter=vals.iterator();
            while(iter.hasNext()){
                String val=(String)iter.next();
                val=val.trim();
                Iterator iter2=targetts.iterator();
                while(iter2.hasNext()){
                    Topic targett=(Topic)iter2.next();
                    targett.setData(helper.getOrCreateTopic(tm,occurrenceType),helper.getOrCreateTopic(tm,occurrenceVersion),val);
                }
            }
        }catch(TopicMapException tme){throw new ExtractionException(tme);}
        
    }
    
}
