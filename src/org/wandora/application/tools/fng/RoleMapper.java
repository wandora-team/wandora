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
 * RoleMapper.java
 *
 * Created on August 24, 2004, 2:03 PM
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
public class RoleMapper {
    
    private String[] map;
    
    /** Creates a new instance of RoleMapper */
    public RoleMapper(String[] map) {
        this.map=map;
    }

    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        logger.writelog("Applying RoleMapper filter");
        
        if(map.length%3!=0){
            logger.writelog("WARNING: map.length mod 3 is not 0");
        }
        
        Topic[] mapT=new Topic[map.length];
        for(int i=0;i<map.length;i++){
            mapT[i]=tm.getTopic(map[i]);
            if(mapT[i]==null){
                logger.writelog("Couldn't find topic "+map[i]+". Aborting.");
                return tm;
            }
        }
        int counter=0;
        for(int i=0;i+2<mapT.length;i+=3){
            Topic atype=mapT[i];
            Topic role=mapT[i+1];
            Topic newRole=mapT[i+2];
            Iterator iter=new Vector(tm.getAssociationsOfType(atype)).iterator();
            while(iter.hasNext()){
                Association a=(Association)iter.next();
                if(a.isRemoved()) continue;
                Topic player=a.getPlayer(role);
                if(player==null) continue;
                a.removePlayer(role);
                a.addPlayer(player, newRole);
                counter++;
            }
        }
        logger.writelog("Mapped "+counter+" roles.");
        return tm;
    }    
}
