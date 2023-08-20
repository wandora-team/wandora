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
import org.tmapi.core.Occurrence;
import org.tmapi.core.TMAPIRuntimeException;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */


public class W2TOccurrence extends AbstractDatatypeAware implements Occurrence {

    protected W2TTopic t;
    protected W2TTopic type;
    protected W2TTopic language;
    
    public W2TOccurrence(W2TTopic t,W2TTopic type,W2TTopic language){
        this.t=t;
        this.type=type;
        this.language=language;
    }
    
    
    @Override
    public Topic getParent() {
        return t;
    }

    @Override
    public Topic getType() {
        return type;
    }

    @Override
    public void setType(Topic topic) {
        org.wandora.topicmap.Topic _topic=((W2TTopic)topic).getWrapped();
        org.wandora.topicmap.Topic _t=t.getWrapped();
        org.wandora.topicmap.Topic _type=type.getWrapped();
        org.wandora.topicmap.Topic _language=(language==null?null:language.getWrapped());
        
        try{
            String value=_t.getData(_type,_language);
            _t.removeData(_type, _language);
            _t.setData(_topic, _language, value);
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
        
        this.type=(W2TTopic)topic;
    }

    @Override
    public TopicMap getTopicMap() {
        return t.tm;
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
        throw new UnsupportedOperationException("Item identifiers not supported");
    }

    @Override
    public void removeItemIdentifier(Locator lctr) {
        throw new UnsupportedOperationException("Item identifiers not supported");
    }

    @Override
    public void remove() {
        org.wandora.topicmap.Topic _t=t.getWrapped();
        org.wandora.topicmap.Topic _type=type.getWrapped();
        org.wandora.topicmap.Topic _language=(language==null?null:language.getWrapped());
        
        try{
            _t.removeData(_type, _language);
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
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
    public Set<Topic> getScope() {
        HashSet<Topic> ret=new HashSet<Topic>();
        if(language!=null) ret.add(language);
        return ret;
    }

    /*
     Add Theme actually replaces the language topic instead of adding it. Remove
     theme does nothing if the topic isn't the language topic and throws an exception
     if it is. This guarantees that there is only a single topic in the scope at
     all times but still allows the topic to be changed. addTheme and removeTheme
     can still be called in succession as if to change a theme to another because
     after addTheme removeTheme will do nothing.
    */
    @Override
    public void addTheme(Topic theme) throws ModelConstraintException {
        
        org.wandora.topicmap.Topic _theme=((W2TTopic)theme).getWrapped();
        org.wandora.topicmap.Topic _t=t.getWrapped();
        org.wandora.topicmap.Topic _type=type.getWrapped();
        org.wandora.topicmap.Topic _language=(language==null?null:language.getWrapped());
        
        try{
            String value=_t.getData(_type,_language);
            _t.removeData(_type, _language);
            _t.setData(_type, _theme, value);
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
        
        this.language=(W2TTopic)theme;
        
    }

    @Override
    public void removeTheme(Topic topic) {
        try{
            if(this.language!=null && this.language.getWrapped().mergesWithTopic(((W2TTopic)topic).getWrapped())){
                throw new UnsupportedOperationException("Occurrences must have one theme topic");
            }
            // else do nothing, the topic isn't in the scope in the first place
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public String getValue() {
        try{
            return t.t.getData(type.t, language!=null?language.t:null);
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }
    @Override
    public void setValue(String s){
        try {
            t.t.setData(type.t, language!=null?language.t:null, s);
        }catch(TopicMapException tme){
            throw new RuntimeException(tme);
        }
    }
    
}
