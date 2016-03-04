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
 * 
 *
 * AbstractTopicFilter.java
 *
 * Created on 19. heinäkuuta 2005, 9:33
 */

package org.wandora.piccolo.actions;
import org.wandora.topicmap.*;
import org.wandora.topicmap.TMBox;
import java.util.*;
/**
 *
 * @author olli
 */
public abstract class AbstractTopicFilter implements TopicFilter {
    
    public abstract boolean topicVisible(Topic t) throws TopicMapException ;
    public abstract String getFilterCacheKey();
    public static boolean associationVisible(Association a,TopicFilter filter) throws TopicMapException {
        if(!filter.topicVisible(a.getType())) return false;
        Iterator roles=a.getRoles().iterator();
        while(roles.hasNext()){
            Topic role=(Topic)roles.next();
            if(!filter.topicVisible(role)) return false;
            if(!filter.topicVisible(a.getPlayer(role))) return false;
        }
        return true;                
    }
    public boolean associationVisible(Association a) throws TopicMapException {
        return associationVisible(a,this);
    }
    public TopicFilter makeNew(Object request) {
        return this;
    }

    public Collection filterTopics(Collection topics) throws TopicMapException {
        return filterTopics(topics,this);
    }
    public Collection filterAssociations(Collection associations) throws TopicMapException {
        return filterAssociations(associations,this);
    }
    
    
    public static Collection filterTopics(Collection topics,TopicFilter filter) throws TopicMapException {
        Vector v=new Vector();
        Iterator iter=topics.iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(filter.topicVisible(t)) v.add(t);
        }
        return v;
    }
    public static Collection filterAssociations(Collection associations,TopicFilter filter) throws TopicMapException {
        Vector v=new Vector();
        Iterator iter=associations.iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            if(filter.associationVisible(a)) v.add(a);
        }
        return v;
    }
    
}
