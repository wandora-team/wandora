/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2013 Wandora Team
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
 */
package org.wandora.topicmap.wandora2tmapi;

import java.util.HashSet;
import java.util.Set;

import org.tmapi.core.Locator;
import org.tmapi.core.ModelConstraintException;
import org.tmapi.core.Name;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;
import org.tmapi.core.Variant;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */


public class W2TVariant extends AbstractDatatypeAware implements Variant {

    protected W2TName name;
    protected W2TTopic t;
    protected Set<org.wandora.topicmap.Topic> scope;
    
    public W2TVariant(W2TName name,Set<org.wandora.topicmap.Topic> scope){
        this.name=name;
        this.t=name.t;
        this.scope=scope;
    }
    
    @Override
    public Name getParent() {
        return name;
    }

    @Override
    public Set<Topic> getScope() {
        return t.tm.wrapTopics(scope);
    }

    @Override
    public Topic getReifier() {
        return null;
    }

    @Override
    public void setReifier(Topic topic) throws ModelConstraintException {
        throw new UnsupportedOperationException("Reification not supported");
    }

    @Override
    public TopicMap getTopicMap() {
        return t.getTopicMap();
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public Set<Locator> getItemIdentifiers() {
        return new HashSet<Locator>();
    }

    @Override
    public void addItemIdentifier(Locator lctr) throws ModelConstraintException {
        throw new UnsupportedOperationException("Item identfiers not supported");
    }

    @Override
    public void removeItemIdentifier(Locator lctr) {
        throw new UnsupportedOperationException("Item identifiers not supported");
    }

    @Override
    public void remove() {
        try{
            t.getWrapped().removeVariant(scope);
        }catch(TopicMapException tme){
            throw new RuntimeException(tme);
        }
    }

    @Override
    public void addTheme(Topic topic) throws ModelConstraintException {
        try{
            org.wandora.topicmap.Topic _topic=((W2TTopic)topic).getWrapped();
            boolean found=false;
            for(org.wandora.topicmap.Topic s : scope){
                if(s.mergesWithTopic(_topic)) {
                    found=true;
                    break;
                }
            }
            if(found) return;
            
            HashSet<org.wandora.topicmap.Topic> newScope=new HashSet<>(scope);
            newScope.add(_topic);
            
            String value=t.getWrapped().getVariant(scope);
            t.getWrapped().removeVariant(scope);
            t.getWrapped().setVariant(newScope, value);
            scope=newScope;
        }catch(TopicMapException tme){
            throw new RuntimeException(tme);
        }
    }

    @Override
    public void removeTheme(Topic topic) {
        try{
            HashSet<org.wandora.topicmap.Topic> newScope=new HashSet<>(scope);
            org.wandora.topicmap.Topic _topic=((W2TTopic)topic).getWrapped();
            boolean found=false;
            for(org.wandora.topicmap.Topic s : scope){
                if(s.mergesWithTopic(_topic)) {
                    found=true;
                }
                else newScope.add(s);
            }
            if(!found) return;
            
            String value=t.getWrapped().getVariant(scope);
            t.getWrapped().removeVariant(scope);
            t.getWrapped().setVariant(newScope, value);
            scope=newScope;
        }catch(TopicMapException tme){
            throw new RuntimeException(tme);
        }
    }

    @Override
    public String getValue() {
        try{
            return t.t.getVariant(scope);
        }catch(TopicMapException tme){
            throw new RuntimeException(tme);
        }
    }
    
    @Override
    public void setValue(String s){
        try {
            t.t.setVariant(scope, s);
        }catch(TopicMapException tme){
            throw new RuntimeException(tme);
        }
    }
    
}
