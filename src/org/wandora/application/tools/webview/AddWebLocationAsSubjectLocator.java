/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 */

package org.wandora.application.tools.webview;


import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class AddWebLocationAsSubjectLocator extends AbstractWebViewTool {

    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        String location = getWebLocation(context);
        Topic topic = getTopic(context);
        
        //System.out.println("@AddWebLocationAsSubjectLocator");
        //System.out.println("  location="+location);
        //System.out.println("  topic="+topic);
        
        if(topic != null && !topic.isRemoved()) {
            if(location != null && location.length() > 0) {
                topic.setSubjectLocator(new Locator(location));
            }
        }
    }
    
    
    @Override
    public String getDescription() {
        return "Adds current web location as a subject locator to the current topic.";
    }
    
}
