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
 * Created on Oct 19, 2011, 8:12:21 PM
 */
package org.wandora.application.gui.topicpanels.treemap;

import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.topicmap.Topic;

/**
 *
 * @author elias, akivela
 */


public class TopicInfo extends MapItem {
    public String name;
    public String type;
    public int instances;
    public Topic t;
    public Rect rectArea;
    
    

    public TopicInfo(Topic t, int instances, int order, int depth) {
        //this(t, instances, order, depth, TYPE_INSTANCE);
        this(t, instances, order, depth, "");
    }

    public TopicInfo(Topic t, int instances, int order, int depth, String type) {
        this.rectArea = null;
        this.type = type;
        this.t = t;
        this.name = getTopicInfoString(t);
        this.instances = instances;
        this.order = order;
        this.size = instances+1;
        this.bounds = new Rect();
        this.depth = depth;
    }
    
    
    
    private String getTopicInfoString(Topic t) {
        try {
            return TopicToString.toString(t);
        }
        catch(Exception e) {
            return "[error]";
        }
    }
}