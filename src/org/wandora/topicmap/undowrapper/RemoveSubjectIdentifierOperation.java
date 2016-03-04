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

import java.util.Collection;
import org.wandora.topicmap.*;

/**
 *
 * @author olli
 */


public class RemoveSubjectIdentifierOperation extends UndoOperation{
    protected TopicMap tm;
    protected Locator si;
    protected Locator otherSi;
    protected boolean dummy=false;

    public RemoveSubjectIdentifierOperation(Topic t,Locator si) throws TopicMapException, UndoException {
        this.tm=t.getTopicMap();
        this.si=si;
        Collection<Locator> sis=t.getSubjectIdentifiers();
        if(!sis.contains(si)) {dummy=true; return;}
        for(Locator s : sis){
            if(!s.equals(si)) {
                this.otherSi=s;
                break;
            }
        }
        if(otherSi==null) throw new UndoException("Topic only has one subject identifier which is about to be removed");
    }
    
    @Override
    public String getLabel() {
        return "Remove Subject Identifier";
    }

    @Override
    public void undo() throws UndoException {
        if(dummy) return;
        try {
            Topic t=tm.getTopic(otherSi);
            if(t==null) throw new UndoException();
            Topic t2=tm.getTopic(si);
            if(t2!=null) throw new UndoException();
            t.addSubjectIdentifier(si);
        } 
        catch(TopicMapException tme) {
            throw new UndoException(tme);
        }
    }

    @Override
    public void redo() throws UndoException {
        if(dummy) return;
        try {
            Topic t=tm.getTopic(otherSi);
            if(t==null) throw new UndoException();
            if(!t.getSubjectIdentifiers().contains(si)) throw new UndoException();
            t.removeSubjectIdentifier(si);
        }
        catch(TopicMapException tme) {
            throw new UndoException(tme);
        }
    }
    
}
