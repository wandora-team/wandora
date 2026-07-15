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
 * FilterNodesOfType.java
 *
 * Created on 6.6.2007, 15:40
 *
 */

package org.wandora.application.tools.graph.filters;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.contexts.GraphEdgeContext;
import org.wandora.application.gui.topicpanels.graphpanel.GraphFilter;
import org.wandora.application.gui.topicpanels.graphpanel.Node;
import org.wandora.application.gui.topicpanels.graphpanel.NodeFilter;
import org.wandora.application.gui.topicpanels.graphpanel.TopicNode;
import org.wandora.application.gui.topicpanels.graphpanel.VModel;
import org.wandora.application.gui.topicpanels.graphpanel.VNode;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.graph.AbstractGraphTool;
import org.wandora.application.tools.graph.CloseTopicNodesOfType;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;


/**
 *
 * @author olli
 */
public class FilterNodesOfType extends AbstractGraphTool {

	private static final long serialVersionUID = 1L;

	private Topic type;
    private GraphFilter filter;
    
    public FilterNodesOfType(Topic type,NodeFilter filter) {
        this(type,(GraphFilter)filter);
    }
    
    public FilterNodesOfType(Topic type,GraphFilter filter) {
        this.setContext(new GraphEdgeContext());
        this.type=type;
        this.filter=filter;
    }
    
    
   
    
    @Override
    public String getName(){
        try{
            return "Filter nodes of type "+TopicToString.toString(type);
        }
        catch(Exception tme){
            tme.printStackTrace(); 
            return "";
        }
    }

    

    
    public void executeSynchronized(Wandora wandora, Context context) {
        VModel model=solveModel(wandora,context);
        CloseTopicNodesOfType.hideTopicsOfType(type,model);
        filter.filterNodesOfType(type);
    }
    
    
    // -------------------------------------------------------------------------
    
    

    public static ArrayList<AbstractGraphTool> makeTools(VNode n, NodeFilter nodeFilter, ArrayList<AbstractGraphTool> tools){
        return makeTools(n==null?null:n.getNode(),nodeFilter,tools);
    }

    
    
    public static ArrayList<AbstractGraphTool> makeTools(Node n, NodeFilter nodeFilter, ArrayList<AbstractGraphTool> tools){
        if(tools==null) tools=new ArrayList<AbstractGraphTool>();
        if(n!=null && n instanceof TopicNode){
            TopicNode tn=(TopicNode)n;
            try {
                for(Topic type : tn.getTopic().getTypes()) {
                    tools.add(new FilterNodesOfType(type,nodeFilter));
                }
            } 
            catch(TopicMapException tme) {
                tme.printStackTrace();
            }
        }        
        return tools;
    }
    
    
    
    public static List<AbstractGraphTool> makeTools(Collection ns, NodeFilter nodeFilter, List<AbstractGraphTool> tools){
        if(tools==null) tools=new ArrayList<AbstractGraphTool>();
        ArrayList<Topic> oldTypes = new ArrayList<Topic>();
        for(Object o : ns) {
            if(o != null && o instanceof VNode) {
                o = ((VNode) o).getNode();
            }
            if(o != null && o instanceof TopicNode) {
                TopicNode tn=(TopicNode)o;
                try {
                    for(Topic type : tn.getTopic().getTypes()) {
                        if(!oldTypes.contains(type)) {
                            oldTypes.add(type);
                            tools.add(new FilterNodesOfType(type,nodeFilter));
                        }
                    }
                }
                catch(TopicMapException tme){
                    tme.printStackTrace();
                }
            }
        }
        return tools;
    }
    
}
