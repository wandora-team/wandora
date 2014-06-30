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
 * FilterEdges.java
 *
 * Created on 7. kesäkuuta 2007, 12:06
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
import static org.wandora.utils.Tuples.*;



/**
 *
 * @author olli
 */
public class FilterEdges extends AbstractGraphTool  {
    
    public static final int FILTER_EDGES_WITH_TYPE = 1;
    public static final int FILTER_INSTANCE_EDGES = 2;
    public static final int FILTER_OCCURRENCE_EDGES = 3;
    
    
    private int filterType = FILTER_EDGES_WITH_TYPE;
    private Topic edgeType;
    private GraphFilter filter;
    
    
    
    public FilterEdges(int filterType, Topic edgeType, NodeFilter filter) {
        this(filterType, edgeType, (GraphFilter)filter);
    }
    
    /**
     */
    public FilterEdges(int filterType, Topic edgeType, GraphFilter filter) {
        this.setContext(new GraphEdgeContext());
        this.edgeType = edgeType;
        this.filterType = filterType;
        this.filter = filter;
    }
    
    
    

    @Override
    public String getName(){
        try {
            switch(filterType) {
                case FILTER_OCCURRENCE_EDGES: return "Filter Occurrence edges";
                case FILTER_INSTANCE_EDGES: return "Filter Class-Instance edges";
                case FILTER_EDGES_WITH_TYPE: return "Filter edges of type "+TopicToString.toString(edgeType);
            }
        }
        catch(Exception tme){
            tme.printStackTrace(); 
        }
        return "";
    }
    
    
    
    public static void hideNodesOfType(int filterType, Topic edgeType, VModel model) {
        HashSet<VEdge> remove=new HashSet<VEdge>();
        try {
            for(VEdge vedge : model.getEdges()){
                Edge e=vedge.getEdge();
                if(filterType==FILTER_INSTANCE_EDGES) {
                    if(e instanceof InstanceEdge) {
                        remove.add(vedge);
                    }
                }
                if(filterType==FILTER_OCCURRENCE_EDGES) {
                    if(e instanceof OccurrenceEdge) {
                        remove.add(vedge);
                    }
                }
                else {
                    if(e instanceof AssociationEdge){
                        AssociationEdge ae=(AssociationEdge)e;
                        Association a=ae.getAssociation();
                        if(a.getType().mergesWithTopic(edgeType)) {
                            remove.add(vedge);
                        }
                    }
                }
            }
        }
        catch(TopicMapException tme) {
            tme.printStackTrace();
        }
        for(VEdge vedge : remove){
            model.removeEdge(vedge);
            T2<VNode,VNode> ns=vedge.getNodes();
            if(ns.e1.getEdges().isEmpty()) model.removeNode(ns.e1);
            if(ns.e2.getEdges().isEmpty()) model.removeNode(ns.e2);
        }        
    }
    
    
    public void executeSynchronized(Wandora admin, Context context) {
        VModel model = solveModel(admin,context);
        hideNodesOfType(filterType, edgeType, model);
        switch(filterType) {
            case FILTER_OCCURRENCE_EDGES: { filter.setFilterOccurrences(true); break; }
            case FILTER_INSTANCE_EDGES: { filter.setFilterInstances(true); break; }
            default: { filter.filterEdgeType(edgeType); break; }
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public static ArrayList<AbstractGraphTool> makeTools(VEdge e,GraphFilter graphFilter,ArrayList<AbstractGraphTool> tools){
        return makeTools(e==null?null:e.getEdge(),graphFilter,tools);
    }
    
    public static ArrayList<AbstractGraphTool> makeTools(Edge e,GraphFilter graphFilter,ArrayList<AbstractGraphTool> tools){
        if(tools==null) tools=new ArrayList<AbstractGraphTool>();
        if(e!=null){
            if(e instanceof AssociationEdge){
                AssociationEdge ae=(AssociationEdge)e;
                Association a=ae.getAssociation();
                try {
                    tools.add(new FilterEdges(FILTER_EDGES_WITH_TYPE, a.getType(), graphFilter));
                }
                catch(TopicMapException tme) {
                    tme.printStackTrace();
                }
            }
            else if(e instanceof InstanceEdge){
                tools.add(new FilterEdges(FILTER_INSTANCE_EDGES, null, graphFilter));
            }
            else if(e instanceof OccurrenceEdge){
                tools.add(new FilterEdges(FILTER_OCCURRENCE_EDGES, null, graphFilter));
            }
        }        
        return tools;
    }
    
    
    
    public static ArrayList<AbstractGraphTool> makeTools(Collection c,GraphFilter graphFilter,ArrayList<AbstractGraphTool> tools){
        if(tools==null) tools=new ArrayList<AbstractGraphTool>();
        ArrayList<Topic> types = new ArrayList<Topic>();
        boolean instanceAdded = false;
        boolean occurrenceAdded = false;
        for(Object o : c) {
            if(o != null) {
                if(o instanceof VEdge) {
                    o = ((VEdge) o).getEdge();
                    if(o == null) continue;
                }
                if(o instanceof AssociationEdge) {
                    AssociationEdge ae=(AssociationEdge)o;
                    Association a=ae.getAssociation();
                    try {
                        Topic type = a.getType();
                        if(!types.contains(type)) {
                            types.add(type);
                            tools.add(new FilterEdges(FILTER_EDGES_WITH_TYPE, type, graphFilter));
                        }
                    }
                    catch(TopicMapException tme){tme.printStackTrace();}
                }
                else if(o instanceof InstanceEdge) {
                    if(!instanceAdded) {
                        instanceAdded = true;
                        tools.add(new FilterEdges(FILTER_INSTANCE_EDGES, null, graphFilter));
                    }
                }
                else if(o instanceof OccurrenceEdge) {
                    if(!occurrenceAdded) {
                        occurrenceAdded = true;
                        tools.add(new FilterEdges(FILTER_OCCURRENCE_EDGES, null, graphFilter));
                    }
                }
            }
        }
        return tools;
    }
    
}
