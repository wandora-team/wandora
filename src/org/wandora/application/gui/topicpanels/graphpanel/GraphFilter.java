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
 * GraphFilter.java
 *
 * Created on 6. kesäkuuta 2007, 15:51
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;



import org.wandora.topicmap.*;
import java.util.*;
/**
 *
 * @author olli, akivela
 */
public class GraphFilter implements NodeFilter, EdgeFilter {
    
    
    
    
    private HashSet<TopicNode> filteredNodes;
    private HashSet<TopicNode> filteredTypes;
    private boolean filterInstances;
    private boolean filterOccurrences;
    private HashSet<TopicNode> filteredEdgeTypes;

    private TopicMapModel model;
    
    
    /** Creates a new instance of GraphFilter */
    public GraphFilter(TopicMapModel tmModel) {
        this.model=tmModel;
        filteredNodes=new HashSet<TopicNode>();
        filteredTypes=new HashSet<TopicNode>();
        filteredEdgeTypes=new HashSet<TopicNode>();
        filterInstances=false;
        filterOccurrences=false;
    }
    
    
    
    
    public void setTopicMapModel(TopicMapModel newModel) {
        this.model=newModel;
    }
    public TopicMapModel getTopicMapModel() {
        return this.model;
    }

    
    
    public void filterNode(TopicNode tn){
        filteredNodes.add(tn);
    }
    public void filterNode(VNode vn){
        Node n=vn.getNode();
        if(n instanceof TopicNode) filterNode((TopicNode)n);
    }
    public void filterNode(Topic t){
        filterNode(model.getNodeFor(t));
    }
    
    
    
    public void filterNodesOfType(TopicNode type){
        filteredTypes.add(type);
    }
    public void filterNodesOfType(Topic type){
        filterNodesOfType(model.getNodeFor(type));
    }
    public void filterNodesOfType(VNode vn){
        Node n=vn.getNode();
        if(n instanceof TopicNode) filterNodesOfType((TopicNode)n);
    }
    public void releaseNodesOfType(TopicNode type){
        filteredTypes.remove(type);
    }
    public void releaseNodesOfType(Topic type){
        releaseNodesOfType(model.getNodeFor(type));
    }
    public void releaseNodesOfType(VNode vn){
        Node n=vn.getNode();
        if(n instanceof TopicNode) {
            releaseNodesOfType((TopicNode) n);
        }
    }
    public void releaseNode(TopicNode tn) {
        filteredNodes.remove(tn);
    }


    
    
    
    public void filterEdgeType(Topic type) {
        filterEdgeType(model.getNodeFor(type));
    }
    public void filterEdgeType(TopicNode type) {
        filteredEdgeTypes.add(type);
    }
    public void filterEdgeType(VNode vn) {
        Node n=vn.getNode();
        if(n instanceof TopicNode) filterEdgeType((TopicNode)n);        
    }
    
    public void releaseEdgeType(Topic type) {
        releaseEdgeType(model.getNodeFor(type));
    }
    public void releaseEdgeType(TopicNode type) {
        filteredEdgeTypes.remove(type);
    }
    public void releaseEdgeType(VNode vn) {
        Node n=vn.getNode();
        if(n instanceof TopicNode) releaseEdgeType((TopicNode)n);        
    }
    
    
    
    
    
    
    
    public Collection<TopicNode> getFilteredNodes(){
        return filteredNodes;
    }
    public Collection<TopicNode> getFilteredNodeTypes(){
        return filteredTypes;
    }
    public Collection<TopicNode> getFilteredEdgeTypes(){
        return filteredEdgeTypes;
    }
    
    
    public boolean getFilterInstances(){
        return filterInstances;
    }
    public void setFilterInstances(boolean b){        
        filterInstances=b;
    }
    
    public boolean getFilterOccurrences(){
        return filterOccurrences;
    }
    public void setFilterOccurrences(boolean b){        
        filterOccurrences=b;
    }

    
    public void clearNodeFilters() {
        filteredNodes = new HashSet<TopicNode>();
    }
    public void clearNodeTypeFilters() {
        filteredTypes = new HashSet<TopicNode>();
    }
    public void clearEdgeFilters() {
        filteredEdgeTypes = new HashSet<TopicNode>();
        filterInstances = false;
        filterOccurrences = false;
     }
   
    
    

    /* ---------------------------------------------------------------------- */
    
    
    
    
    
    public boolean isNodeFiltered(Node n) {
        if(n == null) return true;
        if(n instanceof OccurrenceNode) {
            return filterOccurrences;
        }
        else if(filteredNodes.contains(n)) {
            return true;
        }
        else if(n instanceof TopicNode && !filteredTypes.isEmpty()) {
            TopicNode tn=(TopicNode)n;
            try {
                for(Topic type : tn.getTopic().getTypes()) {
                    if(filteredTypes.contains(model.getNodeFor(type))) return true;
                }
            } 
            catch(TopicMapException tme){
                tme.printStackTrace();
            }
        }
        return false;
    }

    
    
    
    public boolean isEdgeFiltered(Edge e) {
        if(e == null) return true;
        if(e instanceof OccurrenceEdge) {
            return filterOccurrences;
        }
        else if(e instanceof AssociationEdge){
            AssociationEdge ae=(AssociationEdge)e;
            Association a=ae.getAssociation();
            if(a!=null) {
                try {
                    if(filteredEdgeTypes.contains(model.getNodeFor(a.getType()))) return true;
                }
                catch(TopicMapException tme){
                    tme.printStackTrace();
                }
            }
        }
        else if(e instanceof InstanceEdge){
            return filterInstances;
        }
        return false;
    }
    
    
    
    /* ---------------------------------------------------------------------- */
    
    public String describeFilters() {
        StringBuilder sb = new StringBuilder("");
        
        if(!filteredNodes.isEmpty()) {
            sb.append( "Filtered nodes\n" );
            for(TopicNode n : filteredNodes) {
                sb.append(n.getLabel()).append("\n");
            }
            sb.append( "\n");
        }
        else {
            sb.append("No nodes filtered\n");
        }
        
        if(!filteredTypes.isEmpty()) {
            sb.append( "\nFiltered nodes typed as\n");
            for(TopicNode n : filteredTypes) {
                sb.append(n.getLabel()).append( "\n");
            }
            sb.append( "\n");
        }
        else {
            sb.append("No node types filtered\n");
        }
        
        if(!filteredEdgeTypes.isEmpty() || filterInstances || filterOccurrences) {
            sb.append( "\nFiltered edges typed as\n");
            if(filterInstances) {
                sb.append( "Class-Instance\n");
            }
            if(filterOccurrences) {
                sb.append( "Occurrence\n");
            }
            for(TopicNode n : filteredEdgeTypes) {
                sb.append(n.getLabel()).append( "\n");
            }
            sb.append( "\n");
        }
        else {
            sb.append("No edge types filtered\n");
        }
        
        return sb.toString();        
    }
    
    

}
