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

import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;


/**
 *
 * @author olli
 */


public class SetSubjectLocatorOperation extends UndoOperation {
    protected TopicMap tm;
    protected Locator si;
    protected Locator oldSL;
    protected Locator newSL;
    protected MergeOperation merge;
    protected boolean nop=false;
    
    public SetSubjectLocatorOperation(Topic t,Locator newSL) throws TopicMapException, UndoException {
        if(t.getSubjectLocator()!=null && newSL!=null && t.getSubjectLocator().equals(newSL)){
            nop=true;
            return;
        }
        
        this.tm=t.getTopicMap();
        si=t.getOneSubjectIdentifier();
        if(si==null) throw new UndoException("Topic doesn't have a subject identifier");
        oldSL=t.getSubjectLocator();
        this.newSL=newSL;

        if(newSL!=null){
            Topic t2=t.getTopicMap().getTopicBySubjectLocator(newSL);
            if(t2!=null) merge=new MergeOperation(t, t2);
        }
    }

    @Override
    public String getLabel() {
        return "subject locator";
    }


    @Override
    public void undo() throws UndoException {
        if(nop) return;
        try{
            if(merge!=null){
                merge.undo();
            }
            else {
                Topic t=tm.getTopic(si);
                if(t==null) throw new UndoException();
                if(oldSL!=null){
                    Topic t2=tm.getTopicBySubjectLocator(oldSL);
                    if(t2!=null && !t2.mergesWithTopic(t)) throw new UndoException();
                }
                t.setSubjectLocator(oldSL);
            }
        }catch(TopicMapException tme){throw new UndoException(tme);}
    }

    @Override
    public void redo() throws UndoException {
        if(nop) return;
        try{
            Topic t=tm.getTopic(si);
            if(t==null) throw new UndoException();
            
            if(newSL!=null && merge==null){
                // t2 existing is fine if this was supposed to be a merge,
                // otherwise something's wrong
                Topic t2=tm.getTopicBySubjectLocator(newSL);
                if(t2!=null) throw new UndoException();
            }
            // this may result in a merge but that's fine, the topicmap itself
            // will handle that correctly.
            t.setSubjectLocator(newSL);
        }catch(TopicMapException tme){throw new UndoException(tme);}
    }

    
}
