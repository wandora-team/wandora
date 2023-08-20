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
 * ExpandNodesRecursivelyTool.java
 *
 * Created on 12.6.2007, 11:17
 *
 */

package org.wandora.application.tools.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.contexts.GraphNodeContext;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;
import org.wandora.application.gui.topicpanels.graphpanel.VEdge;
import org.wandora.application.gui.topicpanels.graphpanel.VModel;
import org.wandora.application.gui.topicpanels.graphpanel.VNode;
import org.wandora.topicmap.Topic;
import org.wandora.utils.Tuples.T2;



/**
 *
 * @author olli, akivela
 */

public class ExpandNodesRecursivelyTool extends AbstractGraphTool {

	private static final long serialVersionUID = 1L;

	private int depth;
    private HashSet<VNode> openNodes = null;
    
    
    /** Creates a new instance of ExpandNodesRecursivelyTool */
    public ExpandNodesRecursivelyTool(TopicMapGraphPanel gp) {
        super(gp);
        this.setContext(new GraphNodeContext());
        depth=-1;
    }
    public ExpandNodesRecursivelyTool(TopicMapGraphPanel gp, int depth) {
        super(gp);
        this.setContext(new GraphNodeContext());
        this.depth=depth;
    }
    
    @Override
    public String getName(){
        return "Recursive expand tool";
    }
    
    public void expandRecursive(VNode node,VModel model,int depth){
        if(!openNodes.contains(node)) {
            openNodes.add(node);
            model.openNode(node);
            if(depth>0){
                for(VEdge e : node.getEdges()){
                    T2<VNode,VNode> ns=e.getNodes();
                    if(!ns.e1.equals(node)) expandRecursive(ns.e1,model,depth-1);
                    if(!ns.e2.equals(node)) expandRecursive(ns.e2,model,depth-1);
                }
            }
        }
    }

    public void executeSynchronized(Wandora wandora, Context context) {}
    
    @Override
    public void execute(Wandora wandora, Context context){
        if(context != null) {
            Iterator contextObjects = context.getContextObjects();
            if(!contextObjects.hasNext()) {
                ArrayList<Topic> adhocContext = new ArrayList<>();
                adhocContext.add( wandora.getOpenTopic() );
                contextObjects = adhocContext.iterator();
            }
            if(contextObjects.hasNext()) {
                int d=depth;
                if(d==-1){
                    String in=WandoraOptionPane.showInputDialog(wandora,"Enter recursion depth");
                    try {
                        d=Integer.parseInt(in);
                    }
                    catch(NumberFormatException nfe){}
                    if(d<0) {
                        WandoraOptionPane.showMessageDialog(wandora,"Invalid depth");
                        return;
                    }
                }
                try {
                    synchronized(solveGraphPanel(wandora,context)) {
                        VNode node = null;
                        VModel model = null;
                        openNodes = new LinkedHashSet<>();
                        while(contextObjects.hasNext()) {
                            node = (VNode) contextObjects.next();
                            if(node != null) {
                                model = node.getModel();
                                if(model != null) {
                                    expandRecursive(node,node.getModel(),d);
                                }
                            }

                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
                openNodes = null; // Release the node set for garbage collector;
            }
        }
    }    
    
}
