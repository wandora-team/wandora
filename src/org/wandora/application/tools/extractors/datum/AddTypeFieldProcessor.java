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
 * AddTypeFieldProcessor.java
 *
 * Created on 3. joulukuuta 2004, 11:59
 */

package org.wandora.application.tools.extractors.datum;
import org.wandora.application.tools.extractors.*;
import org.wandora.topicmap.*;
import java.util.*;
/**
 *
 * @author  olli
 */
public class AddTypeFieldProcessor implements FieldProcessor {
    
    protected String target;
    protected String type;
    protected boolean reference;
    protected ExtractionHelper helper;
    
    /** Creates a new instance of AddTypeFieldProcessor */
    public AddTypeFieldProcessor(ExtractionHelper helper,String type,boolean reference) {
        this(helper,null,type,reference);
    }
    public AddTypeFieldProcessor(ExtractionHelper helper,String target,String type,boolean reference) {
        this.helper=helper;
        this.target=target;
        this.type=type;
        this.reference=reference;
    }
    
    public void processDatum(java.util.Map datum, String field, TopicMap tm, org.wandora.piccolo.Logger logger) throws ExtractionException {
        Collection types=null;
        String tar=target;
        try{
            if(tar==null) tar=field;
            if(reference){
                types=helper.getOrCreateTopics(datum,type,tm,false);
                if(types==null){
                    logger.writelog("WRN","Null value for field "+type);
                    return;
                }
            }
            else{
                types=new Vector();
                types.add(helper.getOrCreateTopic(tm,type));
            }
            Collection c=helper.getOrCreateTopics(datum,tar,tm,false);
            if(c==null){
                logger.writelog("WRN","Null value for field "+tar);
                return;
            }
            Iterator iter=c.iterator();
            while(iter.hasNext()){
                Topic t=(Topic)iter.next();
                Iterator iter2=types.iterator();
                while(iter2.hasNext()){
                    Topic type=(Topic)iter2.next();
                    t.addType(type);
                }
            }
        }catch(TopicMapException tme){
            throw new ExtractionException(tme);
        }
    }
    
}
