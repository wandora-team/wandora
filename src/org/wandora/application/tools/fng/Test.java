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
 * Test.java
 *
 * Created on September 6, 2004, 9:53 AM
 */

package org.wandora.application.tools.fng;
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
public class Test {
    
    /** Creates a new instance of Test */
    public Test() {
    }

    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        logger.writelog("Applying Test filter");
        Topic hasType=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#E55_Type");
        Iterator iter=tm.getTopics();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(tm.getTopicsOfType(t).size()>0) continue;
            if(t.getAssociations().size()==1){
                Association a=(Association)t.getAssociations().iterator().next();
                if(a.getType()==hasType){
                    logger.writelog(t.getOneSubjectIdentifier().toString());
                }
            }
        }
        return tm;
    }    
}
