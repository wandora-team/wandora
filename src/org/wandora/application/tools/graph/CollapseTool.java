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
 * CollapseTool.java
 *
 * Created on 6. kes�kuuta 2007, 15:21
 *
 */

package org.wandora.application.tools.graph;


import org.wandora.application.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import java.util.*;
import static org.wandora.utils.Tuples.*;
import org.wandora.application.contexts.*;


/**
 *
 * @author olli
 */
public class CollapseTool extends AbstractGraphTool {

	private static final long serialVersionUID = 1L;

	private int depth;
    
    public CollapseTool(TopicMapGraphPanel gp) {
        this(gp, 1);
    }
    public CollapseTool(TopicMapGraphPanel gp, int depth) {
        super(gp);
        this.setContext(new GraphNodeContext());
        this.depth=depth;
    }
    
    
    
    @Override
    public String getName(){
        if(depth==1) return "Collapse node";
        else return "Collapse nodes "+depth+" links deep";
    }
    
    public void collapse(VNode vn,VModel model, HashSet<VNode> processed,int depth){
        processed.add(vn);
        
        if(depth>1){
            for(VEdge edge : vn.getEdges()){
                T2<VNode,VNode> ns=edge.getNodes();
                VNode other=null;
                if(ns.e1.equals(vn)) other=ns.e2;
                else if(ns.e2.equals(vn)) other=ns.e1;
                if(!processed.contains(other)) collapse(other,model,processed,depth-1);
            }
        }
        model.collapseNode(vn);
    }
    
    
    @Override
    public void executeSynchronized(Wandora wandora, Context context) {
        if(context != null) {
            VNode vn = null;
            for(Iterator iter=context.getContextObjects(); iter.hasNext(); ) {
                try {
                    vn = (VNode) iter.next();
                    //TopicMapGraphPanel panel = vn.getPanel();
                    VModel model = vn.getModel();
                    if(vn != null) {
                        if(depth==1) model.collapseNode(vn);
                        else {
                            collapse(vn,model,new HashSet<VNode>(),depth);
                        }
                    }
                }
                catch (Exception e) {
                    singleLog(e);
                }
            }
        }
    }
    
}