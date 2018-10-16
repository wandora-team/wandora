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
 * CreateTopicFieldProcessor.java
 *
 * Created on 24. marraskuuta 2004, 18:14
 */

package org.wandora.application.tools.extractors.datum;



import org.wandora.topicmap.*;
import java.util.*;


/**
 *
 * @author  olli
 */

public class CreateTopicFieldProcessor implements FieldProcessor {
    
    protected ExtractionHelper helper;
    protected boolean setBaseName;
    protected String topicType;
    /** Creates a new instance of CreateTopicFieldProcessor */
    public CreateTopicFieldProcessor(ExtractionHelper helper,boolean setBaseName) {
        this(helper,setBaseName,null);
    }
    public CreateTopicFieldProcessor(ExtractionHelper helper,boolean setBaseName,String topicType) {
        this.helper=helper;
        this.setBaseName=setBaseName;
        this.topicType=topicType;
    }
    
    public void processDatum(java.util.Map datum, String field, TopicMap tm, org.wandora.piccolo.Logger logger) throws ExtractionException {
        try{
            Collection<Topic> ts=helper.getOrCreateTopics(datum, field, tm,setBaseName);
            if(ts==null){
                logger.writelog("WRN","Null value for field "+field);
                return;
            }
            if(topicType!=null){
                Topic type=helper.getOrCreateTopic(tm,topicType);
                Iterator<Topic> iter=ts.iterator();
                while(iter.hasNext()){
                    Topic t=(Topic)iter.next();
                    t.addType(type);
                }
            }
        }
        catch(TopicMapException tme){
        	throw new ExtractionException(tme);
    	}            
    }
    
}
