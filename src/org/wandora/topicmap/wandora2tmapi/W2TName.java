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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.tmapi.core.Locator;
import org.tmapi.core.ModelConstraintException;
import org.tmapi.core.Name;
import org.tmapi.core.TMAPIRuntimeException;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;
import org.tmapi.core.Variant;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */


public class W2TName implements Name {
    
    protected W2TTopic t;
    protected W2TTopicMap tm;
    
    public W2TName(W2TTopicMap tm,W2TTopic t){
        this.tm=tm;
        this.t=t;
    }

    @Override
    public Topic getParent() {
        return t;
    }

    @Override
    public String getValue() {
        try{
            return t.t.getBaseName();
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public void setValue(String string) throws ModelConstraintException {
        try{
            t.getWrapped().setBaseName(string);
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Set<Variant> getVariants() {
        try{
            Set<Set<org.wandora.topicmap.Topic>> scopes=t.t.getVariantScopes();
            
            HashSet<Variant> ret=new HashSet<Variant>();
            
            for(Set<org.wandora.topicmap.Topic> scope : scopes){
                ret.add(new W2TVariant(this,scope));
            }
            
            return ret;
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Variant createVariant(String string, Topic... scope) throws ModelConstraintException {
        return createVariant(string,tm.createLocator(W2TTopicMap.TYPE_STRING_SI),scope);
    }

    @Override
    public Variant createVariant(String string, Collection<Topic> scope) throws ModelConstraintException {
        return createVariant(string,tm.createLocator(W2TTopicMap.TYPE_STRING_SI),scope.toArray(new Topic[0]));
    }

    @Override
    public Variant createVariant(Locator lctr, Topic... scope) throws ModelConstraintException {
        throw new UnsupportedOperationException("Only string variants are supported");
    }

    @Override
    public Variant createVariant(Locator lctr, Collection<Topic> scope) throws ModelConstraintException {
        throw new UnsupportedOperationException("Only string variants are supported");
    }

    @Override
    public Variant createVariant(String string, Locator datatype, Topic... scope) throws ModelConstraintException {
        
        org.wandora.topicmap.Topic _t=t.getWrapped();
        
        HashSet<org.wandora.topicmap.Topic>_scope=new HashSet<>();
        for(Topic s : scope){
            _scope.add(((W2TTopic)s).getWrapped());
        }
        
        try{
            _t.setVariant(_scope, string);
            return new W2TVariant(this, _scope);
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Variant createVariant(String string, Locator datatype, Collection<Topic> scope) throws ModelConstraintException {
        return createVariant(string,datatype,scope.toArray(new Topic[0]));
    }

    @Override
    public Topic getType() {
        return tm.getTopicBySubjectIdentifier(W2TTopicMap.TOPIC_NAME_SI);
    }

    @Override
    public void setType(Topic topic) {
        throw new UnsupportedOperationException("Name type cannot be changed");
    }

    @Override
    public TopicMap getTopicMap() {
        return tm;
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
    }

    @Override
    public void remove() {
        // This doesn't actually remove variants from the wandora model.
        // See the notes about variants and null base names in W2TTopic.java
        try{
            t.getWrapped().setBaseName(null);
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Set<Topic> getScope() {
        return new HashSet<Topic>();
    }

    @Override
    public void addTheme(Topic topic) throws ModelConstraintException {
        throw new UnsupportedOperationException("Scope not supported in names");
    }

    @Override
    public void removeTheme(Topic topic) {
    }

    @Override
    public Topic getReifier() {
        return null;
    }

    @Override
    public void setReifier(Topic topic) throws ModelConstraintException {
        throw new UnsupportedOperationException("Reification not supported");
    }
    
}
