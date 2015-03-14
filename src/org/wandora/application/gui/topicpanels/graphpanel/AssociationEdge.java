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
 * TopicMapEdge.java
 *
 * Created on 5. kesäkuuta 2007, 15:18
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;

import static org.wandora.utils.Tuples.*;
import org.wandora.topicmap.*;
import java.util.*;
import org.wandora.application.gui.topicstringify.TopicToString;

/**
 *
 * @author olli
 */
public class AssociationEdge extends AbstractEdge {
    
    private Association association;
    private TopicMapModel model;
    private T2<Node,Node> nodes;
    
    private T2<String,String> nodeLabels;
    private T2<Topic,Topic> nodeLabelTopics;
    
    private double baseLength=50.0;
    
    
    /** Creates a new instance of AssociationEdge */
    public AssociationEdge(Association association, TopicMapModel model) {
        this.association=association;
        this.model=model;
        try {
            Iterator<Topic> iter = association.getRoles().iterator();
            Topic role1 = iter.next();
            Topic role2 = iter.next();
            nodeLabelTopics = t2(role1,role2);
            nodeLabels = null;
            nodes = t2((Node)model.getNodeFor(association.getPlayer(role1)),
                     (Node)model.getNodeFor(association.getPlayer(role2)));
        }
        catch(TopicMapException tme){
            tme.printStackTrace();
        }
    }
    
    
    public AssociationEdge(Node n1,Node n2,String n2Label,Association association,TopicMapModel model){
        this.model = model;
        this.association = association;
        nodes = t2(n1,n2);
        if(n2Label != null) {
            nodeLabels = t2(null, n2Label);
            nodeLabelTopics = null;
        }
    }
    
    
    
    @Override
    public String getLabel(){
        try {
            return TopicToString.toString(association.getType());
        }
        catch(Exception tme) {
            tme.printStackTrace();
            return null;
        }
    }
    
    
    @Override
    public T2<String,String> getNodeLabels() {
        if(nodeLabels != null) {
            return nodeLabels;
        }
        else if(nodeLabelTopics != null) {
            return t2(TopicToString.toString(nodeLabelTopics.e1),TopicToString.toString(nodeLabelTopics.e2));
        }
        else {
            return t2( "unknown", "unknown" );
        }
    }
    
    
    public Association getAssociation(){
        return association;
    }

    
    @Override
    public T2<Node, Node> getNodes() {
        return nodes;
    }
    
    
    public void setBaseLength(double l){
        baseLength=l;
    }

    
    @Override
    public double getLength() {
        return baseLength+(nodes.e1.getMass()+nodes.e2.getMass())/4.0;
    }


}
