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
import org.wandora.topicmap.memory.TopicMapImpl;


/**
 *
 * @author akivela
 */
public class AddSubjectIdentifierOperation extends UndoOperation {
    protected TopicMap tm;
    protected Locator si;
    protected Locator newSI;
    protected MergeOperation merge;
    protected boolean dummy=false;

    
    public AddSubjectIdentifierOperation(Topic t, Locator newSI) throws TopicMapException, UndoException {
        this.tm=t.getTopicMap();
        if(t.getSubjectIdentifiers().contains(newSI)) dummy=true;
        else {
            si=t.getOneSubjectIdentifier();
            if(si==null) throw new UndoException("Topic doesn't have a subject identifier");
            this.newSI=newSI;

            Topic t2=tm.getTopic(newSI);
            if(t2!=null && !t2.mergesWithTopic(t)) {
                merge=new MergeOperation(t, t2);
            }
        }
    }

    @Override
    public String getLabel() {
        return "Add Subject Identifier";
    }


    @Override
    public void undo() throws UndoException {
        if(dummy) return;
        try{
            Topic t = tm.getTopic(si);
            if(t==null) throw new UndoException();
            
            if(merge!=null) {
                merge.undo();
            }
            else t.removeSubjectIdentifier(newSI);
        }
        catch(TopicMapException tme) {
            throw new UndoException(tme);
        }
    }

    @Override
    public void redo() throws UndoException {
        if(dummy) return;
        try {
            Topic t=tm.getTopic(si);
            if(t==null) throw new UndoException();
            t.addSubjectIdentifier(newSI);
        }
        catch(TopicMapException tme) {
            throw new UndoException(tme);
        }
    }

    


}
