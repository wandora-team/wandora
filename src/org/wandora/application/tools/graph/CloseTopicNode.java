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
 * CloseNodeTopic.java
 *
 * Created on 7. kes�kuuta 2007, 11:24
 *
 */

package org.wandora.application.tools.graph;


import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import java.util.*;


/**
 *
 * @author olli, akivela
 */
public class CloseTopicNode extends AbstractGraphTool {
    

	private static final long serialVersionUID = 1L;

	
	private boolean allButCurrent;
    
    public CloseTopicNode(TopicMapGraphPanel gp) {
        this(gp, false);
    }
    public CloseTopicNode(TopicMapGraphPanel gp, boolean allButCurrent) {
        super(gp);
        this.allButCurrent=allButCurrent;
        this.setContext(new GraphNodeContext(gp));
    }
    
    
    
    @Override
    public String getName(){
        if(allButCurrent) return "Close all topic nodes but this";
        else return "Close topic node";
    }
    
    
    @Override
    public void executeSynchronized(Wandora wandora, Context context) {
        VModel model = null;
        VNode node = null;
        if(!allButCurrent){
            for(Iterator iter = context.getContextObjects(); iter.hasNext(); ) {
                try {
                    node = (VNode) iter.next();
                    if(node != null) {
                        model = node.getModel();
                        if(model != null) {
                            model.removeNode(node);
                        }
                    }
                }
                catch(Exception e) {
                    singleLog(e);
                }
            }
        }
        else{
            HashSet<VNode> selected=new HashSet<VNode>();
            for(Iterator iter = context.getContextObjects(); iter.hasNext(); ) {
                try {
                    node = (VNode) iter.next();
                    if(node != null) selected.add(node);
                }
                catch(Exception e) {
                    singleLog(e);
                }
            }
            model=solveModel(wandora, context);
            ArrayList<VNode> all=new ArrayList<VNode>(model.getNodes());
            for(VNode vn : all){
                if(!selected.contains(vn)) model.removeNode(vn);
            }
        }
    }
}
