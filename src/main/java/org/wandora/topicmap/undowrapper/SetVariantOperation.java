/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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

package org.wandora.topicmap.undowrapper;

import java.util.HashSet;
import java.util.Set;

import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;


/**
 *
 * @author olli
 */
public class SetVariantOperation extends UndoOperation {
    protected TopicMap tm;
    protected Locator si;
    protected Set<Locator> scope;
    protected String oldValue;
    protected String newValue;



    public SetVariantOperation(Topic t,Set<Topic> scope,String value) throws TopicMapException, UndoException {
        this.tm=t.getTopicMap();
        si=t.getOneSubjectIdentifier();
        if(si==null) throw new UndoException("Topic has no subject identifier");
        this.scope=new HashSet<Locator>();
        for(Topic s : scope){
            Locator ssi=s.getOneSubjectIdentifier();
            if(ssi==null) throw new UndoException("Scope topic has no subject identifier");
            this.scope.add(ssi);
        }
        oldValue=t.getVariant(scope);
        newValue=value;
    }

    @Override
    public String getLabel() {
        return "variant";
    }

    @Override
    public void undo() throws UndoException {
        try{
            Topic t=tm.getTopic(si);
            if(t==null) throw new UndoException();
            HashSet<Topic> scope=new HashSet<Topic>();
            for(Locator ssi : this.scope){
                Topic st=tm.getTopic(ssi);
                if(st==null) throw new UndoException();
                scope.add(st);
            }

            if(oldValue==null) t.removeVariant(scope);
            else t.setVariant(scope,oldValue);

        }catch(TopicMapException tme){throw new UndoException(tme);}
    }

    @Override
    public void redo() throws UndoException {
        try{
            Topic t=tm.getTopic(si);
            if(t==null) throw new UndoException();
            HashSet<Topic> scope=new HashSet<Topic>();
            for(Locator ssi : this.scope){
                Topic st=tm.getTopic(ssi);
                if(st==null) throw new UndoException();
                scope.add(st);
            }

            if(newValue==null) t.removeVariant(scope);
            else t.setVariant(scope,newValue);
        }
        catch(TopicMapException tme){
            throw new UndoException(tme);
        }
    }

}
