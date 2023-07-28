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
 *
 * 
 * CopyTopicInstances.java
 *
 * Created on 12. huhtikuuta 2006, 18:50
 *
 */

package org.wandora.application.tools;


import org.wandora.topicmap.TopicMapException;
import org.wandora.application.contexts.*;


/**
 * Copies instance topics of selected topics to clipboard.
 * 
 * @author akivela
 */



public class CopyTopicInstances extends CopyTopics {

	private static final long serialVersionUID = 1L;

	/** Creates a new instance of CopyTopicInstances */
    public CopyTopicInstances()  throws TopicMapException {
    }
    
    @Override
    public String getName() {
        return "Copy topic instances";
    }

    @Override
    public String getDescription() {
        return "Copies instance topics of selected topics to clipboard.";
    }

    @Override
    public void initialize(int copyOrders, int includeOrders) {
        super.initialize(copyOrders, includeOrders);
        setContext(new InstanceContext());
    }
    
}
