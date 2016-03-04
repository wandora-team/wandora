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

import java.util.Set;
import org.wandora.topicmap.*;

/**
 *
 * @author olli
 */
public class SetOccurrenceOperation extends UndoOperation {

    protected TopicMap tm;
    protected Locator si;
    protected Locator typeSi;
    protected Locator versionSi;
    protected String oldValue;
    protected String newValue;

    public SetOccurrenceOperation(Topic t,Topic type,Topic version,String value) throws TopicMapException, UndoException {
        this.tm=t.getTopicMap();
        si=t.getOneSubjectIdentifier();
        if(si==null) throw new UndoException("Topic has no subject identifier");
        typeSi=type.getOneSubjectIdentifier();
        if(typeSi==null) throw new UndoException("Type topic has no subject identifier");
        versionSi=version.getOneSubjectIdentifier();
        if(versionSi==null) throw new UndoException("Version topic has no subject identifier");
        oldValue=t.getData(type, version);
        newValue=value;
    }

    @Override
    public String getLabel() {
        return "occurrence";
    }

    @Override
    public void undo() throws UndoException {
        try{
            Topic t=tm.getTopic(si);
            if(t==null) throw new UndoException();
            Topic type=tm.getTopic(typeSi);
            if(type==null) throw new UndoException();
            Topic version=tm.getTopic(versionSi);
            if(version==null) throw new UndoException();

            if(oldValue==null) t.removeData(type,version);
            else t.setData(type, version,oldValue);
        }catch(TopicMapException tme){throw new UndoException(tme);}
    }

    @Override
    public void redo() throws UndoException {
        try{
            Topic t=tm.getTopic(si);
            if(t==null) throw new UndoException();
            Topic type=tm.getTopic(typeSi);
            if(type==null) throw new UndoException();
            Topic version=tm.getTopic(versionSi);
            if(version==null) throw new UndoException();

            if(newValue==null) t.removeData(type, version);
            else t.setData(type, version,newValue);
        }
        catch(TopicMapException tme){
            throw new UndoException(tme);
        }
    }


}
