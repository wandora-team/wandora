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
 */



package org.wandora.topicmap.undowrapper;

import org.wandora.topicmap.*;


/**
 *
 * @author olli
 */
public class SetBaseNameOperation extends UndoOperation {
    protected TopicMap tm;
    protected Locator si;
    protected String oldName;
    protected String newName;
    protected MergeOperation merge;

    public SetBaseNameOperation(Topic t,String newName) throws TopicMapException, UndoException {
        this.tm=t.getTopicMap();
        si=t.getOneSubjectIdentifier();
        if(si==null) throw new UndoException("Topic doesn't have a subject identifier");
        oldName=t.getBaseName();
        this.newName=newName;

        if(newName!=null){
            Topic t2=t.getTopicMap().getTopicWithBaseName(newName);
            if(t2!=null && !t2.mergesWithTopic(t)){
                merge=new MergeOperation(t, t2);
            }
        }
    }

    @Override
    public String getLabel() {
        return "base name";
    }


    @Override
    public void undo() throws UndoException {
        try{
            if(merge!=null) {
                merge.undo();
            }
            else {
                Topic t=tm.getTopic(si);
                if(t==null) throw new UndoException();
                if(oldName!=null){
                    Topic t2=tm.getTopicWithBaseName(oldName);
                    if(t2!=null && !t2.mergesWithTopic(t)) throw new UndoException();
                }
                t.setBaseName(oldName);
            }
        }
        catch(TopicMapException tme){
            throw new UndoException(tme);
        }
    }

    
    
    
    @Override
    public void redo() throws UndoException {
        try {
            Topic t=tm.getTopic(si);
            if(t==null) throw new UndoException();
            /*
            if(newName!=null){
                Topic t2=tm.getTopicWithBaseName(newName);
                if(t2!=null) throw new UndoException();
            }
            */
            t.setBaseName(newName);
        }
        catch(TopicMapException tme) {
            throw new UndoException(tme);
        }
    }



}
