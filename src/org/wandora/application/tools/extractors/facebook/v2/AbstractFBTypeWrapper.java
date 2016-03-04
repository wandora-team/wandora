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
 */

package org.wandora.application.tools.extractors.facebook.v2;

import com.restfb.types.FacebookType;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Page;
import com.restfb.types.User;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */


public abstract class AbstractFBTypeWrapper extends AbstractFBWrapper {
    
    
    protected static <T> AbstractFBTypeWrapper create(Class<T> type, T object){
        AbstractFBTypeWrapper wrapper;
        if(type.equals(User.class)){
            wrapper = new UserWrapper((User)object);
        } else if (type.equals(Page.class)){
            wrapper = new PageWrapper((Page)object);
        } else {
            throw new IllegalArgumentException("Object type not supported.");
        }
        return wrapper;
    }
    
    // Default implementation
    @Override
    Topic mapToTopicMap(TopicMap tm) throws TopicMapException {
        FacebookType entity = getEnclosedEntity();

        if(entity == null) return null;
        
        String id = entity.getId();
        String name;
        if(entity instanceof NamedFacebookType){
            name = ((NamedFacebookType)entity).getName();
        } else {
            name = id;
        }
        
        logger.log("Processing graph object: " + name);
        
        Topic entityTopic = getOrCreateTopic(tm, getSIBase() + id, name + " (" + id + ")");
        
        Topic entityType = getOrCreateType(tm, getType());
        Topic facebookTopic = getFacebookTopic(tm);
        makeSubclassOf(tm, entityType, facebookTopic);
        
        entityTopic.addType(entityType);
        
        Topic idType = getOrCreateType(tm, "id");
        
        entityTopic.setData(idType, getLangTopic(tm), id);
        
        return entityTopic;
        
    }
    
    abstract FacebookType getEnclosedEntity();

}
