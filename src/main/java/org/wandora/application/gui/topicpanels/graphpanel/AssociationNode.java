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
 * AssociationNode.java
 *
 * Created on 6.6.2007, 11:48
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;


import java.util.*;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.topicmap.*;


/**
 *
 * @author olli
 */
public class AssociationNode extends AbstractNode  {
    
    private Association association;
    private TopicMapModel model;
    private Collection<Edge> edges;
    private Map<Node,Edge> edgeMap;
    
    /** Creates a new instance of AssociationNode */
    public AssociationNode(Association association, TopicMapModel model) throws TopicMapException {
        this.association=association;
        this.model=model;
        edgeMap=new LinkedHashMap<Node,Edge>();
        edges=new ArrayList<Edge>();
        for(Topic role : association.getRoles()){
            Node roleNode=model.getNodeFor(role);
            Node playerNode=model.getNodeFor(association.getPlayer(role));
            AssociationEdge e=new AssociationEdge(this,playerNode,role.getBaseName(),association,model);
            e.setBaseLength(25.0);
            edgeMap.put(roleNode,e);
            edges.add(e);
        }
    }
    
    public Association getAssociation(){
        return association;
    }
    
    @Override
    public double getMass() {
        return massMultiplier * defaultMass / 2.0;
    }

    @Override
    public String getLabel() {
        try {
            return TopicToString.toString(association.getType());
        }
        catch(Exception e) {
            return "-AN-";
        }
    }

    public Edge getEdgeForRole(Node role){
        return edgeMap.get(role);
    }
    
    public Collection<Edge> getEdges() {
        return edges;
    }
    
    @Override
    public boolean autoOpen(){
        return true;
    }
    
 
}
