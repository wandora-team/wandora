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
 * TopicMapTypeManager.java
 *
 * Created on 17. maaliskuuta 2006, 15:45
 */

package org.wandora.topicmap;
import org.wandora.topicmap.memory.*;
import org.wandora.topicmap.database.*;
import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.remote.*;
import org.wandora.topicmap.query.*;
import org.wandora.topicmap.linked.*;
import org.wandora.topicmap.undowrapper.UndoTopicMap;
import org.wandora.topicmap.webservice.*;
/**
 * <p>
 * A class that manages all TopicMapTypes. In particular, has methods to
 * get the TopicMapType of a specific class extending TopicMap or an instance
 * of such a class.
 * </p>
 * <p>
 * Note that currently the getType methods have hard coded mapping for following
 * topic map implementations: TopicMapImpl, RemoteTopicMap, DatabaseTopicMap
 * and LayerStack. This means that there is no way to register new topic map
 * implementations. In future this class should read or automatically detect
 * all implementations.
 * </p>
 *
 * @author olli
 */
public class TopicMapTypeManager {
    
    /** Creates a new instance of TopicMapTypeManager */
    public TopicMapTypeManager() {
    }
    
    /**
     * Get the TopicMapType from a TopicMap.
     */
    public static TopicMapType getType(TopicMap tm) {
        if(tm == null) return null;
        if(tm.getClass().equals(UndoTopicMap.class)) {
            tm = ((UndoTopicMap) tm).getWrappedTopicMap();
        }
        return getType(tm.getClass());
    }
    /**
     * Get the TopicMapType from a topic map implementing class.
     */
    public static TopicMapType getType(Class<? extends TopicMap> c){
        if(c==LayerStack.class) return new LayeredTopicMapType();
        else if(c==DatabaseTopicMap.class) return new DatabaseTopicMapType();
        else if(c==org.wandora.topicmap.database2.DatabaseTopicMap.class) return new org.wandora.topicmap.database2.DatabaseTopicMapType();
        else if(c==TopicMapImpl.class) return new MemoryTopicMapType();
        else if(c==RemoteTopicMap.class) return new RemoteTopicMapType();
        else if(c==QueryTopicMap.class) return new QueryTopicMapType();
        else if(c==LinkedTopicMap.class) return new LinkedTopicMapType();
        else if(c==LayerStack.class) return new LayeredTopicMapType();
        else if(c==WebServiceTopicMap.class) return new WebServiceTopicMapType();
        else return null;
    }
}
