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
import org.wandora.topicmap.memory.TopicMapImpl;

/**
 *
 * @author olli
 */
public class CreateTopicOperation extends UndoOperation {

    protected TopicMap tm;
    protected Locator si;
    protected TopicMap copytm;
    protected Topic copyt;

    public CreateTopicOperation(Topic t) throws TopicMapException, UndoException {
        tm=t.getTopicMap();
        copytm=new TopicMapImpl();
        copyt=copytm.copyTopicIn(t, true);
        si=copyt.getOneSubjectIdentifier();
        if(si==null) throw new UndoException("Topic doesn't have a subject identifier");
    }

    @Override
    public String getLabel() {
        return "create topic";
    }

    @Override
    public void redo() throws UndoException {
        try {
            Collection<Topic> merging=tm.getMergingTopics(copyt);
            if(!merging.isEmpty()) throw new UndoException();
            tm.copyTopicIn(copyt, true);
        }
        catch(TopicMapException tme) { 
            throw new UndoException(tme); 
        }
    }

    @Override
    public void undo() throws UndoException {
        try {
            Topic t=tm.getTopic(si);
            if(t==null) throw new UndoException();
            t.remove();
        } 
        catch(TopicMapException tme) { 
            throw new UndoException(tme); 
        }
    }
}
