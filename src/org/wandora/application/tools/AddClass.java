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
 * AddClass.java
 *
 * Created on 24. lokakuuta 2005, 20:04
 *
 */

package org.wandora.application.tools;


import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import java.util.*;

/**
 *
 * @author akivela
 */
public class AddClass extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	private boolean shouldRefresh = false;
    
    
    /** Creates a new instance of AddClass */
    public AddClass() {
    }
    public AddClass(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Add class";
    }

    @Override
    public String getDescription() {
        return "Add a class topic to selected topics.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        shouldRefresh = false;
        Iterator topics = context.getContextObjects();
        if(topics != null && topics.hasNext()) {
            Topic classTopic=wandora.showTopicFinder();
            
            Topic topic = null;
            if(classTopic != null) {
                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            shouldRefresh = true;
                            topic.addType(classTopic);
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
        }
    }
    
    
    @Override
    public boolean requiresRefresh() {
        return shouldRefresh;
    }
    
}
