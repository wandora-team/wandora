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
 */
package org.wandora.topicmap;
import java.util.*;
/**
 *
 * @author olli
 */
public class TopicMapHashCode {
    public static long getTopicIdentifierCode(Topic t) throws TopicMapException {
        long h=0;
        for(Locator l : t.getSubjectIdentifiers()){
            h+=l.hashCode();
        }
        String bn=t.getBaseName();
        if(bn!=null) h+=bn.hashCode();
        if(h==0) return 1;
        return h;
    }
    public static long getScopeIdentifierCode(Collection<Topic> scope) throws TopicMapException {
        long h=0;
        for(Topic t : scope){
            h+=getTopicIdentifierCode(t);
        }
        if(h==0) return 1;
        return h;
    }
    public static long getTopicHashCode(Topic t) throws TopicMapException {
        long h=0;
        String bn=t.getBaseName();
        if(bn!=null) h+=bn.hashCode();
        Locator sl=t.getSubjectLocator();
        if(sl!=null) h+=sl.hashCode();
        for(Locator l : t.getSubjectIdentifiers()){
            h+=l.hashCode();
        }
        for(Set<Topic> scope : t.getVariantScopes()){
            String v=t.getVariant(scope);
            h+=getScopeIdentifierCode(scope)*v.hashCode();
        }
        return h;
    }
    public static long getAssociationHashCode(Association a) throws TopicMapException {
        long h=0;
        long typeCode=getTopicIdentifierCode(a.getType());
        h+=typeCode;
        for(Topic role : a.getRoles()){
            Topic player=a.getPlayer(role);
            h+=typeCode*getTopicIdentifierCode(role)*getTopicIdentifierCode(player);
        }
        return h;
    }
    public static long getTopicMapHashCode(TopicMap tm) throws TopicMapException {
        long h=0;
        Iterator<Topic> iter=tm.getTopics();
        while(iter.hasNext()){
            h+=getTopicHashCode(iter.next());
        }
        Iterator<Association> iter2=tm.getAssociations();
        while(iter2.hasNext()){
            h+=getAssociationHashCode(iter2.next());
        }
        return h;
    }
}
