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
 *
 * 
 *
 */


package org.wandora.application.tools.occurrences;

import java.util.Iterator;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.contexts.LayeredTopicContext;
import org.wandora.application.gui.FreeOccurrencePrompt;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class EditOccurrences extends AbstractWandoraTool {
    private Topic masterTopic = null;
    private Topic occurrenceType = null;
    
    
    /** Creates a new instance of EditOccurrences */
    public EditOccurrences(Topic occurrenceType, Topic topic) {
        this.masterTopic = topic;
        this.occurrenceType = occurrenceType;
        setContext(new LayeredTopicContext());
    }

    public EditOccurrences(Context preferredContext,  Topic occurrenceType, Topic topic ) {
        this.masterTopic = topic;
        this.occurrenceType = occurrenceType;
        setContext(preferredContext);
    }

    @Override
    public String getName() {
        return "Edit occurrences";
    }

    @Override
    public String getDescription() {
        return "Open occurrence editor for editing context occurrences.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException  {
        if(masterTopic != null && !masterTopic.isRemoved() && occurrenceType != null && !occurrenceType.isRemoved()) {
            FreeOccurrencePrompt d=new FreeOccurrencePrompt(wandora, masterTopic, occurrenceType);
            d.setVisible(true);
        }
    }
    
}
