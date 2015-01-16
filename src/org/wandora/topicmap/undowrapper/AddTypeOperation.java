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



package org.wandora.topicmap.undowrapper;

import org.wandora.topicmap.*;


/**
 *
 * @author olli
 */
public class AddTypeOperation extends UndoOperation {

    protected TopicMap tm;
    protected Locator si;
    protected Locator typeSi;
    protected boolean dummy=false;

    public AddTypeOperation(Topic t,Topic type) throws TopicMapException, UndoException {
        if(t.isOfType(type)){
            dummy=true;
            return;
        }

        tm=t.getTopicMap();
        si=t.getOneSubjectIdentifier();
        if(si==null) throw new UndoException("Topic doesn't have a subject identifier");
        typeSi=type.getOneSubjectIdentifier();
        if(typeSi==null) throw new UndoException("Type topic doesn't have a subject identifier");
    }

    @Override
    public String getLabel() {
        return "add type";
    }

    @Override
    public void redo() throws UndoException {
        if(dummy) return;
        try{
            Topic t=tm.getTopic(si);
            if(t==null) throw new UndoException();
            Topic type=tm.getTopic(typeSi);
            if(type==null) throw new UndoException();
            t.addType(type);
        }catch(TopicMapException tme){
            throw new UndoException(tme);
        }
    }

    @Override
    public void undo() throws UndoException {
        if(dummy) return;
        try {
            Topic t=tm.getTopic(si);
            if(t==null) throw new UndoException();
            Topic type=tm.getTopic(typeSi);
            if(type==null) throw new UndoException();
            t.removeType(type);
        } 
        catch(TopicMapException tme) {
            throw new UndoException(tme);
        }
    }

}
