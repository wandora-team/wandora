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
 * TopicNode.java
 *
 * Created on 5.6.2007, 15:18
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;

import java.awt.Color;
import java.util.*;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.topicmap.*;
import static org.wandora.utils.Tuples.*;

/**
 *
 * @author olli
 */

public class TopicNode extends AbstractNode {
    
    private Topic topic;
    private TopicMapModel model;
    private Collection<Edge> edges;

    public static double edgeMassFactor = 10.0;

    
    /** Creates a new instance of TopicNode */
    public TopicNode(Topic topic, TopicMapModel model) {
        this.topic=topic;
        this.model=model;
    }



    @Override
    public double getMass() {
        if(edges!=null) return massMultiplier * (defaultMass+edges.size()*edgeMassFactor);
        else return massMultiplier * defaultMass;
    }

    
    @Override
    public String getLabel() {
        return TopicToString.toString(topic);
    }
    
    
    public Topic getTopic(){
        return topic;
    }

    
    @Override
    public Collection<Edge> getEdges() {
        if(edges==null) {
            edges=new ArrayList<Edge>();
            try{
                for(Association a : topic.getAssociations()) {
                    if(a.getRoles().size()==2) edges.add(model.getEdgeFor(a));
                    else {
                        AssociationNode n=model.getNodeFor(a);
                        for(Edge e : n.getEdges()){
                            T2<Node,Node> ns=e.getNodes();
                            if(ns.e1.equals(this)) edges.add(e);
                            else if(ns.e2.equals(this)) edges.add(e);
                        }
                    }
                }
                for(Topic type : topic.getTypes()) {
                    edges.add(model.getInstanceEdgeFor(type,topic));
                }
                for(Topic instance : topic.getTopicMap().getTopicsOfType(topic)) {
                    edges.add(model.getInstanceEdgeFor(topic,instance));
                }
                for(Topic occurrenceType : topic.getDataTypes()) {
                    Hashtable<Topic,String> occurrences = topic.getData(occurrenceType);
                    for(Topic occurrenceScope : occurrences.keySet()) {
                        String occurrence = occurrences.get(occurrenceScope);
                        edges.add(model.getOccurrenceEdgeFor(topic,occurrenceType,occurrenceScope,occurrence));
                    }
                }
            }
            catch(TopicMapException tme){
                tme.printStackTrace();
            }
        }
        return edges;
    }
    
    
    @Override
    public String toString(){
        return getLabel();
    }
}
