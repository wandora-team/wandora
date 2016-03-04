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
 * InstanceEdge.java
 *
 * Created on 16. heinäkuuta 2007, 10:55
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;

import static org.wandora.utils.Tuples.*;
import java.awt.Color;
import org.wandora.topicmap.*;
import java.util.*;
import org.wandora.application.gui.topicstringify.TopicToString;


/**
 *
 * @author akivela
 */
public class OccurrenceEdge extends AbstractEdge {
    
    private TopicMapModel model;
    private T2<Node,Node> nodes;
    
    private T2<String,String> nodeLabels;
    
    private Topic carrier;
    private Topic type;
    private Topic scope;
    private String occurrence;
    
    private double baseLength=25.0;
    
    
    
    public OccurrenceEdge(Topic carrier, Topic type, Topic scope, String occurrence, TopicMapModel model) {
        this.model=model;
        this.carrier=carrier;
        this.type=type;
        this.scope=scope;
        this.occurrence=occurrence;
        nodeLabels=t2("Carrier","Occurrence");
    }
    
    
    public Topic getType() {
        return type;
    }
    
    public Topic getScope() {
        return scope;
    }
    
    public Topic getCarrier() {
        return carrier;
    }
    
    

    @Override
    public String getLabel() {
        try {
            return TopicToString.toString(type);
        }
        catch(Exception e) {};
        return "Carrier-Occurrence";
    }
    
    @Override
    public T2<String,String> getNodeLabels(){
        return nodeLabels;
    }
    
    @Override
    public T2<Node, Node> getNodes() {
        if(nodes == null) {
            nodes=t2((Node)model.getNodeFor(carrier),
                     (Node)model.getNodeFor(carrier, type, scope, occurrence));
        }
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
