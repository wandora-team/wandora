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
 * InstanceEdge.java
 *
 * Created on 16.7.2007, 10:55
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;

import static org.wandora.utils.Tuples.*;
import org.wandora.topicmap.*;



/**
 *
 * @author olli
 */
public class InstanceEdge extends AbstractEdge {
    
    private TopicMapModel model;
    private T2<Node,Node> nodes;
    
    private T2<String,String> nodeLabels;
    
    private Topic type;
    private Topic instance;
    
    private double baseLength=50.0;
    
    
    
    public InstanceEdge(Topic type, Topic instance, TopicMapModel model) {
        this.model=model;
        this.type=type;
        this.instance=instance;
        nodeLabels=t2("Class","Instance");
        nodes=t2((Node)model.getNodeFor(type),
                (Node)model.getNodeFor(instance));
    }
    
    public Topic getType(){
        return type;
    }
    public Topic getInstance(){
        return instance;
    }
    
    @Override
    public String getLabel(){
        return "Class-Instance";
    }
    
    @Override
    public T2<String,String> getNodeLabels(){
        return nodeLabels;
    }
    
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
