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
 * Test.java
 *
 * Created on September 6, 2004, 9:53 AM
 */

package org.wandora.application.tools.fng;
import org.wandora.topicmap.TopicInUseException;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.*;
import org.wandora.*;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.utils.*;
import java.util.*;
import java.text.*;

/**
 *
 * @author  olli
 */
public class DeleteFNGTypeIfEmpty {
    
    public String[] types;
    
    /** Creates a new instance of Test */
    public DeleteFNGTypeIfEmpty(String[] types) {
        this.types=types;
    }

    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        logger.writelog("Applying DeleteFNGTypeIfEmpty filter");
        Topic hasType=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#E55_Type");
        if(hasType==null){
            logger.writelog("Couldn't find all needed topics.");
            return tm;
        }
        
        HashSet ts=new HashSet();
        for(int i=0;i<types.length;i++){
            Topic t=tm.getTopic(types[i]);
            if(t==null){
                logger.writelog("Couldn't find topic "+types[i]);
            }
            else ts.add(t);
        }
        
        Vector v=new Vector();
        Iterator iter=tm.getTopics();
        int counter=0;
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(tm.getTopicsOfType(t).size()>0) continue;
            Collection associations=t.getAssociations();
            if(associations.size()==1){
                Association a=(Association)t.getAssociations().iterator().next();
                if(a.getType()!=hasType) continue;
                
                Topic type=a.getPlayer(hasType);
                if(type!=null && ts.contains(type)){
                    v.add(t);
                }
                else{
                    logger.writelog(t.getOneSubjectIdentifier().toString());
                }
            }
        }
        iter=v.iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            try{
                t.remove();
                counter++;
            }catch(TopicInUseException tiue){}
        }
        logger.writelog("Deleted "+counter+" topics");
        return tm;
    }    
}
