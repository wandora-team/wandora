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
 * 
 * CloseTopicNodesOfType.java
 *
 * Created on 7. kesäkuuta 2007, 13:09
 *
 */

package org.wandora.application.tools.graph;


import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import org.wandora.topicmap.*;
import java.util.*;

/**
 *
 * @author olli
 */
public class CloseTopicNodesOfType extends AbstractGraphTool {
    
    private Topic type;
    private GraphFilter filter;
    
    
    public CloseTopicNodesOfType(TopicMapGraphPanel gp, Topic type, NodeFilter filter) {
        this(gp, type,(GraphFilter)filter);
    }
    
    
    public CloseTopicNodesOfType(TopicMapGraphPanel gp, Topic type, GraphFilter filter) {
        super(gp);
        this.setContext(new GraphEdgeContext());
        this.type=type;
        this.filter=filter;
    }


    
    @Override
    public String getName(){
        try{
            return "Hide nodes of type "+type.getBaseName();
        }catch(TopicMapException tme){tme.printStackTrace(); return "";}
    }
    
    public static void hideTopicsOfType(Topic type,VModel model){
        HashSet<VNode> remove=new HashSet<VNode>();
        try{
            for(VNode vnode : model.getNodes()){
                Node n=vnode.getNode();
                if(n instanceof TopicNode){
                    TopicNode tn=(TopicNode)n;
                    if(tn.getTopic().isOfType(type)) remove.add(vnode);
                }
            }
        }catch(TopicMapException tme){tme.printStackTrace();}
        for(VNode vnode : remove){
            model.removeNode(vnode);
        }        
    }

    
    public void executeSynchronized(Wandora admin, Context context) {
        VModel model = null;
        VNode node = null;
        for(Iterator iter = context.getContextObjects(); iter.hasNext(); ) {
            try {
                node = (VNode) iter.next();
                if(node != null) {
                    model = node.getModel();
                    if(model != null) {
                        hideTopicsOfType(type,model);
                    }
                }
            }
            catch(Exception e) {
                singleLog(e);
            }
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public static ArrayList<AbstractGraphTool> makeTools(TopicMapGraphPanel gp, Node n, NodeFilter nodeFilter, ArrayList<AbstractGraphTool> tools){
        if(tools==null) tools=new ArrayList<AbstractGraphTool>();
        if(n instanceof TopicNode){
            TopicNode tn=(TopicNode)n;
            try{
                for(Topic type : tn.getTopic().getTypes()){
                    tools.add(new CloseTopicNodesOfType(gp,type,nodeFilter));
                }
            }catch(TopicMapException tme){tme.printStackTrace();}
        }        
        return tools;
    }
}
