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
 * ReleaseEdges.java
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
 * @author akivela
 */
public class ReleaseEdges extends AbstractGraphTool  {
    
    public static final int FILTER_EDGES_WITH_TYPE = 1;
    public static final int FILTER_INSTANCE_EDGES = 2;
    public static final int FILTER_OCCURRENCE_EDGES = 3;
    
    
    private int filterType = FILTER_EDGES_WITH_TYPE;
    private Topic edgeType;
    private GraphFilter filter;
    
    
    
    public ReleaseEdges(int filterType, Topic edgeType, NodeFilter filter) {
        this(filterType, edgeType, (GraphFilter)filter);
    }
    
    /**
     */
    public ReleaseEdges(int filterType, Topic edgeType, GraphFilter filter) {
        this.setContext(new GraphEdgeContext());
        this.edgeType = edgeType;
        this.filterType = filterType;
        this.filter = filter;
    }
    
    
    

    @Override
    public String getName(){
        try {
            switch(filterType) {
                case FILTER_OCCURRENCE_EDGES: return "Release Occurrence edges";
                case FILTER_INSTANCE_EDGES: return "Release Class-Instance edges";
                case FILTER_EDGES_WITH_TYPE: {
                    if(edgeType != null) return "Release edges of type "+TopicToString.toString(edgeType);
                    else return "[No filtered edges]";
                }
            }
        }
        catch(Exception tme){
            tme.printStackTrace(); 
        }
        return "";
    }
    

    
    
    public void executeSynchronized(Wandora admin, Context context) {
        VModel model = solveModel(admin,context);
        switch(filterType) {
            case FILTER_OCCURRENCE_EDGES: { filter.setFilterOccurrences(false); break; }
            case FILTER_INSTANCE_EDGES: { filter.setFilterInstances(false); break; }
            default: {
                if(edgeType != null)
                    filter.releaseEdgeType(edgeType);
                break; 
            }
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    public static ArrayList<AbstractGraphTool> makeTools(Collection c, GraphFilter graphFilter, ArrayList<AbstractGraphTool> tools){
        if(tools==null) tools=new ArrayList<AbstractGraphTool>();
        ArrayList<Topic> types = new ArrayList<Topic>();

        for(Object o : graphFilter.getFilteredEdgeTypes()) {
            if(o != null) {
                if(o instanceof TopicNode) {
                    TopicNode tn=(TopicNode)o;
                    Topic t = tn.getTopic();
                    try {
                        if(!types.contains(t)) {
                            types.add(t);
                            tools.add(new ReleaseEdges(FILTER_EDGES_WITH_TYPE, t, graphFilter));
                        }
                    }
                    catch(Exception tme){tme.printStackTrace();}
                }
            }
        }
        
        if(graphFilter.getFilterInstances()) {
            tools.add(new ReleaseEdges(FILTER_INSTANCE_EDGES, null, graphFilter));
        }
        if(graphFilter.getFilterOccurrences()) {
            tools.add(new ReleaseEdges(FILTER_OCCURRENCE_EDGES, null, graphFilter));
        }
        
        if(tools.isEmpty()) {
            tools.add(new ReleaseEdges(FILTER_EDGES_WITH_TYPE, null, graphFilter));
        }
        
        
        return tools;
    }
    
}
