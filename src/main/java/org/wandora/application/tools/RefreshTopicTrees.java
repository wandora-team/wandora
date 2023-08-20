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
 * 
 * RefreshTopicTrees.java
 *
 * Created on 7. huhtikuuta 2006, 14:20
 *
 */

package org.wandora.application.tools;


import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.TopicMapException;


/**
 *
 * @author olli
 */
public class RefreshTopicTrees extends AbstractWandoraTool {

	
	private static final long serialVersionUID = 1L;


	/** Creates a new instance of RefreshTopicTrees */
    public RefreshTopicTrees() {
    }

    @Override
    public String getName() {
        return "Refresh topic trees";
    }

    @Override
    public String getDescription() {
        return "Refresh topic trees.";
    } 
    
   
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {      
        wandora.refreshTopicTrees();
    }


}
