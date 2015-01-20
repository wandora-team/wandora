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
 *
 * 
 * ReleaseNodesOfType.java
 *
 * Created on 6. kesäkuuta 2007, 15:40
 *
 */

package org.wandora.application.tools.graph.filters;


import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import org.wandora.application.tools.graph.*;
import org.wandora.topicmap.*;
import java.util.*;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.DummyTool;


/**
 *
 * @author akivela
 */
public class ReleaseNodesOfType extends AbstractGraphTool {
    
    
    
    private Topic type;
    private GraphFilter filter;
    
    public ReleaseNodesOfType(Topic type, NodeFilter filter) {
        this(type,(GraphFilter)filter);
    }
    
    public ReleaseNodesOfType(Topic type, GraphFilter filter) {
        this.setContext(new GraphEdgeContext());
        this.type=type;
        this.filter=filter;
    }
    
    
   
    
    @Override
    public String getName() {
        if(type != null) {
            return "Release nodes of type "+TopicToString.toString(type);
        }
        else {
            return "[No filtered nodes]";
        }
    }

    

    
    public void executeSynchronized(Wandora admin, Context context) {
        if(type != null && filter != null) {
            filter.releaseNodesOfType(type);
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
 
    
    public static ArrayList<AbstractGraphTool> makeTools(GraphFilter graphFilter, ArrayList<AbstractGraphTool> tools) {
        if(tools==null) tools=new ArrayList<AbstractGraphTool>();

        for(TopicNode tn : graphFilter.getFilteredNodeTypes()) {
            if(tn != null) {
                try {
                    Topic type = tn.getTopic();
                    if(type != null && !type.isRemoved()) {
                        tools.add(new ReleaseNodesOfType(type, graphFilter));
                    }
                }
                catch(Exception tme){
                    tme.printStackTrace();
                }
            }
        }
        if(tools.isEmpty()) {
            tools.add(new ReleaseNodesOfType(null, graphFilter));
        }

        return tools;
    }
    
}
