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
 * OccurrenceRemover.java
 *
 * Created on August 25, 2004, 9:42 AM
 */

package org.wandora.application.tools.fng;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
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
public class OccurrenceRemover {
    
    private String[] otypes;
    
    /** Creates a new instance of OccurrenceRemover */
    public OccurrenceRemover(String[] otypes) {
        this.otypes=otypes;
    }
    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        logger.writelog("Applying OccurrenceRemover filter");
        Topic[] ot=new Topic[otypes.length];
        for(int i=0;i<otypes.length;i++){
            ot[i]=tm.getTopic(otypes[i]);
            if(ot[i]==null){
                logger.writelog("Couldn't find topic "+otypes[i]+". Aborting.");
                return tm;
            }
        }
        int counter=0;
        Iterator iter=tm.getTopics();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            for(int i=0;i<ot.length;i++){
                if(!t.getData(ot[i]).isEmpty()){
                    t.removeData(ot[i]);
                    counter++;
                }
            }
        }
        logger.writelog("Removed "+counter+" occurrences.");
        return tm;
    }    
}
